package com.feipulai.exam.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2017/11/24.
 */
@Entity(
        // Define indexes spanning multiple columns here.
        indexes = {
                @Index(value = "studentCode ASC,itemCode ASC, machineCode ASC", unique = true)
        }
)
public class StudentItem {
    public static final int EXAM_NORMAL = 0;
    public static final int EXAM_DELAYED = 1;
    public static final int EXAM_MAKE = 2;
    @Id(autoincrement = true)
    private Long id;//学生项目ID
    @NotNull
    private String studentCode;//考号
    @NotNull
    private String itemCode;//默认为default
    @NotNull
    private int machineCode;
    private int studentType;//考生类型（0.正常，1.择考，2.免考）
    @NotNull
    private int examType;//考试类型 0.正常，1.缓考，2.补考
//    private int makeUpType;//补考类型  0禁止补考  1 可补考
    private String scheduleNo;  //日程编号
    private String remark1;
    private String remark2;
    private String remark3;

    public StudentItem(String studentCode, String itemCode, int machineCode, int studentType, int examType, String scheduleNo) {
        this.studentCode = studentCode;
        this.itemCode = itemCode;
        this.machineCode = machineCode;
        this.studentType = studentType;
        this.examType = examType;
        this.scheduleNo = scheduleNo;
    }

//    public StudentItem(String studentCode, String itemCode, int machineCode, int studentType, int examType, int makeUpType, String scheduleNo) {
//        this.studentCode = studentCode;
//        this.itemCode = itemCode;
//        this.machineCode = machineCode;
//        this.studentType = studentType;
//        this.examType = examType;
//        this.scheduleNo = scheduleNo;
//    }



    @Generated(hash = 383807586)
    public StudentItem() {
    }

    @Generated(hash = 1667857632)
    public StudentItem(Long id, @NotNull String studentCode, @NotNull String itemCode, int machineCode, int studentType, int examType, String scheduleNo,
            String remark1, String remark2, String remark3) {
        this.id = id;
        this.studentCode = studentCode;
        this.itemCode = itemCode;
        this.machineCode = machineCode;
        this.studentType = studentType;
        this.examType = examType;
        this.scheduleNo = scheduleNo;
        this.remark1 = remark1;
        this.remark2 = remark2;
        this.remark3 = remark3;
    }

    @Override
    public String toString() {
        return "StudentItem{" +
                "id=" + id +
                ", studentCode='" + studentCode + '\'' +
                ", itemCode='" + itemCode + '\'' +
                ", machineCode=" + machineCode +
                ", studentType=" + studentType +
                ", examType=" + examType +
                ", scheduleNo='" + scheduleNo + '\'' +
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

    public int getStudentType() {
        return this.studentType;
    }

    public void setStudentType(int studentType) {
        this.studentType = studentType;
    }

    public int getExamType() {
        return this.examType;
    }

    public void setExamType(int examType) {
        this.examType = examType;
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


    public static String setResultState(int state) {

        switch (state) {
            case RoundResult.RESULT_STATE_NORMAL:
                return "正常";
            case RoundResult.RESULT_STATE_FOUL:
                return "补考";
            case RoundResult.RESULT_STATE_BACK:
                return "缓考";
            case RoundResult.RESULT_STATE_WAIVE:
                return "放弃";
            default:
                return "正常";
        }
    }

//    public int getMakeUpType() {
//        return this.makeUpType;
//    }
//
//    public void setMakeUpType(int makeUpType) {
//        this.makeUpType = makeUpType;
//    }
}
