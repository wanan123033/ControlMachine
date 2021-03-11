package com.feipulai.device.manager;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.orhanobut.logger.utils.LogUtils;

public class ShoulderManger {
    private int projectCode = 5;

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
}
