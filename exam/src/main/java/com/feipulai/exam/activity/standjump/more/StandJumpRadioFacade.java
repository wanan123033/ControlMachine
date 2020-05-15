package com.feipulai.exam.activity.standjump.more;

import android.os.Message;
import android.util.Log;

import com.feipulai.common.jump_rope.task.GetDeviceStatesTask;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.device.manager.StandJumpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.StandJumpResult;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.standjump.StandJumpSetting;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RoundResult;
import com.orhanobut.logger.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zzs on  2019/11/12
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandJumpRadioFacade implements RadioManager.OnRadioArrivedListener {

    private ExecutorService mExecutor;
    private GetDeviceStatesTask mGetDeviceStatesTask;
    private int[] mCurrentConnect;
    private List<DeviceDetail> deviceList;
    private StandJumpResponseListener listener;
    private StandJumpSetting standJumpSetting;

    public StandJumpRadioFacade(List<DeviceDetail> deviceDetails, StandJumpSetting setting, StandJumpResponseListener responseListener) {
        this.deviceList = deviceDetails;
        this.listener = responseListener;
        this.standJumpSetting = setting;
        mExecutor = Executors.newFixedThreadPool(2);
        mCurrentConnect = new int[deviceList.size()];
        //运行两个线程,分别处理获取设备状态和LED检录显示
        mGetDeviceStatesTask = new GetDeviceStatesTask(new GetDeviceStatesTask.OnGettingDeviceStatesListener() {
            @Override
            public void onGettingState(int position) {
                StandJumpManager.getState(SettingHelper.getSystemSetting().getHostId(), position + 1, standJumpSetting.getPointsScopeArray()[position] - 42);

                try {
                    if (deviceList.size() == 2) {
                        Thread.sleep(200);
                    } else if (deviceList.size() == 1) {
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onStateRefreshed() {
                int oldState;
                for (int i = 0; i < deviceList.size(); i++) {
                    BaseDeviceState deviceState = deviceList.get(i).getStuDevicePair().getBaseDevice();
                    oldState = deviceState.getState();
                    if (mCurrentConnect[deviceState.getDeviceId() - 1] == 0
                            && oldState != BaseDeviceState.STATE_ERROR
                            && oldState != BaseDeviceState.STATE_CONFLICT) {
//                        Logger.i("zzs----->onStateRefreshed==========>STATE_ERROR" + mCurrentConnect[deviceState.getDeviceId() - 1]);
                        deviceState.setState(BaseDeviceState.STATE_ERROR);
                        listener.refreshDeviceState(i);
                    }
                }
                mCurrentConnect = new int[deviceList.size()];
            }

            @Override
            public int getDeviceCount() {
                return deviceList.size();
            }
        });


        // 开始之前先全部不动,等待开始
        pause();
        mExecutor.execute(mGetDeviceStatesTask);
    }

    public void setDeviceList(List<DeviceDetail> deviceList) {
        this.deviceList = deviceList;
        mCurrentConnect = new int[deviceList.size()];
    }

    public void setStandJumpSetting(StandJumpSetting standJumpSetting) {
        this.standJumpSetting = standJumpSetting;
    }

    @Override
    public void onRadioArrived(Message msg) {
        StandJumpResult result;
        try {
            result = (StandJumpResult) msg.obj;
        } catch (Exception e) {
            return;
        }

        if (msg.what == SerialConfigs.STAND_JUMP_START) {
            mCurrentConnect[result.getDeviceId() - 1] = BaseDeviceState.STATE_FREE;
            deviceList.get(result.getDeviceId() - 1).getStuDevicePair().getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
//            listener.refreshDeviceState(result.getDeviceId() - 1);
            listener.StartDevice(result.getDeviceId());
        } else if (msg.what == SerialConfigs.STAND_JUMP_GET_STATE) {
            if (result.getDeviceId() > deviceList.size()) {
                return;
            }
            BaseDeviceState deviceState = deviceList.get(result.getDeviceId() - 1).getStuDevicePair().getBaseDevice();
//            Logger.i("JUMP_SCORE_RESPONSE====>" + result.toString());
            //状态处理
            if (mCurrentConnect[result.getDeviceId() - 1] == BaseDeviceState.STATE_CONFLICT) {
                return;
            }
            mCurrentConnect[result.getDeviceId() - 1] = BaseDeviceState.STATE_FREE;
            if (deviceState.getState() == BaseDeviceState.STATE_ERROR) {
                deviceState.setState(BaseDeviceState.STATE_FREE);
//                Logger.i("zzs----->onRadioArrived==========>STATE_FREE");
                listener.refreshDeviceState(result.getDeviceId() - 1);
            }
            //成绩处理

            if (result.getState() == StandJumpResult.STATE_END) {
                Log.i("pair_state",deviceList.get(result.getDeviceId() - 1).getStuDevicePair().getBaseDevice().getState()+"");
                //未开始 结束不接收成绩
                if (deviceList.get(result.getDeviceId() - 1).getStuDevicePair().getBaseDevice().getState() == BaseDeviceState.STATE_END
                        || deviceList.get(result.getDeviceId() - 1).getStuDevicePair().getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN) {
                    return;
                }
                BaseStuPair stuPair = deviceList.get(result.getDeviceId() - 1).getStuDevicePair();
                //是否犯规
                if (result.isFoul()) {
                    stuPair.setResultState(RoundResult.RESULT_STATE_FOUL);
                    //成绩返回厘米 数据库保存为毫米
                    stuPair.setResult(result.getScore() * 10);
                    if (stuPair.getStudent() != null)
                        listener.getResult(stuPair);
                    StandJumpManager.setLeisure(SettingHelper.getSystemSetting().getHostId(), result.getDeviceId());
                    //更新设置状态为已结束
                    stuPair.getBaseDevice().setState(BaseDeviceState.STATE_END);
                    //设置设备状态
                    listener.endDevice(stuPair.getBaseDevice());
                } else {
                    stuPair.setResultState(RoundResult.RESULT_STATE_NORMAL);
                    stuPair.setResult(result.getScore() * 10);
                    //   成绩数值超出范围
                    int minValue = TestConfigs.sCurrentItem.getMinValue() == 0 ? TestConfigs.itemMinScope.get(TestConfigs.sCurrentItem.getMachineCode()) : TestConfigs.sCurrentItem.getMinValue();
                    int maxValue = TestConfigs.sCurrentItem.getMaxValue() == 0 ? TestConfigs.itemMaxScope.get(TestConfigs.sCurrentItem.getMachineCode()) : TestConfigs.sCurrentItem.getMaxValue();
                    if (result.getScore() * 10 < minValue
                            || result.getScore() * 10 > maxValue) {
                        Logger.i("成绩数值超出范围:最小值->" + minValue + ",最大值->" + maxValue + ",获取的成绩->" + result.getScore());

                        TtsManager.getInstance().speak("成绩异常，考生请重测");

                        //重测设置设备正在使用中
                        stuPair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
//                        stuPair.setResult(0);
                        //设置设备状态
                        listener.refreshDeviceState(result.getDeviceId() - 1);
//                        //更新成绩
//                        if (stuPair.getStudent() != null)
//                            listener.getResult(stuPair);
//                        listener.AgainTest(result.getDeviceId());
                        StandJumpManager.setLeisure(SettingHelper.getSystemSetting().getHostId(), result.getDeviceId());
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        StandJumpManager.startTest(SettingHelper.getSystemSetting().getHostId(), result.getDeviceId());
                    } else {
                        if (stuPair.getStudent() != null)
                            listener.getResult(stuPair);
                        //结束测试 发送结束指令
//                    StandJumpManager.endTest(SettingHelper.getSystemSetting().getHostId(), result.getDeviceId());
                        StandJumpManager.setLeisure(SettingHelper.getSystemSetting().getHostId(), result.getDeviceId());
                        //更新设置状态为已结束
                        stuPair.getBaseDevice().setState(BaseDeviceState.STATE_END);
                        //设置设备状态
                        listener.endDevice(stuPair.getBaseDevice());
                    }
                }


            }

        }

    }

    public void pause() {
        mGetDeviceStatesTask.pause();
    }

    public void resume() {
        mGetDeviceStatesTask.resume();
    }


    public void finish() {

        mGetDeviceStatesTask.finish();
        mExecutor.shutdownNow();
    }


    public interface StandJumpResponseListener {
        void refreshDeviceState(int deviceId);

        /**
         * 获取成绩
         */
        void getResult(BaseStuPair stuPair);

        /**
         * 开始测试
         */

        void StartDevice(int deviceId);


        /**
         * 结束测试
         */

        void endDevice(BaseDeviceState deviceState);
//
//        /**
//         * 重测
//         */
//        void AgainTest(int deviceId);
    }
}
