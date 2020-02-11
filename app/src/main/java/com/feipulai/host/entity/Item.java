package com.feipulai.host.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2017/11/24.
 */
@Entity
public class Item {

    @Id(autoincrement = true)
    private Long id;//id
    @Unique
    private String itemCode;// 身高体重的项目代码为固定 身高 ---- E01  体重 ---- E02
	private int machineCode;//机器代码   一个机器码可能对应多个项目代码,所以机器码不能为唯一字段
	@Unique
    @NotNull
    private String itemName;//项目名称
    private int minValue;//最小值
    private int maxValue;//最大值
    //Note: 数据库中数据单位与该列值无关,固定为"毫米(mm)"、"毫秒(ms)"、"克(g)"、"次","毫升"
    private String unit;//单位    目前取值范围为"厘米"、"千克"、"毫升"、"秒"、"次""、"分'秒"、"米"
    private int digital;//保留小数位数
    private int testNum;//项目测试次数	表示一次检录的测试次数	默认1次检录测试的次数，检录次数不限
    private int carryMode;//进位方式	不取舍,四舍五入,非零进一,非零舍去  与显示成绩有关
    private int itemType;    //项目类型(计时,计数,远度,计时),但是暂时不用
    private int fResultType;//最终成绩选择模式 （1.最后成绩，2.补考成绩，3.最好）

    private String remark1;//备注1
    private String remark2;//备注2
    private String remark3;//备注3

    @Generated(hash = 1247795045)
    public Item(Long id, String itemCode, int machineCode, @NotNull String itemName,
            int minValue, int maxValue, String unit, int digital, int testNum,
            int carryMode, int itemType, int fResultType, String remark1,
            String remark2, String remark3) {
        this.id = id;
        this.itemCode = itemCode;
        this.machineCode = machineCode;
        this.itemName = itemName;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.unit = unit;
        this.digital = digital;
        this.testNum = testNum;
        this.carryMode = carryMode;
        this.itemType = itemType;
        this.fResultType = fResultType;
        this.remark1 = remark1;
        this.remark2 = remark2;
        this.remark3 = remark3;
    }
    @Generated(hash = 1470900980)
    public Item() {
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
    public int getMachineCode() {
        return this.machineCode;
    }
    public void setMachineCode(int machineCode) {
        this.machineCode = machineCode;
    }
    public String getItemName() {
        return this.itemName;
    }
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    public int getMinValue() {
        return this.minValue;
    }
    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }
    public int getMaxValue() {
        return this.maxValue;
    }
    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }
    public String getUnit() {
        return this.unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
    public int getDigital() {
        return this.digital;
    }
    public void setDigital(int digital) {
        this.digital = digital;
    }
    public int getTestNum() {
        return this.testNum;
    }
    public void setTestNum(int testNum) {
        this.testNum = testNum;
    }
    public int getCarryMode() {
        return this.carryMode;
    }
    public void setCarryMode(int carryMode) {
        this.carryMode = carryMode;
    }
    public int getItemType() {
        return this.itemType;
    }
    public void setItemType(int itemType) {
        this.itemType = itemType;
    }
    public int getFResultType() {
        return this.fResultType;
    }
    public void setFResultType(int fResultType) {
        this.fResultType = fResultType;
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
    
    public int getfResultType() {
        return this.fResultType;
    }
    public void setfResultType(int fResultType) {
        this.fResultType = fResultType;
    }
    
    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", itemCode='" + itemCode + '\'' +
                ", machineCode=" + machineCode +
                ", itemName='" + itemName + '\'' +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                ", unit='" + unit + '\'' +
                ", digital=" + digital +
                ", testNum=" + testNum +
                ", carryMode=" + carryMode +
                ", itemType=" + itemType +
                ", fResultType=" + fResultType +
                ", remark1='" + remark1 + '\'' +
                ", remark2='" + remark2 + '\'' +
                ", remark3='" + remark3 + '\'' +
                '}';
    }
}
