package com.feipulai.exam.activity.sport_timer;

class SportTimeResult {
    private long id;
    private int deviceId;
    private int round;
    private int result;
    private int resultState;
    private int resultRule;
    private int receiveIndex;
    private int partResult;
    private String remark;
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

    public int getResultRule() {
        return resultRule;
    }

    public void setResultRule(int resultRule) {
        this.resultRule = resultRule;
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
                ", resultRule=" + resultRule +
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


}
