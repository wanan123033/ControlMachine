package com.feipulai.host.utils.http;

import android.text.TextUtils;

import com.orhanobut.logger.Logger;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * okhttp网络请求工具类
 * Created by zzs
 */

public class OkHttpHelper {
    /**
     * 默认连接超时时间（单位：秒）
     */
    private static final long DEAFULT_TIME_OUT = 5000L;
    /**
     * 默认读写超时时间（读与写同步，单位：秒）
     */
    private static final long DEFAULT_READ_TIME_OUT = 5000L;
    /**
     * 采用单例模式使用OkHttpClient
     */
    private static volatile OkHttpHelper mOkHttpHelperInstance;
    private OkHttpClient mClientInstance;


    /**
     * 单例模式，私有构造函数，构造函数里面进行一些初始化
     */
    private OkHttpHelper() {
        mClientInstance = new OkHttpClient().newBuilder().connectTimeout(DEAFULT_TIME_OUT, TimeUnit.MILLISECONDS)
                .readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.MILLISECONDS)
                .connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
                .build();
    }

    public static OkHttpHelper getinstance() {
        if (mOkHttpHelperInstance == null) {
            synchronized (OkHttpHelper.class) {
                if (mOkHttpHelperInstance == null) {
                    try {
                        mOkHttpHelperInstance = new OkHttpHelper();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return mOkHttpHelperInstance;
    }





    /**
     * 对外公开的get方法
     *
     * @param path
     */
    public void sendGet(String path, Callback callable) {
        Request request = buildRequest(path, null, HttpMethodType.GET);
        request(request, callable);
    }

    /**
     * 对外公开的post方法
     *
     * @param url
     * @param params
     */
    public void sendPost(String url, Map<String, String> params,Callback callable) {
        Request request = buildRequest(url, params, HttpMethodType.POST);
        request(request, callable);
    }

    /**
     * 对外公开的post方法
     *
     * @param url
     */
    public void sendPost(String url, Callback callable) {
        Request request = buildRequest(url, null, HttpMethodType.POST);
        request(request, callable);
    }

    /**
     * 封装一个request方法，不管post或者get方法中都会用到
     */
    public void request(Request request,   Callback callable) {
        mClientInstance.newCall(request).enqueue(callable);

    }

    /**
     * 构建请求对象
     *
     * @param url
     * @param params
     * @param type
     * @return
     */
    private Request buildRequest(String url, Map<String, String> params, HttpMethodType type) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (type == HttpMethodType.GET) {
            builder.get();
        } else if (type == HttpMethodType.POST) {
            builder.post(buildRequestBody(params));
        }
        return builder.build();
    }

    /**
     * 通过Map的键值对构建请求对象的body
     *
     * @param params
     * @return
     */
    private RequestBody buildRequestBody(Map<String, String> params) {

        FormBody.Builder builder = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String, String> entity : params.entrySet()) {
                if (!TextUtils.isEmpty(entity.getKey()) && !TextUtils.isEmpty(entity.getValue())) {
                    builder.add(entity.getKey(), entity.getValue());
                    Logger.i("key:" + entity.getKey() + "\t\tvalue:" + entity.getValue());
                }
            }
        }
        return builder.build();
    }



    /**
     * 这个枚举用于指明是哪一种提交方式
     */
    enum HttpMethodType {
        GET, POST
    }

}
