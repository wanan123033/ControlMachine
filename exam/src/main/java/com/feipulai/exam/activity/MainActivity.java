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
import com.feipulai.common.view.baseToolbar.StatusBarUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.udp.UdpLEDUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseActivity;
import com.feipulai.exam.activity.base.BaseGroupActivity;
import com.feipulai.exam.activity.data.DataManageActivity;
import com.feipulai.exam.activity.data.DataRetrieveActivity;
import com.feipulai.exam.activity.sargent_jump.SargentItemSelectActivity;
import com.feipulai.exam.activity.setting.SettingActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.CommonUtils;
import com.feipulai.exam.service.UploadService;

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
//        testUpload();
    }

    private void testUpload() {
        serverIntent = new Intent(this, UploadService.class);
        startService(serverIntent);
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode("2012000001");
        for (int i = 0; i < 3; i++) {
            RoundResult roundResult = new RoundResult();
            roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
            roundResult.setStudentCode(studentItem.getStudentCode());
            roundResult.setItemCode(TestConfigs.getCurrentItemCode());
            roundResult.setResult(10);
            roundResult.setMachineResult(10);
            roundResult.setResultState(i == 0 ? 1 : 0);
//        roundResult.setTestTime(TestConfigs.df.format(Calendar.getInstance().getTime()));
            roundResult.setTestTime(System.currentTimeMillis() + "");
            roundResult.setRoundNo(i + 1);
            roundResult.setTestNo(1);
            roundResult.setExamType(studentItem.getExamType());
            roundResult.setScheduleNo(studentItem.getScheduleNo());
            roundResult.setUpdateState(0);
            List<RoundResult> roundResultList = new ArrayList<>();
            roundResultList.add(roundResult);
            UploadResults uploadResults = new UploadResults(studentItem.getScheduleNo(), TestConfigs.getCurrentItemCode(),
                    studentItem.getStudentCode(), "1", "", RoundResultBean.beanCope(roundResultList));


            uploadResult(uploadResults);
        }


    }

    /**
     * 成绩上传
     *
     * @param uploadResults 上传成绩
     */
    private void uploadResult(UploadResults uploadResults) {

        Bundle bundle = new Bundle();
        bundle.putSerializable(UploadResults.BEAN_KEY, uploadResults);
        serverIntent.putExtras(bundle);
        startService(serverIntent);
    }

    private void showTestName() {
        SystemSetting systemSetting = SettingHelper.getSystemSetting();
        StringBuilder sb = new StringBuilder("智能主机(安卓版V" + SystemBrightUtils.getCurrentVersion(this) + ")");

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
//                startActivity(new Intent(MainActivity.this, MiddleDistanceRaceActivity.class));
                if (isSettingFinished()) {
                    if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_FWC) {
                        IntentUtil.gotoActivity(this, TestConfigs.proActivity.get(TestConfigs.sCurrentItem.getMachineCode()));
                        return;
                    }
                    if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                        IntentUtil.gotoActivity(this, TestConfigs.proActivity.get(TestConfigs.sCurrentItem.getMachineCode()));
                        return;
                    }
                    if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_MG) {
                        startActivity(new Intent(MainActivity.this, SargentItemSelectActivity.class));
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


}
