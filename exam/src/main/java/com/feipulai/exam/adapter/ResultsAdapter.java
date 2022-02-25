package com.feipulai.exam.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.entity.RoundResult;

import java.util.List;

/**
 * Created by pengjf on 2019/4/8.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ResultsAdapter extends BaseQuickAdapter<RoundResult,BaseViewHolder> {

    public ResultsAdapter(@Nullable List<RoundResult> data) {
        super(R.layout.item_results, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, RoundResult item) {
        helper.setText(R.id.tv_result,RoundResult.resultStateStr(item.getResultState(),item.getResult()) );
        helper.setText(R.id.tv_index, String.format("第%s次:", toChineseIndex(helper.getAdapterPosition())));
    }

    private String toChineseIndex(int num){
        if (num >8)
            return "";
        String [] c = {"一","二","三","四","五","六","七","八"};
        return c[num];
    }
}
