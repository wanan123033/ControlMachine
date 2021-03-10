package com.feipulai.exam.activity.medicineBall;

import android.os.Message;
import android.util.Log;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.MedicineBallResult;
import com.feipulai.device.serial.beans.MedicineBallSelfCheckResult;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.Item;
import com.orhanobut.logger.Logger;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by pengjf on 2018/12/5.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class MedicineBallResultImpl implements SerialDeviceManager.RS232ResiltListener {
    private static final String TAG = "MedicineBallResultImpl";
    private MainThreadDisposeListener disposeListener;
    private MedicineBallSetting medicineBallSetting;

    public MedicineBallResultImpl(MainThreadDisposeListener disposeListener) {
        this.disposeListener = disposeListener;
        medicineBallSetting = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), MedicineBallSetting.class);
        if (null == medicineBallSetting) {
            medicineBallSetting = new MedicineBallSetting();
        }
    }

    @Override
    public void onRS232Result(Message msg) {
        switch (msg.what) {
            case SerialConfigs.MEDICINE_BALL_EMPTY_RESPONSE:
                disposeListener.onFree();
                log("空闲命令回复");
                break;
            case SerialConfigs.MEDICINE_BALL_SELF_CHECK_RESPONSE:
                MedicineBallSelfCheckResult selfCheckResult = (MedicineBallSelfCheckResult) msg.obj;
                log("自检结果 是否存在未校准杆:" + selfCheckResult.isInCorrect() + "\t未校准杆号:" +
                        Arrays.toString(selfCheckResult.getIncorrectPoles()));

                BaseStuPair pair = new BaseStuPair();
                BaseDeviceState device = new BaseDeviceState();
                //TODO 设备ID 需要讨论
                device.setDeviceId(1);
                pair.setBaseDevice(device);
                disposeListener.onSelfCheck(selfCheckResult);
                break;
            case SerialConfigs.MEDICINE_BALL_START_RESPONSE:
                log("开始命令回复");
                Logger.i("MedicineBall开始测试");
                break;
            case SerialConfigs.MEDICINE_BALL_GET_SCORE_RESPONSE:
                MedicineBallResult result = (MedicineBallResult) msg.obj;
                Logger.i("MedicineBallResult:" + result.toString());
                boolean fault = result.isFault();
                log("是否犯规:" + fault);
                log("0点距离:" + "cm");
                log("扫描点数：" + result.getSweepPoint());
                log("获得成绩:" + result.getResult() + "cm");
                log("总成绩:" + result.getResult() + "cm");
                Item item = TestConfigs.sCurrentItem;
                if (item.getMaxValue() != 0) {
                    if (result.getResult() > item.getMaxValue() * 100 || result.getResult() < item.getMinValue() * 100) {
                        ToastUtils.showShort("测量值出错");
                    } else {
                        disposeListener.onResultArrived(result);
                    }
                } else {

                    disposeListener.onResultArrived(result);
                }

                break;
            case SerialConfigs.MEDICINE_BALL_STOP_RESPONSE:
                log("结束命令回复");
                disposeListener.onStopTest();
                break;
            default:
                break;
        }
    }



    private void log(String msg) {
        Log.i(TAG, msg);
    }

    interface MainThreadDisposeListener {
        void onResultArrived(MedicineBallResult result);

        void onStopTest();

        void onSelfCheck(MedicineBallSelfCheckResult selfCheckResult);

        void onFree();
    }
}
