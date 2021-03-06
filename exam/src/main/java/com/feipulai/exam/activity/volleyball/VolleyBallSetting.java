package com.feipulai.exam.activity.volleyball;

import com.feipulai.exam.config.TestConfigs;

/**
 * Created by James on 2019/1/17 0017.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class VolleyBallSetting {
    public static final int ANTIAIRCRAFT_POLE = 3;
    public static final int WALL_POLE = 2;
    public static final int NO_TIME_LIMIT = 0;
    private int testTime = NO_TIME_LIMIT;// 一轮测试的时间,单位为秒
    private int testNo = 1;// 允许测试的次数
    private int groupMode = TestConfigs.GROUP_PATTERN_SUCCESIVE;
    private boolean fullSkip = false;   //是否开启满分跳过功能
    private int maleFullScore;       //男子满分值
    private int femaleFullScore;     //女子满分值
    private boolean isPenalize;     //是否判罚

    private int testPattern;//0对空 1对墙
    private int type;//0有线1无线一对一 2无线一对多
    //最多设备数量
    private int spDeviceCount = 4;
    private boolean autoPair;
    private int pairNum;

    public int getTestPattern() {
        return testPattern;
    }

    public void setTestPattern(int testPattern) {
        this.testPattern = testPattern;
    }

    public int getTestTime() {
        return testTime;
    }

    public void setTestTime(int testTime) {
        this.testTime = testTime;
    }

    public int getTestNo() {
        return testNo;
    }

    public void setTestNo(int testNo) {
        this.testNo = testNo;
    }

    public int getGroupMode() {
        return groupMode;
    }

    public void setGroupMode(int groupMode) {
        this.groupMode = groupMode;
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

    public boolean isPenalize() {
        return isPenalize;
    }

    public void setPenalize(boolean penalize) {
        isPenalize = penalize;
    }

    @Override
    public String toString() {
        return "VolleyBallSetting{" +
                "testTime=" + testTime +
                ", testNo=" + testNo +
                ", groupMode=" + groupMode +
                ", fullSkip=" + fullSkip +
                ", maleFullScore=" + maleFullScore +
                ", femaleFullScore=" + femaleFullScore +
                ", isPenalize=" + isPenalize +
                '}';
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSpDeviceCount() {
        return spDeviceCount;
    }


    public void setAutoPair(boolean autoPair) {
        this.autoPair = autoPair;
    }

    public boolean isAutoPair() {
        return autoPair;
    }

    public void setPairNum(int pairNum) {
        this.pairNum = pairNum;
    }

    public int getPairNum() {
        return pairNum;
    }
}
