package com.feipulai.exam.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.bean.TypeListBean;
import com.yhy.gvp.adapter.GVPAdapter;

import java.util.List;

/**
 * Created by pengjf on 2019/3/18.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class IndexTypeAdapter extends GVPAdapter<TypeListBean> {
    private Context context;

    public IndexTypeAdapter(Context context, @Nullable List<TypeListBean> data) {
        super(R.layout.grid_view_layout, data);
        this.context = context;
    }

    @Override
    public void bind(View item, int position, TypeListBean data) {
        ImageView imageView = item.findViewById(R.id.iv_type_image);
        TextView tv_type_name = item.findViewById(R.id.tv_type_name);
        tv_type_name.setText(data.getName());
        imageView.setImageResource(data.getImageRes());
    }


}
