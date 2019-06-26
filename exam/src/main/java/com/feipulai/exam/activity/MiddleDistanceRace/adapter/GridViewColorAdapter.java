package com.feipulai.exam.activity.MiddleDistanceRace.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.ColorSelectBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * created by ww on 2019/6/25.
 */
public class GridViewColorAdapter extends BaseAdapter {
    private List<ColorSelectBean> datas;
    private Context mContext;

    public GridViewColorAdapter(Context context, List<ColorSelectBean> datas) {
        mContext = context;
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.gridview_color_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvColor.setBackgroundResource(datas.get(position).getColorId());
//        if (datas.get(position).isSelect()) {
//            holder.ivSelect.setVisibility(View.VISIBLE);
//        } else {
//            holder.ivSelect.setVisibility(View.GONE);
//        }

        return convertView;
    }

    class ViewHolder {
        @BindView(R.id.tv_color)
        TextView tvColor;
//        @BindView(R.id.iv_select)
//        ImageView ivSelect;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
