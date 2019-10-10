package com.feipulai.host.activity.sitreach;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.person.BasePersonTestActivity;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 坐位体前屈
 * Created by zzs on 2018/7/2
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitReachTestActivity extends BasePersonTestActivity {
    //最大使用设备数量
    private int maxDivice;
    private GetResultRunnable resultRunnable = new GetResultRunnable();
    private CheckDeviceRunnable statesRunnable = new CheckDeviceRunnable();
    private ExecutorService mExecutorService;

    /**
     * 记录设备不可用播报时间
     */
    private long disconnectTime = 0;
    //3秒内检设备是否可用
    private volatile boolean isDisconnect = true;
    private MyHandler mHandler = new MyHandler(this);
    private static final int MSG_DISCONNECT = 0X101;
    private static final int UPDATE_DEVICE = 0X102;
    private static final int UPDATE_RESULT = 0X103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SerialDeviceManager.getInstance() != null) {
            SerialDeviceManager.getInstance().setRS232ResiltListener(sitReachResiltListener);
        } else {
            updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
        }
        mExecutorService = Executors.newFixedThreadPool(2);
        mExecutorService.submit(resultRunnable);
        mExecutorService.submit(statesRunnable);

        sitReachResiltListener.setTestState(SitReachResiltListener.TestState.UN_STARTED);
        resultRunnable.setTestState(sitReachResiltListener.getTestState());
        statesRunnable.setTestState(sitReachResiltListener.getTestState());
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void gotoItemSetting() {
        IntentUtil.gotoActivity(this, SitReachSettingActivity.class);
    }

    //    @Override
//    public void switchToFreeTest() {
//        super.switchToFreeTest();
//        startActivity(new Intent(this, SitReachFaceIDActivity.class));
//        finish();
//    }

//    /**
//     * 发送检测设备指令
//     */
//    private void sendCheck() {
//        isDisconnect = true;
//        if (SerialManager.getInstance() != null) {
//            //设备自检,校验连接是否正常
//            SerialManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_EMPTY));
//            mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 3000);
//        }
//        SitReachResiltListener.setTestState(SitReachResiltListener.TestState.UN_STARTED);
//        resultRunnable.setTestState(SitReachResiltListener.getTestState());
//        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
//    }


//    @Override
//    public void gotoItemSetting() {
//
//    }

    @Override
    public void stuSkip() {

    }

    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {
//        sendCheck();
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

//    @Override
//    public List<BaseDeviceState> findDevice() {
//        maxDivice = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.SIT_REACH_TEST_NUMBER, 1);
//        List<BaseDeviceState> deviceStates = new ArrayList<>();
//        for (int i = 0; i < maxDivice; i++) {
//            deviceStates.add(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, i + 1));
//        }
//        return deviceStates;
//    }


//    @Override
//    public String setUnit() {
//        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getUnit())) {
//            return "cm";
//        }
//        return TestConfigs.sCurrentItem.getUnit();
//    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (SerialDeviceManager.getInstance() != null) {
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_END));
        }
        SerialDeviceManager.getInstance().close();
        resultRunnable.setTestState(SitReachResiltListener.TestState.UN_STARTED);
        statesRunnable.setTestState(sitReachResiltListener.getTestState());
        statesRunnable.setFinish(true);
        resultRunnable.setFinish(true);
    }


    private SitReachResiltListener sitReachResiltListener = new SitReachResiltListener(new SitReachResiltListener.HandlerInterface() {
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
//            updateDevice(deviceState);
            Log.i("james1", "getDeviceState--->" + deviceState.toString());
            BaseDeviceState state = new BaseDeviceState();
            state.setState(deviceState.getState());
            state.setDeviceId(1);
            Message msg = mHandler.obtainMessage();
            msg.obj = state;
            msg.what = UPDATE_DEVICE;
            mHandler.sendMessage(msg);
        }

        @Override
        public void getResult(BaseStuPair stuPair) {
//            updateResult(stuPair);
            Message msg = mHandler.obtainMessage();
            msg.obj = stuPair;
            msg.what = UPDATE_RESULT;
            mHandler.sendMessage(msg);
        }

        @Override
        public void EndDevice(boolean isFoul, int result) {
            resultRunnable.setTestState(sitReachResiltListener.getTestState());
            statesRunnable.setTestState(sitReachResiltListener.getTestState());
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
            toastSpeak(getString(R.string.start_test));
        }
    });


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
                        Log.i("james1", "msg---->" + msg.obj.toString());
                        activity.updateDevice((BaseDeviceState) msg.obj);
                        break;
                    case UPDATE_RESULT:
                        activity.updateResult((BaseStuPair) msg.obj);
                        break;
                    case MSG_DISCONNECT:
                        Log.i("james", "1MSG_DISCONNECT:" + activity.isDisconnect);
                        if (activity.isDisconnect) {
//                            // 判断2次提示时间
//                            if (!activity.isDestroyed() && (System.currentTimeMillis() - activity.disconnectTime) > 30000) {
////                                activity.toastSpeak("设备未连接");
//                                activity.disconnectTime = System.currentTimeMillis();
//                            }

                            //设置当前设置为不可用断开状态
                            activity.updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
                            activity.sitReachResiltListener.setTestState(SitReachResiltListener.TestState.UN_STARTED);
                            activity.resultRunnable.setTestState(SitReachResiltListener.TestState.UN_STARTED);
                        }
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
                    Log.i("zzs", "===>" + "sendCommand");
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
                    Log.i("zzs", "===>" + "sendCheckCommand");
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
