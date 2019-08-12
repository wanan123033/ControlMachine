package com.feipulai.host.activity.jump_rope.adapter;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.host.R;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.entity.Student;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CheckPairAdapter
		extends RecyclerView.Adapter<CheckPairAdapter.ViewHolder> implements View.OnClickListener{
	
	private List<StuDevicePair> stuPairs;
	private int selectedPosition;
	private Drawable disconBackground;
	private Drawable lowBatBackground;
	private Drawable stopUseBackground;
	private Drawable conflictBackground;
	private Drawable defaultBackground;
	
	private OnItemClickListener mOnItemClickListener;
	
	public CheckPairAdapter(Context context, List<StuDevicePair> stuPairs){
		this.stuPairs = stuPairs;
		disconBackground = context.getResources().getDrawable(R.drawable.shape_state_red);
		lowBatBackground = context.getResources().getDrawable(R.drawable.shape_state_yellow);
		stopUseBackground = context.getResources().getDrawable(R.drawable.shape_state_grey);
		conflictBackground = context.getResources().getDrawable(R.drawable.shape_state_conflict);
		defaultBackground = context.getResources().getDrawable(R.drawable.shape_state_default);
	}
	
	public void setSelected(int selectedPosition){
		this.selectedPosition = selectedPosition;
	}
	
	public int getSelected(){
		return selectedPosition;
	}
	
	public void setOnItemClickListener(OnItemClickListener onItemClickListener){
		mOnItemClickListener = onItemClickListener;
	}
	
	//Create new views (invoked by the layout manager)
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
		// Create a new view.
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View v = inflater.inflate(R.layout.rv_stu_dev_item,parent,false);
		return new ViewHolder(v);
	}
	
	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(ViewHolder holder,int position){
		StuDevicePair pair = stuPairs.get(position);
		BaseDeviceState deviceState = pair.getBaseDevice();
		int deviceId = deviceState.getDeviceId();
		holder.mTvDeviceId.setText(deviceId + "");
		Student student = pair.getStudent();
		if(student != null){
			holder.mTvStuInfo.setText(student.getStudentName());
		}else{
			holder.mTvStuInfo.setText("");
		}
		
		Drawable backgroundDrawable;
		
		switch(deviceState.getState()){
			
			case BaseDeviceState.STATE_DISCONNECT:
				backgroundDrawable = disconBackground;
				break;
			
			case BaseDeviceState.STATE_LOW_BATTERY:
				backgroundDrawable = lowBatBackground;
				break;
			
			case BaseDeviceState.STATE_STOP_USE:
				backgroundDrawable = stopUseBackground;
				break;
			
			case BaseDeviceState.STATE_CONFLICT:
				backgroundDrawable = conflictBackground;
				break;
			
			default:
				backgroundDrawable = defaultBackground;
				break;
		}
		holder.mTvDeviceId.setBackground(backgroundDrawable);
		holder.mLlPair.setBackgroundColor(position == selectedPosition ? Color.rgb(30,144,255) : Color.WHITE);
		holder.mLlPair.setTag(position);
		holder.mLlPair.setOnClickListener(this);
	}
	
	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount(){
		return stuPairs.size();
	}
	
	@Override
	public void onClick(View v){
		if(mOnItemClickListener != null){
			mOnItemClickListener.onItemClick(v,(Integer)v.getTag());
		}
	}
	
	// provide a reference to the type of views that you are using (custom ViewHolder)
	static class ViewHolder extends RecyclerView.ViewHolder{
		@BindView(R.id.tv_device_id)
		TextView mTvDeviceId;
		@BindView(R.id.tv_stu_info)
		TextView mTvStuInfo;
		@BindView(R.id.ll_pair)
		LinearLayout mLlPair;
		
		ViewHolder(View view){
			super(view);
			ButterKnife.bind(this,view);
		}
		
	}
	
	public interface OnItemClickListener{
		void onItemClick(View view, int position);
	}
	
}
