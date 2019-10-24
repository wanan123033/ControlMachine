package com.feipulai.exam.activity.sargent_jump.adapter;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.entity.Student;

import java.util.List;

/**
 * 分组学生适配器
 * Created by pjf on 2019/08/19
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StuAdapter extends BaseQuickAdapter<Student, BaseViewHolder> {
    /**
     * 测试位
     */
    private int testPosition = -1;

    public void setTestPosition(int testPosition) {
        this.testPosition = testPosition;
    }

    public int getTestPosition() {
        return testPosition;
    }

    public StuAdapter(@Nullable List<Student> data) {
        super(R.layout.item_group_test_stu, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Student student) {
        helper.setText(R.id.item_txt_stu_code, student.getStudentCode());
        helper.setText(R.id.item_txt_stu_name, student.getStudentName());
//        CheckBox cbDeviceState = helper.getView(R.id.item_cb_device_state);
        helper.setText(R.id.item_trackno, helper.getAdapterPosition() +1+ "");
//        if (testPosition == helper.getLayoutPosition()) {
//            helper.setBackgroundRes(R.id.view_content, R.drawable.group_select_bg);
//        } else {
//            helper.setBackgroundColor(R.id.view_content, ContextCompat.getColor(mContext, R.color.white));
//        }


    }
}
