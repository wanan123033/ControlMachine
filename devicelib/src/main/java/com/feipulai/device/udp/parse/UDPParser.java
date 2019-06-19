package com.feipulai.device.udp.parse;


import com.feipulai.device.udp.result.UDPResult;

/**
 * Created by zzs on  2019/5/23
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public abstract class UDPParser {
    public abstract UDPResult parse(byte[] data);
}
