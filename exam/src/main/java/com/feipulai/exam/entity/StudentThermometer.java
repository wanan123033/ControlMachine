package com.feipulai.exam.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 体温测量数据
 * Created by zzs on  2020/4/16
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
@Entity
public class StudentThermometer {

    @Id(autoincrement = true)
    private Long id;//
    @NotNull
    private String studentCode;//考号
    @NotNull
    private String itemCode;//默认为default
    @NotNull
    private int machineCode;
    @NotNull
    private int examType;//考试类型 0.正常，1.缓考，2.补考
    private double thermometer;//体温
    private String measureTime;//测量时间


    @Generated(hash = 585747260)
    public StudentThermometer() {
    }

    @Generated(hash = 337143039)
    public StudentThermometer(Long id, @NotNull String studentCode, @NotNull String itemCode, int machineCode, int examType,
            double thermometer, String measureTime) {
        this.id = id;
        this.studentCode = studentCode;
        this.itemCode = itemCode;
        this.machineCode = machineCode;
        this.examType = examType;
        this.thermometer = thermometer;
        this.measureTime = measureTime;
    }

    public String getMeasureTime() {
        return measureTime;
    }

    public void setMeasureTime(String measureTime) {
        this.measureTime = measureTime;
    }

    public double getThermometer() {
        return thermometer;
    }

    public void setThermometer(double thermometer) {
        this.thermometer = thermometer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public int getMachineCode() {
        return machineCode;
    }

    public void setMachineCode(int machineCode) {
        this.machineCode = machineCode;
    }

    public int getExamType() {
        return examType;
    }

    public void setExamType(int examType) {
        this.examType = examType;
    }
}
