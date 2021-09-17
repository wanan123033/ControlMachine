package com.feipulai.device.manager;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by pengjf on 2020/4/15.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MedicineBallMore {
    //轮询
    private static byte[] CMD_MEDICINE_BALL_STATE = {(byte) 0XAA, 0X15, 0X07, 0X03, 01, 0x01, 0X01, 0x03, 0x00, 0x00,0X00,0X00,
            0X00, 0x00, 0x00,0X00,0X00,0X00,0X00,0X00,0x0d};
    //开始
    private static byte[] CMD_MEDICINE_BALL_START = {(byte) 0XAA, 0X15, 0X07, 0X03, 01, 0x01, 0X01, 0x04, 0x00, 0x00,0X00,0X00,
            0X00, 0x00, 0x00,0X00,0X00,0X00,0X00,0X00,0x0d};
    //设置空闲
    private static byte[] CMD_MEDICINE_BALL_SET_EMPTY = {(byte) 0XAA, 0X15, 0X07, 0X03, 01, 0x01, 0X01, 0x06, 0x00, 0x00,0X00,0X00,
            0X00, 0x00, 0x00,0X00,0X00,0X00,0X00,0X00,0x0d};

    public static void sendEmpty(int hosId,int deviceId){
        byte[] cmd = CMD_MEDICINE_BALL_SET_EMPTY;
        cmd[5] = (byte) hosId;
        cmd[6] = (byte) deviceId;
        cmd[19] = (byte) sum(cmd, 19);
        LogUtils.serial("实心球空指令:" + StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                cmd));
    }

    public static void sendStart(int hostId,int deviceId){
        byte[] cmd = CMD_MEDICINE_BALL_START;
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) (deviceId&0xff);
        cmd[19] = (byte) sum(cmd, 19);
        LogUtils.serial("实心球开始指令:" + StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                cmd));
    }

    public static void sendGetState(int hostId,int deviceId){
        byte[] cmd = CMD_MEDICINE_BALL_STATE;
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) (deviceId&0xff);
        cmd[19] = (byte) sum(cmd, 19);
        LogUtils.serial("实心球获取状态指令:" + StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                cmd));
    }

    private static int sum(byte[] cmd, int end) {
        int sum = 0;
        for (int i = 1; i < end; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }

    /**
     * 实心球设置频段
     *
     * @param originFrequency
     * @param deviceId
     * @param hostId
     */
    public static void setFrequency(int targetChannel, int originFrequency, int deviceId, int hostId) {
        byte[] buf = new byte[21];
        buf[0] = (byte) 0xAA;//包头
        buf[1] = 0x15;    //包长
        buf[2] = 0x07;       //项目编号
        buf[3] = 0x03; //子机
        buf[4] = 0x01;      //主机
        buf[5] = (byte) (hostId & 0xff);     //主机号
        buf[6] = (byte) (deviceId & 0xff);       //子机号
        buf[7] = 0x02;      //命令
        buf[8] = 0;
        buf[9] = 0;
        buf[10] = 0;
        buf[11] = 0;
        buf[12] = (byte) targetChannel;
        buf[13] = 0x04;
        buf[14] = (byte) (hostId & 0xff);
        buf[15] = (byte) (deviceId & 0xff);
        buf[16] = 0x00;
        buf[17] = 0x00;
        buf[18] = 0x00;
        for (int i = 1; i < 19; i++) {
            buf[19] += buf[i] & 0xff;
        }
        buf[20] = 0x0d;   //包尾
        //Logger.i(StringUtility.bytesToHexString(buf));
        //先切到通信频段
        LogUtils.serial("实心球配对指令:" + StringUtility.bytesToHexString(buf));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, buf));
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(targetChannel)));
    }
}
