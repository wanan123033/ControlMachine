package com.feipulai.wireless.activity;

import android.os.Message;
import android.util.Log;

import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.VitalCapacityResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;

import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by pengjf on 2019/2/14.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class WirelessManager implements RadioManager.OnRadioArrivedListener {
    private static final WirelessManager managerInstance = new WirelessManager();
    private static final String TAG = "WirelessManager";
    private WirelessListener listener;
    private final RadioManager radioManager = RadioManager.getInstance();
    private int frequency;
    private WirelessTask wirelessTask;
    private HashSet<Integer> hashSet = new HashSet<>();

    public static WirelessManager getInstance() {
        return managerInstance;
    }

    private WirelessManager() {
        MachineCode.machineCode = 2;
        radioManager.init();
        radioManager.sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
        radioManager.setOnRadioArrived(this);
    }

    public void setListener(WirelessListener listener) {
        this.listener = listener;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
        radioManager.sendCommand(new ConvertCommand(new RadioChannelCommand(frequency)));
    }

//    private ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(5));

    @Override
    public void onRadioArrived(Message msg) {
        switch (msg.what) {
            case SerialConfigs.VITAL_CAPACITY_RESULT:
                final VitalCapacityResult result = (VitalCapacityResult) msg.obj;
                Log.i(TAG, "子机号:" + result.getDeviceId() + "状态:" + result.getState() + "序号:" + result.getIndex());
                int index = result.getIndex();
                switch (index) {
                    case 1:
                        //子机请求配对
                        radioManager.sendCommand(new ConvertCommand(new RadioChannelCommand(result.getFrequency())));
                        listener.onPareListener();
                        break;
                    case 3:
                        //回复配对成功
                        listener.onPairSuccess(result);
                        break;
                    case 5:
                        //回应主机查询
                        listener.onResult(result);
                        if (result.getState() == 4) {//结束
                            //此处不应该直接发结束命令而是交给设备去判断
                            listener.onStop(result);
                            Log.i(TAG,"子机号:" + result.getDeviceId() + "状态:" + result.getState() +
                                    "序号:" + result.getIndex()+"计数:"+result.getCapacity());
                        }
                        break;
                    case 7:
                        //回应开始计数命令  查询
                        hashSet.add(result.getDeviceId());
                        wirelessTask.setDeviceIdSet(hashSet);
                        break;
                    case 9:
                        //回应结束命令
//                        settingFree(result.getDeviceId());
                        break;
                    case 11:
                        break;
                }
                break;
        }
    }

    /**
     * 开启查询线程
     */
    public void startQueryThread(){
        if (wirelessTask == null) {
            wirelessTask = new WirelessTask();
            wirelessTask.start();
        }
    }

    public void sendPair(int deviceId) {
        //AB 02 10 09 01 01 2B 04 00 00 00 00 00 00 F7 0A 回应配对
        cmd(2, deviceId, 1);
        radioManager.sendCommand(new ConvertCommand(new RadioChannelCommand(frequency)));
    }

    /**
     * 开始测试
     *
     * @param deviceId
     */
    public void startTest(int deviceId) {
        cmd(6, deviceId, 3);
    }

    public void stopTest(int deviceId) {
//        cmd(8, deviceId, 4);
        wirelessTask.removeDevice(deviceId);
    }

    public void settingFree(int deviceId) {
        cmd(10, deviceId, 5);
    }

    interface WirelessListener {
        void onPareListener();

        void onPairSuccess(VitalCapacityResult result);

        void onResult(VitalCapacityResult result);

        void onStop(VitalCapacityResult result);
    }

    /**
     * @param index    包序
     * @param deviceId 设备号
     */
    private void cmd(int index, int deviceId, int cmd) {
        byte[] data = {(byte) 0xAB, (byte) index, 0x10, 0x02, (byte) deviceId, (byte) cmd, (byte) frequency, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x00, 0x0A};
        int sum = 0;
        for (int i = 0; i < data.length - 2; i++) {
            sum += data[i];
        }
        data[14] = (byte) sum;

        //todo 此处需考虑添加优先级队列
        //因为开始测试 和 空闲测试两个命令需要优先发出去
        radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
    }


    /**
     * 线程
     * id
     */
    class WirelessTask extends Thread {
        private boolean isStop = true;
        private HashSet<Integer> deviceIds = new HashSet<>();
        private int interval;
        public void setStop(boolean isStop) {
            this.isStop = isStop;
        }

        public void setDeviceIdSet(HashSet<Integer> set) {
            deviceIds = set;
            this.interval = 100 / deviceIds.size(); //因为是每间隔100ms查询一次
        }

        public void removeDevice(int deviceId) {
            deviceIds.remove(deviceId);
        }

        @Override
        public void run() {
            while (isStop) {
                if (deviceIds.size() > 0){
                    for (int i : deviceIds)
                        cmd(4, i, 2);
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void stopQuery(){
        if (wirelessTask!= null){
            wirelessTask.setStop(true);
        }
    }



}
