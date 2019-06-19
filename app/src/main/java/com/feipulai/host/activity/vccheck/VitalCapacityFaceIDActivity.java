package com.feipulai.host.activity.vccheck;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.feipulai.device.led.LEDManager;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BasePersonFaceIDActivity;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.VCResult;
import com.feipulai.device.serial.command.ConvertCommand;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.feipulai.host.activity.vccheck.VCConstant.RESULT_ARRIVED;
import static com.feipulai.host.activity.vccheck.VCConstant.RESULT_UPDATE;
import static com.feipulai.host.activity.vccheck.VCConstant.UPDATE_DEVICE;

public class VitalCapacityFaceIDActivity extends BasePersonFaceIDActivity  {
    private static final String TAG = "VitalCapacityFaceIDActi";
    private ExecutorService mExecutorService ;
    private GetResultRunnable resultRunnable = new GetResultRunnable();
    private VitalHandler mHandler = new VitalHandler(this);
    private SerialDeviceManager mSerialManager;
    private LEDManager mLEDManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLEDManager = new LEDManager();
	    mSerialManager = SerialDeviceManager.getInstance();
        mSerialManager.setRS232ResiltListener(vcResultImpl);

        mExecutorService = Executors.newSingleThreadExecutor();
        mExecutorService.submit(resultRunnable);
    }

    @Override
    public BaseDeviceState findDevice() {

        return new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN,1);
    }

    @Override
    public void sendTestCommand(@NonNull BaseStuPair baseStuPair) {
        vcResultImpl.setTestState(TestState.WAIT_RESULT);
        if (mLEDManager == null){
            mLEDManager = new LEDManager();
            mLEDManager.link(TestConfigs.sCurrentItem.getMachineCode(), hostId);
            mLEDManager.resetLEDScreen(hostId,TestConfigs.sCurrentItem.getItemName());
        }

        if (mSerialManager == null){
            mSerialManager = SerialDeviceManager.getInstance();
            mSerialManager.setRS232ResiltListener(vcResultImpl);

        }
        mLEDManager.showString(hostId, "请测试", 5, 1, true, true);
    }

//    @Override
//    public String setUnit() {
//        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getUnit())) {
//            return "ml";
//        }
//        return TestConfigs.sCurrentItem.getUnit();
//    }

    private int sendTimes ;
    private boolean findFoul ;

    private class GetResultRunnable implements Runnable{
        @Override
        public void run() {
            byte[] retrieve = {(byte) 0xaa, (byte) 0xc1, 0x00, 0x00, (byte) 0xc1};
            while (true) {
                if (true) {
                    sendTimes ++ ;
                    if (sendTimes > 30 ){
                        mHandler.sendEmptyMessage(SerialConfigs.VITAL_CAPACITY_ERROR_MSG);
                    }
                    Log.i(TAG, "===>" + "sendCommand");
                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, retrieve));
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class VitalHandler extends Handler{
        WeakReference<VitalCapacityFaceIDActivity> weakReference ;
        public VitalHandler(VitalCapacityFaceIDActivity activity){
            weakReference = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            VitalCapacityFaceIDActivity activity = weakReference.get();
            if (activity == null ) {
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
                    activity.sendTimes = 0;
                    Log.i(TAG, "===>" + "error");
                    activity.vcResultImpl.setDeviceState(true);
                    activity.updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
                    break;
            }

        }
    }

    private void onResultUpdate(VCResult result) {
        BaseStuPair stuPair = new BaseStuPair();
        BaseDeviceState device = new BaseDeviceState(BaseDeviceState.STATE_ONUSE, 1);
        stuPair.setBaseDevice(device);
        stuPair.setResult(result.getResult());
        updateResult(stuPair);
    }

    private void onResultArrived(VCResult result) {
        BaseDeviceState device = new BaseDeviceState(BaseDeviceState.STATE_END, 1);
        BaseStuPair stuPair = new BaseStuPair();
        stuPair.setResult(result.getResult());
        stuPair.setBaseDevice(device);
        updateResult(stuPair);
        updateDevice(device);

        String text = result + "ml";
        mLEDManager.showString(hostId, text, 5, 1, true, true);


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
            sendTimes = 0 ;
        }
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdown();
        mSerialManager.close();
    }
}
