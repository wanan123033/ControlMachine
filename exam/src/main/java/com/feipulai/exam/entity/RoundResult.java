package com.feipulai.exam.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2017/11/24.
 */
@Entity
public class RoundResult implements Serializable {
    public static final String ENCRYPT_KEY = "CHECKOUT_ENCRYPT";
    public static final int NOT_LAST_RESULT = 0;
    public static final int LAST_RESULT = 1;
    public static final long DEAFULT_GROUP_ID = -1;
    public static final int RESULT_STATE_NORMAL = 1;
    public static final int RESULT_STATE_FOUL = 2;
    public static final int RESULT_STATE_BACK = 3;
    public static final int RESULT_STATE_WAIVE = 4;
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
    private int testNo;//测试次数
    private int machineResult;// 获取到的机器成绩
    private int penaltyNum;// 判罚值   判罚值有正负
    @NotNull
    private int result;//成绩      单位为"毫米(mm)"、"毫秒(ms)"、"克(g)"、"次","毫升"
    /**
     * 取值为{@link #RESULT_STATE_NORMAL}或{@link #RESULT_STATE_FOUL}
     */
    @NotNull
    private int resultState;//成绩状态 //是否犯规 0:未检录 1:正常 2:犯规 3:中退 4:弃权 5:测试     体侧系统没有中退和放弃,且犯规均为机器判定的犯规
    @NotNull
    private int isLastResult;//是否为最好成绩 0-不是 1-是     身高体重最后成绩即为最好成绩
    @NotNull
    private int examType;//考试类型 0.正常 2.补考
    @NotNull
    private String testTime;//测试时间  时间戳
    private String printTime = "";//w打印时间 时间戳
    private String endTime;//结束时间 时间戳
    private int stumbleCount;// 绊绳次数
    @NotNull
    private int updateState;//上传状态 0未上传 1上传
    private byte[] cycleResult;//中长跑每一圈成绩
    private Long groupId = DEAFULT_GROUP_ID;//分组id
    private String scheduleNo;  //日程编号
    private String mtEquipment;//监控设备
    private int roundTestState;//轮次测试标识 0 正常 1重测 （标识本轮成绩是否设置为重测）
    private int resultTestState;//轮次成绩测试标识  0 正常 1重测
    private boolean isDelete = false;//数据是否为删除状态：true 本条数据为弃用，默认为false
    private String remark1;
    private String remark2;
    private String remark3;//保存校验信息  studentCode+ 项目+ 考试类型+ 成绩 + 测试时间

    public final static String BEAN_KEY = "ROUNDRESULT_KEY";


    @Generated(hash = 1393632943)
    public RoundResult() {
    }

    @Generated(hash = 1790186008)
    public RoundResult(Long id, @NotNull String studentCode, @NotNull String itemCode, int machineCode,
                       int roundNo, int testNo, int machineResult, int penaltyNum, int result, int resultState,
                       int isLastResult, int examType, @NotNull String testTime, String printTime, String endTime,
                       int stumbleCount, int updateState, byte[] cycleResult, Long groupId, String scheduleNo,
                       String mtEquipment, int roundTestState, int resultTestState, boolean isDelete,
                       String remark1, String remark2, String remark3) {
        this.id = id;
        this.studentCode = studentCode;
        this.itemCode = itemCode;
        this.machineCode = machineCode;
        this.roundNo = roundNo;
        this.testNo = testNo;
        this.machineResult = machineResult;
        this.penaltyNum = penaltyNum;
        this.result = result;
        this.resultState = resultState;
        this.isLastResult = isLastResult;
        this.examType = examType;
        this.testTime = testTime;
        this.printTime = printTime;
        this.endTime = endTime;
        this.stumbleCount = stumbleCount;
        this.updateState = updateState;
        this.cycleResult = cycleResult;
        this.groupId = groupId;
        this.scheduleNo = scheduleNo;
        this.mtEquipment = mtEquipment;
        this.roundTestState = roundTestState;
        this.resultTestState = resultTestState;
        this.isDelete = isDelete;
        this.remark1 = remark1;
        this.remark2 = remark2;
        this.remark3 = remark3;
    }

