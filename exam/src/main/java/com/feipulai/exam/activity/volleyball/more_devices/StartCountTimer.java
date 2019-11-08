package com.feipulai.exam.activity.volleyball.more_devices;

import android.os.CountDownTimer;

import com.feipulai.exam.activity.volleyball.VolleyBallSetting;

public class StartCountTimer extends CountDownTimer {
    public StartCountTimer(VolleyBallSetting setting) {
        super(setting.getTestTime(), 1000);
    }

    @Override
    public void onTick(long millisUntilFinished) {

    }

    @Override
    public void onFinish() {

    }
}
