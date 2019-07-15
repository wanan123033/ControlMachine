package com.feipulai.exam.activity.setting;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.medicineBall.MedicineBallSetting;
import com.feipulai.exam.activity.pullup.setting.PullUpSetting;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.activity.standjump.StandJumpSetting;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;
import com.feipulai.exam.config.SharedPrefsConfigs;

import butterknife.BindView;
import butterknife.OnItemSelected;

/**
 * Created by zzs on 2018/11/23
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class AdvancedSettingActivity extends BaseTitleActivity
        implements CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.edit_appkey)
    EditText editAppkey;
    @BindView(R.id.sp_situp_angle)
    Spinner spSitupAngle;
    @BindView(R.id.sw_situp)
    CheckBox swSitup;
    @BindView(R.id.sw_pullup)
    CheckBox swPullup;
    @BindView(R.id.sw_volleyball)
    CheckBox swVolleyball;
    @BindView(R.id.sw_med_ball)
    CheckBox swMedBall;
    @BindView(R.id.sw_standjump)
    CheckBox swStandjump;
    private SystemSetting systemSetting;
    private SitUpSetting sitUpSetting;
    private PullUpSetting pullUpSetting;
    private VolleyBallSetting volleyBallSetting;
    private MedicineBallSetting medicineBallSetting;
    private StandJumpSetting standJumpSetting;
    private static final Integer[] ANGLES = {55, 65, 75};

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_advanced_setting;
    }

    @Override
    protected void initData() {

        systemSetting = SettingHelper.getSystemSetting();
        sitUpSetting = SharedPrefsUtil.loadFormSource(this, SitUpSetting.class);
        pullUpSetting = SharedPrefsUtil.loadFormSource(this, PullUpSetting.class);
        volleyBallSetting = SharedPrefsUtil.loadFormSource(this, VolleyBallSetting.class);
        medicineBallSetting = SharedPrefsUtil.loadFormSource(this, MedicineBallSetting.class);
        standJumpSetting = SharedPrefsUtil.loadFormSource(this, StandJumpSetting.class);
        String serverToken = SharedPrefsUtil.getValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.DEFAULT_SERVER_TOKEN, "dGVybWluYWw6dGVybWluYWxfc2VjcmV0");
        editAppkey.setText(serverToken);
        swSitup.setOnCheckedChangeListener(this);
        swSitup.setChecked(sitUpSetting.isPenalize());

        swPullup.setOnCheckedChangeListener(this);
        swPullup.setChecked(pullUpSetting.isPenalize());

        swVolleyball.setOnCheckedChangeListener(this);
        swVolleyball.setChecked(volleyBallSetting.isPenalize());

        swMedBall.setOnCheckedChangeListener(this);
        swMedBall.setChecked(medicineBallSetting.isPenalize());

        swStandjump.setOnCheckedChangeListener(this);
        swStandjump.setChecked(standJumpSetting.isPenalize());

        ArrayAdapter angleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ANGLES);
        angleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSitupAngle.setAdapter(angleAdapter);

        for (int i = 0; i < ANGLES.length; i++) {
            if (ANGLES[i] == sitUpSetting.getAngle()) {
                spSitupAngle.setSelection(i);
                break;
            }
        }
    }

    @OnItemSelected({R.id.sp_situp_angle})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_situp_angle:
                sitUpSetting.setAngle(ANGLES[position]);
                break;

        }
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("高级设置").addLeftText("设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SettingHelper.updateSettingCache(systemSetting);
        SharedPrefsUtil.save(this, sitUpSetting);
        SharedPrefsUtil.save(this, pullUpSetting);
        SharedPrefsUtil.save(this, volleyBallSetting);
        SharedPrefsUtil.save(this, medicineBallSetting);
        SharedPrefsUtil.save(this, standJumpSetting);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {

            case R.id.sw_pullup:
                pullUpSetting.setPenalize(isChecked);
                break;

            case R.id.sw_situp:
                sitUpSetting.setPenalize(isChecked);
                break;

            case R.id.sw_volleyball:
                volleyBallSetting.setPenalize(isChecked);
                break;
            case R.id.sw_med_ball:
                medicineBallSetting.setPenalize(isChecked);
                break;
            case R.id.sw_standjump:
                standJumpSetting.setPenalize(isChecked);
                break;
        }

    }

}
