package com.feipulai.exam.activity.basketball.reentry;

import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.basketball.BasketBallSettingActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;

import butterknife.BindView;
import butterknife.OnClick;

public class BallReentrySettingActivity extends BasketBallSettingActivity {
    private SportTimerManger sportTimerManger;
    @BindView(R.id.et_light_time)
    EditText etLightTime;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_basketball_reentry_setting;
    }

    @Override
    protected void initData() {
        super.initData();
        sportTimerManger = new SportTimerManger();
        tvPair.setVisibility(View.VISIBLE);
        etLightTime.setText(setting.getLightTime() + "");
    }

    @OnClick(R.id.tv_light_time_use)
    public void onLightTimeClick() {
        if (TextUtils.isEmpty(etLightTime.getText().toString())) {
            ToastUtils.showShort("请输入灯亮时长");
            return;
        }
        sportTimerManger.setLightTime(SettingHelper.getSystemSetting().getHostId(), Integer.valueOf(this.etLightTime.getText().toString()) * 1000);

        try {
            //两个指令相间隔100MS
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sportTimerManger.getLightTime(SettingHelper.getSystemSetting().getHostId());

        isDisconnect = true;
        isClickConnect = false;
        mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 2000);
    }

    @Override
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_sensitivity_use:
                if (TextUtils.isEmpty(etSensitivity.getText().toString())) {
                    ToastUtils.showShort("请输入灵敏度");
                    return;
                }
                sportTimerManger.setSensitiveTime(SettingHelper.getSystemSetting().getHostId(), Integer.valueOf(this.etSensitivity.getText().toString()));

                try {
                    //两个指令相间隔100MS
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sportTimerManger.getSensitiveTime(SettingHelper.getSystemSetting().getHostId());

                isDisconnect = true;
                isClickConnect = false;
                mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 2000);


                return;
            case R.id.tv_intercept_time_use:
                if (TextUtils.isEmpty(etInterceptTime.getText().toString())) {
                    ToastUtils.showShort("请输拦截秒数");
                    return;
                }
                sportTimerManger.setMinTime(SettingHelper.getSystemSetting().getHostId(), Integer.valueOf(this.etInterceptTime.getText().toString()));
                try {
                    //两个指令相间隔100MS
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sportTimerManger.getMinTime(SettingHelper.getSystemSetting().getHostId());

                isDisconnect = true;
                isClickConnect = false;
                mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 2000);
                return;
            case R.id.tv_accuracy_use:

                switch (rgAccuracy.getCheckedRadioButtonId()) {
                    case R.id.rb_tenths: //十分位
                        TestConfigs.sCurrentItem.setDigital(1);
                        break;
                    case R.id.rb_percentile://百分位
                        TestConfigs.sCurrentItem.setDigital(2);
                        break;
                    case R.id.rb_thousand://百分位
                        TestConfigs.sCurrentItem.setDigital(3);
                        break;
                }
                toastSpeak("设置成功");
                return;

        }
        super.onViewClicked(view);
    }

    @Override
    public void onViewClicked() {
        IntentUtil.gotoActivity(this, BasketReentryPairActivity.class);
    }

    @Override
    public void onRadioArrived(Message msg) {
        super.onRadioArrived(msg);

        if (msg.what == SerialConfigs.SPORT_TIMER_GET_SENSITIVE) {
            toastSpeak("设置成功");
            this.setting.setSensitivity((Integer) msg.obj);
        } else if (msg.what == SerialConfigs.SPORT_TIMER_GET_MIN_TIME) {
            toastSpeak("设置成功");
            this.setting.setInterceptSecond((Integer) msg.obj);
        } else if (msg.what == SerialConfigs.SPORT_TIMER_GET_LIGHT_TIME) {
            toastSpeak("设置成功");
            this.setting.setLightTime(Integer.valueOf(this.etLightTime.getText().toString()));
        }

    }
}
