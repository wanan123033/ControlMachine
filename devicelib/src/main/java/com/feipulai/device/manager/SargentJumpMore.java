package com.feipulai.device.manager;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.orhanobut.logger.examlogger.LogUtils;

/**
 * Created by pengjf on 2020/4/15.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SargentJumpMore {
    private static byte[] CMD_SARGENT_JUMP_EMPTY = {0X54, 0X44, 00, 0X0B, 01, 0x01, 00, 0x02, 0x00, 0x27, 0x0d};
    private static byte[] CMD_SARGENT_JUMP_START = {0X54, 0X44, 00, 0X0B, 01, 0x01, 00, 0x01, 0x13, 0x27, 0x0d};
    private static byte[] CMD_SARGENT_JUMP_LIGHT_UP = {0X54, 0X44, 00, 0X0D, 01, 0x01, 0X01, 0x06, 0X02,0X00,0x13, 0x27, 0x0d};
    private static byte[] CMD_SARGENT_JUMP_LIGHT_DOWN = {0X54, 0X44, 00, 0X0D, 01, 0x01, 0X01, 0x06, 0X01,0X00,0x13, 0x27, 0x0d};
    private static byte[] CMD_SARGENT_JUMP_IGNORE_BREAK_POINT = {0X54, 0X44, 00, 0X0B, 01, 0x01, 0X01, 0x09, 0x13, 0x27, 0x0d};
    private static byte[] CMD_SARGENT_JUMP_CHECK_SELF = {0X54, 0X44, 00, 0X0B, 01, 0x01, 0X01, 0x04, 0x13, 0x27, 0x0d};
    private static byte[] CMD_SET_BASE_HEIGHT = {0X54, 0X44, 00, 0X0D, 01, 0x01, 01, 0x05, 00, 00, 0x00, 0x27, 0x0d};
    private static byte[] CMD_SARGENT_JUMP_GET_DATA = {0X54, 0X44, 00, 0X0B, 01, 0x01, 0X01, 0x0A, 0x13, 0x27, 0x0d};
    public static void sendStart(int deviceId){
        byte[] cmd = CMD_SARGENT_JUMP_START;
        cmd[4] = (byte) deviceId;
        cmd[6] = 0x01;
        cmd[7] = 0x03;
        cmd[8] = (byte) sum(cmd, 8);
        LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---摸高开始测试指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                cmd));
    }

    private static int sum(byte[] cmd, int index) {
        int sum = 0;
        for (int i = 2; i < index; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }

    public static void sendEmpty(int deviceId){
        byte[] cmd = CMD_SARGENT_JUMP_EMPTY;
        cmd[4] = (byte) deviceId;
        cmd[6] = 0x01;
        cmd[7] = 0x02;
        cmd[8] = (byte) sum(cmd, 8);
        LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---摸高空指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                cmd));
    }


    public static void lightUp(int deviceId){
        byte[] cmd = CMD_SARGENT_JUMP_LIGHT_UP;
        cmd[4] = (byte) deviceId;
        cmd[10] = (byte) sum(cmd, 10);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                cmd));
    }

    public static void lightDown(int deviceId){
        byte[] cmd = CMD_SARGENT_JUMP_LIGHT_DOWN;
        cmd[4] = (byte) deviceId;
        cmd[10] = (byte) sum(cmd, 10);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                cmd));
    }

    public static void checkSelf(int deviceId){
        byte[] cmd = CMD_SARGENT_JUMP_CHECK_SELF;
        cmd[4] = (byte) deviceId;
        cmd[8] = (byte) sum(cmd, 8);
        LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---摸高自检指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                cmd));
    }

    public static void ignoreBad(int deviceId){
        byte[] cmd = CMD_SARGENT_JUMP_IGNORE_BREAK_POINT;
        cmd[4] = (byte) deviceId;
        cmd[8] = (byte) sum(cmd, 8);
        LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---摸高0点设置指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                cmd));
    }

    //离地高度设置范围为0-255
    public static void setBaseHeight(int offGroundDistance, int deviceId) {
        byte[] data =CMD_SET_BASE_HEIGHT;
        data[4] = (byte) deviceId;
        data[8] = (byte) ((offGroundDistance >> 8) & 0xff);// 次低位
        data[9] = (byte) (offGroundDistance & 0xff);// 最低位

        int sum = 0;
        for (int i = 2; i < 10; i++) {
            sum += data[i] & 0xff;
        }
        data[10] = (byte) sum;
        LogUtils.normal(data.length+"---"+ StringUtility.bytesToHexString(data)+"---摸高设置高度范围指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                data));
    }

    public static void getData(int deviceId){
        byte[] cmd = CMD_SARGENT_JUMP_GET_DATA;
        cmd[4] = (byte) deviceId;
        cmd[8] = (byte) sum(cmd, 8);
        LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---摸高0点设置指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                cmd));
    }
}
