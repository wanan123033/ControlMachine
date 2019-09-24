package com.feipulai.device.manager;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.udp.UDPBasketBallConfig;
import com.feipulai.device.udp.UdpClient;

/**
 * Created by zzs on  2019/9/17
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BallManager {


    private int patternType = 0;//0 UDP  1 868 默认0
    private String hostIp;
    private int post;
    private int inetPort;
    private UdpClient.UDPChannelListerner udpChannelListerner;
    private RadioManager.OnRadioArrivedListener radioArrivedListener;

    public BallManager(int patternType) {
        this.patternType = patternType;
        init();
    }


    public void init() {
        if (patternType == 0) {
            UdpClient.getInstance().init(inetPort);
            UdpClient.getInstance().setHostIpPostLocatListener(hostIp, post, udpChannelListerner);
        } else {
            RadioManager.getInstance().setOnRadioArrived(radioArrivedListener);
        }
    }
    /**
     * 设置停止工作状态
     */
    public void sendSetStopStatus() {
        if (patternType == 0) {
            UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
        } else {
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS()));
        }
    }

    /**
     * 设置精度
     *
     * @param uPrecision 0表示设置显示精度为十分秒
     *                   1表示设置显示精度为百分秒
     */
    public void sendSetPrecision(int uPrecision) {
        if (patternType == 0) {
            UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_PRECISION(uPrecision));
        } else {
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                    UDPBasketBallConfig.BASKETBALL_CMD_SET_PRECISION(uPrecision)));
        }
    }

    /**
     * 显示屏
     *
     * @param showType UP =  1 ，DOWN=2
     * @param dataByte
     * @return
     */
    public void sendDisLed(int showType, byte[] dataByte) {
        if (patternType == 0) {
            UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(showType, dataByte));
        } else {
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                    UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(showType, dataByte)));
        }
    }

    /**
     * 设置工作状态
     *
     * @param status STATUS_FREE		1		//FREE
     *               STATUS_WAIT  		2		//WAIT To Start
     *               STATUS_RUNING 	    3		//Start Run
     *               STATUS_PREP  		4		//Prepare to stop
     *               STATUS_PAUSE 		5		//Display stop time,But Timer is Running
     * @return
     */
    public void sendSetStatus(int status) {

        if (patternType == 0) {
            UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STATUS(status));
        } else {
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                    UDPBasketBallConfig.BASKETBALL_CMD_SET_STATUS(status)));
        }
    }

    public void sendGetStatus() {
        if (patternType == 0) {
            UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_GET_STATUS);
        } else {
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                    UDPBasketBallConfig.BASKETBALL_CMD_GET_STATUS));
        }
    }

    /**
     * 设置拦截器拦截的时间
     *
     * @param second 秒
     * @return
     */
    public void sendSetBlockertime(int second) {
        if (patternType == 0) {
            UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_BLOCKERTIME(second));
        } else {
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                    UDPBasketBallConfig.BASKETBALL_CMD_SET_BLOCKERTIME(second)));
        }
    }

    /**
     * 设置拦截器灵敏度
     *
     * @param sensitivity 灵敏度
     * @return
     */
    public void sendSetDelicacy(int sensitivity) {
        if (patternType == 0) {
            UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_T(sensitivity));
        } else {
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                    UDPBasketBallConfig.BASKETBALL_CMD_SET_T(sensitivity)));
        }
    }


    /**
     * 创建对话框
     *
     * @author wzl
     */
    public static class Builder {
        private BallManager ballManager;

        public Builder(int patternType) {
            ballManager = new BallManager(patternType);
        }

        public Builder setHostIp(String hostIp) {
            ballManager.hostIp = hostIp;
            return this;
        }

        public Builder setPost(int post) {
            ballManager.post = post;
            return this;
        }

        public Builder setInetPost(int inetPort) {
            ballManager.inetPort = inetPort;
            return this;
        }

        public Builder setUdpListerner(UdpClient.UDPChannelListerner listener) {
            ballManager.udpChannelListerner = listener;
            return this;
        }

        public Builder setRadioListener(RadioManager.OnRadioArrivedListener listener) {
            ballManager.radioArrivedListener = listener;
            return this;
        }

        /**
         * 创建对话框
         *
         * @return
         */
        public BallManager build() {
            return ballManager;
        }
    }
}
