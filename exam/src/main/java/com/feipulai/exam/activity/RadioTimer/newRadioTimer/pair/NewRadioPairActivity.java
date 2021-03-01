package com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair;

import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.RadioTimer.RunTimerSetting;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.view.DividerItemDecoration;
import com.orhanobut.logger.utils.LogUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class NewRadioPairActivity extends BaseTitleActivity implements RadioContract.View, RadioPairAdapter.OnItemClickListener {

    @BindView(R.id.sw_auto_pair)
    Switch mSwAutoPair;
    @BindView(R.id.rv_pairs)
    public RecyclerView mRvPairs;
    @BindView(R.id.rv_end_pairs)
    public RecyclerView mEndRvPairs;
    @BindView(R.id.ll_beginning_point)
    public LinearLayout beginningPoint;
    @BindView(R.id.ll_ending_point)
    public LinearLayout endingPoint;

    public RadioPairAdapter mAdapter;
    public RadioPairAdapter mEndAdapter;
    RadioTimerPairPresenter presenter;
    @BindView(R.id.btn_helper)
    Button btnHelper;
    @BindView(R.id.btn_end_helper)
    Button btnEndHelper;
    private RunTimerSetting setting;
    private static final int UPDATE_SPECIFIC_ITEM = 0x1;
    private MyHandler mHandler = new MyHandler(this);
    private final int START_POINT = 0;
    private final int END_POINT = 1;

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
        presenter = new RadioTimerPairPresenter(this, this, Integer.parseInt(setting.getRunNum()) + 1);
        mSwAutoPair.setChecked(setting.isAutoPair());
        if (setting.getInterceptPoint() != 2) {
            presenter.setPoint(0);
            presenter.start(1, 0);
            beginningPoint.setVisibility(View.VISIBLE);
            mAdapter = new RadioPairAdapter(this, presenter.getPairs(), START_POINT);
            mRvPairs.setAdapter(mAdapter);
            mRvPairs.setClickable(true);
            mAdapter.setOnItemClickListener(this);
        }
        if (setting.getInterceptPoint() != 1) {
            endingPoint.setVisibility(View.VISIBLE);
            if (setting.getInterceptPoint() != 3) {
                presenter.setPoint(1);
                presenter.start(1, 1);
            }
            mEndAdapter = new RadioPairAdapter(this, presenter.getPairs(), END_POINT);
            mEndRvPairs.setLayoutManager(new GridLayoutManager(this, 5));
            mEndRvPairs.addItemDecoration(dividerItemDecoration);
            mEndRvPairs.setAdapter(mEndAdapter);
            if (setting.getInterceptPoint() != 3) {
                mEndAdapter.setSelected(0);
            } else {
                mEndAdapter.setSelected(-1);
                mEndAdapter.setAddNum(Integer.parseInt(setting.getRunNum()));
            }
            mEndRvPairs.setClickable(true);
            mEndAdapter.setOnItemClickListener(this);
        }
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("设备匹配");
    }

    @Override
    public void updateSpecificItem(int position, int point) {
        LogUtils.operation("正在匹配position:" + position + "point:" + point);
        Message msg = Message.obtain();
        msg.what = UPDATE_SPECIFIC_ITEM;
        msg.arg1 = position;
        msg.arg2 = point;
        mHandler.sendMessage(msg);
    }

    @Override
    public void select(int position, int point) {
        if (point == START_POINT) {
            mAdapter.setSelected(position);
            if (mEndAdapter != null) {
                mEndAdapter.setSelected(-1);
            }
        } else {
            mEndAdapter.setSelected(position);
            if (mAdapter != null) {
                mAdapter.setSelected(-1);
            }
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        if (mEndAdapter != null) {
            mEndAdapter.notifyDataSetChanged();
        }
        updateSpecificItem(position, point);
    }

    @Override
    public void showToast(String msg) {
        toastSpeak(msg);
    }

    @Override
    public void onItemClick(int position, int point) {
        LogUtils.operation("更改了设备position:" + position + "point:" + point);
        presenter.setPoint(point);
        presenter.changeFocusPosition(position, point);
    }

    @OnClick({R.id.sw_auto_pair,R.id.btn_helper,R.id.btn_end_helper})
    public void btnOnClick(View v) {
        switch (v.getId()) {
            case R.id.sw_auto_pair:
                LogUtils.operation("勾选了自动匹配");
                presenter.changeAutoPair(mSwAutoPair.isChecked());
                break;
            case R.id.btn_helper:
                LogUtils.operation("勾选了起始点辅助匹配");
                presenter.changeFocusPosition(-1,4);
                btnHelper.setEnabled(false);
                if (mEndAdapter!= null){
                    mEndAdapter.setSelected(-1);
                    mEndAdapter.notifyDataSetChanged();
                }
                if (mAdapter!= null){
                    mAdapter.setSelected(-1);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.btn_end_helper:
                LogUtils.operation("勾选了终点辅助匹配");
                presenter.changeFocusPosition(-1,5);
                btnEndHelper.setEnabled(false);
                if (mEndAdapter!= null){
                    mEndAdapter.setSelected(-1);
                    mEndAdapter.notifyDataSetChanged();
                }
                if (mAdapter!= null){
                    mAdapter.setSelected(-1);
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }


    @Override
    public void handleMessage(Message msg) {

        switch (msg.what) {
            case UPDATE_SPECIFIC_ITEM:
                switch (msg.arg2){
                    case 0:
                        mAdapter.notifyItemChanged(msg.arg1);
                        break;
                    case 1:
                        mEndAdapter.notifyItemChanged(msg.arg1);
                        break;
                    case 4:
                        btnHelper.setText("起点辅助拦截器√");
                        btnHelper.setEnabled(true);
                        break;
                    case 5:
                        btnEndHelper.setText("终点辅助拦截器√");
                        btnEndHelper.setEnabled(true);
                        break;
                }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.saveSettings();
        presenter.stopPair();
    }

}
