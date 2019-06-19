package com.feipulai.exam.activity.MiddleDistanceRace;

import java.util.List;

/**
 * created by ww on 2019/6/18.
 */
public class MiddleTimingResultBean {
    private int trackNo;//道次
    private String studentName;//姓名
    private String lastResult;//最终成绩  时 分 秒
    private List<String> times;//到第n圈用时  时 分 秒

    public MiddleTimingResultBean() {
    }

    public int getTrackNo() {
        return trackNo;
    }

    public void setTrackNo(int trackNo) {
        this.trackNo = trackNo;
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

    public List<String> getTimes() {
        return times;
    }

    public void setTimes(List<String> times) {
        this.times = times;
    }

    @Override
    public String toString() {
        return "MiddleTimingResultBean{" +
                "trackNo=" + trackNo +
                ", studentName='" + studentName + '\'' +
                ", lastResult='" + lastResult + '\'' +
                ", times=" + times +
                '}';
    }
}
