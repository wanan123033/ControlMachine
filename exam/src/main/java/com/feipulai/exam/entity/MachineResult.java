package com.feipulai.exam.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.Serializable;

/**
 * 机器成绩表
 * Created by zzs on  2019/6/18
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
@Entity
public class MachineResult implements Serializable {

    private static final long serialVersionUID = -6455607275367265889L;
    @Id(autoincrement = true)
    private Long id;//项目轮次成绩ID
    //考生号
    @NotNull
    private String studentCode;
    @NotNull
    private int roundNo;//轮次
    @NotNull
    private int testNo;//测试次数
    @NotNull
    private String itemCode;
    @NotNull
    private int machineCode;//机器代码   一个机器码可能对应多个项目代码,所以机器码不能为唯一字段  每种机器的机器码参考{@link ItemDefault}
    @NotNull
    private int result;////成绩      单位为"毫米(mm)"、"毫秒(ms)"、"克(g)"、"次","毫升"（中长跑最终成绩）
    private long groupId = RoundResult.DEAFULT_GROUP_ID;
    private int resultType=0;//成绩类型 0 机器成绩  1 折返成绩
    private int resultState=0;//成绩状态 0 正常 1人工添加
    @Generated(hash = 1679554979)
    public MachineResult(Long id, @NotNull String studentCode, int roundNo, int testNo,
            @NotNull String itemCode, int machineCode, int result, long groupId, int resultType,
            int resultState) {
        this.id = id;
        this.studentCode = studentCode;
        this.roundNo = roundNo;
        this.testNo = testNo;
        this.itemCode = itemCode;
        this.machineCode = machineCode;
        this.result = result;
        this.groupId = groupId;
        this.resultType = resultType;
        this.resultState = resultState;
    }

    @Generated(hash = 1551746015)
    public MachineResult() {
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

    public int getResult() {
        return this.result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public int getResultType() {
        return resultType;
    }

    public void setResultType(int resultType) {
        this.resultType = resultType;
    }

    public int getResultState() {
        return resultState;
    }

    public void setResultState(int resultState) {
        this.resultState = resultState;
    }

    @Override
    public String toString() {
        return "MachineResult{" +
                " 准考证号='" + studentCode + '\'' +
                ", 轮次=" + roundNo +
                ", 测试次数=" + testNo +
                ", 项目代码='" + itemCode + '\'' +
                ", 机器码=" + machineCode +
                ", 成绩=" + result +
                ", groupId=" + groupId +
                ",id=" + id +
                '}';
    }
}
