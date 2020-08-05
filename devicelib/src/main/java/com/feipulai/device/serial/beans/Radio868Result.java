package com.feipulai.device.serial.beans;


import android.util.Log;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.SerialConfigs;
import com.orhanobut.logger.utils.LogUtils;

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
            LogUtils.all(data.length + "---" + StringUtility.bytesToHexString(data) + "---当前测试项目代码为-1");
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
                sitUp(data);
                break;
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

                sitUpHand(data);
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
                                byte[] bytes = new byte[16];
                                if (data.length > 16 && data[14] == 0x27 && data[15] == 0x0d) {
                                    System.arraycopy(data, 0, bytes, 0, 16);
                                    setType(SerialConfigs.SARGENT_JUMP_GET_SCORE_RESPONSE);
                                    setResult(new SargentJumpResult(bytes));
                                } else if (data.length == 16) {
                                    setType(SerialConfigs.SARGENT_JUMP_GET_SCORE_RESPONSE);
                                    setResult(new SargentJumpResult(data));
                                }

                                break;

                            case 0x06:

                                break;
                            case 0x0b:
                                byte[] bytes1 = new byte[16];
                                if (data.length > 16 && data[14] == 0x27 && data[15] == 0x0d) {
                                    System.arraycopy(data, 0, bytes1, 0, 16);
                                    setType(SerialConfigs.SARGENT_JUMP_SET_MATCH);
                                    setResult(new SargentJumpResult(bytes1));
                                } else if (data.length == 16) {
                                    setType(SerialConfigs.SARGENT_JUMP_SET_MATCH);
                                    setResult(new SargentJumpResult(data));
                                }

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
                            case 0x04:
                                if (data[16] == 0 && data.length != 24) {
                                    byte[] bytes = new byte[16];
                                    System.arraycopy(data, 0, bytes, 0, 16);
                                    setType(SerialConfigs.SARGENT_JUMP_GET_SCORE_RESPONSE);
                                    setResult(new SargentJumpResult(bytes));
                                } else if (data.length == 24) {
                                    setType(SerialConfigs.SARGENT_JUMP_CHECK);
                                    setResult(new SargentJumpResult(data));
                                }

                                break;
                            case 0x00:
                                setType(SerialConfigs.SARGENT_JUMP_EMPTY_RESPONSE);
                                break;
                            case 0x0A:
                                setType(SerialConfigs.SARGENT_GET_DATA);
                                setResult(new SargentJumpResult(data));
                                break;
                        }
                    }
                }
                break;
            case ItemDefault.CODE_PQ:
                if (data[0] == (byte) 0xAA && data[2] == 0x0A && data[data.length - 1] == 0x0d) {
                    Log.e("TAG", StringUtility.bytesToHexString(data));
                    switch (data[7]) {
                        case (byte) 0xb0:
                        case (byte) 0xb1:
                            setType(SerialConfigs.VOLLEY_BALL_SET_MORE_MATCH);
                            setResult(new VolleyPairResult(data));
                            break;
                        case (byte) 0xb3://查询状态
//                            Log.e("GGG",StringUtility.bytesToHexString(data));
                            setType(SerialConfigs.VOLLEY_BALL_STATE);
                            setResult(new VolleyPair868Result(data));
                            break;
                        case (byte) 0xb7://自检
                            setType(SerialConfigs.VOLLEY_BALL_SELFCHECK);
                            setResult(new VolleyPair868Result(data));
                            break;
                        case (byte) 0xb2://查询版本
                            setType(SerialConfigs.VOLLEY_BALL_GET_VER);
                            setResult(new VolleyPair868Result(data));
                            break;
                    }
                }
                break;
            case ItemDefault.CODE_LQYQ:
            case ItemDefault.CODE_ZQYQ:
                if (data[0] == (byte) 0xAA && data[data.length - 1] == 0x0D && data[2] == 0x0d) {
                    if (verifySum(data, 1, data.length - 2) != data[data.length - 2])
                        return;
                    setResult(new Basketball868Result(data));
                    switch (data[7]) {
                        case 0x01://0频配对返回
                            setType(SerialConfigs.DRIBBLEING_FREQUENCY);
                            break;
                        case 0x02://参数设置
                            setType(SerialConfigs.DRIBBLEING_PARAMETER);
                            break;
                        case 0x03://获取状态
                            setType(SerialConfigs.DRIBBLEING_START);
                            break;
                        case 0x04://等待
                            setType(SerialConfigs.DRIBBLEING_AWAIT);
                            break;
                        case 0x05://LED开始计时
                            setType(SerialConfigs.DRIBBLEING_LED_START_TIME);
                            break;
                        case 0x06://停止
                            setType(SerialConfigs.DRIBBLEING_STOP);
                            break;
                        case 0x07://空闲
                            setType(SerialConfigs.DRIBBLEING_FREE);
                            break;
                        case 0x08://led显示
                            setType(SerialConfigs.DRIBBLEING_LED_CONTENT);
                            break;
                        case 0x09://暂停
                            setType(SerialConfigs.DRIBBLEING_PAUSE);
                            break;
                        case 0x0a:
                            setType(SerialConfigs.DRIBBLEING_SET_SETTING);
                            break;

                    }

                }
                if (data[0] == 0xab && data[data.length-1] == 0x56){
                    Log.i("红外=====================",StringUtility.bytesToHexString(data));
                }
                break;

            case ItemDefault.CODE_FHL:

                if ((data[0] & 0xff) == 0xaa && data.length == 16) {
                    if (data[1] == 1 || data[1] == 3) {
                        setType(SerialConfigs.VITAL_CAPACITY_SET_MORE_MATCH);
                    } else {
                        setType(SerialConfigs.VITAL_CAPACITY_RESULT);
                    }
                    setResult(new VitalCapacityResult(data));
                } else if ((data[0] & 0xff) == 0xaa && data.length == 18) {
                    if (data[7] == 1 || data[7] == 2) {
                        setType(SerialConfigs.VITAL_CAPACITY_SET_MORE_MATCH);
                    } else {
                        setType(SerialConfigs.VITAL_CAPACITY_RESULT);
                    }
                    setResult(new VitalCapacityNewResult(data));
                }

                break;
            case ItemDefault.CODE_WLJ:
                if ((data[0] & 0xff) == 0xaa && data.length == 18) {
                    if (data[7] == 1 || data[7] == 2) {
                        setType(SerialConfigs.GRIP_SET_MORE_MATCH);
                    } else {
                        setType(SerialConfigs.VITAL_CAPACITY_RESULT);
                    }
                    setResult(new VitalCapacityNewResult(data));
                }

                break;
            case ItemDefault.CODE_HWSXQ:

                if ((data[0] & 0xff) == 0xaa && data.length == 21) {
                    byte b = data[7];
                    switch (b) {
                        case 1:
                        case 2:
                            setType(SerialConfigs.MEDICINE_BALL_MATCH_MORE);
                            setResult(new MedicineBallNewResult(data));
                            break;
                        case 3:
                            setType(SerialConfigs.MEDICINE_BALL_RESULT_MORE);
                            setResult(new MedicineBallNewResult(data));
                            break;
                        case 4:
                            setType(SerialConfigs.MEDICINE_BALL_START_MORE);
                            setResult(new MedicineBallNewResult(data));
                            break;
                        default:
                            break;


                    }
                }

                break;
            case ItemDefault.CODE_LDTY:
                if ((data[0] & 0xff) == 0xaa && (data[2] & 0xff) == 0x02) {
                    setResult(new StandJumpResult(data));
                    switch (data[7]) {
                        case 0x01://配对
                            setType(SerialConfigs.STAND_JUMP_FREQUENCY);
                            break;
                        case 0x02://设置参数
                            setType(SerialConfigs.STAND_JUMP_PARAMETER);
                            break;
                        case 0x03://查询
                            setType(SerialConfigs.STAND_JUMP_GET_STATE);
                            break;
                        case 0x04://开始
                            setType(SerialConfigs.STAND_JUMP_START);
                            break;
                        case 0x05://结束
                            setType(SerialConfigs.STAND_JUMP_END);
                            break;
                        case 0x06://空闲
                            setType(SerialConfigs.STAND_JUMP_LEISURE);
                            break;
                        case 0x07://版本号
                            setType(SerialConfigs.STAND_JUMP_VERSION);
                            break;
                        case 0x08://自检
                            setType(SerialConfigs.STAND_JUMP_CHECK);
                            break;
                        case 0x09://设置测试长度
                            setType(SerialConfigs.STAND_JUMP_SET_POINTS);
                            break;
                        case 0xa://新自检杆状态
                            setType(SerialConfigs.JUMP_NEW_SELF_CHECK_RESPONSE);
                            setResult(new JumpNewSelfCheckResult(1, data));
                            break;
                    }
                }

                break;
            case ItemDefault.CODE_ZWTQQ:
                if ((data[0] & 0xff) == 0xaa && (data.length == 21)) {
                    if (verifySum(data, 1, data.length - 2) != data[data.length - 2])
                        return;
                    setResult(new SitReachWirelessResult(data));
                    switch (data[7]) {
                        case 0x01://配对//设置参数
                        case 0x02:
                            setType(SerialConfigs.SIT_REACH_FREQUENCY);
                            break;
                        case 0x03://查询
                            setType(SerialConfigs.SIT_REACH_GET_STATE);
                            break;
                        case 0x04://开始
                            setType(SerialConfigs.SIT_REACH_START);
                            break;
                        case 0x05://结束
                            setType(SerialConfigs.SIT_REACH_END);
                            break;
                        case 0x06:
                            setType(SerialConfigs.SIT_REACH_LEISURE);
                            break;
                        case 0x07://版本号
                            setType(SerialConfigs.SIT_REACH_VERSION);
                            break;
                    }
                }
                break;
        }
    }

    private void sitUp(byte[] data) {
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
    }

    private void sitUpHand(byte[] data) {
        if (data.length >= 0x10
                && data[0] == 0x54 && data[1] == 0x55 //[00] [01]：包头高字节0x54   低字节0x55
                && data[3] == 0x10  //[02] [03]：长度高字节0x00   低字节0x10
                && data[5] == 0x00b//[05]：项目编号   5—仰卧起坐    8—俯卧撑 0x0b引体向上手臂检测
                && data[6] == 0x001//[06]：0x01引体向上手臂检测
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

                case 14:
                    setType(SerialConfigs.PULL_UP_GET_ANGLE_DATA);
                    setResult(new ArmStateResult(data));
                    break;
                case 13:
                    setType(SerialConfigs.PULL_UP_GET_GYRO_DATA);
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

    /**
     * 和校验
     *
     * @param data
     * @return
     */
    private byte verifySum(byte[] data, int index, int end) {
        byte sum = 0;
        for (int i = index; i < end; i++) {
            sum += data[i];
        }

        return sum;
    }
}
