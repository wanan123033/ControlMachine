package com.feipulai.exam.netUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.SystemUtil;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.utils.EncryptUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by pengjf on 2018/10/16.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class CommonUtils {
    /**
     * 获取本地软件版本号
     *
     * @param ctx
     * @return
     */
    public static int getLocalVersion(Context ctx) {
        int localVersion = 0;
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
            LogUtil.logDebugMessage("本软件的版本号。。" + localVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    /**
     * 获取应用程序名称
     */
    public static synchronized String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String getDeviceId(Context context) {

        String id;
        //android.telephony.TelephonyManager
        TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephony.getDeviceId() != null) {
            id = mTelephony.getDeviceId();
        } else {
            //android.provider.Settings;
            id = SystemUtil.getCPUSerial();
            if (TextUtils.isEmpty(id)) {
                id = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            }

        }
        return id;
    }

    @SuppressLint({"WifiManagerLeak", "MissingPermission"})
    public static String getDeviceInfo() {
        TelephonyManager phone = (TelephonyManager) MyApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        WifiManager wifi = (WifiManager) MyApplication.getInstance().getSystemService(Context.WIFI_SERVICE);

        return wifi.getConnectionInfo().getMacAddress() + "," + getDeviceId(MyApplication.getInstance()) + "," + getCpuName() + "," + phone.getNetworkOperator();
    }

    /**
     * 获取CPU型号
     *
     * @return
     */
    private static String getCpuName() {
        String str1 = "/proc/cpuinfo";
        String str2 = "";
        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr);
            while ((str2 = localBufferedReader.readLine()) != null) {
                if (str2.contains("Hardware")) {
                    return str2.split(":")[1];
                }
            }
            localBufferedReader.close();
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * 获取签名数据
     *
     * @param params
     * @return
     */
    public static String getSignature(@NonNull HashMap<String, String> params) {

        String signature = "parameData&" + "dateTime&" + "token&";

        if (params.get("token") != null && !"".equals(params.get("token"))) {
            signature += params.get("token") + "&";
        }
        if (params.get("parameData") != null && !"".equals(params.get("parameData"))) {
            signature += params.get("parameData") + "&";
        }
        signature += params.get("dateTime");

        signature = getMD5(signature);
        //Logger.d("signature=======" + signature);
        return signature;
    }

    public static String getMD5(String str) {
        try {
            return DigestUtils.md5Hex(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // 不可能发生
        }
        return null;
    }

    /**
     * 传递参数转string
     */
    public static String getParamData(Object paramData) {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        return gson.toJson(paramData);
    }

    public static ResponseParame encryptQuery(String bizType, String lastUpdateTime, Object object) {
        ResponseParame respost = encryptQuery(bizType, object);
        respost.setLastUpdateTime(lastUpdateTime);
        return respost;
    }

    public static ResponseParame encryptQuery(String bizType, Object object) {
        ResponseParame respost = new ResponseParame();
        respost.setBizType(bizType);
        respost.setToken(MyApplication.TOKEN);
        respost.setMsEquipment(getDeviceInfo());
//        Map<String, Object> signMap = new HashMap<>();
//        signMap.put(bizType, object);
//        respost.setSign(EncryptUtil.getSignData(signMap));
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        respost.setSign(EncryptUtil.getSignData(gson.toJson(object)));
        respost.setData(EncryptUtil.setEncryptData(object));
        respost.setRequestTime(String.valueOf(System.currentTimeMillis()));
//        LogUtils.net("请求请口参数：" + respost.toString());
        return respost;
    }

    public static String getIp() {
        String ipAddress = SettingHelper.getSystemSetting().getServerIp();
        if (TextUtils.isEmpty(ipAddress)) {
            ipAddress = "https://gkapidev.exam.fplcloud.com";
        }
        if (!ipAddress.startsWith("http")) {
            ipAddress = "http://" + ipAddress + "/app/";
        }
        return ipAddress;
    }
}
