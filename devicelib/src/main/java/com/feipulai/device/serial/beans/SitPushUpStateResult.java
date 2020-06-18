package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

import java.io.Serializable;

/**
 * Created by James on 2018/5/17 0017.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitPushUpStateResult implements Serializable, IDeviceResult {

    private static final long serialVersionUID = 2696421872787504009L;

    //[00] [01]：包头高字节0x54   低字节0x55
    //[02] [03]：长度高字节0x00   低字节0x10
    //[04]：子机id
    //[05]：项目编号   5—仰卧起坐    8—俯卧撑
    //[06]：0   –单机模式
    //[07]：4   --获取数据
    //[08] [09]：计数值高字节   低字节 （非计数状态，非结束状态，该部分为0）
    //[10]：子机状态 空闲=1 准备=2 计数=3 结束=4 其他=未知
    //[11]：电池电量（百分比值）
    //[12]：子机baseline  (易中难，分别对应角度是55，65，75度，默认65度)
    //[13]：累加和
    //[14] [15]：包尾高字节0x27   低字节0x0d

    private int deviceId;
    private int projectCode;
    private int result;
    private int state;
    private int batteryLeft;
    private int baseline;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public SitPushUpStateResult(byte[] data) {
        deviceId = data[4] & 0xff;
        projectCode = data[5] & 0xff;

        result = ((data[8] & 0xff) << 8) + (data[9] & 0xff);
        batteryLeft = data[11] & 0xff;
        state = data[10] & 0xff;
        baseline = data[12] & 0xff;
        LogUtils.normal("仰卧起坐俯卧撑返回设备状态数据(解析前):" + data.length + "---" + StringUtility.bytesToHexString(data) + "---\n(解析后):" + toString());


    }

    public SitPushUpStateResult() {
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(int projectCode) {
        this.projectCode = projectCode;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getBatteryLeft() {
        return batteryLeft;
    }

    public void setBatteryLeft(int batteryLeft) {
        this.batteryLeft = batteryLeft;
    }

    public int getBaseline() {
        return baseline;
    }

    public void setBaseline(int baseline) {
        this.baseline = baseline;
    }




    @Override
    public String toString() {
        return "SitPushUpStateResult{" +
                "deviceId=" + deviceId +
                ", projectCode=" + projectCode +
                ", result=" + result +
                ", state=" + state +
                ", batteryLeft=" + batteryLeft +
                ", baseline=" + baseline +
                '}';
    }
}
