package com.feipulai.device.spputils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author chen
 * @version 2018/11/20/14:18
 */

public class SppUtils {

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SppState.MESSAGE_WRITE:
                    break;
                case SppState.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf);
                    if(readBuf != null && readBuf.length > 0) {
                        if(mDataReceivedListener != null)
                            mDataReceivedListener.onDataReceived(readBuf, readMessage);
                    }

                    Log.e(TAG, (Looper.getMainLooper().getThread() == Thread.currentThread())+"");

                    break;
                case SppState.MESSAGE_DEVICE_NAME:
                    mDeviceName = msg.getData().getString(SppState.DEVICE_NAME);
                    mDeviceAddress = msg.getData().getString(SppState.DEVICE_ADDRESS);
                    if(mBluetoothConnectionListener != null)
                        mBluetoothConnectionListener.onDeviceConnected(mDeviceName, mDeviceAddress);
                    isConnected = true;
                    break;
                case SppState.MESSAGE_TOAST:
                    Toast.makeText(mContext, msg.getData().getString(SppState.TOAST)
                            , Toast.LENGTH_SHORT).show();
                    break;
                case SppState.MESSAGE_STATE_CHANGE:
                    if(mBluetoothStateListener != null) mBluetoothStateListener.onServiceStateChanged(msg.arg1);
                    if(isConnected && msg.arg1 != SppState.STATE_CONNECTED) {
                        if(mBluetoothConnectionListener != null)
                            mBluetoothConnectionListener.onDeviceDisconnected();
                        if(isAutoConnectionEnabled) {
                            isAutoConnectionEnabled = false;
                            autoConnect(keyword);
                        }
                        isConnected = false;
                        mDeviceName = null;
                        mDeviceAddress = null;
                    }

                    if(!isConnecting && msg.arg1 == SppState.STATE_CONNECTING) {
                        isConnecting = true;
                    } else if(isConnecting) {
                        if(msg.arg1 != SppState.STATE_CONNECTED) {
                            if(mBluetoothConnectionListener != null)
                                mBluetoothConnectionListener.onDeviceConnectionFailed();
                        }
                        isConnecting = false;
                    }
                    break;
            }
        }
    };
    private String TAG = "SppUtils";
    //??????????????????
    private BluetoothStateListener mBluetoothStateListener = null;
    //????????????????????????
    private OnDataReceivedListener mDataReceivedListener = null;
    //????????????????????????
    private BluetoothConnectionListener mBluetoothConnectionListener = null;
    //??????????????????
    private AutoConnectionListener mAutoConnectionListener = null;
    //?????????
    private Context mContext;
    //???????????????
    private BluetoothAdapter mBluetoothAdapter = null;
    //????????????
    private SppService mChatService = null;

    //??????????????????????????????
    private String mDeviceName = null;
    private String mDeviceAddress = null;

    private boolean isAutoConnecting = false;
    private boolean isAutoConnectionEnabled = false;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private boolean isServiceRunning = false;

    private String keyword = "";

    private BluetoothConnectionListener bcl;
    private int c = 0;
    private SppReciver mReciver;
    private IntentFilter mPairiRequestFilter;
    private IntentFilter mBondStateFilter;
    private IntentFilter mScanFilter;
    private OnDeviceCallBack onDeviceCallBack;

    /**
     * ????????????
     */
    public interface OnDeviceCallBack{
        void onDeviceCallBack(BluetoothDevice device);
    }
    public void setOnDeviceCallBack(OnDeviceCallBack onDeviceCallBack) {
        this.onDeviceCallBack = onDeviceCallBack;
    }


    public SppUtils(Context context ) {
        mContext = context;
        /*????????????*/
        mReciver = new SppReciver(mContext);
        mReciver.setOnDeviceCallBack(new SppReciver.OnDeviceCallBack() {
            @Override
            public void onDeviceCallBack(BluetoothDevice device) {
                if (onDeviceCallBack!=null){
                    onDeviceCallBack.onDeviceCallBack(device);
                }
            }
        });
        //????????????

        mPairiRequestFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        mBondStateFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mScanFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mScanFilter.setPriority(Integer.MAX_VALUE);
        mPairiRequestFilter.setPriority(Integer.MAX_VALUE);
        mBondStateFilter.setPriority(Integer.MAX_VALUE);
        //????????????
        mContext.registerReceiver(mReciver, mPairiRequestFilter);
        mContext.registerReceiver(mReciver, mBondStateFilter);
        mContext.registerReceiver(mReciver, mScanFilter);
        //?????????????????????
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    public SppReciver getmReciver() {
        return mReciver;
    }

    //????????????
    public void setupService() {
        mChatService = new SppService(mContext, mHandler);
    }

    /**
     * ?????????????????????
     * @return
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }


    /**
     * ????????????????????????
     * @return
     */
    public int getServiceState() {
        if(mChatService != null)
            return mChatService.getState();
        else
            return -1;
    }

    /**
     * ??????????????????
     */
    public void startService() {
        if (mChatService != null) {
            if (mChatService.getState() == SppState.STATE_NONE) {
                isServiceRunning = true;
                //????????????
                mChatService.start();
            }
        }
    }

    public void stopService() {
        if (mChatService != null) {
            isServiceRunning = false;
            mChatService.stop();
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (mChatService != null) {
                    isServiceRunning = false;
                    mChatService.stop();
                }
            }
        }, 500);
}

    public void setDeviceTarget() {
        stopService();
        startService();
    }

    public boolean isConnected(){
        return isConnected;
    }

    /*??????????????????????????????*/
    public boolean isBluetoothAvailable() {
        try {
            if (mBluetoothAdapter == null || mBluetoothAdapter.getAddress().equals(null))
                return false;
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }
    /*??????????????????????????????*/
    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }
    /*??????????????????????????????*/
    public boolean isServiceAvailable() {
        return mChatService != null;
    }
    /*????????????????????????*/
    public boolean isAutoConnecting() {
        return isAutoConnecting;
    }
    /*??????????????????*/
    public boolean startDiscovery() {
        return mBluetoothAdapter.startDiscovery();
    }
    /*??????????????????????????????*/
    public boolean isDiscovery() {
        return mBluetoothAdapter.isDiscovering();
    }
    /*??????????????????*/
    public boolean cancelDiscovery() {
        return mBluetoothAdapter.cancelDiscovery();
    }
    /*????????????*/
    public void enable() {
        mBluetoothAdapter.enable();
    }



    //????????????????????????
    public interface BluetoothStateListener {
         void onServiceStateChanged(int state);
    }

    //????????????????????????
    public interface OnDataReceivedListener {
        public void onDataReceived(byte[] data, String message);
    }

    /*????????????????????????*/
    public interface BluetoothConnectionListener {
        //??????
        public void onDeviceConnected(String name, String address);
        //????????????
        public void onDeviceDisconnected();
        //????????????
        public void onDeviceConnectionFailed();
    }

    /*??????????????????*/
    public interface AutoConnectionListener {
        public void onAutoConnectionStarted();
        public void onNewConnection(String name, String address);
    }
    /*??????????????????*/
    public void stopAutoConnect() {
        isAutoConnectionEnabled = false;
    }
    /*????????????*/
    public void connect(String address) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mChatService.connect(device);
    }


    public void connect(Intent data) {
        String address = data.getExtras().getString(SppState.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mChatService.connect(device);
    }

    /*?????????????????????*/
    public void disconnect() {
        if(mChatService != null) {
            isServiceRunning = false;
            mChatService.stop();
            if(mChatService.getState() == SppState.STATE_NONE) {
                isServiceRunning = true;
                mChatService.start();
            }
        }
    }

    public void setBluetoothStateListener (BluetoothStateListener listener) {
        mBluetoothStateListener = listener;
    }
    public void setOnDataReceivedListener (OnDataReceivedListener listener) {
        mDataReceivedListener = listener;
    }
    public void setBluetoothConnectionListener (BluetoothConnectionListener listener) {
        mBluetoothConnectionListener = listener;
    }
    public void setAutoConnectionListener(AutoConnectionListener listener) {
        mAutoConnectionListener = listener;
    }
    /*????????????*/
    public void send(byte[] data, boolean CRLF) {
        if(mChatService.getState() == SppState.STATE_CONNECTED) {
            if(CRLF) {
                byte[] data2 = new byte[data.length + 2];
                for(int i = 0 ; i < data.length ; i++)
                    data2[i] = data[i];
                data2[data2.length - 2] = 0x0A;
                data2[data2.length - 1] = 0x0D;
                mChatService.write(data2);
            } else {
                mChatService.write(data);
            }
        }
    }
    public void send(String data, boolean CRLF) {
        if(mChatService.getState() == SppState.STATE_CONNECTED) {
            if(CRLF)
                mChatService.write(data.getBytes());
        }
    }

    public String getConnectedDeviceName() {
        return mDeviceName;
    }

    public String getConnectedDeviceAddress() {
        return mDeviceAddress;
    }

    //?????????????????????????????????
    public String[] getPairedDeviceName() {
        int c = 0;
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        String[] name_list = new String[devices.size()];
        for(BluetoothDevice device : devices) {
            name_list[c] = device.getName();
            c++;
        }
        return name_list;
    }
    //????????????????????????????????????
    public String[] getPairedDeviceAddress() {
        int c = 0;
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        String[] address_list = new String[devices.size()];
        for(BluetoothDevice device : devices) {
            address_list[c] = device.getAddress();
            c++;
        }
        return address_list;
    }
    public void autoConnect(String keywordName) {
        if(!isAutoConnectionEnabled) {
            keyword = keywordName;
            isAutoConnectionEnabled = true;
            isAutoConnecting = true;
            if(mAutoConnectionListener != null)
                mAutoConnectionListener.onAutoConnectionStarted();
            final ArrayList<String> arr_filter_address = new ArrayList<String>();
            final ArrayList<String> arr_filter_name = new ArrayList<String>();
            String[] arr_name = getPairedDeviceName();
            String[] arr_address = getPairedDeviceAddress();
            for(int i = 0 ; i < arr_name.length ; i++) {
                if(arr_name[i].contains(keywordName)) {
                    arr_filter_address.add(arr_address[i]);
                    arr_filter_name.add(arr_name[i]);
                }
            }

            bcl = new BluetoothConnectionListener() {
                public void onDeviceConnected(String name, String address) {
                    bcl = null;
                    isAutoConnecting = false;
                }

                public void onDeviceDisconnected() { }
                public void onDeviceConnectionFailed() {
                    Log.e("CHeck", "Failed");
                    if(isServiceRunning) {
                        if(isAutoConnectionEnabled) {
                            c++;
                            if(c >= arr_filter_address.size())
                                c = 0;
                            connect(arr_filter_address.get(c));
                            Log.e("CHeck", "Connect");
                            if(mAutoConnectionListener != null)
                                mAutoConnectionListener.onNewConnection(arr_filter_name.get(c)
                                        , arr_filter_address.get(c));
                        } else {
                            bcl = null;
                            isAutoConnecting = false;
                        }
                    }
                }
            };

            setBluetoothConnectionListener(bcl);
            c = 0;
            if(mAutoConnectionListener != null)
                mAutoConnectionListener.onNewConnection(arr_name[c], arr_address[c]);
            if(arr_filter_address.size() > 0)
                connect(arr_filter_address.get(c));
            else
                Toast.makeText(mContext, "Device name mismatch", Toast.LENGTH_SHORT).show();
        }
    }
    public   void unpairDevice(String address) {
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e("unpairDevice", e.getMessage());
        }
    }
    public  void unpairDevice(BluetoothDevice device) {

        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e("unpairDevice", e.getMessage());
        }
    }
    public   void unPairDevices() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
            mBluetoothAdapter.enable();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (mBluetoothAdapter != null) {
            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                unpairDevice(device.getAddress());
            }
        }
    }
    /**????????????????????????*/
    public  byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    public  String bytesToHexString(byte [] buffer){
        String h = "";

        for(int i = 0; i < buffer.length; i++){
            String temp = Integer.toHexString(buffer[i] & 0xFF);
            if(temp.length() == 1){
                temp = "0" + temp;
            }
            h = h + temp;
        }
        return h;
    }


    public void close(){
        if (isBluetoothEnabled()){
            getBluetoothAdapter().disable();
        }
    }


    public void unregisterRecvier(){
        mContext.unregisterReceiver(mReciver);
    }

}
