package com.feipulai.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import com.feipulai.common.utils.ActivityCollector;
import com.orhanobut.logger.utils.LogUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 * <p>
 * Created by yuyuhang on 15/12/7.
 */
public class CrashHandler implements UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static CrashHandler instance = new CrashHandler();
    private Context mContext;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return instance;
    }

    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    // 当UncaughtException发生时会转入该函数来处理
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        //if(ex == null){
        //	return;
        //}

        // String deviceInfo = collectDeviceInfo(mContext);
        String erroMsg = getErrMessage(ex);

        LogUtils.crash("----------start------------------\n" +
                "fatal exception:\t    application crashed!!!\n"
                // + deviceInfo
                + erroMsg);

        // if(mDefaultHandler != null){
        // 	mDefaultHandler.uncaughtException(thread,ex);
        // }
        ActivityCollector.getInstance().finishAllActivity();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    private String collectDeviceInfo(Context ctx) {
        StringBuilder sb = new StringBuilder();
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                sb.append("version code:").append(versionCode).append("\nversion name:").append(versionName).append("\n");
            }
        } catch (NameNotFoundException e) {
            LogUtils.crash("CrashHandleran.NameNotFoundException---> error occured when collect package info"+getErrMessage(e));
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                sb.append(field.getName()).append(":").append(field.get(null).toString()).append("\n");
            } catch (Exception e) {
                LogUtils.crash("CrashHandler.NameNotFoundException---> an error occured when collect crash info"+getErrMessage(e));
            }
        }
        return sb.toString();
    }

    // 保存错误信息到文件中
    private String getErrMessage(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        sb.append("--------------------end---------------------------");
        return sb.toString();
    }

}
