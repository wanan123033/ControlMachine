package com.feipulai.exam.activity.MiddleDistanceRace.adapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.exam.R;
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
    private List<GroupItem> groupItems;
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

    private List<Group> mDatas;

    public MiddleRaceGroupAdapter(List<Group> data) {
        this.mDatas = data;
    }

    public interface OnItemClickListener {
        void onLongClick(int position);
    }

    public void setOnRecyclerViewItemClickListener(OnItemClickListener onItemClickListener) {
        this.onRecyclerViewItemClickListener = onItemClickListener;
    }

    //在Adapter中实现3个方法
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final VH holder, final int position) {
        Item item = DBManager.getInstance().queryItemByCode(mDatas.get(position).getItemCode());
        groupItems = DBManager.getInstance().queryGroupItem(item.getItemCode(), mDatas.get(position).getGroupNo(), mDatas.get(position).getGroupType());

        holder.tvItemRaceNo.setText(String.valueOf(position + 1));
        String sex = "";
        switch (mDatas.get(position).getGroupType()) {
            case 0:
                sex = "男子";
                break;
            case 1:
                sex = "女子";
                break;
            case 2:
                sex = "混合";
                break;
            default:
                break;
        }
        holder.tvItemRaceItem.setText(sex + item.getItemName() + "第" + mDatas.get(position).getGroupNo() + "组");
        holder.tvItemRaceNumber.setText(String.valueOf(groupItems.size()));
        holder.tvItemRaceState.setText(mDatas.get(position).getIsTestComplete() == 0 ? "空闲" : "已测完");

        if (onRecyclerViewItemClickListener != null) {
            holder.ll.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onRecyclerViewItemClickListener.onLongClick(position);
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
