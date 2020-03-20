package com.feipulai.host.activity.standjump.more;

import android.os.Message;

import com.feipulai.device.newProtocol.NewProtocolLinker;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.StandJumpResult;

public class StandJumpLinker
        extends NewProtocolLinker {
//    public StandJumpLinker(int machineCode, int targetFrequency, SitPullPairListener listener) {
//        super(machineCode, targetFrequency, listener);
//    }

    public StandJumpLinker(int machineCode, int targetFrequency, SitPullPairListener listener, int hostId) {
        super(machineCode, targetFrequency, listener, hostId);
    }

    public boolean onRadioArrived(Message paramMessage) {
        if (((paramMessage.what == SerialConfigs.STAND_JUMP_FREQUENCY)
                || (paramMessage.what == SerialConfigs.STAND_JUMP_PARAMETER))) {
            StandJumpResult result = (StandJumpResult) paramMessage.obj;
            checkDevice(result.getDeviceId(), result.getFrequency(), result.getHostId());
            return true;

        }
        return super.onRadioArrived(paramMessage);
    }
}
