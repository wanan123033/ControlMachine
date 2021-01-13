package com.feipulai.device.qrcode;

import android.content.Context;
import android.os.Message;


import com.feipulai.device.serial.Gpio;
import com.feipulai.device.serial.IOPower;
import com.feipulai.device.serial.SerialParams;
import com.feipulai.device.serial.SerialPorter;

import java.util.concurrent.TimeUnit;

/**
 * Created by pengjf on 2018/11/5.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class QRManager {

    private SerialPorter mSerialManager;
    private QRCodeListener mQRCodeListener;
    private static QRManager sQRDevManager;
    private volatile int qrLength;
    private Gpio gpio;

    public void setQRCodeListener(QRCodeListener qRCodeListener) {
        mQRCodeListener = qRCodeListener;
    }

    public void setQrLength(int length) {
        qrLength = length;
    }

    public void setGpio(Context context) {
        gpio = new Gpio(context);
        gpio.gpioDirection(0, 1, 1);
    }

    private QRManager() {
        if (SerialParams.QR_CODE.getVersions()==1){
            IOPower.getInstance().setBarcodePwr(1);
        }
        mSerialManager = new SerialPorter(SerialParams.QR_CODE, new SerialPorter.OnDataArrivedListener() {
            @Override
            public void onDataArrived(Message msg) {
                if (mQRCodeListener != null) {
                    String qrCode = (String) msg.obj;
                    if (qrLength == 0 || qrCode.length() == qrLength) {
                        mQRCodeListener.onQrArrived(qrCode);
                    } else {
                        mQRCodeListener.onWrongLength(qrCode.length(), qrLength);
                    }

                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                        startScan();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public synchronized static QRManager getInstance() {
        if (sQRDevManager == null) {
            sQRDevManager = new QRManager();
        }
        return sQRDevManager;
    }

    public void startScan() {
        for (int i = 0; i < 3; i++) {
            if (SerialParams.QR_CODE.getVersions() == 2 && gpio != null) {
                gpio.gpioWrite(0, 0);
            }else{
                IOPower.getInstance().setBarcodetrig(0);
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void stopScan() {

        if (SerialParams.QR_CODE.getVersions() == 2 && gpio != null) {
            gpio.gpioWrite(0, 1);
        } else {
            IOPower.getInstance().setBarcodetrig(1);
        }
    }

    public void close() {
        mSerialManager.close();
        if (SerialParams.QR_CODE.getVersions()==1){
            IOPower.getInstance().setBarcodePwr(0);
        }

        sQRDevManager = null;
    }

    public interface QRCodeListener {
        void onQrArrived(String qrCode);

        void onWrongLength(int length, int expectLength);
    }

}
