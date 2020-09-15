package com.feipulai.host.activity.sitreach;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.feipulai.device.led.LEDManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BasePersonFaceIDActivity;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.feipulai.host.utils.StringUtility;
import com.orhanobut.logger.utils.LogUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zzs on 2018/8/27
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitReachFaceIDActivity extends BasePersonFaceIDActivity {
    //获取成绩
    private GetResultRunnable resultRunnable = new GetResultRunnable();
    private CheckDeviceRunnable statesRunnable = new CheckDeviceRunnable();
    private ExecutorService mExecutorService;

    private LEDManager mLEDManager;
    //3秒内检设备是否可用
    private volatile boolean isDisconnect = true;
    /**
     * 记录设备不可用播报时间
     */
    private long disconnectTime = 0;
    private MyHandler mHandler = new MyHandler(this);
    private static final int MSG_DISCONNECT = 0X101;
    private static final int MSG_START_TEST = 0X102;
    private BaseStuPair baseStuPair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.operation("SitReachFaceIDActivity onCreate");
        //将回调放在super前，避免第一次检测设备失败
        mLEDManager = new LEDManager();
        if (SerialDeviceManager.getInstance() != null) {

            SerialDeviceManager.getInstance().setRS232ResiltListener(sitReachResiltListener);
        }
        mExecutorService = Executors.newFixedThreadPool(2);
        mExecutorService.submit(resultRunnable);
        mExecutorService.submit(statesRunnable);
        super.onCreate(savedInstanceState);
        sitReachResiltListener.setTestState(SitReachResiltListener.TestState.UN_STARTED);
        resultRunnable.setTestState(sitReachResiltListener.getTestState());
        statesRunnable.setTestState(sitReachResiltListener.getTestState());
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));

    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.operation("SitReachFaceIDActivity onResume");
        if (SerialDeviceManager.getInstance() != null) {
            SerialDeviceManager.getInstance().setRS232ResiltListener(sitReachResiltListener);
        } else {
            updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.operation("SitReachFaceIDActivity onDestroy");
        if (SerialDeviceManager.getInstance() != null) {
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_END));
        }
        SerialDeviceManager.getInstance().close();
        resultRunnable.setTestState(SitReachResiltListener.TestState.UN_STARTED);
        statesRunnable.setTestState(SitReachResiltListener.TestState.UN_STARTED);
        statesRunnable.setFinish(true);
        resultRunnable.setFinish(true);

    }

    /**
     * 设置测试设备
     */
    @Override
    public BaseDeviceState findDevice() {
        return new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1);
    }


    @Override
    public void sendTestCommand(@NonNull BaseStuPair baseStuPair) {
        LogUtils.operation("坐位体前屈开始测试:"+baseStuPair.toString());
        this.baseStuPair = baseStuPair;
        //3秒检测设备间隔
        mHandler.sendEmptyMessageDelayed(MSG_START_TEST, 3000);
    }




    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        startActivity(new Intent(this, SitReachTestActivity.class));
        finish();
    }

    private SitReachResiltListener sitReachResiltListener = new SitReachResiltListener(new SitReachResiltListener.HandlerInterface() {
        @Override
        public void checkDevice(int deviceId) {
            isDisconnect = false;
            updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, deviceId));
        }

        @Override
        public void getDeviceState(BaseDeviceState deviceState) {
            updateDevice(deviceState);
        }

        @Override
        public void getResult(BaseStuPair stuPair) {
            updateResult(stuPair);
        }

        @Override
        public void EndDevice(boolean isFoul, int result) {
            resultRunnable.setTestState(sitReachResiltListener.getTestState());
            statesRunnable.setTestState(sitReachResiltListener.getTestState());
            if (null == baseStuPair.getStudent()) {
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "自由测试", mLEDManager.getX("自由测试"), 0, true, false);
            } else {
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), baseStuPair.getStudent().getStudentName(), mLEDManager.getX(baseStuPair.getStudent().getStudentName()), 0, true, false);

            }
            if (isFoul) {
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "犯规", mLEDManager.getX("犯规"), 2, false, true);
            } else {
//                String text = result + setUnit();
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), ResultDisplayUtils.getStrResultForDisplay(result), mLEDManager.getX(ResultDisplayUtils.getStrResultForDisplay(result)), 2, false, true);

            }
            mHandler.sendEmptyMessageDelayed(MSG_START_TEST, 5000);
        }

        @Override
        public void AgainTest(BaseDeviceState deviceState) {
            resultRunnable.setTestState(sitReachResiltListener.getTestState());
            statesRunnable.setTestState(sitReachResiltListener.getTestState());
        }

        @Override
        public void stopResponse(int deviveId) {

        }
        @Override
        public void ready(int deviveId) {
            toastSpeak("开始测试");
        }
    });


