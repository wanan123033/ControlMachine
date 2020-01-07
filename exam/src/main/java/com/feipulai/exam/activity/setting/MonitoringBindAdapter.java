package com.feipulai.exam.activity.setting;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zzs on  2019/12/31
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MonitoringBindAdapter extends BaseQuickAdapter<MonitoringBean, MonitoringBindAdapter.ViewHolder> {

    public MonitoringBindAdapter(@Nullable List<MonitoringBean> data) {
        super(R.layout.item_monitoring_bind, data);
    }

    @Override
    protected void convert(final ViewHolder viewHolder, MonitoringBean monitoringBean) {
        viewHolder.txtBindTime.setText(monitoringBean.getBindTime());
        viewHolder.txtMonitoringSerial.setText(monitoringBean.getMonitoringSerial());
        //将位置设置为CheckBox的tag
        viewHolder.mCbSelect.setTag(viewHolder.getLayoutPosition());
        viewHolder.mCbSelect.setChecked(monitoringBean.isSelect());
        viewHolder.setOnCheckedChangeListener(R.id.cb_select, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (viewHolder.getLayoutPosition() != -1) {
                            getData().get(viewHolder.getLayoutPosition()).setSelect(isChecked);
                            notifyItemChanged(viewHolder.getLayoutPosition());
                        }

                    }
                });
            }
        });
        if (monitoringBean.isSelect()) {
            viewHolder.txtMonitoringSerial.setSelected(true);
            viewHolder.txtBindTime.setSelected(true);
            viewHolder.mViewCbContent.setSelected(true);
        } else {
            viewHolder.txtMonitoringSerial.setSelected(false);
            viewHolder.txtBindTime.setSelected(false);
            viewHolder.mViewCbContent.setSelected(false);
        }
    }


    class ViewHolder extends BaseViewHolder {
        @BindView(R.id.cb_select)
        CheckBox mCbSelect;
        @BindView(R.id.item_txt_monitoring_serial)
        TextView txtMonitoringSerial;
        @BindView(R.id.item_txt_bind_time)
        TextView txtBindTime;
        @BindView(R.id.view_cb_content)
        RelativeLayout mViewCbContent;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
