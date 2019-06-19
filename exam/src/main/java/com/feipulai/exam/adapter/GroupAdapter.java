package com.feipulai.exam.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.entity.Group;

import java.util.List;

/**
 * Created by pengjf on 2018/11/20.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class GroupAdapter extends BaseQuickAdapter<Group,BaseViewHolder>{
	
	/**
	 * 测试位
	 */
	private int testPosition = -1;
	
	public void setTestPosition(int testPosition){
		this.testPosition = testPosition;
	}
	
	public int getTestPosition(){
		return testPosition;
	}
	
	public GroupAdapter(@Nullable List data){
		super(R.layout.item_group,data);
	}
	
	@Override
	protected void convert(BaseViewHolder helper,Group item){
		StringBuffer sb = new StringBuffer();
		if(item.getGroupType() == Group.MALE){
			sb.append("男子");
		}else if(item.getGroupType() == Group.FEMALE){
			sb.append("女子");
		}else{
			sb.append("男女混合");
		}
		
		sb.append(item.getSortName() + String.format("第%1$d组",item.getGroupNo()));
		helper.setText(R.id.tv_group_name,sb);
		//是否测试完成 0-未测试 1-已测试  2-未测完
		int complete = item.getIsTestComplete();
		helper.setText(R.id.tv_isTest,complete == 0 ? "未测试" : (complete == 1 ? "已测试" : "未测完"));
	}
	
}
