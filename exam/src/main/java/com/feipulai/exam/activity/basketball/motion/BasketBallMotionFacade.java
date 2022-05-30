package com.feipulai.exam.activity.basketball.motion;

import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;

import com.feipulai.common.jump_rope.task.GetDeviceStatesTask;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.BallManager;
import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.Basketball868Result;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.device.udp.result.BasketballResult;
import com.feipulai.exam.activity.basketball.BasketBallListener;
import com.feipulai.exam.activity.basketball.TimeUtil;
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
public class BasketBallMotionFacade implements RadioManager.OnRadioArrivedListener {

    private BasketBallListener.BasketBallResponseListener listener;
    private BallManager ballManager;
    private ExecutorService mExecutor;
    private GetDeviceStatesTask mGetDeviceStatesTask;
    private int[] mCurrentConnect;
    public List<BallDeviceState> deviceStateList = new ArrayList<>();
    private List<SportResult> timeRountList;
    private Map<String, SportResult> numResult = new ArrayMap<>();
    private int interceptSecond = 5;
    private Basketball868Result nearResult;
    private Basketball868Result farResult;
    private Basketball868Result ledResult;
    private int deviceVersion;
    private int patternTypes;
    private SportTimerManger manager;
    private volatile int[] sendIndex;
    private int testState = 0; //0 空闲  1 等待 2 计时
    private int autoAddTime = 0;
    private int useLedType = 0;//使用LED類型 0 標配 1 通用
    public static final int TEST_STATE_FREE = 0;
    public static final int TEST_STATE_BEGIN = 1;
    public static final int TEST_STATE_TIME = 2;
    public void setTestState(int testState) {
        this.testState = testState;
    }
    public int getUseLedType() {
        return useLedType;
    }

    public void setAutoAddTime(int autoAddTime) {
        this.autoAddTime = autoAddTime;
    }

