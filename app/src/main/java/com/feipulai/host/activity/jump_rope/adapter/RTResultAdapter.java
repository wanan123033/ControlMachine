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
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.entity.Student;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by James on 2018/9/5 0005.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class RTResultAdapter
		extends RecyclerView.Adapter<RTResultAdapter.ViewHolder>{
	
	private List<StuDevicePair> stuPairs;
	private int selectedPosition;
	//private Context mContext;
	private Drawable disconBackground;
	private Drawable lowBatBackground;
	private Drawable stopUseBackground;
	private Drawable defaultBackground;
	
	private OnItemClickListener mOnItemClickListener;
	
	public RTResultAdapter(Context context, List<StuDevicePair> stuPairs){
		//mContext = context;
		this.stuPairs = stuPairs;
		disconBackground = context.getResources().getDrawable(R.drawable.shape_state_red);
		lowBatBackground = context.getResources().getDrawable(R.drawable.shape_state_yellow);
		stopUseBackground = context.getResources().getDrawable(R.drawable.shape_state_grey);
		defaultBackground = context.getResources().getDrawable(R.drawable.shape_state_default);
	}
	
	public void setSelected(int selectedPosition){
		this.selectedPosition = selectedPosition;
	}
	
	public void setOnItemClickListener(OnItemClickListener onItemClickListener){
		mOnItemClickListener = onItemClickListener;
	}

	public int getSelected(){
		return selectedPosition;
	}

	//Create new views (invoked by the layout manager)
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
		// Create a new view.
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View v = inflater.inflate(R.layout.real_time_result_item,parent,false);
		return new ViewHolder(v);
	}
	
	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(final ViewHolder holder,int position){
		StuDevicePair pair = stuPairs.get(position);
		BaseDeviceState deviceState = pair.getBaseDevice();
		int deviceId = deviceState.getDeviceId();
		holder.mTvDeviceId.setText(deviceId + "");
		int result = InteractUtils.getResultInt(pair);
		holder.mTvResult.setText(result + "");
		Student student = pair.getStudent();
		if(student != null){
			String studentName = pair.getStudent().getStudentName();
			holder.mTvStuName.setText(studentName);
		}else{
			holder.mTvStuName.setText("");
		}
		
		Drawable backgroundDrawable;
		
		//	测试过程中的会排除掉冲突的手柄,不会出现冲突状态
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
			
			default:
				backgroundDrawable = defaultBackground;
				break;
		}
		holder.mTvDeviceId.setBackground(backgroundDrawable);
		//选中处理
		holder.mLlPair.setBackgroundColor(position == selectedPosition ? Color.rgb(30,144,255) : Color.WHITE);
		if(mOnItemClickListener != null){
			holder.mLlPair.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v){
					mOnItemClickListener.onItemClick(holder.mLlPair,holder.getAdapterPosition());
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
		@BindView(R.id.ll_pair)
		LinearLayout mLlPair;
		@BindView(R.id.tv_device_id)
		TextView mTvDeviceId;
		@BindView(R.id.tv_stu_name)
		TextView mTvStuName;
		@BindView(R.id.tv_result)
		TextView mTvResult;
		
		ViewHolder(View view){
			super(view);
			ButterKnife.bind(this,view);
		}
		
	}
	
	public interface OnItemClickListener{
		void onItemClick(View view, int position);
	}
	
}
