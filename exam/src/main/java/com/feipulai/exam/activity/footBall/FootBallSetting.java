package com.feipulai.exam.activity.footBall;

import com.feipulai.exam.config.TestConfigs;

/**
 * Created by pengjf on 2019/6/26.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class FootBallSetting {
    private boolean autoPair = true;

    private int testNo = 1;// 允许测试的次数
    private int interceptSecond = 2;//默认5秒
    private int sensitivity = 20;//灵敏度
    private String hostIp = "192.168.0.227";//计时仪IP
    private int post = 1026;//端口
    private double penaltySecond;//违例罚秒
    private int resultAccuracy = 1;//0 十分秒  1 百分秒
    private int carryMode = 1;//进位方式 对应项目进位（1.四舍五入 2.舍位 3.非零进取）
    private int useMode = 0;
    /**
     * 满分跳过
     */
    private boolean fullSkip = false;
    private double maleFullScore;//男子
    private double femaleFullScore;//女子
    /**
     * 分组测试模式 0 连续 1 循环
     */
    private int testPattern = 0;
    private int testType = 0;   //0 有线  1 无线 3 红外
    private int deviceVersion = 0;//0:6.4 1无线6.6版本
    private int useLedType = 0;//使用LED類型 0 標配 1 通用
    private double autoPenaltySecond;//自动罚秒
    private boolean autoPenalt = false;
    private int lightTime;//运行计时拦截器灯亮时长

    public int getLightTime() {
        return lightTime;
    }

    public void setLightTime(int lightTime) {
        this.lightTime = lightTime;
    }
    public int getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(int deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public int getTestNo() {
        return testNo;
    }

    public void setTestNo(int testNo) {
        this.testNo = testNo;
    }

    public int getInterceptSecond() {
        return interceptSecond;
    }

    public void setInterceptSecond(int interceptSecond) {
        this.interceptSecond = interceptSecond;
    }

    public int getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(int sensitivity) {
        this.sensitivity = sensitivity;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public int getPost() {
        return post;
    }

    public void setPost(int post) {
        this.post = post;
    }

    public double getPenaltySecond() {
        return penaltySecond;
    }

    public void setPenaltySecond(double penaltySecond) {
        this.penaltySecond = penaltySecond;
    }

    public int getResultAccuracy() {
        return resultAccuracy;
    }

    public void setResultAccuracy(int resultAccuracy) {
        this.resultAccuracy = resultAccuracy;
    }

    public int getCarryMode() {
        return carryMode;
    }

    public void setCarryMode(int carryMode) {
        this.carryMode = carryMode;
    }

    public int getUseMode() {
        return useMode;
    }

    public void setUseMode(int useMode) {
        this.useMode = useMode;
    }

    public boolean isFullSkip() {
        return fullSkip;
    }

    public void setFullSkip(boolean fullSkip) {
        this.fullSkip = fullSkip;
    }

    public double getMaleFullScore() {
        return maleFullScore;
    }

    public void setMaleFullScore(double maleFullScore) {
        this.maleFullScore = maleFullScore;
    }

    public double getFemaleFullScore() {
        return femaleFullScore;
    }

    public void setFemaleFullScore(double femaleFullScore) {
        this.femaleFullScore = femaleFullScore;
    }

    public int getTestPattern() {
        return testPattern;
    }

    public void setTestPattern(int testPattern) {
        this.testPattern = testPattern;
    }

    public void setTestType(int testType) {
        this.testType = testType;
    }

    public int getTestType() {
        return testType;
    }

    public boolean isAutoPair() {
        return autoPair;
    }

    public void setAutoPair(boolean autoPair) {
        this.autoPair = autoPair;
    }

    public int getUseLedType() {
        return useLedType;
    }

    public void setUseLedType(int useLedType) {
        this.useLedType = useLedType;
    }

    public double getAutoPenaltySecond() {
        return autoPenaltySecond;
    }

    public void setAutoPenaltySecond(double autoPenaltySecond) {
        this.autoPenaltySecond = autoPenaltySecond;
    }

    public int getAutoPenaltyTime() {
        if (autoPenalt) {
            return (int) autoPenaltySecond * 1000;
        } else {
            return 0;
        }
    }

    public boolean isAutoPenalt() {
        return autoPenalt;
    }

    public void setAutoPenalt(boolean autoPenalt) {
        this.autoPenalt = autoPenalt;
    }

    public int getDeviceCount() {
        return useLedType == 0 ? 3 : 2;
    }

    public int getAccuracy() {
        switch (TestConfigs.sCurrentItem.getDigital()) {
            case 1:
            case 2:
            case 3:
                return TestConfigs.sCurrentItem.getDigital();
            default:
                return 2;
        }
    }
}
