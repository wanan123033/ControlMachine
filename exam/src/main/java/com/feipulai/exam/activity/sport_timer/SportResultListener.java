package com.feipulai.exam.activity.sport_timer;

import android.os.Message;
import android.util.Log;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.SportResult;

public class SportResultListener implements RadioManager.OnRadioArrivedListener {
    private SportMsgListener sportMsgListener;

    public SportResultListener(SportMsgListener sportMsgListener){
        this.sportMsgListener = sportMsgListener;
    }

    @Override
    public void onRadioArrived(Message msg) {
        switch (msg.what) {
            case SerialConfigs.SPORT_TIMER_CONNECT:
                if (msg.obj instanceof  SportResult){
                    sportMsgListener.onConnect((SportResult) msg.obj);
                }
                break;
            case SerialConfigs.SPORT_TIMER_GET_TIME:
                if (msg.obj instanceof  SportResult){
                    sportMsgListener.onGetTime();
                    Log.i("SportResultListener","获取时间");
                }
                break;
            case SerialConfigs.SPORT_TIMER_RESULT:
                if (msg.obj instanceof  SportResult){
                    sportMsgListener.onGetResult((SportResult) msg.obj);
                    Log.i("SportResultListener","获取缓存内容");
                }
                break;
        }
    }

    public interface SportMsgListener{
        void onConnect(SportResult result);
        void onGetTime();
        void onGetResult(SportResult result);
    }
}
