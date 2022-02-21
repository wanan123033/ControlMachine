package com.feipulai.exam.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.Schedule;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zzs on  2018/12/26
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ItemAdapter extends BaseAdapter {

    private Context mContext;
    private List<Item> itemList;

    public ItemAdapter(Context mContext, List<Item> itemList) {
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
        Item schedule = itemList.get(position);
        if (schedule != null && TextUtils.equals(schedule.getItemCode(), "-99")) {//数据查询使用
            holder.txtSchedule.setText("全部项目");
        } else if (schedule != null){
            holder.txtSchedule.setText(schedule.getItemName());
        }


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
