package com.feipulai.device.serial;

/**
 * Created by pengjf on 2018/11/5.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public enum SerialParams {

    QR_CODE("/dev/ttyMT3", 9600),
    RS232("/dev/ttysWK3", 9600),
    RADIO("/dev/ttysWK0", 115200),
    IC_Card("/dev/ttysWK1", 115200),
    //	ID_CARD("/dev/ttyMT1",115200),
    ID_CARD("/dev/ttysWK2", 115200),
    PRINTER("/dev/ttyMT2", 115200);

    private String path;
    private int baudRate;

    SerialParams(String path, int baudRate) {
        this.baudRate = baudRate;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

}

