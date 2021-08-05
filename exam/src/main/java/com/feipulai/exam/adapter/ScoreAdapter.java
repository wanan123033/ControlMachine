package com.feipulai.exam.adapter;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.List;

public class ScoreAdapter extends BaseQuickAdapter<RoundResult, ScoreAdapter.ScoreViewHolder> {
    private final List<RoundResult> data;
    private OnItemChildClickListener listener;

    public ScoreAdapter(@Nullable List<RoundResult> data) {
        super(R.layout.item_score,data);
        this.data = data;
    }

    @Override
    public void setOnItemChildClickListener(OnItemChildClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void convert(final ScoreViewHolder helper, final RoundResult item) {
        TextView textView = helper.getView(R.id.tv_result);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1));
        if (item.getResultState() == RoundResult.RESULT_STATE_NORMAL){
            helper.setText(R.id.tv_result,item.getRoundNo()+"-"+ResultDisplayUtils.getStrResultForDisplay(item.getResult()));
        }else {
            helper.setText(R.id.tv_result,ResultDisplayUtils.setResultState(item.getResultState()));
        }
        if (item.isDelete()){
            helper.setBackgroundColor(R.id.tv_result, Color.BLUE);
        }else {
            helper.setBackgroundColor(R.id.tv_result, Color.WHITE);
        }

        if (item.getExamType() == 2){
            textView.append("(补考)");
        }
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("TAG","onClick-------------");
                for (RoundResult result : data){
                    result.setIsDelete(false);
                }
                item.setIsDelete(true);
                notifyDataSetChanged();
            }
        });
    }

    static class ScoreViewHolder extends BaseViewHolder{

        public ScoreViewHolder(View view) {
            super(view);
        }
    }
}
