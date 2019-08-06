package com.feipulai.host.activity.setting;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.feipulai.common.utils.NetWorkUtils;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.netUtils.HttpManager;
import com.feipulai.host.netUtils.netapi.UserSubscriber;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class SettingActivity extends BaseActivity implements TextWatcher {

    @BindView(R.id.et_test_name)
    EditText mEtTestName;
    @BindView(R.id.et_test_site)
    EditText mEtTestSite;
    @BindView(R.id.et_sever_ip)
    EditText mEtSeverIp;
    @BindView(R.id.sp_host_id)
    Spinner mSpHostId;
    @BindView(R.id.sw_auto_broadcast)
    Switch mSwAutoBroadcast;
    @BindView(R.id.sw_rt_upload)
    Switch mSwRtUpload;
    @BindView(R.id.sw_auto_print)
    Switch mSwAutoPrint;

    private List<Integer> hostIdList;
    private SystemSetting setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_setting);
        ButterKnife.bind(this);
        setting = SettingHelper.getSystemSetting();

        mEtTestName.setText(setting.getTestName());
        mEtTestSite.setText(setting.getTestSite());
        mEtSeverIp.setText(setting.getServerIp());

        mEtTestSite.addTextChangedListener(this);
        mEtTestName.addTextChangedListener(this);
        mEtSeverIp.addTextChangedListener(this);

        hostIdList = new ArrayList<>();
        Integer maxHostId = ItemDefault.HOST_IDS_MAP.get(TestConfigs.sCurrentItem.getMachineCode());
        for (int i = 1; i <= maxHostId; i++) {
            hostIdList.add(i);
        }

        mSpHostId.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hostIdList));
        mSpHostId.setSelection(SettingHelper.getSystemSetting().getHostId() - 1);

        mSwAutoBroadcast.setChecked(setting.isAutoBroadcast());
        mSwRtUpload.setChecked(setting.isRtUpload());
        mSwAutoPrint.setChecked(setting.isAutoPrint());
    }

    @OnItemSelected({R.id.sp_host_id})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {

            case R.id.sp_host_id:
                setting.setHostId(position + 1);
                break;

        }
    }

    @OnClick({R.id.sw_auto_broadcast, R.id.sw_rt_upload, R.id.sw_auto_print, R.id.btn_bind, R.id.btn_default, R.id.btn_net_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.sw_auto_broadcast:
                setting.setAutoBroadcast(mSwAutoBroadcast.isChecked());
                break;

            case R.id.sw_rt_upload:
                setting.setRtUpload(mSwRtUpload.isChecked());
                break;

            case R.id.sw_auto_print:
                setting.setAutoPrint(mSwAutoPrint.isChecked());
                break;

            case R.id.btn_bind:
                bind();
                break;

            case R.id.btn_default:
                mEtSeverIp.setText(TestConfigs.DEFAULT_IP_ADDRESS);
                setting.setServerIp(TestConfigs.DEFAULT_IP_ADDRESS);
                break;

            case R.id.btn_net_setting:
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                break;

        }

    }

    /**
     * 绑定服务器
     */
    private void bind() {
        String url = mEtSeverIp.getText().toString().trim() + "/app/";
        if (!url.startsWith("http")) {//修改IP
            url = "http://" + url;
        }
        if (!NetWorkUtils.isValidUrl(url)) {
            toastSpeak("非法的服务器地址");
            return;
        }
        HttpManager.getInstance().changeBaseUrl(url);
        UserSubscriber subscriber = new UserSubscriber();
        subscriber.takeBind(setting.getHostId(), this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SettingHelper.updateSettingCache(setting);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        setting.setTestName(mEtTestName.getText().toString().trim());
        setting.setTestSite(mEtTestSite.getText().toString().trim());
        String url = mEtSeverIp.getText().toString().trim() + "/app/";
        if (!url.startsWith("http")) {//修改IP
            url = "http://" + url;
        }
        if (!NetWorkUtils.isValidUrl(url)) {
            return;
        }

        setting.setServerIp(mEtSeverIp.getText().toString().trim());
    }

}
