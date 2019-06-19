package com.feipulai.exam.adapter;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.bean.Tuple;
import com.feipulai.exam.config.TestConfigs;

import java.util.List;

/**
 * Created by pengjf on 2018/11/20.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class TupleAdapter extends BaseQuickAdapter<Tuple, BaseViewHolder> {

    public TupleAdapter(@Nullable List data) {
        super(R.layout.item_tuple, data);
    }


    @Override
    protected void convert(BaseViewHolder helper, Tuple item) {
        helper.setText(R.id.tv_item, item.getMachineName());
        helper.setImageResource(R.id.img_item_price, item.getImgRes());
        CardView cardView = helper.getView(R.id.item_view_card);
        if (TestConfigs.sCurrentItem != null && TestConfigs.sCurrentItem.getMachineCode() == item.getMachineCode()) {
            cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.blue_8A));
        } else {
            cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        }
    }
    
}
