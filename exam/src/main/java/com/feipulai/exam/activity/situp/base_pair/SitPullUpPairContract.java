package com.feipulai.exam.activity.situp.base_pair;

import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;

import java.util.List;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public interface SitPullUpPairContract {

	interface Presenter {
		void start();
		void changeFocusPosition(int position);

		void changeAutoPair(boolean isAutoPair);

		void stopPair();

		void saveSettings();
	}

	interface View {
		void initView(boolean isAutoPair, List<StuDevicePair> stuDevicePairs);

		void updateSpecificItem(int focusPosition);

		void select(int position);

        void showToast(String msg);
    }

}
