package com.feipulai.exam.activity.sargent_jump;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.SargentJumpResult;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class SargentSettingActivity extends BaseTitleActivity implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener, RadioManager.OnRadioArrivedListener {
    @BindView(R.id.cb_run_up)
    CheckBox cbRunUp;
    @BindView(R.id.sp_device_count)
    Spinner spDeviceCount;
    @BindView(R.id.sp_test_round)
    Spinner spTestRound;
    @BindView(R.id.et_begin_point)
    EditText etBeginPoint;
    @BindView(R.id.ll_begin_point)
    LinearLayout llBeginPoint;
    @BindView(R.id.et_full_male)
    EditText etFullMale;
    @BindView(R.id.et_full_female)
    EditText etFullFemale;
    @BindView(R.id.cb_full_return)
    CheckBox cbFullReturn;
    @BindView(R.id.rb_continue)
    RadioButton rbContinue;
    @BindView(R.id.rb_recycle)
    RadioButton rbRecycle;
    @BindView(R.id.rg_model)
    RadioGroup rgModel;
    @BindView(R.id.cb_wireless)
    CheckBox cbWireless;
    @BindView(R.id.tv_match)
    TextView tvMatch;
    private SargentSetting sargentSetting;
    private String[] spinnerItems;
    private RadioManager radioManager;
    private int match;
    private int frequency;
    private SweetAlertDialog alertDialog;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_sargent_setting;
    }

    @Override
    protected void initData() {
        sargentSetting = SharedPrefsUtil.loadFormSource(this, SargentSetting.class);
        if (sargentSetting == null) {
            sargentSetting = new SargentSetting();
        }

        init();
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

    private void init() {
        initSpinners();
        boolean isFullReturn = sargentSetting.isFullReturn();
        cbFullReturn.setChecked(isFullReturn);
        cbFullReturn.setOnCheckedChangeListener(this);
        etFullFemale.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sargentSetting.setFemaleFull(etFullFemale.getText().toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etFullMale.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sargentSetting.setMaleFull(etFullMale.getText().toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etFullMale.setText(sargentSetting.getMaleFull());
        etFullFemale.setText(sargentSetting.getFemaleFull());

        String beginPoint = SharedPrefsUtil.getValue(this, "MG", "beginHeight", "0");
        etBeginPoint.setText(beginPoint);

        etBeginPoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etBeginPoint.getText().toString().length() > 0) {
                    int number = Integer.valueOf(etBeginPoint.getText().toString());
                    if (number > 500) {
                        ToastUtils.showShort("输入范围超出（0~500）");
                    } else {
                        SharedPrefsUtil.putValue(SargentSettingActivity.this, "MG", "beginHeight",
                                etBeginPoint.getText().toString());
                        sargentSetting.setBaseHeight(Integer.parseInt(etBeginPoint.getText().toString().trim()));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        // 0 原地起跳 1助跑
        cbRunUp.setChecked(sargentSetting.getRunUp() == 0);
        cbRunUp.setOnCheckedChangeListener(this);
        // 1无线 0有线
        cbWireless.setChecked(sargentSetting.getType() == 1);
        cbWireless.setVisibility(sargentSetting.getType() == 1 ? View.VISIBLE : View.GONE);
        cbWireless.setOnCheckedChangeListener(this);
        radioManager = RadioManager.getInstance();
        radioManager.init();
        radioManager.setOnRadioArrived(this);
    }

    private void initSpinners() {
        int maxTestNo = TestConfigs.sCurrentItem.getTestNum();
        spinnerItems = new String[]{"1", "2", "3"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems);
        spTestRound.setAdapter(spinnerAdapter);
        if (maxTestNo != 0) {
            // 数据库中已经指定了测试次数,就不能再设置了
            spTestRound.setEnabled(false);
        } else {
            spTestRound.setOnItemSelectedListener(this);
            maxTestNo = sargentSetting.getTestTimes();
        }
        spTestRound.setSelection(maxTestNo - 1);

        String[] deviceCount = {"1", "2", "3"};
        ArrayAdapter<String> adapter0 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, deviceCount);
        spDeviceCount.setAdapter(adapter0);
        spDeviceCount.setOnItemSelectedListener(this);
        spDeviceCount.setEnabled(false);

        rgModel.setVisibility(SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN ? View.GONE : View.VISIBLE);//个人模式隐藏
        rgModel.setOnCheckedChangeListener(this);

        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN) {//循环测试仅给分组模式
            int testPattern = sargentSetting.getTestPattern();
            rgModel.check(testPattern == 0 ? R.id.rb_continue : R.id.rb_recycle);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean b) {
        switch (buttonView.getId()) {
            case R.id.cb_run_up:
                cbRunUp.setChecked(b);
                sargentSetting.setRunUp(b ? 0 : 1);
                break;
            case R.id.cb_full_return:
                if (b) {
                    if (TextUtils.isEmpty(sargentSetting.getMaleFull())
                            || TextUtils.isEmpty(sargentSetting.getMaleFull())) {
                        ToastUtils.showShort("请先设置满分值");
                        cbFullReturn.setChecked(false);
                        sargentSetting.setFullReturn(false);
                    } else {
                        sargentSetting.setFullReturn(true);
                    }
                } else {
                    sargentSetting.setFullReturn(false);
                }
                break;
            case R.id.cb_wireless:
                cbWireless.setChecked(b);
                sargentSetting.setType(b ? 1 : 0);
                break;
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        switch (adapterView.getId()) {
            case R.id.sp_test_round:
                sargentSetting.setTestTimes(position + 1);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_continue:
                sargentSetting.setTestPattern(0);
                break;
            case R.id.rb_recycle:
                sargentSetting.setTestPattern(1);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPrefsUtil.save(this, sargentSetting);
        EventBus.getDefault().post(new BaseEvent(EventConfigs.ITEM_SETTING_UPDATE));
    }

    @OnClick({R.id.tv_match})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_match:
                if (sargentSetting.getType() == 1) {
                    radioManager.sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
                    if (alertDialog == null || !alertDialog.isShowing()) {
                        match = 0;
//                        mProgressDialog = ProgressDialog.show(this, "", "终端匹配中...", true);
                        alertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                        alertDialog.setTitleText("终端匹配中...");
                        alertDialog.setCancelable(false);
                        alertDialog.show();

                        mHandler.sendEmptyMessageDelayed(3, 10 * 1000);
                    }

                }
                break;

        }
    }

    @Override
    public void onRadioArrived(Message msg) {
        switch (msg.what) {
            case SerialConfigs.SARGENT_JUMP_SET_MATCH:
                match++;
                Log.i("sargent", "sargent_match");
                SargentJumpResult result = (SargentJumpResult) msg.obj;
                int fre = result.getFrequency();
                Log.i("sargent", "frequency:" + fre);
                if (match == 1) {
                    radioManager.sendCommand(new ConvertCommand(new RadioChannelCommand(fre)));
                    mHandler.sendEmptyMessageDelayed(1, 600);
                }
                if (match == 2) {
                    mHandler.sendEmptyMessage(2);
                }

                break;
        }
    }

    private int sum(byte[] cmd) {
        int sum = 0;
        for (int i = 2; i <= 12; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    byte[] cmd = SerialConfigs.CMD_SARGENT_JUMP_SET_MATCH;
                    cmd[4] = (byte) 1;
//                    cmd[8] = 42;
                    frequency = SerialConfigs.sProChannels.get(ItemDefault.CODE_MG) + SettingHelper.getSystemSetting().getHostId() - 1;
                    cmd[8] = (byte) (frequency);
                    cmd[13] = (byte) (sum(cmd) & 0xff);
                    Log.i("match", "cmd:" + StringUtility.bytesToHexString(cmd));
                    radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
                    radioManager.sendCommand(new ConvertCommand(new RadioChannelCommand(frequency)));


                    break;
                case 2:
                    alertDialog.dismiss();
                    ToastUtils.showShort("匹配成功");
                    break;
                case 3:
                    if (alertDialog != null && alertDialog.isShowing()) {
                        ToastUtils.showShort("匹配失败");
                        alertDialog.dismiss();
                    }
                    break;
            }

            return false;
        }
    });
}
