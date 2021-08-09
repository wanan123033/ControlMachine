package com.feipulai.exam.activity.RadioTimer.newRadioTimer;

import android.support.annotation.Nullable;
import android.util.SparseArray;
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
    private int intercept;// 0只有起点或终点 1有起终点
    private SparseArray<Integer> array;
    public NewRunAdapter(@Nullable List<RunStudent> data, int type) {
        super(R.layout.item_runner_layout_radio, data);
        this.type = type;
    }

    public void setIntercept(int intercept,SparseArray<Integer> array){
        this.intercept = intercept;
        this.array = array;
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
        if (intercept == 0){
            helper.getView(R.id.tv_device_state).setVisibility(View.INVISIBLE);
        }else {
            switch (array.get(helper.getAdapterPosition())){
                case 0:
                    helper.setBackgroundRes(R.id.tv_device_state,  R.drawable.red_circle);
                    break;
                case 1:
                    helper.setBackgroundRes(R.id.tv_device_state,  R.drawable.green_circle);
                    break;
                case 2:
                    helper.setBackgroundRes(R.id.tv_device_state,  R.drawable.yellow_circle);
                    break;
            }
        }
        switch (item.getConnectState()){
            case 0:
                helper.setBackgroundRes(R.id.tv_state_color,  R.drawable.red_circle);//异常链接
                break;
            case 1:
                helper.setBackgroundRes(R.id.tv_state_color,  R.drawable.green_circle);//正常连接
                break;
            case 2:
                helper.setBackgroundRes(R.id.tv_state_color,  R.drawable.yellow_circle);//计时状态
                break;
        }
    }
}
