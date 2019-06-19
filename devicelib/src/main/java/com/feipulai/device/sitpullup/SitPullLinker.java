package com.feipulai.device.sitpullup;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.PullUpSetFrequencyResult;
import com.feipulai.device.serial.beans.SitPushUpSetFrequencyResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;

public class SitPullLinker implements Handler.Callback {

    private static final int NO_PAIR_RESPONSE_ARRIVED = 0x1;
    private final SitPullPairListener listener;
    private volatile int currentFrequency;
    private HandlerThread handlerThread;
    private Handler mHandler;
    private int machineCode;
    private final int TARGET_FREQUENCY;
    private int currentDeviceId;
    private volatile boolean linking;

    public SitPullLinker(int machineCode, int targetFrequency, SitPullPairListener listener) {
        this.currentFrequency = targetFrequency;
        this.machineCode = machineCode;
        this.TARGET_FREQUENCY = targetFrequency;
        this.listener = listener;
    }

    public void startPair(int deviceId) {
        linking = true;
        currentDeviceId = deviceId;
        if (handlerThread == null) {
            handlerThread = new HandlerThread("handlerThread");
            handlerThread.start();
            mHandler = new Handler(handlerThread.getLooper(), this);
        }
        currentFrequency = 0;
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
    }

    public void cancelPair() {
        linking = false;
        if (handlerThread != null) {
            handlerThread.quit();
        }
        handlerThread = null;
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(TARGET_FREQUENCY)));
        currentFrequency = TARGET_FREQUENCY;
    }

    public boolean onRadioArrived(Message msg) {
        if (!linking) {
            return false;
        }
        int what = msg.what;
        if (machineCode == ItemDefault.CODE_YWQZ && what == SerialConfigs.SIT_UP_MACHINE_BOOT_RESPONSE) {
            SitPushUpSetFrequencyResult sitUpSetFrequencyResult = (SitPushUpSetFrequencyResult) msg.obj;
            Log.i("", sitUpSetFrequencyResult.toString());
            checkDevice(sitUpSetFrequencyResult);
            return true;
        } else if (machineCode == ItemDefault.CODE_YTXS && what == SerialConfigs.PULL_UP_MACHINE_BOOT_RESPONSE) {
            PullUpSetFrequencyResult pullUpSetFrequencyResult = (PullUpSetFrequencyResult) msg.obj;
            Log.i("james", pullUpSetFrequencyResult.toString());
            checkDevice(pullUpSetFrequencyResult);
            return true;
        } else if (machineCode == ItemDefault.CODE_FWC && what == SerialConfigs.PUSH_UP_MACHINE_BOOT_RESPONSE) {
            SitPushUpSetFrequencyResult pushUpSetFrequencyResult = (SitPushUpSetFrequencyResult) msg.obj;
            Log.i("james", pushUpSetFrequencyResult.toString());
            checkDevice(pushUpSetFrequencyResult);
            return true;
        }
        return false;
    }

    private void checkDevice(SitPushUpSetFrequencyResult result) {
        if (result.getProjectCode() == SitPushUpManager.PROJECT_CODE_SIT_UP
                ||result.getProjectCode() == SitPushUpManager.PROJECT_CODE_PUSH_UP) {
            checkDevice(result.getDeviceId(), result.getFrequency());
        }
    }

    private void checkDevice(PullUpSetFrequencyResult result) {
        checkDevice(result.getDeviceId(), result.getFrequency());
    }

    private synchronized void checkDevice(int deviceId, int frequency) {
        if (currentFrequency == 0) {
            // 0频段接收到的结果,肯定是设备的开机广播
            if (frequency == TARGET_FREQUENCY && deviceId == currentDeviceId) {
                onNewDeviceConnect();
            } else {
                listener.setFrequency(currentDeviceId, frequency, TARGET_FREQUENCY);
                currentFrequency = TARGET_FREQUENCY;
                // 那个铁盒子就是有可能等这么久才收到回复
                mHandler.sendEmptyMessageDelayed(NO_PAIR_RESPONSE_ARRIVED, 5000);
            }
        } else if (currentFrequency == TARGET_FREQUENCY) {
            //在主机的目的频段收到的,肯定是设置频段后收到的设备广播
            if (deviceId == currentDeviceId && frequency == TARGET_FREQUENCY) {
                onNewDeviceConnect();
            }
        }
    }

    private synchronized void onNewDeviceConnect() {
        mHandler.removeMessages(NO_PAIR_RESPONSE_ARRIVED);
        listener.onNewDeviceConnect();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {

            case NO_PAIR_RESPONSE_ARRIVED:
                if (!linking) {
                    break;
                }
                RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
                currentFrequency = 0;
                listener.onNoPairResponseArrived();
                return true;
        }
        return false;
    }

    public interface SitPullPairListener {

        void onNoPairResponseArrived();

        void onNewDeviceConnect();

        void setFrequency(int deviceId, int frequency, int targetFrequency);
    }

}
