package com.feipulai.exam.activity.standjump;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BasePersonTestActivity;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.Student;
import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;

/**
 * 立定跳远个人模式
 * Created by zzs on 2018/11/20
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandJumpTestActivity extends BasePersonTestActivity {

    private final static String TAG = "StandJumpTest";
    private StandJumpSetting jumpSetting;
    private static final int MSG_DISCONNECT = 0X101;
    private static final int INIT_AGAIN = 0X102;
    private static final int UPDATE_DEVICE = 0X103;
    private static final int UPDATE_RESULT = 0X104;
    private static final int AGAIN_TEST = 0X105;
    private static final int TOAST_SPEAK = 0X106;
    private MyHandler mHandler;
    //3秒内检测测量垫是否可用
    private volatile boolean isDisconnect;
    private long disconnectTime;

    @Override
    public void initData() {
        jumpSetting = SharedPrefsUtil.loadFormSource(this, StandJumpSetting.class);
        if (jumpSetting == null) {
            jumpSetting = new StandJumpSetting();
        }
        Logger.i(TAG + ":reachSetting ->" + jumpSetting.toString());
        mHandler = new MyHandler(this);
//        SerialDeviceManager.getInstance().setRS232ResiltListener(standResiltListener);
//        sendCheck();
//        cbDeviceState.setVisibility(View.INVISIBLE);
        llState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_ERROR) {
                    toastSpeak("等待连接");
                    onResume();
                    if (pair.getStudent() != null) {
                        standResiltListener.setTestState(StandResiltListener.TestState.START_TEST);
                    }
                }

            }
        });
    }

    @Override
    public void stuSkip() {
        standResiltListener.setTestState(StandResiltListener.TestState.UN_STARTED);
    }

    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {
        sendCheck();
        standResiltListener.setTestState(StandResiltListener.TestState.START_TEST);
        //开始测试
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_START_JUMP));
        //设置当前设置为空闲状态
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_FREE));
//        isDisconnect = true;
//        mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 3000);
    }

    @Override
    public int setTestCount() {
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            return TestConfigs.sCurrentItem.getTestNum();
        } else {
            return jumpSetting.getTestCount();
        }
    }

    @Override
    public void gotoItemSetting() {
        if (standResiltListener.getTestState() == StandResiltListener.TestState.WAIT_RESULT
                || standResiltListener.getTestState() == StandResiltListener.TestState.START_TEST) {
            toastSpeak("测试中,不允许修改设置");
        } else {
            startActivity(new Intent(this, StandJumpSettingActivity.class));
            finish();
        }

    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
        if (jumpSetting.isFullReturn()) {
            if (sex == Student.MALE) {
                return result >= jumpSetting.getManFull() * 10;
            } else {
                return result >= jumpSetting.getWomenFull() * 10;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
        SerialDeviceManager.getInstance().setRS232ResiltListener(standResiltListener);
        sendCheck();
//        cbDeviceState.setVisibility(View.INVISIBLE);
//        if (SerialDeviceManager.getInstance() != null && standResiltListener.getTestState() != StandResiltListener.TestState.UN_STARTED) {
//            //开始测试
//            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_START_JUMP));
//        }
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
        try {
            //睡眠 避免发送开始命令后没收到检测回调
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
//                            activity.cbDeviceState.setVisibility(View.VISIBLE);
                            activity.toastSpeak("测量垫未连接");
//                            // 判断2次提示时间
//                            if (!activity.isDestroyed() && (System.currentTimeMillis() - activity.disconnectTime) > 30000) {
//                                activity.toastSpeak("测量垫未连接");
//                                activity.disconnectTime = System.currentTimeMillis();
//                            }

                            //设置当前设置为不可用断开状态
                            activity.updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
                        }
                        break;
                    case INIT_AGAIN://重新检测设备，初始化设备
//                        activity.sendCheck();
                        break;
                    case AGAIN_TEST:

                        ToastUtils.showShort("设备错误,考生请重测");
                        activity.sendCheck();
                        //开始测试
                        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_START_JUMP));
                        //设置当前设置为空闲状态
                        activity.updateDevice(new BaseDeviceState(BaseDeviceState.STATE_FREE));
                        break;
                    case UPDATE_DEVICE:
                        activity.updateDevice((BaseDeviceState) msg.obj);
                        break;

                    case UPDATE_RESULT:
                        activity.updateResult((BaseStuPair) msg.obj);
                        break;
                    case TOAST_SPEAK:
                        activity.toastSpeak("开始测试");
                        break;
                }
            }

        }
    }

    private StandResiltListener standResiltListener = new StandResiltListener(new StandResiltListener.HandlerInterface() {

        @Override
        public void getDeviceState(final BaseDeviceState deviceState) {
            BaseDeviceState state = new BaseDeviceState();
            state.setState(deviceState.getState());
            Message msg = mHandler.obtainMessage();
            msg.obj = state;
            msg.what = UPDATE_DEVICE;
            mHandler.sendMessage(msg);
        }

        @Override
        public void getResult(final BaseStuPair deviceState) {
            if (jumpSetting.isFullReturn()) {
                if (pair.getStudent().getSex() == Student.MALE) {
                    deviceState.setFullMark(deviceState.getResult() >= jumpSetting.getManFull() * 10);
                } else {
                    deviceState.setFullMark(deviceState.getResult() >= jumpSetting.getWomenFull() * 10);
                }
            }
            Message msg = mHandler.obtainMessage();
            msg.obj = deviceState;
            msg.what = UPDATE_RESULT;
            mHandler.sendMessage(msg);
        }

        @Override
        public void CheckDevice(boolean isCheckDevice) {
            Log.i("james", "CheckDevice");
            isDisconnect = !isCheckDevice;
            if (isCheckDevice && standResiltListener.getTestState() == StandResiltListener.TestState.START_TEST) {
                //开始测试
                SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_START_JUMP));
            }
//            if (!isCheckDevice) {
//                toastSpeak("测量垫已损坏,请更换测量垫");
//            }
        }

        @Override
        public void StartDevice() {
            isDisconnect = false;
//            toastSpeak("测试开始");
            mHandler.sendEmptyMessageDelayed(TOAST_SPEAK, 1000);
//            cbDeviceState.setVisibility(View.INVISIBLE);
        }

        @Override
        public void AgainTest(BaseDeviceState deviceState) {
            Message msg = mHandler.obtainMessage();
            msg.obj = deviceState;
            msg.what = AGAIN_TEST;
            mHandler.sendMessage(msg);
        }

        @Override
        public void EndDevice(boolean isFoul, int result) {
//            mHandler.sendEmptyMessageDelayed(INIT_AGAIN, 5000);
        }
    });

}
