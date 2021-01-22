package com.feipulai.exam.activity.MiddleDistanceRace.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.TimingBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.TIMING_START;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.TIMING_STATE_BACK;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.TIMING_STATE_COMPLETE;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.TIMING_STATE_NOMAL;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.TIMING_STATE_TIMING;
import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.TIMING_STATE_WAITING;

/**
 * created by ww on 2019/6/12.
 */
public class RaceTimingAdapter extends RecyclerView.Adapter<RaceTimingAdapter.VH> {
    private MyClickListener myClickListener;
    private List<TimingBean> timingLists;//选中开始组的信息 参数1：组序号 参数2：组状态（1等待发令2违规返回3完成计时）
    private Context mContext;

    public RaceTimingAdapter(Context context, List<TimingBean> timingLists, MyClickListener listener) {
        this.timingLists = timingLists;
        this.myClickListener = listener;
        mContext = context;
    }

    //自定义接口，用于回调按钮点击事件到Activity
    public interface MyClickListener {
        void clickTimingWaitListener(int position, VH holder);

        void clickTimingBackListener(int position, VH holder);

        void clickTimingCompleteListener(int position, VH holder);

        void clickTimingDelete(int position,VH holder);
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
        @BindView(R.id.iv_timing_delete)
        ImageView ivDelete;
        @BindView(R.id.ll_timing_item)
        LinearLayout llItem;

        public VH(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }


    //在Adapter中实现3个方法
    @Override
    public void onBindViewHolder(final VH holder, final int position) {
        holder.tvTimingGroup.setText(timingLists.get(position).getItemGroupName());
        holder.tvTimingTime.setText(timingLists.get(position).getTime() == 0 ? "发令时刻：" : "发令时刻：" + DateUtil.formatTime2(timingLists.get(position).getTime(), "yyyy-MM-dd HH:mm:ss.SSS"));
        holder.llItem.setBackgroundResource(timingLists.get(position).getColor());

        holder.timer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                Date d = new Date(time);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+:00:00"));
                holder.timer.setText(sdf.format(d));
            }
        });
