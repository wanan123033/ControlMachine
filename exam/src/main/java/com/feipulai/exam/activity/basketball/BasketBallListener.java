package com.feipulai.exam.activity.basketball;

import com.feipulai.device.udp.UDPBasketBallConfig;
import com.feipulai.device.udp.UdpClient;
import com.feipulai.device.udp.result.BasketballResult;
import com.feipulai.device.udp.result.UDPResult;

/**
 * Created by zzs on  2019/6/5
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BasketBallListener implements UdpClient.UDPChannelListerner {

    private BaketBallResponseListener listener;

    public BasketBallListener(BaketBallResponseListener listener) {
        this.listener = listener;
    }

    @Override
    public void channelInactive() {

    }

    @Override
    public void onDataArrived(UDPResult result) {
        BasketballResult basketballResult = (BasketballResult) result.getResult();

        switch (basketballResult.getType()) {
            case UDPBasketBallConfig.CMD_GET_STATUS_RESPONSE:
            case UDPBasketBallConfig.CMD_SET_STATUS_RESPONSE:
                if (basketballResult.getUcStatus() == 0) {
                    listener.triggerStart();
                } else {
                    listener.getDeviceStatus(basketballResult.getUcStatus());
                }
                break;
            case UDPBasketBallConfig.CMD_SET_STATUS_STOP_RESPONSE://停止计时
                listener.getResult(basketballResult);

                break;
            case UDPBasketBallConfig.CMD_BREAK_RESPONSE://拦截成绩
                listener.getResult(basketballResult);
                break;
        }
    }

    public interface BaketBallResponseListener {
        void getDeviceStatus(int status);

        void triggerStart();

        void getResult(BasketballResult result);
    }
}
