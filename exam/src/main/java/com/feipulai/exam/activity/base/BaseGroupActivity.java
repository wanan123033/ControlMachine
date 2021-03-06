package com.feipulai.exam.activity.base;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.led.RunLEDManager;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.MyTcpService;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.ServiceTcpBean;
import com.feipulai.exam.activity.RadioTimer.RunTimerSetting;
import com.feipulai.exam.activity.RadioTimer.newRadioTimer.NewRadioGroupActivity;
import com.feipulai.exam.activity.basketball.BasketBallSelectActivity;
import com.feipulai.exam.activity.basketball.BasketBallSetting;
import com.feipulai.exam.activity.basketball.DribbleShootGroupActivity;
import com.feipulai.exam.activity.basketball.ShootSetting;
import com.feipulai.exam.activity.basketball.ShootSettingActivity;
import com.feipulai.exam.activity.basketball.motion.BaskBallMotionSettingActivity;
import com.feipulai.exam.activity.basketball.motion.BasketBallMotionGroupActivity;
import com.feipulai.exam.activity.basketball.reentry.BallReentryGroupActivity;
import com.feipulai.exam.activity.basketball.reentry.BallReentrySettingActivity;
import com.feipulai.exam.activity.footBall.FootBallSetting;
import com.feipulai.exam.activity.footBall.motion.FootBallMotionGroupActivity;
import com.feipulai.exam.activity.footBall.motion.FootballMotionSettingActivity;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.check.CheckUtils;
import com.feipulai.exam.activity.jump_rope.fragment.IndividualCheckFragment;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.medicineBall.MedicineBallSetting;
import com.feipulai.exam.activity.medicineBall.more_device.BallGroupMoreActivity;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.pushUp.PushUpGroupActivity;
import com.feipulai.exam.activity.pushUp.PushUpSetting;
import com.feipulai.exam.activity.pushUp.distance.PushUpDistanceTestActivity;
import com.feipulai.exam.activity.sargent_jump.SargentSetting;
import com.feipulai.exam.activity.sargent_jump.more_device.SargentTestGroupActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.sitreach.SitReachSetting;
import com.feipulai.exam.activity.sitreach.more_device.SitReachMoreGroupActivity;
import com.feipulai.exam.activity.situp.newSitUp.SitUpArmCheckActivity;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.activity.standjump.StandJumpSetting;
import com.feipulai.exam.activity.standjump.more.StandJumpGroupMoreActivity;
import com.feipulai.exam.activity.volleyball.VolleyBallGroupActivity;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;
import com.feipulai.exam.adapter.BaseGroupAdapter;
import com.feipulai.exam.adapter.GroupAdapter;
import com.feipulai.exam.adapter.ResultsAdapter;
import com.feipulai.exam.adapter.ScheduleAdapter;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.StudentCache;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.view.AdvancedDialog;
import com.feipulai.exam.view.CommonPopupWindow;
import com.orhanobut.logger.utils.LogUtils;
import com.zyyoona7.popup.EasyPopup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

import static com.feipulai.exam.config.SharedPrefsConfigs.MACHINE_SERVER_PORT;
import static com.feipulai.exam.config.SharedPrefsConfigs.MIDDLE_RACE;

public class BaseGroupActivity extends BaseTitleActivity {

    @BindView(R.id.sp_schedule)
    Spinner spSchedule;
    @BindView(R.id.txt_group_name)
    TextView txtGroupName;
    @BindView(R.id.img_last)
    TextView imgLast;
    @BindView(R.id.img_next)
    TextView imgNext;
    @BindView(R.id.txt_start_test)
    TextView tvStartTest;
    @BindView(R.id.rv_test_stu)
    RecyclerView rvTestStu;
    @BindView(R.id.ll_stu_detail)
    LinearLayout llStuDetail;
    @BindView(R.id.rv_results)
    RecyclerView rvResults;
    @BindView(R.id.tv_studentCode)
    TextView tvStudentCode;
    @BindView(R.id.tv_studentName)
    TextView tvStudentName;
    @BindView(R.id.tv_gender)
    TextView tvGender;
    @BindView(R.id.iv_portrait)
    ImageView imgPortrait;
    @BindView(R.id.cb_select_all)
    CheckBox cbSelectAll;
    private List<BaseStuPair> stuPairsList;// ??????????????????
    private CommonPopupWindow groupPop;

    private List<Schedule> scheduleList = new ArrayList<>();
    private ScheduleAdapter scheduleAdapter;

    private List<Group> groupList = new ArrayList<>();
    private GroupAdapter groupAdapter;

