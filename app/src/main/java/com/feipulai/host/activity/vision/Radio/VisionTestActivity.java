package com.feipulai.host.activity.vision.Radio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.GetJsonDataUtil;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.height_weight.HWConfigs;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.BaseEvent;
import com.feipulai.host.config.EventConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.UploadResultUtil;
import com.feipulai.host.netUtils.netapi.ServerIml;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.feipulai.host.utils.TimerIntervalUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * ??????
 * Created by zzs on  2020/9/15
 * ??????????????????????????????????????????   ????????????:??????
 */
public class VisionTestActivity extends BaseTitleActivity implements RadioManager.OnRadioArrivedListener {

    @BindView(R.id.iv_e)
    ImageView ivE;
    @BindView(R.id.txt_time)
    TextView txt_time;
    @BindView(R.id.txt_mar)
    TextView txtMar;
    @BindView(R.id.txt_hint)
    TextView txtHint;
    private VisionBean visionBean;
    private int index = 0; //0 ?????????????????? -1 ????????????????????? -2 ?????????
    private ArrayList<VisionBean> visionBeans;
    private int direction;
    private Random random = new Random();

    private VisionSetting visionSetting;
    private VisionBean.VisionData visionData;//????????????
    private int errorCount = 0;//???????????????

    private Student student;

    private RoundResult roundResult = new RoundResult();
    private LEDManager ledManager = new LEDManager();

//    private int timeLimit = 0;
//    private boolean isStartThrand = true;//????????????????????????
//    private boolean isStartCheck = true;

    private TimerIntervalUtil intervalUtil;
    private BleTouchEvent bleTouchEvent = new BleTouchEvent(this);

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title = String.format(getString(R.string.host_name), TestConfigs.machineNameMap.get(machineCode), SettingHelper.getSystemSetting().getHostId());
        return builder.setTitle(title);
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_vision_test;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int key = bleTouchEvent.onTouch(event);
        switch (key) {
            case BleTouchEvent.EVENT_TOP://???
                checkKey(VisionBean.DIRECTION_TOP);
                break;
            case BleTouchEvent.EVENT_RIGHT://???
                checkKey(VisionBean.DIRECTION_RIGHT);
                break;
            case BleTouchEvent.EVENT_BOTTOM://???
                checkKey(VisionBean.DIRECTION_BOTTOM);
                break;
            case BleTouchEvent.EVENT_LEFT://???
                checkKey(VisionBean.DIRECTION_LEFT);
                break;
            case BleTouchEvent.EVENT_CANCEL://??????
                checkKey(0);
                break;
            case BleTouchEvent.EVENT_CONFIRM://??????
                if (index == -1) {
                    index = 0;
                    errorCount = 0;
                    ivE.setVisibility(View.VISIBLE);
                    txtHint.setVisibility(View.GONE);
                    visionData = visionBean.getVisions().get(index);
                    setImageWidth();
                    if (visionSetting.getStopTime() != 0) {
                        txt_time.setText(visionSetting.getStopTime() + "");
                        intervalUtil.startTime();
                    } else {
                        txt_time.setVisibility(View.GONE);
                    }
//                            isStartCheck = true;
                }
                break;
        }


