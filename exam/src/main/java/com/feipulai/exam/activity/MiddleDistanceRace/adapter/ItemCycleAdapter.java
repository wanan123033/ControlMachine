package com.feipulai.exam.activity.MiddleDistanceRace.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.config.SharedPrefsConfigs;
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
    public static final int FLAG_CYCLE=1;
    public static final int FLAG_ITEMCODE=2;
    private Context mContext;

    //创建ViewHolder
    public static class VH extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_cycle_no)
        TextView tvCycleNo;
        @BindView(R.id.tv_cycle_item)
        TextView tvCycleItem;
        @BindView(R.id.tv_cycles)
        TextView tvCycles;
        @BindView(R.id.tv_item_code)
        TextView tvItemCode;
        @BindView(R.id.ll_race_cycles)
        LinearLayout llRaceCycles;

        public VH(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }


    public ItemCycleAdapter(Context mContext, List<Item> data) {
        this.items = data;
        this.mContext=mContext;
    }

    public interface OnItemClickListener {
//        void onItemCycleLongClick(int position);
        void onItemClick(int position,int flag);
    }

    public void setOnRecyclerViewItemClickListener(OnItemClickListener onItemClickListener) {
        this.onRecyclerViewItemClickListener = onItemClickListener;
    }

    //在Adapter中实现3个方法
    @Override
    public void onBindViewHolder(final VH holder, final int position) {
        holder.tvCycleItem.setText(items.get(position).getItemName());
        holder.tvCycleNo.setText(position + 1 + "");

        String cycleNo = SharedPrefsUtil.getValue(mContext, SharedPrefsConfigs.DEFAULT_PREFS, items.get(position).getItemName(), "0");
        holder.tvCycles.setText(cycleNo);
        holder.tvItemCode.setText(TextUtils.isEmpty(items.get(position).getItemCode())?"":items.get(position).getItemCode());
        if (onRecyclerViewItemClickListener != null) {
//            holder.llRaceCycles.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    onRecyclerViewItemClickListener.onItemCycleLongClick(position);
//                    return false;
//                }
//            });

            holder.tvCycles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRecyclerViewItemClickListener.onItemClick(position,FLAG_CYCLE);
                }
            });

            holder.tvItemCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRecyclerViewItemClickListener.onItemClick(position,FLAG_ITEMCODE);
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
