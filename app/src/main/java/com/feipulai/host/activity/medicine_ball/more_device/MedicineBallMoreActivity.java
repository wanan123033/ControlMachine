package com.feipulai.host.activity.medicine_ball.more_device;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.MedicineBallMore;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.MedicineBallNewResult;

import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseMoreActivity;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.medicine_ball.MedicineBallSetting;
import com.feipulai.host.activity.medicine_ball.MedicineBallSettingActivity;
import com.feipulai.host.activity.medicine_ball.MedicineConstant;
import com.feipulai.host.activity.medicine_ball.pair.MedicineBallPairActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.bean.DeviceDetail;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;


public class MedicineBallMoreActivity extends BaseMoreActivity {
    private static final String TAG = "MedicineMoreActivity";
    private int[] deviceState = {};
    private MedicineBallSetting setting;

    private final int SEND_EMPTY = 1;
    private int beginPoint;
    private boolean using;
    private boolean isResume;
    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, MedicineBallSetting.class);
        if (null == setting) {
            setting = new MedicineBallSetting();
        }
        super.initData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.operation("MedicineBallMoreActivity onResume");
        setting = SharedPrefsUtil.loadFormSource(this, MedicineBallSetting.class);
        if (null == setting) {
            setting = new MedicineBallSetting();
        }
        Logger.i(TAG + ":medicineBallSetting ->" + setting.toString());
