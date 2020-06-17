package com.feipulai.exam.activity.pullup.pair;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.adapter.DevicePairAdapter;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;

import java.util.List;

/**
 * Created by zzs on  2019/11/1
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PullAndSitUpPairAdapter extends DevicePairAdapter {

    public PullAndSitUpPairAdapter(Context context, List<StuDevicePair> stuPairs) {
        super(context, stuPairs);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
            switch (position) {
                case 0:
                    holder.mTvDeviceId.setText("红外探头");
                    break;
                case 1:
                    holder.mTvDeviceId.setText("手臂感应器" );
                    break;
            }

    }

    public ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt) {
        return new ViewHolder(LayoutInflater.from(paramViewGroup.getContext()).inflate(R.layout.rv_ball_dev_item, paramViewGroup, false));
    }
}
