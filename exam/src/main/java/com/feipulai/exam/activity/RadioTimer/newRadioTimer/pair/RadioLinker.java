package com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.SitPushUpSetFrequencyResult;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.orhanobut.logger.utils.LogUtils;

public class RadioLinker implements Handler.Callback{

    public static final int NO_PAIR_RESPONSE_ARRIVED = 0x1;
    public final RadioPairListener listener;
    public volatile int currentFrequency;
    private HandlerThread handlerThread;
    public Handler mHandler;
    public int machineCode;
    public final int TARGET_FREQUENCY;
    public int currentDeviceId;
    private volatile boolean linking;

    public RadioLinker(int machineCode, int targetFrequency, RadioPairListener listener) {
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
        RadioChannelCommand command = new RadioChannelCommand(0);
        LogUtils.normal(command.getCommand().length+"---"+ StringUtility.bytesToHexString(command.getCommand())+"---切0频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command));
    }

    public void cancelPair() {
        linking = false;
        if (handlerThread != null) {
            handlerThread.quit();
        }
        handlerThread = null;
        RadioChannelCommand command = new RadioChannelCommand(TARGET_FREQUENCY);
        LogUtils.normal(command.getCommand().length+"---"+ StringUtility.bytesToHexString(command.getCommand())+"---切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command));
        currentFrequency = TARGET_FREQUENCY;
    }

    public boolean onRadioArrived(Message msg) {
        Log.i("james+++++--------", msg.obj.toString());
        if (!linking) {
            return false;
        }
        int what = msg.what;
        if ((machineCode == ItemDefault.CODE_YWQZ||machineCode == ItemDefault.CODE_SGBQS) && what == SerialConfigs.SIT_UP_MACHINE_BOOT_RESPONSE) {
            SitPushUpSetFrequencyResult sitUpSetFrequencyResult = (SitPushUpSetFrequencyResult) msg.obj;
            Log.i("", sitUpSetFrequencyResult.toString());
            checkDevice(sitUpSetFrequencyResult);
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


    public synchronized void checkDevice(int deviceId, int frequency) {
        Log.e("TAG115----","currentFrequency = "+currentFrequency+",frequency="+frequency+",deviceId="+deviceId+",currentDeviceId="+currentDeviceId);
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

    public synchronized void onNewDeviceConnect() {
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

    public interface RadioPairListener {

        void onNoPairResponseArrived();

        void onNewDeviceConnect();

        void setFrequency(int deviceId, int hostId, int targetFrequency);
    }
}