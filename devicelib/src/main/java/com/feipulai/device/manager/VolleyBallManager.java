package com.feipulai.device.manager;

import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;

/**
 * Created by James on 2018/3/8 0008.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class VolleyBallManager {

    public static final int VOLLEY_BALL_DISCONNECT = -1;
    public static final int VOLLEY_BALL_CONNECT = 0;

    private static final byte[] EMPTY = {0X54, 0X44, 00, 0X10, 00, 0xa, 00, 00, 0, 00, 00, 00, 00, 0x1a, 0x27, 0x0d};
    private static final byte[] CMD_START = {0X54, 0X44, 00, 0X10, 00, 0Xa, 00, 0x01, 00, 00, 00, 00, 00, 0x1b, 0x27, 0x0d};
    private static final byte[] CMD_END = {0X54, 0X44, 00, 0X10, 00, 0Xa, 00, 0x02, 00, 00, 00, 0, 00, 0x1c, 0x27, 0x0d};
    private static final byte[] CMD_GET_SCORE = {0X54, 0X44, 00, 0X10, 00, 0Xa, 00, 0x04, 00, 00, 00, 00, 00, 0x1e, 0x27, 0x0d};
    private static final byte[] CMD_CHECK = {0x54, 0x44, 0x00, 0x10, 0x00, 0x0a, 0x00, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0X21, 0x27, 0x0d};

    // 主机下发，测量杆收到会原样回复(0x44变化为0x55)。主机每5秒发送一次，用于检查测量杆连接是否正常。
    public void emptyCommand() {
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, EMPTY));
    }

    public void startTest() {
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, CMD_START));
    }

    public void stopTest() {
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, CMD_END));
    }

    public void getScore() {
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, CMD_GET_SCORE));
    }

    public void checkDevice() {
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, CMD_CHECK));
    }

    /**
     *
     * @param mode 对空对墙模式，对空0，对墙1
     * @param poleNum 几米杆，2米为2,  3米为3   默认值：对空3米 对墙2米
     * @param wiringType 同端异端接线，同端为0，异端为1  默认值  对空 1 对墙 0
     */
    public void setDeviceMode(int mode, int poleNum, int wiringType) {
        byte[] cmd = {0x54, 0x44, 0, 0x10, 0, 0x0a, 0, 0x11, (byte) mode, (byte) poleNum, (byte) wiringType, 0, 0, 0, 0x27, 0x0d};
        for (int i = 2; i < 13; i++) {
            cmd[13] += cmd[i] & 0xff;
        }
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd));
    }
}
