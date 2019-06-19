package com.feipulai.exam.activity.situp.base_test;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.base.test.AbstractRadioTestActivity;
import com.feipulai.exam.view.WaitDialog;

import butterknife.OnClick;

public abstract class SitPullUpTestActivity<Setting>
		extends AbstractRadioTestActivity<Setting>
		implements SitPullUpTestContract.View<Setting> {
	
	private SitPullUpTestPresenter<Setting> presenter;
	private WaitDialog changBadDialog;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		presenter = (SitPullUpTestPresenter<Setting>) super.presenter;
	}
	
	@Override
	protected abstract SitPullUpTestPresenter<Setting> getPresenter();
	
	@OnClick({R.id.btn_penalize})
	public void onPenalizeClicked(View view) {
		presenter.punish();
	}
	
	@Override
	public void showPenalizeDialog(int max) {
		final NumberPicker numberPicker = new NumberPicker(this);
		numberPicker.setMinValue(0);
		numberPicker.setValue(0);
		numberPicker.setMaxValue(max);
		LinearLayout layout = new LinearLayout(this);
		layout.setGravity(Gravity.CENTER);
		layout.addView(numberPicker, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
		numberPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS); //禁止输入
		
		new AlertDialog.Builder(this).setTitle("请输入判罚值")
				.setIcon(android.R.drawable.ic_dialog_info)
				.setView(layout)
				.setCancelable(false)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						presenter.penalize(-1 * numberPicker.getValue());
					}
				})
				.setNegativeButton("返回", null).show();
	}
	
	public void showChangeBadDialog() {
		changBadDialog = new WaitDialog(this);
		changBadDialog.setCanceledOnTouchOutside(false);
		changBadDialog.setCancelable(false);
		changBadDialog.show();
		// 必须在dialog显示出来后再调用
		changBadDialog.setTitle("请重启待连接设备");
		changBadDialog.btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changBadDialog.dismiss();
				presenter.cancelChangeBad();
			}
		});
	}
	
	@Override
	public void enablePenalize(final boolean enable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				btnPenalize.setVisibility(enable ? View.VISIBLE : View.GONE);
			}
		});
	}
	
	@Override
	public void changeBadSuccess() {
		changBadDialog.dismiss();
		toastSpeak("更换成功");
	}
	
}
