package com.feipulai.exam.activity.sargent_jump.more_device;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.AgainTestDialog;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.base.ResitDialog;
import com.feipulai.exam.activity.data.DataDisplayActivity;
import com.feipulai.exam.activity.data.DataRetrieveActivity;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.adapter.DeviceListAdapter;
import com.feipulai.exam.activity.sargent_jump.pair.SargentPairActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.bean.DataRetrieveBean;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.service.UploadService;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.view.EditResultDialog;
import com.feipulai.exam.view.StuSearchEditText;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public abstract class BaseMoreActivity extends BaseCheckActivity {

    @BindView(R.id.rv_device_list)
    RecyclerView rvDeviceList;
    public List<DeviceDetail> deviceDetails = new ArrayList<>();
    @BindView(R.id.et_input_text)
    StuSearchEditText etInputText;
    @BindView(R.id.lv_results)
    ListView lvResults;
    private int deviceCount = 1;
    private int testNo;
    private DeviceListAdapter deviceListAdapter;
    private LEDManager mLEDManager;
    private Intent serverIntent;
    private ClearHandler clearHandler = new ClearHandler();
    private LedHandler ledHandler = new LedHandler();
    private boolean isPenalize;
    private boolean isNextClickStart = true;
    private EditResultDialog editResultDialog;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_sargent_jump_more;
    }

    @Override
    protected void initData() {
        super.initData();
        RadioChannelCommand command = new RadioChannelCommand(SettingHelper.getSystemSetting().getUseChannel());
        LogUtils.normal(command.getCommand().length + "---" + StringUtility.bytesToHexString(command.getCommand()) + "---切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command));
        init();
        if (SettingHelper.getSystemSetting().isRtUpload()) {
            serverIntent = new Intent(this, UploadService.class);
            startService(serverIntent);
        }
        editResultDialog = new EditResultDialog(this);
        editResultDialog.setListener(new EditResultDialog.OnInputResultListener() {

            @Override
            public void inputResult(String result, int state) {
                BaseStuPair pair = deviceDetails.get(0).getStuDevicePair();
                pair.setTestTime(System.currentTimeMillis() + "");
                pair.setResultState(state);
                pair.setResult(ResultDisplayUtils.getDbResultForUnit(Double.valueOf(result)));
                deviceListAdapter.notifyDataSetChanged();
                doResult(pair, 0);
            }
        });
    }

    private void init() {
        mLEDManager = new LEDManager();
        mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
        PrinterManager.getInstance().init();
        etInputText.setData(lvResults, this);
        setDeviceCount(setDeviceCount());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isUse()) {
            if (deviceDetails.size() != setDeviceCount()) {
                setDeviceCount(setDeviceCount());
            }
        }

    }


    @Override
    public void finish() {
        if (isUse()) {
            toastSpeak("测试中,不允许退出当前界面");
            return;
        }
        super.finish();
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        switch (baseEvent.getTagInt()) {
            case EventConfigs.INSTALL_RESULT:
                RoundResult iRoundResult = (RoundResult) baseEvent.getData();
                for (int i = 0; i < deviceDetails.size(); i++) {
                    DeviceDetail deviceDetail = deviceDetails.get(i);

                    if (TextUtils.equals(deviceDetail.getStuDevicePair().getStudent().getStudentCode(), iRoundResult.getStudentCode())) {
                        String[] timeResult = deviceDetail.getStuDevicePair().getTimeResult();
                        final BaseStuPair pair = deviceDetail.getStuDevicePair();
                        timeResult[iRoundResult.getRoundNo() - 1] = ((iRoundResult.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" :
                                ResultDisplayUtils.getStrResultForDisplay(iRoundResult.getResult()));
                        deviceDetail.getStuDevicePair().setTimeResult(timeResult);
                        deviceListAdapter.notifyDataSetChanged();
                        pair.setResult(iRoundResult.getResult());
                        pair.setResultState(iRoundResult.getResultState());
                        updateResultLed(pair, i);

                        if (iRoundResult.getRoundNo() < setTestCount()) {
                            deviceDetail.setRound(iRoundResult.getRoundNo() + 1);
                        }
                        deviceListAdapter.notifyDataSetChanged();
                        toastSpeak(String.format(getString(R.string.test_speak_hint), pair.getStudent().getSpeakStuName(), deviceDetail.getRound())
                                , String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), deviceDetail.getRound()));
                        LogUtils.operation((i + 1) + "号机：" + pair.getStudent().getStudentName());
                        LogUtils.operation(String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), deviceDetail.getRound()));
                        if (!isNextClickStart) {
                            if (deviceDetails.size() == 1) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
                                        pair.setResult(-999);
                                        deviceListAdapter.notifyDataSetChanged();
                                    }
                                }, 3000);
                                pair.setTestTime(DateUtil.getCurrentTime() + "");
                                sendTestCommand(pair, i);
                            } else {
                                pair.setTestTime(DateUtil.getCurrentTime() + "");
                                sendTestCommand(pair, i);
                            }

                        }
                    }
                }

                break;
            case EventConfigs.UPDATE_RESULT:
                RoundResult roundResult = (RoundResult) baseEvent.getData();
                for (int i = 0; i < deviceDetails.size(); i++) {
                    DeviceDetail deviceDetail = deviceDetails.get(i);
                    BaseStuPair pair = deviceDetail.getStuDevicePair();
                    if (TextUtils.equals(pair.getStudent().getStudentCode(), roundResult.getStudentCode())) {
                        String[] timeResult = pair.getTimeResult();

                        timeResult[roundResult.getRoundNo() - 1] = ((roundResult.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" :
                                ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult()));
                        pair.setTimeResult(timeResult);
                        if (roundResult.getRoundNo() == deviceDetail.getRound()) {
                            pair.setResult(roundResult.getResult());
                            pair.setResultState(roundResult.getResultState());
                            updateResultLed(pair, 0);
                        }

                    }
                }
                deviceListAdapter.notifyDataSetChanged();

                break;

        }
    }

    public void ledShow() {
        int ledMode = SettingHelper.getSystemSetting().getLedMode();
        if (ledMode == 0) {
            mLEDManager.clearScreen(TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
            for (int i = 0; i < deviceCount; i++) {
                StringBuilder data = new StringBuilder();
                data.append(i + 1).append("号机");//1号机         空闲
                for (int j = 0; j < 7; j++) {
                    data.append(" ");
                }
                data.append("空闲");
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data.toString(), 0, i, false, true);
            }
        }
    }

    public void setConfirmVisible(int index, boolean visible) {
        deviceDetails.get(index).setConfirmVisible(visible);
        deviceListAdapter.notifyItemChanged(index);
    }

    public void setShowGetData(int deviceId, boolean enable) {
        deviceListAdapter.setShowGetData(deviceId, enable);
    }

    @Override
    public void onCheckIn(Student student) {
        final StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        final List<RoundResult> roundResultList = DBManager.getInstance().queryFinallyRountScoreByExamTypeList(student.getStudentCode(), studentItem.getExamType());
        testNo = roundResultList == null || roundResultList.size() == 0 ? 1 : roundResultList.get(0).getTestNo();
        //保存成绩，并测试轮次大于测试轮次次数
        if (roundResultList != null && roundResultList.size() >= setTestCount()) {
            if (roundResultList != null && roundResultList.size() >= TestConfigs.getMaxTestCount(this)) {
                SystemSetting setting = SettingHelper.getSystemSetting();
                if (setting.isAgainTest() && setting.isResit()){
                    final Student finalStudent = student;
                    new SweetAlertDialog(this).setContentText("需要重测还是补考呢?")
                            .setCancelText("重测")
                            .setConfirmText("补考")
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    AgainTestDialog dialog = new AgainTestDialog();
                                    dialog.setArguments(finalStudent,roundResultList,studentItem);
                                    dialog.setOnIndividualCheckInListener(BaseMoreActivity.this);
                                    dialog.show(getSupportFragmentManager(),"AgainTestDialog");
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    ResitDialog dialog = new ResitDialog();
                                    dialog.setArguments(finalStudent,roundResultList,studentItem);
                                    dialog.setOnIndividualCheckInListener(BaseMoreActivity.this);
                                    dialog.show(getSupportFragmentManager(),"ResitDialog");
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            }).show();
                }
                if (setting.isAgainTest()){
                    AgainTestDialog dialog = new AgainTestDialog();
                    dialog.setArguments(student,roundResultList,studentItem);
                    dialog.setOnIndividualCheckInListener(this);
                    dialog.show(getSupportFragmentManager(),"AgainTestDialog");
                    return;
                }
                if (setting.isResit()){
                    ResitDialog dialog = new ResitDialog();
                    dialog.setArguments(student,roundResultList,studentItem);
                    dialog.setOnIndividualCheckInListener(this);
                    dialog.show(getSupportFragmentManager(),"ResitDialog");
                    return;
                }else {
                    InteractUtils.toastSpeak(this, "该考生已测试");
                }

            }
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
        for (DeviceDetail deviceDetail : deviceDetails) {
            Student deviceStu = deviceDetail.getStuDevicePair().getStudent();
            if (deviceStu != null && TextUtils.equals(student.getStudentCode(), deviceStu.getStudentCode())) {
                toastSpeak("该考生正在测试，无法添加");
                return;
            }
        }

        int index = 0;
        boolean canUseDevice = false;
        for (int i = 0; i < deviceCount; i++) {
            if (deviceDetails.get(i).isDeviceOpen() && deviceDetails.get(i).getStuDevicePair().isCanTest() &&
                    deviceDetails.get(i).getStuDevicePair().getStudent() == null
                    && deviceDetails.get(i).getStuDevicePair().getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
                index = i;
                canUseDevice = true;
                break;
            }
        }

        if (!canUseDevice && !SettingHelper.getSystemSetting().isInputTest()) {
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
        deviceDetails.get(index).setRound(roundResultList.size() + 1);

        if (SettingHelper.getSystemSetting().getLedMode() == 0) {
            int clertCount = 4 - deviceDetails.size();
            for (int i = clertCount; i >= 1; i--) {
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), new byte[16], 0, i, false, true);
            }
        }


        addStudent(student, index);
        deviceDetails.get(index).getStuDevicePair().setTimeResult(result);
        deviceDetails.get(index).getStuDevicePair().setResult(-999);
        deviceListAdapter.notifyItemChanged(index);

        if (!isNextClickStart) {
            deviceDetails.get(index).getStuDevicePair().setTestTime(DateUtil.getCurrentTime() + "");
            deviceDetails.get(index).getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
            deviceDetails.get(index).getStuDevicePair().setResult(-999);
            deviceListAdapter.notifyItemChanged(index);
            sendTestCommand(deviceDetails.get(index).getStuDevicePair(), index);
        }
    }

    public void setFaultEnable(boolean isPenalize) {
        this.isPenalize = isPenalize;
        deviceListAdapter.setPenalize(isPenalize);
        if (deviceDetails.size()==1){
            deviceDetails.get(0).setPunish(true);
        }
    }

    public void setNextClickStart(boolean nextClickStart) {
        isNextClickStart = nextClickStart;
        deviceListAdapter.setNextClickStart(nextClickStart);
    }

    /**
     * 将考生与设备绑定
     *
     * @param student
     */
    private void addStudent(Student student, int index) {
        DeviceDetail deviceDetail = deviceDetails.get(index);
        deviceDetail.getStuDevicePair().setStudent(student);
        deviceDetail.getStuDevicePair().setCanTest(false);
        deviceDetail.getStuDevicePair().setBaseHeight(0);
        int count = deviceDetail.getRound();
        toastSpeak(String.format(getString(R.string.test_speak_hint), student.getStudentName(), count)
                , String.format(getString(R.string.test_speak_hint), student.getStudentName(), count));
        LogUtils.operation((index + 1) + "号机：" + student.getStudentName());
        LogUtils.operation(String.format(getString(R.string.test_speak_hint), student.getStudentName(), count));
        setShowLed(deviceDetail.getStuDevicePair(), index);

    }

    /**
     * led 显示
     *
     * @param pair
     */
    private void setShowLed(BaseStuPair pair, int index) {
        Student student = pair.getStudent();
        if (student == null)
            return;
        int ledMode = SettingHelper.getSystemSetting().getLedMode();
        if (ledMode == 0) {
            String str = InteractUtils.getStrWithLength(student.getStudentName(), 6);

            str += "准备";
            byte[] data = new byte[0];
            try {
                data = str.getBytes("GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.e("TAGLED", str + "," + index);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, index, false, true);
        } else {
            mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index + 1, student.getLEDStuName() + "   第" + deviceDetails.get(index).getRound() + "次", 0, 0, true, false);
            mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index + 1, "当前：", 0, 1, false, true);

            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), student.getLEDStuName() + "   第" + deviceDetails.get(index).getRound() + "次", 0, 0, true, false);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "当前：", 0, 1, false, true);


            RoundResult bestResult = DBManager.getInstance().queryBestScore(student.getStudentCode(), testNo);
            if (bestResult != null) {
                byte[] data = new byte[16];
                String str = "最好：";
                String result = bestResult.getResultState() == RoundResult.RESULT_STATE_FOUL ? "X" : ResultDisplayUtils.getStrResultForDisplay(bestResult.getResult());
                try {
                    byte[] strData = str.getBytes("GB2312");
                    System.arraycopy(strData, 0, data, 0, strData.length);
                    byte[] resultData = result.getBytes("GB2312");
                    System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index + 1, data, 0, 2, false, true);
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 2, false, true);
            } else {
                mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index + 1, "最好：", 0, 2, false, true);
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "最好：", 0, 2, false, true);

            }
        }

    }

    private void initView() {
        deviceListAdapter = new DeviceListAdapter(deviceDetails);
        GridLayoutManager layoutManager = new GridLayoutManager(this, deviceDetails.size());
        rvDeviceList.setLayoutManager(layoutManager);
        deviceListAdapter.setTestCount(setTestCount());
        deviceListAdapter.setNextClickStart(isNextClickStart);
        rvDeviceList.setAdapter(deviceListAdapter);
        deviceListAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int pos) {
                BaseStuPair pair = deviceDetails.get(pos).getStuDevicePair();
                switch (view.getId()) {
                    case R.id.txt_start:
                        if (pair.getStudent() == null) {
                            toastSpeak("当前无学生测试");
                            return;
                        }
                        if (deviceDetails.get(pos).getRound() > setTestCount()) {
                            toastSpeak("当前学生测试完成");
                            stuSkip(pos);
                            return;
                        }
                        if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN || pair.getBaseDevice().getState() == BaseDeviceState.STATE_FREE ||
                                pair.getBaseDevice().getState() == BaseDeviceState.STATE_END) {
                            pair.setTestTime(DateUtil.getCurrentTime() + "");
                            sendTestCommand(pair, pos);
                            deviceListAdapter.setPenalize(false);
//                            if (isPenalize) {
//                                setConfirmVisible(pos, true);
//                            }
                        }
                        break;
                    case R.id.txt_skip:
                        if (pair.getStudent() != null) {
                            stuSkipDialog(pair.getStudent(), pos);
                        }
                        break;
                    case R.id.txt_punish:
                        if (pair.getStudent() != null) {
//                            penalize(pos);
                            DataRetrieveBean bean = new DataRetrieveBean();
                            bean.setStudentCode(pair.getStudent().getStudentCode());
                            bean.setSex(pair.getStudent().getSex());
                            bean.setTestState(1);
                            bean.setStudentName(pair.getStudent().getStudentName());
                            Intent intent = new Intent(BaseMoreActivity.this, DataDisplayActivity.class);
                            intent.putExtra(DataDisplayActivity.ISSHOWPENALIZEFOUL, isPenalize ? View.VISIBLE : View.GONE);
                            intent.putExtra(DataRetrieveActivity.DATA_ITEM_CODE, getItemCode());
                            intent.putExtra(DataRetrieveActivity.DATA_EXTRA, bean);
                            intent.putExtra(DataDisplayActivity.TESTNO, testNo);
                            startActivity(intent);
                        }
                        break;
                    case R.id.txt_confirm:
                        if (pair.getStudent() != null) {
//                            confirmResult(pos);
                            updateResult(pair);
                            doResult(pair, pos);
                            deviceDetails.get(pos).setConfirmVisible(false);
                            deviceListAdapter.notifyItemChanged(pos);
                            if (isPenalize) {
                                setConfirmVisible(pos, false);
                            }
                        }
                        break;
                    case R.id.txt_get_data:
                        if (pair.getStudent() != null) {
                            showGetData(pos);
                        }
                        break;
                    case R.id.txt_test_result:
                        if (SettingHelper.getSystemSetting().isInputTest() && pair.getStudent() != null) {
                            editResultDialog.showDialog(pair.getStudent());
                        }
                        break;
                    default:
                        break;
                }


            }
        });
    }

    private String getItemCode() {
        return TestConfigs.sCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : TestConfigs.sCurrentItem.getItemCode();
    }

    /**
     * 展示手动获取成绩
     */
    private void showGetData(final int index) {
        SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        alertDialog.setTitleText("是否手动获取成绩");
        alertDialog.setCancelable(false);
        alertDialog.setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                LogUtils.operation("点击了获取成绩:");
                sweetAlertDialog.dismissWithAnimation();
                getData(index);
            }
        }).setCancelText("否").setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();

    }

    public void getData(int pos) {

    }

    //确认成绩判罚
    protected void confirmResult(int pos) {
        showPenalize(pos);
    }

    /**
     * 展示判罚
     */
    private void showPenalize(final int index) {
        final BaseStuPair pair = deviceDetails.get(index).getStuDevicePair();
        SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        alertDialog.setTitleText(getString(R.string.confirm_result));
        alertDialog.setCancelable(false);
        alertDialog.setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                updateResult(pair);
                doResult(pair, index);
            }
        }).setCancelText(getString(R.string.foul)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                pair.setResultState(RoundResult.RESULT_STATE_FOUL);
                updateResult(pair);
                doResult(pair, index);
            }
        }).show();
        deviceDetails.get(index).setConfirmVisible(false);
        deviceListAdapter.notifyItemChanged(index);
    }


    /**
     * 展示判罚
     */
    private void penalize(final int index) {
        final BaseStuPair pair = deviceDetails.get(index).getStuDevicePair();
        SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        alertDialog.setTitleText("确定判罚?");
        alertDialog.setCancelable(false);
        alertDialog.setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                pair.setResultState(RoundResult.RESULT_STATE_FOUL);
                updateResult(pair);
                doResult(pair, index);
                deviceDetails.get(index).setConfirmVisible(false);
                deviceListAdapter.notifyItemChanged(index);
            }
        }).setCancelText("取消").setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();

    }

    protected void stuSkipDialog(final Student student, final int pos) {
        new AlertDialog.Builder(this).setMessage("是否跳过" + student.getStudentName() + "考生测试")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Logger.i("stuSkip:" + student.toString());
                        //测试结束学生清除 ，设备设置空闲状态
                        stuSkip(pos);
//                        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
                    }
                }).setNegativeButton("取消", null).show();
    }

    public void stuSkip(int pos) {
        deviceDetails.get(pos).getStuDevicePair().setStudent(null);
        deviceDetails.get(pos).getStuDevicePair().setCanTest(true);
        deviceDetails.get(pos).getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_FREE);
        deviceDetails.get(pos).getStuDevicePair().setTimeResult(new String[setTestCount()]);
        deviceListAdapter.notifyItemChanged(pos);
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

        return builder.setTitle(title).addRightText("项目设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isUse()) {
                    toastSpeak("测试中,不允许修改设置");
                } else {
                    gotoItemSetting();
                }

            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        });
    }

    /**
     * 是否存在使用中设备
     *
     * @return
     */
    public boolean isUse() {
        boolean isOnUse = false;
        for (DeviceDetail deviceDetail : deviceDetails) {
            if (deviceDetail.getStuDevicePair().getBaseDevice().getState() == BaseDeviceState.STATE_ONUSE) {
                return true;
            }
        }
        return isOnUse;


    }

    public synchronized void updateDevice(@NonNull BaseDeviceState deviceState) {
        int deviceId = deviceState.getDeviceId();
//        if (deviceState != null)
//            LogUtils.operation("更新设备状态:deviceId=" + deviceId + ",deviceState=" + deviceState.toString());
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

        if (pair != null) {
            pair.getBaseDevice().setState(deviceState.getState());
            //状态为测试已结束
            if (deviceState.getState() == BaseDeviceState.STATE_END) {
//                if (isPenalize && pair.getResultState() != RoundResult.RESULT_STATE_FOUL) {
//                    if (setDeviceCount() == 1) {
//                        showPenalize(index);
//                    } else {
//                        deviceDetails.get(index).setConfirmVisible(true);
//                        deviceListAdapter.notifyItemChanged(index);
//                    }
//                } else {
//                    doResult(pair, index);
//                }
//                deviceListAdapter.setPenalize(isPenalize);
//                deviceDetails.get(index).setConfirmVisible(true);
//                deviceListAdapter.notifyDataSetChanged();
                doResult(pair, index);
            }
        }
        refreshDevice(index);
    }

    public void setTxtEnable(int deviceId, boolean enable) {
        deviceListAdapter.setTxtStartEnable(deviceId, enable);
    }

    /**
     * 处理结果
     */
    private synchronized void doResult(final BaseStuPair pair, final int index) {
        if (pair.getStudent() == null)
            return;
        broadResult(pair);
        DeviceDetail detail = deviceDetails.get(index);
        String[] timeResult = detail.getStuDevicePair().getTimeResult();
        if (detail.getRound() > timeResult.length)//防止
            return;
        //设置设备成绩
        if (detail.getRound() - 1 < 0) {
            return;
        }
        timeResult[detail.getRound() - 1] = ((pair.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" :
                ResultDisplayUtils.getStrResultForDisplay(pair.getResult()));
        detail.getStuDevicePair().setTimeResult(timeResult);

        //保存成绩
        saveResult(pair, index);
        printResult(pair, index);
//        broadResult(pair);

        if (detail.getRound() < setTestCount()) {
            if (pair.getResultState() == RoundResult.RESULT_STATE_NORMAL && pair.isFullMark()) {
                //测试结束学生清除 ，设备设置空闲状态
                detail.setRound(0);
                if (!isPenalize) {
                    //4秒后清理学生信息
                    Message msg = new Message();
                    msg.what = pair.getBaseDevice().getDeviceId();
                    msg.obj = detail;
                    clearHandler.sendMessageDelayed(msg, 4000);
                }

                pair.setCanTest(true);
                pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
                return;
            }
            int count = detail.getRound();
            if (detail.getRound() < setTestCount()) {
                detail.setRound(count + 1);
                toastSpeak(String.format(getString(R.string.test_speak_hint), pair.getStudent().getSpeakStuName(), count + 1)
                        , String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), count + 1));
                LogUtils.operation((index + 1) + "号机：" + pair.getStudent().getStudentName());
                LogUtils.operation(String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), count));
                if (!isNextClickStart) {
                    if (deviceDetails.size() == 1) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                deviceDetails.get(index).getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
                                deviceDetails.get(index).getStuDevicePair().setResult(-999);
                                deviceListAdapter.notifyItemChanged(index);
                            }
                        }, 3000);
                        pair.setTestTime(DateUtil.getCurrentTime() + "");
                        sendTestCommand(pair, index);
                    } else {
                        pair.setTestTime(DateUtil.getCurrentTime() + "");
                        sendTestCommand(pair, index);
                    }

                }
            }
            Message msg = new Message();
            msg.obj = pair;
            ledHandler.sendMessageDelayed(msg, 3000);


        } else {
            detail.setRound(0);
            if (!isPenalize) {
                //4秒后清理学生信息
                Message msg = new Message();
                msg.obj = detail;
                clearHandler.sendMessageDelayed(msg, 4000);
            }


        }

        deviceListAdapter.notifyItemChanged(index);
        pair.setCanTest(true);
