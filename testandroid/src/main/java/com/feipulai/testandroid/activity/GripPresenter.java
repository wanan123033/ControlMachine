package com.feipulai.testandroid.activity;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.GripManager;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.VitalCapacityNewResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.testandroid.base.BasePresenter;

/**
 * Created by pengjf on 2020/1/15.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class GripPresenter extends BasePresenter implements GripContract.Presenter {
    private static final int SEND_EMPTY = 1;
    private static final int GET_SCORE_RESPONSE = 2;
    public static final int NO_PAIR_RESPONSE_ARRIVED = 3;
    public static final int PAIR_RESPONSE_ARRIVED = 4;
    int hostId = 1;
    private int currentFrequency;
    private int TARGET_FREQUENCY;
    private GripContract.View view;
    public int currentDeviceId;
    private final GripManager gripManager;
    private int check;
    GripPresenter(int targetFrequency,GripContract.View view) {
        currentDeviceId = 1;
        this.view = view;
        TARGET_FREQUENCY = targetFrequency;
        RadioManager.getInstance().setOnRadioArrived(resultImpl);
        gripManager = new GripManager();
//        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(targetFrequency)));
    }

    @Override
    public void sendEmpty() {
        command(1, 0x02);
        mHandler.sendEmptyMessageDelayed(SEND_EMPTY, 2000);
    }

    @Override
    public void deviceMatch() {
        currentFrequency = 0;
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
        view.showLoading();
    }

    @Override
    public void test() {
        command(1, 0x03);
    }

    @Override
    public void verify(int check) {
        this.check = check;
        command(1, 0x07);
    }

    private WirelessVitalListener resultImpl = new WirelessVitalListener(new WirelessVitalListener.WirelessListener() {
        @Override
        public void onResult(int deviceId, int state, int result, int power) {

            if (result > 0 && state == 4) {
                Message msg = mHandler.obtainMessage();
                msg.what = GET_SCORE_RESPONSE;
                VcWrapper vcWrapper = new VcWrapper(deviceId, result);
                msg.obj = vcWrapper;
                mHandler.sendMessage(msg);
            }
            if (state != 2 && state != 3) {
                //设置空闲
                command(deviceId, 0x05);
            }
        }

        @Override
        public void onStop() {
        }

        @Override
        public void checkDevice(VitalCapacityNewResult result) {
            checkPair(result.getDeviceId(), result.getFrequency());
        }
    });

    //验证配对
    private void checkPair(int deviceId, int frequency) {
        Log.e("TAG115----","currentFrequency = "+currentFrequency+",frequency="+frequency+",deviceId="+deviceId+",currentDeviceId="+currentDeviceId);
        if (currentFrequency == 0) {
            // 0频段接收到的结果,肯定是设备的开机广播
            if (frequency == TARGET_FREQUENCY && deviceId == currentDeviceId) {
                onNewDeviceConnect();
                setFrequency(currentDeviceId, frequency, TARGET_FREQUENCY);
            } else {
                setFrequency(currentDeviceId, frequency, TARGET_FREQUENCY);
                currentFrequency = TARGET_FREQUENCY;
                // 那个铁盒子就是有可能等这么久才收到回复
                mHandler.sendEmptyMessageDelayed(NO_PAIR_RESPONSE_ARRIVED, 5000);
            }
        } else if (currentFrequency == TARGET_FREQUENCY) {
            //在主机的目的频段收到的,肯定是设置频段后收到的设备广播
            if (deviceId == currentDeviceId && frequency == TARGET_FREQUENCY) {
                onNewDeviceConnect();
            }
        }
    }

    private void setFrequency(int currentDeviceId, int frequency, int target_frequency) {
        gripManager.setGrip(target_frequency,1,1);
    }

    public synchronized void onNewDeviceConnect() {
        mHandler.removeMessages(NO_PAIR_RESPONSE_ARRIVED);
        mHandler.sendEmptyMessage(PAIR_RESPONSE_ARRIVED);
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
        if (cmd == 0x07) {
            data[12] = (byte) ((check >> 8) & 0xff);// 高位
            data[13] = (byte) (check & 0xff);// 低位
        }

        int sum = 0;
        for (int i = 1; i < data.length - 2; i++) {
            sum += data[i];
        }
        data[16] = (byte) sum;

        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));
    }

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case GET_SCORE_RESPONSE:
                    VcWrapper result = (VcWrapper) msg.obj;
                    view.setResult((result.getResult()*100));
                    break;
                case SEND_EMPTY:
                    sendEmpty();
                    break;
                case NO_PAIR_RESPONSE_ARRIVED:
                    RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
                    currentFrequency = 0;
                    view.onError("配对失败");
                    break;
                case PAIR_RESPONSE_ARRIVED:
                    view.hideLoading();
                    break;
            }

            return false;
        }
    });

    @Override
    public void detachView() {
        super.detachView();
        mHandler.removeCallbacksAndMessages(null);
    }
}
