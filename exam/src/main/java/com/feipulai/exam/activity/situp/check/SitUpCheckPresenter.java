package com.feipulai.exam.activity.situp.check;

import android.content.Context;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.exam.activity.jump_rope.base.check.RadioCheckContract;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.activity.situp.base_check.SitPullUpCheckPresenter;

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
	protected int getDeviceSumFromSetting() {
		return setting.getDeviceSum();
	}
	
	@Override
	public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
		deviceManager.setFrequency(ItemDefault.CODE_YWQZ,
				originFrequency,
				deviceId,
				SettingHelper.getSystemSetting().getHostId());
	}
	
	@Override
	protected void endTest() {
		deviceManager.endTest();
	}
	
	@Override
	public void onGettingState(int position) {
		deviceManager.getState(position + 1, setting.getAngle());
		if (countForSetAngle++ % 20 == 0) {
			deviceManager.setBaseline(SitPushUpManager.PROJECT_CODE_SIT_UP, setting.getAngle());
		}
	}
	
}
