package com.feipulai.exam.activity.volleyball.adapter;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.volleyball.stepView.StepBean;
import com.feipulai.exam.activity.volleyball.stepView.VerticalStepView;

import java.util.List;

/**
 * Created by zzs on  2019/7/17
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class CheckDeviceAdapter extends BaseQuickAdapter<List<StepBean>, BaseViewHolder> {
    public CheckDeviceAdapter(@Nullable List<List<StepBean>> data) {
        super(R.layout.item_velleyball_check, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, List<StepBean> item) {

        VerticalStepView stepLeft = helper.getView(R.id.item_step_left);
        VerticalStepView stepRight = helper.getView(R.id.item_step_right);
        stepLeft.setShowTextView(false);
        stepLeft.setStepViewTexts(item)
                .setLinePaddingProportion(0.06f)//设置indicator线与线间距的比例系数
                .setCircleRadius(5)
                .setDefaultStepIndicatorNum(mContext.getResources().getDimensionPixelOffset(R.dimen.dp_20))
                .setStepsViewIndicatorCompletedLineColor(ContextCompat.getColor(mContext, android.R.color.transparent))//设置StepsViewIndicator完成线的颜色
                .setStepsViewIndicatorUnCompletedLineColor(ContextCompat.getColor(mContext, android.R.color.transparent))//设置StepsViewIndicator未完成线的颜色
                .setStepViewComplectedTextColor(ContextCompat.getColor(mContext, R.color.viewfinder_laser));//设置StepsView text完成线的颜色
        stepRight.setStepViewTexts(item)
                .setLinePaddingProportion(0.06f)//设置indicator线与线间距的比例系数
                .setCircleRadius(5)
                .setDefaultStepIndicatorNum(mContext.getResources().getDimensionPixelOffset(R.dimen.dp_20))
                .setStepsViewIndicatorCompletedLineColor(ContextCompat.getColor(mContext, android.R.color.transparent))//设置StepsViewIndicator完成线的颜色
                .setStepsViewIndicatorUnCompletedLineColor(ContextCompat.getColor(mContext, android.R.color.transparent))//设置StepsViewIndicator未完成线的颜色
                .setStepViewComplectedTextColor(ContextCompat.getColor(mContext, R.color.viewfinder_laser));//设置StepsView text完成线的颜色

        if (item.contains(new StepBean("", -1))) {
            stepLeft.setStepBackgroundResource(R.drawable.default_icon_bg);
            stepRight.setStepBackgroundResource(R.drawable.default_icon_bg);
        }
        if (item.contains(new StepBean("", 0))) {
            stepLeft.setStepBackgroundResource(R.drawable.attention_bg);
            stepRight.setStepBackgroundResource(R.drawable.attention_bg);
        }
        if (item.contains(new StepBean("", 1))) {
            stepLeft.setStepBackgroundResource(R.drawable.complted_bg);
            stepRight.setStepBackgroundResource(R.drawable.complted_bg);
        }


    }
}
