package com.feipulai.exam.activity.ranger;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.ranger.bluetooth.BluetoothSettingActivity;
import com.feipulai.exam.activity.ranger.usb.RangerUsbDevicesActivity;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class RangerSettingActivity extends BaseTitleActivity implements CompoundButton.OnCheckedChangeListener, TextWatcher {

    @BindView(R.id.sp_item)
    Spinner sp_item;
    @BindView(R.id.sp_test_no)
    Spinner sp_test_no;
    @BindView(R.id.sp_result)
    Spinner sp_result;
    @BindView(R.id.cb_full_skip)
    CheckBox cbFullSkip;
    @BindView(R.id.et_test_time)
    EditText etTestTime;
    @BindView(R.id.sp_test_connect)
    Spinner sp_test_connect;
    private RangerSetting setting;

    private String[] testItems = {"跳高","撑竿跳高","跳远","立定跳远","三级跳远","标枪","铅球","铁饼","链球"};
    private String[] testNos = {"1","2","3"};
    private String[] results = {"精确到小数点后两位","精确到小数点后三位"};
    private String[] connect = {"蓝牙","USB"};

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("设置");
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_ranger_setting;
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, RangerSetting.class);
        ArrayAdapter spTestRoundAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testItems);
        spTestRoundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_item.setAdapter(spTestRoundAdapter);

        ArrayAdapter spTestNoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testNos);
        spTestNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_test_no.setAdapter(spTestNoAdapter);

        ArrayAdapter spResultAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, results);
        spResultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_result.setAdapter(spResultAdapter);

        ArrayAdapter connectAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, connect);
        connectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_test_connect.setAdapter(connectAdapter);

        cbFullSkip.setOnCheckedChangeListener(this);
        etTestTime.addTextChangedListener(this);

        int testNo = setting.getTestNo();
        boolean penglize = setting.isPenglize();
        int autoTestTime = setting.getAutoTestTime();
        int accuracy = setting.getAccuracy();
        int itemType = setting.getItemType();
        int connectType = setting.getConnectType();

        sp_test_no.setSelection(testNo - 1);
        cbFullSkip.setChecked(penglize);
        etTestTime.setText(String.valueOf(autoTestTime));
        sp_result.setSelection(accuracy - 2);
        sp_item.setSelection(itemType);
        sp_test_connect.setSelection(connectType);
        etTestTime.setSelection(String.valueOf(autoTestTime).length());
    }

    @OnClick({R.id.tv_throw,R.id.tv_staJump,R.id.tv_bluetooth,R.id.tv_connect})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_throw:
                if (setting.getItemType() > 4) {
                    ThrowSettingDialog dialog = new ThrowSettingDialog(this, setting);
                    dialog.setItemType(setting.getItemType());
                    dialog.show();
                }else {
                    ToastUtils.showLong("请选择投掷类项目");
                }
                break;
            case R.id.tv_staJump:
                if (setting.getItemType() >= 2 && setting.getItemType() <= 4) {
                    JumpSettingDialog dialog1 = new JumpSettingDialog(this, setting);
                    dialog1.show();
                }else{
                    ToastUtils.showLong("请选择跳远类项目");
                }
                break;
            case R.id.tv_bluetooth:
//                BluetoothSettingDialog dialog2 = new BluetoothSettingDialog(this);
//                dialog2.show();
                startActivity(new Intent(this, BluetoothSettingActivity.class));
                break;
            case R.id.tv_connect:
                startActivity(new Intent(RangerSettingActivity.this, RangerUsbDevicesActivity.class));
                break;
        }
    }

    @OnItemSelected({R.id.sp_item,R.id.sp_test_no,R.id.sp_result,R.id.sp_test_connect})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()){
            case R.id.sp_item:
                setting.setItemType(position);
                break;
            case R.id.sp_test_no:
                setting.setTestNo(position + 1);
                break;
            case R.id.sp_result:
                setting.setAccuracy(position + 2);
                break;
            case R.id.sp_test_connect:
                setting.setConnectType(position);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setting.setPenglize(isChecked);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (etTestTime.getText().length() > 0) {
            try {
                int autoTestTime = Integer.parseInt(etTestTime.getText().toString());
                setting.setAutoTestTime(autoTestTime);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPrefsUtil.save(getApplicationContext(),setting);
    }
}
