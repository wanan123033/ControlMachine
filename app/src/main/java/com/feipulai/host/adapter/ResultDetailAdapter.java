package com.feipulai.host.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.feipulai.host.R;
import com.feipulai.host.activity.height_weight.HWConfigs;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.utils.ResultDisplayUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by James on 2018/2/7 0007.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ResultDetailAdapter extends BaseAdapter{
	
	private LayoutInflater mInflater;
	private List<RoundResult> mRoundResults;
	
	private List<RoundResult> mheightResults;
	private List<RoundResult> mweightResults;
	
	private boolean isHW;
	
	public ResultDetailAdapter(Context context,List<RoundResult> roundResults){
		mInflater = LayoutInflater.from(context);
		mRoundResults = roundResults;
	}
	
	// 供身高体重项目调用
	public ResultDetailAdapter(Context context,List<RoundResult> heightResults,List<RoundResult> weightResults){
		mInflater = LayoutInflater.from(context);
		mheightResults = heightResults;
		mweightResults = weightResults;
		isHW = true;
	}
	
	@Override
	public int getCount(){
		return mRoundResults.size();
	}
	
	@Override
	public Object getItem(int position){
		return mRoundResults.get(position);
	}
	
	@Override
	public long getItemId(int position){
		return position;
	}
	
	@Override
	public View getView(int position,View convertView,ViewGroup parent){
		ViewHolder viewHolder = null;
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.item_result_detail,parent,false);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		String displayStr;
		if(!isHW){
			RoundResult roundResult = mRoundResults.get(position);
			viewHolder.mTvTimes.setText(position + "");
			viewHolder.mTvTestTime.setText(roundResult.getTestTime());
			displayStr = ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult());
			viewHolder.mTvResult.setText(roundResult.getResultState() == RoundResult.RESULT_STATE_FOUL ? "犯规" : displayStr);
		}else{
			RoundResult heightResult = mheightResults.get(position);
			RoundResult weightResult = mweightResults.get(position);
			
			displayStr = ResultDisplayUtils.getStrResultForDisplay(heightResult.getResult(),HWConfigs.HEIGHT_ITEM)
					+ ""
					+ ResultDisplayUtils.getStrResultForDisplay(weightResult.getResult(),HWConfigs.WEIGHT_ITEM);
			viewHolder.mTvTimes.setText(position + "");
			viewHolder.mTvTestTime.setText(heightResult.getTestTime());
			viewHolder.mTvResult.setText(heightResult.getResultState() == RoundResult.RESULT_STATE_FOUL ? "犯规" : displayStr);
		}
		
		return convertView;
	}
	
	static class ViewHolder{
		@BindView(R.id.tv_times)
		TextView mTvTimes;
		@BindView(R.id.tv_result)
		TextView mTvResult;
		@BindView(R.id.tv_test_time)
		TextView mTvTestTime;
		
		ViewHolder(View view){ButterKnife.bind(this,view);}
	}
	
}
