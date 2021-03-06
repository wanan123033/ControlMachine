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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.feipulai.common.utils.AudioUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.NetWorkUtils;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.SplashScreenActivity;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.login.LoginActivity;
import com.feipulai.host.bean.ActivateBean;
import com.feipulai.host.bean.FaceSdkBean;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.netUtils.HttpManager;
import com.feipulai.host.netUtils.HttpSubscriber;
import com.feipulai.host.netUtils.OnResultListener;
import com.feipulai.host.netUtils.netapi.UserSubscriber;
import com.google.gson.Gson;
import com.ww.fpl.libarcface.common.Constants;
import com.ww.fpl.libarcface.util.ConfigUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

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
    @BindView(R.id.rl_net)
    RelativeLayout rl_net;
    @BindView(R.id.view_itemd)
    View view_itemd;
    @BindView(R.id.sw_net)
    CheckBox sw_net;
    @BindView(R.id.txt_host_hint)
    TextView txtHostHint;
    @BindView(R.id.sp_afr)
    Spinner spAfr;
    @BindView(R.id.ll_afr)
    LinearLayout llAfr;
    private ActivateBean activateBean;
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_system_setting;
    }

    @Override
    protected void initData() {
        setting = SettingHelper.getSystemSetting();
        activateBean = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), ActivateBean.class);
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
        if (setting.getCheckTool() == 4) {
            rl_net.setVisibility(View.VISIBLE);
            view_itemd.setVisibility(View.VISIBLE);
            sw_net.setChecked(setting.isNetCheckTool());
        } else {
            rl_net.setVisibility(View.GONE);
            view_itemd.setVisibility(View.GONE);
        }

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
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_YWQZ || TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_SGBQS) {
            txtHostHint.setVisibility(View.VISIBLE);
        }
        spAfr.setSelection(setting.getAfrContrast());
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle(R.string.setting_title);
    }

    @OnItemSelected({R.id.sp_host_id, R.id.sp_check_tool, R.id.sp_afr})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {

            case R.id.sp_host_id:
                setting.setHostId(position + 1);
                txtChannel.setText((SerialConfigs.sProChannels.get(machineCode) + setting.getHostId() - 1) + "");
                break;
            case R.id.sp_check_tool:
                setting.setCheckTool(position);
                if (position == 4) {
                    llAfr.setVisibility(View.VISIBLE);
                    boolean isEngine = ConfigUtil.getISEngine(this);
                    if (!isEngine) {
                        ToastUtils.showShort("????????????????????????????????????");
                    }
                    rl_net.setVisibility(View.VISIBLE);
                    view_itemd.setVisibility(View.VISIBLE);
                    sw_net.setChecked(setting.isNetCheckTool());
                } else {
                    rl_net.setVisibility(View.GONE);
                    view_itemd.setVisibility(View.GONE);
                    llAfr.setVisibility(View.GONE);
                }
                break;
            case R.id.sp_afr:
                setting.setAfrContrast(position);
                break;
        }
    }

    @OnClick({R.id.btn_face_init, R.id.sw_auto_broadcast, R.id.sw_rt_upload, R.id.sw_auto_print,
            R.id.btn_bind, R.id.btn_default, R.id.btn_net_setting, R.id.cb_custom_channel, R.id.sw_net
            , R.id.btn_login})
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
            case R.id.btn_login:
                IntentUtil.gotoActivity(this, LoginActivity.class);
                break;
            case R.id.btn_default:
                mEtSeverIp.setText(TestConfigs.DEFAULT_IP_ADDRESS);
                setting.setServerIp(TestConfigs.DEFAULT_IP_ADDRESS);
                break;

            case R.id.btn_net_setting:
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                break;

            case R.id.sw_net:
                setting.setNetCheckTool(sw_net.isChecked());
                break;

        }

    }
    private void activeFace(final boolean updateActive) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                if (updateActive) {
                    if (activateBean.getJsonFaceSdkKeyList() != null && activateBean.getJsonFaceSdkKeyList().size() > 0) {
                        int activeCode = 0;
                        for (FaceSdkBean faceSdkBean : activateBean.getFaceSdkKeyList()) {
                            String appid = faceSdkBean.getAppId();
                            String sdkKey = faceSdkBean.getSdkKey();
                            activeCode = FaceEngine.activeOnline(SettingActivity.this, appid, sdkKey);
                            if (activeCode == ErrorInfo.MOK && activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                                emitter.onNext(activeCode);
                                break;
                            }
                        }

                        emitter.onNext(activeCode);
                    } else {
                        emitter.onNext(777888);
                    }
                } else {
                    int activeCode = FaceEngine.activeOnline(SettingActivity.this, Constants.APP_ID, Constants.SDK_KEY);
                    if (activeCode != ErrorInfo.MOK && activeCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                        activeCode = FaceEngine.activeOnline(SettingActivity.this, Constants.APP_ID_2, Constants.SDK_KEY_2);
                    }
                    if (activeCode != ErrorInfo.MOK && activeCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                        if (activateBean.getJsonFaceSdkKeyList() != null && activateBean.getJsonFaceSdkKeyList().size() > 0) {
                            for (FaceSdkBean faceSdkBean : activateBean.getFaceSdkKeyList()) {
                                String appid = faceSdkBean.getAppId();
                                String sdkKey = faceSdkBean.getSdkKey();
                                activeCode = FaceEngine.activeOnline(SettingActivity.this, appid, sdkKey);
                                if (activeCode == ErrorInfo.MOK && activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                                    emitter.onNext(activeCode);
                                    break;
                                }
                            }

                        }
                    }
                    emitter.onNext(activeCode);
                }


            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            ToastUtils.showShort(getString(R.string.active_success));
                            ConfigUtil.setISEngine(SettingActivity.this, true);
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            ToastUtils.showShort(getString(R.string.already_activated));
                        } else if (activeCode == 777888) {
                            ToastUtils.showShort("???????????????KEY?????????????????????");
                        } else {
                            new SweetAlertDialog(SettingActivity.this, SweetAlertDialog.WARNING_TYPE).setTitleText("????????????????????????")
                                    .setContentText("????????????????????????ID")
                                    .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                    activate();

                                }
                            }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            }).show();
                            ToastUtils.showShort(getString(R.string.active_failed));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.showShort("????????????????????????");
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void activate() {
        final long runTime = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.APP_USE_TIME, 0L);
        new UserSubscriber().activate(runTime, 1, new OnResultListener<ActivateBean>() {
            @Override
            public void onSuccess(ActivateBean result) {
                activateBean = result;
                if (activateBean.getFaceSdkKeyList() != null) {
                    activateBean.setFaceSdkKeyJson(new Gson().toJson(result.getFaceSdkKeyList()));
                }
                SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.APP_USE_TIME, result.getCurrentRunTime());
                SharedPrefsUtil.save(SettingActivity.this, activateBean);
                if (result.getFaceSdkKeyList() == null && result.getFaceSdkKeyList().size() == 0) {
                    ToastUtils.showShort("???????????????KEY?????????????????????");
                } else {
                    activeFace(true);

                }

            }

            @Override
            public void onFault(int code, String errorMsg) {

            }

        });
    }

    @OnClick({R.id.btn_face_init})
    public void onClickFaceInit() {
        activeFace(false);
    }
    /**
     * ???????????????
     */
    private void bind() {
        String url = mEtSeverIp.getText().toString().trim() + "/";
        if (!url.startsWith("http")) {//??????IP
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
        if (!url.startsWith("http")) {//??????IP
            url = "http://" + url;
        }
        if (!NetWorkUtils.isValidUrl(url)) {
            return;
        }

        setting.setServerIp(mEtSeverIp.getText().toString().trim());
    }

}
