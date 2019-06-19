package com.feipulai.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;


/**
 * 跳转工具类.
 */
public class IntentUtil {


    /**
     * 跳转至指定activity.
     *
     * @param context   上下文环境.
     * @param gotoClass 跳转至界面.
     * @version 1.0
     * @updateInfo
     */
    public static void gotoActivity(Context context, Class<?> gotoClass) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        context.startActivity(intent);
    }

    /**
     * 携带传递数据跳转至指定activity.
     *
     * @param context   上下文环境.
     * @param gotoClass 跳转至界面.
     * @param bundle    携带数据.
     * @updateInfo
     */
    public static void gotoActivity(Context context, Class<?> gotoClass, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        intent.putExtras(bundle);
        context.startActivity(intent);

    }

    /**
     * 跳转至指定activity,并关闭当前activity.
     *
     * @param context   上下文环境.
     * @param gotoClass 跳转至界面.
     * @updateInfo
     */
    public static void gotoActivityAndFinish(Context context, Class<?> gotoClass) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    /**
     * 携带传递数据跳转至指定activity,并关闭当前activity.
     *
     * @param context   上下文环境.
     * @param gotoClass 跳转至界面.
     * @param bundle    携带数据.
     * @version 1.0
     * @updateInfo
     */
    public static void gotoActivityAndFinish(Context context, Class<?> gotoClass, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        intent.putExtras(bundle);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    /**
     * 携带传递数据跳转至指定activity,并关闭当前activity.
     *
     * @param context   上下文环境.
     * @param gotoClass 跳转至界面.
     * @version 1.0
     * @updateInfo
     */
    public static void gotoActivityToTop(Context context, Class<?> gotoClass) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 携带传递数据跳转至指定activity,并关闭当前activity.
     *
     * @param context   上下文环境.
     * @param gotoClass 跳转至界面.
     * @param bundle    携带数据.
     * @version 1.0
     * @updateInfo
     */
    public static void gotoActivityToTop(Context context, Class<?> gotoClass, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    /**
     * 单例模式跳转至指定activity
     *
     * @param context   上下文环境.
     * @param gotoClass 跳转至界面.
     * @param context   上下文
     * @param gotoClass 目标activity
     * @version 1.0
     */
    public static void gotoActivityToTopAndFinish(Context context, Class<?> gotoClass) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    /**
     * 单例模式跳转并携带数据
     *
     * @param context
     * @param gotoClass
     * @param bundle
     */
    public static void gotoActivityToTopAndFinish(Context context, Class<?> gotoClass, Bundle bundle) {
        Intent intent = new Intent();
        intent.putExtras(bundle);
        intent.setClass(context, gotoClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    /**
     * 跳转至指定activity.
     *
     * @param context     上下文环境.
     * @param gotoClass   跳转至界面.
     * @param requestCode 请求码.
     */
    public static void gotoActivityForResult(Context context, Class<?> gotoClass, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    /**
     * 携带传递数据跳转至指定activity.
     *
     * @param context     上下文环境.
     * @param gotoClass   跳转至界面.
     * @param bundle      携带数据.
     * @param requestCode 请求码.
     */
    public static void gotoActivityForResult(Context context, Class<?> gotoClass, Bundle bundle, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        intent.putExtras(bundle);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    /**
     * 跳转至指定activity.
     * (从Fragment中跳转到其它页面，必须用fragment对象调用startActivityForResult（...）,否则收不到回调)
     *
     * @param context     上下文环境.
     * @param gotoClass   跳转至界面.
     * @param requestCode 请求码.
     */
    public static void gotoActivityForResult(Context context, Fragment fragment, Class<?> gotoClass, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 跳转至指定activity.
     * (从Fragment中跳转到其它页面，必须用fragment对象调用startActivityForResult（...）,否则收不到回调)
     *
     * @param context     上下文环境.
     * @param gotoClass   跳转至界面.
     * @param requestCode 请求码.
     */
    public static void gotoActivityForResult(Context context, Fragment fragment, Class<?> gotoClass, Bundle bundle, int requestCode) {
        Intent intent = new Intent();
        intent.putExtras(bundle);
        intent.setClass(context, gotoClass);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 携带传递数据跳转至指定activity.
     *
     * @param context     上下文环境.
     * @param gotoClass   跳转至界面.
     * @param bundle      携带数据.
     * @param requestCode
     */
    public static void gotoActivityToTopForResult(Context context, Class<?> gotoClass, Bundle bundle, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(context, gotoClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    /**
     * 跳转到发送短信界面
     *
     * @param context
     * @param phoneNum
     * @param content
     */
    public static void gotoSendMsmActivity(Context context, String phoneNum, String content) {
        Uri uri = Uri.parse("smsto:" + phoneNum);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra("sms_body", content);
        context.startActivity(intent);
    }

    /**
     * 描述：关闭某个activity
     * <p/>
     *
     * @param activity 需要关闭的activity
     */
    public static void finish(Activity activity) {
        activity.finish();
    }

}
