package com.feipulai.host.activity.pullup.check;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.feipulai.common.view.DividerItemDecoration;
import com.feipulai.common.view.StopUseButton;
import com.feipulai.common.view.WaitDialog;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.activity.base.BaseCheckActivity;
import com.feipulai.host.activity.jump_rope.adapter.CheckPairAdapter;
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.jump_rope.base.check.RadioCheckContract;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.activity.jump_rope.bean.TestCache;
import com.feipulai.host.activity.pullup.pair.PullUpPairActivity;
import com.feipulai.host.activity.pullup.setting.PullUpSetting;
import com.feipulai.host.activity.pullup.setting.PullUpSettingActivity;
import com.feipulai.host.activity.pullup.test.PullUpTestActivity;
import com.feipulai.host.activity.setting.LEDSettingActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.view.StuSearchEditText;
import com.orhanobut.logger.utils.LogUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by pengjf on 2020/3/2.
 * ??????????????????????????????????????????   ????????????:??????
 */
public class BasePullUpActivity extends BaseCheckActivity implements RadioCheckContract.View<PullUpSetting>, CheckPairAdapter.OnItemClickListener {
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

    private CheckPairAdapter mAdapter;
    private Handler mHandler = new BaseActivity.MyHandler(this);
    private WaitDialog changBadDialog;

