package com.feipulai.exam.activity.ranger;

import android.content.DialogInterface;
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
import com.feipulai.exam.activity.ranger.bluetooth.BluetoothManager;
import com.feipulai.exam.utils.Toast;
import com.feipulai.exam.view.OperateProgressBar;

public class RangerMoreActivity extends BaseMoreTestActivity {
    private RangerSetting setting;
    private SppUtils utils;
    private BaseStuPair pair = new BaseStuPair();
    private static final int GET_BLUETOOTH = 222;
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
            handler.sendEmptyMessageDelayed(GET_BLUETOOTH,500);
        }
    };

    @Override
    public void initData() {
        setting = SharedPrefsUtil.loadFormSource(getApplicationContext(),RangerSetting.class);
        utils = BluetoothManager.getSpp(this);
        if (!utils.isBluetoothEnabled()) {
            utils.enable();
        }
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
        if (setting.getBluetoothName() != null && setting.getBluetoothMac() != null && !utils.isConnected()){
            OperateProgressBar.showLoadingUi(this,"正在重连蓝牙:"+setting.getBluetoothName());
            utils.connect(setting.getBluetoothMac());
            utils.setBluetoothConnectionListener(new SppUtils.BluetoothConnectionListener() {
                @Override
                public void onDeviceConnected(String name, String address) {
                    OperateProgressBar.removeLoadingUiIfExist(RangerMoreActivity.this);
                    Toast.showToast(getApplicationContext(),"已连接 "+name,Toast.LENGTH_LONG);
                }

                @Override
                public void onDeviceDisconnected() {
                    OperateProgressBar.removeLoadingUiIfExist(RangerMoreActivity.this);
                    Toast.showToast(getApplicationContext(),"连接断开",Toast.LENGTH_LONG);
                }

                @Override
                public void onDeviceConnectionFailed() {
                    OperateProgressBar.removeLoadingUiIfExist(RangerMoreActivity.this);
                    Toast.showToast(getApplicationContext(),"连接失败,请重启蓝牙",Toast.LENGTH_LONG);
                }
            });
        }
        //循环获取蓝牙状态
        handler.sendEmptyMessageDelayed(GET_BLUETOOTH,500);
    }

    private void onResults(byte[] datas) {
        RangerResult result = new RangerResult(datas);
        if (result.getType() == 1){
            setScore(result,pair);
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
    public void startTest(BaseStuPair stuPair) {
        if (roundNo > setting.getTestNo()){
            ToastUtils.showLong("测试已完成");
            return;
        }
        if (utils.isConnected()) {
            stuPair.setTestTime(System.currentTimeMillis()+"");
            byte[] bytes = new byte[]{0x5A, 0x33, 0x34, 0x30, 0x39, 0x33, 0x03, 0x0d, 0x0a};
            utils.send(bytes, false);
            bytes = new byte[]{0x43, 0x30, 0x36, 0x37, 0x03, 0x0d, 0x0a};
            utils.send(bytes, false);

        }else {
            ToastUtils.showLong("请先连接激光测距仪");
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
        handler.removeMessages(GET_BLUETOOTH);
        utils = null;
        BluetoothManager.spp = null;
    }

    @Override
    public int setTestPattern() {
        return setting.getTestPattern();
    }

    @Override
    protected void commitTest() {
        updatePair(pair.getBaseDevice(), pair, false);
    }

    @Override
    protected void showPenalize() {
        showPenalize(pair.getBaseDevice(),pair);
    }

    @Override
    protected void fgStuTest() {
        pair.setResultState(2);
        updateTestResult(pair);
    }

    public void refreshDevice() {
        if (pair.getBaseDevice() != null) {
            if (pair.getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
                cbDeviceState.setChecked(true);
            } else {
                cbDeviceState.setChecked(false);
            }
        }
    }
    @Override
    public void penalize(int value) {
        pair.setResult(pair.getResult()+value*100);

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
