package com.feipulai.device.manager;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.command.ConvertCommand;

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
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                cmd));
    }

    public static void sendStart(int hostId,int deviceId){
        byte[] cmd = CMD_MEDICINE_BALL_START;
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) (deviceId&0xff);
        cmd[19] = (byte) sum(cmd, 19);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                cmd));
    }

    public static void sendGetState(int hostId,int deviceId){
        byte[] cmd = CMD_MEDICINE_BALL_STATE;
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) (deviceId&0xff);
        cmd[19] = (byte) sum(cmd, 19);
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
}
