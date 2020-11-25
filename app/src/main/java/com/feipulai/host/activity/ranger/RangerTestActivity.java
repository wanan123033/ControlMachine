package com.feipulai.host.activity.ranger;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.spputils.OnDataReceivedListener;
import com.feipulai.device.spputils.SppUtils;
import com.feipulai.device.spputils.beans.RangerResult;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.freedom.BaseFreedomTestActivity;
import com.feipulai.host.activity.ranger.bluetooth.BluetoothManager;
import com.feipulai.host.utils.StringUtility;
import com.feipulai.host.view.OperateProgressBar;
import com.orhanobut.logger.utils.LogUtils;

public class RangerTestActivity extends BaseFreedomTestActivity {
    private static final int GET_BLUETOOTH = 222;
    public static final int WHAT = 43;
    private SppUtils utils;
    private RangerSetting setting;

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT) {
                startTest();
            }else {
                boolean connected = utils.isConnected();
                if (!connected) {
                    setDeviceState(new BaseDeviceState(BaseDeviceState.STATE_ERROR));
                } else {
                    setDeviceState(new BaseDeviceState(BaseDeviceState.STATE_FREE));
                }
                sendEmptyMessageDelayed(GET_BLUETOOTH,500);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.txt_led_setting).setVisibility(View.GONE);
    }

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
                    OperateProgressBar.removeLoadingUiIfExist(RangerTestActivity.this);
                    Toast.makeText(getApplicationContext(),"已连接 "+name,Toast.LENGTH_LONG).show();
                }

                @Override
                public void onDeviceDisconnected() {
                    OperateProgressBar.removeLoadingUiIfExist(RangerTestActivity.this);
                    Toast.makeText(getApplicationContext(),"连接断开",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onDeviceConnectionFailed() {
                    OperateProgressBar.removeLoadingUiIfExist(RangerTestActivity.this);
                    Toast.makeText(getApplicationContext(),"连接失败,请重启蓝牙",Toast.LENGTH_LONG).show();
                }
            });
        }
        //循环获取蓝牙状态
        handler.sendEmptyMessageDelayed(GET_BLUETOOTH,500);
    }

    @Override
    public void gotoItemSetting() {
        startActivity(new Intent(this,RangerSettingActivity.class));
    }

    @Override
    public void startTest() {
        if (utils.isConnected()) {
            byte[] bytes = new byte[]{0x5A, 0x33, 0x34, 0x30, 0x39, 0x33, 0x03, 0x0d, 0x0a};
            utils.send(bytes, false);
            bytes = new byte[]{0x43, 0x30, 0x36, 0x37, 0x03, 0x0d, 0x0a};
            LogUtils.normal(bytes.length+"---"+ StringUtility.bytesToHexString(bytes)+"---激光测距仪开始指令");
            utils.send(bytes, false);
            LogUtils.normal(bytes.length+"---"+ StringUtility.bytesToHexString(bytes)+"---激光测距仪开始指令");
            if (setting.getAutoTestTime() > 0){
                handler.sendEmptyMessageDelayed(WHAT,setting.getAutoTestTime() * 1000);
            }
        } else {
            ToastUtils.showLong("请先连接激光测距仪");
        }
    }

    @Override
    public void stopTest() {
        handler.removeMessages(WHAT);
    }
    private void onResults(byte[] datas) {
        RangerResult result = new RangerResult(datas);
        if (result.getType() == 1){
            int results = calculation(result,setting);
            setScore(results);
        }
    }

    private int calculation(RangerResult result, RangerSetting rangerSetting) {
        int itemType = rangerSetting.getItemType();
        if (itemType == 2 || itemType == 3 || itemType == 4){ //跳远类项目
            double level1 = rangerSetting.getLevel1();
            double level2 = rangerSetting.getLevel2();
            Point jidian1 = RangerUtil.getPoint(level1,rangerSetting.getQd1_hor());
            Point jidian2 = RangerUtil.getPoint(level2,rangerSetting.getQd2_hor());
            double level = RangerUtil.level(result.getLevel_d(),result.getLevel_g(),result.getLevel_m());
            Point p = RangerUtil.getPoint(level,result.getResult());
            double length = RangerUtil.length(jidian1, jidian2, p);
            return (int) length;
        }else if (itemType == 0 || itemType == 1){   //跳高类项目
            return result.getResult();
        }else if (itemType == 5 || itemType == 6 || itemType == 7 || itemType == 8){  //投掷类项目
            double dd = RangerUtil.level(result.getLevel_d(),result.getLevel_g(),result.getLevel_m());
            double inclination = RangerUtil.inclination(rangerSetting.getLevel(), dd);
            double length = RangerUtil.cosine(inclination, rangerSetting.getQd_hor(), result.getResult());
            return ((int) (length - rangerSetting.getRadius()));
        }
        return 0;
    }
}
