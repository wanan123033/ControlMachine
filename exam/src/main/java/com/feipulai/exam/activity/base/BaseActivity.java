package com.feipulai.exam.activity.base;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.ActivityCollector;
import com.feipulai.common.utils.ActivityUtils;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    /**
     * 广播接收器
     */
//    public BroadcastReceiver receiver;

    /**
     * 广播过滤器
     */
//    public IntentFilter filter = new IntentFilter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //应用运行时，保持屏幕高亮，不锁屏
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        // StatusNavUtils.setNavigationBarColor(this, 0x33000000);
        EventBus.getDefault().register(this);
        //知晓当前是在哪一个Activity
        mActivityName = getClass().getSimpleName();
        LogUtils.life(mActivityName + "onCreate");
        //未捕获异常处理,重启防止崩溃
        //Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));
        ActivityCollector.getInstance().onCreate(this);
        //SystemBrightUtils.setBrightness(this, SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS,
        //        SharedPrefsConfigs.BRIGHTNESS, 125));
        machineCode = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, SharedPrefsConfigs
                .DEFAULT_MACHINE_CODE);


//        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
//        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//        registerReceiver(receiver = new WifiReceiver(), filter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Logger.i(newConfig.toString());
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogUtils.life(mActivityName + "onRestart");
    }

    @Override
    protected void onStart() {
        LogUtils.life(mActivityName + "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.life(mActivityName + "onResume");
        // 机器信息为依据,显示title 在onResume方法设置标题避免设置中进行设置名称后未更新标题
//        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
//            setTitle("智能主机[考试版]-" + TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机");
//        } else {
//            setTitle("智能主机[考试版]-" + TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机-" + SettingHelper.getSystemSetting().getTestName());
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.life(mActivityName + "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.life(mActivityName + "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.life(mActivityName + "onDestroy");
//        if (null != receiver) {
//            unregisterReceiver(receiver);
//        }
        ActivityCollector.getInstance().onDestroy(this);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // 判断连续点击事件时间差
            if (ActivityUtils.isFastClick()) {
                return true;
            }
        }
        return super.dispatchTouchEvent(event);
    }


    protected void toastSpeak(final String msg) {
        LogUtils.operation("页面提示:" + msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 播报时间间隔必须>500ms
                long tmp = System.currentTimeMillis();
                if (tmp - lastBroadcastTime > 500) {
                    lastBroadcastTime = tmp;
                    ToastUtils.showShort(msg);
                    TtsManager.getInstance().speak(msg);
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
        LogUtils.operation("页面提示:" + toastMsg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 播报时间间隔必须>500ms
                long tmp = System.currentTimeMillis();
                if (tmp - lastBroadcastTime > 500) {
                    lastBroadcastTime = tmp;
                    ToastUtils.showShort(toastMsg);
                    TtsManager.getInstance().speak(speakMsg);
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BaseEvent baseEvent) {
//        if (baseEvent.getTagInt() == EventConfigs.TOKEN_ERROR) {
//            startActivity(new Intent(this, LoginActivity.class));
//        }
    }

    @Subscribe
    public void onEvent(BaseEvent event) {

    }

    @Subscribe
    public void onEventAsync(BaseEvent event) {

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