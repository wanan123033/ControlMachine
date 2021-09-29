package com.feipulai.host.activity.sporttime.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;
import com.feipulai.host.activity.sporttime.SportTestResult;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.utils.ResultDisplayUtils;


import java.util.List;

public class TimeResultAdapter extends BaseQuickAdapter <SportTestResult,BaseViewHolder>{
    public TimeResultAdapter(@Nullable List data) {
        super(R.layout.item_time_result1, data);
    }
    private int pos;

    @Override
    protected void convert(BaseViewHolder helper, SportTestResult item) {
        helper.setText(R.id.item_txt_round,item.getRound()+"");
        helper.setText(R.id.item_txt_result,item.getResult()==-1?"":
                ResultDisplayUtils.getStrResultForDisplay(item.getResult(), false));
        helper.setText(R.id.item_txt_result_status,item.getResultState() == RoundResult.RESULT_STATE_NORMAL?"正常":
                item.getResultState() ==RoundResult.RESULT_STATE_FOUL?"犯规":"");
    }

    public void setSelectPosition(int i) {
        pos = i;
    }

    public int getSelectPosition() {
        return pos;
    }
}
