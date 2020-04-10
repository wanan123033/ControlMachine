package com.feipulai.exam.activity.ranger;


import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.spputils.OnDataReceivedListener;
import com.feipulai.device.spputils.SppState;
import com.feipulai.device.spputils.SppUtils;
import com.feipulai.device.spputils.beans.RangerResult;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;

public class RangerTestActivity extends BaseTestActivity {
    SppUtils utils;
    RangerSetting setting;

    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            boolean connected = utils.isConnected();
            if (!connected){
                pair.getBaseDevice().setState(BaseDeviceState.STATE_ERROR);
            }else {
                pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
            }
            refreshDevice();
            handler.sendEmptyMessageDelayed(222,500);
        }
    };

    @Override
    protected void initData() {
        super.initData();
        setting = SharedPrefsUtil.loadFormSource(getApplicationContext(),RangerSetting.class);
        utils = BluetoothManager.getSpp(this);
        utils.setupService();
        utils.startService();
        utils.setOnDataReceivedListener(new OnDataReceivedListener() {
            @Override
            protected void onResult(byte[] datas) {
                onResults(datas);
            }
        });
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
        handler.sendEmptyMessageDelayed(222,500);
    }

    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {
        if (utils.isConnected()) {
            byte[] bytes = new byte[]{0x5A, 0x33, 0x34, 0x30, 0x39, 0x33, 0x03, 0x0d, 0x0a};
            utils.send(bytes, false);
            bytes = new byte[]{0x43, 0x30, 0x36, 0x37, 0x03, 0x0d, 0x0a};
            utils.send(bytes, false);
            updateTestBtnState();
        }else {
            ToastUtils.showLong("请先连接激光测距仪");
        }
    }

    @Override
    public int setTestCount() {
        return 2;
    }

    @Override
    public void gotoItemSetting() {
        startActivity(new Intent(this,RangerSettingActivity.class));
    }

    @Override
    public void stuSkip() {
        pair.setStudent(null);
        pair.setResult2(0.0);
        refreshTxtStu(null);

    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
        return false;
    }

    private void onResults(byte[] datas) {
        RangerResult result = new RangerResult(datas);
        double result1 = result.getResult();
        if (result.getType() == 1){
            pair.setResult2(result1);
            updateResult(pair);
        }

    }
    public void onDestroy() {
        super.onDestroy();
        //断开蓝牙连接
        if(utils.getServiceState() == SppState.STATE_CONNECTED){
            utils.disconnect();
        }
        //停止服务
        utils.stopService();
    }
}
