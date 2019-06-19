package com.feipulai.host.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.utils.ResultDisplayUtils;

import java.util.List;

/**
 * Created by pengjf on 2018/8/6.
 * 用于立定跳远和肺活量实心球的适配器
 */

public class Student4JumpAndVitalAdapter extends BaseQuickAdapter<BaseStuPair, BaseViewHolder> {


    public Student4JumpAndVitalAdapter(@Nullable List<BaseStuPair> data) {
        super(R.layout.item_student, data);
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, BaseStuPair pair) {
        Student item = pair.getStudent();
        BaseDeviceState device = pair.getBaseDevice();
        if (item != null) {
            viewHolder.setText(R.id.tv_stu_num, "学号：" + item.getStudentCode())
                    .setText(R.id.tv_stu_sex, item.getSex() == 0 ? "性别：" +
                            "女" : "性别：男")
                    .setText(R.id.tv_stu_name, "姓名：" + item.getStudentName())
                    .setText(R.id.tv_stu_mark, "成绩：" + ((pair.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "犯规" : ResultDisplayUtils.getStrResultForDisplay(pair.getResult())));
        } else {
            viewHolder.setText(R.id.tv_stu_num, "学号：")
                    .setText(R.id.tv_stu_sex, "性别：")
                    .setText(R.id.tv_stu_name, "姓名：")
                    .setText(R.id.tv_stu_mark, "成绩：");
        }
        if (device != null) {
            if (device.getState() != BaseDeviceState.STATE_ERROR) {
                viewHolder.setImageResource(R.id.iv_device_state, R.drawable.ic_radio_checked);
            } else {
                viewHolder.setImageResource(R.id.iv_device_state, R.drawable.ic_pan_tool);
                viewHolder.addOnClickListener(R.id.iv_device_state);
            }
        }

//        Glide.with(mContext).load(item.getUserAvatar()).crossFade().into((ImageView) viewHolder.getView(R.id.iv));
    }

}

