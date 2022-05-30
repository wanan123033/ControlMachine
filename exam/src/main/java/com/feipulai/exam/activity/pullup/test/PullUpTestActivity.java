package com.feipulai.exam.activity.pullup.test;

import android.content.DialogInterface;

import com.feipulai.exam.activity.base.PenalizeDialog;
import com.feipulai.exam.activity.pullup.setting.PullUpSetting;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestActivity;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestPresenter;

public class PullUpTestActivity
		extends SitPullUpTestActivity<PullUpSetting> implements PenalizeDialog.PenalizeListener {

	@Override
	protected SitPullUpTestPresenter<PullUpSetting> getPresenter() {
		return new PullUpTestPresenter(this, this);
	}



	@Override
	public void showPenalizeDialog(int max) {
		PenalizeDialog dialog = new PenalizeDialog(this);
		dialog.setPenalizeListener(this);
		dialog.setMinMaxValue(max * -1,max);
		dialog.show();
	}

	@Override
	public void penalize(int value) {
		presenter.penalize(value);
	}

	@Override
	public void dismisson(DialogInterface dialog) {
		dialog.dismiss();
	}

	@Override
	public boolean getPenalize() {
		return true;
	}


}
