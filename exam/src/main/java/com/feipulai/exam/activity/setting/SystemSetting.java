package com.feipulai.exam.activity.setting;

import android.text.TextUtils;

import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.config.TestConfigs;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
    private String serverIp = "192.168.0.100:7979";

    /**
     * tcp ip
     */
    private String tcpIp;

    private boolean isTCP = false;
    private boolean isTCPSimultaneous = false;
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
     * 打印工具 分组模式可以选择
     * 0 热敏打印
     * 1 A4打印(HP)
     * 2 打开pdf文件选择打印机APP打印
     */
    private int printTool = 0;
    /**
     * 网络是否添加路由表
     */
    private boolean isAddRoute = false;
    /**
     * 单屏0 多屏1
     */
    private int ledMode = 1;

    private int ledVersion = 0;//1:1.0版本 0：V4.1 以上版本  2：4.8版本
    public static final int LED_VERSION_V1 = 0;
    public static final int LED_VERSION_V4 = 1;
    public static final int LED_VERSION_V8 = 2;
    private int radioLed;//用于区分红外计时版本0带盒子版，1不带盒子
    //信道
    private int channel = 1;
    //是否使用自定义信道
    private boolean isCustomChannel;

    private boolean isBindMonitoring;//是否绑定监控设备
    private String monitoringJson;
    //是否启用体温计
    private boolean isStartThermometer = false;
    //单点测试
    private boolean autoScore = false;
    //考点ID
    private String sitCode;

    //在线识别
    private boolean netCheckTool;
    private boolean autoDiscern;

    //手动输入成绩测试
    private boolean isInputTest;

    /**
     * 0 60
     * 1 70
     * 2 80
     * 3 90
     */
    private int afrContrast = 3;
    private boolean againTest;  //现场重测
    private boolean isResit;  //现场补考
    private String againPass = MyApplication.ADVANCED_PWD;
    private String resitPass = MyApplication.ADVANCED_PWD;
    private boolean resitPassBool;//补考密码开关
    private boolean againPassBool;//重测密码开关
    private boolean resultConfirm;  //成绩确认
    private boolean isGroupCheck;//分组检入
    private boolean isStuConfirm;//考生成绩确定（确认考生所有轮次，未测成绩添加放弃）
    private int ledColor; //0 1 红 2绿 3 蓝 满分
    private int ledColor2;//0  1红 2绿 3 蓝 未满分

    public static final int LED_SHOW_COLOR_RED = 1;
    public static final int LED_SHOW_COLOR_GREEN = 2;
    public static final int LED_SHOW_COLOR_YELLOW = 3;

    public boolean isStuConfirm() {
        return isStuConfirm;
    }

    public void setStuConfirm(boolean stuConfirm) {
        isStuConfirm = stuConfirm;
    }

    public int getAfrContrast() {
        return afrContrast;
    }

    public void setAfrContrast(int afrContrast) {
        this.afrContrast = afrContrast;
    }

    public boolean isInputTest() {
        return isInputTest;
    }

    public void setInputTest(boolean inputTest) {
        isInputTest = inputTest;
    }

    public boolean isNetCheckTool() {
        return netCheckTool;
    }

    public void setNetCheckTool(boolean netCheckTool) {
        this.netCheckTool = netCheckTool;
    }

    public int getPrintTool() {
        return printTool;
    }

    public void setPrintTool(int printTool) {
        this.printTool = printTool;
    }

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

    public String getTcpIp() {
        return tcpIp;
    }

    public void setTcpIp(String tcpIp) {
        this.tcpIp = tcpIp;
    }

    public void setTCP(boolean TCP) {
        isTCP = TCP;
    }

    public boolean isTCP() {
        return isTCP;
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

    public String getMonitoringJson() {
        return monitoringJson;
    }

    public void setMonitoringJson(String monitoringJson) {
        this.monitoringJson = monitoringJson;
    }

    public boolean isStartThermometer() {
        return isStartThermometer;
    }

    public void setStartThermometer(boolean startThermometer) {
        isStartThermometer = startThermometer;
    }

    public boolean isTCPSimultaneous() {
        return isTCPSimultaneous;
    }

    public void setTCPSimultaneous(boolean TCPSimultaneous) {
        isTCPSimultaneous = TCPSimultaneous;
    }

    public List<MonitoringBean> getMonitoringList() {
        List<MonitoringBean> list;
        if (TextUtils.isEmpty(monitoringJson)) {
            list = new ArrayList<>();
            return list;
        }
        list = new Gson().fromJson(monitoringJson, new TypeToken<List<MonitoringBean>>() {
        }.getType());
        return list;
    }

    @Override
    public String toString() {
        return "SystemSetting{" +
                "testName='" + testName + '\'' +
                ", testSite='" + testSite + '\'' +
                ", serverIp='" + serverIp + '\'' +
                ", tcpIp='" + tcpIp + '\'' +
                ", hostId=" + hostId +
                ", isTCP='" + isTCP +
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

    public static final int PRINT_A4 = 1;
    public static final int PRINT_CUSTOM_APP = 2;
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
        List<MonitoringBean> monitoringList = getMonitoringList();
        if (!isBindMonitoring || monitoringList == null) {
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

    public boolean isAutoScore() {
        return autoScore;
    }

    public void setAutoScore(boolean autoScore) {
        this.autoScore = autoScore;
    }

    public void setSitCode(String siteCode) {
        this.sitCode = siteCode;
    }

    public String getSitCode() {
        return sitCode;
    }

    public void setAutoDiscern(boolean autoDiscern) {
        this.autoDiscern = autoDiscern;
    }

    public boolean getAutoDiscern() {
        return autoDiscern;
    }

    public int getRadioLed() {
        return radioLed;
    }

    public void setRadioLed(int radioLed) {
        this.radioLed = radioLed;
    }

    public void setAgainTest(boolean againTest) {
        this.againTest = againTest;
    }

    public void setIsResit(boolean isResit) {
        this.isResit = isResit;
    }

    public boolean isResit() {
        return isResit;
    }

    public boolean isAgainTest() {
        return againTest;
    }

    public void setAgainPass(String againPass) {
        this.againPass = againPass;
    }

    public String getAgainPass() {
        return againPass;
    }

    public void setResitPass(String resitPass) {
        this.resitPass = resitPass;
    }

    public String getResitPass() {
        return resitPass;
    }

    public void setResitPassBool(boolean resitPassBool) {
        this.resitPassBool = resitPassBool;
    }

    public boolean getResitPassBool() {
        return resitPassBool;
    }

    public void setAgainPassBool(boolean againPassBool) {
        this.againPassBool = againPassBool;
    }

    public boolean getAgainPassBool() {
        return againPassBool;
    }

    public boolean isResultConfirm() {
        return resultConfirm;
    }

    public void setResultConfirm(boolean resultConfirm) {
        this.resultConfirm = resultConfirm;
    }

    public boolean isGroupCheck() {
        return isGroupCheck;
    }

    public void setGroupCheck(boolean groupCheck) {
        this.isGroupCheck = groupCheck;
    }

    public void setLedColor(int ledColor) {
        this.ledColor = ledColor;
    }

    public int getLedColor() {
        return ledColor + 1;
    }

    public int getLedColor2() {
        return ledColor2 + 1;
    }

    public void setLedColor2(int ledColor2) {
        this.ledColor2 = ledColor2;
    }
}
