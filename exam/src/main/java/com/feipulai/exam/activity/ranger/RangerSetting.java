package com.feipulai.exam.activity.ranger;

public class RangerSetting {
    private int testNo = 1; //测试次数
    private int accuracy = 2; //成绩精确度  2:小数点后两位 3:小数点后三位
    private boolean penglize; //是否启用板距
    private int autoTestTime; //自动测试时间间隔
    private int itemType; //项目类型  {"跳高","撑竿跳高","跳远","立定跳远","三级跳远","标枪","铅球","铁饼","链球"};

    //投掷设置
    private int radius; //投掷弧半径
    private int qd_hor; //起点水平距离
    private int du;     //度
    private int fen;    //分
    private int miao;   //秒

    //跳远设置
    private int qd1_hor; //起点1水平距离
    private int du1;     //度
    private int fen1;    //分
    private int miao1;   //秒

    private int qd2_hor; //起点2水平距离
    private int du2;     //度
    private int fen2;    //分
    private int miao2;   //秒
    private int distance; //两点之间的距离
    private int testPattern; //分组测试模式 0连续 1循环

    private String bluetoothName;
    private String bluetoothMac;

    public int getTestNo() {
        return testNo;
    }

    public void setTestNo(int testNo) {
        this.testNo = testNo;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public boolean isPenglize() {
        return penglize;
    }

    public void setPenglize(boolean penglize) {
        this.penglize = penglize;
    }

    public int getAutoTestTime() {
        return autoTestTime;
    }

    public void setAutoTestTime(int autoTestTime) {
        this.autoTestTime = autoTestTime;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getQd_hor() {
        return qd_hor;
    }

    public void setQd_hor(int qd_hor) {
        this.qd_hor = qd_hor;
    }

    public int getDu() {
        return du;
    }

    public void setDu(int du) {
        this.du = du;
    }

    public int getFen() {
        return fen;
    }

    public void setFen(int fen) {
        this.fen = fen;
    }

    public int getMiao() {
        return miao;
    }

    public void setMiao(int miao) {
        this.miao = miao;
    }

    public int getQd1_hor() {
        return qd1_hor;
    }

    public void setQd1_hor(int qd1_hor) {
        this.qd1_hor = qd1_hor;
    }

    public int getDu1() {
        return du1;
    }

    public void setDu1(int du1) {
        this.du1 = du1;
    }

    public int getFen1() {
        return fen1;
    }

    public void setFen1(int fen1) {
        this.fen1 = fen1;
    }

    public int getMiao1() {
        return miao1;
    }

    public void setMiao1(int miao1) {
        this.miao1 = miao1;
    }

    public int getQd2_hor() {
        return qd2_hor;
    }

    public void setQd2_hor(int qd2_hor) {
        this.qd2_hor = qd2_hor;
    }

    public int getDu2() {
        return du2;
    }

    public void setDu2(int du2) {
        this.du2 = du2;
    }

    public int getFen2() {
        return fen2;
    }

    public void setFen2(int fen2) {
        this.fen2 = fen2;
    }

    public int getMiao2() {
        return miao2;
    }

    public void setMiao2(int miao2) {
        this.miao2 = miao2;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }


    public int getTestPattern() {
        return testPattern;
    }
    public void setTestPattern(int testPattern) {
        this.testPattern = testPattern;
    }

    public String getBluetoothName() {
        return bluetoothName;
    }

    public void setBluetoothName(String bluetoothName) {
        this.bluetoothName = bluetoothName;
    }

    public String getBluetoothMac() {
        return bluetoothMac;
    }

    public void setBluetoothMac(String bluetoothMac) {
        this.bluetoothMac = bluetoothMac;
    }
}
