package com.feipulai.exam.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by zzs on  2019/3/8
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class DateUtil {

    /**
     * 格式化时间
     *
     * @param timeMillis 时间戳
     * @param pattern    时间正则
     * @return 返回格式后的时间
     */
    public static String formatTime(long timeMillis, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(new Date(timeMillis));
    }

    public static String getDeltaT2(long startTime) {
        long timeUsedInsec = System.currentTimeMillis() - startTime;
//        SimpleDateFormat sf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒SSS毫秒");
//        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd- HH:mm:ss.SSS", Locale.CHINA);
        SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.CHINA);
        Date now = new Date(timeUsedInsec);
        String str = sf.format(now);
        return str;
    }

    /**
     * 获取时间差
     *
     * @return
     */
    public static String getDeltaT(long startTime) {
        long timeUsedInsec = System.currentTimeMillis() - startTime;
        int min = (int) ((timeUsedInsec) / 60000);
        int sec = (int) ((timeUsedInsec - min * 60000) / 1000);
        int longmill = (int) (timeUsedInsec - min * 60000 - sec * 1000);
        String string = "";
        if (longmill < 991 && longmill > 90) {
            if (longmill % 10 == 0) {
                string = getMin(min, 0) + ":" + getSec(sec, 0) + "." + longmill / 10;
            } else {
                string = getMin(min, 0) + ":" + getSec(sec, 0) + "." + (longmill / 10 + 1);
            }
        } else if (longmill < 91) {
            if (longmill % 10 == 0) {
                string = getMin(min, 0) + ":" + getSec(sec, 0) + "." + "0" + longmill / 10;
            } else {
                string = getMin(min, 0) + ":" + getSec(sec, 0) + "." + "0" + (longmill / 10 + 1);
            }
        } else {
            if (sec < 59) {
                string = getMin(min, 0) + ":" + getSec(sec, 1) + "." + "00";
            } else {
                string = getMin(min, 1) + ":" + "00" + "." + "00";
            }
        }
        return string;
    }

    private static String getMin(int min, int n) {
        return (min + n) < 10 ? "0" + (min + n) : (min + n) + "";
    }

    private static String getSec(int sec, int n) {
        return (sec + n) < 10 ? "0" + (sec + n) : (sec + n) + "";
    }


    /**
     * 时间计算
     * <h3>Version</h3> 1.0
     * <h3>CreateTime</h3> 2017/10/18,16:15
     * <h3>UpdateTime</h3> 2017/10/18,16:15
     * <h3>CreateAuthor</h3> zzs
     * <h3>UpdateAuthor</h3>
     * <h3>UpdateInfo</h3> (此处输入修改内容,若无修改可不写.)
     */
    public static String caculateTime(long caculTime) {
        if (caculTime < 60 * 1000) {
            return formatTime(caculTime, "ss.SS");
        } else if (caculTime >= 60 * 1000 && caculTime < 60 * 60 * 1000) { // 一小时之内
            return formatTime(caculTime, "mm:ss.SS");
        } else if (caculTime >= 60 * 60 * 1000 && caculTime < 60 * 60 * 24 * 1000) { // 同一天之内
            return formatTime(caculTime, "HH:mm:ss.SS");
        } else {
            return formatTime(caculTime, "dd HH:mm:ss.SS");
        }

    }
}
