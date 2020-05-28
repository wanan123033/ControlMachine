package com.feipulai.exam.activity.sargent_jump;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
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
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.view.WaitDialog;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.examlogger.LogUtils;


import static com.feipulai.exam.activity.sargent_jump.Constants.CONNECTED;
import static com.feipulai.exam.activity.sargent_jump.Constants.END_TEST;
import static com.feipulai.exam.activity.sargent_jump.Constants.GET_SCORE_RESPONSE;
import static com.feipulai.exam.activity.sargent_jump.Constants.MATCH_SUCCESS;
import static com.feipulai.exam.activity.sargent_jump.Constants.SET_MATCH;
import static com.feipulai.exam.activity.sargent_jump.Constants.UN_CONNECT;

public class SargentTestActivity extends BasePersonTestActivity {
    private static final String TAG = "SargentTestActivity";
    private static final int SEND_EMPTY = 0x01;
    private SargentSetting sargentSetting;
//    private ScheduledExecutorService checkService  = Executors.newSingleThreadScheduledExecutor();
    private TestState testState = TestState.UN_STARTED;
    private volatile int check = 0;
    private boolean isConnect;
    private WaitDialog changBadDialog;
    private int frequency;//需设定的主机频段
    private int currentFrequency;//当前主机频段
    private boolean isAddTool;
    private BaseStuPair baseStuPair;
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

        Logger.i(TAG + ":sargentSetting ->" + sargentSetting.toString());
        runUp = sargentSetting.getRunUp();
        // 0 显示 原地起跳 1 隐藏 助跑
        setBaseHeightVisible(runUp);
        if (sargentSetting.getType() == 1 && mBaseToolbar != null) {
            tvDevicePair.setVisibility(View.VISIBLE);
            if (!isAddTool){
                isAddTool = true ;
                mBaseToolbar.addRightText("外接屏幕", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toLedSetting();
                    }
                });
                txtLedSetting.setVisibility(View.GONE);

