package com.feipulai.host.activity.situp.pair;

import android.content.Context;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.host.R;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.activity.jump_rope.check.CheckUtils;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.situp.setting.SitUpSetting;
import com.feipulai.host.config.TestConfigs;

import java.util.List;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

@Deprecated
public class SitUpPairPresenter2
        implements SitUpPairContract.Presenter,
        RadioManager.OnRadioArrivedListener,
        SitPullLinker.SitPullPairListener {
    private SitUpSetting setting;
    private Context context;
    private SitUpPairContract.View view;
    private volatile int focusPosition;
    private List<StuDevicePair> pairs;
    private int machineCode = TestConfigs.sCurrentItem.getMachineCode();
    private final int TARGET_FREQUENCY = SettingHelper.getSystemSetting().getUseChannel();;
    private SitPullLinker linker;
    private SitPushUpManager sitPushUpManager;
    public SitUpPairPresenter2(Context context, SitUpPairContract.View view) {
        this.context = context;
        this.view = view;
        setting = SharedPrefsUtil.loadFormSource(context, SitUpSetting.class);
        sitPushUpManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP);
    }

    @Override
    public void start() {
        pairs = CheckUtils.newPairs(getDeviceSum());
        view.initView(isAutoPair(), pairs);
        RadioManager.getInstance().setOnRadioArrived(this);
        linker = new SitPullLinker(machineCode, TARGET_FREQUENCY, this);
        linker.startPair(1);
    }

    @Override
    public void changeFocusPosition(int position) {
        if (focusPosition == position) {
            return;
        }
        focusPosition = position;
        pairs.get(position).getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
        view.select(position);
        linker.startPair(focusPosition + 1);
    }

    @Override
    public void changeAutoPair(boolean isAutoPair) {
        setting.setAutoPair(isAutoPair);
    }

    protected int getDeviceSum() {
        return setting.getDeviceSum();
    }

    protected boolean isAutoPair(){
        return setting.isAutoPair();
    }

    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency){
        sitPushUpManager.setFrequency(SettingHelper.getSystemSetting().getUseChannel(),
                originFrequency,
                deviceId,
                SettingHelper.getSystemSetting().getHostId());
    }

    @Override
    public void saveSettings(){
        SharedPrefsUtil.save(context,setting);
    }

    @Override
    public void stopPair() {
        linker.cancelPair();
        RadioManager.getInstance().setOnRadioArrived(null);
    }

    public void onNewDeviceConnect() {
        pairs.get(focusPosition).getBaseDevice().setState(BaseDeviceState.STATE_FREE);
        view.updateSpecificItem(focusPosition);
        if (isAutoPair() && focusPosition != pairs.size() - 1) {
            changeFocusPosition(focusPosition + 1);
            //这里先清除下一个的连接状态,避免没有连接但是现实已连接
            BaseDeviceState originState = pairs.get(focusPosition).getBaseDevice();
            originState.setState(BaseDeviceState.STATE_DISCONNECT);
        }
    }

    @Override
    public void onRadioArrived(Message msg) {
        linker.onRadioArrived(msg);
    }

    public synchronized void onNoPairResponseArrived() {
        view.showToast(context.getString(R.string.no_reply_received_hint));
    }

}
