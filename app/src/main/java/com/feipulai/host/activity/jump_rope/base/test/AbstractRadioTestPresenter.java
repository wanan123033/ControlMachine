package com.feipulai.host.activity.jump_rope.base.test;


import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.feipulai.common.jump_rope.facade.CountTimingTestFacade;
import com.feipulai.common.jump_rope.task.GetDeviceStatesTask;
import com.feipulai.common.jump_rope.task.LEDContentGenerator;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.activity.jump_rope.bean.TestCache;
import com.feipulai.host.activity.jump_rope.check.CheckUtils;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.Student;
import com.feipulai.host.utils.SharedPrefsUtil;
import com.orhanobut.logger.Logger;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public abstract class AbstractRadioTestPresenter<Setting>
		implements RadioTestContract.Presenter,
		GetDeviceStatesTask.OnGettingDeviceStatesListener,
		RadioManager.OnRadioArrivedListener,
		CountTimingTestFacade.Listener,
		LEDContentGenerator,
		Handler.Callback {
	
	private static final int FINAL_RESULT_GOT = 0x5;
	protected static final int INVALID_PIV = -100;
	protected CountTimingTestFacade facade;
	
	// 状态  WAIT_BGIN--->TESTING--->WAIT_MACHINE_RESULTS--->WAIT_CONFIRM_RESULTS---->WAIT_BGIN
	protected static final int WAIT_BGIN = 0x0;// 等待开始测试
	protected static final int TESTING = 0x1;// 测试过程中
	protected static final int WAIT_MACHINE_RESULTS = 0x2;// 测试结束,等待获取机器成绩
	protected static final int WAIT_CONFIRM_RESULTS = 0x3;
	protected volatile int testState = WAIT_BGIN;
	
	protected volatile int[] currentConnect;
	private String testDate;
	protected List<StuDevicePair> pairs;
	protected Setting setting;
	private Context context;
	protected RadioTestContract.View<Setting> view;
	protected int[] deviceIdPIV;
	protected int hostId;
	protected int focusPosition;
	protected Handler handler;
	private HandlerThread handlerThread;
	
	protected AbstractRadioTestPresenter(Context context, RadioTestContract.View<Setting> view) {
		this.context = context;
		this.view = view;
		hostId = SharedPrefsUtil.getValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.HOST_ID, 1);
	}
	
	@Override
	public void start() {
		pairs = TestCache.getInstance().getTestingPairs();
		setting = getSetting();
		int size = pairs.size();
		int possibleMaxDeviceId = pairs.get(size - 1).getBaseDevice().getDeviceId();
		currentConnect = new int[possibleMaxDeviceId + 1];
		// 记录每个设备id在recyclerview中的位置  deviceIdPIV[deviceId] = piv
		deviceIdPIV = new int[possibleMaxDeviceId + 1];
		Arrays.fill(deviceIdPIV, INVALID_PIV);
		int piv = 0;
		for (StuDevicePair pair : pairs) {
			deviceIdPIV[pair.getBaseDevice().getDeviceId()] = piv;
			piv++;
		}
		
		handlerThread = new HandlerThread("handlerThread");
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper(), this);
		
		view.initView(pairs, setting);
		
		CountTimingTestFacade.Builder builder = new CountTimingTestFacade.Builder();
		facade = builder.countStartTime(getCountStartTime())
				.countFinishTime(getCountFinishTime())
				.testTime(getTestTimeFromSetting())
				.hostId(hostId)
				.setLEDContentGenerator(this)
				.setOnGettingDeviceStatesListener(this)
				.setSize(size)
				.build();
		facade.setListener(this);
		startTest();
		RadioManager.getInstance().setOnRadioArrived(this);
	}
	
	protected abstract int getCountFinishTime();
	
	protected abstract int getCountStartTime();
	
	protected abstract Setting getSetting();
	
	protected abstract int getTestTimeFromSetting();
	
	protected abstract void resetDevices();
	
	protected abstract void testCountDown(long tick);
	
	@Override
	public void startTest() {
		Logger.i("开始测试,测试考生设备信息:" + pairs.toString());
		testDate = TestConfigs.df.format(Calendar.getInstance(Locale.CHINA).getTime());
		resetDevices();
		view.setViewForStart();
		facade.start();
		testState = TESTING;
	}
	
	@Override
	public void stopNow() {
		facade.stopTotally();
	}
	
	@Override
	public void quitTest() {
		stopNow();
		resetDevices();
		view.quitTest();
	}
	
	@Override
	public void restartTest() {
		facade.stop();
		testState = WAIT_BGIN;
		startTest();
	}
	
	private boolean checkFinalResults() {
		for (StuDevicePair pair : pairs) {
			Student student = pair.getStudent();
			int state = pair.getBaseDevice().getState();
			if (student != null && state == BaseDeviceState.STATE_COUNTING) {
				// 存在手柄还在计数,证明当前成绩还不是最终成绩
				return false;
			}
		}
		return true;
	}
	
	private void saveResults() {
		InteractUtils.saveResults(pairs, testDate);
	}
	
	@Override
	public int stateOfPosition(int position) {
		return pairs.get(position).getBaseDevice().getState();
	}
	
	@Override
	public void setFocusPosition(int position) {
		focusPosition = position;
	}
	
	@Override
	public void onStateRefreshed() {
		int oldState;
		for (int i = 0; i < pairs.size(); i++) {
			StuDevicePair pair = pairs.get(i);
			BaseDeviceState deviceState = pair.getBaseDevice();
			oldState = deviceState.getState();
			if (currentConnect[deviceState.getDeviceId()] == 0
					&& oldState != BaseDeviceState.STATE_DISCONNECT
					&& oldState != BaseDeviceState.STATE_STOP_USE) {
				deviceState.setState(BaseDeviceState.STATE_DISCONNECT);
				view.updateSpecificItem(i);
			}
		}
		int length = currentConnect.length;// 必须是这个长度
		currentConnect = new int[length];
	}
	
	@Override
	public int getDeviceCount() {
		return pairs.size();
	}
	
	@Override
	public void stopUse() {
		CheckUtils.stopUse(pairs, focusPosition);
		view.updateSpecificItem(focusPosition);
	}
	
	@Override
	public void resumeUse() {
		CheckUtils.resumeUse(pairs, focusPosition);
		view.updateSpecificItem(focusPosition);
	}
	
	@Override
	public void onGetReadyTimerTick(long tick) {
		view.tickInUI(tick + "");
		testCountDown(tick);
	}
	
	@Override
	public void onGetReadyTimerFinish() {
		view.enableStopUse(true);
		view.tickInUI("开始");
	}
	
	@Override
	public void onTestingTimerTick(final long tick) {
		view.tickInUI(tick + "");
		if (tick <= 5) {
			view.enableStopRestartTest(false);
		} else {
			view.enableStopRestartTest(true);
		}
	}
	
	@Override
	public void onTestingTimerFinish() {
		view.tickInUI("结束");
		testState = WAIT_MACHINE_RESULTS;
		view.showWaitFinalResultDialog(true);
		handler.sendEmptyMessageDelayed(FINAL_RESULT_GOT, 2000);
	}
	
	@Override
	public void finishTest(){
		facade.pauseGettingState();
		saveResults();
		view.finishTest();
	}
	
	@Override
	public void confirmResults() {
		for (StuDevicePair pair : pairs) {
			if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_DISCONNECT) {
				view.showDisconnectForFinishTest();
				return;
			}
		}
		finishTest();
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			
			case FINAL_RESULT_GOT:
				if (checkFinalResults()) {
					Logger.i("获取到设备最终成绩,考生设备信息:" + pairs.toString());
					testState = WAIT_CONFIRM_RESULTS;
					view.showViewForConfirmResults();
					view.showWaitFinalResultDialog(false);
				} else {
					// 继续等待最终成绩
					handler.sendEmptyMessageDelayed(FINAL_RESULT_GOT, 2000);
				}
				return true;
		}
		return false;
	}
	
}
