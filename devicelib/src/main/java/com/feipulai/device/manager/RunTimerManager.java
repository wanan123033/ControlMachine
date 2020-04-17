package com.feipulai.device.manager;

import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;

/**
 * Created by pengjf on 2020/4/17.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class RunTimerManager {
    /**红外计时
     * @param cmd   控制
     * @param mark  key
     * @param value value
     * @return
     */
    public static byte[] cmd(byte cmd, byte mark, byte value) {
        byte[] setting = {(byte) 0xBB, 0x0C, (byte) 0xA0, 0x00, (byte) 0xA1, 0x00, cmd, mark, value, 0x00, 0x00, 0x0D};
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += setting[i];
        }
        setting[10] = (byte) sum;
        return setting;
    }

    /**
     *
     * @param runNum 拦截器对数
     * @param hostId 主机号
     * @param interceptPoint 拦截点 不需要传入-1
     * @param interceptWay 触发方式 不需要传入-1
     * @param settingSensor 传感器信道 不需要传入-1
     */
    public static void cmdSetting(int runNum,int hostId,int interceptPoint,int interceptWay,int settingSensor){
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc1, (byte) 0x01, (byte) runNum)));//跑道数
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc1, (byte) 0x02, (byte) hostId)));//主机号
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc1, (byte) 0x04, (byte) interceptPoint)));//拦截点
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc1, (byte) 0x05, (byte) (interceptWay + 1))));//触发方式
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc1, (byte) 0x08, (byte) settingSensor)));//传感器信道

        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd((byte) 0xc1,(byte) 0x01,(byte) runNum)));//跑道数
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd((byte) 0xc1,(byte) 0x02,(byte) hostId)));//主机号
        if (interceptPoint != -1){
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd((byte) 0xc1,(byte) 0x04,(byte) interceptPoint)));//拦截点
        }

        if (interceptWay != -1){
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd((byte) 0xc1,(byte) 0x05,(byte) (interceptWay+1))));//触发方式
        }

        if (settingSensor != -1){
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd((byte) 0xc1,(byte) 0x08,(byte) settingSensor)));//传感器信道
        }

    }


    public static void forceStart(){
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc4, (byte) 0x00, (byte) 0x00)));
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                cmd((byte) 0xc4, (byte) 0x00, (byte) 0x00)));
    }



    public static void waitStart(){
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc2, (byte) 0x00, (byte) 0x00)));
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                cmd((byte) 0xc2, (byte) 0x00, (byte) 0x00)));
    }

    public static void stopRun(){
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc5, (byte) 0x00, (byte) 0x00)));
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                cmd((byte) 0xc5, (byte) 0x00, (byte) 0x00)));
    }

    public static void illegalBack(){
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                cmd((byte) 0xc8, (byte) 0x00, (byte) 0x00)));
    }
}
