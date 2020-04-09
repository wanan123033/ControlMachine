package com.feipulai.exam.activity.sitreach;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseGroupTestActivity;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.config.TestConfigs;
import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 坐位体前屈分组模式
 * Created by zzs on 2018/11/20
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitReachGroupTestActivity extends BaseGroupTestActivity implements SitReachResiltListener.HandlerInterface {
    private final static String TAG = "SitReachGroupTest";

    private SitReachSetting reachSetting;
    //3秒内检设备是否可用
    private volatile boolean isDisconnect = true;
    private MyHandler mHandler;
    private static final int MSG_DISCONNECT = 0X101;
    private static final int UPDATE_DEVICE = 0X102;
    private static final int UPDATE_RESULT = 0X103;
    private GetResultRunnable resultRunnable;
    private CheckDeviceRunnable statesRunnable;
    private ExecutorService mExecutorService;
    private SitReachResiltListener sitReachResiltListener;
    //保存当前测试考生
    private BaseStuPair baseStuPair;
    private long disconnectTime;

    @Override
    public void initData() {
        reachSetting = SharedPrefsUtil.loadFormSource(this, SitReachSetting.class);
        if (reachSetting == null) {
            reachSetting = new SitReachSetting();
        }
        Logger.i(TAG + ":reachSetting ->" + reachSetting.toString());
//        if (SerialDeviceManager.getInstance() != null) {
//            SerialDeviceManager.getInstance().setRS232ResiltListener(sitReachResiltListener);
//        } else {
//            updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR));
//        }
//        mExecutorService = Executors.newFixedThreadPool(2);
//        mExecutorService.submit(resultRunnable);
//        mExecutorService.submit(statesRunnable);
//        sitReachResiltListener.setTestState(SitReachResiltListener.TestState.UN_STARTED);
//        resultRunnable.setTestState(sitReachResiltListener.getTestState());
//        statesRunnable.setTestState(sitReachResiltListener.getTestState());
//        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
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
            //获取数据
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_GET_SCORE));
        }
    }

    @Override
    public int setTestCount() {
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            return TestConfigs.sCurrentItem.getTestNum();
        } else {
            return reachSetting.getTestCount();
        }
    }

    @Override
    public void gotoItemSetting() {
        startActivity(new Intent(this, SitReachSettingActivity.class));
        finish();
    }

    @Override
    public void startTest(BaseStuPair stuPair) {
        baseStuPair = stuPair;
        baseStuPair.setTestTime(System.currentTimeMillis()+"");
        Logger.i(TAG + ":startTest ->发送开始测试");
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
    public int setTestPattern() {
        return reachSetting.getTestPattern();
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (SerialDeviceManager.getInstance() != null) {
//            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_END));
//        }
//        SerialDeviceManager.getInstance().close();
//    }

    @Override
    protected void onPause() {
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
        mHandler = null;
        mExecutorService.shutdown();
    }


//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        resultRunnable.setTestState(SitReachResiltListener.TestState.UN_STARTED);
//        statesRunnable.setTestState(sitReachResiltListener.getTestState());
//        statesRunnable.setFinish(true);
//        resultRunnable.setFinish(true);
//        statesRunnable = null;
//        resultRunnable = null;
//        mExecutorService.shutdown();
//
//    }

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
        Logger.i(TAG + ":getDeviceState--->" + deviceState.toString());
        BaseDeviceState state = new BaseDeviceState();
        state.setState(deviceState.getState());
        Message msg = mHandler.obtainMessage();
        msg.obj = state;
        msg.what = UPDATE_DEVICE;
        mHandler.sendMessage(msg);

    }

    @Override
    public void getResult(BaseStuPair stuPair) {
        if (reachSetting.isFullReturn()) {
            if (baseStuPair.getStudent().getSex() == 0) {//男子
                stuPair.setFullMark(stuPair.getResult() >= reachSetting.getManFull() * 10);
            } else {
                stuPair.setFullMark(stuPair.getResult() >= reachSetting.getWomenFull() * 10);
            }
        }
        Logger.i(TAG + ":getResult--->" + stuPair.toString());
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
        toastSpeak("设备错误重测");
    }

    @Override
    public void stopResponse(int deviveId) {

    }

    @Override
    public void ready(int deviveId) {
        toastSpeak("开始测试");
    }

    private static class MyHandler extends Handler {

        private WeakReference<SitReachGroupTestActivity> mActivityWeakReference;

        public MyHandler(SitReachGroupTestActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SitReachGroupTestActivity activity = mActivityWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case UPDATE_DEVICE:

                        activity.updateDevice((BaseDeviceState) msg.obj);
                        break;
                    case UPDATE_RESULT:
                        activity.updateTestResult((BaseStuPair) msg.obj);
                        break;
                    case MSG_DISCONNECT:
                        Logger.i(TAG + ":1MSG_DISCONNECT:" + activity.isDisconnect);
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
