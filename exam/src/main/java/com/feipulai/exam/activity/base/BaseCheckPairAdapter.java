package com.feipulai.exam.activity.base;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.entity.Student;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 公司 深圳菲普莱体育
 * 密级 绝密
 */
@Deprecated
public class BaseCheckPairAdapter extends BaseQuickAdapter<BaseStuPair,BaseCheckPairAdapter.ViewHolder>{
	
	private volatile int mSelect = 0;
	
	public BaseCheckPairAdapter(@Nullable List<BaseStuPair> data){
		super(R.layout.rv_stu_dev_item,data);
	}
	
	@Override
	protected void convert(ViewHolder holder,BaseStuPair pair){
		Student student = pair.getStudent();
		
		BaseDeviceState deviceState = pair.getBaseDevice();
		holder.tvDeviceId.setText(deviceState.getDeviceId() + "");
		holder.tvStuInfo.setText(student != null ? student.getStudentName() : "");
		switch(deviceState.getState()){
			
			case BaseDeviceState.STATE_DISCONNECT:
				holder.tvDeviceId.setBackground(mContext.getResources().getDrawable(R.drawable.shape_state_red));
				//holder.tvStuInfo.setBackgroundColor(mSelect == holder.getLayoutPosition() ? Color.rgb(30,144,255) : Color.WHITE);
				break;
			
			case BaseDeviceState.STATE_LOW_BATTERY:
				holder.tvDeviceId.setBackground(mContext.getResources().getDrawable(R.drawable.shape_state_yellow));
				break;
			
			case BaseDeviceState.STATE_CONFLICT:
				holder.tvDeviceId.setBackground(mContext.getResources().getDrawable(R.drawable.shape_state_conflict));
				break;
			
			case BaseDeviceState.STATE_STOP_USE:
				holder.tvDeviceId.setBackground(mContext.getResources().getDrawable(R.drawable.shape_state_grey));
				break;
			
			default:
				holder.tvDeviceId.setBackground(mContext.getResources().getDrawable(R.drawable.shape_state_default));
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
	
	protected static class ViewHolder extends BaseViewHolder{
		@BindView(R.id.tv_device_id)
		public TextView tvDeviceId;
		@BindView(R.id.tv_stu_info)
		public TextView tvStuInfo;
		@BindView(R.id.ll_pair)
		public LinearLayout ll;
		
		ViewHolder(View view){
			super(view);
			ButterKnife.bind(this,view);
		}
	}
	
}
