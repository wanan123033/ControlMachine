package com.feipulai.host.activity.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.ActivityCollector;
import com.feipulai.common.utils.StatusNavUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.host.config.BaseEvent;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.utils.SharedPrefsUtil;
import com.feipulai.host.utils.SystemBrightUtils;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;

/**
 * Created by James on 2017/11/22.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
@SuppressLint("Registered")
public class BaseActivity extends Activity {

    private String mActivityName;
    public int machineCode;
    public int hostId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        StatusNavUtils.setNavigationBarColor(this, 0x33000000);
        //知晓当前是在哪一个Activity
        mActivityName = getClass().getSimpleName();
        Logger.d(mActivityName + ".onCreate");
        EventBus.getDefault().register(this);
        ActivityCollector.addActivity(this);
        SystemBrightUtils.setBrightness(this, SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS,
                SharedPrefsConfigs.BRIGHTNESS, 125));

        machineCode = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, SharedPrefsConfigs
                .DEFAULT_MACHINE_CODE);
        hostId = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.HOST_ID, 1);
        setTitle("智能主机[体测版]-" + TestConfigs.machineNameMap.get(machineCode) + hostId + "号机");
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
        ActivityCollector.removeActivity(this);
        Logger.d(mActivityName + ".onDestroy");
    }

    protected void toastSpeak(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showShort(msg);
                TtsManager.getInstance().speak(msg);
                Logger.i(msg);
            }
        });
    }

    protected static class MyHandler extends Handler {

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

    // 妈的这个还不能删
    @Subscribe
    public void onEventMainThread(BaseEvent baseEvent) {
    }

}
