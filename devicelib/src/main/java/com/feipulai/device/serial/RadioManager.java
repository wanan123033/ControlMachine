package com.feipulai.device.serial;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.feipulai.device.serial.command.ConvertCommand;
import com.orhanobut.logger.utils.LogUtils;

import java.io.IOException;

/**
 * Created by James on 2018/11/8 0008.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class RadioManager {

    private SerialPorter mSerialPorter;
    private volatile OnRadioArrivedListener mOnRadioArrived;
    private volatile OnRadioR232Listener mOnRadio232;
    private volatile OnKwhListener mOnKwhListener;
    private static volatile RadioManager instance;
    public static final int RADIO_INTERVAL = 120;
    private long mlastSendTime;


    private RadioManager() {
    }

    public synchronized static RadioManager getInstance() {
        if (instance == null) {
            instance = new RadioManager();
        }
        return instance;
    }

    public synchronized void init() {
        if (SerialParams.RADIO.getVersions() != 2) {
            IOPower.getInstance().setUhfcommPwr(1);
        }
        mSerialPorter = new SerialPorter(SerialParams.RADIO, new SerialPorter.OnDataArrivedListener() {
            @Override
            public void onDataArrived(Message msg) {
                if (msg.arg1 == 0XD2) {
                    if (mOnRadio232 != null) {
                        mOnRadio232.onRadio232(msg);
                    }
                } else {
                    if (mOnRadioArrived != null) {
                        mOnRadioArrived.onRadioArrived(msg);
                    }
                }


                if (mOnKwhListener != null && msg.what == SerialConfigs.CONVERTER_KWH_RESPONSE) {
                    mOnKwhListener.onKwhArrived(msg);
                }
            }
        });
    }


    // 程序退出时才调用
    public synchronized void close() {
        if (mSerialPorter == null)
            return;
        mSerialPorter.close();
        if (SerialParams.RADIO.getVersions() != 2) {
            IOPower.getInstance().setUhfcommPwr(0);
        }

        instance = null;
    }

    public void clearListener() {
        mOnRadioArrived = null;
        mOnRadio232 = null;
    }

    public void setOnRadioArrived(OnRadioArrivedListener onRadioArrived) {
        mOnRadioArrived = onRadioArrived;
    }

    public void setOnRadio232(OnRadioR232Listener onRadio232) {
        mOnRadio232 = onRadio232;
    }

    public void setOnKwhListener(OnKwhListener onKwhListener) {
        mOnKwhListener = onKwhListener;
    }

    // 这个地方必须锁住,万恶之源
    public synchronized void sendCommand(ConvertCommand convertCommand) {
//        if (null != mSendingHandler) {
//            Message message = Message.obtain();
//            message.obj = convertCommand;
//            mSendingHandler.sendMessage(message);
//        }
        ensureInterval();
        if (mSerialPorter == null)
            return;
        mSerialPorter.sendCommand(convertCommand);

    }
//    public synchronized void sendSerialCommand(ConvertCommand convertCommand) {
////        if (null != mSendingHandler) {
////            Message message = Message.obtain();
////            message.obj = convertCommand;
////            mSendingHandler.sendMessage(message);
////        }
//        ensureInterval();
//        if (mSerialPorter == null)
//            return;
//        mSerialPorter.sendCommand(convertCommand);
//
//    }
    private void ensureInterval() {
        try {
            //Thread.sleep(RADIO_INTERVAL);
            long curTime = System.currentTimeMillis();
            long expectTime = mlastSendTime + RADIO_INTERVAL;
//            LogUtils.all("ensureInterval====>");
            if (curTime < expectTime) {
                long sleepTime = expectTime - curTime;
                LogUtils.all("ensureInterval====>" + sleepTime + "");
                Thread.sleep(sleepTime > RADIO_INTERVAL ? RADIO_INTERVAL : sleepTime);
            }
            mlastSendTime = System.currentTimeMillis();
        } catch (InterruptedException e) {
            // 保留打断标志
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    public interface OnRadioArrivedListener {
        void onRadioArrived(Message msg);
    }

    public interface OnRadioR232Listener {
        void onRadio232(Message msg);
    }

    public interface OnKwhListener {
        void onKwhArrived(Message msg);
    }
}
