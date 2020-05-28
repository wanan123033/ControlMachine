package com.feipulai.exam.activity.sargent_jump;

import android.os.Message;
import android.util.Log;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.SargentJumpResult;
import com.orhanobut.logger.Logger;

import static com.feipulai.device.serial.SerialConfigs.SARGENT_GET_DATA;
import static com.feipulai.device.serial.SerialConfigs.SARGENT_JUMP_EMPTY_RESPONSE;
import static com.feipulai.device.serial.SerialConfigs.SARGENT_JUMP_GET_SCORE_RESPONSE;
import static com.feipulai.device.serial.SerialConfigs.SARGENT_JUMP_SET_MATCH;
import static com.feipulai.device.serial.SerialConfigs.SARGENT_JUMP_START_RESPONSE;
import static com.feipulai.device.serial.SerialConfigs.SARGENT_JUMP_STOP_RESPONSE;

/**
 * Created by pengjf on 2019/5/13.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class SargentJumpImpl implements SerialDeviceManager.RS232ResiltListener, RadioManager.OnRadioArrivedListener {
    private static final String TAG = "SargentJumpImpl";
    private SargentJumpListener jumpListener;
    private boolean []showRes = {true,true,true,true};
    private int []tempId = new int[4];
    public SargentJumpImpl(SargentJumpListener jumpListener) {
        this.jumpListener = jumpListener;
    }

    @Override
    public void onRS232Result(Message msg) {

        switch (msg.what) {
            case SARGENT_JUMP_EMPTY_RESPONSE:
                if (jumpListener != null) {
                    jumpListener.onFree(0);
                }


                break;
            case SARGENT_JUMP_START_RESPONSE:
                Log.i(TAG, "开始");
                break;
            case SARGENT_JUMP_STOP_RESPONSE:
                Log.i(TAG, "结束");
                break;
            case SARGENT_JUMP_GET_SCORE_RESPONSE:
                if (jumpListener == null) {
                    return;
                }
                SargentJumpResult result = (SargentJumpResult) msg.obj;
                jumpListener.onResultArrived(result);
//                temp = result.getScore();
//                count++;
//                if (count == 0 || temp != jump) {//控制只传一次结果
//                    jump = temp;
//                    jumpListener.onResultArrived(result);
//                }
//                if (count == 3) {
//                    count = 0;
//                }
//                Log.i(TAG, "结果" + result.getScore());
                Logger.i("=>SargentJumpImpl====>" + result.getScore());
                break;


        }
    }

    @Override
    public void onRadioArrived(Message msg) {
        switch (msg.what) {
            case SARGENT_JUMP_EMPTY_RESPONSE:
                if (jumpListener == null) {
                    return;
                }
                SargentJumpResult result = (SargentJumpResult) msg.obj;
                if (result != null) {
                    jumpListener.onFree(result.getDeviceId());
                    Logger.i("SargentJumpImpl","score:"+result.getScore()+"state:"+result.getState()+"id:"+result.getDeviceId());

                    if (result.getState() == 1 ) {//因为一个成绩会轮询查到多次
                        if (showRes[result.getDeviceId()-1]){
                            jumpListener.onResultArrived(result);
                            Logger.i("=>SargentJumpImpl====>" + result.getScore());
                            showRes[result.getDeviceId()-1] = false;
                            tempId[result.getDeviceId()-1] = result.getDeviceId();
                        }

                    } else if (result.getState() == 0) {

                        if (tempId[result.getDeviceId()-1] == result.getDeviceId())
                            showRes[result.getDeviceId()-1] = true;
                    }
                }else {
                    jumpListener.onFree(1);
                }
                break;
            case SARGENT_JUMP_START_RESPONSE:
                Log.i(TAG, "开始");
                break;
            case SARGENT_JUMP_STOP_RESPONSE:
                Log.i(TAG, "结束");
                break;
            case SARGENT_JUMP_GET_SCORE_RESPONSE:
                SargentJumpResult res = (SargentJumpResult) msg.obj;
                if (jumpListener == null || res == null) {
                    return;
                }
                jumpListener.onResultArrived(res);
                Logger.i("=>SargentJumpImpl====>" + res.getScore());

                break;
            case SARGENT_JUMP_SET_MATCH:
                if (jumpListener != null) {
                    SargentJumpResult r = (SargentJumpResult) msg.obj;
                    jumpListener.onMatch(r);
                }
                break;
            case SARGENT_GET_DATA:
                SargentJumpResult result1 = (SargentJumpResult) msg.obj;
                if (showRes[result1.getDeviceId()-1]){
                    jumpListener.onResultArrived(result1);
                    Logger.i("=>SargentJumpImpl====>" + result1.getScore());
                    showRes[result1.getDeviceId()-1] = false;
                    tempId[result1.getDeviceId()-1] = result1.getDeviceId();
                }
                break;
        }
    }

    public interface SargentJumpListener {
        void onResultArrived(SargentJumpResult result);

        void onStopTest();

        void onSelfCheck();

        void onFree(int deviceId);

        void onMatch(SargentJumpResult match);
    }
}
