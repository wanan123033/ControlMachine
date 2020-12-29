package com.feipulai.host.activity.ranger;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.base.TextChangeListener;
import com.feipulai.host.activity.ranger.bluetooth.BluetoothSettingActivity;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class RangerSettingActivity extends BaseTitleActivity {
    @BindView(R.id.et_test_time)
    EditText et_test_time;
    @BindView(R.id.sp_item)
    Spinner sp_item;
    private RangerSetting setting;

    private String[] testItems = {"跳高","撑竿跳高","跳远","立定跳远","三级跳远","标枪","铅球","铁饼","链球"};
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_ranger_setting;
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("项目设置");
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, RangerSetting.class);

        ArrayAdapter spTestRoundAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testItems);
        sp_item.setAdapter(spTestRoundAdapter);
        int itemType = setting.getItemType();
        sp_item.setSelection(itemType);

        et_test_time.addTextChangedListener(new TextChangeListener(R.id.et_test_time) {
            @Override
            protected void afterTextChanged(Editable s, int id) {
                if (et_test_time.getText().length() > 0) {
                    try {
                        int autoTestTime = Integer.parseInt(et_test_time.getText().toString());
                        setting.setAutoTestTime(autoTestTime);
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                }
            }
        });
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
//                BluetoothSettingDialog dialog2 = new BluetoothSettingDialog(this);
//                dialog2.show();
                startActivity(new Intent(this, BluetoothSettingActivity.class));
                break;
        }
    }
    @OnItemSelected({R.id.sp_item})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()){
            case R.id.sp_item:
                setting.setItemType(position);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPrefsUtil.save(this,setting);
    }
}
