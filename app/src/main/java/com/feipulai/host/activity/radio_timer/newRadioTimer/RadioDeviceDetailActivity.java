package com.feipulai.host.activity.radio_timer.newRadioTimer;

import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.WaitDialog;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.activity.radio_timer.RunTimerSetting;
import com.feipulai.host.activity.radio_timer.newRadioTimer.pair.RadioContract;
import com.feipulai.host.view.DividerItemDecoration;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class RadioDeviceDetailActivity extends BaseTitleActivity implements DeviceDetailsAdapter.OnItemClickListener, RadioContract.View, ChangeDeviceUtil.ResponseArrivedListener {

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
    private static final int UPDATE_DIALOG = 0x2;
    private final int START_POINT = 0;
    private final int END_POINT = 1;
    private int selectPoint = 0;
    private int deviceNum;
    private ChangeDeviceUtil changeDevice;
    private int selectDeviceId;
    private WaitDialog changBadDialog;

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


        mRvPairs.addItemDecoration(dividerItemDecoration);
        setting = SharedPrefsUtil.loadFormSource(this, RunTimerSetting.class);
        deviceNum = Integer.parseInt(setting.getRunNum()) + 1;
        changeDevice = new ChangeDeviceUtil(this, setting);
        changeDevice.setCheck(true);
        changeDevice.setCheckState();
        changeDevice.setListener(this);
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
                selectDeviceId = mAdapter.stuPairs.get(position).getBaseDevice().getDeviceId();
            } else {
                oldSelectPosition = mAdapter.getSelected();
                mAdapter.setSelected(-1);
                updateSpecificItem(oldSelectPosition, selectPoint);
                mEndAdapter.setSelected(position);
                selectDeviceId = mEndAdapter.stuPairs.get(position).getBaseDevice().getDeviceId();
            }
            selectPoint = point;
        } else {
            if (point == START_POINT) {
                oldSelectPosition = mAdapter.getSelected();
                mAdapter.setSelected(position);
                selectDeviceId = mAdapter.stuPairs.get(position).getBaseDevice().getDeviceId();
            } else {
                oldSelectPosition = mEndAdapter.getSelected();
                mEndAdapter.setSelected(position);
                selectDeviceId = mEndAdapter.stuPairs.get(position).getBaseDevice().getDeviceId();
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

    @Override
    public void select(int position, int point) {
        updateSpecificItem(position, point);
        mHandler.sendEmptyMessage(UPDATE_DIALOG);
    }

    @Override
    public void showToast(String msg) {
        toastSpeak(msg);
        changeDevice.setCheck(true);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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


    private MyHandler mHandler = new MyHandler(this);

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case SerialConfigs.SPORT_TIMER_CONNECT:
                SportResult sportResult = (SportResult) msg.obj;
                updateDevice(sportResult);
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
            case UPDATE_DIALOG:
                changeDevice.setCheck(true);
                if (changBadDialog != null && changBadDialog.isShowing()) {
                    changBadDialog.dismiss();
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void updateDevice(SportResult sportResult) {
        if (mEndAdapter != null) {
            for (StuDevicePair stuPair : mEndAdapter.stuPairs) {
                if (stuPair.getBaseDevice().getDeviceId() == sportResult.getDeviceId()) {
                    stuPair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                    stuPair.setBattery(sportResult.getBattery());
                    if (sportResult.getDeviceId() < mEndAdapter.stuPairs.size()) {
                        mEndAdapter.notifyItemChanged(sportResult.getDeviceId());
                    } else {
                        mEndAdapter.notifyItemChanged(sportResult.getDeviceId() - mEndAdapter.stuPairs.size() + 1);
                    }

                }
            }
        }

        if (mAdapter != null) {
            for (StuDevicePair stuPair : mAdapter.stuPairs) {
                stuPair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                stuPair.setBattery(sportResult.getBattery());
                if (stuPair.getBaseDevice().getDeviceId() == sportResult.getDeviceId()) {
                    mAdapter.notifyItemChanged(sportResult.getDeviceId());
                }
            }
        }

    }


    @OnClick({R.id.btn_change_bad})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_change_bad:
                changeDevice.setCheck(false);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                showChangeBadDialog();
                changeDevice.start(selectDeviceId, selectPoint);
                break;
        }
    }

    @Override
    public void onReceiveResult(Message msg) {
        mHandler.sendMessage(msg);
    }

    @Override
    protected void onStop() {
        super.onStop();
        changeDevice.setCheck(false);
        changeDevice.release();
    }

    public void showChangeBadDialog() {
        changBadDialog = new WaitDialog(this);
        changBadDialog.setCanceledOnTouchOutside(false);
        changBadDialog.setCancelable(false);
        changBadDialog.show();
        // 必须在dialog显示出来后再调用
        changBadDialog.setTitle("请重启待连接设备");
        changBadDialog.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changBadDialog.dismiss();
                changeDevice.cancelChangeBad();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        changeDevice = null;
    }
}
