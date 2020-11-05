package com.feipulai.exam.activity.data;

import java.io.Serializable;

/**
 * Created by zzs on  2020/7/23
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class DownLoadPhotoHeaders implements Serializable {

    private  int pageNo;
    private int batchTotal;
    private String uploadTime;

    public void setInit(int pageNo, int batchTotal, String uploadTime) {
        this.pageNo = pageNo;
        this.batchTotal = batchTotal;
        this.uploadTime = uploadTime;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getBatchTotal() {
        return batchTotal;
    }

    public void setBatchTotal(int batchTotal) {
        this.batchTotal = batchTotal;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }
}
