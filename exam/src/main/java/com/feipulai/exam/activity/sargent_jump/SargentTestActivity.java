package com.feipulai.exam.activity.sargent_jump;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.LogUtil;
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
import com.orhanobut.logger.utils.LogUtils;


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
    private int frequency;//????????????????????????
    private int currentFrequency;//??????????????????
    private boolean isAddTool;
    private BaseStuPair baseStuPair;

    private void init() {
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_FREE, 1));
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
        // 0 ?????? ???????????? 1 ?????? ??????
        setBaseHeightVisible(runUp);
        if (sargentSetting.getType() == 1 && mBaseToolbar != null) {
            tvDevicePair.setVisibility(View.VISIBLE);
            if (!isAddTool) {
                isAddTool = true;
                mBaseToolbar.addRightText("????????????", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toLedSetting();
                    }
                });
                txtLedSetting.setVisibility(View.GONE);

                mBaseToolbar.addRightText("????????????", new View.OnClickListener() {
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
                    LogUtils.serial(command.getCommand().length + "??????????????????" + StringUtility.bytesToHexString(command.getCommand()));
                    RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(0)));
                    currentFrequency = 0;
                    showChangeBadDialog();

                }

            }
        });

        frequency = SettingHelper.getSystemSetting().getUseChannel();
    }

    @Override
    protected int isShowPenalizeFoul() {
        return sargentSetting.isPenalize() ? View.VISIBLE : View.GONE;
    }

    /**
     * ????????????????????????
     */
    private void showGetScore() {
        new AlertDialog.Builder(this).setMessage("?????????????????????????")
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogUtils.serial("????????????????????????" + StringUtility.bytesToHexString(SerialConfigs.CMD_SARGENT_JUMP_GET_SCORE) + "---");
                        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, SerialConfigs.CMD_SARGENT_JUMP_GET_SCORE));
                        dialog.dismiss();
                    }
                }).setNegativeButton("??????", null).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sargentSetting.getType() == 1) {
            RadioManager.getInstance().init();
            RadioManager.getInstance().setOnRadioArrived(resultImpl);

        } else {
            SerialDeviceManager.getInstance().setRS232ResiltListener(resultImpl);
        }
        sendEmpty();
    }

    public void sendEmpty() {
        check++;
        LogUtils.serial("???????????????" + StringUtility.bytesToHexString(SerialConfigs.CMD_SARGENT_JUMP_EMPTY) + "");
        if (sargentSetting.getType() == 1) {
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
        } else {
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                    SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
        }
        if (check > 2) {
            // ????????????
            isConnect = false;
            mHandler.sendEmptyMessage(UN_CONNECT);
        }
        mHandler.sendEmptyMessageDelayed(SEND_EMPTY, 2000);
    }

    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {
        if (baseStuPair.getStudent() == null) {
            toastSpeak("??????????????????");
            return;
        }
        this.baseStuPair = baseStuPair;
        LogUtils.serial("???????????????" + StringUtility.bytesToHexString(SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
        if (sargentSetting.getType() == 1) {
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                    SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
        } else {
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                    SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
        }

        //??????????????????
        BaseDeviceState baseDevice = baseStuPair.getBaseDevice();
        if (baseDevice != null)
            baseDevice.setState(BaseDeviceState.STATE_FREE);
        else
            baseDevice = new BaseDeviceState(BaseDeviceState.STATE_FREE, 1);
        baseStuPair.setBaseDevice(baseDevice);
        updateDevice(baseDevice);

        if (isConnect) {
            toastSpeak("????????????");
            pair.setTestTime(DateUtil.getCurrentTime() + "");
            LogUtils.serial("????????????????????????" + StringUtility.bytesToHexString(SerialConfigs.CMD_SARGENT_JUMP_START));

            if (sargentSetting.getType() == 1) {
                //??????
                RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                        SerialConfigs.CMD_SARGENT_JUMP_START));
            } else {
                //??????
                SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                        SerialConfigs.CMD_SARGENT_JUMP_START));
            }
        }
        testState = TestState.WAIT_RESULT;
        baseStuPair.setTestTime(DateUtil.getCurrentTime() + "");
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
                    LogUtils.serial("????????????????????????" + StringUtility.bytesToHexString(cmd) + "---");

                    RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, cmd));
                    RadioChannelCommand command = new RadioChannelCommand(frequency);
                    LogUtils.serial("??????????????????" + StringUtility.bytesToHexString(command.getCommand()) + "---");
                    RadioManager.getInstance().sendCommand(new ConvertCommand(command));
                    currentFrequency = frequency;
                case MATCH_SUCCESS:
                    ToastUtils.showShort("????????????");
                    if (changBadDialog != null && changBadDialog.isShowing())
                        changBadDialog.dismiss();
                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
                    break;
                case UN_CONNECT:
                    //??????????????????
                    if (pair.getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
                        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
                    }
                    break;
                case CONNECTED:
                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_FREE, 1));
                    break;
                case GET_SCORE_RESPONSE:
                    SargentJumpResult result = (SargentJumpResult) msg.obj;
                    if (runUp == 0 && baseHeight == 0) {
                        //??????????????????
                        baseHeight = result.getScore();
                        if (baseStuPair == null || baseStuPair.getStudent() != null) {
                            setBaseHeight(baseHeight);
                        }

                    } else {
                        int dbResult = result.getScore() * 10;
                        //??????????????????
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
        int min = sargentSetting.getBaseHeight() * 10;
        int max = (sargentSetting.getBaseHeight() + 116) * 10;
        if (result < min || result > max) {
            toastSpeak("????????????????????????");
            return;
        }
        if (stuPair == null || stuPair.getStudent() == null)
            return;
        stuPair.setResult(result);
        if (testState == TestState.WAIT_RESULT) {
            testState = TestState.UN_STARTED;
            LogUtils.all("???????????????==onResultArrived???" + testState);
            if (sargentSetting.isFullReturn()) {
                if (stuPair.getStudent().getSex() == Student.MALE) {
                    stuPair.setFullMark(stuPair.getResult() >= Integer.parseInt(sargentSetting.getMaleFull()) * 10);
                } else {
                    stuPair.setFullMark(stuPair.getResult() >= Integer.parseInt(sargentSetting.getFemaleFull()) * 10);
                }
            }
            stuPair.setEndTime(DateUtil.getCurrentTime() + "");
            stuPair.setResultState(RoundResult.RESULT_STATE_NORMAL);
            updateResult(stuPair);
            updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END, 1));
            // ??????????????????
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // ??????????????????
                    LogUtils.normal(SerialConfigs.CMD_SARGENT_JUMP_STOP.length + "---" + StringUtility.bytesToHexString(SerialConfigs.CMD_SARGENT_JUMP_STOP) + "---??????????????????");
                    if (sargentSetting.getType() == 1) {
                        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868, SerialConfigs.CMD_SARGENT_JUMP_STOP));
                    } else {
                        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SARGENT_JUMP_STOP));
                    }
                }
            },3000);

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
        super.onEventMainThread(baseEvent);
        switch (baseEvent.getTagInt()) {
            case EventConfigs.ITEM_SETTING_UPDATE:
                initData();
                break;
        }
    }

    private SargentJumpImpl resultImpl = new SargentJumpImpl(new SargentJumpImpl.SargentJumpListener() {
        @Override
        public void onResultArrived(SargentJumpResult result) {
//            if (testState == TestState.UN_STARTED){
//                return;
//            }
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
                //???????????????????????????
                mHandler.sendEmptyMessage(Constants.CONNECTED);
            }

        }

        @Override
        public void onMatch(SargentJumpResult match) {
            int fre = match.getFrequency();
            if (currentFrequency != frequency) {
                RadioChannelCommand command = new RadioChannelCommand(fre);
                LogUtils.serial( "??????????????????" + StringUtility.bytesToHexString(command.getCommand()) + "---");
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
        // ?????????dialog????????????????????????
        changBadDialog.setTitle("????????????????????????");
        changBadDialog.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changBadDialog.dismiss();
            }
        });
    }


}
