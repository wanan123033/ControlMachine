package com.feipulai.exam.activity.jump_rope.check;

import com.feipulai.exam.activity.jump_rope.base.check.RadioCheckContract;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public interface JumpRopeCheckContract{

	interface Presenter extends RadioCheckContract.Presenter{
		void changeDeviceGroup();
        void dealConflict();
		void killAllDevices();
		void changeBadDevice(boolean killCurrent);
	}
	
	interface View<Setting> extends RadioCheckContract.View<Setting>{
		void showChangeDeviceGroup(int deviceGroup);
		void showChangBadWarning();
	}

}
