package com.feipulai.host.activity.radio_timer.newRadioTimer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimerTask {
    private TimeUpdateListener timeUpdateListener;
    private ExecutorService checkService;
    private TimeRunnable timeRunnable;

    public TimerTask(TimeUpdateListener timeUpdateListener, int period) {
        this.timeUpdateListener = timeUpdateListener;
        checkService = Executors.newSingleThreadScheduledExecutor();
        timeRunnable = new TimeRunnable(period);
    }

    private volatile long disposeTime;
    private boolean keepTime;
    private boolean isTrue = true;

    public void keepTime() {
        checkService.submit(timeRunnable);
    }

    private class TimeRunnable implements Runnable {
        private int period;
        private int realTime;


        TimeRunnable(int period) {
            this.period = period;
        }

        @Override
        public void run() {
            while (isTrue) {
                if (keepTime) {
//                    realTime = (int) (SystemClock.elapsedRealtime()-disposeTime);
                    realTime = (int) (System.currentTimeMillis() - disposeTime);
                    timeUpdateListener.onTimeTaskUpdate(realTime);
                    try {
                        Thread.sleep(period);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void setStart() {
//        disposeTime = SystemClock.elapsedRealtime();
        disposeTime = System.currentTimeMillis();
        keepTime = true;
    }

    public void stopKeepTime() {
        keepTime = false;
        disposeTime = 0;
    }

    public void release() {
        if (null != checkService) {
            isTrue = false;
            checkService.shutdownNow();
            stopKeepTime();
        }
    }

    public interface TimeUpdateListener {
        void onTimeTaskUpdate(int time);
    }
}
