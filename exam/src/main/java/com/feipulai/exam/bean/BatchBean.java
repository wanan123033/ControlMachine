package com.feipulai.exam.bean;

import java.io.Serializable;

/**
 * Created by zzs on  2019/7/26
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BatchBean<T> implements Serializable {

    private int batch;
    private int batchTotal;
    private int examType;
    private T dataInfo;


    public int getBatch() {
        return batch;
    }

    public void setBatch(int batch) {
        this.batch = batch;
    }

    public int getBatchTotal() {
        return batchTotal;
    }

    public void setBatchTotal(int batchTotal) {
        this.batchTotal = batchTotal;
    }

    public int getExamType() {
        return examType;
    }

    public void setExamType(int examType) {
        this.examType = examType;
    }

    public T getDataInfo() {
        return dataInfo;
    }

    public void setDataInfo(T dataInfo) {
        this.dataInfo = dataInfo;
    }

    @Override
    public String toString() {
        return "BatchBean{" +
                "batch=" + batch +
                ", batchTotal=" + batchTotal +
                ", examType=" + examType +
                ", dataInfo=" + dataInfo +
                '}';
    }
}
