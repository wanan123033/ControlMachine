package com.orhanobut.logger.utils;

import android.os.Environment;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.EncryptDiskLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.examlogger.CrashLogAdapter;
import com.orhanobut.logger.examlogger.NormalLogAdapter;
import com.orhanobut.logger.examlogger.OperaLogAdapter;
import com.orhanobut.logger.examlogger.SerialLogAdapter;
import com.orhanobut.logger.examlogger.SerialSendLogAdapter;

import java.io.File;
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
    public static final String NORMAL_TAG = "NORMAL_TAG";       //正常日志,接口日志
    public static final String OPERATION_TAG = "OPERATION_TAG"; //操作日志TAG
    public static final String SERIAL_TAG = "SERIAL_TAG"; //串口日志TAG
    public static final String SERIAL_SEND_TAG = "SERIAL_SEND_TAG"; //串口日志TAG
    public static final String CRASH_TAG = "CRASH_TAG";
    public static final String LOG_ENCRYPT_KEY = "19834762";
    /**
     * 应用根目录
     */
    public static final String PATH_BASE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ControlMachine/";
    private static String fileName;

    //初始化日志工具
    public static void initLogger(final boolean logToConsole, boolean logToRaw, String pathName) {
        fileName = pathName;
        Logger.clearLogAdapters();
        //日志打印到控制台,在发布release版本时，会自动不打印
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return logToConsole;
            }
        });

        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH", Locale.CHINA);
        String logFileName = dateFormat.format(Calendar.getInstance().getTime()) + ".txt";
        // 非加密日志存储在在sd卡中“logger”目录中
        String diskLogFilePath = Environment.getExternalStorageDirectory() + "/" + pathName + "/" + logFileName;
        Logger.addLogAdapter(new DiskLogAdapter(diskLogFilePath));

        //加密日志存储
        String encryptLogFilePath = PATH_BASE + "/fair/play/" + logFileName;
        Logger.addLogAdapter(new EncryptDiskLogAdapter(encryptLogFilePath, LOG_ENCRYPT_KEY));

        // 保存正常日志
        String exam_normal = Environment.getExternalStorageDirectory() + "/" + pathName + "/" + "/examlogger/" + "exam_net_" + logFileName;
        Logger.addLogAdapter(new DiskLogAdapter(new NormalLogAdapter(exam_normal)));
        //串品日志
              String exam_serial = Environment.getExternalStorageDirectory() + "/" + pathName + "/" + "/examlogger/" + "exam_serial_" + logFileName;
        Logger.addLogAdapter(new DiskLogAdapter(new SerialLogAdapter(exam_serial)));
        String exam_serial2 = Environment.getExternalStorageDirectory() + "/" + pathName + "/" + "/examlogger/" + "exam_serial_send_" + logFileName;
        Logger.addLogAdapter(new DiskLogAdapter(new SerialSendLogAdapter(exam_serial2)));

        // 保存操作日志
        String exam_operation = Environment.getExternalStorageDirectory() + "/" + pathName + "/" + "/operationLogger/" + "exam_operation_" + logFileName;
        Logger.addLogAdapter(new DiskLogAdapter(new OperaLogAdapter(exam_operation)));

        String exam_crash = Environment.getExternalStorageDirectory() + "/" + pathName + "/" + "/examlogger/" + "exam_crash_" + logFileName;
        Logger.addLogAdapter(new DiskLogAdapter(new CrashLogAdapter(exam_crash)));


//		String rawLogFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/raw_log/" + logFileName;
//		if(logToRaw){
//			Logger.rawLogToFile(rawLogFilePath);
//		}


    }

    public void clearOperationLogger() {
        delete(Environment.getExternalStorageDirectory() + "/" + fileName + "/" + "/operationLogger/");
    }

    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param fileName 要删除的文件名
     * @return 删除成功返回true，否则返回false
     */
    public boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("删除文件失败:" + fileName + "不存在！");
            return false;
        } else {
            if (file.isFile())
                return deleteFile(fileName);
            else
                return deleteDirectory(fileName);
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param dir 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            System.out.println("删除目录失败：" + dir + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = deleteDirectory(files[i]
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            System.out.println("删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            System.out.println("删除目录" + dir + "成功！");
            return true;
        } else {
            return false;
        }
    }

    /**
     * 发送接收日志打印
     *
     * @param message
     */
    public static synchronized void net(String message) {
        Logger.t(NORMAL_TAG).i(message);
    }
    /**
     * 发送接收日志打印
     *
     * @param message
     */
    public static synchronized void normal(String message) {
        Logger.t(NORMAL_TAG).i(message);
    }
    /**
     * 发送串品日志打印
     *
     * @param message
     */
    public static void serialSend(String message) {
        Logger.t(SERIAL_SEND_TAG).i("#"+message);
    }
    /**
     * 发送串品日志打印
     *
     * @param message
     */
    public static synchronized void serial(String message) {
        Logger.t(SERIAL_TAG).i(message);
    }

    /**
     * 用户操作日志打印
     *
     * @param message
     */
    public static synchronized void operation(String message) {
        Logger.t(OPERATION_TAG).i(message);
    }

    /**
     * Activity 生命周期打印
     *
     * @param message
     */
    public static synchronized void life(String message) {
        Logger.t(ALL_TAG).i(message);
    }

    /**
     * 过滤指令日志打印
     *
     * @param message
     */
    public static synchronized void all(String message) {
        Logger.t(ALL_TAG).i(message);
    }

    public static synchronized void crash(String message) {
        Logger.t(CRASH_TAG).i(message);
    }
}
