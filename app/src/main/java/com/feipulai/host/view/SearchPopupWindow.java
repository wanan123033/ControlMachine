package com.feipulai.host.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.feipulai.host.adapter.SearchAdapter;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Student;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2017/12/19.
 */

public class SearchPopupWindow extends PopupWindow {

	private Context mContext;
	private SearchAdapter searchAdapter = null;
	private List<Student> students;
	private ListView lv;
	private EditText etSelect;
	private PopupWindow mPopupWindow;
	private String text;
	/**
	 * 回调接口对象
	 */
	private OnPopupWindowClickListener listener;

	public SearchPopupWindow() {
	}


	private static class Singleholder {
		private static SearchPopupWindow instance = new SearchPopupWindow();

	}

	public static SearchPopupWindow getInstance() {
		return Singleholder.instance;
	}

	public void showPopup(Context context, EditText et, String string) {
		mContext = context;
		etSelect = et;
		text = string;
		if (text.length() > 6 && text.length() < 18) {
			students = DBManager.getInstance().fuzzyQueryByStuCode(text,0,100);
			Log.e(TAG, "showPopup--------------------------: " + students.size());
			if (students == null || students.isEmpty()) {
				changeData();
			} else {
				initView();
			}
		} else if (text.length() == 18) {
			Student student = DBManager.getInstance().queryStudentByIDCode(text);
			students.add(student);
			if (students == null || students.isEmpty()) {
				changeData();
			} else {
				initView();
			}
		} else {
			students = null;
			if (mPopupWindow != null) {
				changeData();
			}
		}
	}

	private void initView() {
		Log.e(TAG, "initView: " + "1111111111111111111111111111");
		lv = new ListView(mContext);
		searchAdapter = new SearchAdapter(mContext, students);
		lv.setAdapter(searchAdapter);

		//每次new pop之前关闭前一个pop-------------需解决重复new pop来更新显示数据问题
		dismiss();
//		if (mPopupWindow==null||!mPopupWindow.isShowing()) {
		//创建POP
		mPopupWindow = new PopupWindow(lv, etSelect.getWidth(), 350);//editText的宽度
		mPopupWindow.setFocusable(false);
		//设置颜色
		mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		//设置外部点击消失
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAsDropDown(etSelect, 0, 0);
//		}

		searchAdapter.setPopuClickListener(new SearchAdapter.PopClickListener() {
			@Override
			public void PopuClickListener(View v, Student student, int position) {
				dismiss();
				if (listener != null) {
					listener.onPopupWindowItemClick(student);
				}
			}
		});

	}

	/**
	 * 为PopupWindow设置回调接口
	 *
	 * @param listener
	 */
	public void setOnPopupWindowClickListener(OnPopupWindowClickListener listener) {
		this.listener = listener;
	}


	/**
	 * 设置数据的方法，供外部调用
	 */
	public void changeData() {
		if (students == null || students.isEmpty()) {
			Log.e(TAG, "changeData: " + "11111111111111111111");
			dismiss();
		} else {
			Log.e(TAG, "changeData: " + "22222222222222222222222");
//			students.addAll(students);
			if (!mPopupWindow.isShowing()) {
				mPopupWindow.showAsDropDown(etSelect, 0, 0);
			}
		}
		if (searchAdapter != null) {
			searchAdapter.notifyDataSetChanged();
		}
	}

	public void dismiss() {
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
		}
	}


	/**
	 * 回调接口.供外部调用
	 *
	 * @author xiaanming
	 */
	public interface OnPopupWindowClickListener {
		/**
		 * 当点击PopupWindow的ListView 的item的时候调用此方法，用回调方法的好处就是降低耦合性
		 *
		 * @param stu 位置
		 */
		void onPopupWindowItemClick(Student stu);
	}
}
