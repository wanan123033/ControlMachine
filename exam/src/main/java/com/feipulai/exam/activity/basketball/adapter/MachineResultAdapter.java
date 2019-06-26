package com.feipulai.exam.activity.basketball.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zzs on  2019/6/18
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MachineResultAdapter extends BaseAdapter {
    private Context mContext;
    private List<MachineResult> dataList;

    public MachineResultAdapter(Context mContext, List<MachineResult> dataList) {
        this.mContext = mContext;
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
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
        holder.txtName.setText("第" + (position + 1) + "次拦截成绩：" +
                ResultDisplayUtils.getStrResultForDisplay(dataList.get(position).getResult()) + "      ");
        return convertView;
    }


    class ViewHolder {
        @BindView(R.id.item_txt_schedule)
        TextView txtName;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
