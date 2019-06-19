package com.feipulai.common.jump_rope.task;


import android.text.TextUtils;

import com.feipulai.device.led.LEDManager;

/**
 * Created by James on 2018/8/1 0001.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class LEDCheckDisplayTask implements Runnable {
	
	private LEDManager mLEDManager = new LEDManager();
	private OnLedDisplayListener listener;
	private volatile boolean isFinished;
	private volatile boolean isWait3Sec = false;
	private volatile boolean mIsLEDDisplaying;
	
	public LEDCheckDisplayTask(OnLedDisplayListener listener) {
		this.listener = listener;
	}
	
	public void finish() {
		isFinished = true;
	}
	
	public void wait3Sec() {
		isWait3Sec = true;
	}
	
	public void pause() {
		mIsLEDDisplaying = false;
	}
	
	public void resume() {
		mIsLEDDisplaying = true;
	}
	
	@Override
	public void run() {
		int total;
		boolean isDropToClear;
		try {
			while (!isFinished) {
				total = 0;
				isDropToClear = false;
				for (int i = 0; i < listener.getDeviceCount() && mIsLEDDisplaying; i++) {
					// 如果刚才显示了一个具体的检录人信息,这里等3s再继续
					if (isWait3Sec) {
						// 在一次显示过程中(没有刷新),有学生检录,isDropToClear为true,将接下来的一些学生信息筛选掉,直到LED显示下一屏
						Thread.sleep(3000);
						isDropToClear = true;
						isWait3Sec = false;
					}
					// Student student = listener.getStuInPosition(i);
					
					//只显示有学生信息配对好的手柄
					int currentY = total % 4;
					boolean clearScreen = total % 4 == 0;
					boolean updateScreen = total % 4 == 3;
					boolean isLast = i == listener.getDeviceCount() - 1;
					
					String strToShow = listener.getStringToShow(i);
					
					if (!TextUtils.isEmpty(strToShow)) {
						if (isDropToClear) {
							if (clearScreen) {
								isDropToClear = false;
							} else {
								continue;
							}
						}
						//保证最后会睡眠
						updateScreen = isLast || updateScreen;
						mLEDManager.showString(
								listener.getHostId(),
								strToShow,
								0,
								currentY,
								clearScreen,
								updateScreen);
						total++;
					} else if (isLast && !clearScreen) {
						if (!isDropToClear) {
							//最后一个元素没有学生,且不需要新开一个屏幕,最后一行就显示为空行,更新屏幕,将之前屏幕缓存的内容显示出来
							mLEDManager.showString(
									listener.getHostId(),
									"                ",
									0,
									currentY,
									false,
									true);
							updateScreen = true;
						}
					} else {
						//跳过,保证不会出现不正常的睡眠
						continue;
					}
					//显示完成本界面后停留3s,再刷新整个界面
					//或者根本没有在显示,停留3s
					if (updateScreen || !mIsLEDDisplaying) {
						Thread.sleep(3000);
					}
				}
				//当前没有人检录,等待,避免总是循环,这台机器受不了
				if (total == 0) {
					Thread.sleep(3000);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public interface OnLedDisplayListener {
		/**
		 * 这里返回设备具体数量,用于获取设备信息时指定
		 *
		 * @return 设备具体数量
		 */
		int getDeviceCount();
		
		// /**
		//  * 获取第position个位置的学生信息,没有就返回null
		//  *
		//  * @param position 位置
		//  * @return 第position个位置的学生信息, 没有就返回null
		//  */
		// Student getStuInPosition(int position);
		
		/**
		 * 对于第position个位置,LED屏幕应显示的内容
		 *
		 * @param position 位置
		 * @return LED屏幕应显示的内容
		 */
		String getStringToShow(int position);
		
		/**
		 * 返回主机号
		 *
		 * @return 返回主机号
		 */
		int getHostId();
	}
	
}
