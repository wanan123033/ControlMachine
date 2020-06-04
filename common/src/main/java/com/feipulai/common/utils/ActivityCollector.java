package com.feipulai.common.utils;

import android.app.Activity;

import java.util.Iterator;
import java.util.Stack;

/**
 * Created by James on 2017/11/22.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ActivityCollector{

	/**
	 * 管理类对象
	 */
	private static ActivityCollector mActivityCollector;
	/**
	 * 打开的activity
	 */
	private static Stack<Activity> mStackActivity;


	public ActivityCollector(){
		mStackActivity = new Stack<>();
	}


	public static ActivityCollector getInstance()
	{
		if (mActivityCollector == null)
		{
			synchronized (ActivityCollector.class)
			{
				if (mActivityCollector == null)
				{
					mActivityCollector = new ActivityCollector();
				}
			}
		}
		return mActivityCollector;
	}


	public void onCreate(Activity activity)
	{
		addActivity(activity);
	}

	public void onResume(Activity activity)
	{
		addActivity(activity);
	}

	public void onDestroy(Activity activity)
	{
		removeActivity(activity);
	}

	/**
	 * 新建了一个activity
	 * @param activity
	 */
	private void addActivity(Activity activity)
	{
		if (!mStackActivity.contains(activity))
		{
			mStackActivity.add(activity);
		}
	}

	private void removeActivity(Activity activity)
	{
		if (activity != null&& mStackActivity.contains(activity))
		{
			mStackActivity.remove(activity);
		}
	}

	public Activity getLastActivity()
	{
		Activity activity = null;
		try
		{
			activity = mStackActivity.lastElement();
		} catch (Exception e)
		{
		}
		return activity;
	}

	public boolean isLastActivity(Activity activity)
	{
		if (activity != null)
		{
			return getLastActivity() == activity;
		} else
		{
			return false;
		}
	}

	public boolean isLastActivity(Class<?> cls)
	{
		if (cls != null)
		{
			return getLastActivity().getClass() == cls;
		} else
		{
			return false;
		}
	}

	public boolean isEmpty()
	{
		return mStackActivity.isEmpty();
	}

	/**
	 * 结束指定类名的Activity
	 */
	public void finishActivity(Class<?> cls)
	{
		Iterator<Activity> it = mStackActivity.iterator();
		while (it.hasNext())
		{
			Activity act = it.next();
			if (act.getClass() == cls)
			{
				it.remove();
				act.finish();
			}
		}
	}

	public void finishAllClassActivityExcept(Activity activity)
	{
		Iterator<Activity> it = mStackActivity.iterator();
		while (it.hasNext())
		{
			Activity act = it.next();
			if (act.getClass() == activity.getClass() && act != activity)
			{
				it.remove();
				act.finish();
			}
		}
	}

	public void finishAllActivity()
	{
		Iterator<Activity> it = mStackActivity.iterator();
		while (it.hasNext())
		{
			Activity act = it.next();
			it.remove();
			act.finish();
		}
	}

	public void finishAllActivityExcept(Class<?> cls)
	{
		Iterator<Activity> it = mStackActivity.iterator();
		while (it.hasNext())
		{
			Activity act = it.next();
			if (act.getClass() != cls)
			{
				it.remove();
				act.finish();
			}
		}
	}

	public void finishAllActivityExcept(Activity activity)
	{
		Iterator<Activity> it = mStackActivity.iterator();
		while (it.hasNext())
		{
			Activity act = it.next();
			if (act != activity)
			{
				it.remove();
				act.finish();
			}
		}
	}
	
}
