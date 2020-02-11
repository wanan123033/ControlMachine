package com.feipulai.testandroid.activity;

import android.os.Message;
import android.util.Log;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.VitalCapacityNewResult;
import com.feipulai.device.serial.beans.VitalCapacityResult;

/**
 * Created by pengjf on 2019/10/9.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class WirelessVitalListener implements RadioManager.OnRadioArrivedListener {

    private static final String TAG = "WirelessVitalListener";
    private WirelessListener listener;
    public WirelessVitalListener(WirelessListener listener){
        this.listener = listener ;
    }

    @Override
    public void onRadioArrived(Message msg) {
        switch (msg.what) {
            case SerialConfigs.VITAL_CAPACITY_RESULT:
                if (msg.obj instanceof VitalCapacityResult){
                    final VitalCapacityResult result = (VitalCapacityResult) msg.obj;
                    Log.i(TAG, "子机号:" + result.getDeviceId() + "状态:" + result.getState() + "序号:" + result.getIndex());
                    int index = result.getIndex();
                    switch (index) {
                        case 5:
                            //回应主机查询
                            listener.onResult(result.getDeviceId(),result.getState(),result.getCapacity(),result.getPower());
                            if (result.getState() == 4) {//结束
                                //此处不应该直接发结束命令而是交给设备去判断
                                listener.onStop();
                                Log.i(TAG,"子机号:" + result.getDeviceId() + "状态:" + result.getState() +
                                        "序号:" + result.getIndex()+"计数:"+result.getCapacity());
                            }
                            break;

                    }
                }else if (msg.obj instanceof VitalCapacityNewResult){
                    final VitalCapacityNewResult result = (VitalCapacityNewResult) msg.obj;
                    Log.i(TAG, "子机号:" + result.getDeviceId() + "状态:" + result.getState() + "序号:" + result.getIndex()+"电量"+result.getPower());
                    int index = result.getIndex();
                    switch (index) {
                        case 3:
                            //回应主机查询
                            listener.onResult(result.getDeviceId(),result.getState(),result.getCapacity(),result.getPower());
                            if (result.getState() == 4) {//结束
                                //此处不应该直接发结束命令而是交给设备去判断
                                listener.onStop();
                                Log.i(TAG,"子机号:" + result.getDeviceId() + "状态:" + result.getState() +
                                        "序号:" + result.getIndex()+"计数:"+result.getCapacity());
                            }
                            break;

                    }
                }

            break;
        }
    }


    interface WirelessListener {

        void onResult(int id, int state, int result, int power);

        void onStop();
    }
}
