package com.feipulai.exam.activity.jump_rope.test;

import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.setting.JumpRopeSetting;

import java.util.List;

/**
 * Created by James on 2019/1/22 0022.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public interface JumpRopeTestContract {
	
	interface Presenter {
		
		void start();
		
		void startTest();
		
		void changeBadDevice();
		
		void setFocusPosition(int position);
		
		void cancelChangeBad();
		
	}
	
	interface View {
		void initView(List<StuDevicePair> pairs, JumpRopeSetting setting);
		
		void updateSpecificItem(int index);
		
		void changeBadSuccess();
		
		void showToast(String msg);
		
		void showChangeBadDialog();
		
	}
	
}
