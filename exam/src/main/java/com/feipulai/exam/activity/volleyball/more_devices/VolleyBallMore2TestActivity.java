package com.feipulai.exam.activity.volleyball.more_devices;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

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
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.concurrent.Executors;

public class VolleyBallMore2TestActivity extends BaseVolleyBallMoreActivity {

    private static final int SEND_EMPTY = 1;
    private static final int END_JS = 2;
    private static final int GET_STATE = 3;
    private static final int TEST_TIME = 8;
    private static final int AUTO_START = 15;



    private VolleyBallJumpImpl resultJump = new VolleyBallJumpImpl(new VolleyBallJumpImpl.VolleyBallCallBack() {
        @Override
        public void getState(VolleyPair868Result obj) {
            Message msg = Message.obtain();
            msg.what = GET_STATE;
            msg.obj = obj;
            mHandler.sendMessage(msg);
        }

        @Override
        public void begin(VolleyPair868Result obj) {

        }

        @Override
        public void onError(byte gan, byte[] bytes) {

        }
    });
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case SEND_EMPTY:
                    getState();
                    break;
                case END_JS:
                    sendEnd((DeviceDetail) msg.obj, msg.arg1);
                    break;
                case GET_STATE:
                    VolleyPair868Result result = ((VolleyPair868Result)msg.obj);
                    int deviceid = result.getDeviceid();
                    int childid = result.getChildId();
                    int state = result.getState();

