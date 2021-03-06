package com.feipulai.exam.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.led.RunLEDManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;

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
    @BindView(R.id.sp_show_color)
    Spinner spShowColor;
    @BindView(R.id.sp_show_color_s)
    Spinner spShowColorS;
    @BindView(R.id.rv_color_s)
    RelativeLayout rv_color_s;
    @BindView(R.id.rv_color)
    RelativeLayout rvColor;
    private LEDManager mLEDManager;
    private RunLEDManager runLEDManager;
    private int hostId;
    private int flag;
    private int ledMode;
    private int ledType;
    private int ledColor;
    private int ledColors;

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
        ledColor = SettingHelper.getSystemSetting().getLedColor() - 1;
        ledColors = SettingHelper.getSystemSetting().getLedColor2() - 1;
//        rvMode.setVisibility(ledType == 0 ? View.VISIBLE : View.GONE);
//
//        ledMode = SettingHelper.getSystemSetting().getLedMode();
//        rvLed.setVisibility(ledMode == 0 ? View.GONE : View.VISIBLE);
        rvLed.setVisibility(View.GONE);
//        String[] strings = new String[]{"??????1??????", "??????2??????", "??????3??????", "??????4??????"};
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
//                mLEDManager.showSubsetString(hostId, i + 1, "???????????????", 3, 3, false, true);
//            }
//        });
        initSp();
    }

    private void initSp() {
        String[] spinnerItems = {"????????????", "????????????"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spShowMode.setAdapter(adapter);
        spShowMode.setSelection(ledMode == 0 ? 0 : 1);
        spShowMode.setEnabled(false);
//        spShowMode.setOnItemSelectedListener(this);

        String[] spinnerItems1 = {"4.1?????????", "4.1??????", "4.8?????????"};
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems1);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ledVersion.setAdapter(adapter1);
        ledVersion.setSelection(ledType);
        ledVersion.setOnItemSelectedListener(this);
        if (ledType == 2) {
            rvColor.setVisibility(View.VISIBLE);
            rv_color_s.setVisibility(View.VISIBLE);
            mLEDManager.setVersions(LEDManager.LED_VERSION_4_8);
        } else {
            rvColor.setVisibility(View.GONE);
            rv_color_s.setVisibility(View.GONE);
        }

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"??????", "??????", "??????"});
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spShowColor.setAdapter(colorAdapter);
        spShowColor.setSelection(ledColor);
        ArrayAdapter<String> colorAdapters = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"??????", "??????", "??????"});
        colorAdapters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spShowColorS.setAdapter(colorAdapters);
        spShowColorS.setSelection(ledColors);
        spShowColorS.setSelection(ledColors);
        spShowColor.setOnItemSelectedListener(this);
        spShowColorS.setOnItemSelectedListener(this);


    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("???????????????");
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
                            runLEDManager.resetLEDScreen(hostId, title);
                        } else {
                            if (SettingHelper.getSystemSetting().getLedVersion() == 0 || SettingHelper.getSystemSetting().getLedVersion() == LEDManager.LED_VERSION_4_8) {
                                mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), hostId, 1);

                                mLEDManager.showSubsetString(hostId, 1, title, 0, true, false, LEDManager.MIDDLE, 1);
                                mLEDManager.showSubsetString(hostId, 1, "???????????????", 3, 3, false, true, 1);
                            } else {
                                mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), hostId);
                                mLEDManager.showString(hostId, title, 0, true, false, LEDManager.MIDDLE, 1);
                                mLEDManager.showString(hostId, "???????????????", 3, 3, true, true, 1);
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
        SettingHelper.updateSettingCache(SettingHelper.getSystemSetting());
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
                if (position == 2) {
                    rvColor.setVisibility(View.VISIBLE);
                    rv_color_s.setVisibility(View.VISIBLE);
                    mLEDManager.setVersions(LEDManager.LED_VERSION_4_8);
                } else {
                    rvColor.setVisibility(View.GONE);
                    rv_color_s.setVisibility(View.GONE);

                }
                break;
            case R.id.sp_show_color:
                SettingHelper.getSystemSetting().setLedColor(position);
                break;
            case R.id.sp_show_color_s:
                SettingHelper.getSystemSetting().setLedColor2(position);
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
