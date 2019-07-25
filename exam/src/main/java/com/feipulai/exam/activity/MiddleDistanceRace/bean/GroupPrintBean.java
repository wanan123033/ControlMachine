package com.feipulai.exam.activity.MiddleDistanceRace.bean;

public class GroupPrintBean {
    private int trackNo;
    private String studentCode;
    private String studentName;
    private String lastResultString;

    public GroupPrintBean() {
    }

    public GroupPrintBean(int trackNo, String studentCode, String studentName, String lastResultString) {
        this.trackNo = trackNo;
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.lastResultString = lastResultString;
    }

    public int getTrackNo() {
        return trackNo;
    }

    public void setTrackNo(int trackNo) {
        this.trackNo = trackNo;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getLastResultString() {
        return lastResultString;
    }

    public void setLastResultString(String lastResultString) {
        this.lastResultString = lastResultString;
    }

    @Override
    public String toString() {
        return "GroupPrintBean{" +
                "trackNo=" + trackNo +
                ", studentCode='" + studentCode + '\'' +
                ", studentName='" + studentName + '\'' +
                ", lastResultString='" + lastResultString + '\'' +
                '}';
    }
}
