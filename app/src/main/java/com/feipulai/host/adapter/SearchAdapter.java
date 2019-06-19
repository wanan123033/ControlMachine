package com.feipulai.host.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.feipulai.host.R;
import com.feipulai.host.entity.Student;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2017/12/15.
 */

public class SearchAdapter extends BaseAdapter {
	private List<Student> datas;
	private LayoutInflater inflater;
	private PopClickListener popClickListener;

	public SearchAdapter(Context context, List<Student> datas) {
		this.datas = datas;
		inflater = LayoutInflater.from(context);
		Log.e(TAG, "SearchAdapter: ----------------" + datas.size());
	}

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.lv_search_item, null);
			holder.tvCode = (TextView) convertView.findViewById(R.id.tv_search_code);
			holder.tvName = (TextView) convertView.findViewById(R.id.tv_search_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		setData(holder, position);

		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (popClickListener != null) {
					popClickListener.PopuClickListener(v, datas.get(position), position);
				}
			}
		});
		return convertView;
	}

	private void setData(ViewHolder holder, int position) {
		holder.tvCode.setText(datas.get(position).getStudentCode());
		holder.tvName.setText(datas.get(position).getStudentName());
	}

	class ViewHolder {
		TextView tvCode, tvName;
	}

	public void setPopuClickListener(PopClickListener pop) {
		this.popClickListener = pop;
	}

	public interface PopClickListener {
		void PopuClickListener(View v, Student student, int position);
	}
}
