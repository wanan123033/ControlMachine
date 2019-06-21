package com.feipulai.exam.activity.basketball.result;

import com.feipulai.exam.entity.MachineResult;

import java.util.List;

/**
 * Created by zzs on  2019/6/18
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BasketBallTestResult {
    private int roundNo;
    private List<MachineResult> machineResultList;//机器成绩
    private int selectMachineResult;//选中的机器成绩
    private int result;//实际成绩
    private int penalizeNum;//违例数
    private int resultState;

    public BasketBallTestResult(int roundNo, List<MachineResult> machineResultList, int selectMachineResult, int result, int penalizeNum, int resultState) {
        this.roundNo = roundNo;
        this.machineResultList = machineResultList;
        this.selectMachineResult = selectMachineResult;
        this.result = result;
        this.penalizeNum = penalizeNum;
        this.resultState = resultState;
    }

    public List<MachineResult> getMachineResultList() {
        return machineResultList;
    }

    public void setMachineResultList(List<MachineResult> machineResultList) {
        this.machineResultList = machineResultList;
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

    public int getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(int roundNo) {
        this.roundNo = roundNo;
    }

    public int getSelectMachineResult() {
        return selectMachineResult;
    }

    public void setSelectMachineResult(int selectMachineResult) {
        this.selectMachineResult = selectMachineResult;
    }

    @Override
    public String toString() {
        return "BasketBallTestResult{" +
                "roundNo=" + roundNo +
                ", machineResultList=" + machineResultList +
                ", selectMachineResult=" + selectMachineResult +
                ", result=" + result +
                ", penalizeNum=" + penalizeNum +
                ", resultState=" + resultState +
                '}';
    }
}
