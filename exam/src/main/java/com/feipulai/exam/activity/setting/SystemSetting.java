package com.feipulai.exam.activity.setting;

import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.exam.config.TestConfigs;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置配置
 * Created by zzs on 2018/11/26
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SystemSetting {

    /**
     * 测试名称
     */
    private String testName = "";
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
     * 测试模式 0 个人 1 分组 默认 0
     */
    private int testPattern = 0;
    /**
     * 是否进行身份验证
     */
    private boolean isIdentityMark = false;
    /**
     * 重测
     */
    private boolean isItemResurvey = false;

    /**
     * 临时新增考生
     */
    private boolean isTemporaryAddStu = false;
    /**
     * 登录用户名
     */
    private String userName;

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
     * 网络是否添加路由表
     */
    private boolean isAddRoute = false;
    /**
     * 单屏0 多屏1
     */
    private int ledMode = 1;

    private int ledVersion = 0;//0:1.0版本 1：V4.1 以上版本
    public static final int LED_VERSION_V1 = 0;
    public static final int LED_VERSION_V4 = 1;

    //信道
    private int channel;
    //是否使用自定义信道
    private boolean isCustomChannel;

    private boolean isBindMonitoring;//是否绑定监控设备
    private List<MonitoringBean> monitoringList = new ArrayList<>();

    public int getQrLength() {
        return qrLength;
    }

    public void setQrLength(int qrLength) {
        this.qrLength = qrLength;
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

    public int getTestPattern() {
        return testPattern;
    }

    public void setTestPattern(int testPattern) {
        this.testPattern = testPattern;
    }

    public boolean isItemResurvey() {
        return isItemResurvey;
    }

    public void setItemResurvey(boolean itemResurvey) {
        isItemResurvey = itemResurvey;
    }

    public boolean isIdentityMark() {
        return isIdentityMark;
    }

    public void setIdentityMark(boolean identityMark) {
        isIdentityMark = identityMark;
    }

    public boolean isTemporaryAddStu() {
        return isTemporaryAddStu;
    }

    public void setTemporaryAddStu(boolean temporaryAddStu) {
        isTemporaryAddStu = temporaryAddStu;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getCheckTool() {
        return checkTool;
    }

    public void setCheckTool(int checkTool) {
        this.checkTool = checkTool;
    }

    public int getLedVersion() {
        return ledVersion;
    }

    public void setLedVersion(int ledVersion) {
        this.ledVersion = ledVersion;
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

    public boolean isBindMonitoring() {
        return isBindMonitoring;
    }

    public void setBindMonitoring(boolean bindMonitoring) {
        isBindMonitoring = bindMonitoring;
    }

    public List<MonitoringBean> getMonitoringList() {
        return monitoringList;
    }

    public void setMonitoringList(List<MonitoringBean> monitoringList) {
        this.monitoringList = monitoringList;
    }

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
                ", testPattern=" + testPattern +
                ", isIdentityMark=" + isIdentityMark +
                ", isItemResurvey=" + isItemResurvey +
                ", isTemporaryAddStu=" + isTemporaryAddStu +
                '}';
    }

    public boolean isAddRoute() {
        return isAddRoute;
    }

    public void setAddRoute(boolean addRoute) {
        isAddRoute = addRoute;
    }

    /**
     * 测试模式 0 个人 1 分组 默认 0
     */
    public static final int PERSON_PATTERN = 0;
    public static final int GROUP_PATTERN = 1;

    /**
     * 检录工具
     */
    public static final int CHECK_TOOL_QR = 0;
    public static final int CHECK_TOOL_IDCARD = 1;
    public static final int CHECK_TOOL_ICCARD = 2;

    public int getLedMode() {
        return ledMode;
    }

    public void setLedMode(int ledMode) {
        this.ledMode = ledMode;
    }

    /***
     * 使用信道
     * @return
     */
    public int getUseChannel() {
        if (TestConfigs.sCurrentItem != null) {
            return isCustomChannel ? channel : SerialConfigs.sProChannels.get(TestConfigs.sCurrentItem.getMachineCode()) + hostId - 1;
        } else {
            return 0;
        }

    }

    public String getBindDeviceName() {
        if (!isBindMonitoring) {
            return "";
        }
        String bindName = "";
        for (int i = 0; i < monitoringList.size(); i++) {
            bindName += monitoringList.get(i).getMonitoringSerial();
            if (i != monitoringList.size() - 1) {
                bindName += ",";
            }
        }
        return bindName;
    }
}
