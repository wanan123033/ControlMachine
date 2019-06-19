package com.feipulai.host.activity.vccheck;

import android.os.Message;
import android.util.Log;

import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.VCResult;

import static com.feipulai.host.activity.vccheck.TestState.DATA_DEALING;

/**
 * Created by pengjf on 2018/12/5.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class VCResultImpl implements SerialDeviceManager.RS232ResiltListener {
    private VCResultListener resultListener ;
    private static final String TAG = "VCResultImpl";
    private int currentValue = 0 ;
    private int finalResultCount;
    public  boolean originValueUpdated;
    private TestState mTestState = TestState.UN_STARTED;
    private VCResult result ;
    private boolean findFoul;

    public VCResultImpl(VCResultListener resultListener){
        this.resultListener = resultListener ;
    }
    public void setTestState(TestState testState){
        mTestState = testState ;
    }

    public void setDeviceState(boolean deviceState){
        findFoul = deviceState ;
    }

    @Override
    public void onRS232Result(Message msg) {
        switch (msg.what) {
            case SerialConfigs.VITAL_CAPACITY_RESULT:

                resultListener.sendTime(0);
                result = (VCResult) msg.obj;
                // FileUtil.saveLog(result.getResult()+"肺活量数据\r\n");
                Log.i(TAG, "===>" + result.getResult() + "ml");
                if (mTestState == TestState.WAIT_RESULT) {
                    resultListener.updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ONUSE, 1));
                    Log.i(TAG, "WAIT_RESULT==currentValue=>" + currentValue + "ml");
                    //currentValue值非0变动时为第一次吹气

                    if (result.getResult() != currentValue) {
                        //在开始记录之前,必须保证这个值被初始化
                        if (!originValueUpdated) {
                            currentValue = result.getResult();
                            originValueUpdated = true;
                            break;
                        }
                        //开始计数
                        currentValue = 0;
                        mTestState = TestState.RESULT_UPDATING;
                        originValueUpdated = false;
                    }
                    //Logger.i(result.toString());
                } else if (mTestState == TestState.RESULT_UPDATING) {
                    //计数更新
                    //计数出现15次
                    if (result.getResult() == currentValue) {
                        finalResultCount++;
                        if (finalResultCount == 15) {
                            result.setResult(currentValue);
                            if (currentValue > 300) {//认为小于300位=为无效数据
                                resultListener.onResultArrived(result);
                                mTestState = DATA_DEALING ;
                            }
                            currentValue = 0;
                            finalResultCount = 0;
                        }
                    } else if (result.getResult() < currentValue && currentValue > 300) {
                        //值降低了,这个时候就是存在换气了,直接锁定最终结果
                        result.setResult(currentValue);
                        resultListener.onResultArrived(result);
                        mTestState = DATA_DEALING ;
                        currentValue = 0;
                        finalResultCount = 0;
                    } else {
                        currentValue = result.getResult();
                    }
                    resultListener.onResultUpdate(result);
                } else if (mTestState == DATA_DEALING) {
                    resultListener.onResultUpdate(result);
                    mTestState = TestState.UN_STARTED;
                } else {//没有开始 并且之前发现错误  此时此时检测正常做修改
                    if (findFoul) {
                        resultListener.updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
                        findFoul = false;
                    }
                }
                break;
        }
    }


    public interface VCResultListener{
        void onResultArrived(VCResult result);
        void onResultUpdate(VCResult result);
        void updateDevice(BaseDeviceState baseDeviceState);
        void sendTime(int time);
    }
}
