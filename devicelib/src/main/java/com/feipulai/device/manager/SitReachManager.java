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
public class SitReachManager {

    public static final int PROJECT_CODE_SIT_REACH = 0x04;  //坐位体前屈


    private int projectCode;

    /**
     * @param projectCode {@link #PROJECT_CODE_SIT_REACH}
     */
    public SitReachManager(int projectCode) {
        this.projectCode = projectCode;
    }

    private void wrapAndSend(byte[] cmd) {
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 【0】0xAA ：包头
     * 【1】0x15 ：包长
     * 【2】0x04 ：项目编号
     * 【3】0x03 ：目标设备编号（子设备）
     * 【4】0x01 ：本设备编号（主机）
     * 【5】0xXX ：本设备主机号
     * 【6】0xXX ：目标设备子机号
     * 【7】0x02 ：命令（设置参数）
     * 【8】0xXX ：目标设备序列号（4字节）
     * 【9】0xXX ：
     * 【10】0xXX ：
     * 【11】0xXX ：
     * 【12】0xXX ：设置目标设备无线频道号
     * 【13】0xXX ：设置目标设备无线传输速率
     * 【14】0xXX ：设置目标设备主机号（与无线频道无关联）
     * 【15】0xXX ：设置目标设备子机号
     * 【16】0x00 ：保留
     * 【17】0x00 ：保留
     * 【18】0x00 ：保留
     * 【19】0xXX ：校验和（除本字节及包头包尾）
     * 【20】0x0D ：包尾
     *
     * 多行编辑 快捷键 Alt+ j
     */
    public void setFrequency(int targetChannel, int deviceId, int hostId) {

        byte[] buf = new byte[21];
        buf[0] = (byte) 0xAA;//包头
        buf[1] = 0x15;    //包长
        buf[2] = (byte) (projectCode & 0xff); //测试项目
        buf[3] = 0x03;     //子机
        buf[4] = (byte) 0x01;   //本设备编号（主机）
        buf[5] = (byte) (hostId & 0xff);
        buf[6] = (byte) (deviceId & 0xff);
        buf[7] = 0x02;      //命令
        buf[8] = 0x00; //(targetChannel & 0xff); //高字节在先
        buf[9] = 0x00;
        buf[10] = 0x00;
        buf[11] = 0x00;
        buf[12] = (byte) (targetChannel & 0xff); //高字节在先
        buf[13] = 0x04;
        buf[14] = (byte) (hostId & 0xff);
        buf[15] = (byte) (deviceId & 0xff);
        buf[16] = 0;
        buf[17] = 0;
        buf[18] = 0;
        for (int i = 1; i < 19; i++) {
            buf[19] += buf[i] & 0xff;
        }
        buf[20] = 0x0d;   //包尾
        LogUtils.normal(buf.length+"---"+ StringUtility.bytesToHexString(buf)+"---设置参数指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, buf));
        RadioChannelCommand command1 = new RadioChannelCommand(targetChannel);
        LogUtils.normal(command1.getCommand().length+"---"+ StringUtility.bytesToHexString(command1.getCommand())+"---切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command1));
    }


    /**
     * 开始测试,该操作完成->倒计时结束,进入计时
     *
     */
    public void startTest(int hostId, int deviceId) {
        //AA 15 04 03 01 03 01 04 00 00 00 00 00 00 00 00 00 00 00 25 0D
        byte[] buf = new byte[21];
        buf[0] = (byte) 0xAA;//包头
        buf[1] = 0x15;    //包长
        buf[2] = (byte) (projectCode & 0xff); //项目编号
        buf[3] = 0x03;     //子机
        buf[4] = (byte) 0x01;   //本设备编号（主机）
        buf[5] = (byte) (hostId & 0xff);
        buf[6] = (byte) (deviceId & 0xff);
        buf[7] = 0x04;      //命令
        buf[8] = 0x00;  //高字节在先
        buf[9] = 0x00;
        buf[10] = 0x00;
        buf[11] = 0x00;
        buf[12] = 0x00;
        buf[13] = 0x00;
        buf[14] = 0x00;
        buf[15] = 0x00;
        buf[16] = 0x00;
        buf[17] = 0x00;
        buf[18] = 0x00;
        for (int i = 1; i < 19; i++) {
            buf[19] += buf[i] & 0xff;
        }
        buf[20] = 0x0d;   //包尾
        LogUtils.normal(buf.length+"---"+ StringUtility.bytesToHexString(buf)+"---开始指令");
        wrapAndSend(buf);
    }



    /**
     * @param deviceId 子机id
     *  【0】0xAA ：包头
     *  【1】0x15 ：包长
     *  【2】0x04 ：项目编号
     *  【3】0x03 ：目标设备编号（子设备）
     *  【4】0x01 ：本设备编号（主机）
     *  【5】0xXX ：本设备主机号
     *  【6】0xXX ：目标设备子机号
     *  【7】0x03 ：命令（查询状态）
     *  【8】0xXX ：目标设备序列号（4字节）
     *  【9】0xXX ：
     *  【10】0xXX ：
     *  【11】0xXX ：
     *  【12】0x00 ：保留
     *  【13】0x00 ：保留
     *  【14】0x00 ：保留
     *  【15】0x00 ：保留
     *  【16】0x00 ：保留
     *  【17】0x00 ：保留
     *  【18】0x00 ：保留
     *  【19】0xXX ：校验和（除本字节及包头包尾）
     *  【20】0x0D ：包尾
     */
    public void getState(int deviceId,int hostId) {
        byte[] buf = new byte[21];
        buf[0] = (byte) 0xAA;//包头
        buf[1] = 0x15;    //包长
        buf[2] = (byte) (projectCode & 0xff); //项目编号
        buf[3] = 0x03;     //子机
        buf[4] = (byte) 0x01;   //本设备
        buf[5] = (byte) (hostId & 0xff);//主机号
        buf[6] = (byte) (deviceId & 0xff);
        buf[7] = 0x03;      //命令
        buf[8] = 0x00;  //高字节在先
        buf[9] = 0x00;
        buf[10] = 0x00;
        buf[11] = 0x00;
        buf[12] = 0x00;
        buf[13] = 0x00;
        buf[14] = 0x00;
        buf[15] = 0x00;
        buf[16] = 0x00;
        buf[17] = 0x00;
        buf[18] = 0x00;
        for (int i = 1; i < 19; i++) {
            buf[19] += buf[i] & 0xff;
        }
        buf[20] = 0x0d;   //包尾
        LogUtils.normal(buf.length+"---"+ StringUtility.bytesToHexString(buf)+"---获取状态指令");
        wrapAndSend(buf);
    }

    /**
     * 结束测试
     */
    public void endTest(int deviceId ,int hostId) {
        //AA 15 04 03 01 03 01 05 00 00 00 00 00 00 00 00 00 00 00 26 0D
        byte[] buf = new byte[21];
        buf[0] = (byte) 0xAA;//包头
        buf[1] = 0x15;    //包长
        buf[2] = (byte) (projectCode & 0xff); //项目编号
        buf[3] = 0x03;     //子机
        buf[4] = (byte) 0x01;   //本设备编号（主机）
        buf[5] = (byte) (hostId & 0xff);
        buf[6] = (byte) (deviceId & 0xff);
        buf[7] = 0x05;      //命令
        buf[8] = 0x00;  //高字节在先
        buf[9] = 0x00;
        buf[10] = 0x00;
        buf[11] = 0x00;
        buf[12] = 0x00;
        buf[13] = 0x04;
        buf[14] = 0x00;
        buf[15] = 0x00;
        buf[16] = 0x00;
        buf[17] = 0x00;
        buf[18] = 0x00;
        for (int i = 1; i < 19; i++) {
            buf[19] += buf[i] & 0xff;
        }
        buf[20] = 0x0d;   //包尾
        LogUtils.normal(buf.length+"---"+ StringUtility.bytesToHexString(buf)+"---结束测试指令");
        wrapAndSend(buf);
    }

    /**
     * 空闲
     * @param deviceId
     * @param hostId
     */
    public void setEmpty(int deviceId ,int hostId){
        //AA 15 04 03 01 03 01 06 00 00 00 00 00 00 00 00 00 00 00 27 0D
        byte[] buf = new byte[21];
        buf[0] = (byte) 0xAA;//包头
        buf[1] = 0x15;    //包长
        buf[2] = (byte) (projectCode & 0xff); //项目编号
        buf[3] = 0x03;     //子机
        buf[4] = (byte) 0x01;   //本设备编号（主机）
        buf[5] = (byte) (hostId & 0xff);
        buf[6] = (byte) (deviceId & 0xff);
        buf[7] = 0x06;      //命令
        buf[8] = 0x00;  //高字节在先
        buf[9] = 0x00;
        buf[10] = 0x00;
        buf[11] = 0x00;
        buf[12] = 0x00;
        buf[13] = 0x00;
        buf[14] = 0x00;
        buf[15] = 0x00;
        buf[16] = 0x00;
        buf[17] = 0x00;
        buf[18] = 0x00;
        for (int i = 1; i < 19; i++) {
            buf[19] += buf[i] & 0xff;
        }
        buf[20] = 0x0d;   //包尾
        LogUtils.normal(buf.length+"---"+ StringUtility.bytesToHexString(buf)+"---设备空闲指令");
        wrapAndSend(buf);
    }

    /**
     * 在使用无线连接时,暂时无法获取到版本号(老版本控制主机也无法实现这个功能)
     */
    public void getVersion(int hostId, int deviceId) {
        byte[] buf = new byte[21];
        buf[0] = (byte) 0xAA;//包头
        buf[1] = 0x15;    //包长
        buf[2] = (byte) (projectCode & 0xff); //项目编号
        buf[3] = 0x03;     //子机
        buf[4] = (byte) 0x01;   //本设备编号（主机）
        buf[5] = (byte) (hostId & 0xff);
        buf[6] = (byte) (deviceId & 0xff);
        buf[7] = 0x07;      //命令
        buf[8] = 0x00;  //高字节在先
        buf[9] = 0x00;
        buf[10] = 0x00;
        buf[11] = 0x00;
        buf[12] = 0x00;
        buf[13] = 0x00;
        buf[14] = 0x00;
        buf[15] = 0x00;
        buf[16] = 0x00;
        buf[17] = 0x00;
        buf[18] = 0x00;
        for (int i = 1; i < 19; i++) {
            buf[19] += buf[i] & 0xff;
        }
        buf[20] = 0x0d;   //包尾
        LogUtils.normal(buf.length+"---"+ StringUtility.bytesToHexString(buf)+"---获取版本信息指令");
        wrapAndSend(buf);
    }

}
