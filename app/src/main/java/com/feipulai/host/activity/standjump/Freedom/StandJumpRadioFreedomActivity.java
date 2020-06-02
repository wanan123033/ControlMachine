package com.feipulai.host.activity.standjump.Freedom;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.StandJumpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.freedom.BaseFreedomTestActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.standjump.StandJumpSetting;
import com.feipulai.host.activity.standjump.StandJumpSettingActivity;
import com.feipulai.host.activity.standjump.more.StandJumpPairActivity;
import com.feipulai.host.activity.standjump.more.StandJumpRadioFacade;
import com.feipulai.host.bean.DeviceDetail;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

/**
 * Created by zzs on  2020/3/18
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandJumpRadioFreedomActivity extends BaseFreedomTestActivity implements StandJumpRadioFacade.StandJumpResponseListener {

    private StandJumpRadioFacade facade;
    private StandJumpSetting standJumpSetting;
    public List<DeviceDetail> deviceDetails = new ArrayList<>();
    private MyHandler mHandler = new MyHandler(this);

    @Override
    protected void initData() {
        standJumpSetting = SharedPrefsUtil.loadFormSource(this, StandJumpSetting.class);
        if (standJumpSetting == null)
            standJumpSetting = new StandJumpSetting();
        super.initData();
        txtDevicePair.setVisibility(View.VISIBLE);
        DeviceDetail detail = new DeviceDetail();
        detail.getStuDevicePair().getBaseDevice().setDeviceId(1);
        detail.getStuDevicePair().setTimeResult(new String[1]);
        detail.setDeviceOpen(true);
        deviceDetails.add(detail);
        facade = new StandJumpRadioFacade(deviceDetails, standJumpSetting, this);
        setDeviceState(new BaseDeviceState(BaseDeviceState.STATE_ERROR));
    }

    @Override
    public void gotoItemSetting() {
        IntentUtil.gotoActivity(this, StandJumpSettingActivity.class);
    }

    @Override
    public void startTest() {
        StandJumpManager.setLeisure(SettingHelper.getSystemSetting().getHostId(), 1);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        StandJumpManager.startTest(SettingHelper.getSystemSetting().getHostId(), 1);
    }

    @OnClick({R.id.txt_device_pair})
    public void devicePair(View view) {
        if (!isStartTest) {
            startActivity(new Intent(this, StandJumpPairActivity.class));
        }
    }

    @Override
    public void stopTest() {
        //设置当前设置为空闲状态
//        setDeviceState(new BaseDeviceState(BaseDeviceState.STATE_FREE));
    }


    @Override
    protected void onResume() {
        super.onResume();

        standJumpSetting = SharedPrefsUtil.loadFormSource(this, StandJumpSetting.class);
        if (standJumpSetting != null) {
            facade.setStandJumpSetting(standJumpSetting);
        }
        facade.resume();
        RadioManager.getInstance().setOnRadioArrived(facade);

    }

    @Override
    protected void onPause() {
        super.onPause();
        facade.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        facade.finish();
        facade = null;
        RadioManager.getInstance().setOnRadioArrived(null);
    }


    @Override
    public void refreshDeviceState(int deviceId) {
        BaseDeviceState state = new BaseDeviceState();
        state.setState(deviceDetails.get(deviceId).getStuDevicePair().getBaseDevice().getState());
        state.setDeviceId(1);
        Message msg = mHandler.obtainMessage();
        msg.obj = state;
        msg.what = UPDATE_DEVICE;
        mHandler.sendMessage(msg);
    }

    @Override
    public void getResult(BaseStuPair stuPair) {
        Message msg = mHandler.obtainMessage();
        msg.obj = stuPair;
        msg.what = UPDATE_RESULT;
        mHandler.sendMessage(msg);
    }

    @Override
    public void StartDevice(int deviceId) {
        //检测通过可以发送测试指令
        toastSpeak(getString(R.string.start_test));
    }

    @Override
    public void endDevice(BaseDeviceState deviceState) {

    }

    private static final int MSG_DISCONNECT = 0X101;
    private static final int UPDATE_DEVICE = 0X103;
    private static final int UPDATE_RESULT = 0X104;

    private static class MyHandler extends Handler {

        private WeakReference<StandJumpRadioFreedomActivity> mActivityWeakReference;

        public MyHandler(StandJumpRadioFreedomActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            StandJumpRadioFreedomActivity activity = mActivityWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_DISCONNECT://连接失败
                        activity.toastSpeak(activity.getString(R.string.device_noconnect));
                        //设置当前设置为不可用断开状态
                        activity.setDeviceState(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
                        break;
                    case UPDATE_DEVICE:
                        activity.setDeviceState((BaseDeviceState) msg.obj);
                        break;

                    case UPDATE_RESULT:
                        activity.settTestResult((BaseStuPair) msg.obj);
                        break;
                }
            }

        }
    }
}