    private BaseGroupAdapter stuAdapter;
    private List<BaseStuPair> pairs = new ArrayList<>();
    private LEDManager mLEDManager;
    private RunLEDManager runLEDManager;
    private LedThread ledThread;
    private String scheduleText;
    private List<RoundResult> resultList = new ArrayList<>();
    private ResultsAdapter resultsAdapter;
    private boolean isBack;
    private SystemSetting systemSetting;
    private EasyPopup mServePop;
    private String server_Port;
    private int currentPort;
    private Intent bindIntent;
    private boolean isBind = false;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_base_group;
    }

    @Override
    protected void initData() {
        ButterKnife.bind(this);
        initView();
        if (MachineCode.machineCode == ItemDefault.CODE_ZFP && SettingHelper.getSystemSetting().getRadioLed() == 0) {
            runLEDManager = new RunLEDManager();
        } else {
            mLEDManager = new LEDManager();
        }
        server_Port = SharedPrefsUtil.getValue(this, MIDDLE_RACE, MACHINE_SERVER_PORT, "4040");
        initConnectPop();
    }

    @Nullable
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
                mServePop.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
            }
        }).addRightImage(R.mipmap.ic_serve_connect, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mServePop.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
            }
        }).addRightText("????????????", new View.OnClickListener() {
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
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ) {
            BasketBallSetting setting = SharedPrefsUtil.loadFormSource(this, BasketBallSetting.class);
            if (setting.getTestType() == 2) {
                startActivity(new Intent(this, ShootSettingActivity.class));
            } else if (setting.getTestType() == 5) {
                startActivity(new Intent(this, BallReentrySettingActivity.class));
            } else if (setting.getTestType() == 4) {
                startActivity(new Intent(this, BaskBallMotionSettingActivity.class));
            } else {
                startActivity(new Intent(this, TestConfigs.settingActivity.get(TestConfigs.sCurrentItem.getMachineCode())));
            }
        } else if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZQYQ) {
            FootBallSetting setting = SharedPrefsUtil.loadFormSource(this, FootBallSetting.class);
            if (setting.getTestType() == 3) {
                startActivity(new Intent(this, FootballMotionSettingActivity.class));
            } else {
                startActivity(new Intent(this, TestConfigs.settingActivity.get(TestConfigs.sCurrentItem.getMachineCode())));
            }
        } else {
            startActivity(new Intent(this, TestConfigs.settingActivity.get(TestConfigs.sCurrentItem.getMachineCode())));
        }

    }


    private void initView() {
        TestConfigs.baseGroupMap.clear();
        TestCache.getInstance().clear();
        rvTestStu.setLayoutManager(new LinearLayoutManager(this));
        stuPairsList = new ArrayList<>();
        stuAdapter = new BaseGroupAdapter(stuPairsList);
        systemSetting = SettingHelper.getSystemSetting();
        rvTestStu.setAdapter(stuAdapter);
        scheduleAdapter = new ScheduleAdapter(this, scheduleList);
        spSchedule.setAdapter(scheduleAdapter);
        groupAdapter = new GroupAdapter(groupList);
        groupPop = new CommonPopupWindow(this, groupAdapter);
        groupPop.setOnPopItemClickListener(new CommonPopupWindow.OnPopItemClickListener() {
            @Override
            public void itemClick(int position) {
                selectGroup(position);
            }
        });
        updateSchedules();

        stuAdapter.setItemClickListener(new BaseGroupAdapter.OnPopItemClickListener() {

            @Override
            public void itemClick(final int pos, boolean isChecked) {
                stuPairsList.get(pos).setCanTest(isChecked);
            }
        });
        stuAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, final int position) {
                if (stuPairsList.size() > 0) {
                    showStuInfo(stuPairsList.get(position).getStudent());
                }
                GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(groupList.get(groupAdapter.getTestPosition()), stuPairsList.get(position).getStudent().getStudentCode());
                List<RoundResult> results = getResults(stuPairsList.get(position).getStudent().getStudentCode());
                Log.e("TAG", results.toString());
                if (results != null && results.size() >= TestConfigs.getMaxTestCount()) {
                    if (groupItem != null && (systemSetting.isResit() || systemSetting.isAgainTest())) {
                        if (systemSetting.isResit()) {
                            ResitDialog dialog = new ResitDialog();
                            dialog.setArguments(stuPairsList.get(position).getStudent(), results, groupItem);
                            dialog.setOnIndividualCheckInListener(new ResitDialog.onClickQuitListener() {
                                @Override
                                public void onCancel() {
                                    stuPairsList.get(position).setCanTest(false);
                                    stuPairsList.get(position).setResit(false);
                                    stuPairsList.get(position).setCanCheck(false);
                                    stuAdapter.notifyItemChanged(position);
                                }

                                @Override
                                public void onCommitPattern(Student student, StudentItem studentItem, List<RoundResult> results, int roundNo) {

                                }

                                @Override
                                public void onCommitGroup(Student student, GroupItem groupItem, List<RoundResult> results, int roundNo) {
                                    stuPairsList.get(position).setTestNo(1);
                                    stuPairsList.get(position).setRoundNo(roundNo);
                                    stuPairsList.get(position).setCanTest(true);
                                    stuPairsList.get(position).setCanCheck(true);
                                    stuAdapter.notifyItemChanged(position);
                                    refreshStuBaseResult(position);

                                }
                            });
                            dialog.show(getSupportFragmentManager(), "ResitDialog");
                        }
                        if (systemSetting.isAgainTest()) {
                            AgainTestDialog dialog = new AgainTestDialog();
                            dialog.setArguments(stuPairsList.get(position).getStudent(), results, groupItem);
                            dialog.setOnIndividualCheckInListener(new ResitDialog.onClickQuitListener() {
                                @Override
                                public void onCancel() {
                                    stuPairsList.get(position).setCanTest(false);
                                    stuPairsList.get(position).setAgain(false);
                                    stuPairsList.get(position).setCanCheck(false);
                                    stuAdapter.notifyItemChanged(position);
                                }

                                @Override
                                public void onCommitPattern(Student student, StudentItem studentItem, List<RoundResult> results, int roundNo) {

                                }

                                @Override
                                public void onCommitGroup(Student student, GroupItem groupItem, List<RoundResult> results, int roundNo) {

                                    stuPairsList.get(position).setTestNo(1);
                                    stuPairsList.get(position).setRoundNo(roundNo);
                                    stuPairsList.get(position).setCanTest(true);
                                    stuPairsList.get(position).setCanCheck(true);
                                    showStuInfo(stuPairsList.get(position).getStudent());
                                    stuAdapter.notifyItemChanged(position);
                                    refreshStuBaseResult(position);
                                }
                            });
                            dialog.show(getSupportFragmentManager(), "AgainTestDialog");
                        }
                    }
                }
            }
        });


    }

    /**
     * ????????????
     */
    private void initConnectPop() {
        mServePop = EasyPopup.create()
                .setContentView(this, R.layout.pop_word_group_serve)
                .setBackgroundDimEnable(true)
                .setDimValue(0.5f)
                //??????????????????PopupWindow?????????????????????
                .setFocusAndOutsideEnable(true)
                .apply();

        Button btnStart = mServePop.findViewById(R.id.btn_start_server);

        final EditText serverPort = mServePop.findViewById(R.id.et_server_port);

        serverPort.setText(server_Port);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(serverPort.getText())) {
                    ToastUtils.showShort("??????????????????");
                    return;
                }
                currentPort = Integer.parseInt(serverPort.getText().toString());
                //??????????????????????????????
                if (!server_Port.equals(serverPort.getText().toString())) {
                    myBinder.stopWork();
                    myBinder.startWork(currentPort);
                }

                if (myTcpService != null && myTcpService.isWork) {
                    ToastUtils.showShort("?????????????????????");
                    return;
                }
                bindTcpService();
                //??????????????????????????????????????????12????????????????????????activity???????????????????????????????????????????????????????????????
