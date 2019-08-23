package com.feipulai.exam.activity.medicineBall;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MedicineBallSettingActivity extends BaseActivity implements AdapterView.OnItemSelectedListener,
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
    private String[] spinnerItems;
    private MedicineBallSetting medicineBallSetting;
    @BindView(R.id.et_begin_point)
    EditText etBeginPoint;
    private SweetAlertDialog alertDialog;
    private SerialHandler mHandler = new SerialHandler(this);
    //3秒内检设备是否可用
    private volatile boolean isDisconnect = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_ball_setting);
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
                if (etBeginPoint.getText().toString().length()> 0){
                    int number = Integer.valueOf(etBeginPoint.getText().toString());
                    if ( number > 5000) {
                        ToastUtils.showShort("输入范围超出（0~5000）");
                    }else {
                        SharedPrefsUtil.putValue(MedicineBallSettingActivity.this, "SXQ", "beginPoint",
                                etBeginPoint.getText().toString());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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
            maxTestNo = medicineBallSetting.getTestTimes();
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
                ToastUtils.showShort("请先设置满分值");
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
    }



    public void checkDevice(View view) {
        alertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        alertDialog.setTitleText("终端自检中...");
        alertDialog.setCancelable(false);
        alertDialog.show();
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
        //3秒自检
        mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 5000);
    }
    private static final int MSG_DISCONNECT = 0X101;
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

        private WeakReference<MedicineBallSettingActivity> mActivityWeakReference;

        public SerialHandler(MedicineBallSettingActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MedicineBallSettingActivity activity = mActivityWeakReference.get();
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

                    case SerialConfigs.MEDICINE_BALL_EMPTY_RESPONSE://空闲命令
                        activity.isDisconnect = false;
                        isDialogShow = false;
                        activity.toastSpeak("设备连接成功");
                        break;
                }
                if (!isDialogShow && activity.alertDialog != null && activity.alertDialog.isShowing()) {
                    activity.alertDialog.dismiss();
                }

            }

        }
    }
}
