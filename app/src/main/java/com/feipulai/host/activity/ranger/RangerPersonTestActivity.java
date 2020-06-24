package com.feipulai.host.activity.ranger;


import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.spputils.OnDataReceivedListener;
import com.feipulai.device.spputils.SppUtils;
import com.feipulai.device.spputils.beans.RangerResult;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.person.BasePersonTestActivity;
import com.feipulai.host.activity.ranger.bluetooth.BluetoothManager;
import com.feipulai.host.view.OperateProgressBar;

import butterknife.BindView;

public class RangerPersonTestActivity extends BasePersonTestActivity {
    @BindView(R.id.tv_foul)
    TextView tv_foul;

    private static final int GET_BLUETOOTH = 222;
    public static final int WHAT = 43;
    private RangerSetting setting;
    private SppUtils utils;

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT) {
                sendTestCommand(pair);
            }else {
                boolean connected = utils.isConnected();
                if (!connected) {
                    pair.getBaseDevice().setState(BaseDeviceState.STATE_ERROR);
                } else {
                    pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                }
                refreshDevice();
                sendEmptyMessageDelayed(GET_BLUETOOTH,500);
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
        if (setting.getBluetoothName() != null && setting.getBluetoothMac() != null && !utils.isConnected()){
            OperateProgressBar.showLoadingUi(this,"正在重连蓝牙:"+setting.getBluetoothName());
            utils.connect(setting.getBluetoothMac());
            utils.setBluetoothConnectionListener(new SppUtils.BluetoothConnectionListener() {
                @Override
                public void onDeviceConnected(String name, String address) {
                    OperateProgressBar.removeLoadingUiIfExist(RangerPersonTestActivity.this);
                    Toast.makeText(getApplicationContext(),"已连接 "+name,Toast.LENGTH_LONG).show();
                }

                @Override
                public void onDeviceDisconnected() {
                    OperateProgressBar.removeLoadingUiIfExist(RangerPersonTestActivity.this);
                    Toast.makeText(getApplicationContext(),"连接断开",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onDeviceConnectionFailed() {
                    OperateProgressBar.removeLoadingUiIfExist(RangerPersonTestActivity.this);
                    Toast.makeText(getApplicationContext(),"连接失败,请重启蓝牙",Toast.LENGTH_LONG).show();
                }
            });
        }
        //循环获取蓝牙状态
        handler.sendEmptyMessageDelayed(GET_BLUETOOTH,500);
    }

    private void onResults(byte[] datas) {
        RangerResult result = new RangerResult(datas);
        int result1 = result.getResult();
        pair.setResult(result1);
        updateResult(pair);
    }

    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {
        tv_foul.setVisibility(View.VISIBLE);
    }

    @Override
    public void gotoItemSetting() {
        startActivity(new Intent(this,RangerSettingActivity.class));
    }

    @Override
    public void stuSkip() {
        tv_foul.setVisibility(View.GONE);
    }
}
