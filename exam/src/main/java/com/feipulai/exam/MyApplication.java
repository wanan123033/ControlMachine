package com.feipulai.exam;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.feipulai.common.CrashHandler;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.utils.bluetooth.BlueToothHelper;
import com.kk.taurus.playerbase.config.PlayerConfig;
import com.kk.taurus.playerbase.config.PlayerLibrary;
import com.kk.taurus.playerbase.record.PlayRecordManager;
import com.orhanobut.logger.utils.LogUtils;
import com.ww.fpl.libarcface.faceserver.FaceServer;


public class MyApplication extends MultiDexApplication {

    public static final String PATH_SPECIFICATION = FileUtil.PATH_BASE + "KS/";
    public static final String PATH_IMAGE = FileUtil.PATH_BASE + "KS_IMAGE/";

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
        LogUtils.initLogger(true, true);
//        CrashHandler.getInstance().init(this);
        SettingHelper.init(this);
        BlueToothHelper.init(this);
        TOKEN = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.TOKEN, "");
        //默认打开WiFi，虹软sdk需要读物唯一标识，某些机器WiFi断开情况下读不到
//        NetUtil.openWifi(this);
        FaceServer.ROOT_PATH = FileUtil.PATH_BASE + "KS_FACE/";
        FileUtil.createAllFile();
        FileUtil.mkdirs(PATH_SPECIFICATION);
        FileUtil.mkdirs(PATH_IMAGE);
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
