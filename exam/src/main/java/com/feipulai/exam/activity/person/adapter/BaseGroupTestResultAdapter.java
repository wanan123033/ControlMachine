package com.feipulai.exam.activity.person.adapter;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;

import java.util.List;

/**
 * 分组学生成绩适配器
 * Created by zzs on 2018/11/21
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BaseGroupTestResultAdapter extends BaseQuickAdapter<String, BaseViewHolder> {


    public BaseGroupTestResultAdapter(@Nullable List<String> data) {
        super(R.layout.item_group_test_result , data);
    }

    @Override
    protected void convert(BaseViewHolder holder, String result) {
        holder.setText(R.id.item_txt_test_time, String.format("成绩%1$d：", holder.getLayoutPosition() + 1));

        if (!TextUtils.isEmpty(result)) {
            holder.setText(R.id.item_txt_test_result, result);
        } else {
            holder.setText(R.id.item_txt_test_result, "");
        }

    }
}
