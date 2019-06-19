package com.feipulai.exam.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.LogUtils;
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.BuildConfig;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseActivity;

/**
 * 现在这完全是一个程序欢迎界面了
 */
public class SplashScreenActivity extends BaseActivity {
	
    public static final String MACHINE_CODE = "machine_code";
	public static final String APP_ID = "15431629";
	public static final String APP_KEY = "ffHKDmoM0Rfwbh96lfIaKkG5";
	public static final String SECRET_KEY = "8EhfDpV69gVojj8R9jYPU3kGSX1VkWjB";
	
	// TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 这里是否还需要延时需要再测试后再修改
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                init();
                Intent intent = new Intent();
                intent.setClass(SplashScreenActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }, 500);
    }

    private void init() {
        SoundPlayUtils.init(MyApplication.getInstance());
	    LogUtils.initLogger(BuildConfig.DEBUG,BuildConfig.DEBUG);
        ToastUtils.init(getApplicationContext());
        //这里初始化时间很长,大约需要3s左右
        TtsManager.getInstance().init(this,APP_ID,APP_KEY,SECRET_KEY);
        // Log.i("james", CommonUtils.getDeviceInfo());
        // AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        // mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, max,0);
    }

}
