package com.feipulai.host.netUtils;

/**
 * Created by zzs on  2019/1/4
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ResponseParame {
    private String bizType;
    private String token;
    private String msEquipment;
    private String requestTime;
    private String sign;
    private String data;
    private String lastUpdateTime;

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMsEquipment() {
        return msEquipment;
    }

    public void setMsEquipment(String msEquipment) {
        this.msEquipment = msEquipment;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public String toString() {
        return "ResponseParame{" +
                "bizType='" + bizType + '\'' +
                ", token='" + token + '\'' +
                ", msEquipment='" + msEquipment + '\'' +
                ", requestTime='" + requestTime + '\'' +
                ", sign='" + sign + '\'' +
                ", data='" + data + '\'' +
                ", lastUpdateTime='" + lastUpdateTime + '\'' +
                '}';
    }
}
