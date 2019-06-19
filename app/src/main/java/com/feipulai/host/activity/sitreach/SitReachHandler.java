package com.feipulai.host.activity.sitreach;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.SitReachResult;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.config.TestConfigs;

/**
 * Created by zzs on 2018/8/17
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
@Deprecated
public class SitReachHandler extends Handler {

    private HandlerInterface handlerInterface;

    public SitReachHandler(HandlerInterface handlerInterface) {
        this.handlerInterface = handlerInterface;
    }

    //测试状态
    enum TestState {
        UN_STARTED, WAIT_RESULT, RESULT_UPDATING
    }

    private TestState testState = TestState.UN_STARTED;

    public TestState getTestState() {
        return testState;
    }

    public void setTestState(TestState testState) {
        this.testState = testState;
    }

    BaseDeviceState deviceState = new BaseDeviceState();

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        switch (msg.what) {

            case SerialConfigs.SIT_AND_REACH_EMPTY_RESPONSE:
                //检测设备是否连接成功
                handlerInterface.checkDevice(1);
                Log.i("james", "空命令回复:");
                break;

            case SerialConfigs.SIT_AND_REACH_START_RESPONSE:
                Log.i("james", "开始命令回复:");
                TtsManager.getInstance().speak("测试开始");
                break;

            case SerialConfigs.SIT_AND_REACH_RESULT_RESPONSE://获取设备返回成绩信息
                SitReachResult result = (SitReachResult) msg.obj;
                //获取设备状态

                //根据测试状态获取成绩
                switch (testState) {
                    case UN_STARTED://未开始
                        deviceState.setDeviceId(1);
                        deviceState.setState(BaseDeviceState.STATE_NOT_BEGAIN);
                        handlerInterface.getDeviceState(deviceState);
                        break;
                    case WAIT_RESULT://开始读成绩

                        //获取设备设置为空闲
                        deviceState.setDeviceId(1);
                        deviceState.setState(BaseDeviceState.STATE_FREE);
                        handlerInterface.getDeviceState(deviceState);
                        switch (result.getState()) {
                            case 1://就绪
                                testState = TestState.RESULT_UPDATING;
                                break;
                        }
                        break;
                    case RESULT_UPDATING://成绩更新数据
                        Log.i("james", "状态:" + result.getState() + "\t成绩:" + result.getScore() + "\t犯规:" + result.isFoul());
                        //获取设备设置为正在使用中
                        deviceState.setDeviceId(1);
                        deviceState.setState(BaseDeviceState.STATE_ONUSE);
                        handlerInterface.getDeviceState(deviceState);
                        switch (result.getState()) {
                            //获取测量数据
                            case 3:

                                BaseStuPair stuPair = new BaseStuPair();
                                stuPair.setResult(result.getScore());
                                stuPair.setResultState(result.isFoul() == true ? -1 : 0);
                                stuPair.setBaseDevice(deviceState);
                                //更新成绩
                                handlerInterface.getResult(stuPair);
                                //犯规则结束测试
                                if (result.isFoul()) {
                                    deviceState.setDeviceId(1);
                                    deviceState.setState(BaseDeviceState.STATE_END);
                                    //设置设备状态
                                    handlerInterface.getDeviceState(deviceState);
                                    //结束设备
                                    handlerInterface.EndDevice(result.isFoul(), result.getScore());
                                    testState = TestState.UN_STARTED;
                                }

                                break;
                            //获取测量结束数据
                            case 4:
                                //获取设备设置为结束
                                deviceState.setDeviceId(1);
                                deviceState.setState(BaseDeviceState.STATE_END);
                                stuPair = new BaseStuPair();
                                stuPair.setResult(result.getScore());
                                stuPair.setResultState(result.isFoul() == true ? -1 : 0);
                                stuPair.setBaseDevice(deviceState);

                                //   成绩数值超出范围
                                if ((TestConfigs.sCurrentItem.getMinValue() != 0 && result.getScore() < TestConfigs.sCurrentItem.getMinValue())
                                        || (TestConfigs.sCurrentItem.getMaxValue() != 0 && result.getScore() > TestConfigs.sCurrentItem.getMaxValue())) {

                                    TtsManager.getInstance().speak(deviceState.getDeviceId() + "号设备错误重测");
                                    testState = TestState.WAIT_RESULT;
                                    //重测设置设备正在使用中
                                    deviceState.setState(BaseDeviceState.STATE_ONUSE);
                                    stuPair.setResult(0);
                                    //更新成绩
                                    handlerInterface.getResult(stuPair);

                                    //设置设备状态
                                    handlerInterface.getDeviceState(deviceState);
                                    handlerInterface.AgainTest(deviceState);
                                    break;
                                }
                                //更新成绩
                                handlerInterface.getResult(stuPair);
                                //设置设备状态
                                handlerInterface.getDeviceState(deviceState);
                                //设置为未测试状态，只取一次结束成绩
                                testState = TestState.UN_STARTED;
                                //结束设备
                                handlerInterface.EndDevice(result.isFoul(), result.getScore());

                                break;
                        }
                        break;
                }

                break;

            case SerialConfigs.SIT_AND_REACH_STOP_RESPONSE:
                Log.i("james", "结束命令回复:");
                handlerInterface.stopResponse(1);
                break;

//            case SerialConfigs.SIT_AND_REACH_GET_VERSION_RESPONSE:
//                SitReachVersionResult versionResult = (SitReachVersionResult) msg.obj;
//                Log.i("james", "版本:" + versionResult.getVersion());
//                break;


        }

    }

    public interface HandlerInterface {
        /**
         * 检测到设备
         *
         * @param deviceId 设备id
         */
        void checkDevice(int deviceId);

        /**
         * 获取设备状态
         *
         * @param deviceState 设备信息实体类
         */
        void getDeviceState(BaseDeviceState deviceState);

        /**
         * 获取成绩
         *
         * @param stuPair
         */
        void getResult(BaseStuPair stuPair);

        /**
         * 测试结束
         *
         * @param isFoul 是否犯规
         * @param result 成绩
         */
        void EndDevice(boolean isFoul, int result);

        /**
         * 重测
         *
         * @param deviceState 重测设备信息实体类
         */
        void AgainTest(BaseDeviceState deviceState);

        /**
         * 发送结束指令回调
         *
         * @param deviveId
         */
        void stopResponse(int deviveId);
    }
}
