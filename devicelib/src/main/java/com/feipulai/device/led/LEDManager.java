package com.feipulai.device.led;

import android.graphics.Bitmap;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.orhanobut.logger.utils.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Created by James on 2017/12/8.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 * <p>
 * LED屏:16x4字符(英文和数字,中文时为8x4个字符)
 */
public class LEDManager {

    public static final int LEFT = 0;
    public static final int MIDDLE = 1;
    public static final int RIGHT = 2;
    public static final int LED_VERSION_4_1 = 1;
    //默认为0 第一版 led4.1 传1
    private int versions = 0;

    public LEDManager() {
    }

    public LEDManager(int versions) {
        this.versions = versions;
    }

    static final Map<Integer, Integer> machineCodesForLed = new HashMap<>();

    static {
        // 体侧版所有项目
        machineCodesForLed.put(0, 0);//公共频道	0
        machineCodesForLed.put(ItemDefault.CODE_TS, 1);//跳绳计数
        machineCodesForLed.put(ItemDefault.CODE_HW, 5);//身高体重
        machineCodesForLed.put(ItemDefault.CODE_LDTY, 4);//立定跳远
        machineCodesForLed.put(ItemDefault.CODE_YWQZ, 10);//仰卧起坐
        machineCodesForLed.put(ItemDefault.CODE_ZWTQQ, 2);//坐位体
        machineCodesForLed.put(ItemDefault.CODE_HWSXQ, 6);//实心球
//		machineCodesForLed.put(ItemDefault.CODE_50M,5);//红外计时
        machineCodesForLed.put(ItemDefault.CODE_ZFP, 5);//红外计时
        // 体侧版没有的项目
        machineCodesForLed.put(ItemDefault.CODE_SL, 0);//视力
        machineCodesForLed.put(ItemDefault.CODE_FWC, 7);//俯卧撑
        machineCodesForLed.put(ItemDefault.CODE_MG, 3);//摸高测试
        machineCodesForLed.put(ItemDefault.CODE_YTXS, 7);//引体向上
        machineCodesForLed.put(ItemDefault.CODE_ZCP, 0);//中长跑
        machineCodesForLed.put(ItemDefault.CODE_PQ, 8);//排球垫球
        machineCodesForLed.put(ItemDefault.CODE_FHL, 8);//肺活量
        machineCodesForLed.put(ItemDefault.CODE_WLJ, 6);//握力
    }

