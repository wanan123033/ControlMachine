package com.feipulai.host.activity.freedom;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.setting.LEDSettingActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by zzs on  2019/10/8
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public abstract class BaseFreedomTestActivity extends BaseTitleActivity {


    @BindView(R.id.cb_device_state)
    CheckBox cbDeviceState;
    @BindView(R.id.txt_test_result)
    TextView txtTestResult;
    @BindView(R.id.txt_start_test)
    TextView txtStartTest;
    public boolean isStartTest = false;
    @BindView(R.id.txt_device_pair)
    public  TextView txtDevicePair;
    private ClearHandler clearHandler = new ClearHandler(this);
    private BaseStuPair baseStuPair;
    private LEDManager mLEDManager;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_base_freedom;
    }

    @Override
    protected void initData() {
        mLEDManager = new LEDManager();
        mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PrinterManager.getInstance().close();
        if (TestConfigs.sCurrentItem != null) {
            mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
            String title = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()) + " " + SettingHelper.getSystemSetting().getHostId();
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), title, mLEDManager.getX(title), 0, true, false);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), getString(R.string.fairplay), 3, 3, false, true);
            mLEDManager = null;
        }


    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title = String.format(getString(R.string.host_name), TestConfigs.machineNameMap.get(machineCode) + "(自由测试)", SettingHelper.getSystemSetting().getHostId());
        return builder.setTitle(title).addRightText(R.string.item_setting_title, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        });
    }

    /**
     * 跳转项目设置页面
     */
    public abstract void gotoItemSetting();

    /**
     * 开始测试
     */
    public abstract void startTest();

    public abstract void stopTest();

    @OnClick({R.id.txt_led_setting, R.id.txt_start_test})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_led_setting:
                IntentUtil.gotoActivity(this, LEDSettingActivity.class);
                break;
            case R.id.txt_start_test:
                if (isStartTest) {
                    if (baseStuPair != null) {
                        toastSpeak("测试中，不允许停止");
                        return;
                    }
                    txtStartTest.setText("开始测试");
                    isStartTest = false;
                    stopTest();
                } else {
                    txtStartTest.setText("停止测试");
                    isStartTest = true;
                    startTest();
                }
                break;
        }
    }

    public void settTestResult(BaseStuPair stuPair) {
        baseStuPair = stuPair;
        txtTestResult.setText(((stuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(stuPair.getResult())));
        String ledResult = ((stuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) ? getString(R.string.foul) : ResultDisplayUtils.getStrResultForDisplay(stuPair.getResult()));
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), ledResult, mLEDManager.getX(ledResult), 2, true, true);
    }


    public void setDeviceState(BaseDeviceState deviceState) {
        if (deviceState.getState() == BaseDeviceState.STATE_END) {
            broadResult(baseStuPair);
            printResult(baseStuPair);
            txtTestResult.setText(((baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult())));
            clearHandler.sendEmptyMessageDelayed(0, 4000);
        }
        Log.e("TAG----",(deviceState.getState() != BaseDeviceState.STATE_ERROR)+"----");
        if (deviceState.getState() != BaseDeviceState.STATE_ERROR) {
            cbDeviceState.setChecked(true);
        } else {
            cbDeviceState.setChecked(false);
        }
    }

    /**
     * 播报结果
     */
    private void broadResult(@NonNull BaseStuPair baseStuPair) {
        if (SettingHelper.getSystemSetting().isAutoBroadcast()) {
            if (baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) {
                TtsManager.getInstance().speak(String.format(getString(R.string.speak_foul), ""));
            } else {

                TtsManager.getInstance().speak(String.format(getString(R.string.speak_result), "", ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult())));
            }


        }
    }

    private void printResult(@NonNull BaseStuPair baseStuPair) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        PrinterManager.getInstance().print(
                String.format(getString(R.string.host_name), TestConfigs.sCurrentItem.getItemName(), SettingHelper.getSystemSetting().getHostId()));
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_stu_result), (baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) ?
                        getString(R.string.foul) : ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult())) );
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_time), TestConfigs.df.format(Calendar.getInstance().getTime())) );
        PrinterManager.getInstance().print(" \n");
        PrinterManager.getInstance().print(" \n");

    }
    protected void setScore(int score){

        txtTestResult.setText(ResultDisplayUtils.getStrResultForDisplay(score));
    }
    /**
     * i清理学生信息
     */
    private static class ClearHandler extends Handler {

        private WeakReference<BaseFreedomTestActivity> mActivityWeakReference;

        public ClearHandler(BaseFreedomTestActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BaseFreedomTestActivity activity = mActivityWeakReference.get();
            Logger.i("ClearHandler:清理学生信息");
            if (activity != null) {
                if (activity.isStartTest) {
                    activity.baseStuPair = null;
                    activity.txtTestResult.setText("");
                    activity.mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "自由测试", activity.mLEDManager.getX("自由测试"), 1, true, true);
                    activity.startTest();
                }

            }

        }
    }
}
