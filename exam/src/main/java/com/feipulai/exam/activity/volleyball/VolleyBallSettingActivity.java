package com.feipulai.exam.activity.volleyball;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.TextView;

import com.feipulai.common.utils.HandlerUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.common.view.dialog.DialogUtils;
import com.feipulai.device.manager.VolleyBallManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.VolleyBallCheck;
import com.feipulai.device.serial.beans.VolleyPair868Result;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.volleyball.more_devices.VolleyBallCheckDialog;
import com.feipulai.exam.activity.volleyball.more_devices.VolleyBallPairActivity;
import com.feipulai.exam.config.TestConfigs;
import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class VolleyBallSettingActivity
        extends BaseTitleActivity
        implements CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener, TextWatcher
        , SerialDeviceManager.RS232ResiltListener, RadioManager.OnRadioArrivedListener {

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
    @BindView(R.id.txt_device_versions)
    TextView txtDeviceVersions;
    @BindView(R.id.tv_device_connect)
    TextView tvDeviceConnect;

    private Integer[] testRound;

    private VolleyBallSetting setting;
    private SweetAlertDialog alertDialog;
    //3???????????????????????????
    private volatile boolean isDisconnect = true;
    private static final int MSG_DISCONNECT = 0X101;
    private static final int MSG_CHECK = 0X102;
    private SerialHandler mHandler = new SerialHandler(this);
    private CheckDeviceView checkDeviceView;
    private VolleyBallManager volleyBallManager;
    private Dialog checkDialog;

    private long disconnectTime;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_volleyball_setting;
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, VolleyBallSetting.class);
        volleyBallManager = new VolleyBallManager(setting.getType());
        //??????????????????
        int maxTestNo = (TestConfigs.sCurrentItem.getTestNum() == 0 && TestConfigs.getMaxTestCount(this) <= TestConfigs.MAX_TEST_NO)
                ? TestConfigs.MAX_TEST_NO : TestConfigs.getMaxTestCount(this);

        testRound = new Integer[maxTestNo];
        for (int i = 0; i < maxTestNo; i++) {
            testRound[i] = i + 1;
        }
        ArrayAdapter spTestRoundAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testRound);
        spTestRoundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTestNo.setAdapter(spTestRoundAdapter);
        spTestNo.setSelection(TestConfigs.getMaxTestCount(this) - 1);
        // ???????????????????????????????????????,?????????????????????
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
        volleyBallManager.getVersions();

        if (setting.getType() > 0) {
            tvDeviceConnect.setVisibility(View.VISIBLE);
        }
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("????????????");
    }

    @Override
    protected void onResume() {
        super.onResume();
        RadioManager.getInstance().setOnRadioArrived(this);
    }

    @Override
    public void finish() {
        SharedPrefsUtil.save(this, setting);
        Logger.i("??????????????????:" + setting.toString());
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
                ToastUtils.showShort("???????????????,????????????");
                break;
            case R.id.tv_device_check:
                if (setting.getType() == 0) {
                    if (volleyBallManager == null) {
                        volleyBallManager = new VolleyBallManager(setting.getType());
                    }
                    showCheckDiglog();
                    volleyBallManager.checkDevice(SettingHelper.getSystemSetting().getHostId(), 1);
                    isDisconnect = true;
//                mProgressDialog = ProgressDialog.show(this, "", "???????????????...", true);
                    alertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                    alertDialog.setTitleText("???????????????...");
                    alertDialog.setCancelable(false);
                    alertDialog.show();

                    //3?????????
                    mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 5000);
                } else if (setting.getType() == 2) {
                    //TODO ??????????????????

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("?????????");
                    builder.setItems(new String[]{"?????????", "?????????", "?????????", "?????????"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            VolleyBallCheckDialog dialog1 = new VolleyBallCheckDialog();
                            Bundle bundle = new Bundle();
                            bundle.putInt("deviceId", which + 1);
                            dialog1.setArguments(bundle);
                            dialog1.show(getFragmentManager(), "VolleyBallCheckDialog");
                        }
                    });
                    builder.create().show();

                } else if (setting.getType() == 1) {
                    VolleyBallCheckDialog dialog1 = new VolleyBallCheckDialog();
                    Bundle bundle = new Bundle();
                    bundle.putInt("deviceId", 1);
                    dialog1.setArguments(bundle);
                    dialog1.show(getFragmentManager(), "VolleyBallCheckDialog");
                }
                break;
        }
    }

    @Override
    public void onRadioArrived(Message msg) {
        switch (msg.what) {
            case SerialConfigs.VOLLEY_BALL_SELFCHECK:
                isDisconnect = false;
                if (checkDeviceView == null) {
                    return;
                }
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                VolleyPair868Result result = (VolleyPair868Result) msg.obj;
                VolleyBallCheck volleyBallCheck = new VolleyBallCheck();
                volleyBallCheck.setCheckType(8);
                volleyBallCheck.setDeviceType(result.getDeviceType());
                volleyBallCheck.setPoleNum(result.getPoleNum());
                volleyBallCheck.setPositionList(result.getPositionList());
                msg.obj = volleyBallCheck;
                HandlerUtil.sendMessage(mHandler, MSG_CHECK, msg.obj);
                break;
        }
    }

    @Override
    public void onRS232Result(final Message msg) {

        switch (msg.what) {
            case SerialConfigs.VOLLEYBALL_CHECK_RESPONSE:
                isDisconnect = false;
                if (checkDeviceView == null) {
                    return;
                }
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                HandlerUtil.sendMessage(mHandler, MSG_CHECK, msg.obj);
                break;
            case SerialConfigs.VOLLEYBALL_LOSE_DOT_RESPONSE:
                toastSpeak("????????????");
                break;
            case SerialConfigs.VOLLEYBALL_VERSION_RESPONSE:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtDeviceVersions.setVisibility(View.VISIBLE);
                        txtDeviceVersions.setText(String.format(getString(R.string.device_versions), msg.obj.toString()));
                    }
                });

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
                ToastUtils.showShort("???????????????????????????1000");
            } else {
                setting.setMaleFullScore(value);
            }
        }

        text = editFemaleFull.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            value = Integer.parseInt(text);
            if (value > 1000) {
                ToastUtils.showShort("???????????????????????????1000");
            } else {
                setting.setFemaleFullScore(value);
            }
        }

        text = etTestTime.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            value = Integer.parseInt(text);
            if (value > 3600) {
                ToastUtils.showShort("?????????????????????3600");
            } else {
                setting.setTestTime(value);
            }
        }
    }


    private void showCheckDiglog() {
        checkDeviceView = new CheckDeviceView(this);

        checkDeviceView.setUnunitedData(setting.getTestPattern() == 0 ? VolleyBallSetting.ANTIAIRCRAFT_POLE : VolleyBallSetting.WALL_POLE);
        checkDeviceView.setWiress(setting.getType() == 1);
        checkDialog = DialogUtils.create(this, checkDeviceView, true);
        checkDialog.show();
        //???????????????????????????????????????????????????-- show ????????????show ???????????????????????????
        checkDeviceView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom) {
                int height = v.getHeight();     //?????????view ???v ????????????????????????

                int needHeight = 430;

                if (height > needHeight) {
                    //?????????????????? LayoutParams ????????? FrameLayout?????????
                    v.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                            needHeight));
                }
            }
        });
    }


    @OnClick(R.id.tv_device_connect)
    public void onViewClicked() {
        IntentUtil.gotoActivity(this, VolleyBallPairActivity.class);
    }


    /**
     * ??????
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
                    case MSG_DISCONNECT://????????????
                        if (activity.isDisconnect) {
                            activity.toastSpeak("???????????????");
                            //??????????????????????????????????????????
                            if (activity.alertDialog.isShowing()) {
                                activity.alertDialog.dismiss();
                            }
                        }
                        break;
                    case MSG_CHECK:
                        final VolleyBallCheck volleyBallCheck = (VolleyBallCheck) msg.obj;

                        if (volleyBallCheck.getCheckType() == 8) {
                            if (volleyBallCheck.getDeviceType() == activity.setting.getTestPattern()) {
//                                int itemPole = volleyBallCheck.getDeviceType() == 0 ? VolleyBallSetting.ANTIAIRCRAFT_POLE : VolleyBallSetting.WALL_POLE;
//                                if (volleyBallCheck.getPoleNum() == itemPole) {
//                                    activity.checkDeviceView.setData(volleyBallCheck.getPoleNum(), volleyBallCheck.getPositionList());
//                                }else{
//                                    activity.checkDeviceView.setData(itemPole, volleyBallCheck.getPositionList());
//                                }
                                activity.checkDeviceView.setData(volleyBallCheck.getPoleNum() / 2, volleyBallCheck.getPositionList());
                            } else {
                                if (!activity.isDestroyed() && (System.currentTimeMillis() - activity.disconnectTime) > 30000) {
                                    activity.toastSpeak("??????????????????????????????????????????");
                                    activity.disconnectTime = System.currentTimeMillis();
                                }
                            }
                        } else {
                            activity.checkDeviceView.setData(activity.setting.getTestPattern() == 0 ? VolleyBallSetting.ANTIAIRCRAFT_POLE : VolleyBallSetting.WALL_POLE, volleyBallCheck.getPositionList());
                        }
                        try {
                            Thread.sleep(100);
                            if (activity.checkDialog.isShowing()) {
                                activity.volleyBallManager.checkDevice(SettingHelper.getSystemSetting().getHostId(), 1);
                            }

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        break;
                }
            }

        }
    }
}
