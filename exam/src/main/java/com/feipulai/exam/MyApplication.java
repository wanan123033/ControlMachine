package com.feipulai.exam;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.feipulai.common.CrashHandler;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.LeakReference;
import com.squareup.leakcanary.LeakTraceElement;


public class MyApplication extends MultiDexApplication {


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private static MyApplication instance;
    //全局context
    //private static Context mContext;
    /**
     * 绑定成功时返回的令牌
     */
    public static String TOKEN = "";
    public static String ADVANCED_PWD = "fpl2019";

    public void onCreate() {
        super.onCreate();
        instance = this;
        CrashHandler.getInstance().init(this);
        SettingHelper.init(this);
        TOKEN = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.TOKEN, "");



        // 初始化工作已经移至mainactivity中,保证尽快进入界面,减少白屏时间
         if (LeakCanary.isInAnalyzerProcess(this)) {
             // This process is dedicated to LeakCanary for heap analysis.
             // You should not init your app in this process.
             return;
         }
         LeakCanary.install(this);
        // Normal app init code...
    }

    public static MyApplication getInstance() {
        return instance;
    }

}
