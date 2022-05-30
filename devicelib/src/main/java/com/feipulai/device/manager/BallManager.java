package com.feipulai.device.manager;

import android.graphics.Paint;

import com.feipulai.device.led.LEDManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.Basketball868Result;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.device.udp.UDPBasketBallConfig;
import com.feipulai.device.udp.UdpClient;
import com.feipulai.device.udp.UdpLEDUtil;
import com.orhanobut.logger.utils.LogUtils;

import java.io.UnsupportedEncodingException;

/**
 * Created by zzs on  2019/9/17
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BallManager {


    private int patternType = 0;//0 UDP  1 868 默认0
    private String hostIp;
    private int post;
    private int inetPort = 1527;
    private UdpClient.UDPChannelListerner udpChannelListerner;
    private RadioManager.OnRadioArrivedListener radioArrivedListener;
    private int useLedType = 0;
    private LEDManager ledManager;

    public BallManager(int patternType) {
        this.patternType = patternType;
        ledManager = new LEDManager(LEDManager.LED_VERSION_4_8);
    }

    public void setHostIpPost(String hostIp, int post) {
        this.hostIp = hostIp;
        this.post = post;
        UdpClient.getInstance().setHostIpPost(hostIp, post);
    }

    public void setHostIpPostLocatListener(String hostIp, int post, UdpClient.UDPChannelListerner listerner) {
        this.hostIp = hostIp;
        this.post = post;
        this.udpChannelListerner = listerner;
        UdpClient.getInstance().setHostIpPostLocatListener(hostIp, post, udpChannelListerner);
    }


    public void init() {
        if (patternType == 0) {
            UdpClient.getInstance().init(inetPort);
            UdpClient.getInstance().setHostIpPostLocatListener(hostIp, post, udpChannelListerner);
        } else {
            RadioManager.getInstance().setOnRadioArrived(radioArrivedListener);
        }
    }

    public void getUDPResultTime() {
        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_GET_RESULT);
        LogUtils.serial("UDP发送篮足球获取拦截时间指令:" + StringUtility.bytesToHexString(UDPBasketBallConfig.BASKETBALL_CMD_GET_RESULT));
    }

    /**
     * 设置停止工作状态
     */
    public void sendSetStopStatus(int hostId) {
        if (patternType == 0) {
            byte[] buf = UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS();
            LogUtils.serial("UDP发送篮足球停止工作状态指令:" + StringUtility.bytesToHexString(buf));
            UdpClient.getInstance().send(buf);
        } else {
            setRadioStopTime(hostId);
        }
    }

    public void sendSetStopStatusTo(int hostId, long timerDate, int timeFormat) {
        ledManager.ballTimeControl(hostId, true, false, true, timerDate, timeFormat, false, 2);
    }

    public void sendSetStopStatusTo(int hostId, long timerDate, int timeFormat, int color) {
        ledManager.ballTimeControl(hostId, true, false, true, timerDate, timeFormat, false, 2, color);
    }

    /**
     * 显示屏
     *
     * @param showType  UP =  1 ，DOWN=2
     * @param data
     * @param rightTest
     * @return
     */
    public void sendDisLed(int hostId, int showType, String data, String rightTest, Paint.Align align) {
        if (patternType == 0) {
            byte[] buf = UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(showType, UdpLEDUtil.getLedByte(data, rightTest, align));
            UdpClient.getInstance().send(buf);
            LogUtils.serial("UDP发送篮足球显示屏指令:" + StringUtility.bytesToHexString(buf));
        } else {
            try {
                byte[] showData = new byte[10];
                byte[] dataByte = data.getBytes("GB2312");

                int x = 0;
                if (align == Paint.Align.CENTER) {
                    x = (10 - (data.getBytes("GBK").length)) / 2;
                } else if (align == Paint.Align.RIGHT) {
                    x = 10 - data.getBytes("GBK").length;
                }

                System.arraycopy(dataByte, 0, showData, x, dataByte.length);

                byte[] rightByte = rightTest.getBytes("GB2312");
                x = 10 - rightTest.getBytes("GBK").length;
                System.arraycopy(rightByte, 0, showData, x, rightByte.length);
                setLedShowData(hostId, showData, 0, showType);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 显示屏
     *
     * @param showType UP =  1 ，DOWN=2
     * @param data
     * @return
     */
    public void sendDisLed(int hostId, int showType, String data, Paint.Align align) {
        if (patternType == 0) {
            byte[] buf = UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(showType, UdpLEDUtil.getLedByte(data, align));
            UdpClient.getInstance().send(buf);
            LogUtils.serial("UDP发送篮足球显示屏指令:" + StringUtility.bytesToHexString(buf));
        } else {
            setLedShowData(hostId, data, showType, align);
        }
    }

    /**
     * 设置工作状态
     *
     * @param status STATUS_FREE		1		//FREE
     *               STATUS_WAIT  		2		//WAIT To Start
     *               STATUS_RUNING 	    3		//Start Run  无法进行统一状态命令， 区分发送命令处理
     *               STATUS_PREP  		4		//Prepare to stop
     *               STATUS_PAUSE 		5		//Display stop time,But Timer is Running
     * @return
     */
    public void sendSetStatus(int hostId, int status, int showFormat) {

        if (patternType == 0) {
            byte[] buf = UDPBasketBallConfig.BASKETBALL_CMD_SET_STATUS(status);
            UdpClient.getInstance().send(buf);
            LogUtils.serial("UDP发送篮足球设置工作状态指令:" + StringUtility.bytesToHexString(buf));
        } else {
            switch (status) {
                case 1:
                    setRadioFreeStates(hostId);
                    break;
                case 2:
                    if (useLedType == 0) {
                        setLedShowData(hostId, "", 2, Paint.Align.CENTER);
                    } else {
                        // waitTime(hostId,showFormat);
                    }
                    setRadioFreeStates(hostId);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    setRadioStartAwait(hostId);
                    break;
                case 5:
                    setRadioPause(hostId);
                    break;
            }
        }
    }

    public void sendSetStatus(int hostId, int status) {

        if (patternType == 0) {
            byte[] buf = UDPBasketBallConfig.BASKETBALL_CMD_SET_STATUS(status);
            UdpClient.getInstance().send(buf);
            LogUtils.serial("UDP发送篮足球设置工作状态指令:" + StringUtility.bytesToHexString(buf));
        } else {
            switch (status) {
                case 1:
                    setRadioFreeStates(hostId);
                    break;
                case 2:
                    if (useLedType == 0) {
                        setLedShowData(hostId, "", 2, Paint.Align.CENTER);
                    } else {
                        //waitTime(hostId,2);
                    }
                    setRadioFreeStates(hostId);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    setRadioStartAwait(hostId);
                    break;
                case 5:
                    setRadioPause(hostId);
                    break;
            }
        }
    }

    public void waitTime(int hostId, int showFormat) {
        ledManager.ballTimeControl(hostId, true, false, true, 0, showFormat, false, 2);
    }

    public void sendGetStatus(int hostId, int deviceId) {
        if (patternType == 0) {
            UdpClient.getInstance().setHostIpPost(hostIp, post);
            byte[] buf = UDPBasketBallConfig.BASKETBALL_CMD_GET_STATUS;
            UdpClient.getInstance().send(buf);
            LogUtils.serial("UDP发送篮足球获取工作状态指令:" + StringUtility.bytesToHexString(buf));
        } else {
            getRadioState(hostId, deviceId);
        }
    }

    /**
     * 设置拦截器拦截的时间
     *
     * @param interceptSecond 秒
     * @return
     */
    public void sendSetBlockertime(int hostId, int sensitivity, int interceptSecond, int precision) {
        if (patternType == 0) {
            UdpClient.getInstance().setHostIpPost(hostIp, post);
            byte[] buf = UDPBasketBallConfig.BASKETBALL_CMD_SET_BLOCKERTIME(interceptSecond);
            UdpClient.getInstance().send(buf);
            LogUtils.serial("发送篮足球获取工作状态指令:" + StringUtility.bytesToHexString(buf));
        } else {
            setSettingParameter(hostId, sensitivity, interceptSecond, precision);
        }
    }

    /**
     * 设置拦截器灵敏度
     *
     * @param sensitivity 灵敏度
     * @return
     */
    public void sendSetDelicacy(int hostId, int sensitivity, int interceptSecond, int precision) {
        if (patternType == 0) {
            UdpClient.getInstance().setHostIpPost(hostIp, post);
            byte[] buf = UDPBasketBallConfig.BASKETBALL_CMD_SET_T(sensitivity);
            UdpClient.getInstance().send(buf);
            LogUtils.serial("发送篮足球设置拦截器灵敏度指令:" + StringUtility.bytesToHexString(buf));
        } else {
            setSettingParameter(hostId, sensitivity, interceptSecond, precision);
        }
    }

    /**
     * 设置精度
     */
    public void sendSetPrecision(int hostId, int sensitivity, int interceptSecond, int precision) {
        if (patternType == 0) {
            UdpClient.getInstance().setHostIpPost(hostIp, post);
            byte[] buf = UDPBasketBallConfig.BASKETBALL_CMD_SET_PRECISION(precision);
            UdpClient.getInstance().send(buf);
            LogUtils.serial("发送篮足球设置精度指令:" + StringUtility.bytesToHexString(buf));
        } else {
            setSettingParameter(hostId, sensitivity, interceptSecond, precision);
        }
    }

    public void setSettingParameter(int hostId, int sensitivity, int interceptSecond, int precision) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x14, 0x0D, 0x03, 0x01, 0x00, 0x00, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d};
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) 0x01;
        cmd[12] = (byte) sensitivity;
        cmd[13] = (byte) interceptSecond;
        cmd[14] = (byte) precision;
        cmd[18] = (byte) sum(cmd, 18);

        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        LogUtils.serial("发送篮足球设置参数:" + StringUtility.bytesToHexString(cmd));
    }

    /**
     * 篮足球设置参数
     */
    private byte[] getRadioParameterBuf(int targetChannel, int deviceId, int hostId, int sensitivity, int interceptSecond) {

        byte[] buf = new byte[20];
        buf[0] = (byte) 0xAA;
        buf[1] = 0x14;//包长
        buf[2] = (byte) 0x0D;     //测试项目
        buf[3] = 0x03;//目标设备编号：0x03（控制盒属于计数器）
        buf[4] = 0x01;      //本设备编号：0x01（主机）
        buf[5] = (byte) (hostId & 0xff);     //本设备主机号
        buf[6] = (byte) (deviceId & 0xff);       //目标设备子机号
        buf[7] = 0x02;
        buf[8] = 0x00; //高字节在先
        buf[9] = 0x00;
        buf[10] = 0x00;
        buf[11] = 0x00;
        buf[12] = (byte) (targetChannel & 0xff); //高字节在先
        buf[13] = 4;
        buf[14] = (byte) hostId;
        buf[15] = (byte) deviceId;
        buf[16] = (byte) (sensitivity & 0xff);
        buf[17] = (byte) (interceptSecond & 0xff);
        for (int i = 1; i < 18; i++) {
            buf[18] += buf[i] & 0xff;
        }
        buf[19] = 0x0d;
        return buf;
    }

    /**
     * 篮足球设置参数
     */
    public void setRadioParameter(int targetChannel, int deviceId, int hostId, int sensitivity, int interceptSecond) {
        byte[] buf = getRadioParameterBuf(targetChannel, deviceId, hostId, sensitivity, interceptSecond);
        LogUtils.serial("发送篮足球设置参数指令：" + StringUtility.bytesToHexString(buf));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, buf));
    }

    /**
     * 篮足球设置参数
     *
     * @param deviceId
     * @param hostId
     */
    public void setRadioFrequency(int targetChannel, int deviceId, int hostId, int sensitivity, int interceptSecond) {

        byte[] buf = getRadioParameterBuf(targetChannel, deviceId, hostId, sensitivity, interceptSecond);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtils.serial("发送篮足球设置频段参数指令：" + StringUtility.bytesToHexString(buf));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, buf));
        RadioChannelCommand b = new RadioChannelCommand(targetChannel);
        RadioManager.getInstance().sendCommand(new ConvertCommand(b));
    }

    public void getRadioState(int hostId, int deviceId) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x14, 0x0D, 0x03, 0x01, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d};
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) deviceId;
        cmd[18] = (byte) sum(cmd, 18);
        LogUtils.serial("发送篮足球获取状态指令：" + StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    public void getRadioLedState(int hostId) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x14, 0x0D, 0x02, 0x01, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d};
        cmd[5] = (byte) hostId;
        cmd[18] = (byte) sum(cmd, 18);
        LogUtils.serial("发送篮足球LED获取状态指令：" + StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 离线：0
     * 空闲：1
     * 等待：2
     * 计时：3
     * 暂停：5（暂停显示时间，不停表只针对显示屏）
     * 结束：6
     * <p>
     * 开始等待
     *
     * @param hostId
     */
    public void setRadioStartAwait(int hostId) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x14, 0x0D, 0x03, 0x01, 0x00, 0x01, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d};
        cmd[5] = (byte) hostId;
        cmd[18] = (byte) sum(cmd, 18);
        LogUtils.serial("篮足球设置开始等待指令:" + StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 离线：0
     * 空闲：1
     * 等待：2
     * 计时：3
     * 暂停：5（暂停显示时间，不停表只针对显示屏）
     * 结束：6
     * <p>
     * 开始等待
     *
     * @param hostId
     */
    public void setRadioLEDStartAwait(int hostId) {
        if (useLedType == 0) {
            byte[] cmd = new byte[]{(byte) 0xAA, 0x14, 0x0D, 0x03, 0x01, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d};
            cmd[5] = (byte) hostId;
            cmd[18] = (byte) sum(cmd, 18);
            LogUtils.serial("篮足球LED设置开始等待指令:" + StringUtility.bytesToHexString(cmd));
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        }
    }

    /**
     * LED开始计时
     *
     * @param hostId
     * @param result
     */
    public void setRadioLedStartTime(int hostId, Basketball868Result result) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x14, 0x0D, 0x02, 0x01, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d};
        cmd[5] = (byte) hostId;
        cmd[14] = (byte) (result.getHour() & 0xff);
        cmd[15] = (byte) (result.getMinth() & 0xff);
        cmd[16] = (byte) (result.getSencond() & 0xff);
        cmd[17] = (byte) (result.getMinsencond() & 0xff);
        cmd[18] = (byte) sum(cmd, 18);
        LogUtils.serial("篮足球LED开始计时指令:" + StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    public void setRadioLedStartTimeTo(int hostId, long timerDate, int accuracy) {
        ledManager.ballTimeControl(hostId, true, true, true, timerDate, accuracy, false, 2);
    }

    /**
     * 设置停止
     */
    public void setRadioStopTime(int hostId) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x14, 0x0D, 0x03, 0x01, 0x00, 0x01, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d};
        cmd[5] = (byte) hostId;
        cmd[18] = (byte) sum(cmd, 18);
        LogUtils.serial("发送篮足球停止工作状态指令:" + StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 设置空闲
     *
     * @param hostId
     */
    public void setRadioFreeStates(int hostId) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x14, 0x0D, 0x03, 0x01, 0x00, 0x01, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d};
        cmd[5] = (byte) hostId;
        cmd[18] = (byte) sum(cmd, 18);
        LogUtils.serial("篮足球设置空闲指令:" + StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 暂停走表（显示屏）
     */
    public void setRadioPause(int hostId) {
        byte[] cmd = new byte[]{(byte) 0xAA, 0x14, 0x0D, 0x02, 0x01, 0x00, 0x00, 0x9, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d};
        cmd[5] = (byte) hostId;
        cmd[18] = (byte) sum(cmd, 18);
        LogUtils.serial("篮足球暂停走表(显示屏)指令:" + StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 显示屏
     *
     * @param showType UP =  1 ，DOWN=2
     * @param
     * @return
     */
    public void setLedShowData(int hostId, String data, int showType, Paint.Align align) {

        int x = 0;
        try {

            byte[] dataByte = data.getBytes("GB2312");

            if (dataByte.length > 10) {
                byte[] copyData = new byte[10];
                System.arraycopy(dataByte, 0, copyData, 0, copyData.length);
                dataByte = copyData;
            }
            if (align == Paint.Align.CENTER) {
                x = (10 - data.getBytes("GBK").length) / 2;
            } else if (align == Paint.Align.RIGHT) {
                x = 10 - data.getBytes("GBK").length;
            }
            setLedShowData(hostId, dataByte, x, showType);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    /**
     * 显示屏
     *
     * @param showType UP =  1 ，DOWN=2
     * @param
     * @return
     */
    public void setLedShowData(int hostId, byte[] dataByte, int x, int showType) {
        byte[] cmd = new byte[dataByte.length + 17];
        cmd[0] = (byte) 0xAA;
        cmd[1] = (byte) cmd.length;
        cmd[2] = 0x0D;
        cmd[3] = 0x02;
        cmd[4] = 0x01;
        cmd[5] = (byte) hostId;
        cmd[7] = 0x8;
        cmd[9] = 0x01;
        cmd[12] = (byte) showType;
        cmd[13] = (byte) x;
        cmd[14] = (byte) (dataByte.length & 0xff);
        System.arraycopy(dataByte, 0, cmd, 15, dataByte.length);
        cmd[cmd.length - 2] = (byte) sum(cmd, cmd.length - 2);
        cmd[cmd.length - 1] = 0x0d;
        LogUtils.serial("发送篮足球显示屏指令:" + StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));

    }


    private int sum(byte[] cmd, int index) {
        int sum = 0;
        for (int i = 1; i < index; i++) {
            sum += cmd[i];
        }
        return sum;
    }

    public void setUseLedType(int useLedType) {
        this.useLedType = useLedType;

    }

    public void hiddenTime(int hostId, int timeFormat) {
        ledManager.ballTimeControl(hostId, false, false, false, 0, timeFormat, false, 2);
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
            ballManager.init();
            return ballManager;
        }
    }
}
