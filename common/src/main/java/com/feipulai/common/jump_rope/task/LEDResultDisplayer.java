package com.feipulai.common.jump_rope.task;


import android.text.TextUtils;

import com.feipulai.device.led.LEDManager;

/**
 * Created by James on 2018/8/14 0014.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class LEDResultDisplayer {

	private int hostId;
	private int size;// 显示的大小
	private int position;
	private LEDManager mLEDManager = new LEDManager();
	private LEDContentGenerator generator;
	private static final String EMPTY_LINE = "                ";


	public LEDResultDisplayer(int size, int hostId, LEDContentGenerator generator) {
		this.size = size;
		this.hostId = hostId;
		this.generator = generator;
	}

	public void showDetails() {
		boolean hasInPage = false;// 本页是否有考生显示
		for (int currentY = 1;
			 currentY <= 3;
			 currentY++) {

			boolean updateScreen = currentY == 3;// 是否需要刷新LED屏
			boolean isLast = position == size - 1;

			String showContent = generator.generate(position);
			boolean hasStudentHere = !TextUtils.isEmpty(showContent);// 当前位置是否有考生

			if (hasStudentHere) {
				hasInPage = true;
			}

			// 到了最后,不管怎样,都是退出的时候了
			if (isLast) {
				if (hasInPage) {
					mLEDManager.showString(hostId, hasStudentHere ? showContent : EMPTY_LINE,
							0, currentY, false, updateScreen);
					if (!updateScreen) {// 填充满一页
						for (int j = currentY + 1; j <= 3; j++) {
                            mLEDManager.showString(hostId, EMPTY_LINE, 0, j, false, j == 3);
                        }
                    }
                    position = 0;
                }
                return;
			}

			if (!hasStudentHere) {
				currentY--;// 回退
				position = (position + 1) % size;
				continue;// 没人,不用显示
			}
			position = (position + 1) % size;
			mLEDManager.showString(hostId, showContent, 0, currentY, false, updateScreen);
		}
	}

}