    public void setDeviceVersion(int deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public void setInterceptSecond(int interceptSecond) {
        this.interceptSecond = interceptSecond;
    }

    public BasketBallMotionFacade(int patternType, int autoAddTime, int useLedType, final BasketBallListener.BasketBallResponseListener listener) {
        this.listener = listener;
        this.autoAddTime = autoAddTime;
        this.patternTypes = patternType;
        this.useLedType = useLedType;
        mExecutor = Executors.newFixedThreadPool(2);
        ballManager = new BallManager(patternType);
        manager = new SportTimerManger();
        initDevice();
        //运行两个线程,分别处理获取设备状态和LED检录显示
        mGetDeviceStatesTask = new GetDeviceStatesTask(new GetDeviceStatesTask.OnGettingDeviceStatesListener() {
            @Override
            public void onGettingState(int position) {
//                if (position == 0) {
//                    ballManager.getRadioLedState(SettingHelper.getSystemSetting().getHostId());
//                } else {
//                    if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ) {
//                        if (nearResult != null && nearResult.getState() == 3) {
//                            manager.getRecentCache(position, SettingHelper.getSystemSetting().getHostId(), sendIndex[position]);
//                        } else {
//                            manager.getDeviceState(position, SettingHelper.getSystemSetting().getHostId());
//                        }
//                    } else {
//                        if (nearResult != null && farResult != null && nearResult.getState() == 3 && farResult.getState() == 3) {
//                            manager.getRecentCache(position, SettingHelper.getSystemSetting().getHostId(), sendIndex[position]);
//                        } else {
//                            manager.getDeviceState(position, SettingHelper.getSystemSetting().getHostId());
//                        }
//                    }
//
//
//                }
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                if (testState == TEST_STATE_BEGIN) {//等待只查起点
                    if (nearResult != null && farResult != null && nearResult.getState() == 3 && farResult.getState() == 3) {
                        manager.getRecentCache(1, SettingHelper.getSystemSetting().getHostId(), sendIndex[position]);
                    } else {
                        manager.getDeviceState(1, SettingHelper.getSystemSetting().getHostId());
                    }
                } else {
                    if (position == 0) {
                            ballManager.getRadioLedState(SettingHelper.getSystemSetting().getHostId());
                    } else {
                        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ) {
                            if (nearResult != null && nearResult.getState() == 3) {
                                manager.getRecentCache(position, SettingHelper.getSystemSetting().getHostId(), sendIndex[position]);
                            } else {
                                manager.getDeviceState(position, SettingHelper.getSystemSetting().getHostId());
                            }
                        } else {
                            if (nearResult != null && farResult != null && nearResult.getState() == 3 && farResult.getState() == 3) {
                                manager.getRecentCache(position, SettingHelper.getSystemSetting().getHostId(), sendIndex[position]);
                            } else {
                                manager.getDeviceState(position, SettingHelper.getSystemSetting().getHostId());
                            }
                        }
                    }
                }
                if (testState == TEST_STATE_FREE) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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

    private void initDevice() {
        mCurrentConnect = new int[getBallDeviceCount()];
        sendIndex = new int[getBallDeviceCount()];
        for (int i = 0; i < getBallDeviceCount(); i++) {
            BallDeviceState deviceState = new BallDeviceState();
            deviceState.setDeviceId(i);
            deviceStateList.add(deviceState);
            sendIndex[i] = 1;
        }

    }
    private boolean isAwaitTrue = true;
    public void awaitState() {
        isAwaitTrue=true;
        nearResult = null;
        farResult = null;
        ledResult = null;
         manager.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 1);
        manager.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 1);
        for (int i = 1; i < getBallDeviceCount(); i++) {
            manager.getDeviceState(i , SettingHelper.getSystemSetting().getHostId());
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (isAwaitTrue) {
                        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ) {
                            if (nearResult != null && nearResult.getState() == 3) {
                                testState = TEST_STATE_BEGIN;
                                listener.getDeviceStatus(2);
                                ballManager.setRadioLEDStartAwait(SettingHelper.getSystemSetting().getHostId());
                                ballManager.waitTime(SettingHelper.getSystemSetting().getHostId(), TestConfigs.sCurrentItem.getDigital());

                                isledStartTime = false;
                                if (timeRountList != null) {
                                    timeRountList.clear();
                                }
                                if (numResult != null) {
                                    numResult.clear();
                                }
                                timeRountList = new ArrayList<>();
                                numResult = new HashMap<>();
                                for (int i = 0; i < sendIndex.length; i++) {
                                    sendIndex[i] = 1;
                                }
                                isAwaitTrue = false;
                            }
                        } else {
                            if (nearResult != null && farResult != null && nearResult.getState() == 3 && farResult.getState() == 3) {
                                testState = TEST_STATE_BEGIN;
                                listener.getDeviceStatus(2);
                                ballManager.setRadioLEDStartAwait(SettingHelper.getSystemSetting().getHostId());
                                ballManager.waitTime(SettingHelper.getSystemSetting().getHostId(), TestConfigs.sCurrentItem.getDigital());

                                isledStartTime = false;
                                if (timeRountList != null) {
                                    timeRountList.clear();
                                }
                                if (numResult != null) {
                                    numResult.clear();
                                }
                                timeRountList = new ArrayList<>();
                                numResult = new HashMap<>();
                                for (int i = 0; i < sendIndex.length; i++) {
                                    sendIndex[i] = 1;
                                }
                                isAwaitTrue = false;
                            }
                        }

                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        return;
                    }
                }
            }
        }).start();
        int time = TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ ? 500 : 700;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


            }
        }, time);

        //设置计时状态
        ballManager.setLedShowData(SettingHelper.getSystemSetting().getHostId(), "", 2, Paint.Align.CENTER);

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

    //处理LED发送计时，避免重复发送指令
    boolean isledStartTime = false;

    @Override
    public void onRadioArrived(Message msg) {
        myHandler.sendMessage(msg);
    }

    private Handler myHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == SerialConfigs.DRIBBLEING_START) {
                Basketball868Result result = (Basketball868Result) msg.obj;
                if (result.getDeviceId() == 0) {
                    ledResult = result;
                }
                if (result.getDeviceId() == 1) {
                    nearResult = result;
                }
                if (result.getDeviceId() == 2) {
                    farResult = result;
                }
                BasketballResult basketballResult = new BasketballResult();
                basketballResult.settNum(result.getDeviceId());
                setState(result);
                EventBus.getDefault().post(new BaseEvent(result, EventConfigs.BALL_STATE));

            } else if (msg.what == SerialConfigs.DRIBBLEING_LED_START_TIME) {
                listener.getDeviceStatus(3);//设置计时状态
            } else if (msg.what == SerialConfigs.SPORT_TIMER_CONNECT) {
                SportResult result = (SportResult) msg.obj;
                setDeviceState(result);
            } else if (msg.what == SerialConfigs.SPORT_TIMER_GET_TIME) {
                //获取子机时间 误差2S同步时间
                if (msg.obj instanceof SportResult) {
                    SportResult sportResult = (SportResult) msg.obj;
                    if (sportResult.getLongTime() > 0) {
                        if (Math.abs(DateUtil.getTime() - sportResult.getLongTime()) > 2000) {
                            manager.syncTime(SettingHelper.getSystemSetting().getHostId(), DateUtil.getTime());
                        }
                    } else {
                        setDeviceState(sportResult);
                    }
                }
            } else if (msg.what == SerialConfigs.SPORT_TIMER_RESULT) {
                SportResult sportResult = (SportResult) msg.obj;
                if (sportResult.getDeviceId() == 0 || (sportResult.getDeviceId() - 1) >= mCurrentConnect.length)
                    return;
                setDeviceState(sportResult);
                if (sportResult.getDeviceState() == 0) {
                    return;//非计时状态成绩无效
                }

                if (sportResult.getLongTime() == 0xFFFFFFFF || sportResult.getLongTime() == 0) {
                    return;
                }
                if (sportResult.getSumTimes() > 0) {
                    for (int i = 1; i < sendIndex.length; i++) {
                        if (i == sportResult.getDeviceId()) {
                            if (sportResult.getSumTimes() < sendIndex[i]) {
                                sendIndex[i] = sportResult.getSumTimes();
                            } else if (sportResult.getSumTimes() >= sendIndex[i]) {
                                sendIndex[i]++;
                            }
                        }

                    }
                }

                if (sportResult.getSumTimes() != 0 && numResult != null && !numResult.containsKey(sportResult.getMapKey())) {
                    Log.i("zzzz", "  timeRountList.add=====>");
                    timeRountList.add(sportResult);

                }
                BasketballResult basketballResult = new BasketballResult();
                basketballResult.settNum(sportResult.getDeviceId());
                if (!isledStartTime) {
                    //第一次拦截为折返 过滤
                    if (patternTypes == 5 && sportResult.getDeviceId() == 2) {
                        return;
                    }
                    Log.i("zzzz", "triggerStart=====>");

                    numResult.put(sportResult.getMapKey(), sportResult);
                    Basketball868Result timeResult = new Basketball868Result();
                    timeResult.setHour(0);
                    timeResult.setSencond(0);
                    timeResult.setMinsencond(0);
                    timeResult.setMinth(0);
                    isledStartTime = true;
                    listener.getDeviceStatus(3);//设置计时状态
                    listener.triggerStart(basketballResult);//开始计时
                    ballManager.setRadioLedStartTime(SettingHelper.getSystemSetting().getHostId(), timeResult);
                    testState = TEST_STATE_TIME;
                    return;
                }
                if (sportResult.getSumTimes() != 0 && timeRountList.size() >= 2 && !numResult.containsKey(sportResult.getMapKey())) { //获取拦截成绩
                    testState = TEST_STATE_TIME;
                    ballManager.setRadioPause(SettingHelper.getSystemSetting().getHostId());
                    numResult.put(sportResult.getMapKey(), sportResult);

                    SportResult startTime = timeRountList.get(0);
                    long testTime = sportResult.getLongTime() - startTime.getLongTime() + autoAddTime;

                    if (testTime < interceptSecond * 1000) {
                        return;
                    }
                    int[] time = TimeUtil.getTestResult(testTime);
                    basketballResult.settNum(sportResult.getDeviceId());
                    if (time != null) {
                        basketballResult.setHour(time[0]);
                        basketballResult.setMinute(time[1]);
                        basketballResult.setSecond(time[2]);
                        basketballResult.setHund(time[3]);
                        //获取到多条成绩为停止状态
                        listener.getResult(basketballResult);//获取拦截时间
                    }


                }

            }
        }
    };

    public void setDeviceState(SportResult sportResult) {
        Basketball868Result basketball868Result = new Basketball868Result();
        basketball868Result.setDeviceId(sportResult.getDeviceId());
        switch (sportResult.getDeviceState()) {
            case 0:
                basketball868Result.setState(1);
                break;
            case 1:
                basketball868Result.setState(3);
                break;
        }

        if (basketball868Result.getDeviceId() == 1) {
            nearResult = basketball868Result;
        }
        if (basketball868Result.getDeviceId() == 2) {
            farResult = basketball868Result;
        }
        setState(basketball868Result);
        EventBus.getDefault().post(new BaseEvent(basketball868Result, EventConfigs.BALL_STATE));
    }

    private int getBallDeviceCount() {
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ) {
            return 2;
        }
        return 3;
    }


    private void setState(Basketball868Result result) {
        //LED 0  子设备 1
        if (deviceStateList.size() == getBallDeviceCount()) {
            if (result.getDeviceId() >= mCurrentConnect.length)
                return;
            if (mCurrentConnect[result.getDeviceId()] == BaseDeviceState.STATE_CONFLICT) {
                return;
            }
            mCurrentConnect[result.getDeviceId()] = BaseDeviceState.STATE_FREE;
            if (deviceStateList.get(result.getDeviceId()).getState() == BaseDeviceState.STATE_DISCONNECT) {
                deviceStateList.get(result.getDeviceId()).setState(mCurrentConnect[result.getDeviceId()]);
                EventBus.getDefault().post(new BaseEvent(deviceStateList.get(result.getDeviceId()), EventConfigs.BALL_STATE_UPDATE));
            }

//            if (TextUtils.isEmpty(deviceStateList.get(result.getDeviceName()).getDeviceSerial())) {
//                deviceStateList.get(result.getDeviceName()).setDeviceSerial(result.getSerialNumber());
//                mCurrentConnect[result.getDeviceName()] = BaseDeviceState.STATE_FREE;
//            } else if (TextUtils.equals(deviceStateList.get(result.getDeviceName()).getDeviceSerial(), result.getSerialNumber())) {
//                mCurrentConnect[result.getDeviceName()] = BaseDeviceState.STATE_FREE;
//            } else {
//                mCurrentConnect[result.getDeviceName()] = BaseDeviceState.STATE_CONFLICT;
//            }
//            if (deviceStateList.get(result.getDeviceName()).getResultState() != mCurrentConnect[result.getDeviceName()]) {
//                deviceStateList.get(result.getDeviceName()).setResultState(mCurrentConnect[result.getDeviceName()]);
//                EventBus.getDefault().post(new BaseEvent(result.getDeviceName(), EventConfigs.BALL_STATE_UPDATE));
//            }

        }
    }

}
