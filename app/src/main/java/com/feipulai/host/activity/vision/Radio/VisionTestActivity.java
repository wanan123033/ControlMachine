package com.feipulai.host.activity.vision.Radio;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
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
import com.feipulai.host.utils.ResultDisplayUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 视力
 * Created by zzs on  2020/9/15
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class VisionTestActivity extends BaseTitleActivity implements RadioManager.OnRadioArrivedListener {

    @BindView(R.id.iv_e)
    ImageView ivE;
    @BindView(R.id.txt_stu_name)
    TextView txtStuName;
    @BindView(R.id.txt_mar)
    TextView txtMar;
    @BindView(R.id.txt_hint)
    TextView txtHint;
    private VisionBean visionBean;
    private int index = 0;
    private ArrayList<VisionBean> visionBeans;
    private int direction;
    private Random random = new Random();

    private VisionSetting visionSetting;
    private VisionBean.VisionData visionData;//当前视标
    private int errorCount = 0;//视标错误数

    private Student student;

    private RoundResult roundResult = new RoundResult();
    private LEDManager ledManager = new LEDManager();

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
    protected void initData() {
        //获取项目设置
        visionSetting = SharedPrefsUtil.loadFormSource(this, VisionSetting.class);
        if (visionSetting == null)
            visionSetting = new VisionSetting();

        if (getIntent().getExtras() != null) {
            student = (Student) getIntent().getExtras().getSerializable("STUDENT");
            txtStuName.setText(student.getStudentName());
        }

        String JsonData = GetJsonDataUtil.getJson(this, "vision.json");//获取assets目录下的json文件数据
        Type type = new TypeToken<List<VisionBean>>() {
        }.getType();
        visionBeans = new Gson().fromJson(JsonData, type);
        visionBean = visionBeans.get(visionSetting.getDistance());

        visionData = visionBean.getVisions().get(index);
        setImageWidth();
        initResult();
    }

    private void initResult() {
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(student.getStudentCode());
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
                byte key = (byte) msg.obj;

                switch (key) {
                    case 0x38://上
                        checkKey(VisionBean.DIRECTION_TOP);
                        break;
                    case 0x36://右
                        checkKey(VisionBean.DIRECTION_RIGHT);
                        break;
                    case 0x32://下
                        checkKey(VisionBean.DIRECTION_BOTTOM);
                        break;
                    case 0x34://左
                        checkKey(VisionBean.DIRECTION_LEFT);
                        break;
                    case 0x42://返回
                        break;
                    case 0x35://确定
                        if (index == -1) {
                            index = 0;
                            errorCount = 0;
                            ivE.setVisibility(View.VISIBLE);
                            txtHint.setVisibility(View.GONE);
                            visionData = visionBean.getVisions().get(index);
                            setImageWidth();
                        }
                        break;
                }
            }
        });

    }

    private void checkKey(int keyDirection) {
        if (index == -1) {
            return;
        }
        Logger.d("checkKey===>keyDirection:" + keyDirection + "       direction:" + direction);
        if (keyDirection == direction) {
            if (visionBean.getVisions().size() == index + 1) {//结束测试
                saveResult();
            } else {
                ++index;
                visionData = visionBean.getVisions().get(index);
                setImageWidth();
            }

        } else {
            ++errorCount;
            if (visionData.getErrorCount() == errorCount) {//结束
                saveResult();
            } else {
                setImageWidth();
            }
        }
    }

    private void saveResult() {
        if (roundResult.getResult() == 0) {//是否在测试左视力
            roundResult.setResult((int) (visionData.getLogMAR_5() * 10));

            index = -1;
            txtHint.setVisibility(View.VISIBLE);
            ivE.clearAnimation();
            ivE.setVisibility(View.GONE);
            showLed(roundResult);
        } else {//是否在测试右视力 结束
            roundResult.setWeightResult((int) (visionData.getLogMAR_5() * 10));

            RoundResult lastResult = DBManager.getInstance().queryLastScoreByStuCode(student.getStudentCode());
            if (lastResult != null) {
                lastResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(lastResult);
            }
            roundResult.setIsLastResult(1);
            DBManager.getInstance().insertRoundResult(roundResult);
            showLed(roundResult);
            EventBus.getDefault().post(new BaseEvent(roundResult, EventConfigs.VISION_TEST_SUCCEED));
            finish();
        }
    }

    private void setImageWidth() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) ivE.getLayoutParams();
        layoutParams.width = visionData.geteDP();
        layoutParams.height = visionData.geteDP();

        ivE.setLayoutParams(layoutParams);
        direction = random.nextInt(4) + 1;
        LogUtil.logDebugMessage("方向为：" + direction);
        float toDegrees = 0;
        switch (direction) {
            case VisionBean.DIRECTION_RIGHT://右
                toDegrees = 0;
                break;

            case VisionBean.DIRECTION_BOTTOM://下
                toDegrees = 90;
                break;
            case VisionBean.DIRECTION_LEFT://左
                toDegrees = 180;
                break;
            case VisionBean.DIRECTION_TOP://上
                toDegrees = 270;
                break;
        }
        Animation animation = new RotateAnimation(0, toDegrees, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillAfter(true);//设置为true，动画转化结束后被应用
        ivE.startAnimation(animation);//開始动画

        if (visionSetting.getTestType() == 0) {
            txtMar.setText(visionData.getLogMAR_5() + "");
        } else {
            txtMar.setText(visionData.getLogMAR_Decimals() + "");
        }


    }

    private void showLed(RoundResult roundResult) {

        ledManager.showString(SettingHelper.getSystemSetting().getHostId(), "左视力：" + ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult()),
                0, 1, false, true);
        if (roundResult.getWeightResult() != 0) {
            ledManager.showString(SettingHelper.getSystemSetting().getHostId(), "右视力：" + ResultDisplayUtils.getStrResultForDisplay(roundResult.getWeightResult()),
                    0, 2, false, true);
        }

    }
}
