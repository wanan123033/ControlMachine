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
import android.widget.TextView;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.NetWorkUtils;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.manager.BallManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.Basketball868Result;
import com.feipulai.device.udp.UDPBasketBallConfig;
import com.feipulai.device.udp.UdpClient;
import com.feipulai.device.udp.UdpLEDUtil;
import com.feipulai.device.udp.result.BasketballResult;
import com.feipulai.device.udp.result.UDPResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.basketball.pair.BasketBallPairActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.sport_timer.SportTimerActivity;
import com.feipulai.exam.activity.sport_timer.pair.SportPairActivity;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;

/**
 * Created by zzs on  2019/6/3
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BasketBallSettingActivity extends BaseTitleActivity implements CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener
        , UdpClient.UDPChannelListerner, RadioManager.OnRadioArrivedListener {

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
    public EditText etInterceptTime;
    @BindView(R.id.et_sensitivity)
    public EditText etSensitivity;
    @BindView(R.id.et_host_ip)
    EditText etHostIp;
    @BindView(R.id.et_port)
    EditText etPort;
    @BindView(R.id.ll_ip)
    LinearLayout llIp;
    @BindView(R.id.rg_accuracy)
    public RadioGroup rgAccuracy;
    @BindView(R.id.sp_carryMode)
    Spinner spCarryMode;
    @BindView(R.id.view_carryMode)
    LinearLayout viewCarryMode;
    @BindView(R.id.et_penaltySecond)
    EditText etPenaltySecond;
    @BindView(R.id.sp_test_mode)
    Spinner spTestMode;
    @BindView(R.id.tv_pair)
    public TextView tvPair;
    @BindView(R.id.tv_ip_connect)
    TextView tvIpConnect;
    private Integer[] testRound;
    private String[] carryMode = new String[]{"四舍五入", "不进位", "非零进位"};
    public BasketBallSetting setting;
    public MyHandler mHandler = new MyHandler(this);
    public static final int MSG_DISCONNECT = 0X101;
    //3秒内检测IP是否可以
    public volatile boolean isDisconnect;
    /**
     * 点击是否为连接
     */
    public boolean isClickConnect;
    private BallManager manager;

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

