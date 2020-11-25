package com.feipulai.exam.activity.sport_timer;

import android.util.Log;

import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.exam.activity.setting.SettingHelper;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SportPresent implements SportContract.Presenter{

    private Disposable disposable;
    SportTimerManger sportTimerManger;
    private boolean connect;
    private SportContract.SportView sportView;
    private volatile int runState;//0空闲 1等待 2结束
    SportPresent (SportContract.SportView sportView ){
        sportTimerManger = new SportTimerManger();
        connect = true;
        RadioManager.getInstance().setOnRadioArrived(sportResultListener);
        this.sportView = sportView;
    }
    @Override
    public void rollConnect() {
        disposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (connect){
                            if (getRunState() == 0){
                                sportTimerManger.connect(1, SettingHelper.getSystemSetting().getHostId());
                            }
                            if (getRunState() == 1){
                                sportTimerManger.getRecentCache(1,SettingHelper.getSystemSetting().getHostId());
                            }
                        }
                    }
                });
    }

    public void stop() {
        if (disposable != null) {
            disposable.dispose();
        }
        RadioManager.getInstance().setOnRadioArrived(null);
    }


    @Override
    public void setContinueRoll(boolean connect) {
        this.connect = connect;
    }

    @Override
    public void waitStart() {
        sportTimerManger.syncTime(1,SettingHelper.getSystemSetting().getHostId(),getTime());
        sportTimerManger.getTime(1,SettingHelper.getSystemSetting().getHostId());
    }

    private SportResultListener sportResultListener = new SportResultListener(new SportResultListener.SportMsgListener() {
        @Override
        public void onConnect(SportResult result) {
            sportView.updateDeviceState(result.getDeviceId(),1);
        }

        @Override
        public void onGetTime() {
            sportView.getTimeUpdate();
            sportTimerManger.setDeviceState(1,SettingHelper.getSystemSetting().getHostId(),1);
        }

        @Override
        public void onGetResult(SportResult result) {
            Log.i("SportResultListener",result.toString());

        }
    });

    /**
     * 返回当前时间精确到毫秒 不要年月日
     * @return
     */
    private int getTime(){
        Calendar Cld = Calendar.getInstance();
        int HH = Cld.get(Calendar.HOUR_OF_DAY);
        int mm = Cld.get(Calendar.MINUTE);
        int SS = Cld.get(Calendar.SECOND);
        int MI = Cld.get(Calendar.MILLISECOND);
        return HH*60*60*1000+mm*60*1000+SS*1000+MI;
    }

    public void setRunState(int runState) {
        this.runState = runState;
    }

    private int getRunState(){
        return runState;
    }
}
