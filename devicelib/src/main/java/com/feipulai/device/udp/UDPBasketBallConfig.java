package com.feipulai.device.udp;

/**
 * Created by zzs on  2019/5/29
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class UDPBasketBallConfig {
    public static final int CMD_SET_TIME_RESPONSE = 1;
    public static final int CMD_SET_STATUS_RESPONSE = 2;
    public static final int CMD_GET_STATUS_RESPONSE = 3;
    public static final int CMD_GET_TIME_RESPONSE = 4;
    public static final int CMD_SET_BRIGHT_RESPONSE = 5;
    public static final int CMD_ASYNTM_PAUSE_RESPONSE = 17;
    public static final int CMD_ASYNTM_NOPAUSE_RESPONSE = 18;
    public static final int CMD_DISPTM_PAUSE_RESPONSE = 19;
    public static final int CMD_SET_STATUS_STOP_RESPONSE = 54;
    public static final int CMD_SET_T_RESPONSE = 57;
    public static final int CMD_BREAK_RESPONSE = 64;
    public static final int CMD_SET_BLOCKERTIME_RESPONSE = 70;
    public static final int CMD_GET_BLOCKERTIME_RESPONSE = 71;
    public static final int CMD_SET_PRECISION_RESPONSE = 72;
    public static final int CMD_GET_PRECISION_RESPONSE = 73;
    /*--------------------------------------篮球-----------------------------------------*/
    /**
     * 获取工作状态
     */
    public static final byte[] BASKETBALL_CMD_GET_STATUS = {(byte) 0XA6, 0X03, (byte) 0XFF};
    /**
     * 获取时间显示
     */
    public static final byte[] BASKETBALL_CMD_GET_TIME = {(byte) 0XA6, 0X04, (byte) 0XFF};
    /**
     * 获取拦截器拦截的时间
     */
    public static final byte[] BASKETBALL_CMD_GET_BLOCKERTIME = {(byte) 0XA6, 0X47, (byte) 0XFF};
    /**
     * 获取计时器时间显示精度
     */
    public static final byte[] BASKETBALL_CMD_GET_PRECISION = {(byte) 0XA6, 0X49, (byte) 0XFF};

    /**
     * 设置显示内容
     */
    public static final byte[] BASKETBALL_CMD_SET_TIME(int hour, int minute, int second, int hund) {
        byte[] cmdBype = {(byte) 0XA6, 0X01, 00, 00, 00, 00, (byte) 0XFF};
        cmdBype[2] = (byte) (hund & 0xff);// 毫秒
        cmdBype[3] = (byte) (second & 0xff);// 秒
        cmdBype[4] = (byte) (minute & 0xff);// 分
        cmdBype[5] = (byte) (hour & 0xff);// 时
        return cmdBype;
    }

    /**
     * 设置工作状态
     * @return
     */
    public static final byte[] BASKETBALL_CMD_SET_STOP_STATUS() {
        byte[] cmdBype = {(byte) 0XA6, 0X02, 0X36, (byte) 0XFF};
        return cmdBype;
    }
    /**
     * 设置工作状态
     *
     * @param status STATUS_FREE		1		//FREE
     *               STATUS_WAIT  		2		//WAIT To Start
     *               STATUS_RUNING 	    3		//Start Run
     *               STATUS_PREP  		4		//Prepare to stop
     *               STATUS_PAUSE 		5		//Display stop time,But Timer is Running
     * @return
     */
    public static final byte[] BASKETBALL_CMD_SET_STATUS(int status) {
        byte[] cmdBype = {(byte) 0XA6, 0X02, 00, (byte) 0XFF};
        cmdBype[2] = (byte) (status & 0xff);// 毫秒
        return cmdBype;
    }
    /**
     * 设置显示内容
     *
     * @param ucLight CMD_CLR    			7      清楚屏幕
     *                CMD_ALL    			8      全亮
     *                CMD_DISP   			9      刷新
     *                CMD_TEST 			10     测试应答，接受到此命令，应答
     * @return
     */
    public static final byte[] BASKETBALL_CMD_SET_BRIGHT(int ucLight) {
        byte[] cmdBype = {(byte) 0XA6, 0X05, 00, (byte) 0XFF};
        cmdBype[2] = (byte) (ucLight & 0xff);
        return cmdBype;
    }

    /**
     * 同步时间并暂停显示
     *
     * @param hour   时
     * @param minute 分
     * @param second 秒
     * @param hund   毫秒
     * @return
     */
    public static final byte[] BASKETBALL_CMD_ASYNTM_PAUSE(int hour, int minute, int second, int hund) {
        byte[] cmdBype = {(byte) 0XA6, 0X11, 00, 00, 00, 00, (byte) 0XFF};
        cmdBype[2] = (byte) (hund & 0xff);// 毫秒
        cmdBype[3] = (byte) (second & 0xff);// 秒
        cmdBype[4] = (byte) (minute & 0xff);// 分
        cmdBype[5] = (byte) (hour & 0xff);// 时
        return cmdBype;
    }

    /**
     * 同步时间不暂停显示
     *
     * @param hour   时
     * @param minute 分
     * @param second 秒
     * @param hund   毫秒
     * @return
     */
    public static final byte[] BASKETBALL_CMD_ASYNTM_NOPAUSE(int hour, int minute, int second, int hund) {
        byte[] cmdBype = {(byte) 0XA6, 0X12, 00, 00, 00, 00, (byte) 0XFF};
        cmdBype[2] = (byte) (hund & 0xff);// 毫秒
        cmdBype[3] = (byte) (second & 0xff);// 秒
        cmdBype[4] = (byte) (minute & 0xff);// 分
        cmdBype[5] = (byte) (hour & 0xff);// 时
        return cmdBype;
    }

    /**
     * 显示时间并暂停，不同步时间
     *
     * @param hour   时
     * @param minute 分
     * @param second 秒
     * @param hund   毫秒
     * @return
     */
    public static final byte[] BASKETBALL_CMD_DISPTM_PAUSE(int hour, int minute, int second, int hund) {
        byte[] cmdBype = {(byte) 0XA6, 0X13, 00, 00, 00, 00, (byte) 0XFF};
        cmdBype[2] = (byte) (hund & 0xff);// 毫秒
        cmdBype[3] = (byte) (second & 0xff);// 秒
        cmdBype[4] = (byte) (minute & 0xff);// 分
        cmdBype[5] = (byte) (hour & 0xff);// 时
        return cmdBype;
    }


    /**
     * 设置拦截器拦截的时间
     *
     * @param second 秒
     * @return
     */
    public static final byte[] BASKETBALL_CMD_SET_BLOCKERTIME(int second) {
        byte[] cmdBype = {(byte) 0XA6, 0X46, 00, (byte) 0XFF};
        cmdBype[2] = (byte) (second & 0xff);// 秒
        return cmdBype;
    }

    /**
     * 设置拦截器灵敏度
     *
     * @param sensitivity 灵敏度
     * @return
     */
    public static final byte[] BASKETBALL_CMD_SET_T(int sensitivity) {
        byte[] cmdBype = {(byte) 0XA6, 0X39, 00, (byte) 0XFF};
        cmdBype[2] = (byte) ((byte) sensitivity % 256);// 秒
        return cmdBype;
    }

    /**
     * 设置计时器时间显示精度
     *
     * @param uPrecision 0表示设置显示精度为十分秒
     *                   1表示设置显示精度为百分秒
     *                   2表示设置显示精度为千分秒
     * @return
     */
    public static final byte[] BASKETBALL_CMD_SET_PRECISION(int uPrecision) {
        byte[] cmdBype = {(byte) 0XA6, 0X48, 00, (byte) 0XFF};
        cmdBype[2] = (byte) (uPrecision & 0xff);// 秒
        return cmdBype;
    }

    /**
     * 显示屏
     *
     * @param showType UP =  1 ，DOWN=2
     * @param dataByte
     * @return
     */
    public static final byte[] BASKETBALL_CMD_DIS_LED(int showType, byte[] dataByte) {
        if (dataByte.length != 160)
            return null;
        byte[] cmdBype = new byte[165];

        cmdBype[0] = (byte) 0xa6;
        switch (showType) {
            case 1:
                cmdBype[1] = 50;
                break;
            case 2:
                cmdBype[1] = 51;
                break;
        }
        cmdBype[2] = 0;
        cmdBype[3] = (byte) 0xa0;
        System.arraycopy(dataByte, 0, cmdBype, 4, dataByte.length);
        cmdBype[164] = (byte) 0xff;


        return cmdBype;
    }

}
