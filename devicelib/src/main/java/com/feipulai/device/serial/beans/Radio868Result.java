package com.feipulai.device.serial.beans;


import android.util.Log;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.SerialConfigs;

import static com.feipulai.device.serial.SerialConfigs.JUMPROPE_RESPONSE;

/**
 * Created by James on 2017/12/5.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class Radio868Result {

    private int mType;
    private Object mResult;

    public Radio868Result(byte[] data) {
        Log.i("james", StringUtility.bytesToHexString(data));
        if (MachineCode.machineCode == -1) {
            return;
        }
        // 不处理其他非当前测试项目的噪音信息
        switch (MachineCode.machineCode) {
            // 跳绳
            case ItemDefault.CODE_TS:
                if (data.length > 12
                        && (data[0] & 0xff) == 0xaa
                        && (data[1] & 0xff) == 0x01
                        && (data[2] & 0xff) == 0xa2
                        && data[12] == 0x0d) {
                    setType(JUMPROPE_RESPONSE);
                    setResult(new JumpRopeResult(data));
                }
                break;

            // 仰卧起坐
            case ItemDefault.CODE_YWQZ:
                if (data.length >= 0x10
                        && data[0] == 0x54 && data[1] == 0x55 //[00] [01]：包头高字节0x54   低字节0x55
                        && data[3] == 0x10  //[02] [03]：长度高字节0x00   低字节0x10
                        && data[5] == 0x05//[05]：项目编号   5—仰卧起坐    8—俯卧撑
                        && data[14] == 0x27 && data[15] == 0x0d//[14] [15]：包尾高字节0x27   低字节0x0d
                ) {
                    // 仰卧起坐
                    int sum = 0;
                    //0b 命令(获取数据)不需要校验
                    if (data[7] != 0x0b) {
                        for (int i = 2; i < 13; i++) {
                            sum += (data[i] & 0xff);
                        }
                        if ((sum & 0xff) != (data[13] & 0xff)) {
                            return;
                        }
                    }
                    switch (data[7]) {

                        case 4:
                            setType(SerialConfigs.SIT_UP_GET_STATE);
                            setResult(new SitPushUpStateResult(data));
                            break;

                        case 0x0b:
                            setType(SerialConfigs.SIT_UP_MACHINE_BOOT_RESPONSE);
                            setResult(new SitPushUpSetFrequencyResult(data));
                            break;

                        case 0x0c:
                            setType(SerialConfigs.SIT_UP_GET_VERSION);
                            setResult(new SitPushUpVersionResult(data));
                            break;

                    }
                }
                break;

            case ItemDefault.CODE_FWC:
                if (data.length >= 0x10
                        && data[0] == 0x54 && data[1] == 0x55 //[00] [01]：包头高字节0x54   低字节0x55
                        && data[3] == 0x10  //[02] [03]：长度高字节0x00   低字节0x10
                        && data[5] == 0x08//[05]：项目编号   5—仰卧起坐    8—俯卧撑
                        && data[14] == 0x27 && data[15] == 0x0d//[14] [15]：包尾高字节0x27   低字节0x0d
                ) {
                    // 俯卧撑
                    int sum = 0;
                    //0b 命令(获取数据)不需要校验
                    if (data[7] != 0x0b) {
                        for (int i = 2; i < 13; i++) {
                            sum += (data[i] & 0xff);
                        }
                        if ((sum & 0xff) != (data[13] & 0xff)) {
                            return;
                        }
                    }
                    switch (data[7]) {

                        case 4:
                            setType(SerialConfigs.PUSH_UP_GET_STATE);
                            setResult(new SitPushUpStateResult(data));
                            break;

                        case 0x0b:
                            setType(SerialConfigs.PUSH_UP_MACHINE_BOOT_RESPONSE);
                            setResult(new SitPushUpSetFrequencyResult(data));
                            break;

                        case 0x0c:
                            setType(SerialConfigs.PUSH_UP_GET_VERSION);
                            setResult(new SitPushUpVersionResult(data));
                            break;

                    }
                }
                break;
            case ItemDefault.CODE_MG:
                if (data[0] == 0x54 && data[1] == 0x55 && data[5] == 0x01) {//[00] [01]：包头高字节0x54   低字节0x55
                    if (data[6] == 0x00) {
                        switch (data[7]) {
                            case 0x00:
                                setType(SerialConfigs.SARGENT_JUMP_EMPTY_RESPONSE);
                                break;

                            case 0x01:
                                setType(SerialConfigs.SARGENT_JUMP_START_RESPONSE);
                                break;

                            case 0x02:
                                setType(SerialConfigs.SARGENT_JUMP_STOP_RESPONSE);
                                break;

                            case 0x04:
                                setType(SerialConfigs.SARGENT_JUMP_GET_SCORE_RESPONSE);
                                setResult(new SargentJumpResult(data));
                                break;

                            case 0x06:

                                break;
                            case 0x0b:
                                setType(SerialConfigs.SARGENT_JUMP_SET_MATCH);
                                setResult(new SargentJumpResult(data));
                                break;
                        }
                    } else if (data[6] == 0x01) {
                        switch (data[7]) {
                            case 0x01:
                                setType(SerialConfigs.SARGENT_JUMP_SET_MORE_MATCH);
                                setResult(new SargentJumpResult(data));
                                break;
                            case 0x02:
                                setType(SerialConfigs.SARGENT_JUMP_EMPTY_RESPONSE);
                                setResult(new SargentJumpResult(data));
                                break;
                        }
                    }
                }
                break;
            case ItemDefault.CODE_PQ:
                if (data[0] == 0xAA && data[2] == 0x0A && data[data.length - 1] == 0x0d) {
                    switch (data[7]) {
                        case (byte) 0xb0:
                            setType(SerialConfigs.VOLLEY_BALL_SET_MORE_MATCH);
                            setResult(new VolleyPairResult(data));
                            break;
                        case (byte) 0xb3://查询状态
                            setType(SerialConfigs.VOLLEY_BALL_SET_MORE_MATCH);
                            setResult(new VolleyPairResult(data));
                            break;
                        case (byte) 0xb7://自检
                            setType(SerialConfigs.VOLLEY_BALL_SET_MORE_MATCH);
                            setResult(new VolleyPairResult(data));
                            break;
                        case (byte) 0xb2://查询版本
                            setType(SerialConfigs.VOLLEY_BALL_SET_MORE_MATCH);
                            setResult(new VolleyPairResult(data));
                            break;
                    }
                }
                break;
//            case ItemDefault.CODE_ZFP://无效
//                if (data.length == 0x0c) {
//                    switch (data[6]) {
//                        case (byte) 0xc1://参数设置
//                            setType(SerialConfigs.RUN_TIMER_SETTING);
//                            break;
//                        case (byte) 0xc2://准备
//                            setType(SerialConfigs.RUN_TIMER_READY);
//                            break;
//                        case (byte) 0xc3://控制设备版本
//
//                            break;
//                        case (byte) 0xc4://强制启动
//                            setType(SerialConfigs.RUN_TIMER_FORCE_START);
//                            break;
//                        case (byte) 0xc5://停止计时
//                            setType(SerialConfigs.RUN_TIMER_STOP);
//                            break;
//                        case (byte) 0xc6://连接状态
//                            setType(SerialConfigs.RUN_TIMER_CONNECT);
//                            setResult(new RunTimerConnectState(data));
//                            break;
//                        case (byte) 0xc7://拦截时间
//                            setType(SerialConfigs.RUN_TIMER_INTERCEPT_TIME);
//                            setResult(new RunTimerResult(data));
//                            break;
//                        case (byte) 0xc8://违规返回
//                            setType(SerialConfigs.RUN_TIMER_FAULT_BACK);
//                            break;
//
//
//                    }
//
//
//                }
//                break;

            case ItemDefault.CODE_YTXS:
                if (data.length >= 0x10
                        && data[0] == 0x54 && data[1] == 0x55
                        && data[3] == 0x10
                        && data[5] == 0x0b
                        && data[14] == 0x27 && data[15] == 0x0d) {
                    int sum = 0;
                    //0B命令不需要校验
                    if (data[7] != 0x0b) {
                        for (int i = 2; i < 13; i++) {
                            sum += (data[i] & 0xff);
                        }
                        if ((sum & 0xff) != (data[13] & 0xff)) {
                            return;
                        }
                    }
                    switch (data[7]) {
                        case 4:
                            setType(SerialConfigs.PULL_UP_GET_STATE);
                            setResult(new PullUpStateResult(data));
                            break;

                        case 0x0b:
                            setType(SerialConfigs.PULL_UP_MACHINE_BOOT_RESPONSE);
                            setResult(new PullUpSetFrequencyResult(data));
                            break;

                        case 0x0c:
                            setType(SerialConfigs.PULL_UP_GET_VERSION);
                            setResult(new PullUpVersionResult(data));
                            break;

                    }
                }
                break;
            case ItemDefault.CODE_FHL:

                if ((data[0] & 0xff) == 0xaa && data.length == 16) {
                    if (data[1] == 1 ||data[1] == 3){
                        setType(SerialConfigs.VITAL_CAPACITY_SET_MORE_MATCH);
                    }else {
                        setType(SerialConfigs.VITAL_CAPACITY_RESULT);
                    }
                    setResult(new VitalCapacityResult(data));
                }else if ((data[0] & 0xff) == 0xaa && data.length == 18){
                    if (data[7] == 1 ||data[7] == 2){
                        setType(SerialConfigs.VITAL_CAPACITY_SET_MORE_MATCH);
                    }else {
                        setType(SerialConfigs.VITAL_CAPACITY_RESULT);
                    }
                    setResult(new VitalCapacityNewResult(data));
                }

                break;
        } /*else if(data.length >= 0x10 && data[0] == 0x54 && data[1] == 0x55 && data[3] == 0x10 && data[14] == 0x27 && data[15] == 0x0d
				&& data[5] == 0x08){
			int sum = 0;
			//0B命令不需要校验
			if(data[7] != 0x0b){
				for(int i = 2;i < 13;i++){
					sum += (data[i] & 0xff);
				}
				if((sum & 0xff) != (data[13] & 0xff)){
					return;
				}
			}
			switch(data[7]){
				
				case 4:
					setType(SerialConfigs.PUSH_UP_GET_STATE);
					setResult(new PushUpStateResult(data));
					break;
				
				case 0x0b:
					setType(SerialConfigs.PUSH_UP_MACHINE_BOOT_RESPONSE);
					//Logger.i(Arrays.toString(data));
					setResult(new PushUpSetFrequencyResult(data));
					break;
				
				case 0x0c:
					setType(SerialConfigs.PUSH_UP_GET_VERSION);
					setResult(new PushUpVersionResult(data));
					break;
				
			}
		}*/
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public void setResult(Object result) {
        mResult = result;
    }

    public Object getResult() {
        return mResult;
    }

    private byte[] getBitArray(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte) (b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }
}
