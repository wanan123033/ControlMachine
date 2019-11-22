package com.feipulai.exam.activity.volleyball.more_devices;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.feipulai.device.manager.VolleyBallRadioManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.VolleyPair868Result;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.concurrent.Executors;

public class VolleyBallGroupActivity extends BaseGroupActivity {
    private static final int SEND_EMPTY = 1;
    private static final int END_JS = 2;
    private static final int GET_STATE = 3;
    private static final int TEST_TIME = 8;
    private static final int AUTO_START = 15;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case SEND_EMPTY:
                    getState();
                    break;
                case END_JS:
//                    sendEnd((DeviceDetail) msg.obj, msg.arg1);
                    break;
                case GET_STATE:
                    VolleyPair868Result result = ((VolleyPair868Result)msg.obj);
                    int deviceid = result.getHostid();
                    int childid = result.getDeviceId();
                    int state = result.getState();

                    //重置机器状态
                    for (int i = 0 ; i < deviceDetails.size() ; i++){
                        BaseStuPair stuDevicePair = deviceDetails.get(i).getStuDevicePair();
                        Log.e("TAG++++----","childid="+childid+"stuDevicePair.getBaseDevice().getDeviceId()="+stuDevicePair.getBaseDevice().getDeviceId());
                        if (childid == stuDevicePair.getBaseDevice().getDeviceId() && deviceid == SettingHelper.getSystemSetting().getHostId()){
                            Log.e("TAG++++","roundNo ================== ");
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
//                    testTime(msg.arg1,msg.arg2, (DeviceDetail) msg.obj);
                    break;
                case AUTO_START:
//                    sendStart((DeviceDetail) msg.obj,msg.arg1);
                    break;
            }
            return false;
        }
    });
    private VolleyBallJumpImpl resultJump = new VolleyBallJumpImpl(new VolleyBallJumpImpl.VolleyBallCallBack() {
        @Override
        public void getState(VolleyPair868Result obj) {
            Message msg = Message.obtain();
            msg.what = GET_STATE;
            msg.obj = obj;
            mHandler.sendMessage(msg);
        }
    });
    private boolean isStartTime = false;


    @Override
    protected void fqCount(DeviceDetail deviceDetail, int i) {

    }

    @Override
    protected void stopCount(DeviceDetail deviceDetail, int i) {

    }

    @Override
    protected void sendPenalty(DeviceDetail deviceDetail, int i) {

    }

    @Override
    protected void sendConfirm(DeviceDetail deviceDetail, int pos) {
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

    @Override
    protected void sendGaveUp(final DeviceDetail deviceDetail, final int pos) {
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
    protected void sendTime(DeviceDetail deviceDetail, int i) {

    }

    @Override
    protected void sendEnd(DeviceDetail deviceDetail, int pos) {
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
    private boolean flag = false;
    @Override
    protected void sendStart(DeviceDetail deviceDetail, int pos) {
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

    @Override
    protected void onResume() {
        super.onResume();
        RadioManager.getInstance().setOnRadioArrived(resultJump);
        getState();
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
    protected void updateTime(int time, int pos) {
        deviceDetails.get(pos).setTestTime(time);
//        deviceListAdapter.notifyItemChanged(pos);

    }

    @Override
    public int setTestCount() {
        return 2;
    }

    @Override
    public int setTestPattern() {
        return 0;
    }
    private String getPenalty(int penaltyNum) {
        if (penaltyNum == 0){
            return "(判罚:"+penaltyNum+")";
        }else {
            return "(判罚:-"+penaltyNum+")";
        }

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
    private boolean joinNext(DeviceDetail deviceDetail, int pos) {

        return true;
    }

}
