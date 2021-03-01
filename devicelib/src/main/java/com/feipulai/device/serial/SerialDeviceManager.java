package com.feipulai.device.serial;

import android.os.Message;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.command.ConvertCommand;
import com.orhanobut.logger.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by James on 2018/11/9 0009.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class SerialDeviceManager {

    private SerialPorter mSerialPorter;
    private volatile RS232ResiltListener mDeviceListener;
    private static volatile SerialDeviceManager sDeviceManager;
    //private int mCurrentBaudrate;
    public static final int RADIO_INTERVAL = 100;
    private long mlastSendTime;

    public void setRS232ResiltListener(RS232ResiltListener listener) {
        // Log.i("james", "listener===>" + listener == null ? "00" : "11");
        mDeviceListener = listener;
        if (SerialParams.RS232.getVersions() == 2) {
            RadioManager.getInstance().setOnRadioArrived(new RadioManager.OnRadioArrivedListener() {
                @Override
                public void onRadioArrived(Message msg) {
                    if (mDeviceListener != null) {
                        mDeviceListener.onRS232Result(msg);
                    }
                }
            });
        }
    }

    private static final Map<Integer, Integer> machinBaudrate = new HashMap<>();

    static {
        machinBaudrate.put(ItemDefault.CODE_HW, 4800);
        machinBaudrate.put(ItemDefault.CODE_FHL, 9600);
        machinBaudrate.put(ItemDefault.CODE_ZWTQQ, 9600);
        machinBaudrate.put(ItemDefault.CODE_HWSXQ, 9600);
        machinBaudrate.put(ItemDefault.CODE_LDTY, 9600);
        machinBaudrate.put(ItemDefault.CODE_ZFP, 9600);
        machinBaudrate.put(ItemDefault.CODE_PQ, 9600);
        machinBaudrate.put(ItemDefault.CODE_MG, 9600);
        machinBaudrate.put(ItemDefault.CODE_FWC, 9600);
        machinBaudrate.put(ItemDefault.CODE_GPS, 9600);
        machinBaudrate.put(ItemDefault.CODE_LQYQ, 9600);
        machinBaudrate.put(ItemDefault.CODE_SHOOT, 9600);
    }

    private SerialDeviceManager() {
        if (SerialParams.RS232.getVersions() == 1) {
            SerialParams.RS232.setBaudRate(machinBaudrate.get(MachineCode.machineCode));
            mSerialPorter = new SerialPorter(SerialParams.RS232, new SerialPorter.OnDataArrivedListener() {
                @Override
                public void onDataArrived(Message msg) {
                    if (mDeviceListener != null) {
                        mDeviceListener.onRS232Result(msg);
                    }
                }
            });
        }

        if (SerialParams.RS232.getVersions() == 2) {
            byte[] cmdBaudRate = new byte[]{(byte) 0XA5, 0X5A, (byte) 0XB2, 4, 0X00, 8, 0, 1, (byte) 0XFF, (byte) 0XAA, 0X55};
            switch (machinBaudrate.get(MachineCode.machineCode)) {
                case 4800:
                    cmdBaudRate[4] = 0x04;
                    break;
                case 9600:
                    cmdBaudRate[4] = 0x09;
                    break;
                case 115200:
                    cmdBaudRate[4] = 115 & 0xff;
                    break;
            }
            sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.CONVERTER, cmdBaudRate));
        }
    }

    public synchronized static SerialDeviceManager getInstance() {
        if (sDeviceManager == null) {
            sDeviceManager = new SerialDeviceManager();
        }
        return sDeviceManager;
    }

    public synchronized void sendCommand(ConvertCommand convertCommand) {
        ensureInterval();
        if (SerialParams.RS232.getVersions() == 1) {
            mSerialPorter.sendCommand(convertCommand);
        }else{
            RadioManager.getInstance().sendCommand(convertCommand);
        }


    }

    public synchronized void sendCommandForLed(ConvertCommand convertCommand) {
        ensureInterval();
        if (SerialParams.RS232.getVersions() == 1) {
            mSerialPorter.sendCommand(convertCommand);
        }else{
            RadioManager.getInstance().sendCommand(convertCommand);
        }
    }

    private void ensureInterval() {
        try {
            //Thread.sleep(RADIO_INTERVAL);
            long curTime = System.currentTimeMillis();
            long expectTime = mlastSendTime + RADIO_INTERVAL;
            // Log.i("for_led",expectTime - curTime + "expectTime");
            if (curTime < expectTime) {
                // Log.i("for_led",expectTime - curTime + "");
                Thread.sleep(expectTime - curTime);
            }
            mlastSendTime = System.currentTimeMillis();
        } catch (InterruptedException e) {
            // 保留打断标志
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    public void close() {
        if (mSerialPorter!=null){
            mSerialPorter.close();
            sDeviceManager = null;
        }

    }

    public interface RS232ResiltListener {
        void onRS232Result(Message msg);
    }

}
