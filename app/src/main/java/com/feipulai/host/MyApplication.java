package com.feipulai.host;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.feipulai.common.CrashHandler;
import com.feipulai.common.utils.ActivityLifeCycle;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.print.FontsUtil;
import com.feipulai.device.AdaptiveConfig;
import com.feipulai.device.serial.SerialParams;
import com.feipulai.host.activity.SplashScreenActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.netUtils.HttpSubscriber;
import com.feipulai.host.netUtils.netapi.UserSubscriber;
import com.orhanobut.logger.utils.LogUtils;
import com.ww.fpl.libarcface.faceserver.FaceServer;

public class MyApplication extends MultiDexApplication {
    public static final String PATH_SPECIFICATION = FileUtil.PATH_BASE + "TC/";
    public static final String PATH_IMAGE = FileUtil.PATH_BASE + "TC_IMAGE/";
    public static final String LOG_PATH_NAME = "TC_LOGGER";
    public static String SOFTWAREUUID = "FP-KTA2108_TC";//软件识别码
    public static String HARDWAREUUID = "FP-KTA2108_TC_ANDROID";//硬件识别码
    public static final String DEVICECODE = "111";//硬件识别码
    public static final String BACKUP_DIR = FileUtil.PATH_BASE + "/TC_BACKUP/";
    public static boolean RADIO_TIME_SYNC;

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
        CrashHandler.getInstance().setUploadOpersion(new CrashHandler.UploadOpersion() {
            @Override
            public void upload(String erroMsg) {
                new UserSubscriber().uploadLog(erroMsg);
                IntentUtil.gotoActivity(instance, SplashScreenActivity.class);
            }
        });
        FaceServer.ROOT_PATH = FileUtil.PATH_BASE + "TC_FACE/";
        FileUtil.createAllFile();
        FileUtil.mkdirs(PATH_SPECIFICATION);
        FileUtil.mkdirs(PATH_IMAGE);
        FileUtil.mkdirs(BACKUP_DIR);
        FileUtil.mkdirs2(FaceServer.ROOT_PATH);

        LogUtils.initLogger(true, BuildConfig.DEBUG, MyApplication.LOG_PATH_NAME);
        SerialParams.init(this);
        DBManager.getInstance();
//        registerActivityLifecycleCallbacks(new ActivityLifeCycle(SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.APP_USE_TIME));
        SOFTWAREUUID = MyApplication.getInstance().getString(R.string.software_uuid);//软件识别码
        HARDWAREUUID = MyApplication.getInstance().getString(R.string.hardware_uuid);//硬件识别码
        if (TextUtils.equals(getString(R.string.ic_card_read_type), "linNan")) {
            //        TODO 岭南IC
            AdaptiveConfig.initIC(AdaptiveConfig.LIN_NAN_SHI_FAN, AdaptiveConfig.DEFAULT, new char[]{0x73, 0x79, 0x6E, 0x70, 0x75, 0x62});
        }
    }

    public static MyApplication getInstance() {
        return instance;
    }

}
