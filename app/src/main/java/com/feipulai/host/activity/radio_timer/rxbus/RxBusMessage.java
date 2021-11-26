package com.feipulai.host.activity.radio_timer.rxbus;

/**
 * Created by pengjf on 2018/10/22.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class RxBusMessage {
    private String message ;

    public RxBusMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
