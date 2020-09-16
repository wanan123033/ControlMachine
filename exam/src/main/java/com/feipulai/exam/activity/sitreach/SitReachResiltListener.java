package com.feipulai.exam.activity.sitreach;


import android.os.Message;

import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.SitReachResult;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RoundResult;
import com.orhanobut.logger.Logger;

/**
 * Created by zzs on 2018/8/17
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitReachResiltListener implements SerialDeviceManager.RS232ResiltListener {

    private HandlerInterface handlerInterface;

    public SitReachResiltListener(HandlerInterface handlerInterface) {
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
    public void onRS232Result(Message msg) {


        switch (msg.what) {

            case SerialConfigs.SIT_AND_REACH_EMPTY_RESPONSE:
                //检测设备是否连接成功
                handlerInterface.checkDevice(1);
                break;

            case SerialConfigs.SIT_AND_REACH_START_RESPONSE:
//                ToastUtils.showShort("测试开始");
//                TtsManager.getInstance().speak("测试开始");
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
                                handlerInterface.ready(1);
                                break;
                        }
                        break;
                    case RESULT_UPDATING://成绩更新数据
                        Logger.i("状态:" + result.getState() + "\t成绩:" + result.getScore() + "\t犯规:" + result.isFoul());
                        //获取设备设置为正在使用中
                        deviceState.setDeviceId(1);
                        deviceState.setState(BaseDeviceState.STATE_ONUSE);
                        handlerInterface.getDeviceState(deviceState);
                        switch (result.getState()) {
                            //获取测量数据
                            case 3:

                                BaseStuPair stuPair = new BaseStuPair();
                                stuPair.setResult(result.getScore());
                                stuPair.setResultState(result.isFoul() == true ? RoundResult.RESULT_STATE_FOUL : RoundResult.RESULT_STATE_NORMAL);
                                stuPair.setBaseDevice(deviceState);
                                //更新成绩
                                handlerInterface.getResult(false, stuPair);
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
                            case 4:   //获取设备设置为结束
                                //设置为未测试状态，只取一次结束成绩
                                testState = TestState.UN_STARTED;

                                deviceState.setDeviceId(1);
                                deviceState.setState(BaseDeviceState.STATE_END);
                                stuPair = new BaseStuPair();
                                stuPair.setResult(result.getScore());
                                stuPair.setResultState(result.isFoul() == true ? RoundResult.RESULT_STATE_FOUL : RoundResult.RESULT_STATE_NORMAL);
                                stuPair.setBaseDevice(deviceState);

                                //   成绩数值超出范围
                                int minValue = TestConfigs.sCurrentItem.getMinValue() == 0 ? TestConfigs.itemMinScope.get(TestConfigs.sCurrentItem.getMachineCode()) : TestConfigs.sCurrentItem.getMinValue();
                                int maxValue = TestConfigs.sCurrentItem.getMaxValue() == 0 ? TestConfigs.itemMaxScope.get(TestConfigs.sCurrentItem.getMachineCode()) : TestConfigs.sCurrentItem.getMaxValue();

//                                if ((TestConfigs.sCurrentItem.getMinValue() != 0 && result.getResult() < TestConfigs.sCurrentItem.getMinValue() * 10)
//                                        || (TestConfigs.sCurrentItem.getMaxValue() != 0 && result.getResult() > TestConfigs.sCurrentItem.getMaxValue() * 10)) {
                                if (result.getScore() < minValue
                                        || result.getScore() > maxValue) {

                                    testState = TestState.WAIT_RESULT;
                                    //重测设置设备正在使用中
                                    deviceState.setState(BaseDeviceState.STATE_ONUSE);
                                    stuPair.setResult(0);
                                    //更新成绩
                                    handlerInterface.getResult(false, stuPair);
                                    //设置设备状态
                                    handlerInterface.getDeviceState(deviceState);
                                    handlerInterface.AgainTest(deviceState);
                                    break;
                                }
                                //更新成绩
                                handlerInterface.getResult(true, stuPair);
                                if (result.getScore() / 10 > -15) {
                                    //设置设备状态
                                    handlerInterface.getDeviceState(deviceState);

                                    //结束设备
                                    handlerInterface.EndDevice(result.isFoul(), result.getScore());
                                }


                                break;
                        }
                        break;
                }

                break;

            case SerialConfigs.SIT_AND_REACH_STOP_RESPONSE:
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
        void getResult(boolean isEnd, BaseStuPair stuPair);

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

        /**
         * 就绪
         */
        void ready(int deviveId);
    }
}
