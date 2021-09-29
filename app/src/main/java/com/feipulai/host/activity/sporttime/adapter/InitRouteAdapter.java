package com.feipulai.host.activity.sporttime.adapter;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;
import com.feipulai.host.activity.sporttime.bean.InitRoute;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InitRouteAdapter extends BaseQuickAdapter<InitRoute, InitRouteAdapter.ViewHolder> {
    public InitRouteAdapter(@Nullable List<InitRoute> data) {
        super(R.layout.rv_stu_dev_item, data);
    }
    private int selectPosition = -1;
    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
    }
    public int getSelectPosition() {
        return selectPosition;
    }

    @Override
    protected void convert(ViewHolder helper, InitRoute item) {
        helper.setText(R.id.tv_device_id,item.getIndex()+"");
        helper.setText(R.id.tv_stu_info,(item.getDeviceName()));
        if (selectPosition == helper.getLayoutPosition()) {
            helper.viewContent.setBackgroundColor(ContextCompat.getColor(mContext, R.color.blue_CB));
        } else {
            helper.viewContent.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_F7));
        }
    }

    public static class ViewHolder extends BaseViewHolder {
        @BindView(R.id.ll_pair)
        public LinearLayout viewContent;
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
