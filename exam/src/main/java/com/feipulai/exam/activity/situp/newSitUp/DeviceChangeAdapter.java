package com.feipulai.exam.activity.situp.newSitUp;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.sport_timer.bean.DeviceState;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceChangeAdapter extends RecyclerView.Adapter<DeviceChangeAdapter.ViewHolder> {
    protected List<DeviceCollect> stuPairs;
    private int selectedPosition;
    //private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public DeviceChangeAdapter(Context context, List<DeviceCollect> stuPairs) {
        //mContext = context;
        this.stuPairs = stuPairs;
    }
    public void setSelected(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public int getSelected() {
        return selectedPosition;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.rv_stu_dev_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        DeviceCollect pair = stuPairs.get(position);
        holder.mTvSitUp.setVisibility(View.VISIBLE);
        holder.mTvArm.setVisibility(View.VISIBLE);
        holder.v.setVisibility(View.VISIBLE);
        holder.mTvDeviceId.setText(String.format(Locale.CHINA, "%d", pair.getSitPushUpStateResult().getDeviceId()));
        holder.mTvStuInfo.setVisibility(View.GONE);
        switch (pair.getSitPushUpStateResult().getState()){
            case BaseDeviceState.STATE_DISCONNECT:
                holder.mTvSitUp.setBackgroundColor(Color.RED);
                break;
            case BaseDeviceState.STATE_COUNTING:
                holder.mTvSitUp.setBackgroundColor(Color.WHITE);
                break;
            case BaseDeviceState.STATE_LOW_BATTERY:
                holder.mTvSitUp.setBackgroundColor(Color.YELLOW);
                break;
            case BaseDeviceState.STATE_STOP_USE:
                holder.mTvSitUp.setBackgroundColor(Color.GRAY);
                break;
        }
        switch (pair.getArmStateResult().getState()){
            case BaseDeviceState.STATE_DISCONNECT:
                holder.mTvArm.setBackgroundColor(Color.RED);
                break;
            case BaseDeviceState.STATE_COUNTING:
                holder.mTvArm.setBackgroundColor(Color.WHITE);
                break;
            case BaseDeviceState.STATE_LOW_BATTERY:
                holder.mTvArm.setBackgroundColor(Color.YELLOW);
                break;
            case BaseDeviceState.STATE_STOP_USE:
                holder.mTvArm.setBackgroundColor(Color.GRAY);
                break;
        }
        //选中处理
//        holder.mLlPair.setBackgroundColor(position == selectedPosition ? Color.rgb(30, 144, 255) : Color.WHITE);
        if (mOnItemClickListener != null) {
            holder.mTvSitUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v.getId(),holder.getAdapterPosition());
                    holder.mTvSitUp.setBackgroundColor(Color.BLUE);
                }
            });

            holder.mTvArm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v.getId(),holder.getAdapterPosition());
                    holder.mTvArm.setBackgroundColor(Color.BLUE);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return stuPairs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_device_id)
        public TextView mTvDeviceId;
        @BindView(R.id.tv_stu_info)
        TextView mTvStuInfo;
        @BindView(R.id.tv_sit_up)
        TextView mTvSitUp;
        @BindView(R.id.tv_arm)
        TextView mTvArm;
        @BindView(R.id.ll_pair)
        LinearLayout mLlPair;
        @BindView(R.id.view1)
        View v;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int viewId,int position);
    }
}
