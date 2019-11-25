package com.feipulai.exam;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.feipulai.common.CrashHandler;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.AdaptiveConfig;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.SharedPrefsConfigs;


public class MyApplication extends MultiDexApplication {

    public static final String PATH_SPECIFICATION = FileUtil.PATH_BASE + "KS/";

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
        //默认打开WiFi，虹软sdk需要读物唯一标识，某些机器WiFi断开情况下读不到
//        NetUtil.openWifi(this);

        FileUtil.createAllFile();
        FileUtil.mkdirs(PATH_SPECIFICATION);
////        TODO 岭南IC
//        AdaptiveConfig.initIC(AdaptiveConfig.LIN_NAN_SHI_FAN, AdaptiveConfig.DEFAULT, new char[]{0x73, 0x79, 0x6E, 0x70, 0x75, 0x62});

        // 初始化工作已经移至mainactivity中,保证尽快进入界面,减少白屏时间
//         if (LeakCanary.isInAnalyzerProcess(this)) {
//             // This process is dedicated to LeakCanary for heap analysis.
//             // You should not init your app in this process.
//             return;
//         }
//         LeakCanary.install(this);
        // Normal app init code...
    }

    public static MyApplication getInstance() {
        return instance;
    }

}
