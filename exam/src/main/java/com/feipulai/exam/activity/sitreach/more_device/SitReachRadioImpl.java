package com.feipulai.exam.activity.sitreach.more_device;

import android.os.Message;
import android.util.Log;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.SitReachWirelessResult;

/**
 * Created by pengjf on 2020/4/20.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitReachRadioImpl implements RadioManager.OnRadioArrivedListener{
    private DisposeListener disposeListener ;
    private static final String TAG = "SitReachRadioImpl";
    public SitReachRadioImpl(DisposeListener disposeListener) {
        this.disposeListener = disposeListener;
    }

    @Override
    public void onRadioArrived(Message msg) {
        if (msg.obj instanceof SitReachWirelessResult){
            SitReachWirelessResult result = (SitReachWirelessResult) msg.obj;
            switch (msg.what){

                case SerialConfigs.SIT_REACH_GET_STATE:
                    Log.i(TAG,"成绩："+result.getCapacity()+"设备状态:"+result.getState()+
                            "deviceId:"+result.getDeviceId()+"主机号:"+result.getHostId());
                    disposeListener.onResultArrived(result);
                    break;
                case SerialConfigs.SIT_REACH_START:
                    Log.i(TAG,"开始："+result.getDeviceId());
                    disposeListener.onStarTest(result.getDeviceId());
                    break;
                case SerialConfigs.SIT_REACH_LEISURE:
                    Log.i(TAG,"空闲："+result.getDeviceId());
                    break;
                case SerialConfigs.SIT_AND_REACH_STOP_RESPONSE:
                    Log.i(TAG,"停止："+result.getDeviceId());
                    break;
            }
        }
    }

    interface DisposeListener{
        void onResultArrived(SitReachWirelessResult result);
        void onStopTest();
        void onStarTest(int deviceId);
    }
}
