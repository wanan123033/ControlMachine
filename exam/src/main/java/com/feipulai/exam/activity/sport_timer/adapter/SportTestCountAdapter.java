package com.feipulai.exam.activity.sport_timer.adapter;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SportTestCountAdapter extends BaseQuickAdapter <String , SportTestCountAdapter.ViewHolder>{
    public SportTestCountAdapter(@Nullable List data) {
        super(R.layout.item_pop, data);
    }

    private int selectPosition = -1;

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
    }
    public int getSelectPosition() {
        return selectPosition;
    }
    @Override
    protected void convert(ViewHolder helper, String item) {
        helper.setText(R.id.tv_result,item);
        if (selectPosition == helper.getLayoutPosition()) {
            helper.viewContent.setBackgroundColor(ContextCompat.getColor(mContext, R.color.blue_CB));
        } else {
            helper.viewContent.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_F7));
        }
    }

    public static class ViewHolder extends BaseViewHolder {
        @BindView(R.id.view_content)
        public RelativeLayout viewContent;
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
