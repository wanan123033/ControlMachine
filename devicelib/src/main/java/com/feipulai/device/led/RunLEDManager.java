package com.feipulai.device.led;

import android.graphics.Bitmap;


import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * Created by James on 2017/12/8.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 * <p>
 * LED屏:16x4字符(英文和数字,中文时为8x4个字符)
 */
public class RunLEDManager {
    public static final int LEFT = 0;
    public static final int MIDDLE = 1;
    public static final int RIGHT = 2;
    private byte LED_PK_HEAD = (byte) 0xcc;
    private byte PK_HEAD = (byte) 0xaa;
    private byte SCANN_SCREEN = (byte) 0xa6;
    private byte PK_END = (byte) 0x0d;
    private byte LED_PK_END = (byte) 0x0e;

    private byte ADJUST_BRIGHTNESS = (byte) 0xa4;
    private byte ADJUST_BRIGHT = (byte) 0xbb;
    private byte ADJUST_DARK = (byte) 0xaa;
    private byte COM_DEVICE_NAME = (byte) 0xa1;


    public RunLEDManager() {
    }


    /**
     * 设置主机号(连接)
     */
    public void link(int hostId) {
        SerialDeviceManager.getInstance().sendCommandForLed(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd((byte) 0xc1, (byte) 0x02, (byte) hostId)));//主机号
    }

    private byte[] cmd(byte cmd, byte mark, byte value) {
        byte[] setting = {(byte) 0xBB, 0x0C, (byte) 0xA0, 0x00, (byte) 0xA1, 0x00, cmd, mark, value, 0x00, 0x00, 0x0D};
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += setting[i];
        }
        setting[10] = (byte) sum;
        return setting;
    }

    /**
     * LED显示屏自检
     * @param hostId      主机号
     */
    public void test( int hostId) {
        byte[] cmd = {LED_PK_HEAD,10,PK_HEAD, 0x05, (byte) 0xa1, (byte) (hostId & 0xff), 0x01, SCANN_SCREEN, PK_END,LED_PK_END};
        SerialDeviceManager.getInstance().sendCommandForLed(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd));
    }

    /**
     * 清空LED显示屏
     * @param hostId      主机号
     */
    public void clearScreen(int hostId) {
        byte[] cmd = {LED_PK_HEAD,13,PK_HEAD, 0x05, COM_DEVICE_NAME, (byte) (hostId & 0xff), 0x01, (byte) 0xa5, 0x01, (byte)
                0x00, 0x00, 0x00,LED_PK_END};
        SerialDeviceManager.getInstance().sendCommandForLed(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd));
    }

    /**
     * @param align 对齐方式 默认为{@link #LEFT}   {@link #MIDDLE}   {@link #RIGHT}
     */
    public void showString( int hostId, String str, int y, boolean clearScreen, boolean update, int align) {
        int x = 0;
        if (align == MIDDLE) {
            try {
                x = (16 - str.getBytes("GBK").length) / 2;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (align == RIGHT) {
            try {
                x = 16 - str.getBytes("GBK").length;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        showString( hostId, str, x, y, clearScreen, update);
    }

    /**
     * 显示字符串
     *
     * @param hostId      主机号
     * @param str         需要显示的字符串
     * @param x           字符串显示X轴位置
     * @param y           字符串显示Y轴位置
     * @param clearScreen 是否清空LED显示屏
     * @param update      是否立即更新显示屏信息
     */
    public void showString( int hostId, String str, int x, int y, boolean clearScreen, boolean update) {
        byte[] data = null;
        try {
            data = str.getBytes("GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //if(data.length > 64){
        //	Logger.e("led文字长度 > 64");
        //	return;
        //}
        byte[] cmd = new byte[data.length + 19];
        cmd[0] =  LED_PK_HEAD;
//        cmd[1] = (byte) (machineCodesForLed.get(machineCode) & 0xff);
        cmd[1] = (byte) cmd.length;
        cmd[2] = PK_HEAD;
        cmd[3] = 0x05;
        cmd[4] = COM_DEVICE_NAME;
        cmd[5] = (byte) (hostId & 0xff);
        cmd[6] = 0x01;
        cmd[7] = (byte) 0xa2;
        cmd[8] = (byte) (clearScreen ? 0x01 : 0x000);
        cmd[9] = (byte) (update ? 0x01 : 0x00);
        cmd[10] = (byte) data.length;
        cmd[11] = (byte) (x & 0xff);
        cmd[12] = (byte) (y & 0xff);
        System.arraycopy(data, 0, cmd, 13, data.length);
        cmd[13 + data.length] = 0x00;
        cmd[14 + data.length] = 0x01;
        cmd[15 + data.length] = 0x00;
        cmd[16 + data.length] = 0x00;
        cmd[17 + data.length] = 0x00;
        cmd[18 + data.length] = LED_PK_END;
        SerialDeviceManager.getInstance().sendCommandForLed(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd));
    }

    /**
     * 该命令没有调试过,不推荐使用,也没有需要使用的地方
     * bmp显示命令： 0xAA dev ledDev hostId ledId 0xA3 clr upd len x y n*data 0x00 0x00
     * 0xA3：bmp显示命令
     */
    public void showBitmap(int machineCode, int hostId, Bitmap bitmap, int x, int y, boolean clearScreen, boolean update) {
        byte[] data;
        if (bitmap == null) {
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
        data = baos.toByteArray();
        byte[] cmd = new byte[data.length + 13];
        cmd[0] = (byte) 0xaa;
        cmd[1] = (byte) (machineCode & 0xff);
        cmd[2] = (byte) 0xa1;
        cmd[3] = (byte) (hostId & 0xff);
        cmd[4] = 0x01;
        cmd[5] = (byte) 0xa3;
        cmd[6] = (byte) (clearScreen ? 0x01 : 0x000);
        cmd[7] = (byte) (update ? 0x01 : 0x00);
        cmd[8] = (byte) data.length;
        cmd[9] = (byte) (x & 0xff);
        cmd[10] = (byte) (y & 0xff);
        System.arraycopy(data, 0, cmd, 11, data.length);
        cmd[11 + data.length] = 0x00;
        cmd[12 + data.length] = 0x00;
        SerialDeviceManager.getInstance().sendCommandForLed(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd));
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加亮度
     * @param hostId      主机号
     */
    public void increaseLightness( int hostId) {
        byte[] cmd = {LED_PK_HEAD,11,PK_HEAD, 0x05, (byte) 0xa1, (byte) (hostId & 0xff), 0x01, ADJUST_BRIGHTNESS, ADJUST_BRIGHT,
               PK_END, LED_PK_END};
        SerialDeviceManager.getInstance().sendCommandForLed(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd));
    }

    /**
     * 减少亮度
     *
     * @param machineCode 测试项目
     * @param hostId      主机号
     */
    public void decreaseLightness(int machineCode, int hostId) {
        byte[] cmd = {LED_PK_HEAD,11,PK_HEAD, 0x05, (byte) 0xa1, (byte) (hostId & 0xff), 0x01, ADJUST_BRIGHTNESS, ADJUST_DARK,
                PK_END, LED_PK_END};
        SerialDeviceManager.getInstance().sendCommandForLed(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd));
    }

    /**
     * 亮度最暗
     *
     * @param machineCode 测试项目
     * @param hostId      主机号
     */
    public void darkest(int machineCode, int hostId) {
        byte[] cmd = {LED_PK_HEAD,11,PK_HEAD, 0x05, (byte) 0xa1, (byte) (hostId & 0xff), 0x01, ADJUST_BRIGHTNESS, 0x00,
                PK_END, LED_PK_END};
        SerialDeviceManager.getInstance().sendCommandForLed(new ConvertCommand(ConvertCommand.CmdTarget.RS232, cmd));
    }

    /**
     * 显示当前项目信息
     *
     * @param hostId 主机号
     */
    public void resetLEDScreen(int hostId,String title) {
        showString(hostId,title+hostId,getX(title+hostId),0,true,false);
        showString(hostId, "菲普莱体育", 3, 3, false, true);
    }

    public int getX(String showMsg) {
        int x;
        try {
            x = 32 / showMsg.getBytes("GBK").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            x = 0;
        }
        return x;
    }

}
