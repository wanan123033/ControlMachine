package com.feipulai.exam.activity.jump_rope.test;

import android.view.View;

import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.exam.activity.jump_rope.base.test.AbstractRadioTestActivity;
import com.feipulai.exam.activity.jump_rope.base.test.AbstractRadioTestPresenter;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.exam.view.WaitDialog;

import java.util.List;

public class JumpRopeTestActivity
        extends AbstractRadioTestActivity<JumpRopeSetting>{

    private WaitDialog changBadDialog;

    @Override
    public void initView(List<StuDevicePair> pairs, JumpRopeSetting setting) {
        llDeviceGroup.setVisibility(View.VISIBLE);
        tvGroup.setText(SerialConfigs.GROUP_NAME[setting.getDeviceGroup()] + "组");
        super.initView(pairs, setting);
    }
    
    @Override
    protected JumpRopeTestPresenter getPresenter() {
        return new JumpRopeTestPresenter(this, this);
    }

    @Override
    public void showChangeBadDialog() {
        changBadDialog = new WaitDialog(this);
        changBadDialog.setCanceledOnTouchOutside(false);
        changBadDialog.setCancelable(false);
        changBadDialog.show();
        // 必须在dialog显示出来后再调用
        changBadDialog.setTitle("请按下手柄按钮");
        changBadDialog.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changBadDialog.dismiss();
                presenter.cancelChangeBad();
            }
        });
    }

    @Override
    public void changeBadSuccess() {
        changBadDialog.dismiss();
        toastSpeak("更换成功");
    }

}
