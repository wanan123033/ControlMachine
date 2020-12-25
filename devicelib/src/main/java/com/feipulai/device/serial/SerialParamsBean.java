package com.feipulai.device.serial;

import java.io.Serializable;

/**
 * Created by zzs on  2020/12/1
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SerialParamsBean implements Serializable {
    //  "name": "ID_CARD",
//          "serial": "/dev/ttysWK2",
//          "baud": 115200,
//          "type": "usb",
//          "pid": 29987,
//          "vid": 6790
    private String name;
    private String serial;
    private int baud;
    private String type;
    private int vid;
    private int pid;
    private int versions;

    public int getVersions() {
        return versions;
    }

    public void setVersions(int versions) {
        this.versions = versions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public int getBaud() {
        return baud;
    }

    public void setBaud(int baud) {
        this.baud = baud;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getVid() {
        return vid;
    }

    public void setVid(int vid) {
        this.vid = vid;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    @Override
    public String toString() {
        return "SerialParamsBean{" +
                "name='" + name + '\'' +
                ", serial='" + serial + '\'' +
                ", baud=" + baud +
                ", type='" + type + '\'' +
                ", vid=" + vid +
                ", pid=" + pid +
                '}';
    }
}
