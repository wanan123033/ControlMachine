package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

import java.util.Arrays;

/**
 * Created by zzs on  2019/11/12
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandJumpResult {
    public static final int STATE_LEISURE = 0;
    public static final int STATE_TEST = 1;
    public static final int STATE_END = 2;
    private String serialNumber;//设备序列号

    private int frequency;
    private int deviceId;
    private int hostId;
    private int state;
    private boolean isFoul;
    private int score;
    private int brokenLEDs[] = new int[3];//记录被遮挡的LED编码
    private int mTerminalCondition;
    public static final int NORMAL = 0x0;
    public static final int NEED_CHANGE = 0x01;
    public static final int HAS_BROKEN_POINTS = 0x02;

    private int batteryLeftPercent;

    public StandJumpResult(byte[] data) {

        serialNumber = (data[8] & 0xff) + (data[9] & 0xff) + (data[10] & 0xff) + (data[11] & 0xff) + "";
        frequency = (data[12] & 0xff);
        deviceId = (data[6] & 0xff);
        hostId = (data[5] & 0xff);

        state = (data[12] & 0xff);

        //获取到的成绩需要加50cm
        score = ((data[14] & 0xff) << 8) + (data[15] & 0xff) + 50;
        if ((data[13] & 0x80) == 0x80) {
            isFoul = true;
        } else {
            for (int i = 16; i < 19; i++) {
                if ((data[i] & 0xff) != 0xff) {
                    break;
                }
                isFoul = true;
            }
        }
        batteryLeftPercent = data[18] & 0xff;
        brokenLEDs[0] = ((data[12] & 0xff) << 8) + (data[13] & 0xff);
        brokenLEDs[1] = ((data[14] & 0xff) << 8) + (data[15] & 0xff);
        brokenLEDs[2] = ((data[16] & 0xff) << 8) + (data[17] & 0xff);
        if (data[18] == 0x01) {
            mTerminalCondition = HAS_BROKEN_POINTS;
        } else if (brokenLEDs[0] != 0 || brokenLEDs[1] != 0 || brokenLEDs[2] != 0) {
            mTerminalCondition = NEED_CHANGE;
        } else {
            mTerminalCondition = NORMAL;
        }

        LogUtils.normal("立定跳远返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());

    }


    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isFoul() {
        return isFoul;
    }

    public void setFoul(boolean foul) {
        isFoul = foul;
    }

    public int[] getBrokenLEDs() {
        return brokenLEDs;
    }

    public void setBrokenLEDs(int[] brokenLEDs) {
        this.brokenLEDs = brokenLEDs;
    }

    public int getmTerminalCondition() {
        return mTerminalCondition;
    }

    public void setmTerminalCondition(int mTerminalCondition) {
        this.mTerminalCondition = mTerminalCondition;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getBatteryLeftPercent() {
        return batteryLeftPercent;
    }

    public void setBatteryLeftPercent(int batteryLeftPercent) {
        this.batteryLeftPercent = batteryLeftPercent;
    }

    @Override
    public String toString() {
        return "StandJumpResult{" +
                "serialNumber='" + serialNumber + '\'' +
                ", frequency=" + frequency +
                ", deviceId=" + deviceId +
                ", hostId=" + hostId +
                ", state=" + state +
                ", isFoul=" + isFoul +
                ", score=" + score +
                ", brokenLEDs=" + Arrays.toString(brokenLEDs) +
                ", mTerminalCondition=" + mTerminalCondition +
                '}';
    }
}
