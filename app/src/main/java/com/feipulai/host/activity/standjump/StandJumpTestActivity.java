package com.feipulai.host.activity.standjump;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.person.BasePersonTestActivity;

import java.lang.ref.WeakReference;


/**
 * 立地跳远
 * Created by zzs on 2018/8/7
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandJumpTestActivity extends BasePersonTestActivity implements StandResiltListener.HandlerInterface {
    private static final int MSG_DISCONNECT = 0X101;
    private static final int INIT_AGAIN = 0X102;
    private static final int UPDATE_DEVICE = 0X103;
    private static final int UPDATE_RESULT = 0X104;
    private MyHandler mHandler;
    //3秒内检测测量垫是否可用
    private volatile boolean isDisconnect;
    //最大使用设备数量
    private int maxDivice;
    private StandResiltListener standResiltListener = new StandResiltListener(this);

    @Override
    protected void initData() {
        super.initData();
        mHandler = new MyHandler(this);
        llState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_ERROR) {
                    toastSpeak(getString(R.string.waiting_connect));
                    onResume();
                    if (pair.getStudent() != null) {
                        standResiltListener.setTestState(StandResiltListener.TestState.START_TEST);
                    }
                }

            }
        });
    }


    @Override
    public void onViewClicked(View view) {
        super.onViewClicked(view);
        if (view.getId() == R.id.tv_free_test) {
            startActivity(new Intent(this, StandJumpFaceIDActivity.class));
            finish();
        }
    }

    @Override
    public void gotoItemSetting() {
        IntentUtil.gotoActivity(this, StandJumpSettingActivity.class);
    }

    @Override
    public void stuSkip() {

    }

    @Override
    public void sendTestCommand(BaseStuPair stuPair) {
        sendCheck();
        standResiltListener.setTestState(StandResiltListener.TestState.START_TEST);
        //开始测试
//        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_START_JUMP));
        //设置当前设置为空闲状态
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_FREE, stuPair.getBaseDevice().getDeviceId()));
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
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
//        try {
//            //睡眠 避免发送开始命令后没收到检测回调
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }


    private static class MyHandler extends Handler {

        private WeakReference<StandJumpTestActivity> mActivityWeakReference;

        public MyHandler(StandJumpTestActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            StandJumpTestActivity activity = mActivityWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_DISCONNECT://连接失败
                        if (activity.isDisconnect) {
                            activity.toastSpeak(activity.getString(R.string.device_noconnect));
                            //设置当前设置为不可用断开状态
                            activity.updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
                        }
                        break;
                    case INIT_AGAIN://重新检测设备，初始化设备
                        activity.sendCheck();
                        break;
                    case UPDATE_DEVICE:
                        activity.updateDevice((BaseDeviceState) msg.obj);
                        break;

                    case UPDATE_RESULT:
                        activity.updateResult((BaseStuPair) msg.obj);
                        break;
                }
            }

        }
    }


    @Override
    public void getDeviceState(final BaseDeviceState deviceState) {
        BaseDeviceState state = new BaseDeviceState();
        state.setState(deviceState.getState());
        state.setDeviceId(1);
        Message msg = mHandler.obtainMessage();
        msg.obj = state;
        msg.what = UPDATE_DEVICE;
        mHandler.sendMessage(msg);
    }

    @Override
    public void getResult(final BaseStuPair deviceState) {
        Message msg = mHandler.obtainMessage();
        msg.obj = deviceState;
        msg.what = UPDATE_RESULT;
        mHandler.sendMessage(msg);
    }

    @Override
    public void CheckDevice(boolean isCheckDevice, int[] brokenLEDs) {
        isDisconnect = !isCheckDevice;
//        if (!isCheckDevice) {
//            toastSpeak("测量垫已损坏,请更换测量垫");
//        }
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
//        Message msg = mHandler.obtainMessage();
//        msg.what = INIT_AGAIN;
//        mHandler.sendMessage(msg);
//        //开始测试
//        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_START_JUMP));
//        //设置当前设置为空闲状态
//        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_FREE, 1));
        sendTestCommand(pair);
    }

    @Override
    public void EndDevice(boolean isFoul, int result) {
//        mHandler.sendEmptyMessageDelayed(INIT_AGAIN, 5000);
    }
}
