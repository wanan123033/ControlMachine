package com.feipulai.host.netUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.feipulai.host.MyApplication;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.utils.EncryptUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orhanobut.logger.Logger;

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

    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String getDeviceId(Context context) {

        String id;
        //android.telephony.TelephonyManager
        TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephony.getDeviceId() != null) {
            id = mTelephony.getDeviceId();
        } else {
            //android.provider.Settings;
            id = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return id;
    }

    @SuppressLint({"WifiManagerLeak", "MissingPermission"})
    public static String getDeviceInfo() {
        TelephonyManager phone = (TelephonyManager) MyApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        WifiManager wifi = (WifiManager) MyApplication.getInstance().getSystemService(Context.WIFI_SERVICE);

        return wifi.getConnectionInfo().getMacAddress() + "," + phone.getDeviceId() + "," + getCpuName() + "," + phone.getNetworkOperator();
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
        if (bizType.equals("7001")){
            respost.setData(gson.toJson(object));
        }else {
            respost.setData(EncryptUtil.setEncryptData(object));
        }
        respost.setData(EncryptUtil.setEncryptData(object));
        respost.setRequestTime(String.valueOf(System.currentTimeMillis()));
        Logger.i("json:============="+respost.toString());
        return respost;
    }

    public static String getIp() {
        String ipAddress = SettingHelper.getSystemSetting().getServerIp();
        if (TextUtils.isEmpty(ipAddress)) {
            ipAddress = "https://api.soft.fplcloud.com";
        }
        if (!ipAddress.startsWith("http")) {
            ipAddress = "http://" + ipAddress + "/";
        }
        return ipAddress;
    }
}
