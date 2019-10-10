package com.feipulai.host.activity.person;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.host.R;
import com.feipulai.host.activity.setting.LEDSettingActivity;
import com.feipulai.host.activity.base.BaseCheckActivity;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.person.adapter.BasePersonTestResultAdapter;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.netapi.ItemSubscriber;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.feipulai.host.view.StuSearchEditText;
import com.orhanobut.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 个人测试基类
 */
public abstract class BasePersonTestActivity extends BaseCheckActivity {
    @BindView(R.id.et_input_text)
    StuSearchEditText etInputText;
    @BindView(R.id.btn_scan)
    TextView btnScan;
    @BindView(R.id.lv_results)
    ListView lvResults;
    @BindView(R.id.img_portrait)
    ImageView imgPortrait;
    @BindView(R.id.txt_stu_code)
    TextView txtStuCode;
    @BindView(R.id.txt_stu_name)
    TextView txtStuName;
    @BindView(R.id.txt_stu_sex)
    TextView txtStuSex;
    @BindView(R.id.rv_test_result)
    RecyclerView rvTestResult;
    @BindView(R.id.cb_device_state)
    public CheckBox cbDeviceState;
    @BindView(R.id.ll_state)
    public LinearLayout llState;
    @BindView(R.id.txt_test_result)
    TextView txtStuResult;
    @BindView(R.id.txt_start_test)
    TextView txtStartTest;
    @BindView(R.id.tv_base_height)
    TextView tvBaseHeight;
    @BindView(R.id.txt_stu_skip)
    TextView txtStuSkip;
    @BindView(R.id.txt_led_setting)
    public TextView txtLedSetting;
    @BindView(R.id.view_skip)
    LinearLayout viewSkip;
    @BindView(R.id.tv_device_pair)
    public TextView tvDevicePair;
    //成绩
    private String[] result;
    private List<String> resultList = new ArrayList<>();
    private BasePersonTestResultAdapter adapter;
    /**
     * 当前设备
     */
    public BaseStuPair pair = new BaseStuPair();
    /**
     * 当前测试次数位
     */
    private int testNo = 1;
    private int roundNo = 1;
    private LEDManager mLEDManager;
    //清理学生信息
    private ClearHandler clearHandler = new ClearHandler(this);
    //    private LedHandler ledHandler = new LedHandler(this);
    private int testType = 0;//0自动 1手动

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_base_person_test;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        PrinterManager.getInstance().init();


    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = String.format(getString(R.string.host_name), TestConfigs.machineNameMap.get(machineCode), SettingHelper.getSystemSetting().getHostId());
        } else {
            title = String.format(getString(R.string.host_name), TestConfigs.machineNameMap.get(machineCode), SettingHelper.getSystemSetting().getHostId())
                    + "-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title).addRightText(R.string.item_setting_title, new View.OnClickListener() {
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

    private void init() {
        mLEDManager = new LEDManager();
        mLEDManager.link(TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));

        PrinterManager.getInstance().init();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        rvTestResult.setLayoutManager(layoutManager);
        result = new String[1];

        //创建适配器
        resultList.addAll(Arrays.asList(result));
        adapter = new BasePersonTestResultAdapter(resultList);
        //给RecyclerView设置适配器
        rvTestResult.setAdapter(adapter);
        etInputText.setData(lvResults, this);

        pair.setBaseDevice(new BaseDeviceState(BaseDeviceState.STATE_FREE));
        refreshDevice();

    }

    public void setTestType(int testType) {
        this.testType = testType;
        if (this.testType == 1) {
            txtStartTest.setVisibility(View.VISIBLE);
        }
    }

    public void setBegin(int isBegin) {
        txtStartTest.setText(isBegin == 0 ? R.string.stop_test : R.string.start_test);
    }

    private void refreshDevice() {
        if (pair.getBaseDevice() != null) {
            if (pair.getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
                cbDeviceState.setChecked(true);
            } else {
                cbDeviceState.setChecked(false);
            }
        }
    }


    /**
     * 发送测试指令 并且此时应将设备状态改变
     */
    public abstract void sendTestCommand(BaseStuPair baseStuPair);


    /**
     * 跳转项目设置页面
     */
    public abstract void gotoItemSetting();

    /**
     * 跳过
     */
    public abstract void stuSkip();


    @Override
    public void onCheckIn(Student student) {

        result = new String[1];
        resultList.clear();
        resultList.addAll(Arrays.asList(result));
        adapter.notifyDataSetChanged();
        addStudent(student);
    }

    @OnClick({R.id.txt_stu_skip, R.id.txt_start_test, R.id.txt_led_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.txt_led_setting:
                toLedSetting();
                break;
            case R.id.txt_stu_skip:
                if (pair.getStudent() != null) {
                    stuSkipDialog();
                }
                break;
            case R.id.txt_start_test:

                if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN || pair.getBaseDevice().getState() == BaseDeviceState.STATE_FREE) {
                    sendTestCommand(pair);
                }

                break;
        }
    }


    public void toLedSetting() {
        if (pair.getBaseDevice().getState() != BaseDeviceState.STATE_NOT_BEGAIN
                && pair.getBaseDevice().getState() != BaseDeviceState.STATE_FREE
                && pair.getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
            toastSpeak(getString(R.string.testing_no_use));
            return;
        }
        startActivity(new Intent(this, LEDSettingActivity.class));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        PrinterManager.getInstance().close();
        if (TestConfigs.sCurrentItem != null) {
            mLEDManager.link(TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
            String title = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()) + " " + SettingHelper.getSystemSetting().getHostId();
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), title, mLEDManager.getX(title), 0, true, false);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), getString(R.string.fairplay), 3, 3, false, true);
            mLEDManager = null;
        }


    }

    private void addStudent(Student student) {

        if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN || pair.getBaseDevice().getState() == BaseDeviceState.STATE_FREE) {
//            roundNo = 1;
            pair.setResult(0);
            pair.setStudent(student);
            refreshTxtStu(student);
            txtStuResult.setText("");
            toastSpeak(String.format(getString(R.string.test_speak_hint), pair.getStudent().getSpeakStuName(), roundNo)
                    , String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), roundNo));
            if (testType == 0) {
                sendTestCommand(pair);
            }
            setShowLed(pair);

            Logger.i("addStudent:" + student.toString());
            Logger.i("addStudent:当前考生进行第" + testNo + "次的第" + roundNo + "轮测试");
        } else {
            toastSpeak(getString(R.string.no_device_add_test_hint));
        }

    }

    /**
     * 加载学生信息
     */
    private void refreshTxtStu(@NonNull Student student) {
        if (student != null) {
            txtStuName.setText(student.getStudentName());
            txtStuSex.setText((student.getSex() == Student.MALE ? R.string.male : R.string.female));
            txtStuCode.setText(student.getStudentCode());

        } else {
            txtStuName.setText("");
            txtStuSex.setText("");
            txtStuCode.setText("");
            txtStuResult.setText("");

        }
    }


    private void stuSkipDialog() {
        new SweetAlertDialog(this)
                .setTitleText(String.format(getString(R.string.dialog_skip_stu_title), pair.getStudent().getStudentName()))
                .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                Logger.i("stuSkip:" + pair.getStudent().toString());
                //测试结束学生清除 ，设备设置空闲状态
                roundNo = 1;
                clearHandler.sendEmptyMessageDelayed(0, 0);
                stuSkip();
                mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));

            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();

            }
        }).show();

    }

    /**
     * 修改已添加设备状态，设备状态为STATE_END判定为测试结束，可进行成绩打印、播报、保存
     *
     * @param deviceState
     */
    public void updateDevice(@NonNull BaseDeviceState deviceState) {
        Logger.i("updateDevice==>" + deviceState.toString());
        if (pair.getBaseDevice() != null) {
            pair.getBaseDevice().setState(deviceState.getState());
            //状态为测试已结束
            if (deviceState.getState() == BaseDeviceState.STATE_END) {
                if (pair.getStudent() != null) {
                    Logger.i("考生" + pair.getStudent().toString());
                }
                Logger.i("设备成绩信息STATE_END==>" + deviceState.toString());
                //ResultDisplayUtils.getStrResultForDisplay(pair.getResult())
                result[roundNo - 1] = ((pair.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(pair.getResult()));
                resultList.clear();
                resultList.addAll(Arrays.asList(result));
                adapter.notifyDataSetChanged();
                pair.setTimeResult(result);
                //保存成绩
                saveResult(pair);
                printResult(pair);
                broadResult(pair);
                roundNo = 1;
                clearHandler.sendEmptyMessageDelayed(0, 4000);
                //当前的测试次数是否在项目设置的轮次中，是否满分跳过考生测试，满分由子类处理，基类只做界面展示
//                if (roundNo < 1) {
//                    if (pair.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
//                        //测试结束学生清除 ，设备设置空闲状态
//                        roundNo = 1;
//                        //4秒后清理学生信息
//                        clearHandler.sendEmptyMessageDelayed(0, 4000);
//                        return;
//                    }
//                    roundNo++;
//                    txtStuResult.setText("");
//                    toastSpeak(String.format(getString(R.string.test_speak_hint), pair.getStudent().getSpeakStuName(), roundNo)
//                            , String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), roundNo));
//                    Message msg = new Message();
//                    msg.obj = pair;
//                    ledHandler.sendMessageDelayed(msg, 2000);
//                    pair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
//                    if (testType != 1) {
//                        sendTestCommand(pair);
//                    }
//
//                } else {
//                    //测试结束学生清除 ，设备设置空闲状态
//                    roundNo = 1;
//                    //4秒后清理学生信息
//                    clearHandler.sendEmptyMessageDelayed(0, 4000);
//
//                }


            }
        }
        refreshDevice();

    }


    /**
     * 保存测试成绩
     *
     * @param baseStuPair 当前设备
     */
    private void saveResult(@NonNull BaseStuPair baseStuPair) {
        Logger.i("saveResult==>" + baseStuPair.toString());
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(baseStuPair.getStudent().getStudentCode());
        String itemCode = TestConfigs.sCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : TestConfigs.sCurrentItem.getItemCode();
        roundResult.setItemCode(itemCode);
        roundResult.setResult(baseStuPair.getResult());
        roundResult.setResultState(baseStuPair.getResultState());
        roundResult.setTestTime(DateUtil.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
        roundResult.setRoundNo(1);
        RoundResult bestResult = DBManager.getInstance().queryBestScore(baseStuPair.getStudent().getStudentCode());
        if (bestResult != null) {
            // 原有最好成绩犯规 或者原有最好成绩没有犯规但是现在成绩更好
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= baseStuPair.getResult()) {
                // 这个时候就要同时修改这两个成绩了
                roundResult.setIsLastResult(1);
                bestResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(bestResult);
            } else {
                roundResult.setIsLastResult(0);
            }
        } else {
            // 第一次测试
            roundResult.setIsLastResult(1);
        }

        DBManager.getInstance().insertRoundResult(roundResult);


        //成绩上传判断成绩类型获取最后成绩
        if (TestConfigs.sCurrentItem.getfResultType() == 0) {
            //最好
            if (bestResult != null && bestResult.getIsLastResult() == 1)
                uploadResult(roundResult, bestResult);
            else
                uploadResult(roundResult, roundResult);
        } else {
            //最后
            uploadResult(roundResult, roundResult);
        }

    }

    /**
     * 成绩上传
     *
     * @param roundResult 当前成绩
     * @param lastResult  最后成绩
     */
    private void uploadResult(RoundResult roundResult, RoundResult lastResult) {
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort(R.string.upload_result_hint);
            return;
        }

//        new RequestBiz().setDataUpLoad(roundResult, lastResult);
        new ItemSubscriber().setDataUpLoad(roundResult, lastResult);

    }

    /**
     * 更新学生成绩
     *
     * @param baseStu
     */
    public synchronized void updateResult(@NonNull BaseStuPair baseStu) {
        if (null != pair.getBaseDevice()) {
            pair.setResultState(baseStu.getResultState());
            pair.setResult(baseStu.getResult());
            txtStuResult.setText(((baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult())));
            refreshDevice();
            updateResultLed(((baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult())));
        }
    }


    /**
     * 播报结果
     */
    private void broadResult(@NonNull BaseStuPair baseStuPair) {
        if (SettingHelper.getSystemSetting().isAutoBroadcast()) {
            if (baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) {
                TtsManager.getInstance().speak(String.format(getString(R.string.speak_foul), baseStuPair.getStudent().getSpeakStuName()));
            } else {

                TtsManager.getInstance().speak(String.format(getString(R.string.speak_result), baseStuPair.getStudent().getSpeakStuName(), ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult())));
            }


        }
    }


    /**
     * LED屏显示
     *
     * @param stuPair
     */
    private void setShowLed(BaseStuPair stuPair) {
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), stuPair.getStudent().getStudentName(), mLEDManager.getX(stuPair.getStudent().getLEDStuName()), 0, true, false);
        if (stuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) {

            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), String.format(getString(R.string.speak_foul), stuPair.getStudent().getSpeakStuName())
                    , mLEDManager.getX(String.format(getString(R.string.speak_foul), stuPair.getStudent().getSpeakStuName())), 2, false, true);

        } else {
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), ResultDisplayUtils.getStrResultForDisplay(stuPair.getResult())
                    , mLEDManager.getX(ResultDisplayUtils.getStrResultForDisplay(stuPair.getResult())), 2, false, true);

        }

    }


    private void updateResultLed(String result) {

        byte[] data = new byte[16];
        String str = "当前：";
        try {
            byte[] strData = str.getBytes("GB2312");
            System.arraycopy(strData, 0, data, 0, strData.length);
            byte[] resultData = result.getBytes("GB2312");
            System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 1, false, true);
    }


    private void printResult(@NonNull BaseStuPair baseStuPair) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        Student student = baseStuPair.getStudent();
        PrinterManager.getInstance().print(" \n");
        PrinterManager.getInstance().print(
                String.format(getString(R.string.host_name), TestConfigs.sCurrentItem.getItemName(), SettingHelper.getSystemSetting().getHostId()) + "\n");
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_stu_code), student.getStudentCode()) + "\n");
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_stu_name), student.getStudentName()) + "\n");
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_stu_result), (baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) ?
                        getString(R.string.foul) : ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult())) + "\n");
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_time), TestConfigs.df.format(Calendar.getInstance().getTime())) + "\n");
        PrinterManager.getInstance().print(" \n");
        PrinterManager.getInstance().print(" \n");

    }


    private static class LedHandler extends Handler {

        private WeakReference<BasePersonTestActivity> mActivityWeakReference;

        public LedHandler(BasePersonTestActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BasePersonTestActivity activity = mActivityWeakReference.get();
            activity.setShowLed((BaseStuPair) msg.obj);

        }

    }

    /**
     * i清理学生信息
     */
    private static class ClearHandler extends Handler {

        private WeakReference<BasePersonTestActivity> mActivityWeakReference;

        public ClearHandler(BasePersonTestActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BasePersonTestActivity activity = mActivityWeakReference.get();
            Logger.i("ClearHandler:清理学生信息");
            if (activity != null) {
                activity.pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                activity.pair.setStudent(null);
                activity.refreshTxtStu(null);
                activity.result = null;
                activity.result = new String[1];
                activity.resultList.clear();
                activity.resultList.addAll(Arrays.asList(activity.result));
                activity.adapter.notifyDataSetChanged();
            }

        }
    }
}
