package com.feipulai.exam.activity.situp.check;

import android.content.Context;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.exam.activity.jump_rope.base.check.RadioCheckContract;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.activity.situp.base_check.SitPullUpCheckPresenter;

import java.util.Calendar;

public class SitUpCheckPresenter extends SitPullUpCheckPresenter<SitUpSetting> {
	
	private final SitPushUpManager deviceManager;
	private int countForSetAngle = 20;
	
	public SitUpCheckPresenter(Context context, RadioCheckContract.View<SitUpSetting> view) {
		super(context, view);
		setting = SharedPrefsUtil.loadFormSource(context, SitUpSetting.class);
		deviceManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP);
	}
	
	@Override
	protected SitUpSetting getSetting() {
		return setting;
	}
	@Override
	protected int getTestPattern() {
		return setting.getGroupMode();
	}

	@Override
	protected int getDeviceSumFromSetting() {
		return setting.getDeviceSum();
	}
	
	@Override
	public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
//		deviceManager.setFrequency(ItemDefault.CODE_YWQZ,
//				originFrequency,
//				deviceId,
//				SettingHelper.getSystemSetting().getHostId());
		deviceManager.setFrequency( deviceFrequency,
				originFrequency,
				deviceId,
				SettingHelper.getSystemSetting().getHostId());
	}
	
	@Override
	protected void endTest() {
		deviceManager.endTest();
	}

	private boolean syncTime;
	@Override
	public void onGettingState(int position) {
		deviceManager.getState(position + 1, setting.getAngle());
		if (countForSetAngle++ % 20 == 0) {
			deviceManager.setBaseline(SitPushUpManager.PROJECT_CODE_SIT_UP, setting.getAngle());
		}

		if (!syncTime) {
			deviceManager.syncTime(systemSetting.getHostId(), getTime(),setting.isShowLed()? 1: 0);
			deviceManager.getTime(position+1,systemSetting.getHostId());
			syncTime = true;
		}
	}

	public int getTime() {
		Calendar Cld = Calendar.getInstance();
		int HH = Cld.get(Calendar.HOUR_OF_DAY);
		int mm = Cld.get(Calendar.MINUTE);
		int SS = Cld.get(Calendar.SECOND);
		int MI = Cld.get(Calendar.MILLISECOND);
		return HH * 60 * 60 * 1000 + mm * 60 * 1000 + SS * 1000 + MI;
	}
	
}
