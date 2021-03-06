package com.feipulai.device.manager;

import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.orhanobut.logger.utils.LogUtils;

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
     * @param interceptPoint 拦截点(起点1，终点2，起终点3) 不需要传入-1
     * @param interceptWay 触发方式 不需要传入-1
     * @param settingSensor 传感器信道 不需要传入-1
     * @param senNum 灵敏度 不需要传入-1
     */
    public static void cmdSetting(int runNum,int hostId,int interceptPoint,int interceptWay,int settingSensor,int senNum){
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc1, (byte) 0x01, (byte) runNum)));//跑道数
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc1, (byte) 0x02, (byte) hostId)));//主机号
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc1, (byte) 0x04, (byte) interceptPoint)));//拦截点
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc1, (byte) 0x05, (byte) (interceptWay + 1))));//触发方式
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc1, (byte) 0x08, (byte) settingSensor)));//传感器信道
        byte[] cmd = cmd((byte) 0xc1,(byte) 0x01,(byte) runNum);
        LogUtils.serial("红外计时设置跑道数指令:" + StringUtility.bytesToHexString(cmd));
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd));//跑道数
        cmd = cmd((byte) 0xc1,(byte) 0x02,(byte) hostId);
        LogUtils.serial("红外计时设置主机号指令:" + StringUtility.bytesToHexString(cmd));
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd));//主机号
        if (interceptPoint != -1){
            cmd = cmd((byte) 0xc1,(byte) 0x04,(byte) interceptPoint);
            LogUtils.serial("红外计时设置拦截点指令:" + StringUtility.bytesToHexString(cmd));
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd));//拦截点
        }

        if (interceptWay != -1){
            cmd = cmd((byte) 0xc1,(byte) 0x05,(byte) (interceptWay+1));
            LogUtils.serial("红外计时设置触发方式指令:" + StringUtility.bytesToHexString(cmd));
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd));//触发方式
        }

        if (settingSensor != -1){
            cmd = cmd((byte) 0xc1,(byte) 0x08,(byte) settingSensor);
            LogUtils.serial("红外计时设置传感器信道指令:" + StringUtility.bytesToHexString(cmd));
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd));//传感器信道
        }

        if (senNum != -1){
            cmd = cmd((byte) 0xc1,(byte) 0x03,(byte) settingSensor);
            LogUtils.serial("红外计时设置灵敏度指令:" + StringUtility.bytesToHexString(cmd));
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd));//灵敏度
        }

    }
    public static void cmdInterceptTime(int time){
        byte [] cmd = cmd((byte) 0xc1,(byte) 0x07,(byte) time);
        LogUtils.serial("红外计时设置拦截间隔指令:" + StringUtility.bytesToHexString(cmd));
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd));//拦截间隔
    }

    public static void forceStart(){
        byte[] cmd = cmd((byte) 0xc4, (byte) 0x00, (byte) 0x00);
        LogUtils.serial("红外计时强制启动指令:" + StringUtility.bytesToHexString(cmd));
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc4, (byte) 0x00, (byte) 0x00)));
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                cmd));
    }



    public static void waitStart(){
        byte[] cmd = cmd((byte) 0xc2, (byte) 0x00, (byte) 0x00);
        LogUtils.serial("红外计时等待发令指令:" + StringUtility.bytesToHexString(cmd));
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc2, (byte) 0x00, (byte) 0x00)));
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                cmd));
    }

    public static void stopRun(){
        byte[] cmd = cmd((byte) 0xc5, (byte) 0x00, (byte) 0x00);
        LogUtils.serial("红外计时结束测试指令:" + StringUtility.bytesToHexString(cmd));
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc5, (byte) 0x00, (byte) 0x00)));
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                cmd));
    }

    public static void illegalBack(){
        byte[] cmd = cmd((byte) 0xc8, (byte) 0x00, (byte) 0x00);
        LogUtils.serial("红外计时违规反返回指令:" + StringUtility.bytesToHexString(cmd));
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                cmd));
    }

    public static void getTime(){
        byte[] cmd = cmd((byte) 0xc7, (byte) 0x00, (byte) 0x00);
        LogUtils.serial("红外计时获取时间指令:" + StringUtility.bytesToHexString(cmd));
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                cmd));
    }

    /**
     * 无线参数设置
     * @param hostId
     * @param interceptType
     * @param interceptNum
     */
    public static void radioSetting(int hostId ,int interceptType,int interceptNum){
        byte [] cmd = {(byte) 0xaa,0x0f,0x01, (byte) 0xa9,0x01,0x05,0x01,0x02,0x01,0x00,0x00,0x00,0x00,0x00,0x55};
        cmd[6] = (byte) hostId;
        cmd[7] = (byte) interceptType;
        cmd[8] = (byte) interceptNum;
        for (int i = 0; i < 13; i++) {
            cmd[13] += cmd[i];
        }
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,cmd));
    }

    public static void radioWait(int hostId ,int interceptType,int interceptNum){
        byte [] cmd = {(byte) 0xaa,0x0f,0x01, (byte) 0x02,0x00,0x00,0x01,0x02,0x01,0x00,0x00,0x00,0x00,0x00,0x55};

        cmd[6] = (byte) hostId;
        cmd[7] = (byte) interceptType;
        cmd[8] = (byte) interceptNum;
        for (int i = 0; i < 13; i++) {
            cmd[13] += cmd[i];
        }
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,cmd));
    }

    public static void radioSendWaitState(int hostId ,int interceptType,int interceptNum){
//        byte [] cmd = {(byte) 0xaa,0x0f,0x02, (byte) 0x03,0x00,0x00,0x01,0x02,0x01,0x00,0x00,0x00,0x00,0x00,0x55};
        byte [] cmd = {(byte) 0xaa,0x0d,0x01, (byte) 0x03,0x00,0x00,0x000,0x00,0x00,0x00,0x00,0x00,0x55};
        cmd[3] = (byte) hostId;
        cmd[4] = (byte) interceptType;
        cmd[5] = (byte) interceptNum;
        for (int i = 0; i < 11; i++) {
            cmd[11] += cmd[i];
        }
//        cmd[6] = (byte) hostId;
//        cmd[7] = (byte) interceptType;
//        cmd[8] = (byte) interceptNum;
//        for (int i = 0; i < 13; i++) {
//            cmd[11] += cmd[i];
//        }
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,cmd));
    }
}
