package com.feipulai.exam.activity.volleyball;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.volleyball.adapter.CheckDeviceAdapter;
import com.feipulai.exam.activity.volleyball.stepView.StepBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zzs on  2019/7/16
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class CheckDeviceView extends RelativeLayout {

    @BindView(R.id.rv_step)
    RecyclerView rvStep;
    private Context mContext;
    private List<List<StepBean>> checkList;

    public CheckDeviceView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CheckDeviceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_volleyball_check_device, null);
        ButterKnife.bind(this, view);
        addView(view);
        rvStep.setLayoutManager(new LinearLayoutManager(mContext));
    }

    public void setData(int lenght, List<Integer> dataCheck) {
        checkList = new ArrayList<>(lenght);
        for (int i = 0; i < lenght; i++) {
            List<StepBean> stepBeanList = new ArrayList<>();
            boolean isAddTest = false;
            for (int j = i * 10; j < i * 10 + 10; j++) {
                if (dataCheck.get(j) == 1) {
                    stepBeanList.add(new StepBean(isAddTest ? "" : mContext.getString(R.string.abnormal), dataCheck.get(j)));
                    isAddTest = true;
                } else {
                    stepBeanList.add(new StepBean("", dataCheck.get(j)));
                }
            }
            checkList.add(stepBeanList);
        }
        rvStep.setAdapter(new CheckDeviceAdapter(checkList));
    }

    /**
     * 初始化未连接数据
     */
    public void setUnunitedData(int initial) {
        checkList = new ArrayList<>(initial);
        for (int i = 0; i < initial; i++) {
            List<StepBean> stepBeanList = new ArrayList<>();
            for (int j = i * 10; j < i * 10 + 10; j++) {
                stepBeanList.add(new StepBean("", -1));
            }
            checkList.add(stepBeanList);
        }
        rvStep.setAdapter(new CheckDeviceAdapter(checkList));
    }
}
