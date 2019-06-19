package com.feipulai.exam.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 分组
 * Created by pengjf on 2018/11/20.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class GroupBean implements Serializable {
    private int groupType;//分组性别（0.男子 1.女子 2.混合）
    private String sortName;//组别
    private int groupNo;//组号（分组）
    private String scheduleNo;//考点日程编号	日程编号就是场次
    private int examType;  //考试类型 0.正常 1.补考，2.缓考
    private String beginTime;//开始时间，Long类型，时间戳   日程开始时间
    private List<StudentBean> studentCodeList;//分组学生

    public int getGroupType() {
        return groupType;
    }

    public void setGroupType(int groupType) {
        this.groupType = groupType;
    }

    public String getSortName() {
        return sortName;
    }

    public void setSortName(String sortName) {
        this.sortName = sortName;
    }

    public int getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(int groupNo) {
        this.groupNo = groupNo;
    }


    public String getScheduleNo() {
        return scheduleNo;
    }

    public void setScheduleNo(String scheduleNo) {
        this.scheduleNo = scheduleNo;
    }

    public int getExamType() {
        return examType;
    }

    public void setExamType(int examType) {
        this.examType = examType;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public List<StudentBean> getStudentCodeList() {
        return studentCodeList;
    }

    public void setStudentCodeList(List<StudentBean> studentList) {
        this.studentCodeList = studentList;
    }

    @Override
    public String toString() {
        return "GroupBean{" +
                "groupType=" + groupType +
                ", sortName='" + sortName + '\'' +
                ", groupNo=" + groupNo +
                ", scheduleNo='" + scheduleNo + '\'' +
                ", examType=" + examType +
                ", beginTime='" + beginTime + '\'' +
                ", studentList=" + studentCodeList +
                '}';
    }
}
