package com.feipulai.device.tcp;

import android.util.Log;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * created by ww on 2019/6/4.
 */
public class TcpConfig {

    public static final byte[] CMD_NOTHING = {(byte) 0x00};
    /**
     * 设备连接返回包
     */
    public static final String CMD_CONNECT_RECEIVE = "a1000ab201020304fff8";
    //设备连接命令
    public static final byte[] CMD_CONNECT = {(byte) 0xa0, (byte) 0x00, (byte) 0x0A, (byte) 0xc5, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0xff, (byte) 0xf8};
    /**
     * 同步时间  包头(1)+包长(2)+命令标识(1)+时间(9)+包尾(2)，包长15字节
     */
    public static final byte[] CMD_UPDATE_DATE = {(byte) 0xa0, (byte) 0x00, (byte) 0x0f, (byte) 0xc2, (byte) 0xff, (byte) 0xf8};
    /**
     * 开始计时 包头(1)+包长(2)+命令标识(1)+时间(9)+包尾(2)，包长15字节
     */
    private static final byte[] CMD_START_TIMING = {(byte) 0xa0, (byte) 0x00, (byte) 0x0f, (byte) 0xc3, (byte) 0xff, (byte) 0xf8};
    /**
     * 结束计时 包头(1)+包长(2)+命令标识(1)+时间(9)+包尾(2)，包长15字节
     */
    private static final byte[] CMD_END_TIMING = {(byte) 0xa0, (byte) 0x00, (byte) 0x0f, (byte) 0xc4, (byte) 0xff, (byte) 0xf8};

    public static byte[] getCmdUpdateDate() {
        return getTimingCMD(CMD_UPDATE_DATE);
    }

    public static byte[] getCmdStartTiming() {
        return getTimingCMD(CMD_START_TIMING);
    }

    public static byte[] getCmdEndTiming() {
        return getTimingCMD(CMD_END_TIMING);
    }


    /**
     * 需要发送的指令 将时间拼接到固定位置
     *
     * @param firstByte
     * @return
     */
    private static byte[] getTimingCMD(byte[] firstByte) {
        byte[] currentDateByte = dateToByte();
        Log.i("currentDateByte", Arrays.toString(currentDateByte));
        byte[] result = Arrays.copyOf(firstByte, firstByte.length + currentDateByte.length);
        System.arraycopy(currentDateByte, 0, result, 4, currentDateByte.length);
        Log.i("getTimingCMD", "------" + bytesToHex(result));
        return result;
    }

    /**
     * 字节数组转16进制
     *
     * @param bytes 需要转换的byte数组
     * @return 转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * byte数组转换成16进制字符数组
     *
     * @param src
     * @return
     */
    public static String[] bytesToHexStrings(byte[] src) {
        if (src == null || src.length <= 0) {
            return null;
        }
        String[] str = new String[src.length];

        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                str[i] = "0" + hv;
            } else {
                str[i] = hv;
            }
        }
        return str;
    }

    private static final Calendar cal = Calendar.getInstance();
    private static int[] dates;

    /**
     * 获取当前系统时间的年份
     *
     * @return
     */
    private static int[] getDateInt() {
        cal.setTime(new Date());
        dates = new int[7];
        dates[0] = cal.get(Calendar.YEAR);
        dates[1] = cal.get(Calendar.MONTH) + 1;
        dates[2] = cal.get(Calendar.DATE);
        dates[3] = cal.get(Calendar.HOUR_OF_DAY);
        dates[4] = cal.get(Calendar.MINUTE);
        dates[5] = cal.get(Calendar.SECOND);
        dates[6] = cal.get(Calendar.MILLISECOND);
        Log.i("dates", Arrays.toString(dates));
        return dates;
    }

    /**
     * 当前时间转换16进制数组CMD命令
     *
     * @return
     */
    public static byte[] dateToByte() {
        getDateInt();
        byte[] bytes = new byte[9];
        String year = Integer.toHexString(dates[0]);
        Log.i("year", "-----------" + year);
        if (year.length() > 3) {
            bytes[0] = Integer.valueOf(year.substring(0, 2), 16).byteValue();
            bytes[1] = Integer.valueOf(year.substring(2, year.length()), 16).byteValue();
        } else {
            bytes[0] = Integer.valueOf(year.substring(0, 1), 16).byteValue();
            bytes[1] = Integer.valueOf(year.substring(1, year.length()), 16).byteValue();
        }

        for (int i = 2; i < 7; i++) {
            bytes[i] = Integer.valueOf(Integer.toHexString(dates[i - 1]), 16).byteValue();
        }

        String ms = Integer.toHexString(dates[6]);
        if (ms.length() > 2) {
            bytes[7] = Integer.valueOf(ms.substring(0, 1), 16).byteValue();
            bytes[8] = Integer.valueOf(ms.substring(1, year.length()), 16).byteValue();
        } else {
            bytes[7] = 0;
            bytes[8] = Integer.valueOf(ms, 16).byteValue();
        }
        return bytes;
    }

//    private static String intToHex(int n) {
//        StringBuilder sb = new StringBuilder(8);
//        String a;
//        char[] b = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
//        while (n != 0) {
//            sb = sb.append(b[n % 16]);
//            n = n / 16;
//        }
//        a = sb.reverse().toString().isEmpty() ? "0" : sb.reverse().toString();
//        return a;
//    }

    /**
     * @param date
     */
    public static long getDateFromCMD(int[] date) {
        cal.set(Calendar.YEAR, date[0]);
        cal.set(Calendar.MONTH, date[1] - 1);
        cal.set(Calendar.DATE, date[2]);
        cal.set(Calendar.HOUR_OF_DAY, date[3]);
        cal.set(Calendar.MINUTE, date[4]);
        cal.set(Calendar.SECOND, date[5]);
        cal.set(Calendar.MILLISECOND, date[6]);
        return cal.getTimeInMillis();
    }
}
