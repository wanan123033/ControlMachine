package com.feipulai.host.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.host.R;
import com.feipulai.host.activity.situp.SitUpStuPair;
import com.feipulai.host.entity.Student;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2017/12/8.
 */
public class SitUpStuPairAdapter extends BaseAdapter{
	
	private LayoutInflater inflater;
	private List<SitUpStuPair> datas;
	private Context context;
	private int mSelect = 0;
	
	public SitUpStuPairAdapter(Context context,List<SitUpStuPair> data){
		this.context = context;
		this.datas = data;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount(){
		return datas.size();
	}
	
	@Override
	public Object getItem(int position){
		return datas.get(position);
	}
	
	@Override
	public long getItemId(int position){
		return position;
	}
	
	@Override
	public View getView(int position,View convertView,ViewGroup parent){
		ViewHolder holder;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.gv_stu_hand_pair_item,null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}
		holder = (ViewHolder)convertView.getTag();
		
		convertView.setBackgroundColor(mSelect == position ? Color.rgb(30,144,255) : Color.rgb(211,211,211));
		
		
		SitPushUpStateResult stateResult = datas.get(position).getDeviceState();
		Student student = datas.get(position).getStudent();
		
		holder.no.setText(stateResult.getDeviceId() + "");
		holder.tvStuName.setText(student != null ? student.getStudentName() : "");
		
		//Log.i("james",stateResult.getJumpRopeState() + "");
		if(stateResult.getState() == SitPushUpManager.STATE_FREE){
			holder.no.setBackground(stateResult.getBatteryLeft() < 10 ? context.getResources().getDrawable(R.drawable.shape_round_textview_yellow) :
					context.getResources().getDrawable(R.drawable.shape_round_textview));
			holder.rl.setBackgroundColor(mSelect == position ? Color.rgb(30,144,255) : Color.WHITE);
			holder.tvStuName.setBackgroundColor(mSelect == position ? Color.rgb(30,144,255) : Color.WHITE);
		}else{
			holder.no.setBackground(context.getResources().getDrawable(R.drawable.shape_round_textview_red));
			holder.rl.setBackgroundColor(mSelect == position ? Color.rgb(30,144,255) : Color.WHITE);
			holder.tvStuName.setBackgroundColor(mSelect == position ? Color.rgb(30,144,255) : Color.WHITE);
		}
		return convertView;
	}
	
	public void setSelectItem(int position){
		mSelect = position;
	}
	
	static class ViewHolder{
		@BindView(R.id.tv_device_id)
		TextView no;
		@BindView(R.id.rl_gv_pair)
		RelativeLayout rl;
		@BindView(R.id.tv_stu_info)
		TextView tvStuName;
		
		ViewHolder(View view){ButterKnife.bind(this,view);}
	}
	
}