    public RoundResult(Long id, @NotNull String studentCode, @NotNull String itemCode, int machineCode,
                       int roundNo, int testNo, int machineResult, int penaltyNum, int result, int resultState,
                       int isLastResult, int examType, @NotNull String testTime, String printTime, String endTime,
                       int stumbleCount, int updateState, byte[] cycleResult, Long groupId, String scheduleNo,
                       String mtEquipment, String remark1, String remark2, String remark3) {
        this.id = id;
        this.studentCode = studentCode;
        this.itemCode = itemCode;
        this.machineCode = machineCode;
        this.roundNo = roundNo;
        this.testNo = testNo;
        this.machineResult = machineResult;
        this.penaltyNum = penaltyNum;
        this.result = result;
        this.resultState = resultState;
        this.isLastResult = isLastResult;
        this.examType = examType;
        this.testTime = testTime;
        this.printTime = printTime;
        this.endTime = endTime;
        this.stumbleCount = stumbleCount;
        this.updateState = updateState;
        this.cycleResult = cycleResult;
        this.groupId = groupId;
        this.scheduleNo = scheduleNo;
        this.mtEquipment = mtEquipment;
        this.remark1 = remark1;
        this.remark2 = remark2;
        this.remark3 = remark3;
    }

    public int getRoundTestState() {
        return roundTestState;
    }

    public void setRoundTestState(int roundTestState) {
        this.roundTestState = roundTestState;
    }

    public int getResultTestState() {
        return resultTestState;
    }

    public void setResultTestState(int resultTestState) {
        this.resultTestState = resultTestState;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
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

    public int getTestNo() {
        return this.testNo;
    }

    public void setTestNo(int testNo) {
        this.testNo = testNo;
    }

    public int getMachineResult() {
        return this.machineResult;
    }

    public void setMachineResult(int machineResult) {
        this.machineResult = machineResult;
    }

    public int getPenaltyNum() {
        return this.penaltyNum;
    }

    public void setPenaltyNum(int penaltyNum) {
        this.penaltyNum = penaltyNum;
    }

    public int getResult() {
        return this.result;
    }

    public void setResult(int result) {
        this.result = result;
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

    public int getExamType() {
        return this.examType;
    }

    public void setExamType(int examType) {
        this.examType = examType;
    }

    public String getTestTime() {
        return this.testTime;
    }

    public void setTestTime(String testTime) {
        this.testTime = testTime;
    }

    public int getStumbleCount() {
        return this.stumbleCount;
    }

    public void setStumbleCount(int stumbleCount) {
        this.stumbleCount = stumbleCount;
    }

    public int getUpdateState() {
        return this.updateState;
    }

    public void setUpdateState(int updateState) {
        this.updateState = updateState;
    }

    public byte[] getCycleResult() {
        return this.cycleResult;
    }

    public void setCycleResult(byte[] cycleResult) {
        this.cycleResult = cycleResult;
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getScheduleNo() {
        return this.scheduleNo;
    }

    public void setScheduleNo(String scheduleNo) {
        this.scheduleNo = scheduleNo;
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

    public String getMtEquipment() {
        return mtEquipment;
    }

    public void setMtEquipment(String mtEquipment) {
        this.mtEquipment = mtEquipment;
    }


    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }


    public String getPrintTime() {
        return this.printTime;
    }


    public void setPrintTime(String printTime) {
        this.printTime = printTime;
    }

    @Override
    public String toString() {
        return "考生成绩{" +
                "准考证号='" + studentCode + '\'' +
                ", 项目代码='" + itemCode + '\'' +
                ", 机器码=" + machineCode +
                ", 轮次=" + roundNo +
                ", 测试次数=" + testNo +
                ", 机器成绩=" + machineResult +
                ", 判罚数=" + penaltyNum +
                ", 成绩=" + result +
                ", 成绩状态（1:正常2:犯规3:中退4:弃权）=" + resultState +
                ", 考试类型（0.正常 2.补考）=" + examType +
                ", 开始时间='" + testTime + '\'' +
                ", 结束时间='" + endTime + '\'' +
                ", 日程编号='" + scheduleNo + '\'' +
                ", 绊绳次数=" + stumbleCount +
                ", 是否轮次重测=" + roundTestState +
                ", 轮次成绩是否重测=" + resultTestState +
                ", 是否无效=" + isDelete +
                ", remark3='" + remark3 + '\'' +
                ", id=" + id +
                ", printTime='" + printTime + '\'' +
                ", 分组ID=" + groupId +
                '}';
    }

    public boolean getIsDelete() {
        return this.isDelete;
    }

    public void setIsDelete(boolean isDelete) {
        this.isDelete = isDelete;
    }
}
