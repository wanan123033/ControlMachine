package com.feipulai.host.activity.radio_timer.newRadioTimer;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.feipulai.host.R;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceDetailsAdapter extends RecyclerView.Adapter<DeviceDetailsAdapter.ViewHolder> {
    protected List<StuDevicePair> stuPairs;
    private int selectedPosition;
    //private Context mContext;
    private int point;
    private int addNum;//在原基础上编号对应增加
    private DeviceDetailsAdapter.OnItemClickListener mOnItemClickListener;

    public DeviceDetailsAdapter(Context context, List<StuDevicePair> stuPairs, int point) {
        //mContext = context;
        this.stuPairs = stuPairs;
        this.point = point;
    }

    public void setAddNum(int addNum) {
        this.addNum = addNum;
    }

    public void setSelected(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public int getSelected() {
        return selectedPosition;
    }

    public void setOnItemClickListener(DeviceDetailsAdapter.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


    @Override
    public DeviceDetailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.rv_stu_dev_item, parent, false);
        return new DeviceDetailsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final DeviceDetailsAdapter.ViewHolder holder, int position) {
        StuDevicePair pair = stuPairs.get(position);
        if (position == 0) {
            holder.mTvDeviceId.setText("-");
        } else {
            holder.mTvDeviceId.setText(String.format(Locale.CHINA, "%d道", pair.getBaseDevice().getDeviceId()));
        }
        holder.mTvStuInfo.setText(String.format(Locale.CHINA,"%d%%", pair.getBattery()));

        //选中处理
        holder.mTvStuInfo.setBackgroundColor(position == selectedPosition ? Color.rgb(30, 144, 255) : Color.WHITE);
        if (mOnItemClickListener != null) {
            holder.mLlPair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.getAdapterPosition(), point);
                }
            });
        }
        if (pair.getBattery()<10 && position != selectedPosition){
            holder.mTvStuInfo.setBackgroundColor(Color.YELLOW);
        }

        if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_DISCONNECT && position != selectedPosition){
            holder.mTvStuInfo.setBackgroundColor(Color.RED);
            holder.mTvStuInfo.setText("");
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
        @BindView(R.id.ll_pair)
        LinearLayout mLlPair;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, int point);
    }
}
