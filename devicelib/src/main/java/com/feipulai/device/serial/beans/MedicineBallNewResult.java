package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

import java.util.Arrays;

/**
 * Created by James on 2018/5/11 0011.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 * 实心球结果
 */

public class MedicineBallNewResult {

	//0xAA  15  0D  01  03  01  01  01  00  00  00  00  46  04  01  01  00  00  00  75  0D
	private int result;

	/**是否犯规，协议中并没有关于是否犯规的说明 ，但实际上应该有*/
	private boolean fault ;
    /**扫描到的点数*/
	private int sweepPoint;
	private int deviceId;
	private int frequency;
    private int state ;//0 空闲 1测量 2结束
    private int hostId;
    public int[] getIncorrectPoles() {
        return incorrectPoles;
    }

    public void setIncorrectPoles(int[] incorrectPoles) {
        this.incorrectPoles = incorrectPoles;
    }

    private int[] incorrectPoles;
    private int numOfPoles;
    private boolean isInCorrect;
	public MedicineBallNewResult(byte[] data){
		result = ((data[14] & 0xff) << 8) + (data[15] & 0xff);
		checkFault(data);
		sweepPoint = data[18]&0xff;
		deviceId = data[6];
		if (data[7] == 1||data[7] == 2){
            frequency = data[12];
            hostId = data[5];
        }else if (data[07]== 3){
		    state = data[12];

		    //检测测量杆是否正常
            //0xAA  15  07  01  03  06  01  03  00  00  00  00  01  80  00  00  00  00  00  AB  0D
            numOfPoles = data[16] & 0xff;
            int tmp = ((data[13] & 0xff) << 8) +  (data[14] & 0xff);
            incorrectPoles = new int[numOfPoles];
            int bit = 0x80_00;
            int j = 0;
            for(int i = 1;i < numOfPoles + 1;i++){
                if((tmp & bit) != 0){
                    isInCorrect = true;
                    incorrectPoles[j ++] = i;
                }
                bit >>= 1;
            }
        }
        LogUtils.normal("实心球返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());

	}

    public int getSweepPoint() {
        return sweepPoint;
    }

	//若byte8的最高位为1或 byte16/byte17这2个字节都为0xff，则表示犯规；
	private void checkFault(byte[] data) {
		for (int i = 16; i < 18; i++) {
			if ((data[i] & 0xff) != 0xff) {
				return;
			}
			fault = true;
		}
	}



	public boolean isFault() {
		return fault;
	}

	public int getResult(){
		return result;
	}
	
	public void setResult(int result){
		this.result = result;
	}


    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "MedicineBallNewResult{" +
                "result=" + result +
                ", fault=" + fault +
                ", sweepPoint=" + sweepPoint +
                ", deviceId=" + deviceId +
                ", frequency=" + frequency +
                ", state=" + state +
                ", incorrectPoles=" + Arrays.toString(incorrectPoles) +
                ", numOfPoles=" + numOfPoles +
                ", isInCorrect=" + isInCorrect +
                '}';
    }

    public boolean isInCorrect() {
        return isInCorrect;
    }

    public void setInCorrect(boolean inCorrect) {
        isInCorrect = inCorrect;
    }

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }
}
