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
import com.feipulai.exam.activity.person.PenalizeDialog;
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
import com.feipulai.exam.entity.GroupItem;
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
    @BindView(R.id.oneView)
    BaseMoreOneView oneView;
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
    private PenalizeDialog penalizeDialog;
    private Student lastStu;
    private String[] lastResult;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_sargent_jump_more;
    }

    @Override
    protected void initData() {
        super.initData();
        RadioChannelCommand command = new RadioChannelCommand(SettingHelper.getSystemSetting().getUseChannel());
        LogUtils.normal(command.getCommand().length + "---" + StringUtility.bytesToHexString(command.getCommand()) + "---????????????");
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
                editResultDialog.dismissDialog();
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
            toastSpeak("?????????,???????????????????????????");
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
                        timeResult[iRoundResult.getRoundNo() - 1] = RoundResult.resultStateStr(iRoundResult.getResultState(), iRoundResult.getResult());

                        pair.setTimeResult(timeResult);
                        deviceListAdapter.notifyDataSetChanged();
                        pair.setResult(iRoundResult.getResult());
                        pair.setResultState(iRoundResult.getResultState());
                        updateResultLed(pair, i);
                        oneView.setResultData(pair);

                        if (iRoundResult.getRoundNo() < setTestCount()) {
                            deviceDetail.setRound(iRoundResult.getRoundNo() + 1);
                        } else {
                            deviceDetail.setRound(0);
                            //4????????????????????????
                            Message msg = new Message();
                            msg.obj = deviceDetail;
                            clearHandler.sendMessageDelayed(msg, 4000);
                            return;
                        }
                        oneView.indexResult(deviceDetail.getRound() - 1);
                        deviceListAdapter.notifyDataSetChanged();
                        toastSpeak(String.format(getString(R.string.test_speak_hint), pair.getStudent().getSpeakStuName(), deviceDetail.getRound())
                                , String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), deviceDetail.getRound()));
                        LogUtils.operation((i + 1) + "?????????" + pair.getStudent().getStudentName());
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
                if (TextUtils.equals(lastStu.getStudentCode(), roundResult.getStudentCode())) {
                    lastResult[roundResult.getRoundNo() - 1] = RoundResult.resultStateStr(roundResult.getResultState(), roundResult.getResult());
                }
                for (int i = 0; i < deviceDetails.size(); i++) {
                    DeviceDetail deviceDetail = deviceDetails.get(i);
                    BaseStuPair pair = deviceDetail.getStuDevicePair();
                    if (TextUtils.equals(pair.getStudent().getStudentCode(), roundResult.getStudentCode())) {
                        String[] timeResult = pair.getTimeResult();

                        timeResult[roundResult.getRoundNo() - 1] = RoundResult.resultStateStr(roundResult.getResultState(), roundResult.getResult());
                        pair.setTimeResult(timeResult);
                        if (roundResult.getRoundNo() == deviceDetail.getRound()) {
                            pair.setResult(roundResult.getResult());
                            pair.setResultState(roundResult.getResultState());
                            updateResultLed(pair, 0);
                        }
                        oneView.setResultData(pair);
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
                data.append(i + 1).append("??????");//1??????         ??????
                for (int j = 0; j < 7; j++) {
                    data.append(" ");
                }
                data.append("??????");
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
        if (setDeviceCount() == 1) {
            oneView.setShowGetData(enable);
        }
    }

    @Override
    public void onCheckIn(Student student) {
        final StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        final List<RoundResult> roundResultList = DBManager.getInstance().queryFinallyRountScoreByExamTypeList(student.getStudentCode(), studentItem.getExamType());
        testNo = roundResultList == null || roundResultList.size() == 0 ? 1 : roundResultList.get(0).getTestNo();
        //??????????????????????????????????????????????????????

        if (roundResultList != null && roundResultList.size() >= setTestCount()) {
            if (roundResultList != null && roundResultList.size() >= TestConfigs.getMaxTestCount(this)) {
                resitOrAgainTest(student, studentItem, roundResultList);

            }
            return;
        } else if (roundResultList != null) {
            for (RoundResult roundResult : roundResultList) {
                if (roundResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && isResultFullReturn(student.getSex(), roundResult.getResult())) {
                    toastSpeak("??????");
                    resitOrAgainTest(student, studentItem, roundResultList);
                    return;
                }

            }
        }

        //??????????????????????????????????????????????????????????????????????????????????????????1????????????????????????+1
        if (roundResultList.size() == 0) {
            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
            testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1;
        } else {
            testNo = roundResultList.get(0).getTestNo();
        }
        for (DeviceDetail deviceDetail : deviceDetails) {
            Student deviceStu = deviceDetail.getStuDevicePair().getStudent();
            if (deviceStu != null && TextUtils.equals(student.getStudentCode(), deviceStu.getStudentCode())) {
                toastSpeak("????????????????????????????????????");
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
            toastSpeak("????????????????????????????????????");
            return;
        }
        deviceDetails.get(index).setRound(0);
        String[] result = new String[setTestCount()];
        for (int i = 0; i < roundResultList.size(); i++) {
            if (i < setTestCount()) {
                result[roundResultList.get(i).getRoundNo() - 1] =
                        RoundResult.resultStateStr(roundResultList.get(i).getResultState(), roundResultList.get(i).getResult());
            }
        }

        List<RoundResult> roundResultAll = DBManager.getInstance().queryFinallyRountScoreByExamTypeAll(student.getStudentCode(), studentItem.getExamType());
        if (roundResultAll.size() >= TestConfigs.getMaxTestCount()) {
            List<Integer> rounds = new ArrayList<>();
            for (int i = 0; i < roundResultList.size(); i++) {
                if (roundResultList.size() > 0) {  //??????????????????
                    int roundNo = roundResultList.get(i).getRoundNo();
                    rounds.add(roundNo);
                }
            }

            for (int j = 1; j <= TestConfigs.getMaxTestCount(); j++) {
                if (!rounds.contains(j)) {
                    deviceDetails.get(index).setRound(j);
                }
            }
        }
        deviceDetails.get(index).setRound(deviceDetails.get(index).getRound() != 0 ? deviceDetails.get(index).getRound()
                : roundResultList.size() + 1);


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
        oneView.initData(deviceDetails.get(index).getStuDevicePair());
        oneView.indexResult(deviceDetails.get(index).getRound() - 1);
        if (!isNextClickStart) {
            deviceDetails.get(index).getStuDevicePair().setTestTime(DateUtil.getCurrentTime() + "");
            deviceDetails.get(index).getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
            deviceDetails.get(index).getStuDevicePair().setResult(-999);
            deviceListAdapter.notifyItemChanged(index);
            sendTestCommand(deviceDetails.get(index).getStuDevicePair(), index);
        }
    }

    private void resitOrAgainTest(Student student, final StudentItem studentItem, final List<RoundResult> roundResultList) {
        SystemSetting setting = SettingHelper.getSystemSetting();
        if (setting.isAgainTest() && setting.isResit()) {
            final Student finalStudent = student;
            new SweetAlertDialog(this).setContentText("????????????????????????????")
                    .setCancelText("??????")
                    .setConfirmText("??????")
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            AgainTestDialog dialog = new AgainTestDialog();
                            dialog.setArguments(finalStudent, roundResultList, studentItem);
                            dialog.setOnIndividualCheckInListener(BaseMoreActivity.this);
                            dialog.show(getSupportFragmentManager(), "AgainTestDialog");
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    })
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            ResitDialog dialog = new ResitDialog();
                            dialog.setArguments(finalStudent, roundResultList, studentItem);
                            dialog.setOnIndividualCheckInListener(BaseMoreActivity.this);
                            dialog.show(getSupportFragmentManager(), "ResitDialog");
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    }).show();
        }
        if (setting.isAgainTest()) {
            AgainTestDialog dialog = new AgainTestDialog();
            dialog.setArguments(student, roundResultList, studentItem);
            dialog.setOnIndividualCheckInListener(this);
            dialog.show(getSupportFragmentManager(), "AgainTestDialog");
            return;
        }
        if (setting.isResit()) {
            ResitDialog dialog = new ResitDialog();
            dialog.setArguments(student, roundResultList, studentItem);
            dialog.setOnIndividualCheckInListener(this);
            dialog.show(getSupportFragmentManager(), "ResitDialog");
            return;
        } else {
            InteractUtils.toastSpeak(this, "??????????????????");
        }
    }

    public void setFaultEnable(boolean isPenalize) {
        this.isPenalize = isPenalize;
        deviceListAdapter.setPenalize(isPenalize);
        if (deviceDetails.size() == 1) {
            deviceDetails.get(0).setPunish(true);
        }
    }

    public void setNextClickStart(boolean nextClickStart) {
        isNextClickStart = nextClickStart;
        deviceListAdapter.setNextClickStart(nextClickStart);
        oneView.setShowStartTest(nextClickStart);
    }

    /**
     * ????????????????????????
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
        LogUtils.operation((index + 1) + "?????????" + student.getStudentName());
        LogUtils.operation(String.format(getString(R.string.test_speak_hint), student.getStudentName(), count));
        setShowLed(deviceDetail.getStuDevicePair(), index);

    }

    /**
     * led ??????
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

            str += "??????";
            byte[] data = new byte[0];
            try {
                data = str.getBytes("GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.e("TAGLED", str + "," + index);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, index, false, true);
        } else {
            mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index + 1, student.getLEDStuName() + "   ???" + deviceDetails.get(index).getRound() + "???", 0, 0, true, false);
            mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index + 1, "?????????", 0, 1, false, true);

            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), student.getLEDStuName() + "   ???" + deviceDetails.get(index).getRound() + "???", 0, 0, true, false);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "?????????", 0, 1, false, true);


            RoundResult bestResult = DBManager.getInstance().queryBestScore(student.getStudentCode(), testNo);
            if (bestResult != null) {
                byte[] data = new byte[16];
                String str = "?????????";
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
                mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index + 1, "?????????", 0, 2, false, true);
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "?????????", 0, 2, false, true);

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
                            toastSpeak("?????????????????????");
                            return;
                        }
                        if (deviceDetails.get(pos).getRound() > setTestCount()) {
                            toastSpeak("????????????????????????");
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
        if (setDeviceCount() == 1) {
            rvDeviceList.setVisibility(View.GONE);
            oneView.setVisibility(View.VISIBLE);
            oneView.refreshDeviceState(deviceDetails.get(0).getStuDevicePair().getBaseDevice());
            oneView.setBtnEnabled(false, false, false);
        } else {
            rvDeviceList.setVisibility(View.VISIBLE);
            oneView.setVisibility(View.GONE);
        }
        oneView.initResultCount(setTestCount());
        penalizeDialog = new PenalizeDialog(this, setTestCount());
        oneView.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BaseStuPair pair = deviceDetails.get(0).getStuDevicePair();
                switch (view.getId()) {
                    case R.id.tv_get_data:
                        if (pair.getStudent() != null) {
                            showGetData(0);
                        }
                        break;

                    case R.id.txt_stu_skip:
                        LogUtils.operation("???????????????");
                        if (pair.getStudent() != null) {
                            stuSkipDialog(pair.getStudent(), 0);
                        }
                        break;
                    case R.id.txt_start_test:
                        LogUtils.operation("?????????????????????");
                        if (pair.getStudent() == null) {
                            toastSpeak("?????????????????????");
                            return;
                        }
                        if (deviceDetails.get(0).getRound() > setTestCount()) {
                            toastSpeak("????????????????????????");
                            stuSkip(0);
                            return;
                        }
                        if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN || pair.getBaseDevice().getState() == BaseDeviceState.STATE_FREE ||
                                pair.getBaseDevice().getState() == BaseDeviceState.STATE_END) {
                            pair.setTestTime(DateUtil.getCurrentTime() + "");
                            sendTestCommand(pair, 0);
                            deviceListAdapter.setPenalize(false);
                        }

                        break;

                    case R.id.tv_foul:
                        if (pair.getStudent() == null) {
                            penalizeDialog.setData(0, pair.getStudent(), pair.getTimeResult(), lastStu, lastResult);
                        } else {
                            penalizeDialog.setData(1, pair.getStudent(), pair.getTimeResult(), lastStu, lastResult);
                        }
                        penalizeDialog.showDialog(0);
                        break;
                    case R.id.tv_inBack:
                        if (pair.getStudent() == null) {
                            penalizeDialog.setData(0, pair.getStudent(), pair.getTimeResult(), lastStu, lastResult);
                        } else {
                            penalizeDialog.setData(1, pair.getStudent(), pair.getTimeResult(), lastStu, lastResult);
                        }
                        penalizeDialog.showDialog(1);
                        break;
                    case R.id.tv_abandon:
                        if (pair.getStudent() == null) {
                            penalizeDialog.setData(0, pair.getStudent(), pair.getTimeResult(), lastStu, lastResult);
                        } else {
                            penalizeDialog.setData(1, pair.getStudent(), pair.getTimeResult(), lastStu, lastResult);
                        }
                        penalizeDialog.showDialog(2);
                        break;
                    case R.id.tv_normal:
                        if (null == pair.getStudent()) {
                            penalizeDialog.setData(0, pair.getStudent(), pair.getTimeResult(), lastStu, lastResult);
                        } else {
                            penalizeDialog.setData(1, pair.getStudent(), pair.getTimeResult(), lastStu, lastResult);
                        }
                        penalizeDialog.showDialog(3);
                        break;
                    case R.id.tv_resurvey:
                        if (pair.getStudent() == null) {
                            return;
                        }
                        AgainTestDialog dialog = new AgainTestDialog();
                        RoundResult roundResult = DBManager.getInstance().queryRoundByRoundNo(pair.getStudent().getStudentCode(), testNo, (oneView.getSelectPosition() + 1));
                        if (roundResult == null) {
                            toastSpeak("???????????????????????????????????????");
                            return;
                        }
                        List<RoundResult> results = new ArrayList<>();
                        results.add(roundResult);
                        dialog.setArguments(pair.getStudent(), results, mStudentItem);
                        dialog.setOnIndividualCheckInListener(new ResitDialog.onClickQuitListener() {
                            @Override
                            public void onCancel() {

                            }

                            @Override
                            public void onCommitPattern(Student student, StudentItem studentItem, List<RoundResult> results, int updateRoundNo) {
                                if (pair.getStudent()!=null){
                                    LogUtils.operation(pair.getStudent().getStudentCode() + "?????????" + updateRoundNo + "?????????");
                                    String[] result = pair.getTimeResult();
                                    result[updateRoundNo - 1] = "";
                                    pair.setTimeResult(result);
                                    oneView.setResultData(pair);
                                    oneView.indexResult(updateRoundNo - 1);
                                    //??????????????????
                                    pair.setRoundNo(updateRoundNo);
                                    deviceDetails.get(0).setRound(updateRoundNo);
                                    toastSpeak(String.format(getString(R.string.test_speak_hint), pair.getStudent().getSpeakStuName(), updateRoundNo)
                                            , String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), updateRoundNo));
                                    setShowLed(pair, 0);
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
                                            sendTestCommand(pair, 0);
                                        } else {
                                            pair.setTestTime(DateUtil.getCurrentTime() + "");
                                            sendTestCommand(pair, 0);
                                        }

                                    }
                                    oneView.setBtnEnabled(true, true, true);
                                }

                            }

                            @Override
                            public void onCommitGroup(Student student, GroupItem groupItem, List<RoundResult> results, int roundNo) {

                            }
                        });
                        dialog.show(getSupportFragmentManager(), "AgainTestDialog");


                        break;
                }
            }
        });
    }

    private String getItemCode() {
        return TestConfigs.sCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : TestConfigs.sCurrentItem.getItemCode();
    }

    /**
     * ????????????????????????
     */
    private void showGetData(final int index) {
        SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        alertDialog.setTitleText("????????????????????????");
        alertDialog.setCancelable(false);
        alertDialog.setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                LogUtils.operation("?????????????????????:");
                sweetAlertDialog.dismissWithAnimation();
                getData(index);
            }
        }).setCancelText("???").setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();

    }

    public void getData(int pos) {

    }

    //??????????????????
    protected void confirmResult(int pos) {
        showPenalize(pos);
    }

    /**
     * ????????????
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
     * ????????????
     */
    private void penalize(final int index) {
        final BaseStuPair pair = deviceDetails.get(index).getStuDevicePair();
        SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        alertDialog.setTitleText("?????????????");
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
        }).setCancelText("??????").setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();

    }

    protected void stuSkipDialog(final Student student, final int pos) {
        new AlertDialog.Builder(this).setMessage("????????????" + student.getStudentName() + "????????????")
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Logger.i("stuSkip:" + student.toString());
                        //???????????????????????? ???????????????????????????
                        stuSkip(pos);
//                        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
                    }
                }).setNegativeButton("??????", null).show();
    }

    public void stuSkip(int pos) {
        deviceDetails.get(pos).getStuDevicePair().setStudent(null);
        deviceDetails.get(pos).getStuDevicePair().setCanTest(true);
        deviceDetails.get(pos).getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_FREE);
        deviceDetails.get(pos).getStuDevicePair().setTimeResult(new String[setTestCount()]);
        deviceListAdapter.notifyItemChanged(pos);
        oneView.initData(deviceDetails.get(pos).getStuDevicePair());
        oneView.setBtnEnabled(false, false, false);
    }


    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "??????";
        } else {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "??????-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title).addRightText("????????????", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isUse()) {
                    toastSpeak("?????????,?????????????????????");
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
     * ???????????????????????????
     *
     * @return
     */
    public boolean isUse() {
        boolean isOnUse = false;
        for (DeviceDetail deviceDetail : deviceDetails) {
            if (deviceDetail.getStuDevicePair().getStudent() != null && deviceDetail.getStuDevicePair().getBaseDevice().getState() == BaseDeviceState.STATE_ONUSE) {
                return true;
            }
        }
        return isOnUse;


    }

    public synchronized void updateDevice(@NonNull BaseDeviceState deviceState) {
        int deviceId = deviceState.getDeviceId();
//        if (deviceState != null)
//            LogUtils.operation("??????????????????:deviceId=" + deviceId + ",deviceState=" + deviceState.toString());
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
            oneView.refreshDeviceState(pair.getBaseDevice());
            //????????????????????????
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
     * ????????????
     */
    private synchronized void doResult(final BaseStuPair pair, final int index) {
        if (pair.getStudent() == null)
            return;
        broadResult(pair);
        DeviceDetail detail = deviceDetails.get(index);
        String[] timeResult = detail.getStuDevicePair().getTimeResult();
        if (detail.getRound() > timeResult.length)//??????
            return;
        //??????????????????
        if (detail.getRound() - 1 < 0) {
            return;
        }
        timeResult[detail.getRound() - 1] = RoundResult.resultStateStr(pair.getResultState(), pair.getResult());
        detail.getStuDevicePair().setTimeResult(timeResult);
        oneView.setResultData(detail.getStuDevicePair());
        lastStu = pair.getStudent();
        lastResult = pair.getTimeResult();
        //????????????
        saveResult(pair, index);
        printResult(pair, index);
//        broadResult(pair);

        if (detail.getRound() < setTestCount()) {
            if (pair.getResultState() == RoundResult.RESULT_STATE_NORMAL && pair.isFullMark()) {
                //???????????????????????? ???????????????????????????
                detail.setRound(0);
                if (!isPenalize) {
                    //4????????????????????????
                    Message msg = new Message();
                    msg.what = pair.getBaseDevice().getDeviceId();
                    msg.obj = detail;
                    clearHandler.sendMessageDelayed(msg, 4000);
                }

                pair.setCanTest(true);
                pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
                return;
            }
            final int count = detail.getRound();
            if (detail.getRound() < setTestCount()) {
                boolean isAllTest = true;

                for (String s : timeResult) {
                    if (TextUtils.isEmpty(s)) {
                        isAllTest = false;
                    }
                }
                if (isAllTest) {
                    if (!isPenalize) {
                        //4????????????????????????
                        Message msg = new Message();
                        msg.what = pair.getBaseDevice().getDeviceId();
                        msg.obj = detail;
                        clearHandler.sendMessageDelayed(msg, 4000);
                        pair.setCanTest(true);
                        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
                    }
                    return;

                }
                int nextRount = getRound(timeResult);
                detail.setRound(nextRount);
                oneView.indexResult(deviceDetails.get(index).getRound() - 1);
                toastSpeak(String.format(getString(R.string.test_speak_hint), pair.getStudent().getSpeakStuName(), nextRount)
                        , String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), nextRount));
                LogUtils.operation((index + 1) + "?????????" + pair.getStudent().getStudentName());
                LogUtils.operation(String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), nextRount));
                if (!isNextClickStart) {
                    if (deviceDetails.size() == 1) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                deviceDetails.get(index).getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
                                deviceDetails.get(index).getStuDevicePair().setResult(-999);
                                oneView.indexResult(deviceDetails.get(index).getRound() - 1);
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
                //4????????????????????????
                Message msg = new Message();
                msg.obj = detail;
                clearHandler.sendMessageDelayed(msg, 4000);
            }


        }

        deviceListAdapter.notifyItemChanged(index);
        pair.setCanTest(true);
