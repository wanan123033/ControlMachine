package com.feipulai.common.jump_rope.task;

import android.util.Log;

/**
 * Created by James on 2018/8/14 0014.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class LEDResultDisplayTask extends PreciseCountDownTimer{
	
	
	private LEDResultDisplayer mLEDResultDisplayer;
	
	public LEDResultDisplayTask(LEDResultDisplayer ledResultDisplayer){
		super(Integer.MAX_VALUE,1000,0);
		this.mLEDResultDisplayer = ledResultDisplayer;
	}

	@Override
	public void onTick(long tick){
		// Log.i("james", "tick:" + tick);
		mLEDResultDisplayer.showDetails();
	}
	
	// 这个计时器没半个月时间什么的是不会结束的了
	@Override
	public void onFinish(){
	}
	
}
