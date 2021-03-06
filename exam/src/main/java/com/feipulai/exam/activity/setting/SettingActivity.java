package com.feipulai.exam.activity.setting;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.NetWorkUtils;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.common.view.dialog.DialogUtils;
import com.feipulai.common.voice.VoiceSettingActivity;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.ConverterVersion;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.device.tcp.SendTcpClientThread;
import com.feipulai.device.udp.UdpLEDUtil;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MainActivity;
import com.feipulai.exam.activity.login.LoginActivity;
import com.feipulai.exam.activity.account.AccountSettingActivity;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.bean.ActivateBean;
import com.feipulai.exam.bean.FaceSdkBean;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.netUtils.HttpManager;
import com.feipulai.exam.netUtils.OnResultListener;
import com.feipulai.exam.netUtils.netapi.HttpSubscriber;
import com.feipulai.exam.utils.bluetooth.BlueToothListActivity;
import com.google.gson.Gson;
import com.orhanobut.logger.utils.LogUtils;
import com.ww.fpl.libarcface.common.Constants;
import com.ww.fpl.libarcface.faceserver.FaceServer;
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
    @BindView(R.id.txt_channel)
    TextView txtChannel;
    @BindView(R.id.edit_custom_channel)
    EditText editCustomChannel;
    @BindView(R.id.cb_custom_channel)
    CheckBox cbCustomChannel;
    @BindView(R.id.cb_monitoring)
    CheckBox cbMonitoring;
    @BindView(R.id.cb_thermometer)
    CheckBox cbThermometer;
    @BindView(R.id.et_tcp_ip)
    EditText mEtTcpIp;
    @BindView(R.id.txt_custom_channel)
    TextView txtCustomChannel;
    @BindView(R.id.btn_monitoring_setting)
    TextView btnMonitoringSetting;
    @BindView(R.id.btn_thermometer)
    TextView btnThermometer;
    @BindView(R.id.txt_advanced)
    TextView txtAdvanced;
    @BindView(R.id.sw_auto_discern)
    CheckBox sw_auto_discern;

    @BindView(R.id.btn_print_setting)
    TextView btnPrintSetting;
    @BindView(R.id.cb_is_tcp)
    CheckBox cbIsTcp;
    @BindView(R.id.rlTCPSimultaneous)
    RelativeLayout rlTCPSimultaneous;
    @BindView(R.id.cb_tcp_simultaneous)
    CheckBox cbTcpSimultaneous;
    @BindView(R.id.sw_auto_score)
    CheckBox mSwAutoScore;
    @BindView(R.id.sp_print_tool)
    Spinner spPrintTool;
    @BindView(R.id.ll_print_tool)
    LinearLayout llPrintTool;
    @BindView(R.id.txt_host_hint)
    TextView txtHostHint;
    @BindView(R.id.rl_group_check)
    RelativeLayout rl_group_check;

    @BindView(R.id.sp_afr)
    Spinner spAfr;
    @BindView(R.id.ll_afr)
    LinearLayout llAfr;
    @BindView(R.id.sw_group_check)
    CheckBox sw_group_check;

    @BindView(R.id.ll_device_version)
    LinearLayout llDeviceVersion;
    @BindView(R.id.txt_device_version)
    TextView txtDeviceVersion;

    private String[] partternList = new String[]{"????????????", "????????????"};
    private List<Integer> hostIdList;
    private SystemSetting systemSetting;
    private ActivateBean activateBean;
    private SendTcpClientThread tcpClientThread;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_system_setting;
    }

    @Override
    protected void initData() {
        systemSetting = SettingHelper.getSystemSetting();
        activateBean = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), ActivateBean.class);

        mEtTestSite.setText(systemSetting.getTestSite());
        mEtSeverIp.setText(systemSetting.getServerIp());

        mEtTcpIp.setText(systemSetting.getTcpIp());

        mEtTestSite.addTextChangedListener(this);
        mEtTestName.addTextChangedListener(this);
        mEtSeverIp.addTextChangedListener(this);
        mEtTcpIp.addTextChangedListener(this);

        hostIdList = new ArrayList<>();
        Integer maxHostId = ItemDefault.HOST_IDS_MAP.get(TestConfigs.sCurrentItem.getMachineCode());
        for (int i = 1; i <= maxHostId; i++) {
            hostIdList.add(i);
        }

