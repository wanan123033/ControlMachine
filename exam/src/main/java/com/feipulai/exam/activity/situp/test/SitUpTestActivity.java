package com.feipulai.exam.activity.situp.test;

import com.feipulai.exam.activity.situp.base_test.SitPullUpTestActivity;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestPresenter;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;

public class SitUpTestActivity
		extends SitPullUpTestActivity<SitUpSetting> {
	
	@Override
	protected SitPullUpTestPresenter<SitUpSetting> getPresenter() {
		return new SitUpTestPresenter(this, this);
	}
	
}
