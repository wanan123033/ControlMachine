package com.feipulai.host.activity.standjump;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import com.feipulai.device.serial.beans.JumpSelfCheckResult;
import com.feipulai.device.serial.beans.StandJumpResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.standjump.more.StandJumpPairActivity;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.feipulai.device.serial.beans.JumpSelfCheckResult.NORMAL;


/**
 * 立定跳远项目设置
 * Created by zzs on 2018/11/21
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandJumpSettingActivity extends BaseTitleActivity implements RadioGroup.OnCheckedChangeListener {

    @BindView(R.id.tv_device_check)
    TextView tvDeviceCheck;
    @BindView(R.id.tv_check_data)
    TextView tvCheckData;
    @BindView(R.id.sp_device_count)
    Spinner spDeviceCount;

    @BindView(R.id.sp_stamdjump_points)
    Spinner spStamdjumpPoints;
    @BindView(R.id.txt_device_scope)
    TextView txtDeviceScope;
    @BindView(R.id.edit_device_scope)
    EditText editDeviceScope;
    @BindView(R.id.txt_scope_confirm)
    TextView txtScopeConfirm;
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

    private Integer[] testRound = new Integer[]{1, 2, 3};

    private StandJumpSetting standSetting;
    private static final int MSG_DISCONNECT = 0X101;
    private static final int JUMP_SET_POINTS_FAILURE = 0X102;
    private SweetAlertDialog alertDialog;
    private SerialHandler mHandler = new SerialHandler(this);
    //3秒内检设备是否可用
    private volatile boolean isDisconnect = true;
    //3秒内检设备设置范围是否成功
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
        //获取项目设置
        standSetting = SharedPrefsUtil.loadFormSource(this, StandJumpSetting.class);
        if (standSetting == null)
            standSetting = new StandJumpSetting();

        ArrayAdapter spDeviceCountAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                standSetting.getTestType() == 0 || SettingHelper.getSystemSetting().isFreedomTest() ?
                        new String[]{"1"} : new String[]{"1", "2", "3", "4"});
        spDeviceCountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDeviceCount.setAdapter(spDeviceCountAdapter);

        rgDevice.setOnCheckedChangeListener(this);
        initPoints();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (standSetting.getTestType() == 0) {
            SerialDeviceManager.getInstance().setRS232ResiltListener(listener);
        } else {
            RadioManager.getInstance().setOnRadioArrived(arrivedListener);
            rgDevice.setVisibility(View.VISIBLE);
            spDeviceCount.setSelection(standSetting.getTestDeviceCount() - 1);
        }
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("项目设置");
    }

    /**
     * 初始化测量杆和点数
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
        txtDeviceScope.setText("范围：51cm-" + (testPoints * 100 + 50 - 8) + "cm");
        scope = standSetting.getPointsScope();


        initMorePoints(1);
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
        txtDeviceScope.setText("范围：51cm-" + (testPoints * 100 + 50 - 8) + "cm");
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {

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

    @OnItemSelected({R.id.sp_stamdjump_points, R.id.sp_device_count})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {

            case R.id.sp_stamdjump_points:
                txtDeviceScope.setText("范围：51cm-" + ((position + 1) * 100 + 50 - 8) + "cm");

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
                    SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SELF_CHECK_JUMP));
                } else {
                    StandJumpManager.checkDevice(SettingHelper.getSystemSetting().getHostId(), deviceId);
                }
                break;
            case R.id.txt_scope_confirm:

                if (TextUtils.isEmpty(editDeviceScope.getText().toString())) {
                    ToastUtils.showShort("请输入设置范围");
                    return;
                }
                scope = Integer.valueOf(editDeviceScope.getText().toString());
                if (scope < 51 || scope > (testPoints * 100 + 50 - 8)) {
                    ToastUtils.showShort("请输入正确的设置范围");
                    return;
                }
//                mProgressDialog = ProgressDialog.show(this, "", "设置范围中...", true);
                alertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                alertDialog.setTitleText("设置范围中...");
                alertDialog.setCancelable(false);
                alertDialog.show();
                isSetPoints = false;
                if (standSetting.getTestType() == 0) {
                    standSetting.setTestPoints(testPoints);
                    SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.SET_CMD_SARGENT_JUMP_SETTING_POINTS(scope - 42)));
                } else {
                    StandJumpManager.setPoints(SettingHelper.getSystemSetting().getHostId(), deviceId, scope - 42);
                }

                //3秒自检
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

        SharedPrefsUtil.save(this, standSetting);

        SerialDeviceManager.getInstance().close();
        RadioManager.getInstance().setOnRadioArrived(null);
    }


    SerialDeviceManager.RS232ResiltListener listener = new SerialDeviceManager.RS232ResiltListener() {
        @Override
        public void onRS232Result(Message msg) {
            mHandler.sendMessage(msg);
        }
    };

    RadioManager.OnRadioArrivedListener arrivedListener = new RadioManager.OnRadioArrivedListener() {
        @Override
        public void onRadioArrived(Message msg) {
            if (msg.what == SerialConfigs.STAND_JUMP_SET_POINTS) {
                isSetPoints = true;
                standSetting.getPointsScopeArray()[deviceId - 1] = scope;
                standSetting.getTestPointsArray()[deviceId - 1] = testPoints;
                toastSpeak("设置成功");
            } else if (msg.what == SerialConfigs.STAND_JUMP_CHECK) {
                isDisconnect = false;
                final StandJumpResult result = (StandJumpResult) msg.obj;
                if (result.getmTerminalCondition() == NORMAL) {
                    toastSpeak(deviceId + "号设备连接成功");
                } else {
                    toastSpeak(deviceId + "号设备发现故障点");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String ledPostion = "";
                            for (int brokenLED : result.getBrokenLEDs()) {
                                if (brokenLED != 0) {
                                    ledPostion += (" " + (brokenLED + 50));
                                }
                            }
                            tvCheckData.setText("发现故障点:" + ledPostion);
                        }
                    });

                }

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
//                mProgressDialog = ProgressDialog.show(this, "", "终端自检中...", true);
        alertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        alertDialog.setTitleText("终端自检中...");
        alertDialog.setCancelable(false);
        alertDialog.show();
        //3秒自检
        mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 3000);
        tvCheckData.setText("");
    }


    /**
     * 回调
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
            //加载窗口是否显示
            boolean isDialogShow = true;
            if (activity != null) {
                switch (msg.what) {
                    case MSG_DISCONNECT://连接失败
                        if (activity.isDisconnect) {
                            activity.toastSpeak("设备未连接");
                            activity.tvCheckData.setText("设备未连接");
                            //设置当前设置为不可用断开状态
                            if (activity.alertDialog.isShowing()) {
                                activity.alertDialog.dismiss();
                            }
                        }
                        break;
                    case SerialConfigs.JUMP_SELF_CHECK_RESPONSE://立地跳远自检失败回调
                        JumpSelfCheckResult result = (JumpSelfCheckResult) msg.obj;
                        Log.i("james", "JUMP_SELF_CHECK_RESPONSE");
                        if (result.getTerminalCondition() == NORMAL) {
                            activity.isDisconnect = false;
                            isDialogShow = false;
                            activity.toastSpeak("设备连接成功");
                        } else {
                            activity.isDisconnect = false;
                            isDialogShow = false;
                            String ledPostion = "";
                            for (int brokenLED : result.getBrokenLEDs()) {
                                if (brokenLED != 0) {
                                    ledPostion += (" " + (brokenLED + 50));
                                }
                            }
                            activity.tvCheckData.setText("发现故障点:" + ledPostion);
                        }
                        break;
                    case SerialConfigs.JUMP_SELF_CHECK_RESPONSE_Simple://立地跳远自检成功回调
                        Log.i("james", "JUMP_SELF_CHECK_RESPONSE_Simple");
                        activity.isDisconnect = false;
                        isDialogShow = false;
                        activity.toastSpeak("设备连接成功");
                        break;
                    case SerialConfigs.JUMP_SET_POINTS:
                        isDialogShow = false;
                        activity.isSetPoints = true;
                        ToastUtils.showShort("设置成功");
                        activity.standSetting.setPointsScope(activity.scope);
                        break;
                    case JUMP_SET_POINTS_FAILURE:
                        isDialogShow = false;
                        if (!activity.isSetPoints) {
                            ToastUtils.showShort("设置失败");
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
