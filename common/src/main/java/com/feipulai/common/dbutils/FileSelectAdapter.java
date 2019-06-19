package com.feipulai.common.dbutils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.feipulai.common.R;

import java.util.List;

public class FileSelectAdapter extends BaseAdapter {
	
	private LayoutInflater mInflater;
	
	private Bitmap mIconToRoot;
	private Bitmap mIconToParent;
	private Bitmap mIconFolder;
	private Bitmap mIconDoc;
	
	private List<FileItemBean> mFileItemBeans;
	
	public FileSelectAdapter(Context context, List<FileItemBean> fileItemBeans){
		mInflater = LayoutInflater.from(context);
		mIconToRoot = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_back_to_root);
		mIconToParent = BitmapFactory.decodeResource(context.getResources(),R.drawable.icon_back_to_parent);
		mIconFolder = BitmapFactory.decodeResource(context.getResources(),R.drawable.icon_folder);
		mIconDoc = BitmapFactory.decodeResource(context.getResources(),R.drawable.icon_doc);
		mFileItemBeans = fileItemBeans;
	}
	
	@Override
	public int getCount(){
		return mFileItemBeans.size();
	}
	
	@Override
	public Object getItem(int position){
		return mFileItemBeans.get(position);
	}
	
	@Override
	public long getItemId(int position){
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder = null;
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.lv_file_row,parent,false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		FileItemBean bean = mFileItemBeans.get(position);
		if(position == 0 && bean.getName().equals(FileSelectActivity.BACK_TO_ROOT)){
			holder.mTvName.setText("返回根目录...");
			holder.mIcon.setImageBitmap(mIconToRoot);
		}else if(position == 1 && bean.getName().equals(FileSelectActivity.BACK_TO_PARENT)){
			holder.mTvName.setText("返回上一层...");
			holder.mIcon.setImageBitmap(mIconToParent);
		}else{
			holder.mTvName.setText(bean.getName());
			if(bean.getFile().isDirectory()){
				holder.mIcon.setImageBitmap(mIconFolder);
			}else{
				holder.mIcon.setImageBitmap(mIconDoc);
			}
		}
		return convertView;
	}
	
	static class ViewHolder{
		ImageView mIcon;
		TextView mTvName;
		
		ViewHolder(View view){
			mIcon = view.findViewById(R.id.icon);
			mTvName = view.findViewById(R.id.tv_name);
		}
	}
	
}


