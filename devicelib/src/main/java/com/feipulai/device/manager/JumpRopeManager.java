package com.feipulai.device.manager;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by James on 2018/3/8 0008.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class JumpRopeManager {

    //跳绳状态定义：
    //中断：-1(人为定义,不是跳绳返回结果)
    //复位：0  空闲：1    准备：2    计数：3    结束：4    休眠：5
    public static final int JUMP_ROPE_DISCONNECT = -1;
    public static final int JUMP_ROPE_RESETING = 0;
    public static final int JUMP_ROPE_SPARE = 1;
    public static final int JUMP_ROPE_READY = 2;
    public static final int JUMP_ROPE_COUNTING = 3;
    public static final int JUMP_ROPE_FINISHED = 4;
    public static final int JUMP_ROPE_SLEEPING = 5;

    public static final int DEFAULT_HAND_MODE = 0x0;//手柄模式（默认0）
    public static final int DEFAULT_COUNT_DOWN_TIME = 5;//手柄 倒计时

    public JumpRopeManager() {
    }

    /**
     * @param hostId    主机号
     * @param handId    手柄号 1-n
     * @param handPower 手柄电量,默认0x06
     * @param handGroup 手柄颜色    1 2 3
     */
    public void link(int channel, int hostId, int handId, int handPower, int handGroup) {
        //Log.i("JumpRopePairActivity","setFrequency(0)");
        RadioChannelCommand commod = new RadioChannelCommand(0);
        LogUtils.normal(commod.getCommand().length+"---"+ StringUtility.bytesToHexString(commod.getCommand())+"---跳绳配对手柄指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(commod));
        //设置手柄
        //Log.i("JumpRopePairActivity","setJumpRope");
        setJumpRope(hostId, handId, handPower, handGroup);
        //切换到目标频
        //Log.i("JumpRopePairActivity","setFrequency");
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(channel)));
        //Log.i("JumpRopePairActivity","getJumpRopeState");
        getJumpRopeState(hostId, handId, handGroup);
    }

    /**
     * 设置手柄
     */
    private void setJumpRope(int hostId, int handId, int handPower, int handGroup) {
        //AA/项目号(01)/A2(手柄)/主机号/00/A1(命令)/主机号/柄号/电源/颜色/和校验/0D
        byte[] cmd = {(byte) 0xaa, 0x01, (byte) 0xa2, (byte) (hostId & 0xff), 0x00, (byte) 0xa1, (byte) (hostId & 0xff), (byte) (handId & 0xff), (byte) (handPower
                & 0xff), (byte) (handGroup & 0xff), 0x00, 0x0d};
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += cmd[i] & 0xff;
        }
        cmd[10] = (byte) (sum & 0xff);
        //Log.i("setJumpRope",StringUtility.bytesToHexString(cmd));
        LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---跳绳配对手柄指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 获取手柄状态
     *
     * @param handId    手柄号 1-n
     * @param handGroup 手柄颜色    1 2 3
     */
    public void getJumpRopeState(int hostId, int handId, int handGroup) {
        //AA/项目号(01)/A2(手柄)/主机号/柄号/A0(命令)/颜色/00/01/0D
        byte[] cmd = {(byte) 0xaa, 0x01, (byte) 0xa2, (byte) (hostId & 0xff), (byte) (handId & 0xff), (byte) 0xa0, (byte) (handGroup & 0xff), 0x00, 0x01, 0x0d};
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        //Log.i("getJumpRopeState",StringUtility.bytesToHexString(cmd));
    }

    /**
     * 休眠手柄
     */
    public void sleep(int hostId) {
        //AA/项目号(01)/A2(手柄)/主机号/00/A2(命令)/和校验/0D
        byte cmd[] = {(byte) 0xaa, 0x01, 0x02, (byte) (hostId & 0xff), 0x00, (byte) 0xa2, 0x00, 0x0d};
        int sum = 0;
        for (int i = 0; i < 6; i++) {
            sum += cmd[i] & 0xff;
        }
        cmd[6] = (byte) (sum & 0xff);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * @param handGroup 手柄颜色    1 2 3
     */
    public void endTest(int hostId, int handGroup) {
        // AA/项目号(01)/A2(手柄)/主机号/颜色/A4(命令)/0D
        byte cmd[] = {(byte) 0xaa, 0x01, (byte) 0xa2, (byte) (hostId & 0xff), (byte) (handGroup & 0xff), (byte) 0xa4, 0x0d};
        LogUtils.normal(cmd.length+"---"+StringUtility.bytesToHexString(cmd)+"---跳绳结束测试指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    public void countDown(int hostId, int handGroupNo, int startTime, int testTime) {
        //AA/项目号(01)/A2(手柄)/主机号/颜色/A3(命令)/倒计时数/时间1/时间2/模式/和校验/0D
        byte cmd[] = {(byte) 0xaa, 0x01, (byte) 0xa2, (byte) (hostId & 0xff), (byte) (handGroupNo & 0xff), (byte) 0xa3, (byte) (startTime & 0xff),
                (byte) (testTime >>> 8), (byte) (testTime & 0xff), (byte) (DEFAULT_HAND_MODE & 0xff), 0x00, 0x0d};
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += cmd[i] & 0xff;
        }
        cmd[10] = (byte) (sum & 0xff);
        LogUtils.normal(cmd.length+"---"+StringUtility.bytesToHexString(cmd)+"---跳绳开始指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 倒计时
     *
     * @param hostId      主机号
     * @param handGroupNo 手柄颜色    1 2 3
     * @param startTime   倒计时剩余时间
     * @param testTime    测试时长
     * @param handMode    模式
     */
    public void countDown(int hostId, int handGroupNo, int startTime, int testTime, int handMode) {
        //AA/项目号(01)/A2(手柄)/主机号/颜色/A3(命令)/倒计时数/时间1/时间2/模式/和校验/0D
        byte cmd[] = {(byte) 0xaa, 0x01, (byte) 0xa2, (byte) (hostId & 0xff), (byte) (handGroupNo & 0xff), (byte) 0xa3, (byte) (startTime & 0xff),
                (byte) (testTime >> 8), (byte) (testTime & 0xff), (byte) (handMode & 0xff), 0x00, 0x0d};
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += cmd[i] & 0xff;
        }
        cmd[10] = (byte) (sum & 0xff);
        LogUtils.normal(cmd.length+"---"+StringUtility.bytesToHexString(cmd)+"---跳绳倒计时指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 开始前切换到跳绳频道并确保所有在线手柄处于空闲状态
     *
     * @param hostId      主机号
     * @param handGroupNo 手柄颜色    1,2,3
     */
    public void initHandles(int hostId, int handGroupNo) {
        endTest(hostId, handGroupNo);
        // endTest(hostId,handGroupNo);
    }

    // 清除命令     主机号频段      方向：主机手柄
    // [0]0xAA
    // [1]项目名 跳绳=0x01
    // [2]设备名 手柄=0xA2
    // [3]主机号
    // [4]手柄号
    // [5]命令=0xA6
    // [6]手柄颜色
    // [7]清除类型   0---清回0频段；  1---清回0频段，并切换到单机模式
    // [8]和校验
    // [9]0x0d
    // 说明：若手柄号为0，表示该主机号所有手柄都要响应本清除命令。
    // 若手柄号不为0，表示该手柄号的手柄响应响应清除命令。
    public void kill(int hostId, int handId, int handGroup, int flag) {
        byte cmd[] = {(byte) 0xaa, 0x01, (byte) 0xa2,
                (byte) (hostId & 0xff),
                (byte) (handId & 0xff),
                (byte) 0xa6,
                (byte) (handGroup & 0xff),
                (byte) flag,
                0,
                0x0d};
        int sum = 0;
        for (int i = 0; i < 8; i++) {
            sum += cmd[i] & 0xff;
        }
        cmd[8] = (byte) (sum & 0xff);
        LogUtils.normal(cmd.length+"---"+StringUtility.bytesToHexString(cmd)+"---跳绳清除指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }


}
