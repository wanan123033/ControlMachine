package com.feipulai.exam.activity.RadioTimer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.RunTimerManager;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.RunTimerConnectState;
import com.feipulai.device.serial.beans.RunTimerResult;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.feipulai.exam.activity.RadioTimer.RunTimerConstant.CONNECT_STATE;
import static com.feipulai.exam.activity.RadioTimer.RunTimerConstant.ILLEGAL_BACK;
import static com.feipulai.exam.activity.RadioTimer.RunTimerConstant.TIME_RESPONSE;
import static com.feipulai.exam.activity.RadioTimer.RunTimerConstant.TIME_UPDATE;

/**
 * Created by pengjf on 2018/12/18.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public abstract class BaseRunTimerActivity extends BaseCheckActivity {
    private static final String TAG = "BaseRunTimerActivity";

    public long baseTimer;
    public RunTimerSetting runTimerSetting;
    private SerialDeviceManager deviceManager;
    /**
     * 测试状态
     */
    public int testState = 0;
    private boolean isForce;//强制开始
//    private boolean isAuto;
    /**
     * 跑到数量
     */
    public int runNum;
    public int interceptPoint; //1起点  2终点
    private HashMap<String, Integer> promoteTimes = new HashMap<>();
    /**
     * 最大测试次数
     */
    public int maxTestTimes;
    public boolean isOverTimes;
    public RunTimerDisposeManager disposeManager;
    private int interceptWay;
    private int settingSensor;
    public boolean reLoad;
    private boolean isBaseTime ;//是否已经计算误差时间
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposeManager = new RunTimerDisposeManager(this);
        init();
    }

    private void init() {
        deviceManager = SerialDeviceManager.getInstance();
        deviceManager.setRS232ResiltListener(runTimerListener);
        getSetting();
    }


    @Override
    protected void onResume() {
        super.onResume();
        reLoad = false;
        runTimerSetting = SharedPrefsUtil.loadFormSource(this, RunTimerSetting.class);
        if (null == runTimerSetting) {
            runTimerSetting = new RunTimerSetting();
        }
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            maxTestTimes = TestConfigs.sCurrentItem.getTestNum();
        } else {
            maxTestTimes = runTimerSetting.getTestTimes();
        }
        if (runNum != Integer.parseInt(runTimerSetting.getRunNum()) || interceptPoint != runTimerSetting.getInterceptPoint()
                || interceptWay != runTimerSetting.getInterceptWay() || settingSensor != runTimerSetting.getSensor()) {
            getSetting();
            reLoad = true;
        }
    }

    /**
     * 设置
     */
    private void getSetting() {
        runTimerSetting = SharedPrefsUtil.loadFormSource(this, RunTimerSetting.class);
        if (null == runTimerSetting) {
            runTimerSetting = new RunTimerSetting();
        }
        Logger.i("runTimerSetting:" + runTimerSetting.toString());

        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            maxTestTimes = TestConfigs.sCurrentItem.getTestNum();
        } else {
            maxTestTimes = runTimerSetting.getTestTimes();
        }

        //跑道数量
        runNum = Integer.parseInt(runTimerSetting.getRunNum());
        interceptPoint = runTimerSetting.getInterceptPoint();
        interceptWay = runTimerSetting.getInterceptWay();
        settingSensor = runTimerSetting.getSensor();
        int hostId = SettingHelper.getSystemSetting().getHostId();
        RunTimerManager.cmdSetting(runNum,hostId,interceptPoint,interceptWay,settingSensor);
        maxTestTimes = runTimerSetting.getTestTimes();
    }


    private RunTimerImpl runTimerListener = new RunTimerImpl(new RunTimerImpl.RunTimerListener() {

        @Override
        public void onGetTime(RunTimerResult result) {

            Log.i(TAG, result.toString());
            if (result.getOrder() == 1 && runTimerSetting.getInterceptWay() == 0 && !isForce) {
                if (isBaseTime){
                    baseTimer = result.getResult();
                    isBaseTime = true;
                }

            }
            Message msg = mHandler.obtainMessage();
            msg.obj = result;
            msg.what = TIME_RESPONSE;
            mHandler.sendMessageDelayed(msg, 100);

        }

        @Override
        public void onConnected(RunTimerConnectState connectState) {
            Log.i(TAG, connectState.toString());
            disposeConnect(connectState);
        }

        @Override
        public void onTestState(int state) {
            testState = state;
            Log.d(TAG, "testState===" + testState);
            switch (testState) {
                case 0:
                case 1:
                case 5://违规返回
                case 6://停止计时
                    baseTimer = 0;
                    changeState(new boolean[]{true, false, false, false, false});// 0 等待 1 强制 2 违规 3 成绩确认 4 预备
                    break;
                case 2://等待计时
                    if (isOverTimes) {
                        toastSpeak("已经超过测试次数");
                        RunTimerManager.stopRun();
                    }
                    changeState(new boolean[]{false, true, false, false, true});
                    if (baseTimer == 0 && runTimerSetting.getInterceptWay() == 0) {//红外拦截
                        baseTimer = System.currentTimeMillis();
                    }

                    break;
                case 3://启动
                    //算出误差时间
                    if (runTimerSetting.getInterceptWay() == 0) {
                        baseTimer = System.currentTimeMillis() - baseTimer;
                        isBaseTime = false;
                    }
                    disposeManager.keepTime();
                    changeState(new boolean[]{false, false, true, false, false});
                    keepTime();

                    break;
                case 4://获取到结果
//                    if (!isAuto){
//                        changeState(new boolean[]{false, false, true, false, false});
//                        isAuto = true;
//                    }else {
//                        changeState(new boolean[]{false, false, true, true, false});
//                    }
                    changeState(new boolean[]{false, false, true, true, false});
                    break;


            }
        }
    });

    /**
     * 连接问题
     *
     * @param connectState
     */
    private void disposeConnect(RunTimerConnectState connectState) {
        if (connectState.getStartPowerArray() != null) {//说明关于电源
//            int[] power = connectState.getPower();
//            if (power[3] == 0) {
//                if (promoteTimes.get("power3") == null || promoteTimes.get("power3") == 0) {
//                    toastSpeak("主控机盒子电源不足");
//                    promoteTimes.put("power3", 1);
//                }
//            } else {
//                promoteTimes.put("power3", 0);
//            }

//            if (power[1] == 0 && interceptPoint == 1) {
//                if (promoteTimes.get("power1") == null || promoteTimes.get("power1") == 0) {
//                    toastSpeak("起点拦截器电量不足");
//                    promoteTimes.put("power1", 1);
//                }
//            }
//
//            if (power[0] == 0 && interceptPoint == 2) {
//                if (promoteTimes.get("power0") == null || promoteTimes.get("power0") == 0) {
//                    toastSpeak("终点拦截器电量不足");
//                    promoteTimes.put("power0", 1);
//                }
//
//            }

//            if (connectState.getStartPowerArray() != null) {
//                byte[] endArray = connectState.getEndPowerArray();
//                for (int i = runNum; i > 0; i--) {
//                    if (endArray[8 - i] == 0) {
//                        if (promoteTimes.get("endArray" + i) == null || promoteTimes.get("endArray" + i) == 0) {
//                            toastSpeak("第" + i + "跑道电量不足");
//                            promoteTimes.put("endArray" + i, 1);
//                        }
//                    } else {
//                        promoteTimes.put("endArray" + i, 0);
//                    }
//                }
//            }

        } else {//关于连接状态
            if (interceptPoint == 1) {//起始点拦截
                if (connectState.getStartIntercept() == 0) {
                    if (promoteTimes.get("startIntercept") == null || promoteTimes.get("startIntercept") == 0) {
                        toastSpeak("起点拦截器异常");
                        promoteTimes.put("startIntercept", 1);
                    }
                } else {
                    promoteTimes.put("startIntercept", 0);
                }

            } else {//终点拦截
                if (connectState.getEndIntercept() == 0) {
                    if (promoteTimes.get("endIntercept") == null || promoteTimes.get("endIntercept") == 0) {
                        toastSpeak("终点拦截器异常");
                        promoteTimes.put("endIntercept", 1);
                    }
                } else {
                    promoteTimes.put("endIntercept", 0);
                }
            }

            //考虑到机器有电量显示，所以只处理连接状态

            byte[] array = new byte[8];
            if (connectState.getStartArray() != null && (interceptPoint == 1 || interceptPoint == 3)) {
                array = connectState.getStartArray();
            } else if (connectState.getEndArray() != null && (interceptPoint == 2 || interceptPoint == 3)) {
                array = connectState.getEndArray();
            }

            Message msg = mHandler.obtainMessage();
            HashMap<String, Integer> map = new HashMap();
            for (int i = 0; i < runNum; i++) {
                map.put("runNum" + i, array[7 - i] == 1 ? 1 : 0);
            }
            msg.obj = map;
            msg.what = CONNECT_STATE;
            mHandler.sendMessage(msg);
        }

//        byte[] array = new byte[8];
//        if (connectState.getStartPowerArray() != null && (interceptPoint == 1 || interceptPoint == 3)) {//起点终点都拦截点
//            array = connectState.getStartPowerArray();
//        } else if (connectState.getStartPowerArray() != null && (interceptPoint == 2 || interceptPoint == 3)) {
//            array = connectState.getEndPowerArray();
//        } else if (connectState.getStartArray() != null && (interceptPoint == 1 || interceptPoint == 3)) {
//            array = connectState.getStartArray();
//        } else if (connectState.getEndArray() != null && (interceptPoint == 2 || interceptPoint == 3)) {
//            array = connectState.getEndArray();
//        }
//
//        Message msg = mHandler.obtainMessage();
//        HashMap<String, Integer> map = new HashMap();
//        for (int i = runNum; i > 0; i--) {
//            map.put("runNum" + i, i);
//            if (array[8 - i] == 1) {
//                map.put("state", 1);
//            } else {
//                map.put("state", 0);
//            }
//
//        }
//        msg.obj = map;
//        msg.what = CONNECT_STATE;
//        mHandler.sendMessage(msg);
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(null);
        deviceManager.close();
//        if (runnable != null) {
//            runnable = null;
//        }
        if (disposable != null) {
            disposable.dispose();
        }
        EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_RESULT));

    }

