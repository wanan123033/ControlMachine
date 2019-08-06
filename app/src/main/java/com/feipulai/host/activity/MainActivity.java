package com.feipulai.host.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.feipulai.common.utils.SystemBrightUtils;
import com.feipulai.common.view.baseToolbar.StatusBarUtil;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.activity.data.DataManageActivity;
import com.feipulai.host.activity.data.DataRetrieveActivity;
import com.feipulai.host.activity.setting.SettingActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.setting.SystemSetting;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.netUtils.CommonUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zzs on 2018/7/19
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MainActivity extends BaseActivity{

	@BindView(R.id.img_code)
	ImageView imgCode;
	@BindView(R.id.txt_main_title)
	TextView txtMainTitle;
	@BindView(R.id.txt_deviceid)
	TextView txtDeviceId;
	private boolean mIsExiting;
	private Intent serverIntent;


	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		RadioManager.getInstance().init();
		StatusBarUtil.setImmersiveTransparentStatusBar(this);//设置沉浸式透明状态栏 配合使用

	}

	private boolean isSettingFinished(){
		if(TestConfigs.sCurrentItem == null){
			toastSpeak("请先选择测试项目");
			startActivity(new Intent(MainActivity.this,MachineSelectActivity.class));
			return false;
		}
		return true;
	}
	@Override
	protected void onResume() {
		super.onResume();
		machineCode = com.feipulai.common.utils.SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, SharedPrefsConfigs
				.DEFAULT_MACHINE_CODE);
		String itemCode = com.feipulai.common.utils.SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, null);
		// Logger.i("machineCode:" + machineCode);
		int initState = TestConfigs.init(this, machineCode, itemCode, null);
		showTestName();
		if (initState != TestConfigs.INIT_NO_MACHINE_CODE) {
			MachineCode.machineCode = machineCode;
		}
	}
	private void showTestName() {
		SystemSetting systemSetting = SettingHelper.getSystemSetting();
		StringBuilder sb = new StringBuilder("智能主机(体测版V" + SystemBrightUtils.getCurrentVersion(this) + ")");

		if (machineCode != SharedPrefsConfigs.DEFAULT_MACHINE_CODE) {
			sb.append("-").append(TestConfigs.machineNameMap.get(machineCode))
					.append(systemSetting.getHostId()).append("号机");
		}
		if (!TextUtils.isEmpty(systemSetting.getTestName())) {
			sb.append("-").append(systemSetting.getTestName());
		}
		txtMainTitle.setText(sb.toString());
		txtDeviceId.setText(CommonUtils.getDeviceId(this));
	}


	@OnClick({R.id.card_test, R.id.card_select, R.id.card_print, R.id.card_parameter_setting, R.id.card_data_admin, R.id.card_system, R.id.card_led, R.id.card_device_cut})
	public void onViewClicked(View view) {
		if (!isSettingFinished()) {
			return;
		}
		switch (view.getId()) {
			case R.id.card_test:
				if (isSettingFinished()) {
					startActivity(new Intent(MainActivity.this,TestConfigs.proActivity.get(TestConfigs.sCurrentItem.getMachineCode())));
				}
				break;
			case R.id.card_select:
				startActivity(new Intent(MainActivity.this, DataRetrieveActivity.class));
				break;
			case R.id.card_print:
				PrinterManager.getInstance().init();
				PrinterManager.getInstance().selfCheck();
				PrinterManager.getInstance().print("\n\n");
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
