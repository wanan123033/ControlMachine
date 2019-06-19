package com.feipulai.exam.exl;

import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by zzs on  2018/12/12
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ExelGroupReadBean extends ExelReadBean {


    private int groupSex;//分组性别
    private String tranches;//组别
    private int groupNo;//组号（分组）
    private String scheduleTime;//日程时间(开始时间)
    private int trackNo;//道次




    public int getGroupSex() {
        return groupSex;
    }

    public void setGroupSex(int groupSex) {
        this.groupSex = groupSex;
    }

    public String getTranches() {
        return tranches;
    }

    public void setTranches(String tranches) {
        this.tranches = tranches;
    }

    public int getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(int groupNo) {
        this.groupNo = groupNo;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public int getTrackNo() {
        return trackNo;
    }

    public void setTrackNo(int trackNo) {
        this.trackNo = trackNo;
    }
}
