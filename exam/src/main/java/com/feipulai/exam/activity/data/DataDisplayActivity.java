package com.feipulai.exam.activity.data;

import android.content.Intent;
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
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.bean.DataRetrieveBean;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.HWConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.service.UploadService;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
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
    public static final String TESTNO = "testNo";
    public static final String GROUP_ID = "GROUP_ID";
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
    @BindView(R.id.tv_ins_penalizeFoul)
    TextView tv_ins_penalizeFoul;
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
    private int vistity;
    private int testNo;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_data_display;
    }

    @Override
    protected void initData() {
        mDataRetrieveBean = (DataRetrieveBean) getIntent().getSerializableExtra(DataRetrieveActivity.DATA_EXTRA);
        itemCode = getIntent().getStringExtra(DataRetrieveActivity.DATA_ITEM_CODE);

        vistity = getIntent().getIntExtra(ISSHOWPENALIZEFOUL, View.GONE);
        testNo = getIntent().getIntExtra(TESTNO, 1);
        tv_penalizeFoul.setVisibility(vistity);
        tv_ins_penalizeFoul.setVisibility(vistity);
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

    private void insertRound(DataRetrieveBean mDataRetrieveBean, int testNo) {
        int roundNo = 1;
        boolean isInsert = false;
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_HW) {
//            List<RoundResult> heightResults = null,weightResults = null;
//            if (mDataRetrieveBean.getGroupId() == RoundResult.DEAFULT_GROUP_ID) {
//                heightResults = DBManager.getInstance().queryResultsByStudentCode(mDataRetrieveBean.getStudentCode(), HWConfigs
//                        .HEIGHT_ITEM);
//                weightResults = DBManager.getInstance().queryResultsByStudentCode(mDataRetrieveBean.getStudentCode(), HWConfigs
//                        .WEIGHT_ITEM);
//            }else {
//                heightResults = DBManager.getInstance().queryResultsByStudentCode(mDataRetrieveBean.getStudentCode(),mDataRetrieveBean.getGroupId(),mDataRetrieveBean.getExamType(),mDataRetrieveBean.getScheduleNo(), HWConfigs
//                        .HEIGHT_ITEM);
//                weightResults = DBManager.getInstance().queryResultsByStudentCode(mDataRetrieveBean.getStudentCode(),mDataRetrieveBean.getGroupId(),mDataRetrieveBean.getExamType(),mDataRetrieveBean.getScheduleNo(), HWConfigs
//                        .WEIGHT_ITEM);
//            }
//            if (heightResults != null && heightResults.size() < testNo){
//                roundNo = heightResults.size() + 1;
//                isInsert = true;
//            }
        } else {
            List<RoundResult> roundResults = null;
            if (mDataRetrieveBean.getGroupId() == RoundResult.DEAFULT_GROUP_ID) {
                roundResults = DBManager.getInstance().queryResultsByStudentCode(itemCode, mDataRetrieveBean.getStudentCode());
            } else {
                roundResults = DBManager.getInstance().queryResultsByStudentCode(itemCode, mDataRetrieveBean.getStudentCode(), mDataRetrieveBean.getGroupId(), mDataRetrieveBean.getExamType(), mDataRetrieveBean.getScheduleNo());
            }
            if (roundResults != null && roundResults.size() < TestConfigs.getMaxTestCount(this)) {
                roundNo = roundResults.size() + 1;
                isInsert = true;
            }
        }
        if (isInsert) {
            RoundResult roundResult = new RoundResult();
            roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
            roundResult.setStudentCode(mDataRetrieveBean.getStudentCode());
            roundResult.setItemCode(TestConfigs.getCurrentItemCode());
            roundResult.setResult(0);
            roundResult.setMachineResult(0);
            roundResult.setResultState(2);
            roundResult.setTestTime(System.currentTimeMillis() + "");
            roundResult.setEndTime(System.currentTimeMillis() + "");
            roundResult.setRoundNo(roundNo);
            roundResult.setTestNo(testNo);
            roundResult.setGroupId(mDataRetrieveBean.getGroupId());
            roundResult.setExamType(mDataRetrieveBean.getExamType());
            roundResult.setScheduleNo(mDataRetrieveBean.getScheduleNo());
            roundResult.setUpdateState(0);
            roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
            RoundResult bestResult = DBManager.getInstance().queryBestScore(mDataRetrieveBean.getStudentCode(), testNo);
            if (bestResult != null) {
                // 原有最好成绩犯规 或者原有最好成绩没有犯规但是现在成绩更好
                if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && roundResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= roundResult.getResult()) {
                    // 这个时候就要同时修改这两个成绩了
                    roundResult.setIsLastResult(1);
                    bestResult.setIsLastResult(0);
                    DBManager.getInstance().updateRoundResult(bestResult);

                } else {
                    if (bestResult.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                        roundResult.setIsLastResult(1);
                        bestResult.setIsLastResult(0);
                        DBManager.getInstance().updateRoundResult(bestResult);

                    } else {
                        roundResult.setIsLastResult(0);

                    }
                }
            } else {
                // 第一次测试
                roundResult.setIsLastResult(1);

            }
            //生成结束时间
            roundResult.setEndTime(System.currentTimeMillis() + "");
            DBManager.getInstance().insertRoundResult(roundResult);
            EventBus.getDefault().post(new BaseEvent(roundResult, EventConfigs.INSTALL_RESULT));
            displayResults();
            toastSpeak("新增成绩成功");
        } else {
            toastSpeak("无法新增轮次成绩");
        }
    }

    private void displayHW() {
        List<RoundResult> heightResults = null, weightResults = null;
        if (mDataRetrieveBean.getGroupId() == RoundResult.DEAFULT_GROUP_ID) {
            heightResults = DBManager.getInstance().queryResultsByStudentCode(mDataRetrieveBean.getStudentCode(), HWConfigs
                    .HEIGHT_ITEM);
            weightResults = DBManager.getInstance().queryResultsByStudentCode(mDataRetrieveBean.getStudentCode(), HWConfigs
                    .WEIGHT_ITEM);
        } else {
            heightResults = DBManager.getInstance().queryResultsByStudentCode(mDataRetrieveBean.getStudentCode(), mDataRetrieveBean.getGroupId(), mDataRetrieveBean.getExamType(), mDataRetrieveBean.getScheduleNo(), HWConfigs
                    .HEIGHT_ITEM);
            weightResults = DBManager.getInstance().queryResultsByStudentCode(mDataRetrieveBean.getStudentCode(), mDataRetrieveBean.getGroupId(), mDataRetrieveBean.getExamType(), mDataRetrieveBean.getScheduleNo(), HWConfigs
                    .WEIGHT_ITEM);
        }

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

        List<RoundResult> roundResults = null;
        if (mDataRetrieveBean.getGroupId() == RoundResult.DEAFULT_GROUP_ID) {
            roundResults = DBManager.getInstance().queryResultsByStudentCode(itemCode, mDataRetrieveBean.getStudentCode());
        } else {
            roundResults = DBManager.getInstance().queryResultsByStudentCode(itemCode, mDataRetrieveBean.getStudentCode(), mDataRetrieveBean.getGroupId(), mDataRetrieveBean.getExamType(), mDataRetrieveBean.getScheduleNo());
        }

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

    @OnClick({R.id.tv_penalizeFoul, R.id.tv_ins_penalizeFoul})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_penalizeFoul:
                penalize();
                break;
            case R.id.tv_ins_penalizeFoul:
                insertRound(mDataRetrieveBean, testNo);
                break;
        }
    }

    private void penalize() {
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
                                RoundResult laseResult;
                                if (currentResult.getGroupId() == RoundResult.DEAFULT_GROUP_ID) {
                                    laseResult = DBManager.getInstance().queryLastRountScoreByExamType(mDataRetrieveBean.getStudentCode(), mDataRetrieveBean.getExamType(), itemCode);
                                } else {
                                    laseResult = DBManager.getInstance().queryLastRountGroupBestScore(mDataRetrieveBean.getStudentCode(), currentResult.getGroupId());
                                }
                                if (laseResult != null && laseResult.getIsLastResult() == 0) {
                                    //更新最好成绩
                                    List<RoundResult> resultList = resultDetailAdapter.getData();
                                    for (RoundResult roundResult : resultList) {
                                        if (roundResult.getId() == laseResult.getId()) {
                                            roundResult.setIsLastResult(1);
                                        } else {
                                            roundResult.setIsLastResult(0);
                                        }

                                    }
//                                    laseResult.setIsLastResult(1);
                                    DBManager.getInstance().updateRoundResult(resultList);
                                }

                                EventBus.getDefault().post(new BaseEvent(currentResult, EventConfigs.UPDATE_RESULT));
                                toastSpeak("成绩状态更新成功!");
                                sweetAlertDialog.dismissWithAnimation();
                                displayResults();
                                uploadResult(currentResult);
                            }

                        }
                    })
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    }).show();
        } else {
            toastSpeak("请先选择一条成绩");
        }
    }

    /**
     * 上传成绩
     *
     * @param currentResult
     */
    private void uploadResult(RoundResult currentResult) {
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort("自动上传成绩需下载更新项目信息");
            return;
        }
        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(currentResult);
        UploadResults uploadResults = new UploadResults(currentResult.getScheduleNo(), TestConfigs.getCurrentItemCode(),
                currentResult.getStudentCode(), currentResult.getTestNo() + "", null, RoundResultBean.beanCope(roundResultList));
        uploadResult(uploadResults);

    }

    private void uploadResult(UploadResults uploadResults) {
        ServerMessage.uploadResult(this, uploadResults);
    }

    RoundResult currentResult;

    @Override
    public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
        currentResult = resultDetailAdapter.getItem(i);
        resultDetailAdapter.setSelectedPos(i);
        resultDetailAdapter.notifyDataSetChanged();
    }
}
