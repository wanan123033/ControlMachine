package com.feipulai.exam.activity.situp.newSitUp;

import android.content.Context;
import android.os.Build;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.ShoulderManger;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.ShoulderResult;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.config.TestConfigs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NewSitUpPairPresenter implements NewSitUpPairContract.Presenter,
        RadioManager.OnRadioArrivedListener,
        SitPullLinker.SitPullPairListener {

    private NewSitUpPairContract.View view;
    public volatile int focusPosition;
    private List<DeviceCollect> pairs = new ArrayList<>();
    public int machineCode = TestConfigs.sCurrentItem.getMachineCode();
    public final int TARGET_FREQUENCY = SettingHelper.getSystemSetting().getUseChannel();
    public SitPullLinker linker;
    private Context context;
    private SitUpSetting setting;
    private int device;//1腰带 2肩胛
    private SitPushUpManager sitPushUpManager;
    private ShoulderManger shoulderManger;

    public NewSitUpPairPresenter(Context context, NewSitUpPairContract.View view) {
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }
        setting = SharedPrefsUtil.loadFormSource(context, SitUpSetting.class);
        sitPushUpManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP);
        shoulderManger = new ShoulderManger();
        this.view = view;
    }

    @Override
    public void start() {
        pairs = setPairs(getDeviceSum());
        view.initView(isAutoPair(), pairs);
        RadioManager.getInstance().setOnRadioArrived(this);
        if (linker == null) {
            linker = new SitPullLinker(machineCode, TARGET_FREQUENCY, this);
            linker.startPair(1);
        }
    }

    private List<DeviceCollect> setPairs(int deviceSum) {
        for (int i = 0; i < deviceSum; i++) {
            ShoulderResult shoulderResult = new ShoulderResult();
            shoulderResult.setDeviceId(i + 1);
            SitPushUpStateResult stateResult = new SitPushUpStateResult();
            stateResult.setDeviceId(i + 1);
            DeviceCollect deviceCollect = new DeviceCollect(stateResult, shoulderResult);
            pairs.add(deviceCollect);
        }
        return pairs;
    }

    private boolean isAutoPair() {
        return setting.isAutoPair();
    }

    private int getDeviceSum() {
        return setting.getDeviceSum();
    }

    @Override
    public void onRadioArrived(Message msg) {
        linker.onRadioArrived(msg);
    }

    @Override
    public void onNoPairResponseArrived() {
        view.showToast("未收到子机回复,设置失败,请重试");
    }

    @Override
    public void onNewDeviceConnect() {
        if (device == 1) {
            pairs.get(focusPosition).getSitPushUpStateResult().setState(BaseDeviceState.STATE_FREE);
        } else {
            pairs.get(focusPosition).getShoulderResult().setState(BaseDeviceState.STATE_FREE);
        }
        view.updateSpecificItem(focusPosition, device);
        if (isAutoPair() && focusPosition != pairs.size() - 1) {
            changeFocusPosition(focusPosition + 1, device);
            //这里先清除下一个的连接状态,避免没有连接但是现实已连接
            pairs.get(focusPosition).getSitPushUpStateResult().setState(BaseDeviceState.STATE_DISCONNECT);
            pairs.get(focusPosition).getShoulderResult().setState(BaseDeviceState.STATE_DISCONNECT);
//            BaseDeviceState originState = pairs.get(focusPosition).getBaseDevice();
//            originState.setState(BaseDeviceState.STATE_DISCONNECT);
        }
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        if (device == 1) {
            sitPushUpManager.setFrequency(deviceFrequency,
                    originFrequency,
                    deviceId,
                    SettingHelper.getSystemSetting().getHostId());
        } else {
            shoulderManger.setFrequency(deviceFrequency, originFrequency, deviceId, SettingHelper.getSystemSetting().getHostId());
        }

    }


    @Override
    public void changeFocusPosition(int position, int device) {
        if (focusPosition == position && this.device == device) {
            return;
        }
        this.device = device;
        focusPosition = position;
        if (device == 1) {
            pairs.get(focusPosition).getSitPushUpStateResult().setState(BaseDeviceState.STATE_DISCONNECT);
        } else {
            pairs.get(focusPosition).getShoulderResult().setState(BaseDeviceState.STATE_DISCONNECT);
        }
//        pairs.get(position).getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
        view.select(position, device);
        linker.startPair(focusPosition + 1);
    }

    @Override
    public void changeAutoPair(boolean isAutoPair) {
        setting.setAutoPair(isAutoPair);
    }

    @Override
    public void stopPair() {
        linker.cancelPair();
        RadioManager.getInstance().setOnRadioArrived(null);
    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context, setting);
    }

    public int getDevice() {
        return device;
    }

    public void setDevice(int device) {
        this.device = device;
    }
}
