package com.feipulai.exam.activity.ranger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.spputils.OnDataReceivedListener;
import com.feipulai.device.spputils.SppUtils;
import com.feipulai.device.spputils.beans.RangerResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.ranger.bluetooth.BluetoothManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class JumpSettingDialog extends AlertDialog.Builder implements TextWatcher {
    @BindView(R.id.et_jump_hor1)
    EditText et_jump_hor1;
    @BindView(R.id.et_jump_d1)
    EditText et_jump_d1;
    @BindView(R.id.et_jump_f1)
    EditText et_jump_f1;
    @BindView(R.id.et_jump_m1)
    EditText et_jump_m1;
    @BindView(R.id.et_jump_hor2)
    EditText et_jump_hor2;
    @BindView(R.id.et_jump_d2)
    EditText et_jump_d2;
    @BindView(R.id.et_jump_f2)
    EditText et_jump_f2;
    @BindView(R.id.et_jump_m2)
    EditText et_jump_m2;
    @BindView(R.id.et_range)
    EditText et_range;

    private SppUtils utils;
    private int clickId;

    RangerSetting rangerSetting;

    public JumpSettingDialog(Context context) {
        super(context);
        setTitle("跳远设置");
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_jump_setting,null,false);
        ButterKnife.bind(this,view);
        setIcon(android.R.drawable.ic_dialog_info);
        setCancelable(false);
        setView(view);
        rangerSetting = SharedPrefsUtil.loadFormSource(getContext(),RangerSetting.class);
        setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPrefsUtil.save(getContext(),rangerSetting);
            }
        });
        setNegativeButton("取消",null);
        utils = BluetoothManager.getSpp(context);
        utils.setOnDataReceivedListener(new OnDataReceivedListener() {
            @Override
            protected void onResult(byte[] datas) {
                onResults(datas);
            }
        });
        initView();
        et_jump_hor1.addTextChangedListener(this);
        et_jump_d1.addTextChangedListener(this);
        et_jump_f1.addTextChangedListener(this);
        et_jump_m1.addTextChangedListener(this);
        et_jump_hor2.addTextChangedListener(this);
        et_jump_d2.addTextChangedListener(this);
        et_jump_f2.addTextChangedListener(this);
        et_jump_m2.addTextChangedListener(this);
    }

    private void initView() {
        et_jump_hor1.setText(rangerSetting.getQd1_hor()+"");
        et_jump_hor1.setSelection(et_jump_hor1.getText().length());
        et_jump_hor2.setText(rangerSetting.getQd2_hor()+"");
        et_jump_hor2.setSelection(et_jump_hor2.getText().length());
        et_jump_d1.setText(rangerSetting.getDu1()+"");
        et_jump_d1.setSelection(et_jump_d1.getText().length());
        et_jump_d2.setText(rangerSetting.getDu2()+"");
        et_jump_d2.setSelection(et_jump_d2.getText().length());
        et_jump_f1.setText(rangerSetting.getFen1()+"");
        et_jump_f1.setSelection(et_jump_f1.getText().length());
        et_jump_f2.setText(rangerSetting.getFen2()+"");
        et_jump_f2.setSelection(et_jump_f2.getText().length());
        et_jump_m1.setText(rangerSetting.getMiao1()+"");
        et_jump_m1.setSelection(et_jump_m1.getText().length());
        et_jump_m2.setText(rangerSetting.getMiao2()+"");
        et_jump_m2.setSelection(et_jump_m2.getText().length());
        et_range.setText(rangerSetting.getDistance()+"");
        et_range.setSelection(et_range.getText().length());

    }

    private void onResults(byte[] datas) {
        RangerResult result = new RangerResult(datas);
        if (clickId == R.id.tv_cc1){
            initResult1(result);
        }else if (clickId == R.id.tv_cc2){
            initResult2(result);
        }

    }

    private void initResult1(RangerResult result) {
        et_jump_hor1.setText(String.valueOf(result.getResult()));
        et_jump_hor1.setSelection(et_jump_hor1.getText().length());
        et_jump_d1.setText(String.valueOf(result.getLevel_d()));
        et_jump_d1.setSelection(et_jump_d1.getText().length());
        et_jump_f1.setText(String.valueOf(result.getLevel_g()));
        et_jump_f1.setSelection(et_jump_f1.getText().length());
        et_jump_m1.setText(String.valueOf(result.getLevel_m()));
        et_jump_m1.setSelection(et_jump_m1.getText().length());

        rangerSetting.setQd1_hor(result.getResult());
        rangerSetting.setDu1(result.getLevel_d());
        rangerSetting.setFen1(result.getLevel_g());
        rangerSetting.setMiao1(result.getLevel_m());
        rangerSetting.setLevel1(RangerUtil.level(rangerSetting.getDu1(),rangerSetting.getFen1(),rangerSetting.getMiao1()));


    }

    private void initResult2(RangerResult result) {
        et_jump_hor2.setText(String.valueOf(result.getResult()));
        et_jump_hor2.setSelection(et_jump_hor2.getText().length());
        et_jump_d2.setText(String.valueOf(result.getLevel_d()));
        et_jump_d2.setSelection(et_jump_d2.getText().length());
        et_jump_f2.setText(String.valueOf(result.getLevel_g()));
        et_jump_f2.setSelection(et_jump_f2.getText().length());
        et_jump_m2.setText(String.valueOf(result.getLevel_m()));
        et_jump_m2.setSelection(et_jump_m2.getText().length());

        rangerSetting.setQd2_hor(result.getResult());
        rangerSetting.setDu2(result.getLevel_d());
        rangerSetting.setFen2(result.getLevel_g());
        rangerSetting.setMiao2(result.getLevel_m());
        rangerSetting.setLevel2(RangerUtil.level(rangerSetting.getDu2(),rangerSetting.getFen2(),rangerSetting.getMiao2()));

        Point point1 = RangerUtil.getPoint(rangerSetting.getLevel1(),rangerSetting.getQd1_hor());
        Point point2 = RangerUtil.getPoint(rangerSetting.getLevel2(),rangerSetting.getQd2_hor());
        rangerSetting.setDistance(RangerUtil.length(point1,point2));
        et_range.setText(rangerSetting.getDistance()+"");

    }

    @OnClick({R.id.tv_cc1,R.id.tv_cc2})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_cc1:
                clickId = R.id.tv_cc1;
                sendparam();
                break;
            case R.id.tv_cc2:
                clickId = R.id.tv_cc2;
                sendparam();
                break;
        }
    }

    private void sendparam() {
        byte[] bytes = new byte[]{0x5A, 0x33, 0x34, 0x30, 0x39, 0x33, 0x03, 0x0d, 0x0a};
        utils.send(bytes, false);
        bytes = new byte[]{0x43, 0x30, 0x36, 0x37, 0x03, 0x0d, 0x0a};
        utils.send(bytes, false);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        try {
            rangerSetting.setQd1_hor(Integer.parseInt(et_jump_hor1.getText().toString()));
            rangerSetting.setDu1(Integer.parseInt(et_jump_d1.getText().toString()));
            rangerSetting.setFen1(Integer.parseInt(et_jump_f1.getText().toString()));
            rangerSetting.setMiao1(Integer.parseInt(et_jump_m1.getText().toString()));
            rangerSetting.setLevel1(RangerUtil.level(rangerSetting.getDu1(),rangerSetting.getFen1(),rangerSetting.getMiao1()));

            rangerSetting.setQd2_hor(Integer.parseInt(et_jump_hor2.getText().toString()));
            rangerSetting.setDu2(Integer.parseInt(et_jump_d2.getText().toString()));
            rangerSetting.setFen2(Integer.parseInt(et_jump_f2.getText().toString()));
            rangerSetting.setMiao2(Integer.parseInt(et_jump_m2.getText().toString()));
            rangerSetting.setLevel2(RangerUtil.level(rangerSetting.getDu2(),rangerSetting.getFen2(),rangerSetting.getMiao2()));
        }catch (NumberFormatException e){
            e.printStackTrace();
        }

    }
}
