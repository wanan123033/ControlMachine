package com.feipulai.host.activity.standjump;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.feipulai.device.led.LEDManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BasePersonFaceIDActivity;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;

import java.lang.ref.WeakReference;

import static com.feipulai.device.serial.SerialConfigs.CMD_SELF_CHECK_JUMP;


/**
 * Created by zzs on 2018/8/10
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandJumpFaceIDActivity extends BasePersonFaceIDActivity implements StandResiltListener.HandlerInterface,
        BasePersonFaceIDActivity.OnMalfunctionClickListener {

    private static final int MSG_DISCONNECT = 0X101;
    private static final int INIT_AGAIN = 0X102;

    private LEDManager mLEDManager;
    //3秒内检测测量垫是否可用
    private volatile boolean isDisconnect = true;
    private MyHandler mHandler = new MyHandler(this);
    private BaseStuPair baseStuPair;
    private StandResiltListener standResiltListener = new StandResiltListener(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SerialDeviceManager.getInstance().setRS232ResiltListener(standResiltListener);
        mLEDManager = new LEDManager();
        init();
    }

    /**
     * 初始化
     */
    private void init() {

        sendCheck();
        setOnMalfunctionClickListener(this);
    }

    /**
     * 发送检测设备指令
     */
    private void sendCheck() {
        isDisconnect = true;
        if (SerialDeviceManager.getInstance() != null) {
            //测量垫自检,校验连接是否正常
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, CMD_SELF_CHECK_JUMP));
            mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 3000);
        }
        standResiltListener.setTestState(StandResiltListener.TestState.UN_STARTED);
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
    }

    /**
     * 当俭入学生触发发送指令方法
     */
    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {
        this.baseStuPair = baseStuPair;
        if (!isDisconnect) {
            //开始测试
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_START_JUMP));
            standResiltListener.setTestState(StandResiltListener.TestState.START_TEST);
            //设置当前设置为空闲状态
            updateDevice(new BaseDeviceState(BaseDeviceState.STATE_FREE, baseStuPair.getBaseDevice().getDeviceId()));
        }

    }

    /**
     * 设置测试设备
     */
    @Override
    public BaseDeviceState findDevice() {
        return new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1);
    }

    //@Override
    public String setUnit() {
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getUnit())) {
            return "cm";
        }
        return TestConfigs.sCurrentItem.getUnit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SerialDeviceManager.getInstance() == null) {
            SerialDeviceManager.getInstance().setRS232ResiltListener(standResiltListener);
            mLEDManager = new LEDManager();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //结束测试 发送结束指令
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_END_JUMP));
        SerialDeviceManager.getInstance().close();
    }

    private static class MyHandler extends Handler {

        private WeakReference<StandJumpFaceIDActivity> mActivityWeakReference;

        public MyHandler(StandJumpFaceIDActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            StandJumpFaceIDActivity activity = mActivityWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_DISCONNECT:
                        if (activity.isDisconnect) {
                            activity.toastSpeak("测量垫未连接");
                            //设置当前设置为不可用断开状态
                            activity.updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
                        } else {
                            //测量垫检测正常发送测试指令
                            activity.sendTestCommand(activity.baseStuPair);
                        }
                        break;
                    case INIT_AGAIN:
                        activity.sendCheck();
                        break;
                }
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, StandJumpTestActivity.class));

    }


    @Override
    public void malfunctionClickListener(@NonNull BaseStuPair baseStuPair) {
        sendCheck();
    }

    @Override
    public void getDeviceState(BaseDeviceState deviceState) {
        updateDevice(deviceState);
    }

    @Override
    public void getResult(BaseStuPair deviceState) {
        updateResult(deviceState);
    }

    @Override
    public void CheckDevice(boolean isCheckDevice, int[] brokenLEDs) {
        isDisconnect = !isCheckDevice;
    }

    @Override
    public void AgainTest(BaseDeviceState deviceState) {
        sendCheck();
        //开始测试
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_START_JUMP));
        //设置当前设置为空闲状态
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_FREE, 1));
    }

    @Override
    public void StartDevice() {
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "请测试", 5, 1, true, true);
        sendTestCommand(baseStuPair);
    }

    @Override
    public void EndDevice(boolean isFoul, int result) {

        if (null == baseStuPair.getStudent()) {
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "自由测试", mLEDManager.getX("自由测试"), 0, true, false);
            mHandler.sendEmptyMessageDelayed(INIT_AGAIN, 5000);
        } else {
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), baseStuPair.getStudent().getStudentName(), mLEDManager.getX(baseStuPair.getStudent().getStudentName()), 0, true, false);

        }
        if (isFoul) {
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "犯规", mLEDManager.getX("犯规"), 2, false, true);
        } else {
            String text = result + setUnit();
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), text, mLEDManager.getX(text), 2, false, true);
        }


    }
}
