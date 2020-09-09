package com.feipulai.exam.activity.data.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.bean.SoftApp;

import java.util.List;

/**
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class AppSoftAdapter extends BaseQuickAdapter<SoftApp, BaseViewHolder> {

    public AppSoftAdapter(@Nullable List<SoftApp> data) {
        super(R.layout.item_soft_app, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, SoftApp item) {
        helper.setText(R.id.item_txt_softwareName, item.getSoftwareName());
        helper.setText(R.id.item_txt_version, item.getVersion());
        helper.setText(R.id.item_txt_remark, item.getRemark());

    }
}
