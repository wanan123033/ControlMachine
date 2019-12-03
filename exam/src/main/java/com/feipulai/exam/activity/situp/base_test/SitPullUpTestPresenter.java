package com.feipulai.exam.activity.situp.base_test;

import android.content.Context;
import android.os.Message;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.IDeviceResult;
import com.feipulai.device.serial.beans.PullUpStateResult;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.exam.activity.jump_rope.base.test.AbstractRadioTestPresenter;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.Student;
import com.orhanobut.logger.Logger;

/**
 * Created by James on 2019/1/22 0022.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public abstract class SitPullUpTestPresenter<Setting>
        extends AbstractRadioTestPresenter<Setting>
        implements SitPullUpTestContract.Presenter,
        SitPullLinker.SitPullPairListener {

    private int machineCode = TestConfigs.sCurrentItem.getMachineCode();
    private final int TARGET_FREQUENCY = SettingHelper.getSystemSetting().getUseChannel();
    private SitPullLinker linker;
    private SitPullUpTestContract.View<Setting> view;

    protected SitPullUpTestPresenter(Context context, SitPullUpTestContract.View<Setting> view) {
        super(context, view);
        this.view = view;
    }

    @Override
    public void changeBadDevice() {
        facade.pauseGettingState();
        if (linker == null) {
            linker = new SitPullLinker(machineCode, TARGET_FREQUENCY, this);
            linker.startPair(deviceIdPIV[focusPosition]);
        }
        view.showChangeBadDialog();
        mLinking = true;
    }

    @Override
    public void cancelChangeBad() {
        mLinking = false;
        linker.cancelPair();
        facade.resumeGettingState();
    }

    public synchronized void onNewDeviceConnect() {
        cancelChangeBad();
        view.changeBadSuccess();
    }

    public synchronized void onNoPairResponseArrived() {
        view.showToast("未收到子机回复,设置失败,请重试");
    }

    @Override
    public void onRadioArrived(Message msg) {
        if (mLinking && linker.onRadioArrived(msg)) {
            return;
        }
        int what = msg.what;
        if (machineCode == ItemDefault.CODE_YWQZ && what == SerialConfigs.SIT_UP_GET_STATE) {
            SitPushUpStateResult stateResult = (SitPushUpStateResult) msg.obj;
            setState(stateResult);
        } else if (machineCode == ItemDefault.CODE_YTXS && what == SerialConfigs.PULL_UP_GET_STATE) {
            PullUpStateResult stateResult = (PullUpStateResult) msg.obj;
            setState(stateResult);
        } else if (machineCode == ItemDefault.CODE_FWC && what == SerialConfigs.PUSH_UP_GET_STATE) {
            SitPushUpStateResult stateResult = (SitPushUpStateResult) msg.obj;
            Logger.i("SitPushUpStateResult===" + stateResult.toString());
            setState(stateResult);
        }
    }

    public abstract void setFrequency(int deviceId, int originFrequency, int deviceFrequency);

    protected abstract boolean canPenalize();

    private void setState(PullUpStateResult stateResult) {
        setState(stateResult.getDeviceId(), stateResult.getState(), stateResult.getBatteryLeft(), stateResult);
    }

    private void setState(SitPushUpStateResult stateResult) {
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

        // WAIT_BGIN 和 TEST_COUNTING 状态没有成绩
        if (testState == WAIT_BGIN || testState == TEST_COUNTING) {
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

        if ((machineCode == ItemDefault.CODE_YWQZ && deviceState == SitPushUpManager.STATE_COUNTING)
                || (machineCode == ItemDefault.CODE_YTXS && deviceState == PullUpManager.STATE_COUNTING)
                ||(machineCode == ItemDefault.CODE_FWC && deviceState == SitPushUpManager.STATE_COUNTING)) {
            newState = BaseDeviceState.STATE_COUNTING;
        } else if ((machineCode == ItemDefault.CODE_YWQZ && deviceState == SitPushUpManager.STATE_ENDED)
                || (machineCode == ItemDefault.CODE_YTXS && deviceState == PullUpManager.STATE_ENDED)
                || (machineCode == ItemDefault.CODE_FWC && deviceState == SitPushUpManager.STATE_ENDED)) {
            newState = BaseDeviceState.STATE_FINISHED;
        } else {
            newState = batteryLeft <= 10 ? BaseDeviceState.STATE_LOW_BATTERY : BaseDeviceState.STATE_FREE;
        }
        originState.setState(newState);
        view.updateSpecificItem(piv);
        currentConnect[deviceId]++;
    }

    @Override
    public void punish() {
        Student student = pairs.get(focusPosition).getStudent();
        if (student == null) {
            view.showToast("未找到考生,不能判罚");
            return;
        }
        int machineResult = pairs.get(focusPosition).getDeviceResult().getResult();
        view.showPenalizeDialog(machineResult);
    }

    @Override
    public void penalize(int number) {
        pairs.get(focusPosition).setPenalty(number);
        view.showToast("判罚成功");
    }

    @Override
    protected void onResultConfirmed() {
        view.enablePenalize(false);
    }

    @Override
    protected void onTestStarted() {
        for (StuDevicePair pair : pairs) {
            pair.setPenalty(0);
        }
        view.enablePenalize(false);
    }

    @Override
    protected void onMachineResultArrived() {
        // 判罚子类处理
        if (canPenalize()) {
            view.enablePenalize(true);
        }
    }

}
