package com.feipulai.exam.bean;

import java.io.Serializable;

public class SoftApp implements Serializable {
    private String softwareName;
    private String version;
    private String remark;
    public String getSoftwareName() {
        return softwareName;
    }

    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "SoftApp{" +
                "softwareName='" + softwareName + '\'' +
                ", version='" + version + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
