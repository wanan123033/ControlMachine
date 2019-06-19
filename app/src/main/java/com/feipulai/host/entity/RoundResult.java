package com.feipulai.host.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.Serializable;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2017/11/24.
 */
@Entity
public class RoundResult implements Serializable {

    public static final int RESULT_STATE_NORMAL = 0;
    public static final int RESULT_STATE_FOUL = -1;

    private static final long serialVersionUID = -433841840345102180L;
    @Id(autoincrement = true)
    private Long id;//项目轮次成绩ID
    @NotNull
    private String studentCode;
    @NotNull
    private String itemCode;//默认为default
    @NotNull
    private int machineCode;
    @NotNull
    private int roundNo;//轮次
    @NotNull
    private int result;//成绩      单位为"毫米(mm)"、"毫秒(ms)"、"克(g)"、"次","毫升"
    private int weightResult;// 体重成绩,身体体重项目有
    private int stumbleCount;// 绊绳次数
	/**
	 * 取值为{@link #RESULT_STATE_NORMAL}或{@link #RESULT_STATE_FOUL}
	 */
    @NotNull
    private int resultState;//成绩状态 0正常  -1犯规    -2中退    -3放弃    体侧系统没有中退和放弃,且犯规均为机器判定的犯规
    @NotNull
    private int isLastResult;//是否为最好成绩 0-不是 1-是     身高体重最后成绩即为最好成绩
    @NotNull
    private String testTime;//测试时间  格式:yyyy-MM-dd HH:mm:ss
    private String printTime;//打印时间 格式:yyyy-MM-dd HH:mm:ss
    @NotNull
    private int updateState;//上传状态 0未上传 1上传

    private String remark1;
    private String remark2;
    private String remark3;

    public final static String BEAN_KEY = "ROUNDRESULT_KEY";


    @Generated(hash = 224505125)
    public RoundResult(Long id, @NotNull String studentCode, @NotNull String itemCode,
            int machineCode, int roundNo, int result, int weightResult, int stumbleCount,
            int resultState, int isLastResult, @NotNull String testTime, String printTime,
            int updateState, String remark1, String remark2, String remark3) {
        this.id = id;
        this.studentCode = studentCode;
        this.itemCode = itemCode;
        this.machineCode = machineCode;
        this.roundNo = roundNo;
        this.result = result;
        this.weightResult = weightResult;
        this.stumbleCount = stumbleCount;
        this.resultState = resultState;
        this.isLastResult = isLastResult;
        this.testTime = testTime;
        this.printTime = printTime;
        this.updateState = updateState;
        this.remark1 = remark1;
        this.remark2 = remark2;
        this.remark3 = remark3;
    }


    @Generated(hash = 1393632943)
    public RoundResult() {
    }

    
    @Override
    public String toString() {
        return "RoundResult{" +
                "id=" + id +
                ", studentCode='" + studentCode + '\'' +
                ", itemCode='" + itemCode + '\'' +
                ", machineCode=" + machineCode +
                ", roundNo=" + roundNo +
                ", result=" + result +
                ", weightResult=" + weightResult +
                ", stumbleCount=" + stumbleCount +
                ", resultState=" + resultState +
                ", isLastResult=" + isLastResult +
                ", testTime='" + testTime + '\'' +
                ", printTime='" + printTime + '\'' +
                ", updateState=" + updateState +
                ", remark1='" + remark1 + '\'' +
                ", remark2='" + remark2 + '\'' +
                ", remark3='" + remark3 + '\'' +
                '}';
    }


    public Long getId() {
        return this.id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getStudentCode() {
        return this.studentCode;
    }


    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }


    public String getItemCode() {
        return this.itemCode;
    }


    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }


    public int getMachineCode() {
        return this.machineCode;
    }


    public void setMachineCode(int machineCode) {
        this.machineCode = machineCode;
    }


    public int getRoundNo() {
        return this.roundNo;
    }


    public void setRoundNo(int roundNo) {
        this.roundNo = roundNo;
    }


    public int getResult() {
        return this.result;
    }


    public void setResult(int result) {
        this.result = result;
    }


    public int getWeightResult() {
        return this.weightResult;
    }


    public void setWeightResult(int weightResult) {
        this.weightResult = weightResult;
    }


    public int getStumbleCount() {
        return this.stumbleCount;
    }


    public void setStumbleCount(int stumbleCount) {
        this.stumbleCount = stumbleCount;
    }


    public int getResultState() {
        return this.resultState;
    }


    public void setResultState(int resultState) {
        this.resultState = resultState;
    }


    public int getIsLastResult() {
        return this.isLastResult;
    }


    public void setIsLastResult(int isLastResult) {
        this.isLastResult = isLastResult;
    }


    public String getTestTime() {
        return this.testTime;
    }


    public void setTestTime(String testTime) {
        this.testTime = testTime;
    }


    public String getPrintTime() {
        return this.printTime;
    }


    public void setPrintTime(String printTime) {
        this.printTime = printTime;
    }


    public int getUpdateState() {
        return this.updateState;
    }


    public void setUpdateState(int updateState) {
        this.updateState = updateState;
    }


    public String getRemark1() {
        return this.remark1;
    }


    public void setRemark1(String remark1) {
        this.remark1 = remark1;
    }


    public String getRemark2() {
        return this.remark2;
    }


    public void setRemark2(String remark2) {
        this.remark2 = remark2;
    }


    public String getRemark3() {
        return this.remark3;
    }


    public void setRemark3(String remark3) {
        this.remark3 = remark3;
    }
    
}
