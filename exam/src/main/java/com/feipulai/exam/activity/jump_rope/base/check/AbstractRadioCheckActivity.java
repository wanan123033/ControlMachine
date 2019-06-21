package com.feipulai.exam.activity.jump_rope.base.check;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.feipulai.common.utils.ActivityUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.adapter.CheckPairAdapter;
import com.feipulai.exam.activity.jump_rope.base.result.RadioResultActivity;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.fragment.IndividualCheckFragment;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.view.DividerItemDecoration;
import com.feipulai.exam.view.WaitDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.ButterKnife;

public abstract class AbstractRadioCheckActivity<Setting>
		extends BaseTitleActivity
		implements RadioCheckContract.View<Setting>,
		CheckPairAdapter.OnItemClickListener,
		View.OnClickListener {
	
	private static final int UPDATE_STATES = 0x1;
	private static final int UPDATE_SPECIFIC_ITEM = 0x2;
	private static final String RESUME_USE = "恢复使用";
	private static final String STOP_USE = "暂停使用";
	
	protected RadioCheckContract.Presenter presenter;
	private WaitDialog changBadDialog;
	private Handler mHandler = new MyHandler(this);
	private CheckPairAdapter mAdapter;
	private IndividualCheckFragment individualCheckFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);// 不知道为什么,必须这么搞
		presenter = getPresenter();
		presenter.start();
	}
	
	@Override
	protected void initData() {
		ButterKnife.bind(this);
	}
	
	@Override
	protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
		String title =   TestConfigs.machineNameMap.get(machineCode)
				+ SettingHelper.getSystemSetting().getHostId()
				+ "号机-"
				+ SettingHelper.getSystemSetting().getTestName();
		builder.setTitle(title).addLeftText("返回", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		SystemSetting systemSetting = SettingHelper.getSystemSetting();
		if (systemSetting.getTestPattern() == SystemSetting.PERSON_PATTERN) {
			builder.addRightText("项目设置", new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(AbstractRadioCheckActivity.this, getProjectSettingActivity()));
				}
			}).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(AbstractRadioCheckActivity.this, getProjectSettingActivity()));
				}
			});
		}
		return builder;
	}
	
	@Override
	public void initView(SystemSetting systemSetting, Setting setting, List<StuDevicePair> pairs) {
		if (systemSetting.getTestPattern() == SystemSetting.PERSON_PATTERN) {
			individualCheckFragment = new IndividualCheckFragment();
			individualCheckFragment.setResultView(getResultView());
			individualCheckFragment.setOnIndividualCheckInListener(presenter);
			ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), individualCheckFragment, getCheckInLayout().getId());
		} else {
			getCheckInLayout().setVisibility(View.GONE);
			getDeleteAllView().setVisibility(View.GONE);
			getDeleteStuView().setVisibility(View.GONE);
			getStopUseView().setVisibility(View.GONE);
		}
		
		getRvPairs().setLayoutManager(new GridLayoutManager(this, 5));
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
		dividerItemDecoration.setDrawBorderTopAndBottom(true);
		dividerItemDecoration.setDrawBorderLeftAndRight(true);
		getRvPairs().addItemDecoration(dividerItemDecoration);
		getRvPairs().setHasFixedSize(true);
		getRvPairs().setClickable(true);
		
		mAdapter = new CheckPairAdapter(this, pairs);
		mAdapter.setOnItemClickListener(this);
		getRvPairs().setAdapter(mAdapter);
		
		getStopUseView().setOnClickListener(this);
		getChangeBadView().setOnClickListener(this);
		getDeleteAllView().setOnClickListener(this);
		getDeleteStuView().setOnClickListener(this);
		getStartTestView().setOnClickListener(this);
		getLedSettingView().setOnClickListener(this);
		getPairView().setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if (v == getLedSettingView()) {// LED设置
			startActivity(new Intent(this, LEDSettingActivity.class));
		} else if (v == getStartTestView()) {// 开始测试
			presenter.startTest();
		} else if (v == getPairView()) {// 设备配对
			startActivity(new Intent(this, getPairActivity()));
		} else if (v == getChangeBadView()) {// 故障更换
			presenter.changeBadDevice();
		} else if (v == getStopUseView()) {// 暂停/恢复使用
			String text = getStopUseView().getText().toString().trim();
			if (text.equals(RESUME_USE)) {
				presenter.resumeUse();
				getStopUseView().setText(STOP_USE);
			} else {
				presenter.stopUse();
				getStopUseView().setText(RESUME_USE);
			}
		} else if (v == getDeleteAllView()) {// 删除所有
			presenter.deleteAll();
		} else if (v == getDeleteStuView()) {// 删除考生
			presenter.deleteStudent();
		}
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		presenter.settingChanged();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		presenter.resetLED();
		presenter.resumeGetStateAndDisplay();
	}
	
	@Override
	public void startTest() {
		Intent intent = new Intent(this, getTestActivity());
		startActivityForResult(intent, 1);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
			case RadioResultActivity.BACK_TO_CHECK:
				if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN) {
					EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_RESULT));
					finish();
				} else {
					presenter.refreshEveryThing();
				}
				break;
		}
	}
	
	@Override
	public void updateSpecificItem(int position) {
		Message msg = Message.obtain();
		msg.what = UPDATE_SPECIFIC_ITEM;
		msg.arg1 = position;
		mHandler.sendMessage(msg);
	}
	
	@Override
	public void showChangeBadDialog() {
		changBadDialog = new WaitDialog(this);
		changBadDialog.setCanceledOnTouchOutside(false);
		changBadDialog.setCancelable(false);
		changBadDialog.show();
		// 必须在dialog显示出来后再调用
		changBadDialog.setTitle(getChangeBadTitle());
		changBadDialog.btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changBadDialog.dismiss();
				presenter.cancelChangeBad();
			}
		});
	}
	
	@Override
	public void onItemClick(View view, int position) {
		int deviceState = presenter.stateOfPosition(position);
		presenter.setFocusPosition(position);
		if (deviceState == BaseDeviceState.STATE_STOP_USE) {
			if (getStopUseView() != null) {
				getStopUseView().setText(RESUME_USE);
			}
		} else if (deviceState == BaseDeviceState.STATE_CONFLICT) {
			onConflictItemClicked();
		} else {
			if (getStopUseView() != null) {
				getStopUseView().setText(STOP_USE);
			}
		}
		select(position);
	}
	
	@Override
	public void select(int position) {
		int oldPosition = mAdapter.getSelected();
		mAdapter.setSelected(position);
		mAdapter.notifyItemChanged(oldPosition);
		mAdapter.notifyItemChanged(position);
		presenter.showStuInfo(position);
	}
	
	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			
			case UPDATE_SPECIFIC_ITEM:
				// Log.i("index", msg.arg1 + "");
				mAdapter.notifyItemChanged(msg.arg1);
				break;
			
			case UPDATE_STATES:
				mAdapter.notifyDataSetChanged();
				break;
		}
	}
	
	@Override
	public void showLowBatteryStartDialog() {
		new AlertDialog.Builder(this)
				.setTitle("警告")
				.setMessage("存在低电量手柄,是否进入测试?")
				.setPositiveButton("继续开始", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startTest();
					}
				}).setNegativeButton("取消", null)
				.show();
	}
	
	@Override
	public void showToast(String msg) {
		toastSpeak(msg);
	}
	
	@Override
	public void changeBadSuccess() {
		changBadDialog.dismiss();
		toastSpeak("更换成功");
	}
	
	@Override
	public void showStuInfo(Student student, List results) {
		InteractUtils.showStuInfo(getStuDetailLayout(), student, results);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (changBadDialog != null && changBadDialog.isShowing()) {
			changBadDialog.dismiss();
		}
		presenter.cancelChangeBad();
		presenter.pauseGetStateAndDisplay();
		presenter.saveSetting();
	}
	
	public void updateAllItems() {
		mHandler.sendEmptyMessage(UPDATE_STATES);
	}
	
	@Override
	public void refreshPairs(List pairs) {
		mAdapter = new CheckPairAdapter(this, pairs);
		mAdapter.setOnItemClickListener(this);
		getRvPairs().setAdapter(mAdapter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		TestCache.getInstance().clear();
		presenter.finishGetStateAndDisplay();
	}
	
	protected abstract RadioCheckContract.Presenter getPresenter();
	
	protected abstract ListView getResultView();
	
	protected abstract void onConflictItemClicked();
	
	protected abstract View getChangeBadView();
	
	protected abstract View getLedSettingView();
	
	protected abstract View getPairView();
	
	protected abstract View getStartTestView();
	
	protected abstract Class<?> getProjectSettingActivity();
	
	protected abstract RecyclerView getRvPairs();
	
	protected abstract TextView getStopUseView();
	
	protected abstract View getDeleteStuView();
	
	protected abstract View getDeleteAllView();
	
	protected abstract View getCheckInLayout();
	
	protected abstract LinearLayout getStuDetailLayout();
	
	protected abstract String getChangeBadTitle();
	
	protected abstract Class<? extends Activity> getTestActivity();
	
	protected abstract Class<? extends Activity> getPairActivity();
	
}
