package com.feipulai.device.serial;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.feipulai.device.printer.PrinterReadRunnable;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.runnable.QRReadRunnable;
import com.feipulai.device.serial.runnable.RS232ReadRunnable;
import com.feipulai.device.serial.runnable.RadioReadRunnable;
import com.feipulai.device.serial.runnable.SerialReadRunnable;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by pengjf on 2018/10/29.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class SerialPorter {

    private OnDataArrivedListener mListener;

    private InputStream mFileInputStream;
    private OutputStream mFileOutputStream;

    private SerialReadRunnable mSerialReadRunnable;
    private SerialPort serialPort;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private HandlerThread mSendingHandlerThread;
    private Handler mSendingHandler;

    public SerialPorter(SerialParams config, OnDataArrivedListener listener) {
        try {
            serialPort = new SerialPort(new File(config.getPath()), config.getBaudRate(), 0);
            mFileInputStream = serialPort.getInputStream();
            mFileOutputStream = serialPort.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        mListener = listener;
        startReading(config);
        startSendThread();
    }

    /**
     * 开启发送消息的线程
     */
    private void startSendThread() {
        // 开启发送消息的线程
        mSendingHandlerThread = new HandlerThread("mSendingHandlerThread");
        mSendingHandlerThread.start();
        // Handler
        mSendingHandler = new Handler(mSendingHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                byte[] sendBytes = (byte[]) msg.obj;

                if (null != mFileOutputStream && null != sendBytes && 0 < sendBytes.length) {
                    try {
                        mFileOutputStream.write(sendBytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private void startReading(SerialParams config) {
        Log.e("TAG", config + "---------");
        switch (config) {

            case QR_CODE:
                mSerialReadRunnable = new QRReadRunnable(mFileInputStream, mListener);
                break;

            case RADIO:
                mSerialReadRunnable = new RadioReadRunnable(mFileInputStream, mListener);
                break;

            case RS232:
                mSerialReadRunnable = new RS232ReadRunnable(mFileInputStream, mListener);
                break;

            case PRINTER:
                mSerialReadRunnable = new PrinterReadRunnable(mFileInputStream, mListener);
                break;
        }
        mExecutor.execute(mSerialReadRunnable);
    }

    // 添加数据通信监听
    public void setListener(OnDataArrivedListener listener) {
        mListener = listener;
    }

    public void sendCommand(ConvertCommand cmd) {
        byte[] toSend = cmd.getCmdBytes();
        LogUtils.serialSend("发送前：" + StringUtility.bytesToHexString(toSend));
////        //TODO 添加写入文件给测试用
//        DistanceParser.writeFileByString(DistanceParser.PATH_BASE, "PARSER_DEVICE_RETURN.txt", "发送==》" + DistanceParser.bytes2HexString(toSend));
        sendCommand(toSend);
        LogUtils.serialSend("发送后：" + StringUtility.bytesToHexString(toSend));

    }

    public void sendCommand(byte[] toSend) {
//        if (null != mFileInputStream && null != mFileOutputStream) {
//            if (null != mSendingHandler) {
//                Message message = Message.obtain();
//                message.obj = toSend;
//                mSendingHandler.sendMessage(message);
//            }
//        }
        try {
            if (mFileOutputStream != null && toSend != null) {
                mFileOutputStream.write(toSend);
                //Log.i("tid:" + Thread.currentThread().getId() + "-----sending", StringUtility.bytesToHexString(toSend));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (mSerialReadRunnable != null) {
            mSerialReadRunnable.stop();
        }
        if (mExecutor != null && !mExecutor.isShutdown()) {
            mExecutor.shutdownNow();
        }

        if (null != mFileInputStream) {
            try {
                mFileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mFileInputStream = null;
        }

        if (null != mFileOutputStream) {
            try {
                mFileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mFileOutputStream = null;
        }
        if (serialPort != null) {
            serialPort.close();
        }
    }

    public interface OnDataArrivedListener {
        void onDataArrived(Message msg);
    }

}
