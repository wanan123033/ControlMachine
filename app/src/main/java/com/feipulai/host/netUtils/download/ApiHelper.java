package com.feipulai.host.netUtils.download;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Created by pengjf on 2018/10/10.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ApiHelper {
	
    private static final String TAG = "ApiHelper";
    private static ApiHelper mInstance;
    private Retrofit mRetrofit;
    private OkHttpClient mHttpClient;

    private ApiHelper() {
        this( 30, 30, 30);
    }

    public ApiHelper( int connTimeout, int readTimeout, int writeTimeout) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(connTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS);

        mHttpClient = builder.build();
    }

    public static ApiHelper getInstance() {
        if (mInstance == null) {
            mInstance = new ApiHelper();
        }
        return mInstance;
    }

    public ApiHelper buildRetrofit(String baseUrl) {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(mHttpClient)
                .build();
        return this;
    }

    public <T> T createService(Class<T> serviceClass) {
        return mRetrofit.create(serviceClass);
    }
    
}
