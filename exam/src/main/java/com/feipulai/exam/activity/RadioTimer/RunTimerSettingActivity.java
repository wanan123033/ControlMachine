package com.feipulai.exam.activity.RadioTimer;

import android.content.Intent;
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
import android.widget.Button;
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
import com.feipulai.device.manager.RunTimerManager;
import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.RunTimerConnectState;
import com.feipulai.device.serial.beans.RunTimerResult;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.NewRadioPairActivity;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.CorrespondTestActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.feipulai.exam.activity.RadioTimer.RunTimerConstant.CONNECT_STATE;
import static com.feipulai.exam.activity.RadioTimer.RunTimerConstant.TIME_RESPONSE;
import static com.feipulai.exam.config.EventConfigs.CONNECT_SETTING;
import static com.feipulai.exam.config.EventConfigs.SETTING_SUCCEED;

public class RunTimerSettingActivity extends BaseTitleActivity implements AdapterView.OnItemSelectedListener,
        RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener,
        TextWatcher,  RadioManager.OnRadioArrivedListener {

    @BindView(R.id.sp_test_times)
    Spinner spTestTimes;
    @BindView(R.id.sp_carry_mode)
    Spinner spMarkDegree;
    @BindView(R.id.sp_intercept_way)
    Spinner spInterceptWay;
    @BindView(R.id.sp_sensor)
    Spinner spSensor;
    @BindView(R.id.radioGroup_degree)
    RadioGroup radioGroupDegree;
    @BindView(R.id.rg_model)
    RadioGroup rgModel;
    @BindView(R.id.rg_timer_select)
    RadioGroup rg_timerSelect;
    //    @BindView(R.id.cb_full_return)
//    CheckBox cbFullReturn;
//    @BindView(R.id.et_full_male)
//    EditText etFullMale;
//    @BindView(R.id.et_full_female)
//    EditText etFullFemale;
    @BindView(R.id.et_run_num)
    EditText etRunNum;
    @BindView(R.id.et_sensitivity_num)
    EditText etSensitivityNum;
    @BindView(R.id.btn_self_check)
    TextView selfCheck;
    @BindView(R.id.cb_start)
    CheckBox cbStart;
    @BindView(R.id.cb_end)
    CheckBox cbEnd;
    @BindView(R.id.rb_hundred_second)
    RadioButton rbHundredSecond;
    @BindView(R.id.rb_ten_second)
    RadioButton rbTenSecond;
    @BindView(R.id.btn_sync_time)
    TextView syncTime;
    @BindView(R.id.btn_connect)
    Button btnConnect;
    @BindView(R.id.et_test_min)
    EditText etTestMin;
    @BindView(R.id.et_sense)
    EditText etSense;
    @BindView(R.id.ll_test_min)
    LinearLayout llTestMin;
    @BindView(R.id.ll_sensitive)
    LinearLayout llSense;
    @BindView(R.id.ll_sensitivity_num)
    LinearLayout llSensitivity;
    private RunTimerSetting runTimerSetting;
    private int intercept_point;
    private SweetAlertDialog alertDialog;
//    private SerialDeviceManager deviceManager;
    private SportTimerManger sportTimerManger;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_run_timer_setting;
    }

    @Override
    protected void initData() {
        init();
    }

    private void init() {
        runTimerSetting = SharedPrefsUtil.loadFormSource(this, RunTimerSetting.class);
        if (null == runTimerSetting) {
            runTimerSetting = new RunTimerSetting();
        }
        initSpinners();
        initRadioGroup();
//        boolean isFullReturn = runTimerSetting.isFullReturn();
//        cbFullReturn.setChecked(isFullReturn);
//        cbFullReturn.setOnCheckedChangeListener(this);
//
//        etFullFemale.setText(runTimerSetting.getFemaleFull());
//        etFullMale.setText(runTimerSetting.getMaleFull());
        etRunNum.setText(runTimerSetting.getRunNum());

//        etFullMale.addTextChangedListener(this);
//        etFullFemale.addTextChangedListener(this);
        etRunNum.addTextChangedListener(this);
        etSensitivityNum.setText(String.format("%d", runTimerSetting.getSensitivityNum()));
        if (runTimerSetting.getConnectType() == 1) {
            selfCheck.setText("????????????");
        }

        if (runTimerSetting.getConnectType() == 1) {
            rgModel.setVisibility(View.GONE);
        }

        if (SettingHelper.getSystemSetting().getRadioLed() == 1) {
            syncTime.setVisibility(View.VISIBLE);
            RadioManager.getInstance().setOnRadioArrived(this);
            sportTimerManger = new SportTimerManger();
            sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 0);
            btnConnect.setVisibility(View.VISIBLE);
            etSense.setText(runTimerSetting.getSensity()+"");
            etTestMin.setText(runTimerSetting.getMinEidit()+"");
            llSense.setVisibility(View.VISIBLE);
            llTestMin.setVisibility(View.VISIBLE);
            llSensitivity.setVisibility(View.GONE);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (runTimerSetting.getConnectType() == 0) {
            SerialDeviceManager.getInstance().setRS232ResiltListener(runTimerListener);
        } else {
            RadioManager.getInstance().setOnRadioArrived(this);
        }
    }
    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("????????????");
    }

    /**
     * ??????????????????
     */
    private void initRadioGroup() {
        radioGroupDegree.setOnCheckedChangeListener(this);

        rgModel.setOnCheckedChangeListener(this);
        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {//????????????????????????
            rgModel.setEnabled(false);
            rgModel.setVisibility(View.GONE);
        }
        //????????????
        int digital = TestConfigs.sCurrentItem.getDigital();

        radioGroupDegree.check(digital == 1 ? R.id.rb_ten_second : R.id.rb_hundred_second);


        //????????????
        intercept_point = runTimerSetting.getInterceptPoint();
        switch (intercept_point) {
            case 1:
                cbStart.setChecked(true);
                cbEnd.setChecked(false);
                break;
            case 2:
                cbStart.setChecked(false);
                cbEnd.setChecked(true);
                break;
            case 3:
                cbStart.setChecked(true);
                cbEnd.setChecked(true);
                break;
            case 0:
                cbStart.setChecked(false);
                cbEnd.setChecked(false);
                break;
        }

        cbStart.setOnCheckedChangeListener(this);
        cbEnd.setOnCheckedChangeListener(this);
        //????????????
        boolean testModel = runTimerSetting.isTestModel();
        rgModel.check(testModel ? R.id.rb_continue : R.id.rb_recycle);
    }

    /**
     * ??????????????????
     */
    private void initSpinners() {
        int maxTestNo = TestConfigs.sCurrentItem.getTestNum();
        String[] times = {"1", "2", "3"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, times);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTestTimes.setAdapter(spinnerAdapter);
        spTestTimes.setOnItemSelectedListener(this);
        if (maxTestNo != 0) {
            // ???????????????????????????????????????,?????????????????????
            spTestTimes.setEnabled(false);
            spTestTimes.setSelection(maxTestNo - 1);
        } else {
            spTestTimes.setSelection(runTimerSetting.getTestTimes() - 1);
        }

        int carryMode = TestConfigs.sCurrentItem.getCarryMode();
        String[] spinnerItems = {"????????????", "?????????", "????????????"};
        ArrayAdapter<String> adapter0 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems);
        adapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMarkDegree.setAdapter(adapter0);
        spMarkDegree.setOnItemSelectedListener(this);
        if (carryMode != 0) {
            spMarkDegree.setEnabled(false);
            spMarkDegree.setSelection(carryMode - 1);
            runTimerSetting.setMarkDegree(carryMode);
        } else {
            spMarkDegree.setSelection(runTimerSetting.getMarkDegree() - 1);
        }

        String[] interceptItems = {"??????????????????", "?????????????????????"};
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, interceptItems);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spInterceptWay.setAdapter(adapter1);
        spInterceptWay.setOnItemSelectedListener(this);
        int intercept = runTimerSetting.getInterceptWay();
        spInterceptWay.setSelection(intercept);

        String[] items = {"0", "1", "2", "3", "4", "5", "6", "7"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSensor.setAdapter(adapter2);
        spSensor.setOnItemSelectedListener(this);
        int sensor = runTimerSetting.getSensor();
        spSensor.setSelection(sensor);

        setTimerSelect();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.sp_test_times:
                runTimerSetting.setTestTimes(i + 1);
                Log.i("post", "post ITEM_SETTING_UPDATE");
                break;
            case R.id.sp_carry_mode:
                runTimerSetting.setMarkDegree(i + 1);
                break;
            case R.id.sp_intercept_way:
                runTimerSetting.setInterceptWay(i);
                break;
            case R.id.sp_sensor:
                runTimerSetting.setSensor(i);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkId) {

        switch (checkId) {
            case R.id.rb_hundred_second:
                runTimerSetting.setDigital(2);
                break;
            case R.id.rb_ten_second:
                runTimerSetting.setDigital(1);
                break;

            case R.id.rb_continue:
                runTimerSetting.setTestModel(true);
                break;
            case R.id.rb_recycle:
                runTimerSetting.setTestModel(false);
                break;
            case R.id.rb_unified:
                runTimerSetting.setTimer_select(false);
                break;
            case R.id.rb_independent:
                runTimerSetting.setTimer_select(true);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.cb_start:
                runTimerSetting.setStartPoint(b ? 1 : 0);
                getInterceptPoint();
                setTimerSelect();
                break;
            case R.id.cb_end:
                runTimerSetting.setEndPoint(b ? 2 : 0);
                getInterceptPoint();
                setTimerSelect();
                break;
        }

    }

    private void getInterceptPoint() {
        runTimerSetting.setInterceptPoint(runTimerSetting.getStartPoint() + runTimerSetting.getEndPoint());
    }

    /**
     * ????????????????????????????????????????????????
     */
    private void setTimerSelect() {
        if (runTimerSetting.getInterceptPoint() == 3 && runTimerSetting.getConnectType() == 1) {//???????????? ?????????????????????
            rg_timerSelect.setVisibility(View.VISIBLE);
            rg_timerSelect.setOnCheckedChangeListener(this);
            rg_timerSelect.check(runTimerSetting.isTimer_select() ? R.id.rb_independent : R.id.rb_unified);
        } else {
            rg_timerSelect.setVisibility(View.GONE);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
//        runTimerSetting.setFemaleFull(etFullFemale.getText().toString().trim());
//        runTimerSetting.setMaleFull(etFullMale.getText().toString().trim());
        runTimerSetting.setRunNum(etRunNum.getText().toString());
        EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_COUNT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        TestConfigs.sCurrentItem.setCarryMode(runTimerSetting.getMarkDegree());
        TestConfigs.sCurrentItem.setDigital(runTimerSetting.getDigital());
        String senNum = etSensitivityNum.getText().toString();
        runTimerSetting.setSensitivityNum(TextUtils.isEmpty(senNum) ? 5 : Integer.parseInt(senNum));
        getInterceptPoint();
        if (sportTimerManger!= null){
            if (TextUtils.isEmpty(etSense.getText().toString())){
                runTimerSetting.setSensity(20);
            }else {
                runTimerSetting.setSensity(Integer.parseInt(etSense.getText().toString().trim()));
            }
            sportTimerManger.setSensitiveTime(SettingHelper.getSystemSetting().getHostId(),runTimerSetting.getMinEidit());
            if (TextUtils.isEmpty(etTestMin.getText().toString())){
                runTimerSetting.setMinEidit(1);
            }else {
                runTimerSetting.setMinEidit(Integer.parseInt(etTestMin.getText().toString().trim()));
            }
            sportTimerManger.setMinTime(SettingHelper.getSystemSetting().getHostId(),runTimerSetting.getMinEidit());
        }
        SharedPrefsUtil.save(this, runTimerSetting);
        EventBus.getDefault().post(new BaseEvent(EventConfigs.CONNECT_SETTING));

    }

    @OnClick({R.id.btn_self_check, R.id.btn_sync_time,R.id.btn_connect})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_self_check:
                if (runTimerSetting.getConnectType() == 1) {
                    startActivity(new Intent(this, NewRadioPairActivity.class));
                } else {
                    if (runTimerSetting.getStartPoint() + runTimerSetting.getEndPoint() == 0) {
                        toastSpeak("?????????????????????");
                        return;
                    }
                    promote = 0;
//                mProgressDialog = ProgressDialog.show(this, "", "???????????????...", true);
                    alertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                    alertDialog.setTitleText("???????????????...");
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                    EventBus.getDefault().post(new BaseEvent(EventConfigs.CONNECT_SETTING));
                    //3?????????
                    mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 5000);
                }
                break;
            case R.id.btn_sync_time:
                sportTimerManger.syncTime(SettingHelper.getSystemSetting().getHostId(), getTime());
