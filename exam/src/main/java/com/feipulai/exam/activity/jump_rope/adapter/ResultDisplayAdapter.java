package com.feipulai.exam.activity.jump_rope.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 公司 深圳菲普莱体育
 * 密级 绝密
 */
public class ResultDisplayAdapter extends RecyclerView.Adapter<ResultDisplayAdapter.ViewHolder>{
	
	private int maxResultSum;
	private Map<Student,List<RoundResult>> result;
	
	public ResultDisplayAdapter(@Nullable Map<Student,List<RoundResult>> data,int maxResultSum){
		result = data;
		this.maxResultSum = maxResultSum;
	}
	
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
		LinearLayout layout = (LinearLayout)LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_resdisplay_item,parent,false);
		for(int i = 0;i < maxResultSum;i++){
			TextView textView = new TextView(parent.getContext());
			textView.setId(i);
			textView.setGravity(Gravity.CENTER);
			textView.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,1));
			layout.addView(textView);
		}
		return new ViewHolder(layout,maxResultSum);
	}
	
	@Override
	public void onBindViewHolder(ViewHolder holder,int position){
		Student student = TestCache.getInstance().getAllStudents().get(position);
		holder.mTvStuCode.setText(student.getStudentCode());
		holder.mTvStuName.setText(student.getStudentName());
		
		List<RoundResult> results = result.get(student);
		for(int i = 0;i < maxResultSum;i++){
			if(results == null || i >= results.size()){
				holder.tvResults[i].setText("未测试");
			}else{
				holder.tvResults[i].setText(ResultDisplayUtils.getStrResultForDisplay(results.get(i).getResult()));
			}
		}
	}
	
	@Override
	public int getItemCount(){
		return TestCache.getInstance().getAllStudents().size();
	}
	
	static class ViewHolder extends RecyclerView.ViewHolder{
		
		@BindView(R.id.tv_stuCode)
		TextView mTvStuCode;
		@BindView(R.id.tv_stuName)
		TextView mTvStuName;
		@BindView(R.id.ll_timing_result)
		LinearLayout mLlTimingResult;
		TextView[] tvResults;
		
		ViewHolder(View view,int maxResultSum){
			super(view);
			ButterKnife.bind(this,view);
			tvResults = new TextView[maxResultSum];
			for(int i = 0;i < tvResults.length;i++){
				tvResults[i] = mLlTimingResult.findViewById(i);
			}
		}
		
	}
	
}
