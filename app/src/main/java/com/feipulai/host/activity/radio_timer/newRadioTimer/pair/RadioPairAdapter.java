package com.feipulai.host.activity.radio_timer.newRadioTimer.pair;

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

public class RadioPairAdapter extends RecyclerView.Adapter<RadioPairAdapter.ViewHolder> {
    protected List<StuDevicePair> stuPairs;
    private int selectedPosition;
    //private Context mContext;
    private int point ;
    private int addNum;//在原基础上编号对应增加
    private OnItemClickListener mOnItemClickListener;
    public RadioPairAdapter(Context context, List<StuDevicePair> stuPairs,int point) {
        //mContext = context;
        this.stuPairs = stuPairs;
        this.point = point;
    }
    public void setAddNum(int addNum){
        this.addNum = addNum;
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
        StuDevicePair pair = stuPairs.get(position);
        BaseDeviceState deviceState = pair.getBaseDevice();
        if (position == 0){
            holder.mTvDeviceId.setText("-");
        }else {
            holder.mTvDeviceId.setText(String.format(Locale.CHINA,"%d道", deviceState.getDeviceId()));
        }
        if (point == 1 && addNum>0){
            if (position == 0){
                holder.mTvStuInfo.setText(deviceState.getState() == BaseDeviceState.STATE_FREE ? "√" : "");
            }else {
                String num = "子机编号"+(deviceState.getDeviceId()+addNum);
                holder.mTvStuInfo.setText(deviceState.getState() == BaseDeviceState.STATE_FREE ? String.format("√%s", num) : num);
            }
        }else {
            holder.mTvStuInfo.setText(deviceState.getState() == BaseDeviceState.STATE_FREE ? "√" : "");
        }

        //选中处理
        holder.mLlPair.setBackgroundColor(position == selectedPosition ? Color.rgb(30, 144, 255) : Color.WHITE);
        if (mOnItemClickListener != null) {
            holder.mLlPair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.getAdapterPosition(),point);
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
