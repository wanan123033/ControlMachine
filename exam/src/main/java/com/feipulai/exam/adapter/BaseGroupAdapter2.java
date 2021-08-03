package com.feipulai.exam.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BaseGroupAdapter2 extends RecyclerView.Adapter<BaseGroupAdapter2.BaseGroupViewHolder> {
    private Context context;
    private List<BaseStuPair> pairs;

    public BaseGroupAdapter2(Context context, List<BaseStuPair> pairs){
        this.context = context;
        this.pairs = pairs;
    }
    @Override
    public BaseGroupViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_base_group_stu, viewGroup, false);
        BaseGroupViewHolder holder = new BaseGroupViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(BaseGroupViewHolder holder, int i) {
        BaseStuPair item = pairs.get(i);
        holder.tv_num.setText(item.getTrackNo() + "");
        holder.tv_stuCode.setText(item.getStudent().getStudentCode());
        holder.tv_stuName.setText(item.getStudent().getStudentName());
        holder.tv_stuMark.setText(item.getResultState() ==  RoundResult.RESULT_STATE_FOUL?"X" : item.isNotBest()?" 未测": ResultDisplayUtils.getStrResultForDisplay(item.getResult()));
        holder.rb_can_test.setChecked(item.isCanTest());
        holder.rb_can_test.setEnabled(item.isCanCheck());//是否可以选中
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class BaseGroupViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.tv_num)
        TextView tv_num;
        @BindView(R.id.tv_stuCode)
        TextView tv_stuCode;
        @BindView(R.id.tv_stuName)
        TextView tv_stuName;
        @BindView(R.id.tv_stuMark)
        TextView tv_stuMark;
        @BindView(R.id.rb_can_test)
        CheckBox rb_can_test;

        public BaseGroupViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
