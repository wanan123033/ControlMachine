package com.feipulai.host.netUtils;

import com.feipulai.host.MyApplication;
import com.feipulai.host.netUtils.netapi.HttpApi;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author pengjf
 */

public class HttpManager {
    
    public String TAG = "HttpManager";
    // public static final String CACHE_NAME = "yourApkName";
    private static final int DEFAULT_CONNECT_TIMEOUT = 3;
    private static final int DEFAULT_WRITE_TIMEOUT = 3;
    private static final int DEFAULT_READ_TIMEOUT = 3;
    private Retrofit retrofit;
    private HttpApi httpApi;
    private int RETRY_COUNT = 1;
    private OkHttpClient.Builder okHttpBuilder;

    private HttpManager() {
        okHttpBuilder = new OkHttpClient.Builder();
        /**
         * 设置头信息
         */
        Interceptor headerInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request.Builder requestBuilder = originalRequest.newBuilder()
                        .addHeader("Accept-Encoding", "gzip")
                        .addHeader("Accept", "application/json")
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .method(originalRequest.method(), originalRequest.body());
                // requestBuilder.addHeader("Authorization", "Bearer " + URLConstant.TOKEN);//添加请求头信息，服务器进行token有效性验证
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        };
        okHttpBuilder.addInterceptor(headerInterceptor);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Logger.d(message);
            }
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpBuilder.addInterceptor(loggingInterceptor)
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
        
        retrofit = new Retrofit.Builder()
                .client(okHttpBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(CommonUtils.getIp(MyApplication.getInstance()))
                .build();
        httpApi = retrofit.create(HttpApi.class);
    }

    private static class SingletonHolder {
        private static final HttpManager INSTANCE = new HttpManager();
    }

    public static HttpManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public void changeBaseUrl(String baseUrl) {
        retrofit = new Retrofit.Builder()
                .client(okHttpBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build();
        httpApi = retrofit.create(HttpApi.class);
    }

    public HttpApi getHttpApi() {
        return httpApi;
    }

    /**
     * 设置订阅 和 所在的线程环境
     */
    public <T> void toSubscribe(Observable<T> o, DisposableObserver<T> s) {
        o.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(RETRY_COUNT)//请求失败重连次数
                .subscribe(s);
    }
    
}