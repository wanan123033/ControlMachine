package com.feipulai.host.netUtils.netapi;

/**
 * Created by pengjf on 2018/10/9.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */


import com.feipulai.host.entity.Item;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.HttpResult;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * 放置请求接口
 */
public interface HttpApi {

    /**
     * 设备绑定接口
     */
    String DEVICE_BINDING_ACTION = "device/binding.action";
    /**
     * 获取项目信息接口
     */
    String GET_ITEM_ALL_ACTION = "item/getAll.action";
    /**
     * 分页获取学生信息
     */
    String GET_STUDENT_ACTION = "student/conditionalGet.action";
    /**
     * 成绩上传接口
     */
    String UPLOAD_RESULT_ACTION = "student/uploadResult.action";

    @POST(DEVICE_BINDING_ACTION)
    @FormUrlEncoded
    Observable<HttpResult<String>> bind(@FieldMap() Map<String, String> map);

    @POST(GET_ITEM_ALL_ACTION)
    @FormUrlEncoded
    Observable<HttpResult<List<Item>>> getAll(@FieldMap() Map<String, String> map);

    @POST(GET_STUDENT_ACTION)
    @FormUrlEncoded
    Observable<HttpResult<List<Student>>> getStudent(@FieldMap() Map<String, String> map);

    @POST(UPLOAD_RESULT_ACTION)
    @FormUrlEncoded
    Observable<HttpResult<String>> uploadResult(@FieldMap() Map<String, String> map);
}
