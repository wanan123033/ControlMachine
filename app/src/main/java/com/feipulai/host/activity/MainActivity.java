package com.feipulai.host.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.activity.data.DataManageActivity;
import com.feipulai.host.activity.data.DataRetrieveActivity;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.utils.FileUtil;
import com.feipulai.host.utils.SharedPrefsUtil;
import com.feipulai.host.view.CircleMenuLayout;
import com.github.lzyzsd.circleprogress.DonutProgress;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zzs on 2018/7/19
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MainActivity extends BaseActivity{

	@BindView(R.id.id_menu_layout)
	CircleMenuLayout menuLayout;
	@BindView(R.id.donut_progress)
	DonutProgress donutProgress;

	private int hostId;
	private String itemCode;
	// private boolean mIsExiting;

	private int[] mItemImgs = new int[]{
			R.drawable.circle_menu_bg_lin_green,
			R.drawable.circle_menu_bg_lin_oceanblue,
			R.drawable.circle_menu_bg_lin_blue,
			R.drawable.circle_menu_bg_lin_violet,
			R.drawable.circle_menu_bg_lin_organge,
			/*R.drawable.circle_menu_bg_lin_yellow*/};

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		RadioManager.getInstance().init();
		String[] mItemTexts = getResources().getStringArray(R.array.main_function);
		menuLayout.setMenuItemBgAndTexts(mItemImgs,mItemTexts);
		menuLayout.setmStartAngle(55);
		menuLayout.setOnMenuItemClickListener(new CircleMenuLayout.OnMenuItemClickListener(){
			@Override
			public void itemClick(View view, int pos) {
				if (!isSettingFinished()) {
					return;
				}
				
				switch (pos) {
					case 0:
						// 显示屏
						startActivity(new Intent(MainActivity.this, LEDSettingActivity.class));
						break;
					case 1:
						// 系统
						startActivity(new Intent(Settings.ACTION_SETTINGS));
						break;
					case 2:
						// 数据管理
						startActivity(new Intent(MainActivity.this, DataManageActivity.class));
						break;
					case 3:
						// 数据查询
						startActivity(new Intent(MainActivity.this, DataRetrieveActivity.class));
						break;
					case 4:
						// 设置
						startActivity(new Intent(MainActivity.this, SettingActivity.class));
						break;
				}
			}

			@Override
			public void itemCenterClick(View view){
				if(isSettingFinished()){
					startActivity(new Intent(MainActivity.this,TestConfigs.proActivity.get(TestConfigs.sCurrentItem.getMachineCode())));
				}
			}
		});
	}

	private boolean isSettingFinished(){
		if(TestConfigs.sCurrentItem == null){
			toastSpeak("请先选择测试项目");
			startActivity(new Intent(MainActivity.this,MachineSelectActivity.class));
			return false;
		}
		return true;
	}

	@OnClick(R.id.btn_machine_select)
	public void onViewClicked(View view){
		switch(view.getId()){

			// 项目切换
			case R.id.btn_machine_select:
				startActivity(new Intent(this,MachineSelectActivity.class));
				break;

		}
	}

	@Override
	protected void onResume(){
		super.onResume();
		donutProgress.setText(FileUtil.getPercentRemainStorage() + "%");
		donutProgress.setDonut_progress(FileUtil.getPercentRemainStorage() + "");
		hostId = SharedPrefsUtil.getValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.HOST_ID,1);
		machineCode = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, SharedPrefsConfigs
				.DEFAULT_MACHINE_CODE);
		itemCode = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, null);
		
		// Logger.i("machineCode:" + machineCode);
		int initState = TestConfigs.init(this,machineCode,itemCode,null);
		
		if (initState == TestConfigs.INIT_NO_MACHINE_CODE) {
			setTitle("智能主机[体测版]");
		}else {
			MachineCode.machineCode = machineCode;
			setTitle("智能主机[体测版]-" + TestConfigs.machineNameMap.get(machineCode) + "\t" + hostId + "号机");
		}
	}

	// @Override
	// public void onBackPressed(){
	// 	super.onBackPressed();
	// 	mIsExiting = true;
	// }

	@Override
	protected void onDestroy(){
		super.onDestroy();
		// if(mIsExiting){
			RadioManager.getInstance().close();
		// }
	}

}
