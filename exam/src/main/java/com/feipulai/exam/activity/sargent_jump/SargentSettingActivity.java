package com.feipulai.exam.activity.sargent_jump;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.SargentJumpResult;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.sargent_jump.pair.SargentPairActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.text.MessageFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
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
    //    @BindView(R.id.cb_wireless)
//    CheckBox cbWireless;
    @BindView(R.id.tv_match)
    TextView tvMatch;
    @BindView(R.id.sp_test_id)
    Spinner spTestId;
    @BindView(R.id.tv_device_check)
    TextView tvDeviceCheck;
    @BindView(R.id.tv_light_minus)
    TextView tvLightMinus;
    @BindView(R.id.tv_light_add)
    TextView tvLightAdd;
    @BindView(R.id.ll_light)
    LinearLayout llLight;
    @BindView(R.id.ll_check)
    LinearLayout llCheck;
    @BindView(R.id.tv_accuracy_use)
    TextView tvAccuracyUse;
    private SargentSetting sargentSetting;
    private String[] spinnerItems;
    private RadioManager radioManager;
    private int match;
    private int frequency;
    private SweetAlertDialog alertDialog;
    private int deviceId;
    private static final int CAN_BE_IGNORE = 0XA2;
    private int ignoreDeviceId;
    private int flagBad;
    private Context mContext;

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
        mContext = this;
        init();
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("项目设置");
    }

    private void init() {
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
//        cbWireless.setChecked(sargentSetting.getType() == 1);
//        cbWireless.setVisibility(sargentSetting.getType() == 1 ? View.VISIBLE : View.GONE);
//        cbWireless.setOnCheckedChangeListener(this);
        tvMatch.setVisibility(sargentSetting.getType() == 2 ? View.VISIBLE : View.GONE);
        radioManager = RadioManager.getInstance();
        radioManager.init();
        radioManager.setOnRadioArrived(this);
        initSpinners();

        if (sargentSetting.getType() != 2) {
            llLight.setVisibility(View.GONE);
            llCheck.setVisibility(View.GONE);
            tvAccuracyUse.setVisibility(View.GONE);
        }
    }

    private void initSpinners() {
        int maxTestNo = TestConfigs.sCurrentItem.getTestNum();
        spinnerItems = new String[]{"1", "2", "3"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTestRound.setAdapter(spinnerAdapter);
        if (maxTestNo != 0) {
            // 数据库中已经指定了测试次数,就不能再设置了
            spTestRound.setEnabled(false);
        } else {
            spTestRound.setOnItemSelectedListener(this);
            maxTestNo = sargentSetting.getTestTimes();
        }
        spTestRound.setSelection(maxTestNo - 1);
        sargentSetting.setTestTimes(maxTestNo);

        String[] deviceCount = {"1", "2", "3", "4"};
        ArrayAdapter<String> adapter0 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, deviceCount);
        adapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDeviceCount.setAdapter(adapter0);
        spDeviceCount.setOnItemSelectedListener(this);
        spDeviceCount.setSelection(sargentSetting.getSpDeviceCount() - 1);
        spDeviceCount.setEnabled(true);
        if (sargentSetting.getType() != 2) {
            spDeviceCount.setSelection(0);
            spDeviceCount.setEnabled(false);
        }
        rgModel.setVisibility(SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN ? View.GONE : View.VISIBLE);//个人模式隐藏
        rgModel.setOnCheckedChangeListener(this);

        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN) {//循环测试仅给分组模式
            int testPattern = sargentSetting.getTestPattern();
            rgModel.check(testPattern == 0 ? R.id.rb_continue : R.id.rb_recycle);
        }

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, deviceCount);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTestId.setAdapter(adapter1);
        spTestId.setOnItemSelectedListener(this);
        deviceId = spTestId.getSelectedItemPosition() + 1;
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
//            case R.id.cb_wireless:
//                cbWireless.setChecked(b);
//                sargentSetting.setType(b ? 1 : 0);
//                break;
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        switch (adapterView.getId()) {
            case R.id.sp_test_round:
                sargentSetting.setTestTimes(position + 1);
                break;
            case R.id.sp_device_count:
                sargentSetting.setSpDeviceCount(position + 1);
                break;
            case R.id.sp_test_id:
                deviceId = spTestId.getSelectedItemPosition() + 1;
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

    @OnClick({R.id.tv_match, R.id.tv_light_minus, R.id.tv_light_add, R.id.tv_device_check, R.id.tv_accuracy_use})
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

                } else if (sargentSetting.getType() == 2) {
                    startActivity(new Intent(SargentSettingActivity.this, SargentPairActivity.class));
                }
                break;
            case R.id.tv_light_minus:
                RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                        setLight(false, deviceId)));
                break;
            case R.id.tv_light_add:
                RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                        setLight(true, deviceId)));
                break;
            case R.id.tv_device_check:
                RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                        check_self(deviceId)));
                break;
            case R.id.tv_accuracy_use:
                Logger.i("基础高度：" + sargentSetting.getBaseHeight() + "设备号：" + deviceId);
                RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                        setBaseHeight(sargentSetting.getBaseHeight(), deviceId)));
                break;

        }
    }

    //离地高度设置范围为0-255
    public byte[] setBaseHeight(int offGroundDistance, int deviceId) {
        byte[] data = {0X54, 0X44, 00, 0X0D, 01, 0x01, 01, 0x05, 00, 00, 0x00, 0x27, 0x0d};
        data[4] = (byte) deviceId;
        data[8] = (byte) ((offGroundDistance >> 8) & 0xff);// 次低位
        data[9] = (byte) (offGroundDistance & 0xff);// 最低位

        int sum = 0;
        for (int i = 2; i < 10; i++) {
            sum += data[i] & 0xff;
        }
        data[10] = (byte) sum;
        return data;
    }

    public byte[] setLight(boolean up, int deviceId) {
        byte[] data;
        if (up) {
            data = Constants.CMD_SARGENT_JUMP_LIGHT_UP;
        } else {
            data = Constants.CMD_SARGENT_JUMP_LIGHT_DOWN;
        }

        data[4] = (byte) deviceId;

        int sum = 0;
        for (int i = 2; i < 10; i++) {
            sum += data[i] & 0xff;
        }
        data[10] = (byte) sum;
        return data;
    }

    public byte[] check_self(int deviceId) {
        byte[] data = Constants.CMD_SARGENT_JUMP_CHECK_SELF;
        data[4] = (byte) deviceId;

        int sum = 0;
        for (int i = 2; i < 8; i++) {
            sum += data[i] & 0xff;
        }
        data[8] = (byte) sum;
        return data;
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

            case SerialConfigs.SARGENT_JUMP_CHECK:
                SargentJumpResult jumpResult = (SargentJumpResult) msg.obj;
                byte[] incorrectPoles = jumpResult.getIncorrectPoles();
                boolean check = true;
                flagBad = 0;
                for (int i = 0; i < incorrectPoles.length; i++) {
                    if (incorrectPoles[i] == 1) {
                        toastSpeak(MessageFormat.format("第{0}对杆出现异常", i + 1));
                        check = false;
                        flagBad++;
                    }
                }
                if (!check && flagBad < 6) {
                    mHandler.sendEmptyMessage(CAN_BE_IGNORE);
                    ignoreDeviceId = jumpResult.getDeviceId();
                } else if (check && flagBad > 5) {
                    toastSpeak("当前设备坏点数超过5点");
                } else if (check) {
                    toastSpeak("当前设备正常");
                } else
                    break;
        }
    }

    private void ignoreBad(int deviceId) {
        byte[] data = Constants.CMD_SARGENT_JUMP_IGNORE_BREAK_POINT;
        data[4] = (byte) deviceId;

        int sum = 0;
        for (int i = 2; i < 8; i++) {
            sum += data[i] & 0xff;
        }
        data[8] = (byte) sum;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                data));
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
                    frequency = SettingHelper.getSystemSetting().getUseChannel();
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
                case CAN_BE_IGNORE:
                    new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText(mContext.getString(com.feipulai.common.R.string.clear_dialog_title))
                            .setContentText("检查到有坏点" + flagBad + "个是否忽略")
                            .setConfirmText("忽略").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            ignoreBad(ignoreDeviceId);
                        }
                    }).setCancelText("否").setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    }).show();
                    break;
            }

            return false;
        }
    });


}
