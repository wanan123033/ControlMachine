package com.feipulai.exam.activity.basketball.adapter;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.basketball.BasketBallSetting;
import com.feipulai.exam.activity.basketball.result.BasketBallTestResult;
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.view.MySpinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zzs on  2019/6/18
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BasketBallResultAdapter extends BaseQuickAdapter<BasketBallTestResult, BasketBallResultAdapter.ViewHolder> {

    private int selectPosition = -1;

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
    }

    public int getSelectPosition() {
        return selectPosition;
    }

    private BasketBallSetting setting;

    public BasketBallResultAdapter(@Nullable List<BasketBallTestResult> data, BasketBallSetting setting) {
        super(R.layout.item_basketball_round_result, data);
        this.setting = setting;
    }

    @Override
    protected void convert(BasketBallResultAdapter.ViewHolder helper, final BasketBallTestResult item) {
        helper.txtRound.setText(item.getRoundNo() + "");
        helper.txtPenalizeNum.setText(item.getPenalizeNum() + "");
        String time = ResultDisplayUtils.getStrResultForDisplay(item.getResult());
        if (time.charAt(0) == '0' && time.charAt(1) == '0'){
            time = time.substring(3,time.toCharArray().length);
        }else if (time.charAt(0) == '0'){
            time = time.substring(1,time.toCharArray().length);
        }
        helper.txtPracticalResult.setText(item.getResult() < 0 ? "" : time);
//
        if (selectPosition == helper.getLayoutPosition()) {
            helper.viewContent.setBackgroundColor(ContextCompat.getColor(mContext, R.color.blue_CB));
        } else {
            helper.viewContent.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_F7));
        }
        helper.txtResultStatus.setText(setResultState(item.getResultState()));
        if (item.getMachineResultList() != null && item.getMachineResultList().size() > 0) {
            helper.spRoundResult.setAdapter(new MachineResultAdapter(mContext, item.getMachineResultList()));
            for (int i = 0; i < item.getMachineResultList().size(); i++) {
                if (item.getMachineResultList().get(i).getResult() == item.getSelectMachineResult()) {
                    helper.spRoundResult.setSelection(i);
                    break;
                }
            }
        } else {
            helper.spRoundResult.setAdapter(new MachineResultAdapter(mContext, new ArrayList<MachineResult>()));
        }
        helper.spRoundResult.setItemClick(new MySpinner.ItemClick() {
            @Override
            public void onClick(int position) {
                item.setResult(item.getMachineResultList().get(position).getResult() + ((int)(setting.getPenaltySecond() * item.getPenalizeNum() * 1000.0)));
                item.setSelectMachineResult(item.getMachineResultList().get(position).getResult());
                notifyDataSetChanged();
            }
        });

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
                return "";
        }
    }

    public static class ViewHolder extends BaseViewHolder {
        @BindView(R.id.view_content)
        public RelativeLayout viewContent;
        @BindView(R.id.item_txt_round)
        public TextView txtRound;
        @BindView(R.id.item_sp_round_result)
        public MySpinner spRoundResult;
        @BindView(R.id.item_txt_practical_result)
        public TextView txtPracticalResult;
        @BindView(R.id.item_txt_penalize_num)
        public TextView txtPenalizeNum;
        @BindView(R.id.item_txt_result_status)
        public TextView txtResultStatus;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
