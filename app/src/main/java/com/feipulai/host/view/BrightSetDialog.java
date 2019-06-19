package com.feipulai.host.view;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.feipulai.host.R;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.utils.SharedPrefsUtil;
import com.feipulai.host.utils.SystemBrightUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by james on 2017/12/25.
 */
public class BrightSetDialog extends Dialog implements SeekBar.OnSeekBarChangeListener{
	
	@BindView(R.id.tv_increase)
	ImageButton mTvIncrease;
	@BindView(R.id.tv_decrease)
	ImageButton mTvDecrease;
	@BindView(R.id.skb_brightness)
	SeekBar mSkbBrightness;
	private Activity mActivity;
	private int currentProgress;
	
	public BrightSetDialog(Activity activity){
		super(activity);
		mActivity = activity;
		//init();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//去掉自定义的dialog的标题,如果不去掉现实的dialog会有一个空白的头部
		// 由此可见dialog和activity是如此的相像。
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		View view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_brightness,null);
		ButterKnife.bind(this,view);
		currentProgress = SharedPrefsUtil.getValue(mActivity,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs
				.BRIGHTNESS,125);
		//setTitle("屏幕亮度");
		mSkbBrightness.setProgress(currentProgress);
		mSkbBrightness.setOnSeekBarChangeListener(this);
		SystemBrightUtils.stopAutoBrightness(mActivity);
		setContentView(view);
		
		//dialog大小设置需要在setContentView之后
		Window dialogWindow = getWindow();
		// 宽度设置为屏幕的0.6
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		DisplayMetrics d = mActivity.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
		lp.width = (int)(d.widthPixels * 0.6);
		dialogWindow.setAttributes(lp);
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser){
		SystemBrightUtils.setBrightness(mActivity,progress);
		currentProgress = progress;
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar){
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar){
		// TODO: 2017/12/25 17:48 设置完成亮度,对话框自动退出功能暂时不做
		SharedPrefsUtil.putValue(mActivity,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.BRIGHTNESS,seekBar.getProgress());
		//Log.e("james",seekBar.getProgress() + "");
	}
	
	@OnClick({R.id.tv_increase,R.id.tv_decrease})
	public void onViewClicked(View view){
		switch(view.getId()){
			
			case R.id.tv_increase:
				if(currentProgress + 15 >= 255){
					currentProgress = 255;
				}else{
					currentProgress += 15;
				}
				mSkbBrightness.setProgress(currentProgress);
				break;
			
			case R.id.tv_decrease:
				if(currentProgress - 15 <= 0){
					currentProgress = 0;
				}else{
					currentProgress -= 15;
				}
				mSkbBrightness.setProgress(currentProgress);
				break;
		}
	}
	
}
