package com.feipulai.exam.activity.person.adapter;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;

import java.util.List;

/**
 * 个人测试成绩列表适配器
 * Created by zzs on 2018/11/20
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BasePersonTestResultAdapter extends BaseQuickAdapter<String, BaseViewHolder> {


    public BasePersonTestResultAdapter(@Nullable List<String> data) {
        super(R.layout.item_person_test_result, data);

    }

    @Override
    protected void convert(BaseViewHolder holder, String result) {

//        holder.setText(R.id.item_txt_test_time, String.format("第%1$d次成绩：", holder.getLayoutPosition() + 1));
        if (!TextUtils.isEmpty(result)) {
            holder.setText(R.id.item_txt_test_result, result);
        } else {
            holder.setText(R.id.item_txt_test_result, "");
        }
        switch (holder.getLayoutPosition()) {
            case 0:
                holder.setText(R.id.item_txt_test_time, "第一次成绩");
                holder.setBackgroundColor(R.id.view_content, ContextCompat.getColor(mContext, R.color.test_first_color));
                break;
            case 1:
                holder.setText(R.id.item_txt_test_time, "第二次成绩");
                holder.setBackgroundColor(R.id.view_content, ContextCompat.getColor(mContext, R.color.test_second_color));
                break;
            case 2:
                holder.setText(R.id.item_txt_test_time, "第三次成绩");
                holder.setBackgroundColor(R.id.view_content, ContextCompat.getColor(mContext, R.color.test_thirdly_color));
                break;
            default:
                holder.setText(R.id.item_txt_test_time, String.format("第%1$d次成绩", holder.getLayoutPosition() + 1));
                holder.setBackgroundColor(R.id.view_content, ContextCompat.getColor(mContext, R.color.test_first_color));
                break;
        }

    }
}
