package com.feipulai.host.activity.ranger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.spputils.OnDataReceivedListener;
import com.feipulai.device.spputils.SppUtils;
import com.feipulai.device.spputils.beans.RangerResult;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.TextChangeListener;
import com.feipulai.host.activity.ranger.bluetooth.BluetoothManager;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class ThrowSettingDialog extends AlertDialog.Builder {
    @BindView(R.id.sp_item)
    Spinner sp_item;
    @BindView(R.id.et_stand)
    EditText et_stand;
    @BindView(R.id.et_range)
    EditText et_range;
    @BindView(R.id.et_d)
    EditText et_d;
    @BindView(R.id.et_f)
    EditText et_f;
    @BindView(R.id.et_m)
    EditText et_m;

    RangerSetting setting;

    String[] items = {"标枪","铅球","铁饼","链球","其他"};

    public ThrowSettingDialog(@NonNull Context context) {
        super(context);
        setting = SharedPrefsUtil.loadFormSource(context,RangerSetting.class);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_throws_setting,null,false);
        ButterKnife.bind(this,view);
        setView(view);
        setTitle("投掷设置");
        setIcon(android.R.drawable.ic_dialog_info);
        setCancelable(false);

        setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPrefsUtil.save(getContext(),setting);
            }
        });
        setNegativeButton("取消",null);
        initView();

        et_stand.addTextChangedListener(new TextChangeListener(R.id.et_stand) {
            @Override
            protected void afterTextChanged(Editable s, int id) {
                afterTextChanged1(s,id);
            }
        });
        et_range.addTextChangedListener(new TextChangeListener(R.id.et_range) {
            @Override
            protected void afterTextChanged(Editable s, int id) {
                afterTextChanged1(s,id);
            }
        });
        et_d.addTextChangedListener(new TextChangeListener(R.id.et_d) {
            @Override
            protected void afterTextChanged(Editable s, int id) {
                afterTextChanged1(s,id);
            }
        });
        et_f.addTextChangedListener(new TextChangeListener(R.id.et_f) {
            @Override
            protected void afterTextChanged(Editable s, int id) {
                afterTextChanged1(s,id);
            }
        });
        et_m.addTextChangedListener(new TextChangeListener(R.id.et_m) {
            @Override
            protected void afterTextChanged(Editable s, int id) {
                afterTextChanged1(s,id);
            }
        });

        ArrayAdapter adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item,items);
        sp_item.setAdapter(adapter);
    }

    private void initView() {
        et_range.setText(setting.getQd_hor()+"");
        et_range.setSelection(et_range.getText().length());
        et_d.setText(setting.getDu()+"");
        et_d.setSelection(et_d.getText().length());
        et_f.setText(setting.getFen()+"");
        et_f.setSelection(et_f.getText().length());
        et_m.setText(setting.getMiao()+"");
        et_m.setSelection(et_m.getText().length());
    }

    @OnClick({R.id.tv_stand,R.id.tv_CC})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_stand:
                switch (sp_item.getSelectedItemPosition()){
                    case 0:
                        et_stand.setText("8000");
                        et_stand.setSelection(4);
                        break;
                    case 1:
                        et_stand.setText("1068");
                        et_stand.setSelection(4);
                        break;
                    case 2:
                        et_stand.setText("1250");
                        et_stand.setSelection(4);
                        break;
                    case 3:
                        et_stand.setText("1068");
                        et_stand.setSelection(4);
                        break;
                    case 4:
                        et_stand.setText("0");
                        et_stand.setSelection(1);
                        break;
                }
                break;
            case R.id.tv_CC:
                SppUtils spp = BluetoothManager.getSpp(getContext());
                spp.setOnDataReceivedListener(new OnDataReceivedListener() {
                    @Override
                    protected void onResult(byte[] datas) {
                        onResults(datas);
                    }
                });
                spp.send(new byte[]{0x5A, 0x33, 0x34, 0x30, 0x39, 0x33, 0x03, 0x0d, 0x0a},false);
                spp.send(new byte[]{0x43, 0x30, 0x36, 0x37, 0x03, 0x0d, 0x0a},false);

                break;
        }
    }

    @OnItemSelected({R.id.sp_item})
    public void spinnerItemSelected(Spinner spinner, int position) {
        setting.setItemType(position);
        switch (position){
            case 0:
                et_stand.setText("8000");
                et_stand.setSelection(4);
                break;
            case 1:
                et_stand.setText("1068");
                et_stand.setSelection(4);
                break;
            case 2:
                et_stand.setText("1250");
                et_stand.setSelection(4);
                break;
            case 3:
                et_stand.setText("1068");
                et_stand.setSelection(4);
                break;
            case 4:
                et_stand.setText("0");
                et_stand.setSelection(1);
                break;
        }

    }

    public void afterTextChanged1(Editable s,int id) {
        try {
            switch (id){
                case R.id.et_stand:
                    setting.setRadius(Integer.parseInt(et_stand.getText().toString()));
                    break;
                case R.id.et_range:
                    setting.setQd_hor(Integer.parseInt(et_range.getText().toString()));
                    break;
                case R.id.et_d:
                    setting.setDu(Integer.parseInt(et_d.getText().toString()));
                    break;
                case R.id.et_f:
                    setting.setFen(Integer.parseInt(et_f.getText().toString()));
                    break;
                case R.id.et_m:
                    setting.setMiao(Integer.parseInt(et_m.getText().toString()));
                    break;
            }
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
    }

    private void onResults(byte[] datas) {
        RangerResult result = new RangerResult(datas);
        if (result.getType() == 1){
            initResult(result);
        }

    }

    private void initResult(RangerResult result) {
        et_d.setText(String.valueOf(result.getLevel_d()));
        et_d.setSelection(et_d.getText().length());
        et_f.setText(String.valueOf(result.getLevel_g()));
        et_f.setSelection(et_f.getText().length());
        et_m.setText(String.valueOf(result.getLevel_m()));
        et_m.setSelection(et_m.getText().length());
        et_range.setText(String.valueOf(result.getResult()));
        et_range.setSelection(et_range.getText().length());

        setting.setDu(result.getLevel_d());
        setting.setFen(result.getLevel_g());
        setting.setMiao(result.getLevel_m());
        setting.setLevel(RangerUtil.level(result.getLevel_d(),result.getLevel_g(),result.getLevel_m()));
    }

    public void setItemType(int itemType) {
        if (itemType > 4){
            sp_item.setSelection(itemType - 5);
        }
    }
}
