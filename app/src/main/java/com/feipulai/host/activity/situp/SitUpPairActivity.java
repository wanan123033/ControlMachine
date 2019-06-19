package com.feipulai.host.activity.situp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Switch;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.view.DividerItemDecoration;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.SitPushUpSetFrequencyResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.activity.base.BaseCheckPairAdapter;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.utils.SharedPrefsUtil;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SitUpPairActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener, RadioManager.OnRadioArrivedListener{
	
	private static final int NO_PAIR_RESPONSE_ARRIVED = 35;
	@BindView(R.id.sw_auto_pair)
	Switch mSwAutoPair;
	@BindView(R.id.rv_pairs)
	RecyclerView mRvPairs;
	
	private SitPushUpManager mSitPushUpManager;
	private int mMaxDevices;
	private int hostId;
	private MyHandler mHandler = new MyHandler(this);
	
	private volatile int mCurrentPosition;
	private int mTargetFrequency;
	private int mCurrentFrequency;
	
	private List<BaseStuPair> mPairs;
	private BaseCheckPairAdapter mAdapter;
	private static final int UPDATE_SPECIFIC_ITEM = 0x1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_situp_pair);
		ButterKnife.bind(this);
		initView();
	}
	
	private void initView(){
		hostId = SharedPrefsUtil.getValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.HOST_ID,1);
		mTargetFrequency = SerialConfigs.sProChannels.get(ItemDefault.CODE_YWQZ) + hostId - 1;
		
		boolean isAutoPair = SharedPrefsUtil.getValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.SIT_UP_AUTO_PAIR,true);
		mSwAutoPair.setChecked(isAutoPair);
		
		mMaxDevices = SharedPrefsUtil.getValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.SIT_UP_TEST_NUMBER,20);
		
		initAadpter();
		
		mSitPushUpManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP);
		// 完全被动地等待接收终端的开机信息
		RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
		mCurrentFrequency = 0;
		
		mRvPairs.setLayoutManager(new GridLayoutManager(this,5));
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
		dividerItemDecoration.setDrawBorderTopAndBottom(true);
		dividerItemDecoration.setDrawBorderLeftAndRight(true);
		mRvPairs.addItemDecoration(dividerItemDecoration);
	}
	
	@Override
	protected void onResume(){
		RadioManager.getInstance().setOnRadioArrived(this);
		super.onResume();
	}
	
	private void initAadpter(){
		mPairs = new Vector<>(mMaxDevices);
		
		for(int i = 0;i < mMaxDevices;i++){
			BaseStuPair pair = new BaseStuPair();
			
			BaseDeviceState state = new BaseDeviceState();
			state.setState(BaseDeviceState.STATE_DISCONNECT);
			state.setDeviceId(i + 1);
			
			pair.setBaseDevice(state);
			
			mPairs.add(pair);
		}
		mAdapter = new DevicePairAdapter(mPairs);
		mRvPairs.setAdapter(mAdapter);
		
		mRvPairs.setClickable(true);
		mAdapter.setOnItemClickListener(this);
	}
	
	@Override
	public void onItemClick(BaseQuickAdapter adapter,View view,int position){
		mCurrentPosition = position;
		mAdapter.setSelectItem(mCurrentPosition);
		mPairs.get(mCurrentPosition).getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
		mAdapter.notifyDataSetChanged();
		RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
		mCurrentFrequency = 0;
	}
	
	@Override
	public void onRadioArrived(Message msg){
		switch(msg.what){
			case SerialConfigs.SIT_UP_MACHINE_BOOT_RESPONSE:
				SitPushUpSetFrequencyResult setFrequencyResult = (SitPushUpSetFrequencyResult)msg.obj;
				if(setFrequencyResult != null){
					checkDevice(setFrequencyResult);
				}
				break;
		}
	}
	
	private static class MyHandler extends Handler{
		
		private WeakReference<SitUpPairActivity> mActivityWeakReference;
		
		public MyHandler(SitUpPairActivity activityWeakReference){
			mActivityWeakReference = new WeakReference<>(activityWeakReference);
		}
		
		@Override
		public void handleMessage(Message msg){
			SitUpPairActivity activity = mActivityWeakReference.get();
			if(activity == null){
				return;
			}
			switch(msg.what){
				
				case NO_PAIR_RESPONSE_ARRIVED:
					activity.onNoPairResponseArrived();
					break;
				
				case UPDATE_SPECIFIC_ITEM:
					activity.mAdapter.notifyItemChanged(msg.arg1);
					break;
				
			}
		}
		
	}
	
	private void onNoPairResponseArrived(){
		//5s以后在主机的目的频没有收到任何东西,切到0频
		if(mCurrentFrequency == mTargetFrequency){
			RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
			mCurrentFrequency = 0;
		}
	}
	
	private void checkDevice(SitPushUpSetFrequencyResult result){
		if(result != null && result.getProjectCode() == SitPushUpManager.PROJECT_CODE_SIT_UP){
			if(mCurrentFrequency == 0){
				// 0频段接收到的结果,肯定是设备的开机广播
				if(result.getFrequency() == mTargetFrequency && result.getDeviceId() == mCurrentPosition + 1){
					onNewDeviceConnect();
				}else{
					mSitPushUpManager.setFrequency(SitPushUpManager.PROJECT_CODE_SIT_UP,result.getFrequency(),mCurrentPosition + 1,hostId);
					mCurrentFrequency = mTargetFrequency;
					// 那个铁盒子就是有可能等这么久才收到回复
					mHandler.sendEmptyMessageDelayed(NO_PAIR_RESPONSE_ARRIVED,5000);
				}
			}else if(mCurrentFrequency == mTargetFrequency){
				//在主机的目的频段收到的,肯定是设置频段后收到的设备广播
				if(result.getDeviceId() == mCurrentPosition + 1 && result.getFrequency() == mTargetFrequency){
					onNewDeviceConnect();
					RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
					mCurrentFrequency = 0;
				}
			}
		}
	}
	
	private void onNewDeviceConnect(){
		int oldPosition = mCurrentPosition;
		mPairs.get(mCurrentPosition).getBaseDevice().setState(BaseDeviceState.STATE_FREE);
		while(mSwAutoPair.isChecked()
				&& mCurrentPosition != mMaxDevices - 1){
			mCurrentPosition++;
			if(mPairs.get(mCurrentPosition).getBaseDevice().getState() != BaseDeviceState.STATE_FREE){
				break;
			}
		}
		mAdapter.setSelectItem(mCurrentPosition);
		updateSpecificItem(oldPosition);
		if(mCurrentPosition != oldPosition){
			updateSpecificItem(mCurrentPosition);
		}
	}
	
	private void updateSpecificItem(int position){
		Message msg = Message.obtain();
		msg.what = UPDATE_SPECIFIC_ITEM;
		msg.arg1 = position;
		mHandler.sendMessage(msg);
	}
	
	@OnClick({R.id.sw_auto_pair})
	public void btnOnClick(View v){
		switch(v.getId()){
			case R.id.sw_auto_pair:
				SharedPrefsUtil.putValue(this,SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.SIT_UP_AUTO_PAIR,mSwAutoPair.isChecked());
				break;
		}
	}
	
	@Override
	protected void onPause(){
		mHandler.removeCallbacks(null);
		RadioManager.getInstance().setOnRadioArrived(null);
		super.onPause();
	}
	
	class DevicePairAdapter extends BaseCheckPairAdapter{
		
		public DevicePairAdapter(@Nullable List<BaseStuPair> data){
			super(data);
		}
		
		@Override
		protected void convert(ViewHolder holder,BaseStuPair pair){
			BaseDeviceState deviceState = pair.getBaseDevice();
			holder.tvDeviceId.setText(deviceState.getDeviceId() + "");
			holder.tvStuInfo.setText(deviceState.getState() == BaseDeviceState.STATE_FREE ? "√" : "");
			holder.ll.setBackgroundColor(getSelectItem() == holder.getLayoutPosition() ? Color.rgb(30,144,255) : Color.WHITE);
		}
		
	}
	
}
