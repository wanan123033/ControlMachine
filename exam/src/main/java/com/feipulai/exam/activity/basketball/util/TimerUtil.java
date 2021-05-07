package com.feipulai.exam.activity.basketball.util;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zzs on  2019/6/24
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class TimerUtil {

    public TimerUtil(TimerAccepListener listener) {
        this.listener = listener;
    }

    private TimerAccepListener listener;
    private Disposable disposable;

    public void startTime(long period) {
        disposable = Observable.interval(0, period, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
//                        Logger.i("accept-------->" + aLong);
                        if (listener != null)
                            listener.timer(aLong);
                    }
                });
    }

    public void startTime(long period, TimeUnit timeUnit) {
        disposable = Observable.interval(0, period, timeUnit)
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
//                        Logger.i("accept-------->" + aLong);
                        if (listener != null)
                            listener.timer(aLong);
                    }
                });
    }

    public void stop() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    public interface TimerAccepListener {
        void timer(Long time);
    }
}
