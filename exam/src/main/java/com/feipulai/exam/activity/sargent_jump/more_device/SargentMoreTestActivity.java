package com.feipulai.exam.activity.sargent_jump.more_device;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.SargentJumpMore;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.SargentJumpResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.SargentJumpImpl;
import com.feipulai.exam.activity.sargent_jump.SargentSetting;
import com.feipulai.exam.activity.sargent_jump.SargentSettingActivity;
import com.feipulai.exam.activity.sargent_jump.pair.SargentPairActivity;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.orhanobut.logger.utils.LogUtils;

import java.util.List;

import butterknife.OnClick;

import static com.feipulai.exam.activity.sargent_jump.Constants.GET_SCORE_RESPONSE;

/**
 * 一对多个人测试
 */
public class SargentMoreTestActivity extends BaseMoreActivity {
    private static final String TAG = "SargentMoreTestActivity";
    private SargentSetting sargentSetting;
    private final static int MAX_DISCONNECT = 3;
    private int[] deviceState = {};
    //SARGENT JUMP

    //    public byte[] CMD_SARGENT_JUMP_STOP = {0X54, 0X44, 00, 0X10, 01, 0x01, 00, 0x02, 00, 00, 00, 00, 00, 0x14, 0x27, 0x0d};
//    public byte[] CMD_SARGENT_JUMP_GET_SCORE = {0X54, 0X44, 00, 0X10, 01, 0x01, 00, 0x04, 00, 00, 00, 00, 00, 0x16, 0x27, 0x0d};
    private final int SEND_EMPTY = 1;
    /**
     * 开启助跑  0不助跑 1助跑
     */
    public int runUp;

    @Override
    protected void initData() {
        sargentSetting = SharedPrefsUtil.loadFormSource(this, SargentSetting.class);
        if (null == sargentSetting) {
            sargentSetting = new SargentSetting();
        }
        super.initData();
        setFaultEnable(sargentSetting.isPenalize());
    }

    @Override
    protected void onResume() {
        super.onResume();
        sargentSetting = SharedPrefsUtil.loadFormSource(this, SargentSetting.class);
        if (null == sargentSetting) {
            sargentSetting = new SargentSetting();
        }
//        setDeviceCount(sargentSetting.getSpDeviceCount());
        deviceState = new int[sargentSetting.getSpDeviceCount()];
        for (int i = 0; i < deviceState.length; i++) {

            deviceState[i] = 0;//连续5次检测不到认为掉线
            setShowGetData(i+1,true);
        }
        runUp = sargentSetting.getRunUp();
        RadioManager.getInstance().setOnRadioArrived(resultImpl);
        sendEmpty();
        setNextClickStart(false);
    }

