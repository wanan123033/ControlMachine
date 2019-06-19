package com.feipulai.host.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;
import com.feipulai.host.entity.Tuple;

import java.util.List;

/**
 * Created by pengjf on 2018/11/20.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class TupleAdapter extends BaseQuickAdapter<Tuple,BaseViewHolder> {

    public TupleAdapter(@Nullable List data) {
        super(R.layout.item_tuple,data);
    }


    @Override
    protected void convert(BaseViewHolder helper, Tuple item) {
        helper.setText(R.id.tv_item,item.getMachineName());
    }
}
