package com.feipulai.host.activity.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SystemBrightUtils;
import com.feipulai.common.view.baseToolbar.StatusBarUtil;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.activity.data.DataManageActivity;
import com.feipulai.host.activity.data.DataRetrieveActivity;
import com.feipulai.host.activity.explain.ExplainActivity;
import com.feipulai.host.activity.setting.LEDSettingActivity;
import com.feipulai.host.activity.setting.SettingActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.setting.SystemSetting;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.CommonUtils;
import com.ww.fpl.libarcface.faceserver.FaceServer;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    private boolean mIsExiting;
    private Intent serverIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        RadioManager.getInstance().init();
        StatusBarUtil.setImmersiveTransparentStatusBar(this);//设置沉浸式透明状态栏 配合使用

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
        showTestName();
        if (initState != TestConfigs.INIT_NO_MACHINE_CODE) {
            MachineCode.machineCode = machineCode;
        }
    }

    private void showTestName() {
        SystemSetting systemSetting = SettingHelper.getSystemSetting();

        StringBuilder sb = new StringBuilder(String.format(systemSetting.isFreedomTest() ? getString(R.string.versions_name_2)
                : getString(R.string.versions_name), SystemBrightUtils.getCurrentVersion(this)));

        if (machineCode != SharedPrefsConfigs.DEFAULT_MACHINE_CODE) {
            sb.append("-").append(
                    String.format(getString(R.string.host_name), TestConfigs.machineNameMap.get(machineCode), systemSetting.getHostId()));
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
                PrinterManager.getInstance().init();
                PrinterManager.getInstance().selfCheck();
                PrinterManager.getInstance().print("\n\n");
//                addTestResult();
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
                IntentUtil.gotoActivity(MainActivity.this, LEDSettingActivity.class);
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
}
