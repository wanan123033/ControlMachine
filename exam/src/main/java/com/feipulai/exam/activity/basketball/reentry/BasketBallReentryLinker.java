package com.feipulai.exam.activity.basketball.reentry;

import android.os.Message;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.Basketball868Result;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;

import org.greenrobot.eventbus.EventBus;

public class BasketBallReentryLinker
        extends SitPullLinker {
    int deviceCount = 0;

    public BasketBallReentryLinker(int machineCode, int targetFrequency, int deviceCount, SitPullPairListener listener) {
        super(machineCode, targetFrequency, listener);
        this.deviceCount = deviceCount;
    }

    public boolean onRadioArrived(Message paramMessage) {
        if (paramMessage.what == SerialConfigs.DRIBBLEING_FREQUENCY) {
            EventBus.getDefault().post(new BaseEvent(paramMessage.obj, EventConfigs.BACKBALL_FREQUENCY_DATA));
        }
        if (((paramMessage.what == SerialConfigs.DRIBBLEING_PARAMETER)
                || (paramMessage.what == SerialConfigs.DRIBBLEING_FREQUENCY))) {
            Basketball868Result result = (Basketball868Result) paramMessage.obj;
            if (this.machineCode == ItemDefault.CODE_LQYQ || machineCode == ItemDefault.CODE_ZQYQ) {
                if ((currentDeviceId == deviceCount - 1) && (result.getDeviceCode() == 2)) {
                    if (paramMessage.what == SerialConfigs.DRIBBLEING_PARAMETER) {//设置成功LED id为0 ，设置配对设备ID为0
                        currentDeviceId = 0;
                    }
                    checkDevice(result.getDeviceId(), result.getFrequency());
                } else if ((currentDeviceId != 2) && (result.getDeviceCode() != 2)) {
                    checkDevice(result.getDeviceId(), result.getFrequency());
                }
            }
            return true;

        }
        if ((machineCode == ItemDefault.CODE_LQYQ || machineCode == ItemDefault.CODE_ZQYQ) && paramMessage.what == SerialConfigs.SPORT_TIMER_MATCH) {
            SportResult result = (SportResult) paramMessage.obj;
            checkDevice(result.getDeviceId(), result.getFrequency(), result.getHostId());
            return true;
        }
        return super.onRadioArrived(paramMessage);
    }

    public synchronized void checkDevice(int deviceId, int frequency, int hostId) {
        if (currentFrequency == 0) {
            // 0频段接收到的结果,肯定是设备的开机广播
            if (frequency == TARGET_FREQUENCY && deviceId == currentDeviceId && hostId == SettingHelper.getSystemSetting().getHostId()) {
                onNewDeviceConnect();
//                if (machineCode == ItemDefault.CODE_SPORT_TIMER){
//                    listener.setFrequency(currentDeviceId, hostId, TARGET_FREQUENCY);
//                }
            } else {
                listener.setFrequency(currentDeviceId, hostId, TARGET_FREQUENCY);

                currentFrequency = TARGET_FREQUENCY;
                // 那个铁盒子就是有可能等这么久才收到回复
                mHandler.sendEmptyMessageDelayed(NO_PAIR_RESPONSE_ARRIVED, 5000);
            }
        } else if (currentFrequency == TARGET_FREQUENCY) {
            //在主机的目的频段收到的,肯定是设置频段后收到的设备广播
            if (deviceId == currentDeviceId && frequency == TARGET_FREQUENCY) {
                onNewDeviceConnect();
            }
        }
    }

}
