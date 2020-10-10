package com.feipulai.common.utils;

import android.util.Log;

import com.orhanobut.logger.Logger;

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
     * 打印调试信息.
     *
     * @updateInfo
     */
    public static void logDebugMessage(String message) {
        if (logSwitch) {
            Logger.i( printFlag + message);
        }
    }

}
