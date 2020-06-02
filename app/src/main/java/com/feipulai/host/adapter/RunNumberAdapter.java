package com.feipulai.host.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.bean.RunStudent;
import com.feipulai.host.entity.Student;

import java.util.List;


/**
 * Created by pengjf on 2018/12/4.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class RunNumberAdapter extends BaseQuickAdapter<RunStudent, BaseViewHolder> {

    public RunNumberAdapter(@Nullable List<RunStudent> data) {
        super(R.layout.item_runner_layout, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, RunStudent item) {
        Student student = item.getStudent();
        helper.setText(R.id.tv_num,helper.getLayoutPosition()+1+"");
        if (student != null){
            helper.setText(R.id.tv_stuCode,student.getStudentCode());
            helper.setText(R.id.tv_stuName,student.getStudentName());
            helper.setText(R.id.tv_stuSex,student.getSex()==0?"男":"女");
//            helper.setText(R.id.tv_stuSchool,student.getSchoolName());
//            helper.setText(R.id.tv_stuClass,student.getClassName());
            helper.setText(R.id.tv_stuMark,item.getMark());
            helper.setText(R.id.tv_stuItem, TestConfigs.sCurrentItem.getItemName());
        }else {
            helper.setText(R.id.tv_stuCode,"");
            helper.setText(R.id.tv_stuName,"");
            helper.setText(R.id.tv_stuSex,"");
//            helper.setText(R.id.tv_stuSchool,"");
//            helper.setText(R.id.tv_stuClass,"");
            helper.setText(R.id.tv_stuMark,"");
        }

        helper.setText(R.id.tv_stuDelete,"删除");
        helper.addOnClickListener(R.id.tv_stuDelete);
    }


}
