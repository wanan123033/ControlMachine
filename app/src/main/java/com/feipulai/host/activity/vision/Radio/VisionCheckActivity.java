package com.feipulai.host.activity.vision.Radio;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseCheckActivity;
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.setting.LEDSettingActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.BaseEvent;
import com.feipulai.host.config.EventConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.feipulai.host.view.StuSearchEditText;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zzs on  2020/9/24
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class VisionCheckActivity extends BaseCheckActivity implements RadioManager.OnRadioArrivedListener {


    @BindView(R.id.ll_stu_detail)
    LinearLayout llStuDetail;
    @BindView(R.id.et_input_text)
    StuSearchEditText etInputText;
    @BindView(R.id.txt_result)
    TextView txtResult;
    @BindView(R.id.txt_exit)
    TextView txtExit;

    @BindView(R.id.lv_results)
    ListView lvResults;
    public final int TARGET_FREQUENCY = SettingHelper.getSystemSetting().getUseChannel();
    private Student mStudent;
    private LEDManager ledManager = new LEDManager();
    private boolean isClear = false;//是否收到成功进行清理
    private static final int CLEAR_INFO = 100;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_vision_check;
    }

    @Override
    public int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }


    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title = String.format(getString(R.string.host_name), TestConfigs.machineNameMap.get(machineCode), SettingHelper.getSystemSetting().getHostId());
        return builder.setTitle(title).addRightText(R.string.item_setting_title, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtil.gotoActivity(VisionCheckActivity.this, VisionSettingActivity.class);
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtil.gotoActivity(VisionCheckActivity.this, VisionSettingActivity.class);
            }
        });
    }

    private BleTouchEvent bleTouchEvent = new BleTouchEvent(this);

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int key = bleTouchEvent.onTouch(event);
        if (key == BleTouchEvent.EVENT_CONFIRM && !isClear) {//确定
            if (mStudent != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("STUDENT", mStudent);
                IntentUtil.gotoActivity(this, VisionTestActivity.class, bundle);
            }
        } else if (key == BleTouchEvent.EVENT_CANCEL) {//返回

            myHandler.sendEmptyMessageDelayed(CLEAR_INFO, 100);
        }


        return super.onTouchEvent(event);
    }

    @Override
    protected void initData() {
        super.initData();
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(TARGET_FREQUENCY)));
        etInputText.setData(lvResults, this);
        showLed();
    }

    @Override
    public void onCheckIn(Student student) {
        mStudent = student;
        InteractUtils.showStuInfo(llStuDetail, student, null);
        txtResult.setText("请遮住右眼\n按确定开始");
        toastSpeak("请遮住右眼按确定开始");
        txtExit.setVisibility(View.VISIBLE);
        showLed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RadioManager.getInstance().setOnRadioArrived(this);
    }

    @Override
    public void onRadioArrived(Message msg) {
        if (msg.obj!=null){
            byte key = (byte) msg.obj;
            if (key == 0x35 && !isClear) {//确定
                if (mStudent != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("STUDENT", mStudent);
                    IntentUtil.gotoActivity(this, VisionTestActivity.class, bundle);
                }
            } else if (key == 0x42) {//返回

                myHandler.sendEmptyMessageDelayed(CLEAR_INFO, 100);
            }
        }

    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.VISION_TEST_SUCCEED) {
            isClear = true;
            RoundResult roundResult = (RoundResult) baseEvent.getData();
            StringBuffer sb = new StringBuffer("左视力：" + ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult()));
            sb.append("\n");
            sb.append("右视力：" + ResultDisplayUtils.getStrResultForDisplay(roundResult.getWeightResult()));
            txtResult.setText(sb);
            myHandler.sendEmptyMessageDelayed(CLEAR_INFO, 4000);

        }
    }

    private Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == CLEAR_INFO) {
                isClear = false;
                mStudent = null;
                InteractUtils.showStuInfo(llStuDetail, null, null);
                txtResult.setText("请检录");
                txtExit.setVisibility(View.GONE);
                myHandler.removeMessages(CLEAR_INFO);
            }
        }
    };


    @OnClick({R.id.img_AFR, R.id.txt_led_setting, R.id.tv_device_pair, R.id.txt_exit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_AFR:
                showAFR();
                break;
            case R.id.txt_led_setting:
                startActivity(new Intent(this, LEDSettingActivity.class));
                break;
            case R.id.tv_device_pair:
                break;
            case R.id.txt_exit:
                myHandler.sendEmptyMessageDelayed(CLEAR_INFO, 100);
                break;
        }
    }

    private void showLed() {
        if (mStudent == null) {
            ledManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));

        } else {
            ledManager.showString(SettingHelper.getSystemSetting().getHostId(), mStudent.getLEDStuName(),
                    0, 0, true, true);
            ledManager.showString(SettingHelper.getSystemSetting().getHostId(), "左视力：",
                    0, 1, false, true);
            ledManager.showString(SettingHelper.getSystemSetting().getHostId(), "右视力：",
                    0, 2, false, true);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Logger.d("onKeyDown===>"+keyCode);
        return super.onKeyDown(keyCode, event);
    }
}
