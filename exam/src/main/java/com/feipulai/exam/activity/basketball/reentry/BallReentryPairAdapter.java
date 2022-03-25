package com.feipulai.exam.activity.basketball.reentry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.adapter.DevicePairAdapter;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.config.TestConfigs;

import java.util.List;

/**
 * Created by zzs on  2019/11/1
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BallReentryPairAdapter extends DevicePairAdapter {

    public BallReentryPairAdapter(Context context, List<StuDevicePair> stuPairs) {
        super(context, stuPairs);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        BaseDeviceState deviceState= stuPairs.get(position).getBaseDevice();

        String version = TextUtils.isEmpty(deviceState.getDeviceVersion()) ? ""
                : deviceState.getDeviceVersion();
        holder.mTvDeviceId.setText(deviceState.getDeviceName()+version);
//        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ) {
//            switch (position) {
//                case 0:
//                    holder.mTvDeviceId.setText("近红外" +version);
//                    break;
//                case 1:
//                    holder.mTvDeviceId.setText("计时屏" +version);
//                    break;
//            }
//        } else {
//            switch (position) {
//                case 0:
//                    holder.mTvDeviceId.setText("近红外" +version);
//                    break;
//                case 1:
//                    holder.mTvDeviceId.setText("远红外" +version);
//                    break;
//                case 2:
//                    holder.mTvDeviceId.setText("计时屏" +version);
//                    break;
//            }
//
//        }


    }

    public ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt) {
        return new ViewHolder(LayoutInflater.from(paramViewGroup.getContext()).inflate(R.layout.rv_ball_dev_item, paramViewGroup, false));
    }
}
