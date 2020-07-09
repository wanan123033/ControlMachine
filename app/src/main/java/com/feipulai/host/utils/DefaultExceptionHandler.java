package com.feipulai.host.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.feipulai.host.MyApplication;
import com.feipulai.host.activity.SplashScreenActivity;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2018/1/26.
 * notice:程序崩溃重启
 */

public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {
	private Thread.UncaughtExceptionHandler defaultUEH;
	Activity activity;

	public DefaultExceptionHandler(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {

		Intent intent = new Intent(activity, SplashScreenActivity.class);

		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);


		PendingIntent pendingIntent = PendingIntent.getActivity(
				MyApplication.getInstance().getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		//Following code will restart your application after 2 seconds
		AlarmManager mgr = (AlarmManager) MyApplication.getInstance().getBaseContext()
				.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
				pendingIntent);

		//This will finish your activity manually
		activity.finish();

		//This will stop your application and take out from it.
		System.exit(2);
	}
}
