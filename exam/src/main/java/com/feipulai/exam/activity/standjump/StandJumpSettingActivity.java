package com.feipulai.exam.activity.standjump;

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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.manager.StandJumpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.JumpNewSelfCheckResult;
import com.feipulai.device.serial.beans.JumpSelfCheckResult;
import com.feipulai.device.serial.beans.StandJumpResult;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.standjump.more.StandJumpPairActivity;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.orhanobut.logger.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.feipulai.device.serial.beans.JumpSelfCheckResult.NORMAL;


/**
 * ????????????????????????
 * Created by zzs on 2018/11/21
 * ??????????????????????????????????????????   ????????????:??????
 */
public class StandJumpSettingActivity extends BaseTitleActivity implements CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {

    @BindView(R.id.tv_device_check)
    TextView tvDeviceCheck;
    @BindView(R.id.tv_check_data)
    TextView tvCheckData;
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
    @BindView(R.id.sp_stamdjump_points)
    Spinner spStamdjumpPoints;
    @BindView(R.id.txt_device_scope)
    TextView txtDeviceScope;
    @BindView(R.id.edit_device_scope)
    EditText editDeviceScope;
    @BindView(R.id.txt_scope_confirm)
    TextView txtScopeConfirm;
    @BindView(R.id.tv_pair)
    TextView tvPair;
    @BindView(R.id.ll_full)
    LinearLayout llFull;
    @BindView(R.id.rg_device)
    RadioGroup rgDevice;
    @BindView(R.id.rb_1)
    RadioButton rb1;
    @BindView(R.id.rb_2)
    RadioButton rb2;
    @BindView(R.id.rb_3)
    RadioButton rb3;
    @BindView(R.id.rb_4)
    RadioButton rb4;

    private Integer[] testRound;

