package com.feipulai.exam.activity.data.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.entity.StudentThermometer;

import java.util.List;

/**
 * Created by zzs on  2020/4/16
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ThermometerSearchAdapter extends BaseQuickAdapter<StudentThermometer, BaseViewHolder> {

    public ThermometerSearchAdapter(@Nullable List<StudentThermometer> data) {
        super(R.layout.item_thermometer_search, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, StudentThermometer item) {
        helper.setText(R.id.item_txt_number, (helper.getLayoutPosition() + 1) + "");
        helper.setText(R.id.item_txt_thermometer, item.getThermometer() + "℃");
        helper.setText(R.id.item_txt_time, DateUtil.formatTime2(Long.valueOf(item.getMeasureTime()), "yyyy-MM-dd HH:mm:ss"));
        helper.setText(R.id.item_txt_exam_state, StudentItem.setResultState(item.getExamType()));

    }
}
