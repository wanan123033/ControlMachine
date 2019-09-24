package com.feipulai.host.activity.jump_rope.base.test;

import android.content.Intent;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.common.view.DividerItemDecoration;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.jump_rope.adapter.RTResultAdapter;
import com.feipulai.host.activity.jump_rope.base.result.RadioResultActivity;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public abstract class AbstractRadioTestActivity<Setting>
        extends BaseTitleActivity
        implements RadioTestContract.View<Setting>,
        RTResultAdapter.OnItemClickListener {

    private static final String STOP_USE = MyApplication.getInstance().getString(R.string.stop_use);
    private static final String RESUME_USE = MyApplication.getInstance().getString(R.string.resume_use);

    private static final int TIME_COUNT = 0x1;
    private static final int UPDATE_SPECIFIC_ITEM = 0x2;
    private static final int SHOW_WAIT_DIALOG = 0x4;
    private static final int DISMISS_WAIT_DIALOG = 0x5;
    private static final int SHOW_VIEW_FOR_CONFIRM_RESULTS = 0x6;

    @BindView(R.id.tv_count)
    TextView tvCount;
    @BindView(R.id.rv_pairs)
    RecyclerView rvPairs;
    @BindView(R.id.btn_stop_using)
    Button btnStopUsing;
    @BindView(R.id.btn_restart)
    Button btnRestart;
    @BindView(R.id.btn_quit_test)
    Button btnQuitTest;

    @BindView(R.id.ll_device_group)
    protected LinearLayout llDeviceGroup;
    @BindView(R.id.tv_group)
    protected TextView tvGroup;

    protected AbstractRadioTestPresenter presenter;
    @BindView(R.id.btn_confirm_results)
    Button btnConfirm;
    private MyHandler mHandler = new MyHandler(this);
    private SweetAlertDialog sweetAlertDialog;
    private RTResultAdapter mAdapter;
    private boolean isTestFinished;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_radio_test;
    }

    @Override
    protected void initData() {
        presenter = getPresenter();
        presenter.start();
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

        return builder.setTitle(title);
    }


    protected abstract AbstractRadioTestPresenter getPresenter();

    @Override
    public void initView(List<StuDevicePair> pairs, Setting setting) {
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 5);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this);
        itemDecoration.setDrawBorderTopAndBottom(true);
        itemDecoration.setDrawBorderLeftAndRight(true);
        rvPairs.setLayoutManager(layoutManager);
        rvPairs.addItemDecoration(itemDecoration);
        mAdapter = new RTResultAdapter(this, pairs);
        rvPairs.setAdapter(mAdapter);
        rvPairs.setClickable(true);
        mAdapter.setOnItemClickListener(this);
    }


    @OnClick({R.id.btn_stop_using, R.id.btn_restart, R.id.btn_quit_test, R.id.btn_confirm_results})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btn_stop_using:
                String text = btnStopUsing.getText().toString().trim();
                if (text.equals(RESUME_USE)) {
                    presenter.resumeUse();
                    btnStopUsing.setText(STOP_USE);
                } else {
                    presenter.stopUse();
                    btnStopUsing.setText(RESUME_USE);
                }
                break;

            case R.id.btn_restart:

                new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.warning))
                        .setContentText(getString(R.string.restart_confirm_hint))
                        .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        presenter.restartTest();
                    }
                }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                }).show();

                break;

            case R.id.btn_quit_test:
                showQuitDialog();
                break;

            case R.id.btn_confirm_results:
                presenter.confirmResults();
                break;

        }
    }


    @Override
    public void enableStopUse(final boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnStopUsing.setVisibility(enable ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        int deviceState = presenter.stateOfPosition(position);
        btnStopUsing.setText(deviceState == BaseDeviceState.STATE_STOP_USE ? RESUME_USE : STOP_USE);

        int oldPosition = mAdapter.getSelected();
        if (oldPosition != position) {
            mAdapter.setSelected(position);
            mAdapter.notifyItemChanged(oldPosition);
            mAdapter.notifyItemChanged(position);
            presenter.setFocusPosition(position);
        }
    }

    // @Override
    // public void updateStates() {
    // 	mHandler.sendEmptyMessage(UPDATE_STATES);
    // }

    @Override
    public void enableStopRestartTest(final boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnQuitTest.setVisibility(enable ? View.VISIBLE : View.GONE);
                btnRestart.setVisibility(enable ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void showWaitFinalResultDialog(boolean showDialog) {
        if (showDialog) {
            mHandler.sendEmptyMessage(SHOW_WAIT_DIALOG);
        } else {
            mHandler.sendEmptyMessage(DISMISS_WAIT_DIALOG);
        }
    }

    @Override
    public void setViewForStart() {
        setEnableState(false, true, true, false);
    }

    @Override
    public void quitTest() {
//        Intent intent = new Intent(AbstractRadioTestActivity.this,
//                TestConfigs.proActivity.get(TestConfigs.sCurrentItem.getMachineCode()));
//        startActivity(intent);
        finish();
    }

    @Override
    public void finishTest() {
        Intent intent = new Intent(this, RadioResultActivity.class);
        startActivityForResult(intent, 1);
        isTestFinished = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RadioResultActivity.BACK_TO_CHECK:
                presenter.stopNow();
                finish();
                break;
        }
    }

    @Override
    public void tickInUI(String text) {
        Message msg = Message.obtain();
        msg.what = TIME_COUNT;
        msg.obj = text;
        mHandler.sendMessage(msg);
    }

    @Override
    public void updateSpecificItem(int piv) {
        Message msg = Message.obtain();
        msg.what = UPDATE_SPECIFIC_ITEM;
        msg.arg1 = piv;
        mHandler.sendMessage(msg);
    }

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what) {

            case UPDATE_SPECIFIC_ITEM:
                mAdapter.notifyItemChanged(msg.arg1);
                break;

            case TIME_COUNT:
                tvCount.setText((CharSequence) msg.obj);
                break;

            case SHOW_WAIT_DIALOG:
                sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                sweetAlertDialog.setTitleText(getString(R.string.dialog_get_result_title));
                sweetAlertDialog.setContentText(getString(R.string.dialog_get_result_message));
                sweetAlertDialog.setCancelable(false);
                sweetAlertDialog.setCanceledOnTouchOutside(false);
                sweetAlertDialog.show();
                break;

            case DISMISS_WAIT_DIALOG:
                if (sweetAlertDialog != null) {
                    sweetAlertDialog.dismiss();
                }
                break;

            case SHOW_VIEW_FOR_CONFIRM_RESULTS:
                setEnableState(false, false, false, true);
                break;
        }
    }

    private void setEnableState(boolean stopUsing, boolean quitTest,
                                boolean restart, boolean confirmResults) {
        btnStopUsing.setVisibility(stopUsing ? View.VISIBLE : View.GONE);
        btnQuitTest.setVisibility(quitTest ? View.VISIBLE : View.GONE);
        btnRestart.setVisibility(restart ? View.VISIBLE : View.GONE);
        btnConfirm.setVisibility(confirmResults ? View.VISIBLE : View.GONE);
    }

    @Override
    public void finish() {
        setResult(RadioResultActivity.BACK_TO_CHECK);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        toastSpeak(getString(R.string.testing_back_stop));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isTestFinished) {
            finish();
            presenter.stopNow();
        }
    }

    @Override
    public void showDisconnectForFinishTest() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.warning))
                .setContentText(getString(R.string.device_disconnect_save_result_hint))
                .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                presenter.finishTest();
            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();
    }

    @Override
    public void showViewForConfirmResults() {
        mHandler.sendEmptyMessage(SHOW_VIEW_FOR_CONFIRM_RESULTS);
    }

    private void showQuitDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.warning))
                .setContentText(getString(R.string.confirm_exit_test_hint))
                .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                presenter.quitTest();
            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();
    }

}
