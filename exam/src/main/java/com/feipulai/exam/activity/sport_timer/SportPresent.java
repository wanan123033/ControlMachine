package com.feipulai.exam.activity.sport_timer;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;

import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.sport_timer.bean.SportTimeResult;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SportPresent implements SportContract.Presenter {

    private Disposable disposable;
    SportTimerManger sportTimerManger;
    private boolean connect;
    private SportContract.SportView sportView;
    private volatile int runState;//0空闲 1等待 2结束
    private int deviceCount;
    private int[] checkState;
    private int[] disConnect;
    private int[] sendIndex;
    private int newTime = -1;
    SportPresent(SportContract.SportView sportView, int deviceCount) {
        sportTimerManger = new SportTimerManger();
        RadioManager.getInstance().setOnRadioArrived(sportResultListener);
        this.sportView = sportView;
        this.deviceCount = deviceCount;
        checkState = new int[deviceCount];
        disConnect = new int[deviceCount];
        sendIndex = new int[deviceCount];
        for (int i = 0; i < checkState.length; i++) {
            checkState[i] = 0;
            disConnect[i] = 0;
            sendIndex[i] = 0;
        }
    }

    @Override
    public void rollConnect() {
        disposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (connect) {
                            if (getRunState() == 0) {
                                for (int i = 0; i < deviceCount; i++) {
                                    sportTimerManger.connect(i + 1, SettingHelper.getSystemSetting().getHostId());
                                }

                            }
                            if (getRunState() == 1) {
                                for (int i = 0; i < deviceCount; i++) {
                                    sportTimerManger.getRecentCache(i + 1, SettingHelper.getSystemSetting().getHostId(),sendIndex[i]);
                                }

                            }

                            for (int i = 0; i < checkState.length; i++) {
                                if (checkState[i] == 0) {
                                    disConnect[i]++;
                                    if (disConnect[i] > 3) {
                                        sportView.updateDeviceState(i + 1, 0);
                                    }
                                } else {
                                    sportView.updateDeviceState(i + 1, 1);
                                    checkState[i] = 0;
                                    disConnect[i] = 0;
                                }
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
        sportTimerManger.syncTime(1, SettingHelper.getSystemSetting().getHostId(), getTime());
        sportTimerManger.getTime(1, SettingHelper.getSystemSetting().getHostId());
    }

    private SportResultListener sportResultListener = new SportResultListener(new SportResultListener.SportMsgListener() {
        @Override
        public void onConnect(SportResult result) {
//            sportView.updateDeviceState(result.getDeviceId(),1);
            checkState[result.getDeviceId() - 1] = 1;
        }

        @Override
        public void onGetTime() {
            sportView.getTimeUpdate();
            sportTimerManger.setDeviceState(1, SettingHelper.getSystemSetting().getHostId(), 1);
        }

        @Override
        public void onGetResult(SportResult result) {
            Log.i("SportResultListener", result.toString());
            checkState[result.getDeviceId() - 1] = 1;
            sendIndex[result.getDeviceId()-1] = result.getSumTimes() == 0? 0:(result.getSumTimes()-1);
            Log.i("SportResultListener", result.getLongTime()+"----"+newTime);
            if (result.getSumTimes()!= 0){
                if (result.getLongTime()>newTime) {
                    newTime = result.getLongTime();
                    sportView.receiveResult(result);
                }
            }
        }
    });

    /**
     * 返回当前时间精确到毫秒 不要年月日
     *
     * @return
     */
    private int getTime() {
        Calendar Cld = Calendar.getInstance();
        int HH = Cld.get(Calendar.HOUR_OF_DAY);
        int mm = Cld.get(Calendar.MINUTE);
        int SS = Cld.get(Calendar.SECOND);
        int MI = Cld.get(Calendar.MILLISECOND);
        return HH * 60 * 60 * 1000 + mm * 60 * 1000 + SS * 1000 + MI;
    }

    public void setRunState(int runState) {
        this.runState = runState;
    }

    private int getRunState() {
        return runState;
    }

    /**
     *
     * @param students
     * @param context
     * @param results
     * @param trackNoMap 序号集合
     */
    public void print(List<Student> students , Context context, Map<Student,List<RoundResult>> results,Map<Student, Integer> trackNoMap) {
        InteractUtils.printResults(null, students, results,
                TestConfigs.getMaxTestCount(context),trackNoMap);
    }

    public void setDeviceStateStop(){
        sportTimerManger.setDeviceState(1, SettingHelper.getSystemSetting().getHostId(), 0);
        for (int i = 0; i < checkState.length; i++) {
            sendIndex[i] = 0;
        }
    }

    public void showStudent(LinearLayout llStuDetail, Student student, int testNo){
        List<RoundResult> scoreResultList = new ArrayList<>();
        RoundResult result = DBManager.getInstance().queryBestScore(student.getStudentCode(), testNo);
        if (result != null) {
            scoreResultList.add(result);
        }
        InteractUtils.showStuInfo(llStuDetail, student, scoreResultList);
    }
}
