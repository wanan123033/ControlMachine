package com.feipulai.exam.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 日程
 * Created by zzs on  2018/12/25
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
@Entity
public class Schedule {
    @Id(autoincrement = true)
    private Long id;
    @Unique
    @NotNull
    private String scheduleNo; //日程序号 （场次）
    @NotNull
    private String beginTime;
    private String endTime;
    @Generated(hash = 478487951)
    public Schedule(Long id, @NotNull String scheduleNo, @NotNull String beginTime,
            String endTime) {
        this.id = id;
        this.scheduleNo = scheduleNo;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }
    @Generated(hash = 729319394)
    public Schedule() {
    }

    public Schedule(String scheduleNo, String beginTime, String endTime) {
        this.scheduleNo = scheduleNo;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }

    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getScheduleNo() {
        return this.scheduleNo;
    }
    public void setScheduleNo(String scheduleNo) {
        this.scheduleNo = scheduleNo;
    }
    public String getBeginTime() {
        return this.beginTime;
    }
    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }
    public String getEndTime() {
        return this.endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    
    
}
