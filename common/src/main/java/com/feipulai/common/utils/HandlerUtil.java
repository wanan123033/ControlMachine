package com.feipulai.common.utils;

import android.os.Handler;
import android.os.Message;

/**
 * handler封装工具类
 *
 */
public class HandlerUtil {

    /**
     * 发送消息.
     *
     * @param handler 异步处理对象.
     * @param what    消息.
     */
    public static void sendMessage(Handler handler, int what) {
        Message message = handler.obtainMessage();
        message.what = what;
        handler.sendMessage(message);
    }

    /**
     * 发送消息.
     *
     * @param handler 异步处理对象.
     * @param what    消息.
     * @param object  传递对象.
     */
    public static void sendMessage(Handler handler, int what, Object object) {
        Message message = handler.obtainMessage();
        message.what = what;
        message.obj = object;
        handler.sendMessage(message);
    }

    /**
     * 发送消息
     *
     * @param handler
     * @param what
     * @param arg1
     * @param object
     */
    public static void sendMessage(Handler handler, int what, int arg1, Object object) {
        Message message = handler.obtainMessage();
        message.what = what;
        message.obj = object;
        message.arg1 = arg1;
        handler.sendMessage(message);
    }

    /**
     * 发送消息.
     *
     * @param handler 异步处理对象.
     * @param what    消息.
     * @param arg1    消息.
     * @param arg2    消息.
     */
    public static void sendMessage(Handler handler, int what, int arg1, int arg2) {
        Message message = handler.obtainMessage();
        message.what = what;
        message.arg1 = arg1;
        message.arg2 = arg2;
        handler.sendMessage(message);
    }

    /**
     * 发送消息.
     *
     * @param handler 异步处理对象.
     * @param what    消息.
     * @param arg1    消息.
     * @param arg2    消息.
     * @param object  传递对象.
     */
    public static void sendMessage(Handler handler, int what, int arg1, int arg2, Object object) {
        Message message = handler.obtainMessage();
        message.what = what;
        message.arg1 = arg1;
        message.arg2 = arg2;
        message.obj = object;
        handler.sendMessage(message);
    }

    /**
     * 发送全局消息
     *
     * @param what
     * @version 1.0
     * @updateInfo
     */
    public static void sendEmptyMessage(int what) {
        Handler handler = new Handler();
        handler.sendEmptyMessage(what);
    }

}
