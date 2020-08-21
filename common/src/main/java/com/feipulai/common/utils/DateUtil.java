package com.feipulai.common.utils;

import android.app.AlarmManager;
import android.content.Context;

import com.feipulai.device.udp.UdpLEDUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by zzs on  2019/3/8
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class DateUtil {
    /**
     * 获取当前时间
     * <h3>Version</h3> 1.0
     * <h3>CreateTime</h3> 2017/10/13,14:05
     * <h3>UpdateTime</h3> 2017/10/13,14:05
     * <h3>CreateAuthor</h3> zzs
     * <h3>UpdateAuthor</h3>
     * <h3>UpdateInfo</h3> (此处输入修改内容,若无修改可不写.)
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }
    /**
     * 获取当前时间(按指定格式).
     * <h3>Version</h3> 1.0
     * <h3>CreateTime</h3> 2017/10/13,14:05
     * <h3>UpdateTime</h3> 2017/10/13,14:05
     * <h3>CreateAuthor</h3> zzs
     * <h3>UpdateAuthor</h3>
     * <h3>UpdateInfo</h3> (此处输入修改内容,若无修改可不写.)
     */
    public static String getCurrentTime(String pattern) {
        return formatTime(getCurrentTime(), pattern);
    }
    /**
     * 获取当前时间(按指定格式).
     * <h3>Version</h3> 1.0
     * <h3>CreateTime</h3> 2017/10/13,14:05
     * <h3>UpdateTime</h3> 2017/10/13,14:05
     * <h3>CreateAuthor</h3> zzs
     * <h3>UpdateAuthor</h3>
     * <h3>UpdateInfo</h3> (此处输入修改内容,若无修改可不写.)
     */
    public static String getCurrentTime2(String pattern) {
        return formatTime2(getCurrentTime(), pattern);
    }
    /**
    /**
     * 获取指定时间格式的时间戳
     *
     * @param time    时间
     * @param pattern 格式
     * @return 时间戳
     */
    public static long getTimeMillis(String time, String pattern) {

        Date date = null;
        try {
            date = new SimpleDateFormat(pattern).parse(time);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }
        return date.getTime();
    }
    /**
     * 格式化时间
     *
     * @param timeMillis 时间戳
     * @param pattern    时间正则
     * @return 返回格式后的时间
     */
    public static String formatTime(long timeMillis, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));//计时时间换算不需要加8
        return sdf.format(new Date(timeMillis));
    }

    public static String formatTime2(long timeMillis, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));//时钟时间换算需要加8
        return sdf.format(new Date(timeMillis));
    }

    public static String formatTime3(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date date = sdf.parse(time);
            return String.valueOf(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 获取时间差
     *
     * @return
     */
    public static String getDeltaT(long time) {
        int min = (int) ((time) / 60000);
        int sec = (int) ((time - min * 60000) / 1000);
        int longmill = (int) (time - min * 60000 - sec * 1000);
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
     *
     * @param digital   1 十分位 2 百分位
     * @param carryMode 0
     */
    public static String caculateTime(long caculTime, int digital, int carryMode) {
        double bigTime = Double.valueOf(caculTime) / 1000;
        //需要先舍掉小数位数后一位之后的所有，再进行进位
        BigDecimal bigDecimal = new BigDecimal(String.valueOf(bigTime)).setScale(digital + 1, BigDecimal.ROUND_DOWN);
        long carryTime;
        switch (carryMode) {
            case 0://不去舍
                carryTime = caculTime;
                break;
            case 1://四舍五入
                carryTime = bigDecimal.setScale(digital, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(1000d)).longValue();
                break;
            case 2: //舍位
//                String pattern = "#.";
//                for (int i = 0; i < digital; i++) {
//                    pattern += "0";
//                }
//                double formatTime = Double.valueOf(new DecimalFormat(pattern).format(bigTime));

                NumberFormat nf = NumberFormat.getNumberInstance();
                // 保留两位小数
                nf.setMaximumFractionDigits(digital);
                // 如果不需要四舍五入，可以使用RoundingMode.DOWN
                nf.setRoundingMode(RoundingMode.DOWN);
                double formatTime = Double.valueOf(nf.format(bigTime).replaceAll(",", ""));
                carryTime = BigDecimal.valueOf(formatTime).multiply(new BigDecimal(1000d)).longValue();
                break;
            case 3://非0进位
                carryTime = bigDecimal.setScale(digital, BigDecimal.ROUND_UP).multiply(new BigDecimal(1000d)).longValue();
                break;
            default:
                carryTime = caculTime;
                break;
        }
        return caculateFormatTime(carryTime, digital);
//        if (carryTime < 60 * 1000) {
//            return formatTime(carryTime, "ss." + (digital == 1 ? "S" : "SS"));
//        } else if (caculTime >= 60 * 1000 && caculTime < 60 * 60 * 1000) { // 一小时之内
//            return formatTime(carryTime, "mm:ss." + (digital == 1 ? "S" : "SS"));
//        } else if (caculTime >= 60 * 60 * 1000 && caculTime < 60 * 60 * 24 * 1000) { // 同一天之内
//            return formatTime(carryTime, "HH:mm:ss." + (digital == 1 ? "S" : "SS"));
//        } else {
//            return formatTime(carryTime, "dd HH:mm:ss." + (digital == 1 ? "S" : "SS"));
//        }
    }

    /**
     * 时间计算
     * <h3>Version</h3> 1.0
     * <h3>CreateTime</h3> 2017/10/18,16:15
     * <h3>UpdateTime</h3> 2017/10/18,16:15
     * <h3>CreateAuthor</h3> zzs
     * <h3>UpdateAuthor</h3>
     * <h3>UpdateInfo</h3> (此处输入修改内容,若无修改可不写.)
     *
     * @param digital 1 百分位 2 十分位
     */
    public static String caculateFormatTime(long caculTime, int digital) {
        String hundDigital;
        switch (digital) {
            case 1:
                hundDigital = ".S";
                break;
            case 2:
                hundDigital = ".SS";
                break;
            case 3:
                hundDigital = ".SSS";
                break;
            default:
                hundDigital = "";
                break;
        }
        if (caculTime < 60 * 1000) {
            return formatTime(caculTime, "ss" + hundDigital);
        } else if (caculTime >= 60 * 1000 && caculTime < 60 * 60 * 1000) { // 一小时之内
            return formatTime(caculTime, "mm:ss" + hundDigital);
        } else if (caculTime >= 60 * 60 * 1000 && caculTime < 60 * 60 * 24 * 1000) { // 同一天之内
            return formatTime(caculTime, "HH:mm:ss" + hundDigital);
        } else {
            return formatTime(caculTime, "dd HH:mm:ss" + hundDigital);
        }
    }

    /**
     * 设置时区 Asia/Shangha
     * @param context
     * @param timeZone
     */
    public static void setTimeZone(Context context, String timeZone) {
        final Calendar now = Calendar.getInstance();
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        LogUtil.logDebugMessage("设置时区名=====》" + tz.getDisplayName());
//        now.setTimeZone(tz);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setTimeZone(timeZone);
    }

    public static String getDefaultTimeZone() {
        return TimeZone.getDefault().getDisplayName();
    }



    public static void setSysDate(Context mContext, long time) {
//        String curr_time = "20160606.120403";
//        UdpLEDUtil.shellExec("/system/bin/date -s " + curr_time+"\n clock -w\n");
//        String curr_time = "052514412019.52";
        String curr_time =formatTime(time,"MMddhhmmyyyy.ss");
        LogUtil.logDebugMessage(curr_time);
        UdpLEDUtil.shellExec("date " +
                curr_time  + "\n busybox hwclock -w \n");

    }
}
