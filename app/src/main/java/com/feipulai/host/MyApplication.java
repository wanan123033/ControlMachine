package com.feipulai.host;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.feipulai.common.CrashHandler;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.SharedPrefsConfigs;

public class MyApplication extends MultiDexApplication {
    public static final String PATH_SPECIFICATION = FileUtil.PATH_BASE + "TC/";

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

    public void onCreate() {
        super.onCreate();
        instance = this;
        SettingHelper.init(this);
        TOKEN = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.TOKEN, "");
        // 初始化工作已经移至mainactivity中,保证尽快进入界面,减少白屏时间
//        CrashHandler.getInstance().init(this);
        FileUtil.createAllFile();
        FileUtil.mkdirs(PATH_SPECIFICATION);
    }

    public static MyApplication getInstance() {
        return instance;
    }

}
