package com.feipulai.device.serial;


import com.feipulai.device.ic.utils.ItemDefault;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by james on 2017/10/26.
 */
public class SerialConfigs {


    private SerialConfigs() {
    }

    //每个项目对应的开始频道号
    public static final Map<Integer, Integer> sProChannels = new HashMap<>();

    static {
        sProChannels.put(0, 0);//公共频道	0
        sProChannels.put(ItemDefault.CODE_HW, 41);//身高体重
        sProChannels.put(ItemDefault.CODE_FHL, 110);//肺活量
        sProChannels.put(ItemDefault.CODE_LDTY, 55);//立定跳远
        sProChannels.put(ItemDefault.CODE_MG, 40);//摸高测试
        sProChannels.put(ItemDefault.CODE_FWC, 25);//俯卧撑
        sProChannels.put(ItemDefault.CODE_YWQZ, 10);//仰卧起坐
        sProChannels.put(ItemDefault.CODE_ZWTQQ, 41);//坐位体
        sProChannels.put(ItemDefault.CODE_TS, 1);//跳绳计数
        sProChannels.put(ItemDefault.CODE_SL, 0);//视力
        sProChannels.put(ItemDefault.CODE_YTXS, 25);//引体向上
        sProChannels.put(ItemDefault.CODE_HWSXQ, 70);//实心球
        sProChannels.put(ItemDefault.CODE_ZCP, 0);//中长跑
        sProChannels.put(ItemDefault.CODE_PQ, 110);//排球垫球
        sProChannels.put(ItemDefault.CODE_ZFP, 41);//红外计时
        sProChannels.put(ItemDefault.CODE_LQYQ, 85);//篮球运球
        sProChannels.put(ItemDefault.CODE_ZQYQ, 95);//足球运球
        sProChannels.put(ItemDefault.CODE_WLJ, 101);//握力计
        sProChannels.put(ItemDefault.CODE_JGCJ, 63);//激光测距
        sProChannels.put(ItemDefault.CODE_SHOOT, 120);//篮球投篮
    }

    public static final String[] GROUP_NAME = {"A", "B", "C"};

    /***********************************************COMMAND****************************************************************/
    //协议框架格式：
    //【0:1】包头，2字节,即0XA5,0X5A
    //【2】命令字，1字节
    //【3】数据长度=N，1字节，0<=N<256, 【4:3+N】
    //【4:3+N】数据
    //【4+N】校验和，1字节，=sum{2:3+N}取余
    //【5+N:6+N】包尾2字节0xaa,0x55
    public static final byte[] CMD_GET_CONVERTER_VERSION = {(byte) 0xa5, 0x5a, (byte) 0xb0, 0x01, 0x00, (byte) 0xff, (byte) 0xaa, 0x55};

    //0x54 0x44(2字节包头) 0x00 0x0c(2字节包长) 0x00(设备号) 0x02(测试项目) 0x00(模式) 0x01(命令字节)
    //0x00 0x00(2字节参数) 0x27 0x0d(2字节包尾)
    public static final byte[] CMD_START_JUMP = {0x54, 0x44, 0x00, 0x0c, 0x00, 0x02, 0x00, 0x01, 0x00, 0x00, 0x27, 0x0d};
    public static final byte[] CMD_END_JUMP = {0x54, 0x44, 0x00, 0x0c, 0x00, 0x02, 0x00, 0x02, 0x00, 0x00, 0x27, 0x0d};
    public static final byte[] CMD_GET_SCORE = {0x54, 0x44, 0x00, 0x0c, 0x00, 0x02, 0x00, 0x04, 0x00, 0x00, 0x27, 0x0d};
    public static final byte[] CMD_SELF_CHECK_JUMP = {0x54, 0x44, 0x00, 0x0c, 0x01, 0x02, 0x00, 0x07, 0x00, 0x00, 0x27, 0x0d};
    public static final byte[] CMD_GET_JUMP_VERSION = {0x54, 0x44, 0x00, 0x0c, 0x00, 0x02, 0x00, 0x0c, 0x00, 0x00, 0x27, 0x0d};

