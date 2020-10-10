package com.feipulai.host.netUtils.download;


import com.feipulai.host.netUtils.ResponseParame;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 *
 */
public interface DownService {

    /**
     * 下载文件
     */
    @Streaming
//    @GET("/run/batchDownloadPhotos")
    @POST("/run/batchDownloadPhotos")
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<Response<ResponseBody>> downloadFile(@Header("Authorization") String token, @Body ResponseParame data);

    @Streaming
    @GET
    Call<ResponseBody> download(@Url String url);

}
