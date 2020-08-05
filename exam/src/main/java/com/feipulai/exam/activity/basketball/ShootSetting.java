package com.feipulai.exam.activity.basketball;

import com.feipulai.exam.config.TestConfigs;

/**
 * Created by pengjf on 2020/4/1.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ShootSetting {
    private int testNo = 1;// 允许测试的次数
    /**
     * 满分跳过
     */
    private boolean fullSkip = false;
    private int maleFullShoot;//男子投篮
    private int femaleFullShoot;//女子投篮
    private int maleFullDribble;//男子运球
    private int interceptNo = 4;
    private int startNo = 2;
    private int back1No = 3;
    private int back2No = 4;
    public int getStartNo() {
        return startNo;
    }

    public void setStartNo(int startNo) {
        this.startNo = startNo;
    }

    public int getBack1No() {
        return back1No;
    }

    public void setBack1No(int back1No) {
        this.back1No = back1No;
    }

    public int getBack2No() {
        return back2No;
    }

    public void setBack2No(int back2No) {
        this.back2No = back2No;
    }
    public int getTestNo() {
        return testNo;
    }

    public void setTestNo(int testNo) {
        this.testNo = testNo;
    }

    public boolean isFullSkip() {
        return fullSkip;
    }

    public void setFullSkip(boolean fullSkip) {
        this.fullSkip = fullSkip;
    }

    public int getMaleFullShoot() {
        return maleFullShoot;
    }

    public void setMaleFullShoot(int maleFullShoot) {
        this.maleFullShoot = maleFullShoot;
    }

    public int getFemaleFullShoot() {
        return femaleFullShoot;
    }

    public void setFemaleFullShoot(int femaleFullShoot) {
        this.femaleFullShoot = femaleFullShoot;
    }

    public int getMaleFullDribble() {
        return maleFullDribble;
    }

    public void setMaleFullDribble(int maleFullDribble) {
        this.maleFullDribble = maleFullDribble;
    }

    public int getFemaleFullDribble() {
        return femaleFullDribble;
    }

    public void setFemaleFullDribble(int femaleFullDribble) {
        this.femaleFullDribble = femaleFullDribble;
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

    private int femaleFullDribble;//女子运球
    /**
     * 分组测试模式 0 连续 1 循环
     */
    private int testPattern = TestConfigs.GROUP_PATTERN_SUCCESIVE;
    private int testType = 2;

    public int getInterceptNo() {
        return interceptNo;
    }

    public void setInterceptNo(int interceptNo) {
        this.interceptNo = interceptNo;
    }
}
