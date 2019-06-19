package com.feipulai.host.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

/**
 * Created by James on 2018/8/15 0015.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class NoActionAnimator extends RecyclerView.ItemAnimator{
	
	@Override
	public boolean animateDisappearance(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo itemHolderInfo, @Nullable ItemHolderInfo itemHolderInfo1) {
		return false;
	}
	
	@Override
	public boolean animateAppearance(@NonNull RecyclerView.ViewHolder viewHolder, @Nullable ItemHolderInfo itemHolderInfo, @NonNull ItemHolderInfo itemHolderInfo1) {
		return false;
	}
	
	@Override
	public boolean animatePersistence(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo itemHolderInfo, @NonNull ItemHolderInfo itemHolderInfo1) {
		return false;
	}
	
	@Override
	public boolean animateChange(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1, @NonNull ItemHolderInfo itemHolderInfo, @NonNull ItemHolderInfo itemHolderInfo1) {
		return false;
	}
	
	@Override
	public void runPendingAnimations(){
	}
	
	// @Override
	// public boolean animateRemove(RecyclerView.ViewHolder holder){
	// 	return false;
	// }
	
	// @Override
	// public boolean animateAdd(RecyclerView.ViewHolder holder){
	// 	return false;
	// }
	//
	// @Override
	// public boolean animateMove(RecyclerView.ViewHolder holder,int fromX,int fromY,int toX,int toY){
	// 	return false;
	// }
	//
	// @Override
	// public boolean animateChange(RecyclerView.ViewHolder oldHolder,RecyclerView.ViewHolder newHolder,int fromLeft,int fromTop,int toLeft,int toTop){
	// 	return false;
	// }

	@Override
	public void endAnimation(RecyclerView.ViewHolder item){
	
	}
	
	@Override
	public void endAnimations(){
	
	}
	
	@Override
	public boolean isRunning(){
		return false;
	}
	
}
