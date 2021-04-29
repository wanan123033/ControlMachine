package com.feipulai.testandroid.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.VitalCapacityNewResult;
import com.feipulai.device.serial.beans.VitalCapacityResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.testandroid.R;
import com.feipulai.testandroid.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VcPairActivity extends AppCompatActivity implements RadioManager.OnRadioArrivedListener {

    @BindView(R.id.btn_pair)
    TextView btnPair;
    @BindView(R.id.btn_pair_res)
    TextView btnPairRes;
    @BindView(R.id.btn_test)
    TextView btnTest;
    @BindView(R.id.btn_vc)
    TextView btnVc;
    @BindView(R.id.et_value)
    EditText etValue;
    @BindView(R.id.btn_adjust)
    TextView btnAdjust;
    private int currentFrequency;
    private int TARGET_FREQUENCY;
    private int currentDeviceId = 1;
    private final int NO_PAIR_RESPONSE_ARRIVED = 1;
    private final int GET_SCORE_RESPONSE = 2;
    private final int SEND_EMPTY = 3;
    private int VERSION = 363;
    private int check;
    private static final String TAG = "VcPairActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vc_pair);
        MachineCode.machineCode = ItemDefault.CODE_FHL;
        TARGET_FREQUENCY = SerialConfigs.sProChannels.get(ItemDefault.CODE_FHL) + 1 - 1;
        ButterKnife.bind(this);
        RadioManager.getInstance().setOnRadioArrived(this);
        sendEmpty();
    }


    @OnClick({R.id.btn_pair, R.id.btn_test, R.id.btn_adjust})
    public void onClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_pair:
                startPair();
                break;
            case R.id.btn_test:

                if (SerialConfigs.USE_VERSION >= VERSION) {
                    command(1, 0x03);
                } else {
                    cmd(0x06, 1, 0x03);
                }

                break;
            case R.id.btn_adjust:
                String ad = etValue.getText().toString().trim();
                if (TextUtils.isEmpty(ad)) {
                    ToastUtil.show(this, "必须输入校准值");
                    return;
                }
                check = Integer.parseInt(ad);
                if (check<= 0){
                    ToastUtil.show(this, "校准值必须大于0");
                    return;
                }

                if (SerialConfigs.USE_VERSION >= VERSION) {
                    command(1, 0x07);
                } else {
                    cmd(0x0E, 1, 0x07);
                }

                break;
        }
    }

    private void startPair() {
        currentFrequency = 0;
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
        btnPairRes.setText("正在匹配");
    }

    @Override
    public void onRadioArrived(Message msg) {
        int what = msg.what;
        switch (what) {
            case SerialConfigs.VITAL_CAPACITY_SET_MORE_MATCH:
                if (msg.obj instanceof VitalCapacityResult) {
                    VitalCapacityResult fhl = (VitalCapacityResult) msg.obj;
                    checkDevice(fhl);
                } else if (msg.obj instanceof VitalCapacityNewResult) {
                    VitalCapacityNewResult fhl = (VitalCapacityNewResult) msg.obj;
                    checkDevice(fhl);
                }
                break;
            case SerialConfigs.VITAL_CAPACITY_RESULT:
                if (msg.obj instanceof VitalCapacityResult) {
                    final VitalCapacityResult result = (VitalCapacityResult) msg.obj;
                    Log.i(TAG, "子机号:" + result.getDeviceId() + "状态:" + result.getState() + "序号:" + result.getIndex());
                    int index = result.getIndex();
                    switch (index) {
                        case 5:
                            //回应主机查询
                            onResult(result.getDeviceId(), result.getState(), result.getCapacity());
                            if (result.getState() == 4) {//结束
                                //此处不应该直接发结束命令而是交给设备去判断

                                Log.i(TAG, "子机号:" + result.getDeviceId() + "状态:" + result.getState() +
                                        "序号:" + result.getIndex() + "计数:" + result.getCapacity());
                            }
                            break;

                    }
                } else if (msg.obj instanceof VitalCapacityNewResult) {
                    final VitalCapacityNewResult result = (VitalCapacityNewResult) msg.obj;
                    Log.i(TAG, "子机号:" + result.getDeviceId() + "状态:" + result.getState() + "序号:" + result.getIndex());
                    int index = result.getIndex();
                    switch (index) {
                        case 3:
                            //回应主机查询
                            onResult(result.getDeviceId(), result.getState(), result.getCapacity());
                            if (result.getState() == 4) {//结束
                                //此处不应该直接发结束命令而是交给设备去判断
                                Log.i(TAG, "子机号:" + result.getDeviceId() + "状态:" + result.getState() +
                                        "序号:" + result.getIndex() + "计数:" + result.getCapacity());
                            }
                            break;


                    }
                }
        }

    }

    private void onResult(int deviceId, int state, int result) {
        if (result > 0 && state == 4) {
            Message msg = mHandler.obtainMessage();
            msg.what = GET_SCORE_RESPONSE;
            VcWrapper vcWrapper = new VcWrapper(deviceId, result);
            msg.obj = vcWrapper;
            mHandler.sendMessage(msg);
        }
        if (state == 4) {
//                cmd(0x0a, deviceId, 5);
            //设置空闲
            if (SerialConfigs.USE_VERSION >= VERSION) {
                command(deviceId, 0x05);
            } else {
                cmd(0x0a, deviceId, 0x05);
            }
        }
    }

    private void checkDevice(VitalCapacityResult result) {
        checkDevice(result.getDeviceId(), result.getFrequency());
    }

    private void checkDevice(VitalCapacityNewResult result) {
        checkDevice(result.getDeviceId(), result.getFrequency());
    }

    private synchronized void checkDevice(int deviceId, int frequency) {
        if (currentFrequency == 0) {
            // 0频段接收到的结果,肯定是设备的开机广播
            if (frequency == TARGET_FREQUENCY && deviceId == currentDeviceId) {
                setFrequency(currentDeviceId, frequency, TARGET_FREQUENCY);
                onNewDeviceConnect();
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


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case NO_PAIR_RESPONSE_ARRIVED:
                    RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
                    currentFrequency = 0;
                    onNoPairResponseArrived();
                    break;
                case GET_SCORE_RESPONSE:
                    VcWrapper result = (VcWrapper) msg.obj;
                    btnVc.setText(result.getResult() + "ml");
                    break;
                case SEND_EMPTY:
                    sendEmpty();
                    break;
            }
            return false;
        }
    });


    private void setFrequency(int deviceId, int originFrequency, int target_frequency) {
        if (SerialConfigs.USE_VERSION >= VERSION) {
            setFrequencyNewFHL(ItemDefault.CODE_FHL,
                    originFrequency,
                    deviceId,
                    1);
        } else {
            setFrequencyFHL(ItemDefault.CODE_FHL,
                    originFrequency,
                    deviceId,
                    1);
        }
    }


    private void onNoPairResponseArrived() {
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
        currentFrequency = 0;
    }

    private void onNewDeviceConnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnPairRes.setText("匹配成功");
            }
        });

    }


    /**
     * 肺活量旧版一对多
     */
    private void setFrequencyFHL(int machineCode, int originFrequency, int deviceId, int hostId) {
        int targetChannel = 0;
        byte[] buf = new byte[16];
        buf[0] = (byte) 0xAB;
        buf[1] = 0x02;    //包头
        buf[2] = 0X10;       //包长
        buf[3] = 0x02;
        buf[4] = (byte) (deviceId & 0xff);      //设备号
        buf[5] = 0X01;
        targetChannel = SerialConfigs.sProChannels.get(machineCode) + hostId - 1;
        buf[6] = (byte) (targetChannel & 0xff); //高字节在先
        buf[7] = 0x04;    //传输速率

        buf[8] = 0;
        buf[9] = 0;
        buf[10] = 0;
        buf[11] = 0;
        buf[12] = 0;
        buf[13] = 0;
        for (int i = 0; i < 14; i++) {
            buf[14] += buf[i] & 0xff;
        }
        buf[15] = 0x0A;   //包尾
        //Logger.i(StringUtility.bytesToHexString(buf));
        //先切到通信频段
        //Log.i("james","originFrequency:" + originFrequency);
        //RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(originFrequency)));
        //Log.i("james",StringUtility.bytesToHexString(buf));
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, buf));
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(targetChannel)));
    }

    /**
     * 肺活量新版一对多
     */
    private void setFrequencyNewFHL(int machineCode, int originFrequency, int deviceId, int hostId) {
        int targetChannel = 0;
        byte[] buf = new byte[18];
        buf[0] = (byte) 0xAA;
        buf[1] = 0x12;    //包长
        buf[2] = 0X09;    //项目编号
        buf[3] = 0x03;    //目标设备编号（子设备）
        buf[4] = 0x01;    //本设备编号（主机）
        buf[5] = (byte) hostId;//本设备主机号
        targetChannel = SerialConfigs.sProChannels.get(machineCode) + hostId - 1;
        buf[6] = (byte) (deviceId & 0xff); //目标设备子机号
        buf[7] = 0x01;    //命令设置参数

        buf[8] = 0;//目标设备序列号（4字节）
        buf[9] = 0;
        buf[10] = 0;
        buf[11] = 0x00;
        buf[12] = (byte) targetChannel;
        buf[13] = 0x04;
        buf[14] = (byte) hostId;
        buf[15] = (byte) deviceId;
        for (int i = 1; i < 16; i++) {
            buf[16] += buf[i];
        }
        buf[17] = 0x0D;   //包尾
        //Logger.i(StringUtility.bytesToHexString(buf));
        //先切到通信频段
        //Log.i("james","originFrequency:" + originFrequency);
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(originFrequency)));
        //Log.i("james",StringUtility.bytesToHexString(buf));
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, buf));
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(targetChannel)));
    }


    /**
     * 新版本一对多控制命令
     */
    private void command(int deviceId, int cmd) {
        byte[] data = {(byte) 0xAA, 0x12, 0x09, 0x03, 0x01, (byte) 1, (byte) deviceId, (byte) cmd,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x00, 0x0D};
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

    /**
     * @param index    包序
     * @param deviceId 设备号
     */
    private void cmd(int index, int deviceId, int cmd) {
        byte[] data = {(byte) 0xAB, (byte) index, 0x10, 0x02, (byte) deviceId, (byte) cmd, (byte) TARGET_FREQUENCY, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x00, 0x0A};
        if (cmd == 0x07) {
            data[7] = (byte) ((check >> 8) & 0xff);// 次低位
            data[8] = (byte) (check & 0xff);// 最低位
        }
        int sum = 0;
        for (int i = 0; i < data.length - 2; i++) {
            sum += data[i];
        }
        data[14] = (byte) sum;
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, data));

    }


    public void sendEmpty() {
        Log.i("send_james", "empty");
        //主机查询子机
        if (SerialConfigs.USE_VERSION >= VERSION) {
            command(1, 0x02);
        } else {
            cmd(4, 1, 0x02);
        }
        mHandler.sendEmptyMessageDelayed(SEND_EMPTY, 1000);
    }


}
