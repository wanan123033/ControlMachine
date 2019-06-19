package com.feipulai.exam.activity.jump_rope.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.entity.Group;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by James on 2018/12/12 0007.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class GroupStateAdapter
		extends BaseAdapter{
	
	private List<Group> list;
	private Context mContext;
	
	public GroupStateAdapter(Context context,List<Group> list){
		this.list = list;
		mContext = context;
	}
	
	@Override
	public int getCount(){
		return list.size();
	}
	
	@Override
	public Object getItem(int position){
		return list.get(position);
	}
	
	@Override
	public long getItemId(int position){
		return position;
	}
	
	@Override
	public View getView(int position,View convertView,ViewGroup parent){
		ViewHolder holder = null;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_group,parent,false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		Group group = list.get(position);
		holder.mTvGroupName.setText(InteractUtils.generateGroupText(group));
		String complete;
		int completeState = group.getIsTestComplete();
		if(completeState == 0){
			complete = "未测试";
		}else if(completeState == 1){
			complete = "已测试";
		}else{
			complete = "未测完";
		}
		holder.mTvIsTest.setText(complete);
		
		return convertView;
	}
	
	class ViewHolder{
		@BindView(R.id.tv_group_name)
		TextView mTvGroupName;
		@BindView(R.id.tv_isTest)
		TextView mTvIsTest;
		
		ViewHolder(View view){ButterKnife.bind(this,view);}
	}
	
}
