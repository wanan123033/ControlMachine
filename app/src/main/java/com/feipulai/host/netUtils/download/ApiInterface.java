package com.feipulai.host.netUtils.download;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Description:
 * Created by jia on 2017/11/30.
 * 人之所以能，是相信能
 */
public interface ApiInterface {

    @Streaming
    @GET
    Call<ResponseBody> download(@Url String url);

}
