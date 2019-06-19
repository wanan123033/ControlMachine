package com.feipulai.host.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.feipulai.host.R;
import com.feipulai.host.entity.Student;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by james on 2017/12/25.
 */

public class SearchResultAdapter extends BaseAdapter {
	
	private List<Student> list;
	private Context mContext;
	
	//public SearchResultAdapter(Context context, Student student) {
	//	list = new ArrayList<>();
	//	list.add(student);
	//	mContext = context;
	//}
	
	public SearchResultAdapter(Context context, List<Student> list) {
		this.list = list;
		mContext = context;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}
	
	@Override
	public Object getItem(int position) {
		return list.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_search_item, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mTvStuCode.setText(list.get(position).getStudentCode());
		holder.mTvIdcardNumCode.setText(list.get(position).getIdCardNo());
		holder.mTvStuName.setText(list.get(position).getStudentName());
		return convertView;
	}
	
	static class ViewHolder {
		@BindView(R.id.tv_stuCode)
		TextView mTvStuCode;
		@BindView(R.id.tvidcard_numCode)
		TextView mTvIdcardNumCode;
		@BindView(R.id.tv_stuName)
		TextView mTvStuName;
		
		ViewHolder(View view) {ButterKnife.bind(this, view);}
	}
	
	
}
