package com.feipulai.host.activity.jump_rope.base.setting;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public interface RadioSettingContract {

	interface Presenter{
		
		void start();

		/**
		 * 设置设备数量
		 */
		void updateDeviceSum(int deviceSum);

		/**
		 * 设置一轮测试的测试时限
		 */
		void updateTestTime(int testTime);

		/**
		 * 查看评分标准
		 */
		void showJudgements();
	}
	
	interface View<Presenter>{

		void showDeviceSum(int deviceSum);

		void showTestTime(int testTime);

		void showMax(int maxDeviceSum);

		void showToast(String err);
	}

}
