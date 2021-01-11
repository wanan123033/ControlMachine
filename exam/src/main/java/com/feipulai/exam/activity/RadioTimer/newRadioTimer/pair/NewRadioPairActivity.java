package com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Switch;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.RadioTimer.RunTimerSetting;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.adapter.DevicePairAdapter;
import com.feipulai.exam.view.DividerItemDecoration;
import com.orhanobut.logger.utils.LogUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class NewRadioPairActivity extends BaseTitleActivity implements RadioContract.View, DevicePairAdapter.OnItemClickListener {

    @BindView(R.id.sw_auto_pair)
    Switch mSwAutoPair;
    @BindView(R.id.rv_pairs)
    public RecyclerView mRvPairs;
    @BindView(R.id.rv_end_pairs)
    public RecyclerView mEndRvPairs;
    public DevicePairAdapter mAdapter;
    public DevicePairAdapter mEndAdapter;
    RadioTimerPairPresenter presenter;
    private  RunTimerSetting setting;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_new_radio_pair;
    }

    @Override
    protected void initData() {
        mRvPairs.setLayoutManager(new GridLayoutManager(this, 5));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        dividerItemDecoration.setDrawBorderTopAndBottom(true);
        dividerItemDecoration.setDrawBorderLeftAndRight(true);
        mRvPairs.addItemDecoration(dividerItemDecoration);

        setting = SharedPrefsUtil.loadFormSource(this, RunTimerSetting.class);
        presenter = new RadioTimerPairPresenter(this,this,Integer.parseInt(setting.getRunNum())+1);
        presenter.start(1);
        mAdapter = new DevicePairAdapter(this,presenter.getPairs());
        mRvPairs.setAdapter(mAdapter);
        mRvPairs.setClickable(true);
        mAdapter.setOnItemClickListener(this);

        mEndRvPairs.setLayoutManager(new GridLayoutManager(this, 5));
        mEndRvPairs.addItemDecoration(dividerItemDecoration);
        mEndAdapter = new DevicePairAdapter(this,presenter.getPairs());
        mEndAdapter.setSelected(-1);
        mEndRvPairs.setAdapter(mEndAdapter);
        mEndRvPairs.setClickable(true);
        mEndAdapter.setOnItemClickListener(this);

    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("设备匹配");
    }

    @Override
    public void updateSpecificItem(int focusPosition) {
        LogUtils.operation("更改了设备ID deviceId 在配对");
        presenter.changeFocusPosition(focusPosition);
    }

    @Override
    public void select(int position) {
        int oldSelectPosition = mAdapter.getSelected();
        mAdapter.setSelected(position);
        updateSpecificItem(oldSelectPosition);
        updateSpecificItem(position);
    }

    @Override
    public void showToast(String msg) {
        toastSpeak(msg);
    }

    @Override
    public void onItemClick(int position) {
        LogUtils.operation("更改了设备ID deviceId 在配对");
        presenter.changeFocusPosition(position);
    }

    @OnClick({R.id.sw_auto_pair})
    public void btnOnClick(View v) {
        switch (v.getId()) {
            case R.id.sw_auto_pair:
                LogUtils.operation("勾选了自动匹配");
                presenter.changeAutoPair(mSwAutoPair.isChecked());
                break;
        }
    }
}
