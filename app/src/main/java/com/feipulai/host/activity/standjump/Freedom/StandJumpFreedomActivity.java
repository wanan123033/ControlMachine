package com.feipulai.host.activity.standjump.Freedom;

import android.os.Handler;
import android.os.Message;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.freedom.BaseFreedomTestActivity;
import com.feipulai.host.activity.standjump.StandJumpSettingActivity;
import com.feipulai.host.activity.standjump.StandResiltListener;
import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;

/**
 * Created by zzs on  2019/10/8
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandJumpFreedomActivity extends BaseFreedomTestActivity implements StandResiltListener.HandlerInterface {
    private static final int MSG_DISCONNECT = 0X101;
    private static final int INIT_AGAIN = 0X102;
    private static final int UPDATE_DEVICE = 0X103;
    private static final int UPDATE_RESULT = 0X104;
    private MyHandler mHandler;
    //3秒内检测测量垫是否可用
    private volatile boolean isDisconnect;
    private StandResiltListener standResiltListener = new StandResiltListener(this);

    @Override
    protected void initData() {
        super.initData();
        mHandler = new MyHandler(this);
        sendCheck();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SerialDeviceManager.getInstance().setRS232ResiltListener(standResiltListener);
        sendCheck();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //结束测试 发送结束指令
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_END_JUMP));
        SerialDeviceManager.getInstance().close();
    }

    @Override
    public void gotoItemSetting() {
        IntentUtil.gotoActivity(this, StandJumpSettingActivity.class);
    }

    @Override
    public void startTest() {
        sendCheck();
        standResiltListener.setTestState(StandResiltListener.TestState.START_TEST);
        //设置当前设置为空闲状态
        setDeviceState(new BaseDeviceState(BaseDeviceState.STATE_FREE));
    }

    @Override
    public void stopTest() {
        sendCheck();
        //设置当前设置为空闲状态
        setDeviceState(new BaseDeviceState(BaseDeviceState.STATE_FREE));
    }

    @Override
    public void getDeviceState(BaseDeviceState deviceState) {
        BaseDeviceState state = new BaseDeviceState();
        state.setState(deviceState.getState());
        state.setDeviceId(1);
        Message msg = mHandler.obtainMessage();
        msg.obj = state;
        msg.what = UPDATE_DEVICE;
        mHandler.sendMessage(msg);
    }

    @Override
    public void getResult(BaseStuPair deviceState) {
        Message msg = mHandler.obtainMessage();
        msg.obj = deviceState;
        msg.what = UPDATE_RESULT;
        mHandler.sendMessage(msg);
    }

    @Override
    public void CheckDevice(boolean isCheckDevice, int[] brokenLEDs) {
        isDisconnect = !isCheckDevice;
        Logger.i("CheckDevice===>" + isCheckDevice + "    =====>" + standResiltListener.getTestState());
        if (isCheckDevice && standResiltListener.getTestState() == StandResiltListener.TestState.START_TEST) {

            //开始测试
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_START_JUMP));
        }
    }

    @Override
    public void StartDevice() {
        isDisconnect = false;
        //检测通过可以发送测试指令
        toastSpeak(getString(R.string.start_test));
    }

    @Override
    public void AgainTest(BaseDeviceState deviceState) {
        toastSpeak(getString(R.string.again_test_hint));
        startTest();
    }

    @Override
    public void EndDevice(boolean isFoul, int result) {

    }

    /**
     * 发送检测设备指令
     */
    private void sendCheck() {
        isDisconnect = true;
        if (SerialDeviceManager.getInstance() != null) {
            //测量垫自检,校验连接是否正常
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SELF_CHECK_JUMP));
            mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 3000);
        }
        standResiltListener.setTestState(StandResiltListener.TestState.UN_STARTED);
        setDeviceState(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));

    }

    private static class MyHandler extends Handler {

        private WeakReference<StandJumpFreedomActivity> mActivityWeakReference;

        public MyHandler(StandJumpFreedomActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            StandJumpFreedomActivity activity = mActivityWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_DISCONNECT://连接失败
                        if (activity.isDisconnect) {
                            activity.toastSpeak(activity.getString(R.string.device_noconnect));
                            //设置当前设置为不可用断开状态
                            activity.setDeviceState(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
                        }
                        break;
                    case INIT_AGAIN://重新检测设备，初始化设备
                        activity.sendCheck();
                        break;
                    case UPDATE_DEVICE:
                        activity.setDeviceState((BaseDeviceState) msg.obj);
                        break;

                    case UPDATE_RESULT:
                        activity.settTestResult((BaseStuPair) msg.obj);
                        break;
                }
            }

        }
    }
}
