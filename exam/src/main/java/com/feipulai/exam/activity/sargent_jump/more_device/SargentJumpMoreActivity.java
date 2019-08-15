package com.feipulai.exam.activity.sargent_jump.more_device;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.adapter.DeviceListAdapter;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.service.UploadService;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;

public abstract class SargentJumpMoreActivity extends BaseCheckActivity {

    @BindView(R.id.rv_device_list)
    RecyclerView rvDeviceList;
    private List<DeviceDetail> deviceDetails = new ArrayList<>();
    private int deviceCount;
    private int testNo;
    private DeviceListAdapter deviceListAdapter;
    private int MAX_COUNT = 4;
    private LEDManager mLEDManager;
    private Intent serverIntent;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_sargent_jump_more;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        if (SettingHelper.getSystemSetting().isRtUpload()) {
            serverIntent = new Intent(this, UploadService.class);
            startService(serverIntent);
        }

    }

    private void init() {
        mLEDManager = new LEDManager();
        mLEDManager.link(TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
        PrinterManager.getInstance().init();
    }

    @Override
    public void onCheckIn(Student student) {
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        List<RoundResult> roundResultList = DBManager.getInstance().queryFinallyRountScoreByExamTypeList(student.getStudentCode(), studentItem.getExamType());
        testNo = roundResultList == null || roundResultList.size() == 0 ? 1 : roundResultList.get(0).getTestNo();
        //保存成绩，并测试轮次大于测试轮次次数
        if (roundResultList != null && roundResultList.size() >= setTestCount()) {
            //已测试，不重测
//            roundNo = roundResult.getRoundNo();
//            selectTestDialog(student);
            toastSpeak("该考生已测试完成");
            return;
        } else if (roundResultList != null) {
            for (RoundResult roundResult : roundResultList) {
                if (roundResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && isResultFullReturn(student.getSex(), roundResult.getResult())) {
                    toastSpeak("满分");
                    return;
                }

            }
        }

        //是否有成绩，没有成绩查底该项目是否有成绩，没有成绩测试次数为1，有成绩测试次数+1
        if (roundResultList.size() == 0) {
            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
            testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1;
        } else {
            testNo = roundResultList.get(0).getTestNo();
        }

        int index = 0;
        boolean canUseDevice = false;
        for (int i = 0; i < deviceCount; i++) {
            int state = deviceDetails.get(i).getStuDevicePair().getBaseDevice().getState();
            if (deviceDetails.get(i).isDeviceOpen() && state == BaseDeviceState.STATE_FREE) {
                index = i;
                canUseDevice = true;
                break;
            }
        }

        if (!canUseDevice) {
            toastSpeak("当前无设备可添加学生测试");
            return;
        }

        String[] result = new String[setTestCount()];
        for (int i = 0; i < roundResultList.size(); i++) {
            if (i < setTestCount()) {
                if (roundResultList.get(i).getResultState() == RoundResult.RESULT_STATE_FOUL) {
                    result[i] = "X";
                } else {
                    result[i] = ResultDisplayUtils.getStrResultForDisplay(roundResultList.get(i).getResult());
                }

            }
        }
        deviceDetails.get(index).setRound(roundResultList.size());
        addStudent(student, index);
        deviceDetails.get(index).getStuDevicePair().setTimeResult(result);
        deviceListAdapter.notifyItemChanged(index);

    }

    /**
     * 将考生与设备绑定
     *
     * @param student
     */
    private void addStudent(Student student, int index) {
        DeviceDetail deviceDetail = deviceDetails.get(index);
        deviceDetail.getStuDevicePair().setStudent(student);

        String[] timeResult = deviceDetail.getStuDevicePair().getTimeResult();
        int count = timeResult.length;
        toastSpeak(String.format(getString(R.string.test_speak_hint), student.getStudentName(), count + 1)
                , String.format(getString(R.string.test_speak_hint), student.getStudentName(), count + 1));
        //TODO LED 展示
        showLed(student);
        sendTestCommand(deviceDetails.get(index).getStuDevicePair(), index);
    }

    /**
     * led 显示 TODO
     *
     * @param student
     */
    private void showLed(Student student) {

    }


    @Override
    protected void initData() {
        for (int i = 0; i < MAX_COUNT; i++) {
            DeviceDetail detail = new DeviceDetail();
            detail.getStuDevicePair().getBaseDevice().setDeviceId(i+1);
            deviceDetails.add(detail);
        }
        initView();

    }

    private void initView() {
        deviceListAdapter = new DeviceListAdapter(deviceDetails);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        rvDeviceList.setLayoutManager(layoutManager);
        rvDeviceList.setAdapter(deviceListAdapter);

        deviceListAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int pos) {
                BaseStuPair pair = deviceDetails.get(pos).getStuDevicePair();
                switch (view.getId()) {
                    case R.id.txt_start:
                        if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN || pair.getBaseDevice().getState() == BaseDeviceState.STATE_FREE) {
                            sendTestCommand(pair, pos);
                        }
                        break;
                    case R.id.txt_skip:
                        if (pair.getStudent() != null) {
                            stuSkipDialog(pair.getStudent());
                        }
                        break;
                    default:
                        break;
                }


            }
        });
    }


    protected void stuSkipDialog(final Student student) {
        new AlertDialog.Builder(this).setMessage("是否跳过" + student.getStudentName() + "考生测试")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Logger.i("stuSkip:" + student.toString());
                        //测试结束学生清除 ，设备设置空闲状态

//                        clearHandler.sendEmptyMessageDelayed(0, 0);
                        stuSkip();
                        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
                    }
                }).setNegativeButton("取消", null).show();
    }


    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机";
        } else {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title).addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }).addRightText("项目设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        });
    }

    public synchronized void updateDevice(@NonNull BaseDeviceState deviceState) {
        Logger.i("updateDevice==>" + deviceState.toString());
        int deviceId = deviceState.getDeviceId();
        BaseStuPair pair = null;
        int index = 0;
        for (int i = 0; i < deviceCount; i++) {
            int id = deviceDetails.get(i).getStuDevicePair().getBaseDevice().getDeviceId();
            if (id == deviceId) {
                pair = deviceDetails.get(i).getStuDevicePair();
                index = i;
                break;
            }

        }

        if (pair.getBaseDevice() != null) {
            pair.getBaseDevice().setState(deviceState.getState());
            //状态为测试已结束
            if (deviceState.getState() == BaseDeviceState.STATE_END) {
                if (pair.getStudent() != null) {
                    Logger.i("考生" + pair.getStudent().toString());
                }
                Logger.i("设备成绩信息STATE_END==>" + deviceState.toString());

                doResult(pair, index);
            }
        }
        refreshDevice(index);
    }

    /**
     * 处理结果
     */
    private synchronized void doResult(BaseStuPair pair, int index) {
        DeviceDetail detail = deviceDetails.get(index);
        String[] timeResult = detail.getStuDevicePair().getTimeResult();
        //设置设备成绩
        timeResult[detail.getRound()] = ((pair.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" :
                ResultDisplayUtils.getStrResultForDisplay(pair.getResult()));
        detail.getStuDevicePair().setTimeResult(timeResult);

        //保存成绩
        saveResult(pair);
        printResult(pair);
        broadResult(pair);
        //todo


    }

    private void broadResult(BaseStuPair baseStuPair) {
        if (SettingHelper.getSystemSetting().isAutoBroadcast()) {
            if (baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) {
                TtsManager.getInstance().speak(baseStuPair.getStudent().getSpeakStuName() + "犯规");
            } else {

                TtsManager.getInstance().speak(String.format(getString(R.string.speak_result), baseStuPair.getStudent().getSpeakStuName(), ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult())));
            }


        }
    }

    private void printResult(BaseStuPair baseStuPair) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        //是否已全部次数测试完成，非满分跳过
        if (baseStuPair.getTimeResult().length < setTestCount() && !baseStuPair.isFullMark()) {
            return;
        }
        Student student = baseStuPair.getStudent();
