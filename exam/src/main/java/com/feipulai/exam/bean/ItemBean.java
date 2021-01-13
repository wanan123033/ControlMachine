package com.feipulai.exam.bean;

import java.io.Serializable;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2017/11/24.
 */
public class ItemBean implements Serializable {
    private String examItemCode;
    private String machineCode;//机器代码   一个机器码可能对应多个项目代码,所以机器码不能为唯一字段
    private String itemName;//项目名称
    private int minResult;//最小值
    private int maxResult;//最大值
    //Note: 数据库中数据单位与该列值无关,固定为"毫米(mm)"、"毫秒(ms)"、"克(g)"、"次","毫升"
    private String resultUnit;//单位    目前取值范围为"厘米"、"千克"、"毫升"、"秒"、"次""、"分'秒"、"米"
    private int decimalDigits;//保留小数位数
    private int resultTestNum;//项目测试次数	表示一次检录的测试次数	默认1次检录测试的次数，检录次数不限
    private int carryMode;//进位方式	不取舍,四舍五入,非零进一,非零舍去  与显示成绩有关 （0.不去舍，1.四舍五入 2.舍位 3.非零进取）
    private int testType;    //项目测量方式,计时，计数，远度，力量，,但是暂时不用
    private int lastResultMode;//最终成绩选择模式 （1.最后成绩，2.补考成绩，3.最好）    该项只在成绩上传时使用

    public String getItemName() {
        return this.itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }


    public int getCarryMode() {
        return this.carryMode;
    }

    public void setCarryMode(int carryMode) {
        this.carryMode = carryMode;
    }

    public String getMachineCode() {
        return this.machineCode;
    }

    public void setMachineCode(String machineCode) {
        this.machineCode = machineCode;
    }


    public int getTestType() {
        return testType;
    }

    public void setTestType(int testType) {
        this.testType = testType;
    }

    public int getLastResultMode() {
        return lastResultMode;
    }

    public void setLastResultMode(int lastResultMode) {
        this.lastResultMode = lastResultMode;
    }

    public String getExamItemCode() {
        return examItemCode;
    }

    public void setExamItemCode(String examItemCode) {
        this.examItemCode = examItemCode;
    }

    public int getMinResult() {
        return minResult;
    }

    public void setMinResult(int minResult) {
        this.minResult = minResult;
    }

    public int getMaxResult() {
        return maxResult;
    }

    public void setMaxResult(int maxResult) {
        this.maxResult = maxResult;
    }

    public String getResultUnit() {
        return resultUnit;
    }

    public void setResultUnit(String resultUnit) {
        this.resultUnit = resultUnit;
    }

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    public int getResultTestNum() {
        return resultTestNum;
    }

    public void setResultTestNum(int resultTestNum) {
        this.resultTestNum = resultTestNum;
    }

    @Override
    public String toString() {
        return "ItemBean{" +
                "examItemCode='" + examItemCode + '\'' +
                ", machineCode=" + machineCode +
                ", itemName='" + itemName + '\'' +
                ", minResult=" + minResult +
                ", maxResult=" + maxResult +
                ", resultUnit='" + resultUnit + '\'' +
                ", decimalDigits=" + decimalDigits +
                ", resultTestNum=" + resultTestNum +
                ", carryMode=" + carryMode +
                ", testType=" + testType +
                ", lastResultMode=" + lastResultMode +
                '}';
    }
}
