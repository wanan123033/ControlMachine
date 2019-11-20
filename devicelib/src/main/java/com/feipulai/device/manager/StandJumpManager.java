package com.feipulai.device.manager;

import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;

/**
 * Created by zzs on  2019/11/12
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandJumpManager {

    /**
     * 设置频道
     * @param hostId
     * @param deviceId
     * @param originFrequency
     * @param points
     */
    public static void setFrequencyParameter(int hostId, int deviceId, int originFrequency, int points) {
        int machineCode = MachineCode.machineCode;
        int targetChannel = SerialConfigs.sProChannels.get(machineCode) + hostId - 1;
        byte[] cmd = new byte[]{(byte) 0xAA, 0x15, 0x02, 0x03, 0x01, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D};
        cmd[12] = (byte) (targetChannel & 0xff);
        cmd[14] = (byte) (hostId & 0xff);
        cmd[15] = (byte) (deviceId & 0xff);
        cmd[16] = (byte) ((points >> 8) & 0xff);// 次低位
        cmd[17] = (byte) (points & 0xff);// 最低位
        cmd[19] = (byte) sum(cmd, 19);

        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(originFrequency)));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(targetChannel)));
    }

    /**
     * 设置使用长度
     * @param hostId
     * @param deviceId
     * @param points
     */
    public static void setPoints(int hostId, int deviceId, int points) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x15, 0x02, 0x03, 0x01, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D};
        cmd[5] = (byte) (hostId & 0xff);
        cmd[6] = (byte) (deviceId & 0xff);
        cmd[12] = (byte) ((points >> 8) & 0xff);// 次低位
        cmd[13] = (byte) (points & 0xff);// 最低位
        cmd[19] = (byte) sum(cmd, 19);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 获取状态
     * @param hostId
     * @param deviceId
     * @param points
     */
    public static void getState(int hostId, int deviceId, int points) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x15, 0x02, 0x03, 0x01, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D};
        cmd[5] = (byte) (hostId & 0xff);
        cmd[6] = (byte) (deviceId & 0xff);
        cmd[12] = (byte) ((points >> 8) & 0xff);// 次低位
        cmd[13] = (byte) (points & 0xff);// 最低位
        cmd[19] = (byte) sum(cmd, 19);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /***
     * 开始测试
     * @param hostId
     * @param deviceId
     */
    public static void startTest(int hostId, int deviceId) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x15, 0x02, 0x03, 0x01, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D};
        cmd[5] = (byte) (hostId & 0xff);
        cmd[6] = (byte) (deviceId & 0xff);
        cmd[19] = (byte) sum(cmd, 19);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 结束
     * @param hostId
     * @param deviceId
     */
    public static void endTest(int hostId, int deviceId) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x15, 0x02, 0x03, 0x01, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D};
        cmd[5] = (byte) (hostId & 0xff);
        cmd[6] = (byte) (deviceId & 0xff);
        cmd[19] = (byte) sum(cmd, 19);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    public static void setLeisure(int hostId, int deviceId) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x15, 0x02, 0x03, 0x01, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D};
        cmd[5] = (byte) (hostId & 0xff);
        cmd[6] = (byte) (deviceId & 0xff);
        cmd[19] = (byte) sum(cmd, 19);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    public static void getVersion(int hostId, int deviceId) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x15, 0x02, 0x03, 0x01, 0x00, 0x00, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D};
        cmd[5] = (byte) (hostId & 0xff);
        cmd[6] = (byte) (deviceId & 0xff);
        cmd[19] = (byte) sum(cmd, 19);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    public static void checkDevice(int hostId, int deviceId) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x15, 0x02, 0x03, 0x01, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D};
        cmd[5] = (byte) (hostId & 0xff);
        cmd[6] = (byte) (deviceId & 0xff);
        cmd[19] = (byte) sum(cmd, 19);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    private static int sum(byte[] cmd, int index) {
        int sum = 0;
        for (int i = 1; i < index; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }
}