                mBaseToolbar.addRightText("获取成绩", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showGetScore();
                    }
                });
            }

        }

        tvDevicePair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sargentSetting.getType() == 1) {
                    RadioChannelCommand command = new RadioChannelCommand(0);
                    LogUtils.normal(command.getCommand().length+"---"+StringUtility.bytesToHexString(command.getCommand())+"---摸高切频指令");
                    RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
                    currentFrequency = 0 ;
                    showChangeBadDialog();

                }

            }
        });

        frequency = SettingHelper.getSystemSetting().getUseChannel();
    }

    /**
     * 显示是否获取成绩
     */
    private void showGetScore() {
        new AlertDialog.Builder(this).setMessage("确认要获取成绩吗?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogUtils.normal(SerialConfigs.CMD_SARGENT_JUMP_GET_SCORE.length+"---"+StringUtility.bytesToHexString(SerialConfigs.CMD_SARGENT_JUMP_GET_SCORE)+"---摸高获取成绩指令");
                        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,SerialConfigs.CMD_SARGENT_JUMP_GET_SCORE ));
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", null).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sargentSetting.getType() == 0) {
            SerialDeviceManager.getInstance().setRS232ResiltListener(resultImpl);
        } else {
            RadioManager.getInstance().init();
            RadioManager.getInstance().setOnRadioArrived(resultImpl);
        }
        sendEmpty();
    }

    public void sendEmpty() {
        check++;
        LogUtils.normal(SerialConfigs.CMD_SARGENT_JUMP_EMPTY.length+"---"+StringUtility.bytesToHexString(SerialConfigs.CMD_SARGENT_JUMP_EMPTY)+"摸高空指令");
        if (sargentSetting.getType() == 0) {
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                    SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
        } else {
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
        }
        if (check > 2) {
            // 失去连接
            isConnect = false;
            mHandler.sendEmptyMessage(UN_CONNECT);
        }
        mHandler.sendEmptyMessageDelayed(SEND_EMPTY, 2000);
    }

    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {
        if (baseStuPair.getStudent() == null) {
            toastSpeak("请先添加学生");
            return;
        }
        this.baseStuPair = baseStuPair;
        testState = TestState.UN_STARTED;
        LogUtils.normal(SerialConfigs.CMD_SARGENT_JUMP_EMPTY.length+"---"+StringUtility.bytesToHexString(SerialConfigs.CMD_SARGENT_JUMP_EMPTY)+"摸高空指令");
        if (sargentSetting.getType() == 0) {
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                    SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
        } else {
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
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
            toastSpeak("开始测试");
            pair.setTestTime(DateUtil.getCurrentTime()+"");
            LogUtils.normal(SerialConfigs.CMD_SARGENT_JUMP_START.length+"---"+StringUtility.bytesToHexString(SerialConfigs.CMD_SARGENT_JUMP_START)+"摸高开始测试指令");

            if (sargentSetting.getType() == 0) {
                SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                        SerialConfigs.CMD_SARGENT_JUMP_START));
            } else {
                RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                        SerialConfigs.CMD_SARGENT_JUMP_START));
            }
        }
        testState = TestState.WAIT_RESULT;
        baseStuPair.setTestTime(DateUtil.getCurrentTime()+"");
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
                    LogUtils.normal(cmd.length+"---" + StringUtility.bytesToHexString(cmd)+"---摸高设置参数指令");

                    RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
                    RadioChannelCommand command = new RadioChannelCommand(frequency);
                    LogUtils.normal(command.getCommand().length+"---" + StringUtility.bytesToHexString(command.getCommand())+"---摸高切频指令");
                    RadioManager.getInstance().sendCommand(new ConvertCommand(command));
                    currentFrequency = frequency;
                case MATCH_SUCCESS:
                    ToastUtils.showShort("配对成功");
                    if (changBadDialog!= null && changBadDialog.isShowing())
                        changBadDialog.dismiss();
                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
                    break;
                case UN_CONNECT:
                    //更新设备状态
                    if (pair.getBaseDevice().getState() != BaseDeviceState.STATE_ERROR){
                        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
                    }
                    break;
                case CONNECTED:
                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
                    break;
                case GET_SCORE_RESPONSE:
                    SargentJumpResult result = (SargentJumpResult) msg.obj;
                    if (runUp == 0 && baseHeight == 0) {
                        //标记原始高度
                        baseHeight = result.getScore();
                        if (baseStuPair==null ||baseStuPair.getStudent() != null){
                            setBaseHeight(baseHeight);
                        }

                    } else {
                        int dbResult = result.getScore() * 10;
                        //原地起跳高度
                        if (runUp == 0) {
                            dbResult = result.getScore() * 10 - baseHeight * 10;
                            if (dbResult > 0) {
                                onResultArrived(dbResult, baseStuPair);
                            }

                        } else {
                            onResultArrived(dbResult, baseStuPair);
                        }
                    }

                    break;
                case END_TEST:
                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
                    break;
                case SEND_EMPTY:
                    sendEmpty();
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
        int min = sargentSetting.getBaseHeight()*10;
        int max = (sargentSetting.getBaseHeight()+116)*10;
        if (result< min || result > max ){
            toastSpeak("数据异常，请重测");
            return;
        }
        if (stuPair ==null || stuPair.getStudent() == null)
            return;
        stuPair.setResult(result);
        if (testState == TestState.WAIT_RESULT) {
            if (sargentSetting.isFullReturn()) {
                if (stuPair.getStudent().getSex() == Student.MALE) {
                    stuPair.setFullMark(stuPair.getResult() >= Integer.parseInt(sargentSetting.getMaleFull()) * 10);
                } else {
                    stuPair.setFullMark(stuPair.getResult() >= Integer.parseInt(sargentSetting.getFemaleFull()) * 10);
                }
            }
            stuPair.setEndTime(DateUtil.getCurrentTime()+"");
            stuPair.setResultState(RoundResult.RESULT_STATE_NORMAL);
            updateResult(stuPair);
            updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END, 1));
            // 发送结束命令
            LogUtils.normal(SerialConfigs.CMD_SARGENT_JUMP_STOP.length+"---"+StringUtility.bytesToHexString(SerialConfigs.CMD_SARGENT_JUMP_STOP)+"摸高结束测试指令");

            if (sargentSetting.getType() == 0) {
                SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SARGENT_JUMP_STOP));
            } else {
                RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, SerialConfigs.CMD_SARGENT_JUMP_STOP));
            }

//            testState = TestState.UN_STARTED;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.life("SargentTestActivity onDestroy");
//        if (SerialDeviceManager.getInstance() != null)
//            SerialDeviceManager.getInstance().close();

        SerialDeviceManager.getInstance().setRS232ResiltListener(null);
        RadioManager.getInstance().setOnRadioArrived(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.life("SargentTestActivity onStop");
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        switch (baseEvent.getTagInt()) {
            case EventConfigs.ITEM_SETTING_UPDATE:
                initData();
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
        public void onFree(int id) {
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
                RadioChannelCommand command = new RadioChannelCommand(fre);
                LogUtils.normal(command.getCommand().length+"---" + StringUtility.bytesToHexString(command.getCommand())+"---摸高切频指令");
                RadioManager.getInstance().sendCommand(new ConvertCommand(command));
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
