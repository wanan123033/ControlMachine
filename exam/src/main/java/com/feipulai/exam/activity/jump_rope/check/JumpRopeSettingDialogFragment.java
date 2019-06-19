package com.feipulai.exam.activity.jump_rope.check;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by James on 2019/2/12 0012.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class JumpRopeSettingDialogFragment extends DialogFragment
		implements AdapterView.OnItemSelectedListener,
		TextWatcher{
	
	Unbinder unbinder;
	@BindView(R.id.btn_show_judgements)
	Button mBtnShowJudgements;
	@BindView(R.id.sp_device_num)
	Spinner mSpDeviceNum;
	@BindView(R.id.sp_test_vez)
	Spinner mSpTestVez;
	@BindView(R.id.rg_model)
	RadioGroup mRgModel;
	@BindView(R.id.ll_group_pattern)
	LinearLayout mLlGroupPattern;
	@BindView(R.id.et_test_time)
	EditText mNpSecond;
	
	private Integer[] testNos;
	private JumpRopeSetting setting;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setCancelable(false);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,
	                         Bundle savedInstanceState){
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		View rootView = inflater.inflate(R.layout.activity_radio_setting,container);
		unbinder = ButterKnife.bind(this,rootView);
		setting = SharedPrefsUtil.loadFormSource(getActivity(),JumpRopeSetting.class);
		init();
		return rootView;
	}
	
	private void init(){
		if(SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN){
			mLlGroupPattern.setVisibility(View.GONE);
		}
		
		int maxTestNo = TestConfigs.sCurrentItem.getTestNum();
		mNpSecond.setText(setting.getTestTime() + "");
		mNpSecond.addTextChangedListener(this);
		
		testNos = new Integer[TestConfigs.MAX_TEST_NO];
		for(int i = 1;i <= testNos.length;i++){
			testNos[i - 1] = i;
		}
		mSpTestVez.setAdapter(new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item,testNos));
		if(maxTestNo != 0){
			// 数据库中已经指定了测试次数,就不能再设置了
			mSpTestVez.setEnabled(false);
		}else{
			maxTestNo = TestConfigs.getMaxTestCount(getActivity());
			mSpTestVez.setOnItemSelectedListener(this);
		}
		mSpTestVez.setSelection(maxTestNo - 1);
		
		mSpDeviceNum.setAdapter(new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item,testNos));
		mSpDeviceNum.setSelection(setting.getDeviceSum() - 1);
		mSpDeviceNum.setOnItemSelectedListener(this);
		
		mRgModel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup group,int checkedId){
				switch(checkedId){
					case R.id.rb_successive:
						setting.setGroupMode(JumpRopeSetting.GROUP_PATTERN_SUCCESIVE);
						break;
					
					case R.id.rb_loop:
						setting.setGroupMode(JumpRopeSetting.GROUP_PATTERN_LOOP);
						break;
				}
			}
		});
		if(setting.getGroupMode() == JumpRopeSetting.GROUP_PATTERN_SUCCESIVE){
			mRgModel.check(R.id.rb_successive);
		}else{
			mRgModel.check(R.id.rb_loop);
		}
		
		
	}
	
	@Override
	public void onPause(){
		super.onPause();
		SharedPrefsUtil.save(getActivity(),setting);
	}
	
	@OnClick(R.id.btn_show_judgements)
	public void onViewClicked(){}
	
	@Override
	public void onItemSelected(AdapterView<?> parent,View view,int position,long id){
		switch(parent.getId()){
			case R.id.sp_device_num:
				setting.setDeviceSum(testNos[position]);
				break;
			
			case R.id.sp_test_vez:
				setting.setTestNo(testNos[position]);
				break;
		}
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> parent){
	}
	
	@Override
	public void beforeTextChanged(CharSequence s,int start,int count,int after){
	}
	
	@Override
	public void onTextChanged(CharSequence s,int start,int before,int count){
	}
	
	@Override
	public void afterTextChanged(Editable s){
		String testTimeStr = mNpSecond.getText().toString().trim();
		if(!TextUtils.isEmpty(testTimeStr)){
			int testTime = Integer.parseInt(testTimeStr);
			if(testTime < 10){
				ToastUtils.showShort("测试时长不能小于10秒");
				return;
			}
			setting.setTestTime(testTime);
		}
	}
	
}
