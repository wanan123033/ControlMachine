package com.feipulai.exam.activity.basketball;

import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.manager.RunTimerManager;
import com.feipulai.device.serial.beans.RunTimerResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.BaseAFRFragment;
import com.feipulai.exam.activity.basketball.adapter.DribbleShootAdapter;
import com.feipulai.exam.activity.basketball.result.BasketBallResult;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 运球投篮测试个人模式
 */
public class DribbleShootActivity extends BaseShootActivity implements BaseAFRFragment.onAFRCompareListener {

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
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.txt_device_status)
    TextView txtDeviceStatus;
    @BindView(R.id.txt_waiting)
    TextView txtWaiting;
    @BindView(R.id.txt_illegal_return)
    TextView txtIllegalReturn;
    @BindView(R.id.txt_continue_run)
    TextView txtContinueRun;
    @BindView(R.id.txt_stop_timing)
    TextView txtStopTiming;
    @BindView(R.id.rv_state)
    RecyclerView rvState;
    @BindView(R.id.tv_led_setting)
    TextView tvLedSetting;
    @BindView(R.id.tv_print)
    TextView tvPrint;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    @BindView(R.id.txt_finish_test)
    TextView txtFinishTest;
    @BindView(R.id.lv_results)
    ListView lvResults;

    private List<BasketBallResult> dateList = new ArrayList<>();
    private DribbleShootAdapter mAdapter;
    private String[] interceptRound = new String[]{"1起点", "2折返1", "3投篮", "4折返2", "5投篮", "6折返1", "7投篮", "8折返2", "9投篮"};
    private ShootSetting setting;
    private FrameLayout afrFrameLayout;
    private BaseAFRFragment afrFragment;
    private int hostId;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_dribble_shoot_activity;
    }

    @Override
    protected void initData() {
        super.initData();
        hostId = SettingHelper.getSystemSetting().getHostId();
        individualCheckFragment.setResultView(lvResults);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), individualCheckFragment, R.id.ll_individual_check);

        mAdapter = new DribbleShootAdapter(this, dateList);
        rvState.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvState.setAdapter(mAdapter);

        if (SettingHelper.getSystemSetting().getCheckTool() == 4 && setAFRFrameLayoutResID() != 0) {
            afrFrameLayout = findViewById(setAFRFrameLayoutResID());
            afrFragment = new BaseAFRFragment();
            afrFragment.setCompareListener(this);
            initAFR();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.life("DribbleShootActivity onResume");
        setting = SharedPrefsUtil.loadFormSource(this, ShootSetting.class);
        if (setting == null) {
            setting = new ShootSetting();
        }
        dateList.clear();
        for (int i = 0; i <= setting.getInterceptNo(); i++) {
            BasketBallResult ballResult = new BasketBallResult();
            ballResult.setName(interceptRound[i]);
            ballResult.setState(false);
            dateList.add(ballResult);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void updateStudent(Student student) {
        tvStudentCode.setText(student.getStudentCode());
        tvStudentName.setText(student.getStudentName());
        tvGender.setText(student.getSex() == 0 ? "男" : "女");
    }

    @Override
    public void getResult(RunTimerResult result) {
        //根据不同的投篮点投篮次数来更新并计时
    }


    @OnClick({R.id.txt_waiting, R.id.txt_illegal_return, R.id.txt_stop_timing,
            R.id.tv_led_setting, R.id.tv_print, R.id.tv_confirm, R.id.txt_finish_test,R.id.img_AFR})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_waiting:
                LogUtils.operation("篮球运球投篮点击了等待发令...");
                RunTimerManager.radioWait(hostId,1,1);
                RunTimerManager.radioWait(hostId,1,1);
                RunTimerManager.radioWait(hostId,1,1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RunTimerManager.radioSendWaitState(hostId,1,1);
                    }
                },1000);
//                RunTimerManager.waitStart();
                break;
            case R.id.txt_illegal_return:
                LogUtils.operation("篮球运球投篮点击了违规返回...");
                RunTimerManager.illegalBack();
                break;

            case R.id.txt_stop_timing:
                LogUtils.operation("篮球运球投篮点击了停止运行...");
                RunTimerManager.stopRun();
                break;
            case R.id.tv_led_setting:
                LogUtils.operation("篮球运球投篮点击了LED设置...");
                IntentUtil.gotoActivity(this, LEDSettingActivity.class);
                break;
            case R.id.tv_print:
                LogUtils.operation("篮球运球投篮点击了打印...");
                break;
            case R.id.tv_confirm:
                LogUtils.operation("篮球运球投篮点击了确认...");
                break;
            case R.id.txt_finish_test:
                LogUtils.operation("篮球运球投篮点击了结束测试...");
                break;
            case R.id.img_AFR:
                showAFR();
                break;
        }
    }
    public void showAFR() {
        if (SettingHelper.getSystemSetting().getCheckTool() != 4) {
            ToastUtils.showShort("未选择人脸识别检录功能");
            return;
        }
        if (afrFrameLayout == null) {
            return;
        }

        boolean isGoto = afrFragment.gotoUVCFaceCamera(!afrFragment.isOpenCamera);
        if (isGoto) {
            if (afrFragment.isOpenCamera) {
                afrFrameLayout.setVisibility(View.VISIBLE);
            } else {
                afrFrameLayout.setVisibility(View.GONE);
            }
        }
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
    public void compareStu(Student student) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                afrFrameLayout.setVisibility(View.GONE);
            }
        });

        if (student == null) {
            InteractUtils.toastSpeak(this, "该考生不存在");
            return;
        }
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        if (studentItem == null) {
            InteractUtils.toastSpeak(this, "无此项目");
            return;
        }
        List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
        if (results != null && results.size() >= TestConfigs.getMaxTestCount(this)) {
            InteractUtils.toastSpeak(this, "该考生已测试");
            return;
        }
        // 可以直接检录
        onIndividualCheckIn(student,studentItem,results);
    }


}
