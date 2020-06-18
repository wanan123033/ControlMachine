package com.feipulai.device.serial.beans;


import com.orhanobut.logger.utils.LogUtils;

import java.io.Serializable;

/**
 * Created by James on 2018/5/17 0017.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ArmStateResult implements Serializable,IDeviceResult {

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
	private int time;//时间相对于开始命令下达的时刻
    private int angle;



    private byte angleState;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

	public ArmStateResult(byte[] data){
		deviceId = data[4] & 0xff;
		projectCode = data[5] & 0xff;
		if (projectCode == 0x0b){
            time = ((data[8] & 0xff) << 8) + (data[9] & 0xff);
            result = data[12]& 0xff;
            angleState = data[10];
            angle = data[11]&0xff;
            LogUtils.normal("手臂检测(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());
        }

	}
    public byte getAngleState() {
        return angleState;
    }

    public void setAngleState(byte angleState) {
        this.angleState = angleState;
    }

	public ArmStateResult(){
	}
	
	public int getDeviceId(){
		return deviceId;
	}
	
	public void setDeviceId(int deviceId){
		this.deviceId = deviceId;
	}
	
	public int getProjectCode(){
		return projectCode;
	}
	
	public void setProjectCode(int projectCode){
		this.projectCode = projectCode;
	}
	
	public int getResult(){
		return result;
	}
	
	public void setResult(int result){
		this.result = result;
	}

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getAngle() {
        if (angleState == 1 || angleState == 3){
           return angle;
        }else {
            return -angle;
        }
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    @Override
    public String toString() {
        return "ArmStateResult{" +
                "deviceId=" + deviceId +
                ", projectCode=" + projectCode +
                ", result=" + result +
                ", time=" + time +
                ", angle=" + angle +
                ", angleState=" + angleState +
                '}';
    }


}
