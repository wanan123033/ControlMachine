package com.feipulai.exam.activity.jump_rope.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.adapter.GroupStateAdapter;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.adapter.ScheduleAdapter;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.Schedule;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

/**
 * Created by James on 2018/12/10 0010.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class GroupCheckFragment
		extends Fragment implements AdapterView.OnItemClickListener{
	
	@BindView(R.id.sp_schedule)
	Spinner mSpSchedule;
	@BindView(R.id.tv_group_name)
	TextView mTvGroupName;
	@BindView(R.id.iv_before)
	ImageView mIvBefore;
	@BindView(R.id.iv_next)
	ImageView mIvNext;
	
	// 当前项目的所有分组日程信息
	private List<Schedule> mAllSchedules;
	// 用于显示查询到的分组信息
	private ListView lvResults;
	// 用于存放当前日程的分组信息list
	private List<Group> mGroupList;
	//用于存放分组信息和对应的状态,在mGroupList的基础上进行验证
	private OnGroupCheckInListener listener;
	private GroupStateAdapter mAdapter;
	private int mCurrentGroupPosition;
	
	public void setListener(OnGroupCheckInListener listener){
		this.listener = listener;
	}
	
	public void setResultView(ListView lvResults){
		this.lvResults = lvResults;
	}
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,@Nullable ViewGroup container,Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.frag_group_check,container,false);
		ButterKnife.bind(this,view);
		
		mAllSchedules = DBManager.getInstance().getCurrentSchedules();
		
		if(mAllSchedules != null && mAllSchedules.size() > 0){
			mSpSchedule.setAdapter(new ScheduleAdapter(getActivity(),mAllSchedules));
			updateGroup(mAllSchedules.get(0).getScheduleNo());
			lvResults.setOnItemClickListener(this);
		}
		return view;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent,View view,int position,long id){
		// 选中了一个分组,检查该分组是否可以被分组检录,如果可以则检录
		// 从组内选出可以测试的学生检录
		Group checkGroup = mGroupList.get(position);
		if(listener != null){
			// 改组有人未测试(没有在改组测试的成绩)
			listener.onGroupCheckIn(checkGroup);
		}
		mTvGroupName.setText(InteractUtils.generateGroupText(checkGroup));
		lvResults.setVisibility(View.GONE);
		mCurrentGroupPosition = position;
	}
	
	@Override
	public void onDestroyView(){
		super.onDestroyView();
		//null.unbind();
	}
	
	@OnItemSelected({R.id.sp_schedule})
	public void spinnerItemSelected(Spinner spinner,int position){
		switch(spinner.getId()){
			
			case R.id.sp_schedule:
				// 日程更新了,需要根据新的日程信息更新分组的显示
				updateGroup(mAllSchedules.get(position).getScheduleNo());
				break;
			
		}
	}
	
	private void updateGroup(String scheduleNo){
		List<Group> groups = DBManager.getInstance().getGroupByScheduleNo(scheduleNo);
		lvResults.setVisibility(View.GONE);
		if(groups == null || groups.size() == 0){
			mGroupList = new ArrayList<>(0);
		}else{
			mGroupList = new ArrayList<>(groups.size());
			// 筛选出可测分组
			for (Group group : groups) {
				if(InteractUtils.isTestableGroup(group)){
					mGroupList.add(group);
				}
			}
		}
		mTvGroupName.setText("请选择分组");
		mAdapter = new GroupStateAdapter(getActivity(),mGroupList);
		lvResults.setAdapter(mAdapter);
		mCurrentGroupPosition = 0;
	}
	
	@OnClick({R.id.tv_group_name,R.id.iv_before,R.id.iv_next})
	public void onViewClicked(View view){
		switch(view.getId()){
			
			case R.id.tv_group_name:
				int currentVisibility = lvResults.getVisibility();
				lvResults.setVisibility(currentVisibility == View.GONE ? View.VISIBLE : View.GONE);
				break;
			
			case R.id.iv_before:
				// 选择前一个未测试分组进行测试
				for(int i = mCurrentGroupPosition + 1;i < mGroupList.size();i++){
					Group group = mGroupList.get(i);
					if(InteractUtils.isTestableGroup(group) && listener != null){
						listener.onGroupCheckIn(group);
						return;
					}
				}
				InteractUtils.toastSpeak(getActivity(),"无可测分组");
				break;
			
			case R.id.iv_next:
				// 选择前一个未测试分组进行测试
				for(int i = mCurrentGroupPosition - 1;i >= 0;i--){
					Group group = mGroupList.get(i);
					if(InteractUtils.isTestableGroup(group) && listener != null){
						listener.onGroupCheckIn(group);
						return;
					}
				}
				InteractUtils.toastSpeak(getActivity(),"无可测分组");
				break;
		}
	}
	
	public interface OnGroupCheckInListener{
		/**
		 * 这里传入的考生信息均通过了验证,且不会有重复
		 */
		void onGroupCheckIn(Group students);
	}
	
}
