package com.feipulai.device.manager;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.orhanobut.logger.examlogger.LogUtils;

/**
 * Created by James on 2018/5/14 0014.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitPushUpManager {

    //工作状态（空闲=1 准备=2 计数=3 结束=4其他=未知）。这里定义-1为断开
    public static final int STATE_DISCONNECT = -1;
    public static final int STATE_FREE = 1;
    public static final int STATE_READY = 2;
    public static final int STATE_COUNTING = 3;
    public static final int STATE_ENDED = 4;

    public static final int PROJECT_CODE_SIT_UP = 5;// 5—仰卧起坐
    public static final int PROJECT_CODE_PUSH_UP = 8;// 8—俯卧撑
    public static final int PROJECT_CODE_SARGENT = 1;// 摸高
    public static final int PROJECT_CODE_VOLLEY_BALL = 10;// 排球
    public static final int PROJECT_CODE_LZQYQ = 0x0D;  //篮足球运球
    public static final int PROJECT_CODE_SXQ = 0x07;  //实心球

    public static final int DEFAULT_COUNT_DOWN_TIME = 5;

    private int projectCode;
    /**
     *
     */
    private int connectType;//0 有线 1 无线

    /**
     * @param projectCode {@link #PROJECT_CODE_SIT_UP} {@link #PROJECT_CODE_PUSH_UP}
     */
    public SitPushUpManager(int projectCode, int sendManagerType) {
        this.connectType = sendManagerType;
        this.projectCode = projectCode;
    }

    /**
     * @param projectCode {@link #PROJECT_CODE_SIT_UP} {@link #PROJECT_CODE_PUSH_UP}
     */
    public SitPushUpManager(int projectCode) {
        this.projectCode = projectCode;
    }


    private void wrapAndSend(byte[] cmd) {
        if (projectCode == PROJECT_CODE_SIT_UP || connectType == 1) {
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        } else {
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd));
        }
    }

    /**
     * 设置终端频率和设备ID,该函数中时延为调优过得,请勿随意更改
     * 该方法执行完成后,会跳转到主机号相对应的频率
     * <p>
     * 子机开机0频段发送
     * [00] [01]：包头高字节0x54  低字节0x55
     * [02] [03]：长度高字节0x00  低字节0x10
     * [04]：子机号
     * [05]：项目编号   5—仰卧起坐    8—俯卧撑
     * [06]：0   –单机模式
     * [07]：11   --设备频段号命令
     * [08]: 通道号
     * [09]: 速率
     * [10]-[12]: 0 0 0
     * [13]：累加和（从02到12共11个字节算术和的低字节）
     * [14] [15]：包尾高字节0x27   低字节0x0d
     * <p>
     * 若 子机号 或 通道号 不同，主机切换到刚才收到的通道号，下发：
     * [00] [01]：包头高字节0x54  低字节0x44
     * [02] [03]：长度高字节0x00  低字节0x10
     * [04]：子机号
     * [05]：项目编号   5—仰卧起坐    8—俯卧撑
     * [06]：0   –单机模式
     * [07]：11   --设备频段号命令
     * [08]: 通道号
     * [09]: 速率
     * [10]-[12]: 0 0 0
     * [13]：累加和（从02到12共11个字节算术和的低字节）
     * [14] [15]：包尾高字节0x27   低字节0x0d
     * <p>
     * 发完后，切换到本机通道号，等待接收上面 0x54 0x55的命令
     *
     * @param originFrequency 原来的频段(终端目前工作所在频段)
     * @param deviceId        终端设备的设备ID
     * @param hostId          主机号
     */
    public void setFrequency(int targetChannel, int originFrequency, int deviceId, int hostId) {

        byte[] buf = new byte[16];
        buf[0] = 0x54;
        buf[1] = 0x44;    //包头
        buf[2] = 0;       //包长
        buf[3] = 0x10;
        buf[4] = (byte) (deviceId & 0xff);      //设备号
        buf[5] = (byte) (projectCode & 0xff);     //测试项目
        buf[6] = 0;       //单机模式
        buf[7] = 0x0b;      //命令
        buf[8] = (byte) (targetChannel & 0xff); //高字节在先
        buf[9] = 4;
        buf[10] = 0;
        buf[11] = 0;
        buf[12] = 0;
        for (int i = 2; i < 13; i++) {
            buf[13] += buf[i] & 0xff;
        }
        buf[14] = 0x27;
        buf[15] = 0x0d;   //包尾
        //Logger.i(StringUtility.bytesToHexString(buf));
        //先切到通信频段
        //Log.i("james","originFrequency:" + originFrequency);
        RadioChannelCommand command = new RadioChannelCommand(originFrequency);
        LogUtils.normal(command.getCommand().length+"---"+StringUtility.bytesToHexString(command.getCommand())+"---仰卧起坐俯卧撑切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(originFrequency)));
        //Log.i("james",StringUtility.bytesToHexString(buf));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtils.normal(buf.length+"---"+StringUtility.bytesToHexString(buf)+"---仰卧起坐俯卧撑设置参数指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, buf));
        RadioChannelCommand command1 = new RadioChannelCommand(targetChannel);
        LogUtils.normal(command1.getCommand().length+"---"+StringUtility.bytesToHexString(command1.getCommand())+"---仰卧起坐俯卧撑切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command1));
    }

    /**
     * 肺活量旧版一对多
     */
    public void setFrequencyFHL(int targetChannel, int originFrequency, int deviceId, int hostId) {

        byte[] buf = new byte[16];
        buf[0] = (byte) 0xAB;
        buf[1] = 0x02;    //包头
        buf[2] = 0X10;       //包长
        buf[3] = 0x02;
        buf[4] = (byte) (deviceId & 0xff);      //设备号
        buf[5] = 0X01;
        buf[6] = (byte) (targetChannel & 0xff); //高字节在先
        buf[7] = 0x04;    //传输速率

        buf[8] = 0;
        buf[9] = 0;
        buf[10] = 0;
        buf[11] = 0;
        buf[12] = 0;
        buf[13] = 0;
        for (int i = 0; i < 14; i++) {
            buf[14] += buf[i] & 0xff;
        }
        buf[15] = 0x0A;   //包尾
        //Logger.i(StringUtility.bytesToHexString(buf));
        //先切到通信频段
        //Log.i("james","originFrequency:" + originFrequency);
        RadioChannelCommand command = new RadioChannelCommand(originFrequency);
        LogUtils.normal(command.getCommand().length+"---"+ StringUtility.bytesToHexString(command.getCommand())+"---肺活量切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command));
        //Log.i("james",StringUtility.bytesToHexString(buf));
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtils.normal(buf.length+"---"+ StringUtility.bytesToHexString(buf)+"---肺活量设置参数指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, buf));
        RadioChannelCommand command1 = new RadioChannelCommand(targetChannel);
        LogUtils.normal(command1.getCommand().length+"---"+ StringUtility.bytesToHexString(command1.getCommand())+"---肺活量切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command1));
    }

    /**
     * 肺活量新版一对多
     */
    public void setFrequencyNewFHL(int targetChannel, int originFrequency, int deviceId, int hostId) {
        byte[] buf = new byte[18];
        buf[0] = (byte) 0xAA;
        buf[1] = 0x12;    //包长
        buf[2] = 0X09;    //项目编号
        buf[3] = 0x03;    //目标设备编号（子设备）
        buf[4] = 0x01;    //本设备编号（主机）
        buf[5] = (byte) hostId;//本设备主机号
        buf[6] = (byte) (deviceId & 0xff); //目标设备子机号
        buf[7] = 0x01;    //命令设置参数

        buf[8] = 0;//目标设备序列号（4字节）
        buf[9] = 0;
        buf[10] = 0;
        buf[11] = 0x00;
        buf[12] = (byte) targetChannel;
        buf[13] = 0x04;
        buf[14] = (byte) hostId;
        buf[15] = (byte) deviceId;
        for (int i = 1; i < 16; i++) {
            buf[16] += buf[i];
        }
        buf[17] = 0x0D;   //包尾
        //Logger.i(StringUtility.bytesToHexString(buf));
        //先切到通信频段
        //Log.i("james","originFrequency:" + originFrequency);
//        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(originFrequency)));
        //Log.i("james",StringUtility.bytesToHexString(buf));
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtils.normal(buf.length+"---"+ StringUtility.bytesToHexString(buf)+"---肺活量设置参数指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, buf));
        RadioChannelCommand command1 = new RadioChannelCommand(targetChannel);
        LogUtils.normal(command1.getCommand().length+"---"+ StringUtility.bytesToHexString(command1.getCommand())+"---肺活量切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command1));
    }

    /**
     * 摸高设置频段
     *
     * @param originFrequency
     * @param deviceId
     * @param hostId
     */
    public void setFrequencyMG(int targetChannel, int originFrequency, int deviceId, int hostId) {
        byte[] buf = new byte[18];
        buf[0] = 0x54;
        buf[1] = 0x44;    //包头
        buf[2] = 0;       //包长
        buf[3] = 0x12;
        buf[4] = (byte) (deviceId & 0xff);      //设备号
        buf[5] = (byte) (projectCode & 0xff);     //测试项目
        buf[6] = 0x01;       //无线模式
        buf[7] = 0x01;      //命令
        buf[8] = (byte) (targetChannel & 0xff); //高字节在先
        buf[9] = 4;
        buf[10] = 0;
        buf[11] = (byte) (hostId & 0xff);
        buf[12] = 0;
        buf[13] = 0;
        buf[14] = 0;
        for (int i = 2; i < 13; i++) {
            buf[15] += buf[i] & 0xff;
        }
        buf[16] = 0x27;
        buf[17] = 0x0d;   //包尾
        //Logger.i(StringUtility.bytesToHexString(buf));
        //先切到通信频段
        //Log.i("james","originFrequency:" + originFrequency);
        RadioChannelCommand command = new RadioChannelCommand(originFrequency);
        LogUtils.normal(command.getCommand().length+"---"+ StringUtility.bytesToHexString(command.getCommand())+"---摸高切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command));
        //Log.i("james",StringUtility.bytesToHexString(buf));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtils.normal(buf.length+"---"+ StringUtility.bytesToHexString(buf)+"---摸高设置参数指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, buf));
        RadioChannelCommand command1 = new RadioChannelCommand(targetChannel);
        LogUtils.normal(command1.getCommand().length+"---"+ StringUtility.bytesToHexString(command1.getCommand())+"---摸高切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command1));
    }

    /**
     * 实心球设置频段
     *
     * @param originFrequency
     * @param deviceId
     * @param hostId
     */
    public void setFrequencySXQ(int targetChannel, int originFrequency, int deviceId, int hostId) {
        byte[] buf = new byte[21];
        buf[0] = (byte) 0xAA;//包头
        buf[1] = 0x15;    //包长
        buf[2] = 0x07;       //项目编号
        buf[3] = 0x03; //子机
        buf[4] = 0x01;      //主机
        buf[5] = (byte) (hostId & 0xff);     //主机号
        buf[6] = (byte) (deviceId & 0xff);       //子机号
        buf[7] = 0x02;      //命令
        buf[8] = 0;
        buf[9] = 0;
        buf[10] = 0;
        buf[11] = 0;
        buf[12] = (byte) targetChannel;
        buf[13] = 0x04;
        buf[14] = (byte) (hostId & 0xff);
        buf[15] = (byte) (deviceId & 0xff);
        buf[16] = 0x00;
        buf[17] = 0x00;
        buf[18] = 0x00;
        for (int i = 1; i < 19; i++) {
            buf[19] += buf[i] & 0xff;
        }
        buf[20] = 0x0d;   //包尾
        //Logger.i(StringUtility.bytesToHexString(buf));
        //先切到通信频段
        //Log.i("james","originFrequency:" + originFrequency);
//        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(originFrequency)));
        //Log.i("james",StringUtility.bytesToHexString(buf));
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        LogUtils.normal(buf.length+"---"+ StringUtility.bytesToHexString(buf)+"---实心球设置参数指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, buf));
        RadioChannelCommand command1 = new RadioChannelCommand(targetChannel);
        LogUtils.normal(command1.getCommand().length+"---"+ StringUtility.bytesToHexString(command1.getCommand())+"---实心球切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(targetChannel)));
    }


    /**
     * 开始测试,该操作完成->倒计时结束,进入计时,设备将处在{@link #STATE_READY} 准备状态
     *
     * @param countTime 倒计时时间
     * @param testTime  测试时间
     */
    public void startTest(int countTime, int testTime) {
        //[00][01]：包头高字节0x54  低字节0x44
        //[02][03]：长度高字节0x00  低字节0x10
        //[04]:0    --表示所有子机都要响应
        //[05]:项目编号 5—仰卧起坐 8—俯卧撑
        //[06]:0    –-单机模式
        //[07]:1    --开始命令
        //[08]:     倒计时时间,单位为秒
        //[09][10]: 计数时间(单位为秒) 高字节 低字节
        //[11][12]:0  0
        //[13]:     累加和(从02到12共11个字节算术和的低字节)
        //[14][15]: 包尾高字节0x27   低字节0x0d
        byte[] cmd = {0x54, 0x44, 0, 0x10, 0, 0, 0, 0x01, 0, 0, 0, 0, 0, 0, 0x27, 0x0d};
        cmd[5] = (byte) (projectCode & 0xff);
        cmd[8] = (byte) (countTime & 0xff);
        cmd[9] = (byte) (testTime >>> 8);
        cmd[10] = (byte) (testTime & 0xff);
        for (int i = 2; i < 13; i++) {
            cmd[13] += cmd[i] & 0xff;
        }
        //Log.i("james",StringUtility.bytesToHexString(cmd));
        if (cmd[5] == 5)
            LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---仰卧起坐开始测试指令");
        else if (cmd[5] == 8)
            LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---俯卧撑开始测试指令");
        wrapAndSend(cmd);
    }

    /**
     * @param deviceId 子机id
     */
    public void getState(int deviceId) {
        getState(deviceId, 65);
    }

    /**
     * @param deviceId 子机id
     * @param baseline 易中难，分别对应角度是55,65,75度,默认65度
     *                 俯卧撑不使用该字节,填0
     */
    public void getState(int deviceId, int baseline) {
        if (projectCode == PROJECT_CODE_PUSH_UP) {
            baseline = 0;
        }
        //[00] [01]：包头高字节0x54   低字节0x44
        //[02] [03]：长度高字节0x00   低字节0x10
        //[04]:子机id （ 一对一模式 0）
        //[05]:项目编号   5—仰卧起坐    8—俯卧撑
        //[06]:0   –单机模式
        //[07]:4   --查询
        //[08]:主机baseline  (易中难，分别对应角度是55，65，75度，默认65度)
        //[09] --[12]：0  0  0  0
        //[13]:累加和(从02到12共11个字节算术和的低字节)
        //[14] [15]:包尾高字节0x27   低字节0x0d
        byte[] cmd = {0x54, 0x44, 0, 0x10, 1, 0, 0, 0x04, 0, 0, 0, 0, 0, 0, 0x27, 0x0d};
        cmd[4] = (byte) (deviceId & 0xff);
        cmd[5] = (byte) (projectCode & 0xff);

        if (projectCode == PROJECT_CODE_PUSH_UP) {
            cmd[8] = 0;
        } else {
            cmd[8] = (byte) (baseline & 0xff);
        }
        for (int i = 2; i < 13; i++) {
            cmd[13] += cmd[i] & 0xff;
        }
        if (cmd[5] == 5)
            LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---仰卧起坐获取状态指令");
        else if (cmd[5] == 8)
            LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---俯卧撑获取状态指令");
        //Log.i("james",StringUtility.bytesToHexString(cmd));
        wrapAndSend(cmd);
    }

    /**
     * 结束测试
     */
    public void endTest() {
        //[00] [01]：包头高字节0x54 低字节0x44
        //[02] [03]：长度高字节0x00 低字节0x10
        //[04]：0-- 表示所有子机都要响应
        //[05]：项目编号 5—仰卧起坐 8—俯卧撑
        //[06]：0   –单机模式
        //[07]：2-- 结束命令
        //[08]-- - [12]：0 0 0 0 0
        //[13]：累加和（从02到12共11个字节算术和的低字节）
        //[14] [15]：包尾高字节0x27 低字节0x0d
        byte[] cmd = {0x54, 0x44, 0, 0x10, 0, 0, 0, 0x02, 0, 0, 0, 0, 0, 0, 0x27, 0x0d};
        cmd[5] = (byte) (projectCode & 0xff);
        for (int i = 2; i < 13; i++) {
            cmd[13] += cmd[i] & 0xff;
        }
        //Log.i("james",StringUtility.bytesToHexString(cmd));
        if (cmd[5] == 5)
            LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---仰卧起坐结束测试指令");
        else if (cmd[5] == 8)
            LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---俯卧撑结束测试指令");
        wrapAndSend(cmd);
    }

    /**
     * 在使用无线连接时,暂时无法获取到版本号(老版本控制主机也无法实现这个功能)
     */
    public void getVersion(int projectCode, int machineId) {
        //[00] [01]：包头高字节0x54   低字节0x44
        //[02] [03]：长度高字节0x00   低字节0x10
        //[04] 0号子机id
        //[05] 项目编号   5—仰卧起坐    8—俯卧撑
        //[06] 0   –单机模式
        //[07] 12  --查询版本
        //[08] --[12]：0  0  0  0  0
        //[13] 累加和(从02到12共11个字节算术和的低字节)
        //[14] [15]:包尾高字节0x27   低字节0x0d
        byte[] cmd = {0x54, 0x44, 0, 0x10, 1, 0, 0, 0x0c, 0, 0, 0, 0, 0, 0, 0x27, 0x0d};
        cmd[4] = (byte) (machineId & 0xff);
        cmd[5] = (byte) (projectCode & 0xff);
        for (int i = 2; i < 13; i++) {
            cmd[13] += cmd[i] & 0xff;
        }
        //Log.i("james",StringUtility.bytesToHexString(cmd));
        if (cmd[5] == 5)
            LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---仰卧起坐获取版本信息指令");
        else if (cmd[5] == 8)
            LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---俯卧撑获取版本信息指令");
        wrapAndSend(cmd);
    }

    /**
     * 设置计数角度命令
     * 子机收到该命令，将相应角度设置并保存。
     * <p>
     * 该命令暂不使用
     */
    public void setBaseline(int projectCode, int baseLine) {
        //[00] [01]：包头高字节0x54  低字节0x44
        //[02] [03]：长度高字节0x00  低字节0x10
        //[04]：0   --表示所有子机都要响应
        //[05]：项目编号   5—仰卧起坐    8—俯卧撑
        //[06]：0   –单机模式
        //[07]：7   -- SetBaselineCmd命令
        //[08]: 主机baseline (易中难，分别对应角度是55，65，75度，默认65度)
        //[09]-[12]: 0 0 0 0
        //[13]：累加和(从02到12共11个字节算术和的低字节)
        //[14] [15]：包尾高字节0x27   低字节0x0d
        byte[] cmd = {0x54, 0x44, 0, 0x10, 0, 0, 0, 0x07, 0, 0, 0, 0, 0, 0, 0x27, 0x0d};
        cmd[5] = (byte) (projectCode & 0xff);
        cmd[8] = (byte) (baseLine & 0xff);
        for (int i = 2; i < 13; i++) {
            cmd[13] += cmd[i] & 0xff;
        }
        if (cmd[5] == 5)
            LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---仰卧起坐设置计数角度命令指令");
        else if (cmd[5] == 8)
            LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---俯卧撑设置计数角度命令指令");
        wrapAndSend(cmd);
    }


}
