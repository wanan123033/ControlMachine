package com.feipulai.host.activity.jump_rope.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.host.R;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.utils.ResultDisplayUtils;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 公司 深圳菲普莱体育
 * 密级 绝密
 */
public class ResultDisplayAdapter extends RecyclerView.Adapter<ResultDisplayAdapter.ViewHolder> {
	
	private Map<Student, RoundResult> results;
	private Map<Student, RoundResult> bestResults;
	private List<StuDevicePair> pairs;
	
	public ResultDisplayAdapter(List<StuDevicePair> pairs,
								@Nullable Map<Student, RoundResult> results,
								Map<Student, RoundResult> bestResults) {
		this.pairs = pairs;
		this.results = results;
		this.bestResults = bestResults;
	}
	
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_resdisplay_item, parent, false);
		return new ViewHolder(layout);
	}
	
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		Student student = pairs.get(position).getStudent();
		holder.tvStuCode.setText(student.getStudentCode());
		holder.tvStuName.setText(student.getStudentName());
		
		RoundResult result = results.get(student);
		RoundResult bestResult = bestResults.get(student);
		
		if (result == null) {
			holder.tvResult.setText(R.string.no_test);
		} else {
			holder.tvResult.setText(ResultDisplayUtils.getStrResultForDisplay(result.getResult()));
		}
		if (bestResult == null) {
			holder.tvBestResult.setText(R.string.no_test);
		}else{
			holder.tvBestResult.setText(ResultDisplayUtils.getStrResultForDisplay(bestResult.getResult()));
			
		}
	}
	
	@Override
	public int getItemCount() {
		return pairs.size();
	}
	
	static class ViewHolder extends RecyclerView.ViewHolder {
		@BindView(R.id.tv_stuCode)
		TextView tvStuCode;
		@BindView(R.id.tv_stuName)
		TextView tvStuName;
		@BindView(R.id.tv_result)
		TextView tvResult;
		@BindView(R.id.tv_best_result)
		TextView tvBestResult;
		@BindView(R.id.ll_title)
		LinearLayout llTitle;
		
		ViewHolder(View view) {
			super(view);
			ButterKnife.bind(this, view);
		}
	}
	
}
