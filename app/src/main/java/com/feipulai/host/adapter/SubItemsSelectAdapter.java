package com.feipulai.host.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;
import com.feipulai.host.entity.Item;

import java.util.List;

/**
 * Created by zzs on  2019/5/16
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SubItemsSelectAdapter extends BaseQuickAdapter<Item, BaseViewHolder> {

    public SubItemsSelectAdapter(@Nullable List<Item> data) {
        super(R.layout.item_subitems, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Item item) {
        helper.setText(R.id.item_txt_subitem_name, item.getItemName());
    }
}
