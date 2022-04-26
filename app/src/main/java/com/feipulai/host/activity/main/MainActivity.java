package com.feipulai.host.activity.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.feipulai.common.utils.ActivityCollector;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SystemBrightUtils;
import com.feipulai.common.view.baseToolbar.StatusBarUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.activity.data.DataManageActivity;
import com.feipulai.host.activity.data.DataRetrieveActivity;
import com.feipulai.host.activity.explain.ExplainActivity;
import com.feipulai.host.activity.radio_timer.RunTimerSelectActivity;
import com.feipulai.host.activity.setting.LEDSettingActivity;
import com.feipulai.host.activity.setting.SettingActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.setting.SystemSetting;
import com.feipulai.host.bean.ActivateBean;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.CommonUtils;
import com.feipulai.host.netUtils.HttpSubscriber;
import com.feipulai.host.netUtils.OnResultListener;
import com.feipulai.host.netUtils.netapi.UserSubscriber;
import com.feipulai.host.utils.TimerUtil;
import com.feipulai.host.view.BatteryView;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.ww.fpl.libarcface.faceserver.FaceServer;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by zzs on 2018/7/19
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MainActivity extends BaseActivity {

    @BindView(R.id.img_code)
    ImageView imgCode;
    @BindView(R.id.txt_main_title)
    TextView txtMainTitle;
    @BindView(R.id.txt_deviceid)
    TextView txtDeviceId;
    @BindView(R.id.view_battery)
    BatteryView batteryView;
    private boolean mIsExiting;
    private Intent serverIntent;


    @BindView(R.id.txt_cut_time)
    TextView txtCutTime;
    @BindView(R.id.txt_use_time)
    TextView txtUseTime;
    private ActivateBean activateBean;
    private SweetAlertDialog activateDialog;
    private LEDManager ledManager = new LEDManager();
    private TimerUtil timerUtil = new TimerUtil(new TimerUtil.TimerAccepListener() {
        @Override
        public void timer(Long time) {

            long todayTime = SharedPrefsUtil.getValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.APP_USE_TIME, 0l);
            if (activateBean == null || activateBean.getValidEndTime() == 0 || activateBean.getValidRunTime() == 0) {
                activateBean = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), ActivateBean.class);
            }
            SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.APP_USE_TIME, todayTime + 60 * 1000);
            if (activateBean.getValidRunTime() != 0 && activateBean.getValidEndTime() != 0
                    && (activateBean.getValidEndTime() - DateUtil.getCurrentTime() <= 3 * 24 * 60 * 60 * 1000 ||
                    activateBean.getValidRunTime() - todayTime <= 24 * 60 * 60 * 1000)) {
                txtCutTime.setVisibility(View.VISIBLE);
                txtCutTime.setText("截止时间：" + DateUtil.formatTime1(activateBean.getValidEndTime(), "yyyy年MM月dd日"));
                if (activateBean.getValidEndTime() - DateUtil.getCurrentTime() <= 3 * 24 * 60 * 60 * 1000) {
                    txtUseTime.setVisibility(View.VISIBLE);
                    txtUseTime.setText("可用时长：" + DateUtil.getUseTime(activateBean.getValidRunTime() - todayTime));
                }
            } else {
                txtCutTime.setVisibility(View.GONE);
                txtCutTime.setVisibility(View.GONE);
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        RadioManager.getInstance().init();
        StatusBarUtil.setImmersiveTransparentStatusBar(this);//设置沉浸式透明状态栏 配合使用
        timerUtil.startTime(60, TimeUnit.SECONDS);
        activateBean = SharedPrefsUtil.loadFormSource(this, ActivateBean.class);
        RadioManager.getInstance().setOnKwhListener(new RadioManager.OnKwhListener() {
            @Override
            public void onKwhArrived(final Message msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int state = msg.arg1;
                        int level = msg.arg2;
                        batteryView.setVisibility(View.VISIBLE);
                        batteryView.updateState(level);
                        if (state == 0) {//放电
                            batteryView.updateView(level);
                        } else {//充电
                            batteryView.updateChargingView(level);
                        }
                    }
                });

            }
        });
    }

    private boolean isSettingFinished() {
        if (TestConfigs.sCurrentItem == null) {
            toastSpeak(getString(R.string.please_select_item));
            startActivity(new Intent(MainActivity.this, MachineSelectActivity.class));
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        machineCode = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, SharedPrefsConfigs
                .DEFAULT_MACHINE_CODE);
        String itemCode = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, null);
        // Logger.i("machineCode:" + machineCode);
        int initState = TestConfigs.init(this, machineCode, itemCode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showTestName();
            }
        });
        try{
            showTestName();
        }catch (Exception e){
            toastSpeak("项目选择错误,请重选");
        }
        if (initState != TestConfigs.INIT_NO_MACHINE_CODE) {
            MachineCode.machineCode = machineCode;
        }
    }

    private void showTestName() {
        SystemSetting systemSetting = SettingHelper.getSystemSetting();

        StringBuilder sb = new StringBuilder(String.format(systemSetting.isFreedomTest() ? getString(R.string.versions_name_2)
                : getString(R.string.versions_name), SystemBrightUtils.getCurrentVersion(this)));

        if (TestConfigs.sCurrentItem != null && machineCode != SharedPrefsConfigs.DEFAULT_MACHINE_CODE) {
            sb.append("-").append(
                    String.format(getString(R.string.host_name), TestConfigs.machineNameMap.get(machineCode), systemSetting.getHostId()));
            String title = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode())
                    + " " + systemSetting.getHostId();
            if (SettingHelper.getSystemSetting().getLedVersion() == 0) {
                ledManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), systemSetting.getHostId(), 1);

                ledManager.showSubsetString(systemSetting.getHostId(), 1, title, 0, true, false, LEDManager.MIDDLE);
                ledManager.showSubsetString(systemSetting.getHostId(), 1, "菲普莱体育", 3, 3, false, true);
            } else {
                ledManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), systemSetting.getHostId());

                ledManager.showString(systemSetting.getHostId(), title, 0, true, false, LEDManager.MIDDLE);
                ledManager.showString(systemSetting.getHostId(), "菲普莱体育", 3, 3, false, true);
            }
        }
        if (!TextUtils.isEmpty(systemSetting.getTestName())) {
            sb.append("-").append(systemSetting.getTestName());
        }
        if (TestConfigs.sCurrentItem != null) {
            sb.append(" [ F - " + SettingHelper.getSystemSetting().getUseChannel() + " ]");
        }
        txtMainTitle.setText(sb.toString());
        txtDeviceId.setText(CommonUtils.getDeviceId(this));
    }

    @OnClick(R.id.txt_help)
    public void onViewClicked() {
        IntentUtil.gotoActivity(this, ExplainActivity.class);
    }

    @OnClick({R.id.card_test, R.id.card_select, R.id.card_print, R.id.card_parameter_setting, R.id.card_data_admin, R.id.card_system, R.id.card_led, R.id.card_device_cut})
    public void onViewClicked(View view) {
        if (!isSettingFinished()) {
            return;
        }
        switch (view.getId()) {
            case R.id.card_test:
                if (isSettingFinished()) {
                    if (SettingHelper.getSystemSetting().isFreedomTest()) {
                        IntentUtil.gotoActivity(MainActivity.this, TestConfigs.freedomActivity.get(TestConfigs.sCurrentItem.getMachineCode()));
                    } else {
                        IntentUtil.gotoActivity(MainActivity.this, TestConfigs.proActivity.get(TestConfigs.sCurrentItem.getMachineCode()));
                    }
                }
                break;
            case R.id.card_select:
                if (SettingHelper.getSystemSetting().isFreedomTest()) {
                    toastSpeak(getString(R.string.freedom_operation_hint));
                } else {
                    IntentUtil.gotoActivity(MainActivity.this, DataRetrieveActivity.class);
                }

                break;
            case R.id.card_print:
//                DBManager.getInstance().deleteAllStudent();
                PrinterManager.getInstance().init();
                PrinterManager.getInstance().selfCheck();
                PrinterManager.getInstance().print("\n\n");
//                addTestResult();
//                List<RoundResult> results = DBManager.getInstance().getResultsListAll();
//                for (RoundResult result : results) {
//                    result.setResultState(RoundResult.RESULT_STATE_NORMAL);
//                }
//                DBManager.getInstance().updateRoundResult(results);

                break;
            case R.id.card_parameter_setting:
                IntentUtil.gotoActivity(MainActivity.this, SettingActivity.class);
                break;
            case R.id.card_data_admin:
                if (SettingHelper.getSystemSetting().isFreedomTest()) {
                    toastSpeak(getString(R.string.freedom_operation_hint));
                } else {
                    IntentUtil.gotoActivity(MainActivity.this, DataManageActivity.class);
                }

                break;
            case R.id.card_system:
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                break;
            case R.id.card_led:
                if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZFP) {
                    Intent intent = new Intent(MainActivity.this, RunTimerSelectActivity.class);
                    intent.putExtra(RunTimerSelectActivity.GOTO_FLAG, 11);
                    startActivity(intent);
                } else {
                    startActivity(new Intent(MainActivity.this, LEDSettingActivity.class));
                }
                break;
            case R.id.card_device_cut:
                IntentUtil.gotoActivity(this, MachineSelectActivity.class);
                break;

        }
    }

    @OnClick(R.id.img_code)
    public void onCodeClicked(View view) {
        if (imgCode.getHeight() <= 55) {
            imgCode.setImageResource(R.mipmap.icon_code_big);
        } else {
            imgCode.setImageResource(R.mipmap.icon_code);
        }

    }

    @Override
    public void onBackPressed() {
        exit();

    }

    @Override
    protected void onDestroy() {
        RadioManager.getInstance().close();
        super.onDestroy();
        if (mIsExiting) {
            FaceServer.getInstance().unInit();
            ActivityCollector.getInstance().finishAllActivity();
            System.exit(0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        } else { // 如果不是back键正常响应
            return super.onKeyDown(keyCode, event);
        }
    }

    private long clickTime = 0; // 第一次点击的时间

    private void exit() {
        if ((System.currentTimeMillis() - clickTime) > 2000) {
            Toast.makeText(this, getText(R.string.exit_hint), Toast.LENGTH_SHORT).show();
            clickTime = System.currentTimeMillis();
        } else {
            mIsExiting = true;
            this.finish();
        }
    }

    private void addTestResult() {
        List<Student> dbStudentList = DBManager.getInstance().dumpAllStudents();
        for (int i = 0; i < dbStudentList.size(); i++) {
            if (i < 500) {
                Student student = dbStudentList.get(i);
                RoundResult roundResult = new RoundResult();
                roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
                roundResult.setStudentCode(student.getStudentCode());
                String itemCode = TestConfigs.sCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : TestConfigs.sCurrentItem.getItemCode();
                roundResult.setItemCode(itemCode);
                roundResult.setResult(1980 + i);
                roundResult.setResultState(0);
                roundResult.setTestTime(DateUtil.getCurrentTime() + "");
                roundResult.setRoundNo(i + 1);
                roundResult.setTestNo(1);
                roundResult.setUpdateState(0);
                roundResult.setIsLastResult(1);
                DBManager.getInstance().insertRoundResult(roundResult);
            } else {
                return;
            }


        }
    }

    private void activate() {
        final long runTime = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.APP_USE_TIME, 0L);
        new UserSubscriber().activate(runTime,0, new OnResultListener<ActivateBean>() {
            @Override
            public void onSuccess(ActivateBean result) {

                activateBean = result;
                if (activateBean.getFaceSdkKeyList() != null) {
                    activateBean.setFaceSdkKeyJson(new Gson().toJson(result.getFaceSdkKeyList()));
                }
                SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.APP_USE_TIME, result.getCurrentRunTime());
                SharedPrefsUtil.save(MainActivity.this, activateBean);
                if (result.getCurrentTime() > result.getValidEndTime()) {
                    LogUtil.logDebugMessage(result.getCurrentTime() + "-----" + result.getValidEndTime());
                    //超出使用时间 重新激活
                    showActivateConfirm();
                    return;
                } else if (runTime > result.getValidRunTime()) {
                    //超出使用时长
                    //弹窗确定重新激活
                    showActivateConfirm();
                    return;
                }
                if (activateDialog != null && activateDialog.isShowing()) {
                    activateDialog.dismissWithAnimation();
                }
            }

            @Override
            public void onFault(int code, String errorMsg) {

            }

        });
    }

    private void showActivateConfirm() {

        if (activateDialog != null && activateDialog.isShowing()) {
            return;
        }
        activateDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText("激活设备")

                .setContentText("已超出可使用时长\n请联系管理员重新激活设备" + "\n" + CommonUtils.getDeviceId(this))
                .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
//                        sweetAlertDialog.dismissWithAnimation();
//                        activateDialog = null;
                        activate();
                    }
                }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        activateDialog = null;
                        finish();
                    }
                });
        activateDialog.setCanceledOnTouchOutside(false);
        activateDialog.show();
    }
}
