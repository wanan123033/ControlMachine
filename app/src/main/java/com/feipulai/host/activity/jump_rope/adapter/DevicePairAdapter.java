package com.feipulai.host.activity.jump_rope.adapter;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.host.R;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DevicePairAdapter
		extends RecyclerView.Adapter<DevicePairAdapter.ViewHolder>{
	
	private List<StuDevicePair> stuPairs;
	private int selectedPosition;
	//private Context mContext;
	
	private OnItemClickListener mOnItemClickListener;
	
	public DevicePairAdapter(Context context, List<StuDevicePair> stuPairs){
		//mContext = context;
		this.stuPairs = stuPairs;
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
	public void onBindViewHolder(final ViewHolder holder,int position){
		StuDevicePair pair = stuPairs.get(position);
		BaseDeviceState deviceState = pair.getBaseDevice();
		holder.mTvDeviceId.setText(deviceState.getDeviceId() + "");
		holder.mTvStuInfo.setText(deviceState.getState() == BaseDeviceState.STATE_FREE ? "√" : "");
		//选中处理
		holder.mLlPair.setBackgroundColor(position == selectedPosition ? Color.rgb(30,144,255) : Color.WHITE);
		if(mOnItemClickListener != null){
			holder.mLlPair.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v){
					mOnItemClickListener.onItemClick(holder.getAdapterPosition());
				}
			});
		}
	}
	
	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount(){
		return stuPairs.size();
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
		void onItemClick(int position);
	}
	
	
}