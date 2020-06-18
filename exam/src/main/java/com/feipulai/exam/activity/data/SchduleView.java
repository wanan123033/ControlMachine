package com.feipulai.exam.activity.data;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.data.adapter.DataMacResultAdapter;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 机器成绩显示View
 * Created by zzs on  2019/6/25
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SchduleView extends LinearLayout {

    private Context mContext;

    @BindView(R.id.tv_result)
    TextView tv_result;
    @BindView(R.id.tv_date)
    TextView tv_date;
    @BindView(R.id.tv_beginTime)
    TextView tv_beginTime;

    public SchduleView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public SchduleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_schdule_result, null);
        ButterKnife.bind(this, view);
        addView(view);
    }
    public void setData(Schedule schedule, RoundResult result){
        tv_result.append(ResultDisplayUtils.getStrResultForDisplay(result.getResult()));
        tv_date.append(TestConfigs.df.format(Long.parseLong(schedule.getBeginTime())));
        tv_beginTime.append(TestConfigs.df.format(Long.parseLong(result.getTestTime())));
    }
}