//                SharedPrefsUtil.putValue(mContext, MyTcpService.SERVICE_CONNECT, MyTcpService.SERVICE_CONNECT, System.currentTimeMillis());
            }
        });
        mServePop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                SharedPrefsUtil.putValue(BaseGroupActivity.this, MIDDLE_RACE, MACHINE_SERVER_PORT, serverPort.getText().toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindService();
    }

    @Override
    public void finish() {
        super.finish();
        if (mLEDManager != null) {
            mLEDManager.link(SettingHelper
                    .getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
            String title = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()) + " " + SettingHelper.getSystemSetting().getHostId();
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), title, mLEDManager.getX(title), 0, true, false);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "???????????????", 3, 3, false, true);
            mLEDManager = null;
        } else if (runLEDManager != null) {
            runLEDManager.link(SettingHelper.getSystemSetting().getHostId());
            String title = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()) + " " + SettingHelper.getSystemSetting().getHostId();
            runLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), title, runLEDManager.getX(title), 0, true, false);
            runLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "???????????????", 3, 3, false, true);
            runLEDManager = null;
        }
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        if (baseEvent.getTagInt() == EventConfigs.UPDATE_TEST_COUNT || baseEvent.getTagInt() == EventConfigs.UPDATE_TEST_RESULT) {
            if (groupList.size() > 0) {
                updateStudents(groupList.get(groupAdapter.getTestPosition()));
            }
            if (!TextUtils.isEmpty(scheduleText)) {
                getGroupList(scheduleText);
            }

        } else if (baseEvent.getTagInt() == EventConfigs.AUTO_ADD_RESULT) {
            scoreUpload();
        } else if (baseEvent.getTagInt() == EventConfigs.STOP_GROUP_LED) {
            if (ledThread != null) {
                ledThread.interrupt();
                ledThread.finish();
                ledThread = null;
            }
        }
    }

    @OnClick({R.id.cb_select_all})
    public void onCheckedClickChanged(View view) {
        CheckBox checkBox = (CheckBox) view;
        for (BaseStuPair pair : stuPairsList) {
            pair.setCanTest(checkBox.isChecked());
        }
        if (checkBox.isChecked()) {
            getCanTestStates(stuPairsList);
        }
        stuAdapter.notifyDataSetChanged();
    }

    /**
     * ????????????
     */
    private void updateSchedules() {
        scheduleList.clear();
        List<Schedule> dbSchedule = DBManager.getInstance().getAllSchedules();
        scheduleList.addAll(dbSchedule);
        if (scheduleList.size() > 0 && TextUtils.equals(scheduleList.get(0).getScheduleNo(), "-1")) {
            scheduleList.remove(0);
        }
        scheduleAdapter.notifyDataSetChanged();
        if (scheduleList != null && scheduleList.size() > 0) {
            scheduleText = scheduleList.get(0).getScheduleNo();
            getGroupList(scheduleText);
            txtGroupName.setText("?????????????????????");
        }
//        LogUtils.operation("??????????????????Schedules(??????):" + scheduleList.toString());
    }

    /**
     * ??????????????????
     */
    private void getGroupList(String scheduleNo) {
        cbSelectAll.setChecked(true);
        groupList.clear();
        List<Group> dbGroupList = DBManager.getInstance().getGroupByScheduleNo(scheduleNo);
        groupList.addAll(dbGroupList);
        groupAdapter.notifyDataSetChanged();
//        LogUtils.operation("??????????????????groupList(????????????):" + groupList.toString());
    }

    // ????????????
    private void selectGroup(int position) {
        StringBuilder sb = new StringBuilder();
        sb.append(groupList.get(position).getGroupType() == Group.MALE ? "??????" :
                (groupList.get(position).getGroupType() == Group.FEMALE ? "??????" : "????????????"))
                .append(groupList.get(position).getSortName())
                .append(String.format("???%1$d???", groupList.get(position).getGroupNo()));
        txtGroupName.setText(sb.toString());
        groupAdapter.setTestPosition(position);
        updateStudents(groupList.get(position));
        LogUtils.all("????????????????????????:" + groupList.get(position).toString());
    }

    /**
     * ??????????????????
     */
    private void updateStudents(Group group) {
        stuPairsList.clear();
        List<Map<String, Object>> dbStudentList = DBManager.getInstance().getStudenByStuItemAndGroup(group);
        for (Map<String, Object> map : dbStudentList) {
            BaseStuPair baseStuPair = new BaseStuPair((Student) map.get("student"), new BaseDeviceState(BaseDeviceState.STATE_ERROR));
            baseStuPair.setTrackNo((Integer) map.get("trackNo"));
            stuPairsList.add(baseStuPair);
        }
        getCanTestStates(stuPairsList);
        stuAdapter.notifyDataSetChanged();
        showLed(stuPairsList);
        if (stuPairsList.size() > 0) {
            LogUtils.all("??????????????????????????????:" + stuPairsList.toString());
            showStuInfo(stuPairsList.get(0).getStudent());
        }

    }

    /**
     * ????????????????????????
     *
     * @param student
     */
    private void showStuInfo(Student student) {
        tvStudentCode.setText(student.getStudentCode());
        tvStudentName.setText(student.getStudentName());
        tvGender.setText(student.getSex() == 0 ? "???" : "???");
//        if (student.getBitmapPortrait() != null) {
//            imgPortrait.setImageBitmap(student.getBitmapPortrait());
//        } else {
//            imgPortrait.setImageResource(R.mipmap.icon_head_photo);
//        }
        Glide.with(imgPortrait.getContext()).load(MyApplication.PATH_IMAGE + student.getStudentCode() + ".jpg")
                .error(R.mipmap.icon_head_photo).placeholder(R.mipmap.icon_head_photo).into(imgPortrait);
        resultList = getResults(student.getStudentCode());
//        InteractUtils.showStuInfo(llStuDetail, student, results);
        resultsAdapter = new ResultsAdapter(resultList);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(resultsAdapter);
        resultsAdapter.notifyDataSetChanged();
    }

    // ????????????????????????
    private List<RoundResult> getResults(String stuCode) {
        return DBManager.getInstance().queryGroupRound(stuCode,
                groupList.get(groupAdapter.getTestPosition()).getId() + "");
    }

    private void refreshStuBaseResult(int position) {
        RoundResult result = DBManager.getInstance().queryGroupBestScore(
                stuPairsList.get(position).getStudent().getStudentCode(), groupList.get(groupAdapter.getTestPosition()).getId());
        if (result != null) {
            stuPairsList.get(position).setResultState(result.getResultState());
            stuPairsList.get(position).setResult(result.getResult());
            if (result.getResultState() != RoundResult.RESULT_STATE_FOUL) {

                if (stuPairsList.get(position).getStudent().getSex() == 0 && TestConfigs.getFullSkip() != null) {//??????????????????
                    if (machineCode == ItemDefault.CODE_ZQYQ || machineCode == ItemDefault.CODE_LQYQ) {
                        if (result.getResult() <= TestConfigs.getFullSkip()[0]) {
                            stuPairsList.get(position).setCanTest(false);
                            stuPairsList.get(position).setCanCheck(false);
                        }
                    } else {
                        if (result.getResult() >= TestConfigs.getFullSkip()[0]) {
                            stuPairsList.get(position).setCanTest(false);
                            stuPairsList.get(position).setCanCheck(false);
                        }
                    }


                }
                if (stuPairsList.get(position).getStudent().getSex() == 1 && TestConfigs.getFullSkip() != null && result.getResult() >= TestConfigs.getFullSkip()[1]) {//??????????????????

                    if (machineCode == ItemDefault.CODE_ZQYQ || machineCode == ItemDefault.CODE_LQYQ) {
                        if (result.getResult() <= TestConfigs.getFullSkip()[1]) {
                            stuPairsList.get(position).setCanTest(false);
                            stuPairsList.get(position).setCanCheck(false);
                        }
                    } else {
                        if (result.getResult() >= TestConfigs.getFullSkip()[1]) {
                            stuPairsList.get(position).setCanTest(false);
                            stuPairsList.get(position).setCanCheck(false);
                        }
                    }
                }
            }

        } else {
            stuPairsList.get(position).setNotBest(true);
        }
        stuAdapter.notifyDataSetChanged();
    }

    /**
     * ????????????????????????
     *
     * @param stuPairs
     */
    private void getCanTestStates(List<BaseStuPair> stuPairs) {

        for (int i = 0; i < stuPairs.size(); i++) {
            Student student = stuPairs.get(i).getStudent();
            List<RoundResult> results = getResults(student.getStudentCode());
            // ????????????????????????,??????????????????????????? baseGroupMap???
            TestConfigs.baseGroupMap.put(student, results);
            if (results != null && results.size() >= TestConfigs.getMaxTestCount()) { //????????????
                stuPairs.get(i).setCanTest(false);
                stuPairs.get(i).setCanCheck(false);
            } else {
                stuPairs.get(i).setCanTest(true);
                stuPairs.get(i).setCanCheck(true);
            }

            RoundResult result = DBManager.getInstance().queryGroupBestScore(
                    student.getStudentCode(), groupList.get(groupAdapter.getTestPosition()).getId());
            if (result != null) {
                stuPairs.get(i).setResultState(result.getResultState());
                stuPairs.get(i).setResult(result.getResult());
                if (result.getResultState() != RoundResult.RESULT_STATE_FOUL) {
                    if (student.getSex() == 0 && TestConfigs.getFullSkip() != null) {//??????????????????
                        if (machineCode == ItemDefault.CODE_ZQYQ || machineCode == ItemDefault.CODE_LQYQ) {
                            if (result.getResult() <= TestConfigs.getFullSkip()[0]) {
                                stuPairs.get(i).setCanTest(false);
                                stuPairs.get(i).setCanCheck(false);
                            }
                        } else {
                            if (result.getResult() >= TestConfigs.getFullSkip()[0]) {
                                stuPairs.get(i).setCanTest(false);
                                stuPairs.get(i).setCanCheck(false);
                            }
                        }


                    }
                    if (student.getSex() == 1 && TestConfigs.getFullSkip() != null) {//??????????????????

                        if (machineCode == ItemDefault.CODE_ZQYQ || machineCode == ItemDefault.CODE_LQYQ) {
                            if (result.getResult() <= TestConfigs.getFullSkip()[1]) {
                                stuPairs.get(i).setCanTest(false);
                                stuPairs.get(i).setCanCheck(false);
                            }
                        } else {
                            if (result.getResult() >= TestConfigs.getFullSkip()[1]) {
                                stuPairs.get(i).setCanTest(false);
                                stuPairs.get(i).setCanCheck(false);
                            }
                        }
                    }
                }

            } else {
                stuPairs.get(i).setNotBest(true);
            }
        }

    }

    /**
     * ????????????????????????
     *
     * @return
     */
