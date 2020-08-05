package com.feipulai.exam.activity.basketball;

import com.feipulai.common.utils.DateUtil;

/**
 * Created by zzs on  2019/11/15
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class TimeUtil {

    public static int[] getTestResult(long date) {
        int[] time = new int[4];
        if (date < 60 * 1000) {
            time[2] = Integer.valueOf(DateUtil.formatTime(date, "ss"));
            time[3] = Integer.valueOf(DateUtil.formatTime(date, "SSS"));
        } else if (date >= 60 * 1000 && date < 60 * 60 * 1000) { // 一小时之内
            time[1] = Integer.valueOf(DateUtil.formatTime(date, "mm"));
            time[2] = Integer.valueOf(DateUtil.formatTime(date, "ss"));
            time[3] = Integer.valueOf(DateUtil.formatTime(date, "SSS"));
        } else if (date >= 60 * 60 * 1000 && date < 60 * 60 * 24 * 1000) { // 同一天之内
            time[0] = Integer.valueOf(DateUtil.formatTime(date, "HH"));
            time[1] = Integer.valueOf(DateUtil.formatTime(date, "mm"));
            time[2] = Integer.valueOf(DateUtil.formatTime(date, "ss"));
            time[3] = Integer.valueOf(DateUtil.formatTime(date, "SSS"));
        } else {
            return null;
        }
        return time;
    }

    public static int[] getTestTime(long date) {
        int[] time = new int[4];
        if (date < 60 * 1000) {
            time[2] = Integer.valueOf(DateUtil.formatTime(date, "ss"));
            time[3] = Integer.valueOf(DateUtil.formatTime(date, "SS"));
        } else if (date >= 60 * 1000 && date < 60 * 60 * 1000) { // 一小时之内
            time[1] = Integer.valueOf(DateUtil.formatTime(date, "mm"));
            time[2] = Integer.valueOf(DateUtil.formatTime(date, "ss"));
            time[3] = Integer.valueOf(DateUtil.formatTime(date, "SS"));
        } else if (date >= 60 * 60 * 1000 && date < 60 * 60 * 24 * 1000) { // 同一天之内
            time[0] = Integer.valueOf(DateUtil.formatTime(date, "HH"));
            time[1] = Integer.valueOf(DateUtil.formatTime(date, "mm"));
            time[2] = Integer.valueOf(DateUtil.formatTime(date, "ss"));
            time[3] = Integer.valueOf(DateUtil.formatTime(date, "SS"));
        } else {
            return null;
        }
        return time;
    }
}
