package com.feipulai.exam.activity.sargent_jump;

import android.os.Message;
import android.util.Log;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.SargentJumpResult;
import com.orhanobut.logger.Logger;

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
    private int count = 0;
    private int jump;
    private int temp;

    public SargentJumpImpl(SargentJumpListener jumpListener) {
        this.jumpListener = jumpListener;
    }

    @Override
    public void onRS232Result(Message msg) {
        SargentJumpResult result = (SargentJumpResult) msg.obj;
        switch (msg.what) {
            case SARGENT_JUMP_EMPTY_RESPONSE:
                if (jumpListener!= null){
                    jumpListener.onFree(result.getDeviceId());
                }


                break;
            case SARGENT_JUMP_START_RESPONSE:
                Log.i(TAG, "开始");
                break;
            case SARGENT_JUMP_STOP_RESPONSE:
                Log.i(TAG, "结束");
                break;
            case SARGENT_JUMP_GET_SCORE_RESPONSE:
                if (jumpListener== null){
                    return;
                }
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
                Logger.i( "=>SargentJumpImpl====>" + result.getScore());
                break;


        }
    }

    @Override
    public void onRadioArrived(Message msg) {
        SargentJumpResult result = (SargentJumpResult) msg.obj;
        switch (msg.what) {
            case SARGENT_JUMP_EMPTY_RESPONSE:
                if (jumpListener== null){
                    return;
                }
                jumpListener.onFree(result.getDeviceId());

                break;
            case SARGENT_JUMP_START_RESPONSE:
                Log.i(TAG, "开始");
                break;
            case SARGENT_JUMP_STOP_RESPONSE:
                Log.i(TAG, "结束");
                break;
            case SARGENT_JUMP_GET_SCORE_RESPONSE:
                if (jumpListener== null){
                    return;
                }
                jumpListener.onResultArrived(result);
//                temp = result.getScore();
//                count++;
//                if (count == 0 || temp != jump) {//控制只传一次结果
//                    jump = temp;
//                    jumpListener.onResultArrived(result);
//                }
//                if (count == 2) {
//                    count = 0;
//                }
                Log.i(TAG, "result" + result.getScore());
                Logger.i( "=>SargentJumpImpl====>" + result.getScore());
                break;
            case SARGENT_JUMP_SET_MATCH:
                if (jumpListener!= null){
                    jumpListener.onMatch(result);
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
