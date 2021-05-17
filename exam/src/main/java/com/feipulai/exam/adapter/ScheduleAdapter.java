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
import com.feipulai.exam.entity.Schedule;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zzs on  2018/12/26
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ScheduleAdapter extends BaseAdapter {

    private Context mContext;
    private List<Schedule> scheduleList;

    public ScheduleAdapter(Context mContext, List<Schedule> scheduleList) {
        this.mContext = mContext;
        this.scheduleList = scheduleList;
    }

    @Override
    public int getCount() {
        return scheduleList.size();
    }

    @Override
    public Object getItem(int position) {
        return scheduleList.get(position);
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
        Schedule schedule = scheduleList.get(position);
        if (TextUtils.equals(schedule.getScheduleNo(), "-1")) {
            holder.txtSchedule.setText("未分配(请选择正确日程)");
        } else if (TextUtils.equals(schedule.getScheduleNo(), "-2")) {//数据查询使用
            holder.txtSchedule.setText("全部日程");
        } else {
            holder.txtSchedule.setText("第" + schedule.getScheduleNo() + "场  " + DateUtil.formatTime2(Long.valueOf(schedule.getBeginTime()), "yyyy-MM-dd HH:mm:ss"));
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
