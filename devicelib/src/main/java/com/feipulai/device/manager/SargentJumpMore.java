package com.feipulai.device.manager;

/**
 * Created by pengjf on 2020/4/15.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SargentJumpMore {
    public static byte[] CMD_SARGENT_JUMP_EMPTY = {0X54, 0X44, 00, 0X0B, 01, 0x01, 00, 0x02, 0x00, 0x27, 0x0d};
    public static byte[] CMD_SARGENT_JUMP_START = {0X54, 0X44, 00, 0X0B, 01, 0x01, 00, 0x01, 0x13, 0x27, 0x0d};
    public static byte[] CMD_SARGENT_JUMP_LIGHT_UP = {0X54, 0X44, 00, 0X0D, 01, 0x01, 0X01, 0x06, 0X02,0X00,0x13, 0x27, 0x0d};
    public static byte[] CMD_SARGENT_JUMP_LIGHT_DOWN = {0X54, 0X44, 00, 0X0D, 01, 0x01, 0X01, 0x06, 0X01,0X00,0x13, 0x27, 0x0d};
    public static byte[] CMD_SARGENT_JUMP_IGNORE_BREAK_POINT = {0X54, 0X44, 00, 0X0B, 01, 0x01, 0X01, 0x09, 0x13, 0x27, 0x0d};
    public static byte[] CMD_SARGENT_JUMP_CHECK_SELF = {0X54, 0X44, 00, 0X0B, 01, 0x01, 0X01, 0x04, 0x13, 0x27, 0x0d};


    public static byte[] getCmdBytes(byte[] data,byte deviceId,byte mode ,byte cmd) {
        data[4] = deviceId;
        data[6] = mode;
        data[7] = cmd;
        int index = data[3]-3;
        data[8] = (byte) sum(data,index);
        return data;
    }

    private static int sum(byte[] cmd, int index) {
        int sum = 0;
        for (int i = 2; i < index; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }
}
