package com.feipulai.device.idcard;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;

import com.feipulai.device.idcard.ZKUSBManager.ZKUSBManager;
import com.feipulai.device.idcard.ZKUSBManager.ZKUSBManagerListener;
import com.feipulai.device.serial.IOPower;
import com.feipulai.device.serial.SerialParams;
import com.orhanobut.logger.Logger;
import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.core.utils.LogHelper;
import com.zkteco.android.biometric.module.idcard.IDCardReader;
import com.zkteco.android.biometric.module.idcard.IDCardReaderFactory;
import com.zkteco.android.biometric.module.idcard.exception.IDCardReaderException;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 身份证模块
 * Created by zzs on 2018/11/6
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class IDCardDevice {
    private ZKUSBManager zkusbManager = null;
    private static final String TAG = "IDCardDevice";
    private IDCardReader idCardReader;
    private boolean isOpen;
    private volatile boolean isShutDown;
    private volatile OnIDReadListener mOnIDReadListener;
    private ExecutorService mExecutor;
    private Context context;
    private ZKUSBManagerListener zkusbManagerListener = new ZKUSBManagerListener() {
        @Override
        public void onCheckPermission(int result) {
            Logger.d("启动权限");
            try {
                startIDCardReader(context);
                startReadRunnable();
            } catch (IDCardReaderException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUSBArrived(UsbDevice device) {
            Logger.d("发现阅读器接入");
        }

        @Override
        public void onUSBRemoved(UsbDevice device) {
            Logger.d("阅读器USB被拔出");
        }
    };

    public void setOnIDReadListener(OnIDReadListener onReadListener) {
        mOnIDReadListener = onReadListener;
    }

    private void startIDCardReader(Context context) throws IDCardReaderException {
        LogHelper.setLevel(Log.INFO);
        Map<String, Object> params = new HashMap<>();
        if (SerialParams.ID_CARD.getType() == 0) {//串口
            params.put(ParameterHelper.PARAM_SERIAL_SERIALNAME, SerialParams.ID_CARD.getPath());
            params.put(ParameterHelper.PARAM_SERIAL_BAUDRATE, SerialParams.ID_CARD.getBaudRate());
            idCardReader = IDCardReaderFactory.createIDCardReader(context, TransportType.SERIALPORT, params);
        } else {//usb
            params.put(ParameterHelper.PARAM_KEY_PID, SerialParams.ID_CARD.getPid());
            params.put(ParameterHelper.PARAM_KEY_VID, SerialParams.ID_CARD.getVid());
            idCardReader = IDCardReaderFactory.createIDCardReader(context, TransportType.USB, params);
            idCardReader.setLibusbFlag(true);
        }
        idCardReader.open(0);
        Logger.d("打开设备成功，SAMID:" + idCardReader.getSAMID(0));
    }

    /**
     * 设备启动,开始识别身份证
     * 在{@link Activity//onResume()}中调用
     */
    public void open(Context context) {
        this.context = context;
        if (isOpen) {
            return;
        }
        if (SerialParams.ID_CARD.getVersions() == 1) {
            for (int i = 0; i < 3; i++) {
                IOPower.getInstance().setIdentityPwr(1);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


        try {
            if (SerialParams.ID_CARD.getType() == 0) {//串口
                startIDCardReader(context);
                startReadRunnable();
            } else {//usb
                zkusbManager = new ZKUSBManager(context, zkusbManagerListener);
                zkusbManager.registerUSBPermissionReceiver();
                zkusbManager.initUSBPermission(SerialParams.ID_CARD.getVid(), SerialParams.ID_CARD.getPid());
                return;
            }


        } catch (IDCardReaderException e) {
            e.printStackTrace();
            if (SerialParams.ID_CARD.getVersions() == 1) {
                IOPower.getInstance().setIdentityPwr(0);
            }

        }
    }

    private void startReadRunnable() {
        mExecutor = Executors.newSingleThreadExecutor();
        isShutDown = false;
        mExecutor.execute(new IDCardReadRunnable());
        Logger.d("连接设备成功");
        isOpen = true;
    }

    //try read id card information
    private class IDCardReadRunnable implements Runnable {

        private static final long READ_INTERVAL = 1000L;

        @Override
        public void run() {
            try {
                while (!isShutDown) {
                    tryReadID();
                    Thread.sleep(READ_INTERVAL);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void tryReadID() throws InterruptedException {
        try {
            Authenticate();
            Thread.sleep(50);
            int retType = idCardReader.readCardEx(0, 1);
            if (retType == 1) {
                IDCardInfo idCardInfo = idCardReader.getLastIDCardInfo();
                Log.i("james", "读取到身份证:" + idCardInfo.getId());
                if (idCardInfo != null && mOnIDReadListener != null) {
                    mOnIDReadListener.onIdCardRead(idCardInfo);
                }
            }
        } catch (IDCardReaderException e) {
            // 发生了也不会怎样
            Logger.i("身份证查找错误：" + e.getErrorCode() + "\n错误信息：" + e.getMessage() + "\n 内部错误码=" + e.getInternalErrorCode());
            //e.printStackTrace();
        }
    }

    //放卡一直读取不判断该返回值继续读卡
    private void Authenticate() throws IDCardReaderException {
        idCardReader.findCard(0);
        idCardReader.selectCard(0);
    }

    /**
     * 关闭设备
     */
    public void close() {
        isShutDown = true;
        if (mExecutor != null && !mExecutor.isShutdown()) {
            mExecutor.shutdownNow();
        }
        if (isOpen) {
            isOpen = false;
        }
        if (SerialParams.ID_CARD.getType() == 1) {
            if (idCardReader != null) {
                zkusbManager.unRegisterUSBPermissionReceiver();
                try {
                    idCardReader.close(0);
                    IDCardReaderFactory.destroy(idCardReader);
                } catch (IDCardReaderException e) {
                    e.printStackTrace();
                }
            }

        } else {
            try {
                if (null!=idCardReader){
                    idCardReader.close(0);
                }
                IDCardReaderFactory.destroy(idCardReader);
            } catch (IDCardReaderException e) {
                e.printStackTrace();
            }
            // 直接断电,省得麻烦
//            IOPower.getInstance().setIdentityPwr(0);
        }
    }

    public interface OnIDReadListener {
        /**
         * 读到身份证信息时调用
         *
         * @param idCardInfo 身份证信息
         */
        public void onIdCardRead(IDCardInfo idCardInfo);
    }

}
