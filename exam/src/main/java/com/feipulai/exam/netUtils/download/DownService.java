package com.feipulai.exam.netUtils.download;



import com.feipulai.exam.netUtils.ResponseParame;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Streaming;

/**
 *
 */
public interface DownService {

    /**
     * 下载文件
     */
    @Streaming
//    @GET("/run/batchDownloadPhotos")
    @POST("/public/updateSoftware")
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<Response<ResponseBody>> downloadFile(@Header("Authorization") String token, @Body RequestBody data);

}
