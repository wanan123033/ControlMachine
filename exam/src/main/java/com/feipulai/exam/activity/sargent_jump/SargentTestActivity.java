package com.feipulai.exam.activity.sargent_jump;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
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
import com.feipulai.exam.activity.person.BasePersonTestActivity;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.view.WaitDialog;
import com.orhanobut.logger.Logger;

import org.openxmlformats.schemas.drawingml.x2006.main.impl.STLineWidthImpl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.feipulai.exam.activity.sargent_jump.Constants.END_TEST;
import static com.feipulai.exam.activity.sargent_jump.Constants.GET_SCORE_RESPONSE;
import static com.feipulai.exam.activity.sargent_jump.Constants.CONNECTED;
import static com.feipulai.exam.activity.sargent_jump.Constants.MATCH_SUCCESS;
import static com.feipulai.exam.activity.sargent_jump.Constants.SET_MATCH;
import static com.feipulai.exam.activity.sargent_jump.Constants.UN_CONNECT;

public class SargentTestActivity extends BasePersonTestActivity {
    private static final String TAG = "SargentTestActivity";
    private SargentSetting sargentSetting;
    private ScheduledExecutorService checkService  = Executors.newSingleThreadScheduledExecutor();
    private SerialDeviceManager mSerialManager;
    private TestState testState = TestState.UN_STARTED;
    private volatile int check = 0;
    private boolean isConnect;
    private RadioManager radioManager;
    private boolean isSetBase = false;
    private WaitDialog changBadDialog;
    private int frequency;//需设定的主机频段
    private int currentFrequency;//当前主机频段
    private boolean isAddTool;
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
            tvDevicePair.setVisibility(View.GONE);
        }
        sendEmpty();

        if (sargentSetting.getType() == 1 && mBaseToolbar != null) {
            if (!isAddTool){
                isAddTool = true ;
                mBaseToolbar.addRightText("外接屏幕", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toLedSetting();
                    }
                });
                txtLedSetting.setVisibility(View.GONE);
            }

        }

        tvDevicePair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sargentSetting.getType() == 1) {
                    radioManager.sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
                    currentFrequency = 0 ;
                    showChangeBadDialog();

                }

            }
        });

        frequency = SerialConfigs.sProChannels.get(ItemDefault.CODE_MG) + SettingHelper.getSystemSetting().getHostId() - 1;
    }

    public void sendEmpty() {
        checkService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                check++;
                if (sargentSetting.getType() == 0) {
                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                            SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
                } else {
                    radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
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
    public void sendTestCommand(BaseStuPair baseStuPair) {
        if (baseStuPair.getStudent() == null) {
            toastSpeak("请先添加学生");
            return;
        }
        testState = TestState.UN_STARTED;
        if (sargentSetting.getType() == 0) {
            mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                    SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
        } else {
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
            if (!isSetBase) {
                if (sargentSetting.getType() == 0) {
                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                            SerialConfigs.CMD_SARGENT_JUMP_GET_SET_0(sargentSetting.getBaseHeight())));
                } else {
                    radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                            SerialConfigs.CMD_SARGENT_JUMP_GET_SET_0(sargentSetting.getBaseHeight())));
                }
                isSetBase = true;
            }
//                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ONUSE, 1));
            toastSpeak("开始测试");
            if (sargentSetting.getType() == 0) {
                mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                        SerialConfigs.CMD_SARGENT_JUMP_START));
            } else {
                radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                        SerialConfigs.CMD_SARGENT_JUMP_START));
            }
        }
        testState = TestState.WAIT_RESULT;
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
                    break;
            }
            return false;
        }
    });

    private int sum(byte[] cmd) {
        int sum = 0;
        for (int i = 2; i <= 12; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }

    private void onResultArrived(int result, BaseStuPair stuPair) {
        if (testState == TestState.WAIT_RESULT) {
            if (sargentSetting.isFullReturn()) {
                if (stuPair.getStudent().getSex() == Student.MALE) {
                    stuPair.setFullMark(stuPair.getResult() >= Integer.parseInt(sargentSetting.getMaleFull()) * 10);
                } else {
                    stuPair.setFullMark(stuPair.getResult() >= Integer.parseInt(sargentSetting.getFemaleFull()) * 10);
                }
            }
            stuPair.setResult(result);
            stuPair.setResultState(0);
            updateResult(stuPair);
            updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END, 1));
            // 发送结束命令
            if (sargentSetting.getType() == 0) {
                mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SARGENT_JUMP_STOP));
            } else {
                radioManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, SerialConfigs.CMD_SARGENT_JUMP_STOP));
            }

//            testState = TestState.UN_STARTED;
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

    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        switch (baseEvent.getTagInt()) {
            case EventConfigs.ITEM_SETTING_UPDATE:
                Log.i(TAG, "ITEM_SETTING_UPDATE");
                initData();
                if (checkService != null) {
                    checkService.shutdown();
                }
                break;
        }

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
