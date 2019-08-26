package com.feipulai.host.activity.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.height_weight.HWConfigs;
import com.feipulai.host.adapter.ResultDetailAdapter;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.utils.ResultDisplayUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;

public class DataDisplayActivity extends BaseTitleActivity {


    @BindView(R.id.txt_stu_code)
    TextView txtStuCode;
    @BindView(R.id.txt_stu_name)
    TextView txtStuName;
    @BindView(R.id.txt_stu_sex)
    TextView txtStuSex;
    @BindView(R.id.txt_result_title)
    TextView txtResultTitle;
    @BindView(R.id.txt_stu_result)
    TextView txtStuResult;
    @BindView(R.id.ll_stu_result)
    LinearLayout llStuResult;
    @BindView(R.id.rv_result)
    RecyclerView rvResult;
    private DataRetrieveBean mDataRetrieveBean;

    private Comparator<RoundResult> roundResultComparator = Collections.reverseOrder(new Comparator<RoundResult>() {
        @Override
        public int compare(RoundResult lhs, RoundResult rhs) {
            return lhs.getTestTime().compareTo(rhs.getTestTime());
        }
    });

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_data_display;
    }

    @Override
    protected void initData() {
        mDataRetrieveBean = (DataRetrieveBean) getIntent().getSerializableExtra(DataRetrieveActivity.DATA_EXTRA);
        llStuResult.setVisibility(View.VISIBLE);
        txtResultTitle.setText("最终成绩:");

        txtStuCode.setText(mDataRetrieveBean.getStudentCode());
        txtStuName.setText(mDataRetrieveBean.getStudentName());
        txtStuSex.setText(mDataRetrieveBean.getSex() == 0 ? "男" : "女");
        rvResult.setLayoutManager(new LinearLayoutManager(this));
        displayResults();
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("成绩详情").addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void displayResults() {
        if (mDataRetrieveBean.getTestState() == 0) {
            //没有测试过,显示未测试
            txtStuResult.setText("未测试");
        } else {
            // TODO: 2019/3/11 应该讲身高体重项目成绩的特殊性剔除掉
            if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_HW) {
//                toastSpeak("身高体重项目暂时成绩还没想清楚咋写");
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

                        txtStuResult.setText(sb.toString());
                        break;

                    case RoundResult.RESULT_STATE_FOUL:
                        txtStuResult.setText("犯规");
                        break;

                }

                break;
            }
        }
        rvResult.setAdapter(new ResultDetailAdapter(heightResults, weightResults));
    }

    private void normalDisplay() {
        //如果测试过,显示成绩
        List<RoundResult> roundResults = DBManager.getInstance().queryResultsByStudentCode(mDataRetrieveBean.getStudentCode());

        Collections.sort(roundResults, roundResultComparator);

        for (RoundResult roundResult : roundResults) {

            if (roundResult.getIsLastResult() == 1) {
                // 0 正常  -1犯规    -2中退    -3放弃
                // 体侧系统没有中退和放弃,且犯规均为机器判定的犯规
                switch (roundResult.getResultState()) {

                    case RoundResult.RESULT_STATE_NORMAL:
                        txtStuResult.setText(ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult()));
                        break;

                    case RoundResult.RESULT_STATE_FOUL:
                        txtStuResult.setText("犯规");
                        break;
                }
                break;
            }
        }
        rvResult.setAdapter(new ResultDetailAdapter(roundResults));
    }

}
