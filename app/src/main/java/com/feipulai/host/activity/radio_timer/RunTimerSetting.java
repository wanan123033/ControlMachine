package com.feipulai.host.activity.radio_timer;

/**
 * Created by pengjf on 2018/12/3.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class RunTimerSetting {
    /**
     * 满分跳过
     */
    private boolean isFullReturn = false;
    /**
     * 精确度 百分位 /十分位
     */
    private int digital = 2;
    /**
     * 拦截点 起始点1 /终点2 两者都选3
     */
    private int interceptPoint = 2;
    /**
     * 循环模式 连续测试 循环测试
     */
    private boolean testModel =true;
    private int startPoint;
    private int endPoint =2;
    /**
     * 测试次数
     */
    private int testTimes = 1;
    /**
     * 成绩进位方式 不进位 四舍五入 非零进位
     */
    private int markDegree = 3;
    /**
     * 拦截方式 红外拦截 传感器拦截
     */
    private int interceptWay = 0;
    /**
     * 计时方式 统一计时 false 独立计时 true 默认统一
     */
    private boolean timer_select;
    /**
     *传感器信道

     */
    private int sensor = 0;
    /**
     * 跑道数量
     */
    private String runNum = "4";
    private int sensitivityNum = 5;
    /**
     * 连接方式 0 有盒子 1无盒子
     */
    private int connectType;
    private boolean autoPair;
    /**
     * 男子满分
     */
    private String maleFull ;
    /**
     * 女子满分
     */
    private String femaleFull ;
    public String getMaleFull() {
        return maleFull;
    }

    public void setMaleFull(String maleFull) {
        this.maleFull = maleFull;
    }

    public String getFemaleFull() {
        return femaleFull;
    }

    public void setFemaleFull(String femaleFull) {
        this.femaleFull = femaleFull;
    }

    public int getTestTimes() {
        return testTimes;
    }

    public void setTestTimes(int testTimes) {
        this.testTimes = testTimes;
    }

    public int getMarkDegree() {
        return markDegree;
    }

    public void setMarkDegree(int markDegree) {
        this.markDegree = markDegree;
    }

    public int getInterceptWay() {
        return interceptWay;
    }

    public void setInterceptWay(int interceptWay) {
        this.interceptWay = interceptWay;
    }

    public int getSensor() {
        return sensor;
    }

    public void setSensor(int sensor) {
        this.sensor = sensor;
    }


    public boolean isFullReturn() {
        return isFullReturn;
    }

    public void setFullReturn(boolean fullReturn) {
        isFullReturn = fullReturn;
    }

    public int getDigital() {
        return digital;
    }

    public void setDigital(int digital) {
        this.digital = digital;
    }

    public int getInterceptPoint() {
        return interceptPoint;
    }

    public void setInterceptPoint(int interceptPoint) {
        this.interceptPoint = interceptPoint;
    }

    public boolean isTestModel() {
        return testModel;
    }

    public void setTestModel(boolean testModel) {
        this.testModel = testModel;
    }

    public String getRunNum() {
        return runNum;
    }

    public void setRunNum(String runNum) {
        this.runNum = runNum;
    }

    @Override
    public String toString() {
        return "SportTimerSetting{" +
                "isFullReturn=" + isFullReturn +
                ", digital=" + digital +
                ", interceptPoint=" + interceptPoint +
                ", testModel=" + testModel +
                ", testTimes=" + testTimes +
                ", markDegree=" + markDegree +
                ", interceptWay=" + interceptWay +
                ", sensor=" + sensor +
                ", runNum='" + runNum + '\'' +
                ", maleFull='" + maleFull + '\'' +
                ", femaleFull='" + femaleFull + '\'' +
                '}';
    }

    public int getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(int startPoint) {
        this.startPoint = startPoint;
    }

    public int getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(int endPoint) {
        this.endPoint = endPoint;
    }

    public int getSensitivityNum() {
        return sensitivityNum;
    }

    public void setSensitivityNum(int sensitivityNum) {
        this.sensitivityNum = sensitivityNum;
    }

    public int getConnectType() {
        return connectType;
    }

    public void setConnectType(int connectType) {
        this.connectType = connectType;
    }

    public boolean isAutoPair() {
        return autoPair;
    }

    public void setAutoPair(boolean autoPair) {
        this.autoPair = autoPair;
    }

    public boolean isTimer_select() {
        return timer_select;
    }

    public void setTimer_select(boolean timer_select) {
        this.timer_select = timer_select;
    }
}
