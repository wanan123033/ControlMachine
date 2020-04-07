package com.feipulai.exam.activity.volleyball.more_devices;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.feipulai.common.jump_rope.task.GetDeviceStatesTask;
import com.feipulai.common.jump_rope.task.PreciseCountDownTimer;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.VolleyBallRadioManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.VolleyPair868Result;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;
import com.feipulai.exam.activity.volleyball.VolleyBallSettingActivity;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 排球垫球,个人模式
 */
public class VolleyBallMoreTestActivity extends BaseVolleyBallMoreActivity {

    private static final int SEND_EMPTY = 1;
    private static final int END_JS = 2;
    private static final int GET_STATE = 3;
    private static final int TEST_TIME = 8;
    private static final int AUTO_START = 15;
    private static final int RESH_STATE = 22;
    private int[] mCurrentConnect;
    private boolean flag = false;
    VolleyBallSetting setting;
    private ExecutorService exec;
    private GetDeviceStatesTask mGetDeviceStatesTask = new GetDeviceStatesTask(new GetDeviceStatesTask.OnGettingDeviceStatesListener() {
        @Override
        public void onGettingState(int position) {
            BaseDeviceState device = deviceDetails.get(position).getStuDevicePair().getBaseDevice();
            int deviceId = device.getDeviceId();
            int hostId = SettingHelper.getSystemSetting().getHostId();
            VolleyBallRadioManager.getInstance().getState(hostId, deviceId);
        }

        @Override
        public void onStateRefreshed() {
            int oldState;
            for (int i = 0; i < deviceDetails.size(); i++) {
                BaseDeviceState deviceState = deviceDetails.get(i).getStuDevicePair().getBaseDevice();
                oldState = deviceState.getState();
                if (mCurrentConnect[deviceState.getDeviceId() - 1] == 0
                        && oldState != BaseDeviceState.STATE_ERROR
                        && oldState != BaseDeviceState.STATE_CONFLICT) {
                    Logger.i("zzs----->onStateRefreshed==========>STATE_ERROR" + mCurrentConnect[deviceState.getDeviceId() - 1]);
                    deviceState.setState(BaseDeviceState.STATE_ERROR);
                    final int finalI = i;
                    Message message = Message.obtain();
                    message.what = RESH_STATE;
                    message.arg1 = finalI;
                    mHandler.sendMessage(message);

                }
            }
            mCurrentConnect = new int[deviceDetails.size()];
        }

        @Override
        public int getDeviceCount() {
            return 4;
        }
    });


