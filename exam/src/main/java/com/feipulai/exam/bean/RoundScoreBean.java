package com.feipulai.exam.bean;

import java.io.Serializable;
import java.util.List;

public class RoundScoreBean implements Serializable {
    /**
     * "exist":  0,  "0：不存在，1：存在"
     * "roundList": [{
     * "roundNo": ""， //轮次号
     * "resultStatus": 0, // 成绩状态，0:未检录 1:正常 2:犯规 3:中退 4:弃权 5:测试
     * "result": "", //成绩=机器成绩+判罚值
     * 	"testTime": "", //测试时间
     * "machineResult":"",
     * "mtEquipment": "", //监控设备信息
     * "penalty": ""， //判罚值
     * "stumbleCount": 0, //绊绳次数，仅跳绳项目要有
     *     "printTime": "", //打印时间
     * "msEquipment": "已存在的设备信息"
     * } ]
     */
    private int exist;
    private List<ScoreBean> roundList;

    @Override
    public String toString() {
        return "RoundScoreBean{" +
                "exist=" + exist +
                ", roundList=" + roundList +
                '}';
    }

    public int getExist() {
        return exist;
    }

    public void setExist(int exist) {
        this.exist = exist;
    }

    public List<ScoreBean> getRoundList() {
        return roundList;
    }

    public void setRoundList(List<ScoreBean> roundList) {
        this.roundList = roundList;
    }

    public static class ScoreBean{
        public String roundNo;
        public int resultStatus;
        public String result;
        public String testTime;
        public String machineResult;
        public String mtEquipment;
        public String penalty;
        public int stumbleCount;
        private String printTime;
        private String msEquipment;

        @Override
        public String toString() {
            return "ScoreBean{" +
                    "roundNo='" + roundNo + '\'' +
                    ", resultStatus=" + resultStatus +
                    ", result='" + result + '\'' +
                    ", testTime='" + testTime + '\'' +
                    ", machineResult='" + machineResult + '\'' +
                    ", mtEquipment='" + mtEquipment + '\'' +
                    ", penalty='" + penalty + '\'' +
                    ", stumbleCount=" + stumbleCount +
                    ", printTime='" + printTime + '\'' +
                    ", msEquipment='" + msEquipment + '\'' +
                    '}';
        }

        public String getRoundNo() {
            return roundNo;
        }

        public void setRoundNo(String roundNo) {
            this.roundNo = roundNo;
        }

        public int getResultStatus() {
            return resultStatus;
        }

        public void setResultStatus(int resultStatus) {
            this.resultStatus = resultStatus;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getTestTime() {
            return testTime;
        }

        public void setTestTime(String testTime) {
            this.testTime = testTime;
        }

        public String getMachineResult() {
            return machineResult;
        }

        public void setMachineResult(String machineResult) {
            this.machineResult = machineResult;
        }

        public String getMtEquipment() {
            return mtEquipment;
        }

        public void setMtEquipment(String mtEquipment) {
            this.mtEquipment = mtEquipment;
        }

        public String getPenalty() {
            return penalty;
        }

        public void setPenalty(String penalty) {
            this.penalty = penalty;
        }

        public int getStumbleCount() {
            return stumbleCount;
        }

        public void setStumbleCount(int stumbleCount) {
            this.stumbleCount = stumbleCount;
        }

        public String getPrintTime() {
            return printTime;
        }

        public void setPrintTime(String printTime) {
            this.printTime = printTime;
        }

        public String getMsEquipment() {
            return msEquipment;
        }

        public void setMsEquipment(String msEquipment) {
            this.msEquipment = msEquipment;
        }
    }
}
