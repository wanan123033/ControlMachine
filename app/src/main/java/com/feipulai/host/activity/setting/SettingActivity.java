package com.feipulai.host.activity.setting;

import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.utils.AudioUtil;
import com.feipulai.common.utils.NetWorkUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.netUtils.HttpManager;
import com.feipulai.host.netUtils.netapi.UserSubscriber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class SettingActivity extends BaseTitleActivity implements TextWatcher {

    @BindView(R.id.et_test_name)
    EditText mEtTestName;
    @BindView(R.id.et_test_site)
    EditText mEtTestSite;
    @BindView(R.id.et_sever_ip)
    EditText mEtSeverIp;
    @BindView(R.id.sp_host_id)
    Spinner mSpHostId;
    @BindView(R.id.sw_auto_broadcast)
    CheckBox mSwAutoBroadcast;
    @BindView(R.id.sw_rt_upload)
    CheckBox mSwRtUpload;
    @BindView(R.id.sw_auto_print)
    CheckBox mSwAutoPrint;
    @BindView(R.id.sp_check_tool)
    Spinner spCheckTool;
    @BindView(R.id.txt_channel)
    TextView txtChannel;
    @BindView(R.id.edit_custom_channel)
    EditText editCustomChannel;
    @BindView(R.id.cb_custom_channel)
    CheckBox cbCustomChannel;
    private List<Integer> hostIdList;
    private SystemSetting setting;
    @BindView(R.id.sb_volume)
    SeekBar sb_volume;
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_system_setting;
    }

    @Override
    protected void initData() {
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

        ArrayAdapter mSpHostIdAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hostIdList);
        mSpHostIdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpHostId.setAdapter(mSpHostIdAdapter);
        mSpHostId.setSelection(setting.getHostId() - 1);
        mSwAutoBroadcast.setChecked(setting.isAutoBroadcast());
        mSwRtUpload.setChecked(setting.isRtUpload());
        mSwAutoPrint.setChecked(setting.isAutoPrint());


        ArrayAdapter spCheckToolAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.check_tool)));
        spCheckToolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCheckTool.setAdapter(spCheckToolAdapter);

        spCheckTool.setSelection(setting.getCheckTool());

        txtChannel.setText((SerialConfigs.sProChannels.get(machineCode) + setting.getHostId() - 1) + "");

        editCustomChannel.setText(setting.getChannel() + "");
        cbCustomChannel.setChecked(setting.isCustomChannel());

        sb_volume.setMax(AudioUtil.getInstance(this).getMediaMaxVolume());
        sb_volume.setProgress(AudioUtil.getInstance(this).getMediaVolume());
        sb_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                AudioUtil.getInstance(SettingActivity.this).setMediaVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle(R.string.setting_title);
    }

    @OnItemSelected({R.id.sp_host_id, R.id.sp_check_tool})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {

            case R.id.sp_host_id:
                setting.setHostId(position + 1);
                txtChannel.setText((SerialConfigs.sProChannels.get(machineCode) + setting.getHostId() - 1) + "");
                break;
            case R.id.sp_check_tool:
                setting.setCheckTool(position);
                break;
        }
    }

    @OnClick({R.id.sw_auto_broadcast, R.id.sw_rt_upload, R.id.sw_auto_print, R.id.btn_bind, R.id.btn_default, R.id.btn_net_setting, R.id.cb_custom_channel})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.sw_auto_broadcast:
                setting.setAutoBroadcast(mSwAutoBroadcast.isChecked());
                break;
            case R.id.cb_custom_channel:
                setting.setCustomChannel(cbCustomChannel.isChecked());
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
            toastSpeak(MyApplication.getInstance().getString(R.string.illegal_server_address));
            return;
        }
        HttpManager.getInstance().changeBaseUrl(url);
        UserSubscriber subscriber = new UserSubscriber();
        subscriber.takeBind(setting.getHostId(), this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!TextUtils.isEmpty(editCustomChannel.getText().toString().trim())) {
            setting.setChannel(Integer.valueOf(editCustomChannel.getText().toString().trim()));
        }
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(
                setting.getUseChannel())));
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
