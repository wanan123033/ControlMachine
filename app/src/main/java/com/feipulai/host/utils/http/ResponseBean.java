package com.feipulai.host.utils.http;

/**
 * 返回状态属性.
 * <p/>
 * <br/> Author:zzs
 */
public class ResponseBean<T> {

    /**
     * 返回状态码
     */
    private int state;
    /**
     * 返回消息
     */
    private String msg;
    /**
     * 返回数据
     */
    private T body;

    public ResponseBean() {

    }

    /**
     * 构造函数
     * <p/>
     * <br/> Version: 1.0
     * <br/> CreateAuthor:  CodeApe
     * <br/> UpdateAuthor:  CodeApe
     * <br/> UpdateInfo:  (此处输入修改内容,若无修改可不写.)
     *
     * @param state 状态码
     * @param msg   状态描述符
     * @param body  附带信息
     */
    public ResponseBean(int state, String msg, T body) {
        this.state = state;
        this.msg = msg;
        this.body = body;
    }


    /**
     * 判断请求是否成功.
     *
     * @return ture为请求成功, false为请求失败.
     */
    public boolean isSuccess() {
        if (RESPONSE_STATUS_SUCCESS == state) {
            return true;
        } else {
            return false;
        }
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


    @Override
    public String toString() {
        return "ResponseBean{" +
                "state=" + state +
                ", msg='" + msg + '\'' +
                ", body=" + body +
                +'\'' +
                '}';
    }

    /**
     * 请求接口数据成功状态码
     */
    public static final int RESPONSE_STATUS_SUCCESS = 0;
    /**
     * 网络异常响应码
     */
    public static final int STATUS_NET_ERROR = -101;
}
