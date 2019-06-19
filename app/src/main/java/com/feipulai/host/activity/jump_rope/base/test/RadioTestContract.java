package com.feipulai.host.activity.jump_rope.base.test;


import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;

import java.util.List;

/**
 * Created by James on 2019/1/22 0022.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public interface RadioTestContract {
	
	interface Presenter {
		
		void start();
		
		void stopUse();
		
		void resumeUse();
		
		void startTest();
		
		void restartTest();
		
		void quitTest();
		
		int stateOfPosition(int position);
		
		void setFocusPosition(int position);
		
		void stopNow();
		
		void confirmResults();
		
		void finishTest();
		
	}
	
	interface View<Setting> {
		
		void initView(List<StuDevicePair> pairs, Setting setting);
		
		void updateSpecificItem(int index);
		
		void tickInUI(String text);
		
		// void updateStates();
		
		void enableStopRestartTest(boolean enable);
		
		void showWaitFinalResultDialog(boolean showDialog);
		
		void setViewForStart();
		
		void quitTest();
		
		void finishTest();
		
		void enableStopUse(boolean enable);
		
		void showViewForConfirmResults();
		
		void showDisconnectForFinishTest();
	}
	
}
