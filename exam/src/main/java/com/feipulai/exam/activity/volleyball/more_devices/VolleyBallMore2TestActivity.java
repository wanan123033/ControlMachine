package com.feipulai.exam.activity.volleyball.more_devices;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.VolleyBallRadioManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.beans.VolleyPair868Result;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.pair.VolleyBallPairActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;
import com.feipulai.exam.activity.volleyball.VolleyBallSettingActivity;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;

import java.util.concurrent.Executors;

import butterknife.OnClick;

public class VolleyBallMore2TestActivity extends BaseVolleyBallMoreActivity {

    private static final int SEND_EMPTY = 1;
    private static final int END_JS = 2;
    private static final int GET_STATE = 3;
    private static final int TEST_TIME = 8;
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
    private String[] scoreStr = new String[3];
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
                            if (state == VolleyPair868Result.STATE_TIME_PREPARE || state == VolleyPair868Result.STATE_COUNT_PREPARE || state == VolleyPair868Result.STATE_FREE){
                                stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
                                scoreStr[1] = "设备空闲";
                            }else if (state == VolleyPair868Result.STATE_TIMING || state == VolleyPair868Result.STATE_COUNTING){
                                stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
                                scoreStr[1] = "计数中";
                            }else if (state == VolleyPair868Result.STATE_TIME_END || state == VolleyPair868Result.STATE_COUNT_END){
                                stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_END);
                                scoreStr[1] = "计数已结束";
                            }
                            stuDevicePair.getBaseDevice().setBatteryLeft(result.getElectricityState());
                            scoreStr[0] = ResultDisplayUtils.getStrResultForDisplay(result.getScore());
                            scoreStr[2] = "";
                            stuDevicePair.setTimeResult(scoreStr);
                            updateResult(stuDevicePair);
                            break;
                        }
                    }
                    break;
                case TEST_TIME:
                    testTime(msg.arg1,msg.arg2, (DeviceDetail) msg.obj);
                    break;
            }
            return false;
        }
    });

    private void testTime(int pos,int time,DeviceDetail deviceDetail) {
        updateTime(time,pos);

        Message msg1 = Message.obtain();
        msg1.what = TEST_TIME;
        msg1.arg1 = pos;
        msg1.arg2 = time - 1;
        msg1.obj = deviceDetail;
        if (msg1.arg2 >= 0) {
            Log.e("TAG","msg.arga2 = "+ msg1.arg2);
            mHandler.sendMessageDelayed(msg1, 1000);
        }else {
            Log.e("TAG","msg.arga2 jishi= "+ msg1.arg2+",deviceDetail = "+deviceDetail);
            sendEnd((DeviceDetail) msg1.obj,pos);
        }
    }



    VolleyBallSetting setting;

    @Override
    protected void onResume() {
        super.onResume();
        setting = SharedPrefsUtil.loadFormSource(this, VolleyBallSetting.class);
        RadioManager.getInstance().setOnRadioArrived(resultJump);
    }

    private void getState() {
        for (int i = 0 ; i < deviceDetails.size() ; i++) {
            BaseDeviceState device = deviceDetails.get(i).getStuDevicePair().getBaseDevice();
            int deviceId = device.getDeviceId();
            int hostId = SettingHelper.getSystemSetting().getHostId();
            VolleyBallRadioManager.getInstance().getState(hostId,deviceId);
        }
        mHandler.sendEmptyMessageDelayed(SEND_EMPTY,1000);
    }

    @Override
    protected void gotoItemSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择");
        builder.setItems(new String[]{"一号机", "二号机", "三号机", "四号机"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), VolleyBallSettingActivity.class);
                intent.putExtra("deviceId",which + 1);
                startActivity(intent);
            }
        });
        builder.create().show();
    }

    @Override
    public void sendStart(DeviceDetail deviceDetail, int pos) {

        int hostId = (byte) SettingHelper.getSystemSetting().getHostId();
        int deviceId = (byte) deviceDetail.getStuDevicePair().getBaseDevice().getDeviceId();
        VolleyBallRadioManager.getInstance().start(hostId,deviceId);
        //TODO 开始计时
        if (setting.getTestTime() > 0){
            Message msg1 = Message.obtain();
            msg1.what = TEST_TIME;
            msg1.arg1 = pos;
            msg1.obj = deviceDetail;
            msg1.arg2 = setting.getTestTime();
            mHandler.sendMessage(msg1);
        }
        getState();
    }

    @Override
    public void sendEnd(final DeviceDetail deviceDetail, final int pos) {
        mHandler.removeMessages(SEND_EMPTY);
        SystemClock.sleep(200);

        int hostId = SettingHelper.getSystemSetting().getHostId();
        int deviceId = (byte)  deviceDetail.getStuDevicePair().getBaseDevice().getDeviceId();
        VolleyBallRadioManager.getInstance().stop(hostId,deviceId);
        SystemClock.sleep(100);
        VolleyBallRadioManager.getInstance().deviceFree(hostId,deviceId);
        mHandler.removeMessages(END_JS);

        SystemClock.sleep(200);
        BaseStuPair stuDevicePair = deviceDetail.getStuDevicePair();
        stuDevicePair.getBaseDevice().setState(BaseDeviceState.STATE_END);
        updateResult(stuDevicePair);

        mHandler.removeCallbacksAndMessages(null);
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
                sendEnd(deviceDetail,pos);
                sendConfirm(deviceDetail,pos);
                dialog.dismiss();

            }
        });

        builder.setNegativeButton("放弃", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                sendEnd(deviceDetail,pos);
                stuSkip(pos);
                deviceDetails.get(pos).getStuDevicePair().setStudent(null);
                deviceListAdapter.notifyItemChanged(pos);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void sendConfirm(DeviceDetail deviceDetail, int pos) {
        stuSkip(pos);
        deviceDetail.getStuDevicePair().setStudent(null);

        updateResult(deviceDetail.getStuDevicePair());
    }

    @Override
    public void sendPenalty(DeviceDetail deviceDetail, int pos) {
        showPenalizeDialog(deviceDetail,pos);
    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
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

        new AlertDialog.Builder(this).setTitle("请输入判罚值")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int value = -1 * numberPicker.getValue();
                        pair.getStuDevicePair().setResult(value + pair.getStuDevicePair().getResult());
                        toastSpeak("判罚成功");
                        sendConfirm(pair,pos);
                    }
                })
                .setNegativeButton("返回", null).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceDetails.clear();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void clearHandler() {
        deviceDetails.clear();
        setDeviceCount(4);
        mHandler.removeCallbacksAndMessages(null);
    }
}
