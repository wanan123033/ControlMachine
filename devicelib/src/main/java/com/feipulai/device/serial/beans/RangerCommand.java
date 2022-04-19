package com.feipulai.device.serial.beans;

public class RangerCommand {
    /**
     * 模式变换  测距仪修改成测距模式
     */
    public static final byte[] MODE_UPDATE = new byte[]{0x5A, 0x33, 0x34, 0x30, 0x39, 0x33, 0x03, 0x0d, 0x0a};
    /**
     * 测距指令
     */
    public static final byte[] RANGER_COMMAND = new byte[]{0x43, 0x30, 0x36, 0x37, 0x03, 0x0d, 0x0a};
}
