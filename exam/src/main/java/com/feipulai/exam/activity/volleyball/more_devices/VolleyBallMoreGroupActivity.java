package com.feipulai.exam.activity.volleyball.more_devices;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.VolleyPair868Result;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.more_device.BaseMoreGroupActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;

import butterknife.OnClick;

public class VolleyBallMoreGroupActivity extends BaseMoreGroupActivity {
    private static final int SEND_EMPTY = 1;
    private static final int END_JS = 2;
    private static final int GET_STATE = 3;
    private static final int START_STATE = 5;

    @Override
    public int setTestDeviceCount() {
        return 0;
    }

    private VolleyBallSetting volleyBallSetting;
    private int[] deviceState = {};
    private final int TARGET_FREQUENCY = SettingHelper.getSystemSetting().getUseChannel();

    private VolleyBallJumpImpl resultImpl = new VolleyBallJumpImpl(new VolleyBallJumpImpl.VolleyBallCallBack() {
        @Override
        public void getState(VolleyPair868Result obj) {
            Message msg = Message.obtain();
            msg.what = GET_STATE;
            msg.obj = obj;
            mHandler.sendMessage(msg);
        }
    });

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        builder.setTitle("排球垫球");
        return builder;
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case GET_STATE:
                    byte[] dataArr = ((VolleyPair868Result) msg.obj).getDataArr();
                    int deviceid = dataArr[5];
                    int childid = dataArr[6];
                    int state = dataArr[12];
                    //重置机器状态
                    for (int i = 0; i < deviceDetails.size(); i++) {
                        BaseDeviceState baseDevice = deviceDetails.get(i).getStuDevicePair().getBaseDevice();
                        if (childid == baseDevice.getDeviceId() && deviceid == SettingHelper.getSystemSetting().getHostId()) {
                            if (state == 0x01 || state == 0x11 || state == 0x00) {
                                baseDevice.setState(BaseDeviceState.STATE_NOT_BEGAIN);
                            } else if (state == 0x02 || state == 0x12) {
                                baseDevice.setState(BaseDeviceState.STATE_ONUSE);
                            } else if (state == 0x03 || state == 0x13) {
                                baseDevice.setState(BaseDeviceState.STATE_END);
                            }
                            baseDevice.setBatteryLeft(dataArr[15]);
                            deviceDetails.get(i).getStuDevicePair().setResult(dataArr[13] * 0x0100 + dataArr[14]);
                            updateDevice(baseDevice);
                        }
                    }
                    break;
                case SEND_EMPTY:
                    sendEmpty();
                    break;
                case END_JS:
                    sendEnd((int) msg.obj);
                    break;
            }
            return false;
        }
    });

    @Override
    protected void initData() {
        super.initData();
        volleyBallSetting = SharedPrefsUtil.loadFormSource(this, VolleyBallSetting.class);
        if (null == volleyBallSetting) {
            volleyBallSetting = new VolleyBallSetting();
        }
        setDeviceCount(volleyBallSetting.getSpDeviceCount());
        deviceState = new int[volleyBallSetting.getSpDeviceCount()];
        for (int i = 0; i < deviceState.length; i++) {
            deviceState[i] = 0;//连续5次检测不到认为掉线
        }

        RadioManager.getInstance().setOnRadioArrived(resultImpl);
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(TARGET_FREQUENCY)));

        sendEmpty();
    }

    @OnClick({R.id.txt_device_pair})
    public void onClick(View view) {
        startActivity(new Intent(this, VolleyBallPairActivity.class));
    }

    private void sendEmpty() {
        getState();
        mHandler.sendEmptyMessageDelayed(SEND_EMPTY, 5000);
    }

    /**
     * 开始计数
     *
     * @param deviceId
     */
    private void sendStart(byte deviceId) {
        byte[] cmd = {(byte) 0xAA, 0x0F, 0x0A, 0x03, 0x01, 0x00, 0x00, (byte) 0xC5, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x0D};
        cmd[5] = (byte) SettingHelper.getSystemSetting().getHostId();
        cmd[6] = deviceId;
        cmd[13] = (byte) sum(cmd, 13);

        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
    }

    /**
     * 停止计数
     *
     * @param deviceId
     */
    private void sendEnd(int deviceId) {
        byte[] cmd = {(byte) 0xAA, 0x0F, 0x0A, 0x03, 0x01, 0x00, 0x00, (byte) 0xC5, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D};
        cmd[5] = (byte) SettingHelper.getSystemSetting().getHostId();
        cmd[6] = (byte) deviceId;
        cmd[13] = (byte) sum(cmd, 13);

        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
        sendEnd(deviceId);
    }


    private void getState() {
        byte[] cmd = {(byte) 0xAA, 0x0e, 0x0a, 0x03, 0x01, 0x00, 0x00, (byte) 0xC3, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d};
        for (int i = 0; i < deviceDetails.size(); i++) {
            BaseDeviceState device = deviceDetails.get(i).getStuDevicePair().getBaseDevice();
            int deviceId = device.getDeviceId();
            int hostId = SettingHelper.getSystemSetting().getHostId();
            cmd[5] = (byte) hostId;
            cmd[6] = (byte) deviceId;
            cmd[12] = (byte) sum(cmd, 12);

            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));

        }
    }

    private int sum(byte[] cmd, int index) {
        int sum = 0;
        for (int i = 2; i < index; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }

    @Override
    public void toStart(int pos) {
        BaseStuPair pair = deviceDetails.get(pos).getStuDevicePair();
        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
        int id = pair.getBaseDevice().getDeviceId();
        sendStart((byte) id);

        Message msg1 = Message.obtain();
        msg1.what = END_JS;
        msg1.obj = id;
        mHandler.sendMessageDelayed(msg1, 1000 * 60);
    }

    @Override
    public int setTestCount() {
        return 1;
    }

    @Override
    public int setTestPattern() {
        return 0;
    }
}