//        setDeviceCount(setting.getSpDeviceCount());
        deviceState = new int[setting.getTestDeviceCount()];

        for (int i = 0; i < deviceState.length; i++) {

            deviceState[i] = 0;//??????5???????????????????????????
        }
        beginPoint = Integer.parseInt(SharedPrefsUtil.getValue(this, "SXQ", "beginPoint", "0"));

        RadioManager.getInstance().setOnRadioArrived(medicineBall);
        isResume = true;
        if (isResume){
            sendEmpty();
        }

    }

    private void sendEmpty() {
        LogUtils.operation("MedicineBallMoreActivity sendEmpty");
        Log.i(TAG, "james_send_empty");
        for (int i = 0; i < deviceDetails.size(); i++) {
            BaseDeviceState baseDevice = deviceDetails.get(i).getStuDevicePair().getBaseDevice();
            if (deviceState[i] == 0) {
                if (baseDevice.getState() != BaseDeviceState.STATE_ERROR) {
                    baseDevice.setState(BaseDeviceState.STATE_ERROR);
                    updateDevice(baseDevice);
                }

            } else {
                if (baseDevice.getState() == BaseDeviceState.STATE_ERROR) {
                    baseDevice.setState(BaseDeviceState.STATE_NOT_BEGAIN);
                    updateDevice(baseDevice);
                }
                deviceState[i] -= 1;
            }


        }
        for (DeviceDetail detail : deviceDetails) {
            int hostId =  SettingHelper.getSystemSetting().getHostId();
            int deviceId = detail.getStuDevicePair().getBaseDevice().getDeviceId();
            MedicineBallMore.sendGetState(hostId,deviceId);
        }
        mHandler.sendEmptyMessageDelayed(SEND_EMPTY, 1000);
    }



    @Override
    public int setTestDeviceCount() {
        return setting.getTestDeviceCount();
    }



    @Override
    public void gotoItemSetting() {
        if (using) {
            toastSpeak("???????????????,????????????");
            return;
        }
        startActivity(new Intent(this, MedicineBallSettingActivity.class));
    }

    @Override
    public void sendTestCommand(BaseStuPair pair, int index) {
        LogUtils.operation("????????????:pair="+pair.toString()+"----index="+index);
        pair.setStartTime(DateUtil.getCurrentTime());
        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
        updateDevice(pair.getBaseDevice());
        int id = pair.getBaseDevice().getDeviceId();
        sendStart((byte) id);
        using = true;
    }


    private void sendStart(byte id) {
        LogUtils.operation("MedicineBallMoreActivity sendStart id="+id);
        MedicineBallMore.sendStart(SettingHelper.getSystemSetting().getHostId(),id);
    }

    private void sendFree(int deviceId) {
        LogUtils.operation("MedicineBallMoreActivity sendFree deviceId="+deviceId);
        MedicineBallMore.sendEmpty(SettingHelper.getSystemSetting().getHostId(),deviceId);
    }

    private boolean isInCorrect;
    private int PROMPT_TIMES;

    private void disposeDevice(MedicineBallNewResult result) {
        isInCorrect = result.isInCorrect();
        if (isInCorrect) {
            PROMPT_TIMES++;
            if (PROMPT_TIMES >= 2 && PROMPT_TIMES < 4) {
                int[] errors = result.getIncorrectPoles();
                for (int i = 1; i < errors.length + 1; i++) {
                    if (errors[i - 1] == 1) {
                        int e = errors[i] + 1;
                        toastSpeak(String.format("%s?????????????????????", "???" + e));
                    }
                }

            }
            deviceState[result.getDeviceId() - 1] = 0;//????????????
            if (result.getState() == 1) {
                sendFree(result.getDeviceId());
            }
        } else {
            PROMPT_TIMES = 0;
            deviceState[result.getDeviceId() - 1] = 5;
        }

    }

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MedicineConstant.GET_SCORE_RESPONSE:
                    MedicineBallNewResult result = (MedicineBallNewResult) msg.obj;
                    for (DeviceDetail detail : deviceDetails) {
                        if (detail.getStuDevicePair().getBaseDevice().getDeviceId() == result.getDeviceId()) {
                            int dbResult = result.getResult() * 10 + beginPoint * 10;
                            detail.getStuDevicePair().setResultState(result.isFault() ? RoundResult.RESULT_STATE_FOUL : RoundResult.RESULT_STATE_NORMAL);
                            if (result.getSweepPoint() < 2) {
                                showValidResult(dbResult, detail.getStuDevicePair(), result.getDeviceId());
                            } else {
                                onResultArrived(dbResult, detail.getStuDevicePair());
                            }
                            break;
                        }

                    }
                    break;
                case SEND_EMPTY:
                    sendEmpty();
                    break;
            }

            return false;
        }
    });

    private void onResultArrived(int result, BaseStuPair stuPair) {
        using = false;
        if (result < beginPoint * 10 || result > 5000 * 10) {
            toastSpeak("????????????????????????");
            return;
        }

        if (stuPair == null || stuPair.getStudent() == null)
            return;

        stuPair.setEndTime(DateUtil.getCurrentTime());
        stuPair.setResult(result);
        updateResult(stuPair);
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END, stuPair.getBaseDevice().getDeviceId()));

    }


    @Override
    protected void onStop() {
        super.onStop();
        isResume = false;
        LogUtils.operation("MedicineBallMoreActivity onStop");
//        mHandler.removeCallbacksAndMessages(null);
//        RadioManager.getInstance().setOnRadioArrived(null);
    }

    @OnClick({R.id.tv_device_pair})
    public void onClick(View view) {
        LogUtils.operation("MedicineBallMoreActivity ???????????????????????????");
        startActivity(new Intent(this, MedicineBallPairActivity.class));
    }

    private MedicineBallImpl medicineBall = new MedicineBallImpl(new MedicineBallImpl.MainThreadDisposeListener() {
        @Override
        public void onResultArrived(final MedicineBallNewResult result) {
            LogUtils.operation("MedicineBallMoreActivity ???????????????:"+result.toString());

            // MedicineBallNewResult{result=50, fault=false, sweepPoint=1, deviceId=2, frequency=0, state=2}
            if (result.getState() == 2) {
                Message msg = mHandler.obtainMessage();
                msg.obj = result;
                msg.what = MedicineConstant.GET_SCORE_RESPONSE;
                mHandler.sendMessage(msg);
                sendFree(result.getDeviceId());
            }
            disposeDevice(result);
        }



        @Override
        public void onStopTest() {

        }

        @Override
        public void onStarTest(int deviceId) {

            Student student = deviceDetails.get(deviceId - 1).getStuDevicePair().getStudent();
            if (student != null) {
                toastSpeak(MessageFormat.format("???{0}????????????", student.getStudentName()));
            }
        }
    });

    private void showValidResult(final int result, final BaseStuPair stuPair, final int deviceId) {
        final SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        alertDialog.setTitleText(String.format("%d??????%s??????????????????", deviceId, stuPair.getStudent().getStudentName()));
        alertDialog.setCancelable(false);
        alertDialog.setConfirmText("???").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                onResultArrived(result, stuPair);
                sweetAlertDialog.dismissWithAnimation();
            }
        }).setCancelText("???").setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sendFree(deviceId);
                stuPair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
                updateDevice(stuPair.getBaseDevice());
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.operation("MedicineBallMoreActivity onDestroy");
        RadioManager.getInstance().setOnRadioArrived(null);
        mHandler.removeCallbacksAndMessages(null);
    }
}
