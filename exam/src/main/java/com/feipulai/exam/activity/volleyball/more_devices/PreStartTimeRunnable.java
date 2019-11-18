package com.feipulai.exam.activity.volleyball.more_devices;

import android.content.Context;
import android.os.SystemClock;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.VolleyBallRadioManager;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;
import com.feipulai.exam.bean.DeviceDetail;

public class PreStartTimeRunnable implements Runnable {
    private int time;
    private int hostId;
    private int deviceId;
    private int timeSum;
    private TimeListener listener;
    public PreStartTimeRunnable(Context context,DeviceDetail deviceDetail) throws IllegalAccessException {
//        time = deviceDetail.getPreTime();
        time = 0;
        if (time < 0){
            throw new IllegalAccessException("VolleyBall PreTime < 0");
        }
        hostId = SettingHelper.getSystemSetting().getHostId();
        deviceId = deviceDetail.getStuDevicePair().getBaseDevice().getDeviceId();
        VolleyBallSetting setting = SharedPrefsUtil.loadFormSource(context,VolleyBallSetting.class);
        timeSum = setting.getTestTime();
    }
    @Override
    public void run() {
        int tt = time;
        if (time == 0){
            VolleyBallRadioManager.getInstance().startTime(hostId, deviceId, 0, timeSum);
            if (listener != null){
                listener.startTime();
            }
        }else {
            for (int i = time; i >= 0; i--) {
                if (i == 0) {
                    VolleyBallRadioManager.getInstance().startTime(hostId, deviceId, 0, timeSum);
                    if (listener != null) {
                        listener.startTime();
                    }
                } else {
                    VolleyBallRadioManager.getInstance().startTime(hostId, deviceId, i, timeSum);
                    SystemClock.sleep(1000);

                    if (listener != null) {
                        listener.preTime(tt--);
                    }
                }
            }
        }
    }

    public void setListener(TimeListener listener) {
        this.listener = listener;
    }

    interface TimeListener{
        void startTime();
        void preTime(int time);
    }
}
