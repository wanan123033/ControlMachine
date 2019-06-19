package com.feipulai.exam.activity.sargent_jump;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.SargentJumpResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.exam.activity.medicineBall.TestState;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BasePersonTestActivity;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.Student;
import com.orhanobut.logger.Logger;

import org.openxmlformats.schemas.drawingml.x2006.main.impl.STLineWidthImpl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.feipulai.exam.activity.sargent_jump.Constants.END_TEST;
import static com.feipulai.exam.activity.sargent_jump.Constants.GET_SCORE_RESPONSE;
import static com.feipulai.exam.activity.sargent_jump.Constants.CONNECTED;
import static com.feipulai.exam.activity.sargent_jump.Constants.UN_CONNECT;

public class SargentTestActivity extends BasePersonTestActivity {
    private static final String TAG = "SargentTestActivity";
    private SargentSetting sargentSetting;
    private ScheduledExecutorService checkService;
    private SerialDeviceManager mSerialManager;
    private TestState testState = TestState.UN_STARTED;
    private volatile int check = 0;
    private boolean isConnect;
    private RadioManager radioManager;

    private void init() {
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
    }

    @Override
    protected void initData() {
        init();
        sargentSetting = SharedPrefsUtil.loadFormSource(this, SargentSetting.class);
        if (null == sargentSetting) {
            sargentSetting = new SargentSetting();
        }
        if (sargentSetting.getType() == 0) {
            mSerialManager = SerialDeviceManager.getInstance();
            mSerialManager.setRS232ResiltListener(resultImpl);
        } else {
            radioManager = RadioManager.getInstance();
            radioManager.init();
            radioManager.setOnRadioArrived(resultImpl);
        }

        Logger.i(TAG + ":sargentSetting ->" + sargentSetting.toString());
        runUp = sargentSetting.getRunUp();
        // 0 显示 原地起跳 1 隐藏 助跑
        setBaseHeightVisible(runUp);
        if (sargentSetting.getType() == 0) {
            mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                    SerialConfigs.CMD_SARGENT_JUMP_GET_SET_0(sargentSetting.getBaseHeight())));
        }
        sendEmpty();
    }

    public void sendEmpty() {
        checkService = Executors.newSingleThreadScheduledExecutor();
        checkService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                check++;
                if (sargentSetting.getType() == 0) {
                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                            SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
                }else {
                    radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
                }
                if (check > 3) {
                    // 失去连接
                    check = 0;
                    isConnect = false;
                    mHandler.sendEmptyMessage(UN_CONNECT);
                }
            }
        }, 1000, 3000, TimeUnit.MILLISECONDS);

    }

    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {
        if (baseStuPair.getStudent() == null) {
            toastSpeak("请先添加学生");
            return;
        }
        testState = TestState.UN_STARTED;
        if (sargentSetting.getType() == 0){
            mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                    SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
        }else {
            radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                    SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
        }

        //更新设备状态
        BaseDeviceState baseDevice = baseStuPair.getBaseDevice();
        if (baseDevice != null)
            baseDevice.setState(BaseDeviceState.STATE_FREE);
        else
            baseDevice = new BaseDeviceState(BaseDeviceState.STATE_FREE, 1);
        baseStuPair.setBaseDevice(baseDevice);
        updateDevice(baseDevice);

        if (isConnect) {
            testState = TestState.WAIT_RESULT;
//                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ONUSE, 1));
            toastSpeak("开始测试");
            if (sargentSetting.getType() == 0){
                mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                        SerialConfigs.CMD_SARGENT_JUMP_START));
            }else {
                radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                        SerialConfigs.CMD_SARGENT_JUMP_START));
            }

            setBegin(0);
        }
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
    public void stuSkip() {

    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
        if (sargentSetting.isFullReturn()) {
            if (sex == Student.MALE) {
                return result >= Integer.valueOf(sargentSetting.getMaleFull()) * 10;
            } else {
                return result >= Integer.valueOf(sargentSetting.getFemaleFull()) * 10;
            }
        }
        return false;
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
            if (check == 1) {
                if (sargentSetting.getType() == 0){
                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                            SerialConfigs.CMD_SARGENT_JUMP_GET_SET_0(sargentSetting.getBaseHeight())));
                }else {
                    radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                            SerialConfigs.CMD_SARGENT_JUMP_GET_SET_0(sargentSetting.getBaseHeight())));
                }

            }

            if (!isConnect) {
                isConnect = true;
                //修改设备状态为连接
                mHandler.sendEmptyMessage(Constants.CONNECTED);
            }

        }
    });

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UN_CONNECT:
                    //更新设备状态
                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
                    break;
                case CONNECTED:
                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
                    break;
                case GET_SCORE_RESPONSE:
                    setBegin(1);
                    SargentJumpResult result = (SargentJumpResult) msg.obj;
                    if (runUp == 0 && baseHeight == 0) {
                        //标记原始高度
                        baseHeight = result.getScore();
                        setBaseHeight(baseHeight);

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
            stuPair.setResult(result);
            stuPair.setResultState(0);
            updateResult(stuPair);
            updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END, 1));
            // 发送结束命令
            if (sargentSetting.getType() == 0){
                mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SARGENT_JUMP_STOP));
            }else {
                radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, SerialConfigs.CMD_SARGENT_JUMP_STOP));
            }

            testState = TestState.UN_STARTED;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSerialManager != null)
            mSerialManager.close();
        if (checkService != null) {
            checkService.shutdown();
            checkService = null;
        }
        if (radioManager != null)
            radioManager.close();
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        switch (baseEvent.getTagInt()) {
            case EventConfigs.ITEM_SETTING_UPDATE:
                Log.i(TAG,"ITEM_SETTING_UPDATE");
                initData();
                if (checkService!= null){
                    checkService.shutdown();
                }
                sendEmpty();
                break;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (checkService!= null){
            checkService.shutdown();
        }
    }
}
