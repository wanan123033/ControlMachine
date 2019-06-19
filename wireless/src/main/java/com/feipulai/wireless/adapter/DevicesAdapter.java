package com.feipulai.wireless.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.wireless.R;
import com.feipulai.wireless.beans.BasePair;

import java.util.List;

/**
 * Created by pengjf on 2019/2/13.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class DevicesAdapter extends BaseQuickAdapter<BasePair, BaseViewHolder> {
    /**1 配对 2控制*/
    private int type ;
    public DevicesAdapter( @Nullable List<BasePair> data,int type) {
        super(R.layout.item_device, data);
        this.type = type ;
    }

    @Override
    protected void convert(BaseViewHolder helper, BasePair item) {
        helper.setText(R.id.tv_device_id,"子机号:"+item.getDeviceId());
        helper.setText(R.id.tv_device_power,"电量:"+item.getPower());
        helper.setText(R.id.tv_device_vital,"肺活量:"+item.getCount());
        if (type == 1){
            helper.setVisible(R.id.btn_start_test,false);
            helper.setVisible(R.id.btn_set_free,false);
        }else {
            helper.setVisible(R.id.btn_start_test,true);
            helper.setVisible(R.id.btn_set_free,true);
            helper.addOnClickListener(R.id.btn_start_test);
        }
        helper.addOnClickListener(R.id.btn_set_free);
        switch (item.getState()){
            case 1:
                helper.setText(R.id.tv_device_state,"状态:离线");
                break;
            case 2:
                helper.setText(R.id.tv_device_state,"状态:空闲");
                break;
            case 3:
                helper.setText(R.id.tv_device_state,"状态:测试");
                break;
            case 4:
                helper.setText(R.id.tv_device_state,"状态:结束");
                break;
            case 5:
                helper.setText(R.id.tv_device_state,"状态:其它");
                break;

        }
    }
}