//        holder.timer.setBase(System.currentTimeMillis());

        switch (timingLists.get(position).getState()) {
            case TIMING_STATE_NOMAL://初始化状态
                holder.tvTimingState.setText("空闲");
                holder.tvTimingState.setTextColor(Color.BLACK);
                holder.tvTimingState.setBackgroundResource(R.color.result_points);

                holder.btnTimingWait.setEnabled(true);
                holder.btnTimingWait.setBackgroundResource(R.drawable.btn_background);
                holder.btnTimingWait.setTextColor(Color.WHITE);

                holder.btnTimingBack.setEnabled(false);
                holder.btnTimingBack.setBackgroundResource(R.color.grey_A8);
                holder.btnTimingBack.setTextColor(Color.GRAY);

                holder.btnTimingComplete.setEnabled(false);
                holder.btnTimingComplete.setBackgroundResource(R.color.grey_A8);
                holder.btnTimingComplete.setTextColor(Color.GRAY);
                break;
            case TIMING_STATE_WAITING://等待发令状态(此刻已经开始计时)
//                holder.timer.setTextColor(Color.GREEN);
//                //开始计时后完成计时按钮才可点击
//
//                holder.btnTimingComplete.setEnabled(true);
//                holder.btnTimingComplete.setBackgroundResource(R.drawable.btn_background);
//                holder.btnTimingComplete.setTextColor(Color.WHITE);
//
////                startTiming(holder.timer);
//                holder.tvTimingState.setText("计时");
//                holder.tvTimingState.setTextColor(Color.WHITE);
//                holder.tvTimingState.setBackgroundResource(R.color.viewfinder_laser);
                break;
            case TIMING_STATE_BACK:
//                holder.timer.setBase(SystemClock.elapsedRealtime());//计时器清零
//                holder.timer.setTextColor(Color.WHITE);
//                holder.timer.release();
                holder.tvTimingState.setText("空闲");
                holder.tvTimingState.setTextColor(Color.BLACK);
                holder.tvTimingState.setBackgroundResource(R.color.result_points);
                break;
            case TIMING_STATE_COMPLETE:
//                holder.timer.setBase(SystemClock.elapsedRealtime());//计时器清零
//                holder.timer.setTextColor(Color.WHITE);
//                holder.timer.release();
                holder.tvTimingState.setText("空闲");
                holder.tvTimingState.setTextColor(Color.BLACK);
                holder.tvTimingState.setBackgroundResource(R.color.result_points);
                break;
            case TIMING_STATE_TIMING://当前正在计时状态（要和等待状态分清楚）
                break;
            case TIMING_START:
                holder.timer.setTextColor(Color.GREEN);
                //开始计时后完成计时按钮才可点击

                holder.btnTimingComplete.setEnabled(true);
                holder.btnTimingComplete.setBackgroundResource(R.drawable.btn_background);
                holder.btnTimingComplete.setTextColor(Color.WHITE);

                startTiming(holder.timer);
                holder.tvTimingState.setText("计时");
                holder.tvTimingState.setTextColor(Color.WHITE);
                holder.tvTimingState.setBackgroundResource(R.color.viewfinder_laser);

                holder.btnTimingBack.setEnabled(true);
                holder.btnTimingBack.setBackgroundResource(R.drawable.btn_background);
                holder.btnTimingBack.setTextColor(Color.WHITE);

                holder.btnTimingWait.setEnabled(false);
                holder.btnTimingWait.setBackgroundResource(R.color.grey_A8);
                holder.btnTimingWait.setTextColor(Color.GRAY);
                break;
            default:
                break;
        }

        if (myClickListener != null) {
            holder.btnTimingWait.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myClickListener.clickTimingWaitListener(position, holder);
                }
            });
            holder.btnTimingBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myClickListener.clickTimingBackListener(position, holder);
                }
            });

            holder.btnTimingComplete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myClickListener.clickTimingCompleteListener(position, holder);
                }
            });

            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myClickListener.clickTimingDelete(position,holder);
                }
            });
        }
    }

    public void notifyBackGround(VH holder, int flag) {
        switch (flag) {
            case TIMING_STATE_NOMAL://初始化状态
                holder.tvTimingState.setText("空闲");
                holder.tvTimingState.setTextColor(Color.BLACK);
                holder.tvTimingState.setBackgroundResource(R.color.result_points);

                holder.btnTimingWait.setEnabled(true);
                holder.btnTimingWait.setBackgroundResource(R.drawable.btn_background);
                holder.btnTimingWait.setTextColor(Color.WHITE);

                holder.btnTimingBack.setEnabled(false);
                holder.btnTimingBack.setBackgroundResource(R.color.grey_A8);
                holder.btnTimingBack.setTextColor(Color.GRAY);

                holder.btnTimingComplete.setEnabled(false);
                holder.btnTimingComplete.setBackgroundResource(R.color.grey_A8);
                holder.btnTimingComplete.setTextColor(Color.GRAY);
                break;
            case TIMING_STATE_WAITING:
                holder.tvTimingState.setText("等待");
                holder.btnTimingWait.setEnabled(false);
                holder.btnTimingWait.setBackgroundResource(R.color.grey_A8);
                holder.btnTimingWait.setTextColor(Color.GRAY);

                holder.btnTimingBack.setEnabled(true);
                holder.btnTimingBack.setBackgroundResource(R.drawable.btn_background);
                holder.btnTimingBack.setTextColor(Color.WHITE);

                holder.btnTimingComplete.setEnabled(false);
                holder.btnTimingComplete.setBackgroundResource(R.color.grey_A8);
                holder.btnTimingComplete.setTextColor(Color.GRAY);
                break;
            case TIMING_STATE_BACK:
                holder.timer.setBase(SystemClock.elapsedRealtime());//计时器清零
                holder.timer.setTextColor(Color.WHITE);
                holder.timer.stop();
                holder.tvTimingState.setText("空闲");
                holder.tvTimingState.setTextColor(Color.BLACK);
                holder.tvTimingState.setBackgroundResource(R.color.result_points);

                holder.btnTimingWait.setEnabled(true);
                holder.btnTimingWait.setBackgroundResource(R.drawable.btn_background);
                holder.btnTimingWait.setTextColor(Color.WHITE);

                holder.btnTimingBack.setEnabled(false);
                holder.btnTimingBack.setBackgroundResource(R.color.grey_A8);
                holder.btnTimingBack.setTextColor(Color.GRAY);

                holder.btnTimingComplete.setEnabled(false);
                holder.btnTimingComplete.setBackgroundResource(R.color.grey_A8);
                holder.btnTimingComplete.setTextColor(Color.GRAY);
                break;
            case TIMING_STATE_COMPLETE:
                holder.timer.setBase(SystemClock.elapsedRealtime());//计时器清零
                holder.timer.setTextColor(Color.WHITE);
                holder.timer.stop();
                holder.tvTimingState.setText("空闲");
                holder.tvTimingState.setTextColor(Color.BLACK);
                holder.tvTimingState.setBackgroundResource(R.color.result_points);

                holder.btnTimingWait.setEnabled(true);
                holder.btnTimingWait.setBackgroundResource(R.drawable.btn_background);
                holder.btnTimingWait.setTextColor(Color.WHITE);

                holder.btnTimingBack.setEnabled(false);
                holder.btnTimingBack.setBackgroundResource(R.color.grey_A8);
                holder.btnTimingBack.setTextColor(Color.GRAY);

                holder.btnTimingComplete.setEnabled(false);
                holder.btnTimingComplete.setBackgroundResource(R.color.grey_A8);
                holder.btnTimingComplete.setTextColor(Color.GRAY);
                break;
            case TIMING_START:
//                holder.timer.setTextColor(Color.GREEN);
//                //开始计时后完成计时按钮才可点击
//
//                holder.btnTimingComplete.setEnabled(true);
//                holder.btnTimingComplete.setBackgroundResource(R.drawable.btn_background);
//                holder.btnTimingComplete.setTextColor(Color.WHITE);
//
//                startTiming(holder.timer);
//                holder.tvTimingState.setText("计时");
//                holder.tvTimingState.setTextColor(Color.WHITE);
//                holder.tvTimingState.setBackgroundResource(R.color.viewfinder_laser);
                break;
            default:
                break;
        }
    }

    private void startTiming(Chronometer time) {
        time.setBase(SystemClock.elapsedRealtime());//计时器清零
//        int hour = (int) ((SystemClock.elapsedRealtime() - time.getBase()) / 1000 / 60);
//        time.setFormat("0" + String.valueOf(hour) + ":%s");
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
