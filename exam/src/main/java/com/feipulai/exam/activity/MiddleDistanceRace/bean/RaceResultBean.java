package com.feipulai.exam.activity.MiddleDistanceRace.bean;

import java.util.Arrays;

/**
 * created by ww on 2019/6/24.
 */
public class RaceResultBean {
    private String no;//组号
    private int cycle;//圈数
    private long startTime;//开始时间
    private int vestNo;//背心号
    private String itemCode;//
    private String itemName;//
    private String[] results;//不包含标题栏，{道次,姓名,最终成绩,第一圈,第二圈,第三圈,第四圈...}---成绩1ms值（存储数据用）
    private int color;//颜色
    private int resultState;
    private String studentName;
    private String studentCode;
    //0:未检录 1:正常 2:犯规 3:中退 4:弃权 5:测试
    public static int STATE_NORMAL=1;
    public static int STATE_DQ=2;
    public static int STATE_DNF=3;
    public static int STATE_DNS=4;
    public static int STATE_DT=5;
    private boolean isSelect;

    public RaceResultBean() {
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getVestNo() {
        return vestNo;
    }

    public void setVestNo(int vestNo) {
        this.vestNo = vestNo;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public int getCycle() {
        return cycle;
    }

    public void setCycle(int cycle) {
        this.cycle = cycle;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String[] getResults() {
        return results;
    }

    public void setResults(String[] results) {
        this.results = results;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getResultState() {
        return resultState;
    }

    public void setResultState(int resultState) {
        this.resultState = resultState;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    @Override
    public String toString() {
        return "RaceResultBean{" +
                "no='" + no + '\'' +
                ", cycle=" + cycle +
                ", startTime=" + startTime +
                ", vestNo=" + vestNo +
                ", itemCode='" + itemCode + '\'' +
                ", itemName='" + itemName + '\'' +
                ", results=" + Arrays.toString(results) +
                ", color=" + color +
                ", resultState=" + resultState +
                ", studentName='" + studentName + '\'' +
                ", studentCode='" + studentCode + '\'' +
                ", isSelect=" + isSelect +
                '}';
    }
}