//        UdpClient.getInstance().init(1527);
//        UdpClient.getInstance().setHostIpPostLocatListener(setting.getHostIp(), setting.getPost(), this);
        manager = new BallManager.Builder(setting.getTestType()).setRadioListener(this).setHostIp(setting.getHostIp())
                .setInetPost(1527).setPost(setting.getPost()).setUdpListerner(this).build();
        //设置测试次数
        int maxTestNo = (TestConfigs.sCurrentItem.getTestNum() == 0 && TestConfigs.getMaxTestCount(this) <= TestConfigs.MAX_TEST_NO)
                ? TestConfigs.MAX_TEST_NO : TestConfigs.getMaxTestCount(this);

        testRound = new Integer[maxTestNo];
        for (int i = 0; i < maxTestNo; i++) {
            testRound[i] = i + 1;
        }
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
            rgGroupMode.check(R.id.rb_continuous);
        } else {
            rgGroupMode.check(R.id.rb_loop);
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
        spCarryMode.setSelection(TestConfigs.sCurrentItem.getCarryMode() > 0 ? TestConfigs.sCurrentItem.getCarryMode() - 1 : 0);
//        viewCarryMode.setVisibility(getAccuracy() == 1 ? View.VISIBLE : View.GONE);

        etInterceptTime.setText(setting.getInterceptSecond() + "");
        etSensitivity.setText(setting.getSensitivity() + "");
        etHostIp.setText(setting.getHostIp());
        etPort.setText(setting.getPost() + "");
        rgAccuracy.check(getAccuracy() == 1 ? R.id.rb_tenths : getAccuracy() == 2 ? R.id.rb_percentile : R.id.rb_thousand);

        etPenaltySecond.setText(setting.getPenaltySecond() + "");

        if (setting.getTestType() == 1 || setting.getTestType() == 3) {
            tvPair.setVisibility(View.VISIBLE);
            etPort.setEnabled(false);
            etHostIp.setEnabled(false);
            tvIpConnect.setEnabled(false);
            llIp.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.init();
    }

    private int getAccuracy() {
        switch (TestConfigs.sCurrentItem.getDigital()) {
            case 1:
            case 2:
            case 3:
                return TestConfigs.sCurrentItem.getDigital();
            default:
                return 2;
        }
    }

    @Override
    public void channelInactive() {

    }

    @Override
    public void onRadioArrived(Message msg) {
        this.isDisconnect = false;
        if (msg.what == SerialConfigs.DRIBBLEING_START) {
            toastSpeak("连接成功");
        } else if (msg.what == SerialConfigs.DRIBBLEING_SET_SETTING) {
            toastSpeak("设置成功");
            Basketball868Result result = (Basketball868Result) msg.obj;
            this.setting.setSensitivity(result.getSensitivity());
            this.setting.setInterceptSecond(result.getInterceptSecond());
            TestConfigs.sCurrentItem.setDigital(result.getuPrecision() + 1);
        }
    }

    @Override
    public void onDataArrived(UDPResult result) {
        this.isDisconnect = false;
        switch (result.getType()) {
            case UDPBasketBallConfig.CMD_GET_STATUS_RESPONSE:
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
                TestConfigs.sCurrentItem.setDigital(basketballResult.getuPrecision() + 1);
                break;
            case UDPBasketBallConfig.CMD_SET_BLOCKERTIME_RESPONSE:
                ToastUtils.showShort("设置成功");
                basketballResult = (BasketballResult) result.getResult();
                setting.setInterceptSecond(basketballResult.getSecond());
                break;
        }

    }

    @Override
    public void finish() {
        if (!TextUtils.isEmpty(etPenaltySecond.getText().toString()))
            setting.setPenaltySecond(Double.parseDouble(etPenaltySecond.getText().toString()));
        EventBus.getDefault().post(new BaseEvent(EventConfigs.ITEM_SETTING_UPDATE));
        SharedPrefsUtil.save(this, setting);
        DBManager.getInstance().updateItem(TestConfigs.sCurrentItem);
        Logger.i("保存设置:" + setting.toString());
        EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_RESULT));
        mHandler.removeCallbacksAndMessages(null);
        super.finish();
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("项目设置");
    }

    private boolean isCheckSetting() {
        if (TextUtils.isEmpty(editMaleFull.getText().toString())) {
            ToastUtils.showShort("启动满分跳过必须设置满分值");
            ToastUtils.showShort("请输入男子满分值");
            return false;
        }

        setting.setMaleFullScore(Double.valueOf(editMaleFull.getText().toString()));
        if (TextUtils.isEmpty(editFemaleFull.getText().toString())) {
            ToastUtils.showShort("启动满分跳过必须设置满分值");
            ToastUtils.showShort("请输入女子满分值");
            return false;
        }
        setting.setFemaleFullScore(Double.valueOf(editFemaleFull.getText().toString()));
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
            case R.id.rb_continuous://连续测试
                setting.setTestPattern(0);
                break;
//            case R.id.rb_tenths: //十分位
////                setting.setResultAccuracy(0);
//                viewCarryMode.setVisibility(View.VISIBLE);
//
//                break;
//            case R.id.rb_percentile://百分位
////                setting.setResultAccuracy(1);
//                viewCarryMode.setVisibility(View.GONE);
//                break;
        }
    }

    @OnItemSelected({R.id.sp_carryMode, R.id.sp_test_no})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_carryMode:
                TestConfigs.sCurrentItem.setCarryMode(position + 1);
                break;
            case R.id.sp_test_no:
                setting.setTestNo(position + 1);
                break;
        }
    }

    @OnClick(R.id.tv_pair)
    public void onViewClicked() {
        if (setting.getTestType() == 4) {
            IntentUtil.gotoActivity(this, SportPairActivity.class);
            return;
        }
        IntentUtil.gotoActivity(this, BasketBallPairActivity.class);
    }

    @OnClick({R.id.tv_sensitivity_use, R.id.tv_ip_connect, R.id.tv_accuracy_use, R.id.tv_intercept_time_use})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_intercept_time_use:
                if (TextUtils.isEmpty(etInterceptTime.getText().toString())) {
                    ToastUtils.showShort("请输拦截秒数");
                    return;
                }
