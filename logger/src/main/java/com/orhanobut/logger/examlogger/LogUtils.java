package com.orhanobut.logger.examlogger;

import android.os.Environment;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class LogUtils {
    public static final String ALL_TAG = "ALL_TAG";                 //所有日志TAG
    public static final String UNNORMAL_TAG = "UNNORMAL_TAG";    //被过滤的指令日志
    public static final String NORMAL_TAG = "NORMAL_TAG";       //正常日志
    public static final String OPERATION_TAG = "OPERATION_TAG"; //操作日志TAG
    public static final String LIFE_TAG = "LIFE_TAG"; //生命周期日志TAG

    public static void initLogger(final boolean isConsole) {
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return isConsole;
            }
        });
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd", Locale.CHINA);
        String logFileName = dateFormat.format(Calendar.getInstance().getTime()) + ".txt";
        // 保存所有日志
        String exam_all = Environment.getExternalStorageDirectory() + "/logger/" + "exam_all_" + logFileName;
        Logger.addLogAdapter(new DiskLogAdapter(new AllLogAdapter(exam_all)));
        // 保存非正常日志
        String exam_unnormal = Environment.getExternalStorageDirectory() + "/logger/" + "exam_unnormal_" + logFileName;
        Logger.addLogAdapter(new DiskLogAdapter(new UnNormalLogAdapter(exam_unnormal)));
        // 保存正常日志
        String exam_normal = Environment.getExternalStorageDirectory() + "/logger/" + "exam_normal_" + logFileName;
        Logger.addLogAdapter(new DiskLogAdapter(new NormalLogAdapter(exam_normal)));
        // 保存操作日志
        String exam_operation = Environment.getExternalStorageDirectory() + "/logger/" + "exam_operation_" + logFileName;
        Logger.addLogAdapter(new DiskLogAdapter(new OperaLogAdapter(exam_operation)));

        // 保存Activity生命周期日志
        String exam_life = Environment.getExternalStorageDirectory() + "/logger/" + "exam_life_" + logFileName;
        Logger.addLogAdapter(new DiskLogAdapter(new LifeLogAdapter(exam_life)));
    }
    public static void normal(String message){
        Logger.t(LogUtils.NORMAL_TAG).i(message);
    }

    public static void operation(String message) {
        Logger.t(LogUtils.OPERATION_TAG).i(message);
    }
    public static void life(String message) {
        Logger.t(LogUtils.LIFE_TAG).i(message);
    }

    public static void all(String message) {
        Logger.t(LogUtils.ALL_TAG).i(message);
    }
}
