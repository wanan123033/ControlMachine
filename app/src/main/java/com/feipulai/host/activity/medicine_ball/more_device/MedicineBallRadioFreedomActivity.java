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
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.freedom.BaseFreedomTestActivity;
import com.feipulai.host.activity.medicine_ball.MedicineBallSetting;
import com.feipulai.host.activity.medicine_ball.MedicineBallSettingActivity;
import com.feipulai.host.activity.medicine_ball.MedicineConstant;
import com.feipulai.host.activity.medicine_ball.pair.MedicineBallPairActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.bean.DeviceDetail;
import com.feipulai.host.entity.RoundResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by pengjf on 2020/6/4.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MedicineBallRadioFreedomActivity extends BaseFreedomTestActivity {
    private static final String TAG = "RadioFreedomActivity";
    private MedicineBallSetting setting;
    public List<DeviceDetail> deviceDetails = new ArrayList<>();
    private int[] deviceState = {};
    private final int SEND_EMPTY = 1;
    private int beginPoint;
    @Override
    public void gotoItemSetting() {
        startActivity(new Intent(this, MedicineBallSettingActivity.class));
    }

    @Override
    public void startTest() {
        DeviceDetail deviceDetail = deviceDetails.get(0);
        BaseStuPair pair = deviceDetail.getStuDevicePair();
        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
        setDeviceState(pair.getBaseDevice());
        int id = pair.getBaseDevice().getDeviceId();
        sendStart((byte) id);
    }

    @Override
    public void stopTest() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sendEmpty();
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, MedicineBallSetting.class);
        if (null == setting) {
            setting = new MedicineBallSetting();
        }
        super.initData();

        txtDevicePair.setVisibility(View.VISIBLE);
        DeviceDetail detail = new DeviceDetail();
        detail.getStuDevicePair().getBaseDevice().setDeviceId(1);
        detail.getStuDevicePair().setTimeResult(new String[1]);
        detail.setDeviceOpen(true);
        deviceDetails.add(detail);
        setDeviceState(new BaseDeviceState(BaseDeviceState.STATE_ERROR));
        RadioManager.getInstance().setOnRadioArrived(medicineBall);
        deviceState = new int[deviceDetails.size()];
        beginPoint = Integer.parseInt(SharedPrefsUtil.getValue(this, "SXQ", "beginPoint", "0"));

    }

    @OnClick({R.id.txt_device_pair})
    public void devicePair(View view) {
        if (!isStartTest) {
            startActivity(new Intent(this, MedicineBallPairActivity.class));
        }
    }
    
    private void sendEmpty() {
        for (int i = 0; i < deviceDetails.size(); i++) {
            BaseDeviceState baseDevice = deviceDetails.get(i).getStuDevicePair().getBaseDevice();
            if (deviceState[i] == 0) {
                if (baseDevice.getState() != BaseDeviceState.STATE_ERROR) {
                    baseDevice.setState(BaseDeviceState.STATE_ERROR);
                    setDeviceState(baseDevice);
                }

            } else {
                if (baseDevice.getState() == BaseDeviceState.STATE_ERROR) {
                    baseDevice.setState(BaseDeviceState.STATE_NOT_BEGAIN);
                    setDeviceState(baseDevice);
                }
                deviceState[i] -= 1;
            }


        }
        for (DeviceDetail detail : deviceDetails) {
            int hostId = SettingHelper.getSystemSetting().getHostId();
            int deviceId = detail.getStuDevicePair().getBaseDevice().getDeviceId();
            MedicineBallMore.sendGetState(hostId, deviceId);
        }
        mHandler.sendEmptyMessageDelayed(SEND_EMPTY, 1000);
    }

    private void sendStart(byte id) {
        MedicineBallMore.sendStart(SettingHelper.getSystemSetting().getHostId(),id);
    }

    private void sendFree(int deviceId) {
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
                        toastSpeak(String.format("%s测量杆出现异常", "第" + e));
                    }
                }

            }
            deviceState[result.getDeviceId() - 1] = 0;//出现异常
            if (result.getState() == 1) {
                sendFree(result.getDeviceId());
            }
        } else {
            PROMPT_TIMES = 0;
            deviceState[result.getDeviceId() - 1] = 5;
        }

    }

    private MedicineBallImpl medicineBall = new MedicineBallImpl(new MedicineBallImpl.MainThreadDisposeListener() {
        @Override
        public void onResultArrived(MedicineBallNewResult result) {
            Log.i(TAG, result.toString());

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

            toastSpeak("请开始测试");

        }
    });

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
        if (result < beginPoint * 10 || result > 5000 * 10) {
            toastSpeak("数据异常，请重测");
            return;
        }

        if (stuPair == null || stuPair.getStudent() == null)
            return;

        stuPair.setEndTime(DateUtil.getCurrentTime());
        stuPair.setResult(result);
        settTestResult(stuPair);
        setDeviceState(new BaseDeviceState(BaseDeviceState.STATE_END, stuPair.getBaseDevice().getDeviceId()));

    }

    private void showValidResult(final int result, final BaseStuPair stuPair, final int deviceId) {
        final SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        alertDialog.setTitleText(String.format("%d号机%s成绩是否有效", deviceId, stuPair.getStudent().getStudentName()));
        alertDialog.setCancelable(false);
        alertDialog.setConfirmText("是").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                onResultArrived(result, stuPair);
                sweetAlertDialog.dismissWithAnimation();
            }
        }).setCancelText("否").setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sendFree(deviceId);
                stuPair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
                setDeviceState(stuPair.getBaseDevice());
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RadioManager.getInstance().setOnRadioArrived(null);
    }
}
