package com.feipulai.host.activity.radio_timer;

import android.os.Message;
import android.util.Log;

import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.RunTimerConnectState;
import com.feipulai.device.serial.beans.RunTimerResult;

import static com.feipulai.device.serial.SerialConfigs.RUN_TIMER_CONNECT;
import static com.feipulai.device.serial.SerialConfigs.RUN_TIMER_FAULT_BACK;
import static com.feipulai.device.serial.SerialConfigs.RUN_TIMER_FORCE_START;
import static com.feipulai.device.serial.SerialConfigs.RUN_TIMER_INTERCEPT_TIME;
import static com.feipulai.device.serial.SerialConfigs.RUN_TIMER_READY;
import static com.feipulai.device.serial.SerialConfigs.RUN_TIMER_SETTING;
import static com.feipulai.device.serial.SerialConfigs.RUN_TIMER_STOP;


/**
 * Created by pengjf on 2018/12/6.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class RunTimerImpl implements SerialDeviceManager.RS232ResiltListener {
    private static final String TAG = "RunTimerImpl";
    private RunTimerListener listener ;

    public RunTimerImpl(RunTimerListener listener) {
        this.listener = listener;
    }

    @Override
    public void onRS232Result(Message msg) {
        switch (msg.what){
            case RUN_TIMER_SETTING:
                log("红外计时===>设置" );
                listener.onTestState(1);
                break;
            case RUN_TIMER_READY:
                log("红外计时===>ready" );
                listener.onTestState(2);
                break;
            case RUN_TIMER_FORCE_START:
                log("红外计时===>forceStart" );
                listener.onTestState(3);
                break;

            case RUN_TIMER_INTERCEPT_TIME:
                log("红外计时===>拦截计时" );
                RunTimerResult result = (RunTimerResult) msg.obj;
                listener.onGetTime(result);
                listener.onTestState(4);
                break;
            case RUN_TIMER_CONNECT:
                log("红外计时===>连接" );
                RunTimerConnectState connectState = (RunTimerConnectState) msg.obj;
                listener.onConnected(connectState);
                break;
            case RUN_TIMER_FAULT_BACK:
                log("红外计时===>faultBack" );
                listener.onTestState(5);
                break;
            case RUN_TIMER_STOP:
                log("红外计时===>stop" );
                listener.onTestState(6);
                break;

        }
    }

    private void log(String s) {
        Log.i(TAG,s);
    }



    interface RunTimerListener{
        void onGetTime(RunTimerResult result);
        void onConnected(RunTimerConnectState connectState);
        void onTestState(int state);
    }
}
