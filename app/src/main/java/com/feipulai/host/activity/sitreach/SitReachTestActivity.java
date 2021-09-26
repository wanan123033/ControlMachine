package com.feipulai.host.activity.sitreach;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.person.BasePersonTestActivity;
import com.feipulai.host.utils.StringUtility;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 坐位体前屈个人模式
 * Created by zzs on 2018/11/20
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitReachTestActivity extends BasePersonTestActivity implements SitReachResiltListener.HandlerInterface {
    private final static String TAG = "SitReachTest";
    //3秒内检设备是否可用
    private volatile boolean isDisconnect = true;

    private static final int MSG_DISCONNECT = 0X101;
    private static final int UPDATE_DEVICE = 0X102;
    private static final int UPDATE_RESULT = 0X103;
    private static final int TOAST_SPEAK = 0X104;
    private MyHandler mHandler;
    private GetResultRunnable resultRunnable;
    private CheckDeviceRunnable statesRunnable;
    private SitReachResiltListener sitReachResiltListener;
    private ExecutorService mExecutorService;
    private long disconnectTime;
    //保存当前测试考生
    private BaseStuPair baseStuPair;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler = new MyHandler(this);
        if (sitReachResiltListener == null) {
            sitReachResiltListener = new SitReachResiltListener(this);
            sitReachResiltListener.setTestState(SitReachResiltListener.TestState.UN_STARTED);
        }
        SerialDeviceManager.getInstance().setRS232ResiltListener(sitReachResiltListener);
        mExecutorService = Executors.newFixedThreadPool(2);
        resultRunnable = new GetResultRunnable();
        statesRunnable = new CheckDeviceRunnable();
        mExecutorService.submit(resultRunnable);
        mExecutorService.submit(statesRunnable);
        resultRunnable.setTestState(sitReachResiltListener.getTestState());
        statesRunnable.setTestState(sitReachResiltListener.getTestState());
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
        if (SerialDeviceManager.getInstance() != null && sitReachResiltListener.getTestState() != SitReachResiltListener.TestState.UN_STARTED) {
            //开始测试
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_START));
            LogUtils.normal("坐位体前屈开始指令:"+ StringUtility.bytesToHexString(SerialConfigs.CMD_SIT_REACH_START));
            //获取数据
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_GET_SCORE));
            LogUtils.normal("坐位体前屈获取数据:"+ StringUtility.bytesToHexString(SerialConfigs.CMD_SIT_REACH_GET_SCORE));
        }
    }

    @Override
    public void stuSkip() {
        LogUtils.normal("坐位体前屈跳过测试:"+StringUtility.bytesToHexString(SerialConfigs.CMD_SIT_REACH_END));
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_END));
        sitReachResiltListener.setTestState(SitReachResiltListener.TestState.UN_STARTED);
        resultRunnable.setTestState(sitReachResiltListener.getTestState());
        statesRunnable.setTestState(sitReachResiltListener.getTestState());
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_FREE, 1));
    }

    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {
        this.baseStuPair = baseStuPair;
        LogUtils.normal("坐位体前屈开始测试:"+baseStuPair.toString());
        Logger.i(TAG + ":sendTestCommand发送开始测试");
        sitReachResiltListener.setTestState(SitReachResiltListener.TestState.WAIT_RESULT);
        resultRunnable.setTestState(sitReachResiltListener.getTestState());
        statesRunnable.setTestState(sitReachResiltListener.getTestState());
        if (SerialDeviceManager.getInstance() != null) {
            //开始测试
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_START));
            //获取数据
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_GET_SCORE));
        }
    }



    @Override
    public void gotoItemSetting() {
        startActivity(new Intent(this, SitReachSettingActivity.class));
    }


    @Override
    protected void onPause() {
        LogUtils.operation("SitReachTestActivity onPause");
        super.onPause();
        if (SerialDeviceManager.getInstance() != null) {
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_END));
        }
        SerialDeviceManager.getInstance().close();
        resultRunnable.setTestState(SitReachResiltListener.TestState.UN_STARTED);
        statesRunnable.setTestState(sitReachResiltListener.getTestState());
        statesRunnable.setFinish(true);
        resultRunnable.setFinish(true);
        statesRunnable = null;
        resultRunnable = null;
        mHandler.removeCallbacksAndMessages(null);
        mExecutorService.shutdown();
    }



    @Override
    public void checkDevice(int deviceId) {
        isDisconnect = false;
        Message msg = mHandler.obtainMessage();
        msg.obj = new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1);
        msg.what = UPDATE_DEVICE;
        mHandler.sendMessage(msg);
    }

    @Override
    public void getDeviceState(BaseDeviceState deviceState) {
        LogUtils.operation(TAG + ":getDeviceState--->" + deviceState.toString());
        BaseDeviceState state = new BaseDeviceState();
        state.setState(deviceState.getState());
        Message msg = mHandler.obtainMessage();
        msg.obj = state;
        msg.what = UPDATE_DEVICE;
        mHandler.sendMessage(msg);

    }

    @Override
    public void getResult(BaseStuPair stuPair) {

        LogUtils.operation(TAG + ":getResult--->" + stuPair.toString());
        Message msg = mHandler.obtainMessage();
        msg.obj = stuPair;
        msg.what = UPDATE_RESULT;
        mHandler.sendMessage(msg);
    }

    @Override
    public void EndDevice(boolean isFoul, int result) {
        LogUtils.operation(TAG + ":EndDevice--->");
        resultRunnable.setTestState(sitReachResiltListener.getTestState());
        statesRunnable.setTestState(sitReachResiltListener.getTestState());
    }

    @Override
    public void AgainTest(BaseDeviceState deviceState) {
        LogUtils.operation(TAG + ":AgainTest--->");
        resultRunnable.setTestState(sitReachResiltListener.getTestState());
        statesRunnable.setTestState(sitReachResiltListener.getTestState());
        toastSpeak("设备错误重测");
    }

    @Override
    public void stopResponse(int deviveId) {

    }

    @Override
    public void ready(int deviveId) {
        LogUtils.operation(TAG + ":ready--->");
        mHandler.sendEmptyMessageDelayed(TOAST_SPEAK, 1000);
    }

    private static class MyHandler extends Handler {

        private WeakReference<SitReachTestActivity> mActivityWeakReference;

        public MyHandler(SitReachTestActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SitReachTestActivity activity = mActivityWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case UPDATE_DEVICE:
                        activity.updateDevice((BaseDeviceState) msg.obj);
                        break;
                    case UPDATE_RESULT:
                        activity.updateResult((BaseStuPair) msg.obj);
                        break;
                    case MSG_DISCONNECT:
                        LogUtils.operation(TAG + ":1MSG_DISCONNECT:" + activity.isDisconnect);
                        if (activity.isDisconnect) {
                            // 判断2次提示时间
                            if (!activity.isDestroyed() && (System.currentTimeMillis() - activity.disconnectTime) > 30000) {
                                activity.toastSpeak("设备未连接");
                                activity.disconnectTime = System.currentTimeMillis();
                            }

                            //设置当前设置为不可用断开状态
                            activity.updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));

                            activity.sitReachResiltListener.setTestState(SitReachResiltListener.TestState.UN_STARTED);
                            if (activity.resultRunnable != null) {
                                activity.resultRunnable.setTestState(SitReachResiltListener.TestState.UN_STARTED);
                            }

                        }
                        break;
                    case TOAST_SPEAK:
                        activity.toastSpeak("开始测试");
                        break;
                }
            }

        }
    }

    /**
     * 获取成绩
     */
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
                    LogUtils.operation( "坐位体前屈===>" + "sendCommand");
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
                //开始测试不发送自检指令，会出现4秒内未收到返回数据 ，因为在这个时间段还在接收获取成绩的数据
                if (testState == SitReachResiltListener.TestState.UN_STARTED) {
                    LogUtils.operation("坐位体前屈===>" + "sendCheckCommand");
                    if (SerialDeviceManager.getInstance() != null) {

                        try {
                            //设备自检,校验连接是否正常
                            isDisconnect = true;
                            if (SerialDeviceManager.getInstance() != null) {
                                //设备自检,校验连接是否正常
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
