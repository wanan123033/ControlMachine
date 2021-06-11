package com.feipulai.host.activity.height_weight;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.HeightWeightResult;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.activity.base.BaseCheckActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.UploadResultUtil;
import com.feipulai.host.netUtils.netapi.ServerIml;
import com.feipulai.host.utils.PrinterUtils;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.feipulai.host.utils.ResultUtils;
import com.feipulai.host.view.StuSearchEditText;
import com.orhanobut.logger.utils.LogUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by James on 2018/9/25 0025.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class HeightWeightCheckActivity
        extends BaseCheckActivity implements SerialDeviceManager.RS232ResiltListener {

    private static final int UPDATE_NEW_RESULT = 0x1;
    private static final int PREPARE_FOR_CHECK_IN = 0x2;
    private static final int CLEAR_DATA = 0x3;
    @BindView(R.id.txt_stu_code)
    TextView txtStuCode;
    @BindView(R.id.txt_stu_name)
    TextView txtStuName;
    @BindView(R.id.txt_stu_sex)
    TextView txtStuSex;
    @BindView(R.id.et_input_text)
    StuSearchEditText etInputText;
    @BindView(R.id.txt_test_result)
    TextView txtTestResult;
    @BindView(R.id.txt_height_result)
    TextView txtHeightResult;
    @BindView(R.id.txt_weight_result)
    TextView txtWeightResult;
    @BindView(R.id.lv_results)
    ListView lvResults;
    @BindView(R.id.img_portrait)
    ImageView imgPortrait;
    // 身高体重LED显示暂时不做
    private LEDManager mLEDManager = new LEDManager();

    private Student mStudent;
    private volatile boolean isTesting;// 是否正在测试中(检录成功了等待测试成绩)
    private volatile boolean isTestFinished;// 是否已经获得了成绩,测试已经完成,正在展示成绩信息,等待用户点击确认
    private Handler mHandler = new BaseActivity.MyHandler(this);
    private volatile RoundResult mHeightResult;
    private volatile RoundResult mWeightResult;
    private RoundResult mLastHeightResult;
    private RoundResult mLastWeightResult;
    private long startTime;
    private long endTime;

    @Override
    public int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_height_weight_check;
    }

    @Override
    protected void initData() {
        super.initData();

        init();
        prepareForCheckIn();
    }


    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = String.format(getString(R.string.host_name), TestConfigs.machineNameMap.get(machineCode), SettingHelper.getSystemSetting().getHostId());
        } else {
            title = String.format(getString(R.string.host_name), TestConfigs.machineNameMap.get(machineCode), SettingHelper.getSystemSetting().getHostId())
                    + "-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title);
    }

    private void init() {

        etInputText.setData(lvResults, this);
    }

    private void prepareForCheckIn() {
        isTesting = false;
        isTestFinished = false;
    }

    private void prepareForTest() {
        isTesting = true;
        isTestFinished = false;

        mLastHeightResult = DBManager.getInstance().queryLastScoreByStuCode(mStudent.getStudentCode(), HWConfigs
                .HEIGHT_ITEM);
        mLastWeightResult = DBManager.getInstance().queryLastScoreByStuCode(mStudent.getStudentCode(), HWConfigs.WEIGHT_ITEM);
//
//        String displayHeight = "";
//        String displayWeight = "";
//        if(mLastHeightResult != null){
//            displayHeight = ResultDisplayUtils.getStrResultForDisplay(mLastHeightResult.getResult(),HWConfigs.HEIGHT_ITEM);
//            displayWeight = ResultDisplayUtils.getStrResultForDisplay(mLastWeightResult.getResult(),HWConfigs.WEIGHT_ITEM);
//        }

        txtStuSex.setText(mStudent.getSex() == Student.MALE ? R.string.male : R.string.female);
        txtStuCode.setText(mStudent.getStudentCode());
        txtStuName.setText(mStudent.getStudentName());
        Glide.with(imgPortrait.getContext()).load(MyApplication.PATH_IMAGE + mStudent.getStudentCode() + ".jpg")
                .error(R.mipmap.icon_head_photo).placeholder(R.mipmap.icon_head_photo).into(imgPortrait);
        startTime = DateUtil.getCurrentTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SerialDeviceManager.getInstance().setRS232ResiltListener(this);
        //mLEDManager.resetLEDScreen(hostId,TestConfigs.machineNameMap.get(ItemDefault.CODE_HW));
    }

    @Override
    public void onCheckIn(Student student) {
        // 正在测试的话就不理会检录信息
        if (isTesting) {
            return;
        }
        mStudent = student;
        prepareForTest();
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), mStudent.getLEDStuName(), mLEDManager.getX(mStudent.getLEDStuName()), 0, true, true);
        toastSpeak(mStudent.getSpeakStuName() + "请准备");
        LogUtils.operation("检入到学生信息:" + mStudent.toString());
    }

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what) {

            case UPDATE_NEW_RESULT:
                String displayHeight = ResultDisplayUtils.getStrResultForDisplay(mHeightResult.getResult(), HWConfigs.HEIGHT_ITEM);
                String displayWeight = ResultDisplayUtils.getStrResultForDisplay(mWeightResult.getResult(), HWConfigs.WEIGHT_ITEM);
                LogUtils.operation("获取到学生成绩:mHeightResult=" + mHeightResult.toString());
                LogUtils.operation("获取到学生成绩:mWeightResult=" + mWeightResult.toString());
                txtHeightResult.setText(displayHeight);
                txtWeightResult.setText(displayWeight);
                txtTestResult.setText(displayHeight + "\n" + displayWeight);
                if (SettingHelper.getSystemSetting().isAutoBroadcast()) {
                    TtsManager.getInstance().speak(
                            String.format(getString(R.string.height_weight_speak), displayHeight, displayWeight));
                }
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "身高：" + displayHeight,
                        0, 1, false, true);
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "体重：" + displayWeight,
                        0, 2, false, true);

                mHandler.sendEmptyMessageDelayed(CLEAR_DATA, 4000);
                break;

            case PREPARE_FOR_CHECK_IN:
                prepareForCheckIn();
                break;
            case CLEAR_DATA:
                txtHeightResult.setText("");
                txtWeightResult.setText("");
                txtTestResult.setText("");
                txtStuSex.setText("");
                txtStuCode.setText("");
                txtStuName.setText("");
                isTesting = false;
                isTestFinished = false;
                imgPortrait.setImageResource(R.mipmap.icon_head_photo);
                break;
        }
    }

    @Override
    public void onRS232Result(Message msg) {
        // 只有测试状态下,才理会机器信息
        if (!isTesting || isTestFinished) {
            return;
        }
        switch (msg.what) {
            case SerialConfigs.HEIGHT_WEIGHT_RESULT:
                HeightWeightResult result = (HeightWeightResult) msg.obj;

                int heightMinValue = TestConfigs.sCurrentItem.getMinValue();
                int heightMaxValue = TestConfigs.sCurrentItem.getMaxValue();
                int weightMinValue = HWConfigs.WEIGHT_ITEM.getMinValue();
                int weightMaxValue = HWConfigs.WEIGHT_ITEM.getMaxValue();

                if (heightMinValue > 0 || heightMaxValue > 0) {
                    if (result.getHeight() * 10 < heightMinValue || result.getHeight() * 10 > heightMaxValue) {
                        toastSpeak("身高数值不在有效范围内，请重测");
                        return;
                    }
                }
                if (weightMinValue > 0 || weightMaxValue > 0) {
                    if (result.getWeight() * 1000 < weightMinValue || result.getWeight() * 1000 > weightMaxValue) {
                        toastSpeak("体重数值不在有效范围内，请重测");
                        return;
                    }
                }

                endTime = DateUtil.getCurrentTime();

                mHeightResult = ResultUtils.generateRoughResultWithRaw(mStudent, result, 1);
                mWeightResult = ResultUtils.generateRoughResultWithRaw(mStudent, result, 2);
                mWeightResult.setTestTime(startTime + "");
                mWeightResult.setPrintTime(endTime + "");
                mHeightResult.setWeightResult(mWeightResult.getResult());
                ResultUtils.saveResults(this, mHeightResult, mLastHeightResult);
                ResultUtils.saveResults(this, mWeightResult, mLastWeightResult);
                if (mLastHeightResult == null) {
//                    mLastHeightResult = mHeightResult;
                    mLastHeightResult = DBManager.getInstance().queryLastScoreByStuCode(mStudent.getStudentCode(), HWConfigs
                            .HEIGHT_ITEM);
                    mLastWeightResult = DBManager.getInstance().queryLastScoreByStuCode(mStudent.getStudentCode(), HWConfigs.WEIGHT_ITEM);
                }
                mHandler.sendEmptyMessage(UPDATE_NEW_RESULT);

                if (SettingHelper.getSystemSetting().isAutoPrint()) {
                    PrinterUtils.printResult(this, mStudent, mHeightResult, mWeightResult);
                }

                if (SettingHelper.getSystemSetting().isRtUpload()) {
//                    itemSubscriber.setDataUpLoad(mHeightResult, mHeightResult);
                    ServerIml.uploadResult(UploadResultUtil.getUploadData(mHeightResult, mLastHeightResult));

//                    itemSubscriber.setDataUpLoad(mWeightResult, mWeightResult);
                }

                //mLEDManager.showString(hostId,mStudent.getStudentName(),5,0,true,false);
                //mLEDManager.showString(hostId,"身高:" + displayHeight,2,2,false,false);
                //mLEDManager.showString(hostId,"体重:" + displayWeight,2,3,false,true);

                isTestFinished = true;
                //mHandler.sendEmptyMessage(PREPARE_FOR_CHECK_IN);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.operation("HeightWeightCheckActivity onPause");
        SerialDeviceManager.getInstance().setRS232ResiltListener(null);
        SerialDeviceManager.getInstance().close();
    }


    @OnClick(R.id.txt_skip)
    public void onViewClicked() {
        if (mStudent != null)
            LogUtils.operation("用户点击跳过:" + mStudent.toString());
        if (!isTestFinished)
            mHandler.sendEmptyMessage(CLEAR_DATA);
    }

    @OnClick(R.id.img_AFR)
    public void onImgAFRClicked() {
        LogUtils.operation("用户点击了人脸识别");
        showAFR();
    }


}