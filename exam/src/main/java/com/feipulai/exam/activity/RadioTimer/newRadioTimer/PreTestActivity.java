package com.feipulai.exam.activity.RadioTimer.newRadioTimer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.RadioTimer.RunTimerSetting;
import com.feipulai.exam.activity.RadioTimer.RunTimerSettingActivity;
import com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.NewRadioPairActivity;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.adapter.RunNumberAdapter;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.RunStudent;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.view.StuSearchEditText;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class PreTestActivity extends BaseCheckActivity {

    @BindView(R.id.img_portrait)
    ImageView imgPortrait;
    @BindView(R.id.txt_stu_code)
    TextView txtStuCode;
    @BindView(R.id.txt_stu_name)
    TextView txtStuName;
    @BindView(R.id.txt_stu_sex)
    TextView txtStuSex;
    @BindView(R.id.txt_stu_result)
    TextView txtStuResult;
    @BindView(R.id.et_input_text)
    StuSearchEditText etInputText;
    @BindView(R.id.btn_scan)
    TextView btnScan;
    @BindView(R.id.img_AFR)
    ImageView imgAFR;
    @BindView(R.id.tv_num)
    TextView tvNum;
    @BindView(R.id.tv_stuCode)
    TextView tvStuCode;
    @BindView(R.id.tv_stuName)
    TextView tvStuName;
    @BindView(R.id.tv_stuSex)
    TextView tvStuSex;
    @BindView(R.id.tv_stuItem)
    TextView tvStuItem;
    @BindView(R.id.tv_stuMark)
    TextView tvStuMark;
    @BindView(R.id.tv_stuDelete)
    TextView tvStuDelete;
    @BindView(R.id.rv_timer)
    RecyclerView rvTimer;
    @BindView(R.id.btn_start)
    TextView btnStart;
    @BindView(R.id.btn_led)
    TextView btnLed;
    @BindView(R.id.btn_device_pair)
    TextView btnPair;
    @BindView(R.id.rl_bottom)
    RelativeLayout rlBottom;
    @BindView(R.id.lv_results)
    ListView lvResults;
    @BindView(R.id.frame_camera)
    FrameLayout frameCamera;
    private RunNumberAdapter mAdapter;
    private List<RunStudent> mList = new ArrayList<>();
    private RunTimerSetting runTimerSetting;
    private int runNum;
    private int testNo = 1;
    private int maxTestTimes;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_pre_test;
    }

    @Override
    public void setRoundNo(Student student, int roundNo) {

    }
    @Override
    protected void initData() {
        getSetting();
        for (int i = 0; i < runNum; i++) {
            RunStudent runStudent = new RunStudent();
            runStudent.setResultList(new ArrayList<RunStudent.WaitResult>());
            mList.add(runStudent);
        }
        mAdapter = new RunNumberAdapter(mList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvTimer.setLayoutManager(layoutManager);
        rvTimer.setAdapter(mAdapter);
        btnStart.setSelected(true);
        btnLed.setSelected(true);
        btnPair.setSelected(true);
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

                deleteDialog(position);

            }
        });
        etInputText.setData(lvResults, this);
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        switch (baseEvent.getTagInt()) {
            case EventConfigs.UPDATE_TEST_COUNT:
            getSetting();
            mList.clear();
            for (int i = 0; i < runNum; i++) {
                RunStudent runStudent = new RunStudent();
                runStudent.setResultList(new ArrayList<RunStudent.WaitResult>());
                mList.add(runStudent);
            }
            mAdapter.notifyDataSetChanged();
            break;

        }
    }

    /**
     * 设置
     */
    private void getSetting() {
        runTimerSetting = SharedPrefsUtil.loadFormSource(this, RunTimerSetting.class);
        if (null == runTimerSetting) {
            runTimerSetting = new RunTimerSetting();
        }
        Logger.i("runTimerSetting:" + runTimerSetting.toString());
        //跑道数量
        runNum = Integer.parseInt(runTimerSetting.getRunNum());
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            maxTestTimes = TestConfigs.sCurrentItem.getTestNum();
        } else {
            maxTestTimes = runTimerSetting.getTestTimes();
        }
    }

    /**
     * 删除考生
     *
     * @param position
     */
    private void deleteDialog(final int position) {
        new AlertDialog.Builder(this).setMessage("是否确定删除此考生?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mList.get(position) != null) {
                            mList.get(position).setStudent(null);
                            mAdapter.notifyDataSetChanged();
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", null).show();
    }

    @Override
    public void onCheckIn(Student student) {
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        List<RoundResult> roundResultList = DBManager.getInstance().queryFinallyRountScoreByExamTypeList(student.getStudentCode(), studentItem.getExamType());
        //保存成绩，并测试轮次大于测试轮次次数
        if (student != null)
            LogUtils.operation("运动计时检入到学生:" + student.toString());
        if (studentItem != null)
            LogUtils.operation("运动计时检入到学生StudentItem:" + studentItem.toString());
        if (roundResultList != null)
            LogUtils.operation("运动计时检入到学生成绩:" + roundResultList.size() + "----" + roundResultList.toString());
        if (roundResultList != null && roundResultList.size() >= maxTestTimes) {
            //已测试，不重测
//            roundNo = roundResult.getRoundNo();
//            selectTestDialog(student);
            toastSpeak("该考生所有轮次已全部测试完成");
            LogUtils.operation("运动计时已考完:stuCode=" + student.getStudentCode());
            return;
        }

        //是否有成绩，没有成绩查底该项目是否有成绩，没有成绩测试次数为1，有成绩测试次数+1
        if (roundResultList != null && roundResultList.size() == 0) {
            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
            testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1;
        } else {
            testNo = roundResultList.get(0).getTestNo();
        }
        if (isExistStudent(student)) {
            toastSpeak("该考生已存在");
            return;
        }
        addStudent(student);
        Logger.i("runTimer:" + studentItem.toString());
    }

    /**
     * 判断 学生是否已存在
     *
     * @param student
     * @return
     */
    private boolean isExistStudent(Student student) {
        for (RunStudent runStudent : mList) {
            if (runStudent.getStudent() != null && student.getStudentCode().equals(runStudent.getStudent().getStudentCode())) {
                return true;
            }
        }
        return false;
    }

    private void addStudent(Student student) {
        LogUtils.operation("添加学生信息:" + student.toString());
        for (int i = 0; i < runNum; i++) {
            if (mList.get(i).getStudent() == null) {
                mList.get(i).setStudent(student);
                break;
            }
        }
        updateStuInfo(student);
        mAdapter.notifyDataSetChanged();
        showLedReady(mList, false);
    }

    /**
     * 显示道次准备
     */
    private void showLedReady(List<RunStudent> runs, boolean ready) {
        if (runs.size() < 0)
            return;
//        disposeManager.showReady(runs, ready);
    }

    private void updateStuInfo(Student student) {
        txtStuCode.setText(student.getStudentCode());
        txtStuName.setText(student.getStudentName());
        txtStuSex.setText(student.getSex() == 0 ? "男" : "女");
        Glide.with(imgPortrait.getContext()).load(MyApplication.PATH_IMAGE + student.getStudentCode() + ".jpg")
                .error(R.mipmap.icon_head_photo).placeholder(R.mipmap.icon_head_photo).into(imgPortrait);
    }

    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机";
        } else {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title).addRightText("项目设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        });
    }

    private void gotoItemSetting() {
        startActivity(new Intent(this, RunTimerSettingActivity.class));
    }

    @OnClick({R.id.btn_start, R.id.btn_led, R.id.btn_device_pair,R.id.img_AFR})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                LogUtils.operation("运动计时点击了进入测试");
                if (mList.get(0).getStudent() == null) {
                    ToastUtils.showShort("请先添加学生");
                    return;
                }
                Intent intent = new Intent(this,NewRadioTestActivity.class);
                intent.putExtra("runStudent", (Serializable) mList);
                startActivity(intent);
                break;
            case R.id.btn_led:
                LogUtils.operation("运动计时点击了外接屏幕");
                startActivity(new Intent(this, LEDSettingActivity.class));
                break;
            case R.id.btn_device_pair:
                startActivity(new Intent(this, NewRadioPairActivity.class));
                break;
            case R.id.img_AFR:
                showAFR();
                break;
        }
    }

    @Override
    public void compareStu(final Student student) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (student == null) {
                    InteractUtils.toastSpeak(PreTestActivity.this, "该考生不存在");
                    return;
                }else{
                    afrFrameLayout.setVisibility(View.GONE);
                }
                StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
                if (studentItem == null) {
                    InteractUtils.toastSpeak(PreTestActivity.this, "无此项目");
                    return;
                }
                // 可以直接检录
                checkInUIThread(student,studentItem);
            }
        });


    }
}
