package com.feipulai.host.activity.jump_rope.test;


import android.content.Context;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.JumpRopeManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.IDeviceResult;
import com.feipulai.device.serial.beans.JumpRopeResult;
import com.feipulai.host.R;
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.jump_rope.base.test.AbstractRadioTestPresenter;
import com.feipulai.host.activity.jump_rope.base.test.RadioTestContract;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.JumpDeviceState;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.host.entity.Student;

import java.util.Locale;

/**
 * Created by James on 2019/1/22 0022.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class JumpRopeTestPresenter
        extends AbstractRadioTestPresenter<JumpRopeSetting> {

    private JumpRopeManager deviceManager;

    JumpRopeTestPresenter(Context context, RadioTestContract.View<JumpRopeSetting> view) {
        super(context, view);
        deviceManager = new JumpRopeManager();
        setting = SharedPrefsUtil.loadFormSource(context, JumpRopeSetting.class);
    }

    @Override
    protected int getCountFinishTime() {
        return JumpRopeManager.DEFAULT_COUNT_DOWN_TIME;
    }

    @Override
    protected int getCountStartTime() {
        return JumpRopeManager.DEFAULT_COUNT_DOWN_TIME;
    }

    @Override
    protected JumpRopeSetting getSetting() {
        return setting;
    }

    @Override
    protected int getTestTimeFromSetting() {
        return setting.getTestTime();
    }

    @Override
    protected void resetDevices() {
        deviceManager.endTest(hostId, setting.getDeviceGroup() + 1);
    }

    @Override
    protected void testCountDown(long tick) {
        deviceManager.countDown(hostId, setting.getDeviceGroup() + 1, (int) tick, setting.getTestTime());
    }

    @Override
    public String generate(int position) {
        if (pairs.get(position).getStudent() == null) {
            return null;
        }
        String showContent;
        StuDevicePair pair = pairs.get(position);
        int deviceId = pair.getBaseDevice().getDeviceId();
        if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_STOP_USE) {
            showContent = String.format(Locale.CHINA, "%1s%-3d%-12s",
                    SerialConfigs.GROUP_NAME[setting.getDeviceGroup()],
                    deviceId,
                    context.getString(R.string.stop_use));
        } else {
            Student student = pair.getStudent();
            String studentName = InteractUtils.getStrWithLength(student.getStudentName(), 4);
            int result = InteractUtils.getResultInt(pair);
            showContent = String.format(Locale.CHINA, "%1s%-3d" + studentName + "%-4d",
                    SerialConfigs.GROUP_NAME[setting.getDeviceGroup()],
                    deviceId,
                    result);
        }
        return showContent;
    }

    @Override
    public int stateOfPosition(int position) {
        return pairs.get(position).getBaseDevice().getState();
    }

    @Override
    public void setFocusPosition(int position) {
        focusPosition = position;
    }


    @Override
    public void onGettingState(int position) {
        BaseDeviceState deviceState = pairs.get(position).getBaseDevice();
        deviceManager.getJumpRopeState(hostId, deviceState.getDeviceId(), setting.getDeviceGroup() + 1);
    }

    public void setDeviceState(JumpRopeResult result) {
        // Log.i("james", "JumpRopeResult:" + result.toString());
        int deviceId = result.getHandId();
        // 不同组 非当前范围内手柄
        if (result.getHandGroup() != setting.getDeviceGroup() + 1
                || deviceId >= deviceIdPIV.length
                || deviceIdPIV[deviceId] == INVALID_PIV) {
            return;
        }
        int piv = deviceIdPIV[deviceId];
        StuDevicePair pair = pairs.get(deviceIdPIV[deviceId]);
        JumpDeviceState originState = (JumpDeviceState) pair.getBaseDevice();

        if (result.getFactoryId() != originState.getFactoryId()
                || originState.getState() == BaseDeviceState.STATE_STOP_USE) {
            return;
        }
        if (pair.getStudent() != null) {
            IDeviceResult deviceResult = pair.getDeviceResult();
            int res = deviceResult == null ? 0 : deviceResult.getResult();
            // Log.i("中断次数","" + result.getStumbleTimes());
            // 成绩只能增加
            if (result.getResult() >= res) {
                pair.setDeviceResult(result);
            }
        }

        int newState;
        switch (result.getState()) {

            case JumpRopeManager.JUMP_ROPE_COUNTING:
                newState = BaseDeviceState.STATE_COUNTING;
                break;

            case JumpRopeManager.JUMP_ROPE_FINISHED:
                newState = BaseDeviceState.STATE_FINISHED;
                break;

            default:
                newState = result.getBatteryLeftPercent() <= 10 ? BaseDeviceState.STATE_LOW_BATTERY : BaseDeviceState.STATE_FREE;
                break;
        }
        originState.setState(newState);
        view.updateSpecificItem(piv);
        currentConnect[deviceId]++;
    }

    @Override
    public void onRadioArrived(Message msg) {
        if (msg.what != SerialConfigs.JUMPROPE_RESPONSE) {
            return;
        }
        JumpRopeResult result = (JumpRopeResult) msg.obj;
        setDeviceState(result);
    }

}
