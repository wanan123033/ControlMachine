package com.feipulai.host.netUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by pengjf on 2018/10/9.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 * <p>
 * 结果集的处理
 */

public class HttpResult<T> implements Serializable {
    @SerializedName("code")
    private int state;
    private String msg;
    @SerializedName("data")
    private T body;
    private String sign;
    private int encrypt = ENCRYPT_FALSE;
    public static int ENCRYPT_TRUE = 1;
    public static int ENCRYPT_FALSE = 0;

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public int getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(int encrypt) {
        this.encrypt = encrypt;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }


}
