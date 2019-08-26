package com.feipulai.exam.activity.jump_rope.check;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.base.check.AbstractRadioCheckActivity;
import com.feipulai.exam.activity.jump_rope.base.check.RadioCheckContract;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.pair.JumpRopePairActivity;
import com.feipulai.exam.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.exam.activity.jump_rope.setting.JumpRopeSettingActivity;
import com.feipulai.exam.activity.jump_rope.test.JumpRopeTestActivity;
import com.feipulai.exam.activity.setting.SystemSetting;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class JumpRopeCheckActivity
        extends AbstractRadioCheckActivity<JumpRopeSetting>
        implements JumpRopeCheckContract.View<JumpRopeSetting> {

    @BindView(R.id.iv_portrait)
    ImageView mIvPortrait;
    @BindView(R.id.rv_pairs)
    RecyclerView mRvPairs;
    @BindView(R.id.tv_group)
    TextView mTvGroup;
    @BindView(R.id.btn_device_pair)
    Button mBtnDevicePair;
    @BindView(R.id.btn_change_hand_group)
    Button mBtnHandChange;
    @BindView(R.id.btn_change_bad)
    Button mBtnChangeBad;
    @BindView(R.id.btn_start_test)
    Button mBtnStartTest;
    @BindView(R.id.btn_stop_use)
    Button mBtnStopUse;
    @BindView(R.id.btn_led_setting)
    Button mBtnLedSetting;
    @BindView(R.id.btn_delete_student)
    Button mBtnDeleteStudent;
    @BindView(R.id.btn_del_all)
    Button mBtnDelAll;
    @BindView(R.id.view_bottom)
    LinearLayout mViewBottom;
    @BindView(R.id.lv_results)
    ListView mLvResults;
    @BindView(R.id.ll_stu_detail)
    LinearLayout mLlStuDetail;
    @BindView(R.id.rl_check_in)
    RelativeLayout rlCheckIn;
    @BindView(R.id.tv_conflict)
    TextView tvConflict;
    @BindView(R.id.ll_device_group)
    LinearLayout llDeviceGroup;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_jump_rope_check;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void initView(SystemSetting systemSetting, JumpRopeSetting setting, List<StuDevicePair> pairs) {
        super.initView(systemSetting, setting, pairs);
        mTvGroup.setText(SerialConfigs.GROUP_NAME[setting.getDeviceGroup()] + "组");
        tvConflict.setVisibility(View.VISIBLE);
        llDeviceGroup.setVisibility(View.VISIBLE);
    }

    // @Override
    // protected void onRestart() {
    // 	super.onRestart();
    // 	JumpRopeSetting setting = SharedPrefsUtil.loadFormSource(this, JumpRopeSetting.class);
    // mTvGroup.setText(SerialConfigs.GROUP_NAME[setting.getDeviceGroup()] + "组");
    // }

    @Override
    protected View getChangeBadView() {
        return mBtnChangeBad;
    }

    @Override
    protected View getLedSettingView() {
        return mBtnLedSetting;
    }

    @Override
    protected View getPairView() {
        return mBtnDevicePair;
    }

    @Override
    protected View getStartTestView() {
        return mBtnStartTest;
    }

    @Override
    protected Class<?> getProjectSettingActivity() {
        return JumpRopeSettingActivity.class;
    }

    @Override
    protected Class<? extends Activity> getPairActivity() {
        return JumpRopePairActivity.class;
    }

    @Override
    protected RadioCheckContract.Presenter getPresenter() {
        return new JumpRopeCheckPresenter(this, this);
    }

    @Override
    protected ListView getResultView() {
        return mLvResults;
    }

    @Override
    protected RecyclerView getRvPairs() {
        return mRvPairs;
    }

    @Override
    protected TextView getStopUseView() {
        return mBtnStopUse;
    }

    @Override
    protected View getDeleteStuView() {
        return mBtnDeleteStudent;
    }

    @Override
    protected View getDeleteAllView() {
        return mBtnDelAll;
    }

    @Override
    protected View getCheckInLayout() {
        return rlCheckIn;
    }

    @Override
    protected LinearLayout getStuDetailLayout() {
        return mLlStuDetail;
    }

    @Override
    protected String getChangeBadTitle() {
        return "请按下手柄按钮";
    }

    @Override
    protected Class<? extends Activity> getTestActivity() {
        return JumpRopeTestActivity.class;
    }

    @Override
    protected void onConflictItemClicked() {
        ((JumpRopeCheckContract.Presenter) presenter).dealConflict();
    }

    @Override
    public void showChangeDeviceGroup(int deviceGroup) {
        mTvGroup.setText(SerialConfigs.GROUP_NAME[deviceGroup] + "组");
        updateAllItems();
    }

    @Override
    public void showChangBadWarning() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.warning))
                .setContentText(getString(R.string.clear_device_hint))
                .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                ((JumpRopeCheckPresenter) presenter).changeBadDevice(true);
            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                ((JumpRopeCheckPresenter) presenter).changeBadDevice(false);
            }
        }).show();


    }

    @OnClick({R.id.btn_change_hand_group, R.id.btn_kill_devices})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btn_change_hand_group:
                ((JumpRopeCheckContract.Presenter) presenter).changeDeviceGroup();
                break;

            case R.id.btn_kill_devices:
                ((JumpRopeCheckContract.Presenter) presenter).killAllDevices();
                break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
    }
}
