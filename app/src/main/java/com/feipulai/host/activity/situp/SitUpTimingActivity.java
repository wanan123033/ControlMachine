package com.feipulai.host.activity.situp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.jump_rope.facade.CountTimingTestFacade;
import com.feipulai.common.jump_rope.task.GetDeviceStatesTask;
import com.feipulai.common.jump_rope.task.LEDContentGenerator;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.DividerItemDecoration;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.jump_rope.adapter.RTResultAdapter;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.SharedPrefsConfigs;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SitUpTimingActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener,
		LEDContentGenerator,
		GetDeviceStatesTask.OnGettingDeviceStatesListener,
		CountTimingTestFacade.Listener,
		RadioManager.OnRadioArrivedListener{
	
	public static final String TESTING_PAIRS = "TESTING_PAIRS";
	private static final int TIME_COUNT = 0x1;
	private static final int UPDATE_SPECIFIC_ITEM = 0x2;
	private static final int MSG_TEST_ENDED = 0x3;
	private static final int MSG_RESULT_GOT = 0x5;
	private static final int INVALID_PIV = -100;
	
	@BindView(R.id.tv_count)
	TextView mTvCount;
	@BindView(R.id.rv_pairs)
	RecyclerView mRvPairs;
	@BindView(R.id.btn_stop_using)
	Button mBtnStopUsing;
	@BindView(R.id.btn_restart)
	Button mBtnRestart;
	@BindView(R.id.btn_stop_test)
	Button mBtnStopTest;
	
	private int hostId;
	
	private List<BaseStuPair> mPairs;
	
	private int mTestTime;
	private volatile int[] mCurrentConnect;
	
	private CountTimingTestFacade mFacade;
	
	private RTResultAdapter mAdapter;
	
	private MyHandler mHandler = new MyHandler(this);
	
	private volatile int mCurrentPosition;
	private ProgressDialog mProgressDialog;
	
	private SitPushUpManager mSitPushUpManager;
	private int projectCode = SitPushUpManager.PROJECT_CODE_SIT_UP;
	
	private int[] mDevIdPIV;
	private int size;
	private int maxDeviceId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sit_up_timing);
		ButterKnife.bind(this);
		init();
	}
	
	private void init(){
		
		hostId = SettingHelper.getSystemSetting().getHostId();
		mTestTime = SharedPrefsUtil.getValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.SIT_UP_TEST_TIME,30);
		
		Intent intent = getIntent();
		mPairs = (List<BaseStuPair>)intent.getSerializableExtra(TESTING_PAIRS);
		//mPairs = Collections.synchronizedList(mPairs);
		size = mPairs.size();
		maxDeviceId = mPairs.get(size - 1).getBaseDevice().getDeviceId();
		mCurrentConnect = new int[maxDeviceId + 1];
		mDevIdPIV = new int[maxDeviceId + 1];
		
		int piv = 0;
		for(BaseStuPair pair : mPairs){
			mDevIdPIV[pair.getBaseDevice().getDeviceId()] = piv;
			piv++;
		}
		
		RadioManager.getInstance().setOnRadioArrived(this);
		
		mSitPushUpManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP);
		
		initAdapter();
		
		initTestFacade();
	}
	
	private void initTestFacade(){
		CountTimingTestFacade.Builder builder = new CountTimingTestFacade.Builder();
		mFacade = builder.countStartTime(SitPushUpManager.DEFAULT_COUNT_DOWN_TIME)
				.countFinishTime(SitPushUpManager.DEFAULT_COUNT_DOWN_TIME)
				.testTime(mTestTime)
				.hostId(hostId)
				.setLEDContentGenerator(this)
				.setOnGettingDeviceStatesListener(this)
				// .setStuDevicePairs(mPairs)
				.build();
		mFacade.setListener(this);
		mFacade.start();
	}
	
	private void initAdapter(){
		mRvPairs.setLayoutManager(new GridLayoutManager(this,5));
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
		dividerItemDecoration.setDrawBorderTopAndBottom(true);
		dividerItemDecoration.setDrawBorderLeftAndRight(true);
		mRvPairs.addItemDecoration(dividerItemDecoration);
		
		// mRvPairs.setItemAnimator(new NoActionAnimator());
		
		// mAdapter = new RTResultAdapter(mPairs);
		mRvPairs.setAdapter(mAdapter);
		
		mRvPairs.setClickable(true);
		// mAdapter.setOnItemClickListener(this);
	}
	
	@OnClick({R.id.btn_stop_using,R.id.btn_restart,R.id.btn_stop_test})
	public void onViewClicked(View view){
		switch(view.getId()){
			
			case R.id.btn_stop_using:
				mPairs.get(mCurrentPosition).getBaseDevice().setState(BaseDeviceState.STATE_STOP_USE);
				mAdapter.notifyItemChanged(mCurrentPosition);
				break;
			
			case R.id.btn_restart:
				new AlertDialog.Builder(this).setTitle("重新开始将取消当前测试,确定重新开始测试吗？")
						.setIcon(android.R.drawable.ic_dialog_info)
						.setPositiveButton("确定",new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog,int which){
								mFacade.stop();
								initTestFacade();
							}
						})
						.setNegativeButton("取消",null).show();
				break;
			
			case R.id.btn_stop_test:
				showDialog();
				break;
		}
	}
	
	@Override
	public void onItemClick(BaseQuickAdapter adapter,View view,int position){
		int oldPosition = mCurrentPosition;
		mCurrentPosition = position;
		// mAdapter.setSelectItem(position);
		mAdapter.notifyItemChanged(oldPosition);
		if(mCurrentPosition != oldPosition){
			mAdapter.notifyItemChanged(mCurrentPosition);
		}
	}
	
	// @Override
	public String generate(BaseStuPair pair){
		String showContent;
		int deviceId = pair.getBaseDevice().getDeviceId();
		if(pair.getBaseDevice().getState() == BaseDeviceState.STATE_STOP_USE){
			showContent = String.format("%-3d",deviceId) + "暂停使用";
		}else{
			String studentName = pair.getStudent().getStudentName();
			if(studentName.length() >= 4){
				studentName = studentName.substring(0,4);
			}else{
				StringBuilder sb = new StringBuilder();
				sb.append(studentName);
				int spaces = 8 - studentName.length() * 2;
				//Log.i("james","spaces:" + spaces);
				for(int j = 0;j < spaces;j++){
					sb.append(' ');
				}
				studentName = sb.toString();
				//Log.i("james",studentName);
			}
			showContent = String.format("%-3d",deviceId) +
					studentName + String.format("%-3d",pair.getResult());
		}
		return showContent;
	}
	
	@Override
	public void onGettingState(int position){
		BaseDeviceState deviceState = mPairs.get(position).getBaseDevice();
		if(deviceState.getState() != BaseDeviceState.STATE_STOP_USE){
			//Log.i("james",deviceState.getDeviceId() + "");
			mSitPushUpManager.getState(deviceState.getDeviceId());
		}
	}
	
	@Override
	public void onStateRefreshed(){
		int oldState;
		for(int i = 0;i < size;i++){
			BaseStuPair handStuPair = mPairs.get(i);
			BaseDeviceState deviceState = handStuPair.getBaseDevice();
			oldState = deviceState.getState();
			if(mCurrentConnect[deviceState.getDeviceId()] == 0
					&& oldState != BaseDeviceState.STATE_DISCONNECT
					&& oldState != BaseDeviceState.STATE_STOP_USE){
				deviceState.setState(BaseDeviceState.STATE_DISCONNECT);
				updateSpecificItem(i);
			}
		}
		mCurrentConnect = new int[maxDeviceId + 1];
	}
	
	@Override
	public int getDeviceCount(){
		return size;
	}
	
	@Override
	public void onGetReadyTimerTick(long tick){
		tickInUI(tick + "");
		mSitPushUpManager.startTest((int)tick,mTestTime);
	}
	
	@Override
	public void onGetReadyTimerFinish(){
		tickInUI("开始");
	}
	
	@Override
	public void onTestingTimerTick(long tick){
		tickInUI(tick + "");
		//new LEDManager().showString(hostId,String.format("%3d",tick),6,0,false,true);
	}
	
	
	@Override
	public void onTestingTimerFinish(){
		tickInUI("结束");
		mHandler.sendEmptyMessage(MSG_TEST_ENDED);
	}
	
	private void tickInUI(String text){
		Message msg = Message.obtain();
		msg.what = TIME_COUNT;
		msg.obj = text;
		mHandler.sendMessage(msg);
	}
	
	@Override
	public void onRadioArrived(Message msg){
		if(msg.what == SerialConfigs.SIT_UP_GET_STATE){
			SitPushUpStateResult result = (SitPushUpStateResult)msg.obj;
			int deviceId = result.getDeviceId();
			// 非当前范围内设备手柄
			int piv = mDevIdPIV[deviceId];
			if(deviceId > mDevIdPIV.length
					|| piv == INVALID_PIV){
				return;
			}
			
			BaseStuPair pair = mPairs.get(piv);
			BaseDeviceState originState = pair.getBaseDevice();
			
			// 暂停使用需要保留下来
			if(originState.getState() != BaseDeviceState.STATE_STOP_USE){
				
				pair.setResult(result.getResult());
				int newState;
				switch(result.getState()){
					
					case SitPushUpManager.STATE_ENDED:
						newState = BaseDeviceState.STATE_FINISHED;
						break;
					
					case SitPushUpManager.STATE_COUNTING:
						newState = BaseDeviceState.STATE_COUNTING;
						break;
					
					default:
						boolean lowBattery = result.getBatteryLeft() <= 10;
						newState = lowBattery ? BaseDeviceState.STATE_LOW_BATTERY : BaseDeviceState.STATE_FREE;
				}
				
				originState.setState(newState);
				
				updateSpecificItem(piv);
				
				mCurrentConnect[deviceId]++;
			}
		}
	}
	
	private void updateSpecificItem(int piv){
		Message msg = Message.obtain();
		msg.what = UPDATE_SPECIFIC_ITEM;
		msg.arg1 = piv;
		mHandler.sendMessage(msg);
	}
	
	@Override
	public String generate(int position) {
		return null;
	}
	
	private static class MyHandler extends Handler{
		
		private WeakReference<SitUpTimingActivity> mWeakReference;
		
		public MyHandler(SitUpTimingActivity activity){
			mWeakReference = new WeakReference<>(activity);
		}
		
		@Override
		public void handleMessage(Message msg){
			SitUpTimingActivity activity = mWeakReference.get();
			if(activity == null){
				return;
			}
			switch(msg.what){
				
				case UPDATE_SPECIFIC_ITEM:
					activity.mAdapter.notifyItemChanged(msg.arg1);
					break;
				
				case TIME_COUNT:
					activity.mTvCount.setText((CharSequence)msg.obj);
					break;
				
				case MSG_TEST_ENDED:
					activity.mProgressDialog = new ProgressDialog(activity);
					activity.mProgressDialog.setTitle("获取最终成绩中");
					activity.mProgressDialog.setCancelable(false);
					activity.mProgressDialog.setCanceledOnTouchOutside(false);
					activity.mProgressDialog.setMessage("获取成绩中,请稍等...");
					activity.mProgressDialog.show();
					activity.mHandler.sendEmptyMessageDelayed(MSG_RESULT_GOT,5000);
					break;
				
				case MSG_RESULT_GOT:
					//这里不结束测试,由跳绳手柄自己结束跳绳测试
					// 结束5s后结束发送跳绳命令,获取所有跳绳状态结果
					boolean isFinalResult = activity.checkFinalResults();
					if(isFinalResult){
						activity.mProgressDialog.dismiss();
						Intent intent = new Intent(activity,SitUpResultActivity.class);
						intent.putExtra(SitUpResultActivity.TEST_RESULTS,(Serializable)activity.mPairs);
						activity.startActivity(intent);
						activity.finish();
					}else{
						activity.mHandler.sendEmptyMessageDelayed(MSG_RESULT_GOT,3000);
					}
					break;
			}
		}
		
	}
	
	private boolean checkFinalResults(){
		for(BaseStuPair pair : mPairs){
			int state = pair.getBaseDevice().getState();
			if(state == BaseDeviceState.STATE_COUNTING){
				//	存在手柄还在计数,证明当前成绩还不是最终成绩
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void onBackPressed(){
		showDialog();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		mHandler.removeCallbacksAndMessages(null);
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		mFacade.stop();
		//一旦界面不在前台了,直接结束测试,没有中间态
		finish();
	}
	
	private void showDialog(){
		new AlertDialog.Builder(this).setTitle("确定退出当前测试吗？")
				.setIcon(android.R.drawable.ic_dialog_info)
				.setPositiveButton("确定",new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog,int which){
						Intent intent = new Intent(SitUpTimingActivity.this,SitUpCheckActivity.class);
						startActivity(intent);
						finish();
					}
				})
				.setNegativeButton("返回",null).show();
	}
	
}
