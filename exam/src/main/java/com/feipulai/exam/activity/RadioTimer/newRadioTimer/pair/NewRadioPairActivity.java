package com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair;

import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

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
    @BindView(R.id.tv_force_start)
    TextView tvForceStart;
    public RadioPairAdapter mAdapter;
    public RadioPairAdapter mEndAdapter;
    private RadioTimerPairPresenter presenter;
//    @BindView(R.id.btn_helper)
//    Button btnHelper;
//    @BindView(R.id.btn_end_helper)
//    Button btnEndHelper;
    private RunTimerSetting setting;
    private static final int UPDATE_SPECIFIC_ITEM = 0x1;
    private MyHandler mHandler = new MyHandler(this);
    private final int START_POINT = 0;
    private final int END_POINT = 1;
    private int selectPoint = 0;
    private boolean matchStart;
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
        presenter = new RadioTimerPairPresenter(this, this, Integer.parseInt(setting.getRunNum()));
        mSwAutoPair.setChecked(setting.isAutoPair());
        if (setting.getInterceptPoint() != 2) {//????????????
            presenter.start(0, 0);
            selectPoint = 0;
            beginningPoint.setVisibility(View.VISIBLE);
            mAdapter = new RadioPairAdapter(this, presenter.getPairs(), START_POINT);
            mRvPairs.setAdapter(mAdapter);
            mRvPairs.setClickable(true);
            mAdapter.setOnItemClickListener(this);
        }
        if (setting.getInterceptPoint() != 1) {//????????????
            endingPoint.setVisibility(View.VISIBLE);
            if (setting.getInterceptPoint() == 2) {
                presenter.start(0, 1);
                selectPoint = 1;
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
        return builder.setTitle("????????????");
    }

    @Override
    public void updateSpecificItem(int position, int point) {
        if (matchStart){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvForceStart.setText("???????????????");
                }
            });
            return;
        }
        LogUtils.all("????????????position:" + position + "point:" + point);
        Message msg = Message.obtain();
        msg.what = UPDATE_SPECIFIC_ITEM;
        msg.arg1 = position;
        msg.arg2 = point;
        mHandler.sendMessage(msg);
    }

    @Override
    public void select(int position, int point) {
        matchStart = false;
        int oldSelectPosition;
        if (selectPoint != point){
            if (point == START_POINT){
                oldSelectPosition  = mEndAdapter.getSelected();
                mEndAdapter.setSelected(-1);
                updateSpecificItem(oldSelectPosition,selectPoint);
                mAdapter.setSelected(position);
            }else {
                oldSelectPosition  = mAdapter.getSelected();
                mAdapter.setSelected(-1);
                updateSpecificItem(oldSelectPosition,selectPoint);
                mEndAdapter.setSelected(position);
            }
            selectPoint = point;
        }else {
            if (point == START_POINT){
                oldSelectPosition  = mAdapter.getSelected();
                mAdapter.setSelected(position);
            }else {
                oldSelectPosition  = mEndAdapter.getSelected();
                mEndAdapter.setSelected(position);
            }
            updateSpecificItem(oldSelectPosition,point);
        }
        updateSpecificItem(position,point);
    }

    @Override
    public void showToast(String msg) {
        toastSpeak(msg);
    }

    @Override
    public void onItemClick(int position, int point) {
        matchStart = false;
        LogUtils.all("???????????????position:" + position + "point:" + point);
        presenter.changeFocusPosition(position, point);
    }

    @OnClick({R.id.sw_auto_pair,R.id.tv_force_start})//,R.id.btn_helper,R.id.btn_end_helper
    public void btnOnClick(View v) {
        switch (v.getId()) {
            case R.id.sw_auto_pair:
                LogUtils.all("?????????????????????");
                presenter.changeAutoPair(mSwAutoPair.isChecked());
                break;
//            case R.id.btn_helper:
//                LogUtils.operation("??????????????????????????????");
//                presenter.changeFocusPosition(-1,4);
//                btnHelper.setEnabled(false);
//                if (mEndAdapter!= null){
//                    mEndAdapter.setSelected(-1);
//                    mEndAdapter.notifyDataSetChanged();
//                }
//                if (mAdapter!= null){
//                    mAdapter.setSelected(-1);
//                    mAdapter.notifyDataSetChanged();
//                }
//                break;
//            case R.id.btn_end_helper:
//                LogUtils.operation("???????????????????????????");
//                presenter.changeFocusPosition(-1,5);
//                btnEndHelper.setEnabled(false);
//                if (mEndAdapter!= null){
//                    mEndAdapter.setSelected(-1);
//                    mEndAdapter.notifyDataSetChanged();
//                }
//                if (mAdapter!= null){
//                    mAdapter.setSelected(-1);
//                    mAdapter.notifyDataSetChanged();
//                }
//                break;
            case R.id.tv_force_start:
                if (mEndAdapter !=null ){
                    mEndAdapter.setSelected(-1);
                    mEndAdapter.notifyDataSetChanged();
                }
                matchStart = true;
                presenter.setPair(90);

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
//                        final int pos = msg.arg1;
//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                presenter.setState(pos);
//                            }
//                        },100);

                        break;
                    case 1:
                        mEndAdapter.notifyItemChanged(msg.arg1);
                        break;
//                    case 4:
//                        btnHelper.setText("????????????????????????");
//                        btnHelper.setEnabled(true);
//                        break;
//                    case 5:
//                        btnEndHelper.setText("????????????????????????");
//                        btnEndHelper.setEnabled(true);
//                        break;
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
