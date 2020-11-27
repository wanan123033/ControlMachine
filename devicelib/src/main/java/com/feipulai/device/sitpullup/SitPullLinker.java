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
import com.feipulai.device.serial.beans.SitReachWirelessResult;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.beans.VitalCapacityNewResult;
import com.feipulai.device.serial.beans.VitalCapacityResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.orhanobut.logger.utils.LogUtils;

public class SitPullLinker implements Handler.Callback {

    public static final int NO_PAIR_RESPONSE_ARRIVED = 0x1;
    public final SitPullPairListener listener;
    public volatile int currentFrequency;
    private HandlerThread handlerThread;
    public Handler mHandler;
    public int machineCode;
    public final int TARGET_FREQUENCY;
    public int currentDeviceId;
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
        } else if (machineCode == ItemDefault.CODE_YTXS && what == SerialConfigs.PULL_UP_MACHINE_BOOT_RESPONSE) {
            PullUpSetFrequencyResult pullUpSetFrequencyResult = (PullUpSetFrequencyResult) msg.obj;
            Log.i("james----", pullUpSetFrequencyResult.toString());
            checkDevice(pullUpSetFrequencyResult);
            return true;
        } else if (machineCode == ItemDefault.CODE_FWC && what == SerialConfigs.PUSH_UP_MACHINE_BOOT_RESPONSE) {
            SitPushUpSetFrequencyResult pushUpSetFrequencyResult = (SitPushUpSetFrequencyResult) msg.obj;
            Log.i("james+++++", pushUpSetFrequencyResult.toString());
            checkDevice(pushUpSetFrequencyResult);
            return true;
        }else if (machineCode == ItemDefault.CODE_FHL && what == SerialConfigs.VITAL_CAPACITY_SET_MORE_MATCH){
            if (msg.obj instanceof VitalCapacityResult) {
                VitalCapacityResult fhl = (VitalCapacityResult) msg.obj;
                checkDevice(fhl);
            } else if (msg.obj instanceof VitalCapacityNewResult) {
                VitalCapacityNewResult fhl = (VitalCapacityNewResult) msg.obj;
                checkDevice(fhl);
            }
            return true;
        } else if (machineCode == ItemDefault.CODE_WLJ && what == SerialConfigs.GRIP_SET_MORE_MATCH){
             if (msg.obj instanceof VitalCapacityNewResult) {
                VitalCapacityNewResult fhl = (VitalCapacityNewResult) msg.obj;
                checkDevice(fhl);
            }
            return true;
        }
        else if (machineCode == ItemDefault.CODE_ZWTQQ && (what == SerialConfigs.SIT_REACH_FREQUENCY) ){
            if (msg.obj instanceof SitReachWirelessResult) {
                SitReachWirelessResult result = (SitReachWirelessResult) msg.obj;
                checkDevice(result.getDeviceId(),result.getFrequency());
            }
            return true;
        }
        return false;
    }

    private void checkDevice(VitalCapacityResult result) {
        checkDevice(result.getDeviceId(), result.getFrequency());
    }

    private void checkDevice(VitalCapacityNewResult result) {
        checkDevice(result.getDeviceId(), result.getFrequency());
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

    public interface SitPullPairListener {

        void onNoPairResponseArrived();

        void onNewDeviceConnect();

        void setFrequency(int deviceId, int hostId, int targetFrequency);
    }

}
