package com.feipulai.device.manager;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.orhanobut.logger.utils.LogUtils;

public class SportTimerManger {

    /**
     * 发送配对设置
     *
     * @param deviceId
     * @param targetFrequency 【0】包头，1字节，0XAA
     *                        【1】包长，1字节，N=17
     *                        【2】项目编号，1字节，0x0E （运动计时仪）
     *                        【3】目标设备编号，1字节，0x03计时子机
     *                        【4】本设备编号，1字节，0x01 安卓机
     *                        【5】本主机号，1字节
     *                        【6】目标设备子机号，1字节
     *                        【7】命令字，1字节，20
     *                        【8：9】目标设备序列号，2字节，默认0x00 0x00
     *                        【10】设置无线信道号，1字节
     *                        【11】设置无线传输速率，1字节
     *                        【12】设置主机号，1字节
     *                        【13】设置子机号，1字节
     *                        【N-3】预留，1字节，0x00
     *                        【N-2】检验和，1字节，sum={1:N-3}
     *                        【N-1】包尾，1字节,0x0D
     */
    public void setFrequency(int deviceId, int targetFrequency, int hostId, int targetHostId) {
        byte data[] = new byte[16];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 17;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) hostId;
        data[6] = (byte) deviceId;
        data[7] = (byte) 20;
        data[8] = (byte) 0x00;
        data[9] = (byte) 0x00;
        data[10] = (byte) (targetFrequency & 0xff);
        data[11] = (byte) 0x04;
        data[12] = (byte) targetHostId;
        data[13] = (byte) deviceId;
        data[14] = (byte) 0x00;
        for (int i = 1; i <= data.length - 3; i++) {
            data[15] += data[i];
        }
        data[16] = (byte) 0x0d;

        RadioChannelCommand command = new RadioChannelCommand(targetFrequency);
        LogUtils.normal(data.length + "---" + StringUtility.bytesToHexString(data) + "---运动计时设置参数指令");
        LogUtils.normal(command.getCommand().length + "---" + StringUtility.bytesToHexString(command.getCommand()) + "---运动计时切换到指定频道指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
        RadioManager.getInstance().sendCommand(new ConvertCommand(command));
    }

