package com.feipulai.host.activity.setting;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;
import com.feipulai.host.bean.CorrespondBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CorrespondAdapter extends BaseQuickAdapter<CorrespondBean, CorrespondAdapter.CorrespondViewHolder> {
    public CorrespondAdapter(Context context, @Nullable List<CorrespondBean> data) {
        super(R.layout.rv_dev_item,data);
    }

    @Override
    protected void convert(CorrespondViewHolder helper, CorrespondBean item) {
        helper.setText(R.id.tv_device_id,item.deviceId+"");
        helper.setText(R.id.tv_stu_info,"发送:"+item.sendNum);
        helper.setText(R.id.tv_stu,"接收:"+item.receiverNum);
        helper.setText(R.id.tv_sit_up,"丢包率:"+(item.quality == null ? "0%":item.quality));
    }

    static class CorrespondViewHolder extends BaseViewHolder{
        @BindView(R.id.tv_device_id)
        TextView mTvDeviceId;
        @BindView(R.id.tv_stu_info)
        TextView mTvStuInfo;
        @BindView(R.id.ll_pair)
        LinearLayout mLlPair;
        public CorrespondViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }

}
