package com.feipulai.common.jump_rope.task;

import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.device.led.LEDManager;

/**
 * Created by James on 2018/8/14 0014.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class GetReadyCountDownTimer extends PreciseCountDownTimer{
	
	private int hostId;
	private LEDManager mLEDManager = new LEDManager();
	private onGetReadyTimerListener listener;

	public GetReadyCountDownTimer(long millisInFuture,long interval,int hostId,onGetReadyTimerListener listener){
		super(millisInFuture,interval,0);
		this.hostId = hostId;
		this.listener = listener;
	}
	
	@Override
	public void onTick(long tick){
		if(tick <= 5){
			SoundPlayUtils.play((int)tick);
		}
		if(listener != null){
			listener.beforeTick(tick);
		}
		StringBuilder showSB = new StringBuilder();
		for(int j = 0;j < 5;j++){
			showSB.append(j < tick ? "●" : "○");
		}
		mLEDManager.showString(hostId,showSB.toString(),3,1,true,true);
		
		if(listener != null){
			listener.afterTick(tick);
		}
	}
	
	@Override
	public void onFinish(){
		SoundPlayUtils.play(11);
		
		if(listener != null){
			listener.onFinish();
		}
	}
	
	public interface onGetReadyTimerListener{
		
		void beforeTick(long tick);
		
		void afterTick(long tick);
		
		void onFinish();
		
	}
	
}
