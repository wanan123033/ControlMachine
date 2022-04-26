package com.feipulai.host.bean;

import java.io.Serializable;

public class FaceSdkBean implements Serializable {
    private String appId;//应用id
    private String sdkKey;//sdkkey
    private String activeKey;//收费时才有，激活码
    private String keyType;//key类型：0.免费，1.收费
    private String version;//sdk版本号

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSdkKey() {
        return sdkKey;
    }

    public void setSdkKey(String sdkKey) {
        this.sdkKey = sdkKey;
    }

    public String getActiveKey() {
        return activeKey;
    }

    public void setActiveKey(String activeKey) {
        this.activeKey = activeKey;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "FaceSdkBean{" +
                "appId='" + appId + '\'' +
                ", sdkKey='" + sdkKey + '\'' +
                ", activeKey='" + activeKey + '\'' +
                ", keyType='" + keyType + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