//        PrinterManager.getInstance().print("\n");
        PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "号机");
        PrinterManager.getInstance().print("考  号:" + student.getStudentCode());
        PrinterManager.getInstance().print("姓  名:" + student.getStudentName());
        for (int i = 0; i < baseStuPair.getTimeResult().length; i++) {
            if (!TextUtils.isEmpty(baseStuPair.getTimeResult()[i])) {
                PrinterManager.getInstance().print(String.format("第 %1$d 次：", i + 1) + baseStuPair.getTimeResult()[i]);
            } else {
                PrinterManager.getInstance().print(String.format("第%1$d次：", i + 1));
            }
        }
        PrinterManager.getInstance().print("打印时间:" + TestConfigs.df.format(Calendar.getInstance().getTime()));
        PrinterManager.getInstance().print("\n");
    }

    private void saveResult(BaseStuPair baseStuPair) {
        Logger.i("saveResult==>" + baseStuPair.toString());
        if (baseStuPair.getStudent() == null)
            return;
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(baseStuPair.getStudent().getStudentCode());
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(baseStuPair.getStudent().getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(baseStuPair.getResult());
        roundResult.setMachineResult(baseStuPair.getResult());
        roundResult.setResultState(baseStuPair.getResultState());
//        roundResult.setTestTime(TestConfigs.df.format(Calendar.getInstance().getTime()));
        roundResult.setTestTime(System.currentTimeMillis() + "");
        roundResult.setRoundNo(baseStuPair.getTimeResult().length);
        roundResult.setTestNo(testNo);
        roundResult.setExamType(studentItem.getExamType());
        roundResult.setScheduleNo(studentItem.getScheduleNo());
        roundResult.setUpdateState(0);
        RoundResult bestResult = DBManager.getInstance().queryBestScore(baseStuPair.getStudent().getStudentCode(), testNo);
        if (bestResult != null) {
            // 原有最好成绩犯规 或者原有最好成绩没有犯规但是现在成绩更好
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && baseStuPair.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= baseStuPair.getResult()) {
                // 这个时候就要同时修改这两个成绩了
                roundResult.setIsLastResult(1);
                bestResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(bestResult);
                updateLastResultLed(roundResult);
            } else {
                if (bestResult.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                    roundResult.setIsLastResult(1);
                    bestResult.setIsLastResult(0);
                    DBManager.getInstance().updateRoundResult(bestResult);
                    updateLastResultLed(roundResult);
                } else {
                    roundResult.setIsLastResult(0);
                    updateLastResultLed(bestResult);
                }
            }
        } else {
            // 第一次测试
            roundResult.setIsLastResult(1);
            updateLastResultLed(roundResult);
        }


        DBManager.getInstance().insertRoundResult(roundResult);
        Logger.i("saveResult==>insertRoundResult->" + roundResult.toString());
        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        UploadResults uploadResults = new UploadResults(studentItem.getScheduleNo(), TestConfigs.getCurrentItemCode(),
                baseStuPair.getStudent().getStudentCode(), testNo + "", "", RoundResultBean.beanCope(roundResultList));


        uploadResult(uploadResults);
    }

    /**
     * 成绩上传
     *
     * @param uploadResults 上传成绩
     */
    private void uploadResult(UploadResults uploadResults) {
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort("自动上传成绩需下载更新项目信息");
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable(UploadResults.BEAN_KEY, uploadResults);
        serverIntent.putExtras(bundle);
        startService(serverIntent);
    }

    /**
     * LED结果展示
     */
    private void updateLastResultLed(RoundResult roundResult) {

    }


    /**
     * 更新学生成绩
     *
     * @param baseStu
     */
    public synchronized void updateResult(@NonNull BaseStuPair baseStu) {
        int deviceId = baseStu.getBaseDevice().getDeviceId();
        BaseStuPair pair = null;
        int index = 0;
        for (int i = 0; i < deviceCount; i++) {
            int id = deviceDetails.get(i).getStuDevicePair().getBaseDevice().getDeviceId();
            if (id == deviceId) {
                pair = deviceDetails.get(i).getStuDevicePair();
                index = i;
                break;
            }

        }
        if (null != pair.getBaseDevice()) {
            pair.setResultState(baseStu.getResultState());
            pair.setResult(baseStu.getResult());
            pair.setFullMark(baseStu.isFullMark());

            refreshDevice(index);

            //TODO LED
            deviceListAdapter.notifyItemChanged(index);
        }
    }

    private void refreshDevice(int index) {
        if (deviceDetails.get(index).getStuDevicePair().getBaseDevice() != null) {
            deviceListAdapter.notifyItemChanged(index);
        }
    }


    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
        for (int i = 0; i < deviceCount; i++) {

        }
    }

    /**
     * 设置项目测试轮次次数
     */
    public abstract int setTestCount();

    public abstract boolean isResultFullReturn(int sex, int result);

    /**
     * 跳转项目设置页面
     */
    public abstract void gotoItemSetting();

    protected abstract void sendTestCommand(BaseStuPair pair, int index);

    protected abstract void stuSkip();


}
