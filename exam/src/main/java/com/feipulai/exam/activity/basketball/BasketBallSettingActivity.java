package com.feipulai.exam.activity.basketball;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.udp.UDPBasketBallConfig;
import com.feipulai.device.udp.UdpClient;
import com.feipulai.device.udp.result.BasketballResult;
import com.feipulai.device.udp.result.UDPResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.view.baseToolbar.BaseToolbar;
import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;

/**
 * Created by zzs on  2019/6/3
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BasketBallSettingActivity extends BaseTitleActivity implements CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener
        , UdpClient.UDPChannelListerner {

    @BindView(R.id.sp_test_no)
    Spinner spTestNo;
    @BindView(R.id.cb_full_skip)
    CheckBox cbFullSkip;
    @BindView(R.id.edit_male_full)
    EditText editMaleFull;
    @BindView(R.id.edit_female_full)
    EditText editFemaleFull;
    @BindView(R.id.rg_group_mode)
    RadioGroup rgGroupMode;
    @BindView(R.id.et_intercept_time)
    EditText etInterceptTime;
    @BindView(R.id.et_sensitivity)
    EditText etSensitivity;
    @BindView(R.id.et_host_ip)
    EditText etHostIp;
    @BindView(R.id.et_port)
    EditText etPort;

    @BindView(R.id.rg_accuracy)
    RadioGroup rgAccuracy;
    @BindView(R.id.sp_carryMode)
    Spinner spCarryMode;
    @BindView(R.id.view_carryMode)
    LinearLayout viewCarryMode;
    private Integer[] testRound = new Integer[]{1, 2, 3};

    private String[] carryMode = new String[]{"不进位", "四舍五入", "非零进位"};
    private BasketBallSetting setting;
    //    private UdpClient udpClient;
    private MyHandler mHandler = new MyHandler(this);
    private static final int MSG_DISCONNECT = 0X101;
    //3秒内检测IP是否可以
    private volatile boolean isDisconnect;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_basketball_setting;
    }

    @Override
    protected void initData() {
        //获取项目设置
        setting = SharedPrefsUtil.loadFormSource(this, BasketBallSetting.class);
        if (setting == null)
            setting = new BasketBallSetting();
        UdpClient.getInstance().setHostIpPostLocatListener(setting.getHostIp(), setting.getPost(), this);
        //设置测试次数
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testRound);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTestNo.setAdapter(adapter);
        spTestNo.setSelection(TestConfigs.getMaxTestCount(this) - 1);
        // 数据库中已经指定了测试次数,就不能再设置了
        spTestNo.setEnabled(TestConfigs.sCurrentItem.getTestNum() == 0);

        cbFullSkip.setOnCheckedChangeListener(this);
        if (setting.getMaleFullScore() > 0) {
            editMaleFull.setText(setting.getMaleFullScore() + "");
        }
        if (setting.getFemaleFullScore() > 0) {
            editFemaleFull.setText(setting.getFemaleFullScore() + "");
        }
        cbFullSkip.setChecked(setting.isFullSkip());

        if (setting.getTestPattern() == 0) {//连续测试
            rgGroupMode.check(R.id.rb_continuous_test);
        } else {
            rgGroupMode.check(R.id.rb_circulation_test);
        }
        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
            rgGroupMode.setVisibility(View.GONE);
        } else {
            rgGroupMode.setVisibility(View.VISIBLE);
        }
        rgGroupMode.setOnCheckedChangeListener(this);
        rgAccuracy.setOnCheckedChangeListener(this);
        //设置测试次数
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, carryMode);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCarryMode.setAdapter(adapter);
        spCarryMode.setSelection(setting.getCarryMode() == 3 ? 2 : setting.getCarryMode());
        viewCarryMode.setVisibility(setting.getResultAccuracy() == 0 ? View.VISIBLE : View.GONE);

        etInterceptTime.setText(setting.getInterceptSecond() + "");
        etSensitivity.setText(setting.getSensitivity() + "");
        etHostIp.setText(setting.getHostIp());
        etPort.setText(setting.getPost() + "");
        rgAccuracy.check(setting.getResultAccuracy() == 0 ? R.id.rb_tenths : R.id.rb_percentile);
    }

    @Override
    public void channelInactive() {

    }

    @Override
    public void onDataArrived(UDPResult result) {
        switch (result.getType()) {
            case UDPBasketBallConfig.CMD_GET_STATUS_RESPONSE:
                isDisconnect = false;
                setting.setHostIp(etHostIp.getText().toString());
                setting.setPost(Integer.valueOf(etPort.getText().toString()));
                ToastUtils.showShort("连接成功");
                break;
            case UDPBasketBallConfig.CMD_SET_T_RESPONSE:
                ToastUtils.showShort("设置成功");
                setting.setSensitivity(Integer.valueOf(etSensitivity.getText().toString()));
                break;
            case UDPBasketBallConfig.CMD_SET_PRECISION_RESPONSE:
                ToastUtils.showShort("设置成功");
                BasketballResult basketballResult = (BasketballResult) result.getResult();
                setting.setResultAccuracy(basketballResult.getuPrecision());
                break;
        }

    }

    @Override
    public void finish() {
        if (!TextUtils.isEmpty(etInterceptTime.getText().toString()))
            setting.setInterceptSecond(Integer.valueOf(etInterceptTime.getText().toString()));

        SharedPrefsUtil.save(this, setting);
        Logger.i("保存设置:" + setting.toString());
        super.finish();
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

    private boolean isCheckSetting() {
        if (TextUtils.isEmpty(editMaleFull.getText().toString())) {
            ToastUtils.showShort("启动满分跳过必须设置满分值");
            ToastUtils.showShort("请输入男子满分值");
            return false;
        }

        setting.setMaleFullScore(Integer.valueOf(editMaleFull.getText().toString()));
        if (TextUtils.isEmpty(editFemaleFull.getText().toString())) {
            ToastUtils.showShort("启动满分跳过必须设置满分值");
            ToastUtils.showShort("请输入女子满分值");
            return false;
        }
        setting.setFemaleFullScore(Integer.valueOf(editFemaleFull.getText().toString()));
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_full_skip:
                if (isChecked && isCheckSetting()) {
                    setting.setFullSkip(true);
                } else {
                    cbFullSkip.setChecked(false);
                    setting.setFullSkip(false);
                }
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_loop://循环测试
                setting.setTestPattern(1);
                break;
            case R.id.rb_successive://连续测试
                setting.setTestPattern(0);
                break;
            case R.id.rb_tenths: //十分位
//                setting.setResultAccuracy(0);
                viewCarryMode.setVisibility(View.VISIBLE);

                break;
            case R.id.rb_percentile://百分位
//                setting.setResultAccuracy(1);
                viewCarryMode.setVisibility(View.GONE);
                break;
        }
    }

    @OnItemSelected({R.id.sp_carryMode})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_carryMode:
                setting.setCarryMode(position == 2 ? 3 : position);
                break;

        }
    }

    @OnClick({R.id.tv_sensitivity_use, R.id.tv_ip_connect, R.id.tv_accuracy_use})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_sensitivity_use://灵敏度
                if (TextUtils.isEmpty(etSensitivity.getText().toString())) {
                    ToastUtils.showShort("请输入灵敏度");
                    return;
                }
                UdpClient.getInstance().setHostIpPost(setting.getHostIp(), setting.getPost());
                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_T(Integer.valueOf(etSensitivity.getText().toString())));
                break;
            case R.id.tv_ip_connect://连接
                if (TextUtils.isEmpty(etHostIp.getText().toString())) {
                    ToastUtils.showShort("请输入IP地址");
                    return;
                }
                if (TextUtils.isEmpty(etPort.getText().toString())) {
                    ToastUtils.showShort("请输入端口号");
                    return;
                }

                UdpClient.getInstance().setHostIpPost(etHostIp.getText().toString(), Integer.valueOf(etPort.getText().toString()));
                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_GET_STATUS);
                isDisconnect = true;
                mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 3000);
                break;
            case R.id.tv_accuracy_use:
                switch (rgAccuracy.getCheckedRadioButtonId()) {
                    case R.id.rb_tenths: //十分位
                        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_PRECISION(0));
                        break;
                    case R.id.rb_percentile://百分位
                        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_PRECISION(1));
                        break;
                }
                break;
        }
    }

    private static class MyHandler extends Handler {

        private WeakReference<BasketBallSettingActivity> mActivityWeakReference;

        public MyHandler(BasketBallSettingActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BasketBallSettingActivity activity = mActivityWeakReference.get();
            if (activity.isDisconnect) {
                activity.toastSpeak("连接失败");
            }
        }
    }
}