package com.feipulai.exam.activity.MiddleDistanceRace.server;

/**
 * created by ww on 2019/7/30.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class WebConfig {
    private int port;//端口
    private int maxParallels;//最大监听数

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxParallels() {
        return maxParallels;
    }

    public void setMaxParallels(int maxParallels) {
        this.maxParallels = maxParallels;
    }
}
