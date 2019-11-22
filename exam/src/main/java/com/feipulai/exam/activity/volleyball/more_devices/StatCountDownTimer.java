package com.feipulai.exam.activity.volleyball.more_devices;

import android.os.CountDownTimer;

import com.feipulai.exam.bean.DeviceDetail;

public class StatCountDownTimer extends CountDownTimer {
    private Listener listener;

    public StatCountDownTimer(long millisInFuture) {
        super(millisInFuture*1000, 1000);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        listener.onTick(millisUntilFinished);
    }

    @Override
    public void onFinish() {
        listener.onFinish();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener{
        void onTick(long millisUntilFinished);
        void onFinish();
    }
}
