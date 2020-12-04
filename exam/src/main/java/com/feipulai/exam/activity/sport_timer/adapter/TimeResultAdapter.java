package com.feipulai.exam.activity.sport_timer.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.sport_timer.bean.SportTimeResult;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.List;

public class TimeResultAdapter extends BaseQuickAdapter <SportTimeResult,BaseViewHolder>{
    public TimeResultAdapter(@Nullable List data) {
        super(R.layout.item_time_result, data);
    }


    @Override
    protected void convert(BaseViewHolder helper, SportTimeResult item) {
        helper.setText(R.id.item_txt_round,item.getRound()+"");
        helper.setText(R.id.item_txt_result,item.getResult()==-1?"":
                ResultDisplayUtils.getStrResultForDisplay(item.getPartResult(), false));
        helper.setText(R.id.item_txt_result_status,item.getResultState() == RoundResult.RESULT_STATE_NORMAL?"正常":
                item.getResultState() ==RoundResult.RESULT_STATE_FOUL?"犯规":"");
    }
}
