package com.feipulai.host.activity.height_weight;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.HeightWeightResult;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.activity.base.BaseCheckActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.netapi.ItemSubscriber;
import com.feipulai.host.utils.PrinterUtils;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.feipulai.host.utils.ResultUtils;
import com.feipulai.host.view.StuSearchEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by James on 2018/9/25 0025.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class HeightWeightCheckActivity
		extends BaseCheckActivity implements SerialDeviceManager.RS232ResiltListener{
	
	private static final int UPDATE_NEW_RESULT = 0x1;
	private static final int PREPARE_FOR_CHECK_IN = 0x2;
	
	@BindView(R.id.et_input_text)
	StuSearchEditText mEtInputText;
	@BindView(R.id.ll_search)
	LinearLayout mLlSearch;
	@BindView(R.id.lv_results)
	ListView mLvResults;
	//@BindView(R.id.tv_led_setting)
	//TextView mTvLedSetting;
	@BindView(R.id.tv_student_code)
	TextView tvStudentCode;
	@BindView(R.id.tv_student_name)
	TextView tvStudentName;
	@BindView(R.id.tv_gender)
	TextView tvGender;
	@BindView(R.id.tv_exist_height)
	TextView tvExistHeight;
	@BindView(R.id.tv_exist_weight)
	TextView tvExistWeight;
	@BindView(R.id.tv_height_result)
	TextView tvHeightResult;
	@BindView(R.id.tv_weight_result)
	TextView tvWeightResult;
	@BindView(R.id.ll_test)
	LinearLayout llTest;
	@BindView(R.id.btn_confirm)
	Button btnConfirm;
	
	// 身高体重LED显示暂时不做
	//private LEDManager mLEDManager = new LEDManager();

	private Student mStudent;
	private volatile boolean isTesting;// 是否正在测试中(检录成功了等待测试成绩)
	private volatile boolean isTestFinished;// 是否已经获得了成绩,测试已经完成,正在展示成绩信息,等待用户点击确认
	private Handler mHandler = new BaseActivity.MyHandler(this);
	private volatile RoundResult mHeightResult;
	private volatile RoundResult mWeightResult;
	private RoundResult mLastHeightResult;
	private RoundResult mLastWeightResult;
	private ItemSubscriber itemSubscriber;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_height_weight_check);
		ButterKnife.bind(this);
		init();
		prepareForCheckIn();
		String string = null;
		string.toCharArray();
	}
	
	private void init(){
		if(SettingHelper.getSystemSetting().isRtUpload()){
			itemSubscriber = new ItemSubscriber();
		}
		mEtInputText.setData(mLvResults,this);
		itemSubscriber = new ItemSubscriber();
	}
	
	private void prepareForCheckIn(){
		isTesting = false;
		isTestFinished = false;
		mLlSearch.setVisibility(View.VISIBLE);
		llTest.setVisibility(View.GONE);
	}
	
	private void prepareForTest(){
		isTesting = true;
		isTestFinished = false;
		mLastHeightResult = null;
		mLastWeightResult = null;
		
		mLastHeightResult = DBManager.getInstance().queryLastScoreByStuCode(mStudent.getStudentCode(),HWConfigs
				.HEIGHT_ITEM);
		mLastWeightResult = DBManager.getInstance().queryLastScoreByStuCode(mStudent.getStudentCode(),HWConfigs.WEIGHT_ITEM);
		
		String displayHeight = "";
		String displayWeight = "";
		if(mLastHeightResult != null){
			displayHeight = ResultDisplayUtils.getStrResultForDisplay(mLastHeightResult.getResult(),HWConfigs.HEIGHT_ITEM);
			displayWeight = ResultDisplayUtils.getStrResultForDisplay(mLastWeightResult.getResult(),HWConfigs.WEIGHT_ITEM);
		}
		tvExistHeight.setText(displayHeight);
		tvExistWeight.setText(displayWeight);
		
		tvHeightResult.setText("");
		tvWeightResult.setText("");
		
		tvGender.setText(mStudent.getSex() == Student.MALE ? "男" : "女");
		tvStudentCode.setText(mStudent.getStudentCode());
		tvStudentName.setText(mStudent.getStudentName());
		
		mLlSearch.setVisibility(View.GONE);
		llTest.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		SerialDeviceManager.getInstance().setRS232ResiltListener(this);
		//mLEDManager.resetLEDScreen(hostId,TestConfigs.machineNameMap.get(ItemDefault.CODE_HW));
	}
	
	@Override
	public void onCheckIn(Student student){
		// 正在测试的话就不理会检录信息
		if(isTesting){
			return;
		}
		mStudent = student;
		prepareForTest();
	}
	
	@Override
	protected void handleMessage(Message msg){
		switch(msg.what){
			
			case UPDATE_NEW_RESULT:
				String displayHeight = ResultDisplayUtils.getStrResultForDisplay(mHeightResult.getResult(),HWConfigs.HEIGHT_ITEM);
				String displayWeight = ResultDisplayUtils.getStrResultForDisplay(mWeightResult.getResult(),HWConfigs.WEIGHT_ITEM);
				tvHeightResult.setText(displayHeight);
				tvWeightResult.setText(displayWeight);
				if(SettingHelper.getSystemSetting().isAutoBroadcast()){
					TtsManager.getInstance().speak("身高" + displayHeight + "体重" + displayWeight);
				}
				break;
			
			case PREPARE_FOR_CHECK_IN:
				prepareForCheckIn();
				break;
		}
	}
	
	@Override
	public void onRS232Result(Message msg){
		// 只有测试状态下,才理会机器信息
		if(!isTesting || isTestFinished){
			return;
		}
		switch(msg.what){
			case SerialConfigs.HEIGHT_WEIGHT_RESULT:
				HeightWeightResult result = (HeightWeightResult)msg.obj;
				
				mHeightResult = ResultUtils.generateRoughResultWithRaw(mStudent,result,1);
				mWeightResult = ResultUtils.generateRoughResultWithRaw(mStudent,result,2);
				
				ResultUtils.saveResults(this,mHeightResult,mLastHeightResult);
				ResultUtils.saveResults(this,mWeightResult,mLastWeightResult);
				
				mHandler.sendEmptyMessage(UPDATE_NEW_RESULT);
				
				if(SettingHelper.getSystemSetting().isAutoPrint()){
					PrinterUtils.printResult(this,mStudent,mHeightResult,mWeightResult);
				}
				
				if(SettingHelper.getSystemSetting().isRtUpload()){
					itemSubscriber.setDataUpLoad(mHeightResult,mHeightResult);
					itemSubscriber.setDataUpLoad(mWeightResult,mWeightResult);
				}
				
				//mLEDManager.showString(hostId,mStudent.getStudentName(),5,0,true,false);
				//mLEDManager.showString(hostId,"身高:" + displayHeight,2,2,false,false);
				//mLEDManager.showString(hostId,"体重:" + displayWeight,2,3,false,true);
				
				isTestFinished = true;
				//mHandler.sendEmptyMessage(PREPARE_FOR_CHECK_IN);
				break;
		}
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		SerialDeviceManager.getInstance().setRS232ResiltListener(null);
		SerialDeviceManager.getInstance().close();
		finish();
	}
	
	//public void test(View view){
	//	final Message msg = new Message();
	//	msg.what = SerialConfigs.HEIGHT_WEIGHT_RESULT;
	//	msg.obj = new HeightWeightResult(75.0,170.0);
	//	new Thread(new Runnable(){
	//		@Override
	//		public void run(){
	//			onRS232Result(msg);
	//		}
	//	}).start();
	//}
	
	@OnClick(R.id.btn_confirm)
	public void onViewClicked(){
		prepareForCheckIn();
	}
	
//	@Override
//	public void onWrongLength(int length, int expectLength) {
//
//	}
	//@OnClick({R.id.tv_led_setting})
	//public void onViewClicked(View view){
	//	switch(view.getId()){
	//
	//		case R.id.tv_led_setting:
	//			startActivity(new Intent(this,LEDSettingActivity.class));
	//			break;
	//
	//	}
	//}
	
}