package com.feipulai.exam.adapter;

import android.support.annotation.Nullable;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.entity.Student;

import java.util.List;

import butterknife.OnClick;

public class AdvancedStuAdapter extends BaseQuickAdapter <BaseStuPair , BaseViewHolder> {
    public AdvancedStuAdapter( @Nullable List<BaseStuPair> data) {
        super(R.layout.item_abandon_stu_list, data);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, BaseStuPair baseStuPair) {
        Student student = baseStuPair.getStudent();
        baseViewHolder.setText(R.id.tv_studentTrank,baseStuPair.getTrackNo()+"");
        baseViewHolder.setText(R.id.tv_studentCode,student.getStudentCode());
        baseViewHolder.setText(R.id.tv_studentName,student.getStudentName());
    }


}
