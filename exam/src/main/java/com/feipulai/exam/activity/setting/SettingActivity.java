package com.feipulai.exam.activity.setting;

import android.content.Intent;
import android.graphics.Paint;
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
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.utils.AudioUtil;
import com.feipulai.common.utils.NetWorkUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.udp.UdpLEDUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LoginActivity;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.netUtils.HttpManager;

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
    @BindView(R.id.sw_broadcast_name)
    CheckBox mSwBroadcastName;
    @BindView(R.id.rl_broadcast_name)
    RelativeLayout rlBroadcastName;
    @BindView(R.id.sw_rt_upload)
    CheckBox mSwRtUpload;
    @BindView(R.id.sw_auto_print)
    CheckBox mSwAutoPrint;
    @BindView(R.id.sw_identity_mark)
    CheckBox mSwIdentityMark;
    @BindView(R.id.sw_add_student)
    CheckBox mSwAddStudent;
    @BindView(R.id.sp_pattern)
    Spinner spPattern;
    @BindView(R.id.sp_check_tool)
    Spinner spCheckTool;
    @BindView(R.id.btn_net_setting)
    TextView btnNetSetting;
    @BindView(R.id.sp_qr_length)
    Spinner spQrLength;
    @BindView(R.id.sb_volume)
    SeekBar sb_volume;
    @BindView(R.id.cb_route)
    CheckBox cbRoute;
    private String[] partternList = new String[]{"个人测试", "分组测试"};
    private List<Integer> hostIdList;
    private SystemSetting systemSetting;


    @Override
    protected int setLayoutResID() {
        return R.layout.activity_system_setting;
    }

    @Override
    protected void initData() {
        systemSetting = SettingHelper.getSystemSetting();

        mEtTestSite.setText(systemSetting.getTestSite());
        mEtSeverIp.setText(systemSetting.getServerIp());

        mEtTestSite.addTextChangedListener(this);
        mEtTestName.addTextChangedListener(this);
        mEtSeverIp.addTextChangedListener(this);

        hostIdList = new ArrayList<>();
        Integer maxHostId = ItemDefault.HOST_IDS_MAP.get(TestConfigs.sCurrentItem.getMachineCode());
        for (int i = 1; i <= maxHostId; i++) {
            hostIdList.add(i);
        }

        ArrayAdapter spPatternAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(partternList));
        spPatternAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPattern.setAdapter(spPatternAdapter);
        ArrayAdapter mSpHostIdAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hostIdList);
        mSpHostIdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpHostId.setAdapter(mSpHostIdAdapter);
        ArrayAdapter spCheckToolAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.check_tool)));
        spCheckToolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCheckTool.setAdapter(spCheckToolAdapter);

        spCheckTool.setSelection(systemSetting.getCheckTool());
        Integer qrlength[] = new Integer[21];
        for (int i = 0; i < qrlength.length; i++) {
            qrlength[i] = i;
        }
        ArrayAdapter spQrLengthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(qrlength));
        spQrLengthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spQrLength.setAdapter(spQrLengthAdapter);
        spQrLength.setSelection(systemSetting.getQrLength());

        mSpHostId.setSelection(systemSetting.getHostId() - 1);
        mSwAutoBroadcast.setChecked(systemSetting.isAutoBroadcast());
        mSwBroadcastName.setChecked(systemSetting.isBroadcastName());
        mSwAutoPrint.setChecked(systemSetting.isAutoPrint());
        mSwRtUpload.setChecked(systemSetting.isRtUpload());
        mSwIdentityMark.setChecked(systemSetting.isIdentityMark());
        mSwAddStudent.setChecked(systemSetting.isTemporaryAddStu());
        if (systemSetting.getTestPattern() == SystemSetting.PERSON_PATTERN) {
            spPattern.setSelection(0);
        } else {
            spPattern.setSelection(1);
        }

        btnNetSetting.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        btnNetSetting.getPaint().setAntiAlias(true);//抗锯齿

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

        String locatIp = NetWorkUtils.getLocalIp();
        cbRoute.setText(TextUtils.isEmpty(locatIp) ? "以太网未配置" : locatIp);
        if (TextUtils.isEmpty(locatIp)) {
            systemSetting.setAddRoute(false);
        }
        cbRoute.setChecked(systemSetting.isAddRoute());
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("设置").addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEtTestName.setText(systemSetting.getTestName());
    }


    @OnItemSelected({R.id.sp_host_id, R.id.sp_check_tool, R.id.sp_pattern, R.id.sp_qr_length})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_host_id:
                systemSetting.setHostId(position + 1);
                break;
            case R.id.sp_pattern:
                systemSetting.setTestPattern(position == 0 ? SystemSetting.PERSON_PATTERN : SystemSetting.GROUP_PATTERN);
                break;
            case R.id.sp_qr_length:
                systemSetting.setQrLength(position);
                break;
            case R.id.sp_check_tool:
                systemSetting.setCheckTool(position);
                break;
        }
    }

    @OnClick({R.id.sw_auto_broadcast, R.id.sw_broadcast_name, R.id.sw_rt_upload, R.id.sw_auto_print, R.id.btn_bind, R.id.btn_default, R.id.btn_net_setting
            , R.id.txt_advanced, R.id.sw_identity_mark, R.id.sw_add_student, R.id.cb_route})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.sw_auto_broadcast:
                systemSetting.setAutoBroadcast(mSwAutoBroadcast.isChecked());
                if (mSwAutoBroadcast.isChecked()) {
                    rlBroadcastName.setVisibility(View.VISIBLE);
                } else {
                    rlBroadcastName.setVisibility(View.GONE);
                }
                break;
            case R.id.sw_broadcast_name:
                systemSetting.setBroadcastName(mSwBroadcastName.isChecked());
                break;
            case R.id.sw_rt_upload:
                systemSetting.setRtUpload(mSwRtUpload.isChecked());
                break;

            case R.id.sw_auto_print:
                systemSetting.setAutoPrint(mSwAutoPrint.isChecked());
                break;

            case R.id.sw_identity_mark:
                systemSetting.setIdentityMark(mSwIdentityMark.isChecked());
                break;
            case R.id.sw_add_student:
                systemSetting.setTemporaryAddStu(mSwAddStudent.isChecked());
                break;
            case R.id.btn_bind:
                gotoLogin();
                break;

            case R.id.btn_default:
                mEtSeverIp.setText(TestConfigs.DEFAULT_IP_ADDRESS);
                systemSetting.setServerIp(TestConfigs.DEFAULT_IP_ADDRESS);
                break;

            case R.id.btn_net_setting:
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                break;

            case R.id.txt_advanced:
                startActivity(new Intent(this, AdvancedPwdActivity.class));
                break;
            case R.id.cb_route:
                if (TextUtils.isEmpty(NetWorkUtils.getLocalIp())) {
                    ToastUtils.showShort("以太网未配置，请先启动并配置");
                    cbRoute.setChecked(false);
                    return;
                }
                String locatIp = NetWorkUtils.getLocalIp();
                String routeIp = locatIp.substring(0, locatIp.lastIndexOf("."));
                if (cbRoute.isChecked()) {
                    UdpLEDUtil.shellExec("ip route add " + routeIp + ".0/24 dev eth0 proto static scope link table wlan0 \n");
                    systemSetting.setAddRoute(true);
                } else {
                    UdpLEDUtil.shellExec("ip route del  " + routeIp + ".0/24 dev eth0");
                    systemSetting.setAddRoute(false);
                }
                break;
        }

    }

    /**
     * 登录
     */
    private void gotoLogin() {
        String url = mEtSeverIp.getText().toString().trim() + "/app/";
        if (!url.startsWith("http")) {//修改IP
            url = "http://" + url;
        }
        if (!NetWorkUtils.isValidUrl(url)) {
            toastSpeak("非法的服务器地址");
            return;
        }
        SettingHelper.updateSettingCache(systemSetting);
        HttpManager.resetManager();
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        HttpManager.resetManager();
        SettingHelper.updateSettingCache(systemSetting);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {

        systemSetting.setTestName(mEtTestName.getText().toString().trim());
        systemSetting.setTestSite(mEtTestSite.getText().toString().trim());
        systemSetting.setServerIp(mEtSeverIp.getText().toString().trim());
    }


}
