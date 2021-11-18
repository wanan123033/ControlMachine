package com.feipulai.exam.activity.RadioTimer.newRadioTimer;

import android.os.SystemClock;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerTask {
    private TimeUpdateListener listener;
    private int period ;
    private ScheduledExecutorService checkService;
    private volatile long disposeTime;
    private volatile boolean keepTime;
    public TimerTask(final TimeUpdateListener timeListener, final int period) {
        this.listener = timeListener;
        this.period = period;

    }

    public void keepTime() {
        checkService = Executors.newSingleThreadScheduledExecutor();
        checkService.scheduleWithFixedDelay(checkRun, 1000, period, TimeUnit.MILLISECONDS);
    }

    private CheckRun checkRun = new CheckRun();

    private class CheckRun implements Runnable {

        @Override
        public void run() {
            intervalRun();
        }
    }

    private void intervalRun() {
        if (keepTime){
            int realTime = (int) (SystemClock.elapsedRealtime() - disposeTime);
            listener.onTimeTaskUpdate(realTime);
        }
    }

    public void setStart() {
        keepTime = true;
        disposeTime  = SystemClock.elapsedRealtime();
    }

    public void stopKeepTime() {
        keepTime = false;
        disposeTime = 0;
    }

    public void release() {
        if (null != checkService) {
            checkService.shutdownNow();
            stopKeepTime();
        }
    }

    public interface TimeUpdateListener {
        void onTimeTaskUpdate(int time);
    }
}
