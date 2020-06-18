package com.feipulai.host.activity.explain;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;

import java.io.File;
import java.util.List;

/**
 * Created by zzs on  2019/11/21
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ExplainAdapter extends BaseQuickAdapter<File, BaseViewHolder> {


    public ExplainAdapter(@Nullable List<File> data) {
        super(R.layout.item_explain, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, File item) {
        helper.setText(R.id.item_txt_file_name, item.getName());
    }
}
