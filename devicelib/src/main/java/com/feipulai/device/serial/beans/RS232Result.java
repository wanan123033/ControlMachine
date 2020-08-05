package com.feipulai.device.serial.beans;

import android.util.Log;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.SerialConfigs;
import com.orhanobut.logger.utils.LogUtils;


/**
 * Created by James on 2017/12/5.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

/**
 * 判断来自串口的信息类型,产生对应的结果(结果由对应的解析类产生)
 * 有些成果不需要结果返回,不设置结果即可
 */
public class RS232Result {

    private int type;
    private Object mResult;

    public RS232Result(byte[] data) {
        if (data == null) {
            return;
        }
        //Logger.i(StringUtility.bytesToHexString(data));
         Log.i("RS232Result", StringUtility.bytesToHexString(data));
        //Log.i("james","I got sth");
        if (MachineCode.machineCode == -1) {
            LogUtils.all(data.length + "---" + StringUtility.bytesToHexString(data) + "---当前测试项目代码为-1");
            return;
        }
        switch (MachineCode.machineCode) {

            //肺活量
            case ItemDefault.CODE_FHL:
                if (data.length == 5
                        && (data[0] & 0xff) == 0xaa
                        && (data[1] & 0xff) == 0xd0
                        && (((data[1] & 0xff) + (data[2] & 0xff) + (data[3] & 0xff)) & 0xff) == (data[4] & 0xff)) {
                    setType(SerialConfigs.VITAL_CAPACITY_RESULT);
                    setResult(new VCResult(data));
                }
                break;

            case ItemDefault.CODE_ZFP:
            case ItemDefault.CODE_LQYQ:
            case ItemDefault.CODE_SHOOT:
                if (data[data.length - 1] == 0x0d) {//包尾 ==0xd
                    switch (data[6]) {
                        case (byte) 0xc1://参数设置
                            setType(SerialConfigs.RUN_TIMER_SETTING);
                            break;
                        case (byte) 0xc2://准备
                            setType(SerialConfigs.RUN_TIMER_READY);
                            break;
                        case (byte) 0xc3://控制设备版本

                            break;
                        case (byte) 0xc4://强制启动
                            setType(SerialConfigs.RUN_TIMER_FORCE_START);
                            break;
                        case (byte) 0xc5://停止
                            setType(SerialConfigs.RUN_TIMER_STOP);
                            break;
                        case (byte) 0xc6://连接状态
                            setType(SerialConfigs.RUN_TIMER_CONNECT);
                            setResult(new RunTimerConnectState(data));
                            break;
                        case (byte) 0xc7://拦截时间
                            setType(SerialConfigs.RUN_TIMER_INTERCEPT_TIME);
                            setResult(new RunTimerResult(data));
                            break;
                        case (byte) 0xc8://违规返回
                            setType(SerialConfigs.RUN_TIMER_FAULT_BACK);
                            break;

                    }
                }
                break;
            // 立定跳远
            case ItemDefault.CODE_LDTY:
                // 红外实心球
            case ItemDefault.CODE_HWSXQ:
                // 坐位体前屈
            case ItemDefault.CODE_ZWTQQ:
                //摸高
            case ItemDefault.CODE_MG:
                // 排球
            case ItemDefault.CODE_PQ:
                setIfPossible(data);
                break;
            case ItemDefault.CODE_HW:
                setType(SerialConfigs.HEIGHT_WEIGHT_RESULT);
                setResult(new HeightWeightResult(data));
                break;
            case ItemDefault.CODE_FWC:
                if (data.length >= 0x10
                        && data[0] == 0x54 && data[1] == 0x55 //[00] [01]：包头高字节0x54   低字节0x55
                        && data[3] == 0x10  //[02] [03]：长度高字节0x00   低字节0x10
                        && data[5] == 0x08//[05]：项目编号   5—仰卧起坐    8—俯卧撑
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

            case ItemDefault.CODE_GPS:
                setType(SerialConfigs.GPS_TIME_RESPONSE);
                setResult(new GPSTimeResult(data));
                break;

        }
    }

    //包头：0X54(T) 0X55 (U)
    //包长：0X00,0X00
    //设备号：0x00
    //测试项目：0X00 ：0--空项目 1--摸高   2--立定跳远 4—光栅版坐位体前屈  7—实心球   10—排球垫球
    //模式：0X00
    //命令：0X00
    //参数：0X00…0X00   (不同项目不同命令，参数的字节数不同，若长度为16，共有6字节)
    //包尾：0X27(.)，0X0D(回车)
    private void setIfPossible(byte[] data) {
        // ((head[2] & 0xff) << 8) + (head[3] & 0xff)-->((data[2] & 0xff) * 256 + (data[3] & 0xff))
        if (data.length < 4 || data.length != ((data[2] & 0xff) << 8) + (data[3] & 0xff)) {
            //验证长度
            log("rs232 wrong data.length = " + data.length);
            return;
        } else if ((data[0] & 0xff) != 0x54 || (data[1] & 0xff) != 0x55) {
            //验证文件头
            log("rs232 wrong data head");
            return;
        } else if ((data[data.length - 2] & 0xff) != 0x27 || (data[data.length - 1] & 0xff) != 0x0d) {
            //验证结尾
            log("rs232 wrong data end");
            return;
        } else {
            int sum = 0;
            switch (data[5] & 0xff) {

                case 0x02://立定跳远
                    switch (data[7] & 0xff) {
                        case 0x01://开始测试
                            setType(SerialConfigs.JUMP_START_RESPONSE);
                            break;
                        case 0x02://结束测试
                            setType(SerialConfigs.JUMP_END_RESPONSE);
                            break;
                        case 0x04://立定跳远成绩
                            setType(SerialConfigs.JUMP_SCORE_RESPONSE);
                            setResult(new JumpScore(data));
                            break;
                        case 0x07://测量垫检测结果
                            if (data.length == 0x38) {
                                setType(SerialConfigs.JUMP_SELF_CHECK_RESPONSE);
                                setResult(new JumpSelfCheckResult(data));
                            } else {
                                setType(SerialConfigs.JUMP_SELF_CHECK_RESPONSE_Simple);
                            }
                            break;
                        case 0xa:
                            setType(SerialConfigs.JUMP_NEW_SELF_CHECK_RESPONSE);
                            setResult(new JumpNewSelfCheckResult(0, data));
                            break;
                        case 0x0c://测量垫版本
                            setType(SerialConfigs.JUMP_GET_VERSION_RESPONSE);
                            setResult(new JumpVersion(data));
                            break;
                        case 0x06://设置点数
                            setType(SerialConfigs.JUMP_SET_POINTS);
                            setResult(data);
                            break;
                    }
                    break;

                case 0x4://光栅版坐位体前屈
                    for (int i = 2; i < data.length - 3; i++) {
                        sum += (data[i] & 0xff);
                    }
                    //校验和不正确
                    if ((sum & 0xff) != (data[data.length - 3] & 0xff)) {
                        return;
                    }
                    switch (data[7] & 0xff) {
                        case 0x00:
                            setType(SerialConfigs.SIT_AND_REACH_EMPTY_RESPONSE);
                            break;

                        case 0x01:
                            setType(SerialConfigs.SIT_AND_REACH_START_RESPONSE);
                            break;

                        case 0x04:
                            setType(SerialConfigs.SIT_AND_REACH_RESULT_RESPONSE);
                            setResult(new SitReachResult(data));
                            break;

                        case 0x02:
                            setType(SerialConfigs.SIT_AND_REACH_STOP_RESPONSE);
                            break;

                        case 0x0c:
                            setType(SerialConfigs.SIT_AND_REACH_GET_VERSION_RESPONSE);
                            setResult(new SitReachVersionResult(data));
                            break;

                    }
                    break;

                case 0x07://实心球
                    switch (data[7] & 0xff) {

                        case 0:
                            setType(SerialConfigs.MEDICINE_BALL_EMPTY_RESPONSE);
                            break;

                        case 1:
                            setType(SerialConfigs.MEDICINE_BALL_START_RESPONSE);
                            break;

                        case 2:
                            setType(SerialConfigs.MEDICINE_BALL_STOP_RESPONSE);
                            break;

                        case 4:
                            setType(SerialConfigs.MEDICINE_BALL_GET_SCORE_RESPONSE);
                            setResult(new MedicineBallResult(data));
                            break;

                        case 0x07:
                            setType(SerialConfigs.MEDICINE_BALL_SELF_CHECK_RESPONSE);
                            setResult(new MedicineBallSelfCheckResult(data));
                            break;

                    }
                    break;

                //case 0x0://空项目
                //	break;
                //
                case 0x01://摸高
                    for (int i = 2; i < data.length - 3; i++) {
                        sum += (data[i] & 0xff);
                    }
                    //校验和不正确
//                	if((sum & 0xff) != (data[data.length - 3] & 0xff)){
//                		setValid(false);
//                		return;
//                	}
                    switch (data[7] & 0xff) {

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
                            setType(SerialConfigs.SARGENT_JUMP_SET_0__RESPONSE);
                            break;

                    }
                    break;

                case 0xa://排球垫球

                    switch (data[7] & 0xff) {
                        case 0:
                            setType(SerialConfigs.VOLLEYBALL_EMPTY_RESPONSE);
                            break;

                        case 1:
                            setType(SerialConfigs.VOLLEYBALL_START_RESPONSE);
                            break;

                        case 2:
                            setType(SerialConfigs.VOLLEYBALL_STOP_RESPONSE);
                            break;

                        case 4:
                            for (int i = 2; i < data.length - 3; i++) {
                                sum += (data[i] & 0xff);
                            }
                            //校验和不正确
                            if ((sum & 0xff) != (data[data.length - 3] & 0xff)) {
                                break;
                            }
                            setType(SerialConfigs.VOLLEYBALL_RESULT_RESPONSE);
                            setResult(new VolleyBallResult(data));
                            break;
                        case 6:
                            setType(SerialConfigs.VOLLEYBALL_LOSE_DOT_RESPONSE);
                            setResult(data[8]);
                            break;
                        case 7:
                            type = data[12];
                            if (type == 0) {
                                break;
                            }
                            setType(SerialConfigs.VOLLEYBALL_CHECK_RESPONSE);
                            setResult(new VolleyBallCheck(data));
                            break;
                        case 8:
                            type = data[8];
                            if (type == 0) {
                                break;
                            }
                            setType(SerialConfigs.VOLLEYBALL_CHECK_RESPONSE);
                            setResult(new VolleyBallCheck(data));
                            break;
                        case 12:
                            String version = (data[8] >>> 4) + "." + (data[8] & 0x0f) + "." + (data[9] & 0xff);
                            setType(SerialConfigs.VOLLEYBALL_VERSION_RESPONSE);
                            setResult(version);
                            break;
                    }
                    break;

            }
        }
    }

    private void log(String s) {
        Log.i("RS232Result", s);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setResult(Object result) {
        mResult = result;
    }

    public Object getResult() {
        return mResult;
    }

}