    /**
     * 联络
     *
     * @param deviceId
     * @param hostId
     */
    public void connect(int deviceId, int hostId) {
        byte data[] = new byte[13];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 13;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) hostId;
        data[6] = (byte) deviceId;
        data[7] = (byte) 0;
        data[8] = (byte) 0x00;
        data[9] = (byte) 0x00;
        data[10] = (byte) 0x00;
        for (int i = 1; i <= data.length - 3; i++) {
            data[11] += data[i];
        }
        data[12] = (byte) 0x0d;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
        LogUtils.normal(data.length + "---" + StringUtility.bytesToHexString(data) + "---运动计时联络信号设备号是： "+deviceId);
    }

    /**
     * 同步时间
     * @param deviceId
     * @param hostId
     * @param time
     */
    public void syncTime(int deviceId, int hostId, int time) {
        byte data[] = new byte[15];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 15;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) hostId;
        data[6] = (byte) 0xFF;
        data[7] = (byte) 1;

        data[8] = (byte) (time >> 24 & 0xff);
        data[9] = (byte) (time >> 16 & 0xff);
        data[10] = (byte) (time >> 8 & 0xff);
        data[11] = (byte) (time & 0xff);

        data[12] = (byte) 00;
        for (int i = 1; i <= data.length - 3; i++) {
            data[13] += data[i];
        }
        data[14] = (byte) 0x0d;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
        LogUtils.normal(data.length + "---" + StringUtility.bytesToHexString(data) + "---运动计时同步时间");
    }

    public void getTime(int deviceId, int hostId){
        byte data[] = new byte[13];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 13;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) hostId;
        data[6] = (byte) deviceId;
        data[7] = (byte) 2;
        data[8] = (byte) 0x00;
        data[9] = (byte) 0x00;
        data[10] = (byte) 0x00;
        for (int i = 1; i <= data.length - 3; i++) {
            data[11] += data[i];
        }
        data[12] = (byte) 0x0d;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
        LogUtils.normal(data.length + "---" + StringUtility.bytesToHexString(data) + "---运动计时获取时间设备号是： "+deviceId);
    }

    /**
     * 读取缓存
     *
     * @param deviceId
     * @param hostId   【0】包头，1字节，0XAA
     *                 【1】包长，1字节，N=14
     *                 【2】项目编号，1字节，0x0E （运动计时仪）
     *                 【3】目标设备编号，1字节，0x03计时子机
     *                 【4】本设备编号，1字节，0x01 安卓机
     *                 【5】本主机号，1字节
     *                 【6】目标设备子机号，1字节
     *                 【7】命令字，1字节，13
     *                 【8：9】目标设备序列号，2字节，默认0x00 0x00
     *                 【10】获取第几次按钮按下状态信息，1字节，
     *                 每次等待发令后默认从第0次开始
     *                 【N-3】预留，1字节，0x00
     *                 【N-2】检验和，1字节，sum={1:N-3}
     *                 【N-1】包尾，1字节,0x0D
     */
    public void getRecentCache(int deviceId, int hostId,int index) {
        byte data[] = new byte[14];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 14;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) hostId;
        data[6] = (byte) deviceId;
        data[7] = (byte) 13;
        data[8] = (byte) 0x00;
        data[9] = (byte) 0x00;
        data[10] = (byte) index;
        data[11] = (byte) 0x00;
        for (int i = 1; i <= data.length - 3; i++) {
            data[12] += data[i];
        }
        data[13] = (byte) 0x0d;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
        LogUtils.normal(data.length + "---" + StringUtility.bytesToHexString(data) + "---运动计时读取缓存设备号是： "+deviceId+"包序是："+index);
    }

    /**
     * 设置子机工作状态
     * @param deviceId
     * @param hostId
     * @param state
     * 【0】包头，1字节，0XAA
     * 【1】包长，1字节，N=12
     * 【2】项目编号，1字节，0x0E （运动计时仪）
     * 【3】目标设备编号，1字节，0x03计时子机
     * 【4】本设备编号，1字节，0x01 安卓机
     * 【5】本主机号，1字节
     * 【6】目标设备子机号，1字节，0xFF表示所有子机
     * 【7】命令字，1字节，03
     * 【8】状态，1字节，0停止计时 1开始计时
     * 【N-3】预留，1字节，0x00
     * 【N-2】检验和，1字节，sum={1:N-3}
     * 【N-1】包尾，1字节,0x0D
     */
    public void setDeviceState(int deviceId, int hostId,int state){
        byte data[] = new byte[12];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 12;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) hostId;
        data[6] = (byte) 0xff;
        data[7] = (byte) 3;
        data[8] = (byte) (state&0xff);
        data[9] = (byte) 0x00;
        for (int i = 1; i <= data.length - 3; i++) {
            data[10] += data[i];
        }
        data[11] = (byte) 0x0d;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
        LogUtils.normal(data.length + "---" + StringUtility.bytesToHexString(data) + "---运动计时设置工作状态");
    }

    /**
     * 获取工作状态
     * @param deviceId
     * @param hostId
     */
    public void getDeviceState(int deviceId, int hostId) {
        byte data[] = new byte[13];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 13;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) hostId;
        data[6] = (byte) deviceId;
        data[7] = (byte) 4;
        data[8] = (byte) 0x00;
        data[9] = (byte) 0x00;
        data[10] = (byte) 0x00;
        for (int i = 1; i <= data.length - 3; i++) {
            data[11] += data[i];
        }
        data[12] = (byte) 0x0d;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
        LogUtils.normal(data.length + "---" + StringUtility.bytesToHexString(data) + "---运动计时获取设备状态： "+deviceId);
    }
}
