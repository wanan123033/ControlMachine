package com.feipulai.exam.activity.RadioTimer.newRadioTimer;

import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class TimerTask {
    private TimeUpdateListener listener;
    private int period = 0;
    private Disposable disposable;
    public TimerTask(final TimeUpdateListener timeListener, final int period) {
        this.listener = timeListener;
        this.period = period;

    }

    public void keepTime() {

    }

    public void setStart() {
        disposable = Observable.interval(0, period, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Logger.i("accept-------->" + aLong);
                        if (listener != null)
                            listener.onTimeTaskUpdate(aLong.intValue()*period);
                    }
                });
    }

    public void stopKeepTime() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    public void release() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    public interface TimeUpdateListener {
        void onTimeTaskUpdate(int time);
    }
}
