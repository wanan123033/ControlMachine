package com.feipulai.exam.activity.situp.base_pair;

import android.content.Intent;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.adapter.DevicePairAdapter;
import com.feipulai.exam.activity.sport_timer.SportInitWayActivity;
import com.feipulai.exam.view.DividerItemDecoration;
import com.orhanobut.logger.utils.LogUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public abstract class SitPullPairActivity extends BaseTitleActivity
        implements SitPullUpPairContract.View,
        DevicePairAdapter.OnItemClickListener {

    private static final int UPDATE_SPECIFIC_ITEM = 0x1;
    private static final String TAG = "SitPullPairActivity";
    @BindView(R.id.sw_auto_pair)
    Switch mSwAutoPair;
    @BindView(R.id.rv_pairs)
    public RecyclerView mRvPairs;
    @BindView(R.id.ll_device_group_setting)
    LinearLayout llDeviceGroupSetting;
    @BindView(R.id.tv_init_way)
    public TextView txtInitWay;
    public DevicePairAdapter mAdapter;
    private MyHandler mHandler = new MyHandler(this);
    public SitPullUpPairPresenter presenter;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_radio_pair;
    }

    @Override
    protected void initData() {
        presenter = getPresenter();
        presenter.start();
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("设备匹配");
    }

    public abstract SitPullUpPairPresenter getPresenter();

    @Override
    public void initView(boolean isAutoPair, List pairs) {
        llDeviceGroupSetting.setVisibility(View.GONE);
        txtInitWay.setVisibility(View.GONE);
        mSwAutoPair.setChecked(isAutoPair);

        mRvPairs.setLayoutManager(new GridLayoutManager(this, 5));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        dividerItemDecoration.setDrawBorderTopAndBottom(true);
        dividerItemDecoration.setDrawBorderLeftAndRight(true);
        mRvPairs.addItemDecoration(dividerItemDecoration);

        mAdapter = new DevicePairAdapter(this, pairs);
        mRvPairs.setAdapter(mAdapter);
        mRvPairs.setClickable(true);
        mAdapter.setOnItemClickListener(this);
    }

    public void setInitWayVisible(){
        txtInitWay.setVisibility(View.VISIBLE);
    }
    @Override
    public void onItemClick(int position) {
        LogUtils.operation("更改了设备ID deviceId 在配对");
        presenter.changeFocusPosition(position);
    }

    @Override
    public void updateSpecificItem(int position) {
        Message msg = Message.obtain();
        msg.what = UPDATE_SPECIFIC_ITEM;
        msg.arg1 = position;
        mHandler.sendMessage(msg);
    }

    @Override
    public void select(int position) {
        int oldSelectPosition = mAdapter.getSelected();
        mAdapter.setSelected(position);
        updateSpecificItem(oldSelectPosition);
        updateSpecificItem(position);
    }

    @Override
    public void handleMessage(Message msg) {

        switch (msg.what) {
            case UPDATE_SPECIFIC_ITEM:
                mAdapter.notifyItemChanged(msg.arg1);
                break;
        }
    }

    @OnClick({R.id.sw_auto_pair,R.id.tv_init_way})
    public void btnOnClick(View v) {
        switch (v.getId()) {
            case R.id.sw_auto_pair:
                LogUtils.operation("勾选了自动匹配");
                presenter.changeAutoPair(mSwAutoPair.isChecked());
                break;
            case R.id.tv_init_way:
                startActivity(new Intent(this, SportInitWayActivity.class));
                break;
        }
    }

    public void showToast(String msg) {
        toastSpeak(msg);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.life("SitPullPairActivity onPause");
        presenter.saveSettings();
        presenter.stopPair();
    }

}
