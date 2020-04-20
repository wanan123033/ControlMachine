package com.feipulai.exam.activity.ranger;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.spputils.OnDataReceivedListener;
import com.feipulai.device.spputils.SppUtils;
import com.feipulai.device.spputils.beans.RangerResult;
import com.feipulai.exam.activity.base.PenalizeDialog;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.ranger.bluetooth.BluetoothManager;

public class RangerTestActivity extends BaseTestActivity  implements PenalizeDialog.PenalizeListener{
    private static final int GET_BLUETOOTH = 222;
    private static final int TEST = 333;
    SppUtils utils;
    RangerSetting setting;

    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == GET_BLUETOOTH) {
                boolean connected = utils.isConnected();
                if (!connected) {
                    pair.getBaseDevice().setState(BaseDeviceState.STATE_ERROR);
                } else {
                    pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                }
                refreshDevice();
                handler.sendEmptyMessageDelayed(GET_BLUETOOTH, 500);
            }else if (msg.what == TEST){
                BaseStuPair pair = (BaseStuPair) msg.obj;
                if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN || pair.getBaseDevice().getState() == BaseDeviceState.STATE_FREE) {
                    sendTestCommand(pair);
                    pair.setTestTime(System.currentTimeMillis()+"");
                }
            }
        }
    };

    @Override
    protected void initData() {
        super.initData();
        setting = SharedPrefsUtil.loadFormSource(getApplicationContext(),RangerSetting.class);
        utils = BluetoothManager.getSpp(this);
        if (utils.isBluetoothEnabled()) {
            utils.setupService();
            utils.startService();
            utils.setOnDataReceivedListener(new OnDataReceivedListener() {
                @Override
                protected void onResult(byte[] datas) {
                    onResults(datas);
                }
            });
        }else {
            utils.enable();
            utils.setupService();
            utils.startService();
            utils.setOnDataReceivedListener(new OnDataReceivedListener() {
                @Override
                protected void onResult(byte[] datas) {
                    onResults(datas);
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!utils.isBluetoothEnabled()) {
            //开启蓝牙
            utils.enable();
        } else {
            //开启服务
            if(!utils.isServiceAvailable()) {
                utils.setupService();
                utils.startService();
            }
        }

        //循环获取蓝牙状态
        handler.sendEmptyMessageDelayed(GET_BLUETOOTH,500);
    }

    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {
        if (testNo > setting.getTestNo()){
            ToastUtils.showLong("测试已完成");
            stuSkip();
            return;
        }
        Log.e("TAG----",pair.getStudent()+"--"+utils.isConnected());
        if (baseStuPair.getStudent() != null) {
            if (utils.isConnected()) {

                byte[] bytes = new byte[]{0x5A, 0x33, 0x34, 0x30, 0x39, 0x33, 0x03, 0x0d, 0x0a};
                utils.send(bytes, false);
                bytes = new byte[]{0x43, 0x30, 0x36, 0x37, 0x03, 0x0d, 0x0a};
                utils.send(bytes, false);
                if (testNo < setting.getTestNo() && setting.getAutoTestTime() > 0){  //开启自动测距
                    Message msg = Message.obtain();
                    msg.obj = baseStuPair;
                    msg.what = TEST;
                    handler.sendMessageDelayed(msg,setting.getAutoTestTime() * 1000);
                    return;
                }
                updateTestBtnState();
            } else {
                ToastUtils.showLong("请先连接激光测距仪");
            }
        }else {
            ToastUtils.showLong("请添加学生测试");
        }
    }

    @Override
    public int setTestCount() {
        return setting.getTestNo();
    }

    @Override
    public void gotoItemSetting() {
        startActivity(new Intent(this,RangerSettingActivity.class));
    }

    @Override
    public void stuSkip() {
        pair.setStudent(null);
        pair.setResult(0);
        refreshTxtStu(null);
    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
        return false;
    }

    @Override
    protected void confrim() {
        saveResult(pair);
        updateInitBtnState();
        Log.e("TAG----","testNo="+testNo+",setting.getTestNo()="+setting.getTestNo());

        if (testNo == setting.getTestNo() + 1){
            testNo = 1;
            stuSkip();
        }
    }

    private void onResults(byte[] datas) {
        RangerResult result = new RangerResult(datas);
        int result1 = result.getResult();
        if (result.getType() == 1){
            setScore(result1);
        }

    }
    public void onDestroy() {
        super.onDestroy();
        //断开蓝牙连接
        if (utils.isConnected()){
            utils.disconnect();
        }
        //停止服务
        utils.stopService();
        handler.removeMessages(GET_BLUETOOTH);
        utils = null;
        BluetoothManager.spp = null;
    }

    @Override
    public void showPenalize() {
        if (setting.isPenglize()) {
            PenalizeDialog dialog = new PenalizeDialog(this);
            dialog.setPenalizeListener(this);
            dialog.setMinMaxValue(-1, 1);
            dialog.show();
        }
    }
    @Override
    public void penalize(int value) {
        pair.setResult(pair.getResult()+value*100);
        updateResult(pair);
    }

    @Override
    public void dismisson(DialogInterface dialog) {
        dialog.dismiss();
    }

    @Override
    public boolean getPenalize() {
        return true;
    }
}
