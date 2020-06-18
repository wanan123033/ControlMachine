package com.feipulai.device.udp.parse;


import android.util.Log;

import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.udp.UDPBasketBallConfig;
import com.feipulai.device.udp.result.BasketballResult;
import com.feipulai.device.udp.result.UDPResult;

/**
 * Created by zzs on  2019/5/23
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BasketballParser extends UDPParser {
    @Override
    public UDPResult parse(byte[] data) {
        if ((data[0] & 0xff) != 0xa6)
            return null;
        UDPResult udpResult = new UDPResult();
        BasketballResult result = new BasketballResult();
        result.setType((data[1] & 0xff));
        switch ((data[1] & 0xff)) {
            case UDPBasketBallConfig.CMD_SET_TIME_RESPONSE://设置显示内容返回：0XA6， CMD_SET_TIME , Hund, Second, Minute, Hour,0XFF
                result.setHund(data[2]*10);
                result.setSecond(data[3]);
                result.setMinute(data[4]);
                result.setHour(data[5]);
                break;
            case UDPBasketBallConfig.CMD_GET_TIME_RESPONSE://获取时间显示 0XA6(包头) CMD_GET_TIME (命令), ucFD(遮挡1,2) , Hund(毫秒), Second(秒), Minute(分), Hour(时), flag(标志) ,0XFF(包尾)
                result.setUcFD(data[2]);
                result.setHund(data[3]*10);
                result.setSecond(data[4]);
                result.setMinute(data[5]);
                result.setHour(data[6]);
                result.setFlag(data[7]);
                break;
            case UDPBasketBallConfig.CMD_SET_BRIGHT_RESPONSE://设置显示内容
                break;
            case UDPBasketBallConfig.CMD_SET_STATUS_RESPONSE://设置工作状态 0XA6,CMD_SET_STATUS, ucStatus,0XFF

            case UDPBasketBallConfig.CMD_ASYNTM_PAUSE_RESPONSE://同步时间并暂停显示0XA6(包头) CMD_SET_STATUS (命令), ucStatus(状态),0XFF(包尾)
            case UDPBasketBallConfig.CMD_ASYNTM_NOPAUSE_RESPONSE://同步时间不暂停显示
            case UDPBasketBallConfig.CMD_DISPTM_PAUSE_RESPONSE://显示时间并暂停，不同步时间
                result.setUcStatus(data[2]);
                break;
            case UDPBasketBallConfig.CMD_SET_STATUS_STOP_RESPONSE://停止
                result.setHund(data[2]*10);
                result.setSecond(data[3]);
                result.setMinute(data[4]);
                result.setHour(data[5]);
                break;
            case UDPBasketBallConfig.CMD_BREAK_RESPONSE://中断时间
                result.settNum(data[2]);
                result.setHund(data[3]*10);
                result.setSecond(data[4]);
                result.setMinute(data[5]);
                result.setHour(data[6]);
                break;

            case UDPBasketBallConfig.CMD_SET_BLOCKERTIME_RESPONSE://设置拦截器拦截的时间：0XA6， CMD_SET_ BLOCKERTIME, Second, 0XFF
            case UDPBasketBallConfig.CMD_GET_BLOCKERTIME_RESPONSE://获取拦截器拦截的时间0XA6， CMD_ GET _ BLOCKERTIME, Second, 0XFF
                result.setSecond(data[2]&0xff);
                break;
            case UDPBasketBallConfig.CMD_SET_PRECISION_RESPONSE://设置计时器时间显示精度
            case UDPBasketBallConfig.CMD_GET_PRECISION_RESPONSE://获取计时器时间显示精度
                result.setuPrecision(data[2]&0xff);
                break;
            case UDPBasketBallConfig.CMD_SET_T_RESPONSE://灵敏度
                break;
            case UDPBasketBallConfig.CMD_GET_STATUS_RESPONSE://获取工作状态
                if ((data[2]&0xff) == 0 && (data[6]&0xff) == 0xff) {
                    return null;
                }
                result.setUcStatus(data[2]&0xff);
                result.settNum(data[6]&0xff);

                break;

            default:
                return null;
        }
        udpResult.setType(result.getType());
        udpResult.setResult(result);
        return udpResult;
    }
}
