package com.feipulai.common.jump_rope.task;

import android.util.Log;

/**
 * Created by James on 2018/3/23 0023.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 * <p>
 * 精确的倒计时线程(使用纳秒)
 */
public abstract class PreciseCountDownTimer implements Runnable{
	
	private final long millisInFuture;
	private long interval;
	private long delay;
	private volatile boolean mCancelled;
	private long mStartTime;
	
	/**
	 * @param millisInFuture 倒计时毫秒数
	 * @param interval       倒计时间隔时间    ms
	 * @param delay          倒计时开始延时时间  ms
	 */
	public PreciseCountDownTimer(long millisInFuture,long interval,long delay){
		mStartTime = System.nanoTime();
		this.millisInFuture = millisInFuture;
		this.interval = interval;
		this.delay = delay;
		if(millisInFuture < 0 || interval < 0 || delay < 0){
			throw new IllegalArgumentException("参数不能<0");
		}
	}
	
	/**
	 * Callback fired on regular interval.
	 *
	 * @param tick The amount of tciks until finished.
	 */
	public abstract void onTick(long tick);
	
	/**
	 * Callback fired when the time is up.
	 */
	public abstract void onFinish();
	
	/**
	 * Cancel the countdown.
	 */
	public final void cancel(){
		mCancelled = true;
	}
	
	@Override
	public void run(){
		if(mCancelled){
			return;
		}
		mStartTime = mStartTime + delay * 1000_000;
		long endTime = mStartTime + millisInFuture * 1000_000;
		while(System.nanoTime() < mStartTime){
			try{
				Thread.sleep(10);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		
		long leftTime = (endTime - System.nanoTime()) / 1000_000;//转成毫秒数
		long leftTick = leftTime / interval + 1;
		if(!mCancelled){
			onTick(leftTick);
		}
		
		while(System.nanoTime() < endTime && !mCancelled){
			leftTime = (endTime - System.nanoTime()) / 1000_000;
			long left = leftTime / interval + 1;
			if(leftTick != left){
				leftTick = left;
				if(leftTick != 0 && !mCancelled){
					onTick(leftTick);
				}
			}
		}

		if(!mCancelled){
			onFinish();
		}

	}
	
}
