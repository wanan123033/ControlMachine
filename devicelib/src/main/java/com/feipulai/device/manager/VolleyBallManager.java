package com.feipulai.device.manager;

import android.os.SystemClock;
import android.util.Log;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;

import java.util.Arrays;

/**
 * Created by James on 2018/3/8 0008.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class VolleyBallManager {

    public static final int VOLLEY_BALL_DISCONNECT = -1;
    public static final int VOLLEY_BALL_CONNECT = 0;

    private static final byte[] EMPTY = {0X54, 0X44, 0x00, 0X10, 0x00, 0xa, 0x00, 0x00, 0, 0x00, 0x00, 0x00, 0x00, 0x1a, 0x27, 0x0d};
    private static final byte[] CMD_START = {0X54, 0X44, 0x00, 0X10, 0x00, 0Xa, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1b, 0x27, 0x0d};
    private static final byte[] CMD_END = {0X54, 0X44, 0x00, 0X10, 0x00, 0Xa, 0x00, 0x02, 0x00, 0x00, 0x00, 0, 0x00, 0x1c, 0x27, 0x0d};
    private static final byte[] CMD_GET_SCORE = {0X54, 0X44, 0x00, 0X10, 0x00, 0Xa, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1e, 0x27, 0x0d};
    private static final byte[] CMD_CHECK = {0x54, 0x44, 0x00, 0x10, 0x00, 0x0a, 0x00, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0X21, 0x27, 0x0d};
    private static final byte[] CMD_LOSE_DOT = {0x54, 0x44, 0x00, 0x10, 0x00, 0x0a, 0x00, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x20, 0x27, 0x0d};
    private static final byte[] CMD_CANCEL_LOSE_DOT = {0x54, 0x44, 0x00, 0x10, 0x00, 0x0a, 0x00, 0x06, 0x01, 0x00, 0x00, 0x00, 0x00, 0x21, 0x27, 0x0d};
    private static final byte[] CMD_VERSIONS = {0x54, 0x44, 0x00, 0x10, 0x00, 0x0a, 0x00, 0x0C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x26, 0x27, 0x0d};
    private int pattryType;
    private VolleyBallRadioManager volleyBallRadioManager;

    public VolleyBallManager(int pattryType) {
        this.pattryType = pattryType;
        if (pattryType == 1) {
            volleyBallRadioManager = VolleyBallRadioManager.getInstance();
        }
    }


    // 主机下发，测量杆收到会原样回复(0x44变化为0x55)。主机每5秒发送一次，用于检查测量杆连接是否正常。
    public void emptyCommand() {
        if (pattryType == 0) {
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, EMPTY));
        }


    }

    public void startTest() {
        if (pattryType == 0) {
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, CMD_START));

        }
    }

    public void startTest(int hostId, int deviceId, int time, int testTime) {

        if (pattryType == 0) {
            Log.e("TAG===", Arrays.toString(CMD_START));
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, CMD_START));
        } else {
            if (testTime == 0) {
                volleyBallRadioManager.startCount(hostId, deviceId);
            } else {
                volleyBallRadioManager.startTime(hostId, deviceId, time, testTime);
            }
        }
    }

    public void stopTest() {
        if (pattryType == 0)
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, CMD_END));
    }

    public void stopTest(int hostId, int deviceId, int timeSum) {
        if (pattryType == 0)
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, CMD_END));
        else {
            if (timeSum == 0) {
                volleyBallRadioManager.stopCount(hostId, deviceId);
            } else {
                volleyBallRadioManager.stopTime(hostId, deviceId);
            }
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            VolleyBallRadioManager.getInstance().deviceFree(hostId, 1);
        }
    }

    public void getScore() {
        if (pattryType == 0)
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, CMD_GET_SCORE));
    }

    public void getScore(int hostId, int deviceId) {
        if (pattryType == 0)
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, CMD_GET_SCORE));
        else {
            volleyBallRadioManager.getState(hostId, deviceId);
        }
    }

    public void checkDevice(int hostId, int deviceId) {
        if (pattryType == 0)
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, CMD_CHECK));
        else {
            VolleyBallRadioManager.getInstance().selfCheck(hostId, deviceId);
        }
    }

    public void getVersions() {
        if (pattryType == 0) {
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, CMD_VERSIONS));
        }
    }

    /**
     * 忽略点
     */
    public void loseDot() {
        if (pattryType == 0) {
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, CMD_LOSE_DOT));
        }
    }

    /**
     * 取消忽略点
     */
    public void cancelLoseDot() {
        if (pattryType == 0) {
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, CMD_CANCEL_LOSE_DOT));
        } else {

        }
    }

    /**
     * 忽略点
     */
    public void loseDot(int deviceId, int hostId, byte[] checkResult) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x13, 0x0A, 0x03, 0x01, 0x00, 0x00, (byte) 0xC9, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00, 0x0d};
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) deviceId;
        cmd[12] = checkResult[0];
        cmd[13] = checkResult[1];
        cmd[14] = checkResult[2];
        cmd[15] = checkResult[3];
        cmd[16] = checkResult[4];
        cmd[17] = (byte) sum(cmd, 17);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        SystemClock.sleep(100);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        SystemClock.sleep(100);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 取消忽略点
     */
    public void cancelLoseDot(int deviceId, int hostId) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x13, 0x0A, 0x03, 0x01, 0x00, 0x00, (byte) 0xC9, 0x00, 0x00, 0x00, 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00, 0x0d};
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) deviceId;
        cmd[17] = (byte) sum(cmd, 17);

        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        SystemClock.sleep(100);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        SystemClock.sleep(100);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    private int sum(byte[] cmd, int index) {
        int sum = 0;
        for (int i = 2; i < index; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }

    public void setFrequency(int targetChannel, int deviceId, int hostId) {

        byte[] buf = new byte[17];
        buf[0] = (byte) 0xAA;
        buf[1] = 0x11;//包长
        buf[2] = (byte) (10 & 0xff);     //测试项目
        buf[3] = 0x03;//目标设备编号：0x03（控制盒属于计数器）
        buf[4] = 0x01;      //本设备编号：0x01（主机）
        buf[5] = (byte) (hostId & 0xff);     //本设备主机号
        buf[6] = (byte) (deviceId & 0xff);       //目标设备子机号
//        buf[6] = 0;
        buf[7] = (byte) 0xc1;      //命令
        buf[8] = 0x00; //高字节在先
        buf[9] = 0x00;
        buf[10] = 0x00;
        buf[11] = 0x00;
        buf[12] = (byte) (targetChannel & 0xff); //高字节在先
        buf[13] = (byte) hostId;
        buf[14] = (byte) (deviceId & 0xff);
        for (int i = 1; i < 15; i++) {
            buf[15] += buf[i] & 0xff;
        }
        buf[16] = 0x0d;   //包尾
        //Logger.i(StringUtility.bytesToHexString(buf));
        //先切到通信频段
        //Log.i("james","originFrequency:" + originFrequency);
//        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(originFrequency)));
        //Log.i("james",StringUtility.bytesToHexString(buf));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, buf));
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(targetChannel)));

    }
}
