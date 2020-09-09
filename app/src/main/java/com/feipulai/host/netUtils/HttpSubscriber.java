package com.feipulai.host.netUtils;

import android.content.Context;
import android.util.Log;

import com.feipulai.host.MyApplication;
import com.feipulai.host.bean.SoftApp;
import com.feipulai.host.bean.UpdateApp;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class HttpSubscriber {
    public void getApps(Context context, String version, final OnResultListener listener) {
        Map<String, String> parameData = new HashMap<>();
        parameData.put("softwareUuid", MyApplication.SOFTWAREUUID);
        parameData.put("hardwareUuid", MyApplication.HARDWAREUUID);
        parameData.put("version", "1.1.9.2");
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
