package com.feipulai.exam.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.feipulai.common.utils.ActivityCollector;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.NetWorkUtils;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SystemBrightUtils;
import com.feipulai.common.view.baseToolbar.StatusBarUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialParams;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.device.udp.UdpLEDUtil;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.MiddleDistanceRaceForGroupActivity;
import com.feipulai.exam.activity.MiddleDistanceRace.MiddleDistanceRaceForPersonActivity;
import com.feipulai.exam.activity.MiddleDistanceRace.MyTcpService;
import com.feipulai.exam.activity.RadioTimer.RunTimerSelectActivity;
import com.feipulai.exam.activity.base.BaseActivity;
import com.feipulai.exam.activity.base.BaseGroupActivity;
import com.feipulai.exam.activity.basketball.util.TimerUtil;
import com.feipulai.exam.activity.data.DataManageActivity;
import com.feipulai.exam.activity.data.DataRetrieveActivity;
import com.feipulai.exam.activity.explain.ExplainActivity;
import com.feipulai.exam.activity.setting.MonitoringBean;
import com.feipulai.exam.activity.setting.MonitoringBindActivity;
import com.feipulai.exam.activity.setting.SettingActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.bean.ActivateBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.netUtils.CommonUtils;
import com.feipulai.exam.netUtils.OnResultListener;
import com.feipulai.exam.netUtils.netapi.HttpSubscriber;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.service.UploadService;
import com.feipulai.exam.tcp.FileUtils;
import com.feipulai.exam.view.BatteryView;
import com.google.gson.Gson;
import com.orhanobut.logger.utils.LogUtils;
import com.ww.fpl.libarcface.faceserver.FaceServer;
import com.ww.fpl.videolibrary.StorageUtils;
import com.ww.fpl.videolibrary.play.util.PUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends BaseActivity/* implements DialogInterface.OnClickListener */ {
    @BindView(R.id.img_code)
    ImageView imgCode;
    @BindView(R.id.txt_main_title)
    TextView txtMainTitle;
    @BindView(R.id.txt_deviceid)
    TextView txtDeviceId;
    @BindView(R.id.txt_cut_time)
    TextView txtCutTime;
    @BindView(R.id.txt_use_time)
    TextView txtUseTime;
    @BindView(R.id.view_battery)
    BatteryView batteryView;
    private boolean mIsExiting;
    private Intent serverIntent;
    private Intent bindIntent;
    private ActivateBean activateBean;
    private SweetAlertDialog activateDialog;
    private LEDManager ledManager = new LEDManager();
    private TimerUtil timerUtil = new TimerUtil(new TimerUtil.TimerAccepListener() {
        @Override
        public void timer(Long time) {
            long todayTime = SharedPrefsUtil.getValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.APP_USE_TIME, 0l);

            SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.APP_USE_TIME, todayTime + 60 * 1000);
            if (activateBean == null || activateBean.getValidEndTime() == 0 || activateBean.getValidRunTime() == 0) {
                activateBean = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), ActivateBean.class);
            }

            if (activateBean.getValidRunTime() != 0 && activateBean.getValidEndTime() != 0 &&
                    (activateBean.getValidEndTime() - DateUtil.getCurrentTime() <= 3 * 24 * 60 * 60 * 1000 ||
                            activateBean.getValidRunTime() - todayTime <= 24 * 60 * 60 * 1000)) {
                txtCutTime.setVisibility(View.VISIBLE);
                txtCutTime.setText("???????????????" + DateUtil.formatTime1(activateBean.getValidEndTime(), "yyyy???MM???dd???"));
                if (activateBean.getValidEndTime() - DateUtil.getCurrentTime() <= 3 * 24 * 60 * 60 * 1000) {
                    txtUseTime.setVisibility(View.VISIBLE);
                    txtUseTime.setText("???????????????" + DateUtil.getUseTime(activateBean.getValidRunTime() - todayTime));
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        StatusBarUtil.setImmersiveTransparentStatusBar(this);//?????????????????????????????? ????????????

        //????????????
        if (SettingHelper.getSystemSetting().isAddRoute() && !TextUtils.isEmpty(NetWorkUtils.getLocalIp())) {
            String locatIp = NetWorkUtils.getLocalIp();
            String routeIp = locatIp.substring(0, locatIp.lastIndexOf("."));
            UdpLEDUtil.shellExec("ip route add " + routeIp + ".0/24 dev eth0 proto static scope link table wlan0 \n");
        }

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
                        if (state == 0) {//??????
                            batteryView.updateView(level);
                        } else {//??????
                            batteryView.updateChargingView(level);
                        }
                    }
                });

            }
        });
        timerUtil.startTime(60, TimeUnit.SECONDS);
        activate();
