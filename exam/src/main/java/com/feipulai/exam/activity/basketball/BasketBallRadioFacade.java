package com.feipulai.exam.activity.basketball;

import android.os.Message;
import android.text.TextUtils;

import com.feipulai.common.jump_rope.task.GetDeviceStatesTask;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.BallManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.Basketball868Result;
import com.feipulai.exam.activity.basketball.bean.BallDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
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
    private List<BallDeviceState> deviceStateList;

    public BasketBallRadioFacade(int patternType, final List<BallDeviceState> deviceStateList, final BasketBallListener.BasketBallResponseListener listener) {
        this.listener = listener;
        this.deviceStateList = deviceStateList;
        mExecutor = Executors.newFixedThreadPool(2);
        ballManager = new BallManager(patternType);
        mCurrentConnect = new int[deviceStateList.size()];
        //运行两个线程,分别处理获取设备状态和LED检录显示
        mGetDeviceStatesTask = new GetDeviceStatesTask(new GetDeviceStatesTask.OnGettingDeviceStatesListener() {
            @Override
            public void onGettingState(int position) {
                if (getDeviceCount() == position) {
                    ballManager.getRadioLedState(SettingHelper.getSystemSetting().getHostId());
                } else {
                    ballManager.getRadioState(SettingHelper.getSystemSetting().getHostId(), position);
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
                        EventBus.getDefault().post(new BaseEvent(i, EventConfigs.BALL_STATE_UPDATE));
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

    @Override
    public void onRadioArrived(Message msg) {
        if (msg.what == SerialConfigs.DRIBBLEING_START) {
            Basketball868Result result = (Basketball868Result) msg.obj;
            setState(result);
        }

    }

    private int getBallDeviceCount() {
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ) {
            return 2;
        }
        return 3;
    }



    private void setState(Basketball868Result result) {
        if (deviceStateList.size() == getBallDeviceCount()) {
            if (mCurrentConnect[result.getDeviceId()] == BaseDeviceState.STATE_CONFLICT) {
                return;
            }
            if (TextUtils.isEmpty(deviceStateList.get(result.getDeviceId()).getDeviceSerial())) {
                deviceStateList.get(result.getDeviceId()).setDeviceSerial(result.getSerialNumber());
                mCurrentConnect[result.getDeviceId()] = BaseDeviceState.STATE_FREE;
            } else if (TextUtils.equals(deviceStateList.get(result.getDeviceId()).getDeviceSerial(), result.getSerialNumber())) {
                mCurrentConnect[result.getDeviceId()] = BaseDeviceState.STATE_FREE;
            } else {
                mCurrentConnect[result.getDeviceId()] = BaseDeviceState.STATE_CONFLICT;
            }

            if (deviceStateList.get(result.getDeviceId()).getState() != mCurrentConnect[result.getDeviceId()]) {
                deviceStateList.get(result.getDeviceId()).setState(mCurrentConnect[result.getDeviceId()]);
                EventBus.getDefault().post(new BaseEvent(result.getDeviceId(), EventConfigs.BALL_STATE_UPDATE));
            }

        }
    }

}
