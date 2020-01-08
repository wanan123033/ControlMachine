package com.feipulai.exam.activity.setting;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.CheckDeviceOpener;
import com.feipulai.device.ic.NFCDevice;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
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
    public void onQrArrived(String qrCode) {
        etSerial.setText(qrCode);
    }

    @Override
    public void onQRWrongLength(int length, int expectLength) {

    }

    @OnClick({R.id.btn_bind, R.id.btn_unBind, R.id.cb_select_all})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_bind:
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
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        SettingHelper.getSystemSetting().setMonitoringList(monitoringBeans);
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
                ToastUtils.showShort("解绑成功");
            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();
    }
}
