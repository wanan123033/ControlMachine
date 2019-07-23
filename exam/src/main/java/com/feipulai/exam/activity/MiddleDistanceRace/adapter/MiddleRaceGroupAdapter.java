package com.feipulai.exam.activity.MiddleDistanceRace.adapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.GroupItemBean;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.Item;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * created by ww on 2019/6/12.
 */
public class MiddleRaceGroupAdapter extends RecyclerView.Adapter<MiddleRaceGroupAdapter.VH> {
    private OnItemClickListener onRecyclerViewItemClickListener;

    //创建ViewHolder
    public static class VH extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_item_race_no)
        TextView tvItemRaceNo;
        @BindView(R.id.tv_item_race_item)
        TextView tvItemRaceItem;
        @BindView(R.id.tv_item_race_number)
        TextView tvItemRaceNumber;
        @BindView(R.id.tv_item_race_state)
        TextView tvItemRaceState;
        @BindView(R.id.ll_race_group)
        LinearLayout ll;

        public VH(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    private List<GroupItemBean> mDatas;

    public MiddleRaceGroupAdapter(List<GroupItemBean> data) {
        this.mDatas = data;
    }

    public interface OnItemClickListener {
        void onMiddleRaceGroupLongClick(int position);
    }

    public void setOnRecyclerViewItemClickListener(OnItemClickListener onItemClickListener) {
        this.onRecyclerViewItemClickListener = onItemClickListener;
    }

    //在Adapter中实现3个方法
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final VH holder, final int position) {
        holder.tvItemRaceNo.setText(String.valueOf(position + 1));
        holder.tvItemRaceItem.setText(mDatas.get(position).getGroupItemName());
        holder.tvItemRaceNumber.setText(mDatas.get(position).getGroupItems().size() + "");
        String colorId = mDatas.get(position).getGroup().getColorId();
        switch (mDatas.get(position).getGroup().getIsTestComplete()) {
            case 0:
                holder.tvItemRaceState.setText("");
                holder.tvItemRaceNo.setBackgroundResource(R.color.white);
                break;
            case 3:
                holder.tvItemRaceState.setText("空闲");
                holder.tvItemRaceNo.setBackgroundResource(TextUtils.isEmpty(colorId) ? R.color.white : Integer.parseInt(colorId));
                break;
            case 4:
                holder.tvItemRaceState.setText("关联");
                holder.tvItemRaceNo.setBackgroundResource(TextUtils.isEmpty(colorId) ? R.color.white : Integer.parseInt(colorId));
                break;
            case 5:
                holder.tvItemRaceState.setText("已完成");
                holder.tvItemRaceNo.setBackgroundResource(TextUtils.isEmpty(colorId) ? R.color.white : Integer.parseInt(colorId));
                break;
            case 6:
                holder.tvItemRaceState.setText("等待");
                holder.tvItemRaceNo.setBackgroundResource(TextUtils.isEmpty(colorId) ? R.color.white : Integer.parseInt(colorId));
                break;
            case 7:
                holder.tvItemRaceState.setText("计时");
                holder.tvItemRaceNo.setBackgroundResource(TextUtils.isEmpty(colorId) ? R.color.white : Integer.parseInt(colorId));
                break;
            default:
                break;
        }

        if (onRecyclerViewItemClickListener != null) {
            holder.ll.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onRecyclerViewItemClickListener.onMiddleRaceGroupLongClick(position);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater.from指定写法
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_race_student_group, parent, false);
        return new VH(v);
    }
}
