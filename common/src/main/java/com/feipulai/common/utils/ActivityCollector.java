package com.feipulai.common.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 2017/11/22.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ActivityCollector{
	
	private static List<Activity> mActivityList = new ArrayList<>();
	
	public static void addActivity(Activity activity){
		mActivityList.add(activity);
	}
	
	public static void removeActivity(Activity actuvity){
		mActivityList.remove(actuvity);
	}
	
	public static void finishAll(){
		for(Activity activity : mActivityList){
			if(!activity.isFinishing()){
				// activity.finish方法调用时,只是将activity移出栈,至于何时activity回收,由系统决定
				// 所以不能调用activity.finish方法后立即调用System.exit(0)，否则activity生命周期不会走完
				activity.finish();
			}
		}
		//Logger.d("application exiting");
		//System.exit(0);
	}
	
}
