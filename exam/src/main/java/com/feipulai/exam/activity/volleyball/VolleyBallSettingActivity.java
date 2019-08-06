package com.feipulai.exam.activity.volleyball;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.feipulai.common.utils.DialogUtils;
import com.feipulai.common.utils.HandlerUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.manager.VolleyBallManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.VolleyBallCheck;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;
import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class VolleyBallSettingActivity
        extends BaseTitleActivity
        implements CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener, TextWatcher
        , SerialDeviceManager.RS232ResiltListener {

    @BindView(R.id.sp_test_no)
    Spinner spTestNo;
    @BindView(R.id.cb_full_skip)
    CheckBox cbFullSkip;
    @BindView(R.id.edit_male_full)
    EditText editMaleFull;
    @BindView(R.id.edit_female_full)
    EditText editFemaleFull;
    @BindView(R.id.ll_full_skip)
    LinearLayout llFullSkip;
    @BindView(R.id.rg_group_mode)
    RadioGroup rgGroupMode;
    @BindView(R.id.et_test_time)
    EditText etTestTime;

    private Integer[] testRound = new Integer[]{1, 2, 3};

    private VolleyBallSetting setting;
    private ProgressDialog mProgressDialog;
    //3秒内检设备是否可用
    private volatile boolean isDisconnect = true;
    private static final int MSG_DISCONNECT = 0X101;
    private static final int MSG_CHECK = 0X102;
    private SerialHandler mHandler = new SerialHandler(this);
    private CheckDeviceView checkDeviceView;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_volleyball_setting;
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, VolleyBallSetting.class);

        ArrayAdapter spTestRoundAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testRound);
        spTestRoundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTestNo.setAdapter(spTestRoundAdapter);
        spTestNo.setSelection(TestConfigs.getMaxTestCount(this) - 1);
        // 数据库中已经指定了测试次数,就不能再设置了
        spTestNo.setEnabled(TestConfigs.sCurrentItem.getTestNum() == 0);

        boolean isGroupMode = SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN;
        rgGroupMode.setVisibility(isGroupMode ? View.VISIBLE : View.GONE);
        rgGroupMode.setOnCheckedChangeListener(this);
        rgGroupMode.check(setting.getGroupMode() == TestConfigs.GROUP_PATTERN_SUCCESIVE ? R.id.rb_successive : R.id.rb_loop);

        editMaleFull.setText(setting.getMaleFullScore() + "");
        editFemaleFull.setText(setting.getFemaleFullScore() + "");

        cbFullSkip.setChecked(setting.isFullSkip());
        llFullSkip.setVisibility(setting.isFullSkip() ? View.VISIBLE : View.GONE);
        cbFullSkip.setOnCheckedChangeListener(this);

        etTestTime.setText(setting.getTestTime() + "");
        editMaleFull.addTextChangedListener(this);
        editFemaleFull.addTextChangedListener(this);
        etTestTime.addTextChangedListener(this);
        SerialDeviceManager.getInstance().setRS232ResiltListener(this);
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("项目设置").addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void finish() {
        SharedPrefsUtil.save(this, setting);
        Logger.i("保存排球设置:" + setting.toString());
        super.finish();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_loop:
                setting.setGroupMode(TestConfigs.GROUP_PATTERN_LOOP);
                break;

            case R.id.rb_successive:
                setting.setGroupMode(TestConfigs.GROUP_PATTERN_SUCCESIVE);
                break;
        }
    }

    @OnItemSelected({R.id.sp_test_no})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {

            case R.id.sp_test_no:
                setting.setTestNo(position + 1);
                break;

        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {

            case R.id.cb_full_skip:
                setting.setFullSkip(isChecked);
                llFullSkip.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                break;

        }
    }

    @OnClick({R.id.tv_judgement, R.id.tv_device_check})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.tv_judgement:
                ToastUtils.showShort("功能开发中,敬请期待");
                break;
            case R.id.tv_device_check:
                showCheckDiglog();
                isDisconnect = true;
                mProgressDialog = ProgressDialog.show(this, "", "终端自检中...", true);
                new VolleyBallManager().checkDevice();
                //3秒自检
                mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 3000);
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        String text = editMaleFull.getText().toString();
        int value;
        if (!TextUtils.isEmpty(text)) {
            value = Integer.parseInt(text);
            if (value > 1000) {
                ToastUtils.showShort("满分最大值不能超过1000");
            } else {
                setting.setMaleFullScore(value);
            }
        }

        text = editFemaleFull.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            value = Integer.parseInt(text);
            if (value > 1000) {
                ToastUtils.showShort("满分最大值不能超过1000");
            } else {
                setting.setFemaleFullScore(value);
            }
        }

        text = etTestTime.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            value = Integer.parseInt(text);
            if (value > 1000) {
                ToastUtils.showShort("满分最大值不能超过1000");
            } else {
                setting.setTestTime(value);
            }
        }
    }

    @Override
    public void onRS232Result(Message msg) {
        switch (msg.what) {
            case SerialConfigs.VOLLEYBALL_CHECK_RESPONSE:
                if (checkDeviceView == null) {
                    return;
                }
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }


                HandlerUtil.sendMessage(mHandler, MSG_CHECK, msg.obj);


                break;
        }
    }

    private void showCheckDiglog() {
        checkDeviceView = new CheckDeviceView(this);
        checkDeviceView.setUnunitedData(setting.getTestPattern() == 0 ? VolleyBallSetting.ANTIAIRCRAFT_POLE : VolleyBallSetting.WALL_POLE);
        DialogUtils.create(this, checkDeviceView, true).show();
        //这种设置宽高的方式也是好使的！！！-- show 前调用，show 后调用都可以！！！
        checkDeviceView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom) {
                int height = v.getHeight();     //此处的view 和v 其实是同一个控件

                int needHeight = 430;

                if (height > needHeight) {
                    //注意：这里的 LayoutParams 必须是 FrameLayout的！！
                    v.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                            needHeight));
                }
            }
        });
    }


    /**
     * 回调
     */
    private static class SerialHandler extends Handler {

        private WeakReference<VolleyBallSettingActivity> mActivityWeakReference;

        public SerialHandler(VolleyBallSettingActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            VolleyBallSettingActivity activity = mActivityWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_DISCONNECT://连接失败
                        if (activity.isDisconnect) {
                            activity.toastSpeak("设备未连接");
                            //设置当前设置为不可用断开状态
                            if (activity.mProgressDialog.isShowing()) {
                                activity.mProgressDialog.dismiss();
                            }
                        }
                        break;
                    case MSG_CHECK:
                        final VolleyBallCheck volleyBallCheck = (VolleyBallCheck) msg.obj;
                        if (volleyBallCheck
                                .getPositionList() == null || volleyBallCheck.getPositionList().size() == 0) {
                            activity.checkDeviceView.setUnunitedData(activity.setting.getTestPattern() == 0 ? VolleyBallSetting.ANTIAIRCRAFT_POLE : VolleyBallSetting.WALL_POLE);
                        } else {
                            activity.checkDeviceView.setData(activity.setting.getTestPattern() == 0 ? VolleyBallSetting.ANTIAIRCRAFT_POLE : VolleyBallSetting.WALL_POLE, volleyBallCheck.getPositionList());
                        }
                        break;
                }
            }

        }
    }
}