//        pair.getBaseDevice().setResultState(BaseDeviceState.STATE_FREE);

    }

    /**
     * ??????????????????
     */
    private int getRound(String[] timeResult) {
        int j = 0;
        for (int i = 0; i < timeResult.length; i++) {
            if (TextUtils.isEmpty(timeResult[i])) {
                return ++i;
            }
            j++;
        }
        return ++j;
    }

    private void printResult(BaseStuPair baseStuPair, int index) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        //???????????????????????????????????????????????????
        if (deviceDetails.get(index).getRound() < setTestCount() && !baseStuPair.isFullMark()) {
            return;
        }
        Student student = baseStuPair.getStudent();
//        PrinterManager.getInstance().print("\n");
        PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "??????");
        PrinterManager.getInstance().print("???  ???:" + student.getStudentCode());
        PrinterManager.getInstance().print("???  ???:" + student.getStudentName());
        for (int i = 0; i < baseStuPair.getTimeResult().length; i++) {
            if (!TextUtils.isEmpty(baseStuPair.getTimeResult()[i])) {
                PrinterManager.getInstance().print(String.format("??? %1$d ??????", i + 1) + baseStuPair.getTimeResult()[i]);
            } else {
                PrinterManager.getInstance().print(String.format("???%1$d??????", i + 1));
            }
        }
        PrinterManager.getInstance().print("????????????:" + TestConfigs.df.format(Calendar.getInstance().getTime()));
        PrinterManager.getInstance().print("\n");
    }

    private void saveResult(BaseStuPair baseStuPair, int index) {
        LogUtils.all("????????????:" + baseStuPair.toString());
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
        if (baseStuPair.getRoundNo() != 0) {
            roundResult.setRoundNo(baseStuPair.getRoundNo());
            roundResult.setResultTestState(RoundResult.RESULT_RESURVEY_STATE);
            baseStuPair.setRoundNo(0);
        } else {
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
            // ???????????????????????? ????????????????????????????????????????????????????????????
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && baseStuPair.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= baseStuPair.getResult()) {
                // ????????????????????????????????????????????????
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
            // ???????????????
            roundResult.setIsLastResult(1);
            updateLastResultLed(baseStuPair, index);


        }
        DBManager.getInstance().insertRoundResult(roundResult);
        LogUtils.operation("????????????:" + roundResult.toString());
        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        UploadResults uploadResults = new UploadResults(studentItem.getScheduleNo(), TestConfigs.getCurrentItemCode(),
                baseStuPair.getStudent().getStudentCode(), testNo + "", null, RoundResultBean.beanCope(roundResultList));


        uploadResult(uploadResults);
    }

    /**
     * ????????????
     *
     * @param uploadResults ????????????
     */
    private void uploadResult(UploadResults uploadResults) {
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort("?????????????????????????????????????????????");
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable(UploadResults.BEAN_KEY, uploadResults);
        serverIntent.putExtras(bundle);
        startService(serverIntent);
    }

    /**
     * LED????????????
     */
    private void updateLastResultLed(BaseStuPair baseStuPair, int index) {
//        int ledMode = SettingHelper.getSystemSetting().getLedMode();
//        String result = roundResult.getResultState() == RoundResult.RESULT_STATE_FOUL ? "X" : ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult());
//        if (ledMode == 0) {
//            int x = ResultDisplayUtils.getStringLength(result);
//            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), result, 16 - x, index, false, true);
//        } else {
//            byte[] data = new byte[16];
//            String str = "?????????";
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
//            String str1 = "?????????";
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
     * ??????????????????
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
                TtsManager.getInstance().speak(baseStuPair.getStudent().getSpeakStuName() + "??????");
            } else {

                TtsManager.getInstance().speak(String.format(getString(R.string.speak_result), baseStuPair.getStudent().getSpeakStuName(), ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult())));
            }


        }
    }

    public void refreshDevice(int index) {
        if (deviceDetails.get(index).getStuDevicePair().getBaseDevice() != null) {
            if (setDeviceCount() == 1) {
                 oneView.refreshDeviceState(deviceDetails.get(index).getStuDevicePair().getBaseDevice());
            }
            deviceListAdapter.notifyItemChanged(index);
        }
    }


    private void updateResultLed(BaseStuPair baseStu, int index) {
        int ledMode = SettingHelper.getSystemSetting().getLedMode();
        String result = (baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult());
        if (ledMode == 0) {
            int x = ResultDisplayUtils.getStringLength(result);
            if (baseStu.isFullMark()){
                int color = SettingHelper.getSystemSetting().getLedColor();
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), result, 16 - x, index, false, true,color);
            }else {
                int color = SettingHelper.getSystemSetting().getLedColor2();
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), result, 16 - x, index, false, true,color);

            }
