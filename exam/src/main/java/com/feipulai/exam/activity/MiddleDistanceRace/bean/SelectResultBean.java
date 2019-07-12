package com.feipulai.exam.activity.MiddleDistanceRace.bean;

import com.feipulai.exam.entity.RoundResult;

import java.util.Arrays;

/**
 * created by ww on 2019/6/24.
 */
public class SelectResultBean {

    private String studentName;
    private String studentCode;
    private int sex;
    private int trackNo;
    private int[] results;//{第一圈,第二圈,第三圈,第四圈...}---成绩1ms值

    public SelectResultBean() {
    }

    public SelectResultBean(String studentName, String studentCode, int sex, int trackNo, int[] results) {
        this.studentName = studentName;
        this.studentCode = studentCode;
        this.sex = sex;
        this.trackNo = trackNo;
        this.results = results;
    }

    public int[] getResults() {
        return results;
    }

    public void setResults(int[] results) {
        this.results = results;
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

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getTrackNo() {
        return trackNo;
    }

    public void setTrackNo(int trackNo) {
        this.trackNo = trackNo;
    }

    @Override
    public String toString() {
        return "SelectResultBean{" +
                "results=" + Arrays.toString(results) +
                ", studentName='" + studentName + '\'' +
                ", studentCode='" + studentCode + '\'' +
                ", sex=" + sex +
                ", trackNo=" + trackNo +
                '}';
    }
}
