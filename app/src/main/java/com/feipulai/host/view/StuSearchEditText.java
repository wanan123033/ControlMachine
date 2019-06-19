package com.feipulai.host.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.feipulai.host.activity.base.BaseCheckActivity;
import com.feipulai.host.adapter.SearchResultAdapter;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Student;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 2018/2/8 0008.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

/**
 * 用于输入学号或身份证号,同时实时显示学生信息结果到指定的{@link ListView}中
 * note:必须指定显示数据的listview和activity,机必须调用{@link #setData(ListView, BaseCheckActivity)}
 */
@SuppressLint("AppCompatCustomView")
public class StuSearchEditText extends EditText {
	
	private ListView mLvResults;
	private List<Student> mStudentList;
	private BaseCheckActivity mActivity;
	private RecyclerView mRecyclerView;
	
	public StuSearchEditText(Context context) {
		super(context);
		init();
	}
	
	public StuSearchEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public StuSearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	private void init() {
		setInputType(EditorInfo.TYPE_CLASS_NUMBER);
		setImeOptions(EditorInfo.IME_ACTION_GO);
		setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO
						|| (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					if (!TextUtils.isEmpty(getText().toString()) && (mStudentList != null && mStudentList.size() == 0)) {
						Student student = new Student();
						student.setStudentCode(getText().toString());
						showAddHint(student);
					}
					
					return true;
				}
				return false;
			}
		});
		addTextChangedListener(new TextWatcher() {
			//input deals
			//CharSequence s参数表示当前TextView内部的mText成员变量，实际上就是当前显示的文本；
			//int start参数表示需要改变的文字区域的起点，即选中的文本区域的起始点；
			//int count参数表示需要改变的文字的字符数目，即选中的文本区域的字符的数目；
			//int after参数表示替换的文字的字符数目。
			//特别的，当TextView删除文本的时候，after的值为0，此时TextView使用用空字符串代替需要改变的文字区域来达到删除文字的目的。
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			//CharSequence s参数表示当前TextView内部的mText成员变量，此时的mText已经被修改过了，但此时mText所表示的文本还没有被显示到UI组件上;
			//int start参数表示改变的文字区域的起点;
			//int before参数表示改变的文字区域在改变前的旧的文本长度，即选中文字区域的文本长度；
			//int count参数表示改变的文字区域在修改后的新的文本长度。
			//特别的，当TextView添加文本的时候，before 的值为0，此时相当于TextView将空的字符区域用新的文本代替。
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				int length = s.length();
				String str = s.toString();
				SearchResultAdapter adapter;
				if (mLvResults != null) {
					if (length == 0) {
						mLvResults.setVisibility(View.GONE);
						return;
					}
					if (length == 18) {
						//精确搜索身份证
						Student student = DBManager.getInstance().queryStudentByIDCode(str);
						mStudentList = new ArrayList<>();
						if (student != null) {
							mStudentList.add(student);
						}
					} else {
						//模糊搜索考号
						mStudentList = DBManager.getInstance().fuzzyQueryByStuCode(str, 100, 0);
					}
					if (mStudentList == null || mStudentList.size() == 0) {
						mLvResults.setVisibility(View.GONE);
					} else {
						adapter = new SearchResultAdapter(getContext(), mStudentList);
						mLvResults.setAdapter(adapter);
						mLvResults.setVisibility(View.VISIBLE);
						if (mRecyclerView != null) {
							mRecyclerView.setVisibility(GONE);
						}
					}
				}
			}
			
		});
	}
	
	private void showAddHint(final Student student) {
		new AlertDialog.Builder(mActivity)
				.setCancelable(false)
				.setTitle("提示")
				.setMessage("无考生信息，是否新增")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new AddStudentDialog(mActivity).showDialog(student, false);
					}
				})
				.setNegativeButton("否", null)
				.show();
		
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_STAR) {
			append("X");
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	
	public void setData(ListView listView, BaseCheckActivity activity) {
		mActivity = activity;
		mLvResults = listView;
		mLvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Student student = mStudentList.get(position);
				//Log.i("james","position:" + position + "mStudentList:" + mStudentList.toString());
				// showInput(false);
				mActivity.checkInput(student);
				Logger.i("input student code:" + student.getStudentCode());
				setText("");
			}
		});
	}
	
	public void setData(RecyclerView recyclerView, ListView listView, BaseCheckActivity activity) {
		mRecyclerView = recyclerView;
		mActivity = activity;
		mLvResults = listView;
		mLvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Student student = mStudentList.get(position);
				//Log.i("james","position:" + position + "mStudentList:" + mStudentList.toString());
				// showInput(false);
				mActivity.checkInput(student);
				Logger.i("input student code:" + student.getStudentCode());
				setText("");
			}
		});
		
	}
	
	/**
	 * 是否关闭键盘
	 *
	 * @param show true 显示， false 关闭键盘
	 */
	public void showInput(boolean show) {
		try {
			if (show) {
				InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInputFromInputMethod(mActivity.getCurrentFocus().getApplicationWindowToken(), 0);
			} else {
				InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mActivity.getCurrentFocus().getApplicationWindowToken(), 0);
			}
		} catch (NullPointerException e1) {
		
		} catch (Exception e) {
		}
	}
}
