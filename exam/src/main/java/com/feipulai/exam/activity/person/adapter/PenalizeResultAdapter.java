package com.feipulai.exam.activity.person.adapter;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.common.utils.NumberEnum;
import com.feipulai.exam.R;

import java.util.List;

/**
 * 个人测试成绩列表适配器
 * Created by zzs on 2018/11/20
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PenalizeResultAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    private int click = -1;
    public PenalizeResultAdapter(@Nullable List<String> data) {
        super(R.layout.item_results_penalize, data);

    }

    @Override
    protected void convert(BaseViewHolder holder, String result) {

//        holder.setText(R.id.item_txt_test_time, String.format("第%1$d次成绩：", holder.getLayoutPosition() + 1));
        if (!TextUtils.isEmpty(result)) {
            holder.setText(R.id.tv_result, result);
        } else {
            holder.setText(R.id.tv_result, "");
        }
        holder.setText(R.id.tv_index, NumberEnum.valueOfTo(holder.getLayoutPosition()+1).getValue());

        holder.setBackgroundColor(R.id.ll_content,holder.getLayoutPosition() == click? Color.YELLOW:Color.WHITE);
        holder.addOnClickListener(R.id.ll_content);

    }

    public void setClick(int click) {
        this.click = click;
        notifyDataSetChanged();
    }

    public int getClick(){
        return click;
    }
}
