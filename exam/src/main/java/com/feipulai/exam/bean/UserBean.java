package com.feipulai.exam.bean;

import java.io.Serializable;

/**
 * 用户信息
 * Created by zzs on  2018/12/29
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class UserBean implements Serializable {
    private String token;
    private String examName;//考试名称
    private String siteId; //考点ID
    private String examPersonnelPhotoSuffix;
    private String[] permission;
    private String type;
    private String examPersonnelPhotoData;
    private int equipmentstatus;
    private String examPersonnelId;
    private int examPersonnelPhotoType;
    private String typevalue;
    private String[] deviceIds;

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

    @Override
    public String toString() {
        return "UserBean{" +
                "token='" + token + '\'' +
                ", examName='" + examName + '\'' +
                '}';
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

    public String[] getPermission() {
        return permission;
    }

    public void setPermission(String[] permission) {
        this.permission = permission;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getFileName() {
        return examPersonnelId + "." + examPersonnelPhotoSuffix;
    }

    public String[] getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(String[] deviceIds) {
        this.deviceIds = deviceIds;
    }
}
