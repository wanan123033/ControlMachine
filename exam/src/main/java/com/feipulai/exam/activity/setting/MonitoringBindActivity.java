package com.feipulai.exam.activity.setting;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.CheckDeviceOpener;
import com.feipulai.device.ic.NFCDevice;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.utils.CodeUtils;
import com.google.gson.Gson;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 监控绑定
 * Created by zzs on  2019/12/31
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MonitoringBindActivity extends BaseTitleActivity implements CheckDeviceOpener.OnCheckDeviceArrived {

    @BindView(R.id.et_serial)
    EditText etSerial;
    @BindView(R.id.cb_select_all)
    CheckBox cbSelectAll;
    @BindView(R.id.rv_bind_monitoring)
    RecyclerView rvBindMonitoring;
    private List<MonitoringBean> monitoringBeans;
    private MonitoringBindAdapter bindAdapter;
    private List<MonitoringBean> selectBeans = new ArrayList<>();

    private static final int RESULT_LOAD_IMAGE = 122;
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_monitoring_bind;
    }

    @Override
    protected void initData() {
        CheckDeviceOpener.getInstance().setOnCheckDeviceArrived(this);
        CheckDeviceOpener.getInstance().open(this, false,
                false,
                true);
        monitoringBeans = SettingHelper.getSystemSetting().getMonitoringList();

        for (MonitoringBean monitoringBean : monitoringBeans) {
            monitoringBean.setSelect(false);
        }
        bindAdapter = new MonitoringBindAdapter(monitoringBeans);
        rvBindMonitoring.setLayoutManager(new LinearLayoutManager(this));
        rvBindMonitoring.setAdapter(bindAdapter);
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("监控绑定");
    }

    @Override
    public void onICCardFound(NFCDevice nfcd) {

    }

    @Override
    public void onIdCardRead(IDCardInfo idCardInfo) {

    }

    @Override
    public void onQrArrived(final String qrCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                etSerial.setText(qrCode);
                if (!TextUtils.isEmpty(qrCode)){
                    etSerial.setSelection(qrCode.length() - 1);
                }

            }
        });

    }

    @Override
    public void onQRWrongLength(int length, int expectLength) {

    }

    @OnClick({R.id.btn_bind, R.id.btn_unBind, R.id.cb_select_all,R.id.btn_bitBind})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_bind:
                bind();
                break;
            case R.id.btn_unBind:
                selectBeans.clear();
                for (MonitoringBean monitoringBean : monitoringBeans) {
                    if (monitoringBean.isSelect()) {
                        selectBeans.add(monitoringBean);
                    }
                }
                if (selectBeans.size() > 0) {
                    showUnBindDialog();
                } else {
                    ToastUtils.showShort("请选择解绑的监控");
                }
                break;
            case R.id.cb_select_all:
                for (MonitoringBean monitoringBean : monitoringBeans) {
                    monitoringBean.setSelect(cbSelectAll.isChecked());
                }
                bindAdapter.notifyDataSetChanged();
                break;
            case R.id.btn_bitBind:
                Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
                break;
        }
    }

    private void bind() {
        if (TextUtils.isEmpty(etSerial.getText().toString().trim())) {
            ToastUtils.showShort("请输入绑定监控序列号");
        } else {
            MonitoringBean addBean = new MonitoringBean(etSerial.getText().toString().trim(), DateUtil.getCurrentTime2("yyyy-MM-dd HH:mm:ss"), cbSelectAll.isChecked());
            if (monitoringBeans.contains(addBean)) {
                ToastUtils.showShort("当前监控序列号已存在");
                return;
            }

            monitoringBeans.add(addBean);
            bindAdapter.notifyDataSetChanged();
            etSerial.setText("");
            ToastUtils.showShort("绑定成功");
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        SettingHelper.getSystemSetting().setMonitoringJson(new Gson().toJson(monitoringBeans));
        SettingHelper.updateSettingCache(SettingHelper.getSystemSetting());
    }

    private void showUnBindDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.clear_dialog_title))
                .setContentText("是否进行解绑？")
                .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();

                for (MonitoringBean monitoringBean : selectBeans) {
                    int index = monitoringBeans.indexOf(monitoringBean);
                    if (index != -1) {
                        monitoringBeans.remove(index);
                    }
                }
                bindAdapter.notifyDataSetChanged();
                if (selectBeans.size() == 0) {
                    cbSelectAll.setChecked(false);
                }
                ToastUtils.showShort("解绑成功");
            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //我们需要判断requestCode是否是我们之前传给startActivityForResult()方法的RESULT_LOAD_IMAGE，并且返回的数据不能为空
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            //查询我们需要的数据
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();


            CodeUtils.analyzeBitmap(picturePath, new CodeUtils.AnalyzeCallback() {
                @Override
                public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                    etSerial.setText(result);
                    etSerial.setSelection(result.length());
                    bind();
                }

                @Override
                public void onAnalyzeFailed() {

                }
            });
        }
    }
}