//                UdpClient.getInstance().setHostIpPost(setting.getHostIp(), setting.getPost());
//                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_BLOCKERTIME(Integer.valueOf(etInterceptTime.getText().toString())));
                manager.setHostIpPost(setting.getHostIp(), setting.getPost());
                manager.sendSetBlockertime(SettingHelper.getSystemSetting().getHostId(), this.setting.getSensitivity(),
                        Integer.valueOf(this.etInterceptTime.getText().toString()), TestConfigs.sCurrentItem.getDigital() == 1 ? 0 : 1);
                isDisconnect = true;
                isClickConnect = false;
                mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 2000);
                break;
            case R.id.tv_sensitivity_use://灵敏度
                if (TextUtils.isEmpty(etSensitivity.getText().toString())) {
                    ToastUtils.showShort("请输入灵敏度");
                    return;
                }
                manager.setHostIpPost(setting.getHostIp(), setting.getPost());
                manager.sendSetDelicacy(SettingHelper.getSystemSetting().getHostId(), Integer.valueOf(this.etSensitivity.getText().toString()),
                        this.setting.getInterceptSecond(), TestConfigs.sCurrentItem.getDigital() == 1 ? 0 : 1);
//                UdpClient.getInstance().setHostIpPost(setting.getHostIp(), setting.getPost());
//                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_T(Integer.valueOf(etSensitivity.getText().toString())));
                isDisconnect = true;
                isClickConnect = false;
                mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 2000);
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
                if (SettingHelper.getSystemSetting().isAddRoute()) {
                    String locatIp = NetWorkUtils.getLocalIp();
                    String routeIp = locatIp.substring(0, locatIp.lastIndexOf("."));
                    UdpLEDUtil.shellExec("ip route add " + routeIp + ".0/24 dev eth0 proto static scope link table wlan0 \n");
                }


                manager.setHostIpPost(etHostIp.getText().toString(), Integer.valueOf(etPort.getText().toString()));
                manager.sendGetStatus(SettingHelper.getSystemSetting().getHostId(), 1);
//                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_GET_STATUS);
                isDisconnect = true;
                isClickConnect = true;
                mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 2000);
                break;
            case R.id.tv_accuracy_use:
                switch (rgAccuracy.getCheckedRadioButtonId()) {
                    case R.id.rb_tenths: //十分位
                        manager.sendSetPrecision(SettingHelper.getSystemSetting().getHostId(), Integer.valueOf(this.etSensitivity.getText().toString()),
                                this.setting.getInterceptSecond(), 0);

                        break;
                    case R.id.rb_percentile://百分位
                        manager.sendSetPrecision(SettingHelper.getSystemSetting().getHostId(), Integer.valueOf(this.etSensitivity.getText().toString()),
                                this.setting.getInterceptSecond(), 1);
                        break;
                    case R.id.rb_thousand://百分位
                        manager.sendSetPrecision(SettingHelper.getSystemSetting().getHostId(), Integer.valueOf(this.etSensitivity.getText().toString()),
                                this.setting.getInterceptSecond(), 2);
                        break;
                }
                isDisconnect = true;
                isClickConnect = false;
                mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 2000);
                break;
        }
    }


    public static class MyHandler extends Handler {

        private WeakReference<BasketBallSettingActivity> mActivityWeakReference;

        public MyHandler(BasketBallSettingActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BasketBallSettingActivity activity = mActivityWeakReference.get();
            if (activity.isDisconnect) {
                if (activity.isClickConnect) {
                    activity.toastSpeak("连接失败");
                } else {
                    activity.toastSpeak("设备未连接，设置失败");
                }
            }
        }
    }
}
