package com.feipulai.exam.activity.pullup.test;

import com.feipulai.exam.activity.pullup.setting.PullUpSetting;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestActivity;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestPresenter;

public class PullUpTestActivity
		extends SitPullUpTestActivity<PullUpSetting> {
	
	@Override
	protected SitPullUpTestPresenter<PullUpSetting> getPresenter() {
		return new PullUpTestPresenter(this, this);
	}
	
}
