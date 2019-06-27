package com.feipulai.exam.activity.MiddleDistanceRace.adapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.entity.ChipInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * created by ww on 2019/6/12.
 */
public class ChipSettingAdapter extends RecyclerView.Adapter<ChipSettingAdapter.VH> {
    private OnItemClickListener onRecyclerViewItemClickListener;

    //创建ViewHolder
    public static class VH extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_chip_setting_no)
        TextView tvChipSettingNo;
        @BindView(R.id.tv_chip_color_name)
        TextView tvChipColorName;
        @BindView(R.id.tv_chip_no)
        TextView tvChipNo;
        @BindView(R.id.tv_chip_ID1)
        TextView tvChipID1;
        @BindView(R.id.tv_chip_ID2)
        TextView tvChipID2;
        @BindView(R.id.ll_chip_item)
        LinearLayout llChipItem;

        public VH(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    private List<ChipInfo> mDatas;

    public ChipSettingAdapter(List<ChipInfo> data) {
        this.mDatas = data;
    }

    public interface OnItemClickListener {
        void onChipSettingLongClick(int position);
    }

    public void setOnRecyclerViewItemClickListener(OnItemClickListener onItemClickListener) {
        this.onRecyclerViewItemClickListener = onItemClickListener;
    }

    //在Adapter中实现3个方法
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final VH holder, final int position) {
        holder.tvChipColorName.setText(mDatas.get(position).getColorGroupName());
        holder.tvChipID1.setText(mDatas.get(position).getChipID1());
        holder.tvChipID2.setText(mDatas.get(position).getChipID2());
        if (currentposition == position) {
            if (IDCardNo == 1) {
                holder.tvChipID1.setBackgroundResource(R.color.green_yellow);
            } else if (IDCardNo == 2) {
                holder.tvChipID2.setBackgroundResource(R.color.green_yellow);
            }
        } else {
            holder.tvChipID2.setBackgroundResource(R.color.white);
            holder.tvChipID1.setBackgroundResource(R.color.white);
        }
        holder.tvChipNo.setText(mDatas.get(position).getVestNo() + "");
        holder.tvChipColorName.setBackgroundResource(mDatas.get(position).getColor());
        holder.tvChipSettingNo.setText(position + 1 + "");
        if (onRecyclerViewItemClickListener != null) {
            holder.llChipItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onRecyclerViewItemClickListener.onChipSettingLongClick(position);
                    return false;
                }
            });
        }
    }

    private boolean isChangeBackGround = false;
    private int IDCardNo = 0;
    private int currentposition = -1;


    public void changeBackGround(int n, int position) {
        isChangeBackGround = true;
        IDCardNo = n;
        currentposition = position;
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater.from指定写法
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chip_setting, parent, false);
        return new VH(v);
    }
}
