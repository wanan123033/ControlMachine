package com.feipulai.host.activity.medicine_ball.more_device;

import android.os.Message;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.MedicineBallNewResult;

/**
 * Created by pengjf on 2019/11/11.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MedicineBallImpl implements RadioManager.OnRadioArrivedListener{
    private MainThreadDisposeListener disposeListener;
    public MedicineBallImpl(MainThreadDisposeListener disposeListener) {
        this.disposeListener = disposeListener ;
    }

    @Override
    public void onRadioArrived(Message msg) {
        if (msg.obj instanceof MedicineBallNewResult){
            MedicineBallNewResult result = (MedicineBallNewResult) msg.obj;
            switch (msg.what){

                case SerialConfigs.MEDICINE_BALL_RESULT_MORE:
                    disposeListener.onResultArrived(result);
                    break;
                case SerialConfigs.MEDICINE_BALL_START_MORE:
                    disposeListener.onStarTest(result.getDeviceId());
                    break;
            }
        }
    }

    interface MainThreadDisposeListener{
        void onResultArrived(MedicineBallNewResult result);
        void onStopTest();
        void onStarTest(int deviceId);
    }
}
