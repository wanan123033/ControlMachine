package com.feipulai.exam.activity.volleyball.more_devices;

import android.os.Message;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.VolleyPair868Result;

public class VolleyBallJumpImpl implements RadioManager.OnRadioArrivedListener {

    private VolleyBallCallBack callback;

    public VolleyBallJumpImpl(VolleyBallCallBack callBack){
        this.callback = callBack;
    }
    @Override
    public void onRadioArrived(Message msg) {
        if (msg.what == 0x40){
            byte[] data = ((VolleyPair868Result)msg.obj).getDataArr();
            switch (data[7]){
                case (byte) 0xB3:
                    callback.getState((VolleyPair868Result) msg.obj);
                    break;
                case (byte) 0xb7:
                    byte gan = data[13];
                    if (data[14] == 0x00 && data[15] == 0x00 && data[16] == 0x00 && data[17] == 0x00 && data[18] == 0x00){
                        callback.begin((VolleyPair868Result)msg.obj);
                    }else {
                        callback.onError(gan,new byte[]{data[14],data[15],data[16],data[17],data[18]});
                    }
                    break;
            }
        }
    }
    
    public interface VolleyBallCallBack{

        void getState(VolleyPair868Result obj);

        void begin(VolleyPair868Result obj);

        void onError(byte gan, byte[] bytes);
    }
}
