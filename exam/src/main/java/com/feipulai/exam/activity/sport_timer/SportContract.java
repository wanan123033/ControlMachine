package com.feipulai.exam.activity.sport_timer;

import com.feipulai.device.serial.beans.SportResult;

public interface SportContract {
    interface Presenter {
        void rollConnect();//轮询
        void setContinueRoll(boolean connect);
        void waitStart();

    }

    interface SportView{
        void updateDeviceState(int deviceId,int state);
        void getTimeUpdate();
        void receiveResult(SportResult sportResult);
        void getDeviceState(int deviceState);
    }
}