    /**
     * 连接屏幕
     *
     * @param machineCode 测试项目机器码{@link ItemDefault}.CODE_XXX
     * @param hostId      主机号
     */
    public void link(int channel, int machineCode, int hostId) {
        if (machineCodesForLed.get(machineCode) == null) {
            return;
        }
        if (versions == LED_VERSION_4_1) {
            link(channel,machineCode, hostId, 1);
            return;
        }
        //先切到0频道
        RadioChannelCommand channelCommand = new RadioChannelCommand(0);
        LogUtils.normal(channelCommand.getCommand().length+"---"+StringUtility.bytesToHexString(channelCommand.getCommand())+"---LED切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(channelCommand));

        //连接LED屏
        byte[] command = {(byte) 0xaa, 0x00, (byte) 0xa1, 0x00, 0x00, (byte) 0xa1, 0x00, 0x01, 4, 0x0d};
        command[1] = (byte) (machineCodesForLed.get(machineCode) & 0xff);
        command[6] = (byte) (hostId & 0xff);
        LogUtils.normal(command.length+"---"+StringUtility.bytesToHexString(command)+"---LED连接指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, command));

        //调到与LED同频进行
//        RadioChannelCommand channelCommand1 = new RadioChannelCommand(hostId + SerialConfigs.sProChannels.get(machineCode) - 1);
        RadioChannelCommand channelCommand1 = new RadioChannelCommand(channel);
        LogUtils.normal(channelCommand1.getCommand().length+"---"+StringUtility.bytesToHexString(channelCommand1.getCommand())+"---LED同频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(channelCommand1));
    }

//        /**
//         * 连接屏幕
//         *
//         * @param machineCode 测试项目机器码{@link ItemDefault}.CODE_XXX
//         * @param hostId      主机号
//         */
//        public void link(int machineCode, int hostId) {
//            link(machineCode, hostId, 1);
//        }

    /**
     * 连接屏幕
     *
     * @param machineCode 测试项目机器码{@link ItemDefault}.CODE_XXX
     * @param hostId      主机号
     */
    public void link(int channel, int machineCode, int hostId, int ledId) {
        if (machineCodesForLed.get(machineCode) == null) {
            return;
        }
        //先切到0频道
        RadioChannelCommand channelCommand = new RadioChannelCommand(0);
        LogUtils.normal(channelCommand.getCommand().length+"---"+StringUtility.bytesToHexString(channelCommand.getCommand())+"---LED切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(channelCommand));

        //连接LED屏
        byte[] command = {(byte) 0xaa, 0x00, (byte) 0xa1, 0x00, 0x02, (byte) 0xa1, 0x00, 0x00, 0x00, 0x00, 0x0d};
        command[1] = (byte) (machineCodesForLed.get(machineCode) & 0xff);
        command[6] = (byte) (channel & 0xff);
        command[7] = (byte) (ledId & 0xff);
        command[8] = (byte) (hostId & 0xff);
        for (int i = 0; i < 9; i++) {
            command[9] += command[i] & 0xff;
        }

        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, command));
        LogUtils.normal(command.length+"---"+StringUtility.bytesToHexString(command)+"---LED连接指令");
        //调到与LED同频进行
        RadioChannelCommand channelCommand1 = new RadioChannelCommand(channel);
        LogUtils.normal(channelCommand1.getCommand().length+"---"+StringUtility.bytesToHexString(channelCommand1.getCommand())+"---LED同频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(channelCommand1));
    }


    /**
     * LED显示屏自检
     *
     * @param machineCode 测试项目机器码
     * @param hostId      主机号
     */
    public void test(int machineCode, int hostId) {
        if (machineCodesForLed.get(machineCode) == null) {
            return;
        }
        if (versions == LED_VERSION_4_1) {
            test(machineCode, hostId, 1);
            return;
        }
        byte[] cmd = {(byte) 0xAA, (byte) (machineCodesForLed.get(machineCode) & 0xff), (byte) 0xa1, (byte) (hostId & 0xff), 0x01, (byte) 0xA6, 0x0D};
        LogUtils.normal(cmd.length+"---"+StringUtility.bytesToHexString(cmd)+"---LED自检指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    public void test(int machineCode, int hostId, int ledId) {
        if (machineCodesForLed.get(machineCode) == null) {
            return;
        }
        byte[] cmd = {(byte) 0xAA, (byte) (machineCodesForLed.get(machineCode) & 0xff), (byte) 0xa1, (byte) (hostId & 0xff), 0x01, (byte) 0xA6, 0x0D};
        cmd[4] = (byte) (ledId & 0xff);
        LogUtils.normal(cmd.length+"---"+StringUtility.bytesToHexString(cmd)+"---LED自检指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 清空LED显示屏
     *
     * @param machineCode 测试项目机器码
     * @param hostId      主机号
     */
    public void clearScreen(int machineCode, int hostId) {
//        if (machineCodesForLed.get(machineCode) == null) {
//            return;
//        }
//        byte[] cmd = {(byte) 0xAA, (byte) (machineCodesForLed.get(machineCode) & 0xff), (byte) 0xa1, (byte) (hostId & 0xff), 0x01, (byte) 0xa5, 0x01, (byte)
//                0x00, 0x00, 0x00};
//        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        if (versions == LED_VERSION_4_1) {
            clearScreen(machineCode, hostId, 1);
            return;
        }
        clearScreen(machineCode, hostId, 1);
    }

    /**
     * 清空LED显示屏
     *
     * @param machineCode 测试项目机器码
     * @param hostId      主机号
     */
    public void clearScreen(int machineCode, int hostId, int ledId) {
        if (machineCodesForLed.get(machineCode) == null) {
            return;
        }
        byte[] cmd = {(byte) 0xAA, (byte) (machineCodesForLed.get(machineCode) & 0xff), (byte) 0xa1, (byte) (hostId & 0xff), 0x01, (byte) 0xa5, 0x01, (byte)
                0x00, 0x00, 0x00};
        cmd[4] = (byte) (ledId & 0xff);
        LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---LED频清空LED显示屏指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }


    public void showString(int hostId, String str, int y, boolean clearScreen, boolean update, int align) {
        showString(MachineCode.machineCode, hostId, 1, str, y, clearScreen, update, align);
    }

    public void showString(int hostId, String str, int x, int y, boolean clearScreen, boolean update) {

        showSubsetString(MachineCode.machineCode, hostId, 1, str, x, y, clearScreen, update);
    }

    public void showString(int machineCode, int hostId, String str, int x, int y, boolean clearScreen, boolean update) {
        showSubsetString(machineCode, hostId, 1, str, x, y, clearScreen, update);
    }

    public void showString(int hostId, byte[] data, int x, int y, boolean clearScreen, boolean update) {
        showString(MachineCode.machineCode, hostId, 1, data, x, y, clearScreen, update);
    }

    public void showSubsetString(int hostId, int ledId, String str, int y, boolean clearScreen, boolean update, int align) {
        showString(MachineCode.machineCode, hostId, ledId, str, y, clearScreen, update, align);
    }

    public void showSubsetString(int hostId, int ledId, String str, int x, int y, boolean clearScreen, boolean update) {
        try {
            byte[] data = str.getBytes("GB2312");
            showString(MachineCode.machineCode, hostId, ledId, data, x, y, clearScreen, update);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void showSubsetString(int machineCode, int hostId, int ledId, String str, int x, int y, boolean clearScreen, boolean update) {
        try {
            byte[] data = str.getBytes("GB2312");
            showString(machineCode, hostId, ledId, data, x, y, clearScreen, update);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void showSubsetString(int hostId, int ledId, byte[] data, int x, int y, boolean clearScreen, boolean update) {
        showString(MachineCode.machineCode, hostId, ledId, data, x, y, clearScreen, update);
    }


    /**
     * @param align 对齐方式 默认为{@link #LEFT}   {@link #MIDDLE}   {@link #RIGHT}
     */
    public void showString(int machineCode, int hostId, int ledId, String str, int y, boolean clearScreen, boolean update, int align) {
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

        try {
            byte[] data = str.getBytes("GB2312");
            showString(machineCode, hostId, ledId, data, x, y, clearScreen, update);
//            byte[] data = new byte[16];
//            System.arraycopy(stringData, 0, data, x, stringData.length);
//
//            showString(machineCode, hostId, ledId, data, 0, y, clearScreen, update);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    /**
     * 显示字符串
     *
     * @param machineCode 测试项目机器码
     * @param hostId      主机号
     * @param data        需要显示的字符串
     * @param x           字符串显示X轴位置
     * @param y           字符串显示Y轴位置
     * @param clearScreen 是否清空LED显示屏
     * @param update      是否立即更新显示屏信息
     */
    public void showString(int machineCode, int hostId, int ledId, byte[] data, int x, int y, boolean clearScreen, boolean update) {
        if (machineCodesForLed.get(machineCode) == null) {
            return;
        }
//		byte[] data = null;
//		try{
//			data = str.getBytes("GB2312");
//		}catch(UnsupportedEncodingException e){
//			e.printStackTrace();
//		}
        //if(data.length > 64){
        //	Logger.e("led文字长度 > 64");
        //	return;
        //}
        byte[] cmd = new byte[data.length + 13];
        cmd[0] = (byte) 0xaa;
        cmd[1] = (byte) (machineCodesForLed.get(machineCode) & 0xff);
        cmd[2] = (byte) 0xa1;
        cmd[3] = (byte) (hostId & 0xff);
        cmd[4] = (byte) (ledId & 0xff);
        cmd[5] = (byte) 0xa2;
        cmd[6] = (byte) (clearScreen ? 0x01 : 0x000);
        cmd[7] = (byte) (update ? 0x01 : 0x00);
        cmd[8] = (byte) data.length;
        cmd[9] = (byte) (x & 0xff);
        cmd[10] = (byte) (y & 0xff);
        System.arraycopy(data, 0, cmd, 11, data.length);
        cmd[11 + data.length] = 0x00;
        cmd[12 + data.length] = 0x00;
        LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---LED频显示字符指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd)/*,CMD_SEND_INTERVAL*/);

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
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加亮度
     *
     * @param machineCode 测试项目机器码
     * @param hostId      主机号
     */
    public void increaseLightness(int machineCode, int hostId) {
//        if (machineCodesForLed.get(machineCode) == null) {
//            return;
//        }
//        byte[] cmd = {(byte) 0xAA, (byte) (machineCodesForLed.get(machineCode) & 0xff), (byte) 0xa1, (byte) (hostId & 0xff), 0x01, (byte) 0xa4, (byte) 0xbb,
//                (byte) 0x0d};
//        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        increaseLightness(machineCode, hostId, 1);
    }

    /**
     * 增加亮度
     *
     * @param machineCode 测试项目机器码
     * @param hostId      主机号
     */
    public void increaseLightness(int machineCode, int hostId, int ledId) {
        if (machineCodesForLed.get(machineCode) == null) {
            return;
        }
        byte[] cmd = {(byte) 0xAA, (byte) (machineCodesForLed.get(machineCode) & 0xff), (byte) 0xa1, (byte) (hostId & 0xff), 0x01, (byte) 0xa4, (byte) 0xbb,
                (byte) 0x0d};
        cmd[4] = (byte) (ledId & 0xff);
        LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---LED频增加亮度指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 减少亮度
     *
     * @param machineCode 测试项目
     * @param hostId      主机号
     */
    public void decreaseLightness(int machineCode, int hostId) {
//        if (machineCodesForLed.get(machineCode) == null) {
//            return;
//        }
//        byte[] cmd = {(byte) 0xAA, (byte) (machineCodesForLed.get(machineCode) & 0xff), (byte) 0xa1, (byte) (hostId & 0xff), 0x01, (byte) 0xa4, (byte) 0xaa,
//                (byte) 0x0d};
//        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        decreaseLightness(machineCode, hostId, 1);
    }

    /**
     * 减少亮度
     *
     * @param machineCode 测试项目
     * @param hostId      主机号
     */
    public void decreaseLightness(int machineCode, int hostId, int ledId) {
        if (machineCodesForLed.get(machineCode) == null) {
            return;
        }
        byte[] cmd = {(byte) 0xAA, (byte) (machineCodesForLed.get(machineCode) & 0xff), (byte) 0xa1, (byte) (hostId & 0xff), 0x01, (byte) 0xa4, (byte) 0xaa,
                (byte) 0x0d};
        cmd[4] = (byte) (ledId & 0xff);
        LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---LED频减少亮度指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 亮度最暗
     *
     * @param machineCode 测试项目
     * @param hostId      主机号
     */
    public void darkest(int machineCode, int hostId) {
        if (machineCodesForLed.get(machineCode) == null) {
            return;
        }
        byte[] cmd = {(byte) 0xAA, (byte) (machineCodesForLed.get(machineCode) & 0xff), (byte) 0xa1, (byte) (hostId & 0xff), 0x01, (byte) 0xa4, 0x00, (byte)
                0x0d};
        LogUtils.normal(cmd.length+"---"+ StringUtility.bytesToHexString(cmd)+"---LED频亮度最暗指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 显示当前项目信息
     *
     * @param hostId 主机号
     */
    public void resetLEDScreen(int hostId, String machineName) {
        //第一行项目加主机号
        //第二行“请检录”
        //第三行空
        //第四行“菲普莱体育”
        String title;
        title = machineName + " " + hostId;
        showString(hostId, title, getX(title), 0, true, false);
        showString(hostId, "请检录", 5, 1, false, false);
        showString(hostId, "菲普莱体育", 3, 3, false, true);
    }

    /**
     * 显示当前项目信息
     *
     * @param hostId 主机号
     */
    public void resetLEDScreen(int hostId, int ledId, String machineName) {
        //第一行项目加主机号
        //第二行“请检录”
        //第三行空
        //第四行“菲普莱体育”
        String title;
        title = machineName + " " + hostId;
        showSubsetString(hostId, ledId, title, getX(title), 0, true, false);
        showSubsetString(hostId, ledId, "请检录", 5, 1, false, false);
        showSubsetString(hostId, ledId, "菲普莱体育", 3, 3, false, true);
    }


    public int getX(String showMsg) {
        int x;
        try {
            if (isInt(showMsg)) {
                x = 16 / showMsg.getBytes("GBK").length;
            } else {
                x = 32 / showMsg.getBytes("GBK").length;
            }
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            x = 0;
        }

        return x;
    }


    public static boolean isInt(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }
}
