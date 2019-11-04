package com.feipulai.device.manager;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.command.ConvertCommand;

public class VolleyBallRadioManager {
    private static VolleyBallRadioManager radioManager;
    private VolleyBallRadioManager(){
    }
    public synchronized static VolleyBallRadioManager getInstance(){
        if (radioManager == null){
            radioManager = new VolleyBallRadioManager();
        }
        return radioManager;
    }

    public void stop(int hostId,int deviceId){
        byte[] cmd = {(byte) 0xAA,0x0F,0x0A,0x03,0x01,0x00,0x00, (byte) 0xC5,0x00,0x00,0x00,0x00,0x00,0x00,0x0D};
        cmd[5] = (byte) hostId;
        cmd[6] = (byte)  deviceId;
        cmd[13] = (byte) sum(cmd,13);
//        Log.e("TAG---", StringUtility.bytesToHexString(cmd));
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,cmd));
    }

    public void deviceFree(int hostId,int deviceId){
        byte[] cmd = {(byte) 0xAA,0x0e,0x0A,0x03,0x01,0x00,0x00, (byte) 0xC6,0x00,0x00,0x00,0x00,0x00,0x0d};
        cmd[5] = (byte) hostId;
        cmd[6] = (byte)  deviceId;
        cmd[12] = (byte) sum(cmd,12);

        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    public void start(int hostId,int deviceId){
        byte[] cmd = {(byte) 0xAA,0x0F,0x0A,0x03,0x01,0x00,0x00, (byte) 0xC5,0x00,0x00,0x00,0x00,0x01,0x00,0x0D};
        cmd[5] = (byte) hostId;
        cmd[6] = (byte)  deviceId;
        cmd[13] = (byte) sum(cmd,13);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,cmd));
    }

    public void getState(int hostId,int deviceId){
        byte[] cmd = {(byte) 0xAA,0x0e,0x0a,0x03,0x01,0x00,0x00, (byte) 0xC3,0x00,0x00,0x00,0x00,0x00,0x0d};
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) deviceId;
        cmd[12] = (byte) sum(cmd,12);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }
    private int sum(byte[] cmd, int index) {
        int sum = 0;
        for (int i = 2; i < index; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }

}
