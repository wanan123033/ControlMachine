package com.feipulai.host;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.feipulai.common.CrashHandler;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.print.FontsUtil;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.orhanobut.logger.utils.LogUtils;
import com.ww.fpl.libarcface.faceserver.FaceServer;

public class MyApplication extends MultiDexApplication {
    public static final String PATH_SPECIFICATION = FileUtil.PATH_BASE + "TC/";
    public static final String PATH_IMAGE = FileUtil.PATH_BASE + "TC_IMAGE/";
    public static final String LOG_PATH_NAME = "TC_LOGGER";
    public static final String SOFTWAREUUID = "FPL_KS_2020_09_01_000000";//软件识别码
    public static final String HARDWAREUUID = "FPL_ANDROID_KS_2020_09_01_000000";//硬件识别码
    public static final String DEVICECODE = "111";//硬件识别码
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
        CrashHandler.getInstance().init(this);
        LogUtils.initLogger(true,true,"/sdcard/TClogger");
        FaceServer.ROOT_PATH = FileUtil.PATH_BASE + "TC_FACE/";
        FileUtil.createAllFile();
        FileUtil.mkdirs(PATH_SPECIFICATION);
        FileUtil.mkdirs(PATH_IMAGE);
        FileUtil.mkdirs2(FaceServer.ROOT_PATH);
    }

    public static MyApplication getInstance() {
        return instance;
    }

}
