package com.feipulai.exam.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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
import com.feipulai.common.utils.NetWorkUtils;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SystemBrightUtils;
import com.feipulai.common.view.baseToolbar.StatusBarUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.udp.UdpLEDUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.MiddleDistanceRaceForGroupActivity;
import com.feipulai.exam.activity.MiddleDistanceRace.MiddleDistanceRaceForPersonActivity;
import com.feipulai.exam.activity.MiddleDistanceRace.MyTcpService;
import com.feipulai.exam.activity.base.BaseActivity;
import com.feipulai.exam.activity.base.BaseGroupActivity;
import com.feipulai.exam.activity.data.DataManageActivity;
import com.feipulai.exam.activity.data.DataRetrieveActivity;
import com.feipulai.exam.activity.data.print.PrintPreviewActivity;
import com.feipulai.exam.activity.explain.ExplainActivity;
import com.feipulai.exam.activity.setting.MonitoringBean;
import com.feipulai.exam.activity.setting.MonitoringBindActivity;
import com.feipulai.exam.activity.setting.SettingActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.netUtils.CommonUtils;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.service.UploadService;
import com.ww.fpl.libarcface.faceserver.FaceServer;
import com.ww.fpl.videolibrary.StorageUtils;
import com.ww.fpl.videolibrary.play.util.PUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity/* implements DialogInterface.OnClickListener */ {
    @BindView(R.id.img_code)
    ImageView imgCode;
    @BindView(R.id.txt_main_title)
    TextView txtMainTitle;
    @BindView(R.id.txt_deviceid)
    TextView txtDeviceId;
    private boolean mIsExiting;
    private Intent serverIntent;
    private Intent bindIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        StatusBarUtil.setImmersiveTransparentStatusBar(this);//设置沉浸式透明状态栏 配合使用

        //配置网络
        if (SettingHelper.getSystemSetting().isAddRoute() && !TextUtils.isEmpty(NetWorkUtils.getLocalIp())) {
            String locatIp = NetWorkUtils.getLocalIp();
            String routeIp = locatIp.substring(0, locatIp.lastIndexOf("."));
            UdpLEDUtil.shellExec("ip route add " + routeIp + ".0/24 dev eth0 proto static scope link table wlan0 \n");
        }
        //测试数据
//        List<GroupItem> items = DBManager.getInstance().queryGroupItemByCode("11");
//        List<RoundResult> roundResults = new ArrayList<>();
//        RoundResult roundResult;
//        int countI = 0;
//        for (GroupItem groupItem : items
//                ) {
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
        //所有界面在此关闭tcp上传线程（待测试）
        if (ServerMessage.subscriber0 != null) {
            ServerMessage.subscriber0.stopSendTcpThread();
        }
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
        roundResult.setStudentCode("193012100030");
        String itemCode = TestConfigs.sCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : TestConfigs.sCurrentItem.getItemCode();
        roundResult.setItemCode(itemCode);
        roundResult.setResult(122);
        roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
        roundResult.setTestTime(DateUtil.getCurrentTime() + "");
        roundResult.setRoundNo(1);
        roundResult.setTestNo(1);
        roundResult.setExamType(0);
        roundResult.setUpdateState(0);
        roundResult.setIsLastResult(1);
        roundResult.setScheduleNo("1");
        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
        roundResult.setIsLastResult(1);
        DBManager.getInstance().insertRoundResult(roundResult);

//        List<RoundResult> results = new ArrayList<>();
//        results.add(roundResult);
//        UploadResults uploadResults = new UploadResults("1", TestConfigs.getCurrentItemCode(),
//                "193012100002", 1 + "", null, RoundResultBean.beanCope(results));
//        uploadResult(uploadResults);
    }

    /**
     * 成绩上传
     *
     * @param uploadResults 上传成绩
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
        StringBuilder sb = new StringBuilder("智能主机(考试版V" + SystemBrightUtils.getCurrentVersion(this) + ")");

        if (machineCode != SharedPrefsConfigs.DEFAULT_MACHINE_CODE) {
//            if (TestConfigs.sCurrentItem != null && TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
//                sb.append("-").append(TestConfigs.sCurrentItem.getItemName())
//                        .append(systemSetting.getHostId()).append("号机");
//            } else {
            sb.append("-").append(TestConfigs.machineNameMap.get(machineCode))
                    .append(systemSetting.getHostId()).append("号机");
//            }
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
            toastSpeak("请先选择测试项目");
            // 跳到设置界面
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
                PrinterManager.getInstance().init();
                PrinterManager.getInstance().selfCheck();
                PrinterManager.getInstance().print("\n\n");
//                addTestResult();
//                IntentUtil.gotoActivity(this,PrintPreviewActivity.class);
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
                startActivity(new Intent(MainActivity.this, LEDSettingActivity.class));
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
            Intent tcpServiceIntent = new Intent(this, MyTcpService.class);
            stopService(tcpServiceIntent);
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
            Toast.makeText(this, "再按一次后退键退出程序", Toast.LENGTH_SHORT).show();
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
            new AlertDialog.Builder(this).setTitle(monitoringArray.length == 0 ? "当前无绑定监控设备" : "当前绑定的监控设备")
                    .setItems(monitoringArray, null)
                    .setPositiveButton("去测试", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            gotoTestActivity();
                        }
                    })
                    .setNegativeButton("去绑定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            IntentUtil.gotoActivity(MainActivity.this, MonitoringBindActivity.class);
                        }
                    })
                    .show();
        }
    }
}
