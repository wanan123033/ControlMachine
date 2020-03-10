package com.feipulai.host.activity.pullup.check;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.common.view.DividerItemDecoration;
import com.feipulai.common.view.WaitDialog;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseCheckActivity;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.adapter.CheckPairAdapter;
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.jump_rope.base.check.RadioCheckContract;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.activity.jump_rope.bean.TestCache;
import com.feipulai.host.activity.setting.LEDSettingActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
@Deprecated
public abstract class BaseUpCheckActivity<Setting>
        extends BaseCheckActivity
        implements RadioCheckContract.View<Setting>,
        CheckPairAdapter.OnItemClickListener,
        View.OnClickListener {
    protected RadioCheckContract.Presenter presenter;

    private static final int UPDATE_STATES = 0x1;
    private static final int UPDATE_SPECIFIC_ITEM = 0x2;
    private static final String RESUME_USE = "恢复使用";
    private static final String STOP_USE = "暂停使用";
    private CheckPairAdapter mAdapter;
    private Handler mHandler = new MyHandler(this);
    private WaitDialog changBadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);// 不知道为什么,必须这么搞
        presenter = getPresenter();
        presenter.start();
    }


    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        boolean isTestNameEmpty = TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName());
        String title = TestConfigs.machineNameMap.get(machineCode)
                + SettingHelper.getSystemSetting().getHostId() + "号机"
                + (isTestNameEmpty ? "" : ("-" + SettingHelper.getSystemSetting().getTestName()));
        builder.setTitle(title);


        builder.addRightText("项目设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BaseUpCheckActivity.this, getProjectSettingActivity()));
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BaseUpCheckActivity.this, getProjectSettingActivity()));
            }
        });

        return builder;
    }

    @Override
    public void onClick(View v) {
        if (v == getLedSettingView()) {// LED设置
            startActivity(new Intent(this, LEDSettingActivity.class));
        } else if (v == getStartTestView()) {// 开始测试
            presenter.startTest();
        } else if (v == getPairView()) {// 设备配对
            startActivity(new Intent(this, getPairActivity()));
        } else if (v == getChangeBadView()) {// 故障更换
            presenter.changeBadDevice();
        } else if (v == getStopUseView()) {// 暂停/恢复使用
            String text = getStopUseView().getText().toString().trim();
            if (text.equals(RESUME_USE)) {
                presenter.resumeUse();
                getStopUseView().setText(STOP_USE);
            } else {
                presenter.stopUse();
                getStopUseView().setText(RESUME_USE);
            }
        } else if (v == getDeleteAllView()) {// 删除所有
//            presenter.deleteAll();
            showKillAllWarning();
        } else if (v == getDeleteStuView()) {// 删除考生
            presenter.deleteStudent();
        }
    }

    public void showKillAllWarning() {
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

    @Override
    public void onCheckIn(Student student) {
        presenter.onCheckIn(student);
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
    public void initView(Setting setting, List<StuDevicePair> pairs) {
        mAdapter = new CheckPairAdapter(this, pairs);
        mAdapter.setOnItemClickListener(this);
        getRvPairs().setAdapter(mAdapter);
        setResultView();

        getRvPairs().setLayoutManager(new GridLayoutManager(this, 5));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        dividerItemDecoration.setDrawBorderTopAndBottom(true);
        dividerItemDecoration.setDrawBorderLeftAndRight(true);
        getRvPairs().addItemDecoration(dividerItemDecoration);
        getRvPairs().setHasFixedSize(true);
        getRvPairs().setClickable(true);

        getStopUseView().setOnClickListener(this);
        getChangeBadView().setOnClickListener(this);
        getDeleteAllView().setOnClickListener(this);
        getDeleteStuView().setOnClickListener(this);
        getStartTestView().setOnClickListener(this);
        getLedSettingView().setOnClickListener(this);
        getPairView().setOnClickListener(this);
    }

    @Override
    public void updateSpecificItem(int position) {
        Message msg = Message.obtain();
        msg.what = UPDATE_SPECIFIC_ITEM;
        msg.arg1 = position;
        mHandler.sendMessage(msg);
    }

    @Override
    public void changeBadSuccess() {
        changBadDialog.dismiss();
        toastSpeak(getString(R.string.replace_success));
    }


    @Override
    public void showStuInfo(Student student, RoundResult results) {
        InteractUtils.showStuInfo(getStuDetailLayout(), student, results);
    }

    @Override
    public void showChangeBadDialog() {
        changBadDialog = new WaitDialog(this);
        changBadDialog.setCanceledOnTouchOutside(false);
        changBadDialog.show();
        changBadDialog.setTitle(getChangeBadTitle());
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
        Intent intent = new Intent(this, getTestActivity());
        startActivityForResult(intent, 1);
    }

    @Override
    public void refreshPairs(List<StuDevicePair> pairs) {
        mAdapter = new CheckPairAdapter(this, pairs);
        mAdapter.setOnItemClickListener(this);
        getRvPairs().setAdapter(mAdapter);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TestCache.getInstance().clear();
        presenter.finishGetStateAndDisplay();
    }

    protected abstract RadioCheckContract.Presenter getPresenter();

    protected abstract void onConflictItemClicked();

    protected abstract View getChangeBadView();

    protected abstract View getLedSettingView();

    protected abstract View getPairView();

    protected abstract View getStartTestView();

    protected abstract Class<?> getProjectSettingActivity();

    protected abstract RecyclerView getRvPairs();

    protected abstract TextView getStopUseView();

    protected abstract View getDeleteStuView();

    protected abstract View getDeleteAllView();

    protected abstract LinearLayout getStuDetailLayout();

    protected abstract String getChangeBadTitle();

    protected abstract Class<? extends Activity> getTestActivity();

    protected abstract Class<? extends Activity> getPairActivity();

    protected abstract void setResultView();

}
