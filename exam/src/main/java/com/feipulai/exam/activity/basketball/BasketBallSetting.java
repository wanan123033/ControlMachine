package com.feipulai.exam.activity.basketball;

/**
 * Created by zzs on  2019/6/4
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BasketBallSetting {
    private int testNo = 1;// 允许测试的次数
    private int interceptSecond = 5;//默认5秒
    private int sensitivity = 15;//灵敏度
    private String hostIp = "192.168.0.227";//计时仪IP
    private int post = 1026;//端口
    private int penaltySecond;//违例罚秒
    private int resultAccuracy = 1;//0 十分位  1 百分位
    private int carryMode = 0;//进位方式 对应项目进位（0.不去舍，1.四舍五入 2.舍位 3.非零进取），只使用0.不去舍，1.四舍五入 3.非零进取
    /**
     * 满分跳过
     */
    private boolean fullSkip = false;
    private int maleFullScore;//男子
    private int femaleFullScore;//女子
    /**
     * 分组测试模式 0 连续 1 循环
     */
    private int testPattern = 0;

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

    public boolean isFullSkip() {
        return fullSkip;
    }

    public void setFullSkip(boolean fullSkip) {
        this.fullSkip = fullSkip;
    }

    public int getMaleFullScore() {
        return maleFullScore;
    }

    public void setMaleFullScore(int maleFullScore) {
        this.maleFullScore = maleFullScore;
    }

    public int getFemaleFullScore() {
        return femaleFullScore;
    }

    public void setFemaleFullScore(int femaleFullScore) {
        this.femaleFullScore = femaleFullScore;
    }

    public int getTestPattern() {
        return testPattern;
    }

    public void setTestPattern(int testPattern) {
        this.testPattern = testPattern;
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
                ", resultAccuracy=" + resultAccuracy +
                ", carryMode=" + carryMode +
                ", fullSkip=" + fullSkip +
                ", maleFullScore=" + maleFullScore +
                ", femaleFullScore=" + femaleFullScore +
                ", testPattern=" + testPattern +
                '}';
    }
}