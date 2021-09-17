package com.feipulai.device.manager;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.device.udp.UDPBasketBallConfig;
import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by pengjf on 2020/1/7.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class GripManager {
    /**
     * 握力计
     */
    public void setGrip(int targetChannel,  int deviceId, int hostId) {
        byte[] buf = new byte[18];
        buf[0] = (byte) 0xAA;
        buf[1] = 0x12;    //包长
        buf[2] = 0X0c;    //项目编号
        buf[3] = 0x03;    //目标设备编号（子设备）
        buf[4] = 0x01;    //本设备编号（主机）
        buf[5] = (byte) hostId;//本设备主机号
        buf[6] = (byte) (deviceId & 0xff); //目标设备子机号
        buf[7] = 0x01;    //命令设置参数

        buf[8] = 0;//目标设备序列号（4字节）
        buf[9] = 0;
        buf[10] = 0;
        buf[11] = 0x00;
        buf[12] = (byte) targetChannel;
        buf[13] = 0x04;
        buf[14] = (byte) hostId;
        buf[15] = (byte) deviceId;
        for (int i = 1; i < 16; i++) {
            buf[16] += buf[i];
        }
        buf[17] = 0x0D;   //包尾
        //Logger.i(StringUtility.bytesToHexString(buf));
        //先切到通信频段
        //Log.i("james","originFrequency:" + originFrequency);
//        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(originFrequency)));
        //Log.i("james",StringUtility.bytesToHexString(buf));
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RadioChannelCommand command = new RadioChannelCommand(targetChannel);
        LogUtils.serial("握力计设置频段参数指令:" + StringUtility.bytesToHexString(buf));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, buf));
        RadioManager.getInstance().sendCommand(new ConvertCommand(command));
    }

    /**
     *
     * @param hostId
     * @param deviceId
     * @param cmd 2获取状态 3开始 5空闲
     */
    public static void sendCommand(int hostId,int deviceId,int cmd){
        byte[] data = {(byte) 0xAA, 0x12, 0x09, 0x03, 0x01, (byte) hostId, (byte) deviceId, (byte) cmd,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x00, 0x0D};

        if (MachineCode.machineCode == ItemDefault.CODE_WLJ) {
            data[2] = 0x0c;
        } else {
            data[2] = 0x09;
        }
        int sum = 0;
        for (int i = 1; i < data.length - 2; i++) {
            sum += data[i];
        }
        data[16] = (byte) sum;

        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
        LogUtils.serial("握力计获取设备信息指令:" + StringUtility.bytesToHexString(data));
    }



}
