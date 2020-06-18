package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

import java.util.Arrays;

/**
 * 接收指令的父类
 */
public class BaseVolleyReceiveZl {
    private byte tou;     //包头
    private byte chang;   //包长
    private byte item_code;  //项目编号
    private byte device_code; //目标设备编号(主机)
    private byte device_code1; //本设备编号(控制盒)
    private byte device_host;  //本设备主机号
    private byte device_child; //本设备子机号
    private byte zl;           //指令编号
    private int device_sn;     //本设备序列号
    private byte wireless;     //本设备无线频道号
    private byte host;         //本设备主机号
    private byte child;        //本设备子机号
    private byte sum;          //校验和
    private byte wei;          //包尾

    private String soft;     //软件版本
    private String harware;  //硬件版本

    private byte state; //0 空闲；0x01 计时准备；0x02 计时中；0x03 计时结束；0x11 计数准备；0x12 计数中；0x13 计数结束；
    private int sun;  //计数值

    private byte type; //对空垫球 0x00；对墙垫球 0x01
    private byte ganchang; //1 米杆：0x02；2 米杆：0x04；3 米杆：0x06
    private byte[] result = new byte[5]; //对管自检结果，每对对管占一位，0 正常，1 异常；

    public BaseVolleyReceiveZl(byte[] data){

        tou = data[0];
        chang = data[1];
        item_code = data[2];
        device_code = data[3];
        device_code1 = data[4];
        device_host = data[5];
        device_child = data[6];
        zl = data[7];
        device_sn = ((((data[8] & 0xff) << 8 | (data[9] & 0xff)) << 8) | (data[10] & 0xff)) << 8 | (data[11] & 0xff);
        switch (zl){
            case (byte) 0xB1:   //联机
            case (byte) 0xb0:
                wireless = data[12];
                host = data[13];
                child = data[14];
                sum = data[15];
                wei = data[16];
                break;
            case (byte) 0xB2:   //查询版本
                StringBuffer sb = new StringBuffer();
                for (int i = 12 ; i <= 19 ; i++){
                    sb.append(data[i]);
                }
                soft = sb.toString();
                sb.delete(0,soft.length());
                for (int i = 20 ; i <= 27 ; i++){
                    sb.append(data[i]);
                }
                harware = sb.toString();
                sum = data[28];
                wei = data[29];
                break;
            case (byte) 0xb3:  //查询状态
                state = data[12];
                sun = data[13] * 0x0100 + data[14];
                sum = data[15];
                wei = data[16];
                break;
            case (byte) 0xb7:  //自检
                type = data[12];
                ganchang = data[13];
                result[0] = data[14];
                result[1] = data[15];
                result[2] = data[16];
                result[3] = data[17];
                result[4] = data[18];
                sum = data[19];
                wei = data[20];
                break;
        }

        LogUtils.normal("排球返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());
    }

    public byte getTou() {
        return tou;
    }

    public byte getChang() {
        return chang;
    }

    public byte getItem_code() {
        return item_code;
    }

    public byte getDevice_code() {
        return device_code;
    }

    public byte getDevice_code1() {
        return device_code1;
    }

    public byte getDevice_host() {
        return device_host;
    }

    public byte getDevice_child() {
        return device_child;
    }

    public byte getZl() {
        return zl;
    }

    public int getDevice_sn() {
        return device_sn;
    }

    public byte getWireless() {
        return wireless;
    }

    public byte getHost() {
        return host;
    }

    public byte getChild() {
        return child;
    }

    public byte getSum() {
        return sum;
    }

    public byte getWei() {
        return wei;
    }

    public String getSoft() {
        return soft;
    }

    public String getHarware() {
        return harware;
    }

    public byte getState() {
        return state;
    }

    public int getSun() {
        return sun;
    }

    public byte getType() {
        return type;
    }

    public byte getGanchang() {
        return ganchang;
    }

    public byte[] getResult(){
        return result;
    }

    @Override
    public String toString() {
        return "BaseVolleyReceiveZl{" +
                "tou=" + tou +
                ", chang=" + chang +
                ", item_code=" + item_code +
                ", device_code=" + device_code +
                ", device_code1=" + device_code1 +
                ", device_host=" + device_host +
                ", device_child=" + device_child +
                ", zl=" + zl +
                ", device_sn=" + device_sn +
                ", wireless=" + wireless +
                ", host=" + host +
                ", child=" + child +
                ", sum=" + sum +
                ", wei=" + wei +
                ", soft='" + soft + '\'' +
                ", harware='" + harware + '\'' +
                ", state=" + state +
                ", sun=" + sun +
                ", type=" + type +
                ", ganchang=" + ganchang +
                ", result=" + Arrays.toString(result) +
                '}';
    }
}
