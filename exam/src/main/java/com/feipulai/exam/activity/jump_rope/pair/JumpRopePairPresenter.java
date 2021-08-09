package com.feipulai.exam.activity.jump_rope.pair;

import android.content.Context;
import android.os.Build;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.JumpRopeManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.JumpRopeResult;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.JumpDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.check.CheckUtils;
import com.feipulai.exam.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class JumpRopePairPresenter
        implements JumpRopePairContract.Presenter,
        RadioManager.OnRadioArrivedListener {

    private JumpRopeSetting setting;
    private Context context;
    private JumpRopePairContract.View view;
    private volatile int focusPosition;
    private List<StuDevicePair> pairs;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private JumpRopeManager jumpRopeManager;
    private volatile boolean mIsThreadStoped = false;
    private volatile boolean mIsConnecting = false;

    public JumpRopePairPresenter(Context context, JumpRopePairContract.View view) {
        this.context = context;
        setting = SharedPrefsUtil.loadFormSource(context, JumpRopeSetting.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }
        this.view = view;
    }

    @Override
    public void start() {
        jumpRopeManager = new JumpRopeManager();
        PairTask pairTask = new PairTask();
        executor.execute(pairTask);
        List<BaseStuPair> stuPairs = (List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu");
        pairs = CheckUtils.newPairs(setting.getDeviceSum(),stuPairs);
        view.initView(setting, pairs);
    }

    @Override
    public void changeFocusPosition(int position) {
        if (focusPosition == position){
            return;
        }
        focusPosition = position;
        pairs.get(position).getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
        view.select(position);
    }

    @Override
    public void changeDeviceGroup(int deviceGroup) {
        setting.setDeviceGroup(deviceGroup);
        for (StuDevicePair pair : pairs) {
            pair.getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
        }
        changeFocusPosition(0);
        view.updateAllItems();
    }

    @Override
    public void changeAutoPair(boolean isAutoPair) {
        setting.setAutoPair(isAutoPair);
    }

    @Override
    public void resumePair() {
        RadioManager.getInstance().setOnRadioArrived(this);
        mIsConnecting = true;
    }

    @Override
    public void pausePair() {
        mIsConnecting = false;
    }

    @Override
    public void stopPair() {
        mIsThreadStoped = true;
        executor.shutdown();
    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context, setting);
    }

    class PairTask implements Runnable {
        @Override
        public void run() {
            while (!mIsThreadStoped) {
                if (mIsConnecting) {
                    jumpRopeManager.link(SettingHelper.getSystemSetting().getUseChannel(),SettingHelper.getSystemSetting().getHostId(),
                            focusPosition + 1, 0x06,
                            setting.getDeviceGroup() + 1);
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRadioArrived(Message msg) {
        switch (msg.what) {
            case SerialConfigs.JUMPROPE_RESPONSE:
                JumpRopeResult result = (JumpRopeResult) msg.obj;
                int handId = result.getHandId();
                if (focusPosition != handId - 1
                        || setting.getDeviceGroup() != result.getHandGroup() - 1
                        || result.getState() != JumpRopeManager.JUMP_ROPE_SPARE) {
                    return;
                }
                JumpDeviceState originState = (JumpDeviceState) pairs.get(focusPosition).getBaseDevice();
                if (originState.getState() != BaseDeviceState.STATE_FREE) {
                    // 不是最后一个的时候,根据设置进行自动切换
                    originState.setState(BaseDeviceState.STATE_FREE);
                    view.updateSpecificItem(focusPosition);
                    if (setting.isAutoPair() && focusPosition != setting.getDeviceSum() - 1) {
                        changeFocusPosition(focusPosition + 1);
                        //这里先清除下一个的连接状态,避免没有连接但是现实已连接
                        originState = (JumpDeviceState) pairs.get(focusPosition).getBaseDevice();
                        originState.setState(BaseDeviceState.STATE_DISCONNECT);
                    }
                }
                break;
        }
    }

}
