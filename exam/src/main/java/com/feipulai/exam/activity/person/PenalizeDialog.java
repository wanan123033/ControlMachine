package com.feipulai.exam.activity.person;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.db.DataBaseExecutor;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.adapter.PenalizeResultAdapter;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.service.UploadService;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by pengjf on  2021/7/15
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PenalizeDialog {

    @BindView(R.id.tip)
    TextView tip;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.txt_stu_code)
    TextView txtStuCode;
    @BindView(R.id.tv_no_student)
    TextView tvNoStudent;
    @BindView(R.id.txt_stu_name)
    TextView txtStuName;
    @BindView(R.id.view_txt_cancel)
    TextView viewTxtCancel;
    @BindView(R.id.view_txt_confirm)
    TextView viewTxtConfirm;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.turn_last)
    TextView turnLast;
    @BindView(R.id.turn_next)
    TextView turnNext;
    @BindView(R.id.rv_penalize)
    RecyclerView rvPenalize;
    private Context context;
    private String[] title = {"犯规", "中退", "放弃", "正常"};
    private Dialog dialog;
    private Window window = null;
    private int testTimes;//测试次数
    private int state;//0未检录1已检录未测试2已检录正在测试
    private PenalizeResultAdapter mAdapter;
    private List<String> mList = new ArrayList<>();
    private Student lastStudent;
    private String[] lastResult;//上一个学生成绩
    private Student student;//当前学生
    private String[] results;
    private int resultState;
    private long groupId = -1;
    private int selectPosition = -1;
    private Group group;
    private GroupItem groupItem;

    /**
     * @param context
     */
    public PenalizeDialog(Context context, int testTimes) {
        this.context = context;
        this.testTimes = testTimes;
        results = new String[testTimes];
        init();
    }


    protected void init() {
        dialog = new Dialog(context, R.style.dialog_style);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_penalize, null);
        ButterKnife.bind(this, view);
        window = dialog.getWindow(); // 得到对话框
        window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        mList.addAll(Arrays.asList(results));
        mAdapter = new PenalizeResultAdapter(mList);
        GridLayoutManager layoutManager = new GridLayoutManager(context, testTimes);
        rvPenalize.setLayoutManager(layoutManager);
        rvPenalize.setAdapter(mAdapter);
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                mAdapter.setClick(i);
                selectPosition = i;
            }
        });
    }


    @OnClick({R.id.turn_last, R.id.turn_next, R.id.view_txt_cancel, R.id.view_txt_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.turn_last:
                turnNext.setVisibility(View.VISIBLE);
                turnLast.setVisibility(View.INVISIBLE);
                if (null == lastStudent) {
                    setDialogDismiss("未找到考生");
                    return;
                }
                mList.clear();
                mList.addAll(Arrays.asList(lastResult));//上一个
                setSelect(lastResult);
                setStuInfo(lastStudent);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.turn_next:
                turnNext.setVisibility(View.INVISIBLE);
                turnLast.setVisibility(View.VISIBLE);
                mList.clear();
                mList.addAll(Arrays.asList(results));//当前
                setSelect(results);
                setStuInfo(student);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.view_txt_cancel:
                if (resultState == RoundResult.RESULT_STATE_FOUL) {
                    EventBus.getDefault().post(new BaseEvent(EventConfigs.FOUL_DIALOG_MISS));
                }
                dismissDialog();
                break;
            case R.id.view_txt_confirm:
                if (mAdapter.getClick() == -1) {
                    ToastUtils.showShort("请先选择更改轮次成绩");
                    return;
                }
                if (state == 0) {
                    updateResult(lastStudent, resultState);
                } else {
                    if (turnLast.getVisibility() == View.VISIBLE) {
                        updateResult(student, resultState);
                    } else {
                        if (lastStudent == null) {
                            updateResult(student, resultState);
                        } else {
                            updateResult(lastStudent, resultState);
                        }

                    }
                }

                dismissDialog();
                break;
        }
    }

    public void showDialog(int tip) {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
        tvTitle.setText(title[tip]);
        switch (tip) {
            case 0:
                resultState = RoundResult.RESULT_STATE_FOUL;
                break;
            case 1:
                resultState = RoundResult.RESULT_STATE_BACK;
                break;
            case 2:
                resultState = RoundResult.RESULT_STATE_WAIVE;
                break;
            case 3:
                resultState = RoundResult.RESULT_STATE_NORMAL;
                break;
        }
        if (lastStudent == null && student == null) {
            setDialogDismiss("未找到考生");
        }
        if (lastStudent == null) {
            turnLast.setVisibility(View.INVISIBLE);
        }
        if (selectPosition != -1) {
            mAdapter.setClick(selectPosition);
            mAdapter.notifyItemChanged(selectPosition);
        }
    }

    public void setData(int state, Student student, String[] result, Student lastStudent, String[] lastResult) {
        this.lastResult = lastResult;
        this.results = result;
        this.student = student;
        this.lastStudent = lastStudent;
        this.state = state;
        if (getGroupId() != -1) {
            group = DBManager.getInstance().queryGroupById(getGroupId());
            groupItem = DBManager.getInstance().getItemStuGroupItem(TestConfigs.getCurrentItemCode(), student.getStudentCode());

        }

        switch (state) {
            case 0:
                if (null == lastStudent) {
                    setDialogDismiss("未找到考生");
                } else {
                    mList.clear();
                    mList.addAll(Arrays.asList(lastResult));//上一个
                    setSelect(lastResult);
                    setStuInfo(lastStudent);
                }
                mAdapter.notifyDataSetChanged();
                break;
            case 1:
                if (groupId == -1) {//个人模式
                    if (resultState == RoundResult.RESULT_STATE_NORMAL) {
                        if (lastStudent != null) {
                            setStuInfo(lastStudent);
                            mList.clear();
                            mList.addAll(Arrays.asList(lastResult));//上一个
                            setSelect(lastResult);
                        } else {
                            setDialogDismiss("未找到考生");
                        }
                    } else {
                        turnLast.setVisibility(View.VISIBLE);
                        if (student != null) {
                            setStuInfo(student);
                            mList.clear();
                            mList.addAll(Arrays.asList(result));//当前
                            setSelect(result);
                        }
                    }
                } else {//分组模式
                    if (lastStudent != null) {
                        turnLast.setVisibility(View.VISIBLE);
                    }
                    if (student != null) {
                        setStuInfo(student);
                        mList.clear();
                        mList.addAll(Arrays.asList(result));//当前
                        setSelect(result);
                    }
                }

                mAdapter.notifyDataSetChanged();
                break;
            case 2:
                if (student != null) {
                    setStuInfo(student);
                    mList.clear();
                    mList.addAll(Arrays.asList(result));//当前
                    selectPosition = -1;
                    setSelect(result);
                    if (selectPosition == -1) {
                        setDialogDismiss("成绩不能为空");
                    }
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }

    }

    private void setSelect(String[] res) {
        for (int i = 0; i < res.length; i++) {
            if (!TextUtils.isEmpty(res[i])) {
                selectPosition = i;
            }
        }
    }

    private void setStuInfo(Student student) {
        txtStuCode.setText(student.getStudentCode());
        txtStuName.setText(student.getStudentName());
    }

    private void setDialogDismiss(String title) {
        tvNoStudent.setText(title);
        llContent.setVisibility(View.GONE);
        tvNoStudent.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissDialog();
            }
        }, 2000);
    }

    private void dismissDialog() {
        llContent.setVisibility(View.VISIBLE);
        tvNoStudent.setVisibility(View.GONE);
        turnLast.setVisibility(View.GONE);
        turnNext.setVisibility(View.GONE);
        this.student = null;
        resultState = -1;
        groupId = -1;
        selectPosition = -1;
        txtStuCode.setText("");
        txtStuName.setText("");
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

    }


    /**
     * 更新成绩状态
     *
     * @param queryStudent
     * @param resultState
     */
    private void updateResult(Student queryStudent, int resultState) {
        if (null == queryStudent) {
            return;
        }

//        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(queryStudent.getStudentCode());
//        List<RoundResult> roundResultList = DBManager.getInstance().queryFinallyRountScoreByExamTypeList(student.getStudentCode(), studentItem.getExamType());
        List<RoundResult> roundResultList = DBManager.getInstance().queryResultsByStudentCode(queryStudent.getStudentCode());
        //如果是空值判罚应该增加一个值 包含groupId?
        int testNo = 1;

        RoundResult dbRoundResult;
        if (groupId != -1) {
            dbRoundResult = DBManager.getInstance().queryGroupRoundNoResult(queryStudent.getStudentCode(), getGroupId() + "", mAdapter.getClick() + 1);
        } else {
            if (roundResultList.size() == 0) {
                RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(queryStudent.getStudentCode());
                testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1;
            } else {
                testNo = roundResultList.get(0).getTestNo();
            }
            dbRoundResult = DBManager.getInstance().queryRoundByRoundNo(queryStudent.getStudentCode(),
                    testNo, mAdapter.getClick() + 1);
        }

        if (resultState != RoundResult.RESULT_STATE_NORMAL && dbRoundResult == null) {
            RoundResult roundResult = new RoundResult();
            SystemSetting systemSetting = SettingHelper.getSystemSetting();
            if (systemSetting.getTestPattern() == SystemSetting.PERSON_PATTERN) {
                StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(queryStudent.getStudentCode());
                roundResult.setExamType(studentItem.getExamType());
                roundResult.setScheduleNo(studentItem.getScheduleNo());
            } else {
//                GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(TestConfigs.getCurrentItemCode(), queryStudent.getStudentCode());
                roundResult.setExamType(groupItem.getExamType());
                roundResult.setScheduleNo(groupItem.getScheduleNo());
            }
            roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
            roundResult.setStudentCode(queryStudent.getStudentCode());
            roundResult.setItemCode(TestConfigs.getCurrentItemCode());
            roundResult.setMachineResult(0);
            roundResult.setTestNo(testNo);
            roundResult.setUpdateState(0);
            roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
            if (getGroupId() != -1) {
                roundResult.setGroupId(getGroupId());
            }
            roundResult.setRoundNo(mAdapter.getClick() + 1);
            roundResult.setIsLastResult(0);
            roundResult.setUpdateState(0);
            roundResult.setResult(0);
            roundResult.setResultState(resultState);
            roundResult.setTestTime(System.currentTimeMillis() + "");
            roundResult.setEndTime(System.currentTimeMillis() + "");
            if (roundResult.getRoundNo() == 1) {
                roundResult.setIsLastResult(1);
            }
            roundResultList.add(roundResult);
            results[roundResult.getRoundNo() - 1] = RoundResult.resultStateStr(roundResult.getResultState(), roundResult.getResult());
            DBManager.getInstance().insertRoundResult(roundResult);
            EventBus.getDefault().post(new BaseEvent(roundResult, EventConfigs.INSTALL_RESULT));
            LogUtils.operation("新增判罚：" + roundResult.toString());
            upload(roundResult);
        } else if (null != roundResultList && roundResultList.size() > mAdapter.getClick()) {
            RoundResult updateResult = null;
            for (RoundResult roundResult : roundResultList) {
                if (roundResult.getRoundNo() - 1 == mAdapter.getClick()) {
                    updateResult = roundResult;
                    roundResult.setResultState(resultState);
                    roundResult.setIsLastResult(0);
                    results[roundResult.getRoundNo() - 1] = RoundResult.resultStateStr(roundResult.getResultState(), roundResult.getResult());

                    DBManager.getInstance().updateRoundResult(roundResult);
                }
            }
//            roundResultList.get(mAdapter.getClick()).setResultState(resultState);
//            roundResultList.get(mAdapter.getClick()).setIsLastResult(0);
//            DBManager.getInstance().updateRoundResult(roundResultList.get(mAdapter.getClick()));
            RoundResult r = roundResultList.get(0);

            for (RoundResult result : roundResultList) {
                if (r.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                    if (result.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
                        r = result;
                    }
                } else if (result.getResultState() == RoundResult.RESULT_STATE_NORMAL && result.getResult() > r.getResult()) {
                    r = result;
                }
            }
            r.setIsLastResult(1);
            DBManager.getInstance().updateRoundResult(r);
            LogUtils.operation("判定为：" + tvTitle.getText().toString() + roundResultList.get(mAdapter.getClick()).toString());
            EventBus.getDefault().post(new BaseEvent(updateResult, EventConfigs.UPDATE_RESULT));
            upload(updateResult);
        }

        print();
    }

    private void print() {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        //是否全部测试完成
        boolean isAllTest = true;
        for (String s : results) {
            if (TextUtils.isEmpty(s)) {
                isAllTest = false;
            }
        }
        if (!isAllTest) {
            return;
        }
        DataBaseExecutor.addTask(new Runnable() {
            @Override
            public void run() {
                if (getGroupId() != -1) {
                    PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "号机  " + group.getGroupNo() + "组");
                    PrinterManager.getInstance().print("序  号:" + groupItem.getTrackNo() + "");
                } else {
                    PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "号机  ");
                }
                Student student = DBManager.getInstance().queryStudentByCode(groupItem.getStudentCode());
                PrinterManager.getInstance().print("考  号:" + student.getStudentCode() + "");
                PrinterManager.getInstance().print("姓  名:" + student.getStudentName() + "");
                for (int i = 0; i < results.length; i++) {
                    if (!TextUtils.isEmpty(results[i])) {
                        PrinterManager.getInstance().print(String.format("第%1$d次：", i + 1) + results[i] + "");
                    } else {
                        PrinterManager.getInstance().print(String.format("第%1$d次：", i + 1) + "");
                    }
                }
                PrinterManager.getInstance().print("打印时间:" + TestConfigs.df.format(Calendar.getInstance().getTime()) + "");
                PrinterManager.getInstance().print(" \n");
            }
        });

    }

    private void upload(RoundResult roundResult) {
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort("自动上传成绩需下载更新项目信息");
            return;
        }
        if (SettingHelper.getSystemSetting().isRtUpload()) {
            List<RoundResult> roundResultList = new ArrayList<>();
            roundResultList.add(roundResult);
            UploadResults uploadResults;
            if (getGroupId() != -1) {
                Group group = DBManager.getInstance().queryGroupById(getGroupId());
                uploadResults = new UploadResults(roundResult.getScheduleNo()
                        , TestConfigs.getCurrentItemCode(), roundResult.getStudentCode()
                        , "1", group, RoundResultBean.beanCope(roundResultList, group));
            } else {
                uploadResults = new UploadResults(roundResult.getScheduleNo()
                        , TestConfigs.getCurrentItemCode(), roundResult.getStudentCode()
                        , "1", null, RoundResultBean.beanCope(roundResultList));
            }

            Intent serverIntent = new Intent(context, UploadService.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(UploadResults.BEAN_KEY, uploadResults);
            serverIntent.putExtras(bundle);
            context.startService(serverIntent);
        }

    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public int getSelectPosition() {
        return selectPosition;
    }

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
    }
}