    private static final int UPDATE_STATES = 0x1;
    private static final int UPDATE_SPECIFIC_ITEM = 0x2;
    private static final String STOP_USE = MyApplication.getInstance().getString(R.string.stop_use);
    private static final String RESUME_USE = MyApplication.getInstance().getString(R.string.resume_use);
    protected PullUpCheckPresenter presenter;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_sit_pull_up_check;
    }
    @Override
    public int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }

    @Override
    protected void initData() {
        super.initData();
        presenter = new PullUpCheckPresenter(this, this);
        presenter.start();
    }

    @Override
    public void initView(PullUpSetting pullUpSetting, List<StuDevicePair> pairs) {
        etSelect.setData(mLvResults, this);

        mRvPairs.setLayoutManager(new GridLayoutManager(this, 5));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        dividerItemDecoration.setDrawBorderTopAndBottom(true);
        dividerItemDecoration.setDrawBorderLeftAndRight(true);
        mRvPairs.addItemDecoration(dividerItemDecoration);
        mRvPairs.setHasFixedSize(true);
        mRvPairs.setClickable(true);

        mAdapter = new CheckPairAdapter(this, pairs);
        mAdapter.setOnItemClickListener(this);
        mRvPairs.setAdapter(mAdapter);
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = String.format(getString(R.string.host_name), TestConfigs.machineNameMap.get(machineCode), SettingHelper.getSystemSetting().getHostId());
        } else {
            title = String.format(getString(R.string.host_name), TestConfigs.machineNameMap.get(machineCode), SettingHelper.getSystemSetting().getHostId())
                    + "-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title).addRightText(R.string.item_setting_title, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BasePullUpActivity.this, PullUpSettingActivity.class));
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BasePullUpActivity.this, PullUpSettingActivity.class));
            }
        });
    }

    @OnClick({R.id.btn_device_pair, R.id.btn_change_bad, R.id.btn_start_test,
            R.id.btn_stop_use, R.id.btn_led_setting, R.id.btn_delete_student, R.id.btn_del_all,R.id.img_AFR})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_AFR:
                LogUtils.operation("???????????????????????????");
                showAFR();
                break;
            case R.id.btn_device_pair:
                LogUtils.operation("???????????????????????????");
                startActivity(new Intent(this, PullUpPairActivity.class));
                break;


            case R.id.btn_change_bad:
                LogUtils.operation("???????????????????????????");
                presenter.changeBadDevice();
                break;

            case R.id.btn_start_test:
                LogUtils.operation("???????????????????????????");
                presenter.startTest();
                break;

            case R.id.btn_stop_use:
                LogUtils.operation("???????????????????????????");
                String text = btnStopUse.getText().toString().trim();
                if (text.equals(RESUME_USE)) {
                    presenter.resumeUse();
                    btnStopUse.setText(STOP_USE);
                } else {
                    presenter.stopUse();
                    btnStopUse.setText(RESUME_USE);
                }
                break;

            case R.id.btn_led_setting:
                LogUtils.operation("???????????????LED??????");
                startActivity(new Intent(this, LEDSettingActivity.class));
                break;

            case R.id.btn_delete_student:
                LogUtils.operation("???????????????????????????");
                presenter.deleteStudent();
                break;

            case R.id.btn_del_all:
                LogUtils.operation("???????????????????????????");
                presenter.deleteAll();
                break;
        }
    }


    @Override
    protected void onRestart() {
        LogUtils.operation("BasePullUpActivity onRestart");
        super.onRestart();
        presenter.settingChanged();
    }

    @Override
    protected void onResume() {
        LogUtils.operation("BasePullUpActivity onResume");
        super.onResume();
        presenter.resumeGetStateAndDisplay();
    }

    @Override
    protected void onPause() {
        LogUtils.operation("BasePullUpActivity onPause");
        super.onPause();
        presenter.cancelChangeBad();
        presenter.pauseGetStateAndDisplay();
        presenter.saveSetting();
    }

    @Override
    protected void onDestroy() {
        LogUtils.operation("BasePullUpActivity onDestroy");
        super.onDestroy();
        TestCache.getInstance().clear();
        presenter.finishGetStateAndDisplay();
    }


    @Override
    public void updateSpecificItem(int index) {
        LogUtils.operation("BasePullUpActivity updateSpecificItem index="+index);
        Message msg = Message.obtain();
        msg.what = UPDATE_SPECIFIC_ITEM;
        msg.arg1 = index;
        mHandler.sendMessage(msg);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {

            case UPDATE_SPECIFIC_ITEM:
                // Log.i("index", msg.arg1 + "");
                mAdapter.notifyItemChanged(msg.arg1);
                break;

            case UPDATE_STATES:
                mAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onCheckIn(Student student) {
        presenter.onCheckIn(student);
    }

    @Override
    public void onItemClick(View view, int position) {
        int deviceState = presenter.stateOfPosition(position);
        presenter.setFocusPosition(position);
        if (deviceState == BaseDeviceState.STATE_STOP_USE) {
            btnStopUse.setText(RESUME_USE);
        } else if (deviceState == BaseDeviceState.STATE_CONFLICT) {
            presenter.dealConflict();
            mAdapter.notifyItemChanged(position);
        } else {
            btnStopUse.setText(STOP_USE);
        }
        select(position);
    }

    @Override
    public void changeBadSuccess() {
        changBadDialog.dismiss();
        toastSpeak(getString(R.string.replace_success));
    }

    @Override
    public void select(int position) {
        int oldPosition = mAdapter.getSelected();
        mAdapter.setSelected(position);
        mAdapter.notifyItemChanged(oldPosition);
        mAdapter.notifyItemChanged(position);
    }

    @Override
    public void showStuInfo(Student student, RoundResult results) {
        InteractUtils.showStuInfo(mLlStuDetail, student, results);
    }

    @Override
    public void showChangeBadDialog() {
        changBadDialog = new WaitDialog(this);
        changBadDialog.setCanceledOnTouchOutside(false);
        changBadDialog.show();
        changBadDialog.setTitle(getString(R.string.please_restart_device));
        changBadDialog.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changBadDialog.dismiss();
                presenter.cancelChangeBad();
            }
        });
    }

    @Override
    public void showToast(String msg) {
        toastSpeak(msg);
    }

    @Override
    public void updateAllItems() {
        mHandler.sendEmptyMessage(UPDATE_STATES);
    }

    @Override
    public void startTest() {
        Intent intent = new Intent(this, PullUpTestActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.refreshEveryThing();
    }

    @Override
    public void refreshPairs(List<StuDevicePair> pairs) {
        mAdapter = new CheckPairAdapter(this, pairs);
        mAdapter.setOnItemClickListener(this);
        mRvPairs.setAdapter(mAdapter);
    }

    @Override
    public void showLowBatteryStartDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.warning))
                .setContentText(getString(R.string.low_battery_goto_hint))
                .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                startTest();
            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();


    }

}
