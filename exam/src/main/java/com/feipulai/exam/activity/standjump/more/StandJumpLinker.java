package com.feipulai.exam.activity.standjump.more;

import android.os.Message;

import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.StandJumpResult;
import com.feipulai.device.sitpullup.SitPullLinker;

public class StandJumpLinker
        extends SitPullLinker {
    public StandJumpLinker(int machineCode, int targetFrequency, SitPullPairListener listener) {
        super(machineCode, targetFrequency, listener);
    }

    public boolean onRadioArrived(Message paramMessage) {
        if (((paramMessage.what == SerialConfigs.STAND_JUMP_FREQUENCY)
                || (paramMessage.what == SerialConfigs.STAND_JUMP_PARAMETER))) {
            StandJumpResult result = (StandJumpResult) paramMessage.obj;
            checkDevice(result.getDeviceId(), result.getFrequency());
            return true;

        }
        return super.onRadioArrived(paramMessage);
    }
}