//    public int[] getFullSkip() {
//        int code = MachineCode.machineCode;
//        int[] full = null;
//        switch (code) {
//            case ItemDefault.CODE_ZWTQQ:
//                SitReachSetting sitReachSetting = SharedPrefsUtil.loadFormSource(this, SitReachSetting.class);
//                if (sitReachSetting.isFullReturn()) {
//                    full = new int[2];
//                    full[0] = (int) (sitReachSetting.getManFull() * 10);
//                    full[1] = (int) (sitReachSetting.getWomenFull() * 10);
//                }
//                break;
//            case ItemDefault.CODE_LDTY:
//                StandJumpSetting jumpSetting = SharedPrefsUtil.loadFormSource(this, StandJumpSetting.class);
//                if (jumpSetting.isFullReturn()) {
//                    full = new int[2];
//                    full[0] = jumpSetting.getManFull() * 10;
//                    full[1] = jumpSetting.getWomenFull() * 10;
//                }
//
//                break;
//            case ItemDefault.CODE_HWSXQ:
//                MedicineBallSetting medicineBallSetting = SharedPrefsUtil.loadFormSource(this, MedicineBallSetting.class);
//                if (medicineBallSetting.isFullReturn()) {
//                    full = new int[2];
//                    if (!TextUtils.isEmpty(medicineBallSetting.getMaleFull())) {
//                        full[0] = Integer.parseInt(medicineBallSetting.getMaleFull()) * 10;
//                    }
//                    if (!TextUtils.isEmpty(medicineBallSetting.getFemaleFull())) {
//                        full[1] = Integer.parseInt(medicineBallSetting.getFemaleFull()) * 10;
//                    }
//                }
//                break;
//            case ItemDefault.CODE_MG:
//                SargentSetting sargentSetting = SharedPrefsUtil.loadFormSource(this, SargentSetting.class);
//                if (sargentSetting.isFullReturn()) {
//                    full = new int[2];
//                    if (!TextUtils.isEmpty(sargentSetting.getMaleFull())) {
//                        full[0] = Integer.parseInt(sargentSetting.getMaleFull()) * 10;
//                    }
//                    if (!TextUtils.isEmpty(sargentSetting.getFemaleFull())) {
//                        full[1] = Integer.parseInt(sargentSetting.getFemaleFull()) * 10;
//                    }
//                }
//                break;
//            case ItemDefault.CODE_PQ:
//                VolleyBallSetting volleyBallSetting = SharedPrefsUtil.loadFormSource(this, VolleyBallSetting.class);
//                if (volleyBallSetting.isFullSkip()) {
//                    full = new int[2];
//                    full[0] = volleyBallSetting.getMaleFullScore();
//                    full[1] = volleyBallSetting.getFemaleFullScore();
//                }
//                break;
//
//        }
//        return full;
//    }
    @OnItemSelected({R.id.sp_schedule})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {

            case R.id.sp_schedule:
                txtGroupName.setText("?????????????????????");
                stuPairsList.clear();
                stuAdapter.notifyDataSetChanged();
                scheduleText = scheduleList.get(position).getScheduleNo();
                getGroupList(scheduleText);
                break;

        }
    }

    @SuppressWarnings("unchecked")
    @OnClick({R.id.txt_group_name, R.id.img_last, R.id.img_next, R.id.txt_start_test, R.id.txt_print, R.id.score_upload})
