package com.feipulai.exam.entity;


import com.feipulai.exam.bean.UserBean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

import java.util.Arrays;

/**
 * Created by zzs on  2021/1/27
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
@Entity
public class Account {
    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private String account;
    @NotNull
    private String password;


    private String type;//0 超级管理员  1操作员


    private Long createTime;
    private Long updateTime;

    private String token;
    private String examName;//考试名称
    private String siteId; //考点ID
    private String examPersonnelPhotoSuffix;
    private String permission;
    private String examPersonnelPhotoData;
    private int equipmentstatus;
    @Unique
    @NotNull
    private String examPersonnelId;
    private int examPersonnelPhotoType;
    private String typevalue;

    private String faceFeature;

    public Account(UserBean userBean) {
        type = userBean.getType();
        token = userBean.getToken();
        examName = userBean.getExamName();//考试名称
        siteId = userBean.getSiteId(); //考点ID
        examPersonnelPhotoSuffix = userBean.getExamPersonnelPhotoSuffix();
        permission = Arrays.toString(userBean.getPermission());
        examPersonnelPhotoData = userBean.getExamPersonnelPhotoData();
        equipmentstatus = userBean.getEquipmentstatus();
        examPersonnelId = userBean.getExamPersonnelId();
        examPersonnelPhotoType = userBean.getExamPersonnelPhotoType();
        typevalue = userBean.getTypevalue();
    }

    public Account(Long id, @NotNull String account, @NotNull String password,
                   int type) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.type = type + "";
    }

    @Generated(hash = 1714747078)
    public Account(Long id, @NotNull String account, @NotNull String password,
            String type, Long createTime, Long updateTime, String token,
            String examName, String siteId, String examPersonnelPhotoSuffix,
            String permission, String examPersonnelPhotoData, int equipmentstatus,
            @NotNull String examPersonnelId, int examPersonnelPhotoType,
            String typevalue, String faceFeature) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.type = type;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.token = token;
        this.examName = examName;
        this.siteId = siteId;
        this.examPersonnelPhotoSuffix = examPersonnelPhotoSuffix;
        this.permission = permission;
        this.examPersonnelPhotoData = examPersonnelPhotoData;
        this.equipmentstatus = equipmentstatus;
        this.examPersonnelId = examPersonnelId;
        this.examPersonnelPhotoType = examPersonnelPhotoType;
        this.typevalue = typevalue;
        this.faceFeature = faceFeature;
    }

    @Generated(hash = 882125521)
    public Account() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getExamPersonnelPhotoSuffix() {
        return examPersonnelPhotoSuffix;
    }

    public void setExamPersonnelPhotoSuffix(String examPersonnelPhotoSuffix) {
        this.examPersonnelPhotoSuffix = examPersonnelPhotoSuffix;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getExamPersonnelPhotoData() {
        return examPersonnelPhotoData;
    }

    public void setExamPersonnelPhotoData(String examPersonnelPhotoData) {
        this.examPersonnelPhotoData = examPersonnelPhotoData;
    }

    public int getEquipmentstatus() {
        return equipmentstatus;
    }

    public void setEquipmentstatus(int equipmentstatus) {
        this.equipmentstatus = equipmentstatus;
    }

    public String getExamPersonnelId() {
        return examPersonnelId;
    }

    public void setExamPersonnelId(String examPersonnelId) {
        this.examPersonnelId = examPersonnelId;
    }

    public int getExamPersonnelPhotoType() {
        return examPersonnelPhotoType;
    }

    public void setExamPersonnelPhotoType(int examPersonnelPhotoType) {
        this.examPersonnelPhotoType = examPersonnelPhotoType;
    }

    public String getTypevalue() {
        return typevalue;
    }

    public void setTypevalue(String typevalue) {
        this.typevalue = typevalue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }


    public String getFaceFeature() {
        return faceFeature;
    }

    public void setFaceFeature(String faceFeature) {
        this.faceFeature = faceFeature;
    }
}