    private VolleyBallJumpImpl resultJump = new VolleyBallJumpImpl(new VolleyBallJumpImpl.VolleyBallCallBack() {
        @Override
        public void getState(VolleyPair868Result obj) {
            Log.e("TAG---","getState(VolleyPair868Result obj)");
            Message msg = Message.obtain();
            msg.what = GET_STATE;
            msg.obj = obj;
            mHandler.sendMessage(msg);
        }
    });
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_EMPTY:
                    getState();
                    break;
                case END_JS:
                    sendEnd((DeviceDetail) msg.obj, msg.arg1);
                    break;
                case GET_STATE:
                    VolleyPair868Result result = ((VolleyPair868Result) msg.obj);
                    int hostid = result.getHostid();
                    int deviceId = result.getDeviceId();
                    int state = result.getState();
                    if (deviceDetails.size() < deviceId || deviceId == 0) {
                        return false;
                    }
                    mCurrentConnect[deviceId - 1] = BaseDeviceState.STATE_FREE;
                    Log.e("TAG=-=-=-=", "deviceId = " + deviceId + ",state=" + state);
                    //重置机器状态
                    BaseStuPair stuDevicePair = deviceDetails.get(deviceId - 1).getStuDevicePair();
                    if (deviceId == stuDevicePair.getBaseDevice().getDeviceId() && hostid == SettingHelper.getSystemSetting().getHostId()) {
                        if ((state == VolleyPair868Result.STATE_FREE) && !isStartTime
                                ) {
                            stuDevicePair.setLEDupdate(false);
                            stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
                        } else if (state == VolleyPair868Result.STATE_TIMING || state == VolleyPair868Result.STATE_COUNTING) {
                            stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
                            stuDevicePair.setLEDupdate(true);
                        } else if (state == VolleyPair868Result.STATE_TIME_END || state == VolleyPair868Result.STATE_COUNT_END) {
                            stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_END);
                            stuDevicePair.setLEDupdate(true);
                        } else if (state == VolleyPair868Result.STATE_TIME_PREPARE ||
                                state == VolleyPair868Result.STATE_COUNT_PREPARE) {
                            stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_PRE_TIME);
                        }
                        stuDevicePair.getBaseDevice().setBatteryLeft(result.getElectricityState());
                        if (stuDevicePair.getStudent() != null) {
                            int roundNo = deviceDetails.get(deviceId - 1).getStuDevicePair().getRoundNo();
                            Log.e("TAG++++", "roundNo = " + roundNo);
                            stuDevicePair.setResult(result.getScore());
                            String[] timeResult = stuDevicePair.getTimeResult();
                            timeResult[roundNo] = ResultDisplayUtils.getStrResultForDisplay(result.getScore());
                        }
                        updateResult(stuDevicePair);
                        break;
                    }


                    break;
                case RESH_STATE:
                    refreshDevice(msg.arg1);
                    break;
                case AUTO_START:
                    sendStart((DeviceDetail) msg.obj, msg.arg1);
                    break;
            }
            return false;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exec = Executors.newFixedThreadPool(10);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setDeviceCount(4);
        setting = SharedPrefsUtil.loadFormSource(this, VolleyBallSetting.class);
        deviceListAdapter.setTestCount(setting.getTestNo());
        deviceListAdapter.notifyDataSetChanged();
        RadioManager.getInstance().setOnRadioArrived(resultJump);

        getState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void fqCount(DeviceDetail deviceDetail, int pos) {
        sendEnd(deviceDetail, pos);
        stuSkip(pos);

        deviceDetails.get(pos).getStuDevicePair().setStudent(null);
        deviceListAdapter.notifyItemChanged(pos);

        int hostId = SettingHelper.getSystemSetting().getHostId();
        int deviceId = deviceDetail.getStuDevicePair().getBaseDevice().getDeviceId();
        VolleyBallRadioManager.getInstance().deviceFree(hostId, deviceId);
        deviceDetails.get(pos).setTestTime(0);
        mHandler.removeMessages(SEND_EMPTY);
        mHandler.sendEmptyMessage(SEND_EMPTY);
    }

    private void getState() {
        mCurrentConnect = new int[deviceDetails.size()];
        pause();
        exec.execute(mGetDeviceStatesTask);
        mGetDeviceStatesTask.resume();
    }

    public void pause() {
        mGetDeviceStatesTask.pause();
    }

    @Override
    protected void gotoItemSetting() {
        if (!isExit()) {
            toastSpeak("正在测试项目");
            return;
        }
        Intent intent = new Intent(getApplicationContext(), VolleyBallSettingActivity.class);
        intent.putExtra("deviceId", 3);
        startActivity(intent);
    }

    @Override
    public void sendStart(DeviceDetail deviceDetail, int pos) {
        mHandler.removeMessages(AUTO_START);
        BaseStuPair stuPair = deviceDetail.getStuDevicePair();
        stuPair.getBaseDevice().setState(BaseDeviceState.STATE_PRE_TIME);
        int hostId = SettingHelper.getSystemSetting().getHostId();
        int deviceId = deviceDetail.getStuDevicePair().getBaseDevice().getDeviceId();

        if (flag) {
            setShowLed(deviceDetail.getStuDevicePair(), pos);
        }
        //TODO 开始计时
        if (setting.getTestTime() > 0) {
            startTime(deviceDetail, pos);
        } else {
            VolleyBallRadioManager.getInstance().startCount(hostId, deviceId);
        }
    }

    private boolean isStartTime = false;
    private PreStartTimeRunnable runable;

    private void startTime(final DeviceDetail deviceDetail, final int pos) {
        deviceDetail.getStuDevicePair().setStartTime(System.currentTimeMillis()+"");
        try {
            runable = new PreStartTimeRunnable(this, deviceDetail);
            runable.setListener(new PreStartTimeRunnable.TimeListener() {
                @Override
                public void startTime() {
                    if (deviceDetail.getCount() != null){
                        deviceDetail.getCount().cancel();
                    }
                    deviceDetail.setCount(new PreciseCountDownTimer(setting.getTestTime() * 1000,1000,0) {
                        @Override
                        public void onTick(final long tick) {

                            Log.e("TAG","--------------"+tick);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    deviceDetail.setTestTime((int) tick);
                                    deviceListAdapter.notifyItemChanged(pos);
                                }
                            });

                        }

                        @Override
                        public void onFinish() {
                            isStartTime = false;
                        }
                    });
                    exec.execute(deviceDetail.getCount());
                }

                @Override
                public void preTime(final int time) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("preTime", "-------" + time);
                            isStartTime = true;
                            updateTime(time, pos);
                            deviceListAdapter.notifyItemChanged(pos);
                        }
                    });
                }
            });
            exec.execute(runable);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void sendEnd(final DeviceDetail deviceDetail, final int pos) {
        BaseStuPair stuDevicePair = deviceDetail.getStuDevicePair();
        stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_END);
        updateResult(stuDevicePair);
