package com.feipulai.exam.activity.basketball.adapter;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.entity.RoundResult;

import java.util.List;

/**
 * 个人测试成绩列表适配器
 * Created by zzs on 2018/11/20
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ShootResultAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    private int selectPosition = -1;
    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
    }

    public int getSelectPosition() {
        return selectPosition;
    }
    public ShootResultAdapter(@Nullable List<String> data) {
        super(R.layout.item_shoot_test_result, data);

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

    private String setResultState(int state) {

        switch (state) {
            case RoundResult.RESULT_STATE_NORMAL:
                return "正常";
            case RoundResult.RESULT_STATE_FOUL:
                return "犯规";
            case RoundResult.RESULT_STATE_BACK:
                return "中退";
            case RoundResult.RESULT_STATE_WAIVE:
                return "放弃";
            default:
                return "";
        }
    }
}
