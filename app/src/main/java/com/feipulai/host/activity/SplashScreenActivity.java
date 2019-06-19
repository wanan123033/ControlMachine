package com.feipulai.host.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.LogUtils;
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.BuildConfig;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.tts.TtsConfig;

/**
 * 现在这完全是一个程序欢迎界面了
 */
public class SplashScreenActivity extends BaseActivity{
	
	public static final String MACHINE_CODE = "machine_code";
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		// 这里是否还需要延时需要再测试后再修改
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run(){
				init();
				startActivity(new Intent(SplashScreenActivity.this,MainActivity.class));
				finish();
			}
		},500);
	}
	
	private void init(){
//		SoundPlayUtils.init(MyApplication.getInstance());
//		LogUtils.initLogger(BuildConfig.DEBUG,BuildConfig.DEBUG);
		ToastUtils.init(getApplicationContext());
		//这里初始化时间很长,大约需要3s左右
		TtsManager.getInstance().init(this,TtsConfig.APP_ID,TtsConfig.APP_KEY,TtsConfig.SECRET_KEY);
		

		new Thread(new Runnable() {
            @Override
            public void run() {
                SoundPlayUtils.init(MyApplication.getInstance());
                LogUtils.initLogger(BuildConfig.DEBUG,BuildConfig.DEBUG);
                //这里初始化时间很长,大约需要3s左右
//                TtsManager.getInstance().init(SplashScreenActivity.this,TtsConfig.APP_ID,TtsConfig.APP_KEY,TtsConfig.SECRET_KEY);
            }
        }).start();
	}
	
}
