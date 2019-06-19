package com.feipulai.device.udp.result;

/**
 * Created by zzs on  2019/5/23
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class UDPResult {
    private int type;
    private Object mResult;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getResult() {
        return mResult;
    }

    public void setResult(Object mResult) {
        this.mResult = mResult;
    }


    @Override
    public String toString() {
        return "UDPResult{" +
                "type=" + type +
                ", mResult=" + mResult +
                '}';
    }
}
