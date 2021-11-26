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

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewDevicePairAdapter extends RecyclerView.Adapter<NewDevicePairAdapter.ViewHolder>{
    protected List<DeviceCollect> stuPairs;
    private int selectedPosition;

    public int getDevice() {
        return device;
    }

    private int device =1;//1 腰带 2肩胛
    //private Context mContext;
    private OnItemClickListener mOnItemClickListener;

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
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


    @Override
    public NewDevicePairAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.rv_stu_dev_item, parent, false);
        return new NewDevicePairAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final NewDevicePairAdapter.ViewHolder holder, int position) {
        DeviceCollect pair = stuPairs.get(position);
        holder.mTvSitUp.setVisibility(View.VISIBLE);
        holder.mTvShoulder.setVisibility(View.VISIBLE);
        holder.v.setVisibility(View.VISIBLE);
        holder.mTvDeviceId.setText(String.format(Locale.CHINA, "%d", pair.getSitPushUpStateResult().getDeviceId()));
        holder.mTvStuInfo.setVisibility(View.GONE);
        if (position == selectedPosition && device == 1){
            holder.mTvSitUp.setBackgroundColor(Color.BLUE);
        }else {
            holder.mTvSitUp.setBackgroundColor(Color.WHITE);
        }
        if (position == selectedPosition && device == 2){
            holder.mTvShoulder.setBackgroundColor(Color.BLUE);
        }else {
            holder.mTvShoulder.setBackgroundColor(Color.WHITE);
        }

        holder.mTvSitUp.setText(stuPairs.get(position).getSitPushUpStateResult().getState() == BaseDeviceState.STATE_FREE ? "√" : "");
        holder.mTvShoulder.setText(stuPairs.get(position).getShoulderResult().getState() == BaseDeviceState.STATE_FREE ? "√" : "");
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

            holder.mTvShoulder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v.getId(),holder.getAdapterPosition());
                    holder.mTvShoulder.setBackgroundColor(Color.BLUE);
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
        @BindView(R.id.tv_sit_up)//腰带
        TextView mTvSitUp;
        @BindView(R.id.tv_arm)
        TextView mTvShoulder;//肩胛
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
