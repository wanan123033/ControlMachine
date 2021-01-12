package com.feipulai.host.netUtils.netapi;

/**
 * Created by pengjf on 2018/10/9.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */


import com.feipulai.host.bean.ActivateBean;
import com.feipulai.host.bean.BatchBean;
import com.feipulai.host.bean.ItemBean;
import com.feipulai.host.bean.SoftApp;
import com.feipulai.host.bean.StudentBean;
import com.feipulai.host.bean.UpdateApp;
import com.feipulai.host.bean.UploadResults;
import com.feipulai.host.bean.UserBean;
import com.feipulai.host.bean.UserPhoto;
import com.feipulai.host.netUtils.HttpResult;
import com.feipulai.host.netUtils.ResponseParame;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * 放置请求接口
 */
public interface HttpApi {

    /**
     * 设备绑定接口
     */
    String DEVICE_BINDING_ACTION = "/run/deviceBind";
    /**
     * 获取项目信息接口
     */
    String GET_ITEM_ALL_ACTION = "/run/downItemInfo";
    /**
     * 分页获取学生信息
     */
    String GET_STUDENT_ACTION = "/run/downSiteScheduleItemStudent";
    /**
     * 成绩上传接口
     */
    String UPLOAD_RESULT_ACTION = "/run/uploadStudentResult";

    String GET_SOFT_APP = "/app/public/checkSoftwareVersion";
    String UPDATE_SOFT_APP = "/app/public/updateSoftware";

    @POST(DEVICE_BINDING_ACTION)
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<UserBean>> bind(@Header("Authorization") String token, @Body ResponseParame data);

    @POST(GET_ITEM_ALL_ACTION)
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<List<ItemBean>>> getAllItem(@Header("Authorization") String token, @Body ResponseParame data);

    @POST(GET_STUDENT_ACTION)
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<BatchBean<List<StudentBean>>>> getStudentData(@Header("Authorization") String token, @Body ResponseParame data);

    @POST(UPLOAD_RESULT_ACTION)
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<List<UploadResults>>> uploadResult(@Header("Authorization") String token, @Body ResponseParame data);

    @POST(GET_SOFT_APP)
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<List<SoftApp>>> getSoftApp(@Body RequestBody body);

    @POST(UPDATE_SOFT_APP)
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<UpdateApp>> updateSoftApp(@Body RequestBody body);

    @POST("run/compareFaceFeature")
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<UserPhoto>> netSh(@Header("Authorization") String token, @Body ResponseParame data);
    @POST("https://api.soft.fplcloud.com/terminal/softwareactivate/active")
    Observable<HttpResult<ActivateBean>> activate(@Body ResponseParame parame);

    @POST("https://api.soft.fplcloud.com/terminal/softwarerunlog/add")
    Observable<HttpResult<String>> uploadLog(@Body ResponseParame parame);
}