        return super.onTouchEvent(event);
    }

    @Override
    protected void initData() {
        NavigationBarStatusBar(this, true);
        //??????????????????
        visionSetting = SharedPrefsUtil.loadFormSource(this, VisionSetting.class);
        if (visionSetting == null)
            visionSetting = new VisionSetting();

        if (getIntent().getExtras() != null) {
            student = (Student) getIntent().getExtras().getSerializable("STUDENT");
//            txtStuName.setText(student.getStudentName());
        }

        String JsonData = GetJsonDataUtil.getJson(this, "vision.json");//??????assets????????????json????????????
        Type type = new TypeToken<List<VisionBean>>() {
        }.getType();
        visionBeans = new Gson().fromJson(JsonData, type);
        visionBean = visionBeans.get(visionSetting.getDistance());

        visionData = visionBean.getVisions().get(index);


        intervalUtil = new TimerIntervalUtil(new TimerIntervalUtil.TimerAccepListener() {
            @Override
            public void timer(Long time) {
                String showtime = (visionSetting.getStopTime() - time) + "";
                txt_time.setText(showtime);

                byte[] data = new byte[16];
                try {
//                    if (student != null && !TextUtils.isEmpty(student.getLEDStuName())) {
//                        byte[] resultData = student.getLEDStuName().getBytes("GB2312");
//                        System.arraycopy(resultData, 0, data, 0, resultData.length);
//                    }
                    byte[] resultData = showtime.getBytes("GB2312");
                    System.arraycopy(resultData, 0, data, 14, resultData.length);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                ledManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 3, false, true);

                if (time == visionSetting.getStopTime()) {
                    intervalUtil.stop();
                    ++errorCount;
                    if (visionData.getErrorCount() == errorCount) {//??????
                        saveResult();

                    } else {
                        intervalUtil.startTime();
                        setImageWidth();
                    }
                }

            }
        });
        if (student == null) {
            index = -1;
            txtHint.setVisibility(View.VISIBLE);
            txtHint.setText("???????????????\n???????????????");
            toastSpeak("??????????????????????????????");
            ivE.clearAnimation();
            ivE.setVisibility(View.GONE);
        } else {
            setImageWidth();
            if (visionSetting.getStopTime() != 0) {
//            timeThread.start();
                txt_time.setText(visionSetting.getStopTime() + "");
                intervalUtil.startTime();
            } else {
                txt_time.setVisibility(View.GONE);
            }
        }

        initResult();

    }

    /**
     * ???????????????????????????
     *
     * @param activity
     */
    public static void NavigationBarStatusBar(Activity activity, boolean hasFocus) {
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void initResult() {
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        if (student != null) {
            roundResult.setStudentCode(student.getStudentCode());
        }

        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
        roundResult.setTestTime(DateUtil.getCurrentTime() + "");
        roundResult.setRoundNo(1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RadioManager.getInstance().setOnRadioArrived(this);
    }


    @Override
    public void onRadioArrived(final Message msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (msg.obj!=null){
                    byte key = (byte) msg.obj;

                    switch (key) {
                        case 0x38://???
                            checkKey(VisionBean.DIRECTION_TOP);
                            break;
                        case 0x36://???
                            checkKey(VisionBean.DIRECTION_RIGHT);
                            break;
                        case 0x32://???
                            checkKey(VisionBean.DIRECTION_BOTTOM);
                            break;
                        case 0x34://???
                            checkKey(VisionBean.DIRECTION_LEFT);
                            break;
                        case 0x42://??????
                            checkKey(0);
                            break;
                        case 0x35://??????
                            if (index == -1) {
                                index = 0;
                                errorCount = 0;
                                ivE.setVisibility(View.VISIBLE);
                                txtHint.setVisibility(View.GONE);
                                visionData = visionBean.getVisions().get(index);
                                setImageWidth();
                                if (visionSetting.getStopTime() != 0) {
                                    txt_time.setText(visionSetting.getStopTime() + "");
                                    intervalUtil.startTime();
                                } else {
                                    txt_time.setVisibility(View.GONE);
                                }
//                            isStartCheck = true;
                            }
                            break;
                    }
                }

            }
        });

    }

    private void checkKey(int keyDirection) {
        if (index == -1 || index == -2) {
            return;
        }
        if (visionSetting.getStopTime() != 0) {
            intervalUtil.stop();
            intervalUtil.startTime();
        }

        txt_time.setText(visionSetting.getStopTime() + "");
        Logger.d("checkKey===>keyDirection:" + keyDirection + "       direction:" + direction);
        if (keyDirection == direction) {
            if (visionBean.getVisions().size() == index + 1) {//????????????
                saveResult();
            } else {
                ++index;
                visionData = visionBean.getVisions().get(index);
                setImageWidth();
            }

        } else {
            ++errorCount;
            if (visionData.getErrorCount() == errorCount) {//??????
                saveResult();
            } else {
                setImageWidth();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void saveResult() {
        intervalUtil.stop();
        if (roundResult.getResult() == 0) {//????????????????????????
            roundResult.setResult((int) (visionData.getLogMAR_5() * 10));
            index = -1;
            txtHint.setVisibility(View.VISIBLE);
            toastSpeak("??????????????????????????????");
            txtHint.setText("???????????????\n???????????????");
            ivE.clearAnimation();
            ivE.setVisibility(View.GONE);
            showLed(roundResult);

        } else {//???????????????????????? ??????
            roundResult.setWeightResult((int) (visionData.getLogMAR_5() * 10));
            if (student == null) {
                index = -2;
                showLed(roundResult);
                txtHint.setVisibility(View.VISIBLE);
                txtHint.setText("????????????" + ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult()) + "\n" +
                        "????????????" + ResultDisplayUtils.getStrResultForDisplay(roundResult.getWeightResult()));
                ivE.clearAnimation();
                ivE.setVisibility(View.GONE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        index = -1;
                        roundResult = new RoundResult();
                        txtHint.setText("???????????????\n???????????????");
                        toastSpeak("??????????????????????????????");
                    }
                }, 3000);
            } else {
                RoundResult lastResult = DBManager.getInstance().queryLastScoreByStuCode(student.getStudentCode());
                if (lastResult != null) {
                    lastResult.setIsLastResult(0);
                    DBManager.getInstance().updateRoundResult(lastResult);
                }
                roundResult.setIsLastResult(1);
                DBManager.getInstance().insertRoundResult(roundResult);
                showLed(roundResult);
                //?????? ?????????????????????????????????
                uploadResult(roundResult, roundResult);
                EventBus.getDefault().post(new BaseEvent(roundResult, EventConfigs.VISION_TEST_SUCCEED));
                finish();
            }

        }

    }

    /**
     * ????????????
     *
     * @param roundResult ????????????
     * @param lastResult  ????????????
     */
    protected void uploadResult(RoundResult roundResult, RoundResult lastResult) {
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort(R.string.upload_result_hint);
            return;
        }

        ServerIml.uploadResult(UploadResultUtil.getUploadData(roundResult, lastResult));
