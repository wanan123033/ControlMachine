package com.feipulai.host.activity.vccheck;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BasePersonTestActivity;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.VCResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.host.utils.SharedPrefsUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.feipulai.host.activity.vccheck.VCConstant.RESULT_ARRIVED;
import static com.feipulai.host.activity.vccheck.VCConstant.RESULT_UPDATE;
import static com.feipulai.host.activity.vccheck.VCConstant.UPDATE_DEVICE;

/**
 * 肺活量测试
 */
public class VitalCapacityTestActivity extends BasePersonTestActivity {

    private static final String TAG = "VitalCapacityActivity";
    private ExecutorService mExecutorService;
    private GetResultRunnable resultRunnable = new GetResultRunnable();
    private boolean inited;
    private int maxDevice;
    private MyHandler mHandler = new MyHandler(this);
    private volatile boolean isStart;

    boolean originValueUpdated = false;
    // 若连续3秒内没收到数据则认为连接异常
    private AtomicInteger sendTimes = new AtomicInteger(0);
    private boolean toFreeTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SerialDeviceManager.getInstance().setRS232ResiltListener(vcResultImpl);

        if (!inited) {
            mExecutorService = Executors.newSingleThreadExecutor();
            mExecutorService.submit(resultRunnable);
        }
        inited = true;
        isStart = true;
    }

    @Override
    public void sendTestCommand(BaseStuPair stuPair) {
        vcResultImpl.setTestState(TestState.WAIT_RESULT);
        toFreeTest = false;
        originValueUpdated = false;
    }

    @Override
    public List<BaseDeviceState> findDevice() {
        maxDevice = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.VITAL_CAPACITY_TEST_NUMBER, 1);
        List<BaseDeviceState> deviceStates = new ArrayList<>();
        for (int i = 0; i < maxDevice; i++) {
            deviceStates.add(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, i + 1));
        }
        return deviceStates;
    }

    
    private class GetResultRunnable implements Runnable {

        @Override
        public void run() {
            byte[] retrieve = {(byte) 0xaa, (byte) 0xc1, 0x00, 0x00, (byte) 0xc1};
            try {
                while (isStart) {
                    Log.i(TAG, "===>" + "send");
                    Log.i(TAG, "===>" + "sendTimes" + sendTimes.get());
                    if (sendTimes.addAndGet(1) >= 30) {
                        mHandler.sendEmptyMessage(SerialConfigs.VITAL_CAPACITY_ERROR_MSG);
                    }
                    SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, retrieve));
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class MyHandler extends Handler {

        private WeakReference<VitalCapacityTestActivity> weakReference;

        public MyHandler(VitalCapacityTestActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            VitalCapacityTestActivity activity = weakReference.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case RESULT_ARRIVED:
                    if (msg.obj instanceof VCResult) {
                        VCResult result = (VCResult) msg.obj;
                        activity.onResultArrived(result);
                    }
                    break;
                case RESULT_UPDATE:
                    if (msg.obj instanceof VCResult) {
                        VCResult result1 = (VCResult) msg.obj;
                        activity.onResultUpdate(result1);
                    }

                    break;
                case UPDATE_DEVICE:
                    if (msg.obj instanceof BaseDeviceState){
                        BaseDeviceState baseDeviceState = (BaseDeviceState) msg.obj;
                        activity.updateDevice(baseDeviceState);
                    }

                    break;
                case SerialConfigs.VITAL_CAPACITY_ERROR_MSG:
                    activity.sendTimes.set(0);
                    Log.i(TAG, "===>" + "error");
                    activity.updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
                    activity.vcResultImpl.setDeviceState(true);
                    break;

            }
        }
    }

    private void onResultUpdate(VCResult result) {
        if (result.getResult() < 300)//无效数据
        {
            return;
        }
        if (toFreeTest)//因为如果添加学生后直接到自由测试，返回后此处也会更新 所以直接return
            return;
        BaseStuPair baseStuPair = new BaseStuPair();
        BaseDeviceState deviceState = new BaseDeviceState(BaseDeviceState.STATE_END, 1);
        baseStuPair.setBaseDevice(deviceState);
        baseStuPair.setResult(result.getResult());
        updateResult(baseStuPair);
    }

    private void onResultArrived(VCResult result) {
        if (result.getResult() < 300) {
            return;
        }
        if (toFreeTest)//因为如果添加学生后直接到自由测试，返回后此处也会更新 所以直接return
            return;

        BaseStuPair baseStuPair = new BaseStuPair();
        //todo 此处修改设备ID
        BaseDeviceState deviceState = new BaseDeviceState(BaseDeviceState.STATE_END, 1);
        baseStuPair.setResult(result.getResult());
        baseStuPair.setBaseDevice(deviceState);
        updateResult(baseStuPair);
        updateDevice(deviceState);
    }

    @Override
    public void switchToFreeTest() {
        toFreeTest = true;
        Intent intent = new Intent(this, VitalCapacityFaceIDActivity.class);
        startActivity(intent);
    }

    private VCResultImpl vcResultImpl = new VCResultImpl(new VCResultImpl.VCResultListener() {
        @Override
        public void onResultArrived(VCResult result) {
            Message msg = mHandler.obtainMessage();
            msg.obj = result;
            msg.what = RESULT_ARRIVED;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onResultUpdate(VCResult result) {
            Message msg = mHandler.obtainMessage();
            msg.obj = result;
            msg.what = RESULT_UPDATE;
            mHandler.sendMessage(msg);
        }

        @Override
        public void updateDevice(BaseDeviceState baseDeviceState) {
            Message msg = mHandler.obtainMessage();
            msg.obj = baseDeviceState;
            msg.what = UPDATE_DEVICE;
            mHandler.sendMessage(msg);
        }

        @Override
        public void sendTime(int time) {
            sendTimes.set(0);
        }
    });


    @Override
    protected void onStop() {
        super.onStop();
        isStart = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdown();
        SerialDeviceManager.getInstance().close();
    }


}
