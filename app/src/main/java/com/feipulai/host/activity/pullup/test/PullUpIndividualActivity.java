package com.feipulai.host.activity.pullup.test;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.WaitDialog;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.device.serial.beans.PullUpStateResult;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.person.BasePersonTestActivity;
import com.feipulai.host.activity.pullup.setting.PullUpSettingActivity;
import com.feipulai.host.activity.setting.LEDSettingActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;

public class PullUpIndividualActivity extends BasePersonTestActivity
        implements PullUpTestFacade.Listener {

    private Handler handler = new MyHandler(this);
    private PullUpTestFacade facade;
    private WaitDialog changBadDialog;
    protected volatile int state = WAIT_CHECK_IN;
    private static final int WAIT_CHECK_IN = 0x0;
    private static final int WAIT_BEGIN = 0x1;
    private static final int TESTING = 0x2;
    private static final int WAIT_CONFIRM = 0x3;
    private static final int UPDATE_SCORE = 0x3;
    private boolean isStopped;

    @Override
    protected void initData() {
        super.initData();
        facade = new PullUpTestFacade(SettingHelper.getSystemSetting().getHostId(), this);
        state = WAIT_BEGIN;
        tvDevicePair.setVisibility(View.VISIBLE);
        tvDevicePair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBadDevice();
            }
        });
        txtLedSetting.setVisibility(View.GONE);
        setTextViewsVisibility(false, false, false, false, false);
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        boolean isTestNameEmpty = TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName());
        title = TestConfigs.machineNameMap.get(machineCode)
                + SettingHelper.getSystemSetting().getHostId() + "??????"
                + (isTestNameEmpty ? "" : ("-" + SettingHelper.getSystemSetting().getTestName()));
        return builder.setTitle(title).addRightText("????????????", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConfigurableNow()) {
                    startActivity(new Intent(PullUpIndividualActivity.this, LEDSettingActivity.class));
                } else {
                    toastSpeak("?????????,??????????????????????????????");
                }
            }
        }).addRightText("????????????", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProjectSetting();
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProjectSetting();
            }
        });

    }

    private void startProjectSetting() {
        IntentUtil.gotoActivity(this, PullUpSettingActivity.class);
    }

    @Override
    public void sendTestCommand(BaseStuPair stuPair) {

        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_FREE, stuPair.getBaseDevice().getDeviceId()));
        prepareForBegin();
    }

    private void prepareForBegin() {
        if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_DISCONNECT) {
            toastSpeak("???????????????,??????????????????");
            return;
        }
//        facade.startTest();
        state = WAIT_BEGIN;
        pair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
        updateDevice(pair.getBaseDevice());
        setTextViewsVisibility(true, true, false, false, false);
    }

    @Override
    public void pullStart() {
        state = TESTING;
        facade.startTest();
        isStopped = false;
    }

    @Override
    public void pullStop() {
        state = WAIT_CONFIRM;
        setTextViewsVisibility(false, false, false, false, false);
        facade.stopTest();
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END));
        pair.setEndTime(DateUtil.getCurrentTime());
        pair.setResult(0);
        isStopped = true;
    }

    @Override
    public void gotoItemSetting() {
        IntentUtil.gotoActivity(this, PullUpSettingActivity.class);
    }

    @Override
    public void stuSkip() {

    }

    @Override
    public void onDeviceConnectState(int state) {
        handler.sendEmptyMessage(state);
    }

    @Override
    public void onGetReadyTimerTick(long tick) {
        tickInUI(tick + "");
    }


    @Override
    public void onGetReadyTimerFinish() {
        tickInUI("??????");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setShowLed(pair);
                pair.setStartTime(DateUtil.getCurrentTime());
                setTextViewsVisibility(false, false, true, false, true);
            }
        });
    }

    @Override
    public void pullAbandon() {
        facade.abandonTest();
        state = WAIT_BEGIN;
        prepareForBegin();
    }

    @Override
    public void onScoreArrived(PullUpStateResult result) {
        if (isConfigurableNow()) {
            return;
        }
        Message msg = Message.obtain();
        msg.what = UPDATE_SCORE;
        msg.obj = result;
        handler.sendMessage(msg);
    }

    @Override
    public void onNoPairResponseArrived() {
        toastSpeak("?????????????????????,????????????,?????????");
    }

    @Override
    public void onNewDeviceConnect() {
        cancelChangeBad();
        pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
        toastSpeak("??????????????????");
    }

    public void cancelChangeBad() {
        facade.cancelLinking();
        if (changBadDialog != null) {
            changBadDialog.dismiss();
        }
    }

    public void showChangeBadDialog() {
        changBadDialog = new WaitDialog(this);
        changBadDialog.setCanceledOnTouchOutside(false);
        changBadDialog.setCancelable(false);
        changBadDialog.show();
        // ?????????dialog????????????????????????
        changBadDialog.setTitle("????????????????????????");
        changBadDialog.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelChangeBad();
            }
        });
    }


    public void changeBadDevice() {
        if (!isConfigurableNow()) {
            ToastUtils.showShort("?????????,?????????????????????");
            return;
        }
        facade.link();
        showChangeBadDialog();
    }

    private boolean isConfigurableNow() {
        return state == WAIT_CHECK_IN || state == WAIT_BEGIN;
    }

    @Override
    protected void handlerMessage(Message msg) {
        super.handlerMessage(msg);
        switch (msg.what) {
            case PullUpManager.STATE_DISCONNECT:
                cbDeviceState.setChecked(false);
                pair.getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
                break;

            case PullUpManager.STATE_FREE:
                cbDeviceState.setChecked(true);
//                if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_ERROR){
//
//                }
                pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                break;

            case UPDATE_SCORE:
                if (!isStopped) {
                    PullUpStateResult result = (PullUpStateResult) msg.obj;
                    pair.setResult(result.getResult());
                    pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
                    updateResult(pair);
                }

                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        handler.removeCallbacks(null);
        facade.stopTotally();
    }
}
