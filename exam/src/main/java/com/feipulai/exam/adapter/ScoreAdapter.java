package com.feipulai.exam.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.List;

public class ScoreAdapter extends BaseQuickAdapter<RoundResult, ScoreAdapter.ScoreViewHolder> {
    public ScoreAdapter(@Nullable List<RoundResult> data) {
        super(android.R.layout.simple_list_item_1,data);
    }

    @Override
    protected void convert(ScoreViewHolder helper, RoundResult item) {
        helper.getView(android.R.id.text1).setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1));
        if (item.getResultState() == RoundResult.RESULT_STATE_NORMAL){
            helper.setText(android.R.id.text1,item.getRoundNo()+"-"+ResultDisplayUtils.getStrResultForDisplay(item.getResult()));
        }else {
            helper.setText(android.R.id.text1,ResultDisplayUtils.setResultState(item.getResultState()));
        }
    }

    static class ScoreViewHolder extends BaseViewHolder{

        public ScoreViewHolder(View view) {
            super(view);
        }
    }
}
