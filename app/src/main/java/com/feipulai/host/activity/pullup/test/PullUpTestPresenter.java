package com.feipulai.host.activity.pullup.test;

import android.content.Context;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.JumpRopeManager;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.IDeviceResult;
import com.feipulai.device.serial.beans.PullUpStateResult;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.host.R;
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.jump_rope.base.test.AbstractRadioTestPresenter;
import com.feipulai.host.activity.jump_rope.base.test.RadioTestContract;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.activity.pullup.setting.PullUpSetting;
import com.feipulai.host.config.TestConfigs;

/**
 * Created by James on 2019/1/22 0022.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PullUpTestPresenter
        extends AbstractRadioTestPresenter<PullUpSetting>
        implements RadioTestContract.Presenter,
        SitPullLinker.SitPullPairListener {

    private int machineCode = TestConfigs.sCurrentItem.getMachineCode();
    private PullUpTestContract.View<PullUpSetting> view;
    private final PullUpManager deviceManager;

    protected PullUpTestPresenter(Context context, PullUpTestContract.View<PullUpSetting> view) {
        super(context,view);
        this.view = view;
        setting = SharedPrefsUtil.loadFormSource(context, PullUpSetting.class);
        deviceManager = new PullUpManager();
    }

    @Override
    protected int getCountStartTime() {
        return JumpRopeManager.DEFAULT_COUNT_DOWN_TIME;
    }

    @Override
    protected int getCountFinishTime() {
        return 10;
    }

    @Override
    protected PullUpSetting getSetting() {
        return setting;
    }

    @Override
    protected int getTestTimeFromSetting() {
        return setting.getTestTime();
    }

    @Override
    protected void resetDevices() {
        for (StuDevicePair pair: pairs) {
            deviceManager.endTest(pair.getBaseDevice().getDeviceId());
        }
    }

    @Override
    protected void testCountDown(long tick) {
        for (StuDevicePair pair: pairs) {
            deviceManager.startTest(pair.getBaseDevice().getDeviceId(),(int) tick, setting.getTestTime(),0);
        }
    }

    @Override
    public void onGettingState(int position) {
        BaseDeviceState deviceState = pairs.get(position).getBaseDevice();
        deviceManager.getState(deviceState.getDeviceId());
    }

    @Override
    public String generate(int position) {
        return InteractUtils.generateLEDTestString(pairs, position);
    }
    @Override
    public int ledColor(int position) {
        return 0;
    }
    @Override
    public void onRadioArrived(Message msg) {

        int what = msg.what;
        if (machineCode == ItemDefault.CODE_YTXS && what == SerialConfigs.PULL_UP_GET_STATE) {
            PullUpStateResult stateResult = (PullUpStateResult) msg.obj;
            setState(stateResult);
        }
    }


    private void setState(PullUpStateResult stateResult) {
        setState(stateResult.getDeviceId(), stateResult.getState(), stateResult.getBatteryLeft(), stateResult);
    }

    private void setState(int deviceId, int deviceState, int batteryLeft, IDeviceResult result) {
        // Log.i("james", "deviceId:" + deviceId + "\tdeviceState:" + deviceState + "\tresult:" + result.getResult());
        // 非当前范围内设备手柄w
        if (deviceId >= deviceIdPIV.length
                || deviceIdPIV[deviceId] == INVALID_PIV) {
            return;
        }

        int piv = deviceIdPIV[deviceId];

        StuDevicePair pair = pairs.get(piv);
        BaseDeviceState originState = pair.getBaseDevice();

        if (originState.getState() == BaseDeviceState.STATE_STOP_USE) {
            return;
        }

        // WAIT_BGIN 和 TEST_COUNTING 状态没有成绩|| testState == TEST_COUNTING
        if (testState == WAIT_BGIN) {
            pair.setDeviceResult(null);
        } else if (pair.getStudent() != null) {
            IDeviceResult deviceResult = pair.getDeviceResult();
            int res = deviceResult == null ? 0 : deviceResult.getResult();
            // 成绩只能增加
            if (result.getResult() >= res) {
                pair.setDeviceResult(result);
            }
        }

        int newState;

        if (((machineCode == ItemDefault.CODE_YWQZ||machineCode == ItemDefault.CODE_SGBQS) && deviceState == SitPushUpManager.STATE_COUNTING)) {
            newState = BaseDeviceState.STATE_COUNTING;
        } else if (((machineCode == ItemDefault.CODE_YWQZ||machineCode == ItemDefault.CODE_SGBQS) && deviceState == SitPushUpManager.STATE_ENDED)) {
            newState = BaseDeviceState.STATE_FINISHED;
        } else {
            newState = batteryLeft <= 10 ? BaseDeviceState.STATE_LOW_BATTERY : BaseDeviceState.STATE_FREE;
        }
        originState.setState(newState);
        view.updateSpecificItem(piv);
        currentConnect[deviceId]++;
        if (testState == WAIT_MACHINE_RESULTS || testState == WAIT_CONFIRM_RESULTS) {
            endGetResultPairs[deviceId]++;
        }
    }

    @Override
    public void onNoPairResponseArrived() {
        view.showToast(context.getString(R.string.no_reply_received_hint));
    }

    @Override
    public void onNewDeviceConnect() {
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        deviceManager.setFrequency(originFrequency, deviceId, deviceFrequency);
    }




}