//    private Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//            if (testState == 3 || testState == 4) {
//                //每100毫秒
//                mHandler.sendEmptyMessage(TIME_UPDATE);
//                mHandler.postDelayed(this, 100);
//            }
//            if (testState == 2 || testState == 5 || testState == 6) {
//                baseTimer = 0;
//                updateText("00:00.00");
//                mHandler.removeMessages(TIME_UPDATE);
//            }
//        }
//
//    };


    private Disposable disposable;
    private long disposeTime;

    public void keepTime() {
        disposable = Observable.interval(0, 100, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {

                        if (testState == 3 || testState == 4) {
                            disposeTime = aLong * 100;
                            updateTimeText((int) disposeTime);
                        }
                        if (testState == 2 || testState == 5 || testState == 6) {
                            disposeTime = 0;
                            updateText("00:00.00");
                            stop();
                        }

                    }
                });
    }

    public void stop() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case TIME_UPDATE:
                    if (testState == 3 || testState == 4) {
                        long time = System.currentTimeMillis() - baseTimer;
                        updateTimeText((int) time);
                        mHandler.sendEmptyMessageDelayed(TIME_UPDATE, 101);
                    }

                    if (testState == 2 || testState == 5 || testState == 6) {
                        baseTimer = 0;
                        updateText("00:00.00");
                        mHandler.removeMessages(TIME_UPDATE);
                    }
                    break;
                case TIME_RESPONSE:
                    if (msg.obj instanceof RunTimerResult) {
                        RunTimerResult result = (RunTimerResult) msg.obj;
                        if (runTimerSetting.getInterceptWay() == 1) {
                            Logger.i("getInterceptWay() == 1：" + result.getResult());
                            updateTableUI(result);
                        } else if (result.getResult() - baseTimer > 1000) {
                            Logger.i("getInterceptWay() == 0：" + (result.getResult() - baseTimer) + "baseTimer:" + baseTimer);
                            updateTableUI(result);
                        }

                    }
                    break;
                case CONNECT_STATE:
                    HashMap<String, Integer> map = (HashMap) msg.obj;
                    updateConnect(map);
                    break;
                case ILLEGAL_BACK:
                    illegalBack();
                    break;
            }
            return false;
        }
    });

    /**
     * 格式化时间
     *
     * @param time
     * @return
     */
    public String getFormatTime(int time) {
        return ResultDisplayUtils.getStrResultForDisplay(time, false);
    }

    private void updateTimeText(int time) {
        String timeValue = getFormatTime(time);
        updateText(timeValue);
        disposeManager.showTime(timeValue);
    }

    public void waitStart() {
        isForce = false;
//        isAuto = false ;
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc2, (byte) 0x00, (byte) 0x00)));
        RunTimerManager.waitStart();
    }

    public void forceStart() {
        isForce = true;
//        isAuto = true;
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc4, (byte) 0x00, (byte) 0x00)));
        RunTimerManager.forceStart();
    }

    public void faultBack() {
        showConfirm();
    }

    public void markConfirm() {
        mHandler.removeMessages(TIME_UPDATE);
//        deviceManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.cmd((byte) 0xc5, (byte) 0x00, (byte) 0x00)));
        RunTimerManager.stopRun();
    }

    public void stopRun() {
        RunTimerManager.stopRun();
    }

    private void showConfirm() {
        new AlertDialog.Builder(this).setMessage("确认要违规返回吗?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RunTimerManager.illegalBack();
                        mHandler.sendEmptyMessageDelayed(ILLEGAL_BACK, 100);
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", null).show();

    }

    public abstract void updateText(String time);

    public abstract void updateTableUI(RunTimerResult result);

    public abstract void updateConnect(HashMap<String, Integer> map);

    /**
     * 0 等待 1 强制 2 违规 3 成绩确认 4 预备
     * @param state
     */
    public abstract void changeState(final boolean[] state);

    public abstract void illegalBack();
}