//                mHandler.sendEmptyMessageDelayed(MSG_SYNC_TIME, 500);
                break;
            case R.id.btn_connect:
                startActivity(new Intent(this, CorrespondTestActivity.class));
                break;
        }
    }

    /**
     * ????????????????????????????????? ???????????????
     *
     * @return
     */
    public int getTime() {
        Calendar Cld = Calendar.getInstance();
        int HH = Cld.get(Calendar.HOUR_OF_DAY);
        int mm = Cld.get(Calendar.MINUTE);
        int SS = Cld.get(Calendar.SECOND);
        int MI = Cld.get(Calendar.MILLISECOND);
        return HH * 60 * 60 * 1000 + mm * 60 * 1000 + SS * 1000 + MI;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        switch (baseEvent.getTagInt()){
            case SETTING_SUCCEED:
                Log.i("CONNECT_SETTING","SETTING_SUCCEED");
                mHandler.sendEmptyMessage(MSG_CONNECT);
                break;
        }
    }



    private final int MSG_DISCONNECT = 0x1001;
    private final int MSG_CONNECT = 0x1002;
    private final int MSG_SYNC_TIME = 0x1003;
    private int promote = 0;
    //3???????????????????????????
    private volatile boolean isDisconnect = true;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //????????????????????????
            boolean isDialogShow = true;
            switch (msg.what) {
                case MSG_DISCONNECT://????????????
                    if (isDisconnect) {
                        toastSpeak("???????????????");
                        //??????????????????????????????????????????
                        if (alertDialog.isShowing()) {
                            alertDialog.dismiss();
                        }
                    }
                    break;

                case MSG_CONNECT://????????????
                    isDisconnect = false;
                    isDialogShow = false;
                    if (promote == 0) {
                        toastSpeak("??????????????????");
                        promote++;
                    }
                    break;
                case MSG_SYNC_TIME:
                    sportTimerManger.getTime(1, SettingHelper.getSystemSetting().getHostId());
//                    int runNum = Integer.parseInt(runTimerSetting.getRunNum());
//                    if (runTimerSetting.getInterceptPoint() == 3) {
//                        runNum = runNum * 2;
//                    }
//                    for (int i = 0; i < runNum; i++) {
//                        try {
//                            sportTimerManger.getTime(i + 1, SettingHelper.getSystemSetting().getHostId());
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    break;
            }
            if (!isDialogShow && alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            return false;
        }
    });

    @Override
    public void onRadioArrived(Message msg) {
        switch (msg.what) {
            case SerialConfigs.SPORT_TIMER_GET_TIME:
                if (msg.obj instanceof SportResult) {
                    if (((SportResult) msg.obj).getLongTime() > 0) {
                        MyApplication.RADIO_TIME_SYNC = true;
                        ToastUtils.showShort("??????????????????");
                    }

                }
                break;
        }

    }

    private RunTimerImpl runTimerListener = new RunTimerImpl(new RunTimerImpl.RunTimerListener() {

        @Override
        public void onGetTime(RunTimerResult result) {


        }

        @Override
        public void onConnected(RunTimerConnectState connectState) {
            EventBus.getDefault().post(new BaseEvent(EventConfigs.SETTING_SUCCEED));
        }

        @Override
        public void onTestState(int state) {

        }
    });

}