    //SIT AND REACH
    public static final byte[] CMD_SIT_REACH_EMPTY = {0X54, 0X44, 00, 0X10, 00, 0x04, 00, 00, 00, 00, 00, 00, 00, 0x14, 0x27, 0x0d};
    public static final byte[] CMD_SIT_REACH_START = {0X54, 0X44, 00, 0X10, 00, 0x04, 00, 0x01, 00, 00, 00, 00, 00, 0x15, 0x27, 0x0d};
    public static final byte[] CMD_SIT_REACH_END = {0X54, 0X44, 00, 0X10, 00, 0x04, 00, 0x02, 00, 00, 00, 00, 00, 0x16, 0x27, 0x0d};
    public static final byte[] CMD_SIT_REACH_GET_SCORE = {0X54, 0X44, 00, 0X10, 00, 0x04, 00, 0x04, 00, 00, 00, 00, 00, 0x18, 0x27, 0x0d};
    public static final byte[] CMD_SIT_REACH_GET_VERSION = {0X54, 0X44, 00, 0X10, 00, 0x04, 00, 0x0c, 00, 00, 00, 00, 00, 0x20, 0x27, 0x0d};


    //SARGENT JUMP
    public static final byte[] CMD_SARGENT_JUMP_EMPTY = {0X54, 0X44, 00, 0X10, 01, 0x01, 00, 00, 00, 00, 00, 00, 00, 0x12, 0x27, 0x0d};
    public static final byte[] CMD_SARGENT_JUMP_START = {0X54, 0X44, 00, 0X10, 01, 0x01, 00, 0x01, 00, 00, 00, 00, 00, 0x13, 0x27, 0x0d};
    public static final byte[] CMD_SARGENT_JUMP_STOP = {0X54, 0X44, 00, 0X10, 01, 0x01, 00, 0x02, 00, 00, 00, 00, 00, 0x14, 0x27, 0x0d};
    public static final byte[] CMD_SARGENT_JUMP_GET_SCORE = {0X54, 0X44, 00, 0X10, 01, 0x01, 00, 0x04, 00, 00, 00, 00, 00, 0x16, 0x27, 0x0d};
    public static final byte[] CMD_SARGENT_JUMP_SET_MATCH = {0X54, 0X44, 00, 0X10, 0, 0x01, 00, 0x0b, 00, 0x04, 00, 00, 00, 0x15, 0x27, 0x0d};

    //VC
    public static final byte[] CMD_VC_SELECT = {(byte) 0xaa, (byte) 0xc1, 0x00, 0x00, (byte) 0xc1};

    //离地高度设置范围为0-255
    public static byte[] CMD_SARGENT_JUMP_GET_SET_0(int offGroundDistance) {
        byte[] data = {0X54, 0X44, 00, 0X10, 01, 0x01, 00, 0x06, 00, 00, 00, 00, 00, 0x00, 0x27, 0x0d};
        data[8] = (byte) ((offGroundDistance >> 8) & 0xff);// 次低位
        data[9] = (byte) (offGroundDistance & 0xff);// 最低位

        int sum = 0;
        for (int i = 2; i <= 12; i++) {
            sum += data[i] & 0xff;
        }
        data[13] = (byte) sum;
        return data;
    }


