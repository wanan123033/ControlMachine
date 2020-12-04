package com.feipulai.exam.activity.basketball;

import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.feipulai.common.jump_rope.task.GetDeviceStatesTask;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.BallManager;
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
public class BasketBallRadioFacade implements RadioManager.OnRadioArrivedListener {

    private BasketBallListener.BasketBallResponseListener listener;
    private BallManager ballManager;
    private ExecutorService mExecutor;
    private GetDeviceStatesTask mGetDeviceStatesTask;
    private int[] mCurrentConnect;
    public List<BallDeviceState> deviceStateList = new ArrayList<>();
    private List<Basketball868Result> timeRountList;
    private Map<String, Basketball868Result> numResult;
    private int interceptSecond = 5;

    public void setInterceptSecond(int interceptSecond) {
        this.interceptSecond = interceptSecond;
    }

    public BasketBallRadioFacade(int patternType, final BasketBallListener.BasketBallResponseListener listener) {
        this.listener = listener;
        mExecutor = Executors.newFixedThreadPool(2);
        ballManager = new BallManager(patternType);

        initDevice();
        //运行两个线程,分别处理获取设备状态和LED检录显示
        mGetDeviceStatesTask = new GetDeviceStatesTask(new GetDeviceStatesTask.OnGettingDeviceStatesListener() {
            @Override
            public void onGettingState(int position) {
                if (position == 0) {
                    ballManager.getRadioLedState(SettingHelper.getSystemSetting().getHostId());
                } else {
                    ballManager.getRadioState(SettingHelper.getSystemSetting().getHostId(), position);
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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

        // 开始之前先全部不动,等待开始
        pause();
        mExecutor.execute(mGetDeviceStatesTask);
    }

    private void initDevice() {
        for (int i = 0; i < getBallDeviceCount(); i++) {
            BallDeviceState deviceState = new BallDeviceState();
            deviceState.setDeviceId(i);
            deviceStateList.add(deviceState);
        }
        mCurrentConnect = new int[deviceStateList.size()];
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
            Log.i("zzs", "SerialConfigs=====>" + msg.what);

            if (msg.what == SerialConfigs.DRIBBLEING_START) {
                Basketball868Result result = (Basketball868Result) msg.obj;
                BasketballResult basketballResult = new BasketballResult();
                basketballResult.settNum(result.getDeviceId());
                Log.i("zzs", result.toString());
                setState(result);

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
                        ballManager.setRadioLedStartTime(SettingHelper.getSystemSetting().getHostId(), timeResult);
                        isledStartTime = true;
                        listener.getDeviceStatus(3);//设置计时状态
                        listener.triggerStart(basketballResult);//开始计时
                    }
                    if (result.getSum() != 0 && timeRountList.size() >= 2 && !numResult.containsKey(result.getMapKey())) { //获取拦截成绩
                        ballManager.setRadioPause(SettingHelper.getSystemSetting().getHostId());
                        numResult.put(result.getMapKey(), result);

                        Basketball868Result startTime = timeRountList.get(0);
                        long testTime = result.getInterceptTime() - startTime.getInterceptTime();

                        if (testTime < interceptSecond * 1000) {
                            return;
                        }
                        int[] time = TimeUtil.getTestResult(testTime);
                        if (time != null) {
                            basketballResult.setHour(time[0]);
                            basketballResult.setMinute(time[1]);
                            basketballResult.setSecond(time[2]);
                            basketballResult.setHund(time[3]);
                            //获取到多条成绩为停止状态
                            Log.i("zzzz", "getResult=====>" + basketballResult.toString());
                            String showLEDTime = DateUtil.caculateFormatTime(basketballResult.getResult(), TestConfigs.sCurrentItem.getDigital() == 0 ? 2 : TestConfigs.sCurrentItem.getDigital());
                            if (showLEDTime.charAt(0) == '0' && showLEDTime.charAt(1) == '0') {
                                showLEDTime = showLEDTime.substring(3, showLEDTime.toCharArray().length);
                            } else if (showLEDTime.charAt(0) == '0') {
                                showLEDTime = showLEDTime.substring(1, showLEDTime.toCharArray().length);
                            }
                            ballManager.setLedShowData(SettingHelper.getSystemSetting().getHostId(), showLEDTime, 2, Paint.Align.RIGHT);
                            listener.getResult(basketballResult);//获取拦截时间
                        }
                    }
                }

            } else if (msg.what == SerialConfigs.DRIBBLEING_FREE) {//空闲
                Log.i("zzzz", "  DRIBBLEING_FREE=====>");
                listener.getDeviceStatus(6);

            } else if (msg.what == SerialConfigs.DRIBBLEING_AWAIT) {//等待
                Log.i("zzzz", " DRIBBLEING_AWAIT=====>");
                listener.getDeviceStatus(2);
                isledStartTime = false;
                timeRountList = new ArrayList<>();
                numResult = new HashMap<>();
            } else if (msg.what == SerialConfigs.DRIBBLEING_STOP) {//停止
                Log.i("zzzz", " DRIBBLEING_STOP=====>");
                Basketball868Result result = (Basketball868Result) msg.obj;
                BasketballResult basketballResult = new BasketballResult();
//                ballManager.setRadioPause(SettingHelper.getSystemSetting().getHostId());
                basketballResult.setHour(result.getHour());
                basketballResult.setMinute(result.getMinth());
                basketballResult.setSecond(result.getSencond());
                basketballResult.setHund(result.getMinsencond());
                //获取到多条成绩为停止状态
                basketballResult.setUcStatus(4);
                listener.getStatusStop(basketballResult);
            } else if (msg.what == SerialConfigs.DRIBBLEING_LED_START_TIME) {
                listener.getDeviceStatus(3);//设置计时状态
            }
        }
    };

    private int getBallDeviceCount() {
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ) {
            return 2;
        }
        return 3;
    }


    private void setState(Basketball868Result result) {
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

//            if (TextUtils.isEmpty(deviceStateList.get(result.getDeviceName()).getDeviceSerial())) {
//                deviceStateList.get(result.getDeviceName()).setDeviceSerial(result.getSerialNumber());
//                mCurrentConnect[result.getDeviceName()] = BaseDeviceState.STATE_FREE;
//            } else if (TextUtils.equals(deviceStateList.get(result.getDeviceName()).getDeviceSerial(), result.getSerialNumber())) {
//                mCurrentConnect[result.getDeviceName()] = BaseDeviceState.STATE_FREE;
//            } else {
//                mCurrentConnect[result.getDeviceName()] = BaseDeviceState.STATE_CONFLICT;
//            }
//            if (deviceStateList.get(result.getDeviceName()).getState() != mCurrentConnect[result.getDeviceName()]) {
//                deviceStateList.get(result.getDeviceName()).setState(mCurrentConnect[result.getDeviceName()]);
//                EventBus.getDefault().post(new BaseEvent(result.getDeviceName(), EventConfigs.BALL_STATE_UPDATE));
//            }

        }
    }

}
