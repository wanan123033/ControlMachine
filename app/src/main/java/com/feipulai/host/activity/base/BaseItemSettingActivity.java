package com.feipulai.host.activity.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.JumpSelfCheckResult;
import com.feipulai.device.serial.beans.MedicineBallSelfCheckResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.host.R;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.utils.SharedPrefsUtil;
import com.feipulai.host.view.NumPickerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

import static com.feipulai.device.serial.beans.JumpSelfCheckResult.NEED_CHANGE;


public class BaseItemSettingActivity extends BaseActivity {
    private static final String TAG = "BaseItemSettingActivity";
    @BindView(R.id.tv_device_self_check)
    TextView tvDeviceSelfCheck;
    @BindView(R.id.tv_standard)
    TextView tvStandard;
    @BindView(R.id.ll_device_time)
    NumPickerView llDeviceTime;
    @BindView(R.id.ll_test_time)
    NumPickerView llTestTime;
    @BindView(R.id.btn_terminal_matching)
    Button btnTerminalMatching;
    @BindView(R.id.ll_begin_point)
    LinearLayout beginPoint;
    @BindView(R.id.et_begin_point)
    EditText etBeginPoint;
    @BindView(R.id.sp_stamdjump_points)
    Spinner spStamdjumpPoints;
    @BindView(R.id.txt_stamdjump)
    TextView txtStamdjump;

    //3秒内检设备是否可用
    private volatile boolean isDisconnect = true;
    private MyHandler mHandler = new MyHandler(this);
    private static final int MSG_DISCONNECT = 0X101;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_item_setting);
        ButterKnife.bind(this);
        init();

    }

    private void init() {

        llDeviceTime.setDisable();
        btnTerminalMatching.setFocusable(false);
        btnTerminalMatching.setClickable(false);
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_HWSXQ){
            beginPoint.setVisibility(View.VISIBLE);
        }else {
            beginPoint.setVisibility(View.GONE);
        }

        String beginPoint = SharedPrefsUtil.getValue(this,"SXQ","beginPoint","0");
        etBeginPoint.setText(beginPoint);

        etBeginPoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SharedPrefsUtil.putValue(BaseItemSettingActivity.this,"SXQ","beginPoint",
                        etBeginPoint.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LDTY) {
            spStamdjumpPoints.setVisibility(View.VISIBLE);
            txtStamdjump.setVisibility(View.VISIBLE);
            List<Integer> pointsList = new ArrayList<>();
            for (int i = 1; i < 4; i++) {
                pointsList.add(i);
            }
            int points = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.STAND_JUMP_TEST_POINTS, 3);
            spStamdjumpPoints.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pointsList));
            spStamdjumpPoints.setSelection(points - 1);
        }
    }

    @OnItemSelected({R.id.sp_stamdjump_points})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_stamdjump_points:
                SharedPrefsUtil.putValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.STAND_JUMP_TEST_POINTS, position + 1);
                SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.SET_CMD_SARGENT_JUMP_SETTING_POINTS((position + 1) * 100)));
                break;
        }
    }

    @OnClick({R.id.tv_device_self_check, R.id.tv_standard, R.id.btn_terminal_matching})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_device_self_check:
                //终端自检
                sendSelfCheckCommand();
                break;
            case R.id.tv_standard:
                //查看评分标准
                break;
            case R.id.btn_terminal_matching:
                // 终端匹配
                break;
        }
    }

    /**
     * 发送自检命令
     */
    private void sendSelfCheckCommand() {
        isDisconnect = true;
        mProgressDialog = ProgressDialog.show(this, "", "终端自检中...", true);
        if (SerialDeviceManager.getInstance() != null) {
            SerialDeviceManager.getInstance().setRS232ResiltListener(listener);
            // 自检,校验连接是否正常
            switch (TestConfigs.sCurrentItem.getMachineCode()) {
                case ItemDefault.CODE_LDTY://立地跳远
                    SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SELF_CHECK_JUMP));
                    break;
                case ItemDefault.CODE_ZWTQQ://坐位体前屈
                    SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_EMPTY));
                    break;
                case ItemDefault.CODE_FHL://肺活量 没有自检命令，发送查询指令，有返回则连接成功，3秒没返回数据连接失败
                    SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_VC_SELECT));
                    break;
                case ItemDefault.CODE_HWSXQ://红外实心球
                    SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
                    break;
            }

            //3秒自检
            mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 3000);
        }
    }

    SerialDeviceManager.RS232ResiltListener listener = new SerialDeviceManager.RS232ResiltListener() {
        @Override
        public void onRS232Result(Message msg) {
            mHandler.sendMessage(msg);
        }
    };

    /**
     * 回调
     */
    private static class MyHandler extends Handler {

        private WeakReference<BaseItemSettingActivity> mActivityWeakReference;

        public MyHandler(BaseItemSettingActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BaseItemSettingActivity activity = mActivityWeakReference.get();
            //加载窗口是否显示
            boolean isDialogShow = true;
            if (activity != null) {
                switch (msg.what) {
                    case MSG_DISCONNECT://连接失败
                        if (activity.isDisconnect) {
                            activity.toastSpeak("设备未连接");
                            //设置当前设置为不可用断开状态
                            if (activity.mProgressDialog.isShowing()) {
                                activity.mProgressDialog.dismiss();
                            }
                        }
                        break;
                    case SerialConfigs.SIT_AND_REACH_EMPTY_RESPONSE://坐位体前屈自检回调
                        activity.isDisconnect = false;
                        activity.toastSpeak("设备连接成功");
                        break;
                    case SerialConfigs.JUMP_SELF_CHECK_RESPONSE://立地跳远自检失败回调
                        JumpSelfCheckResult result = (JumpSelfCheckResult) msg.obj;
                        if (result.getTerminalCondition() == NEED_CHANGE) {
                            activity.isDisconnect = true;
                            isDialogShow = true;
                        } else {
                            activity.isDisconnect = false;
                            isDialogShow = false;
                            activity.toastSpeak("设备连接成功");
                        }
                        break;
                    case SerialConfigs.JUMP_SELF_CHECK_RESPONSE_Simple://立地跳远自检成功回调
                        activity.isDisconnect = false;
                        isDialogShow = false;
                        activity.toastSpeak("设备连接成功");
                        break;
                    case SerialConfigs.VITAL_CAPACITY_RESULT://肺活量
                        activity.isDisconnect = false;
                        isDialogShow = false;
                        activity.toastSpeak("设备连接成功");
                        break;
                    case SerialConfigs.MEDICINE_BALL_SELF_CHECK_RESPONSE://红外实心球
                        MedicineBallSelfCheckResult selfCheckResult = (MedicineBallSelfCheckResult) msg.obj;
                        if (selfCheckResult.isInCorrect()) {
                            activity.isDisconnect = true;
                            isDialogShow = true;
                        } else {
                            activity.isDisconnect = false;
                            isDialogShow = false;
                            activity.toastSpeak("设备连接成功");
                        }

                        break;
                }
                if (!isDialogShow && activity.mProgressDialog.isShowing()) {
                    activity.mProgressDialog.dismiss();
                }

            }

        }
    }
}
