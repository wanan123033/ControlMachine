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

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewDevicePairAdapter extends RecyclerView.Adapter<DeviceChangeAdapter.ViewHolder>{
    protected List<DeviceCollect> stuPairs;
    private int selectedPosition;

    public int getDevice() {
        return device;
    }

    private int device =1;//1 腰带 2肩胛
    //private Context mContext;
    private DeviceChangeAdapter.OnItemClickListener mOnItemClickListener;

    public NewDevicePairAdapter(Context context, List<DeviceCollect> stuPairs) {
        //mContext = context;
        this.stuPairs = stuPairs;
    }
    public void setSelected(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public int getSelected() {
        return selectedPosition;
    }

    public void setSelectDevice(int device){
        this.device = device;
    }
    public void setOnItemClickListener(DeviceChangeAdapter.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


    @Override
    public DeviceChangeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.rv_stu_dev_item, parent, false);
        return new DeviceChangeAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final DeviceChangeAdapter.ViewHolder holder, int position) {
        DeviceCollect pair = stuPairs.get(position);
        holder.mTvSitUp.setVisibility(View.VISIBLE);
        holder.mTvArm.setVisibility(View.VISIBLE);
        holder.v.setVisibility(View.VISIBLE);
        holder.mTvDeviceId.setText(String.format(Locale.CHINA, "%d", pair.getSitPushUpStateResult().getDeviceId()));
        holder.mTvStuInfo.setVisibility(View.GONE);
        if (position == selectedPosition && device == 2){
            holder.mTvArm.setBackgroundColor(Color.BLUE);
        }else {
            holder.mTvArm.setBackgroundColor(Color.WHITE);
        }

        if (position == selectedPosition && device == 1){
            holder.mTvSitUp.setBackgroundColor(Color.BLUE);
        }else {
            holder.mTvSitUp.setBackgroundColor(Color.WHITE);
        }

        //选中处理
//        holder.mLlPair.setBackgroundColor(position == selectedPosition ? Color.rgb(30, 144, 255) : Color.WHITE);
        if (mOnItemClickListener != null) {
            holder.mTvSitUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v.getId(),holder.getAdapterPosition());
                    holder.mTvSitUp.setBackgroundColor(Color.BLUE);
                    notifyDataSetChanged();
                }
            });

            holder.mTvArm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v.getId(),holder.getAdapterPosition());
                    holder.mTvArm.setBackgroundColor(Color.BLUE);
                    notifyDataSetChanged();
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
