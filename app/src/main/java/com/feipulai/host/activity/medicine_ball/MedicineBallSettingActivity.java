package com.feipulai.host.activity.medicine_ball;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MedicineBallSettingActivity extends BaseTitleActivity {

    @BindView(R.id.tv_device_check)
    TextView tvDeviceCheck;
    @BindView(R.id.tv_check_data)
    TextView tvCheckData;
    @BindView(R.id.tv_scoring)
    TextView tvScoring;
    @BindView(R.id.edit_device_scope)
    EditText etBeginPoint;

    @BindView(R.id.sp_device_count)
    Spinner spDeviceCount;
    private MedicineBallSetting medicineBallSetting;
    private SweetAlertDialog alertDialog;
    //5秒内检设备是否可用
    private volatile boolean isDisconnect = true;
    private static final int MSG_DISCONNECT = 0X101;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_medicine_ball_setting;
    }

    @Override
    protected void initData() {
        medicineBallSetting = SharedPrefsUtil.loadFormSource(this, MedicineBallSetting.class);
        if (medicineBallSetting == null)
            medicineBallSetting = new MedicineBallSetting();

        ArrayAdapter spDeviceCountAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, medicineBallSetting.getTestType() ==0 ?
                new String[]{"1"}:new String[]{"1","2","3","4"});
        spDeviceCountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDeviceCount.setAdapter(spDeviceCountAdapter);
        spDeviceCount.setSelection(medicineBallSetting.getTestType() == 0? 0:medicineBallSetting.getTestDeviceCount()-1);

        SerialDeviceManager.getInstance().setRS232ResiltListener(listener);

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


    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle(R.string.item_setting_title);
    }

    @OnClick({R.id.tv_device_check})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_device_check:
                isDisconnect = true;
                alertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                alertDialog.setTitleText(getString(R.string.device_check_hint));
                alertDialog.setCancelable(false);
                alertDialog.show();

                SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
                //5秒自检
                mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 5000);
                tvCheckData.setText("");
                break;

        }
    }

    @OnItemSelected(R.id.sp_device_count)
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()){
            case R.id.sp_device_count:
                medicineBallSetting.setTestDeviceCount(position+1);
                break;
        }
    }

    SerialDeviceManager.RS232ResiltListener listener = new SerialDeviceManager.RS232ResiltListener() {
        @Override
        public void onRS232Result(Message msg) {
            mHandler.sendMessage(msg);
        }
    };

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DISCONNECT:
                    if (isDisconnect) {
                        toastSpeak(getString(R.string.device_noconnect));
                        tvCheckData.setText(R.string.device_noconnect);

                    }
                    break;
                case SerialConfigs.MEDICINE_BALL_EMPTY_RESPONSE://空闲命令
                    isDisconnect = false;
                    toastSpeak(getString(R.string.device_connect_succeed));

                    break;
            }
            if (alertDialog!= null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }

            return false;
        }
    });

    @Override
    protected void onPause() {
        super.onPause();
        SharedPrefsUtil.save(this, medicineBallSetting);
        SerialDeviceManager.getInstance().close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(null);
    }


}
