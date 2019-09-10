package com.feipulai.exam.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;

import java.util.List;

/**
 * Created by pengjf on 2019/4/8.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class LedMoreAdapter extends BaseQuickAdapter<String,BaseViewHolder> {

    public LedMoreAdapter(@Nullable List<String> data) {
        super(R.layout.item_led, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.tv_item,item);

    }
}