    private StandJumpSetting standSetting;
    private static final int MSG_DISCONNECT = 0X101;
    private static final int JUMP_SET_POINTS_FAILURE = 0X102;
    private SweetAlertDialog alertDialog;
    private SerialHandler mHandler = new SerialHandler(this);
    //3???????????????????????????
    private volatile boolean isDisconnect = true;
    //3???????????????????????????????????????
    private volatile boolean isSetPoints = true;
    private int scope;
    private int testPoints;
    private int deviceId = 1;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_standjump_setting;
    }

    @Override
    protected void initData() {
        cbFullReturn.setOnCheckedChangeListener(this);
        //??????????????????
        standSetting = SharedPrefsUtil.loadFormSource(this, StandJumpSetting.class);
        if (standSetting == null)
            standSetting = new StandJumpSetting();

        //??????????????????
        int maxTestNo = (TestConfigs.sCurrentItem.getTestNum() == 0 && TestConfigs.getMaxTestCount(this) <= TestConfigs.MAX_TEST_NO)
                ? TestConfigs.MAX_TEST_NO : TestConfigs.getMaxTestCount(this);
        testRound = new Integer[maxTestNo];
        for (int i = 0; i < maxTestNo; i++) {
            testRound[i] = i + 1;
        }
        ArrayAdapter spTestRoundAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testRound);
        spTestRoundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTestRound.setAdapter(spTestRoundAdapter);

        ArrayAdapter spDeviceCountAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                standSetting.getTestType() == 0 ? new String[]{"1"} : new String[]{"1", "2", "3", "4"});
        spDeviceCountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDeviceCount.setAdapter(spDeviceCountAdapter);

        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
            rgTestPattern.setVisibility(View.GONE);
        } else {
            rgTestPattern.setVisibility(View.VISIBLE);
        }
        rgTestPattern.setOnCheckedChangeListener(this);
        rgDevice.setOnCheckedChangeListener(this);
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            // ???????????????????????????????????????,?????????????????????
            spTestRound.setEnabled(false);
            spTestRound.setSelection(TestConfigs.sCurrentItem.getTestNum() - 1);
        } else {
            spTestRound.setSelection(standSetting.getTestCount() - 1);
        }


        if (standSetting.getTestPattern() == 0) {//????????????
            rgTestPattern.check(R.id.rb_continuous_test);
        } else {
            rgTestPattern.check(R.id.rb_circulation_test);
        }

        if (standSetting.getManFull() > 0) {
            editManFull.setText(standSetting.getManFull() + "");
        }
        if (standSetting.getWomenFull() > 0) {
            editWomenFull.setText(standSetting.getWomenFull() + "");
        }
        cbFullReturn.setChecked(standSetting.isFullReturn());
        initPoints();


        setEditTextWatcherListener();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (standSetting.getTestType() == 0) {
            SerialDeviceManager.getInstance().setRS232ResiltListener(listener);
        } else {
            RadioManager.getInstance().setOnRadioArrived(arrivedListener);
            rgDevice.setVisibility(View.VISIBLE);
            tvPair.setVisibility(View.VISIBLE);
            spDeviceCount.setSelection(standSetting.getTestDeviceCount() - 1);
        }
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("????????????");
    }

    /**
     * ???????????????????????????
     */
    private void initPoints() {
        testPoints = standSetting.getTestPoints();
        Integer[] pointsByte = new Integer[]{1, 2, 3};
        ArrayAdapter spPointsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pointsByte);
        spPointsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStamdjumpPoints.setAdapter(spPointsAdapter);
        spStamdjumpPoints.setSelection(testPoints - 1);
        if (standSetting.getPointsScope() > 0) {
            editDeviceScope.setText(standSetting.getPointsScope() + "");
        } else {
            editDeviceScope.setText((testPoints * 100 + 50 - 8) + "");
        }
        txtDeviceScope.setText("?????????51cm-" + (testPoints * 100 + 50 - 8) + "cm");
        scope = standSetting.getPointsScope();

        if (standSetting.getTestType()==1){
            initMorePoints(1);
        }

    }


    private void initMorePoints(int deviceId) {
        this.deviceId = deviceId;
        testPoints = standSetting.getTestPointsArray()[deviceId - 1];
        scope = standSetting.getPointsScopeArray()[deviceId - 1];
        spStamdjumpPoints.setSelection(testPoints - 1);
        if (scope > 0) {
            editDeviceScope.setText(scope + "");
        } else {
            editDeviceScope.setText((testPoints * 100 + 50 - 8) + "");
        }
        txtDeviceScope.setText("?????????51cm-" + (testPoints * 100 + 50 - 8) + "cm");
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_circulation_test://????????????
                standSetting.setTestPattern(1);
                break;
            case R.id.rb_continuous_test://????????????
                standSetting.setTestPattern(0);
                break;
            case R.id.rb_1://
                initMorePoints(1);
                break;
            case R.id.rb_2://
                initMorePoints(2);
                break;
            case R.id.rb_3://
                initMorePoints(3);
                break;
            case R.id.rb_4://
                initMorePoints(4);
                break;

        }
    }

    @OnItemSelected({R.id.sp_test_round, R.id.sp_stamdjump_points, R.id.sp_device_count})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_test_round:
                if (TestConfigs.sCurrentItem.getTestNum() > 0 && TestConfigs.sCurrentItem.getTestNum() != position + 1) {
                    TestConfigs.sCurrentItem.setTestNum(0);
                    DBManager.getInstance().updateItem(TestConfigs.sCurrentItem);
                }
                standSetting.setTestCount(position + 1);
                EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_COUNT));
                break;
            case R.id.sp_stamdjump_points:

                if (standSetting.isFullReturn()) {
                    if (!isCheckSetting()) {
                        standSetting.setFullReturn(false);
                        cbFullReturn.setChecked(false);
                    }
                }
                txtDeviceScope.setText("?????????51cm-" + ((position + 1) * 100 + 50 - 8) + "cm");

                if (position + 1 != testPoints) {
                    editDeviceScope.setText(((position + 1) * 100 + 50 - 8) + "");
                }


                testPoints = position + 1;
                break;
            case R.id.sp_device_count:
                standSetting.setTestDeviceCount(position + 1);
                switch (position) {
                    case 0:
                        rb1.setVisibility(View.VISIBLE);
                        rb2.setVisibility(View.GONE);
                        rb3.setVisibility(View.GONE);
                        rb4.setVisibility(View.GONE);
                        break;
                    case 1:
                        rb1.setVisibility(View.VISIBLE);
                        rb2.setVisibility(View.VISIBLE);
                        rb3.setVisibility(View.GONE);
                        rb4.setVisibility(View.GONE);
                        break;
                    case 2:
                        rb1.setVisibility(View.VISIBLE);
                        rb2.setVisibility(View.VISIBLE);
                        rb3.setVisibility(View.VISIBLE);
                        rb4.setVisibility(View.GONE);
                        break;
                    case 3:
                        rb1.setVisibility(View.VISIBLE);
                        rb2.setVisibility(View.VISIBLE);
                        rb3.setVisibility(View.VISIBLE);
                        rb4.setVisibility(View.VISIBLE);
                        break;
                }


                break;
        }
    }

    @OnClick({R.id.tv_device_check, R.id.txt_scope_confirm, R.id.tv_pair})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_device_check:
                checkDialog();
                if (standSetting.getTestType() == 0) {
                    LogUtils.normal(SerialConfigs.CMD_SELF_CHECK_JUMP.length + "---" + StringUtility.bytesToHexString(SerialConfigs.CMD_SELF_CHECK_JUMP) + "---??????????????????");

                    SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SELF_CHECK_JUMP));
                } else {
                    StandJumpManager.checkDevice(SettingHelper.getSystemSetting().getHostId(), deviceId);
                }
                break;
            case R.id.txt_scope_confirm:

                if (TextUtils.isEmpty(editDeviceScope.getText().toString())) {
                    ToastUtils.showShort("?????????????????????");
                    return;
                }
                scope = Integer.valueOf(editDeviceScope.getText().toString());
                if (scope < 51 || scope > (testPoints * 100 + 50 - 8)) {
                    ToastUtils.showShort("??????????????????????????????");
                    return;
                }
