package com.feipulai.exam.activity.sargent_jump;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.SargentJumpResult;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.exam.activity.medicineBall.TestState;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseGroupTestActivity;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.view.WaitDialog;
import com.orhanobut.logger.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.feipulai.exam.activity.sargent_jump.Constants.CONNECTED;
import static com.feipulai.exam.activity.sargent_jump.Constants.END_TEST;
import static com.feipulai.exam.activity.sargent_jump.Constants.GET_SCORE_RESPONSE;
import static com.feipulai.exam.activity.sargent_jump.Constants.MATCH_SUCCESS;
import static com.feipulai.exam.activity.sargent_jump.Constants.SET_MATCH;
import static com.feipulai.exam.activity.sargent_jump.Constants.UN_CONNECT;

public class SargentGroupActivity extends BaseGroupTestActivity {

    private static final String TAG = "SargentGroupActivity";
    private SargentSetting sargentSetting;
    private SerialDeviceManager mSerialManager;

    private volatile int check = 0;
    private boolean isConnect;
    private ScheduledExecutorService checkService = Executors.newSingleThreadScheduledExecutor();
    private TestState testState = TestState.UN_STARTED;
    //保存当前测试考生
    private BaseStuPair baseStuPair;
    private RadioManager radioManager;
    private boolean isSetBase = false;
    private int frequency;//需设定的主机频段
    private int currentFrequency;//当前主机频段
    private WaitDialog changBadDialog;
    private boolean isAddTool;

