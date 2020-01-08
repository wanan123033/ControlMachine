package com.feipulai.host.activity.vccheck;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseMoreActivity;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.entity.DeviceDetail;
import com.feipulai.host.entity.Item;
import com.feipulai.host.entity.RoundResult;

/**
 * Created by pengjf on 2019/10/8.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class VitalTestActivity extends BaseMoreActivity {

    private static final String TAG = "VitalTestActivity";
    private int[] deviceState = new int[4];
    private int[] powerState = new int[4];
    private int[] tempPower = {-1,-1,-1,-1};
    private final int SEND_EMPTY = 1;
    private final int GET_SCORE_RESPONSE = 2;
    int hostId = SettingHelper.getSystemSetting().getHostId();
    private int frequency = SettingHelper.getSystemSetting().getUseChannel();;
    //    private int velocity = SerialConfigs.VITAL_VELOCITY;
    private int VERSION = 363;

    @Override
    protected void initData() {
        super.initData();
        for (int i = 0; i < deviceState.length; i++) {

            deviceState[i] = 0;//连续5次检测不到认为掉线
        }
        getToolbar().getRightView(0).setVisibility(View.GONE);
        getToolbar().getRightView(1).setVisibility(View.GONE);
    }

    @Override
    public void gotoItemSetting() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        RadioManager.getInstance().setOnRadioArrived(resultImpl);
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(frequency)));

        sendEmpty();
    }

    @Override
    public void sendTestCommand(BaseStuPair pair, int index) {
        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
        updateDevice(pair.getBaseDevice());
        int id = pair.getBaseDevice().getDeviceId();
        sendStart((byte) id);
    }


    private void sendStart(byte id) {

        if (SerialConfigs.USE_VERSION >= VERSION) {
            command(id, 0x03);
        } else {
            cmd(0x06, id, 0x03);
        }
    }

    /**
     * @param index    包序
     * @param deviceId 设备号
     */
    private void cmd(int index, int deviceId, int cmd) {
        byte[] data = {(byte) 0xAB, (byte) index, 0x10, 0x02, (byte) deviceId, (byte) cmd, (byte) frequency, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x00, 0x0A};
        int sum = 0;
        for (int i = 0; i < data.length - 2; i++) {
            sum += data[i];
        }
        data[14] = (byte) sum;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));

    }

    /**
     * 新版本一对多控制命令
     */
    private void command(int deviceId, int cmd) {
        byte[] data = {(byte) 0xAA, 0x12, 0x09, 0x03, 0x01, (byte) hostId, (byte) deviceId, (byte) cmd,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x00, 0x0D};

        if (MachineCode.machineCode == ItemDefault.CODE_WLJ){
            data[2] = 0x0c;
        }else {
            data[2] = 0x09;
        }
        int sum = 0;
        for (int i = 1; i < data.length - 2; i++) {
            sum += data[i];
        }
        data[16] = (byte) sum;

        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
    }

    public void sendEmpty() {
        Log.i(TAG, "send_empty");
        for (int i = 0; i < deviceState.length; i++) {
            BaseDeviceState baseDevice = deviceDetails.get(i).getStuDevicePair().getBaseDevice();
            if (deviceState[i] == 0) {
                if (baseDevice.getState() != BaseDeviceState.STATE_ERROR) {
                    baseDevice.setState(BaseDeviceState.STATE_ERROR);
                    updateDevice(baseDevice);
                }

            } else {
                if (baseDevice.getState() == BaseDeviceState.STATE_ERROR) {
                    baseDevice.setState(BaseDeviceState.STATE_NOT_BEGAIN);
                    updateDevice(baseDevice);
                }
                deviceState[i] -= 1;

                if (tempPower[i] != powerState[i]){
                    deviceDetails.get(i).getStuDevicePair().setPower(powerState[i]);
                    updateDevice(baseDevice);
                    tempPower[i] = powerState[i];
                }
            }


        }
        for (DeviceDetail detail : deviceDetails) {
            //主机查询子机
            if (SerialConfigs.USE_VERSION >= VERSION) {
                command(detail.getStuDevicePair().getBaseDevice().getDeviceId(), 0x02);
            } else {
                cmd(4, detail.getStuDevicePair().getBaseDevice().getDeviceId(), 0x02);
            }
        }
        mHandler.sendEmptyMessageDelayed(SEND_EMPTY, 1000);


    }


    private WirelessVitalListener resultImpl = new WirelessVitalListener(new WirelessVitalListener.WirelessListener() {
        @Override
        public void onResult(int deviceId, int state, int result,int power) {
            deviceState[deviceId - 1] = 5;
            powerState[deviceId-1] = power;
            if (result > 0 && state == 4) {
                Message msg = mHandler.obtainMessage();
                msg.what = GET_SCORE_RESPONSE;
                VcWrapper vcWrapper = new VcWrapper(deviceId, result);
                msg.obj = vcWrapper;
                mHandler.sendMessage(msg);
            }
            if (state != 2 && state != 3) {
//                cmd(0x0a, deviceId, 5);
                //设置空闲
                if (SerialConfigs.USE_VERSION >= VERSION) {
                    command(deviceId, 0x05);
                } else {
                    cmd(0x0a, deviceId, 0x05);
                }
            }
        }

        @Override
        public void onStop() {
//            cmd(8, result.getDeviceId(),4 );
        }
    });

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case GET_SCORE_RESPONSE:
                    VcWrapper result = (VcWrapper) msg.obj;
                    for (DeviceDetail detail : deviceDetails) {
                        if (detail.getStuDevicePair().getBaseDevice().getDeviceId() == result.getDeviceId()) {
                            onResultArrived(result.getResult(), detail.getStuDevicePair());
                        }

                    }
                    break;
                case SEND_EMPTY:
                    sendEmpty();
                    break;
            }

            return false;
        }
    });

    private void onResultArrived(int result, BaseStuPair stuPair) {
        if (MachineCode.machineCode == ItemDefault.CODE_WLJ){
            result = result*100;
        }
        stuPair.setResult(result);
        stuPair.setResultState(RoundResult.RESULT_STATE_NORMAL);
        updateResult(stuPair);
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END, stuPair.getBaseDevice().getDeviceId()));

    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
    }
}
