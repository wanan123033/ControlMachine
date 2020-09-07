package com.feipulai.exam.bean;

public class UpdateApp {
    private String softwareName;
    private String version;
    private String softwareUrl;
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

    public String getSoftwareUrl() {
        return softwareUrl;
    }

    public void setSoftwareUrl(String softwareUrl) {
        this.softwareUrl = softwareUrl;
    }

    @Override
    public String toString() {
        return "UpdateApp{" +
                "softwareName='" + softwareName + '\'' +
                ", version='" + version + '\'' +
                ", softwareUrl='" + softwareUrl + '\'' +
                '}';
    }
}
