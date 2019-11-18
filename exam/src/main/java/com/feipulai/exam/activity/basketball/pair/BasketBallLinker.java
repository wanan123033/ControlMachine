package com.feipulai.exam.activity.basketball.pair;

import android.os.Message;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.Basketball868Result;
import com.feipulai.device.sitpullup.SitPullLinker;

public class BasketBallLinker
        extends SitPullLinker {
    public BasketBallLinker(int machineCode, int targetFrequency, SitPullPairListener listener) {
        super(machineCode, targetFrequency, listener);
    }

    public boolean onRadioArrived(Message paramMessage) {

        if (((paramMessage.what == SerialConfigs.DRIBBLEING_PARAMETER)
                || (paramMessage.what == SerialConfigs.DRIBBLEING_FREQUENCY))) {
            Basketball868Result result = (Basketball868Result) paramMessage.obj;
            if (this.machineCode == ItemDefault.CODE_LQYQ) {
                if ((currentDeviceId == 2) && (result.getDeviceCode() == 2)) {
                    if (paramMessage.what == SerialConfigs.DRIBBLEING_PARAMETER) {//设置成功LED id为0 ，设置配对设备ID为0
                        currentDeviceId = 0;
                    }
                    checkDevice(result.getDeviceId(), result.getFrequency());
                } else if ((currentDeviceId != 2) && (result.getDeviceCode() != 2)) {
                    checkDevice(result.getDeviceId(), result.getFrequency());
                }
            } else {
                if ((currentDeviceId == 3) && (result.getDeviceCode() == 2)) {
                    if (paramMessage.what == SerialConfigs.DRIBBLEING_PARAMETER) {
                        currentDeviceId = 0;
                    }
                    checkDevice(result.getDeviceId(), result.getFrequency());
                } else if ((currentDeviceId != 3) && (result.getDeviceCode() != 2)) {
                    checkDevice(result.getDeviceId(), result.getFrequency());
                }
            }
            return true;

        }
        return super.onRadioArrived(paramMessage);
    }
}
