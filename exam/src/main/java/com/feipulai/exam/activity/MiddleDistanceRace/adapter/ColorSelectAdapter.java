package com.feipulai.exam.activity.MiddleDistanceRace.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.entity.ChipGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * created by ww on 2019/6/12.
 */
public class ColorSelectAdapter extends RecyclerView.Adapter<ColorSelectAdapter.VH> {

    private List<ChipGroup> chipGroups;
    private OnItemClickListener onRecyclerViewItemClickListener;

    //创建ViewHolder
    public static class VH extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_color_group_no)
        TextView tvColorGroupNo;
        @BindView(R.id.tv_color_group_name)
        TextView tvColorGroupName;
        @BindView(R.id.tv_color)
        TextView tvColor;
        @BindView(R.id.tv_color_group_size)
        TextView tvColorGroupSize;
        @BindView(R.id.tv_remark)
        TextView tvRemark;
        @BindView(R.id.ll_color_group)
        LinearLayout llColorGroupItem;

        public VH(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }


    public ColorSelectAdapter(List<ChipGroup> data) {
        this.chipGroups = data;
    }

    public interface OnItemClickListener {
        void onColorSelectClick(int position);
    }

    public void setOnRecyclerViewItemClickListener(OnItemClickListener onItemClickListener) {
        this.onRecyclerViewItemClickListener = onItemClickListener;
    }

    //在Adapter中实现3个方法
    @Override
    public void onBindViewHolder(final VH holder, final int position) {
        holder.tvColor.setText("");
        holder.tvColorGroupName.setText(chipGroups.get(position).getColorGroupName());
        holder.tvColorGroupNo.setText(position + 1 + "");
        holder.tvColorGroupSize.setText(chipGroups.get(position).getStudentNo() + "");
        holder.tvRemark.setText(chipGroups.get(position).getGroupType() == 0 ? "正常组" : "备用组");

        if (mPosi == position) {
            holder.llColorGroupItem.setBackgroundResource(R.color.blue);
        } else {
            holder.llColorGroupItem.setBackgroundResource(R.color.white);
        }

        holder.tvColor.setBackgroundResource(chipGroups.get(position).getColor());

        if (onRecyclerViewItemClickListener != null) {
            holder.llColorGroupItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRecyclerViewItemClickListener.onColorSelectClick(position);

                }
            });
        }
    }

    private int mPosi = -1;

    public void changeBackGround(int posi) {
        mPosi = posi;
    }

    @Override
    public int getItemCount() {
        return chipGroups.size();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater.from指定写法
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color_group, parent, false);
        return new VH(v);
    }
}
