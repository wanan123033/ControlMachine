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
        private int point;//指的是位置只有起点 或只有终点时 0 ，起终点都有1
        public RadioTimerPairPresenter(Context context, RadioContract.View view,int deviceSum) {
                this.context = context;
                this.view = view;
                this.deviceSum = deviceSum;
                manager = new SportTimerManger();
                setting = SharedPrefsUtil.loadFormSource(context, RunTimerSetting.class);
                isAutoPair = setting.isAutoPair();
        }

        @Override
        public void start(int deviceId,int point) {
            pairs = CheckUtils.newPairs(deviceSum);
            RadioManager.getInstance().setOnRadioArrived(this);
            if (linker==null){
                linker = new RadioLinker(machineCode, TARGET_FREQUENCY, this);
                if (setting.getInterceptPoint() == 3 && point == 1){
                    linker.startPair(deviceId+Integer.parseInt(setting.getRunNum()));//有起终点的编号=deviceId+道次数
                }else {
                    linker.startPair(deviceId);
                }

            }

        }

        public void setPoint(int point){
            this.point = point;
        }

        public List<StuDevicePair> getPairs(){
            return pairs;
        }

        @Override
        public void changeFocusPosition(int position,int point) {
            if (focusPosition == position && this.point == point) {
                return;
            }
            this.point = point;
            focusPosition = position;
            if (point == 4 || point == 5){
                linker.startPair(0);//标记为辅助拦截器
                return;
            }
            pairs.get(position).getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
            view.select(position,point);
            if (setting.getInterceptPoint() == 3 && point == 1){
                linker.startPair(focusPosition + 1+Integer.parseInt(setting.getRunNum()));
            }else {
                linker.startPair(focusPosition + 1);
            }

        }

        @Override
        public  void changeAutoPair(boolean isAutoPair){
            this.isAutoPair = isAutoPair;
            setting.setAutoPair(isAutoPair);
        }

        public void setFrequency(int deviceId,  int deviceHostId, int targetFrequency){
            manager.setFrequency(deviceId,targetFrequency,deviceHostId,SettingHelper.getSystemSetting().getHostId());
        }

        @Override
        public  void saveSettings(){
            SharedPrefsUtil.save(context,setting);
        }

        @Override
        public void stopPair() {
            linker.cancelPair();
            RadioManager.getInstance().setOnRadioArrived(null);
        }

        public void onNewDeviceConnect() {
            view.updateSpecificItem(focusPosition,point);
            if (point<3){
                pairs.get(focusPosition).getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                if (isAutoPair && focusPosition != pairs.size() - 1) {
                    changeFocusPosition(focusPosition + 1,point);
                    //这里先清除下一个的连接状态,避免没有连接但是现实已连接
                    BaseDeviceState originState = pairs.get(focusPosition).getBaseDevice();
                    originState.setState(BaseDeviceState.STATE_DISCONNECT);
                }
            }

        }

        @Override
        public void onRadioArrived(Message msg) {
            if (linker== null)
                return;
            linker.onRadioArrived(msg);
        }

        public synchronized void onNoPairResponseArrived() {
            view.showToast("未收到子机回复,设置失败,请重试");
        }

}
