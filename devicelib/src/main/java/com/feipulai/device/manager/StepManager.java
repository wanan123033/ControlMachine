package com.feipulai.device.manager;


import android.util.Log;

import com.feipulai.device.ic.utils.IC_ResultResolve;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Created by zzs on  2020/4/20
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StepManager {

    public static final byte[] STEP_SYSN = "$227OS01410017300000000".getBytes();


    public static void stepSysn() {

        byte[] cmd = new byte[STEP_SYSN.length + 2];
        System.arraycopy(STEP_SYSN, 0, cmd, 0, STEP_SYSN.length);
        cmd[cmd.length - 2] = (byte) 0X0D;
        cmd[cmd.length - 1] = (byte) 0X0A;

        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.CONVERTER, cmd));
    }

    public static void stepTest(String second) {
//        public static final byte[] STEP_TEST = "$228OT01410017300000000".getBytes();
        StringBuilder cmdString = new StringBuilder("$228OT01410017300000000");
        cmdString.replace(cmdString.length() - second.length(), cmdString.length(), second);


        byte[] STEP_TEST = cmdString.toString().getBytes();

        int sum = 0;
        //0b 命令(获取数据)不需要校验
        for (int i = 4; i < STEP_TEST.length; i++) {
            sum += (STEP_TEST[i] & 0xff);
        }
        byte[] sumByte = ((sum % 256) + "").getBytes();


        System.arraycopy(sumByte, 0, STEP_TEST, 1, sumByte.length);

        byte[] cmd = new byte[STEP_TEST.length + 2];
        System.arraycopy(STEP_TEST, 0, cmd, 0, STEP_TEST.length);
        cmd[cmd.length - 2] = (byte) 0X0D;
        cmd[cmd.length - 1] = (byte) 0X0A;
        try {
            Log.d("868", new String(cmd, "ISO8859-1"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.CONVERTER, cmd));
    }

}
