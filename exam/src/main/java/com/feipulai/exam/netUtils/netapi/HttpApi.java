package com.feipulai.exam.netUtils.netapi;

/**
 * Created by pengjf on 2018/10/9.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */


import com.feipulai.exam.bean.ActivateBean;
import com.feipulai.exam.bean.BatchBean;
import com.feipulai.exam.bean.GroupBean;
import com.feipulai.exam.bean.ItemBean;
import com.feipulai.exam.bean.RoundScoreBean;
import com.feipulai.exam.bean.ScheduleBean;
import com.feipulai.exam.bean.SoftApp;
import com.feipulai.exam.bean.StudentBean;
import com.feipulai.exam.bean.UpdateApp;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.bean.UserBean;
import com.feipulai.exam.bean.UserPhoto;
import com.feipulai.exam.netUtils.HttpResult;
import com.feipulai.exam.netUtils.ResponseParame;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * 放置请求接口
 */
public interface HttpApi {

    /**
     * 设备登录
     */
    String LOGIN_ACTION = "/auth/terminal/token";

    //日程-->项目--》学生
    /**
     * 获取日程
     */
    String GET_SCHEDULE_ACTION = "/run/downSiteScheduleInfo";
    /**
     * 获取项目信息接口
     */
    String GET_ITEM_ALL_ACTION = "/run/downItemInfo";
    /**
     * 获取学生信息
     */
    String GET_STUDENT_ACTION = "/run/downSiteScheduleItemStudent";
    /**
     * 获取分组信息
     */
    String GET_GROUP_ACTION = "/run/downSiteScheduleItemGroup";
    /**
     * 获取分组信息
     */
    String GET_GROUP_INFO_ACTION = "/run/downSiteScheduleItemGroupByGroupNo";
    /**
     * 成绩上传接口
     */
    String UPLOAD_RESULT_ACTION = "/run/uploadStudentResult";
    /**
     * 云端成绩获取
     */
    String GET_RESULT_ACTION = "/run/checkTerminalStudentSingleTest";

    String GET_SOFT_APP = "https://api.soft.fplcloud.com/public/checkSoftwareVersion";
    String UPDATE_SOFT_APP = "/public/updateSoftware";

    @POST(LOGIN_ACTION)
    @FormUrlEncoded
    Observable<HttpResult<UserBean>> login(@Header("Authorization") String token, @FieldMap() Map<String, String> map);

    @POST(GET_SCHEDULE_ACTION)
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<ScheduleBean>> getScheduleAll(@Header("Authorization") String token, @Body ResponseParame data);

    @POST(GET_ITEM_ALL_ACTION)
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<List<ItemBean>>> getItemAll(@Header("Authorization") String token, @Body ResponseParame data);

    @POST(GET_STUDENT_ACTION)
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<BatchBean<List<StudentBean>>>> getStudent(@Header("Authorization") String token, @Body ResponseParame data);

    @POST(GET_GROUP_ACTION)
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<BatchBean<List<GroupBean>>>> getGroupAll(@Header("Authorization") String token, @Body ResponseParame data);

    @POST(GET_GROUP_INFO_ACTION)
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<List<GroupBean>>> getGroupInfo(@Header("Authorization") String token, @Body ResponseParame data);

    @POST(UPLOAD_RESULT_ACTION)
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<List<UploadResults>>> uploadResult(@Header("Authorization") String token, @Body ResponseParame data);

    @POST(GET_RESULT_ACTION)
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<RoundScoreBean>> getRoundScore(@Header("Authorization")String token, @Body ResponseParame data);

    @POST(GET_SOFT_APP)
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<List<SoftApp>>> getSoftApp(@Body RequestBody body);

    @POST(UPDATE_SOFT_APP)
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<UpdateApp>> updateSoftApp(@Body RequestBody body);

    @POST("run/compareFaceFeature")
    @Headers("Content-Type:application/json;charset=UTF-8")
    Observable<HttpResult<UserPhoto>> compareFaceFeature(@Header("Authorization") String token, @Body ResponseParame data);

    @POST("https://api.soft.fplcloud.com/terminal/softwareactivate/active")
    Observable<HttpResult<ActivateBean>> activate(@Body ResponseParame parame);

    @POST("https://api.soft.fplcloud.com/terminal/softwarerunlog/add")
    Observable<HttpResult<String>> uploadLog(@Body ResponseParame parame);
}
