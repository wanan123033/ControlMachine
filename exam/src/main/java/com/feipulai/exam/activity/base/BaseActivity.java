package com.feipulai.exam.activity.base;

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
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;


/**
 * Created by pengjf on 2018/11/16.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

@SuppressLint("Registered")
public class BaseActivity extends FragmentActivity {
    private String mActivityName;
    private long lastBroadcastTime;
    public int machineCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        // StatusNavUtils.setNavigationBarColor(this, 0x33000000);
        EventBus.getDefault().register(this);
        //知晓当前是在哪一个Activity
        mActivityName = getClass().getSimpleName();
        Logger.d(mActivityName + ".onCreateView");
        //未捕获异常处理,重启防止崩溃
        //Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));
        ActivityCollector.getInstance().onCreate(this);
        //SystemBrightUtils.setBrightness(this, SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS,
        //        SharedPrefsConfigs.BRIGHTNESS, 125));
        machineCode = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, SharedPrefsConfigs
                .DEFAULT_MACHINE_CODE);
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

        // 机器信息为依据,显示title 在onResume方法设置标题避免设置中进行设置名称后未更新标题
//        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
//            setTitle("智能主机[考试版]-" + TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机");
//        } else {
//            setTitle("智能主机[考试版]-" + TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机-" + SettingHelper.getSystemSetting().getTestName());
//        }
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
        ActivityCollector.getInstance().onDestroy(this);
        EventBus.getDefault().unregister(this);
        Logger.d(mActivityName + ".onDestroy");
    }

    protected void toastSpeak(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 播报时间间隔必须>500ms
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

//    protected void toastBatchSpeak(final String msg) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                // 播报时间间隔必须>500ms
//                long tmp = System.currentTimeMillis();
//                if (tmp - lastBroadcastTime > 500) {
//                    lastBroadcastTime = tmp;
//                    ToastUtils.showShort(msg);
//                    TtsManager.getInstance().batchSpeak(msg);
//                    Logger.i(msg);
//                }
//            }
//        });
//    }

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
                    Logger.i(toastMsg.toString());
                }
            }
        });
    }

    @Subscribe
    public void onEventMainThread(BaseEvent baseEvent) {
//        if (baseEvent.getTagInt() == EventConfigs.TOKEN_ERROR) {
//            startActivity(new Intent(this, LoginActivity.class));
//        }
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
    /**
     * 重写方法返回 true 可以显示 menu
     */
    //public boolean getIsShowMenu(){
    //return false;
    //}
}