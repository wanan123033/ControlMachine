package com.feipulai.host.activity.jump_rope.check;

import android.content.Intent;
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
import com.feipulai.common.view.WaitDialog;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.setting.LEDSettingActivity;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.activity.base.BaseCheckActivity;
import com.feipulai.host.activity.jump_rope.adapter.CheckPairAdapter;
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.jump_rope.base.result.RadioResultActivity;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.TestCache;
import com.feipulai.host.activity.jump_rope.pair.JumpRopePairActivity;
import com.feipulai.host.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.host.activity.jump_rope.setting.JumpRopeSettingActivity;
import com.feipulai.host.activity.jump_rope.test.JumpRopeTestActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.view.StuSearchEditText;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class JumpRopeCheckActivity
        extends BaseCheckActivity
        implements JumpRopeCheckContract.View<JumpRopeSetting>,
        CheckPairAdapter.OnItemClickListener {

    private static final int UPDATE_STATES = 0x1;
    private static final int UPDATE_SPECIFIC_ITEM = 0x2;
    private static final String STOP_USE = MyApplication.getInstance().getString(R.string.stop_use);
    private static final String RESUME_USE = MyApplication.getInstance().getString(R.string.resume_use);

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
    @BindView(R.id.ll_stu_detail)
    LinearLayout mLlStuDetail;
    //	@BindView(R.id.tv_project_setting)
//	TextView tvProjectSetting;
    @BindView(R.id.et_select)
    StuSearchEditText etSelect;
    @BindView(R.id.lv_results)
    ListView lvResults;
    @BindView(R.id.ll_device_group)
    LinearLayout llDeviceGroup;
    @BindView(R.id.tv_conflict)
    TextView tvConflict;

    private WaitDialog changBadDialog;
    private Handler mHandler = new BaseActivity.MyHandler(this);
    private CheckPairAdapter mAdapter;
    private JumpRopeCheckContract.Presenter presenter;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_jump_rope_check;
    }
    @Override
    public int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }
    @Override
    protected void initData() {
        super.initData();
        presenter = new JumpRopeCheckPresenter(this, this);
        presenter.start();
        tvConflict.setVisibility(View.VISIBLE);
        llDeviceGroup.setVisibility(View.VISIBLE);
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
                startActivity(new Intent(JumpRopeCheckActivity.this, JumpRopeSettingActivity.class));
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(JumpRopeCheckActivity.this, JumpRopeSettingActivity.class));
            }
        });
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        presenter.settingChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.resumeGetStateAndDisplay();
    }

    @Override
    public void initView(JumpRopeSetting setting, List pairs) {
        etSelect.setData(lvResults, this);
        mTvGroup.setText(String.format(getString(R.string.group_name), SerialConfigs.GROUP_NAME[setting.getDeviceGroup()]));

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

    @Override
    public void updateSpecificItem(int position) {
        Message msg = Message.obtain();
        msg.what = UPDATE_SPECIFIC_ITEM;
        msg.arg1 = position;
        mHandler.sendMessage(msg);
    }

    @Override
    public void showChangeDeviceGroup(int deviceGroup) {
        mTvGroup.setText(String.format(getString(R.string.group_name), SerialConfigs.GROUP_NAME[deviceGroup]));
        updateAllItems();
    }

    @OnClick({R.id.btn_device_pair, R.id.btn_change_hand_group, R.id.btn_change_bad, R.id.btn_start_test,
            R.id.btn_stop_use, R.id.btn_led_setting, R.id.btn_delete_student, R.id.btn_del_all, R.id.btn_kill_devices, R.id.img_AFR})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_AFR:
                showAFR();
                break;
            case R.id.btn_device_pair:
                startActivity(new Intent(this, JumpRopePairActivity.class));
                break;

            case R.id.btn_change_hand_group:
                presenter.changeDeviceGroup();
                break;

            case R.id.btn_change_bad:
                presenter.changeBadDevice();
                break;

            case R.id.btn_start_test:
                presenter.startTest();
                break;

            case R.id.btn_stop_use:
                String text = mBtnStopUse.getText().toString().trim();
                if (text.equals(RESUME_USE)) {
                    presenter.resumeUse();
                    mBtnStopUse.setText(STOP_USE);
                } else {
                    presenter.stopUse();
                    mBtnStopUse.setText(RESUME_USE);
                }
                break;

            case R.id.btn_led_setting:
                startActivity(new Intent(this, LEDSettingActivity.class));
                break;

            case R.id.btn_delete_student:
                presenter.deleteStudent();
                break;

            case R.id.btn_del_all:
                showkillAllWarning();

                break;
            case R.id.btn_kill_devices:
//                presenter.killAllDevices();
                showDeleteAllWarning();
                break;

//            case R.id.tv_project_setting:
//
//                break;
        }
    }

    public void showkillAllWarning() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.warning))
                .setContentText("是否删除全部检入考生")
                .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                presenter.deleteAll();
            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();

            }
        }).show();
    }

    public void showDeleteAllWarning() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.warning))
                .setContentText(getString(R.string.clear_all_device_hint))
                .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                ((JumpRopeCheckContract.Presenter) presenter).killAllDevices();
            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();

            }
        }).show();
    }

    @Override
    public void onItemClick(View view, int position) {
        int deviceState = presenter.stateOfPosition(position);
        presenter.setFocusPosition(position);
        if (deviceState == BaseDeviceState.STATE_STOP_USE) {
            mBtnStopUse.setText(RESUME_USE);
        } else if (deviceState == BaseDeviceState.STATE_CONFLICT) {
            presenter.dealConflict();
        } else {
            mBtnStopUse.setText(STOP_USE);
        }
        select(position);
    }

    @Override
    public void select(int position) {
        int oldPosition = mAdapter.getSelected();
        mAdapter.setSelected(position);
        mAdapter.notifyItemChanged(oldPosition);
        mAdapter.notifyItemChanged(position);
    }

    @Override
    public void showStuInfo(Student student, RoundResult lastResult) {
        InteractUtils.showStuInfo(mLlStuDetail, student, lastResult);
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
    public void startTest() {
        Intent intent = new Intent(this, JumpRopeTestActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RadioResultActivity.BACK_TO_CHECK:
                // 一切从头再来
                presenter.refreshEveryThing();
                break;
        }
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

    @Override
    public void showChangeBadDialog() {
        changBadDialog = new WaitDialog(this);
        changBadDialog.setCanceledOnTouchOutside(false);
        changBadDialog.setCancelable(false);
        changBadDialog.show();
        // 必须在dialog显示出来后再调用
        changBadDialog.setTitle(getString(R.string.dialog_chang_bad_title));
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
    public void changeBadSuccess() {
        changBadDialog.dismiss();
        toastSpeak(getString(R.string.replace_success));
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.cancelChangeBad();
        presenter.pauseGetStateAndDisplay();
        presenter.saveSetting();
    }

    @Override
    public void onCheckIn(Student student) {
        presenter.onCheckIn(student);
    }

    public void updateAllItems() {
        mHandler.sendEmptyMessage(UPDATE_STATES);
    }

    @Override
    public void refreshPairs(List pairs) {
        mAdapter = new CheckPairAdapter(this, pairs);
        mAdapter.setOnItemClickListener(this);
        mRvPairs.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TestCache.getInstance().clear();
        presenter.finishGetStateAndDisplay();
    }

//    @Override
//    public void onWrongLength(int length, int expectLength) {
//        InteractUtils.toast(this, "条码与当前设置位数不一致,请重扫条码");
//    }

}
