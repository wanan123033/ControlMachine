package com.feipulai.host.activity.base;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.ActivityCollector;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.host.config.BaseEvent;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

/**
 * Created by James on 2017/11/22.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
@SuppressLint("Registered")
public class BaseActivity extends FragmentActivity {

    private String mActivityName;
    public int machineCode;
    //    public int hostId;
    private long lastBroadcastTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        super.onCreate(savedInstanceState);
//        StatusNavUtils.setNavigationBarColor(this, 0x33000000);
        //知晓当前是在哪一个Activity
        mActivityName = getClass().getSimpleName();
        Logger.d(mActivityName + ".onCreate");
        EventBus.getDefault().register(this);
        ActivityCollector.getInstance().onCreate(this);
//        SystemBrightUtils.setBrightness(this, SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS,
//                SharedPrefsConfigs.BRIGHTNESS, 125));

        machineCode = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, SharedPrefsConfigs
                .DEFAULT_MACHINE_CODE);
//        hostId = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.HOST_ID, 1);
//        setTitle("智能主机[体测版]-" + TestConfigs.machineNameMap.get(machineCode) + hostId + "号机");
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Logger.i(newConfig.toString());
        super.onConfigurationChanged(newConfig);
    }
    @Override
    protected void onRestart() {
        Logger.d(mActivityName + ".onRestart");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Logger.d(mActivityName + ".onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Logger.d(mActivityName + ".onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Logger.d(mActivityName + ".onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Logger.d(mActivityName + ".onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        ActivityCollector.getInstance().onDestroy(this);
        Logger.d(mActivityName + ".onDestroy");
    }

    protected void toastSpeak(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 播报时间间隔必须>500ms
                LogUtils.operation("语音提示:"+msg);
                long tmp = System.currentTimeMillis();
                if (tmp - lastBroadcastTime > 500) {
                    lastBroadcastTime = tmp;
                    ToastUtils.showShort(msg);
                    TtsManager.getInstance().speak(msg);
                    Logger.i(msg);
                }
            }
        });
    }

    public void toastSpeak(final String speakMsg, final String toastMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 播报时间间隔必须>500ms
                long tmp = System.currentTimeMillis();
                if (tmp - lastBroadcastTime > 500) {
                    lastBroadcastTime = tmp;
                    ToastUtils.showShort(toastMsg);
                    TtsManager.getInstance().speak(speakMsg);
                    Logger.i(toastMsg);
                }
            }
        });
    }

    public static class MyHandler extends Handler {

        private WeakReference<BaseActivity> mWeakReference;

        public MyHandler(BaseActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseActivity activity = mWeakReference.get();
            if (activity == null) {
                return;
            }
            activity.handleMessage(msg);
        }

    }

    protected void handleMessage(Message msg) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BaseEvent baseEvent) {
    }

}
