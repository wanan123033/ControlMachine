package com.feipulai.exam.activity.pullup.pair;

import android.os.Message;
import android.util.Log;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.Basketball868Result;
import com.feipulai.device.serial.beans.PullUpSetFrequencyResult;
import com.feipulai.device.serial.beans.SitPushUpSetFrequencyResult;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;

import org.greenrobot.eventbus.EventBus;

public class PullSitLinker
        extends SitPullLinker {
    public PullSitLinker(int machineCode, int targetFrequency, SitPullPairListener listener) {
        super(machineCode, targetFrequency, listener);
    }

    public boolean onRadioArrived(Message paramMessage) {
//        if (paramMessage.what == SerialConfigs.PULL_UP_MACHINE_BOOT_RESPONSE) {
//            EventBus.getDefault().post(new BaseEvent(paramMessage.obj, EventConfigs.BACKBALL_FREQUENCY_DATA));
//        }
//        if (((paramMessage.what == SerialConfigs.DRIBBLEING_PARAMETER)
//                || (paramMessage.what == SerialConfigs.DRIBBLEING_FREQUENCY))) {
//            Basketball868Result result = (Basketball868Result) paramMessage.obj;
//            if (this.machineCode == ItemDefault.CODE_LQYQ) {
//                if ((currentDeviceId == 2) && (result.getDeviceCode() == 2)) {
//                    if (paramMessage.what == SerialConfigs.DRIBBLEING_PARAMETER) {//设置成功LED id为0 ，设置配对设备ID为0
//                        currentDeviceId = 0;
//                    }
//                    checkDevice(result.getDeviceName(), result.getFrequency());
//                } else if ((currentDeviceId != 2) && (result.getDeviceCode() != 2)) {
//                    checkDevice(result.getDeviceName(), result.getFrequency());
//                }
//            } else {
//                if ((currentDeviceId == 3) && (result.getDeviceCode() == 2)) {
//                    if (paramMessage.what == SerialConfigs.DRIBBLEING_PARAMETER) {
//                        currentDeviceId = 0;
//                    }
//                    checkDevice(result.getDeviceName(), result.getFrequency());
//                } else if ((currentDeviceId != 3) && (result.getDeviceCode() != 2)) {
//                    checkDevice(result.getDeviceName(), result.getFrequency());
//                }
//            }
//            return true;
//
//        }

        if (paramMessage.what == SerialConfigs.PULL_UP_MACHINE_BOOT_RESPONSE) {//引体向上配对
            PullUpSetFrequencyResult pullUpSetFrequencyResult = (PullUpSetFrequencyResult) paramMessage.obj;
            Log.i("james----", pullUpSetFrequencyResult.toString());
            checkDevice(pullUpSetFrequencyResult);
            return true;
        }else if (paramMessage.what == SerialConfigs.SIT_UP_MACHINE_BOOT_RESPONSE) {//仰卧起坐
            SitPushUpSetFrequencyResult sitUpSetFrequencyResult = (SitPushUpSetFrequencyResult) paramMessage.obj;
            Log.i("", sitUpSetFrequencyResult.toString());
            checkDevice(sitUpSetFrequencyResult.getDeviceId(),sitUpSetFrequencyResult.getFrequency());
            return true;
        }
        return super.onRadioArrived(paramMessage);
    }

    private void checkDevice(PullUpSetFrequencyResult result) {
        checkDevice(result.getDeviceId(), result.getFrequency());
    }
}