//    @Override
//    public void malfunctionClickListener(@NonNull BaseStuPair baseStuPair) {
//        sendCheckCommand(baseStuPair.getBaseDevice().getDeviceId());
//    }

    //    /**
//     * 发送检测设备指令
//     */
//    public void sendCheckCommand(int deviceId) {
//        isDisconnect = true;
//        if (SerialManager.getInstance() != null) {
//            //设备自检,校验连接是否正常
//            SerialManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_EMPTY));
//            mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 3000);
//        }
//        SitReachResiltListener.setTestState(SitReachResiltListener.TestState.UN_STARTED);
//        resultRunnable.setTestState(SitReachResiltListener.getTestState());
//        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, deviceId));
//    }
    private void sendStartCommand() {
        sitReachResiltListener.setTestState(SitReachResiltListener.TestState.WAIT_RESULT);
        resultRunnable.setTestState(sitReachResiltListener.getTestState());
        statesRunnable.setTestState(sitReachResiltListener.getTestState());
        if (SerialDeviceManager.getInstance() != null) {
            //开始测试
            LogUtils.normal("坐位体前屈开始指令:"+StringUtility.bytesToHexString(SerialConfigs.CMD_SIT_REACH_START));
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_START));
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "请测试", 5, 1, true, true);
            //获取数据
            LogUtils.normal("坐位体前屈获取数据:"+StringUtility.bytesToHexString(SerialConfigs.CMD_SIT_REACH_GET_SCORE));
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_GET_SCORE));
        }
    }

    private static class MyHandler extends Handler {

        private WeakReference<SitReachFaceIDActivity> mActivityWeakReference;

        public MyHandler(SitReachFaceIDActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SitReachFaceIDActivity activity = mActivityWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_DISCONNECT:
                        LogUtils.operation("坐位体前屈 2MSG_DISCONNECT:" + activity.isDisconnect);
                        if (activity.isDisconnect) {
                            // 判断2次点击事件时间

                            if (!activity.isDestroyed() && (System.currentTimeMillis() - activity.disconnectTime) > 30000) {
//                                activity.toastSpeak("设备未连接");
                                activity.disconnectTime = System.currentTimeMillis();
                            }

                            //设置当前设置为不可用断开状态
                            activity.updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
                            activity.sitReachResiltListener.setTestState(SitReachResiltListener.TestState.UN_STARTED);
                            activity.resultRunnable.setTestState(SitReachResiltListener.TestState.UN_STARTED);
                        }
                        break;
                    case MSG_START_TEST:
                        if (activity.baseStuPair.getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
                            activity.sendStartCommand();
                        }
                        break;
                }
            }

        }
    }

    private class GetResultRunnable implements Runnable {
        SitReachResiltListener.TestState testState = SitReachResiltListener.TestState.UN_STARTED;

        public void setTestState(SitReachResiltListener.TestState testState) {
            this.testState = testState;
        }

        private boolean isFinish = false;

        public void setFinish(boolean finish) {
            isFinish = finish;
        }

        @Override
        public void run() {
            while (!isFinish) {
                if (testState != SitReachResiltListener.TestState.UN_STARTED) {
                    LogUtils.normal("坐位体前屈获取成绩指令:"+ StringUtility.bytesToHexString(SerialConfigs.CMD_SIT_REACH_GET_SCORE));
                    if (SerialDeviceManager.getInstance() != null)
                        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_GET_SCORE));
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取设置状态
     */
    private class CheckDeviceRunnable implements Runnable {
        private boolean isFinish = false;

        public void setFinish(boolean finish) {
            isFinish = finish;
        }

        SitReachResiltListener.TestState testState = SitReachResiltListener.TestState.UN_STARTED;

        public void setTestState(SitReachResiltListener.TestState testState) {
            this.testState = testState;
        }

        @Override
        public void run() {
            while (!isFinish) {
                if (testState == SitReachResiltListener.TestState.UN_STARTED) {
                    Log.i("zzs", "===>" + "sendCheckCommand");
                    if (SerialDeviceManager.getInstance() != null) {
                        try {
                            //设备自检,校验连接是否正常
                            isDisconnect = true;
                            if (SerialDeviceManager.getInstance() != null) {
                                //设备自检,校验连接是否正常
                                LogUtils.normal("坐位体前屈设备自检指令:"+ StringUtility.bytesToHexString(SerialConfigs.CMD_SIT_REACH_EMPTY));
                                SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_EMPTY));
                                mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 3000);
                            }
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
