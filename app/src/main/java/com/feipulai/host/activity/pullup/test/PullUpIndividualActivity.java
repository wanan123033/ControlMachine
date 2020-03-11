package com.feipulai.host.activity.pullup.test;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.WaitDialog;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.device.serial.beans.PullUpStateResult;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
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

    @Override
    protected void initData() {
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

    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        boolean isTestNameEmpty = TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName());
        title = TestConfigs.machineNameMap.get(machineCode)
                + SettingHelper.getSystemSetting().getHostId() + "号机"
                + (isTestNameEmpty ? "" : ("-" + SettingHelper.getSystemSetting().getTestName()));
        return builder.setTitle(title) .addRightText("外接屏幕", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConfigurableNow()) {
                    startActivity(new Intent(PullUpIndividualActivity.this, LEDSettingActivity.class));
                } else {
                    toastSpeak("测试中,不能进行外接屏幕设置");
                }
            }
        }).addRightText("项目设置", new View.OnClickListener() {
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
            toastSpeak("设备未连接,不能开始测试");
            return;
        }
//        facade.startTest();
        state = WAIT_BEGIN;
        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
        updateDevice(pair.getBaseDevice());
        setTextViewsVisibility(true,true,false,false,false);
    }

    @Override
    public void pullStart() {
        state = TESTING;
        facade.startTest();
    }

    @Override
    public void pullStop() {
        state = WAIT_CONFIRM;
        setTextViewsVisibility(false,false,false,false,false);
        facade.stopTest();
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END));
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
        tickInUI("开始");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTextViewsVisibility(false,false,true,false,true);
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
        toastSpeak("未收到子机回复,设置失败,请重试");
    }

    @Override
    public void onNewDeviceConnect() {
        cancelChangeBad();
        toastSpeak("设备连接成功");
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
        // 必须在dialog显示出来后再调用
        changBadDialog.setTitle("请重启待连接设备");
        changBadDialog.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelChangeBad();
            }
        });
    }


    public void changeBadDevice() {
        if (!isConfigurableNow()) {
            ToastUtils.showShort("测试中,不允许更换设备");
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
                if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_ERROR){
                    pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                }
                break;

            case UPDATE_SCORE:
                PullUpStateResult result = (PullUpStateResult) msg.obj;
                pair.setResult(result.getResult());
                updateResult(pair);
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
