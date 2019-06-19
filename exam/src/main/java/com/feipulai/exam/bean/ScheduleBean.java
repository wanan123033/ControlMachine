package com.feipulai.exam.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 日程
 * Created by zzs on  2018/12/25
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ScheduleBean implements Serializable {
    public static int SITE_EXAMTYPE = 0;

    private String siteName;
    private String siteCode;
    private int examType;//0 个人，分组
    private List<ResponseSchedule> siteScheduleInfoVOList;

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public int getExamType() {
        return examType;
    }

    public void setExamType(int examType) {
        this.examType = examType;
    }

    public List<ResponseSchedule> getSiteScheduleInfoVOList() {
        return siteScheduleInfoVOList;
    }

    public void setSiteScheduleInfoVOList(List<ResponseSchedule> siteScheduleInfoVOList) {
        this.siteScheduleInfoVOList = siteScheduleInfoVOList;
    }

    public static class ResponseSchedule implements Serializable {
        private String scheduleNo; //日程序号 （场次）
        private String beginTime;
        private String endTime;

        private List<ItemBean> examItemVOList;

        public String getScheduleNo() {
            return scheduleNo;
        }

        public void setScheduleNo(String scheduleNo) {
            this.scheduleNo = scheduleNo;
        }

        public String getBeginTime() {
            return beginTime;
        }

        public void setBeginTime(String beginTime) {
            this.beginTime = beginTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public List<ItemBean> getExamItemVOList() {
            return examItemVOList;
        }

        public void setExamItemVOList(List<ItemBean> examItemVOList) {
            this.examItemVOList = examItemVOList;
        }

        @Override
        public String toString() {
            return "ResponseSchedule{" +
                    "scheduleNo='" + scheduleNo + '\'' +
                    ", beginTime='" + beginTime + '\'' +
                    ", endTime='" + endTime + '\'' +
                    ", examItemVOList=" + examItemVOList +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ScheduleBean{" +
                "siteName='" + siteName + '\'' +
                ", siteCode='" + siteCode + '\'' +
                ", examType=" + examType +
                ", siteScheduleInfoVOList=" + siteScheduleInfoVOList +
                '}';
    }
}
