package com.feipulai.device.serial.beans;

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
	public MedicineBallNewResult(byte[] data){
		result = ((data[14] & 0xff) << 8) + (data[15] & 0xff);
		checkFault(data);
		sweepPoint = data[18]&0xff;
		deviceId = data[6];
		if (data[7] == 1){
            frequency = data[12];
        }else if (data[07]== 3){
		    state = data[12];
        }

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
                '}';
    }
}
