package com.feipulai.exam.activity.medicineBall;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.MedicineBallSelfCheckResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseActivity;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.medicineBall.pair.MedicineBallPairActivity;
import com.feipulai.exam.activity.setting.CorrespondTestActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MedicineBallSettingActivity extends BaseTitleActivity implements AdapterView.OnItemSelectedListener,
        RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "MedicineBallSettingActi";
    @BindView(R.id.sp_device_count)
    Spinner spDeviceCount;
    @BindView(R.id.sp_test_round)
    Spinner spTestRound;
    @BindView(R.id.rg_model)
    RadioGroup rgModel;
    @BindView(R.id.cb_full_return)
    CheckBox cbFullReturn;
    @BindView(R.id.et_full_male)
    EditText etFullMale;
    @BindView(R.id.et_full_female)
    EditText etFullFemale;
    @BindView(R.id.tv_match)
    TextView tvMatch;
    @BindView(R.id.tv_device_check)
    TextView tvDeviceCheck;
    @BindView(R.id.tv_device_result)
    TextView tvDeviceResult;
    @BindView(R.id.btn_connect)
    Button btnConnect;

    private String[] spinnerItems;
    private MedicineBallSetting medicineBallSetting;
    @BindView(R.id.et_begin_point)
    EditText etBeginPoint;
    private SweetAlertDialog alertDialog;
    private SerialHandler mHandler = new SerialHandler(this);
    //3???????????????????????????
    private volatile boolean isDisconnect = true;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_medicine_ball_setting;
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("????????????");
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        medicineBallSetting = SharedPrefsUtil.loadFormSource(this, MedicineBallSetting.class);
        if (medicineBallSetting == null) {
            medicineBallSetting = new MedicineBallSetting();
        }
        init();
    }

    private void init() {
        initSpinners();
        boolean isFullReturn = medicineBallSetting.isFullReturn();
        cbFullReturn.setChecked(isFullReturn);
        cbFullReturn.setOnCheckedChangeListener(this);
        etFullFemale.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                medicineBallSetting.setFemaleFull(etFullFemale.getText().toString().trim());
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
                medicineBallSetting.setMaleFull(etFullMale.getText().toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etFullMale.setText(medicineBallSetting.getMaleFull());
        etFullFemale.setText(medicineBallSetting.getFemaleFull());
        String beginPoint = SharedPrefsUtil.getValue(this, "SXQ", "beginPoint", "0");
        etBeginPoint.setText(beginPoint);

        etBeginPoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etBeginPoint.getText().toString().length() > 0) {
                    int number = Integer.valueOf(etBeginPoint.getText().toString());
                    if (number > 5000) {
                        ToastUtils.showShort("?????????????????????0~5000???");
                    } else {
                        SharedPrefsUtil.putValue(MedicineBallSettingActivity.this, "SXQ", "beginPoint",
                                etBeginPoint.getText().toString());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tvMatch.setVisibility(medicineBallSetting.getConnectType() == 1 ? View.VISIBLE : View.GONE);
        btnConnect.setVisibility(medicineBallSetting.getConnectType() == 1 ? View.VISIBLE : View.GONE);
        tvDeviceCheck.setVisibility(medicineBallSetting.getConnectType() == 0 ? View.VISIBLE : View.GONE);
    }

    private void initSpinners() {
        int maxTestNo = TestConfigs.sCurrentItem.getTestNum();
        spinnerItems = new String[]{"1", "2", "3"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTestRound.setAdapter(spinnerAdapter);
        if (maxTestNo != 0) {
            // ???????????????????????????????????????,?????????????????????
            spTestRound.setEnabled(false);
        } else {
            spTestRound.setOnItemSelectedListener(this);
            maxTestNo = medicineBallSetting.getTestTimes();
        }
        spTestRound.setSelection(maxTestNo - 1);

        String[] deviceCount = {"1", "2", "3", "4"};
        ArrayAdapter<String> adapter0 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, deviceCount);
        adapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDeviceCount.setAdapter(adapter0);
        spDeviceCount.setOnItemSelectedListener(this);
        spDeviceCount.setSelection(medicineBallSetting.getSpDeviceCount() - 1);
        spDeviceCount.setEnabled(medicineBallSetting.getConnectType() == 0 ? false : true);
        if (medicineBallSetting.getConnectType() == 0) {
            spDeviceCount.setSelection(0);
            spDeviceCount.setEnabled(false);
        }
        rgModel.setVisibility(SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN ? View.GONE : View.VISIBLE);//??????????????????
        rgModel.setOnCheckedChangeListener(this);

        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN) {//??????????????????????????????
            int testPattern = medicineBallSetting.getTestPattern();
            rgModel.check(testPattern == 0 ? R.id.rb_continue : R.id.rb_recycle);
        }
        SerialDeviceManager.getInstance().setRS232ResiltListener(listener);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.sp_test_round:
                medicineBallSetting.setTestTimes(i + 1);
                EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_COUNT));
                break;
            case R.id.sp_device_count:
                medicineBallSetting.setSpDeviceCount(i + 1);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
        switch (checkId) {
            case R.id.rb_continue:
                medicineBallSetting.setTestPattern(0);
                break;
            case R.id.rb_recycle:
                medicineBallSetting.setTestPattern(1);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            if (TextUtils.isEmpty(medicineBallSetting.getMaleFull())
                    || TextUtils.isEmpty(medicineBallSetting.getMaleFull())) {
                ToastUtils.showShort("?????????????????????");
                cbFullReturn.setChecked(false);
                medicineBallSetting.setFullReturn(false);
            } else {
                medicineBallSetting.setFullReturn(true);
            }
        } else {
            medicineBallSetting.setFullReturn(false);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPrefsUtil.save(this, medicineBallSetting);
        mHandler.removeCallbacks(null);
    }


    public void checkDevice(View view) {
        tvDeviceResult.setText("");
        alertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        alertDialog.setTitleText("???????????????...");
        alertDialog.setCancelable(false);
        alertDialog.show();
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
        //3?????????
        mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 5000);
    }

    private static final int MSG_DISCONNECT = 0X101;
    SerialDeviceManager.RS232ResiltListener listener = new SerialDeviceManager.RS232ResiltListener() {
        @Override
        public void onRS232Result(Message msg) {
            mHandler.sendMessage(msg);
        }
    };

    @OnClick({R.id.tv_match,R.id.btn_connect})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_match:
                startActivity(new Intent(this, MedicineBallPairActivity.class));
                break;
            case R.id.btn_connect:
                startActivity(new Intent(this, CorrespondTestActivity.class));
                break;
        }
    }

    /**
     * ??????
     */
    private static class SerialHandler extends Handler {

        private WeakReference<MedicineBallSettingActivity> mActivityWeakReference;

        public SerialHandler(MedicineBallSettingActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MedicineBallSettingActivity activity = mActivityWeakReference.get();
            //????????????????????????
            boolean isDialogShow = true;
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

                    case SerialConfigs.MEDICINE_BALL_EMPTY_RESPONSE://????????????
                        activity.isDisconnect = false;
                        isDialogShow = false;
                        activity.toastSpeak("??????????????????");
                        break;
                    case SerialConfigs.MEDICINE_BALL_SELF_CHECK_RESPONSE:
                        MedicineBallSelfCheckResult selfCheckResult = (MedicineBallSelfCheckResult) msg.obj;
                        int[] errors = selfCheckResult.getIncorrectPoles();
                        for (int i = 1; i < errors.length + 1; i++) {
                            if (errors[i - 1] == 1) {
                                int e = errors[i] + 1;
                                activity.tvDeviceResult.setText(String.format("%s?????????????????????", "???" + e));
                            }
                        }

                        break;
                }
                if (!isDialogShow && activity.alertDialog != null && activity.alertDialog.isShowing()) {
                    activity.alertDialog.dismiss();
                }

            }

        }
    }
}
