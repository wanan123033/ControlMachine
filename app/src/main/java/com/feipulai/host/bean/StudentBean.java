package com.feipulai.host.bean;

import java.io.Serializable;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2017/11/24.
 */
public class StudentBean implements Serializable {
    private String studentCode;//考号
    private String studentName;//姓名
    private int gender;//性别 0-男  1-女
    private String idCard;//身份证号
    private String className;//班级
    private String schoolName;//学校名称
    private String gradeName;//年级名称
    private String subject;//专业或科目
    private String deptName;//院系
    private int studentType;//考生类型（0.正常，1.择考，2.免考）
    private String examNo;//考试编号
    private String registeredNo;//报名号
    private int examType;//考试类型 0.正常 2.补考，1.缓考
    private String scheduleNo;//日程编号
    private int trackNo; //道号
    private String examItemCode;
    private int machineCode;//机器代码   一个机器码可能对应多个项目代码,所以机器码不能为唯一字段
    private String photoData;//学生头像信息(base64)
    private String faceFeature;//学生头像特征信息(byte[])
    private String downloadTime;//下载时间  时间戳

    public String getPhotoData() {
        return photoData;
    }

    public void setPhotoData(String photoData) {
        this.photoData = photoData;
    }

    public String getFaceFeature() {
        return faceFeature;
    }

    public void setFaceFeature(String faceFeature) {
        this.faceFeature = faceFeature;
    }

    public String getStudentCode() {
        return this.studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getStudentName() {
        return this.studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }


    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public int getStudentType() {
        return studentType;
    }

    public void setStudentType(int studentType) {
        this.studentType = studentType;
    }

    public String getExamNo() {
        return examNo;
    }

    public void setExamNo(String examNo) {
        this.examNo = examNo;
    }

    public String getRegisteredNo() {
        return registeredNo;
    }

    public void setRegisteredNo(String registeredNo) {
        this.registeredNo = registeredNo;
    }

    public int getExamType() {
        return examType;
    }

    public void setExamType(int examType) {
        this.examType = examType;
    }

    public String getScheduleNo() {
        return scheduleNo;
    }

    public void setScheduleNo(String scheduleNo) {
        this.scheduleNo = scheduleNo;
    }

    public int getTrackNo() {
        return trackNo;
    }

    public void setTrackNo(int trackNo) {
        this.trackNo = trackNo;
    }

    public String getExamItemCode() {
        return examItemCode;
    }

    public void setExamItemCode(String examItemCode) {
        this.examItemCode = examItemCode;
    }

    public int getMachineCode() {
        return machineCode;
    }

    public void setMachineCode(int machineCode) {
        this.machineCode = machineCode;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(String downloadTime) {
        this.downloadTime = downloadTime;
    }

    @Override
    public String toString() {
        return "StudentBean{" +
                "studentCode='" + studentCode + '\'' +
                ", studentName='" + studentName + '\'' +
                ", gender=" + gender +
                ", idCard='" + idCard + '\'' +
                ", className='" + className + '\'' +
                ", schoolName='" + schoolName + '\'' +
                ", gradeName='" + gradeName + '\'' +
                ", subject='" + subject + '\'' +
                ", deptName='" + deptName + '\'' +
                ", studentType=" + studentType +
                ", examNo='" + examNo + '\'' +
                ", registeredNo='" + registeredNo + '\'' +
                ", examType=" + examType +
                ", scheduleNo='" + scheduleNo + '\'' +
                ", trackNo=" + trackNo +
                ", examItemCode='" + examItemCode + '\'' +
                ", machineCode=" + machineCode +
                '}';
    }
}
