package com.feipulai.host.activity.radio_timer.newRadioTimer;

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
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.led.RunLEDManager;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseCheckActivity;
import com.feipulai.host.activity.radio_timer.RunTimerSetting;
import com.feipulai.host.activity.radio_timer.RunTimerSettingActivity;
import com.feipulai.host.activity.radio_timer.newRadioTimer.pair.NewRadioPairActivity;
import com.feipulai.host.activity.setting.LEDSettingActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.adapter.RunNumberAdapter;
import com.feipulai.host.bean.RunStudent;
import com.feipulai.host.config.BaseEvent;
import com.feipulai.host.config.EventConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.entity.StudentItem;
import com.feipulai.host.view.StuSearchEditText;
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
    protected void initData() {
        super.initData();
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
    protected void onResume() {
        super.onResume();
        initLed();
    }

    private void initLed() {
        RunLEDManager runLEDManager = null;
        LEDManager mLEDManager = null;
        int hostId = SettingHelper.getSystemSetting().getHostId();
        String title = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode())
                + " " + hostId;
        int flag = 0;
        if (SettingHelper.getSystemSetting().getRadioLed() == 0) {
            runLEDManager = new RunLEDManager();
            flag = 0;
        } else {
            mLEDManager = new LEDManager();
            flag = 1;
        }
        if (flag == 0) {
            runLEDManager.resetLEDScreen(hostId, title);
        } else {
            if (SettingHelper.getSystemSetting().getLedVersion() == 0) {
                mLEDManager.showSubsetString(hostId, 1, title, 0, true, false, LEDManager.MIDDLE);
                mLEDManager.showSubsetString(hostId, 1, "???????????????", 3, 3, false, true);
            } else {
                mLEDManager.showString(hostId, title, 0, true, false, LEDManager.MIDDLE);
                mLEDManager.showString(hostId, "???????????????", 3, 3, false, true);
            }
        }
    }

    @Override
    public int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        switch (baseEvent.getTagInt()) {
            case EventConfigs.UPDATE_TEST_COUNT:
                int tmp = runNum;
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
     * ??????
     */
    private void getSetting() {
        runTimerSetting = SharedPrefsUtil.loadFormSource(this, RunTimerSetting.class);
        if (null == runTimerSetting) {
            runTimerSetting = new RunTimerSetting();
        }
        Logger.i("runTimerSetting:" + runTimerSetting.toString());
        //????????????
        runNum = Integer.parseInt(runTimerSetting.getRunNum());
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            maxTestTimes = TestConfigs.sCurrentItem.getTestNum();
        } else {
            maxTestTimes = runTimerSetting.getTestTimes();
        }
    }

    /**
     * ????????????
     *
     * @param position
     */
    private void deleteDialog(final int position) {
        new AlertDialog.Builder(this).setMessage("????????????????????????????")
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mList.get(position) != null) {
                            mList.get(position).setStudent(null);
                            mAdapter.notifyDataSetChanged();
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton("??????", null).show();
    }

    @Override
    public void onCheckIn(Student student) {
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        //??????????????????????????????????????????????????????
        if (student != null)
            LogUtils.operation("???????????????????????????:" + student.toString());
        if (studentItem != null)
            LogUtils.operation("???????????????????????????StudentItem:" + studentItem.toString());
        if (isExistStudent(student)) {
            toastSpeak("??????????????????");
            return;
        }
        addStudent(student);
        Logger.i("runTimer:" + studentItem.toString());
    }

    /**
     * ?????? ?????????????????????
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
        LogUtils.operation("??????????????????:" + student.toString());
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
     * ??????????????????
     */
    private void showLedReady(List<RunStudent> runs, boolean ready) {
        if (runs.size() < 0)
            return;
//        disposeManager.showReady(runs, ready);
    }

    private void updateStuInfo(Student student) {
        txtStuCode.setText(student.getStudentCode());
        txtStuName.setText(student.getStudentName());
        txtStuSex.setText(student.getSex() == 0 ? "???" : "???");
        Glide.with(imgPortrait.getContext()).load(MyApplication.PATH_IMAGE + student.getStudentCode() + ".jpg")
                .error(R.mipmap.icon_head_photo).placeholder(R.mipmap.icon_head_photo).into(imgPortrait);
    }

    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "??????";
        } else {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "??????-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title).addRightText("????????????", new View.OnClickListener() {
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

    @OnClick({R.id.btn_start, R.id.btn_led, R.id.btn_device_pair, R.id.img_AFR})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                LogUtils.operation("?????????????????????????????????");
                if (mList.get(0).getStudent() == null) {
                    ToastUtils.showShort("??????????????????");
                    return;
                }
                Intent intent = new Intent(this, NewRadioTestActivity.class);
                intent.putExtra("runStudent", (Serializable) mList);
                startActivity(intent);
                break;
            case R.id.btn_led:
                LogUtils.operation("?????????????????????????????????");
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


}
