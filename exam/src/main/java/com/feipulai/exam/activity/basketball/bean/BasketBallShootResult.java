package com.feipulai.exam.activity.basketball.bean;

public class BasketBallShootResult {

    private int roundNo;
    private String displayResult;//
    private int result;//实际成绩
    private int penalizeNum;//违例数
    private int resultState; //-999 预设置状态，不做成绩处理

    public BasketBallShootResult(int roundNo, String displayResult, int result, int penalizeNum, int resultState) {
        this.roundNo = roundNo;
        this.displayResult = displayResult;
        this.result = result;
        this.penalizeNum = penalizeNum;
        this.resultState = resultState;
    }

    public int getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(int roundNo) {
        this.roundNo = roundNo;
    }

    public String getDisplayResult() {
        return displayResult;
    }

    public void setDisplayResult(String displayResult) {
        this.displayResult = displayResult;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getPenalizeNum() {
        return penalizeNum;
    }

    public void setPenalizeNum(int penalizeNum) {
        this.penalizeNum = penalizeNum;
    }

    public int getResultState() {
        return resultState;
    }

    public void setResultState(int resultState) {
        this.resultState = resultState;
    }


    @Override
    public String toString() {
        return "BasketBallShootResult{" +
                "roundNo=" + roundNo +
                ", displayResult='" + displayResult + '\'' +
                ", result=" + result +
                ", penalizeNum=" + penalizeNum +
                ", resultState=" + resultState +
                '}';
    }
}
