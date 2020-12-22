package com.feipulai.exam.activity.data;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.data.adapter.ResultDetailAdapter;
import com.feipulai.exam.bean.DataRetrieveBean;
import com.feipulai.exam.config.HWConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.utils.LogUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 查询详情
 */
public class DataDisplayActivity extends BaseTitleActivity implements BaseQuickAdapter.OnItemClickListener {

    public static final String ISSHOWPENALIZEFOUL = "ISSHOWPENALIZEFOUL";
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
    @BindView(R.id.tv_penalizeFoul)
    TextView tv_penalizeFoul;

    @BindView(R.id.img_portrait)
    ImageView imgPortrait;
    private DataRetrieveBean mDataRetrieveBean;
    private ResultDetailAdapter resultDetailAdapter;
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
        int vistity = getIntent().getIntExtra(ISSHOWPENALIZEFOUL, View.GONE);
        tv_penalizeFoul.setVisibility(vistity);
        Log.e("itemCode", "---------" + itemCode);
        mTvStuCode.setText(mDataRetrieveBean.getStudentCode());
        mTvStuName.setText(mDataRetrieveBean.getStudentName());
        mTvSex.setText(mDataRetrieveBean.getSex() == 0 ? "男" : "女");
//        if (TextUtils.isEmpty(mDataRetrieveBean.getPortrait())) {
//            imgPortrait.setImageResource(R.mipmap.icon_head_photo);
//        } else {
//            imgPortrait.setImageBitmap(mDataRetrieveBean.getBitmapPortrait());
//        }
        Glide.with(imgPortrait.getContext()).load(MyApplication.PATH_IMAGE + mDataRetrieveBean.getStudentCode() + ".jpg")
                .error(R.mipmap.icon_head_photo).placeholder(R.mipmap.icon_head_photo).into(imgPortrait);

        rvResult.setLayoutManager(new LinearLayoutManager(this));
        displayResults();
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("成绩详情");
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
        resultDetailAdapter = new ResultDetailAdapter(heightResults, weightResults);
        rvResult.setAdapter(resultDetailAdapter);
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
        resultDetailAdapter = new ResultDetailAdapter(roundResults);
        resultDetailAdapter.setOnItemClickListener(this);
        rvResult.setAdapter(resultDetailAdapter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.life("DataDisplayActivity onCreate");
    }
    @OnClick(R.id.tv_penalizeFoul)
    public void onClick(View view){
        if (currentResult != null) {
            new SweetAlertDialog(this)
                    .setTitleText("提示")
                    .setContentText("确定是犯规?")
                    .setConfirmText("是")
                    .setCancelText("否")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            if (currentResult != null) {
                                currentResult.setResultState(RoundResult.RESULT_STATE_FOUL);
                                DBManager.getInstance().updateRoundResult(currentResult);
                                toastSpeak("成绩状态更新成功!");
                                sweetAlertDialog.dismissWithAnimation();
                                displayResults();
                            }

                        }
                    })
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    }).show();
        }else {
            toastSpeak("请先选择一条成绩");
        }
    }
    RoundResult currentResult;
    @Override
    public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
        currentResult = resultDetailAdapter.getItem(i);
        resultDetailAdapter.setSelectedPos(i);
        resultDetailAdapter.notifyDataSetChanged();
    }
}
