package com.feipulai.device.serial.beans;

import com.feipulai.device.serial.SerialConfigs;
import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by James on 2018/4/11 0011.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class VCResult {

    private int result;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }


    public VCResult(byte data[]) {
        result = ((data[2] & 0xff) << 8) + (data[3] & 0xff);

        LogUtils.serial("肺活量返回成绩(解析前):" + StringUtility.bytesToHexString(data));
        LogUtils.serial("肺活量返回成绩(解析后):" + toString());
    }

    @Override
    public String toString() {
        return "VCResult{" +
                "result=" + result +
                '}';
    }
}