    @Override
    public int setTestCount() {
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            return TestConfigs.sCurrentItem.getTestNum();
        } else {
            return sargentSetting.getTestTimes();
        }
    }

    @Override
    public int setDeviceCount() {
        return sargentSetting.getSpDeviceCount();
    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
        if (sargentSetting.isFullReturn()) {
            if (sex == Student.MALE) {
                return result >= Integer.valueOf(sargentSetting.getMaleFull()) * 10;
            } else {
                return result >= Integer.valueOf(sargentSetting.getFemaleFull()) * 10;
            }
        }
        return false;
    }

    @Override
    public void gotoItemSetting() {
        startActivity(new Intent(this, SargentSettingActivity.class));
    }

    @Override
    protected void sendTestCommand(BaseStuPair pair, int index) {
        LogUtils.operation("摸高开始测试:第="+(index+1)+"机:"+pair.getStudent().toString());
        pair.setTestTime(DateUtil.getCurrentTime()+"");
        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
        updateDevice(pair.getBaseDevice());
        int id = pair.getBaseDevice().getDeviceId();
        sendStart((byte) id);
    }

    @Override
    public void getData(int pos) {
        SargentJumpMore.getData(pos+1);
    }

    private void sendStart(byte id) {
        SargentJumpMore.sendStart(id);
    }


    public void sendEmpty() {
        Log.i(TAG, "james_send_empty");
        for (int i = 0; i < deviceState.length; i++) {
            BaseDeviceState baseDevice = deviceDetails.get(i).getStuDevicePair().getBaseDevice();
            if (deviceState[i] == 0) {
                if (baseDevice.getState() != BaseDeviceState.STATE_ERROR) {
                    baseDevice.setState(BaseDeviceState.STATE_ERROR);
                    updateDevice(baseDevice);
                }

            } else {
                if (baseDevice.getState() == BaseDeviceState.STATE_ERROR) {
                    baseDevice.setState(BaseDeviceState.STATE_NOT_BEGAIN);
                    updateDevice(baseDevice);
                }
                deviceState[i] -= 1;
            }


        }
        for (DeviceDetail detail : deviceDetails) {
            SargentJumpMore.sendEmpty(detail.getStuDevicePair().getBaseDevice().getDeviceId());
        }
        mHandler.sendEmptyMessageDelayed(SEND_EMPTY, 1000);


    }

    private int sum(byte[] cmd, int index) {
        int sum = 0;
        for (int i = 2; i < index; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }

    private SargentJumpImpl resultImpl = new SargentJumpImpl(new SargentJumpImpl.SargentJumpListener() {
        @Override
        public void onResultArrived(SargentJumpResult result) {
            Message msg = mHandler.obtainMessage();
            msg.obj = result;
            msg.what = GET_SCORE_RESPONSE;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onStopTest() {

        }

        @Override
        public void onSelfCheck() {

        }

        @Override
        public void onFree(int deviceId) {
            deviceState[deviceId-1] = MAX_DISCONNECT;
        }

        @Override
        public void onMatch(SargentJumpResult match) {


        }
    });


    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case GET_SCORE_RESPONSE:
                    SargentJumpResult result = (SargentJumpResult) msg.obj;
                    if (result != null)
                        LogUtils.all("摸高更新成绩:score="+result.getScore()+",result="+result.toString());
                    for (DeviceDetail detail : deviceDetails) {
                        if (detail.getStuDevicePair().getBaseDevice().getDeviceId() == result.getDeviceId()) {
                            int dbResult = result.getScore() * 10;
                            if (runUp == 1) {
                                onResultArrived(dbResult, detail.getStuDevicePair());
                            } else {
                                if (detail.getStuDevicePair().getBaseHeight() == 0) {
                                    detail.getStuDevicePair().setBaseHeight(dbResult);
                                } else {
                                    onResultArrived(dbResult - detail.getStuDevicePair().getBaseHeight(), detail.getStuDevicePair());
                                }
                            }

                        }

                    }
                    break;
                case SEND_EMPTY:
                    sendEmpty();
                    break;
            }

            return false;
        }
    });

    private void onResultArrived(int result, BaseStuPair stuPair) {
        LogUtils.operation("摸高成绩更新:"+stuPair.getStudent()+"---"+result);
        if (result < 0 || result > (sargentSetting.getBaseHeight() + 216) * 10) {
            toastSpeak("数据异常，请重测");
            return;
        }
        if (stuPair == null || stuPair.getStudent() == null)
            return;
        stuPair.setResult(result);
        stuPair.setEndTime(DateUtil.getCurrentTime()+"");
        stuPair.setResultState(RoundResult.RESULT_STATE_NORMAL);
        if (sargentSetting.isFullReturn()) {
            if (stuPair.getStudent().getSex() == Student.MALE) {
                stuPair.setFullMark(stuPair.getResult() >= Integer.parseInt(sargentSetting.getMaleFull()) * 10);
            } else {
                stuPair.setFullMark(stuPair.getResult() >= Integer.parseInt(sargentSetting.getFemaleFull()) * 10);
            }
        }
        updateResult(stuPair);
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END, stuPair.getBaseDevice().getDeviceId()));

    }


    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.life("SargentMoreTestActivity onStop");
        mHandler.removeCallbacksAndMessages(null);
    }

    @OnClick({R.id.tv_device_pair})
    public void onClick(View view) {
        LogUtils.operation("摸高跳转至SargentPairActivity");
        startActivity(new Intent(this, SargentPairActivity.class));
    }
    @Override
    public void setRoundNo(Student student, int roundNo) {
    }
}
