package com.feipulai.exam.activity.standjump;

import android.os.Message;
import android.util.Log;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.JumpScore;
import com.feipulai.device.serial.beans.JumpSelfCheckResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RoundResult;
import com.orhanobut.logger.Logger;

import static com.feipulai.device.serial.SerialConfigs.JUMP_SCORE_RESPONSE;
import static com.feipulai.device.serial.SerialConfigs.JUMP_SELF_CHECK_RESPONSE;
import static com.feipulai.device.serial.SerialConfigs.JUMP_SELF_CHECK_RESPONSE_Simple;
import static com.feipulai.device.serial.SerialConfigs.JUMP_START_RESPONSE;
import static com.feipulai.device.serial.beans.JumpSelfCheckResult.HAS_BROKEN_POINTS;
import static com.feipulai.device.serial.beans.JumpSelfCheckResult.NEED_CHANGE;

/**
 * 立定跳远回调
 * Created by zzs on 2018/8/10
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandResiltListener implements SerialDeviceManager.RS232ResiltListener {
    /***测试状态  UN_STARTED 未测试，START_TEST准备状态 WAIT_RESULT开始测试  TEST_END 测试结束，成功获取成绩*/
    enum TestState {
        UN_STARTED, START_TEST, WAIT_RESULT, TEST_END
    }

    private HandlerInterface handlerInterface;

    public StandResiltListener(HandlerInterface handlerInterface) {
        this.handlerInterface = handlerInterface;
    }

    //测试状态
    private TestState testState = TestState.UN_STARTED;

    public void setTestState(TestState testState) {
        this.testState = testState;
    }

    public TestState getTestState() {
        return testState;
    }

    @Override
    public void onRS232Result(Message msg) {

        switch (msg.what) {

            case JUMP_SELF_CHECK_RESPONSE:
                Log.i("james", "JUMP_SELF_CHECK_RESPONSE received");
                JumpSelfCheckResult result = (JumpSelfCheckResult) msg.obj;
                Logger.i("JUMP_SELF_CHECK_RESPONSE:" + result.toString());
                if (result.getTerminalCondition() == NEED_CHANGE) {
                    Log.i("james", "JUMP_SELF_CHECK_RESPONSE NEED_CHANGE");

                    //当设备状态为非读取成绩状态则修改为未测试
                    if (testState != TestState.WAIT_RESULT) {
                        testState = TestState.UN_STARTED;
                    }
//                    //设置当前设置为不可用断开状态
//                    handlerInterface.getDeviceState(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
//                    //测量垫检测失败
//                    handlerInterface.CheckDevice(false, result.getBrokenLEDs());

                    //设置当前设置
                    handlerInterface.getDeviceState(new BaseDeviceState(BaseDeviceState.STATE_FREE, 1));
                    handlerInterface.CheckDevice(true, result.getBrokenLEDs());
                } else if (result.getTerminalCondition() == HAS_BROKEN_POINTS) {
                    //设置当前设置
                    handlerInterface.getDeviceState(new BaseDeviceState(BaseDeviceState.STATE_FREE, 1));
                    handlerInterface.CheckDevice(true, result.getBrokenLEDs());
                }else{
                    //测量垫检测通过
                    handlerInterface.CheckDevice(true, null);
                    handlerInterface.getDeviceState(new BaseDeviceState(BaseDeviceState.STATE_FREE, 1));
                }
                break;

            case JUMP_SELF_CHECK_RESPONSE_Simple:
                Log.i("james", "JUMP_SELF_CHECK_RESPONSE_Simple received");
                //测量垫检测通过
                handlerInterface.CheckDevice(true, null);
                //设置当前设置
                handlerInterface.getDeviceState(new BaseDeviceState(BaseDeviceState.STATE_FREE, 1));
                break;

            case JUMP_START_RESPONSE:
                Log.i("james", "JUMP_START_RESPONSE");
                //测试指令回复，可以开始测试
                handlerInterface.StartDevice();
                testState = TestState.WAIT_RESULT;
                //设置当前设置为空闲状态
                handlerInterface.getDeviceState(new BaseDeviceState(BaseDeviceState.STATE_FREE, 1));
                break;

            case JUMP_SCORE_RESPONSE:
                //成绩数据回复

                //设置当前设备为正在使用状态
                BaseDeviceState deviceState = new BaseDeviceState(BaseDeviceState.STATE_ONUSE, 1);
                handlerInterface.getDeviceState(deviceState);
                // 立定跳远机器成绩获取
                if (testState == TestState.WAIT_RESULT) {
                    JumpScore jumpScore = (JumpScore) msg.obj;
                    Logger.i("TestState" + testState + "=>JUMP_SCORE_RESPONSE====>" + jumpScore.getScore());

                    BaseStuPair stuPair = new BaseStuPair();
                    //设置获取成绩机器
                    stuPair.setBaseDevice(deviceState);

                    //是否犯规
                    if (jumpScore.isFoul()) {
                        stuPair.setResultState(RoundResult.RESULT_STATE_FOUL);
                        //成绩返回厘米 数据库保存为毫米
                        stuPair.setResult(jumpScore.getScore() * 10);
                    } else {
                        stuPair.setResultState(RoundResult.RESULT_STATE_NORMAL);
                        stuPair.setResult(jumpScore.getScore() * 10);
                        //   成绩数值超出范围
                        int minValue = TestConfigs.sCurrentItem.getMinValue() == 0 ? TestConfigs.itemMinScope.get(TestConfigs.sCurrentItem.getMachineCode()) : TestConfigs.sCurrentItem.getMinValue();
                        int maxValue = TestConfigs.sCurrentItem.getMaxValue() == 0 ? TestConfigs.itemMaxScope.get(TestConfigs.sCurrentItem.getMachineCode()) : TestConfigs.sCurrentItem.getMaxValue();

//                        if ((TestConfigs.sCurrentItem.getMinValue() != 0 && (jumpScore.getResult() * 10) < TestConfigs.sCurrentItem.getMinValue())
//                                || (TestConfigs.sCurrentItem.getMaxValue() != 0 && (jumpScore.getResult() * 10) > TestConfigs.sCurrentItem.getMaxValue())) {
//                            Logger.i("成绩数值超出范围:最小值->" + TestConfigs.sCurrentItem.getMinValue() + ",最大值->" + TestConfigs.sCurrentItem.getMaxValue() + ",获取的成绩->" + jumpScore.getResult());
                        if (jumpScore.getScore() * 10 < minValue
                                || jumpScore.getScore() * 10 > maxValue) {
                            Logger.i("成绩数值超出范围:最小值->" + minValue + ",最大值->" + maxValue + ",获取的成绩->" + jumpScore.getScore());

                            TtsManager.getInstance().speak("设备错误，考生请重测");
                            testState = TestState.WAIT_RESULT;
                            //重测设置设备正在使用中
                            deviceState.setState(BaseDeviceState.STATE_ONUSE);
                            stuPair.setResult(0);
                            //设置设备状态
                            handlerInterface.getDeviceState(deviceState);
                            //更新成绩
                            handlerInterface.getResult(stuPair);
                            handlerInterface.AgainTest(deviceState);
                            break;
                        }
                    }
                    //成绩成功获取设置设备为结束状态
                    testState = TestState.TEST_END;

                    handlerInterface.getResult(stuPair);
                    //结束测试 发送结束指令
                    SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_END_JUMP));
                    handlerInterface.EndDevice(jumpScore.isFoul(), jumpScore.getScore());
                    //更新设置状态为已结束
                    deviceState.setState(BaseDeviceState.STATE_END);
                    handlerInterface.getDeviceState(deviceState);
                }
                break;
            case SerialConfigs.JUMP_END_RESPONSE:
                //结束指令回复
                break;

        }
    }

    /**
     * 回调接口
     */
    public interface HandlerInterface {
        /**
         * 获取设备状态
         */

        void getDeviceState(BaseDeviceState deviceState);

        /**
         * 获取成绩
         */
        void getResult(BaseStuPair deviceState);

        /**
         * 检测设备 true 正常 false 故障
         */

        void CheckDevice(boolean isCheckDevice, int[] brokenLEDs);

        /**
         * 开始测试
         */

        void StartDevice();

        /**
         * 重测
         *
         * @param deviceState 重测设备信息实体类
         */
        void AgainTest(BaseDeviceState deviceState);

        /**
         * 测试结束 isFoul 是否犯规 result成绩
         */
        void EndDevice(boolean isFoul, int result);
    }

}
