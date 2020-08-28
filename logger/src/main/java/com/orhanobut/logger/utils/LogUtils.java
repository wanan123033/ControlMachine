package com.orhanobut.logger.utils;

import android.os.Environment;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.EncryptDiskLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.examlogger.CrashLogAdapter;
import com.orhanobut.logger.examlogger.NormalLogAdapter;
import com.orhanobut.logger.examlogger.OperaLogAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by James on 2019/2/15 0015.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class LogUtils {
    public static final String ALL_TAG = "ALL_TAG";                 //所有日志TAG
    public static final String NORMAL_TAG = "NORMAL_TAG";       //正常日志
    public static final String OPERATION_TAG = "OPERATION_TAG"; //操作日志TAG
    public static final String CRASH_TAG = "CRASH_TAG";
    public static final String LOG_ENCRYPT_KEY = "19834762";
    /**
     * 应用根目录
     */
    public static final String PATH_BASE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ControlMachine/";

    //初始化日志工具
    public static void initLogger(final boolean logToConsole, boolean logToRaw, String pathName) {
        //日志打印到控制台,在发布release版本时，会自动不打印
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return logToConsole;
            }
        });

        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd", Locale.CHINA);
        String logFileName = dateFormat.format(Calendar.getInstance().getTime()) + ".txt";
        // 非加密日志存储在在sd卡中“logger”目录中
        String diskLogFilePath = Environment.getExternalStorageDirectory() + pathName + logFileName;
        Logger.addLogAdapter(new DiskLogAdapter(diskLogFilePath));

        //加密日志存储
        String encryptLogFilePath = PATH_BASE + "/fair/play/" + logFileName;
        Logger.addLogAdapter(new EncryptDiskLogAdapter(encryptLogFilePath, LOG_ENCRYPT_KEY));

        // 保存正常日志
        String exam_normal = Environment.getExternalStorageDirectory() + pathName + "/examlogger/" + "exam_normal_" + logFileName;
        Logger.addLogAdapter(new DiskLogAdapter(new NormalLogAdapter(exam_normal)));
        // 保存操作日志
        String exam_operation = Environment.getExternalStorageDirectory() + pathName + "/examlogger/" + "exam_operation_" + logFileName;
        Logger.addLogAdapter(new DiskLogAdapter(new OperaLogAdapter(exam_operation)));

        String exam_crash = Environment.getExternalStorageDirectory() + pathName + "/examlogger/" + "exam_crash_" + logFileName;
        Logger.addLogAdapter(new DiskLogAdapter(new CrashLogAdapter(exam_crash)));


//		String rawLogFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/raw_log/" + logFileName;
//		if(logToRaw){
//			Logger.rawLogToFile(rawLogFilePath);
//		}


    }

    /**
     * 发送接收日志打印
     *
     * @param message
     */
    public static void normal(String message) {
        Logger.t(NORMAL_TAG).i(message);
    }

    /**
     * 用户操作日志打印
     *
     * @param message
     */
    public static void operation(String message) {
        Logger.t(OPERATION_TAG).i(message);
    }

    /**
     * Activity 生命周期打印
     *
     * @param message
     */
    public static void life(String message) {
        Logger.t(ALL_TAG).i(message);
    }

    /**
     * 过滤指令日志打印
     *
     * @param message
     */
    public static void all(String message) {
        Logger.t(ALL_TAG).i(message);
    }

    public static void crash(String message) {
        Logger.t(CRASH_TAG).i(message);
    }
}
