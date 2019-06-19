package com.feipulai.host.activity.standjump;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.JumpScore;
import com.feipulai.device.serial.beans.JumpSelfCheckResult;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;

import static com.feipulai.device.serial.SerialConfigs.JUMP_SCORE_RESPONSE;
import static com.feipulai.device.serial.SerialConfigs.JUMP_SELF_CHECK_RESPONSE;
import static com.feipulai.device.serial.SerialConfigs.JUMP_SELF_CHECK_RESPONSE_Simple;
import static com.feipulai.device.serial.SerialConfigs.JUMP_START_RESPONSE;
import static com.feipulai.device.serial.beans.JumpSelfCheckResult.NEED_CHANGE;


/**
 * 立地跳远Handler回调
 * Created by zzs on 2018/8/10
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
@Deprecated
public class StandHandler extends Handler {
    /***测试状态  UN_STARTED 未测试，START_TEST准备状态 WAIT_RESULT开始测试  TEST_END 测试结束，成功获取成绩*/
    enum TestState {
        UN_STARTED, START_TEST, WAIT_RESULT, TEST_END
    }

    private HandlerInterface handlerInterface;

    public StandHandler(HandlerInterface handlerInterface) {
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
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case JUMP_SELF_CHECK_RESPONSE:
                Log.i("james", "JUMP_SELF_CHECK_RESPONSE received");
                JumpSelfCheckResult result = (JumpSelfCheckResult) msg.obj;
                if (result.getTerminalCondition() == NEED_CHANGE) {
                    Log.i("james", "JUMP_SELF_CHECK_RESPONSE NEED_CHANGE");
                    ToastUtils.showLong("测量垫已损坏,请更换测量垫");
                    //当设备状态为非读取成绩状态则修改为未测试
                    if (testState != TestState.WAIT_RESULT) {
                        testState = TestState.UN_STARTED;
                    }
                    //设置当前设置为不可用断开状态
                    handlerInterface.getDeviceState(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
                    //测量垫检测失败
                    handlerInterface.CheckDevice(false);
                }
                break;

            case JUMP_SELF_CHECK_RESPONSE_Simple:
                Log.i("james", "JUMP_SELF_CHECK_RESPONSE_Simple received");
                //测量垫检测通过
                handlerInterface.CheckDevice(true);
                break;

            case JUMP_START_RESPONSE:
                Log.i("james", "JUMP_START_RESPONSE");
                TtsManager.getInstance().speak("测试开始");
                //测试指令回复，可以开始测试adb connect 192.168.137.139
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
                    Log.i("james", "JUMP_SCORE_RESPONSE====>" + jumpScore.getScore());

                    BaseStuPair stuPair = new BaseStuPair();
                    //设置获取成绩机器
                    stuPair.setBaseDevice(deviceState);

                    //是否犯规
                    if (jumpScore.isFoul()) {
                        stuPair.setResultState(-1);
                        stuPair.setResult(jumpScore.getScore());
                    } else {
                        stuPair.setResultState(0);
                        stuPair.setResult(jumpScore.getScore());

                    }
                    //成绩成功获取设置设备为结束状态
                    testState = TestState.TEST_END;

                    handlerInterface.getResult(stuPair);
                    // TODO: 2018/11/9 0009 16:51
                    //结束测试 发送结束指令
                    //SerialPorter.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_END_JUMP));
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

        void CheckDevice(boolean isCheckDevice);

        /**
         * 开始测试
         */

        void StartDevice();

        /**
         * 测试结束 isFoul 是否犯规 result成绩
         */
        void EndDevice(boolean isFoul, int result);
    }

}
