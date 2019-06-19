package com.feipulai.exam.activity.situp.base_check;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipulai.common.view.StopUseButton;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.base.check.AbstractRadioCheckActivity;
import com.feipulai.exam.activity.jump_rope.base.check.RadioCheckContract;

import butterknife.BindView;

public abstract class SitPullUpCheckActivity<Setting>
		extends AbstractRadioCheckActivity<Setting>
		implements RadioCheckContract.View<Setting> {
	
	@BindView(R.id.rv_pairs)
	RecyclerView mRvPairs;
	@BindView(R.id.lv_results)
	ListView mLvResults;
	@BindView(R.id.ll_stu_detail)
	LinearLayout mLlStuDetail;
	@BindView(R.id.rl_check_in)
	RelativeLayout rlCheckIn;
	@BindView(R.id.iv_portrait)
	ImageView ivPortrait;
	@BindView(R.id.tv_studentCode)
	TextView tvStudentCode;
	@BindView(R.id.tv_studentName)
	TextView tvStudentName;
	@BindView(R.id.tv_gender)
	TextView tvGender;
	@BindView(R.id.tv_grade)
	TextView tvGrade;
	@BindView(R.id.btn_start_test)
	Button btnStartTest;
	@BindView(R.id.btn_led_setting)
	Button btnLedSetting;
	@BindView(R.id.btn_change_bad)
	Button btnChangeBad;
	@BindView(R.id.btn_device_pair)
	Button btnDevicePair;
	@BindView(R.id.view_bottom)
	LinearLayout viewBottom;
	@BindView(R.id.btn_stop_use)
	StopUseButton btnStopUse;
	@BindView(R.id.btn_delete_student)
	Button btnDeleteStudent;
	@BindView(R.id.btn_del_all)
	Button btnDelAll;
	
	@Override
	protected int setLayoutResID() {
		return R.layout.activity_sit_pull_up_check;
	}
	
	@Override
	protected ListView getResultView() {
		return mLvResults;
	}
	
	@Override
	protected View getChangeBadView() {
		return btnChangeBad;
	}
	
	@Override
	protected View getLedSettingView() {
		return btnLedSetting;
	}
	
	@Override
	protected View getPairView() {
		return btnDevicePair;
	}
	
	@Override
	protected View getStartTestView() {
		return btnStartTest;
	}
	
	@Override
	protected RecyclerView getRvPairs() {
		return mRvPairs;
	}
	
	@Override
	protected View getCheckInLayout() {
		return rlCheckIn;
	}
	
	@Override
	protected void onConflictItemClicked() {
		// 没有这个
	}
	
	@Override
	protected TextView getStopUseView() {
		return btnStopUse;
	}
	
	@Override
	protected View getDeleteStuView() {
		return btnDeleteStudent;
	}
	
	@Override
	protected View getDeleteAllView() {
		return btnDelAll;
	}
	
	@Override
	protected LinearLayout getStuDetailLayout() {
		return mLlStuDetail;
	}
	
	@Override
	protected String getChangeBadTitle() {
		return "请重启待连接设备";
	}
	
}
