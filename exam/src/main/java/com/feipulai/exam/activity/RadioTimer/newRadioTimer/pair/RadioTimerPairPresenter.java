package com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair;

import android.content.Context;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.exam.activity.RadioTimer.RunTimerSetting;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.check.CheckUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;

import java.util.List;

public class RadioTimerPairPresenter implements RadioContract.Presenter,
            RadioManager.OnRadioArrivedListener,
        RadioLinker.RadioPairListener {
        private boolean isAutoPair;
        private Context context;
        private RadioContract.View view;
        public volatile int focusPosition;
        private List<StuDevicePair> pairs;
        public int machineCode = TestConfigs.sCurrentItem.getMachineCode();
        public final int TARGET_FREQUENCY = SettingHelper.getSystemSetting().getUseChannel();
        public RadioLinker linker;
        private int deviceSum;
        private SportTimerManger manager;
        private RunTimerSetting setting;

        public RadioTimerPairPresenter(Context context, RadioContract.View view,int deviceSum) {
                this.context = context;
                this.view = view;
                this.deviceSum = deviceSum;
                manager = new SportTimerManger();
                setting = SharedPrefsUtil.loadFormSource(context, RunTimerSetting.class);
                isAutoPair = setting.isAutoPair();
        }

        @Override
        public void start(int deviceId) {
            pairs = CheckUtils.newPairs(deviceSum);
            RadioManager.getInstance().setOnRadioArrived(this);
            if (linker==null){
                linker = new RadioLinker(machineCode, TARGET_FREQUENCY, this);
                linker.startPair(deviceId);
            }

        }

        public List<StuDevicePair> getPairs(){
            return pairs;
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
        public  void changeAutoPair(boolean isAutoPair){
            this.isAutoPair = isAutoPair;
        }

        public void setFrequency(int deviceId,  int hostId, int targetFrequency){
            manager.setFrequency(deviceId,targetFrequency,hostId,SettingHelper.getSystemSetting().getHostId());
        }

        @Override
        public  void saveSettings(){
            setting.setAutoPair(isAutoPair);
        }

        @Override
        public void stopPair() {
            linker.cancelPair();
            RadioManager.getInstance().setOnRadioArrived(null);
        }

        public void onNewDeviceConnect() {
            pairs.get(focusPosition).getBaseDevice().setState(BaseDeviceState.STATE_FREE);
            view.updateSpecificItem(focusPosition);
            if (isAutoPair && focusPosition != pairs.size() - 1) {
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
            view.showToast("未收到子机回复,设置失败,请重试");
        }

}
