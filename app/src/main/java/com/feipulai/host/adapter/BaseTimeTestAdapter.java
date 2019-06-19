package com.feipulai.host.adapter;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class BaseTimeTestAdapter extends BaseQuickAdapter<BaseStuPair, BaseTimeTestAdapter.ViewHolder> {

    private int mSelect;

    public BaseTimeTestAdapter(@Nullable List<BaseStuPair> data) {
        super(R.layout.gv_stu_hand_pair_item, data);
    }

    @Override
    protected void convert(ViewHolder holder, BaseStuPair pair) {
        holder.no.setText(pair.getBaseDevice().getDeviceId() + "");
        holder.result.setText(pair.getResult() + "");
        holder.result.setTextColor(Color.BLACK);
        int state = pair.getBaseDevice().getState();
        // TODO: 2018/7/31 0031 10:10
        //if (state == PullUpStateResult.STATE_DISCONNECT) {
        //   helper.no.setBackground(mContext.getResources().getDrawable(R.drawable.shape_round_textview_red));
        //} else if (state == PullUpStateResult.STATE_READY) {
        //   helper.result.setText("0");
        //} else if (item.getBaseDevice().getBatteryLeft() < 10) {
        //   helper.no.setBackground(mContext.getResources().getDrawable(R.drawable.shape_round_textview_yellow));
        //} else {
        //   helper.no.setBackground(mContext.getResources().getDrawable(R.drawable.shape_round_textview));
        //}
//        if (mSelect == holder.getLayoutPosition()) {
//
//            holder.itemView.setBackgroundColor(Color.rgb(30, 144, 255));
//        } else {
//            holder.itemView.setBackgroundColor(Color.rgb(211, 211, 211));
//        }
        switch (state) {

            case BaseDeviceState.STATE_DISCONNECT:
                holder.no.setBackground(mContext.getResources().getDrawable(R.drawable.shape_round_textview_red));
                //holder.tvStuInfo.setBackgroundColor(mSelect == holder.getLayoutPosition() ? Color.rgb(30,144,255) : Color.WHITE);
                break;

            case BaseDeviceState.STATE_LOW_BATTERY:
                holder.no.setBackground(mContext.getResources().getDrawable(R.drawable.shape_round_textview_yellow));
                break;

            case BaseDeviceState.STATE_FREE:
                holder.no.setBackground(mContext.getResources().getDrawable(R.drawable.shape_round_textview));
                break;

            case BaseDeviceState.STATE_CONFLICT:
                // TODO: 2018/7/31 0031 9:53 冲突的背景暂时不处理
                break;

            case BaseDeviceState.STATE_STOP_USE:
                holder.no.setBackground(mContext.getResources().getDrawable(R.drawable.shape_round_textview_grey));
                break;

        }
        //选中处理
        holder.rl.setBackgroundColor(mSelect == holder.getLayoutPosition() ? Color.rgb(30,144,255) : Color.WHITE);
    }


    public void setSelectItem(int position) {
        mSelect = position;
    }

    class ViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_device_id)
        TextView no;
        @BindView(R.id.rl_gv_pair)
        RelativeLayout rl;
        @BindView(R.id.tv_stu_info)
        TextView result;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
