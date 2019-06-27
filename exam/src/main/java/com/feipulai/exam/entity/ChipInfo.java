package com.feipulai.exam.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

/**
 * created by ww on 2019/6/24.
 */
@Entity(
        indexes = {
                @Index(value = "colorGroupName ASC,vestNo ASC", unique = true)
        }
)
public class ChipInfo {
    @Id(autoincrement = true)
    private Long id;
    private String colorGroupName;//颜色组名
    private int color;
    private int vestNo;//背心号
    private String chipID1;//芯片ID1
    private String chipID2;//芯片ID2
@Generated(hash = 1914600295)
public ChipInfo(Long id, String colorGroupName, int color, int vestNo,
        String chipID1, String chipID2) {
    this.id = id;
    this.colorGroupName = colorGroupName;
    this.color = color;
    this.vestNo = vestNo;
    this.chipID1 = chipID1;
    this.chipID2 = chipID2;
}
@Generated(hash = 1524822476)
public ChipInfo() {
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
public int getColor() {
    return this.color;
}
public void setColor(int color) {
    this.color = color;
}
public int getVestNo() {
    return this.vestNo;
}
public void setVestNo(int vestNo) {
    this.vestNo = vestNo;
}
public String getChipID1() {
    return this.chipID1;
}
public void setChipID1(String chipID1) {
    this.chipID1 = chipID1;
}
public String getChipID2() {
    return this.chipID2;
}
public void setChipID2(String chipID2) {
    this.chipID2 = chipID2;
}

}
