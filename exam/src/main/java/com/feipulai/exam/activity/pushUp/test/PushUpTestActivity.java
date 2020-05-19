package com.feipulai.exam.activity.pushUp.test;

import com.feipulai.exam.activity.pushUp.PushUpSetting;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestActivity;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestPresenter;

public class PushUpTestActivity
		extends SitPullUpTestActivity<PushUpSetting> {
	
	@Override
	protected SitPullUpTestPresenter<PushUpSetting> getPresenter() {
		return new PushUpTestPresenter(this, this);
	}
}
