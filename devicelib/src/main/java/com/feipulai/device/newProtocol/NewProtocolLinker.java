package com.feipulai.device.newProtocol;

import android.os.Message;
import android.util.Log;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.MedicineBallNewResult;
import com.feipulai.device.serial.beans.SargentJumpResult;
import com.feipulai.device.serial.beans.VitalCapacityNewResult;
import com.feipulai.device.serial.beans.VitalCapacityResult;
import com.feipulai.device.serial.beans.VolleyPairResult;
import com.feipulai.device.sitpullup.SitPullLinker;

/**
 * Created by zzs on  2019/12/10
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class NewProtocolLinker extends SitPullLinker {
    private int deviceHostId;

    public NewProtocolLinker(int machineCode, int targetFrequency, SitPullPairListener listener, int hostId) {
        super(machineCode, targetFrequency, listener);
        this.deviceHostId = hostId;
    }

    public synchronized void checkDevice(int deviceId, int frequency, int hostId) {
        if (currentFrequency == 0) {
            // 0频段接收到的结果,肯定是设备的开机广播
            if (frequency == TARGET_FREQUENCY && deviceId == currentDeviceId && hostId == deviceHostId) {
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

    @Override
    public boolean onRadioArrived(Message msg) {
        int what = msg.what;
        if (machineCode == ItemDefault.CODE_MG && what == SerialConfigs.SARGENT_JUMP_SET_MORE_MATCH) {
            SargentJumpResult sargentJumpResult = (SargentJumpResult) msg.obj;
            checkDevice(sargentJumpResult);
            return true;
        } else if (machineCode == ItemDefault.CODE_PQ && what == SerialConfigs.VOLLEY_BALL_SET_MORE_MATCH) {
            Log.e("TAG87----", msg.obj.toString());
            VolleyPairResult volleyPairResult = (VolleyPairResult) msg.obj;
            checkDevice(volleyPairResult);
            return true;
        } else if (machineCode == ItemDefault.CODE_FHL && what == SerialConfigs.VITAL_CAPACITY_SET_MORE_MATCH) {
            if (msg.obj instanceof VitalCapacityResult) {
                VitalCapacityResult fhl = (VitalCapacityResult) msg.obj;
                checkDevice(fhl);
            } else if (msg.obj instanceof VitalCapacityNewResult) {
                VitalCapacityNewResult fhl = (VitalCapacityNewResult) msg.obj;
                checkDevice(fhl);
            }
            return true;
        } else if (machineCode == ItemDefault.CODE_HWSXQ && what == SerialConfigs.MEDICINE_BALL_MATCH_MORE) {
            MedicineBallNewResult mbn = (MedicineBallNewResult) msg.obj;
            checkDevice(mbn);
            return true;
        }
        return super.onRadioArrived(msg);
    }

    private void checkDevice(SargentJumpResult result) {
        checkDevice(result.getDeviceId(), result.getFrequency(),result.getHostId());
    }

    private void checkDevice(VolleyPairResult result) {
        checkDevice(result.getDeviceId(), result.getFrequency(), result.getHostId());
    }

    private void checkDevice(MedicineBallNewResult result) {
        checkDevice(result.getDeviceId(), result.getFrequency(),result.getHostId());
    }

    private void checkDevice(VitalCapacityResult result) {
        checkDevice(result.getDeviceId(), result.getFrequency());
    }

    private void checkDevice(VitalCapacityNewResult result) {
        checkDevice(result.getDeviceId(), result.getFrequency());
    }
}
