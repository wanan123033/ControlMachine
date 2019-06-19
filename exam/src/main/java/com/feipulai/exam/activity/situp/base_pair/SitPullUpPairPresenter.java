package com.feipulai.exam.activity.situp.base_pair;

import android.content.Context;
import android.os.Message;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.check.CheckUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;

import java.util.List;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public abstract class SitPullUpPairPresenter
		implements SitPullUpPairContract.Presenter,
		RadioManager.OnRadioArrivedListener,
		SitPullLinker.SitPullPairListener {
	
	private Context context;
	private SitPullUpPairContract.View view;
	private volatile int focusPosition;
	private List<StuDevicePair> pairs;
	private int machineCode = TestConfigs.sCurrentItem.getMachineCode();
	private final int TARGET_FREQUENCY = SerialConfigs.sProChannels.get(TestConfigs.sCurrentItem.getMachineCode()) + SettingHelper.getSystemSetting().getHostId() - 1;
	private SitPullLinker linker;
	
	public SitPullUpPairPresenter(Context context, SitPullUpPairContract.View view) {
		this.context = context;
		this.view = view;
	}
	
	@Override
	public void start() {
		pairs = CheckUtils.newPairs(getDeviceSum());
		view.initView(isAutoPair(), pairs);
		RadioManager.getInstance().setOnRadioArrived(this);
		linker = new SitPullLinker(machineCode, TARGET_FREQUENCY, this);
		linker.startPair(1);
	}
	
	@Override
	public void changeFocusPosition(int position) {
		if (focusPosition == position) {
			return;
		}
		focusPosition = position;
		pairs.get(position).getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
		view.select(position);
		linker.startPair(focusPosition + 1);
	}
	
	@Override
	public abstract void changeAutoPair(boolean isAutoPair);
	
	protected abstract int getDeviceSum();
	
	protected abstract boolean isAutoPair();
	
	public abstract void setFrequency(int deviceId, int originFrequency, int deviceFrequency);
	
	@Override
	public abstract void saveSettings();
	
	@Override
	public void stopPair() {
		linker.cancelPair();
		RadioManager.getInstance().setOnRadioArrived(null);
	}
	
	public void onNewDeviceConnect() {
		pairs.get(focusPosition).getBaseDevice().setState(BaseDeviceState.STATE_FREE);
		view.updateSpecificItem(focusPosition);
		if (isAutoPair() && focusPosition != pairs.size() - 1) {
			changeFocusPosition(focusPosition + 1);
			//这里先清除下一个的连接状态,避免没有连接但是现实已连接
			BaseDeviceState originState = pairs.get(focusPosition).getBaseDevice();
			originState.setState(BaseDeviceState.STATE_DISCONNECT);
		}
	}
	
	@Override
	public void onRadioArrived(Message msg) {
		linker.onRadioArrived(msg);
	}
	
	public synchronized void onNoPairResponseArrived() {
		view.showToast("未收到子机回复,设置失败,请重试");
	}
	
}
