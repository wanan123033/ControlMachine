package com.feipulai.device.manager;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.orhanobut.logger.utils.LogUtils;

public class VitalCapacityManager {
    /**
     * 旧版
     * @param index
     * @param deviceId
     * @param cmd
     */
    public static void cmd(int index, int deviceId, int cmd,int frequency){
        byte[] data = {(byte) 0xAB, (byte) index, 0x10, 0x02, (byte) deviceId, (byte) cmd, (byte) frequency, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x00, 0x0A};
        int sum = 0;
        for (int i = 0; i < data.length - 2; i++) {
            sum += data[i];
        }
        data[14] = (byte) sum;

        LogUtils.serial("肺活量开始测试指令:"+ StringUtility.bytesToHexString(data));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
    }

    public static void command(int deviceId, int cmd ,int hostId){
        byte[] data = {(byte) 0xAA, 0x12, 0x09, 0x03, 0x01, (byte) hostId, (byte) deviceId, (byte) cmd,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x00, 0x0D};

        if (MachineCode.machineCode == ItemDefault.CODE_WLJ) {
            data[2] = 0x0c;
        } else {
            data[2] = 0x09;
        }
        int sum = 0;
        for (int i = 1; i < data.length - 2; i++) {
            sum += data[i];
        }
        data[16] = (byte) sum;
        LogUtils.serial("肺活量开始测试指令:"+ StringUtility.bytesToHexString(data));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
    }
}
