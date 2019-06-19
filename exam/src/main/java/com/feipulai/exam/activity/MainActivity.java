package com.feipulai.exam.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.NetWorkUtils;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SystemBrightUtils;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.udp.UdpLEDUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.MiddleDistanceRaceActivity;
import com.feipulai.exam.activity.base.BaseActivity;
import com.feipulai.exam.activity.base.BaseGroupActivity;
import com.feipulai.exam.activity.data.DataManageActivity;
import com.feipulai.exam.activity.data.DataRetrieveActivity;
import com.feipulai.exam.activity.setting.SettingActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.view.baseToolbar.StatusBarUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity/* implements DialogInterface.OnClickListener */ {
    @BindView(R.id.img_code)
    ImageView imgCode;
    @BindView(R.id.txt_main_title)
    TextView txtMainTitle;

    private boolean mIsExiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        RadioManager.getInstance().init();
        StatusBarUtil.setImmersiveTransparentStatusBar(this);//设置沉浸式透明状态栏 配合使用

        //配置网络
        if (SettingHelper.getSystemSetting().isAddRoute() && !TextUtils.isEmpty(NetWorkUtils.getLocalIp())) {
            String locatIp = NetWorkUtils.getLocalIp();
            String routeIp = locatIp.substring(0, locatIp.lastIndexOf("."));
            UdpLEDUtil.shellExec("ip route add " + routeIp + ".0/24 dev eth0 proto static scope link table wlan0 \n");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        machineCode = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, SharedPrefsConfigs
                .DEFAULT_MACHINE_CODE);
        String itemCode = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, null);
        // Logger.i("machineCode:" + machineCode);
        int initState = TestConfigs.init(this, machineCode, itemCode, null);
        showTestName();
        if (initState != TestConfigs.INIT_NO_MACHINE_CODE) {
            MachineCode.machineCode = machineCode;
        }
    }

    private void showTestName() {
        SystemSetting systemSetting = SettingHelper.getSystemSetting();
        StringBuilder sb = new StringBuilder("智能主机(安卓版V" + SystemBrightUtils.getCurrentVersion(this) + ")");

        if (machineCode != SharedPrefsConfigs.DEFAULT_MACHINE_CODE) {
//			sb.append("-").append(TestConfigs.machineNameMap.get(machineCode))
//					.append(systemSetting.getHostId()).append("号机");
            if (TestConfigs.sCurrentItem != null && TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
                sb.append("-").append(TestConfigs.sCurrentItem.getItemName())
                        .append(systemSetting.getHostId()).append("号机");
            } else {
                sb.append("-").append(TestConfigs.machineNameMap.get(machineCode))
                        .append(systemSetting.getHostId()).append("号机");
            }

        }
        if (!TextUtils.isEmpty(systemSetting.getTestName())) {
            sb.append("-").append(systemSetting.getTestName());
        }
        txtMainTitle.setText(sb.toString());
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
//                startActivity(new Intent(MainActivity.this, MiddleDistanceRaceActivity.class));
                if (isSettingFinished()) {
                    if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_FWC) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("selectType", 1);
                        IntentUtil.gotoActivity(this, TestConfigs.proActivity.get(TestConfigs.sCurrentItem.getMachineCode()), bundle);
                        return;
                    }
                    if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
                        startActivity(new Intent(MainActivity.this, TestConfigs.proActivity.get(TestConfigs.sCurrentItem.getMachineCode())));
                    } else {
                        startActivity(new Intent(MainActivity.this, BaseGroupActivity.class));
                    }
                }
                break;
            case R.id.card_select:
                startActivity(new Intent(MainActivity.this, DataRetrieveActivity.class));
                break;
            case R.id.card_print:
                PrinterManager.getInstance().init();
                PrinterManager.getInstance().selfCheck();
                PrinterManager.getInstance().print("\n\n");
//                addTestResult();
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
                break;

        }
    }

    @OnClick(R.id.img_code)
    public void onCodeClicked(View view) {
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
            this.finish();
        }
    }
    // @Override
    // public void onEventMainThread(BaseEvent baseEvent) {
    //     super.onEventMainThread(baseEvent);
    //     if (EventConfigs.DATA_RESTORE_SUCCEED == baseEvent.getTagInt()) {
    //         onResume();
    //     }
    // }

    // for test
    // private void addTestResult(int testNo) {
    //     List<Group> groupList = DBManager.getInstance().getGroupByScheduleNo("1");
    //     for (Group group : groupList) {
    //         List<Map<String, Object>> dbStudentList = DBManager.getInstance().getStudenByStuItemAndGroup(group);
    //         for (Map<String, Object> map : dbStudentList) {
    //             Student student = (Student) map.get("student");
    //             for (int i = 0; i < 2; i++) {
    //                 RoundResult roundResult = new RoundResult();
    //                 roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
    //                 roundResult.setStudentCode(student.getStudentCode());
    //                 String itemCode = TestConfigs.sCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : TestConfigs.sCurrentItem.getItemCode();
    //                 roundResult.setItemCode(itemCode);
    //                 roundResult.setResult(100);
    //                 roundResult.setMachineResult(100);
    //                 roundResult.setResultState(0);
    //                 roundResult.setTestTime(System.currentTimeMillis() + "");
    //                 roundResult.setRoundNo(i + 1);
    //                 roundResult.setTestNo(testNo);
    //                 roundResult.setExamType(0);
    //                 roundResult.setGroupId(group.getId());
    //                 roundResult.setUpdateState(0);
    //                 roundResult.setScheduleNo("1");
    //                 if (i == 1) {
    //                     roundResult.setIsLastResult(1);
    //                 }
    //                 DBManager.getInstance().insertRoundResult(roundResult);
    //             }
    //         }
    //     }
    // }
    // private void addTestResult() {
    //     List<Student> dbStudentList = DBManager.getInstance().dumpAllStudents();
    //     for (Student student : dbStudentList) {
    //         for (int i = 0; i < 2; i++) {
    //             RoundResult roundResult = new RoundResult();
    //             roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
    //             roundResult.setStudentCode(student.getStudentCode());
    //             String itemCode = TestConfigs.sCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : TestConfigs.sCurrentItem.getItemCode();
    //             roundResult.setItemCode(itemCode);
    //             roundResult.setResult(100 + i);
    //             roundResult.setResultState(0);
    //             roundResult.setTestTime(TestConfigs.df.format(Calendar.getInstance().getTime()));
    //             roundResult.setRoundNo(i);
    //             roundResult.setTestNo(1);
    //             roundResult.setExamType(0);
    //             roundResult.setUpdateState(0);
    //             roundResult.setScheduleNo("1");
    //             if (i == 1) {
    //                 roundResult.setIsLastResult(1);
    //             }
    //             DBManager.getInstance().insertRoundResult(roundResult);
    //         }
    //     }
    // }

}