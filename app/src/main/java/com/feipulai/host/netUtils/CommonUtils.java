package com.feipulai.host.netUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;

import com.feipulai.host.MyApplication;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.utils.SharedPrefsUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;

/**
 * Created by pengjf on 2018/10/16.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class CommonUtils {

    @SuppressLint("HardwareIds")
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

    /**
     * 获取签名数据
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
        //return DigestUtils.md5Hex(str.getBytes("UTF-8"));
        return  new String(Hex.encodeHex(DigestUtils.md5(str))) ;
    }

    /**
     * 传递参数转string
     */
    public static String getParamData(Object paramData) {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        return gson.toJson(paramData);
    }

    public static HashMap<String, String> query(Object object) {
        HashMap<String, String> query = new HashMap<>();
        query.put("parameData", CommonUtils.getParamData(object));//customer
        query.put("token", MyApplication.TOKEN);
        query.put("dateTime", System.currentTimeMillis() + "");
        query.put("signature", CommonUtils.getSignature(query));
        return query;
    }

    public static String getIp(Context context) {
        String ipAddress = SharedPrefsUtil.getValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.IP_ADDRESS,
                TestConfigs.DEFAULT_IP_ADDRESS) + "/app/";
        if (!ipAddress.startsWith("http")) {
            ipAddress = "http://" + ipAddress;
        }
        return ipAddress;
    }
}