//        ????????????
//        List<GroupItem> items = DBManager.getInstance().queryGroupItemByCode("11");
//        List<RoundResult> roundResults = new ArrayList<>();
//        RoundResult roundResult;
//        int countI = 0;
//        for (GroupItem groupItem : items
//        ) {
//            Group group = DBManager.getInstance().queryGroup("11", 1);
//            roundResult = new RoundResult();
//            roundResult.setGroupId(group.getId());
//            roundResult.setIsLastResult(1);
//            roundResult.setItemCode("11");
//            roundResult.setMachineCode(3);
//            roundResult.setMachineResult(1130 + countI * 100);
//            roundResult.setResult(1130 + countI * 100);
//            roundResult.setResultState(1);
//            roundResult.setRoundNo(1);
//            roundResult.setScheduleNo("1");
//            roundResult.setStudentCode(groupItem.getStudentCode());
//            roundResult.setTestNo(1);
//            roundResult.setTestTime(System.currentTimeMillis() + "");
//            roundResults.add(roundResult);
//            countI++;
//        }
//        roundResults.get(2).setResultState(2);
//        DBManager.getInstance().insertRoundResults(roundResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            LEDManager.versions = SettingHelper.getSystemSetting().getLedVersion();
            machineCode = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, SharedPrefsConfigs
                    .DEFAULT_MACHINE_CODE);
            String itemCode = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, null);
            int initState = TestConfigs.init(this, machineCode, itemCode, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showTestName();
                }
            });
            showTestName();
            if (initState != TestConfigs.INIT_NO_MACHINE_CODE) {
                MachineCode.machineCode = machineCode;
            }
            //????????????????????????tcp???????????????????????????
            if (ServerMessage.subscriber0 != null) {
                ServerMessage.subscriber0.stopSendTcpThread();
            }
        } catch (Exception e) {
            Log.i("wrong", e.getMessage());
            toastSpeak("??????????????????,?????????");
        }
        RadioChannelCommand command = new RadioChannelCommand(SettingHelper.getSystemSetting().getUseChannel());
        LogUtils.serial("????????????" + StringUtility.bytesToHexString(command.getCommand()) + "---");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command));
//        testUpload();