//                mProgressDialog = ProgressDialog.show(this, "", "???????????????...", true);
                alertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                alertDialog.setTitleText("???????????????...");
                alertDialog.setCancelable(false);
                alertDialog.show();
                isSetPoints = false;

                if (standSetting.getTestType() == 0) {
                    standSetting.setTestPoints(testPoints);
                    byte[] buk = SerialConfigs.SET_CMD_SARGENT_JUMP_SETTING_POINTS(scope - 42);
                    LogUtils.normal(buk.length + "---" + StringUtility.bytesToHexString(buk) + "---????????????????????????");
                    SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, buk));
                } else {
                    StandJumpManager.setPoints(SettingHelper.getSystemSetting().getHostId(), deviceId, scope - 42);
                }

                //3?????????
                mHandler.sendEmptyMessageDelayed(JUMP_SET_POINTS_FAILURE, 3000);
                break;
            case R.id.tv_pair:
                IntentUtil.gotoActivity(this, StandJumpPairActivity.class);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (standSetting.isFullReturn()) {
            isCheckSetting();
        }
        SharedPrefsUtil.save(this, standSetting);
        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN && standSetting.getTestType() == 0) {
            startActivity(new Intent(this, StandJumpTestActivity.class));
        }
        SerialDeviceManager.getInstance().close();
        RadioManager.getInstance().setOnRadioArrived(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
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
                    standSetting.setFullReturn(false);
                }
                if (s.length() > 0) {
                    int number = Integer.valueOf(editManFull.getText().toString());
                    if (number > 340) {
                        ToastUtils.showShort("?????????????????????50~340???");
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
                    standSetting.setFullReturn(false);
                }
                if (s.length() > 0) {
                    int number = Integer.valueOf(editWomenFull.getText().toString());
                    if (number > 340) {
                        ToastUtils.showShort("?????????????????????50~340???");
                        editWomenFull.setText(womenFull);
                        editWomenFull.setSelection(womenFull.length() - 1);
                    }
                }
            }
        });


    }

    private boolean isCheckSetting() {
        if (TextUtils.isEmpty(editManFull.getText().toString())) {
            ToastUtils.showShort("???????????????????????????????????????");
            ToastUtils.showShort("????????????????????????");
            return false;
        }
        if (!isCheckFull(Integer.valueOf(editManFull.getText().toString())))
            return false;
        standSetting.setManFull(Integer.valueOf(editManFull.getText().toString()));
        if (TextUtils.isEmpty(editWomenFull.getText().toString())) {
            ToastUtils.showShort("???????????????????????????????????????");
            ToastUtils.showShort("????????????????????????");
            return false;
        }
        if (!isCheckFull(Integer.valueOf(editWomenFull.getText().toString())))
            return false;
        standSetting.setWomenFull(Integer.valueOf(editWomenFull.getText().toString()));
        return true;
    }

    private boolean isCheckFull(int manFull) {
        switch (testPoints) {
            case 1:
                if (manFull < 50 && manFull > 140) {
                    ToastUtils.showShort("?????????????????????????????????(50~140)");
                    return false;
                }
                break;
            case 2:
                if (manFull < 50 && manFull > 240) {
                    ToastUtils.showShort("?????????????????????????????????(50~240)");
                    return false;
                }
                break;
            case 3:
                if (manFull < 50 && manFull > 340) {
                    ToastUtils.showShort("?????????????????????????????????(50~340)");
                    return false;
                }
                break;
        }
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_full_return:
                if (isChecked && isCheckSetting()) {
                    standSetting.setFullReturn(true);
                } else {
                    cbFullReturn.setChecked(false);
                    standSetting.setFullReturn(false);
                }
                break;
        }
    }


    SerialDeviceManager.RS232ResiltListener listener = new SerialDeviceManager.RS232ResiltListener() {
        @Override
        public void onRS232Result(Message msg) {
            mHandler.sendMessage(msg);
        }
    };

    RadioManager.OnRadioArrivedListener arrivedListener = new RadioManager.OnRadioArrivedListener() {
        @Override
        public void onRadioArrived(final Message msg) {
            if (msg.what == SerialConfigs.STAND_JUMP_SET_POINTS) {
                isSetPoints = true;
                standSetting.getPointsScopeArray()[deviceId - 1] = scope;
                standSetting.getTestPointsArray()[deviceId - 1] = testPoints;
                toastSpeak("????????????");
            } else if (msg.what == SerialConfigs.STAND_JUMP_CHECK) {
                isDisconnect = false;
                final StandJumpResult result = (StandJumpResult) msg.obj;
                if (result.getmTerminalCondition() == NORMAL) {
                    toastSpeak(deviceId + "?????????????????????");
                } else {
                    toastSpeak(deviceId + "????????????????????????");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String ledPostion = "";
                            for (int brokenLED : result.getBrokenLEDs()) {
                                if (brokenLED != 0) {
                                    ledPostion += (" " + (brokenLED + 50));
                                }
                            }
                            tvCheckData.setText("???????????????:" + ledPostion);
                        }
                    });

                }

            } else if (msg.what == SerialConfigs.JUMP_NEW_SELF_CHECK_RESPONSE) {
                final JumpNewSelfCheckResult checkResult = (JumpNewSelfCheckResult) msg.obj;
                isDisconnect = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String poleState = "";
                        for (int i = 0; i < checkResult.getJumpPoleState().length; i++) {
                            if (checkResult.getJumpPoleState()[i] == 0) {
                                poleState += ("  " + (i + 1));
                            }
                        }
                        if (!TextUtils.isEmpty(poleState)) {
                            if (!TextUtils.isEmpty(tvCheckData.getText().toString())) {
                                tvCheckData.append("\n");
                            }
                            tvCheckData.append("??????????????????" + poleState + "??????");
                            toastSpeak("?????????????????????");
                        }
                    }
                });

            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (alertDialog != null && alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                }
            });
        }
    };


    private void checkDialog() {
        isDisconnect = true;
//                mProgressDialog = ProgressDialog.show(this, "", "???????????????...", true);
        alertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        alertDialog.setTitleText("???????????????...");
        alertDialog.setCancelable(false);
        alertDialog.show();
        //3?????????
        mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 3000);
        tvCheckData.setText("");
    }


    /**
     * ??????
     */
    private static class SerialHandler extends Handler {

        private WeakReference<StandJumpSettingActivity> mActivityWeakReference;

        public SerialHandler(StandJumpSettingActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            StandJumpSettingActivity activity = mActivityWeakReference.get();
            //????????????????????????
            boolean isDialogShow = true;
            if (activity != null) {
                switch (msg.what) {
                    case MSG_DISCONNECT://????????????
                        if (activity.isDisconnect) {
                            activity.toastSpeak("???????????????");
                            activity.tvCheckData.setText("???????????????");
                            //??????????????????????????????????????????
                            if (activity.alertDialog.isShowing()) {
                                activity.alertDialog.dismiss();
                            }
                        }
                        break;
                    case SerialConfigs.JUMP_SELF_CHECK_RESPONSE://??????????????????????????????
                        JumpSelfCheckResult result = (JumpSelfCheckResult) msg.obj;
                        Log.i("james", "JUMP_SELF_CHECK_RESPONSE");
                        if (result.getTerminalCondition() == NORMAL) {
                            activity.isDisconnect = false;
                            isDialogShow = false;
                            activity.toastSpeak("??????????????????");
                        } else {
                            activity.isDisconnect = false;
                            isDialogShow = false;
                            String ledPostion = "";
                            for (int brokenLED : result.getBrokenLEDs()) {
                                if (brokenLED != 0) {
                                    ledPostion += (" " + (brokenLED + 50));
                                }
                            }
                            activity.tvCheckData.setText("???????????????:" + ledPostion);
                        }
                        break;
                    case SerialConfigs.JUMP_NEW_SELF_CHECK_RESPONSE://?????????????????????
                        JumpNewSelfCheckResult checkResult = (JumpNewSelfCheckResult) msg.obj;
                        String poleState = "";
                        for (int i = 0; i < checkResult.getJumpPoleState().length; i++) {
                            if (checkResult.getJumpPoleState()[i] == 0) {
                                poleState += ("  " + (i + 1));
                            }
                        }
                        if (!TextUtils.isEmpty(poleState)) {
                            if (!TextUtils.isEmpty(activity.tvCheckData.getText().toString())) {
                                activity.tvCheckData.append("\n");
                            }
                            activity.tvCheckData.append("??????????????????" + poleState + "??????");
                            activity.toastSpeak("?????????????????????");
                        }


                        break;
                    case SerialConfigs.JUMP_SELF_CHECK_RESPONSE_Simple://??????????????????????????????
                        Log.i("james", "JUMP_SELF_CHECK_RESPONSE_Simple");
                        activity.isDisconnect = false;
                        isDialogShow = false;
                        activity.toastSpeak("??????????????????");
                        break;
                    case SerialConfigs.JUMP_SET_POINTS:
                        isDialogShow = false;
                        activity.isSetPoints = true;
                        ToastUtils.showShort("????????????");
                        activity.standSetting.setPointsScope(activity.scope);
                        activity.standSetting.setTestPoints(activity.testPoints);
                        break;
                    case JUMP_SET_POINTS_FAILURE:
                        isDialogShow = false;
                        if (!activity.isSetPoints) {
                            ToastUtils.showShort("????????????");
//                            activity.standSetting.setTestPoints(3);
//                            activity.standSetting.setPointsScope(0);
//                            activity.initPoints();
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
