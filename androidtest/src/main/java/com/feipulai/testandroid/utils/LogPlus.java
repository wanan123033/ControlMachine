package com.feipulai.testandroid.utils;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by pengjf on 2019/3/27.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class LogPlus {
    private static int sCurrentLogLevel = Log.DEBUG;
    private static String sPrefix = null;

    @IntDef({ Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR })
    @Retention(RetentionPolicy.SOURCE)
    public @interface LogLevel {
    }

    /**
     * 初始化LogPlus，可选
     *
     * @param prefix Tag前缀，可以防止 RIL，IMS，AT，GSM，STK，CDMA，SMS 开头的log不会被打印
     * @param logLevel 打log等级
     */
    public static void init(String prefix, @LogLevel int logLevel) {
        if (prefix != null) {
            prefix = prefix.trim();
            if (prefix.length() > 0) {
                sPrefix = prefix;
            }
        }
        sCurrentLogLevel = logLevel;
    }

    public static void v(@Nullable String tag, String msg, @Nullable Throwable tr) {
        log(Log.VERBOSE, tag, msg, tr);
    }

    public static void v(@Nullable String tag, String msg) {
        log(Log.VERBOSE, tag, msg, null);
    }

    public static void v(String msg, @Nullable Throwable tr) {
        log(Log.VERBOSE, null, msg, tr);
    }

    public static void v(String msg) {
        log(Log.VERBOSE, null, msg, null);
    }

    public static void d(@Nullable String tag, String msg, @Nullable Throwable tr) {
        log(Log.DEBUG, tag, msg, tr);
    }

    public static void d(@Nullable String tag, String msg) {
        log(Log.DEBUG, tag, msg, null);
    }

    public static void d(String msg, @Nullable Throwable tr) {
        log(Log.DEBUG, null, msg, tr);
    }

    public static void d(String msg) {
        log(Log.DEBUG, null, msg, null);
    }

    public static void i(@Nullable String tag, String msg, @Nullable Throwable tr) {
        log(Log.INFO, tag, msg, tr);
    }

    public static void i(@Nullable String tag, String msg) {
        log(Log.INFO, tag, msg, null);
    }

    public static void i(String msg, @Nullable Throwable tr) {
        log(Log.INFO, null, msg, tr);
    }

    public static void i(String msg) {
        log(Log.INFO, null, msg, null);
    }

    public static void w(@Nullable String tag, String msg, @Nullable Throwable tr) {
        log(Log.WARN, tag, msg, tr);
    }

    public static void w(@Nullable String tag, String msg) {
        log(Log.WARN, tag, msg, null);
    }

    public static void w(String msg, @Nullable Throwable tr) {
        log(Log.WARN, null, msg, tr);
    }

    public static void w(String msg) {
        log(Log.WARN, null, msg, null);
    }

    public static void e(@Nullable String tag, String msg, @Nullable Throwable tr) {
        log(Log.ERROR, tag, msg, tr);
    }

    public static void e(@Nullable String tag, String msg) {
        log(Log.ERROR, tag, msg, null);
    }

    public static void e(String msg, @Nullable Throwable tr) {
        log(Log.ERROR, null, msg, tr);
    }

    public static void e(String msg) {
        log(Log.ERROR, null, msg, null);
    }

    private static void log(int logLevel, String tag, String msg, Throwable tr) {

        if (logLevel < sCurrentLogLevel) {
            return;
        }
        StackTraceElement e = Thread.currentThread().getStackTrace()[4];
        String fileName = e.getFileName();
        int lineNum = e.getLineNumber();
        String methodName = e.getMethodName();

        StringBuilder sb = new StringBuilder();

        sb.append(methodName)
                .append('(')
                .append(fileName)
                .append(':')
                .append(lineNum)
                .append(')')
                .append(msg);

        msg = sb.toString();

        sb.delete(0, sb.length());

        // 注意，如果tag是"IMS"开头的(还有其他)，log会不打印，所以加个"前缀_"
        // 参考这里 http://stackoverflow.com/a/36469141/5324526
        if (sPrefix != null) {
            sb.append(sPrefix).append('_');
        }

        if (tag == null || tag.length() == 0 || tag.trim().length() == 0) {
            String className = e.getClassName();
            int index = className.lastIndexOf('.') + 1;
            sb.append(className, index, className.length());
        } else {
            sb.append(tag);
        }

        tag = sb.toString();

        if (tr == null) {
            switch (logLevel) {
                case Log.VERBOSE:
                    Log.v(tag, msg);
                    break;
                case Log.DEBUG:
                    Log.d(tag, msg);
                    break;
                case Log.INFO:
                    Log.i(tag, msg);
                    break;
                case Log.WARN:
                    Log.w(tag, msg);
                    break;
                case Log.ERROR:
                    Log.e(tag, msg);
                    break;
            }
        } else {
            switch (logLevel) {
                case Log.VERBOSE:
                    Log.v(tag, msg, tr);
                    break;
                case Log.DEBUG:
                    Log.d(tag, msg, tr);
                    break;
                case Log.INFO:
                    Log.i(tag, msg, tr);
                    break;
                case Log.WARN:
                    Log.w(tag, msg, tr);
                    break;
                case Log.ERROR:
                    Log.e(tag, msg, tr);
                    break;
            }
        }
    }
}
