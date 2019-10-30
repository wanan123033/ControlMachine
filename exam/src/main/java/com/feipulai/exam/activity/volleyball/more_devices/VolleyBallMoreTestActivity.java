package com.feipulai.exam.activity.volleyball.more_devices;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.VolleyPair868Result;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.more_device.BaseMoreActivity;
import com.feipulai.exam.activity.sargent_jump.pair.VolleyBallPairActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;
import com.feipulai.exam.activity.volleyball.VolleyBallSettingActivity;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

public class VolleyBallMoreTestActivity extends BaseMoreActivity {

    private static final int SEND_EMPTY = 1;
    private static final int END_JS = 2;
    private static final int GET_STATE = 3;
    private static final int START_STATE = 5;

    private final int TIME_DURATION = 1000*60;   //ms(毫秒)  计时时长

    private String[] scoreStr = new String[3];
    private VolleyBallJumpImpl resultJump = new VolleyBallJumpImpl(new VolleyBallJumpImpl.VolleyBallCallBack() {
        @Override
        public void getState(VolleyPair868Result obj) {
            Message msg = Message.obtain();
            msg.what = GET_STATE;
            msg.obj = obj;
            mHandler.sendMessage(msg);

        }

        @Override
        public void begin(VolleyPair868Result obj) {
            Message msg = Message.obtain();
            msg.what = START_STATE;
            msg.obj = obj;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onError(byte gan, byte[] bytes) {

        }
    });
    private int[] deviceState = {};
    private VolleyBallSetting volleyBallSetting;

    private final int TARGET_FREQUENCY = SerialConfigs.sProChannels.get(TestConfigs.sCurrentItem.getMachineCode()) + SettingHelper.getSystemSetting().getHostId() - 1;

    private int runUp;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case SEND_EMPTY:
                    sendEmpty();
                    break;
                case END_JS:
                    sendEnd1((int) msg.obj);
                    break;
                case GET_STATE:
                    VolleyPair868Result result = ((VolleyPair868Result)msg.obj);
                    int deviceid = result.getDeviceid();
                    int childid = result.getChildId();
                    int state = result.getState();
                    //重置机器状态
                    for (int i = 0 ; i < deviceDetails.size() ; i++){
                        BaseStuPair stuDevicePair = deviceDetails.get(i).getStuDevicePair();
                        if (childid == stuDevicePair.getBaseDevice().getDeviceId() && deviceid == SettingHelper.getSystemSetting().getHostId()){
                            if (state == VolleyPair868Result.STATE_TIME_PREPARE || state == VolleyPair868Result.STATE_COUNT_PREPARE || state == VolleyPair868Result.STATE_FREE){
                                stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
                                scoreStr[1] = "设备空闲";
                            }else if (state == VolleyPair868Result.STATE_TIMING || state == VolleyPair868Result.STATE_COUNTING){
                                stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
                                scoreStr[1] = "计数中";
                            }else if (state == VolleyPair868Result.STATE_TIME_END || state == VolleyPair868Result.STATE_COUNT_END){
                                stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_END);
                                scoreStr[1] = "计数已结束";
                            }
                            stuDevicePair.getBaseDevice().setBatteryLeft(result.getElectricityState());
                            scoreStr[0] = ResultDisplayUtils.getStrResultForDisplay(result.getScore());
                            scoreStr[2] = "";
                            stuDevicePair.setTimeResult(scoreStr);
                            updateResult(stuDevicePair);
//                            updateDevice(stuDevicePair.getBaseDevice());
                            break;
                        }
                    }
                    break;
                case START_STATE:
                    for (int i = 0 ; i < deviceIds.size() ; i++){
                        deviceIds.get(i).setState(BaseDeviceState.STATE_ONUSE);
                        updateDevice(deviceIds.get(i));
                        sendStart((byte) deviceIds.get(i).getDeviceId());

                        Message msg1 = Message.obtain();
                        msg1.what = END_JS;
                        msg1.obj = deviceIds.get(i).getDeviceId();
                        mHandler.sendMessageDelayed(msg1,volleyBallSetting.getTestTime() * 1000);

                    }
                    deviceIds.clear();
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onResume() {
        super.onResume();

        volleyBallSetting = SharedPrefsUtil.loadFormSource(this, VolleyBallSetting.class);
        if (null == volleyBallSetting) {
            volleyBallSetting = new VolleyBallSetting();
        }
        setDeviceCount(volleyBallSetting.getSpDeviceCount());
        deviceState = new int[volleyBallSetting.getSpDeviceCount()];
        for (int i = 0; i < deviceState.length; i++) {
            deviceState[i] = 0;//连续5次检测不到认为掉线
        }

        runUp = volleyBallSetting.getRunUp();
        RadioManager.getInstance().setOnRadioArrived(resultJump);
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(TARGET_FREQUENCY)));
        sendEmpty();
    }

    private void sendEmpty() {
        getState();
        mHandler.sendEmptyMessageDelayed(SEND_EMPTY, 1000);
    }

    private void getState() {
        byte[] cmd = {(byte) 0xAA,0x0e,0x0a,0x03,0x01,0x00,0x00, (byte) 0xC3,0x00,0x00,0x00,0x00,0x00,0x0d};
        for (int i = 0 ; i < deviceDetails.size() ; i++) {
            BaseDeviceState device = deviceDetails.get(i).getStuDevicePair().getBaseDevice();
            int deviceId = device.getDeviceId();
            int hostId = SettingHelper.getSystemSetting().getHostId();
            cmd[5] = (byte) hostId;
            cmd[6] = (byte) deviceId;
            cmd[12] = (byte) sum(cmd,12);

            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));

        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 设备空闲
     */
    private void sendEnd(int deviceId) {
        byte[] cmd = {(byte) 0xAA,0x0e,0x0A,0x03,0x01,0x00,0x00, (byte) 0xC6,0x00,0x00,0x00,0x00,0x00,0x0d};
        int hostId = SettingHelper.getSystemSetting().getHostId();
        cmd[5] = (byte) hostId;
        cmd[6] = (byte) deviceId;
        cmd[12] = (byte) sum(cmd,12);

        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));


        stuSkipDialog(deviceDetails.get(deviceId - 1).getStuDevicePair().getStudent(), deviceId - 1);
    }

    private int sum(byte[] cmd, int index) {
        int sum = 0;
        for (int i = 2; i < index; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }
    @OnClick({R.id.tv_device_pair})
    public void onClick(View view){
        startActivity(new Intent(this, VolleyBallPairActivity.class));
    }
    @Override
    public int setTestCount() {
        return 2;
    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
        return false;
    }

    @Override
    public void gotoItemSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择");
        builder.setItems(new String[]{"一号机", "二号机", "三号机", "四号机"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), VolleyBallSettingActivity.class);
                intent.putExtra("deviceId",which + 1);
                startActivity(intent);
            }
        });
        builder.create().show();

    }
    List<BaseDeviceState> deviceIds = new ArrayList<>();
    @Override
    protected void sendTestCommand(BaseStuPair pair, int index) {
        Log.e("TAG","---------"+pair.toString());
//        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
        int id = pair.getBaseDevice().getDeviceId();
//        selfCheck((byte) id);
        deviceIds.add(pair.getBaseDevice());

        mHandler.sendEmptyMessage(START_STATE);
    }

    /**
     *开始计数
     * @param deviceId
     */
    private void sendStart(byte deviceId) {
        byte[] cmd = {(byte) 0xAA,0x0F,0x0A,0x03,0x01,0x00,0x00, (byte) 0xC5,0x00,0x00,0x00,0x00,0x01,0x00,0x0D};
        cmd[5] = (byte) SettingHelper.getSystemSetting().getHostId();
        cmd[6] = deviceId;
        cmd[13] = (byte) sum(cmd,13);

        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,cmd));
    }

    /**
     * 停止计数
     * @param deviceId
     */
    private void sendEnd1(int deviceId){
        byte[] cmd = {(byte) 0xAA,0x0F,0x0A,0x03,0x01,0x00,0x00, (byte) 0xC5,0x00,0x00,0x00,0x00,0x00,0x00,0x0D};
        cmd[5] = (byte) SettingHelper.getSystemSetting().getHostId();
        cmd[6] = (byte) deviceId;
        cmd[13] = (byte) sum(cmd,13);

        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,cmd));
        SystemClock.sleep(100);
        sendEnd(deviceId);
    }

    /**
     * 自检设备
     * @param deviceId
     */
    private void selfCheck(byte deviceId){
        byte[] cmd = {(byte) 0xAA,0x0E,0x0A,0x03,0x01,0x00,0x00, (byte) 0xC7,0x00,0x00,0x00,0x00,0x00,0x0d};
        cmd[5] = (byte) SettingHelper.getSystemSetting().getHostId();
        cmd[6] = deviceId;
        cmd[12] = (byte) sum(cmd,12);

        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,cmd));
    }
}
