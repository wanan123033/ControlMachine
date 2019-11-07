package com.feipulai.host.adapter;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.RunStudent;
import com.feipulai.host.entity.Student;

import java.util.List;

/**
 * Created by pengjf on 2018/12/4.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class RunNumberAdapter2 extends BaseQuickAdapter<RunStudent,BaseViewHolder>{

    public RunNumberAdapter2(@Nullable List<RunStudent> data) {
        super(R.layout.item_runner_layout2, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, RunStudent item) {
        Student student = item.getStudent();
        helper.setText(R.id.tv_num,helper.getLayoutPosition()+1+"");
        if (student != null){
            helper.setText(R.id.tv_stuCode,student.getStudentCode());
            helper.setText(R.id.tv_stuName,student.getStudentName());
            helper.setText(R.id.tv_stuSex,student.getSex()==0?"男":"女");
            helper.setText(R.id.tv_stuItem, TestConfigs.sCurrentItem.getItemName());
//            helper.setText(R.id.tv_stuClass,student.getClassName());
            helper.setText(R.id.tv_stuMark,item.getMark());
        }else {
            helper.setText(R.id.tv_stuCode,"");
            helper.setText(R.id.tv_stuName,"");
            helper.setText(R.id.tv_stuSex,"");
//            helper.setText(R.id.tv_stuSchool,"");
//            helper.setText(R.id.tv_stuClass,"");

        }
        helper.addOnClickListener(R.id.tv_stuMark);
        helper.setText(R.id.tv_stuMark,TextUtils.isEmpty(item.getMark())? "": item.getMark());
        helper.setText(R.id.tv_device_state,item.getConnectState() == 1 ?
                "正常":"异常");
        helper.setBackgroundRes(R.id.tv_state_color,item.getConnectState() == 1 ? R.mipmap.icon_green:R.mipmap.icon_red);
//        helper.setText(R.id.tv_stuDelete,"删除");
//        helper.addOnClickListener(R.id.tv_stuDelete);
    }


}