// R.id.tv_project_setting,
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_group_name:
                groupPop.updateAdapter(groupList);
                groupPop.showPopOrDismiss();

                break;
            case R.id.img_last:
                TestConfigs.baseGroupMap.clear();
                TestCache.getInstance().clear();
                if (groupList.size() <= groupAdapter.getTestPosition())
                    return;
                if (groupAdapter.getTestPosition() > 0) {
                    selectGroup(groupAdapter.getTestPosition() - 1);
                }
                break;
            case R.id.img_next:
                TestConfigs.baseGroupMap.clear();
                TestCache.getInstance().clear();
                if (groupList.size() <= groupAdapter.getTestPosition())
                    return;
                if (groupList.size() > 0 && groupAdapter.getTestPosition() != groupList.size() - 1) {
                    selectGroup(groupAdapter.getTestPosition() + 1);
                }
                break;
//            case R.id.tv_project_setting:
//                startActivity(new Intent(this, TestConfigs.settingActivity.get(TestConfigs.sCurrentItem.getMachineCode())));
//                break;
            case R.id.txt_start_test:
                TestCache.getInstance().clear();
                TestConfigs.baseGroupMap.clear();
                if (groupAdapter.getTestPosition() == -1) {
                    toastSpeak("??????????????????");
                    return;
                }
                if (groupList.size() <= groupAdapter.getTestPosition())
                    return;
                if (systemSetting.isGroupCheck()) {
                    Intent intent = new Intent(this, BaseGroupCheckActivity.class);
                    intent.putExtra(BaseGroupCheckActivity.GROUP_INFO, groupList.get(groupAdapter.getTestPosition()));
                    startActivity(intent);
                    return;
                }
