package com.feipulai.host.netUtils;

/**
 * Created by pengjf on 2018/10/9.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 *
 * 结果集的处理
 */

public class HttpResult<T> {
    
    private int state;
    private String msg ;
    private T body;
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
