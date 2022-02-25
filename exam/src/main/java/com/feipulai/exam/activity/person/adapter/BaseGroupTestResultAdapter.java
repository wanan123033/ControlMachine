package com.feipulai.exam.activity.person.adapter;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.NumberEnum;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.SelectResult;

import java.util.List;

/**
 * 分组学生成绩适配器
 * Created by zzs on 2018/11/21
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BaseGroupTestResultAdapter extends BaseQuickAdapter<SelectResult, BaseViewHolder> {


    public BaseGroupTestResultAdapter(@Nullable List<SelectResult> data) {
        super(R.layout.item_group_test_result, data);
    }


    @Override
    protected void convert(BaseViewHolder holder, SelectResult result) {

        if (!TextUtils.isEmpty(result.getResult())) {
            holder.setText(R.id.item_txt_test_result, result.getResult());
        } else {
            holder.setText(R.id.item_txt_test_result, "");
        }
        RelativeLayout viewContent = holder.getView(R.id.view_content);
        holder.setText(R.id.item_txt_test_round, NumberEnum.valueOfTo(holder.getLayoutPosition() + 1).getValue());

        if (result.isIndex()) {
            viewContent.setBackgroundResource(R.drawable.bg_test_result_yellow);
        } else {
            viewContent.setBackgroundResource(R.drawable.bg_group_result_selector);
            viewContent.setEnabled(true);
        }
        if (result.isIndex() && result.isSelect()) {
//            viewContent.setEnabled(false);
//            viewContent.setSelected(false);
            viewContent.setBackgroundResource(R.drawable.bg_test_result_yellow);
        } else if (result.isSelect()) {
            viewContent.setSelected(true);
            viewContent.setEnabled(true);
        } else {
            viewContent.setSelected(false);
        }
//        holder.getView(R.id.view_content).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                LogUtil.logDebugMessage("view_content3");
//            }
//        });
    }
}
