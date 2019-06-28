package com.feipulai.exam.adapter;

import android.support.annotation.Nullable;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.List;

/**
 * Created by pengjf on 2018/11/20.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class BaseGroupAdapter extends BaseQuickAdapter<BaseStuPair, BaseViewHolder> {

    private OnPopItemClickListener itemClickListener;

    public void setItemClickListener(OnPopItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public BaseGroupAdapter(@Nullable List<BaseStuPair> data) {
        super(R.layout.item_base_group_stu, data);
    }


    @Override
    protected void convert(final BaseViewHolder helper, final BaseStuPair item) {
        if (item.getStudent() != null) {
            helper.setText(R.id.tv_num, item.getTrackNo() + "");
            helper.setText(R.id.tv_stuCode, item.getStudent().getStudentCode());
            helper.setText(R.id.tv_stuName, item.getStudent().getStudentName());
            helper.setText(R.id.tv_stuMark, item.getResultState() ==  RoundResult.RESULT_STATE_FOUL?"X" : item.isNotBest()?" 未测":ResultDisplayUtils.getStrResultForDisplay(item.getResult()));
           helper.setChecked(R.id.rb_can_test,item.isCanTest());

            CheckBox checkBox = helper.getView(R.id.rb_can_test);
            checkBox.setEnabled(item.isCanCheck());//是否可以选中

            helper.setOnCheckedChangeListener(R.id.rb_can_test, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (itemClickListener != null) {
                        itemClickListener.itemClick(helper.getAdapterPosition(), isChecked);
                    }

                }
            });
        }
    }

    public interface OnPopItemClickListener {
        void itemClick(int pos, boolean isChecked);
    }
}
