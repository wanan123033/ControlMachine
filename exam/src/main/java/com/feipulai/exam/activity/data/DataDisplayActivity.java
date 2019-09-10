package com.feipulai.exam.activity.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.data.adapter.ResultDetailAdapter;
import com.feipulai.exam.bean.DataRetrieveBean;
import com.feipulai.exam.config.HWConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;

/**
 * 查询详情
 */
public class DataDisplayActivity extends BaseTitleActivity {

    @BindView(R.id.tv_stuName)
    TextView mTvStuName;
    @BindView(R.id.tv_sex)
    TextView mTvSex;
    @BindView(R.id.tv_stuCode)
    TextView mTvStuCode;
    @BindView(R.id.tv_best_result)
    TextView mTvBestResult;
    @BindView(R.id.rv_result)
    RecyclerView rvResult;
    private DataRetrieveBean mDataRetrieveBean;
    //    private Item mCurrentItem;

    private Comparator<RoundResult> roundResultComparator = Collections.reverseOrder(new Comparator<RoundResult>() {
        @Override
        public int compare(RoundResult lhs, RoundResult rhs) {
            return lhs.getTestTime().compareTo(rhs.getTestTime());
        }
    });
    private String itemCode;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_data_display;
    }

    @Override
    protected void initData() {
        mDataRetrieveBean = (DataRetrieveBean) getIntent().getSerializableExtra(DataRetrieveActivity.DATA_EXTRA);
        itemCode = getIntent().getStringExtra(DataRetrieveActivity.DATA_ITEM_CODE);
        mTvStuCode.setText(mDataRetrieveBean.getStudentCode());
        mTvStuName.setText(mDataRetrieveBean.getStudentName());
        mTvSex.setText(mDataRetrieveBean.getSex() == 0 ? "男" : "女");
        displayResults();
        rvResult.setLayoutManager(new LinearLayoutManager(this));
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("成绩详情") ;
    }

    private void displayResults() {
        if (mDataRetrieveBean.getTestState() == 0) {
            //没有测试过,显示未测试
            mTvBestResult.setText("未测试");
            return;
        } else {
            if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_HW) {
                displayHW();
            } else {
                normalDisplay();
            }
        }
    }

    private void displayHW() {

        List<RoundResult> heightResults = DBManager.getInstance().queryResultsByStudentCode(mDataRetrieveBean.getStudentCode(), HWConfigs
                .HEIGHT_ITEM);
        List<RoundResult> weightResults = DBManager.getInstance().queryResultsByStudentCode(mDataRetrieveBean.getStudentCode(), HWConfigs
                .WEIGHT_ITEM);

        Collections.sort(heightResults, roundResultComparator);
        Collections.sort(weightResults, roundResultComparator);

        RoundResult roundResult;
        for (int i = 0; i < heightResults.size(); i++) {
            roundResult = heightResults.get(i);
            if (roundResult.getIsLastResult() == 1) {
                // 0 正常  -1犯规    -2中退    -3放弃
                // 体侧系统没有中退和放弃,且犯规均为机器判定的犯规
                switch (roundResult.getResultState()) {

                    case RoundResult.RESULT_STATE_NORMAL:
                        StringBuilder sb = new StringBuilder();
                        sb.append(ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult(), HWConfigs.HEIGHT_ITEM));
                        sb.append("/");
                        sb.append(ResultDisplayUtils.getStrResultForDisplay(weightResults.get(i).getResult(), HWConfigs.WEIGHT_ITEM));
                        mTvBestResult.setText(sb.toString());
                        break;

                    case RoundResult.RESULT_STATE_FOUL:
                        mTvBestResult.setText("X");
                        break;

                }

                break;
            }
        }
        rvResult.setAdapter(new ResultDetailAdapter(heightResults, weightResults));
    }

    private void normalDisplay() {
        //如果测试过,显示成绩
        List<RoundResult> roundResults = DBManager.getInstance().queryResultsByStudentCode(itemCode, mDataRetrieveBean.getStudentCode());

        Collections.sort(roundResults, roundResultComparator);

        for (RoundResult roundResult : roundResults) {

            if (roundResult.getIsLastResult() == 1) {
                // 0 正常  -1犯规    -2中退    -3放弃
                // 体侧系统没有中退和放弃,且犯规均为机器判定的犯规
                switch (roundResult.getResultState()) {

                    case RoundResult.RESULT_STATE_NORMAL:
                        mTvBestResult.setText(ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult()));
                        break;

                    case RoundResult.RESULT_STATE_FOUL:
                        mTvBestResult.setText("X");
                        break;
                }
                break;
            }
        }
        rvResult.setAdapter(new ResultDetailAdapter(roundResults));
    }

}
