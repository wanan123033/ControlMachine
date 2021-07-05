package com.feipulai.exam;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.feipulai.common.CrashHandler;
import com.feipulai.common.utils.ActivityLifeCycle;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.AdaptiveConfig;
import com.feipulai.device.serial.SerialParams;
import com.feipulai.exam.activity.SplashScreenActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.entity.Account;
import com.feipulai.exam.netUtils.netapi.HttpSubscriber;
import com.feipulai.exam.utils.bluetooth.BlueToothHelper;
import com.kk.taurus.playerbase.config.PlayerConfig;
import com.kk.taurus.playerbase.config.PlayerLibrary;
import com.kk.taurus.playerbase.record.PlayRecordManager;
import com.orhanobut.logger.utils.LogUtils;
import com.ww.fpl.libarcface.faceserver.FaceServer;


public class MyApplication extends MultiDexApplication {

    public static final String PATH_SPECIFICATION = FileUtil.PATH_BASE + "KS/";//说明文档路径
    public static final String PATH_IMAGE = FileUtil.PATH_BASE + "KS_IMAGE/";//图片存在路径
    public static final String PATH_APK = FileUtil.PATH_BASE + "KS_APK/";//图片存在路径
    public static final String PATH_PDF_IMAGE = FileUtil.PATH_BASE + "KS_PDF_IMAGE/";//成绩图片与PDF文件存放路径
    public static final String PATH_LOG_NAME = "KS_LOGGER";//日志文件夹名称
    public static String SOFTWAREUUID = "FP-KTA2108_KS";//软件识别码
    public static String HARDWAREUUID = "FP-KTA2108_KS_ANDROID";//硬件识别码
    public static final String PATH_FACE = FileUtil.PATH_BASE + "KS_FACE_IMG/"; //人脸识别图片信息文件路径
    public static final String BACKUP_DIR = FileUtil.PATH_BASE + "/KS_BACKUP/";
    public static final String DEVICECODE = "111";//硬件识别码
    public static Account account;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private static MyApplication instance;
    //全局CONTEXT
    //private static Context mContext;
    /**
     * 绑定成功时返回的令牌
     */
    public static String TOKEN = "";
    public static String ADVANCED_PWD = "fpl2019";

    public void onCreate() {
        super.onCreate();
        instance = this;
        LogUtils.initLogger(true, true, PATH_LOG_NAME);
        CrashHandler.getInstance().init(this);
        CrashHandler.getInstance().setUploadOpersion(new CrashHandler.UploadOpersion() {
            @Override
            public void upload(String erroMsg) {
                new HttpSubscriber().uploadLog(erroMsg);
                IntentUtil.gotoActivity(instance, SplashScreenActivity.class);
            }
        });
        SettingHelper.init(this);
        BlueToothHelper.init(this);
        TOKEN = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.TOKEN, "");
        //默认打开WiFi，虹软sdk需要读物唯一标识，某些机器WiFi断开情况下读不到
//        NetUtil.openWifi(this);
        FaceServer.ROOT_PATH = FileUtil.PATH_BASE + "KS_FACE/";
        FileUtil.createAllFile();
        FileUtil.mkdirs(PATH_SPECIFICATION);
        FileUtil.mkdirs(PATH_IMAGE);
        FileUtil.mkdirs(PATH_PDF_IMAGE);
        FileUtil.mkdirs(BACKUP_DIR);
//        FileUtil.mkdirs(PATH_FACE);
        FileUtil.mkdirs(FaceServer.ROOT_PATH);
        //视频播放初始化库
        PlayerLibrary.init(this);
//        IjkPlayer.init(this);
        //如果添加了'cn.jiajunhui:exoplayer:xxxx'该依赖
//        ExoMediaPlayer.init(this);
        //播放记录的配置
        //开启播放记录
        PlayerConfig.playRecord(true);
        PlayRecordManager.setRecordConfig(
                new PlayRecordManager.RecordConfig.Builder()
                        .setMaxRecordCount(100)
                        //.setRecordKeyProvider()
                        //.setOnRecordCallBack()
                        .build());
        if (TextUtils.equals(getString(R.string.ic_card_read_type), "linNan")) {
            //        TODO 岭南IC
            AdaptiveConfig.initIC(AdaptiveConfig.LIN_NAN_SHI_FAN, AdaptiveConfig.DEFAULT, new char[]{0x73, 0x79, 0x6E, 0x70, 0x75, 0x62});
        }

        SerialParams.init(this);

//        registerActivityLifecycleCallbacks(new ActivityLifeCycle(SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.APP_USE_TIME));
        SOFTWAREUUID = MyApplication.getInstance().getString(R.string.software_uuid);//软件识别码
        HARDWAREUUID = MyApplication.getInstance().getString(R.string.hardware_uuid);//硬件识别码
    }

    public static MyApplication getInstance() {
        return instance;
    }

}