    @Override
    public void initData() {
        sargentSetting = SharedPrefsUtil.loadFormSource(this, SargentSetting.class);
        if (null == sargentSetting) {
            sargentSetting = new SargentSetting();
        }
        Logger.i(TAG + ":sargentSetting ->" + sargentSetting.toString());

        if (sargentSetting.getType() == 0){
            mSerialManager = SerialDeviceManager.getInstance();
            mSerialManager.setRS232ResiltListener(resultImpl);
        }else {
            radioManager = RadioManager.getInstance();
            radioManager.init();
            radioManager.setOnRadioArrived(resultImpl);
        }

        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
        runUp = sargentSetting.getRunUp();
        // 0 显示 原地起跳 1 隐藏 助跑
        setBaseHeightVisible(runUp);
        if (runUp == 0) {
            setBaseHeight(0);
        }

        sendEmpty();

        if (sargentSetting.getType() == 1 && mBaseToolbar != null) {
            if (!isAddTool){
                isAddTool = true ;
                mBaseToolbar.addRightText("设备配对", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        radioManager.sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
                        currentFrequency = 0 ;
                        showChangeBadDialog();
                    }
                });
            }

        }
        frequency = SerialConfigs.sProChannels.get(ItemDefault.CODE_MG) + SettingHelper.getSystemSetting().getHostId() - 1;
    }

    public void sendEmpty() {
        checkService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                check++;
                if (sargentSetting.getType() == 0){
                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                            SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
                }else {
                    radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                            SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
                }

                if (check > 2) {
                    // 失去连接
                    check = 0;
                    isConnect = false;
                    mHandler.sendEmptyMessage(UN_CONNECT);
                }
            }
        }, 1000, 3000, TimeUnit.MILLISECONDS);

    }

    @Override
    public int setTestCount() {
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            return TestConfigs.sCurrentItem.getTestNum();
        } else {
            return sargentSetting.getTestTimes();
        }
    }

    @Override
    public void gotoItemSetting() {
        startActivity(new Intent(this, SargentSettingActivity.class));
    }

    @Override
    public void startTest(BaseStuPair stuPair) {
        baseStuPair = stuPair;
        if (sargentSetting.getType() == 0){
            mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                    SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
        }else {
            radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                    SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
        }

        testState = TestState.UN_STARTED;
        BaseDeviceState baseDevice = baseStuPair.getBaseDevice();
        if (baseDevice != null)
            baseDevice.setState(BaseDeviceState.STATE_FREE);
        else
            baseDevice = new BaseDeviceState(BaseDeviceState.STATE_FREE, 1);
        baseStuPair.setBaseDevice(baseDevice);
        updateDevice(baseDevice);
        if (isConnect) {
            if (! isSetBase) {
                if (sargentSetting.getType() == 0){
                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                            SerialConfigs.CMD_SARGENT_JUMP_GET_SET_0(sargentSetting.getBaseHeight())));
                }else {
                    radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                            SerialConfigs.CMD_SARGENT_JUMP_GET_SET_0(sargentSetting.getBaseHeight())));
                }
                isSetBase = true;
            }

            if (sargentSetting.getType() == 0){
                mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                        SerialConfigs.CMD_SARGENT_JUMP_START));
            }else {
                radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                        SerialConfigs.CMD_SARGENT_JUMP_START));
            }

        }
        testState = TestState.WAIT_RESULT;
    }

    @Override
    public int setTestPattern() {
        return sargentSetting.getTestPattern();
    }


    private SargentJumpImpl resultImpl = new SargentJumpImpl(new SargentJumpImpl.SargentJumpListener() {
        @Override
        public void onResultArrived(SargentJumpResult result) {
            Message msg = mHandler.obtainMessage();
            msg.obj = result;
            msg.what = GET_SCORE_RESPONSE;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onStopTest() {
            mHandler.sendEmptyMessage(END_TEST);
        }

        @Override
        public void onSelfCheck() {

        }

        @Override
        public void onFree() {
            check = 0;
            if (!isConnect) {
                isConnect = true;
                //修改设备状态为连接
                mHandler.sendEmptyMessage(Constants.CONNECTED);
            }
        }

        @Override
        public void onMatch(SargentJumpResult match) {
            int fre = match.getFrequency();
            if (currentFrequency != frequency) {
                radioManager.sendCommand(new ConvertCommand(new RadioChannelCommand(fre)));
                mHandler.sendEmptyMessageDelayed(SET_MATCH, 600);
                currentFrequency = fre;
            } else {
                mHandler.sendEmptyMessage(MATCH_SUCCESS);
            }
        }
    });

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SET_MATCH:
                    byte[] cmd = SerialConfigs.CMD_SARGENT_JUMP_SET_MATCH;
                    cmd[4] = (byte) 1;
//                    cmd[8] = 42;

                    cmd[8] = (byte) (frequency);
                    cmd[13] = (byte) (sum(cmd) & 0xff);
                    Log.i("match", "cmd:" + StringUtility.bytesToHexString(cmd));
                    radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
                    radioManager.sendCommand(new ConvertCommand(new RadioChannelCommand(frequency)));
                    currentFrequency = frequency;
                case MATCH_SUCCESS:
                    ToastUtils.showShort("配对成功");
                    if (changBadDialog!= null && changBadDialog.isShowing())
                        changBadDialog.dismiss();
                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
                    break;
                case UN_CONNECT:
                    //更新设备状态
                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
                    break;
                case CONNECTED:
                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
                    break;
                case GET_SCORE_RESPONSE:

                    SargentJumpResult result = (SargentJumpResult) msg.obj;
                    if (runUp == 0 && baseHeight == 0) {
                        //标记原始高度
                        baseHeight = result.getScore();
                        setBaseHeight(baseHeight);
                        setBegin();
                    } else {
                        int dbResult = result.getScore() * 10;
                        //原地起跳高度
                        if (runUp == 0) {
                            dbResult = result.getScore() * 10 - baseHeight * 10;
                            if (dbResult > 0) {
                                BaseStuPair basePair = new BaseStuPair();
                                onResultArrived(dbResult, basePair);
                            }

                        } else {
                            BaseStuPair basePair = new BaseStuPair();
                            onResultArrived(dbResult, basePair);
                        }
                    }

                    break;
                case END_TEST:
                    BaseDeviceState device1 = new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1);
                    updateDevice(device1);
                    toastSpeak("测试结束");
                    break;
            }
            return false;
        }
    });

    private void onResultArrived(int result, BaseStuPair stuPair) {
        if (testState == TestState.WAIT_RESULT) {
            if (sargentSetting.isFullReturn()) {
                if (baseStuPair.getStudent().getSex() == Student.MALE) {
                    stuPair.setFullMark(result >= Integer.parseInt(sargentSetting.getMaleFull()) * 10);
                } else {
                    stuPair.setFullMark(result >= Integer.parseInt(sargentSetting.getFemaleFull()) * 10);
                }
            }
            stuPair.setResult(result);
            stuPair.setResultState(RoundResult.RESULT_STATE_NORMAL);
            updateTestResult(stuPair);
            updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END, 1));
            // 发送结束命令
            if (sargentSetting.getType() == 0){
                mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SARGENT_JUMP_STOP));
            }else {
                radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, SerialConfigs.CMD_SARGENT_JUMP_STOP));
            }
//            testState = TestState.UN_STARTED;
        }
    }

    private int sum(byte[] cmd) {
        int sum = 0;
        for (int i = 2; i <= 12; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (checkService!= null){
            checkService.shutdown();
        }
    }

    public void showChangeBadDialog() {
        changBadDialog = new WaitDialog(this);
        changBadDialog.setCanceledOnTouchOutside(false);
        changBadDialog.setCancelable(false);
        changBadDialog.show();
        // 必须在dialog显示出来后再调用
        changBadDialog.setTitle("请重启待连接设备");
        changBadDialog.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changBadDialog.dismiss();
            }
        });
    }
}
