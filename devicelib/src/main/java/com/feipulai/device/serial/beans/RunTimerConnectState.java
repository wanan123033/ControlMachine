package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

import java.util.Arrays;

/**
 * Created by James on 2018/5/11 0011.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 * 实心球结果
 */

public class RunTimerConnectState {

    public int getOrderSensor() {
        return orderSensor;
    }

    public void setOrderSensor(int orderSensor) {
        this.orderSensor = orderSensor;
    }

    public byte[] getStartArray() {
        return startArray;
    }

    public void setStartArray(byte[] startArray) {
        this.startArray = startArray;
    }

    public byte[] getEndArray() {
        return endArray;
    }

    public void setEndArray(byte[] endArray) {
        this.endArray = endArray;
    }

    /**
     * power[0] 折返点
     * power[1] 起点
     * power[2] 发令传感器电量状态
     * power[3] 主机控制盒电量状态
     *
     * @return
     */
    public int[] getPower() {
        return power;
    }

    public void setPower(int[] power) {
        this.power = power;
    }

    public byte[] getStartPowerArray() {
        return startPowerArray;
    }

    public void setStartPowerArray(byte[] startPowerArray) {
        this.startPowerArray = startPowerArray;
    }

    public byte[] getEndPowerArray() {
        return endPowerArray;
    }

    public void setEndPowerArray(byte[] endPowerArray) {
        this.endPowerArray = endPowerArray;
    }

    public int getStartIntercept() {
        return startIntercept;
    }

    public void setStartIntercept(int startIntercept) {
        this.startIntercept = startIntercept;
    }

    public int getEndIntercept() {
        return endIntercept;
    }

    public void setEndIntercept(int endIntercept) {
        this.endIntercept = endIntercept;
    }

    /**
     * （2.6）控制盒－>PC，连机命令(0XC6)
     * [0]PK_HEAD_INCEPT	：包头				 	0xBB
     * [1]PK_LEN			:  包长					0x0C
     * [2]SYS_NAME		：系统名：折返跑计时	0xA0
     * [3]SYS_ID			：系统ID				0x00
     * [4]DES_DEVICE		：目的设备:控制盒		0xA1
     * [5]DEVICE_ID		：目的设备ID			0x00
     * [6]DEVICE_CMD		：连机命令         		0xC6
     * [7]PARAME1			：参数1  				0x01-0x02
     * [8]PARAME2			：参数2  				0x00-0xFF
     * [9]PARAME3			：参数3					0x00-0xFF
     * [10]SUM				：校验累加和=[0]+….+[9].0x00-0xFF
     * [11]PK_END			：包尾					0x0D
     * <p>
     * [7]PARAME1：   BIT7-BIT4为1  表示折返跑连接状态
     * BIT3：主机控制盒连接状态  有线连接  恒定为1
     * BIT2：发令传感器连接状态  1：正常  0：异常
     * BIT1：起点拦截器0连接状态1：正常  0：异常
     * BIT0：折返点拦截器0连接状态1：正常  0：异常
     * [8]PARAME2：	起点拦截器无线连接状况
     * BIT7-BIT0 表示拦截器8-1
     * 对应位为1表示连接正常
     * 对应位为0表示连接异常
     * [9]PARAME3：	折返点拦截器无线连接状况
     * BIT7-BIT0 表示拦截器8-1
     * 对应位为1表示连接正常
     * 对应位为0表示连接异常
     * <p>
     * <p>
     * [8]PARAME2：	起点拦截器电量状况
     * BIT7-BIT0 表示拦截器8-1
     * 对应位为1表示电量正常
     * 对应位为0表示电量异常
     * [9]PARAME3：	折返点拦截器电量状况
     * BIT7-BIT0 表示拦截器8-1
     * 对应位为1表示电量正常
     * 对应位为0表示电量异常
     */
    private int orderSensor;
    private int startIntercept;
    private int endIntercept;
    /**
     * 起点拦截器无线连接状况
     */
    private byte[] startArray;
    /**
     * 折返点拦截器无线连接状况
     */
    private byte[] endArray;
    /**
     * [7]PARAME1：  BIT7-BIT4为2  表示折返跑电量状态
     * BIT3：主机控制盒电量状态  1：正常  0：异常
     * BIT2：发令传感器电量状态  1：正常  0：异常
     * BIT1：起点拦截器0电量状态1：正常  0：异常
     * BIT0：折返点拦截器0电量状态1：正常  0：异常
     */
    private int[] power = new int[4];
    private byte startPowerArray[];
    private byte endPowerArray[];


    public RunTimerConnectState(byte[] data) {

        byte state = (byte) (data[7] & 0xff);
        byte state2 = (byte) (data[8] & 0xff);
        byte state3 = (byte) (data[9] & 0xff);
        checkConnect(state, state2, state3);
        LogUtils.normal("折返跑返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());

    }


    private void checkConnect(byte b, byte PARAME2, byte PARAME3) {

        byte[] bitArray = getBitArray(b);
        if (getHeight4(b) == 1) {
            endIntercept = bitArray[7];
            startIntercept = bitArray[6];
            orderSensor = bitArray[5];
            startArray = getBitArray(PARAME2);
            endArray = getBitArray(PARAME3);
        } else if (getHeight4(b) == 2) {
            power[3] = bitArray[4];
            power[2] = bitArray[5];
            power[1] = bitArray[6];
            power[0] = bitArray[7];
            startPowerArray = getBitArray(PARAME2);
            endPowerArray = getBitArray(PARAME3);
        }

    }


    @Override
    public String toString() {
        return "RunTimerConnectState{" +
                "orderSensor=" + orderSensor +
                ", startIntercept=" + startIntercept +
                ", endIntercept=" + endIntercept +
                ", startArray=" + Arrays.toString(startArray) +
                ", endArray=" + Arrays.toString(endArray) +
                ", power=" + Arrays.toString(power) +
                ", startPowerArray=" + Arrays.toString(startPowerArray) +
                ", endPowerArray=" + Arrays.toString(endPowerArray) +
                '}';
    }

    /**
     * 将byte转换为一个长度为8的byte数组，数组每个值代表bit
     */
    private byte[] getBitArray(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte) (b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }

    /**
     * 获取高四位
     *
     * @param data
     * @return
     */
    private int getHeight4(byte data) {
        int height;
        height = ((data & 0xf0) >> 4);
        return height;
    }

    /**
     * 获取低四位
     *
     * @param data
     * @return
     */
    private int getLow4(byte data) {
        int low;
        low = (data & 0x0f);
        return low;
    }

}
