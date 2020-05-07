package com.feipulai.exam.activity.basketball;

import com.feipulai.exam.config.TestConfigs;

/**
 * Created by zzs on  2019/6/4
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BasketBallSetting {
    public static final int WIRED_TYPE = 0;
    public static final int WIRELESS_TYPE = 1;
    private boolean autoPair = true;
    private int testNo = 1;// 允许测试的次数
    private int interceptSecond = 5;//默认5秒
    private int sensitivity = 15;//灵敏度
    private String hostIp = "192.168.0.227";//计时仪IP
    private int post = 1026;//端口
    private int penaltySecond;//违例罚秒
    /**
     * 满分跳过
     */
    private boolean fullSkip = false;
    private double maleFullScore;//男子
    private double femaleFullScore;//女子
    /**
     * 分组测试模式 0 连续 1 循环
     */
    private int testPattern = TestConfigs.GROUP_PATTERN_SUCCESIVE;
    private int testType = 0;

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

    public int getPenaltySecond() {
        return penaltySecond;
    }

    public void setPenaltySecond(int penaltySecond) {
        this.penaltySecond = penaltySecond;
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

    public int getTestType() {
        return testType;
    }

    public void setTestType(int testType) {
        this.testType = testType;
    }

    public boolean isAutoPair() {
        return autoPair;
    }

    public void setAutoPair(boolean autoPair) {
        this.autoPair = autoPair;
    }

    @Override
    public String toString() {
        return "BasketBallSetting{" +
                "testNo=" + testNo +
                ", interceptSecond=" + interceptSecond +
                ", sensitivity=" + sensitivity +
                ", hostIp='" + hostIp + '\'' +
                ", post=" + post +
                ", penaltySecond=" + penaltySecond +
                ", fullSkip=" + fullSkip +
                ", maleFullScore=" + maleFullScore +
                ", femaleFullScore=" + femaleFullScore +
                ", testPattern=" + testPattern +
                '}';
    }
}
