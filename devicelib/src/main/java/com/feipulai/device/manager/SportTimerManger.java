package com.feipulai.device.manager;

public class SportTimerManger {

    /**
     * 发送配对信息
     * @param deviceId
     * @param originFrequency
     * @param deviceFrequency
     * 【0】包头，1字节，0XAA
     * 【1】包长，1字节，N=16
     * 【2】项目编号，1字节，0x0E （运动计时仪）
     * 【3】目标设备编号，1字节，0x03计时子机
     * 【4】本设备编号，1字节，0x01 安卓机
     * 【5】本主机号，1字节
     * 【6】目标设备子机号，1字节
     * 【7】命令字，1字节，20
     * 【8：9】目标设备序列号，2字节，默认0x00 0x00
     * 【10】设置无线信道号，1字节
     * 【11】设置无线传输速率，1字节
     * 【12】设置主机号，1字节
     * 【N-3】预留，1字节，0x00
     * 【N-2】检验和，1字节，sum={1:N-3}
     * 【N-1】包尾，1字节,0x0D
     */
    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency,int hostId) {
        byte data []  = new byte[16];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 16;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x01;
        data[4] = (byte) 0X03;
        data[5] = (byte) hostId;
        data[6] = (byte) deviceId;
        data[7] = (byte) 20;
        data[8] = (byte) 0x00;
        data[9] = (byte) 0x00;
        data[10] = (byte)originFrequency;
        data[11] = (byte) 0x04;
        data[12] = (byte) hostId;
        data[13] = (byte) 0x00;
        data[14] = (byte) 0x00;
        data[15] = (byte) 0x0d;

        for (int i = 2;i<data.length -3;i++){
            data[data[1]-2] += data[i];
        }
    }


}
