package com.feipulai.exam.activity.basketball;

import android.os.Message;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.Basketball868Result;
import com.feipulai.device.udp.UDPBasketBallConfig;
import com.feipulai.device.udp.UdpClient;
import com.feipulai.device.udp.result.BasketballResult;
import com.feipulai.device.udp.result.UDPResult;
import com.orhanobut.logger.Logger;

/**
 * Created by zzs on  2019/6/5
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BasketBallListener implements UdpClient.UDPChannelListerner, RadioManager.OnRadioArrivedListener {

    private BasketBallResponseListener listener;

    public BasketBallListener(final BasketBallResponseListener listener) {
        this.listener = listener;

    }


    @Override
    public void channelInactive() {

    }

    @Override
    public void onDataArrived(UDPResult result) {
        BasketballResult basketballResult = (BasketballResult) result.getResult();
        Logger.i("onDataArrived===>" + basketballResult.toString());
        switch (basketballResult.getType()) {
            case UDPBasketBallConfig.CMD_GET_STATUS_RESPONSE:
            case UDPBasketBallConfig.CMD_SET_STATUS_RESPONSE:
                if (basketballResult.getUcStatus() == 0) {
                    if (basketballResult.gettNum() == -1)
                        return;
                    listener.triggerStart(basketballResult);
                } else {
                    listener.getDeviceStatus(basketballResult.getUcStatus());
                }
                break;
            case UDPBasketBallConfig.CMD_SET_STATUS_STOP_RESPONSE://停止计时
                listener.getStatusStop(basketballResult);

                break;
            case UDPBasketBallConfig.CMD_BREAK_RESPONSE://拦截成绩
                listener.getResult(basketballResult);
                break;
        }
    }

    @Override
    public void onRadioArrived(Message msg) {
//        BasketballResult basketballResult = (BasketballResult) msg.obj;
//        Logger.i("onDataArrived===>" + basketballResult.toString());
//        switch (basketballResult.getType()) {
//            case UDPBasketBallConfig.CMD_GET_STATUS_RESPONSE:
//            case UDPBasketBallConfig.CMD_SET_STATUS_RESPONSE:
//                if (basketballResult.getUcStatus() == 0) {
//                    if (basketballResult.gettNum() == -1)
//                        return;
//                    listener.triggerStart(basketballResult);
//                } else {
//                    listener.getDeviceStatus(basketballResult.getUcStatus());
//                }
//                break;
//            case UDPBasketBallConfig.CMD_SET_STATUS_STOP_RESPONSE://停止计时
//                listener.getStatusStop(basketballResult);
//
//                break;
//            case UDPBasketBallConfig.CMD_BREAK_RESPONSE://拦截成绩
//                listener.getResult(basketballResult);
//                break;
//        }
    }

    public interface BasketBallResponseListener {


        void getDeviceStatus(int status);

        /**
         * 触发开发
         */
        void triggerStart(BasketballResult basketballResult);

        /**
         * 拦截成绩
         *
         * @param result
         */
        void getResult(BasketballResult result);

        /**
         * 停止
         *
         * @param result
         */
        void getStatusStop(BasketballResult result);

    }


}
