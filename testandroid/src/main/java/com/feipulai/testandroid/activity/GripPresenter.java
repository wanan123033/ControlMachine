package com.feipulai.testandroid.activity;

import android.os.Handler;
import android.os.Message;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.testandroid.base.BasePresenter;

/**
 * Created by pengjf on 2020/1/15.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class GripPresenter extends BasePresenter implements GripContract.Presenter {
    private final int SEND_EMPTY = 1;
    private final int GET_SCORE_RESPONSE = 2;
    int hostId = 1;
    private int frequency = 109;
    GripPresenter() {
        RadioManager.getInstance().setOnRadioArrived(resultImpl);
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(frequency)));
    }

    @Override
    public void sendEmpty() {
        command(1, 0x02);
        mHandler.sendEmptyMessageDelayed(SEND_EMPTY, 1000);
    }

    @Override
    public void deviceMatch() {

    }

    @Override
    public void test() {

    }

    @Override
    public void verify() {

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
    });

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

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case GET_SCORE_RESPONSE:

                    break;
                case SEND_EMPTY:
                    sendEmpty();
                    break;
            }

            return false;
        }
    });

}
