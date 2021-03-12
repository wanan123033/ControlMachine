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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.manager.RunTimerManager;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.RunTimerConnectState;
import com.feipulai.device.serial.beans.RunTimerResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.NewRadioPairActivity;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class RunTimerSettingActivity extends BaseTitleActivity implements AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener, TextWatcher, RunTimerImpl.RunTimerListener {

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
    private RunTimerSetting runTimerSetting;
    private int intercept_point;
    private SweetAlertDialog alertDialog;
    private SerialDeviceManager deviceManager;

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
        deviceManager = SerialDeviceManager.getInstance();
        etSensitivityNum.setText(String.format("%d", runTimerSetting.getSensitivityNum()));
        if (runTimerSetting.getConnectType() == 1){
            selfCheck.setText("设备配对");
        }

        if (runTimerSetting.getConnectType() == 1){
            rgModel.setVisibility(View.GONE);
        }
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("项目设置");
    }

    /**
     * 单选框的设置
     */
    private void initRadioGroup() {
        radioGroupDegree.setOnCheckedChangeListener(this);

        rgModel.setOnCheckedChangeListener(this);
        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {//个人模式模式隐藏
            rgModel.setEnabled(false);
            rgModel.setVisibility(View.GONE);
        }
        //成绩精度
        boolean isSecond;
        int di = TestConfigs.sCurrentItem.getDigital();
        if (di != 0) {
            isSecond = di == 1;
            rbHundredSecond.setEnabled(false);
            rbTenSecond.setEnabled(false);
        } else {
            isSecond = runTimerSetting.isSecond();
        }
        radioGroupDegree.check(isSecond ? R.id.rb_ten_second : R.id.rb_hundred_second);


        //拦截方式
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
        //测试模式
        boolean testModel = runTimerSetting.isTestModel();
        rgModel.check(testModel ? R.id.rb_continue : R.id.rb_recycle);
    }

    /**
     * 下拉列表设置
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
            // 数据库中已经指定了测试次数,就不能再设置了
            spTestTimes.setEnabled(false);
            spTestTimes.setSelection(maxTestNo - 1);
        } else {
            spTestTimes.setSelection(runTimerSetting.getTestTimes() - 1);
        }

        int carryMode = TestConfigs.sCurrentItem.getCarryMode();
        String[] spinnerItems = {"四舍五入", "不进位", "非零进位"};
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

        String[] interceptItems = {"红外拦截触发", "发令传感器触发"};
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
                runTimerSetting.setSecond(false);
                break;
            case R.id.rb_ten_second:
                runTimerSetting.setSecond(true);
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
     * 判断去盒子版统一计时独立计时显示
     */
    private void setTimerSelect() {
        if (runTimerSetting.getInterceptPoint() == 3 && runTimerSetting.getConnectType() == 1){//有起终点 并且是无盒子版
            rg_timerSelect.setVisibility(View.VISIBLE);
            rg_timerSelect.setOnCheckedChangeListener(this);
            rg_timerSelect.check(runTimerSetting.isTimer_select() ? R.id.rb_independent:R.id.rb_unified);
        }else {
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        String senNum = etSensitivityNum.getText().toString();
        runTimerSetting.setSensitivityNum(TextUtils.isEmpty(senNum)?5:Integer.parseInt(senNum));
        getInterceptPoint();
        SharedPrefsUtil.save(this, runTimerSetting);
    }

    @OnClick({R.id.btn_self_check})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_self_check:
                if (runTimerSetting.getConnectType() == 1){
                    startActivity(new Intent(this, NewRadioPairActivity.class));
                }else {
                    if (runTimerSetting.getStartPoint() + runTimerSetting.getEndPoint() == 0) {
                        toastSpeak("必须设置拦截点");
                        return;
                    }
                    promote = 0;
//                mProgressDialog = ProgressDialog.show(this, "", "终端自检中...", true);
                    alertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                    alertDialog.setTitleText("终端自检中...");
                    alertDialog.setCancelable(false);
                    alertDialog.show();

                    int hostId = SettingHelper.getSystemSetting().getHostId();
//                deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd((byte) 0xc1, (byte) 0x02, (byte) hostId)));//主机号
                    int runNum = Integer.parseInt(runTimerSetting.getRunNum());
                    deviceManager.setRS232ResiltListener(new RunTimerImpl(this));
//                deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd((byte) 0xc1, (byte) 0x01, (byte) runNum)));//跑道数
                    int interceptPoint = runTimerSetting.getInterceptPoint();
//                deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd((byte) 0xc1, (byte) 0x04, (byte) interceptPoint)));//拦截点
                    int way = runTimerSetting.getInterceptWay();
//                deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd((byte) 0xc1, (byte) 0x05, (byte) (way + 1))));//触发方式
                    int sensor = runTimerSetting.getSensor();
//                deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd((byte) 0xc1, (byte) 0x08, (byte) (sensor))));//传感器信道
                    RunTimerManager.cmdSetting(runNum, hostId, interceptPoint, way, sensor, 10);
                    //3秒自检
                    mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 5000);
                    break;
                }
        }
    }

//    private byte[] cmd(byte cmd, byte mark, byte value) {
//        byte[] setting = {(byte) 0xBB, 0x0C, (byte) 0xA0, 0x00, (byte) 0xA1, 0x00, cmd, mark, value, 0x00, 0x00, 0x0D};
//        int sum = 0;
//        for (int i = 0; i < 9; i++) {
//            sum += setting[i];
//        }
//        setting[10] = (byte) sum;
//        return setting;
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onGetTime(RunTimerResult result) {

    }

    @Override
    public void onConnected(RunTimerConnectState connectState) {
        int intercept = runTimerSetting.getInterceptPoint();
        switch (intercept) {
            case 1:
                if (connectState.getStartIntercept() == 1) {
                    mHandler.sendEmptyMessage(MSG_CONNECT);
                }
                break;
            case 2:
                if (connectState.getEndIntercept() == 1) {
                    mHandler.sendEmptyMessage(MSG_CONNECT);
                }
                break;
            case 3:
                if (connectState.getEndIntercept() == 1 || connectState.getStartIntercept() == 1) {
                    mHandler.sendEmptyMessage(MSG_CONNECT);
                }
                break;
        }


    }

    @Override
    public void onTestState(int state) {

    }

    private final int MSG_DISCONNECT = 0x1001;
    private final int MSG_CONNECT = 0x1002;
    private int promote = 0;
    //3秒内检设备是否可用
    private volatile boolean isDisconnect = true;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //加载窗口是否显示
            boolean isDialogShow = true;
            switch (msg.what) {
                case MSG_DISCONNECT://连接失败
                    if (isDisconnect) {
                        toastSpeak("设备未连接");
                        //设置当前设置为不可用断开状态
                        if (alertDialog.isShowing()) {
                            alertDialog.dismiss();
                        }
                    }
                    break;

                case MSG_CONNECT://连接成功
                    isDisconnect = false;
                    isDialogShow = false;
                    if (promote == 0) {
                        toastSpeak("设备连接成功");
                        promote++;
                    }
                    break;
            }
            if (!isDialogShow && alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            return false;
        }
    });
}
