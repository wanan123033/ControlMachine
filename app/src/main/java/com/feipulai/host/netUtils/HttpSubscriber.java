package com.feipulai.host.netUtils;

import android.content.Context;
import android.util.Log;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.host.MyApplication;
import com.feipulai.host.bean.SoftApp;
import com.feipulai.host.bean.UpdateApp;
import com.feipulai.host.bean.UserBean;
import com.feipulai.host.config.SharedPrefsConfigs;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class HttpSubscriber {
    /**
     * 用户登录
     *
     * @param username
     * @param password
     */
    public void login(Context context, String username, String password, OnResultListener listener) {
        if (HttpManager.DEFAULT_CONNECT_TIMEOUT==5){
            HttpManager.DEFAULT_CONNECT_TIMEOUT = 60;
            HttpManager.DEFAULT_READ_TIMEOUT = 60;
            HttpManager.DEFAULT_WRITE_TIMEOUT = 60;
            HttpManager.resetManager();
        }
        Map<String, String> parameData = new HashMap<>();
        parameData.put("username", username + "@" + CommonUtils.getDeviceId(context));
//        parameData.put("username", username);
        parameData.put("password", password);
        //TODO 登录协议与其它接口分离，单传用户名和密码
        String serverToken = SharedPrefsUtil.getValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.DEFAULT_SERVER_TOKEN, "dGVybWluYWw6dGVybWluYWxfc2VjcmV0");
        Observable<HttpResult<UserBean>> observable = HttpManager.getInstance().getHttpApi().login("Basic " + serverToken, parameData);
//        Observable<HttpResult<UserBean>> observable = HttpManager.getInstance().getHttpApi().login(CommonUtils.query("1001", parameData));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<UserBean>(listener, context));
    }
    public void getApps(Context context, String version, final OnResultListener listener) {
        Map<String, String> parameData = new HashMap<>();
        parameData.put("softwareUuid", MyApplication.SOFTWAREUUID);
        parameData.put("hardwareUuid", MyApplication.HARDWAREUUID);
        parameData.put("version", version);
        parameData.put("deviceCode", MyApplication.DEVICECODE);
        final RequestBody requestBody = RequestBody.create(MediaType.parse("Content-Type, application/json"), new JSONObject(parameData).toString());
        Observable<HttpResult<List<SoftApp>>> observable = HttpManager.getInstance().getHttpApi().getSoftApp(requestBody);
        HttpManager.getInstance().changeBaseUrl("https://api.soft.fplcloud.com");
        HttpManager.getInstance().toSubscribe(observable,new RequestSub<List<SoftApp>>(new OnResultListener<List<SoftApp>>() {
            @Override
            public void onSuccess(List<SoftApp> result) {
                Log.i("SoftApp","Observable");
                listener.onSuccess(result);
            }

            @Override
            public void onFault(int code, String errorMsg) {
                Log.i("SoftApp",errorMsg);
                listener.onFault(code,errorMsg);
            }
        }));
    }

    /**
     * 获取APP更新的url
     * @param version
     * @param updateSoftwareVersion
     * @param authorizeCode
     * @param listener
     */
    public void updateApp(String version,String updateSoftwareVersion,String authorizeCode,
                          final OnResultListener listener) {
        Map<String, String> parameData = new HashMap<>();
        parameData.put("softwareUuid", MyApplication.SOFTWAREUUID);
        parameData.put("hardwareUuid", MyApplication.HARDWAREUUID);
        parameData.put("version", version);
        parameData.put("updateSoftwareVersion",updateSoftwareVersion);
        parameData.put("authorizeCode",authorizeCode);
        parameData.put("enableCompression","0");
        parameData.put("deviceCode", MyApplication.DEVICECODE);
        final RequestBody requestBody = RequestBody.create(MediaType.parse("Content-Type, application/json"), new JSONObject(parameData).toString());
        Observable<HttpResult<UpdateApp>>observable = HttpManager.getInstance().getHttpApi().updateSoftApp(requestBody);
        HttpManager.getInstance().changeBaseUrl("https://api.soft.fplcloud.com");
        HttpManager.getInstance().toSubscribe(observable,new RequestSub<UpdateApp>(new OnResultListener<UpdateApp>() {
            @Override
            public void onSuccess(UpdateApp result) {
                listener.onSuccess(result);
            }

            @Override
            public void onFault(int code, String errorMsg) {
                Log.i("UpdateApp",code +errorMsg);
                listener.onFault(code,errorMsg);
            }
        }));



    }
}
