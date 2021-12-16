package com.feipulai.host.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class StudentFace {
    @Id(autoincrement = true)
    private Long id;
    @Unique
    @NotNull
    private String studentCode;//考号
    private String faceFeature;
    @Generated(hash = 179770560)
    public StudentFace(Long id, @NotNull String studentCode, String faceFeature) {
        this.id = id;
        this.studentCode = studentCode;
        this.faceFeature = faceFeature;
    }
    @Generated(hash = 1582801354)
    public StudentFace() {
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
    public String getFaceFeature() {
        return this.faceFeature;
    }
    public void setFaceFeature(String faceFeature) {
        this.faceFeature = faceFeature;
    }
}
