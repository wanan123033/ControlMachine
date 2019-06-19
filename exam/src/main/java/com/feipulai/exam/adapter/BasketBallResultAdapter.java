package com.feipulai.exam.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Spinner;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.basketball.result.BasketBallTestResult;
import com.feipulai.exam.entity.RoundResult;

import java.util.List;

/**
 * Created by zzs on  2019/6/18
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BasketBallResultAdapter extends BaseQuickAdapter<BasketBallTestResult, BaseViewHolder> {

    private int selectPosition = 0;

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
    }

    public int getSelectPosition() {
        return selectPosition;
    }

    public BasketBallResultAdapter(@Nullable List<BasketBallTestResult> data) {
        super(R.layout.item_basketball_round_result, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, BasketBallTestResult item) {
        helper.setText(R.id.item_txt_round, item.getRoundNo() + "");
        helper.setText(R.id.item_txt_penalize_num, item.getPenalizeNum() + "");
        View view = helper.getView(R.id.view_content);
        if (selectPosition == helper.getLayoutPosition()) {
            view.setSelected(true);
        } else {
            view.setSelected(false);
        }
        helper.setText(R.id.item_txt_result_status, setResultState(item.getResultState()));
        Spinner spResult = helper.getView(R.id.sp_round_result);
        spResult.setAdapter(new MachineResultAdapter(mContext, item.getMachineResultList()));
    }

    private String setResultState(int state) {

        switch (state) {
            case RoundResult.RESULT_STATE_NORMAL:
                return "正常";
            case RoundResult.RESULT_STATE_FOUL:
                return "犯规";
            case RoundResult.RESULT_STATE_BACK:
                return "中退";
            case RoundResult.RESULT_STATE_WAIVE:
                return "放弃";
            default:
                return "正常考";
        }
    }
}
