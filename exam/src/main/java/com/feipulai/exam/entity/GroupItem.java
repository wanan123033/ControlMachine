package com.feipulai.exam.entity;

import android.text.TextUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 项目分组学生
 * Created by zzs on  2018/12/12
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
@Entity(
        // Define indexes spanning multiple columns here.
        indexes = {
                @Index(value = "itemCode ASC,groupType ASC ,sortName ASC ,groupNo ASC ,scheduleNo ASC,studentCode ASC  ", unique = true)
        }
)
public class GroupItem {


    @Id(autoincrement = true)
    private Long id;
    @NotNull
    private String itemCode;//项目代码
    @NotNull
    private int groupType;//分组性别（0.男子 1.女子 2.混合）
    @NotNull
    private String sortName;//组别
    @NotNull
    private int groupNo;//组号（分组）
    @NotNull
    private String scheduleNo;//考点日程编号	日程编号就是场次
    @NotNull
    private String studentCode;//学生考号
    @NotNull
    private int trackNo;//道次
    @NotNull
    private int identityMark;//身份验证标示 0 无验证 1 有验证

    private int examType;//考试类型 0.正常，1.缓考 2.补考

    private String remark1;
    private String remark2;
    private String remark3;
@Generated(hash = 471412480)
public GroupItem(Long id, @NotNull String itemCode, int groupType, @NotNull String sortName, int groupNo, @NotNull String scheduleNo,
        @NotNull String studentCode, int trackNo, int identityMark, int examType, String remark1, String remark2, String remark3) {
    this.id = id;
    this.itemCode = itemCode;
    this.groupType = groupType;
    this.sortName = sortName;
    this.groupNo = groupNo;
    this.scheduleNo = scheduleNo;
    this.studentCode = studentCode;
    this.trackNo = trackNo;
    this.identityMark = identityMark;
    this.examType = examType;
    this.remark1 = remark1;
    this.remark2 = remark2;
    this.remark3 = remark3;
}

    public GroupItem(String itemCode, int groupType, String sortName, int groupNo, String scheduleNo, String studentCode, int trackNo, int identityMark, int examType) {
        this.itemCode = itemCode;
        this.groupType = groupType;
        this.sortName = sortName;
        this.groupNo = groupNo;
        this.scheduleNo = scheduleNo;
        this.studentCode = studentCode;
        this.trackNo = trackNo;
        this.identityMark = identityMark;
        this.examType = examType;
    }

    @Generated(hash = 7721114)
public GroupItem() {
}

public Long getId() {
    return this.id;
}
public void setId(Long id) {
    this.id = id;
}
public String getItemCode() {
    return this.itemCode;
}
public void setItemCode(String itemCode) {
    this.itemCode = itemCode;
}
public int getGroupType() {
    return this.groupType;
}
public void setGroupType(int groupType) {
    this.groupType = groupType;
}
public String getSortName() {
    return this.sortName;
}
public void setSortName(String sortName) {
    this.sortName = sortName;
}
public int getGroupNo() {
    return this.groupNo;
}
public void setGroupNo(int groupNo) {
    this.groupNo = groupNo;
}
public String getScheduleNo() {
    return this.scheduleNo;
}
public void setScheduleNo(String scheduleNo) {
    this.scheduleNo = scheduleNo;
}
public String getStudentCode() {
    return this.studentCode;
}
public void setStudentCode(String studentCode) {
    this.studentCode = studentCode;
}
public int getTrackNo() {
    return this.trackNo;
}
public void setTrackNo(int trackNo) {
    this.trackNo = trackNo;
}
public int getIdentityMark() {
    return this.identityMark;
}
public void setIdentityMark(int identityMark) {
    this.identityMark = identityMark;
}
public int getExamType() {
    return this.examType;
}
public void setExamType(int examType) {
    this.examType = examType;
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
