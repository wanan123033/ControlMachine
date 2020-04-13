package com.feipulai.exam.activity.sargent_jump;

/**
 * Created by pengjf on 2019/5/20.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class Constants {
    public static final int UN_CONNECT = 0XA1 ;
    public static final int CONNECTED = 0XA2 ;
    public static final int GET_SCORE_RESPONSE = 0xA3;
    public static final int END_TEST = 0xA4 ;
    public static final int SET_MATCH = 0Xa5;
    public static final int MATCH_SUCCESS = 0Xa6;
    public static byte[] CMD_SARGENT_JUMP_EMPTY = {0X54, 0X44, 00, 0X0B, 01, 0x01, 00, 0x02, 0x00, 0x27, 0x0d};
    public static byte[] CMD_SARGENT_JUMP_START = {0X54, 0X44, 00, 0X0B, 01, 0x01, 00, 0x01, 0x13, 0x27, 0x0d};
    public static byte[] CMD_SARGENT_JUMP_LIGHT_UP = {0X54, 0X44, 00, 0X0D, 01, 0x01, 0X01, 0x06, 0X02,0X00,0x13, 0x27, 0x0d};
    public static byte[] CMD_SARGENT_JUMP_LIGHT_DOWN = {0X54, 0X44, 00, 0X0D, 01, 0x01, 0X01, 0x06, 0X01,0X00,0x13, 0x27, 0x0d};
    public static byte[] CMD_SARGENT_JUMP_IGNORE_BREAK_POINT = {0X54, 0X44, 00, 0X0B, 01, 0x01, 0X01, 0x09, 0x13, 0x27, 0x0d};
    public static byte[] CMD_SARGENT_JUMP_CHECK_SELF = {0X54, 0X44, 00, 0X0B, 01, 0x01, 0X01, 0x04, 0x13, 0x27, 0x0d};
}
