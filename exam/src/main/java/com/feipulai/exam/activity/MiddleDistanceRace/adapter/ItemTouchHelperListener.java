package com.feipulai.exam.activity.MiddleDistanceRace.adapter;

/**
 * created by ww on 2019/10/21.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public interface ItemTouchHelperListener {
    /**
     * @param fromPosition 起始位置
     * @param toPosition 移动的位置
     */
    void onMove(int fromPosition, int toPosition);
    void onSwipe(int position);
    void onClear();
}
