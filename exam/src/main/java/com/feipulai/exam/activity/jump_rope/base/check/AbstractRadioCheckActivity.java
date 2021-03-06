package com.feipulai.exam.activity.jump_rope.base.check;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.feipulai.common.utils.ActivityUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.AgainTestDialog;
import com.feipulai.exam.activity.base.BaseAFRFragment;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.base.ResitDialog;
import com.feipulai.exam.activity.jump_rope.adapter.CheckPairAdapter;
import com.feipulai.exam.activity.jump_rope.base.result.RadioResultActivity;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.fragment.IndividualCheckFragment;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.situp.newSitUp.DeviceChangeActivity;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.view.DividerItemDecoration;
import com.feipulai.exam.view.WaitDialog;
import com.orhanobut.logger.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

public abstract class AbstractRadioCheckActivity<Setting>
        extends BaseTitleActivity
        implements RadioCheckContract.View<Setting>,
        CheckPairAdapter.OnItemClickListener,
        View.OnClickListener, BaseAFRFragment.onAFRCompareListener {

    private static final int UPDATE_STATES = 0x1;
    private static final int UPDATE_SPECIFIC_ITEM = 0x2;
    private static final String RESUME_USE = "????????????";
    private static final String STOP_USE = "????????????";

    protected RadioCheckContract.Presenter presenter;
    private WaitDialog changBadDialog;
    private Handler mHandler = new MyHandler(this);
    private CheckPairAdapter mAdapter;
    private IndividualCheckFragment individualCheckFragment;
    protected FrameLayout afrFrameLayout;
    protected BaseAFRFragment afrFragment;
    private ResitDialog.onClickQuitListener onClickQuitListener = new ResitDialog.onClickQuitListener() {
        @Override
        public void onCancel() {

        }

        @Override
        public void onCommitPattern(Student student, StudentItem studentItem, List<RoundResult> results, int roundNo) {
            presenter.onIndividualCheckIn(student, studentItem, results);
            presenter.setRoundNo(student, roundNo);
        }

        @Override
        public void onCommitGroup(Student student, GroupItem groupItem, List<RoundResult> results, int roundNo) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);// ??????????????????,???????????????
        presenter = getPresenter();
        presenter.start();
    }

    @Override
    protected void initData() {
        ButterKnife.bind(this);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (individualCheckFragment != null && individualCheckFragment.dispatchKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        boolean isTestNameEmpty = TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName());
        String title = TestConfigs.machineNameMap.get(machineCode)
                + SettingHelper.getSystemSetting().getHostId() + "??????"
                + (isTestNameEmpty ? "" : ("-" + SettingHelper.getSystemSetting().getTestName()));
        builder.setTitle(title);

        SystemSetting systemSetting = SettingHelper.getSystemSetting();
        if (systemSetting.getTestPattern() == SystemSetting.PERSON_PATTERN) {
            builder.addRightText("????????????", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AbstractRadioCheckActivity.this, getProjectSettingActivity()));
                }
            }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AbstractRadioCheckActivity.this, getProjectSettingActivity()));
                }
            });
        }
        return builder;
    }

    @Override
    public void initView(SystemSetting systemSetting, Setting setting, List<StuDevicePair> pairs) {
        if (systemSetting.getTestPattern() == SystemSetting.PERSON_PATTERN) {
            individualCheckFragment = new IndividualCheckFragment();
            individualCheckFragment.setResultView(getResultView());
            individualCheckFragment.setOnIndividualCheckInListener(presenter);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), individualCheckFragment, getCheckInLayout().getId());
        } else {
            getCheckInLayout().setVisibility(View.GONE);
            getDeleteAllView().setVisibility(View.GONE);
            getDeleteStuView().setVisibility(View.GONE);
            getStopUseView().setVisibility(View.GONE);
            getImgAFR().setVisibility(View.GONE);
        }

        getRvPairs().setLayoutManager(new GridLayoutManager(this, 5));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        dividerItemDecoration.setDrawBorderTopAndBottom(true);
        dividerItemDecoration.setDrawBorderLeftAndRight(true);
        getRvPairs().addItemDecoration(dividerItemDecoration);
        getRvPairs().setHasFixedSize(true);
        getRvPairs().setClickable(true);
        Log.e("TAG----", pairs.size() + "-----");
        mAdapter = new CheckPairAdapter(this, pairs);
        mAdapter.setOnItemClickListener(this);
        getRvPairs().setAdapter(mAdapter);

        getStopUseView().setOnClickListener(this);
        getChangeBadView().setOnClickListener(this);
        getDeleteAllView().setOnClickListener(this);
        getDeleteStuView().setOnClickListener(this);
        getStartTestView().setOnClickListener(this);
        getLedSettingView().setOnClickListener(this);
        getPairView().setOnClickListener(this);
        if (getDetailsView() != null) {
            getDetailsView().setOnClickListener(this);
        }
        if (SettingHelper.getSystemSetting().getCheckTool() == 4 && setAFRFrameLayoutResID() != 0) {
            afrFrameLayout = findViewById(setAFRFrameLayoutResID());
            afrFragment = new BaseAFRFragment();
            afrFragment.setCompareListener(this);
            initAFR();
        }
    }

    protected abstract int setAFRFrameLayoutResID();

    @Override
    public void onClick(View v) {
        if (v == getLedSettingView()) {// LED??????
            startActivity(new Intent(this, LEDSettingActivity.class));
        } else if (v == getStartTestView()) {// ????????????
            presenter.startTest();

        } else if (v == getPairView()) {// ????????????
            startActivity(new Intent(this, getPairActivity()));
        } else if (v == getChangeBadView()) {// ????????????
            presenter.changeBadDevice();
        } else if (v == getStopUseView()) {// ??????/????????????
            String text = getStopUseView().getText().toString().trim();
            if (text.equals(RESUME_USE)) {
                presenter.resumeUse();
                getStopUseView().setText(STOP_USE);
            } else {
                presenter.stopUse();
                getStopUseView().setText(RESUME_USE);
            }
        } else if (v == getDeleteAllView()) {// ????????????
//            presenter.deleteAll();
            showDeleteAllWarning();
        } else if (v == getDeleteStuView()) {// ????????????
            presenter.deleteStudent();
        } else if (v == getDetailsView()) {
            startActivity(new Intent(this, DeviceChangeActivity.class));
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        presenter.settingChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.resetLED();
        presenter.resumeGetStateAndDisplay();
    }

    @Override
    public void startTest() {
        Intent intent = new Intent(this, getTestActivity());
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RadioResultActivity.BACK_TO_CHECK:
                if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN) {
                    EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_RESULT));
                    finish();
                } else {
                    presenter.refreshEveryThing();
                }
                break;
        }
    }

    @Override
    public void updateSpecificItem(int position) {
        Message msg = Message.obtain();
        msg.what = UPDATE_SPECIFIC_ITEM;
        msg.arg1 = position;
        mHandler.sendMessage(msg);
    }

    @Override
    public void showChangeBadDialog() {
        changBadDialog = new WaitDialog(this);
        changBadDialog.setCanceledOnTouchOutside(false);
        changBadDialog.setCancelable(false);
        changBadDialog.show();
        // ?????????dialog????????????????????????
        changBadDialog.setTitle(getChangeBadTitle());
        changBadDialog.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changBadDialog.dismiss();
                presenter.cancelChangeBad();
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        int deviceState = presenter.stateOfPosition(position);
        presenter.setFocusPosition(position);
        if (deviceState == BaseDeviceState.STATE_STOP_USE) {
            if (getStopUseView() != null) {
                getStopUseView().setText(RESUME_USE);
            }
        } else if (deviceState == BaseDeviceState.STATE_CONFLICT) {
            onConflictItemClicked();
        } else {
            if (getStopUseView() != null) {
                getStopUseView().setText(STOP_USE);
            }
        }
        select(position, true);
    }

    @Override
    public void select(int position, boolean isRefreshStu) {
        int oldPosition = mAdapter.getSelected();
        mAdapter.setSelected(position);
        mAdapter.notifyItemChanged(oldPosition);
        mAdapter.notifyItemChanged(position);
        if (isRefreshStu && presenter != null) {
            presenter.showStuInfo(position);
        }

    }

    @Override
    protected void handleMessage(Message msg) {
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

    public void showDeleteAllWarning() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.warning))
                .setContentText("??????????????????????????????")
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
    public void showConstraintStartDialog(boolean contaisLowBattery) {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.warning))
                .setContentText(contaisLowBattery ? "??????????????????????????????????????????????????????????????????????"
                        : "??????????????????????????????????????????????????????????")
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
    public void showToast(String msg) {
        toastSpeak(msg);
    }

    @Override
    public void changeBadSuccess() {
        changBadDialog.dismiss();
        toastSpeak("????????????");
    }

    @Override
    public void showStuInfo(Student student, List results) {
        InteractUtils.showStuInfo(getStuDetailLayout(), student, results);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (changBadDialog != null && changBadDialog.isShowing()) {
            changBadDialog.dismiss();
        }
        presenter.cancelChangeBad();
        presenter.pauseGetStateAndDisplay();
        presenter.saveSetting();
    }

    public void updateAllItems() {
        mHandler.sendEmptyMessage(UPDATE_STATES);
    }

    @Override
    public void refreshPairs(List pairs) {
        mAdapter = new CheckPairAdapter(this, pairs);
        mAdapter.setOnItemClickListener(this);
        getRvPairs().setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        TestCache.getInstance().clear();
        presenter.finishGetStateAndDisplay();
        presenter = null;
    }

    private void initAFR() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(setAFRFrameLayoutResID(), afrFragment);
        transaction.commitAllowingStateLoss();// ????????????
    }

    @Override
    public void compareStu(final Student student) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (student != null) {
                    individualCheckFragment.checkQulification(student.getStudentCode(), IndividualCheckFragment.STUDENT_CODE);

                    afrFrameLayout.setVisibility(View.GONE);
                }else{
                    InteractUtils.toastSpeak(AbstractRadioCheckActivity.this, "??????????????????");
                }
