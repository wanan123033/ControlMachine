package com.feipulai.common.utils;

import android.util.Log;

/**
 * 打印类.
 *
 */
public class LogUtil {

    /**
     * 打印开关
     */
    private static boolean logSwitch = true;
    /**
     * 打印标记
     */
    private static String printFlag = "fairplay =====> ";

    /**
     * 打开打印.
     *
     * @version 1.0
     * @updateInfo
     */
    public static void openLog() {
        logSwitch = true;
    }

    /**
     * 关闭打印.
     *
     * @updateInfo
     */
    public static void closeLog() {
        logSwitch = false;
    }

    /**
     * 查看打印功能是否打开.
     *
     * @updateInfo
     */
    public static boolean isOpenLog() {
        return logSwitch;
    }

    /**
     * 打印信息.
     *
     * @updateInfo
     */
    public static void logMessage(String tag, String message) {
        if (logSwitch) {
            Log.i(tag, printFlag + message);
        }
    }

    /**
     * 打印调试信息.
     *
     * @updateInfo
     */
    public static void logDebugMessage(String message) {
        if (logSwitch) {
            Log.i("debug", printFlag + message);
        }
    }

    /**
     * 打印系统信息.
     *
     * @updateInfo
     */
    public static void logSystemMessage(String message) {
        if (logSwitch) {
            Log.i("system", printFlag + message);
        }
    }

    /**
     * 打印错误信息.
     *
     * @updateInfo
     */
    public static void logErrorMessage(String error) {
        if (logSwitch) {
            Log.i("error", printFlag + error);
            Log.e("error", printFlag + error);
        }
    }

    /**
     * 打印长日志
     * <p/>
     */

    public static void logLongMessage(String message) {

        if (message.length() > 4000) {
            for (int i = 0; i < message.length(); i += 4000) {
                if (i + 4000 < message.length())
                    Log.i(printFlag + "--------" + i + "----------", message.substring(i, i + 4000) + "\n--------");
                else
                    Log.i(printFlag + "--------" + i + "----------", message.substring(i, message.length()) + "\n--------");
            }
        } else Log.i("\n--------" + printFlag, message + "\n--------");
    }
}
