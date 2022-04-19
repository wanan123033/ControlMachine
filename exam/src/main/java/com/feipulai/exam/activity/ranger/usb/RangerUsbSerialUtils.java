package com.feipulai.exam.activity.ranger.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.exam.BuildConfig;
import com.feipulai.exam.activity.ranger.driver.UsbSerialDriver;
import com.feipulai.exam.activity.ranger.driver.UsbSerialPort;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class RangerUsbSerialUtils implements SerialInputOutputManager.Listener {
    private Context context;
    private static final int baudRate = 19200;
    private boolean connected = false;
    private static final int WRITE_WAIT_MILLIS = 2000;
    private OnBufferListenner onBufferListenner;

    private String string = "";

    @Override
    public void onNewData(byte[] data) {
        try {
            String hexString = StringUtility.bytesToHexString(data);
            Log.e("TAG",hexString);
            if (data[0] == 0x3F){
                string = "";
            }
            string = string+new String(data, StandardCharsets.US_ASCII);
            byte[] buffer = string.getBytes(StandardCharsets.US_ASCII);
            if (buffer.length == 51 && buffer[49] == 0x0D && buffer[50] == 0x0A){
                Log.e("TAG","结果已出:"+StringUtility.bytesToHexString(buffer));
                if (onBufferListenner != null){
                    onBufferListenner.onBuffer(buffer);
                }
            }else {
                Log.e("TAG","结果未出");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onRunError(Exception e) {
        e.printStackTrace();
    }

    public boolean isConnected() {
        return connected;
    }

    public void sendCommand(byte[] command) {
        try {
            usbSerialPort.write(command, WRITE_WAIT_MILLIS);
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtils.showShort("请先连接设备");
        }
    }

    public void setOnBufferListenner(OnBufferListenner onBufferListenner) {
        this.onBufferListenner = onBufferListenner;
    }

    private enum UsbPermission { Unknown, Requested, Granted, Denied };
    private static RangerUsbSerialUtils rangerUsbSerialUtils;
    private UsbDevice device;
    private UsbSerialDriver driver;
    private ListItem item;
    private Handler mainHandler;

    private UsbSerialPort usbSerialPort;
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";
    
    private BroadcastReceiver usbPermissionRecevice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(INTENT_ACTION_GRANT_USB)) {
                usbPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        ? UsbPermission.Granted : UsbPermission.Denied;
                RangerUsbSerialUtils.getInstance(context).connectDevice(usbManager,device,driver,item);
            }
        }
    };
    private UsbManager usbManager;
    private UsbPermission usbPermission;
    private UsbDeviceConnection usbConnection;
    private boolean withIoManager = true;
    private SerialInputOutputManager usbIoManager;

    private RangerUsbSerialUtils(Context context){
        this.context = context;
        context.registerReceiver(usbPermissionRecevice,new IntentFilter(INTENT_ACTION_GRANT_USB));
        mainHandler = new Handler(Looper.getMainLooper());
    }
    public static synchronized RangerUsbSerialUtils getInstance(Context context){
        if (rangerUsbSerialUtils == null){
            rangerUsbSerialUtils = new RangerUsbSerialUtils(context);
        }
        rangerUsbSerialUtils.context = context;
        return rangerUsbSerialUtils;
    }

    /**
     * 连接设备
     * @param usbManager
     * @param device  设备对象
     * @param driver  驱动对象
     * @param item   设备信息
     */
    public void connectDevice(UsbManager usbManager, UsbDevice device, UsbSerialDriver driver, ListItem item) {
        this.device = device;
        this.driver = driver;
        this.item = item;
        this.usbManager = usbManager;

        usbSerialPort = driver.getPorts().get(item.port);

        usbConnection = usbManager.openDevice(driver.getDevice());
        if(usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(driver.getDevice())) {
            usbPermission = UsbPermission.Requested;
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if(usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice()))
                ToastUtils.showShort("connection failed: permission denied");
            else
                ToastUtils.showShort("connection failed: open failed");
            return;
        }
        try {
            usbSerialPort.open(usbConnection);
            usbSerialPort.setParameters(baudRate, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            if(withIoManager) {
                usbIoManager = new SerialInputOutputManager(usbSerialPort, this);
                Executors.newSingleThreadExecutor().submit(usbIoManager);
            }
            ToastUtils.showShort("connected");
            connected = true;
        } catch (Exception e) {
            ToastUtils.showShort("connection failed: " + e.getMessage());
            disconnect();
        }
    }
    public void disconnect() {
        connected = false;
        if(usbIoManager != null)
            usbIoManager.stop();
        usbIoManager = null;
        try {
            usbSerialPort.close();
        } catch (IOException ignored) {}
        usbSerialPort = null;
    }
    public interface OnBufferListenner{
        void onBuffer(byte[] buffer);
    }
}
