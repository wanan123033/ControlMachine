package com.feipulai.host.activity.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.SwipeRefreshView;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseCheckActivity;
import com.feipulai.host.adapter.DataRetrieveAdapter;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.netapi.ItemSubscriber;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * 数据查询
 * Created by james on 2018/1/2.
 */
public class DataRetrieveActivity extends BaseCheckActivity
		implements SwipeRefreshLayout.OnRefreshListener,
		SwipeRefreshView.OnLoadMoreListener,
		AdapterView.OnItemClickListener {
	
	public static final String DATA_EXTRA = "data_extra";
	public final static String UPDATE_MESSAGE = "com.feipulai.host.update_data_message";
	
	@BindView(R.id.rb_all)
	RadioButton mRbAll;
	@BindView(R.id.et_input_text)
	EditText mEtInputText;
	@BindView(R.id.btn_query)
	Button mBtnQuery;
	@BindView(R.id.btn_upload)
	Button mBtnUpload;
	@BindView(R.id.cb_select_all)
	CheckBox mCbSelectAll;
	@BindView(R.id.lv_results)
	ListView mLvResults;
	@BindView(R.id.swiperefresh)
	SwipeRefreshView mSwiperefresh;
	@BindView(R.id.cb_un_upload)
	CheckBox cbUnUpload;
	@BindView(R.id.cb_uploaded)
	CheckBox cbUploaded;
	@BindView(R.id.cb_tested)
	CheckBox cbTested;
	@BindView(R.id.cb_un_tested)
	CheckBox cbUnTested;
	@BindView(R.id.txt_stu_sumNumber)
	TextView txtStuSumNumber;
	@BindView(R.id.txt_stu_manNumber)
	TextView txtStuManNumber;
	@BindView(R.id.txt_stu_womemNumber)
	TextView txtStuWomemNumber;
	
	private DataRetrieveAdapter mAdapter;
	private List<DataRetrieveBean> mList;
	private static final int LOAD_ITEMS = 100;
	private int mPageNum;
	
	public BroadcastReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_retrieve);
		ButterKnife.bind(this);
		String machineName = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode());
		setTitle("数据查看 - " + machineName);
		setAddable(false);
		registerReceiver();
		
		mSwiperefresh.setColorSchemeResources(R.color.swipe_color_1, R.color.swipe_color_2, R.color.swipe_color_3, R.color.swipe_color_4);
		mSwiperefresh.setOnRefreshListener(this);
		mSwiperefresh.setOnLoadMoreListener(this);
		mLvResults.setOnItemClickListener(this);
		
		setListAdapter();
		setAllList();
	}
	
	@OnTextChanged(value = {R.id.et_input_text}, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
	public void onTextChanged(Editable s) {
		if (s.length() == 0) {
			setCheckStates(true, false, false, false, false);
			mPageNum = 0;
			mList.clear();
			setAllList();
			mAdapter.notifyDataSetChanged();
		}
	}
	
	@OnClick({R.id.btn_query, R.id.btn_upload, R.id.rb_all})
	public void onViewClicked(View view) {
		switch (view.getId()) {
			
			case R.id.btn_query:
				String input = mEtInputText.getText().toString().trim();
				mPageNum = 0;
				queryStudent(input);
				break;
			
			case R.id.btn_upload:
				List<RoundResult> roundResults = new ArrayList<>();
				for (int i = 0; i < mList.size(); i++) {
					DataRetrieveBean bean = mList.get(i);
					if (bean.isChecked() && bean.getTestState() == 1) {
						List<RoundResult> results = DBManager.getInstance().queryUploadStudentResults(bean.getStudentCode(), false);
						roundResults.addAll(results);
					}
				}
				if (roundResults.size() == 0) {
					ToastUtils.showShort("请选择未上传成绩考生");
				} else {
					//上传数据前先进行项目信息校验
					ItemSubscriber subscriber = new ItemSubscriber();
					subscriber.getItemAll(MyApplication.TOKEN, this, null, roundResults);
				}
				break;
			
			case R.id.rb_all:
				if (mRbAll.isChecked()) {
					setCheckStates(true, false, false, false, false);
					setAllList();
				}
				break;
			
		}
	}
	
	@OnCheckedChanged({R.id.cb_select_all, R.id.cb_uploaded, R.id.cb_un_upload, R.id.cb_un_tested, R.id.cb_tested})
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		switch (buttonView.getId()) {
			
			case R.id.cb_select_all:
				for (DataRetrieveBean bean : mList) {
					bean.setChecked(isChecked);
				}
				mAdapter.notifyDataSetChanged();
				break;
			
			case R.id.cb_tested://已测
				if (isChecked) {
					mRbAll.setChecked(false);
					cbUnTested.setChecked(false);
				}
				break;
			
			case R.id.cb_un_tested://未测
				if (isChecked) {
					mRbAll.setChecked(false);
					cbTested.setChecked(false);
				}
				break;
			
			case R.id.cb_uploaded://已上传
				if (isChecked) {
					mRbAll.setChecked(false);
					cbUnUpload.setChecked(false);
				}
				break;
			
			case R.id.cb_un_upload://未上传
				if (isChecked) {
					mRbAll.setChecked(false);
					cbUploaded.setChecked(false);
				}
				break;
			
		}
		//非选择全部的复选框
		if (buttonView.getId() == R.id.cb_select_all) {
			return;
		}
		mPageNum = 0;
		//刷选条件必须至少有一个
		if (cbUploaded.isChecked() || cbUnUpload.isChecked() || cbTested.isChecked() || cbUnTested.isChecked()) {
			//选择未测，证明没有成绩，所有选择已上传与未上传都是空列表
			if (cbUnTested.isChecked() && (cbUploaded.isChecked() || cbUnUpload.isChecked())) {
				mList.clear();
			} else {
				chooseStudent();
			}
		} else {
			// 没有选择任何条件
			mRbAll.setChecked(true);
			setAllList();
		}
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onCheckIn(Student student) {
		mList.clear();
		addToList(student);
	}
	
	private void setCheckStates(boolean all, boolean unUpload, boolean uploaded, boolean tested, boolean untested) {
		mRbAll.setChecked(all);
		cbUnUpload.setChecked(unUpload);
		cbUploaded.setChecked(uploaded);
		cbTested.setChecked(tested);
		cbUnTested.setChecked(untested);
	}
	
	private void addToList(Student student) {
		List<RoundResult> lastScore = DBManager.getInstance().queryResultsByStudentCode(student.getStudentCode());
		addToList(student, lastScore != null && lastScore.size() > 0);
	}
	
	private void addToList(Student student, boolean tested) {
		mList.add(new DataRetrieveBean(student.getStudentCode(),
				student.getStudentName(),
				student.getSex(),
				tested ? 1 : 0));
		mAdapter.notifyDataSetChanged();
	}
	
	// 查找所有学生信息
	private void setAllList() {
		List<Student> studentList = DBManager.getInstance().getItemStudent(TestConfigs.sCurrentItem.getMachineCode(),
				LOAD_ITEMS, LOAD_ITEMS * mPageNum);
		if (mPageNum == 0) {
			mList.clear();
			Map<String, Object> countMap = DBManager.getInstance().getItemStudenCount();
			setStuCount(countMap.get("count"), countMap.get("man_count"), countMap.get("women_count"));
			Logger.i("zzs===>" + countMap.toString());
		}
		if (studentList == null || studentList.size() == 0) {
			ToastUtils.showShort("没有更多数据了");
			return;
		}
		//这个必须在获取到数据后再自增
		mPageNum++;
		for (Student student : studentList) {
			addToList(student);
		}
	}
	
	// 模糊查询学生信息
	private void queryStudent(String inputText) {
		if (TextUtils.isEmpty(inputText)) {
			ToastUtils.showShort("请输入搜索内容");
			return;
		}
		List<Student> students = DBManager.getInstance().fuzzyQueryByStuCode(inputText, LOAD_ITEMS, LOAD_ITEMS *
				mPageNum);
		
		if (students == null || students.size() == 0) {
			ToastUtils.showShort(mPageNum == 0 ? "该考生不存在" : "没有更多数据了");
			return;
		} else if (mPageNum == 0) {
			mList.clear();
		}
		mPageNum++;
		for (Student student : students) {
			addToList(student);
		}
		mAdapter.notifyDataSetChanged();
	}
	
	private void setListAdapter() {
		mList = new ArrayList<>();
		mAdapter = new DataRetrieveAdapter(this, mList/*,checkArray*/);
		mLvResults.setAdapter(mAdapter);
	}
	
	// 根据条件,更新考生信息
	private void chooseStudent() {
		List<Student> studentList = DBManager.getInstance().getChooseStudentList(TestConfigs.getCurrentItemCode(),
				cbTested.isChecked(), cbUnTested.isChecked(), cbUploaded.isChecked(), cbUnUpload.isChecked(),
				LOAD_ITEMS, LOAD_ITEMS * mPageNum);
		if (mPageNum == 0) {
			mList.clear();
			Map<String, Object> countMap = DBManager.getInstance().getChooseStudentCount(cbTested.isChecked(), cbUnTested.isChecked(),
					cbUploaded.isChecked(), cbUnUpload.isChecked());
			setStuCount(countMap.get("count"), countMap.get("man_count"), countMap.get("women_count"));
			Logger.i("zzs===>" + countMap.toString());
		}
		if (studentList == null || studentList.size() == 0) {
			ToastUtils.showShort("没有更多数据了");
			return;
		}
		mPageNum++;
		boolean tested = cbTested.isChecked() || (cbUploaded.isChecked() || cbUnUpload.isChecked());
		for (Student student : studentList) {
			addToList(student, tested);
		}
	}
	
	private void setStuCount(Object sumCount, Object mamCount, Object womenCount) {
		txtStuSumNumber.setText("总数：" + sumCount);
		txtStuManNumber.setText("男：" + mamCount);
		txtStuWomemNumber.setText("女：" + womenCount);
	}
	
	@Override
	public void onRefresh() {
		// Stop the refreshing indicator
		mSwiperefresh.setRefreshing(false);
	}
	
	@Override
	public void onLoadMore() {
		//如果这里设置adapter,会导致每次都自动跳转到第一个item显示
		//如果这里不设置adapter,那么在SwipeRefreshView中的mListView.addFooterView(mFooterView)无效,
		//mListView.removeFooterView(mFooterView)会抛出异常导致奔溃
		//暂时的解决方案:我们对mFooterView的需求并不高,直接去掉就行,这里直接notifyDataSetChanged,跳转的问题也解决了
		//setListAdapter();
		if (TextUtils.isEmpty(mEtInputText.getText().toString().trim())) {
			if (mRbAll.isChecked()) {
				setAllList();
			} else {
				chooseStudent();
			}
		} else {
			queryStudent(mEtInputText.getText().toString().trim());
		}
		mAdapter.notifyDataSetChanged();
		// 加载完数据设置为不加载状态,将加载进度收起来
		mSwiperefresh.setLoading(false);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, DataDisplayActivity.class);
		intent.putExtra(DATA_EXTRA, mList.get(position));
		startActivity(intent);
		Logger.i("onItemClick:" + position);
	}
	
	private void registerReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UPDATE_MESSAGE);
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(UPDATE_MESSAGE)) {
					mPageNum = 0;
					onLoadMore();
				}
			}
		};
		registerReceiver(receiver, intentFilter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}
	
	@Override
	public void onWrongLength(int length, int expectLength) {
	
	}
}