    //MEDICINE BALL
    public static final byte[] CMD_MEDICINE_BALL_EMPTY = {0X54, 0X44, 00, 0x0c, 00, 0x07, 00, 00, 00, 0, 0x27, 0x0d};
    public static final byte[] CMD_MEDICINE_BALL_FAST_EMPTY = {0X54, 0X44, 00, 0x0c, 00, 0x07, 00, 00, 01, 0, 0x27, 0x0d};
    public static final byte[] CMD_MEDICINE_BALL_START = {0X54, 0X44, 00, 0x0c, 00, 0x07, 00, 0x01, 00, 0, 0x27, 0x0d};
    public static final byte[] CMD_MEDICINE_BALL_STOP = {0X54, 0X44, 00, 0x0c, 00, 0x07, 00, 0x02, 00, 0, 0x27, 0x0d};
    public static final byte[] CMD_MEDICINE_BALL_GET_SCORE = {0X54, 0X44, 00, 0x0c, 00, 0x07, 00, 0x04, 00, 0, 0x27, 0x0d};
    public static final byte[] CMD_MEDINCINE_BALL_SELF_CHECK = {0X54, 0X44, 00, 0x0c, 00, 0x07, 00, 0x07, 00, 0, 0x27, 0x0d};
    public static final byte[] CMD_MEDICINE_BALL_SET_BASE_POINT = {0X54, 0X44, 00, 0x0c, 00, 0x07, 00, 0x09, 00, 0, 0x27, 0x0d};
    public static final byte[] CMD_MEDICINE_BALL_SET_START_POINT = {0X54, 0X44, 00, 0x0c, 00, 0x07, 00, 0x06, 00, 0, 0x27, 0x0d};

    /**
     * 设置立定跳远点数
     *
     * @param points
     * @return
     */
    public static byte[] SET_CMD_SARGENT_JUMP_SETTING_POINTS(int points) {
        byte[] cmdBype = {0X54, 0X44, 00, 0X0C, 00, 0x02, 00, 0x06, 00, 00, 0x27, 0x0d};
        cmdBype[8] = (byte) ((points >> 8) & 0xff);// 次低位
        cmdBype[9] = (byte) (points & 0xff);// 最低位
        return cmdBype;
    }

    /**
     * @param startPoint 起始距离(cm)
     * @return 需要发送的测量杠设置字节
     */
    public static byte[] CMD_MEDINCINE_BALL_SET_START_POINT(int startPoint) {
        byte[] data = {0X54, 0X44, 00, 0x10, 00, 0x07, 00, 0x06, 00, 00, 00, 00, 00, 00, 0x27, 0x0d};
        data[8] = (byte) (startPoint & 0xff00);
        data[9] = (byte) (startPoint & 0xff);
        return data;
    }

    //离地高度设置范围为0-255
    public static byte[] CMD_MEDINCINE_BALL_GET_SET_0(int offGroundDistance) {
        byte[] data = {0X54, 0X44, 00, 0X10, 00, 0x01, 00, 0x06, 00, 00, 00, 00, 00, 0x00, 0x27, 0x0d};
        data[9] = (byte) (offGroundDistance & 0xff);
        return data;
    }

    //VOLLEYBALL
    public static final byte[] CMD_VOLLEYBALL_EMPTY = {0X54, 0X44, 00, 0X10, 00, 0Xa, 00, 00, 00, 00, 00, 00, 00, 0x1a, 0x27, 0x0d};
    public static final byte[] CMD_VOLLEYBALL_START = {0X54, 0X44, 00, 0X10, 00, 0Xa, 00, 0x01, 00, 00, 00, 00, 00, 0x1b, 0x27, 0x0d};
    public static final byte[] CMD_VOLLEYBALL_STOP = {0X54, 0X44, 00, 0X10, 00, 0Xa, 00, 0x02, 00, 00, 00, 00, 00, 0x1c, 0x27, 0x0d};
    public static final byte[] CMD_VOLLEYBALL_GET_SCORE = {0X54, 0X44, 00, 0X10, 00, 0Xa, 00, 0x04, 00, 00, 00, 00, 00, 0x1e, 0x27, 0x0d};

    /************************************************RESPONSE********************************************************************/

