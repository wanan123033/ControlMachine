package com.feipulai.exam.activity.data.adapter;

import android.support.annotation.Nullable;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;

import java.util.List;

/**
 * Created by zzs on  2021/10/29
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ResultDateAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    private int selectPosition = 0;

    public int getSelectPosition() {
        return selectPosition;
    }

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
    }

    public ResultDateAdapter(@Nullable List<String> data) {
        super(R.layout.item_result_date, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        TextView txtDate = helper.getView(R.id.item_txt_date);
        txtDate.setText(item);
        if (helper.getLayoutPosition() == selectPosition) {
            txtDate.setSelected(true);
        } else {
            txtDate.setSelected(false);
        }
    }
}
