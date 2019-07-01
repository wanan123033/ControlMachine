package com.feipulai.exam.activity.MiddleDistanceRace.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.entity.ChipGroup;
import com.feipulai.exam.entity.Item;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * created by ww on 2019/6/12.
 */
public class ItemCycleAdapter extends RecyclerView.Adapter<ItemCycleAdapter.VH> {

    private List<Item> items;
    private OnItemClickListener onRecyclerViewItemClickListener;

    //创建ViewHolder
    public static class VH extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_cycle_no)
        TextView tvCycleNo;
        @BindView(R.id.tv_cycle_item)
        TextView tvCycleItem;
        @BindView(R.id.tv_cycles)
        TextView tvCycles;
        @BindView(R.id.ll_race_cycles)
        LinearLayout llRaceCycles;

        public VH(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }


    public ItemCycleAdapter(List<Item> data) {
        this.items = data;
    }

    public interface OnItemClickListener {
        void onItemCycleLongClick(int position);
    }

    public void setOnRecyclerViewItemClickListener(OnItemClickListener onItemClickListener) {
        this.onRecyclerViewItemClickListener = onItemClickListener;
    }

    //在Adapter中实现3个方法
    @Override
    public void onBindViewHolder(final VH holder, final int position) {
        holder.tvCycleItem.setText(items.get(position).getItemName());
        holder.tvCycleNo.setText(position + 1 + "");
        holder.tvCycles.setText(items.get(position).getCycleNo() + "");
        if (onRecyclerViewItemClickListener != null) {
            holder.llRaceCycles.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onRecyclerViewItemClickListener.onItemCycleLongClick(position);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater.from指定写法
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_race_cycles, parent, false);
        return new VH(v);
    }
}
