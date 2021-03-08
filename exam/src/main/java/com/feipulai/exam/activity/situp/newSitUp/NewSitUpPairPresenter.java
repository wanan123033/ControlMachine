package com.feipulai.exam.activity.situp.newSitUp;

import android.content.Context;
import android.os.Build;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.check.CheckUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairContract;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.config.TestConfigs;

import java.util.List;
import java.util.Objects;

public class NewSitUpPairPresenter implements SitPullUpPairContract.Presenter,
        RadioManager.OnRadioArrivedListener,
        SitPullLinker.SitPullPairListener {

    private SitPullUpPairContract.View view;
    public volatile int focusPosition;
    private List<StuDevicePair> pairs;
    public int machineCode = TestConfigs.sCurrentItem.getMachineCode();
    public final int TARGET_FREQUENCY = SettingHelper.getSystemSetting().getUseChannel();
    public SitPullLinker linker;
    private Context context;
    private SitUpSetting setting;
    private int device;//1腰带 2肩胛
    private SitPushUpManager sitPushUpManager;

    public NewSitUpPairPresenter(Context context, SitPullUpPairContract.View view) {
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }
        setting = SharedPrefsUtil.loadFormSource(context, SitUpSetting.class);
        sitPushUpManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP);
        this.view = view;
    }

    @Override
    public void start() {
        pairs = CheckUtils.newPairs(getDeviceSum());
        view.initView(isAutoPair(), pairs);
        RadioManager.getInstance().setOnRadioArrived(this);
        if (linker==null){
            linker = new SitPullLinker(machineCode, TARGET_FREQUENCY, this);
            linker.startPair(1);
        }
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
        pairs.get(focusPosition).getBaseDevice().setState(BaseDeviceState.STATE_FREE);
        view.updateSpecificItem(focusPosition);
        if (isAutoPair() && focusPosition != pairs.size() - 1) {
            changeFocusPosition(focusPosition + 1);
            //这里先清除下一个的连接状态,避免没有连接但是现实已连接
            BaseDeviceState originState = pairs.get(focusPosition).getBaseDevice();
            originState.setState(BaseDeviceState.STATE_DISCONNECT);
        }
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        sitPushUpManager.setFrequency( deviceFrequency,
                originFrequency,
                deviceId,
                SettingHelper.getSystemSetting().getHostId());
    }


    @Override
    public void changeFocusPosition(int position) {
        if (focusPosition == position) {
            return;
        }
        focusPosition = position;
        pairs.get(position).getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
        view.select(position);
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
        SharedPrefsUtil.save(context,setting);
    }

    public int getDevice() {
        return device;
    }

    public void setDevice(int device) {
        this.device = device;
    }
}
