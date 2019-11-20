package com.feipulai.exam.activity.volleyball.more_devices;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

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
    private boolean flag = true;

    public PreStartTimeRunnable(Context context, DeviceDetail deviceDetail) throws IllegalAccessException {
//        time = deviceDetail.getPreTime();
        time = 5;
        if (time < 0) {
            throw new IllegalAccessException("VolleyBall PreTime < 0");
        }
        hostId = SettingHelper.getSystemSetting().getHostId();
        deviceId = deviceDetail.getStuDevicePair().getBaseDevice().getDeviceId();
        VolleyBallSetting setting = SharedPrefsUtil.loadFormSource(context, VolleyBallSetting.class);
        timeSum = setting.getTestTime();
    }

    @Override
    public void run() {
        int tt = time;
        if (time == 0) {
            Log.i("PreStartTimeRunnable","startTime---------0");
            VolleyBallRadioManager.getInstance().startTime(hostId, deviceId, 0, timeSum);
            if (listener != null) {
                listener.startTime();
            }
        } else {
            for (int i = time; i >= 0; i--) {
                if (flag) {
                    if (i == 0) {
                        VolleyBallRadioManager.getInstance().startTime(hostId, deviceId, 0, timeSum);
                        Log.i("PreStartTimeRunnable","startTime");
                        if (listener != null) {
                            listener.startTime();
                        }
                    } else {
                        if (listener != null) {
                            listener.preTime(i);
                        }
                        Log.i("PreStartTimeRunnable","preTime");
                        VolleyBallRadioManager.getInstance().startTime(hostId, deviceId, i, timeSum);
                        SystemClock.sleep(1000);
                    }
                } else {
                    break;
                }
            }
        }
    }

    public void stop() {
        flag = false;
    }

    public void setListener(TimeListener listener) {
        this.listener = listener;
    }

    interface TimeListener {
        void startTime();

        void preTime(int time);
    }
}
