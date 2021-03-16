package com.feipulai.exam.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.led.RunLEDManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.adapter.LedMoreAdapter;
import com.feipulai.exam.config.TestConfigs;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * ledManager
 */
public class LEDSettingActivity extends BaseTitleActivity implements AdapterView.OnItemSelectedListener {

    @BindView(R.id.btn_led_connect)
    Button btnLedConnect;
    @BindView(R.id.sp_show_mode)
    Spinner spShowMode;
    @BindView(R.id.rv_led)
    RecyclerView rvLed;
    @BindView(R.id.led_version)
    Spinner ledVersion;
    @BindView(R.id.rv_mode)
    RelativeLayout rvMode;
    private LEDManager mLEDManager;
    private RunLEDManager runLEDManager;
    private int hostId;
    private int flag;
    private int ledMode;
    private int ledType;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_led_setting;
    }

    @Override
    protected void initData() {
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZFP && SettingHelper.getSystemSetting().getRadioLed() == 0) {
            runLEDManager = new RunLEDManager();
            flag = 0;
        } else {
            mLEDManager = new LEDManager();
            flag = 1;
        }

        ledType = SettingHelper.getSystemSetting().getLedVersion();
//        rvMode.setVisibility(ledType == 0 ? View.VISIBLE : View.GONE);
//
//        ledMode = SettingHelper.getSystemSetting().getLedMode();
//        rvLed.setVisibility(ledMode == 0 ? View.GONE : View.VISIBLE);
          rvLed.setVisibility(View.GONE);
//        String[] strings = new String[]{"屏幕1连接", "屏幕2连接", "屏幕3连接", "屏幕4连接"};
//        LedMoreAdapter adapter = new LedMoreAdapter(Arrays.asList(strings));
//        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
//        rvLed.setLayoutManager(layoutManager);
//        rvLed.setAdapter(adapter);
//        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
//                mLEDManager.link(TestConfigs.sCurrentItem.getMachineCode(), hostId, i + 1);
//                String title = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode())
//                        + " " + hostId;
//                mLEDManager.showSubsetString(hostId, i + 1, title, 0, true, false, LEDManager.MIDDLE);
//                mLEDManager.showSubsetString(hostId, i + 1, "菲普莱体育", 3, 3, false, true);
//            }
//        });
        initSp();
    }

    private void initSp() {
        String[] spinnerItems = {"单屏模式", "多屏模式"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spShowMode.setAdapter(adapter);
        spShowMode.setSelection(ledMode == 0 ? 0 : 1);
        spShowMode.setEnabled(false);
//        spShowMode.setOnItemSelectedListener(this);

        String[] spinnerItems1 = {"4.1及以上", "4.1以下"};
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems1);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ledVersion.setAdapter(adapter1);
        ledVersion.setSelection(ledType == 0 ? 0 : 1);
        ledVersion.setOnItemSelectedListener(this);


    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("显示屏设置");
    }

    @Override
    protected void onResume() {
        super.onResume();
        hostId = SettingHelper.getSystemSetting().getHostId();
    }

    @OnClick({R.id.btn_led_connect, R.id.btn_led_self, R.id.img_led_luminance_munus, R.id.img_led_luminance_add})
    public void onViewClicked(View view) {

        switch (view.getId()) {
            case R.id.btn_led_connect:
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        String title = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode())
                                + " " + hostId;
                        if (flag == 0) {
                            runLEDManager.link(hostId);
                            runLEDManager.resetLEDScreen(hostId,title);
                        } else {
                            if (SettingHelper.getSystemSetting().getLedVersion() == 0) {
                                mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), hostId, 1);

                                mLEDManager.showSubsetString(hostId, 1, title, 0, true, false, LEDManager.MIDDLE);
                                mLEDManager.showSubsetString(hostId, 1, "菲普莱体育", 3, 3, false, true);
                            } else {
                                mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), hostId);
                                mLEDManager.showString(hostId, title, 0, true, false, LEDManager.MIDDLE);
                                mLEDManager.showString(hostId, "菲普莱体育", 3, 3, false, true);
                            }
                        }
                    }
                });

                break;

            case R.id.btn_led_self:
                if (flag == 0) {
                    runLEDManager.test(hostId);
                } else {

                    if (SettingHelper.getSystemSetting().getLedVersion() == 1) {
                        mLEDManager.test(TestConfigs.sCurrentItem.getMachineCode(), hostId);
                    } else {
                        for (int i = 1; i <= 4; i++) {
                            mLEDManager.test(TestConfigs.sCurrentItem.getMachineCode(), hostId, i);
                        }
                    }
                }
                break;

            case R.id.img_led_luminance_munus:
                if (flag == 0) {
                    runLEDManager.decreaseLightness(TestConfigs.sCurrentItem.getMachineCode(), hostId);
                } else {
                    if (SettingHelper.getSystemSetting().getLedVersion() == 0) {
                        mLEDManager.decreaseLightness(TestConfigs.sCurrentItem.getMachineCode(), hostId);
                    } else {
                        for (int i = 1; i <= 4; i++) {
                            mLEDManager.decreaseLightness(TestConfigs.sCurrentItem.getMachineCode(), hostId, i);
                        }
                    }

                }
                break;

            case R.id.img_led_luminance_add:
                if (flag == 0) {
                    runLEDManager.increaseLightness(hostId);
                } else {

                    if (SettingHelper.getSystemSetting().getLedMode() == 0) {
                        mLEDManager.increaseLightness(TestConfigs.sCurrentItem.getMachineCode(), hostId);
                    } else {
                        for (int i = 1; i <= 4; i++) {
                            mLEDManager.increaseLightness(TestConfigs.sCurrentItem.getMachineCode(), hostId, i);
                        }
                    }
                }
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_show_mode:
                SettingHelper.getSystemSetting().setLedMode(position);
                rvLed.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
                btnLedConnect.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                break;
            case R.id.led_version:
                SettingHelper.getSystemSetting().setLedVersion(position);
                rvMode.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                if (rvLed.getVisibility() == View.VISIBLE) {
                    rvLed.setVisibility(position == 1 ? View.GONE : View.VISIBLE);
                }
                if (btnLedConnect.getVisibility() == View.GONE) {
                    btnLedConnect.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
                }

                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
