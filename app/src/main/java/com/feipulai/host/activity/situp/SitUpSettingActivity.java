package com.feipulai.host.activity.situp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.utils.SharedPrefsUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SitUpSettingActivity extends BaseActivity implements NumberPicker.OnValueChangeListener, AdapterView.OnItemSelectedListener{
	
	@BindView(R.id.btn_show_judgements)
	Button mBtnShowJudgements;
	@BindView(R.id.sp_device_num)
	Spinner mSpDeviceNum;
	@BindView(R.id.np_minute)
	NumberPicker mNpMinute;
	@BindView(R.id.np_second)
	NumberPicker mNpSecond;
	
	private int testTime;
	private Integer[] spContents;
	private int deviceNumber;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jump_rope_setting);
		ButterKnife.bind(this);
		init();
	}
	
	private void init(){
		testTime = SharedPrefsUtil.getValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.SIT_UP_TEST_TIME,30);
		deviceNumber = SharedPrefsUtil.getValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.SIT_UP_TEST_NUMBER,20);
		
		//设置np1的最小值和最大值
		mNpMinute.setMinValue(0);
		mNpMinute.setMaxValue(59);
		
		mNpSecond.setMinValue(0);
		mNpSecond.setMaxValue(59);
		
		mNpMinute.setValue(testTime / 60);
		mNpSecond.setValue(testTime % 60);
		
		mNpMinute.setOnValueChangedListener(this);
		mNpSecond.setOnValueChangedListener(this);
		
		spContents = new Integer[99];
		for(int i = 1;i <= spContents.length;i++){
			spContents[i - 1] = i;
		}
		
		mSpDeviceNum.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,spContents));
		mSpDeviceNum.setSelection(deviceNumber - 1);
		mSpDeviceNum.setOnItemSelectedListener(this);
	}
	
	@OnClick(R.id.btn_show_judgements)
	public void onViewClicked(){}
	
	@Override
	public void onValueChange(NumberPicker picker,int oldVal,int newVal){
		testTime = mNpMinute.getValue() * 60 + mNpSecond.getValue();
		SharedPrefsUtil.putValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.SIT_UP_TEST_TIME,testTime);
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent,View view,int position,long id){
		SharedPrefsUtil.putValue(SitUpSettingActivity.this,SharedPrefsConfigs.DEFAULT_PREFS,
				SharedPrefsConfigs.SIT_UP_TEST_NUMBER,
				spContents[position]);
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> parent){
	}
	
}