//        deviceDetail.getCount().cancel();
//        setShowLed(deviceDetail.getStuDevicePair(), pos);
    }

    @Override
    public void sendTime(DeviceDetail deviceDetail, int pos) {

    }

    @Override
    public void sendGaveUp(final DeviceDetail deviceDetail, final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("操作警告");
        builder.setMessage("成绩是否保存");
        builder.setPositiveButton("保存成绩", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isStartTime = false;
                deviceDetail.getCount().cancel();
                deviceDetail.getStuDevicePair().setResultState(3);
                deviceDetails.get(pos).setTestTime(0);
                deviceListAdapter.notifyItemChanged(pos);
                sendConfirm(deviceDetail, pos);
                dialog.dismiss();
                deviceDetail.setFinsh(true);
            }
        });

        builder.setNegativeButton("重新开始", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mHandler.removeMessages(TEST_TIME);
                deviceDetail.getCount().cancel();
                isStartTime = false;
                deviceDetails.get(pos).setTestTime(0);
                deviceListAdapter.notifyItemChanged(pos);
                dialog.dismiss();
                deviceDetail.setFinsh(true);

                int hostId = SettingHelper.getSystemSetting().getHostId();
                int deviceId = deviceDetail.getStuDevicePair().getBaseDevice().getDeviceId();
                runable.stop();
                VolleyBallRadioManager.getInstance().deviceFree(hostId, deviceId);
            }
        });
        builder.create().show();
    }

    @Override
    public void sendConfirm(DeviceDetail deviceDetail, int pos) {
        int hostId = SettingHelper.getSystemSetting().getHostId();
        int deviceId = deviceDetail.getStuDevicePair().getBaseDevice().getDeviceId();
        VolleyBallRadioManager.getInstance().deviceFree(hostId, deviceId);

        if (deviceDetail.getStuDevicePair().getResultState() == 0){
            deviceDetail.getStuDevicePair().setResultState(1);
        }
        deviceDetail.getCount().cancel();
        saveResult(deviceDetail.getStuDevicePair(), pos);
        updateResult(deviceDetail.getStuDevicePair());

        //TODO 是否进入下一轮测试
        showRoundNoDialog(deviceDetail, pos);
    }

    private void showRoundNoDialog(final DeviceDetail deviceDetail, final int pos) {

        //TODO 进入下一轮测试
        boolean flag = joinNext(deviceDetail, pos);

        //TODO 5秒自动开始
        if (!flag) {
            deviceDetail.getStuDevicePair().setStudent(null);
            deviceListAdapter.notifyItemChanged(pos);
        }
    }

    private boolean joinNext(DeviceDetail deviceDetail, int pos) {
        Log.e("TSFF", "deviceDetail.getStuDevicePair().getRoundNo()=" + deviceDetail.getStuDevicePair().getRoundNo() + ",testNo=" + setting.getTestNo());
        //当前次数是否大于测试次数  当前测试次数从0开始
        if (deviceDetail.getStuDevicePair().getRoundNo() >= setting.getTestNo() - 1) {
            deviceDetail.getStuDevicePair().setRoundNo(0);  //清理轮次
            stuSkip(pos);
            return false;
        }
        //满分跳过
        if (setting.isFullSkip() &&
                isResultFullReturn(deviceDetail.getStuDevicePair().getStudent().getSex(),
                        deviceDetail.getStuDevicePair().getResult() - deviceDetail.getStuDevicePair().getPenaltyNum())) {
            deviceDetail.getStuDevicePair().setRoundNo(0);  //清理轮次
            stuSkip(pos);
            deviceDetail.getStuDevicePair().setFullMark(true);
            printResult(deviceDetail.getStuDevicePair());
            toastSpeak("满分");

            return false;
        }
        deviceDetail.getStuDevicePair().setResultState(0);
        deviceDetail.setRound(deviceDetail.getStuDevicePair().getRoundNo() + 1);
        deviceDetail.getStuDevicePair().setPenaltyNum(0);
        addStudent(deviceDetail.getStuDevicePair().getStudent(), pos, false);

        //当前轮次加1
        deviceDetail.getStuDevicePair().setRoundNo(deviceDetail.getStuDevicePair().getRoundNo() + 1);
        deviceListAdapter.notifyItemChanged(pos);

        flag = true;
        return true;
    }

    @Override
    public void stopCount(DeviceDetail deviceDetail, int pos) {
        int hostId = SettingHelper.getSystemSetting().getHostId();
        int deviceId = deviceDetail.getStuDevicePair().getBaseDevice().getDeviceId();
        VolleyBallRadioManager.getInstance().stopCount(hostId, deviceId);
        BaseStuPair stuDevicePair = deviceDetail.getStuDevicePair();
        stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_END);
        updateResult(stuDevicePair);
    }

    @Override
    public void sendPenalty(DeviceDetail deviceDetail, int pos) {
        showPenalizeDialog(deviceDetail, pos);


    }

    @Override
    protected void deviceFree(DeviceDetail deviceDetail, int index) {
        int hostId = SettingHelper.getSystemSetting().getHostId();
        int deviceId = deviceDetail.getStuDevicePair().getBaseDevice().getDeviceId();
        VolleyBallRadioManager.getInstance().deviceFree(hostId, deviceId);
    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
        if (!setting.isFullSkip()) {
            return false;
        }
        if (sex == 0 && result >= setting.getMaleFullScore()) {
            return true;
        } else if (sex == 1 && result >= setting.getFemaleFullScore()) {
            return true;
        }
        return false;
    }

    public void showPenalizeDialog(final DeviceDetail pair, final int pos) {
        final NumberPicker numberPicker = new NumberPicker(this);

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(pair.getStuDevicePair().getResult());
        LinearLayout layout = new LinearLayout(this);
        layout.setGravity(Gravity.CENTER);
        layout.addView(numberPicker, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        numberPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS); //禁止输入
        Log.e("TAG", "pair.getStuDevicePair().getResult()=" + pair.getStuDevicePair().getResult());
        new AlertDialog.Builder(this).setTitle("请输入判罚值")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int value = -1 * numberPicker.getValue();
                        pair.getStuDevicePair().setResult(value + pair.getStuDevicePair().getResult());
                        String[] timeResult = pair.getStuDevicePair().getTimeResult();
                        timeResult[pair.getStuDevicePair().getRoundNo()] = ResultDisplayUtils.getStrResultForDisplay(pair.getStuDevicePair().getResult());
                        pair.getStuDevicePair().setPenaltyNum(numberPicker.getValue());
                        updateResultLed(pair.getStuDevicePair(), pos);
                        pair.getStuDevicePair().setResultState(2);
                        toastSpeak("判罚成功");
                        sendConfirm(pair, pos);
                        pair.setFinsh(true);
                    }
                })
                .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceDetails.clear();
        exec.shutdown();
        mHandler.removeCallbacksAndMessages(null);
        RadioManager.getInstance().setOnRadioArrived(null);
    }

    @Override
    public void finish() {
        mGetDeviceStatesTask.finish();
        super.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGetDeviceStatesTask.pause();
    }
}