//                if (student == null) {
//                    InteractUtils.toastSpeak(AbstractRadioCheckActivity.this, "??????????????????");
//                    return;
//                } else {
//                    afrFrameLayout.setVisibility(View.GONE);
//                }
//                final StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
//                if (studentItem == null) {
//                    InteractUtils.toastSpeak(AbstractRadioCheckActivity.this, "????????????");
//                    return;
//                }
//                final List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
//                if (results != null && results.size() >= TestConfigs.getMaxTestCount(AbstractRadioCheckActivity.this)) {
//                    SystemSetting setting = SettingHelper.getSystemSetting();
//                    if (setting.isAgainTest() && setting.isResit()){
//                        final Student finalStudent = student;
//                        new SweetAlertDialog(AbstractRadioCheckActivity.this).setContentText("????????????????????????????")
//                                .setCancelText("??????")
//                                .setConfirmText("??????")
//                                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                    @Override
//                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                        AgainTestDialog dialog = new AgainTestDialog();
//                                        dialog.setArguments(finalStudent,results,studentItem);
//                                        dialog.setOnIndividualCheckInListener(onClickQuitListener);
//                                        dialog.show(getSupportFragmentManager(),"AgainTestDialog");
//                                        sweetAlertDialog.dismissWithAnimation();
//                                    }
//                                })
//                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                    @Override
//                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                        ResitDialog dialog = new ResitDialog();
//                                        dialog.setArguments(finalStudent,results,studentItem);
//                                        dialog.setOnIndividualCheckInListener(onClickQuitListener);
//                                        dialog.show(getSupportFragmentManager(),"ResitDialog");
//                                        sweetAlertDialog.dismissWithAnimation();
//                                    }
//                                }).show();
//                        return;
//                    }
//                    if (setting.isAgainTest()){
//                        AgainTestDialog dialog = new AgainTestDialog();
//                        dialog.setArguments(student,results,studentItem);
//                        dialog.setOnIndividualCheckInListener(onClickQuitListener);
//                        dialog.show(getSupportFragmentManager(),"AgainTestDialog");
//                        return;
//                    }
//                    if (setting.isResit()){
//                        ResitDialog dialog = new ResitDialog();
//                        dialog.setArguments(student,results,studentItem);
//                        dialog.setOnIndividualCheckInListener(onClickQuitListener);
//                        dialog.show(getSupportFragmentManager(),"ResitDialog");
//                    }else {
//                        InteractUtils.toastSpeak(AbstractRadioCheckActivity.this, "??????????????????");
//                    }
//                    return;
//                }
//                LogUtils.operation("???????????????" + student.toString());
//                // ??????????????????
//                presenter.onIndividualCheckIn(student, studentItem, results);
            }
        });


    }

    protected abstract RadioCheckContract.Presenter getPresenter();

    protected abstract ListView getResultView();

    protected abstract void onConflictItemClicked();

    protected abstract View getChangeBadView();

    protected abstract View getDetailsView();

    protected abstract View getLedSettingView();

    protected abstract View getPairView();

    protected abstract View getStartTestView();

    protected abstract Class<?> getProjectSettingActivity();

    protected abstract RecyclerView getRvPairs();

    protected abstract TextView getStopUseView();

    protected abstract View getDeleteStuView();

    protected abstract View getDeleteAllView();

    protected abstract View getCheckInLayout();

    protected abstract View getImgAFR();

    protected abstract LinearLayout getStuDetailLayout();

    protected abstract String getChangeBadTitle();

    protected abstract Class<? extends Activity> getTestActivity();

    protected abstract Class<? extends Activity> getPairActivity();
}
