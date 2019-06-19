package com.feipulai.host.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
public class SitUpTimingResultAdapter extends BaseAdapter{
	
	private LayoutInflater inflater;
	private List<SitUpStuPair> datas;
	private Context context;
	private int mSelect;
	
	public SitUpTimingResultAdapter(Context context,List<SitUpStuPair> data){
		this.context = context;
		this.datas = data;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount(){
		return datas == null ? 0 : datas.size();
	}
	
	@Override
	public Object getItem(int position){
		return datas == null ? null : datas.get(position);
	}
	
	@Override
	public long getItemId(int position){
		return position;
	}
	
	@Override
	public View getView(int position,View convertView,ViewGroup parent){
		ViewHolder holder;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.item_time_result,null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}
		holder = (ViewHolder)convertView.getTag();
		
		
		SitUpStuPair pair = datas.get(position);
		SitPushUpStateResult state = pair.getDeviceState();
		Student student = pair.getStudent();
		holder.no.setText(state.getDeviceId() + "");
		holder.studentName.setText(student.getStudentName());
		holder.result.setText(state.getResult() + "");
		//if(pair.isICWritten()){
		//	holder.flag.setText("√");
		//	holder.flag.setTextColor(Color.GREEN);
		//}else{
		//	×
		//holder.flag.setText("");
		//holder.flag.setTextColor(Color.RED);
		//}
		
		if(mSelect == position){
			holder.rl.setBackgroundColor(Color.rgb(30,144,255));
			holder.result.setBackgroundColor(Color.rgb(30,144,255));
			holder.studentName.setBackgroundColor(Color.rgb(30,144,255));
			//holder.flag.setBackgroundColor(Color.rgb(30,144,255));
		}else{
			holder.rl.setBackgroundColor(Color.WHITE);
			holder.result.setBackgroundColor(Color.WHITE);
			holder.studentName.setBackgroundColor(Color.WHITE);
			//holder.flag.setBackgroundColor(Color.WHITE);
		}
		return convertView;
	}
	
	public void setSelectItem(int position){
		mSelect = position;
	}
	
	
	static class ViewHolder{
		@BindView(R.id.tv_timeResultHandNo)
		TextView no;
		@BindView(R.id.rl_timing_handno)
		RelativeLayout rl;
		@BindView(R.id.tv_timeResultStudentName)
		TextView studentName;
		@BindView(R.id.tv_timeResult)
		TextView result;
		//@BindView(R.id.tv_timeResultFlag)
		//TextView flag;
		
		ViewHolder(View view){ButterKnife.bind(this,view);}
	}
	
}