//        ServerIml.uploadResult(this, UploadResultUtil.getUploadData(results));
    }

    private void setImageWidth() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) ivE.getLayoutParams();
        layoutParams.width = visionData.geteDP();
        layoutParams.height = visionData.geteDP();

        ivE.setLayoutParams(layoutParams);
        direction = random.nextInt(4) + 1;
        LogUtil.logDebugMessage("????????????" + direction);
        float toDegrees = 0;
        switch (direction) {
            case VisionBean.DIRECTION_RIGHT://???
                toDegrees = 0;
                break;

            case VisionBean.DIRECTION_BOTTOM://???
                toDegrees = 90;
                break;
            case VisionBean.DIRECTION_LEFT://???
                toDegrees = 180;
                break;
            case VisionBean.DIRECTION_TOP://???
                toDegrees = 270;
                break;
        }
        Animation animation = new RotateAnimation(0, toDegrees, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillAfter(true);//?????????true?????????????????????????????????
        ivE.startAnimation(animation);//????????????

        if (visionSetting.getTestType() == 0) {
            txtMar.setText(visionData.getLogMAR_5() + "");
        } else {
            txtMar.setText(visionData.getLogMAR_Decimals() + "");
        }
    }


    private void showLed(RoundResult roundResult) {

        ledManager.showString(SettingHelper.getSystemSetting().getHostId(), "????????????" + ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult()),
                0, 1, false, true);
        if (roundResult.getWeightResult() != 0) {
            ledManager.showString(SettingHelper.getSystemSetting().getHostId(), "????????????" + ResultDisplayUtils.getStrResultForDisplay(roundResult.getWeightResult()),
                    0, 2, false, true);
            toastSpeak("?????????" + ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult())
                    + "?????????" + ResultDisplayUtils.getStrResultForDisplay(roundResult.getWeightResult()));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        isStartThrand = false;
//        isStartCheck = false;
        intervalUtil.stop();
        byte[] data = new byte[16];
        ledManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 3, false, true);

    }
}
