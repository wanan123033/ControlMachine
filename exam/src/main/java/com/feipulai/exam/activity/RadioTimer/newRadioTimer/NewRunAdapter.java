package com.feipulai.exam.activity.RadioTimer.newRadioTimer;

import android.support.annotation.Nullable;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RunStudent;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.List;

public class NewRunAdapter extends BaseQuickAdapter<RunStudent, BaseViewHolder> {

    private int type;//统一, 1独立

    public NewRunAdapter(@Nullable List<RunStudent> data, int type) {
        super(R.layout.item_runner_layout2, data);
        this.type = type;
    }

    @Override
    protected void convert(BaseViewHolder helper, RunStudent item) {
        Student student = item.getStudent();
        helper.setText(R.id.tv_num, helper.getLayoutPosition() + 1 + "");
        if (type == 1) {
            helper.getView(R.id.tv_stuMark).setVisibility(View.GONE);
            helper.setVisible(R.id.ll_test_time, true);
        }
        if (student != null) {
            helper.setText(R.id.tv_stuCode, student.getStudentCode());
            helper.setText(R.id.tv_stuName, student.getStudentName());
            helper.setText(R.id.tv_stuSex, student.getSex() == 0 ? "男" : "女");
            helper.setText(R.id.tv_stuItem, TestConfigs.sCurrentItem.getItemName());
            //            helper.setText(R.id.tv_stuClass,student.getClassName());
            helper.setText(R.id.tv_stuMark, item.getMark());
            helper.addOnClickListener(R.id.tv_stuMark);
            helper.addOnClickListener(R.id.tv_time_detail);
            helper.setText(R.id.tv_stuTime, ResultDisplayUtils.getStrResultForDisplay(item.getIndependentTime(), false));
        } else {
            helper.setText(R.id.tv_stuCode, "");
            helper.setText(R.id.tv_stuName, "");
            helper.setText(R.id.tv_stuSex, "");
            //            helper.setText(R.id.tv_stuSchool,"");
            //            helper.setText(R.id.tv_stuClass,"");
            helper.setText(R.id.tv_stuMark, "");
        }
        helper.setText(R.id.tv_device_state, item.getConnectState() == 1 ?
                "正常" : "异常");
        helper.setBackgroundRes(R.id.tv_state_color, item.getConnectState() == 1 ? R.mipmap.icon_green : R.mipmap.icon_red);
        //        helper.setText(R.id.tv_stuDelete,"删除");
        //        helper.addOnClickListener(R.id.tv_stuDelete);
    }
}
