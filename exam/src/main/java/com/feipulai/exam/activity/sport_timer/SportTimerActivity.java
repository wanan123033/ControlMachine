package com.feipulai.exam.activity.sport_timer;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.feipulai.common.utils.ActivityUtils;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseAFRFragment;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.fragment.IndividualCheckFragment;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.sport_timer.pair.SportPairActivity;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class SportTimerActivity extends BaseTitleActivity implements BaseAFRFragment.onAFRCompareListener,
        IndividualCheckFragment.OnIndividualCheckInListener ,SportContract.SportView{

    @BindView(R.id.lv_results)
    ListView lvResults;
    @BindView(R.id.iv_portrait)
    ImageView ivPortrait;
    @BindView(R.id.tv_studentCode)
    TextView tvStudentCode;
    @BindView(R.id.tv_studentName)
    TextView tvStudentName;
    @BindView(R.id.tv_gender)
    TextView tvGender;
    @BindView(R.id.tv_grade)
    TextView tvGrade;
    @BindView(R.id.ll_stu_detail)
    LinearLayout llStuDetail;
    @BindView(R.id.ll_individual_check)
    LinearLayout llIndividualCheck;
    @BindView(R.id.tv_pair)
    TextView tvPair;
    @BindView(R.id.img_AFR)
    ImageView imgAFR;
    @BindView(R.id.cb_device_state)
    CheckBox cbDeviceState;
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.txt_device_status)
    TextView txtDeviceStatus;
    @BindView(R.id.tv_foul)
    TextView tvFoul;
    @BindView(R.id.tv_inBack)
    TextView tvInBack;
    @BindView(R.id.tv_abandon)
    TextView tvAbandon;
    @BindView(R.id.tv_normal)
    TextView tvNormal;
    @BindView(R.id.txt_waiting)
    TextView txtWaiting;
    @BindView(R.id.txt_illegal_return)
    TextView txtIllegalReturn;
    @BindView(R.id.txt_continue_run)
    TextView txtContinueRun;
    @BindView(R.id.txt_stop_timing)
    TextView txtStopTiming;
    @BindView(R.id.rv_test_result)
    RecyclerView rvTestResult;
    @BindView(R.id.view_list_head)
    LinearLayout viewListHead;
    @BindView(R.id.view_part_result)
    LinearLayout viewPartResult;
    @BindView(R.id.list_item)
    RecyclerView listItem;
    @BindView(R.id.rv_region_mark)
    RecyclerView rvRegionMark;
    @BindView(R.id.rl_group)
    LinearLayout rlGroup;
    @BindView(R.id.tv_print)
    TextView tvPrint;
    @BindView(R.id.tv_end_result)
    TextView endResult;
    @BindView(R.id.tv_part_result)
    TextView partResult;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    @BindView(R.id.txt_finish_test)
    TextView txtFinishTest;
    @BindView(R.id.frame_camera)
    FrameLayout frameCamera;

    private FrameLayout afrFrameLayout;
    private BaseAFRFragment afrFragment;
    private IndividualCheckFragment individualCheckFragment;
    private SportPresent sportPresent;
    private SportTimerSetting setting;
    private DeviceDialog deviceDialog;
    private List<DeviceState> deviceStates;
    private TimeResultAdapter timeResultAdapter;
    private List<SportTimeResult> resultList =  new ArrayList<>();
    private SportTestCountAdapter testCountAdapter;
    private PartResultAdapter partResultAdapter;
    private List<SportTimeResult> partResultList;
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_sport_timer;
    }

    @Override
    protected void initData() {
        if (SettingHelper.getSystemSetting().getCheckTool() == 4 && setAFRFrameLayoutResID() != 0) {
            afrFrameLayout = findViewById(setAFRFrameLayoutResID());
            afrFragment = new BaseAFRFragment();
            afrFragment.setCompareListener(this);
            initAFR();
        }

        individualCheckFragment = new IndividualCheckFragment();
        individualCheckFragment.setResultView(lvResults);
        individualCheckFragment.setOnIndividualCheckInListener(this);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), individualCheckFragment, R.id.ll_individual_check);
        sportPresent = new SportPresent(this);
        sportPresent.rollConnect();
        setting = SharedPrefsUtil.loadFormSource(this, SportTimerSetting.class);
        if (setting == null)
            setting = new SportTimerSetting();
        deviceStates = new ArrayList<>();
        for (int i = 0 ;i< setting.getDeviceCount();i++){
            DeviceState deviceState = new DeviceState();
            deviceState.setDeviceId(i+1);
            deviceState.setDeviceState(0);
            deviceStates.add(deviceState);
        }
        deviceDialog = new DeviceDialog(this,deviceStates);

        timeResultAdapter = new TimeResultAdapter(resultList);
        rvTestResult.setLayoutManager(new LinearLayoutManager(this));
        rvTestResult.setAdapter(timeResultAdapter);

        int testNum = TestConfigs.sCurrentItem.getTestNum();
        if (testNum == 0){
            testNum = setting.getTestTimes();
        }

        List<String> testTimes =  new ArrayList<>();
        for (int i = 0;i< testNum ;i++){
            testTimes.add(String.format(Locale.CHINA,"轮次%d", i + 1));
        }
        listItem.setLayoutManager(new LinearLayoutManager(this));
        testCountAdapter = new SportTestCountAdapter(testTimes);
        listItem.setAdapter(testCountAdapter);

        endResult.setSelected(true);
        partResult.setSelected(false);
        viewPartResult.setVisibility(View.GONE);

        partResultList = new ArrayList<>();
        partResultAdapter = new PartResultAdapter(partResultList);
        rvRegionMark.setLayoutManager(new LinearLayoutManager(this));
        rvRegionMark.setAdapter(partResultAdapter);
        txtIllegalReturn.setSelected(false);
        txtStopTiming.setSelected(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int frequency = SettingHelper.getSystemSetting().getUseChannel();
        RadioChannelCommand command = new RadioChannelCommand(frequency);
        LogUtils.normal(command.getCommand().length+"---" + StringUtility.bytesToHexString(command.getCommand())+"---切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command));
        sportPresent.setContinueRoll(true);
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        boolean isTestNameEmpty = TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName());
        title = TestConfigs.machineNameMap.get(machineCode)
                + SettingHelper.getSystemSetting().getHostId() + "号机"
                + (isTestNameEmpty ? "" : ("-" + SettingHelper.getSystemSetting().getTestName()));
        return builder.setTitle(title).addRightText("项目设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProjectSetting();
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProjectSetting();
            }
        });
    }

    private void startProjectSetting() {
        IntentUtil.gotoActivityForResult(this, SportSettingActivity.class, 1);
    }

    private void initAFR() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(setAFRFrameLayoutResID(), afrFragment);
        transaction.commitAllowingStateLoss();// 提交更改


    }

    public int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }

    @Override
    public void compareStu(final Student student) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (student == null) {
                    InteractUtils.toastSpeak(SportTimerActivity.this, "该考生不存在");
                    return;
                } else {
                    afrFrameLayout.setVisibility(View.GONE);
                }
                StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
                if (studentItem == null) {
                    InteractUtils.toastSpeak(SportTimerActivity.this, "无此项目");
                    return;
                }
                List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
                if (results != null && results.size() >= TestConfigs.getMaxTestCount(SportTimerActivity.this)) {
                    InteractUtils.toastSpeak(SportTimerActivity.this, "该考生已测试");
                    return;
                }
                // 可以直接检录
                onIndividualCheckIn(student, studentItem, results);
            }
        });
    }


    @Override
    public void onIndividualCheckIn(Student student, StudentItem studentItem, List<RoundResult> results) {

    }

    @OnClick({R.id.tv_pair,R.id.txt_waiting,R.id.cb_device_state,R.id.tv_end_result,R.id.tv_part_result})
    public void btnOnClick(View v) {
        switch (v.getId()) {
            case R.id.tv_pair:
                startActivity(new Intent(this, SportPairActivity.class));
                break;
            case R.id.txt_waiting:
                sportPresent.waitStart();
                break;
            case R.id.cb_device_state:
                deviceDialog.show();
                cbDeviceState.setChecked(!cbDeviceState.isChecked());
                break;
            case R.id.tv_end_result:
                endResult.setSelected(true);
                partResult.setSelected(false);
                viewListHead.setVisibility(View.VISIBLE);
                viewPartResult.setVisibility(View.GONE);
                break;
            case R.id.tv_part_result:
                endResult.setSelected(false);
                partResult.setSelected(true);
                viewListHead.setVisibility(View.GONE);
                viewPartResult.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void updateDeviceState(final int deviceId, final int state) {
        if (deviceStates.get(deviceId-1).getDeviceState()!= state){
            deviceStates.get(deviceId-1).setDeviceState(state);
        }
    }

    @Override
    public void getTimeUpdate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtWaiting.setSelected(false);
                txtStopTiming.setSelected(true);
                sportPresent.setRunState(1);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        sportPresent.setContinueRoll(false);
    }
}
