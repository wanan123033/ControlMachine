package com.feipulai.exam.activity.ranger;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.spputils.beans.RangerResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;

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
    private RangerSetting setting;

    private String[] testItems = {"跳高","撑竿跳高","跳远","立定跳远","三级跳远","标枪","铅球","铁饼","链球"};
    private String[] testNos = {"1","2","3"};
    private String[] results = {"精确到小数点后两位","精确到小数点后三位"};

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_ranger_setting;
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, RangerSetting.class);
        ArrayAdapter spTestRoundAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testItems);
        sp_item.setAdapter(spTestRoundAdapter);

        ArrayAdapter spTestNoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testNos);
        sp_test_no.setAdapter(spTestNoAdapter);

        ArrayAdapter spResultAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, results);
        sp_result.setAdapter(spResultAdapter);

        cbFullSkip.setOnCheckedChangeListener(this);
        etTestTime.addTextChangedListener(this);
    }

    @OnClick({R.id.tv_throw,R.id.tv_staJump,R.id.tv_bluetooth})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_throw:
                ThrowSettingDialog dialog = new ThrowSettingDialog(this);
                dialog.show();
                break;
            case R.id.tv_staJump:
                JumpSettingDialog dialog1 = new JumpSettingDialog(this);
                dialog1.show();
                break;
            case R.id.tv_bluetooth:
                BluetoothSettingDialog dialog2 = new BluetoothSettingDialog(this);
                dialog2.show();
//                startActivity(new Intent(this,BluetoothSettingActivity.class));
                break;
        }
    }

    @OnItemSelected({R.id.sp_item,R.id.sp_test_no,R.id.sp_result})
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
        int autoTestTime =Integer.parseInt(etTestTime.getText().toString());
        setting.setAutoTestTime(autoTestTime);
    }
}