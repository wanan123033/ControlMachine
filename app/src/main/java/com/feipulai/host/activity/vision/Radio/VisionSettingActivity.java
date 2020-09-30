package com.feipulai.host.activity.vision.Radio;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.standjump.StandJumpSetting;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;

/**
 * 视力设置
 * Created by zzs on  2020/9/29
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class VisionSettingActivity extends BaseTitleActivity {

    @BindView(R.id.sp_test_distance)
    Spinner spTestDistance;
    @BindView(R.id.sp_result_type)
    Spinner spResultType;
    @BindView(R.id.sp_stop_time)
    Spinner spStopTime;
    private VisionSetting visionSetting;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_vision_setting;
    }

    @Override
    protected void initData() {
        //获取项目设置
        visionSetting = SharedPrefsUtil.loadFormSource(this, VisionSetting.class);
        if (visionSetting == null)
            visionSetting = new VisionSetting();

        ArrayAdapter spDistanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.vision_distance));
        spDistanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTestDistance.setAdapter(spDistanceAdapter);
        spTestDistance.setSelection(visionSetting.getDistance());

        ArrayAdapter spResultTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.vision_result_type));
        spResultTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spResultType.setAdapter(spResultTypeAdapter);
        spResultType.setSelection(visionSetting.getTestType());

        ArrayAdapter spStopTimeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.vision_stop_time));
        spStopTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStopTime.setAdapter(spStopTimeAdapter);
        spStopTime.setSelection(visionSetting.getStopTime() == 0 ? 0 : visionSetting.getStopTime() == 3 ? 1 : 2);
    }

    @OnItemSelected({R.id.sp_stop_time, R.id.sp_result_type, R.id.sp_test_distance})
    public void spinnerItemSelected(Spinner spinner, int position) {

        switch (spinner.getId()) {
            case R.id.sp_test_distance:
                visionSetting.setDistance(position);
                break;
            case R.id.sp_result_type:
                visionSetting.setTestType(position);
                break;
            case R.id.sp_stop_time:
                visionSetting.setTestType(position == 0 ? 0 : position == 1 ? 3 : 5);
                break;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPrefsUtil.save(this, visionSetting);

    }

}
