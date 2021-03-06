package com.feipulai.host.activity.situp.pair;

import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.feipulai.common.view.DividerItemDecoration;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.jump_rope.adapter.DevicePairAdapter;
import com.orhanobut.logger.utils.LogUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public abstract class BasePairActivity extends BaseTitleActivity
        implements SitUpPairContract.View,
        DevicePairAdapter.OnItemClickListener {

    private static final int UPDATE_SPECIFIC_ITEM = 0x1;
    private static final String TAG = "BasePairActivity";
    @BindView(R.id.sw_auto_pair)
    Switch mSwAutoPair;
    @BindView(R.id.rv_pairs)
    RecyclerView mRvPairs;
    @BindView(R.id.ll_device_group_setting)
    LinearLayout llDeviceGroupSetting;

    private DevicePairAdapter mAdapter;
    private MyHandler mHandler = new MyHandler(this);
    private BasePairPresenter presenter;

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
        return builder.setTitle(R.string.sit_up_pair_title);
    }

    public abstract BasePairPresenter getPresenter();

    @Override
    public void initView(boolean isAutoPair, List pairs) {
        llDeviceGroupSetting.setVisibility(View.GONE);

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

    @Override
    public void onItemClick(int position) {
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
        LogUtils.operation("?????????????????????:position="+position);
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

    @OnClick({R.id.sw_auto_pair})
    public void btnOnClick(View v) {
        switch (v.getId()) {
            case R.id.sw_auto_pair:
                LogUtils.operation("???????????????????????????");
                presenter.changeAutoPair(mSwAutoPair.isChecked());
                break;
        }
    }

    public void showToast(String msg) {
        toastSpeak(msg);
    }

    @Override
    protected void onPause() {
        LogUtils.operation("BasePairActivity onPause");
        super.onPause();
        presenter.saveSettings();
        presenter.stopPair();
    }

}
