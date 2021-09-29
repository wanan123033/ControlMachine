package com.feipulai.host.activity.sporttime.adapter;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;
import com.feipulai.host.activity.sporttime.bean.SportTimeResult;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.utils.ResultDisplayUtils;


import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PartResultAdapter extends BaseQuickAdapter<SportTimeResult, PartResultAdapter.ViewHolder> {
    public PartResultAdapter(@Nullable List<SportTimeResult> data) {
        super(R.layout.item_part_result, data);
    }

    private int selectPosition = -1;

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
    }
    public int getSelectPosition() {
        return selectPosition;
    }

    @Override
    protected void convert(ViewHolder helper, SportTimeResult item) {
        helper.setText(R.id.tv_rule, item.getRouteName() + "");
        helper.setText(R.id.tv_receive_index, item.getReceiveIndex() == -1? "":item.getReceiveIndex()+"");
        helper.setText(R.id.tv_part_result, item.getPartResult() ==-1?"" :
                ResultDisplayUtils.getStrResultForDisplay(item.getPartResult(), false));
        helper.setText(R.id.tv_remark, item.getRemark() + "");

        if (selectPosition == helper.getLayoutPosition()) {
            helper.viewContent.setBackgroundColor(ContextCompat.getColor(mContext, R.color.blue_CB));
        }else {
            if (item.getResultState() == RoundResult.RESULT_STATE_FOUL){
                helper.viewContent.setBackgroundColor(ContextCompat.getColor(mContext, R.color.Yellow));
            }else {
                helper.viewContent.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
            }
        }
    }

    public static class ViewHolder extends BaseViewHolder {
        @BindView(R.id.view_content)
        public LinearLayout viewContent;
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
