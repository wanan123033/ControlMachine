package com.feipulai.exam.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * created by ww on 2019/6/24.
 */
@Entity
public class ChipGroup {
    @Id(autoincrement = true)
    private Long id;
    @Unique
    private String colorGroupName;//颜色组名
    private int studentNo;//人数
    @Unique
    private int color;//组颜色
    private int groupType;//0正常组1备用组
    @Generated(hash = 1248248952)
    public ChipGroup(Long id, String colorGroupName, int studentNo, int color,
            int groupType) {
        this.id = id;
        this.colorGroupName = colorGroupName;
        this.studentNo = studentNo;
        this.color = color;
        this.groupType = groupType;
    }
    @Generated(hash = 865690056)
    public ChipGroup() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getColorGroupName() {
        return this.colorGroupName;
    }
    public void setColorGroupName(String colorGroupName) {
        this.colorGroupName = colorGroupName;
    }
    public int getStudentNo() {
        return this.studentNo;
    }
    public void setStudentNo(int studentNo) {
        this.studentNo = studentNo;
    }
    public int getColor() {
        return this.color;
    }
    public void setColor(int color) {
        this.color = color;
    }
    public int getGroupType() {
        return this.groupType;
    }
    public void setGroupType(int groupType) {
        this.groupType = groupType;
    }

    @Override
    public String toString() {
        return "ChipGroup{" +
                "id=" + id +
                ", colorGroupName='" + colorGroupName + '\'' +
                ", studentNo=" + studentNo +
                ", color=" + color +
                ", groupType=" + groupType +
                '}';
    }
}
