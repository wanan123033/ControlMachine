package com.feipulai.exam.activity.basketball;

import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;

import com.feipulai.common.jump_rope.task.GetDeviceStatesTask;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.BallManager;
import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.Basketball868Result;
import com.feipulai.device.udp.result.BasketballResult;
import com.feipulai.exam.activity.basketball.bean.BallDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zzs on  2019/11/4
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public abstract class BaseRadioFacade implements RadioManager.OnRadioArrivedListener {

    private BasketBallListener.BasketBallResponseListener listener;
    private BallManager ballManager;
    private ExecutorService mExecutor;
    private GetDeviceStatesTask mGetDeviceStatesTask;
    private int[] mCurrentConnect;
    public List<BallDeviceState> deviceStateList = new ArrayList<>();
    private List<Basketball868Result> timeRountList;
    private Map<String, Basketball868Result> numResult = new ArrayMap<>();
    private int interceptSecond = 5;
    private Basketball868Result nearResult;
    private Basketball868Result farResult;
    private Basketball868Result ledResult;
    private int deviceVersion;
    private int autoAddTime = 0;
    private int useLedType = 0;//使用LED類型 0 標配 1 通用
    private int testState = 0; //0 空闲  1 等待 2 计时
    public static final int TEST_STATE_FREE = 0;
    public static final int TEST_STATE_BEGIN = 1;
    public static final int TEST_STATE_TIME = 2;
    //处理LED发送计时，避免重复发送指令
    boolean isledStartTime = false;

    public void setAutoAddTime(int autoAddTime) {
        this.autoAddTime = autoAddTime;
    }

    public int getUseLedType() {
        return useLedType;
    }

    public void setDeviceVersion(int deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public void setInterceptSecond(int interceptSecond) {
        this.interceptSecond = interceptSecond;
    }

    public BaseRadioFacade(int patternType, int autoAddTime, int useLedType, final BasketBallListener.BasketBallResponseListener listener) {
        this.listener = listener;
        this.autoAddTime = autoAddTime;
        this.useLedType = useLedType;
        mExecutor = Executors.newFixedThreadPool(2);
        ballManager = new BallManager(patternType);
        initDevice();
        //运行两个线程,分别处理获取设备状态和LED检录显示
        mGetDeviceStatesTask = new GetDeviceStatesTask(new GetDeviceStatesTask.OnGettingDeviceStatesListener() {
            @Override
            public void onGettingState(int position) {
//                if (position == 0) {
//                    ballManager.getRadioLedState(SettingHelper.getSystemSetting().getHostId());
//                } else {
//                    ballManager.getRadioState(SettingHelper.getSystemSetting().getHostId(), position);
//                }
                if (testState == TEST_STATE_BEGIN) {//等待只查起点
                    sendDeviceSelect(1, nearResult, farResult);
                } else {
                    sendDeviceSelect(position, nearResult, farResult);
                }


            }

            @Override
            public void onStateRefreshed() {
                int oldState;
                for (int i = 0; i < deviceStateList.size(); i++) {
                    BallDeviceState deviceState = deviceStateList.get(i);
                    oldState = deviceState.getState();
                    if (mCurrentConnect[deviceState.getDeviceId()] == 0
                            && oldState != BaseDeviceState.STATE_DISCONNECT
                            && oldState != BaseDeviceState.STATE_CONFLICT) {
                        deviceState.setState(BaseDeviceState.STATE_DISCONNECT);
                        EventBus.getDefault().post(new BaseEvent(deviceState, EventConfigs.BALL_STATE_UPDATE));
                    }
                }
                mCurrentConnect = new int[getBallDeviceCount()];
            }

            @Override
            public int getDeviceCount() {
                return deviceStateList.size();
            }
        });
        mGetDeviceStatesTask.setLoopCount(10);
        // 开始之前先全部不动,等待开始
        pause();
        mExecutor.execute(mGetDeviceStatesTask);
    }

    //设备查询
    public abstract void sendDeviceSelect(int position, Basketball868Result nearResult, Basketball868Result farResult);

    public abstract void sendAwait();

    public abstract boolean awaitDeviceState(Basketball868Result nearResult, Basketball868Result farResult);

    public abstract void awaitSucceed();

    private void initDevice() {
        for (int i = 0; i < getBallDeviceCount(); i++) {
            BallDeviceState deviceState = new BallDeviceState();
            deviceState.setDeviceId(i);
            deviceStateList.add(deviceState);
        }
        mCurrentConnect = new int[deviceStateList.size()];
    }

    private boolean isAwaitTrue = true;

    public void awaitState() {
        if (deviceVersion == 1) {
            isAwaitTrue = true;
            nearResult = null;
            farResult = null;
            ledResult = null;
            sendAwait();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (isAwaitTrue) {
                            if (awaitDeviceState(nearResult, farResult)) {
                                testState = TEST_STATE_BEGIN;
                                isAwaitTrue = false;
                                listener.getDeviceStatus(2);
                                ballManager.setRadioLEDStartAwait(SettingHelper.getSystemSetting().getHostId());
                                ballManager.waitTime(SettingHelper.getSystemSetting().getHostId(), TestConfigs.sCurrentItem.getDigital());
                                isledStartTime = false;
                                timeRountList = new ArrayList<>();
                                numResult = new HashMap<>();
                                awaitSucceed();
                            }
                        } else {
                            return;
                        }
                    }
                }
            }).start();
            //设置计时状态
            ballManager.setLedShowData(SettingHelper.getSystemSetting().getHostId(), "", 2, Paint.Align.CENTER);

        }

    }

    public void pause() {
        mGetDeviceStatesTask.pause();
    }

    public void resume() {
        mGetDeviceStatesTask.resume();
    }


    public void finish() {
        mGetDeviceStatesTask.finish();
        mExecutor.shutdownNow();
    }

    public boolean isDeviceNormal() {
        for (BallDeviceState deviceState : deviceStateList) {
            if (useLedType == 1 && deviceState.getDeviceId() == 0) {
                continue;
            }
            if (deviceState.getState() == BallDeviceState.STATE_DISCONNECT) {
                return false;
            }
        }
        return true;
    }


    public int getBallDeviceCount() {
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ) {
            return 2;
        }
        return 3;
    }

    public void radioResultTime(Basketball868Result result ){
        BasketballResult basketballResult = new BasketballResult();
        basketballResult.settNum(result.getDeviceId());
        setState(result);
        EventBus.getDefault().post(new BaseEvent(result, EventConfigs.BALL_STATE));
        if (result.getState() == 3 && timeRountList != null && result.getDeviceCode() != 2) {//计时

            //保存每一次拦截成绩
            if (result.getSum() != 0 && !numResult.containsKey(result.getMapKey())) {
                Log.i("zzzz", "  timeRountList.add=====>");
                timeRountList.add(result);

            }
            if (!isledStartTime) {
                Log.i("zzzz", "triggerStart=====>");
                numResult.put(result.getMapKey(), result);
                Basketball868Result timeResult = new Basketball868Result();
                timeResult.setHour(0);
                timeResult.setSencond(0);
                timeResult.setMinsencond(0);
                timeResult.setMinth(0);
                isledStartTime = true;
                listener.getDeviceStatus(3);//设置计时状态
                listener.triggerStart(basketballResult);//开始计时
                ballManager.setRadioLedStartTime(SettingHelper.getSystemSetting().getHostId(), timeResult);
                return;
            }
            if (result.getSum() != 0 && timeRountList.size() >= 2 && !numResult.containsKey(result.getMapKey())) { //获取拦截成绩
                ballManager.setRadioPause(SettingHelper.getSystemSetting().getHostId());

                numResult.put(result.getMapKey(), result);

                Basketball868Result startTime = timeRountList.get(0);
                long testTime = result.getInterceptTime() - startTime.getInterceptTime() + autoAddTime;

                if (testTime < interceptSecond * 1000) {
                    return;
                }
                int[] time = TimeUtil.getTestResult(testTime);
                if (time != null) {
                    basketballResult.setHour(time[0]);
                    basketballResult.setMinute(time[1]);
                    basketballResult.setSecond(time[2]);
                    basketballResult.setHund(time[3]);
                    listener.getResult(basketballResult);//获取拦截时间
                }
            }
        }
    }

    public void setState(Basketball868Result result) {
        //LED 0  子设备 1
        if (deviceStateList.size() == getBallDeviceCount()) {
            if (result.getDeviceId() > mCurrentConnect.length)
                return;
            if (mCurrentConnect[result.getDeviceId()] == BaseDeviceState.STATE_CONFLICT) {
                return;
            }
            mCurrentConnect[result.getDeviceId()] = BaseDeviceState.STATE_FREE;
            if (deviceStateList.get(result.getDeviceId()).getState() == BaseDeviceState.STATE_DISCONNECT) {
                deviceStateList.get(result.getDeviceId()).setState(mCurrentConnect[result.getDeviceId()]);
                EventBus.getDefault().post(new BaseEvent(deviceStateList.get(result.getDeviceId()), EventConfigs.BALL_STATE_UPDATE));
            }
        }
    }

}
