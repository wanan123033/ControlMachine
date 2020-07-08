package com.feipulai.host.activity.setting;

import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.host.config.TestConfigs;

/**
 * 设置配置
 * Created by zzs on 2018/11/26
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SystemSetting {

    /**
     * 测试名称
     */
    private String testName;
    /**
     * 测试地点
     */
    private String testSite;
    /**
     * 服务器Ip
     */
    private String serverIp;
    /**
     * 主机号
     */
    private int hostId = 1;
    /**
     * 成绩播报
     */
    private boolean isAutoBroadcast = false;
    /**
     * 自动打印
     */
    private boolean isAutoPrint = false;
    /**
     * 实时上传
     */
    private boolean isRtUpload = false;
    /**
     * 临时新增考生
     */
    private boolean isTemporaryAddStu = false;

    /**
     * 条码长度
     */
    private int qrLength;
    /**
     * 检录工具
     * 0　条形码/二维码（默认）
     * 1　身份证
     * 2　IC卡
     * 3  外接扫描枪
     * 4　人脸识别（暂无）后续增加
     * 5　指纹（暂无）后续增加
     */
    private int checkTool = 0;

    /**
     * 登录用户名
     */
    private String userName;
    /**
     * 是否自由测试
     */
    private boolean isFreedomTest;
    /**
     * 单屏0 多屏1
     */
    private int ledMode = 0;
    private int ledVersion = 0;

    //信道
    private int channel;
    //是否使用自定义信道
    private boolean isCustomChannel;

    public boolean isFreedomTest() {
        return isFreedomTest;
    }

    public void setFreedomTest(boolean freedomTest) {
        isFreedomTest = freedomTest;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestSite() {
        return testSite;
    }

    public void setTestSite(String testSite) {
        this.testSite = testSite;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public boolean isAutoBroadcast() {
        return isAutoBroadcast;
    }

    public void setAutoBroadcast(boolean autoBroadcast) {
        isAutoBroadcast = autoBroadcast;
    }

    public boolean isAutoPrint() {
        return isAutoPrint;
    }

    public void setAutoPrint(boolean autoPrint) {
        isAutoPrint = autoPrint;
    }

    public boolean isRtUpload() {
        return isRtUpload;
    }

    public void setRtUpload(boolean rtUpload) {
        isRtUpload = rtUpload;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isTemporaryAddStu() {
        return isTemporaryAddStu;
    }

    public void setTemporaryAddStu(boolean temporaryAddStu) {
        isTemporaryAddStu = temporaryAddStu;
    }

    public int getQrLength() {
        return qrLength;
    }

    public void setQrLength(int qrLength) {
        this.qrLength = qrLength;
    }

    public int getCheckTool() {
        return checkTool;
    }

    public void setCheckTool(int checkTool) {
        this.checkTool = checkTool;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public boolean isCustomChannel() {
        return isCustomChannel;
    }

    public void setCustomChannel(boolean customChannel) {
        isCustomChannel = customChannel;
    }

    /**
     * 检录工具
     */
    public static final int CHECK_TOOL_QR = 0;
    public static final int CHECK_TOOL_IDCARD = 1;
    public static final int CHECK_TOOL_ICCARD = 2;

    @Override
    public String toString() {
        return "SystemSetting{" +
                "testName='" + testName + '\'' +
                ", testSite='" + testSite + '\'' +
                ", serverIp='" + serverIp + '\'' +
                ", hostId=" + hostId +
                ", isAutoBroadcast=" + isAutoBroadcast +
                ", isAutoPrint=" + isAutoPrint +
                ", isRtUpload=" + isRtUpload +
                ", isTemporaryAddStu=" + isTemporaryAddStu +
                ", qrLength=" + qrLength +
                ", checkTool=" + checkTool +
                ", userName='" + userName + '\'' +
                '}';
    }

    public int getLedMode() {
        return ledMode;
    }

    public void setLedMode(int ledMode) {
        this.ledMode = ledMode;
    }

    public int getLedVersion() {
        return ledVersion;
    }

    public void setLedVersion(int ledVersion) {
        this.ledVersion = ledVersion;
    }

    /***
     * 使用信道
     * @return
     */
    public int getUseChannel() {
        return isCustomChannel ? channel : SerialConfigs.sProChannels.get(TestConfigs.sCurrentItem.getMachineCode()) + hostId - 1;
    }
}