//                if (groupList.get(groupAdapter.getTestPosition()).getIsTestComplete() == 1) {
//                    ToastUtils.showShort("????????????????????????????????????");
//                    return;
//                }
                TestConfigs.baseGroupMap.put("group", groupList.get(groupAdapter.getTestPosition()));
                TestCache.getInstance().setGroup(groupList.get(groupAdapter.getTestPosition()));
                pairs.clear();
                for (BaseStuPair pair : stuPairsList) {
                    if (pair.isCanTest()) {
                        Student student = pair.getStudent();
                        List<RoundResult> results = getResults(student.getStudentCode());
                        setStuPairsData(pair, results);
                        // ????????????????????????,??????????????????????????? baseGroupMap???
                        TestConfigs.baseGroupMap.put(student, results);
                        List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRoundAll
                                (student.getStudentCode(), groupList.get(groupAdapter.getTestPosition()).getId() + "");
                        if (roundResultList.size() >= TestConfigs.getMaxTestCount()) {
                            List<Integer> rounds = new ArrayList<>();
                            for (int i = 0; i < results.size(); i++) {
                                if (results.size() > 0) {  //??????????????????
                                    int roundNo = results.get(i).getRoundNo();
                                    rounds.add(roundNo);
                                }
                            }

                            for (int j = 1; j <= TestConfigs.getMaxTestCount(); j++) {
                                if (!rounds.contains(j)) {
                                    pair.setRoundNo(j);
                                    break;
                                }
                            }
                        }


                        pairs.add(pair);
                    }
                }
                if (pairs.size() <= 0) {
                    toastSpeak("??????????????????????????????!");
                    return;
                }
                if (isAllTest()) {
                    ToastUtils.showShort("????????????????????????????????????");
                    return;
                }
                isBack = true;
                TestConfigs.baseGroupMap.put("basePairStu", pairs);
                StudentCache.getStudentCaChe().clear();
                for (int i = 0; i < pairs.size(); i++) {
                    StudentCache.getStudentCaChe().addStudent(pairs.get(i).getStudent());
                    LogUtils.operation("????????????????????????:" + pairs.get(i).getStudent().toString());
                }


