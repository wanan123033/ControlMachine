package com.feipulai.exam.adapter;

import android.graphics.Color;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.TimingBean;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.utils.DateUtil;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * created by ww on 2019/6/12.
 */
public class RaceTimingAdapter extends RecyclerView.Adapter<RaceTimingAdapter.VH> {
    private MyClickListener myClickListener;
    private List<TimingBean> timingLists;//选中开始组的信息 参数1：组序号 参数2：组状态（1等待发令2违规返回3完成计时）

    public RaceTimingAdapter(List<TimingBean> timingLists, MyClickListener listener) {
        this.timingLists = timingLists;
        this.myClickListener = listener;
    }

    //自定义接口，用于回调按钮点击事件到Activity
    public interface MyClickListener {
        void clickTimingWaitListener(int position);

        void clickTimingBackListener(int position);

        void clickTimingCompleteListener(int position);
    }

    //创建ViewHolder
    public static class VH extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_timing_state)
        TextView tvTimingState;
        @BindView(R.id.timer)
        Chronometer timer;
        @BindView(R.id.btn_timing_wait)
        Button btnTimingWait;
        @BindView(R.id.btn_timing_back)
        Button btnTimingBack;
        @BindView(R.id.btn_timing_complete)
        Button btnTimingComplete;
        @BindView(R.id.tv_timing_time)
        TextView tvTimingTime;
        @BindView(R.id.tv_timing_group)
        TextView tvTimingGroup;

        public VH(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }


    //在Adapter中实现3个方法
    @Override
    public void onBindViewHolder(VH holder, final int position) {
        holder.tvTimingTime.setText(timingLists.get(position).getTime() == 0 ? "发令时刻：" : "发令时刻：" + DateUtil.formatTime(timingLists.get(position).getTime(), "yyyy-MM-dd HH:mm:ss.SSS"));
        switch (timingLists.get(position).getState()) {
            case 1:
                startTiming(holder.timer);
                holder.tvTimingState.setText("计时");
                holder.tvTimingState.setTextColor(Color.WHITE);
                holder.tvTimingState.setBackgroundResource(R.color.viewfinder_laser);
                break;
            case 2:
                holder.timer.setBase(SystemClock.elapsedRealtime());//计时器清零
                holder.timer.stop();
                holder.tvTimingState.setText("空闲");
                holder.tvTimingState.setTextColor(Color.BLACK);
                holder.tvTimingState.setBackgroundResource(R.color.green_yellow);
                break;
            case 3:
                break;
            default:
                break;
        }

        if (myClickListener != null) {
            holder.btnTimingWait.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myClickListener.clickTimingWaitListener(position);
                }
            });
            holder.btnTimingBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myClickListener.clickTimingBackListener(position);
                }
            });
            holder.btnTimingComplete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myClickListener.clickTimingCompleteListener(position);
                }
            });
        }
    }

    private void startTiming(Chronometer time) {
        time.setBase(SystemClock.elapsedRealtime());//计时器清零
        int hour = (int) ((SystemClock.elapsedRealtime() - time.getBase()) / 1000 / 60);
        time.setFormat("0" + String.valueOf(hour) + ":%s");
        time.start();
    }

    @Override
    public int getItemCount() {
        return timingLists.size();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater.from指定写法
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_timing, parent, false);
        return new VH(v);
    }
}
