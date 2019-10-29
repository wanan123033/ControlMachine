package com.feipulai.exam.activity.standjump;

import android.os.Message;

import com.feipulai.device.manager.VolleyBallManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;
import com.feipulai.exam.bean.DeviceDetail;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zzs on  2019/10/25
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandRadioHelper implements RadioManager.OnRadioArrivedListener {

    private ExecutorService executor;
    private StandDetector deviceDetector;
    private Listener listener;
    private VolleyBallSetting setting;
    private List<DeviceDetail> deviceDetails;

    public StandRadioHelper(List<DeviceDetail> deviceDetails, VolleyBallSetting setting, Listener listener) {
        this.listener = listener;
        this.deviceDetails = deviceDetails;
        this.setting = setting;
        executor = Executors.newCachedThreadPool();
        RadioManager.getInstance().setOnRadioArrived(this);
        deviceDetector = new StandDetector();
        deviceDetector.startDetect();
    }

    @Override
    public void onRadioArrived(Message msg) {
        // 收到了,就证明连接正常(重新连接了或者连接本身就是正常的)
        deviceDetector.missCount.getAndSet(0);
        listener.onDeviceConnectState(VolleyBallManager.VOLLEY_BALL_CONNECT);
    }


    private class StandDetector {

        private ExecutorService executor = Executors.newSingleThreadExecutor();
        private AtomicInteger missCount = new AtomicInteger();
        private volatile boolean detecting = true;

        private void startDetect() {
            detecting = true;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    while (detecting) {
                        // 测试过程中不断获取成绩

                        for (DeviceDetail deviceDetail : deviceDetails) {
                            if (deviceDetail.isDeviceOpen() && deviceDetail.getStuDevicePair().getStudent() != null) {
//                                deviceManager.getScore();
                            } else {
//                                deviceManager.emptyCommand();
                            }
                        }

                        int count = missCount.addAndGet(1);
                        if (count >= 10) {
                            // 认为设备已经断开了连接
                            listener.onDeviceConnectState(VolleyBallManager.VOLLEY_BALL_DISCONNECT);
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        private void stopDetect() {
            detecting = false;
            executor.shutdown();
        }

    }


    public interface Listener {

        void onDeviceConnectState(int state);

    }
}
