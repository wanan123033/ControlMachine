package com.feipulai.exam.activity.situp.test;

import android.content.DialogInterface;

import com.feipulai.exam.activity.base.PenalizeDialog;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestActivity;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestPresenter;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;

public class SitUpTestActivity
        extends SitPullUpTestActivity<SitUpSetting> implements PenalizeDialog.PenalizeListener {

    @Override
    protected SitPullUpTestPresenter<SitUpSetting> getPresenter() {
        return new SitUpTestPresenter(this, this);
    }

    @Override
    public void showPenalizeDialog(int max) {
        PenalizeDialog dialog = new PenalizeDialog(this);
        dialog.setPenalizeListener(this);
        dialog.setMinMaxValue(max * -1, 50);
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
