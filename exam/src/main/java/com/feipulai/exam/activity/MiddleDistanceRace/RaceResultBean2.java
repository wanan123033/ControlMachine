package com.feipulai.exam.activity.MiddleDistanceRace;

import com.feipulai.exam.entity.Student;

import java.util.Arrays;

/**
 * created by ww on 2019/6/24.
 */
public class RaceResultBean2 {
    private String no;//组的序号（非组号，与数据库无关）
    private int cycle;//圈数
    private long startTime;//开始时间
    private int vestNo;//背心号
    private String itemCode;//
    private String[] results;//{道次,姓名,最终成绩,第一圈,第二圈,第三圈,第四圈...}
    private int color;//颜色
    private String studentName;
    private String studentCode;

    public RaceResultBean2() {
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

    @Override
    public String toString() {
        return "RaceResultBean2{" +
                "no='" + no + '\'' +
                ", cycle=" + cycle +
                ", startTime=" + startTime +
                ", vestNo=" + vestNo +
                ", itemCode='" + itemCode + '\'' +
                ", results=" + Arrays.toString(results) +
                ", color=" + color +
                ", studentName='" + studentName + '\'' +
                ", studentCode='" + studentCode + '\'' +
                '}';
    }
}
