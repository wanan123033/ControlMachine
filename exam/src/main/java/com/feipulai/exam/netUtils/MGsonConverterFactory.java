package com.feipulai.exam.netUtils;

import android.text.TextUtils;

import com.feipulai.exam.utils.EncryptUtil;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by zzs on  2019/1/17
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

/**
 * 自定义转换器
 * Add myConverter factory for serialization and deserialization of objects.
 * Created by Administrator on 2017/9/5.
 */

public class MGsonConverterFactory extends Converter.Factory {
    private Gson gson;

    public static MGsonConverterFactory create() {
        return new MGsonConverterFactory();
    }

    private MGsonConverterFactory() {
        gson = new Gson();
    }


    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new ResponseBodyConverter<>(type, gson);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonRequestBodyConverter<>(gson, adapter);
    }

    public final class GsonRequestBodyConverter<T> implements Converter<T, RequestBody> {
        private final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
        private final Charset UTF_8 = Charset.forName("UTF-8");

        private final Gson gson;
        private final TypeAdapter<T> adapter;

        GsonRequestBodyConverter(Gson gson, TypeAdapter<T> adapter) {
            this.gson = gson;
            this.adapter = adapter;
        }

        @Override
        public RequestBody convert(T value) throws IOException {
            Buffer buffer = new Buffer();
            Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
            JsonWriter jsonWriter = gson.newJsonWriter(writer);
            adapter.write(jsonWriter, value);
            jsonWriter.close();
            return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
        }
    }

//    /**
//     * 自定义请求RequestBody
//     */
//    public static class RequestBodyConverter<T> implements Converter<T, RequestBody> {
//        private static final MediaType MEDIA_TYPE1 = MediaType.parse("application/json;charset=UTF-8");
//        private Gson gson;
//
//
//        public RequestBodyConverter(Gson gson) {
//            this.gson = gson;
//        }
//
//        @Override
//        public RequestBody convert(T value) throws IOException {//T就是传入的参数
//
//            return RequestBody.create(MEDIA_TYPE1, gson.toJson(value));
//
//        }
//
//    }

    /**
     * 自定义响应ResponseBody
     */
    public class ResponseBodyConverter<T> implements Converter<ResponseBody, T> {
        private Gson gson;
        private Type type;

        public ResponseBodyConverter(Type type, Gson gson) {
            this.type = type;
            this.gson = gson;
        }

        /**
         * @param value
         * @return T
         * @throws IOException
         */
        @Override
        public T convert(ResponseBody value) throws IOException {
            try {
                String httpValue = value.string();
                HttpResult<Object> httpResult = new Gson().fromJson(httpValue, HttpResult.class);
                JsonParser jsonParser = new JsonParser();
                if (httpResult.getEncrypt() == HttpResult.ENCRYPT_TRUE && !TextUtils.isEmpty(httpResult.getBody().toString())) {
                    String decodeBody = EncryptUtil.decodeHttpData(httpResult);
//                    JsonObject obj = gson.fromJson(decodeBody, JsonObject.class);
                    httpResult.setBody(jsonParser.parse(decodeBody));
                    String json = gson.toJson(httpResult);
                    Logger.i("httpJson====>" + json);
                    T t = gson.fromJson(json, type);
                    return t;
                } else {
                    try {
//                        httpResult.setBody(jsonParser.parse(httpResult.getBody().toString()));
//                        String json = gson.toJson(httpResult);
                        T t = gson.fromJson(httpValue, type);
                        return t;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            } finally {
                value.close();
            }
        }

    }
}
//public class ResponseBodyConverter<T> implements Converter<ResponseBody, T> {
//
//    private boolean isAes;
//    private Type type;
//
//    public ResponseBodyConverter(Type type, boolean isAes) {
//        this.isAes = isAes;
//        this.type = type;
//    }
//
//    @Override
//    public T convert(ResponseBody value) throws IOException {
//        try {
//            HttpResult<String> httpResult = new Gson().fromJson(value.string(), HttpResult.class);
//            if (httpResult.getEncrypt() == HttpResult.ENCRYPT_TRUE) {
//                String decodeBody = EncryptUtil.decodeHttpData(httpResult);
//                return new Gson().fromJson(decodeBody, type);
//            } else {
//                String json = value.string();
//                return new Gson().fromJson(json, type);
//            }
//        } finally {
//            value.close();
//        }
//    }
//}
//
