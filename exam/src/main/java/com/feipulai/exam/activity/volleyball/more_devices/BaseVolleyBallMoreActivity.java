package com.feipulai.exam.activity.volleyball.more_devices;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.AgainTestDialog;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.base.ResitDialog;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;
import com.feipulai.exam.activity.volleyball.adapter.DeviceListAdapter;
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
import com.feipulai.exam.view.StuSearchEditText;
import com.orhanobut.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public abstract class BaseVolleyBallMoreActivity extends BaseCheckActivity {
    @BindView(R.id.rv_device_list)
    RecyclerView ll_top;
    @BindView(R.id.et_input_text)
    StuSearchEditText etInputText;
    @BindView(R.id.lv_results)
    ListView lvResults;

    protected List<DeviceDetail> deviceDetails = new ArrayList<>();
    DeviceListAdapter deviceListAdapter;
    private int testNo;       //????????????
    protected LEDManager mLEDManager;
    private Intent serverIntent;
    private VolleyBallSetting setting;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_sargent_jump_more;
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
                gotoItemSetting();
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        });
    }

    protected abstract void gotoItemSetting();


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
        mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(),TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
        PrinterManager.getInstance().init();
        etInputText.setData(lvResults, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initLEDShow();
    }

    public void initLEDShow() {
        int ledMode = SettingHelper.getSystemSetting().getLedMode();
        int pairNum = setting.getPairNum();
        if (pairNum == 0) {
            pairNum = 4;
        }
        Log.e("TAG", "pairNum=" + pairNum);
        if (ledMode == 0) {
            for (int i = 0; i < pairNum; i++) {
                StringBuilder data = new StringBuilder();
                data.append(i + 1).append("??????");//1??????         ??????
                for (int j = 0; j < 7; j++) {
                    data.append(" ");
                }
                data.append("??????");
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data.toString(), 0, i, false, true);
            }
            for (int i = pairNum; i < 4; i++) {
                StringBuilder data = new StringBuilder();
                for (int j = 0; j < 16; j++) {
                    data.append(" ");
                }
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data.toString(), 0, i, false, true);
            }
        }
    }

    @Override
    protected void initData() {
        super.initData();
        setting = SharedPrefsUtil.loadFormSource(this, VolleyBallSetting.class);
        setDeviceCount(4);
        deviceListAdapter = new DeviceListAdapter(deviceDetails);
        deviceListAdapter.setTestCount(setting.getTestNo());

        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        ll_top.setLayoutManager(layoutManager);
        ((SimpleItemAnimator) ll_top.getItemAnimator()).setSupportsChangeAnimations(false);
        ll_top.setAdapter(deviceListAdapter);
        deviceListAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                switch (view.getId()) {
                    case R.id.txt_start:
                        sendStart(deviceDetails.get(i), i);
                        deviceDetails.get(i).setFinsh(false);
                        break;
                    case R.id.txt_end:
                        sendEnd(deviceDetails.get(i), i);
                        stuSkip(i);
                        deviceDetails.get(i).getStuDevicePair().setStudent(null);
                        refreshDevice(i);
//                        int hostId = SettingHelper.getSystemSetting().getHostId();
//                        int deviceId = (byte) deviceDetails.get(i).getStuDevicePair().getBaseDevice().getDeviceName();
//                        VolleyBallRadioManager.getInstance().deviceFree(hostId, deviceId);
                        deviceDetails.get(i).setFinsh(true);
                        break;
                    case R.id.txt_time:
                        sendTime(deviceDetails.get(i), i);

                        break;
                    case R.id.txt_gave_up:
                        sendGaveUp(deviceDetails.get(i), i);
                        deviceDetails.get(i).setFinsh(false);
                        break;
                    case R.id.txt_confirm:
                        sendConfirm(deviceDetails.get(i), i);
                        deviceDetails.get(i).setFinsh(true);
                        break;
                    case R.id.txt_penalty:
                        sendPenalty(deviceDetails.get(i), i);
                        deviceDetails.get(i).setFinsh(true);
                        break;
                    case R.id.txt_js:
                        stopCount(deviceDetails.get(i), i);
                        deviceDetails.get(i).setFinsh(false);
                        break;
                    case R.id.txt_fq:
                        fqCount(deviceDetails.get(i), i);
                        deviceDetails.get(i).setFinsh(true);
                        break;
                }
            }
        });
    }

    protected abstract void fqCount(DeviceDetail deviceDetail, int pos);

    public void setDeviceCount(int deviceCount) {
        deviceDetails.clear();
        VolleyBallSetting setting = SharedPrefsUtil.loadFormSource(this, VolleyBallSetting.class);
        for (int i = 0; i < deviceCount; i++) {
            final DeviceDetail detail = new DeviceDetail();
            detail.getStuDevicePair().getBaseDevice().setDeviceId(i + 1);
            detail.getStuDevicePair().setTimeResult(new String[setting.getTestNo()]);
            detail.setTestTime(setting.getTestTime());
            detail.setDeviceOpen(true);
            detail.setFinsh(true);
            detail.getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_ERROR);
            deviceDetails.add(detail);
        }
    }

    public abstract void sendStart(DeviceDetail deviceDetail, int pos);

    public abstract void sendEnd(DeviceDetail deviceDetail, int pos);

    public abstract void sendTime(DeviceDetail deviceDetail, int pos);

    public abstract void sendGaveUp(DeviceDetail deviceDetail, int pos);

    public abstract void sendConfirm(DeviceDetail deviceDetail, int pos);

    public abstract void stopCount(DeviceDetail deviceDetail, int pos);

    public abstract void sendPenalty(DeviceDetail deviceDetail, int pos);

    @Override
    public void onCheckIn(Student student) {
        final StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        final List<RoundResult> roundResultList = DBManager.getInstance().queryFinallyRountScoreByExamTypeList(student.getStudentCode(), studentItem.getExamType());
        testNo = roundResultList == null || roundResultList.size() == 0 ? 1 : roundResultList.get(0).getTestNo();
        //??????????????????????????????????????????????????????
        if (roundResultList != null && roundResultList.size() >= setTestCount()) {
            if (roundResultList != null && roundResultList.size() >= TestConfigs.getMaxTestCount(this)) {
                SystemSetting setting = SettingHelper.getSystemSetting();
                if (setting.isAgainTest() && setting.isResit()){
                    final Student finalStudent = student;
                    new SweetAlertDialog(this).setContentText("????????????????????????????")
                            .setCancelText("??????")
                            .setConfirmText("??????")
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    AgainTestDialog dialog = new AgainTestDialog();
                                    dialog.setArguments(finalStudent,roundResultList,studentItem);
                                    dialog.setOnIndividualCheckInListener(BaseVolleyBallMoreActivity.this);
                                    dialog.show(getSupportFragmentManager(),"AgainTestDialog");
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    ResitDialog dialog = new ResitDialog();
                                    dialog.setArguments(finalStudent,roundResultList,studentItem);
                                    dialog.setOnIndividualCheckInListener(BaseVolleyBallMoreActivity.this);
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
                    InteractUtils.toastSpeak(this, "??????????????????");
                }

            }
            return;
        } else if (roundResultList != null) {
            for (RoundResult roundResult : roundResultList) {
                if (roundResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && isResultFullReturn(student.getSex(), roundResult.getResult())) {
                    toastSpeak("??????");
                    return;
                }
            }
        }

        int index = 0;
        boolean canUseDevice = false;
        for (int i = 0; i < 4; i++) {
            if (deviceDetails.get(i).isDeviceOpen() &&
                    deviceDetails.get(i).getStuDevicePair().isCanTest() &&
                    deviceDetails.get(i).getStuDevicePair().getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
                index = i;
                canUseDevice = true;
                break;
            }
        }

        if (!canUseDevice) {
            toastSpeak("????????????????????????????????????");
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
        deviceDetails.get(index).getStuDevicePair().setRoundNo(roundResultList.size());
        deviceDetails.get(index).getStuDevicePair().setTimeResult(result);

        //??????????????????????????????????????????????????????????????????????????????????????????1????????????????????????+1
        if (roundResultList.size() == 0) {
            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
            testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1;
        } else {
            testNo = roundResultList.get(0).getTestNo();
        }
        addStudent(student, index, true);
        deviceListAdapter.notifyItemChanged(index);

        deviceFree(deviceDetails.get(index), index);
    }

    protected abstract void deviceFree(DeviceDetail deviceDetail, int index);

    protected void addStudent(Student student, int index, boolean b) {
        DeviceDetail deviceDetail = deviceDetails.get(index);
        deviceDetail.getStuDevicePair().setStudent(student);
        deviceDetail.getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
        deviceDetail.getStuDevicePair().setCanTest(false);
        deviceDetail.getStuDevicePair().setBaseHeight(0);
//        deviceDetail.setTestTime(setting.getTestTime());

        int count = deviceDetail.getRound();
        toastSpeak(String.format(getString(R.string.test_speak_hint), student.getStudentName(), count + 1)
                , String.format(getString(R.string.test_speak_hint), student.getStudentName(), count + 1));
        if (b)
            setShowLed(deviceDetail.getStuDevicePair(), index);

    }

    /**
     * led ??????
     *
     * @param pair
     */
    protected void setShowLed(BaseStuPair pair, int index) {
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
            RoundResult bestResult = DBManager.getInstance().queryBestScore(student.getStudentCode(), testNo);
            if (bestResult != null && bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
                byte[] data = new byte[16];
                String str = "?????????";
                try {
                    byte[] strData = str.getBytes("GB2312");
                    System.arraycopy(strData, 0, data, 0, strData.length);
                    byte[] resultData = ResultDisplayUtils.getStrResultForDisplay(bestResult.getResult()).getBytes("GB2312");
                    System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index + 1, data, 0, 2, false, true);
            } else {
                mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), index + 1, "?????????", 0, 2, false, true);
            }
        }
    }

    protected void stuSkip(int pos) {
        deviceDetails.get(pos).setTestTime(0);

        deviceDetails.get(pos).getStuDevicePair().setStudent(null);
        deviceDetails.get(pos).getStuDevicePair().setCanTest(true);
        deviceDetails.get(pos).getStuDevicePair().setTimeResult(new String[setTestCount()]);
        deviceListAdapter.notifyItemChanged(pos);
    }

    @OnClick({R.id.txt_led_setting, R.id.tv_device_pair})
    public void onViewClicked(View view) {
        if (!isExit()) {
            toastSpeak("??????????????????");
            return;
        }
        switch (view.getId()) {
            case R.id.txt_led_setting:
                startActivity(new Intent(this, LEDSettingActivity.class));
                break;
            case R.id.tv_device_pair:
                startActivity(new Intent(this, VolleyBallPairActivity.class));
                break;
        }
    }

    /**
     * ??????????????????
     *
     * @param baseStu
     */
    public synchronized void updateResult(@NonNull BaseStuPair baseStu) {
        int deviceId = baseStu.getBaseDevice().getDeviceId();
        Log.e("TAG", "deviceId=" + deviceId);
        BaseStuPair pair = null;
        int index = 0;
        for (int i = 0; i < 4; i++) {
            int id = deviceDetails.get(i).getStuDevicePair().getBaseDevice().getDeviceId();
            Log.e("TAG", "id=" + id);
            if (id == deviceId) {
                pair = deviceDetails.get(i).getStuDevicePair();
                index = i;
                break;
            }
        }
        Log.e("TAG", "pair=" + pair);
        if (null != pair.getBaseDevice()) {
            pair.setResultState(baseStu.getResultState());
            pair.setResult(baseStu.getResult());
            pair.setFullMark(baseStu.isFullMark());
            if (pair.getLEDupdate())
                updateResultLed(baseStu, index);
            refreshDevice(index);

        }
    }@Override
    public void setRoundNo(Student student, int roundNo) {

    }
    public void saveResult(final BaseStuPair baseStuPair, final int index) {
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
        roundResult.setPenaltyNum(baseStuPair.getPenaltyNum());
        roundResult.setResultState(1);
//        roundResult.setTestTime(TestConfigs.df.format(Calendar.getInstance().getTime()));
        roundResult.setTestTime(baseStuPair.getTestTime());
        roundResult.setRoundNo(baseStuPair.getRoundNo() + 1);
        roundResult.setTestNo(testNo);
        roundResult.setExamType(studentItem.getExamType());
        roundResult.setScheduleNo(studentItem.getScheduleNo());
        roundResult.setUpdateState(0);
        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
        RoundResult bestResult = DBManager.getInstance().queryBestScore(baseStuPair.getStudent().getStudentCode(), testNo);
        if (bestResult != null)
            Log.e("TAG======",bestResult.toString());
        if (bestResult != null) {
            // ???????????????????????? ????????????????????????????????????????????????????????????
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && baseStuPair.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= baseStuPair.getResult()) {
                // ????????????????????????????????????????????????
                roundResult.setIsLastResult(1);
                bestResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(bestResult);
                updateLastResultLed(roundResult, index);
            } else {
                if (bestResult.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                    roundResult.setIsLastResult(1);
                    bestResult.setIsLastResult(0);
                    DBManager.getInstance().updateRoundResult(bestResult);
                    updateLastResultLed(roundResult, index);
                } else {
                    roundResult.setIsLastResult(0);
                    updateLastResultLed(bestResult, index);
                }
            }
        } else {
            // ???????????????
            roundResult.setIsLastResult(1);
            updateLastResultLed(roundResult, index);
        }
        roundResult.setEndTime(System.currentTimeMillis()+"");

        DBManager.getInstance().insertRoundResult(roundResult);
        Logger.i("saveResult==>insertRoundResult->" + roundResult.toString());
        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        UploadResults uploadResults = new UploadResults(studentItem.getScheduleNo(), TestConfigs.getCurrentItemCode(),
                baseStuPair.getStudent().getStudentCode(), testNo + "", null, RoundResultBean.beanCope(roundResultList));

        uploadResult(uploadResults);

        printResult(baseStuPair);
    }

    protected void updateResultLed(BaseStuPair baseStu, int index) {
        int ledMode = SettingHelper.getSystemSetting().getLedMode();
        byte[] data = new byte[16];
        if (ledMode == 0) {
            if (baseStu.getStudent() != null) {

                try {
                    String str = baseStu.getStudent().getStudentName();
                    String result = ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult());
                    if (baseStu.getResult() == 0 && baseStu.getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN) {
                        result = "??????";
                    } else if (baseStu.getBaseDevice().getState() == BaseDeviceState.STATE_ONUSE) {
                        result = baseStu.getResult() + "??????";
                    } else if (baseStu.getBaseDevice().getState() == BaseDeviceState.STATE_END) {
                        result = baseStu.getResult() + "??????";
                    }
                    byte[] strData = str.getBytes("GB2312");
                    System.arraycopy(strData, 0, data, 0, strData.length);
                    byte[] resultData = result.getBytes("GB2312");
                    System.arraycopy(resultData, 0, data, data.length - resultData.length, resultData.length);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, index, false, true);
            }
        }
    }

    protected void refreshDevice(int index) {
        Log.e("TAG--------","------refreshDevice,----index="+index);
        if (deviceDetails.get(index).getStuDevicePair().getBaseDevice() != null) {
            deviceListAdapter.notifyItemChanged(index);
        }
    }

    private int setTestCount() {
        return setting.getTestNo();
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
    private void updateLastResultLed(RoundResult roundResult, int index) {
//        int ledMode = SettingHelper.getSystemSetting().getLedMode();
//        if (ledMode == 0) {
//            String result = ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult());
//            int x = 7;
//            if (roundResult.getResult()< 1000){
//                x= 12;
//            }else if (roundResult.getResult()>= 1000 && roundResult.getResult()< 10000){
//                x= 10;
//            }else if (roundResult.getResult()>= 10000){
//                x= 8 ;
//            }
//            Log.e("TAGP",result+","+x);
//            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), result, x, index, false, true);
//        }
    }

    protected void updateTime(int time, int pos) {
        deviceDetails.get(pos).setTestTime(time);
//        deviceListAdapter.notifyItemChanged(pos);

    }

    public synchronized void updateDevice(@NonNull BaseDeviceState deviceState) {
        int deviceId = deviceState.getDeviceId();
        BaseStuPair pair = null;
        int index = 0;
        for (int i = 0; i < 4; i++) {
            int id = deviceDetails.get(i).getStuDevicePair().getBaseDevice().getDeviceId();
            if (id == deviceId) {
                pair = deviceDetails.get(i).getStuDevicePair();
                index = i;
                break;
            }

        }

        if (pair != null) {
            pair.getBaseDevice().setState(deviceState.getState());
            //????????????????????????
            if (deviceState.getState() == BaseDeviceState.STATE_END) {
                if (pair.getStudent() != null) {
                    Logger.i("??????" + pair.getStudent().toString());
                }
                Logger.i("??????????????????STATE_END==>" + deviceState.toString());
                pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                pair.setCanTest(true);
            }
        }
        refreshDevice(index);
    }

    protected void printResult(BaseStuPair baseStuPair) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        Student student = baseStuPair.getStudent();
        List<RoundResult> roundResults = DBManager.getInstance().queryRountScore(student.getStudentCode());
        //???????????????????????????????????????????????????
        if (roundResults.size() < setTestCount() && !baseStuPair.isFullMark()) {
            return;
        }
//        PrinterManager.getInstance().print("\n");
        PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "??????");
        PrinterManager.getInstance().print("???  ???:" + student.getStudentCode());
        PrinterManager.getInstance().print("???  ???:" + student.getStudentName());
        for (int i = 0; i < roundResults.size(); i++) {
            int result = roundResults.get(i).getResult();
            if (result != 0) {
                String strResultForDisplay = ResultDisplayUtils.getStrResultForDisplay(result);
                PrinterManager.getInstance().print(String.format("???%1$d??????", i + 1) + strResultForDisplay + getPenalty(roundResults.get(i).getPenaltyNum()));
            } else {
                PrinterManager.getInstance().print(String.format("???%1$d??????", i + 1) + "0???(??????:0)");
            }
        }
        PrinterManager.getInstance().print("????????????:" + TestConfigs.df.format(Calendar.getInstance().getTime()));
        PrinterManager.getInstance().print("\n");
    }

    private String getPenalty(int penaltyNum) {
        if (penaltyNum == 0) {
            return "(??????:" + penaltyNum + ")";
        } else {
            return "(??????:-" + penaltyNum + ")";
        }

    }

    /**
     * ???????????????????????????????????????
     *
     * @param studentCode ??????
     * @param examType    ???????????? 0.?????????1.?????????2.??????
     * @param sex         ??????  0-???  1-???
     * @return ?????????(0, 1, 2 ????????????)  (-1 ????????????)
     */
    protected int getRoundNo(String studentCode, int examType, int sex) {
        List<RoundResult> roundResultList = DBManager.getInstance().queryFinallyRountScoreByExamTypeList(studentCode, examType);
        if (setting.isFullSkip()) {
            int rounNo = 0;
            for (RoundResult result : roundResultList) {
                if (sex == 0) {
                    rounNo = result.getResult() >= setting.getMaleFullScore() ? -1 : roundResultList.size();
                } else if (sex == 1) {
                    rounNo = result.getResult() >= setting.getFemaleFullScore() ? -1 : roundResultList.size();
                }
                if (rounNo == -1) {
                    break;
                }
            }
            return rounNo;
        } else {
            if (roundResultList.size() >= setting.getTestNo()) {
                return -1;
            }
            return roundResultList.size();
        }
    }

    public abstract boolean isResultFullReturn(int sex, int result);

    @Override
    public void finish() {

        if (!isExit()) {
            toastSpeak("??????????????????");
            return;
        }
        super.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && !isExit()) {
            toastSpeak("??????????????????");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * ??????????????????
     *
     * @return true ??????  false ?????????
     */
    protected boolean isExit() {
        boolean isExit = false;
        for (int i = 0; i < deviceDetails.size(); i++) {
            isExit = deviceDetails.get(i).isFinal();
            if (!isExit) {
                break;
            }
        }
        return isExit;
    }
}
