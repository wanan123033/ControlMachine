package com.feipulai.host.activity.base;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;
import com.feipulai.host.entity.Student;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 公司 深圳菲普莱体育
 * 密级 绝密
 */
public class BaseResultDisplayAdapter extends BaseQuickAdapter<BaseStuPair,BaseResultDisplayAdapter.ViewHolder>{
	
	private int mSelect = 0;
	
	public BaseResultDisplayAdapter(@Nullable List<BaseStuPair> data){
		super(R.layout.rv_result_display_item,data);
	}
	
	@Override
	protected void convert(ViewHolder holder,BaseStuPair pair){
		Student student = pair.getStudent();
		
		BaseDeviceState deviceState = pair.getBaseDevice();
		holder.mTvDeviceId.setText(deviceState.getDeviceId() + "");
		holder.mTvStuCode.setText(student != null ? student.getStudentCode() : "");
		holder.mTvStuName.setText(student != null ? student.getStudentName() : "");
		holder.mTvResult.setText(pair.getResult() + "");
		
		switch(deviceState.getState()){
			
			case BaseDeviceState.STATE_DISCONNECT:
				holder.mTvDeviceId.setBackgroundColor(Color.RED);
				break;
			
			case BaseDeviceState.STATE_LOW_BATTERY:
				holder.mTvDeviceId.setBackgroundColor(Color.YELLOW);
				break;
			
			case BaseDeviceState.STATE_CONFLICT:
				// TODO: 2018/7/31 0031 9:53 冲突的背景暂时不处理
				holder.mTvDeviceId.setBackgroundColor(Color.parseColor("#ff808080"));
				break;
			
			case BaseDeviceState.STATE_STOP_USE:
				holder.mTvDeviceId.setBackgroundColor(Color.parseColor("#ff808080"));
				break;
			
			default:
				holder.mTvDeviceId.setBackgroundColor(Color.WHITE);
				break;
			
		}
		//选中处理
		holder.ll.setBackgroundColor(mSelect == holder.getLayoutPosition() ? Color.rgb(30,144,255) : Color.WHITE);
	}
	
	public void setSelectItem(int position){
		mSelect = position;
	}
	
	public int getSelectItem(){
		return mSelect;
	}
	
	static class ViewHolder extends BaseViewHolder{
		@BindView(R.id.tv_device_id)
		TextView mTvDeviceId;
		@BindView(R.id.tv_stuCode)
		TextView mTvStuCode;
		@BindView(R.id.tv_stuName)
		TextView mTvStuName;
		@BindView(R.id.tv_result)
		TextView mTvResult;
		@BindView(R.id.ll_timing_result)
		LinearLayout ll;
		
		ViewHolder(View view){
			super(view);
			ButterKnife.bind(this,view);
		}
	}
	
}
