package com.feipulai.exam.activity.sitreach;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseGroupTestActivity;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RoundResult;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 坐位体前屈分组模式
 * Created by zzs on 2018/11/20
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitReachGroupTestActivity extends BaseGroupTestActivity implements SitReachResiltListener.HandlerInterface {
    private final static String TAG = "SitReachGroupTest";

    private SitReachSetting reachSetting;
    //3秒内检设备是否可用
    private volatile boolean isDisconnect = true;
    private MyHandler mHandler;
    private static final int MSG_DISCONNECT = 0X101;
    private static final int UPDATE_DEVICE = 0X102;
    private static final int UPDATE_RESULT = 0X103;
    private GetResultRunnable resultRunnable;
    private CheckDeviceRunnable statesRunnable;
    private ExecutorService mExecutorService;
    private SitReachResiltListener sitReachResiltListener;
    //保存当前测试考生
    private BaseStuPair baseStuPair;
    private long disconnectTime;

    @Override
    public void initData() {
        reachSetting = SharedPrefsUtil.loadFormSource(this, SitReachSetting.class);
        if (reachSetting == null) {
            reachSetting = new SitReachSetting();
        }
        Logger.i(TAG + ":reachSetting ->" + reachSetting.toString());
//        if (SerialDeviceManager.getInstance() != null) {
//            SerialDeviceManager.getInstance().setRS232ResiltListener(sitReachResiltListener);
//        } else {
//            updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR));
//        }
//        mExecutorService = Executors.newFixedThreadPool(2);
//        mExecutorService.submit(resultRunnable);
//        mExecutorService.submit(statesRunnable);
//        sitReachResiltListener.setTestState(SitReachResiltListener.TestState.UN_STARTED);
//        resultRunnable.setTestState(sitReachResiltListener.getTestState());
//        statesRunnable.setTestState(sitReachResiltListener.getTestState());
//        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.life("SitReachGroupTestActivity onResume");
        mHandler = new MyHandler(this);
        if (sitReachResiltListener == null) {
            sitReachResiltListener = new SitReachResiltListener(this);
            sitReachResiltListener.setTestState(SitReachResiltListener.TestState.UN_STARTED);
        }
        SerialDeviceManager.getInstance().setRS232ResiltListener(sitReachResiltListener);
        mExecutorService = Executors.newFixedThreadPool(2);
        resultRunnable = new GetResultRunnable();
        statesRunnable = new CheckDeviceRunnable();
        mExecutorService.submit(resultRunnable);
        mExecutorService.submit(statesRunnable);
        resultRunnable.setTestState(sitReachResiltListener.getTestState());
        statesRunnable.setTestState(sitReachResiltListener.getTestState());
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
        if (SerialDeviceManager.getInstance() != null && sitReachResiltListener.getTestState() != SitReachResiltListener.TestState.UN_STARTED) {
            //开始测试
            LogUtils.normal(SerialConfigs.CMD_SIT_REACH_START.length + "---" + StringUtility.bytesToHexString(SerialConfigs.CMD_SIT_REACH_START) + "---坐位体前屈开始测试指令");
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_START));
            //获取数据
            LogUtils.normal(SerialConfigs.CMD_SIT_REACH_GET_SCORE.length + "---" + StringUtility.bytesToHexString(SerialConfigs.CMD_SIT_REACH_GET_SCORE) + "---坐位体前屈获取数据指令");
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_GET_SCORE));
        }
    }

    @Override
    public int setTestCount() {
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            return TestConfigs.sCurrentItem.getTestNum();
        } else {
            return reachSetting.getTestCount();
        }
    }

    @Override
    public void gotoItemSetting() {
        LogUtils.operation("坐位体前屈跳转至SitReachSettingActivity");
        startActivity(new Intent(this, SitReachSettingActivity.class));
        finish();
    }

    @Override
    public void startTest(BaseStuPair stuPair) {
        baseStuPair = stuPair;
        baseStuPair.setTestTime(System.currentTimeMillis() + "");
        LogUtils.operation("坐位体前屈开始测试:" + stuPair.toString());
        sitReachResiltListener.setTestState(SitReachResiltListener.TestState.WAIT_RESULT);
        resultRunnable.setTestState(sitReachResiltListener.getTestState());
        statesRunnable.setTestState(sitReachResiltListener.getTestState());
        if (SerialDeviceManager.getInstance() != null) {
            //开始测试
            LogUtils.normal(SerialConfigs.CMD_SIT_REACH_START.length + "---" + StringUtility.bytesToHexString(SerialConfigs.CMD_SIT_REACH_START) + "---坐位体前屈开始测试指令");
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_START));
            //获取数据
            LogUtils.normal(SerialConfigs.CMD_SIT_REACH_GET_SCORE.length + "---" + StringUtility.bytesToHexString(SerialConfigs.CMD_SIT_REACH_GET_SCORE) + "---坐位体前屈获取数据指令");
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_GET_SCORE));
        }
    }

    @Override
    public int setTestPattern() {
        return reachSetting.getTestPattern();
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (SerialDeviceManager.getInstance() != null) {
//            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_END));
//        }
//        SerialDeviceManager.getInstance().close();
//    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.life("SitReachGroupTestActivity onPause");
        if (SerialDeviceManager.getInstance() != null) {
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_END));
        }
        SerialDeviceManager.getInstance().close();
        resultRunnable.setTestState(SitReachResiltListener.TestState.UN_STARTED);
        statesRunnable.setTestState(sitReachResiltListener.getTestState());
        statesRunnable.setFinish(true);
        resultRunnable.setFinish(true);
        statesRunnable = null;
        resultRunnable = null;
        mHandler = null;
        mExecutorService.shutdown();
    }


