package com.feipulai.common.db;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;

import com.feipulai.common.utils.DialogUtils;
import com.feipulai.common.utils.ToastUtils;
import com.orhanobut.logger.Logger;

/**
 * Created by James on 2018/1/4 0004.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public abstract class ClearDataProcess {
	
	// public static final int CLEAR_RESULTS = 0x1;
	public static final int CLEAR_DATABASE = 0x2;
	public static final int CLEAR_FOR_RESTORE = 0x3;
	
	private Context context;
	private OnProcessFinishedListener listener;
	private int clearType;
	
	public ClearDataProcess(@NonNull Context context, int clearType, OnProcessFinishedListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener can not be null");
		}
		this.listener = listener;
		this.context = context;
		this.clearType = clearType;
	}
	
	public void process() {
		//查看是否有未上传数据
		if (getUnUploadNum() > 0) {
			//有未上传数据,提醒
			showUnUploadDialog();
		} else {
			//提醒会清空本地数据
			showClearWarning();
		}
	}
	
	protected abstract int getUnUploadNum();
	
	private void showUnUploadDialog() {
		new AlertDialog.Builder(context)
				.setTitle("警告")
				.setMessage("本地有未上传数据,是否确定清空本地数据?")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Logger.i("有未上传数据,用户手动确定,下一步-->等待用户确定删除本地数据");
						showClearWarning();
					}
				})
				.setNegativeButton("否", null)
				.show();
	}
	
	private void showClearWarning(){
		switch (clearType) {
			// case CLEAR_RESULTS:
			// 	showAlertClearResultsDialog();
			// 	break;
			
			case CLEAR_DATABASE:
			case CLEAR_FOR_RESTORE:
				showAlertClearDataDialog();
				break;
		}
	}
	
	// private void showAlertClearResultsDialog() {
	// 	new AlertDialog.Builder(context)
	// 			.setTitle("警告")
	// 			.setMessage("该操作会清空本地考生成绩信息,是否确定清空这些信息?")
	// 			.setPositiveButton("是", new DialogInterface.OnClickListener() {
	// 				@Override
	// 				public void onClick(DialogInterface dialog, int which) {
	// 					Logger.i("用户手动确定清除本地成绩信息,下一步-->等待用户输入验证码");
	// 					showAuthCodeDialog();
	// 				}
	// 			})
	// 			.setNegativeButton("否", null)
	// 			.show();
	// }
	
	private void showAlertClearDataDialog() {
		new AlertDialog.Builder(context)
				.setTitle("警告")
				.setMessage("该操作会清空本地考生信息和成绩信息,是否确定清空这些信息?")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Logger.i("用户手动确定清除本地考生信息和成绩信息,下一步-->等待用户输入验证码");
						showAuthCodeDialog();
					}
				})
				.setNegativeButton("否", null)
				.show();
	}
	
	private void showAuthCodeDialog() {
		//每次调用都需要重新生成,因为每次要生成新的验证码
		final EditText editText = new EditText(context);
		//设置只允许输入数字
		editText.setInputType(InputType.TYPE_CLASS_NUMBER);
		editText.setSingleLine();
		editText.setBackgroundColor(0xffcccccc);
		final int authCode = (int) (Math.random() * 9000 + 1000);
		Logger.i("生成验证码:" + authCode);
		new AlertDialog.Builder(context)
				.setTitle("清空本地数据")
				.setView(editText)
				.setMessage("请输入验证码:\n" + authCode)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						verifyAuthCode(dialog, editText.getText().toString().trim(), authCode);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						DialogUtils.setMShowing(dialog, true);
					}
				})
				.show();
	}
	
	private void verifyAuthCode(DialogInterface dialog, String text, int autoCode) {
		if (TextUtils.isEmpty(text)) {
			DialogUtils.setMShowing(dialog, false);
			ToastUtils.showShort("请输入验证码");
			return;
		}
		if (text.equals(autoCode + "")) {
			Logger.i("用户输入了正确的验证码:" + autoCode);
			DialogUtils.setMShowing(dialog, true);
			switch (clearType) {
				// case CLEAR_RESULTS:
				// 	listener.onClearResultsConfirmed();
				// 	break;
				
				case CLEAR_DATABASE:
					listener.onClearDBConfirmed();
					break;
				
				case CLEAR_FOR_RESTORE:
					listener.onRestoreConfirmed();
					break;
			}
		} else {
			DialogUtils.setMShowing(dialog, false);
			ToastUtils.showShort("验证码错误");
		}
	}
	
	public interface OnProcessFinishedListener {
		/**
		 * 确认恢复数据库
		 */
		void onRestoreConfirmed();
		
		/**
		 * 确认清除所有数据
		 */
		void onClearDBConfirmed();
		
		// /**
		//  * 确认清除所有成绩
		//  */
		// void onClearResultsConfirmed();
	}
	
}
