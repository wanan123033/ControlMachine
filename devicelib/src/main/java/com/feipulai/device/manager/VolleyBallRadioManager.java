package com.feipulai.device.manager;

import android.os.SystemClock;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.orhanobut.logger.utils.LogUtils;

public class VolleyBallRadioManager {
    private static VolleyBallRadioManager radioManager;

    private VolleyBallRadioManager() {
    }

    public synchronized static VolleyBallRadioManager getInstance() {
        if (radioManager == null) {
            radioManager = new VolleyBallRadioManager();
        }
        return radioManager;
    }

    /**
     * 停止计数
     *
     * @param hostId
     * @param deviceId
     */
    public void stopCount(int hostId, int deviceId) {
        byte[] cmd = {(byte) 0xAA, 0x0F, 0x0A, 0x03, 0x01, 0x00, 0x00, (byte) 0xC5, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D};
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) deviceId;
        cmd[13] = (byte) sum(cmd, 13);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        LogUtils.serial("排球停止计数指令:" + StringUtility.bytesToHexString(cmd));
    }

    /**
     * 停止计时
     *
     * @param hostId
     * @param deviceId
     */
    public void stopTime(int hostId, int deviceId) {
        byte[] cmd = {(byte) 0xAA, 0x0F, 0x0A, 0x03, 0x01, 0x00, 0x00, (byte) 0xC4, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D};
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) deviceId;
        cmd[13] = (byte) sum(cmd, 13);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        LogUtils.serial("排球停止计数指令:" + StringUtility.bytesToHexString(cmd));
    }

    /**
     * 设备空闲
     *
     * @param hostId
     * @param deviceId
     */
    public void deviceFree(int hostId, int deviceId) {
        byte[] cmd = {(byte) 0xAA, 0x0e, 0x0A, 0x03, 0x01, 0x00, 0x00, (byte) 0xC6, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d};
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) deviceId;
        cmd[12] = (byte) sum(cmd, 12);
        LogUtils.serial("排球空闲指令:" + StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));

        try {
            Thread.sleep(100);
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
            Thread.sleep(100);
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始计数
     *
     * @param hostId
     * @param deviceId
     */
    public void startCount(int hostId, int deviceId) {
        byte[] cmd = {(byte) 0xAA, 0x0F, 0x0A, 0x03, 0x01, 0x00, 0x00, (byte) 0xC5, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x0D};
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) deviceId;
        cmd[13] = (byte) sum(cmd, 13);
        LogUtils.serial("排球开始计数指令:" + StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 开始计时
     *
     * @param hostId
     * @param deviceId
     * @param time     准备倒计时时长
     * @param timeSum  计时长度
     */
    public void startTime(int hostId, int deviceId, int time, int timeSum) {
        byte[] cmd = {(byte) 0xAA, 0x12, 0x0A, 0x03, 0x01, 0x00, 0x00, (byte) 0xC4, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x0D};
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) deviceId;
        cmd[13] = (byte) time;
        cmd[14] = (byte) (timeSum / 0x0100);
        cmd[15] = (byte) (timeSum % 0x0100);
        cmd[16] = (byte) sum(cmd, 16);
        LogUtils.serial("排球预备计时指令:" + StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        SystemClock.sleep(100);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        SystemClock.sleep(100);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 获取状态
     *
     * @param hostId
     * @param deviceId
     */
    public void getState(int hostId, int deviceId) {
        byte[] cmd = {(byte) 0xAA, 0x0e, 0x0a, 0x03, 0x01, 0x00, 0x00, (byte) 0xC3, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d};
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) deviceId;
        cmd[12] = (byte) sum(cmd, 12);
        LogUtils.serial("排球获取状态指令:" + StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 自检
     *
     * @param hostId
     * @param deviceId
     */
    public void selfCheck(int hostId, int deviceId) {
        byte[] cmd = {(byte) 0xAA, 0x0E, 0x0A, 0x03, 0x01, 0x00, 0x00, (byte) 0xC7, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d};
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) deviceId;
        cmd[12] = (byte) sum(cmd, 12);
        LogUtils.serial("排球自检指令:" + StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    private int sum(byte[] cmd, int index) {
        int sum = 0;
        for (int i = 2; i < index; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }

}
