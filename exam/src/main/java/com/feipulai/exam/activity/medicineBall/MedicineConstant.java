package com.feipulai.exam.activity.medicineBall;

/**
 * Created by pengjf on 2018/12/5.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class MedicineConstant {
    public static final int END_TEST = 0XFF01 ;
    public static final int GET_SCORE_RESPONSE = 0XFF02;
    public static final int SELF_CHECK_RESPONSE = 0XFF03;

    //轮询
    public static byte[] CMD_MEDICINE_BALL_EMPTY = {(byte) 0XAA, 0X15, 0X07, 0X03, 01, 0x01, 0X01, 0x03, 0x00, 0x00,0X00,0X00,
            0X00, 0x00, 0x00,0X00,0X00,0X00,0X00,0X00,0x0d};
    //开始
    public static byte[] CMD_MEDICINE_BALL_START = {(byte) 0XAA, 0X15, 0X07, 0X03, 01, 0x01, 0X01, 0x04, 0x00, 0x00,0X00,0X00,
            0X00, 0x00, 0x00,0X00,0X00,0X00,0X00,0X00,0x0d};
    //设置空闲
    public static byte[] CMD_MEDICINE_BALL_SET_EMPTY = {(byte) 0XAA, 0X15, 0X07, 0X03, 01, 0x01, 0X01, 0x06, 0x00, 0x00,0X00,0X00,
            0X00, 0x00, 0x00,0X00,0X00,0X00,0X00,0X00,0x0d};
}
