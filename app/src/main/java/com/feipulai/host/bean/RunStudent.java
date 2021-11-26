package com.feipulai.host.bean;

import com.feipulai.host.entity.Student;

import java.io.Serializable;
import java.util.List;

/**
 * Created by pengjf on 2018/12/4.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class RunStudent implements Serializable {
    private Student student ;
    private int runId ;
    private String mark ;
    private int connectState ;
    /**分组道次*/
    private int trackNo ;
    private List<WaitResult> resultList;
    private int originalMark ;
    private int independentTime;
    private int roundNo;
    private String flag ="";//标记错误项
    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public int getRunId() {
        return runId;
    }

    public void setRunId(int runId) {
        this.runId = runId;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    private boolean isDelete ;

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public int getConnectState() {
        return connectState;
    }

    public void setConnectState(int connectState) {
        this.connectState = connectState;
    }

    public int getTrackNo() {
        return trackNo;
    }

    public void setTrackNo(int trackNo) {
        this.trackNo = trackNo;
    }

    public List<WaitResult> getResultList() {
        return resultList;
    }

    public void setResultList(List<WaitResult> resultList) {
        this.resultList = resultList;
    }

    public int getOriginalMark() {
        return originalMark;
    }

    public void setOriginalMark(int originalMark) {
        this.originalMark = originalMark;
    }

    public int getIndependentTime() {
        return independentTime;
    }

    public void setIndependentTime(int independentTime) {
        this.independentTime = independentTime;
    }

    public void setRoundNo(int roundNo) {
        this.roundNo = roundNo;
    }

    public int getRoundNo() {
        return roundNo;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public static class WaitResult{
        private String waitResult;
        private int oriResult;

        public String getWaitResult() {
            return waitResult;
        }

        public void setWaitResult(String waitResult) {
            this.waitResult = waitResult;
        }

        public int getOriResult() {
            return oriResult;
        }

        public void setOriResult(int oriResult) {
            this.oriResult = oriResult;
        }
    }
}
