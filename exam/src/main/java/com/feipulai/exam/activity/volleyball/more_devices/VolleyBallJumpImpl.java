package com.feipulai.exam.activity.volleyball.more_devices;

import android.os.Message;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.VolleyPair868Result;

public class VolleyBallJumpImpl implements RadioManager.OnRadioArrivedListener {

    private VolleyBallCallBack callback;

    public VolleyBallJumpImpl(VolleyBallCallBack callBack){
        this.callback = callBack;
    }
    @Override
    public void onRadioArrived(Message msg) {
        if (msg.what == SerialConfigs.VOLLEY_BALL_STATE) {
            callback.getState((VolleyPair868Result) msg.obj);
        }
    }
    
    public interface VolleyBallCallBack{

        void getState(VolleyPair868Result obj);
    }
}