    //determines whether this response is for the converter or not
    public static final int CONVERTER_VERSION_RESPONSE = 0x1;
    public static final int CONVERTER_COM2_SETTING_RESPONSE = 0x2;
    public static final int CONVERTER_RADIO_CHANNEL_SETTING_RESPONSE = 0x3;
    public static final int FROM_868_RESPONSE = 0x4;
    public static final int FROM_RS232_RESPONSE = 0x5;
    //determines which kind of stand jump response it is
    public static final int JUMP_SELF_CHECK_RESPONSE = 0x6;
    public static final int JUMP_NEW_SELF_CHECK_RESPONSE = 0x10;
    public static final int JUMP_START_RESPONSE = 0x7;
    public static final int JUMP_SCORE_RESPONSE = 0x8;
    public static final int JUMP_END_RESPONSE = 0x9;

    public static final int JUMP_GET_VERSION_RESPONSE = 0xa;
    public static final int JUMP_SELF_CHECK_RESPONSE_Simple = 0xb;
    public static final int HEIGHT_WEIGHT_RESULT = 0xc;
    public static final int VITAL_CAPACITY_RESULT = 0xd;

    //SIT AND REACH
    public static final int SIT_AND_REACH_EMPTY_RESPONSE = 0xe;
    public static final int SIT_AND_REACH_START_RESPONSE = 0xf;
    public static final int SIT_AND_REACH_RESULT_RESPONSE = 0x10;
    public static final int SIT_AND_REACH_STOP_RESPONSE = 0x11;
    public static final int SIT_AND_REACH_GET_VERSION_RESPONSE = 0x12;

    //SARGENT JUMP
    public static final int SARGENT_JUMP_EMPTY_RESPONSE = 0x13;
    public static final int SARGENT_JUMP_START_RESPONSE = 0x14;
    public static final int SARGENT_JUMP_STOP_RESPONSE = 0x15;
    public static final int SARGENT_JUMP_GET_SCORE_RESPONSE = 0x16;
    public static final int SARGENT_JUMP_SET_0__RESPONSE = 0x17;

    //VOLLEYBALL
    public static final int VOLLEYBALL_EMPTY_RESPONSE = 0x18;
    public static final int VOLLEYBALL_START_RESPONSE = 0x19;
    public static final int VOLLEYBALL_STOP_RESPONSE = 0x1a;
    public static final int VOLLEYBALL_RESULT_RESPONSE = 0x1b;
    public static final int VOLLEYBALL_CHECK_RESPONSE = 0x1c;
    public static final int VOLLEYBALL_LOSE_DOT_RESPONSE = 0x1d;
    public static final int VOLLEYBALL_VERSION_RESPONSE = 0x1e;
    //MEDICINE BALL
    public static final int MEDICINE_BALL_SELF_CHECK_RESPONSE = 0x1c;
    public static final int MEDICINE_BALL_GET_SCORE_RESPONSE = 0x1d;
    public static final int MEDICINE_BALL_START_RESPONSE = 0x1e;
    public static final int MEDICINE_BALL_STOP_RESPONSE = 0x1f;
    public static final int MEDICINE_BALL_EMPTY_RESPONSE = 0x20;

    //PULL UP
    public static final int PULL_UP_MACHINE_BOOT_RESPONSE = 0x21;
    public static final int PULL_UP_GET_STATE = 0x22;
    public static final int PULL_UP_GET_VERSION = 0x23;

    //PUSH UP
    public static final int PUSH_UP_MACHINE_BOOT_RESPONSE = 0x24;
    public static final int PUSH_UP_GET_STATE = 0X25;
    public static final int PUSH_UP_GET_VERSION = 0X26;

    //SIT UP
    public static final int SIT_UP_MACHINE_BOOT_RESPONSE = 0x27;
    public static final int SIT_UP_GET_VERSION = 0x28;
    public static final int SIT_UP_GET_STATE = 0x29;

    //跳绳
    public static final int JUMPROPE_RESPONSE = 0x2a;

    public static final int MEDICINE_SEND_EMPTY = 0X2B;
    public static final int VITAL_CAPACITY_ERROR_MSG = 0X2C;

    public static final int QR_CODE_READ = 0x2d;
    public static final int PRINTER_STATE = 0x2e;


