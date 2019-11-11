package com.feipulai.exam.activity.medicineBall;

/**
 * Created by pengjf on 2018/12/5.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class MedicineConstant {
    public static final int END_TEST = 0XFF01 ;
    public static final int GET_SCORE_RESPONSE = 0XFF02;
    public static final int SELF_CHECK_RESPONSE = 0XFF03;
    public static byte[] CMD_SARGENT_JUMP_EMPTY = {0X54, 0X44, 00, 0X0B, 01, 0x01, 00, 0x02, 0x00, 0x27, 0x0d};
    public static byte[] CMD_SARGENT_JUMP_START = {0X54, 0X44, 00, 0X0B, 01, 0x01, 00, 0x01, 0x13, 0x27, 0x0d};
}
