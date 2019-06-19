package com.feipulai.host.adapter;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.entity.Student;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BaseTestResultAdapter extends BaseQuickAdapter<BaseStuPair, BaseTestResultAdapter.ViewHolder> {

    private int mSelect;

    public BaseTestResultAdapter(@Nullable List<BaseStuPair> data) {
        super(R.layout.item_time_result, data);
    }

    @Override
    protected void convert(ViewHolder helper, BaseStuPair item) {
        Student student = item.getStudent();
        helper.no.setText(item.getBaseDevice().getDeviceId() + "");
        helper.studentName.setText(student.getStudentName());
        helper.result.setText(item.getResult() + "");

        if (mSelect == helper.getLayoutPosition()) {
            helper.rl.setBackgroundColor(Color.rgb(30, 144, 255));
            helper.result.setBackgroundColor(Color.rgb(30, 144, 255));
            helper.studentName.setBackgroundColor(Color.rgb(30, 144, 255));
        } else {
            helper.rl.setBackgroundColor(Color.WHITE);
            helper.result.setBackgroundColor(Color.WHITE);
            helper.studentName.setBackgroundColor(Color.WHITE);
        }
    }


    public void setSelectItem(int position) {
        mSelect = position;
    }


    static class ViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_timeResultHandNo)
        TextView no;
        @BindView(R.id.rl_timing_handno)
        RelativeLayout rl;
        @BindView(R.id.tv_timeResultStudentName)
        TextView studentName;
        @BindView(R.id.tv_timeResult)
        TextView result;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
//public class BaseTestResultAdapter extends BaseAdapter {
//
//    private LayoutInflater inflater;
//    private List<BasePairBean> datas;
//    private Context context;
//    private int mSelect;
//
//    public BaseTestResultAdapter(Context context, List<BasePairBean> data) {
//        this.context = context;
//        this.datas = data;
//        inflater = LayoutInflater.from(context);
//    }
//
//    @Override
//    public int getCount() {
//        return datas == null ? 0 : datas.size();
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return datas == null ? null : datas.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ViewHolder holder;
//        if (convertView == null) {
//            convertView = inflater.inflate(R.layout.item_time_result, null);
//            holder = new ViewHolder(convertView);
//            convertView.setTag(holder);
//        }
//        holder = (ViewHolder) convertView.getTag();
//
//        Student student = datas.get(position).getStudent();
//        holder.no.setText(datas.get(position).getDeviceId() + "");
//        holder.studentName.setText(student.getStudentName());
//        holder.result.setText(datas.get(position).getResult() + "");
//
//        if (mSelect == position) {
//            holder.rl.setBackgroundColor(Color.rgb(30, 144, 255));
//            holder.result.setBackgroundColor(Color.rgb(30, 144, 255));
//            holder.studentName.setBackgroundColor(Color.rgb(30, 144, 255));
//        } else {
//            holder.rl.setBackgroundColor(Color.WHITE);
//            holder.result.setBackgroundColor(Color.WHITE);
//            holder.studentName.setBackgroundColor(Color.WHITE);
//        }
//        return convertView;
//    }
//
//    public void setSelectItem(int position) {
//        mSelect = position;
//    }
//
//
//    static class ViewHolder {
//        @BindView(R.id.tv_timeResultHandNo)
//        TextView no;
//        @BindView(R.id.rl_timing_handno)
//        RelativeLayout rl;
//        @BindView(R.id.tv_timeResultStudentName)
//        TextView studentName;
//        @BindView(R.id.tv_timeResult)
//        TextView result;
//
//        ViewHolder(View view) {
//            ButterKnife.bind(this, view);
//        }
//    }
//
//}