//        createFile();
    }

    public String PATH = Environment.getExternalStorageDirectory() + "/HKVideo/";

    private void createFile() {
        ArrayList<StorageUtils.Volume> storys = StorageUtils.getVolume(this);
        for (StorageUtils.Volume volume : storys
        ) {
            if (volume.isRemovable() && volume.getState().equals("mounted")) {
                PATH = volume.getPath() + "/HKVideo/";
                break;
            }
        }
        Log.i("PATH", "1--->" + PATH);
        if (!PUtil.createFile(PATH)) {
            PATH = Environment.getExternalStorageDirectory() + "/HKVideo/";
        }
        Log.i("PATH", "2--->" + PATH);
    }


    private void addTestResult() {
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode("0111960128");
        String itemCode = TestConfigs.sCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : TestConfigs.sCurrentItem.getItemCode();
        roundResult.setItemCode(itemCode);
        roundResult.setResult(54500);
        roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
        roundResult.setTestTime("1621038454000");
        roundResult.setEndTime("1621038499500");
        roundResult.setRoundNo(1);
        roundResult.setTestNo(1);
        roundResult.setExamType(0);
        roundResult.setUpdateState(0);
        roundResult.setIsLastResult(1);
        roundResult.setScheduleNo("1");
        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
        DBManager.getInstance().insertRoundResult(roundResult);
        roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode("0111960128");
        roundResult.setItemCode(itemCode);
        roundResult.setResult(54700);
        roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
        roundResult.setTestTime("1621039570000");
        roundResult.setEndTime("1621039624700");
        roundResult.setRoundNo(2);
        roundResult.setTestNo(1);
        roundResult.setExamType(0);
        roundResult.setUpdateState(0);
        roundResult.setIsLastResult(0);
        roundResult.setScheduleNo("1");
        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
        DBManager.getInstance().insertRoundResult(roundResult);

//        List<RoundResult> results = new ArrayList<>();
//        results.add(roundResult);
//        UploadResults uploadResults = new UploadResults("1", TestConfigs.getCurrentItemCode(),
//                "193012100002", 1 + "", null, RoundResultBean.beanCope(results));
//        uploadResult(uploadResults);
    }

    /**
     * ????????????
     *
     * @param uploadResults ????????????
     */
    private void uploadResult(UploadResults uploadResults) {
        Intent serverIntent = new Intent(this, UploadService.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(UploadResults.BEAN_KEY, uploadResults);
        serverIntent.putExtras(bundle);
        startService(serverIntent);
    }

    private void showTestName() {
        SystemSetting systemSetting = SettingHelper.getSystemSetting();
        StringBuilder sb = new StringBuilder("????????????(?????????V" + SystemBrightUtils.getCurrentVersion(this) + ")");

        if (machineCode != SharedPrefsConfigs.DEFAULT_MACHINE_CODE) {
//            if (TestConfigs.sCurrentItem != null && TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
//                sb.append("-").append(TestConfigs.sCurrentItem.getItemName())
//                        .append(systemSetting.getHostId()).append("??????");
//            } else {
            sb.append("-").append(TestConfigs.machineNameMap.get(machineCode))
                    .append(systemSetting.getHostId()).append("??????");
//            }
            String title = TestConfigs.machineNameMap.get(machineCode)
                    + " " + systemSetting.getHostId();
            if (SettingHelper.getSystemSetting().getLedVersion() == 0) {
                ledManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), systemSetting.getHostId(), 1);

                ledManager.showSubsetString(systemSetting.getHostId(), 1, title, 0, true, false, LEDManager.MIDDLE);
                ledManager.showSubsetString(systemSetting.getHostId(), 1, "???????????????", 3, 3, false, true);
            } else {
                ledManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), systemSetting.getHostId());
                ledManager.ballTimeControl(SettingHelper.getSystemSetting().getHostId(),false,false,false,0,2,false,2);
                ledManager.showString(systemSetting.getHostId(), title, 0, true, false, LEDManager.MIDDLE);
                ledManager.showString(systemSetting.getHostId(), "???????????????", 3, 3, false, true);
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

    private boolean isSettingFinished() {
        if (TestConfigs.sCurrentItem == null) {
            toastSpeak("????????????????????????");
            // ??????????????????
            startActivity(new Intent(MainActivity.this, MachineSelectActivity.class));
            return false;
        }
        return true;
    }

    @OnClick({R.id.card_test, R.id.card_select, R.id.card_print, R.id.card_parameter_setting, R.id.card_data_admin, R.id.card_system, R.id.card_led, R.id.card_device_cut})
    public void onViewClicked(View view) {
        if (!isSettingFinished()) {
            return;
        }
        switch (view.getId()) {
            case R.id.card_test:
                if (SettingHelper.getSystemSetting().isBindMonitoring()) {
                    showBindMonitoringDialog();
                    return;
                }
                gotoTestActivity();
                break;
            case R.id.card_select:
                startActivity(new Intent(MainActivity.this, DataRetrieveActivity.class));
                break;
            case R.id.card_print:
                SerialParams.init(this);
                PrinterManager.getInstance().init();
                PrinterManager.getInstance().selfCheck();
                PrinterManager.getInstance().print("\n\n");

//                LogUtil.logDebugMessage("???????????????" + SystemUtil.getFirmwareVersion());
//                ActivateBean activateBean = SharedPrefsUtil.loadFormSource(this, ActivateBean.class);
//                DateUtil.setSysDate(this, activateBean.getCurrentTime());


//                addTestResult();
//                IntentUtil.gotoActivity(this,PrintPreviewActivity.class);
                //todo ??????????????????
//                List<RoundResult> roundResults = new ArrayList<>();
//                List<Student> studentList = DBManager.getInstance().dumpAllStudents();
//                for (Student student : studentList) {
//                    RoundResult roundResult = new RoundResult();
//                    roundResult.setIsLastResult(1);
//                    roundResult.setItemCode(TestConfigs.getCurrentItemCode());
//                    roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
//                    roundResult.setMachineResult(1130 * 100);
//                    roundResult.setResult(1130 * 100);
//                    roundResult.setResultState(1);
//                    roundResult.setRoundNo(1);
//                    roundResult.setScheduleNo("3");
//                    roundResult.setStudentCode(student.getStudentCode());
//                    roundResult.setTestNo(1);
//                    roundResult.setTestTime(System.currentTimeMillis() + "");
//                    roundResults.add(roundResult);
//                }
//                DBManager.getInstance().insertRoundResults(roundResults);
//                toastSpeak("????????????");
                break;
            case R.id.card_parameter_setting:
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                break;
            case R.id.card_data_admin:
                startActivity(new Intent(MainActivity.this, DataManageActivity.class));
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
                startActivity(new Intent(this, MachineSelectActivity.class));
//                addTestResult();
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
            timerUtil.stop();
            Intent tcpServiceIntent = new Intent(this, MyTcpService.class);
            stopService(tcpServiceIntent);
            ActivityCollector.getInstance().finishAllActivity();
            System.exit(0);

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // ?????????????????????back???
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        } else { // ????????????back???????????????
            return super.onKeyDown(keyCode, event);
        }
    }

    private long clickTime = 0; // ????????????????????????

    private void exit() {
        if ((System.currentTimeMillis() - clickTime) > 2000) {
            Toast.makeText(this, "?????????????????????????????????", Toast.LENGTH_SHORT).show();
            clickTime = System.currentTimeMillis();
        } else {
            mIsExiting = true;
            FaceServer.getInstance().unInit();
            this.finish();
        }
    }


    @OnClick(R.id.txt_help)
    public void onViewClicked() {
        IntentUtil.gotoActivity(this, ExplainActivity.class);
    }


    private void gotoTestActivity() {
        if (isSettingFinished()) {

            if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
                    startActivity(new Intent(MainActivity.this, MiddleDistanceRaceForPersonActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, MiddleDistanceRaceForGroupActivity.class));
                }
                return;
            }
            if (TestConfigs.selectActivity.contains(TestConfigs.sCurrentItem.getMachineCode())) {
                IntentUtil.gotoActivity(this, TestConfigs.proActivity.get(TestConfigs.sCurrentItem.getMachineCode()));
                return;
            }
            if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
                startActivity(new Intent(MainActivity.this, TestConfigs.proActivity.get(TestConfigs.sCurrentItem.getMachineCode())));
            } else {
                startActivity(new Intent(MainActivity.this, BaseGroupActivity.class));
            }
        }
    }

    private void showBindMonitoringDialog() {
        if (SettingHelper.getSystemSetting().isBindMonitoring()) {

            List<MonitoringBean> monitoringBeans = SettingHelper.getSystemSetting().getMonitoringList();
            String[] monitoringArray = new String[monitoringBeans.size()];
            for (int i = 0; i < monitoringBeans.size(); i++) {
                MonitoringBean monitoringBean = SettingHelper.getSystemSetting().getMonitoringList().get(i);
                monitoringArray[i] = "< " + monitoringBean.getMonitoringSerial() + " > - " + monitoringBean.getBindTime();
            }
            new AlertDialog.Builder(this).setTitle(monitoringArray.length == 0 ? "???????????????????????????" : "???????????????????????????")
                    .setItems(monitoringArray, null)
                    .setPositiveButton("?????????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            gotoTestActivity();
                        }
                    })
                    .setNegativeButton("?????????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            IntentUtil.gotoActivity(MainActivity.this, MonitoringBindActivity.class);
                        }
                    })
                    .show();
        }
    }


    private void activate() {
        final long runTime = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.APP_USE_TIME, 0L);
        new HttpSubscriber().activate(runTime, 0, new OnResultListener<ActivateBean>() {
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
                    //?????????????????? ????????????
                    showActivateConfirm();
                    return;
                } else if (runTime > result.getValidRunTime()) {
                    //??????????????????
                    //????????????????????????
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

            @Override
            public void onResponseTime(String responseTime) {

            }
        });
    }

    private void showActivateConfirm() {

        if (activateDialog != null && activateDialog.isShowing()) {
            return;
        }
        activateDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText("????????????")

                .setContentText("????????????????????????\n????????????????????????????????????" + "\n" + CommonUtils.getDeviceId(this))
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
