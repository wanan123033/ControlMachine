package com.feipulai.exam.activity.data.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.entity.Item;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zzs on  2018/12/26
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ItemSelectAdapter extends BaseAdapter {

    private Context mContext;
    private List<Item> itemList;

    public ItemSelectAdapter(Context mContext, List<Item> itemList) {
        this.mContext = mContext;
        this.itemList = itemList;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_sp_schedule, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.txtSchedule.setText(itemList.get(position).getItemName());
        return convertView;
    }


    class ViewHolder {
        @BindView(R.id.item_txt_schedule)
        TextView txtSchedule;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
