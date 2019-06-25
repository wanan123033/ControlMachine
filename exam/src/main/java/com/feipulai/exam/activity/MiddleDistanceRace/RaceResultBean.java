package com.feipulai.exam.activity.MiddleDistanceRace;

import java.util.Arrays;
import java.util.List;

/**
 * created by ww on 2019/6/24.
 */
public class RaceResultBean {
    private String no;//组的序号（非组号，与数据库无关）
    private String track;//道次
    private String studentName;//考生姓名
    private String lastResult;//最终成绩
    private String[] results;//到达每一圈的成绩，圈数可设置

    public RaceResultBean() {
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getLastResult() {
        return lastResult;
    }

    public void setLastResult(String lastResult) {
        this.lastResult = lastResult;
    }

    public String[] getResults() {
        return results;
    }

    public void setResults(String[] results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "RaceResultBean{" +
                "no='" + no + '\'' +
                ", track='" + track + '\'' +
                ", studentName='" + studentName + '\'' +
                ", lastResult='" + lastResult + '\'' +
                ", results=" + Arrays.toString(results) +
                '}';
    }
}
