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
     *                        【1】包长，1字节，N=16
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
     *                        【N-3】预留，1字节，0x00
     *                        【N-2】检验和，1字节，sum={1:N-3}
     *                        【N-1】包尾，1字节,0x0D
     */
    public void setFrequency(int deviceId, int targetFrequency, int hostId, int targetHostId) {
        byte data[] = new byte[15];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 15;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) targetHostId;//hostId
        data[6] = (byte) deviceId;
        data[7] = (byte) 20;
        data[8] = (byte) 0x00;
        data[9] = (byte) 0x00;
        data[10] = (byte) (targetFrequency & 0xff);
        data[11] = (byte) 0x04;
//        data[12] = (byte) targetHostId;
        data[12] = (byte) 0x00;
        for (int i = 1; i <= data.length - 3; i++) {
            data[13] += data[i];
        }
        data[14] = (byte) 0x0d;

        RadioChannelCommand command = new RadioChannelCommand(targetFrequency);
        LogUtils.serial("运动计时设置参数指令" + StringUtility.bytesToHexString(data));
        LogUtils.serial("运动计时切换到指定频道指令" + StringUtility.bytesToHexString(command.getCommand()));
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
        LogUtils.serial("运动计时联络信号设备号是： " + StringUtility.bytesToHexString(data));
    }

    /**
     * 同步时间
     *
     * @param hostId
     * @param time
     */
    public void syncTime(int hostId, int time) {
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
        LogUtils.serial("运动计时同步时间： " + StringUtility.bytesToHexString(data));
    }

    /**
     * 同步时间
     *
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
        data[6] = (byte) (deviceId & 0xff);
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
        LogUtils.serial("运动计时同步时间deviceId： " + StringUtility.bytesToHexString(data));
    }

    public void getTime(int deviceId, int hostId) {
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
        LogUtils.serial("运动计时获取时间设备号是： " + StringUtility.bytesToHexString(data));
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
    public void getRecentCache(int deviceId, int hostId, int index) {
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
        LogUtils.serial("运动计时读取缓存： " + StringUtility.bytesToHexString(data));
    }

    /**
     * 设置子机工作状态
     *
     * @param hostId
     * @param state  【0】包头，1字节，0XAA
     *               【1】包长，1字节，N=12
     *               【2】项目编号，1字节，0x0E （运动计时仪）
     *               【3】目标设备编号，1字节，0x03计时子机
     *               【4】本设备编号，1字节，0x01 安卓机
     *               【5】本主机号，1字节
     *               【6】目标设备子机号，1字节，0xFF表示所有子机
     *               【7】命令字，1字节，03
     *               【8】状态，1字节，0停止计时 1开始计时
     *               【N-3】预留，1字节，0x00
     *               【N-2】检验和，1字节，sum={1:N-3}
     *               【N-1】包尾，1字节,0x0D
     */
    public void setDeviceState(int hostId, int state) {
        byte data[] = new byte[12];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 12;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) hostId;
        data[6] = (byte) 0xff;
        data[7] = (byte) 3;
        data[8] = (byte) (state & 0xff);
        data[9] = (byte) 0x00;
        for (int i = 1; i <= data.length - 3; i++) {
            data[10] += data[i];
        }
        data[11] = (byte) 0x0d;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
        LogUtils.serial("运动计时设置工作状态： " + StringUtility.bytesToHexString(data));
    }

    public void setDeviceState(int deviceId, int hostId, int state) {
        byte data[] = new byte[12];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 12;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) hostId;
        data[6] = (byte) (deviceId & 0xff);
        data[7] = (byte) 3;
        data[8] = (byte) (state & 0xff);
        data[9] = (byte) 0x00;
        for (int i = 1; i <= data.length - 3; i++) {
            data[10] += data[i];
        }
        data[11] = (byte) 0x0d;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
        LogUtils.serial("运动计时设置工作状态： " + StringUtility.bytesToHexString(data));
    }

    /**
     * 获取工作状态
     *
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
        LogUtils.serial("运动计时获取设备状态： " + StringUtility.bytesToHexString(data));
    }

    /**
     * 设置 子机 按钮重复按下间隔时间
     *
     * @param deviceId
     * @param hostId
     * @param time     0-255
     */
    public void setMinTime(int hostId, int time) {
        byte data[] = new byte[12];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 12;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) hostId;
        data[6] = (byte) (0xff);
        data[7] = (byte) 18;
        data[8] = (byte) (time & 0xff);
        data[9] = (byte) 0x00;
        for (int i = 1; i <= data.length - 3; i++) {
            data[10] += data[i];
        }
        data[11] = (byte) 0x0d;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
        LogUtils.serial("运动计时设置按钮重复按下间隔时间： " + StringUtility.bytesToHexString(data));
    }

    /**
     * 设置 子机 按钮重复按下间隔时间
     *
     * @param hostId
     */
    public void getMinTime(int hostId) {
        byte data[] = new byte[13];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 13;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) hostId;
        data[6] = (byte) 0X01;
        data[7] = (byte) 19;
        data[8] = (byte) 0x00;
        data[9] = (byte) 0x00;
        data[10] = 0x00;
        for (int i = 1; i <= data.length - 3; i++) {
            data[11] += data[i];
        }
        data[12] = (byte) 0x0d;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
    }

    public void setSensitiveTime(int hostId, int time) {
        byte data[] = new byte[12];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 12;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) hostId;
        data[6] = (byte) (0xff);
        data[7] = (byte) 5;
        data[8] = (byte) (time & 0xff);
        data[9] = (byte) 0x00;
        for (int i = 1; i <= data.length - 3; i++) {
            data[10] += data[i];
        }
        data[11] = (byte) 0x0d;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
        LogUtils.serial("运动计时设置工作状态： " + StringUtility.bytesToHexString(data));
    }

    public void getSensitiveTime(int hostId) {
        byte data[] = new byte[13];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 13;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) hostId;
        data[6] = (byte) 0X01;
        data[7] = (byte) 6;
        data[8] = (byte) 0x00;
        data[9] = (byte) 0x00;
        data[10] = (byte) 0x00;
        for (int i = 1; i <= data.length - 3; i++) {
            data[11] += data[i];
        }
        data[12] = (byte) 0x0d;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
    }

    public void setForceStart(int hostId, int time) {
        byte data[] = new byte[12];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 12;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) hostId;
        data[6] = (byte) (0xff);
        data[7] = (byte) 25;
        data[8] = (byte) (time & 0xff);
        data[9] = (byte) 0x00;
        for (int i = 1; i <= data.length - 3; i++) {
            data[10] += data[i];
        }
        data[11] = (byte) 0x0d;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
        LogUtils.serial("运动计时强启： " + StringUtility.bytesToHexString(data));
    }


    public void setLightTime(int hostId, int lightTime) {
        byte data[] = new byte[13];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 13;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) hostId;
        data[6] = (byte) (0xff);
        data[7] = (byte) 07;
        data[8] = (byte) (lightTime >> 8 & 0xff);
        data[9] = (byte) (lightTime & 0xff);
        for (int i = 1; i <= data.length - 3; i++) {
            data[11] += data[i];
        }
        data[12] = (byte) 0x0d;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
        LogUtils.serial("运动计时设置灯亮时间： " + StringUtility.bytesToHexString(data));
    }

    public void getLightTime(int hostId) {
        byte data[] = new byte[13];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 13;
        data[2] = (byte) 0x0E;
        data[3] = (byte) 0x03;
        data[4] = (byte) 0X01;
        data[5] = (byte) hostId;
        data[6] = (byte) 0X01;
        data[7] = (byte) 8;
        for (int i = 1; i <= data.length - 3; i++) {
            data[11] += data[i];
        }
        data[12] = (byte) 0x0d;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
        LogUtils.serial("运动计时获取灯亮时间： " + StringUtility.bytesToHexString(data));
    }
}
