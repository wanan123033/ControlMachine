package com.feipulai.host.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.feipulai.host.R;
import com.feipulai.host.activity.data.DataRetrieveBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by James on 2018/1/3 0003.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class DataRetrieveAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener{
	
	private Context mContext;
	private List<DataRetrieveBean> mList;
	private LayoutInflater mInflater;
	
	public DataRetrieveAdapter(Context context,List<DataRetrieveBean> retrieveDatas){
		mContext = context;
		mList = retrieveDatas;
		mInflater = LayoutInflater.from(mContext);
	}
	
	@Override
	public int getCount(){
		return mList.size();
	}
	
	@Override
	public Object getItem(int position){
		return mList.get(position);
	}
	
	@Override
	public long getItemId(int position){
		return position;
	}
	
	@Override
	public View getView(int position,View convertView,ViewGroup parent){
		ViewHolder viewHolder = null;
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.item_data_retieve,null);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		DataRetrieveBean retieveData = mList.get(position);
		viewHolder.mTvStuCode.setText(retieveData.getStudentCode());
		viewHolder.mTvStuName.setText(retieveData.getStudentName());
		viewHolder.mTvSex.setText(retieveData.getSex() == 0 ? "男" : "女");
		viewHolder.mTvTestState.setText(retieveData.getTestState() == 0 ? "未测" : "已测");
		//viewHolder.mTvScore.setText(retieveData.getResult() == 0 ? "" : retieveData.getResult() + "");
		//将位置设置为CheckBox的tag
		viewHolder.mCbSelect.setTag(position);
		viewHolder.mCbSelect.setChecked(retieveData.isChecked());
		viewHolder.mCbSelect.setOnCheckedChangeListener(this);
		return convertView;
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView,boolean isChecked){
		mList.get((int)buttonView.getTag()).setChecked(isChecked);
	}
	
	static class ViewHolder{
		@BindView(R.id.cb_select)
		CheckBox mCbSelect;
		@BindView(R.id.tv_stuCode)
		TextView mTvStuCode;
		@BindView(R.id.tv_stuName)
		TextView mTvStuName;
		@BindView(R.id.tv_sex)
		TextView mTvSex;
		@BindView(R.id.tv_testState)
		TextView mTvTestState;
		//@BindView(R.id.ll_detail)
		//LinearLayout mLlDetail;
		
		ViewHolder(View view){ButterKnife.bind(this,view);}
	}
	
}