package com.feipulai.exam.activity.basketball;

import android.support.v7.widget.RecyclerView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.feipulai.common.utils.ActivityUtils;
import com.feipulai.exam.R;
import com.feipulai.exam.entity.Student;

import butterknife.BindView;

/**
 * 投篮测试个人模式
 *
 * @date 2020/3/27
 */
public class BasketBallShootActivity extends BaseShootActivity {

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

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_basket_ball_shoot;
    }

    @Override
    protected void initData() {
        super.initData();
        individualCheckFragment.setResultView(lvResults);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), individualCheckFragment, R.id.ll_individual_check);
    }
    @Override
    protected void updateStudent(Student student) {
        tvStudentCode.setText(student.getStudentCode());
        tvStudentName.setText(student.getStudentName());
        tvGender.setText(student.getSex()==0?"男":"女");
    }


}