    //50m 红外计时
    public static final int RUN_TIMER_CONNECT = 0x2F;
    public static final int RUN_TIMER_INTERCEPT_TIME = 0x30;
    public static final int RUN_TIMER_FAULT_BACK = 0x31;
    public static final int RUN_TIMER_SETTING = 0x32;
    public static final int RUN_TIMER_READY = 0x33;
    public static final int RUN_TIMER_FORCE_START = 0x34;
    public static final int RUN_TIMER_STOP = 0x35;

    public static final int JUMP_SET_POINTS = 0x36;
    public static final int SARGENT_JUMP_SET_MATCH = 0x37;

    //gps 时间测量获取用的
    public static final int GPS_TIME_RESPONSE = 0x38;
    public static final int SARGENT_JUMP_SET_MORE_MATCH = 0x39;
    public static final int VOLLEY_BALL_SET_MORE_MATCH = 0x40;
    //肺活量
    public static final int VITAL_CAPACITY_SET_MORE_MATCH = 0x41;
    public static final int VOLLEY_BALL_CHECK_MATCH = 0x42;
    public static int USE_VERSION = 363;//肺活量新版本为及以后为363

    //排球垫球
    public static final int VOLLEY_BALL_SET_PAIR = 0x41;
    public static final int VOLLEY_BALL_EDITION = 0x42;
    public static final int VOLLEY_BALL_STATE = 0x43;
    public static final int VOLLEY_BALL_SELFCHECK = 0x44;
    public static final int VOLLEY_BALL_B0 = 0x45;
    public static final int VOLLEY_BALL_GET_VER = 0x4A;

    //篮球足球运球
    public static final int DRIBBLEING_RESULT = 0x46;
    public static final int DRIBBLEING_FREQUENCY = 0x47;
    public static final int DRIBBLEING_PARAMETER = 0x48;
    public static final int DRIBBLEING_START = 0x49;
    public static final int DRIBBLEING_LED_CONTENT = 0x50;
    public static final int DRIBBLEING_STOP = 0x51;
    public static final int DRIBBLEING_PAUSE = 0x52;
    public static final int DRIBBLEING_FREE = 0x53;
    public static final int DRIBBLEING_AWAIT = 0x54;
    public static final int DRIBBLEING_LED_START_TIME = 0x55;
    public static final int DRIBBLEING_SET_SETTING = 0x56;

    //实心球无线
    public static final int MEDICINE_BALL_MATCH_MORE = 0x56;
    public static final int MEDICINE_BALL_RESULT_MORE = 0x57;
    public static final int MEDICINE_BALL_START_MORE = 0x58;

    //立定跳远
    public static final int STAND_JUMP_FREQUENCY = 0x58;
    public static final int STAND_JUMP_PARAMETER = 0x59;
    public static final int STAND_JUMP_START = 0x60;
    public static final int STAND_JUMP_END = 0x61;
    public static final int STAND_JUMP_VERSION = 0x62;
    public static final int STAND_JUMP_LEISURE = 0x63;
    public static final int STAND_JUMP_GET_STATE = 0x64;
    public static final int STAND_JUMP_CHECK = 0x65;
    public static final int STAND_JUMP_SET_POINTS = 0x66;

    //握力计
    public static final int GRIP_SET_MORE_MATCH = 0x70;
    public static final int SARGENT_JUMP_CHECK = 0x71;

    //坐位体前屈
    public static final int SIT_REACH_FREQUENCY = 0x72;
    public static final int SIT_REACH_START = 0x74;
    public static final int SIT_REACH_END = 0x75;
    public static final int SIT_REACH_VERSION = 0x76;
    public static final int SIT_REACH_GET_STATE = 0x77;
    public static final int SIT_REACH_LEISURE = 0x78;
    //摸高新增获取数据
    public static final int SARGENT_GET_DATA = 0x79;
    //新增引体向上手臂检测
    public static final int PULL_UP_GET_GYRO_DATA = 0x80;
    public static final int PULL_UP_GET_ANGLE_DATA = 0x81;
}
