package com.feipulai.exam.activity.setting;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.exam.activity.medicineBall.MedicineBallSetting;
import com.feipulai.exam.activity.pullup.setting.PullUpSetting;
import com.feipulai.exam.activity.sargent_jump.SargentSetting;
import com.feipulai.exam.activity.sitreach.SitReachSetting;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.activity.standjump.StandJumpSetting;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.ww.fpl.libarcface.common.Constants;
import com.ww.fpl.libarcface.util.ConfigUtil;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zzs on 2018/11/23
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class AdvancedSettingActivity extends BaseTitleActivity{

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
    @BindView(R.id.sw_sit_reach)
    CheckBox swSitReach;
    @BindView(R.id.sw_standjump2)
    CheckBox swStandjump2;
    @BindView(R.id.sw_sargent)
    CheckBox swSargent;
    @BindView(R.id.sw_medicine_ball)
    CheckBox swMedicineBall;
    @BindView(R.id.sp_jump_rope_state_count)
    Spinner spJumpRopeStateCount;
    private SystemSetting systemSetting;
    private SitUpSetting sitUpSetting;
    private PullUpSetting pullUpSetting;
    private VolleyBallSetting volleyBallSetting;
    private MedicineBallSetting medicineBallSetting;
    private StandJumpSetting standJumpSetting;

    private SitReachSetting sitReachSetting;
    private SargentSetting sargentSetting;
    private static final Integer[] ANGLES = {55, 65, 75};
    private Integer[] ropeStateCount = new Integer[28];
    private JumpRopeSetting jumpRopeSetting;

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
        jumpRopeSetting = SharedPrefsUtil.loadFormSource(this, JumpRopeSetting.class);
        sitReachSetting = SharedPrefsUtil.loadFormSource(this,SitReachSetting.class);
        sargentSetting = SharedPrefsUtil.loadFormSource(this,SargentSetting.class);

        String serverToken = SharedPrefsUtil.getValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.DEFAULT_SERVER_TOKEN, "dGVybWluYWw6dGVybWluYWxfc2VjcmV0");
        editAppkey.setText(serverToken);
        swSitup.setChecked(sitUpSetting.isPenalize());

        swPullup.setChecked(pullUpSetting.isPenalize());

        swVolleyball.setChecked(volleyBallSetting.isPenalize());

        swMedBall.setChecked(medicineBallSetting.isPenalize());

        swStandjump.setChecked(standJumpSetting.isPenalize());

        swSitReach.setChecked(sitReachSetting.isPenalize());

        swStandjump2.setChecked(standJumpSetting.isPenalizeFoul());

        swSargent.setChecked(sargentSetting.isPenalize());

        swMedicineBall.setChecked(medicineBallSetting.isPenalizeFoul());
        ArrayAdapter angleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ANGLES);
        angleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSitupAngle.setAdapter(angleAdapter);

        for (int i = 0; i < ANGLES.length; i++) {
            if (ANGLES[i] == sitUpSetting.getAngle()) {
                spSitupAngle.setSelection(i);
                break;
            }
        }

        for (int i = 0; i < 28; i++) {
            ropeStateCount[i] = i + 3;
        }
        ArrayAdapter ropeStateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ropeStateCount);
        ropeStateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spJumpRopeStateCount.setAdapter(ropeStateAdapter);
        spJumpRopeStateCount.setSelection(jumpRopeSetting.getGetStateLoopCount() - 3);
    }

    @OnItemSelected({R.id.sp_situp_angle, R.id.sp_jump_rope_state_count})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_situp_angle:
                sitUpSetting.setAngle(ANGLES[position]);
                break;
            case R.id.sp_jump_rope_state_count:
                jumpRopeSetting.setGetStateLoopCount(ropeStateCount[position]);
                break;
        }
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("高级设置");
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPrefsUtil.putValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.DEFAULT_SERVER_TOKEN, "dGVybWluYWw6dGVybWluYWxfc2VjcmV0");
        SettingHelper.updateSettingCache(systemSetting);
        SharedPrefsUtil.save(this, sitUpSetting);
        SharedPrefsUtil.save(this, pullUpSetting);
        SharedPrefsUtil.save(this, volleyBallSetting);
        SharedPrefsUtil.save(this, medicineBallSetting);
        SharedPrefsUtil.save(this, standJumpSetting);
        SharedPrefsUtil.save(this, jumpRopeSetting);
        SharedPrefsUtil.save(this, sitReachSetting);
        SharedPrefsUtil.save(this, sargentSetting);
    }

    @OnCheckedChanged({R.id.sw_pullup,R.id.sw_situp,R.id.sw_volleyball,R.id.sw_med_ball,R.id.sw_standjump,R.id.sw_sit_reach,R.id.sw_standjump2,R.id.sw_sargent,R.id.sw_medicine_ball})
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
            case R.id.sw_sit_reach:
                sitReachSetting.setPenalize(isChecked);
                break;
            case R.id.sw_standjump2:
                standJumpSetting.setPenalizeFoul(isChecked);
                break;
            case R.id.sw_sargent:
                sargentSetting.setPenalize(isChecked);
                break;
            case R.id.sw_medicine_ball:
                medicineBallSetting.setPenalizeFoul(isChecked);
                break;
        }
    }


    private void activeFace() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                int activeCode = FaceEngine.activeOnline(getApplicationContext(), Constants.APP_ID, Constants.SDK_KEY);
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            ToastUtils.showShort(getString(R.string.active_success));
                            ConfigUtil.setISEngine(getApplicationContext(), true);
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            ToastUtils.showShort(getString(R.string.already_activated));
                        } else {
                            ToastUtils.showShort(getString(R.string.active_failed));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

}
