package com.feipulai.exam.activity.sitreach;

import android.content.Intent;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 坐位体前屈项目设置
 * Created by zzs on 2018/11/21
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitReachSettingActivity extends BaseTitleActivity implements CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {

    @BindView(R.id.tv_device_check)
    TextView tvDeviceCheck;
    @BindView(R.id.sp_device_count)
    Spinner spDeviceCount;
    @BindView(R.id.sp_test_round)
    Spinner spTestRound;
    @BindView(R.id.cb_full_return)
    CheckBox cbFullReturn;
    @BindView(R.id.edit_man_full)
    EditText editManFull;
    @BindView(R.id.edit_women_full)
    EditText editWomenFull;
    @BindView(R.id.rb_continuous_test)
    RadioButton rbContinuousTest;
    @BindView(R.id.rb_circulation_test)
    RadioButton rbCirculationTest;
    @BindView(R.id.rg_test_pattern)
    RadioGroup rgTestPattern;


    private Integer[] testRound = new Integer[]{1, 2, 3};

    private SitReachSetting reachSetting;
    private static final int MSG_DISCONNECT = 0X101;
    private SweetAlertDialog alertDialog;
    private SerialHandler mHandler = new SerialHandler(this);
    //3秒内检设备是否可用
    private volatile boolean isDisconnect = true;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_sitreach_setting;
    }

    @Override
    protected void initData() {
        cbFullReturn.setOnCheckedChangeListener(this);
        //获取项目设置
        reachSetting = SharedPrefsUtil.loadFormSource(this, SitReachSetting.class);
        if (reachSetting == null)
            reachSetting = new SitReachSetting();
        ArrayAdapter spTestRoundAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testRound);
        spTestRoundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTestRound.setAdapter(spTestRoundAdapter);
        ArrayAdapter spDeviceCountAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"1"});
        spDeviceCountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDeviceCount.setAdapter(spDeviceCountAdapter);
        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
            rgTestPattern.setVisibility(View.GONE);
        } else {
            rgTestPattern.setVisibility(View.VISIBLE);
        }
        rgTestPattern.setOnCheckedChangeListener(this);

        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            // 数据库中已经指定了测试次数,就不能再设置了
            spTestRound.setEnabled(false);
            spTestRound.setSelection(TestConfigs.sCurrentItem.getTestNum() - 1);
        } else {
            spTestRound.setSelection(reachSetting.getTestCount() - 1);
        }


        if (reachSetting.getTestPattern() == 0) {//连续测试
            rgTestPattern.check(R.id.rb_continuous_test);
        } else {
            rgTestPattern.check(R.id.rb_circulation_test);
        }


        if (reachSetting.getManFull() > 0) {
            editManFull.setText(reachSetting.getManFull() + "");
        }
        if (reachSetting.getWomenFull() > 0) {
            editWomenFull.setText(reachSetting.getWomenFull() + "");
        }
        cbFullReturn.setChecked(reachSetting.isFullReturn());
        SerialDeviceManager.getInstance().setRS232ResiltListener(listener);
        setEditTextWatcherListener();
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("项目设置") ;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_circulation_test://循环测试
                reachSetting.setTestPattern(1);
                break;
            case R.id.rb_continuous_test://连续测试
                reachSetting.setTestPattern(0);
                break;
        }
    }

    @OnItemSelected({R.id.sp_test_round})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_test_round:
                reachSetting.setTestCount(position + 1);
                EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_COUNT));
                break;

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (reachSetting.isFullReturn()) {
            isCheckSetting();
        }
        SharedPrefsUtil.save(this, reachSetting);
        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
            startActivity(new Intent(this, SitReachTestActivity.class));
        }
        SerialDeviceManager.getInstance().close();
    }


    private boolean isCheckSetting() {
        if (TextUtils.isEmpty(editManFull.getText().toString())) {
            ToastUtils.showShort("启动满分跳过必须设置满分值");
            ToastUtils.showShort("请输入男子满分值");
            return false;
        }
        if (TextUtils.equals(editManFull.getText().toString(), "-") || TextUtils.equals(editManFull.getText().toString(), "+")) {
            ToastUtils.showShort("请输入男子满分值");
            return false;
        }
        int manFull = Integer.valueOf(editManFull.getText().toString());
        if (manFull < -20) {
            ToastUtils.showShort("满分值输入范围超出（-20~40）");
            return false;
        }
        reachSetting.setManFull(Integer.valueOf(editManFull.getText().toString()));
        if (TextUtils.isEmpty(editWomenFull.getText().toString())) {
            ToastUtils.showShort("启动满分跳过必须设置满分值");
            ToastUtils.showShort("请输入女子满分值");
            return false;
        }
        if (TextUtils.equals(editWomenFull.getText().toString(), "-") || TextUtils.equals(editWomenFull.getText().toString(), "+")) {
            ToastUtils.showShort("请输入男子满分值");
            return false;
        }
        int womentFull = Integer.valueOf(editWomenFull.getText().toString());
        if (womentFull < -20) {
            ToastUtils.showShort("满分值输入范围超出（-20~40）");
            return false;
        }
        reachSetting.setWomenFull(Integer.valueOf(editWomenFull.getText().toString()));
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_full_return:
                if (isChecked && isCheckSetting()) {
                    reachSetting.setFullReturn(true);
                } else {
                    cbFullReturn.setChecked(false);
                    reachSetting.setFullReturn(false);
                }
                break;
        }
    }

    private void setEditTextWatcherListener() {
        editManFull.addTextChangedListener(new TextWatcher() {
            String manFull = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                manFull = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    cbFullReturn.setChecked(false);
                    reachSetting.setFullReturn(false);
                }
                if (s.length() > 0 && !TextUtils.equals(s.toString(), "-") && !TextUtils.equals(s.toString(), "+")) {

                    int number = Integer.valueOf(editManFull.getText().toString());
                    if (number > 40) {
                        ToastUtils.showShort("满分值输入范围超出（-20~40）");
                        editManFull.setText(manFull);
                        editManFull.setSelection(manFull.length() - 1);
                    }
                }

            }
        });
        editWomenFull.addTextChangedListener(new TextWatcher() {
            String womenFull;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                womenFull = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    cbFullReturn.setChecked(false);
                    reachSetting.setFullReturn(false);
                }

                if (s.length() > 0 && !TextUtils.equals(s.toString(), "-") && !TextUtils.equals(s.toString(), "+")) {
                    int number = Integer.valueOf(editWomenFull.getText().toString());
                    if (number > 40) {
                        ToastUtils.showShort("满分值输入范围超出（-20~40）");
                        editWomenFull.setText(womenFull);
                        editWomenFull.setSelection(womenFull.length() - 1);
                    }
                }
            }
        });
    }

    @OnClick(R.id.tv_device_check)
    public void onViewClicked() {
//        mProgressDialog = ProgressDialog.show(this, "", "终端自检中...", true);
        alertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        alertDialog.setTitleText("终端自检中...");
        alertDialog.setCancelable(false);
        alertDialog.show();
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_EMPTY));
        //3秒自检
        mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 3000);
    }

    SerialDeviceManager.RS232ResiltListener listener = new SerialDeviceManager.RS232ResiltListener() {
        @Override
        public void onRS232Result(Message msg) {
            mHandler.sendMessage(msg);
        }
    };

    /**
     * 回调
     */
    private static class SerialHandler extends Handler {

        private WeakReference<SitReachSettingActivity> mActivityWeakReference;

        public SerialHandler(SitReachSettingActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SitReachSettingActivity activity = mActivityWeakReference.get();
            //加载窗口是否显示
            boolean isDialogShow = true;
            if (activity != null) {
                switch (msg.what) {
                    case MSG_DISCONNECT://连接失败
                        if (activity.isDisconnect) {
                            activity.toastSpeak("设备未连接");
                            //设置当前设置为不可用断开状态
                            if (activity.alertDialog.isShowing()) {
                                activity.alertDialog.dismiss();
                            }
                        }
                        break;
                    case SerialConfigs.SIT_AND_REACH_EMPTY_RESPONSE:
                        //检测设备是否连接成功
                        activity.isDisconnect = false;
                        isDialogShow = false;
                        activity.toastSpeak("设备连接成功");
                        Logger.i("空命令回复:");
                        break;
                }
                if (!isDialogShow && activity.alertDialog != null && activity.alertDialog.isShowing()) {
                    activity.alertDialog.dismiss();
                }

            }

        }
    }
}
