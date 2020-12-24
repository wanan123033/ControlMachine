package com.feipulai.exam.activity.sport_timer.bean;

import java.util.List;

public class SportTestResult {
    public SportTestResult(){}
    public SportTestResult(int round, int result, int resultState,List<SportTimeResult> sportTimeResults) {
        this.round = round;
        this.result = result;
        this.resultState = resultState;
        this.sportTimeResults = sportTimeResults;
    }
    private int round;
    private List<SportTimeResult> sportTimeResults;
    private int result = -1;
    private int select;
    private int resultState;//-999 预设置状态，不做成绩处理
    private String testTime;
    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
    public List<SportTimeResult> getSportTimeResults() {
        return sportTimeResults;
    }

    public void setSportTimeResults(List<SportTimeResult> sportTimeResults) {
        this.sportTimeResults = sportTimeResults;
    }

    public int getSelect() {
        return select;
    }

    public void setSelect(int select) {
        this.select = select;
    }

    @Override
    public String toString() {
        return "SportTestResult{" +
                "round=" + round +
                ", sportTimeResults=" + sportTimeResults +
                ", testResult=" + result +
                ", select=" + select +
                ", resultState=" + resultState +
                '}';
    }

    public int getResultState() {
        return resultState;
    }

    public void setResultState(int resultState) {
        this.resultState = resultState;
    }

    public String getTestTime() {
        return testTime;
    }

    public void setTestTime(String testTime) {
        this.testTime = testTime;
    }
}
