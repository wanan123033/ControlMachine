package com.feipulai.host.utils.http;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.feipulai.host.MyApplication;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.utils.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;

import okhttp3.Callback;


/**
 * 发送请求
 *
 * @author zzs
 */
public class OkHttpRequest {
    //TODO 测试
    public static String ROOT_URL  /*="http://192.168.0.60:8036/app/"*/;
    private static final String TAG = "OkHttpRequest";
    /**
     * 采用单例模式使用OkHttpClient
     */
    private static volatile OkHttpRequest okHttpRequest;

    public static OkHttpRequest getinstance() {
        if (okHttpRequest == null) {
            synchronized (OkHttpHelper.class) {
                if (okHttpRequest == null) {
                    try {
                        okHttpRequest = new OkHttpRequest();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return okHttpRequest;
    }

    private boolean getIpLocation() {
        String ip = SettingHelper.getSystemSetting().getServerIp();
        if (TextUtils.isEmpty(ip)) {
            Toast.makeText(MyApplication.getInstance(), "服务无法访问，请先在系统设置设置IP地址", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            ROOT_URL = "http://" + ip + "/app/";
            return true;
        }

    }

    /**
     * 生成Post请求默认参数.
     *
     * @return 返回包含默认参数map.
     */
    public HashMap<String, String> getPostRequestHead(String method) {

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("method", method);
        map.put("parameData", "");
        map.put("dateTime", System.currentTimeMillis() + "");
        map.put("token", MyApplication.TOKEN);
        map.put("signature", "");

        return map;
    }

    /**
     * 生成Post请求默认参数.
     *
     * @return 返回包含默认参数map.
     */
    public HashMap<String, String> getPostRequestHead() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("parameData", "");
        map.put("signature", "");
        map.put("token", MyApplication.TOKEN);
        map.put("dateTime", System.currentTimeMillis() + "");
        return map;
    }

    /**
     * 发送post请求（无参数的指定地址）
     *
     * @param path
     * @return
     */
    public void sendPostRequest(String path, Callback callable) {
        OkHttpHelper helper = OkHttpHelper.getinstance();
        helper.sendPost(path, callable);
    }

    /**
     * 发送Post请求.
     *
     * @param path   Post请求路径.
     * @param params 传递参数.
     */
    public void sendPostRequest(String path, HashMap<String, String> params, Callback callable) {
        if (!getIpLocation()) {
            callable.onFailure(null, null);
            return;
        }
        OkHttpHelper helper = OkHttpHelper.getinstance();
        //Logger.i("url:" + path);
        //Logger.i(params.toString());
        helper.sendPost(path, params, callable);
    }


    /**
     * 发送Post请求.
     *
     * @param params 传递参数.
     */
    public void sendPostRequest(HashMap<String, String> params, Callback callable) {
        if (!getIpLocation()) {
            return;
        }

        OkHttpHelper helper = OkHttpHelper.getinstance();

        helper.sendPost(ROOT_URL + params.get("method"), params, callable);

    }


    /**
     * 发送Post请求.
     *
     * @param parameData parameData传递参数.
     */
    public void sendPostRequest(HashMap<String, String> parameData, String action, Callback callable) {
        if (!getIpLocation()) {
            callable.onFailure(null, null);
            return;
        }
        HashMap<String, String> requestParams = getPostRequestHead();
        if (parameData != null) {
            requestParams.put("parameData", getParameData(parameData));
        }
        requestParams.put("signature", getSignature(requestParams));
        sendPostRequest(ROOT_URL + action, requestParams, callable);
    }

    /**
     * 签名参数值生成
     * <h3>CreateAuthor</h3> zzs
     */
    public String getSignature(@NonNull HashMap<String, String> params) {
        //        String signature = "";
        //        String signatureValue = "";
        //        for (Map.Entry<String, String> entry : params.entrySet()) {
        //            if (!TextUtils.equals(entry.getKey(), "signature"))
        //                signature += entry.getKey() + "&";
        //            if (!TextUtils.isEmpty(entry.getValue())) {
        //                signatureValue += entry.getValue() + "&";
        //            }
        //        }
        //        signatureValue = signatureValue.substring(0, signatureValue.length() - 1);

        String signature = "parameData&" + "dateTime&" + "token&";
        if (params.get("token") != null && !"".equals(params.get("token"))) {
            signature += params.get("token") + "&";
        }
        if (params.get("parameData") != null && !"".equals(params.get("parameData"))) {
            signature += params.get("parameData") + "&";
        }
        signature += params.get("dateTime");

        try {
            signature = HttpUtils.getMD5(signature);
            //Logger.d("signature=======" + signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signature;
    }

    /**
     * 传递参数转string
     * <h3>CreateAuthor</h3> zzs
     */
    public String getParameData(HashMap<String, String> parameData) {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        //Logger.d("getParameData=======" + gson.toJson(parameData));
        return gson.toJson(parameData);
    }


}
