package com.feipulai.exam.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * 分组
 * Created by pengjf on 2018/11/20.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
@Entity(nameInDb = "GROUPING", indexes = {
        @Index(value = "scheduleNo ASC,groupType ASC,sortName ASC,groupNo ASC,itemCode ASC", unique = true)
})

public class Group {

    public static final int NOT_TEST = 0;
    public static final int NOT_FINISHED = 2;
    public static final int FINISHED = 1;

    public static final int MALE = 0;
    public static final int FEMALE = 1;
    public static final int MIXTURE = 2;

    @Id(autoincrement = true)
    private Long id;
    @NotNull
    private int groupType;//分组性别（0.男子 1.女子 2.混合）
    @NotNull
    private String sortName;//组别
    @NotNull
    private int groupNo;//组号（分组）
    @NotNull
    private String scheduleNo;//考点日程编号	日程编号就是场次
    @NotNull
    private int examType;  //考试类型 0.正常 2.补考，1.缓考
    @NotNull
    private int isTestComplete;//是否测试完成 0-未测试 1-已测试  2-未测完 （中长跑:3-空闲 4-关联 5-完成)
    @NotNull
    private String itemCode;//项目代码

    private String remark1;//备注1字段---颜色组名
    private String remark2;//备注2字段---颜色
    private String remark3;


    @Generated(hash = 681271684)
    public Group(Long id, int groupType, @NotNull String sortName, int groupNo, @NotNull String scheduleNo, int examType,
                 int isTestComplete, @NotNull String itemCode, String remark1, String remark2, String remark3) {
        this.id = id;
        this.groupType = groupType;
        this.sortName = sortName;
        this.groupNo = groupNo;
        this.scheduleNo = scheduleNo;
        this.examType = examType;
        this.isTestComplete = isTestComplete;
        this.itemCode = itemCode;
        this.remark1 = remark1;
        this.remark2 = remark2;
        this.remark3 = remark3;
    }

    @Generated(hash = 117982048)
    public Group() {
    }

    public Group(int groupType, String sortName, int groupNo, String scheduleNo, String itemCode, int examType, int isTestComplete) {
        this.groupType = groupType;
        this.sortName = sortName;
        this.groupNo = groupNo;
        this.scheduleNo = scheduleNo;
        this.examType = examType;
        this.isTestComplete = isTestComplete;
        this.itemCode = itemCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


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


    public String getRemark1() {
        return remark1;
    }

    public void setRemark1(String remark1) {
        this.remark1 = remark1;
    }

    public String getRemark2() {
        return remark2;
    }

    public void setRemark2(String remark2) {
        this.remark2 = remark2;
    }

    public String getRemark3() {
        return remark3;
    }

    public void setRemark3(String remark3) {
        this.remark3 = remark3;
    }

    public int getIsTestComplete() {
        return this.isTestComplete;
    }

    public void setIsTestComplete(int isTestComplete) {
        this.isTestComplete = isTestComplete;
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

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", groupType=" + groupType +
                ", sortName='" + sortName + '\'' +
                ", groupNo=" + groupNo +
                ", scheduleNo='" + scheduleNo + '\'' +
                ", examType=" + examType +
                ", isTestComplete=" + isTestComplete +
                ", remark1='" + remark1 + '\'' +
                ", remark2='" + remark2 + '\'' +
                ", remark3='" + remark3 + '\'' +
                '}';
    }

    public String getItemCode() {
        return this.itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }
}
