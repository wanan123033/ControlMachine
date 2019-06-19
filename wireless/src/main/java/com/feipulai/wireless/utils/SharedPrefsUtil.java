package com.feipulai.wireless.utils;

import android.content.Context;

/**
 * Created by pengjf on 2019/2/18.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class SharedPrefsUtil {
    /** 数据存储的XML名称 **/
    public final static String NAME = "feipulai.wireless";

    /**
     * 存储数据(Long)
     */
    public static void putLongValue(Context context, String key, long value) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putLong(key, value).commit();
    }

    /**
     * 存储数据(Int)
     */
    public static void putIntValue(Context context, String key, int value) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putInt(key, value).commit();
    }

    /**
     * 存储数据(String)
     */
    public static void putStringValue(Context context, String key, String value) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putString(key, value).commit();
    }

    /**
     * 存储数据(boolean)
     */
    public static void putBooleanValue(Context context, String key,
                                       boolean value) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putBoolean(key, value).commit();
    }

    /**
     * 取出数据(Long)
     */
    public static long getLongValue(Context context, String key, long defValue) {
        return context.getSharedPreferences(NAME,Context.MODE_PRIVATE).getLong(key, defValue);
    }

    /**
     * 取出数据(int)
     */
    public static int getIntValue(Context context, String key, int defValue) {
        return context.getSharedPreferences(NAME,Context.MODE_PRIVATE).getInt(key, defValue);
    }

    /**
     * 取出数据(boolean)
     */
    public static boolean getBooleanValue(Context context, String key,
                                          boolean defValue) {
        return context.getSharedPreferences(NAME,Context.MODE_PRIVATE).getBoolean(key, defValue);
    }

    /**
     * 取出数据(String)
     */
    public static String getStringValue(Context context, String key,
                                        String defValue) {
        return context.getSharedPreferences(NAME,Context.MODE_PRIVATE).getString(key, defValue);
    }

    /**
     * 清空所有数据
     */
    public static void clear(Context context) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().clear().commit();
    }

    /**
     * 移除指定数据
     */
    public static void remove(Context context, String key) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().remove(key).commit();
    }

}
