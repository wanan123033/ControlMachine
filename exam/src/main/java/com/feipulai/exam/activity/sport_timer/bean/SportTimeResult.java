package com.feipulai.exam.activity.sport_timer.bean;

public class SportTimeResult {
    private long id;
    private int deviceId;
    private int round;
    private int result = -1;
    private int resultState;
    private String routeName;
    private int receiveIndex = -1;
    private int partResult = -1;
    private String remark = "";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

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

    public int getResultState() {
        return resultState;
    }

    public void setResultState(int resultState) {
        this.resultState = resultState;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public int getReceiveIndex() {
        return receiveIndex;
    }

    @Override
    public String toString() {
        return "SportTimeResult{" +
                "id=" + id +
                ", deviceId=" + deviceId +
                ", round=" + round +
                ", result=" + result +
                ", resultState=" + resultState +
                ", routeName=" + routeName +
                ", receiveIndex=" + receiveIndex +
                ", partResult=" + partResult +
                ", remark='" + remark + '\'' +
                '}';
    }

    public void setReceiveIndex(int receiveIndex) {
        this.receiveIndex = receiveIndex;
    }

    public int getPartResult() {
        return partResult;
    }

    public void setPartResult(int partResult) {
        this.partResult = partResult;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public SportTimeResult(){}

    public SportTimeResult(int round,int result,int resultState){
        this.round = round;
        this.result = result;
        this.resultState = resultState;
    }
}
