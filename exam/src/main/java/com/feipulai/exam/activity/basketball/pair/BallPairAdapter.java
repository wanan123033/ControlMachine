package com.feipulai.exam.activity.basketball.pair;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.adapter.DevicePairAdapter;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.config.TestConfigs;

import java.util.List;

/**
 * Created by zzs on  2019/11/1
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BallPairAdapter extends DevicePairAdapter {

    public BallPairAdapter(Context context, List<StuDevicePair> stuPairs) {
        super(context, stuPairs);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ) {
            holder.mTvDeviceId.setText(position == 0 ? "近红外" : "计时屏");
        } else {
            holder.mTvDeviceId.setText(position == 0 ? "近红外" : position == 1 ? "远红外" : "计时屏");
        }
    }

    public DevicePairAdapter.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt) {
        return new DevicePairAdapter.ViewHolder(LayoutInflater.from(paramViewGroup.getContext()).inflate(R.layout.rv_ball_dev_item, paramViewGroup, false));
    }
}