package com.feipulai.exam.activity.situp.newSitUp;

import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.feipulai.common.jump_rope.task.GetDeviceStatesTask;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.manager.ShoulderManger;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.ShoulderResult;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.view.DividerItemDecoration;
import com.feipulai.exam.view.WaitDialog;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 更换设备设备状态
 */
public class DeviceChangeActivity extends BaseTitleActivity implements GetDeviceStatesTask.OnGettingDeviceStatesListener, RadioManager.OnRadioArrivedListener, SitPullLinker.SitPullPairListener {

    private static final int UPDATE_SPECIFIC_ITEM = 1;
    @BindView(R.id.rv_pairs)
    RecyclerView mRvPairs;
    @BindView(R.id.btn_change_bad)
    Button btnChangeBad;
    private List<DeviceCollect> deviceCollects = new ArrayList<>();
    private DeviceChangeAdapter mAdapter;
    private GetDeviceStatesTask statesTask;
    private SitUpSetting setting;
    SitPushUpManager deviceManager;
    protected final int TARGET_FREQUENCY = SettingHelper.getSystemSetting().getUseChannel();
    private ExecutorService mExecutor;
    private ShoulderManger shoulderManger;
    private int[] sitUpUpdate;
    private int[] shoulder;
    private WaitDialog changBadDialog;
    private SitPullLinker linker;
    private boolean mLinking;
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_device_change;
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, SitUpSetting.class);
        sitUpUpdate = new int[setting.getDeviceSum()];
        shoulder = new int[setting.getDeviceSum()];
        int deviceSum = setting.getDeviceSum();
        for (int i = 0; i < deviceSum; i++) {
            ShoulderResult shoulderResult = new ShoulderResult();
            shoulderResult.setDeviceId(i + 1);
            SitPushUpStateResult stateResult = new SitPushUpStateResult();
            stateResult.setDeviceId(i + 1);
            DeviceCollect deviceCollect = new DeviceCollect(stateResult, shoulderResult);
            deviceCollects.add(deviceCollect);
        }
        mRvPairs.setLayoutManager(new GridLayoutManager(this, 5));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        dividerItemDecoration.setDrawBorderTopAndBottom(true);
        dividerItemDecoration.setDrawBorderLeftAndRight(true);
        mRvPairs.addItemDecoration(dividerItemDecoration);
        mAdapter = new DeviceChangeAdapter(this, deviceCollects);
        mRvPairs.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new DeviceChangeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int viewId, int position) {
                mAdapter.setSelected(position);

                switch (viewId) {
                    case R.id.tv_arm:
                        mAdapter.setSelectDevice(2);
                        break;
                    case R.id.tv_sit_up:
                        mAdapter.setSelectDevice(1);
                        break;
                }
            }
        });

        deviceManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP);
        shoulderManger = new ShoulderManger();
        statesTask = new GetDeviceStatesTask(this);
        RadioManager.getInstance().setOnRadioArrived(this);
        RadioChannelCommand command = new RadioChannelCommand(TARGET_FREQUENCY);
        LogUtils.normal(command.getCommand().length + "---" + StringUtility.bytesToHexString(command.getCommand()) + "---切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command));
        mExecutor = Executors.newFixedThreadPool(1);
        mExecutor.execute(statesTask);

    }

    @Override
    protected void onResume() {
        super.onResume();
        statesTask.resume();
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("设备详情");
    }


    @OnClick({R.id.btn_change_bad})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_change_bad:
                statesTask.pause();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (linker == null) {
                    linker = new SitPullLinker(machineCode, TARGET_FREQUENCY, this);
                }
                linker.startPair(mAdapter.getSelected()+1);
                mLinking = true;
                showChangeBadDialog();
                break;
        }
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
                cancelChangeBad();
            }
        });
    }

    public void cancelChangeBad() {
        mLinking = false;
        if (linker != null) {
            linker.cancelPair();
        }
        if (changBadDialog!= null && changBadDialog.isShowing()){
            changBadDialog.dismiss();
        }
        statesTask.resume();
    }

    @Override
    public void onGettingState(int position) {
        deviceManager.getState(position + 1, setting.getAngle());
        shoulderManger.getState(position + 1, SettingHelper.getSystemSetting().getHostId());
    }

    @Override
    public void onStateRefreshed() {
        for (int i = 0; i < setting.getDeviceSum(); i++) {
            if (shoulder[i] > 1 && shoulder[i]<3) {
                deviceCollects.get(i).getShoulderResult().setState(BaseDeviceState.STATE_DISCONNECT);
                updateDevice(i + 1, 2);
            }
            shoulder[i]++;
            if (sitUpUpdate[i]> 1 && sitUpUpdate[i]<3) {
                deviceCollects.get(i).getSitPushUpStateResult().setState(BaseDeviceState.STATE_DISCONNECT);
                updateDevice(i + 1, 1);
            }
            sitUpUpdate[i]++;
        }
    }

    @Override
    public int getDeviceCount() {
        return setting.getDeviceSum();
    }

    @Override
    public void onRadioArrived(Message msg) {
        if (mLinking && linker.onRadioArrived(msg)) {
            return;
        }
        if (msg.obj instanceof SitPushUpStateResult) {
            Logger.i("仰卧起坐" + msg.obj.toString());
            SitPushUpStateResult stateResult = (SitPushUpStateResult) msg.obj;
            setState(stateResult);
        } else if (msg.obj instanceof ShoulderResult) {
            Logger.i("肩胛模式" + msg.obj.toString());
            ShoulderResult shoulder = (ShoulderResult) msg.obj;
            setState(shoulder);
        }
    }

    private void setState(SitPushUpStateResult stateResult) {
        setSitUpState(stateResult.getDeviceId(), stateResult.getState(), stateResult.getBatteryLeft());
    }

    private void setState(ShoulderResult stateResult) {
        setShoulderState(stateResult.getDeviceId(), stateResult.getState(), stateResult.getBattery());
    }

    private void setSitUpState(int deviceId, int deviceState, int batteryLeft) {
        if (deviceId > setting.getDeviceSum()) {
            return;
        }
        sitUpUpdate[deviceId - 1] = 1;
        int oldState = deviceCollects.get(deviceId - 1).getSitPushUpStateResult().getState();
        if (oldState != deviceState) {
            if (deviceState != 4) {
                deviceCollects.get(deviceId - 1).getSitPushUpStateResult().setState(BaseDeviceState.STATE_FREE);
            } else {
                deviceCollects.get(deviceId - 1).getSitPushUpStateResult().setState(BaseDeviceState.STATE_STOP_USE);
            }
            updateDevice(deviceId, 1);
//                mAdapter.notifyItemChanged(deviceId-1);
        }
        if (batteryLeft < 10) {
            deviceCollects.get(deviceId - 1).getSitPushUpStateResult().setState(BaseDeviceState.STATE_LOW_BATTERY);
//                mAdapter.notifyItemChanged(deviceId-1);
            updateDevice(deviceId, 1);
        }
    }

    private void setShoulderState(int deviceId, int deviceState, int batteryLeft) {
        if (deviceId > setting.getDeviceSum()) {
            return;
        }
        shoulder[deviceId-1] = 1;
        int oldState = deviceCollects.get(deviceId - 1).getShoulderResult().getState();
        if (oldState != deviceState) {
            if (deviceState != 4) {
                deviceCollects.get(deviceId - 1).getShoulderResult().setState(BaseDeviceState.STATE_FREE);
            } else {
                deviceCollects.get(deviceId - 1).getShoulderResult().setState(BaseDeviceState.STATE_STOP_USE);
            }
//                mAdapter.notifyItemChanged(deviceId-1);
            updateDevice(deviceId, 2);
        }
        if (batteryLeft < 10) {
            deviceCollects.get(deviceId - 1).getShoulderResult().setState(BaseDeviceState.STATE_LOW_BATTERY);
//                mAdapter.notifyItemChanged(deviceId-1);
            updateDevice(deviceId, 2);
        }


    }

    private void updateDevice(int deviceId, int device) {
        Message msg = Message.obtain();
        msg.what = UPDATE_SPECIFIC_ITEM;
        msg.arg1 = deviceId - 1;
        msg.arg2 = device;
        mHandler.sendMessage(msg);
    }

    @Override
    protected void onStop() {
        super.onStop();
        statesTask.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        statesTask.finish();
        mExecutor.shutdownNow();
    }

    private MyHandler mHandler = new MyHandler(this);

    @Override
    public void handleMessage(Message msg) {

        switch (msg.what) {
            case UPDATE_SPECIFIC_ITEM:
                mAdapter.setSelectDevice(msg.arg2);
                mAdapter.notifyItemChanged(msg.arg1);
                break;
        }
    }

    @Override
    public void onNoPairResponseArrived() {
        toastSpeak("匹配失败");
    }

    @Override
    public void onNewDeviceConnect() {
        toastSpeak("更换设备成功");
        cancelChangeBad();

    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int targetFrequency) {
        if (mAdapter.getDevice() == 1){
            deviceManager.setFrequency( targetFrequency,
                    originFrequency,
                    deviceId,
                    SettingHelper.getSystemSetting().getHostId());
        }else {
            shoulderManger.setFrequency(targetFrequency,originFrequency,deviceId,SettingHelper.getSystemSetting().getHostId());
        }
    }
}
