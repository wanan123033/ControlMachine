package com.feipulai.exam.activity.RadioTimer.newRadioTimer;

import android.os.Message;

import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.exam.activity.RadioTimer.RunTimerSetting;
import com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioContract;
import com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioLinker;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChangeDeviceUtil implements RadioManager.OnRadioArrivedListener, RadioLinker.RadioPairListener {
    private int machineCode = TestConfigs.sCurrentItem.getMachineCode();
    private final int TARGET_FREQUENCY = SettingHelper.getSystemSetting().getUseChannel();
    private RadioLinker linker;
    private ScheduledExecutorService checkService;
    private RadioContract.View view;
    private SportTimerManger sportTimerManger;
    private RunTimerSetting setting;
    private int point;
    private int deviceId;
    private volatile boolean check;
    public ChangeDeviceUtil(RadioContract.View view, RunTimerSetting setting){
        this.view = view;
        this.setting = setting;
        checkService = Executors.newSingleThreadScheduledExecutor();
        sportTimerManger = new SportTimerManger();

    }

    public void setCheckState(){
        checkService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (check){
                    intervalRun();
                }
            }
        }, 100, 400, TimeUnit.MILLISECONDS);
    }
    public void start(int deviceId, int point) {
        this.point = point;
        this.deviceId = deviceId;
        RadioManager.getInstance().setOnRadioArrived(this);
        if (linker == null) {
            linker = new RadioLinker(machineCode, TARGET_FREQUENCY, this);
        }
        linker.startPair(deviceId);
    }

    private void intervalRun() {
        int deviceNum = Integer.parseInt(setting.getRunNum()) + 1;
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
    public void onRadioArrived(Message msg) {
        switch (msg.what) {
            case SerialConfigs.SPORT_TIMER_CONNECT:
                if (msg.obj instanceof SportResult) {

                }
                break;
            case SerialConfigs.SPORT_TIMER_MATCH:
                if (linker == null)
                    return;
                linker.onRadioArrived(msg);
                break;
        }

    }

    @Override
    public void onNoPairResponseArrived() {
        view.showToast("未收到子机回复,设置失败,请重试");
    }

    @Override
    public void onNewDeviceConnect() {
        view.select(deviceId,point);
    }

    @Override
    public void setFrequency(int deviceId, int hostId, int targetFrequency) {
        sportTimerManger.setFrequency(deviceId, targetFrequency, hostId, SettingHelper.getSystemSetting().getHostId());
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

}