//        pair.getBaseDevice().setResultState(BaseDeviceState.STATE_FREE);

    }


    private void printResult(BaseStuPair baseStuPair, int index) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        //是否已全部次数测试完成，非满分跳过
        if (deviceDetails.get(index).getRound() < setTestCount() && !baseStuPair.isFullMark()) {
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

    private void saveResult(BaseStuPair baseStuPair, int index) {
        LogUtils.all("保存成绩:" + baseStuPair.toString());
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
        roundResult.setTestTime(baseStuPair.getTestTime());
        roundResult.setEndTime(DateUtil.getCurrentTime() + "");
        if (baseStuPair.getRoundNo() != 0){
            roundResult.setRoundNo(baseStuPair.getRoundNo());
            roundResult.setResultTestState(1);
            baseStuPair.setRoundNo(0);
        }else {
            roundResult.setRoundNo(deviceDetails.get(index).getRound());
            roundResult.setResultTestState(0);
        }
        roundResult.setTestNo(testNo);
        roundResult.setExamType(studentItem.getExamType());
        roundResult.setScheduleNo(studentItem.getScheduleNo());
        roundResult.setUpdateState(0);
        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
        RoundResult bestResult = DBManager.getInstance().queryBestScore(baseStuPair.getStudent().getStudentCode(), testNo);
        if (bestResult != null) {
            // 原有最好成绩犯规 或者原有最好成绩没有犯规但是现在成绩更好
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && baseStuPair.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= baseStuPair.getResult()) {
                // 这个时候就要同时修改这两个成绩了
                roundResult.setIsLastResult(1);
                bestResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(bestResult);
                updateLastResultLed(baseStuPair, index);
            } else {
                if (bestResult.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                    roundResult.setIsLastResult(1);
                    bestResult.setIsLastResult(0);
                    DBManager.getInstance().updateRoundResult(bestResult);
                    updateLastResultLed(baseStuPair, index);
                } else {
                    roundResult.setIsLastResult(0);
                    updateLastResultLed(baseStuPair, index);
                }
            }
        } else {
            // 第一次测试
            roundResult.setIsLastResult(1);
            updateLastResultLed(baseStuPair, index);


        }
        DBManager.getInstance().insertRoundResult(roundResult);
        LogUtils.operation("保存成绩:" + roundResult.toString());
        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        UploadResults uploadResults = new UploadResults(studentItem.getScheduleNo(), TestConfigs.getCurrentItemCode(),
                baseStuPair.getStudent().getStudentCode(), testNo + "", null, RoundResultBean.beanCope(roundResultList));


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
    private void updateLastResultLed(BaseStuPair baseStuPair, int index) {
//        int ledMode = SettingHelper.getSystemSetting().getLedMode();
//        String result = roundResult.getResultState() == RoundResult.RESULT_STATE_FOUL ? "X" : ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult());
//        if (ledMode == 0) {
//            int x = ResultDisplayUtils.getStringLength(result);
//            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), result, 16 - x, index, false, true);
//        } else {
//            byte[] data = new byte[16];
//            String str = "最好：";
//            try {
//                byte[] strData = str.getBytes("GB2312");
//                System.arraycopy(strData, 0, data, 0, strData.length);
//                byte[] resultData = result.getBytes("GB2312");
//                System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 2, false, true);

//            byte[] data1 = new byte[16];
//            String str1 = "当前：";
//            try {
//                byte[] strData = str1.getBytes("GB2312");
//                System.arraycopy(strData, 0, data1, 0, strData.length);
//                BaseStuPair baseStu = deviceDetails.get(index).getStuDevicePair();
//                String res = (baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult());
//                byte[] resultData = res.getBytes("GB2312");
//                System.arraycopy(resultData, 0, data1, data1.length - resultData.length - 1, resultData.length);
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index, data, 0, 1, false, true);
//        }

        updateResultLed(baseStuPair, index);
//        deviceListAdapter.notifyItemChanged(index);
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
        if (null != pair.getBaseDevice() && pair.getStudent() != null) {
            pair.setResultState(baseStu.getResultState());
            pair.setResult(baseStu.getResult());
            pair.setFullMark(baseStu.isFullMark());

//            refreshDevice(index);


        }


    }

    private void broadResult(BaseStuPair baseStuPair) {
        if (deviceCount > 1)
            return;
        if (SettingHelper.getSystemSetting().isAutoBroadcast()) {
            if (baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) {
                TtsManager.getInstance().speak(baseStuPair.getStudent().getSpeakStuName() + "犯规");
            } else {

                TtsManager.getInstance().speak(String.format(getString(R.string.speak_result), baseStuPair.getStudent().getSpeakStuName(), ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult())));
            }


        }
    }

    public void refreshDevice(int index) {
        if (deviceDetails.get(index).getStuDevicePair().getBaseDevice() != null) {
            deviceListAdapter.notifyItemChanged(index);
        }
    }


    private void updateResultLed(BaseStuPair baseStu, int index) {
        int ledMode = SettingHelper.getSystemSetting().getLedMode();
        String result = (baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult());
        if (ledMode == 0) {
            int x = ResultDisplayUtils.getStringLength(result);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), result, 16 - x, index, false, true);
        } else {
            byte[] data = new byte[16];
            String str1 = "当前：";
            try {
                byte[] strData = str1.getBytes("GB2312");
                System.arraycopy(strData, 0, data, 0, strData.length);
                String res = (baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult());
                byte[] resultData = res.getBytes("GB2312");
                System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index, data, 0, 1, false, true);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 1, false, true);

            RoundResult bestResult = DBManager.getInstance().queryBestScore(baseStu.getStudent().getStudentCode(), testNo);
            int res = 0;
            String resultString = "";
            if (bestResult != null) {
                res = bestResult.getResult();
                resultString = (bestResult.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(res);
            }
            if (baseStu.getResultState() == RoundResult.RESULT_STATE_NORMAL && baseStu.getResult() > res) {
                res = baseStu.getResult();
                resultString = (baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(res);
            }

            data = new byte[16];
            String str = "最好：";
            try {
                byte[] strData = str.getBytes("GB2312");
                System.arraycopy(strData, 0, data, 0, strData.length);
                byte[] resultData = resultString.getBytes("GB2312");
                System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index + 1, data, 0, 2, false, true);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 2, false, true);


        }
    }

    @OnClick({R.id.txt_led_setting, R.id.tv_device_pair, R.id.img_AFR})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_led_setting:

                if (isUse()) {
                    toastSpeak("测试中,不允许修改设置");
                } else {
                    startActivity(new Intent(this, LEDSettingActivity.class));
                }

                break;
            case R.id.tv_device_pair:
                if (isUse()) {
                    toastSpeak("测试中,不允许修改设置");
                } else {
                    startActivity(new Intent(this, SargentPairActivity.class));
                }

                break;
            case R.id.img_AFR:
                showAFR();
                break;
        }
    }


    public class LedHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BaseStuPair pair = (BaseStuPair) msg.obj;
            setShowLed(pair, pair.getBaseDevice().getDeviceId() - 1);
        }
    }

    private class ClearHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DeviceDetail detail = (DeviceDetail) msg.obj;
            detail.getStuDevicePair().setTimeResult(new String[setTestCount()]);
            detail.getStuDevicePair().setStudent(null);
            detail.getStuDevicePair().setResult(-999);
            deviceListAdapter.notifyDataSetChanged();
        }
    }


    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
        deviceDetails.clear();
        for (int i = 0; i < deviceCount; i++) {
            DeviceDetail detail = new DeviceDetail();
            detail.getStuDevicePair().getBaseDevice().setDeviceId(i + 1);
            detail.getStuDevicePair().setTimeResult(new String[setTestCount()]);
            detail.setDeviceOpen(true);
            if (deviceCount == 1) {
                detail.getStuDevicePair().setResult(-999);
                detail.setItemType(DeviceDetail.ITEM_ONE);
            }
            deviceDetails.add(detail);
        }
        initView();
    }

    public void updateAdapterTestCount() {

        deviceListAdapter.setTestCount(setTestCount());
        for (DeviceDetail deviceDetail : deviceDetails) {
            deviceDetail.getStuDevicePair().setTimeResult(new String[setTestCount()]);
        }
        deviceListAdapter.notifyDataSetChanged();
    }

    /**
     * 设置项目测试轮次次数
     */
    public abstract int setTestCount();

    public abstract int setDeviceCount();

    public abstract boolean isResultFullReturn(int sex, int result);

    /**
     * 跳转项目设置页面
     */
    public abstract void gotoItemSetting();

    protected abstract void sendTestCommand(BaseStuPair pair, int index);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (SettingHelper.getSystemSetting().getLedMode() == 0) {

            mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
        } else {
            for (DeviceDetail deviceDetail : deviceDetails) {
                mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), deviceDetail.getStuDevicePair().getBaseDevice().getDeviceId(),
                        TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));

            }
        }
    }

    public void showAFR() {
        if (SettingHelper.getSystemSetting().getCheckTool() != 4) {
            ToastUtils.showShort("未选择人脸识别检录功能");
            return;
        }
        if (afrFrameLayout == null) {
            return;
        }

        boolean isGoto = afrFragment.gotoUVCFaceCamera(!afrFragment.isOpenCamera);
        if (isGoto) {
            if (afrFragment.isOpenCamera) {
                afrFrameLayout.setVisibility(View.VISIBLE);
            } else {
                afrFrameLayout.setVisibility(View.GONE);
            }
        }
    }



    @Override
    public int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }
}

