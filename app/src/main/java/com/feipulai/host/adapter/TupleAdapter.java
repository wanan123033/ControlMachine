package com.feipulai.host.adapter;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.bean.Tuple;

import java.util.List;

/**
 * Created by pengjf on 2018/11/20.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class TupleAdapter extends BaseQuickAdapter<Tuple, BaseViewHolder> {
    boolean isFreedomTest;

    public TupleAdapter(@Nullable List data, boolean isFreedomTest) {
        super(R.layout.item_tuple, data);
        this.isFreedomTest = isFreedomTest;
    }


    @Override
    protected void convert(BaseViewHolder helper, Tuple item) {
        helper.setText(R.id.tv_item, item.getMachineName());
        helper.setImageResource(R.id.img_item_price, item.getImgRes());
        CardView cardView = helper.getView(R.id.item_view_card);
        if (TestConfigs.sCurrentItem != null && TestConfigs.sCurrentItem.getMachineCode() == item.getMachineCode() && isFreedomTest == SettingHelper.getSystemSetting().isFreedomTest()) {
            cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.blue_8A));
        } else {
            cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        }
    }
}