//                if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_YWQZ
//                        && SharedPrefsUtil.loadFormSource(this, SitUpSetting.class).getTestType() == 1) {
//                    startActivity(new Intent(this, SitUpArmCheckActivity.class));
//                    return;
//                }
                gotoActivity();
                break;

            case R.id.txt_print:
                if (stuPairsList == null || stuPairsList.size() == 0) {
                    break;
                }
                // ????????????????????????????????????,???????????????????????????????????????,???????????????????????????(?????????????????????)
                List<Student> students = new ArrayList<>(stuPairsList.size());
                Map<Student, List<RoundResult>> results = new HashMap<>(stuPairsList.size() * 2);
                Map<Student, Integer> trackNoMap = new HashMap<>();
                for (int i = 0; i < stuPairsList.size(); i++) {
                    BaseStuPair stuPair = stuPairsList.get(i);
                    Student student = stuPair.getStudent();
                    students.add(student);
                    results.put(student, getResults(student.getStudentCode()));
                    trackNoMap.put(student, stuPair.getTrackNo());
                }
                if (SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_A4
                        || SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_CUSTOM_APP) {
                    InteractUtils.printA4Result(this, groupList.get(groupAdapter.getTestPosition()));
                } else {
                    InteractUtils.printResults(groupList.get(groupAdapter.getTestPosition()),
                            students,
                            results,
                            TestConfigs.getMaxTestCount(),
                            trackNoMap);
                }

                break;
            case R.id.score_upload:
                scoreUpload();
                break;
        }
    }

    private void gotoActivity() {
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_FWC) {
            PushUpSetting setting = SharedPrefsUtil.loadFormSource(this, PushUpSetting.class);
            if ((setting.getTestType() == PushUpSetting.WIRELESS_TYPE && setting.getDeviceSum() == 1)
                    || setting.getTestType() == PushUpSetting.WIRED_TYPE) {
                startActivity(new Intent(this, PushUpGroupActivity.class));
                return;
            }
            if (setting.getTestType() == 2) {
                startActivity(new Intent(this, PushUpDistanceTestActivity.class));
            }
        }
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_MG &&
                SharedPrefsUtil.loadFormSource(this, SargentSetting.class).getType() == 2) {
            startActivity(new Intent(this, SargentTestGroupActivity.class));
            return;
        }

        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_HWSXQ &&
                SharedPrefsUtil.loadFormSource(this, MedicineBallSetting.class).getConnectType() == 1) {
            startActivity(new Intent(this, BallGroupMoreActivity.class));
            return;
        }

        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_PQ
                && SharedPrefsUtil.loadFormSource(this, VolleyBallSetting.class).getType() == 2) {

            startActivity(new Intent(this, VolleyBallGroupActivity.class));
            return;
        }
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LDTY
                && SharedPrefsUtil.loadFormSource(this, StandJumpSetting.class).getTestType() == 1) {
            startActivity(new Intent(this, StandJumpGroupMoreActivity.class));
            return;
        }
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZWTQQ
                && SharedPrefsUtil.loadFormSource(this, SitReachSetting.class).getTestType() == 1) {
            startActivity(new Intent(this, SitReachMoreGroupActivity.class));
            return;
        }

        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ) {
            BasketBallSetting setting = SharedPrefsUtil.loadFormSource(this, BasketBallSetting.class);
            if (setting.getTestType() == 5) {
                startActivity(new Intent(this, BallReentryGroupActivity.class));
                return;
            } else if (SharedPrefsUtil.loadFormSource(this, ShootSetting.class).getTestType() == 2) {
                startActivity(new Intent(this, DribbleShootGroupActivity.class));
                return;
            } else if (SharedPrefsUtil.loadFormSource(this, ShootSetting.class).getTestType() == 4) {
                startActivity(new Intent(this, BasketBallMotionGroupActivity.class));
                return;
            }


        }

        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZQYQ) {
            FootBallSetting setting = SharedPrefsUtil.loadFormSource(this, FootBallSetting.class);
            if (setting.getTestType() == 3) {
                startActivity(new Intent(this, FootBallMotionGroupActivity.class));
                return;
            }
        }

        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZFP
                && SharedPrefsUtil.loadFormSource(this, RunTimerSetting.class).getConnectType() == 1) {
            startActivity(new Intent(this, NewRadioGroupActivity.class));
            return;
        }

        startActivity(new Intent(this, TestConfigs.groupActivity.get(TestConfigs.sCurrentItem.getMachineCode())));

    }

    List<UploadResults> uploadResultsList = new ArrayList<>();

    /**
     * ??????????????????
     */
    private void scoreUpload() {
        List<BaseStuPair> data = stuAdapter.getData();
        uploadResultsList.clear();
        List<BaseStuPair> stuNoResultList = new ArrayList<>();
        for (BaseStuPair stuPair : data) {
            List<RoundResult> roundResultList = getResults(stuPair.getStudent().getStudentCode());

            if (roundResultList != null && !roundResultList.isEmpty()) {
                RoundResult currentResult = roundResultList.get(0);
                UploadResults uploadResults = new UploadResults(currentResult.getScheduleNo(), TestConfigs.getCurrentItemCode(),
                        currentResult.getStudentCode(), currentResult.getTestNo() + "", groupList.get(groupAdapter.getTestPosition()), RoundResultBean.beanCope(roundResultList));
                uploadResultsList.add(uploadResults);
            } else {
                stuNoResultList.add(stuPair);
            }
//            ServerMessage.uploadResult(uploadResultsList);

        }
        if (SettingHelper.getSystemSetting().isStuConfirm() && stuNoResultList.size() > 0) {
            AdvancedDialog advancedDialog = new AdvancedDialog();
            advancedDialog.setArguments(stuNoResultList, groupList.get(groupAdapter.getTestPosition()).getId());
            advancedDialog.show(getSupportFragmentManager(), "AdvancedDialog");
        } else {
            ServerMessage.baseUploadResult(this, uploadResultsList);
        }

    }

    /**
     * ??????????????????????????????
     *
     * @param roundResultList
     */
    public void setStuPairsData(BaseStuPair pair, List<RoundResult> roundResultList) {

        String[] result = new String[TestConfigs.getMaxTestCount()];
        for (int j = 0; j < roundResultList.size(); j++) {
            if (j < result.length) {
                switch (roundResultList.get(j).getResultState()) {
                    case RoundResult.RESULT_STATE_FOUL:
                        result[j] = "X";
                        break;
                    case RoundResult.RESULT_STATE_WAIVE:
                        result[j] = "??????";
                        break;
                    case RoundResult.RESULT_STATE_BACK:
                    case -2:
                        result[j] = "??????";
                        break;
                    default:
                        result[j] = ResultDisplayUtils.getStrResultForDisplay(roundResultList.get(j).getResult());
                        break;
                }
            } else {
                break;
            }
        }
        pair.setTimeResult(result);
    }

    private boolean isAllTest() {

        for (BaseStuPair stuPair : stuPairsList) {
            Log.e("TAG----", stuPair.getTimeResult() + "");
            if (stuPair.getTimeResult() != null) {
                for (String s : stuPair.getTimeResult()) {
                    if (TextUtils.isEmpty(s)) {
                        return false;
                    }
                }
            } else {
                return false;
            }

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        TestConfigs.baseGroupMap.clear();
        TestCache.getInstance().clear();
    }

    // LED ????????????
    private void showLed(List<BaseStuPair> pairList) {
        String title = txtGroupName.getText().toString().trim();
        if (ledThread == null) {
            ledThread = new LedThread();
            ledThread.setData(title, pairList);
            ledThread.start();
        } else {
            ledThread.setData(title, pairList);
        }
    }

    class LedThread extends Thread {

        private volatile boolean isFinish;
        private String title;
        private volatile List<BaseStuPair> list;
        int hostId = SettingHelper.getSystemSetting().getHostId();

        private void finish() {
            isFinish = true;
        }

        private void setData(String title, List<BaseStuPair> list) {
            if (title.length() > 8) {
                //led ?????????8????????? ???????????????1???
                StringBuilder sb = new StringBuilder();
                String groupNo = groupList.get(groupAdapter.getTestPosition()).getGroupNo() + "";
                int index = groupNo.length() > 8 ? 7 : groupNo.length();
                sb.append(title.substring(0, 8 - (index + 1)))
                        .append(groupNo).append("???");
                this.title = sb.toString();
            } else {
                this.title = title;
            }
            List<BaseStuPair> tmpList = new ArrayList<>(list);
            Student emptyStudent = new Student();
            emptyStudent.setStudentName("");
            BaseStuPair emptyPair = new BaseStuPair(emptyStudent, null);
            // ?????????????????????
            int fillSize = list.size() % 6;
            if (fillSize != 0) {
                for (int i = 0; i < 6 - (list.size() % 6); i++) {
                    tmpList.add(emptyPair);
                }
            }
            this.list = tmpList;
        }

        @Override
        public void run() {
            while (true) {
                if (isFinish) {
                    return;
                }
                List<BaseStuPair> showList = list;// ???????????????????????????
                try {
                    int page = showList.size() / 6;
                    display:
                    for (int i = 0; i < page; i++) {// ???
                        if (mLEDManager == null && runLEDManager == null)
                            return;
                        if (MachineCode.machineCode == ItemDefault.CODE_ZFP && SettingHelper.getSystemSetting().getRadioLed() == 0) {
                            runLEDManager.showString(hostId, title, 0, 0, true, false);
                        } else {
                            mLEDManager.showString(hostId, title, 0, 0, true, false);
                        }

                        for (int j = 0; j < 3 && !isFinish; j++) {// 3???
                            if (showList != list) {// ?????????????????????
                                break display;
                            }
                            String leftStuName = showList.get(i * 6 + j * 2).getStudent().getStudentName();
                            leftStuName = InteractUtils.getStrWithLength(leftStuName, 4);
                            String rightStuName = showList.get(i * 6 + j * 2 + 1).getStudent().getStudentName();
                            rightStuName = InteractUtils.getStrWithLength(rightStuName, 4);

                            if (MachineCode.machineCode == ItemDefault.CODE_ZFP && SettingHelper.getSystemSetting().getRadioLed() == 0) {
                                runLEDManager.showString(hostId, leftStuName + rightStuName, 0, j + 1, false, j == 2);
                            } else {
                                mLEDManager.showString(hostId, leftStuName + rightStuName, 0, j + 1, false, j == 2);
                            }

                            // mLEDManager.showString(hostId, rightStuName, 8, j + 1, false, j == 2);
                        }
                        Thread.sleep(1500);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    protected void onPause() {
        super.onPause();
        if (ledThread != null) {
            ledThread.interrupt();
            ledThread.finish();
            ledThread = null;
        }

    }

    private MyTcpService.Work myBinder;
    private MyTcpService myTcpService;

    private void bindTcpService() {
        bindIntent = new Intent(BaseGroupActivity.this, MyTcpService.class);

        startService(bindIntent);
        isBind = bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    public void unBindService() {
        if (isBind && serviceConnection != null) {
            unbindService(serviceConnection);
            myTcpService.unRegisterCallBack(callBack);
            if (myBinder != null) {
                myBinder.stopWork();
            }
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (MyTcpService.Work) service;
            myTcpService = myBinder.getMyService();

            myTcpService.registerCallBack(callBack);

            if (myBinder != null && !myTcpService.isWork) {
                myBinder.startWork(currentPort);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (myTcpService != null) {
                myTcpService.unRegisterCallBack(callBack);
            }
        }
    };

    private MyTcpService.CallBack callBack = new MyTcpService.CallBack() {
        @Override
        public void postMessage(final ServiceTcpBean message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    ToastUtils.showLong("??????????????????????????????????????????");


                    if (scheduleList.size() == 0) {
                        updateSchedules();
                    } else {
                        if (TextUtils.equals(message.getSchedule().getScheduleNo(), scheduleText)) {
                            getGroupList(scheduleText);
                        } else {
                            updateSchedules();
                        }

                    }

                }
            });
        }

        @Override
        public void postConnectMessage(final String info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShort(info);
                }
            });
        }
    };

}
