package com.feipulai.host.activity.vision.bluetooth;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.search.SearchResult;

import java.util.List;

/**
 * Created by zzs on  2020/4/13
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BlueToothListAdapter extends BaseQuickAdapter<SearchResult, BaseViewHolder> {

    public BlueToothListAdapter(@Nullable List<SearchResult> data) {
        super(R.layout.item_bluetooth_list, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, SearchResult item) {
        helper.setText(R.id.item_txt_name, item.getName());
        helper.setText(R.id.item_txt_mac, item.getAddress());
        helper.setText(R.id.item_txt_rssi, String.format("Rssi: %d", item.rssi));

        Beacon beacon = new Beacon(item.scanRecord);
        helper.setText(R.id.item_txt_adv, beacon.toString());

//        Intent intent = new Intent();
//        intent.setClass(mContext, DeviceDetailActivity.class);
//        intent.putExtra("mac", result.getAddress());
//        mContext.startActivity(intent);

    }
}
