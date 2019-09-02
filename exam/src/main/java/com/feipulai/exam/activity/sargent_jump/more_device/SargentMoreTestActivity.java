package com.feipulai.exam.activity.sargent_jump.more_device;

import android.os.Handler;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.SargentJumpResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.SargentJumpImpl;
import com.feipulai.exam.activity.sargent_jump.SargentSetting;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;

import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.feipulai.exam.activity.sargent_jump.Constants.GET_SCORE_RESPONSE;

public class SargentMoreTestActivity extends SargentJumpMoreActivity {

    private SargentSetting sargentSetting;
    private ScheduledExecutorService checkService  = Executors.newSingleThreadScheduledExecutor();
    private HashSet<BaseStuPair> pairHashSet = new HashSet<>();
    @Override
    protected void initData() {
        super.initData();
        sargentSetting = SharedPrefsUtil.loadFormSource(this, SargentSetting.class);
        if (null == sargentSetting) {
            sargentSetting = new SargentSetting();
        }

        RadioManager.getInstance().init();
        RadioManager.getInstance().setOnRadioArrived(resultImpl);
        sendEmpty();

    }

    @Override
    public int setTestCount() {
        return sargentSetting.getTestTimes();
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

    }

    @Override
    protected void sendTestCommand(BaseStuPair pair, int index) {
        pairHashSet.add(pair);
        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
        updateDevice(pair.getBaseDevice());

    }


    @Override
    public void toStart(int pos) {

    }
    public void sendEmpty() {
        checkService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                        SerialConfigs.CMD_SARGENT_JUMP_EMPTY));
            }
        }, 1000, 3000, TimeUnit.MILLISECONDS);

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

        }

        @Override
        public void onMatch(SargentJumpResult match) {


        }
    });

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case GET_SCORE_RESPONSE:
                    SargentJumpResult result = (SargentJumpResult) msg.obj;
                    for (BaseStuPair stuPair : pairHashSet){
                        if (stuPair.getBaseDevice().getDeviceId() == result.getDeviceId()){
                            if (stuPair.getBaseDevice().getState() == BaseDeviceState.STATE_ONUSE){
                                int dbResult = result.getScore() * 10;
                                onResultArrived(dbResult, stuPair);
                            }
                        }

                    }
                    break;
            }

            return false;
        }
    });

    private void onResultArrived(int result, BaseStuPair stuPair) {
        if (result< sargentSetting.getBaseHeight()*10 || result > (sargentSetting.getBaseHeight()+116)*10 ){
            toastSpeak("数据异常，请重测");
            return;
        }
        if (stuPair ==null || stuPair.getStudent() == null)
            return;
        if (sargentSetting.isFullReturn()) {
            if (stuPair.getStudent().getSex() == Student.MALE) {
                stuPair.setFullMark(stuPair.getResult() >= Integer.parseInt(sargentSetting.getMaleFull()) * 10);
            } else {
                stuPair.setFullMark(stuPair.getResult() >= Integer.parseInt(sargentSetting.getFemaleFull()) * 10);
            }
        }
        stuPair.setResult(result);
        stuPair.setResultState(RoundResult.RESULT_STATE_NORMAL);
        updateResult(stuPair);
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END, stuPair.getBaseDevice().getDeviceId()));
    }
}
