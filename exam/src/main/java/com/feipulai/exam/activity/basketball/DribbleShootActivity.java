package com.feipulai.exam.activity.basketball;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.feipulai.common.utils.ActivityUtils;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.basketball.adapter.DribbleShootAdapter;
import com.feipulai.exam.activity.basketball.result.BasketBallResult;
import com.feipulai.exam.entity.Student;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 运球投篮测试个人模式
 */
public class DribbleShootActivity extends BaseShootActivity {

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
    @BindView(R.id.cb_near)
    CheckBox cbNear;
    @BindView(R.id.cb_far)
    CheckBox cbFar;
    @BindView(R.id.cb_led)
    CheckBox cbLed;
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
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_dribble_shoot_activity;
    }

    @Override
    protected void initData() {
        super.initData();
        individualCheckFragment.setResultView(lvResults);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), individualCheckFragment, R.id.ll_individual_check);
        for (int i = 0;i< 6;i++){
            BasketBallResult ballResult = new BasketBallResult();
            ballResult.setName("折返点"+(i+1));
            ballResult.setState(i%2 == 0);
            dateList.add(ballResult);
        }
        mAdapter = new DribbleShootAdapter(this,dateList);
        rvState.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        rvState.setAdapter(mAdapter);
    }

    @Override
    protected void updateStudent(Student student) {
        tvStudentCode.setText(student.getStudentCode());
        tvStudentName.setText(student.getStudentName());
        tvGender.setText(student.getSex()==0?"男":"女");
    }


    @OnClick({R.id.txt_waiting, R.id.txt_illegal_return, R.id.txt_continue_run, R.id.txt_stop_timing,
            R.id.tv_led_setting, R.id.tv_print, R.id.tv_confirm, R.id.txt_finish_test})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_waiting:

                break;
            case R.id.txt_illegal_return:

                break;
            case R.id.txt_continue_run:

                break;
            case R.id.txt_stop_timing:

                break;
            case R.id.tv_led_setting:
                IntentUtil.gotoActivity(this, LEDSettingActivity.class);
                break;
            case R.id.tv_print:

                break;
            case R.id.tv_confirm:

                break;
            case R.id.txt_finish_test:
                break;
        }
    }
}
