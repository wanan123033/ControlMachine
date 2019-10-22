package com.feipulai.exam.activity.MiddleDistanceRace.adapter;

import android.graphics.Color;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

/**
 * created by ww on 2019/10/21.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperListener listener;
    /**
     * 是否可以拖拽
     */
    private boolean isCanDrag = false;
    /**
     * 是否可以被滑动
     */
    private boolean isCanSwipe = false;

    public ItemTouchHelperCallback(ItemTouchHelperListener adapter) {
        listener = adapter;
    }

    public ItemTouchHelperCallback(ItemTouchHelperListener adapter, boolean canDrag, boolean canSwipe) {
        listener = adapter;
        this.isCanDrag = canDrag;
        this.isCanSwipe = canSwipe;
    }

    /**
     * 设置是否可以被拖拽
     *
     * @param canDrag 是true，否false
     */
    public void setDragEnable(boolean canDrag) {
        isCanDrag = canDrag;
    }

    /**
     * 设置是否可以被滑动
     *
     * @param canSwipe 是true，否false
     */
    public void setSwipeEnable(boolean canSwipe) {
        isCanSwipe = canSwipe;
    }

    /**
     * 当Item被长按的时候是否可以被拖拽
     *
     * @return
     */
    @Override
    public boolean isLongPressDragEnabled() {
        return isCanDrag;
    }

    /**
     * Item是否可以被滑动(H：左右滑动，V：上下滑动)
     *
     * @return
     */
    @Override
    public boolean isItemViewSwipeEnabled() {
        return isCanSwipe;
    }

    /**
     * 当用户拖拽或者滑动Item的时候需要我们告诉系统滑动或者拖拽的方向
     *
     * @param recyclerView
     * @param viewHolder
     * @return
     */
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {// GridLayoutManager
            // flag如果值是0，相当于这个功能被关闭
            int dragFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlag = 0;
            // create make
            return makeMovementFlags(dragFlag, swipeFlag);
        } else if (layoutManager instanceof LinearLayoutManager) {// linearLayoutManager
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            int orientation = linearLayoutManager.getOrientation();

            int dragFlag = 0;
            int swipeFlag = 0;

            // 为了方便理解，相当于分为横着的ListView和竖着的ListView
            if (orientation == LinearLayoutManager.HORIZONTAL) {// 如果是横向的布局
                swipeFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                dragFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            } else if (orientation == LinearLayoutManager.VERTICAL) {// 如果是竖向的布局，相当于ListView
                dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                swipeFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            }
            return makeMovementFlags(dragFlag, swipeFlag);
        }
        return 0;
    }

    /**
     * 当Item被拖拽的时候被回调
     *
     * @param recyclerView
     * @param viewHolder
     * @param target
     * @return
     */
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        /**
         * 回调
         */
        listener.onMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
        /**
         * 回调
         */
        listener.onSwipe(viewHolder.getAdapterPosition());
    }

    public static boolean isIdle = false;

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        //当开始拖拽的时候
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
            isIdle = true;
        }
    }

    //当手指松开的时候
    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        super.clearView(recyclerView, viewHolder);
        if (isIdle) {
            Log.i("clearView", "-------------------");
            listener.onClear();
            isIdle = false;
        }
    }
}