//            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), result, 16 - x, index, false, true);
        } else {
            byte[] data = new byte[16];
            String str1 = "?????????";
            try {
                byte[] strData = str1.getBytes("GB2312");
                System.arraycopy(strData, 0, data, 0, strData.length);
                String res = (baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult());
                byte[] resultData = res.getBytes("GB2312");
                System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            int color = 0;
            if (baseStu.isFullMark()){
                color = SettingHelper.getSystemSetting().getLedColor();
            }else {
                color = SettingHelper.getSystemSetting().getLedColor2();
            }
            mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index, data, 0, 1, false, true,color);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 1, false, true,color);

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
            String str = "?????????";
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
                    toastSpeak("?????????,?????????????????????");
                } else {
                    startActivity(new Intent(this, LEDSettingActivity.class));
                }

                break;
            case R.id.tv_device_pair:
                if (isUse()) {
                    toastSpeak("?????????,?????????????????????");
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
            detail.getStuDevicePair().setRoundNo(0);
            detail.getStuDevicePair().setCanTest(true);
            deviceListAdapter.notifyDataSetChanged();
            oneView.initData(detail.getStuDevicePair());
            oneView.setBtnEnabled(false, false, false);
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
     * ??????????????????????????????
     */
    public abstract int setTestCount();

    /**
     * ??????????????????????????????????????? ?????????settingSP?????????initView?????????
     */
    public abstract int setDeviceCount();

    public abstract boolean isResultFullReturn(int sex, int result);

    /**
     * ????????????????????????
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
            ToastUtils.showShort("?????????????????????????????????");
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
        if (setDeviceCount() == 1) {
            return oneView.getAFRFrameLayoutResID();
        }
        return R.id.frame_camera;
    }
}

