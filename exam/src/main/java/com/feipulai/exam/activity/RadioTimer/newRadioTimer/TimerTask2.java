package com.feipulai.exam.activity.RadioTimer.newRadioTimer;


import com.feipulai.common.utils.LogUtil;
import com.feipulai.exam.activity.basketball.util.TimerUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class TimerTask2 {
    private TimeUpdateListener timeUpdateListener;
    private int period = 0;
    private TimerUtil timerUtil;
    private TimerUtil timerIOUtil;

    public TimerTask2(final TimeUpdateListener timeListener, final int period) {
        this.timeUpdateListener = timeListener;
        this.period = period;
        timerUtil = new TimerUtil(new TimerUtil.TimerAccepListener() {
            @Override
            public void timer(Long time) {
                LogUtil.logDebugMessage(""+(time.intValue() * period));
                timeUpdateListener.onTimeTaskUpdate(time.intValue() * period);
            }
        });
        timerIOUtil = new TimerUtil(new TimerUtil.TimerAccepListener() {
            @Override
            public void timer(Long time) {
                timeUpdateListener.onTimeIOTaskUpdate(time.intValue() * (period+100));
            }
        });
    }


    public void keepTime() {
    }



    public void setStart() { 

        timerUtil.startTime(period);
        timerIOUtil.startTimeIO(period+100);
    }

    public void stopKeepTime() {

        timerUtil.stop();
        timerIOUtil.stop();
    }

    public void release() {

        timerUtil.stop();
        timerIOUtil.stop();
    }

    public interface TimeUpdateListener {
        void onTimeTaskUpdate(int time);

        void onTimeIOTaskUpdate(int time);
    }
}