//        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
//            spPattern.setSelection(1);
//            spPattern.setEnabled(false);
//        }

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
        //????????????
        ArrayAdapter spQrLengthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(qrlength));
        spQrLengthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spQrLength.setAdapter(spQrLengthAdapter);
        spQrLength.setSelection(systemSetting.getQrLength());
        //????????????
        ArrayAdapter printAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.print_tool)));
        printAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPrintTool.setAdapter(printAdapter);

        mSpHostId.setSelection(systemSetting.getHostId() - 1);
        mSwAutoBroadcast.setChecked(systemSetting.isAutoBroadcast());
        mSwAutoPrint.setChecked(systemSetting.isAutoPrint());
        mSwRtUpload.setChecked(systemSetting.isRtUpload());
        mSwIdentityMark.setChecked(systemSetting.isIdentityMark());
        mSwAddStudent.setChecked(systemSetting.isTemporaryAddStu());
        mSwAutoScore.setChecked(systemSetting.isAutoScore());
        if (systemSetting.getTestPattern() == SystemSetting.PERSON_PATTERN) {
            spPattern.setSelection(0);
            rl_group_check.setVisibility(View.GONE);
        } else {
            spPattern.setSelection(1);
            llPrintTool.setVisibility(View.VISIBLE);
            rl_group_check.setVisibility(View.VISIBLE);
            spPrintTool.setSelection(systemSetting.getPrintTool());
            if (systemSetting.getPrintTool() == 1) {
                btnPrintSetting.setVisibility(View.VISIBLE);
            }
        }

        btnNetSetting.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //?????????
        btnNetSetting.getPaint().setAntiAlias(true);//?????????

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
        cbRoute.setText(TextUtils.isEmpty(locatIp) ? "??????????????????" : locatIp);
        if (TextUtils.isEmpty(locatIp)) {
            systemSetting.setAddRoute(false);
        }
        cbRoute.setChecked(systemSetting.isAddRoute());

        txtChannel.setText((SerialConfigs.sProChannels.get(machineCode) + systemSetting.getHostId() - 1) + "");

        editCustomChannel.setText(systemSetting.getChannel() + "");
        cbCustomChannel.setChecked(systemSetting.isCustomChannel());
        sw_group_check.setChecked(systemSetting.isGroupCheck());
        cbMonitoring.setChecked(systemSetting.isBindMonitoring());
        cbThermometer.setChecked(systemSetting.isStartThermometer());
        cbIsTcp.setChecked(systemSetting.isTCP());
        cbTcpSimultaneous.setChecked(systemSetting.isTCPSimultaneous());
        if (systemSetting.isTCP()) {
            rlTCPSimultaneous.setVisibility(View.VISIBLE);
        }

        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_YWQZ || TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_SGBQS) {
            txtHostHint.setVisibility(View.VISIBLE);
        }

        spAfr.setSelection(systemSetting.getAfrContrast());

        setDeviceVersion();
    }

    /**
     * ????????????????????????
     */
    public void setDeviceVersion() {

        RadioManager.getInstance().setOnRadioArrived(new RadioManager.OnRadioArrivedListener() {
            @Override
            public void onRadioArrived(Message msg) {

                if (msg.what == SerialConfigs.CONVERTER_VERSION_RESPONSE) {
                    final ConverterVersion ver = (ConverterVersion) msg.obj;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            llDeviceVersion.setVisibility(View.VISIBLE);
                            txtDeviceVersion.setText(ver.getVersionCode());
                        }
                    });
                }
            }
        });
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.CONVERTER, SerialConfigs.CMD_GET_CONVERTER_VERSION));
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("??????");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEtTestName.setText(systemSetting.getTestName());
    }


    @OnItemSelected({R.id.sp_host_id, R.id.sp_check_tool, R.id.sp_pattern, R.id.sp_qr_length, R.id.sp_print_tool, R.id.sp_afr})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_host_id:
                systemSetting.setHostId(position + 1);
                txtChannel.setText((SerialConfigs.sProChannels.get(machineCode) + systemSetting.getHostId() - 1) + "");
                break;
            case R.id.sp_pattern:
                if (position == SystemSetting.PERSON_PATTERN) {
                    systemSetting.setTestPattern(SystemSetting.PERSON_PATTERN);
                    llPrintTool.setVisibility(View.GONE);
                    rl_group_check.setVisibility(View.GONE);
                } else {
                    systemSetting.setTestPattern(SystemSetting.GROUP_PATTERN);
                    llPrintTool.setVisibility(View.VISIBLE);
                    rl_group_check.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.sp_qr_length:
                systemSetting.setQrLength(position);
                break;
            case R.id.sp_check_tool:
                systemSetting.setCheckTool(position);
                if (position == 4) {
                    llAfr.setVisibility(View.VISIBLE);
                    boolean isEngine = ConfigUtil.getISEngine(this);
                    if (!isEngine) {
                        ToastUtils.showShort("????????????????????????????????????");
                    }else{
                        FaceServer.getInstance().init(SettingActivity.this);
                    }
                } else {
                    llAfr.setVisibility(View.GONE);
                }
                break;
            case R.id.sp_print_tool:
                if (position == 0) {
                    btnPrintSetting.setVisibility(View.INVISIBLE);
                } else {
                    btnPrintSetting.setVisibility(View.VISIBLE);
                }
                systemSetting.setPrintTool(position);
                break;
            case R.id.sp_afr:
                systemSetting.setAfrContrast(position);
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
                            FaceServer.getInstance().init(SettingActivity.this);
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            ToastUtils.showShort(getString(R.string.already_activated));
                            ConfigUtil.setISEngine(SettingActivity.this, true);
                            FaceServer.getInstance().init(SettingActivity.this);
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
        new HttpSubscriber().activate(runTime, 1, new OnResultListener<ActivateBean>() {
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

            @Override
            public void onResponseTime(String responseTime) {

            }
        });
    }

    @OnClick({R.id.btn_face_init})
    public void onClickFaceInit() {
        activeFace(false);
    }


    @OnClick({R.id.sw_auto_broadcast, R.id.sw_rt_upload, R.id.sw_auto_print, R.id.btn_bind, R.id.btn_default, R.id.btn_net_setting, R.id.btn_tcp_test
            , R.id.txt_advanced, R.id.sw_identity_mark, R.id.sw_add_student, R.id.cb_route, R.id.cb_custom_channel, R.id.cb_monitoring, R.id.btn_account_setting,
            R.id.btn_monitoring_setting, R.id.btn_thermometer, R.id.cb_thermometer, R.id.cb_is_tcp, R.id.sw_auto_score, R.id.btn_print_setting, R.id.sw_auto_discern
            , R.id.btn_voice_setting, R.id.cb_tcp_simultaneous, R.id.sw_group_check})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_tcp_test:
                testTcpConnect();
                break;
            case R.id.cb_is_tcp://????????????TCP
                systemSetting.setTCP(cbIsTcp.isChecked());
                if (systemSetting.isTCP()) {
                    rlTCPSimultaneous.setVisibility(View.VISIBLE);
                } else {
                    rlTCPSimultaneous.setVisibility(View.GONE);
                }
                break;
            case R.id.cb_tcp_simultaneous:
                systemSetting.setTCPSimultaneous(cbTcpSimultaneous.isChecked());
                break;
            case R.id.btn_thermometer://?????????????????????
                IntentUtil.gotoActivity(this, BlueToothListActivity.class);
                break;
            case R.id.cb_thermometer:
                systemSetting.setStartThermometer(cbThermometer.isChecked());
                break;
            case R.id.cb_monitoring: //????????????
                systemSetting.setBindMonitoring(cbMonitoring.isChecked());
                break;
            case R.id.btn_monitoring_setting://????????????
                IntentUtil.gotoActivity(this, MonitoringBindActivity.class);
                break;
            case R.id.sw_auto_broadcast://????????????
                systemSetting.setAutoBroadcast(mSwAutoBroadcast.isChecked());
                break;
            case R.id.sw_rt_upload://????????????
                systemSetting.setRtUpload(mSwRtUpload.isChecked());
                break;

            case R.id.sw_auto_print://????????????
                systemSetting.setAutoPrint(mSwAutoPrint.isChecked());
                break;

            case R.id.sw_identity_mark:
                systemSetting.setIdentityMark(mSwIdentityMark.isChecked());
                break;
            case R.id.sw_add_student://????????????
                systemSetting.setTemporaryAddStu(mSwAddStudent.isChecked());
                break;
            case R.id.sw_auto_score:
                systemSetting.setAutoScore(mSwAutoScore.isChecked());
                break;
            case R.id.cb_custom_channel://???????????????
                if (TextUtils.isEmpty(editCustomChannel.getText().toString())) {
                    ToastUtils.showShort("????????????????????????");
                    cbCustomChannel.setChecked(false);
                    return;
                } else if (Integer.valueOf(editCustomChannel.getText().toString()) == 0 ||
                        Integer.valueOf(editCustomChannel.getText().toString()) > 140) {
                    ToastUtils.showShort("????????????????????????????????????");
                    cbCustomChannel.setChecked(false);
                    return;
                }
                systemSetting.setCustomChannel(cbCustomChannel.isChecked());
                break;
            case R.id.btn_bind://??????
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
//                startActivity(new Intent(this, AdvancedPwdActivity.class));
                showAdvancedPwdDialog();
                break;
            case R.id.cb_route:
                if (TextUtils.isEmpty(NetWorkUtils.getLocalIp())) {
                    ToastUtils.showShort("??????????????????????????????????????????");
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
            case R.id.btn_print_setting://???????????????
                IntentUtil.gotoActivity(this, PrintSettingActivity.class);
                break;
            case R.id.sw_auto_discern:
                systemSetting.setNetCheckTool(sw_auto_discern.isChecked());
                break;
            case R.id.btn_account_setting://????????????
                IntentUtil.gotoActivity(this, AccountSettingActivity.class);
                break;
            case R.id.btn_voice_setting://????????????
                if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_TS) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("mode3", true);
                    IntentUtil.gotoActivity(this, VoiceSettingActivity.class, bundle);
                } else {
                    IntentUtil.gotoActivity(this, VoiceSettingActivity.class);
                }

                break;
            case R.id.sw_group_check:  //????????????
                systemSetting.setGroupCheck(sw_group_check.isChecked());
                break;
        }

    }

    /**
     * ??????
     */
    private void gotoLogin() {
        String url = mEtSeverIp.getText().toString().trim() + "/app/";
        if (!url.startsWith("http")) {//??????IP
            url = "http://" + url;
        }
        if (!NetWorkUtils.isValidUrl(url)) {
            toastSpeak("????????????????????????");
            return;
        }
        SettingHelper.updateSettingCache(systemSetting);
        HttpManager.resetManager();
        startActivity(new Intent(this, LoginActivity.class));
    }


    private void testTcpConnect() {
        if (tcpClientThread == null) {
            String ipStr = null;
            String portStr = null;
            try {
                String tcpIp = systemSetting.getTcpIp();
                ipStr = tcpIp.split(":")[0];
                portStr = tcpIp.split(":")[1];
            } catch (Exception e) {
                ToastUtils.showShort("??????????????????TCP??????");
                return;
            }

            tcpClientThread = new SendTcpClientThread(ipStr, Integer.parseInt(portStr), new SendTcpClientThread.SendTcpListener() {
                @Override
                public void onMsgReceive(String text) {

                }

                @Override
                public void onSendFail(final String msg) {

                }

                @Override
                public void onConnectFlag(boolean isConnect) {
                    Log.e("onConnectFlag", "---------" + isConnect);
                    if (isConnect) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort("?????????????????????");
                            }
                        });

                    } else {
                        tcpClientThread.exit = true;
                        tcpClientThread = null;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort("?????????????????????");
                            }
                        });

                    }
                }
            });
            tcpClientThread.start();
        } else {
            if (tcpClientThread.isInterrupted()) {
                tcpClientThread.start();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (tcpClientThread != null) {
            tcpClientThread.exit = true;
            tcpClientThread = null;
        }
        if (!TextUtils.isEmpty(editCustomChannel.getText().toString().trim())) {
            systemSetting.setChannel(Integer.valueOf(editCustomChannel.getText().toString().trim()));
        }
        RadioChannelCommand command = new RadioChannelCommand(systemSetting.getUseChannel());
        LogUtils.serial("????????????" + StringUtility.bytesToHexString(command.getCommand()) + "---");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command));

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
        systemSetting.setTcpIp(mEtTcpIp.getText().toString().trim());
    }

    public void showAdvancedPwdDialog() {

        View view = LayoutInflater.from(this).inflate(R.layout.view_advanced_pwd, null);
        final EditText editPwd = view.findViewById(R.id.edit_pwd);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);

        final Dialog dialog = DialogUtils.create(this, view, true);
        dialog.show();
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.equals(editPwd.getText().toString(), MyApplication.ADVANCED_PWD)) {
                    startActivity(new Intent(SettingActivity.this, AdvancedSettingActivity.class));
                    dialog.dismiss();
                } else {
                    ToastUtils.showShort("????????????");
                }
            }
        });
    }


}
