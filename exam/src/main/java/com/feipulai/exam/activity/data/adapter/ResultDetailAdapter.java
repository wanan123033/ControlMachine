package com.feipulai.exam.activity.data.adapter;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.dialog.DialogUtils;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.data.MachineResultView;
import com.feipulai.exam.activity.data.SchduleView;
import com.feipulai.exam.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.exam.config.HWConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by James on 2018/2/7 0007.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ResultDetailAdapter extends BaseQuickAdapter<RoundResult, ResultDetailAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<RoundResult> mRoundResults;

    private List<RoundResult> mheightResults;
    private List<RoundResult> mweightResults;

    private boolean isHW;
    private int selectedPos = -1;

    public ResultDetailAdapter(@Nullable List<RoundResult> data) {
        super(R.layout.item_result_detail, data);
    }

    public ResultDetailAdapter(@Nullable List<RoundResult> mheightResults, List<RoundResult> mweightResults) {
        super(R.layout.item_result_detail, mheightResults);
        this.mheightResults = mheightResults;
        this.mweightResults = mweightResults;
        isHW = true;
    }


    public void setSelectedPos(int position) {
        this.selectedPos = position;
    }

    @Override
    protected void convert(ViewHolder viewHolder, final RoundResult roundResult) {
        String displayStr;
        if (roundResult.getResultTestState() == 1){
            viewHolder.tv_in.setText("重测");
        }else {
            viewHolder.tv_in.setText("");
        }
        if (!isHW) {
            viewHolder.mTvTimes.setText(roundResult.getRoundNo() + "");
            if (!TextUtils.isEmpty(roundResult.getTestTime())) {
                viewHolder.mTvTestTime.setText(TestConfigs.df.format(Long.parseLong(roundResult.getTestTime())));
            }

            displayStr = ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult());
            if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_TS) {
                viewHolder.mTvResult.setText(setResult(roundResult.getResultState(), displayStr) + "(" + roundResult.getStumbleCount() + ")");
            } else {
                viewHolder.mTvResult.setText(setResult(roundResult.getResultState(), displayStr));
            }

        } else {
            RoundResult heightResult = roundResult;
            RoundResult weightResult = mweightResults.get(viewHolder.getLayoutPosition());

            displayStr = ResultDisplayUtils.getStrResultForDisplay(heightResult.getResult(), HWConfigs.HEIGHT_ITEM)
                    + ""
                    + ResultDisplayUtils.getStrResultForDisplay(weightResult.getResult(), HWConfigs.WEIGHT_ITEM);
            viewHolder.mTvTimes.setText(roundResult.getRoundNo() + "");
            if (!TextUtils.isEmpty(roundResult.getTestTime())) {
                viewHolder.mTvTestTime.setText(TestConfigs.df.format(Long.parseLong(roundResult.getTestTime())));
            }
            viewHolder.mTvResult.setText(setResult(heightResult.getResultState(), displayStr));
        }
        if (viewHolder.getLayoutPosition() == 0 || getData().get(viewHolder.getLayoutPosition()).getExamType() != getData().get(viewHolder.getLayoutPosition() - 1).getExamType()) {
            viewHolder.mViewHead.setVisibility(View.VISIBLE);
            viewHolder.mTvStatus.setText(setResultState(roundResult.getExamType()));
        } else {
            viewHolder.mViewHead.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(roundResult.getEndTime())) {
            viewHolder.mTvEndTime.setText(TestConfigs.df.format(Long.parseLong(roundResult.getEndTime())));
        }

        viewHolder.mTvResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMachineResult(roundResult);
            }
        });

        viewHolder.mTvTimes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSchedule(roundResult);
            }
        });
        if (viewHolder.getLayoutPosition() == selectedPos) {
            viewHolder.setBackgroundRes(R.id.ll_result, R.drawable.blue_radius_10);
        } else {
            viewHolder.setBackgroundRes(R.id.ll_result, R.drawable.grey_radius_10);
        }
    }

    private void showSchedule(RoundResult roundResult) {
        Schedule schedule = DBManager.getInstance().getSchedulesByNo(roundResult.getScheduleNo());
        SchduleView view = new SchduleView(mContext);
        view.setData(schedule, roundResult);
        DialogUtils.create(mContext, view, true).show();
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom) {
                int height = v.getHeight();     //此处的view 和v 其实是同一个控件

                int needHeight = 260;

                if (height > needHeight) {
                    //注意：这里的 LayoutParams 必须是 FrameLayout的！！
                    v.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                            needHeight));
                }
            }
        });
    }

    private void setMachineResult(RoundResult roundResult) {
        List<MachineResult> machineResultList = DBManager.getInstance().getItemRoundMachineResult(roundResult.getStudentCode(),
                roundResult.getTestNo(), roundResult.getRoundNo());
        if (machineResultList.size() > 0) {
            MachineResultView resultView = new MachineResultView(mContext);
            resultView.setData(machineResultList);
            DialogUtils.create(mContext, resultView, true).show();
            //这种设置宽高的方式也是好使的！！！-- show 前调用，show 后调用都可以！！！
            resultView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                           int oldRight, int oldBottom) {
                    int height = v.getHeight();     //此处的view 和v 其实是同一个控件

                    int needHeight = 260;

                    if (height > needHeight) {
                        //注意：这里的 LayoutParams 必须是 FrameLayout的！！
                        v.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                needHeight));
                    }
                }
            });

        }


    }

    private String setResult(int state, String result) {

        switch (state) {
            case RoundResult.RESULT_STATE_NORMAL:
                return result;
            case RoundResult.RESULT_STATE_FOUL:
                return "X";
            case RoundResult.RESULT_STATE_BACK:
                return "中退";
            case RoundResult.RESULT_STATE_WAIVE:
                return "放弃";
            default:
                return result;
        }
    }

    private String setResultState(int state) {

        switch (state) {
            case RoundResult.RESULT_STATE_NORMAL:
                return "正常考";
            case RoundResult.RESULT_STATE_FOUL:
                return "补考";
            case RoundResult.RESULT_STATE_BACK:
                return "缓考";
            case RoundResult.RESULT_STATE_WAIVE:
                return "放弃";
            default:
                return "正常考";
        }
    }

    static class ViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_times)
        TextView mTvTimes;
        @BindView(R.id.tv_result)
        TextView mTvResult;
        @BindView(R.id.tv_test_time)
        TextView mTvTestTime;
        @BindView(R.id.item_tv_status)
        TextView mTvStatus;
        @BindView(R.id.item_view_head)
        LinearLayout mViewHead;
        @BindView(R.id.tv_end_time)
        TextView mTvEndTime;
        @BindView(R.id.tv_in)
        TextView tv_in;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
