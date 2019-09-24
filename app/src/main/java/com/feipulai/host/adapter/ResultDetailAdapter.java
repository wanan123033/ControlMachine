package com.feipulai.host.adapter;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;
import com.feipulai.host.activity.height_weight.HWConfigs;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.utils.ResultDisplayUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by James on 2018/2/7 0007.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */


public class ResultDetailAdapter extends BaseQuickAdapter<RoundResult, ResultDetailAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<RoundResult> mRoundResults;

    private List<RoundResult> mheightResults;
    private List<RoundResult> mweightResults;

    private boolean isHW;

    public ResultDetailAdapter(@Nullable List<RoundResult> data) {
        super(R.layout.item_result_detail, data);
    }

    public ResultDetailAdapter(@Nullable List<RoundResult> mheightResults, List<RoundResult> mweightResults) {
        super(R.layout.item_result_detail, mheightResults);
        this.mheightResults = mheightResults;
        this.mweightResults = mweightResults;
        isHW = true;
    }

    @Override
    protected void convert(ViewHolder viewHolder, final RoundResult roundResult) {
        String displayStr;
        if (!isHW) {
            viewHolder.mTvTimes.setText(roundResult.getRoundNo() + "");
            viewHolder.mTvTestTime.setText(roundResult.getTestTime());
            displayStr = ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult());
            viewHolder.mTvResult.setText(setResult(roundResult.getResultState(), displayStr));
        } else {
            RoundResult heightResult = roundResult;
            RoundResult weightResult = mweightResults.get(viewHolder.getLayoutPosition());

            displayStr = ResultDisplayUtils.getStrResultForDisplay(heightResult.getResult(), HWConfigs.HEIGHT_ITEM)
                    + ""
                    + ResultDisplayUtils.getStrResultForDisplay(weightResult.getResult(), HWConfigs.WEIGHT_ITEM);
            viewHolder.mTvTimes.setText(roundResult.getRoundNo() + "");
            viewHolder.mTvTestTime.setText(roundResult.getTestTime());
            viewHolder.mTvResult.setText(setResult(heightResult.getResultState(), displayStr));
        }
        if (viewHolder.getLayoutPosition() == 0) {
            viewHolder.mViewHead.setVisibility(View.VISIBLE);
            viewHolder.mTvStatus.setText(setResultState(roundResult.getResultState()));
        } else {
            viewHolder.mViewHead.setVisibility(View.GONE);
        }
    }

    private String setResult(int state, String result) {

        switch (state) {
            case RoundResult.RESULT_STATE_NORMAL:
                return result;
            case RoundResult.RESULT_STATE_FOUL:
                return "X";
            default:
                return result;
        }
    }

    private String setResultState(int state) {

        switch (state) {
            case RoundResult.RESULT_STATE_NORMAL:
                return mContext.getString(R.string.normal_test);
            case RoundResult.RESULT_STATE_FOUL:
                return mContext.getString(R.string.make_test);
            default:
                return mContext.getString(R.string.normal_test);
        }
    }

    static class ViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_times)
        TextView mTvTimes;
        @BindView(R.id.tv_result)
        TextView mTvResult;
        @BindView(R.id.tv_test_time)
        TextView mTvTestTime;
        @BindView(R.id.item_tv_status)
        TextView mTvStatus;
        @BindView(R.id.item_view_head)
        LinearLayout mViewHead;


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
