package com.feipulai.device.manager;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.orhanobut.logger.utils.LogUtils;

public class ShoulderManger {

    /**
     * 设置终端频率和设备ID,该函数中时延为调优过得,请勿随意更改
     * 该方法执行完成后,会跳转到主机号相对应的频率
     * <p>
     * 子机开机0频段发送
     【0】包头，1字节，0XAA
     【1】包长，1字节，N=15
     【2】项目编号，1字节，0x05 （仰卧起坐）
     【3】目标设备编号，1字节，0x03背部检测子机
     【4】本设备编号，1字节，0x01 安卓机
     【5】本主机号，1字节
     【6】目标设备子机号，1字节
     【7】命令字，1字节，14
     【8：9】目标设备序列号，2字节，默认0x00 0x00
     【10】设置无线信道号，1字节
     【11】设置无线传输速率，1字节
     【N-3】预留，1字节，0x00
     【N-2】检验和，1字节，sum={1:N-3}
     【N-1】包尾，1字节,0x0D
     * <p>
     * 发完后，切换到本机通道号，等待接收上面 0x54 0x55的命令
     *
     * @param originFrequency 原来的频段(终端目前工作所在频段)
     * @param deviceId        终端设备的设备ID
     * @param hostId          主机号
     */
    public void setFrequency(int targetChannel, int originFrequency, int deviceId, int hostId) {

        byte[] buf = new byte[15];
        buf[0] = (byte) 0xAA;//包头
        buf[1] = 15;    //包长
        buf[2] = 0x05;       //项目编号
        buf[3] = 0x03;
        buf[4] = (byte) (0x01 & 0xff);      //设备号
        buf[5] = (byte) (hostId & 0xff);     //本主机号
        buf[6] = (byte) (deviceId& 0xff);       //目标设备子机号
        buf[7] = 14;      //命令
        buf[8] = 0;
        buf[9] = 0;
        buf[10] = (byte) (targetChannel & 0xff);
        buf[11] = 4;
        buf[12] = 0;
        for (int i = 1; i < 12; i++) {
            buf[13] += buf[i] & 0xff;
        }
        buf[14] = 0x0d;   //包尾
        //Logger.i(StringUtility.bytesToHexString(buf));
        //先切到通信频段
        //Log.i("james","originFrequency:" + originFrequency);
//        RadioChannelCommand command = new RadioChannelCommand(originFrequency);
//        LogUtils.normal(command.getCommand().length+"---"+ StringUtility.bytesToHexString(command.getCommand())+"---仰卧起坐肩胛");
//        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(originFrequency)));
//        //Log.i("james",StringUtility.bytesToHexString(buf));
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        LogUtils.normal(buf.length+"---"+StringUtility.bytesToHexString(buf)+"---仰卧起坐肩胛设置参数指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, buf));
        RadioChannelCommand command1 = new RadioChannelCommand(targetChannel);
        LogUtils.normal(command1.getCommand().length+"---"+StringUtility.bytesToHexString(command1.getCommand())+"---仰卧起坐肩胛切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command1));
    }


    public void getState(int deviceId,int hostId){
        byte data[] = new byte[13];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 13;
        data[2] = (byte) 0x05;
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
        LogUtils.normal(data.length + "---" + StringUtility.bytesToHexString(data) + "---仰卧起坐肩胛联络信号设备号是： "+deviceId);
    }

    public void syncTime(int hostId, int time){
        byte data[] = new byte[15];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 15;
        data[2] = (byte) 0x05;
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
        LogUtils.normal(data.length + "---" + StringUtility.bytesToHexString(data) + "---仰卧起坐肩胛同步时间");
    }

    public void getTime(int deviceId, int hostId){
        byte data[] = new byte[13];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 13;
        data[2] = (byte) 0x05;
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
        LogUtils.normal(data.length + "---" + StringUtility.bytesToHexString(data) + "---仰卧起坐肩胛获取时间设备号是： "+deviceId);
    }

    /**
     * 读取缓存
     *
     * @param deviceId
     * @param hostId   【0】包头，1字节，0XAA
     *                 【1】包长，1字节，N=14
     *                 【2】项目编号，1字节，0x05 （仰卧起坐）
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
        data[2] = (byte) 0x05;
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
        LogUtils.normal(data.length + "---" + StringUtility.bytesToHexString(data) + "---仰卧起坐肩胛读取缓存设备号是： "+deviceId+"包序是："+index);
    }

    /**
     * 设置子机工作状态
     * @param deviceId
     * @param hostId
     * @param state
     * 【0】包头，1字节，0XAA
     * 【1】包长，1字节，N=12
     * 【2】项目编号，1字节，0x0E （仰卧起坐肩胛仪）
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
        data[2] = (byte) 0x05;
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
        LogUtils.normal(data.length + "---" + StringUtility.bytesToHexString(data) + "---仰卧起坐肩胛设置工作状态");
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
        data[2] = (byte) 0x05;
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
        LogUtils.normal(data.length + "---" + StringUtility.bytesToHexString(data) + "---仰卧起坐肩胛获取设备状态： "+deviceId);
    }
}
