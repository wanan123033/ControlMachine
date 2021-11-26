package com.feipulai.exam.activity.RadioTimer.newRadioTimer;

import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class TimerKeeper {
    private TimeUpdateListener timeUpdateListener;

    public TimerKeeper(TimeUpdateListener timeUpdateListener){
        this.timeUpdateListener = timeUpdateListener;
    }

    private Disposable disposable;
    private volatile long disposeTime;
    private boolean keepTime;
    private int temp;
    public void keepTime() {
        disposable = Observable.interval(0, 100, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong)  {
                        if (keepTime){
                            temp = (int) (SystemClock.elapsedRealtime()-disposeTime) ;
                            timeUpdateListener.onTimeUpdate(( temp));
                        }
                    }
                });
    }

    public void setStartInit(){
        disposeTime = SystemClock.elapsedRealtime();
        keepTime = true;
    }

    public void stopKeepTime(){
        keepTime = false;
        disposeTime = 0;
        temp = 0;
    }

    public void release() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    interface TimeUpdateListener{
         void onTimeUpdate(int time);
    }
}
