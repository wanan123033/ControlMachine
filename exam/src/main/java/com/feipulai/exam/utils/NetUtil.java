package com.feipulai.exam.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * created by ww on 2019/8/27.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class NetUtil {
    public static boolean openWifi(Context context) {
        Log.i("openWifi", "--------------------");
        //获取wifi管理服务
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean bRet = false;
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            bRet = wifiManager.setWifiEnabled(true);
        }
        return bRet;
    }
}