//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        resultRunnable.setTestState(SitReachResiltListener.TestState.UN_STARTED);
//        statesRunnable.setTestState(sitReachResiltListener.getTestState());
//        statesRunnable.setFinish(true);
//        resultRunnable.setFinish(true);
//        statesRunnable = null;
//        resultRunnable = null;
//        mExecutorService.shutdown();
//
//    }

    @Override
    public void checkDevice(int deviceId) {
        isDisconnect = false;
        Message msg = mHandler.obtainMessage();
        msg.obj = new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1);
        msg.what = UPDATE_DEVICE;
        mHandler.sendMessage(msg);
    }

    @Override
    public void getDeviceState(BaseDeviceState deviceState) {
        BaseDeviceState state = new BaseDeviceState();
        state.setState(deviceState.getState());
        Message msg = mHandler.obtainMessage();
        msg.obj = state;
        msg.what = UPDATE_DEVICE;
        mHandler.sendMessage(msg);

    }

    @Override
    public void getResult(boolean isEnd, BaseStuPair stuPair) {
        if (isEnd) {
            if (stuPair.getResult()/10 <= -15) {
                confirmResult(stuPair);
                return;
            }
        }
        if (reachSetting.isFullReturn()) {
            if (baseStuPair.getStudent().getSex() == 0) {//男子
                stuPair.setFullMark(stuPair.getResult() >= reachSetting.getManFull() * 10);
            } else {
                stuPair.setFullMark(stuPair.getResult() >= reachSetting.getWomenFull() * 10);
            }
        }
        Logger.i(TAG + ":getResult--->" + stuPair.toString());
        Message msg = mHandler.obtainMessage();
        msg.obj = stuPair;
        msg.what = UPDATE_RESULT;
        mHandler.sendMessage(msg);
    }

    @Override
    public void EndDevice(boolean isFoul, int result) {
        resultRunnable.setTestState(sitReachResiltListener.getTestState());
        statesRunnable.setTestState(sitReachResiltListener.getTestState());
    }

    @Override
    public void AgainTest(BaseDeviceState deviceState) {
        resultRunnable.setTestState(sitReachResiltListener.getTestState());
        statesRunnable.setTestState(sitReachResiltListener.getTestState());
        toastSpeak("错误重测");
    }

    @Override
    public void stopResponse(int deviveId) {

    }

    @Override
    public void ready(int deviveId) {
        toastSpeak("开始测试");
    }

    boolean clicked;

    private void confirmResult(final BaseStuPair stuPair) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                clicked = false;
                SweetAlertDialog alertDialog = new SweetAlertDialog(SitReachGroupTestActivity.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
                alertDialog.setTitleText(getString(R.string.confirm_result));
                alertDialog.setContentText("当前成绩是否为最终成绩");
                alertDialog.setCancelable(false);
                alertDialog.setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();

                        if (!clicked) {
                            if (reachSetting.isFullReturn()) {
                                if (baseStuPair.getStudent().getSex() == 0) {//男子
                                    stuPair.setFullMark(stuPair.getResult() >= reachSetting.getManFull() * 10);
                                } else {
                                    stuPair.setFullMark(stuPair.getResult() >= reachSetting.getWomenFull() * 10);
                                }
                            }
                            Logger.i(TAG + ":getResult--->" + stuPair.toString());
                            Message msg = mHandler.obtainMessage();
                            msg.obj = stuPair;
                            msg.what = UPDATE_RESULT;
                            mHandler.sendMessage(msg);
                            clicked = true;

                            //设置设备状态
                            BaseDeviceState deviceState = stuPair.getBaseDevice();
                            deviceState.setState(BaseDeviceState.STATE_END);
                            getDeviceState(deviceState);
                            //结束设备
                            EndDevice(stuPair.getResultState() == RoundResult.RESULT_STATE_FOUL, stuPair.getResult());
                        }

                    }
                }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        if (!clicked) {
                            sitReachResiltListener.setTestState(SitReachResiltListener.TestState.WAIT_RESULT);
                            //重测设置设备正在使用中
                            stuPair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
                            stuPair.setResult(0);
                            //更新成绩
                            getResult(false, stuPair);
                            //设置设备状态
                            getDeviceState(stuPair.getBaseDevice());
                            AgainTest(stuPair.getBaseDevice());
                            clicked = true;
                        }

                    }
                }).show();
            }
        });

    }

    private static class MyHandler extends Handler {

        private WeakReference<SitReachGroupTestActivity> mActivityWeakReference;

        public MyHandler(SitReachGroupTestActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SitReachGroupTestActivity activity = mActivityWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case UPDATE_DEVICE:
                        LogUtils.operation("坐位体前屈更新状态:" + msg.obj.toString());
                        activity.updateDevice((BaseDeviceState) msg.obj);
                        break;
                    case UPDATE_RESULT:
                        LogUtils.operation("坐位体前屈更新成绩:" + msg.obj.toString());
                        activity.updateTestResult((BaseStuPair) msg.obj);
                        break;
                    case MSG_DISCONNECT:
                        LogUtils.operation("坐位体前屈设备未连接...");
                        if (activity.isDisconnect) {
                            // 判断2次提示时间
                            if (!activity.isDestroyed() && (System.currentTimeMillis() - activity.disconnectTime) > 30000) {
                                activity.toastSpeak("设备未连接");
                                activity.disconnectTime = System.currentTimeMillis();
                            }

                            //设置当前设置为不可用断开状态
                            activity.updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));

                            activity.sitReachResiltListener.setTestState(SitReachResiltListener.TestState.UN_STARTED);
                            if (activity.resultRunnable != null) {
                                activity.resultRunnable.setTestState(SitReachResiltListener.TestState.UN_STARTED);
                            }

                        }
                        break;
                }
            }

        }
    }

    /**
     * 获取成绩
     */
    private class GetResultRunnable implements Runnable {
        SitReachResiltListener.TestState testState = SitReachResiltListener.TestState.UN_STARTED;

        public void setTestState(SitReachResiltListener.TestState testState) {
            this.testState = testState;
        }

        private boolean isFinish = false;

        public void setFinish(boolean finish) {
            isFinish = finish;
        }

        @Override
        public void run() {
            while (!isFinish) {
                if (testState != SitReachResiltListener.TestState.UN_STARTED) {
                    Log.i("zzs", "===>" + "sendCommand");
                    if (SerialDeviceManager.getInstance() != null) {
                        LogUtils.normal(SerialConfigs.CMD_SIT_REACH_GET_SCORE.length + "---" + StringUtility.bytesToHexString(SerialConfigs.CMD_SIT_REACH_GET_SCORE) + "---坐位体前屈空闲指令");
                        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_GET_SCORE));
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取设置状态
     */
    private class CheckDeviceRunnable implements Runnable {
        private boolean isFinish = false;

        public void setFinish(boolean finish) {
            isFinish = finish;
        }

        SitReachResiltListener.TestState testState = SitReachResiltListener.TestState.UN_STARTED;

        public void setTestState(SitReachResiltListener.TestState testState) {
            this.testState = testState;
        }

        @Override
        public void run() {
            while (!isFinish) {
                //开始测试不发送自检指令，会出现4秒内未收到返回数据 ，因为在这个时间段还在接收获取成绩的数据
                if (testState == SitReachResiltListener.TestState.UN_STARTED) {
                    Log.i("zzs", "===>" + "sendCheckCommand");
                    if (SerialDeviceManager.getInstance() != null) {

                        try {
                            //设备自检,校验连接是否正常
                            isDisconnect = true;
                            if (SerialDeviceManager.getInstance() != null) {
                                //设备自检,校验连接是否正常
                                LogUtils.normal(SerialConfigs.CMD_SIT_REACH_EMPTY.length + "---" + StringUtility.bytesToHexString(SerialConfigs.CMD_SIT_REACH_EMPTY) + "---坐位体前屈空闲指令");
                                SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_SIT_REACH_EMPTY));
                                mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 3000);
                            }
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
