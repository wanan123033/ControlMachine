package com.feipulai.exam.activity.RadioTimer.newRadioTimer;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.RadioTimer.RunTimerSetting;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.view.DividerItemDecoration;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RadioDeviceDetailActivity extends BaseTitleActivity implements DeviceDetailsAdapter.OnItemClickListener, RadioManager.OnRadioArrivedListener {

    @BindView(R.id.rv_pairs)
    public RecyclerView mRvPairs;
    @BindView(R.id.rv_end_pairs)
    public RecyclerView mEndRvPairs;
    @BindView(R.id.ll_beginning_point)
    public LinearLayout beginningPoint;
    @BindView(R.id.ll_ending_point)
    public LinearLayout endingPoint;

    public DeviceDetailsAdapter mAdapter;
    public DeviceDetailsAdapter mEndAdapter;
    //    @BindView(R.id.btn_helper)
//    Button btnHelper;
//    @BindView(R.id.btn_end_helper)
//    Button btnEndHelper;
    private RunTimerSetting setting;
    private static final int UPDATE_SPECIFIC_ITEM = 0x1;
    private final int START_POINT = 0;
    private final int END_POINT = 1;
    private int selectPoint = 0;
    SportTimerManger sportTimerManger;
    private ScheduledExecutorService checkService;
    private int deviceNum;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_radio_device_deail;
    }

    @Override
    protected void initData() {
        mRvPairs.setLayoutManager(new GridLayoutManager(this, 5));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        dividerItemDecoration.setDrawBorderTopAndBottom(true);
        dividerItemDecoration.setDrawBorderLeftAndRight(true);
        sportTimerManger = new SportTimerManger();
        checkService = Executors.newSingleThreadScheduledExecutor();
        mRvPairs.addItemDecoration(dividerItemDecoration);
        setting = SharedPrefsUtil.loadFormSource(this, RunTimerSetting.class);
        deviceNum = Integer.parseInt(setting.getRunNum()) + 1;
//        presenter = new RadioTimerPairPresenter(this, this, Integer.parseInt(setting.getRunNum()));
        if (setting.getInterceptPoint() != 2) {//起点拦截
            selectPoint = 0;
            beginningPoint.setVisibility(View.VISIBLE);
            mAdapter = new DeviceDetailsAdapter(this, newPairs(deviceNum), START_POINT);
            mRvPairs.setAdapter(mAdapter);
            mRvPairs.setClickable(true);
            mAdapter.setOnItemClickListener(this);
            mAdapter.setSelected(-1);
        }
        if (setting.getInterceptPoint() != 1) {//终点拦截
            endingPoint.setVisibility(View.VISIBLE);
            if (setting.getInterceptPoint() == 2) {
                selectPoint = 1;
            }
            mEndAdapter = new DeviceDetailsAdapter(this, newPairs(deviceNum), END_POINT);
            mEndRvPairs.setLayoutManager(new GridLayoutManager(this, 5));
            mEndRvPairs.addItemDecoration(dividerItemDecoration);
            if (setting.getInterceptPoint() == 3) {
                for (StuDevicePair stuPair : mEndAdapter.stuPairs) {
                    if (stuPair.getBaseDevice().getDeviceId() != 0) {
                        stuPair.getBaseDevice().setDeviceId(stuPair.getBaseDevice().getDeviceId() + deviceNum - 1);
                    }

                }
            }
            mEndAdapter.setSelected(-1);
            mEndRvPairs.setAdapter(mEndAdapter);
            mEndRvPairs.setClickable(true);
            mEndAdapter.setOnItemClickListener(this);
        }

        checkService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                intervalRun();
            }
        }, 100, 400, TimeUnit.MILLISECONDS);
    }

    private void intervalRun() {
        if (setting.getInterceptPoint() != 3) {
            for (int i = 0; i < deviceNum; i++) {
                sportTimerManger.connect(i, SettingHelper.getSystemSetting().getHostId());
            }
        } else {
            int num = deviceNum * 2 - 2;
            for (int i = 0; i < num; i++) {
                sportTimerManger.connect(i, SettingHelper.getSystemSetting().getHostId());
            }
        }

    }

    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {

        return builder.setTitle("设备详情");
    }

    @Override
    public void onItemClick(int position, int point) {
        int oldSelectPosition;
        if (selectPoint != point) {
            if (point == START_POINT) {
                oldSelectPosition = mEndAdapter.getSelected();
                mEndAdapter.setSelected(-1);
                updateSpecificItem(oldSelectPosition, selectPoint);
                mAdapter.setSelected(position);
            } else {
                oldSelectPosition = mAdapter.getSelected();
                mAdapter.setSelected(-1);
                updateSpecificItem(oldSelectPosition, selectPoint);
                mEndAdapter.setSelected(position);
            }
            selectPoint = point;
        } else {
            if (point == START_POINT) {
                oldSelectPosition = mAdapter.getSelected();
                mAdapter.setSelected(position);
            } else {
                oldSelectPosition = mEndAdapter.getSelected();
                mEndAdapter.setSelected(position);
            }
            updateSpecificItem(oldSelectPosition, point);
        }
        updateSpecificItem(position, point);
    }

    public void updateSpecificItem(int position, int point) {
        LogUtils.operation("正在匹配position:" + position + "point:" + point);
        Message msg = Message.obtain();
        msg.what = UPDATE_SPECIFIC_ITEM;
        msg.arg1 = position;
        msg.arg2 = point;
        mHandler.sendMessage(msg);
    }

    private List<StuDevicePair> newPairs(int size) {
        List<StuDevicePair> pairs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            StuDevicePair pair = new StuDevicePair();
            BaseDeviceState state = new BaseDeviceState(BaseDeviceState.STATE_DISCONNECT, i);
            pair.setBaseDevice(state);
            pairs.add(pair);
        }
        return pairs;
    }

    @Override
    public void onRadioArrived(Message msg) {
        switch (msg.what) {
            case SerialConfigs.SPORT_TIMER_CONNECT:
                if (msg.obj instanceof SportResult) {
                    mHandler.sendMessage(msg);
                }
                break;

        }
    }

    private MyHandler mHandler = new MyHandler(this);

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case SerialConfigs.SPORT_TIMER_CONNECT:
                SportResult sportResult = (SportResult) msg.obj;
                if (sportResult.getDeviceId() < deviceNum) {
                    updateDevice(sportResult, mAdapter);
                } else {
                    updateDevice(sportResult, mEndAdapter);
                }
                break;
            case UPDATE_SPECIFIC_ITEM:
                switch (msg.arg2) {
                    case 0:
                        mAdapter.notifyItemChanged(msg.arg1);
                        break;
                    case 1:
                        mEndAdapter.notifyItemChanged(msg.arg1);
                        break;
                }
                break;
        }
    }

    private void updateDevice(SportResult sportResult, DeviceDetailsAdapter adapter) {
        for (StuDevicePair stuPair : adapter.stuPairs) {
            if (stuPair.getBaseDevice().getDeviceId() == sportResult.getDeviceId()) {
                stuPair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                stuPair.setBattery(sportResult.getBattery());
                adapter.notifyItemChanged(sportResult.getDeviceId());
            }
        }
    }



    @OnClick({R.id.btn_change_bad})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_change_bad:

                break;
        }
    }
}
