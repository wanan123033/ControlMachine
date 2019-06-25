package com.feipulai.exam.activity.data.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.List;

/**
 * Created by zzs on  2019/6/25
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class DataMacResultAdapter extends BaseQuickAdapter<MachineResult, BaseViewHolder> {

    public DataMacResultAdapter(@Nullable List<MachineResult> data) {
        super(R.layout.item_sp_schedule, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, MachineResult item) {
        helper.setText(R.id.item_txt_schedule, "第" + item.getRoundNo() + "轮 第" +
                (helper.getLayoutPosition() + 1) + "次拦截成绩：" +
                ResultDisplayUtils.getStrResultForDisplay(item.getResult()) + "      ");
    }
}
