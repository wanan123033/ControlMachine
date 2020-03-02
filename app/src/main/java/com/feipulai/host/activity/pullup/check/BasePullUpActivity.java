package com.feipulai.host.activity.pullup.check;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.feipulai.common.view.StopUseButton;
import com.feipulai.host.R;
import com.feipulai.host.activity.jump_rope.base.check.RadioCheckContract;
import com.feipulai.host.view.StuSearchEditText;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pengjf on 2020/3/2.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public abstract class BasePullUpActivity<Setting> extends BaseUpCheckActivity<Setting> implements RadioCheckContract.View<Setting> {
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
    LinearLayout mLlStuDetail;
    @BindView(R.id.et_select)
    StuSearchEditText etSelect;
    @BindView(R.id.rl_check_in)
    LinearLayout rlCheckIn;
    @BindView(R.id.rv_pairs)
    RecyclerView mRvPairs;
    @BindView(R.id.lv_results)
    ListView mLvResults;
    @BindView(R.id.tv_group)
    TextView tvGroup;
    @BindView(R.id.ll_device_group)
    LinearLayout llDeviceGroup;
    @BindView(R.id.tv_conflict)
    TextView tvConflict;
    @BindView(R.id.btn_start_test)
    Button btnStartTest;
    @BindView(R.id.btn_stop_use)
    StopUseButton btnStopUse;
    @BindView(R.id.btn_change_bad)
    Button btnChangeBad;
    @BindView(R.id.btn_device_pair)
    Button btnDevicePair;
    @BindView(R.id.btn_led_setting)
    Button btnLedSetting;
    @BindView(R.id.btn_delete_student)
    Button btnDeleteStudent;
    @BindView(R.id.btn_del_all)
    Button btnDelAll;
    @BindView(R.id.view_bottom)
    LinearLayout viewBottom;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_sit_pull_up_check;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @Override
    protected ListView getResultView() {
        return mLvResults;
    }

    @Override
    protected View getChangeBadView() {
        return btnChangeBad;
    }

    @Override
    protected View getLedSettingView() {
        return btnLedSetting;
    }

    @Override
    protected View getPairView() {
        return btnDevicePair;
    }

    @Override
    protected View getStartTestView() {
        return btnStartTest;
    }

    @Override
    protected RecyclerView getRvPairs() {
        return mRvPairs;
    }

    @Override
    protected View getCheckInLayout() {
        return rlCheckIn;
    }

    @Override
    protected void onConflictItemClicked() {
        // 没有这个
    }

    @Override
    protected TextView getStopUseView() {
        return btnStopUse;
    }

    @Override
    protected View getDeleteStuView() {
        return btnDeleteStudent;
    }

    @Override
    protected View getDeleteAllView() {
        return btnDelAll;
    }

    @Override
    protected LinearLayout getStuDetailLayout() {
        return mLlStuDetail;
    }

    @Override
    protected String getChangeBadTitle() {
        return "请重启待连接设备";
    }
}
