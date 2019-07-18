package com.feipulai.exam.netUtils;

import com.feipulai.exam.netUtils.netapi.HttpApi;

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
    public static final String CACHE_NAME = "yourApkName";
    public static String BASE_URL = URLConstant.BASE_URL;
    private static final int DEFAULT_CONNECT_TIMEOUT = 5;
    private static final int DEFAULT_WRITE_TIMEOUT = 20;
    private static final int DEFAULT_READ_TIMEOUT = 20;
    private Retrofit retrofit;
    private HttpApi httpApi;
    /**
     * 请求失败重连次数
     */
    private int RETRY_COUNT = 0;
    private OkHttpClient.Builder okHttpBuilder;
    private static HttpManager instance;

    //构造方法私有
    private HttpManager() {
        //手动创建一个OkHttpClient并设置超时时间
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
//                String serverToken = SharedPrefsUtil.getValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.DEFAULT_SERVER_TOKEN, "dGVybWluYWw6dGVybWluYWxfc2VjcmV0");
//                requestBuilder.addHeader("Authorization", "Basic " + serverToken);//添加请求头信息，服务器进行token有效性验证
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        };
        okHttpBuilder.addInterceptor(headerInterceptor);


//        if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
//                Logger.d(message);
            }


        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        //设置 Debug Log 模式
        okHttpBuilder.addInterceptor(loggingInterceptor);
//        }

        /**
         * 设置超时和重新连接
         */

        okHttpBuilder.connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS);


        okHttpBuilder.readTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS);
        okHttpBuilder.connectTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS);
        //错误重连
        okHttpBuilder.retryOnConnectionFailure(true);
        retrofit = new Retrofit.Builder()
                .client(okHttpBuilder.build())
//                .addConverterFactory(GsonConverterFactory.create())//json转换成JavaBean
                .addConverterFactory(MGsonConverterFactory.create())//加入刚刚自定义的转换器MGsonConverterFactory
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(CommonUtils.getIp())
                .build();
        httpApi = retrofit.create(HttpApi.class);
    }

    public synchronized static HttpManager getInstance() {
        if (instance == null) {
            instance = new HttpManager();
        }
        return instance;
    }

    public static void resetManager() {
        instance = null;
    }

    /**
     * 获取retrofit
     *
     * @return
     */
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

    /**
     * 获取httpService
     *
     * @return
     */
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