package com.feipulai.host.view;
/**
 * 深圳市菲普莱体育发展有限公司		秘密级别：绝密
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.feipulai.host.R;


/**
 * 自定义的覆盖视图
 * 需要增加一些功能,如设置字体大小、颜色
 */
public class OperateProgressBar{

	private static final int LOADING_VIEW_ID = R.id.id_ui_progress_loading_data;
	//该layout大小为屏幕大小，所以可以屏蔽触摸事件，将所有的触摸时间均分发至该view中
	private static final int LOADING_VIEW_LAYOUT_ID = R.layout.progress_view_loading_data;
	private static final int ACTIVITY_ROOT_VIEW_ID = android.R.id.content;
	private static final int DEFAULT_BG_COLOR = Color.TRANSPARENT;
	private static final String DEFAULT_MESSAGE = "正在加载数据";

	private OperateProgressBar() {
	}

	/**
	 * 在Acticity中添加一个View以提示正在加载数据
	 */
	public static boolean showLoadingUi(Activity activity) {
		return showLoadingUi(activity, DEFAULT_MESSAGE);
	}

	public static boolean showLoadingUi(Activity activity, String message) {
		return showLoadingUi(activity, message, DEFAULT_BG_COLOR);
	}

	/**
	 * @param activity Activity
	 * @param message  提示文字
	 * @param backgroundColor 背景色
	 */
	public static boolean showLoadingUi(Activity activity, String message, int backgroundColor){
		//子view屏蔽事件绑定
		ViewGroup view = (ViewGroup) LayoutInflater.from(activity).inflate(LOADING_VIEW_LAYOUT_ID, getActivityRootLayout(activity), true);
		view.setBackgroundColor(backgroundColor);
		((TextView) view.findViewById(R.id.loading_process_dialog_txt)).setText(message);
		return true;
	}

	/**
	 * 如果进度条存在，移除之
	 * @return 执行移除返回true,否则返回false
	 */
	public static boolean removeLoadingUiIfExist(final Activity activity) {
		final View view = activity.findViewById(LOADING_VIEW_ID);
		if (view != null) {
			ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).setDuration(500);
			animator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					getActivityRootLayout(activity).removeView(view);
					//再检查一次
					View v = activity.findViewById(LOADING_VIEW_ID);
					if(v != null){
						getActivityRootLayout(activity).removeView(v);
					}
					super.onAnimationEnd(animation);
				}
			});
			animator.start();
			return true;
		}
		return false;
	}
	/**
	 * 获取Activity的根视图Framlayout
	 */

	public static FrameLayout getActivityRootLayout(Activity activity) {
		return (FrameLayout) activity.findViewById(ACTIVITY_ROOT_VIEW_ID);
	}

}