                    //重置机器状态
                    for (int i = 0 ; i < deviceDetails.size() ; i++){
                        BaseStuPair stuDevicePair = deviceDetails.get(i).getStuDevicePair();
                        if (childid == stuDevicePair.getBaseDevice().getDeviceId() && deviceid == SettingHelper.getSystemSetting().getHostId()){
                            stuDevicePair.setLEDupdate(false);
                            if ((state == VolleyPair868Result.STATE_TIME_PREPARE ||
                                    state == VolleyPair868Result.STATE_COUNT_PREPARE ||
                                    state == VolleyPair868Result.STATE_FREE) && !isStartTime
                                    ){
                                stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
                            }else if (state == VolleyPair868Result.STATE_TIMING || state == VolleyPair868Result.STATE_COUNTING){
                                stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
                                stuDevicePair.setLEDupdate(true);
                            }else if (state == VolleyPair868Result.STATE_TIME_END || state == VolleyPair868Result.STATE_COUNT_END){
                                stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_END);

                            }
                            stuDevicePair.getBaseDevice().setBatteryLeft(result.getElectricityState());
                            if (stuDevicePair.getStudent() != null ) {
                                int roundNo = deviceDetails.get(i).getStuDevicePair().getRoundNo();
                                Log.e("TAG++++","roundNo = "+ roundNo);
                                stuDevicePair.setResult(result.getScore());
                                String[] timeResult = stuDevicePair.getTimeResult();
                                timeResult[roundNo] = ResultDisplayUtils.getStrResultForDisplay(result.getScore());
                            }
                            updateResult(stuDevicePair);
                            break;
                        }
                    }


                    break;
                case TEST_TIME:
                    testTime(msg.arg1,msg.arg2, (DeviceDetail) msg.obj);
                    break;
                case AUTO_START:
                    sendStart((DeviceDetail) msg.obj,msg.arg1);
                    break;
            }
            return false;
        }
    });


    private void testTime(int pos,int time,DeviceDetail deviceDetail) {
        isStartTime = true;
        deviceDetail.setTestTime(time);
        deviceListAdapter.notifyItemChanged(pos);
        if (deviceDetail.getTime() < 0){
            isStartTime = false;
            return;
        }

        Message msg = Message.obtain();
        msg.what = TEST_TIME;
        msg.obj = deviceDetail;
        msg.arg1 = pos;
        msg.arg2 = --time;
        mHandler.sendMessageDelayed(msg,1000);
    }



    VolleyBallSetting setting;

    @Override
    protected void onResume() {
        super.onResume();
        setDeviceCount(4);
        deviceListAdapter.notifyDataSetChanged();
        setting = SharedPrefsUtil.loadFormSource(this, VolleyBallSetting.class);
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
        sendEnd(deviceDetail,pos);
        stuSkip(pos);

        deviceDetails.get(pos).getStuDevicePair().setStudent(null);
        deviceListAdapter.notifyItemChanged(pos);

        int hostId = SettingHelper.getSystemSetting().getHostId();
        int deviceId = deviceDetail.getStuDevicePair().getBaseDevice().getDeviceId();
        VolleyBallRadioManager.getInstance().deviceFree(hostId,deviceId);
        deviceDetails.get(pos).setTestTime(0);
        mHandler.removeMessages(SEND_EMPTY);
        mHandler.sendEmptyMessage(SEND_EMPTY);
    }

    private void getState() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0 ; i < deviceDetails.size() ; i++) {
                    BaseDeviceState device = deviceDetails.get(i).getStuDevicePair().getBaseDevice();
                    int deviceId = device.getDeviceId();
                    int hostId = SettingHelper.getSystemSetting().getHostId();
                    VolleyBallRadioManager.getInstance().getState(hostId,deviceId);
                }
                mHandler.removeMessages(SEND_EMPTY);
                mHandler.sendEmptyMessageDelayed(SEND_EMPTY,800);
            }
        });

    }

    @Override
    protected void gotoItemSetting() {
        if (!isExit()){
            toastSpeak("正在测试项目");
            return;
        }
        Intent intent = new Intent(getApplicationContext(), VolleyBallSettingActivity.class);
        intent.putExtra("deviceId",3);
        startActivity(intent);
    }

    @Override
    public void sendStart(DeviceDetail deviceDetail, int pos) {
        mHandler.removeMessages(AUTO_START);
        BaseStuPair stuPair = deviceDetail.getStuDevicePair();
        stuPair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
        int hostId = SettingHelper.getSystemSetting().getHostId();
        int deviceId = deviceDetail.getStuDevicePair().getBaseDevice().getDeviceId();

        if (flag){
            setShowLed(deviceDetail.getStuDevicePair(),pos);
        }
        //TODO 开始计时
        if (setting.getTestTime() > 0){
            startTime(deviceDetail,pos);
        }else{
            VolleyBallRadioManager.getInstance().startCount(hostId,deviceId);
        }
    }
    private boolean isStartTime = false;
    private void startTime(final DeviceDetail deviceDetail, final int pos) {
        try {
            final PreStartTimeRunnable runable = new PreStartTimeRunnable(this,deviceDetail);
            runable.setListener(new PreStartTimeRunnable.TimeListener() {
                @Override
                public void startTime() {
                    //TODO 倒计时
                    deviceDetail.setTestTime(setting.getTestTime());
                    Message msg = Message.obtain();
                    msg.what = TEST_TIME;
                    msg.obj = deviceDetail;
                    msg.arg1 = pos;
                    msg.arg2 = setting.getTestTime();
                    mHandler.removeMessages(TEST_TIME);
                    mHandler.sendMessage(msg);
                }
                @Override
                public void preTime(final int time) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTime(time,pos);
                            deviceListAdapter.notifyItemChanged(pos);
                        }
                    });
                }
            });
            Executors.newSingleThreadExecutor().execute(runable);
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    public void sendEnd(final DeviceDetail deviceDetail, final int pos) {

        SystemClock.sleep(200);

        int hostId = SettingHelper.getSystemSetting().getHostId();
        int deviceId = (byte)  deviceDetail.getStuDevicePair().getBaseDevice().getDeviceId();
        if (setting.getTestTime() > 0){
            VolleyBallRadioManager.getInstance().stopTime(hostId,deviceId);
        }else {
            VolleyBallRadioManager.getInstance().stopCount(hostId,deviceId);
        }
        SystemClock.sleep(100);

        mHandler.removeMessages(END_JS);

        SystemClock.sleep(200);
        BaseStuPair stuDevicePair = deviceDetail.getStuDevicePair();
        stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_END);
        updateResult(stuDevicePair);

    }

    @Override
    public void sendTime(DeviceDetail deviceDetail, int pos) {

    }

    @Override
    public void sendGaveUp(final DeviceDetail deviceDetail, final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("操作警告");
        builder.setMessage("成绩是否保存");
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mHandler.removeMessages(TEST_TIME);
                isStartTime = false;
                deviceDetails.get(pos).setTestTime(0);
                deviceListAdapter.notifyItemChanged(pos);
                sendEnd(deviceDetail,pos);
                sendConfirm(deviceDetail,pos);
                dialog.dismiss();
                deviceDetail.setFinsh(true);
            }
        });

        builder.setNegativeButton("放弃", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mHandler.removeMessages(TEST_TIME);
                sendEnd(deviceDetail,pos);
                stuSkip(pos);
                isStartTime = false;
                deviceDetails.get(pos).getStuDevicePair().setStudent(null);
                deviceDetails.get(pos).setTestTime(0);
                deviceListAdapter.notifyItemChanged(pos);
                dialog.dismiss();
                deviceDetail.setFinsh(true);

                int hostId = SettingHelper.getSystemSetting().getHostId();
                int deviceId = deviceDetail.getStuDevicePair().getBaseDevice().getDeviceId();
                VolleyBallRadioManager.getInstance().deviceFree(hostId,deviceId);
            }
        });
        builder.create().show();
    }

    @Override
    public void sendConfirm(DeviceDetail deviceDetail, int pos) {
        int hostId = SettingHelper.getSystemSetting().getHostId();
        int deviceId = deviceDetail.getStuDevicePair().getBaseDevice().getDeviceId();
        VolleyBallRadioManager.getInstance().deviceFree(hostId,deviceId);

        VolleyBallRadioManager.getInstance().deviceFree(hostId,deviceId);

        VolleyBallRadioManager.getInstance().deviceFree(hostId,deviceId);
        saveResult(deviceDetail.getStuDevicePair(),pos);
        updateResult(deviceDetail.getStuDevicePair());

        //TODO 是否进入下一轮测试
        showRoundNoDialog(deviceDetail,pos);
    }

    private void showRoundNoDialog(final DeviceDetail deviceDetail, final int pos) {

        //TODO 进入下一轮测试
        boolean flag = joinNext(deviceDetail,pos);

        //TODO 5秒自动开始
        if (!flag) {
            deviceDetail.getStuDevicePair().setStudent(null);
            deviceListAdapter.notifyItemChanged(pos);
        }
    }
    private boolean flag = false;
    private boolean joinNext(DeviceDetail deviceDetail, int pos) {
        Log.e("TSFF","deviceDetail.getStuDevicePair().getRoundNo()="+deviceDetail.getStuDevicePair().getRoundNo()+",testNo="+setting.getTestNo());
        //当前次数是否大于测试次数  当前测试次数从0开始
        if (deviceDetail.getStuDevicePair().getRoundNo() >= setting.getTestNo() - 1){
            deviceDetail.getStuDevicePair().setRoundNo(0);  //清理轮次
            stuSkip(pos);
            return false;
        }
        //满分跳过
        if (setting.isFullSkip() &&
                isResultFullReturn(deviceDetail.getStuDevicePair().getStudent().getSex(),
                        deviceDetail.getStuDevicePair().getResult() - deviceDetail.getStuDevicePair().getPenaltyNum())){
            deviceDetail.getStuDevicePair().setRoundNo(0);  //清理轮次
            stuSkip(pos);
            deviceDetail.getStuDevicePair().setFullMark(true);
            printResult(deviceDetail.getStuDevicePair());
            toastSpeak("满分");

            return false;
        }
        deviceDetail.setRound(deviceDetail.getStuDevicePair().getRoundNo() + 1);
        deviceDetail.getStuDevicePair().setPenaltyNum(0);
        addStudent(deviceDetail.getStuDevicePair().getStudent(),pos, false);

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
        VolleyBallRadioManager.getInstance().stopCount(hostId,deviceId);

        SystemClock.sleep(100);
//        VolleyBallRadioManager.getInstance().deviceFree(hostId,deviceId);
        BaseStuPair stuDevicePair = deviceDetail.getStuDevicePair();
        stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_END);
        updateResult(stuDevicePair);

    }

    @Override
    public void sendPenalty(DeviceDetail deviceDetail, int pos) {
        showPenalizeDialog(deviceDetail,pos);


    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
        if (sex == 0 && result >=setting.getMaleFullScore()){
            return true;
        }else if (sex == 1 && result >= setting.getFemaleFullScore()){
            return true;
        }
        return false;
    }

    public void showPenalizeDialog(final DeviceDetail pair,final int pos) {
        final NumberPicker numberPicker = new NumberPicker(this);

        numberPicker.setMinValue(0);
//        numberPicker.setValue(pairs.get(0).getPenalty());
        numberPicker.setMaxValue(pair.getStuDevicePair().getResult());
        LinearLayout layout = new LinearLayout(this);
        layout.setGravity(Gravity.CENTER);
        layout.addView(numberPicker, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        numberPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS); //禁止输入
        Log.e("TAG","pair.getStuDevicePair().getResult()="+pair.getStuDevicePair().getResult());
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
                        toastSpeak("判罚成功");
                        updateResultLed(pair.getStuDevicePair(),pos);
                        sendConfirm(pair,pos);
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
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void finish() {

        super.finish();
    }
}
