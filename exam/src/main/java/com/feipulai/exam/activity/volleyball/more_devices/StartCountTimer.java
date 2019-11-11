package com.feipulai.exam.activity.volleyball.more_devices;

import android.os.CountDownTimer;

import com.feipulai.exam.activity.volleyball.VolleyBallSetting;

public class StartCountTimer extends CountDownTimer {
    private CountDownTimerListener listener;
    private int position;

    public StartCountTimer(VolleyBallSetting setting, CountDownTimerListener listener) {
        super(setting.getTestTime() * 1000L, 1000);
        this.listener = listener;
    }

    public void setPosition(int position){
        this.position = position;
    }
    @Override
    public void onTick(long millisUntilFinished) {
        listener.updateTime((int) (millisUntilFinished / 1000L),position);
    }

    @Override
    public void onFinish() {
//        listener.sendEnd()
    }

    public interface CountDownTimerListener{

        void updateTime(int millisUntilFinished, int position);
    }
}
