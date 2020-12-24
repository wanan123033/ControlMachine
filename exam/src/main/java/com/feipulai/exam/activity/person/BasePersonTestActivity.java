package com.feipulai.exam.activity.person;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.data.DataDisplayActivity;
import com.feipulai.exam.activity.data.DataRetrieveActivity;
import com.feipulai.exam.activity.person.adapter.BasePersonTestResultAdapter;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.bean.DataRetrieveBean;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.service.UploadService;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.view.StuSearchEditText;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 个人测试基类
 */
public abstract class BasePersonTestActivity extends BaseCheckActivity {
    private static final String TAG = "BasePersonTestActivity";
    @BindView(R.id.et_input_text)
    StuSearchEditText etInputText;
    @BindView(R.id.btn_scan)
    TextView btnScan;
    @BindView(R.id.img_AFR)
    ImageView imgAFR;
    @BindView(R.id.lv_results)
    ListView lvResults;
    @BindView(R.id.img_portrait)
    ImageView imgPortrait;
    @BindView(R.id.txt_stu_code)
    TextView txtStuCode;
    @BindView(R.id.txt_stu_name)
    TextView txtStuName;
    @BindView(R.id.txt_stu_sex)
    TextView txtStuSex;
    @BindView(R.id.rv_test_result)
    RecyclerView rvTestResult;
    @BindView(R.id.cb_device_state)
    public CheckBox cbDeviceState;
    @BindView(R.id.ll_state)
    public LinearLayout llState;
    @BindView(R.id.txt_test_result)
    TextView txtStuResult;
    @BindView(R.id.txt_start_test)
    TextView txtStartTest;
    @BindView(R.id.tv_base_height)
    TextView tvBaseHeight;
    @BindView(R.id.txt_stu_skip)
    TextView txtStuSkip;
    @BindView(R.id.txt_led_setting)
    public TextView txtLedSetting;
    @BindView(R.id.view_skip)
    LinearLayout viewSkip;
    @BindView(R.id.tv_device_pair)
    public TextView tvDevicePair;
    @BindView(R.id.rl)
    RelativeLayout rl;
    @BindView(R.id.tv_penalizeFoul)
    TextView tv_penalizeFoul;
    //    @BindView(R.id.txt_stu_fault)
//    TextView txtStuFault;
    //成绩
    private String[] result;
    private List<String> resultList = new ArrayList<>();
    private BasePersonTestResultAdapter adapter;
    /**
     * 当前设备
     */
    public BaseStuPair pair = new BaseStuPair();
    /**
     * 当前测试次数位
     */
    private int testNo = 1;
    private int roundNo = 1;
    private LEDManager mLEDManager;
    //清理学生信息
    private ClearHandler clearHandler = new ClearHandler(this);
    private LedHandler ledHandler = new LedHandler(this);
    private Intent serverIntent;
    private int testType = 0;//0自动 1手动
//    private boolean isFault;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_base_person_test;
    }

    @Override
    public int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.life("BasePersonTestActivity onCreate");
        init();
        PrinterManager.getInstance().init();
        if (SettingHelper.getSystemSetting().isRtUpload()) {
            serverIntent = new Intent(this, UploadService.class);
            startService(serverIntent);
        }
        tv_penalizeFoul.setVisibility(isShowPenalizeFoul());
    }

    protected abstract int isShowPenalizeFoul();


    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机";
        } else {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title)
//                .addRightText("人脸识别", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                gotoUVCFaceCamera();
//            }
//        })
                .addRightText("项目设置", new View.OnClickListener() {
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == 1) {
            String userNo = data.getStringExtra("UserName");
            Student student = DBManager.getInstance().queryStudentByCode(userNo);
            onCheckIn(student);
        }
    }

    private void init() {
        mLEDManager = new LEDManager();
        mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));

        PrinterManager.getInstance().init();

        GridLayoutManager layoutManager = new GridLayoutManager(this, setTestCount());
        rvTestResult.setLayoutManager(layoutManager);
        result = new String[setTestCount()];

        //创建适配器
        resultList.addAll(Arrays.asList(result));
        adapter = new BasePersonTestResultAdapter(resultList);
        //给RecyclerView设置适配器
        rvTestResult.setAdapter(adapter);

        etInputText.setData(lvResults, this);

        pair.setBaseDevice(new BaseDeviceState(BaseDeviceState.STATE_FREE));
        refreshDevice();

    }

    public void setTestType(int testType) {
        this.testType = testType;
        if (this.testType == 1) {
            txtStartTest.setVisibility(View.VISIBLE);
        }
    }

    public void setBegin(int isBegin) {
        txtStartTest.setText(isBegin == 0 ? "暂停测试" : "开始测试");
    }

    private void refreshDevice() {
        if (pair.getBaseDevice() != null) {
            if (pair.getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
                cbDeviceState.setChecked(true);
            } else {
                cbDeviceState.setChecked(false);
            }
        }
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.TOKEN_ERROR) {
            ToastUtils.showShort("自动上传失败，请先进行登录");
        }
    }

    public void setBaseHeightVisible(int visible) {
        tvBaseHeight.setVisibility(visible == 0 ? View.VISIBLE : View.GONE);
    }

    public void setBaseHeight(int height) {
        tvBaseHeight.setText("原始高度" + ResultDisplayUtils.getStrResultForDisplay(height * 10));
    }

    /**
     * 犯规按键是否可见
     */
//    public void setFaultVisible(boolean visible){
//        txtStuFault.setVisibility(visible? View.VISIBLE:View.GONE);
//    }
//    public void setFaultEnable(boolean enable) {
//        isFault = enable;
//    }

    /**
     * 发送测试指令 并且此时应将设备状态改变
     */
    public abstract void sendTestCommand(BaseStuPair baseStuPair);


    /**
     * 设置项目测试轮次次数
     */
    public abstract int setTestCount();

    /**
     * 跳转项目设置页面
     */
    public abstract void gotoItemSetting();

    /**
     * 跳过
     */
    public abstract void stuSkip();

    /**
     * 判断成绩是否满分跳过
     *
     * @param result
     * @return
     */
    public abstract boolean isResultFullReturn(int sex, int result);

    @Override
    public void onCheckIn(Student student) {
        if (student == null) {
            ToastUtils.showShort("查无此人");
            return;
        }
        if (pair.getStudent() != null || pair.getBaseDevice().getState() != BaseDeviceState.STATE_FREE) {
            toastSpeak("当前无设备可添加学生测试");
            return;
        }
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        List<RoundResult> roundResultList = DBManager.getInstance().queryFinallyRountScoreByExamTypeList(student.getStudentCode(), studentItem.getExamType());
        testNo = roundResultList == null || roundResultList.size() == 0 ? 1 : roundResultList.get(0).getTestNo();
        //保存成绩，并测试轮次大于测试轮次次数
        if (student != null)
            LogUtils.operation("检入到学生:" + student.toString());
        if (studentItem != null)
            LogUtils.operation("检入到学生StudentItem:" + studentItem.toString());
        if (roundResultList != null)
            LogUtils.operation("检入到学生成绩:" + roundResultList.size() + "----" + roundResultList.toString());
        if (roundResultList != null && roundResultList.size() >= setTestCount()) {
            //已测试，不重测
//            roundNo = roundResult.getRoundNo();
//            selectTestDialog(student);

            toastSpeak("该考生已测试完成");
            return;
        } else if (roundResultList != null) {
            for (RoundResult roundResult : roundResultList) {
                if (roundResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && isResultFullReturn(student.getSex(), roundResult.getResult())) {
                    toastSpeak("满分");
                    LogUtils.operation("该学生已满分跳过测试:" + roundResult.getStudentCode());
                    return;
                }
            }
        }
        //是否有成绩，没有成绩查底该项目是否有成绩，没有成绩测试次数为1，有成绩测试次数+1
        if (roundResultList.size() == 0) {
            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
            testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1;
        } else {
            testNo = roundResultList.get(0).getTestNo();
        }

        roundNo = roundResultList.size() + 1;
        LogUtils.operation("当前轮次 roundNo = " + roundNo);
        result = null;
        result = new String[setTestCount()];
        for (int i = 0; i < roundResultList.size(); i++) {
            if (i < setTestCount()) {
                if (roundResultList.get(i).getResultState() == RoundResult.RESULT_STATE_FOUL) {
                    result[i] = "X";
                } else {
                    result[i] = ResultDisplayUtils.getStrResultForDisplay(roundResultList.get(i).getResult());
                }

            }
        }
        resultList.clear();
        resultList.addAll(Arrays.asList(result));
        adapter.notifyDataSetChanged();
        addStudent(student);
    }

    @OnClick({R.id.txt_stu_skip, R.id.txt_start_test, R.id.txt_led_setting, R.id.img_AFR,R.id.tv_penalizeFoul})
//R.id.txt_stu_fault
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.txt_led_setting:
                LogUtils.operation("点击了外接屏幕");
                toLedSetting();
                break;
            case R.id.txt_stu_skip:
                LogUtils.operation("点击了跳过");
                if (pair.getStudent() != null) {
                    stuSkipDialog();
                }
                break;
            case R.id.txt_start_test:
                LogUtils.operation("点击了开始测试");
                if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN || pair.getBaseDevice().getState() == BaseDeviceState.STATE_FREE) {
                    pair.setTestTime(DateUtil.getCurrentTime() + "");
                    sendTestCommand(pair);
                }

                break;
//            case R.id.txt_stu_fault:
//                showPenalize();
//                break;
            case R.id.img_AFR:
//                gotoUVCFaceCamera();
                showAFR();
                break;
            case R.id.tv_penalizeFoul:
                if (pair.getStudent() != null) {
                    DataRetrieveBean bean = new DataRetrieveBean();
                    bean.setStudentCode(pair.getStudent().getStudentCode());
                    bean.setSex(pair.getStudent().getSex());
                    bean.setTestState(1);
                    bean.setStudentName(pair.getStudent().getStudentName());
                    Intent intent = new Intent(this, DataDisplayActivity.class);
                    intent.putExtra(DataDisplayActivity.ISSHOWPENALIZEFOUL, isShowPenalizeFoul());
                    intent.putExtra(DataRetrieveActivity.DATA_ITEM_CODE, getItemCode());
                    intent.putExtra(DataRetrieveActivity.DATA_EXTRA, bean);
                    startActivity(intent);
                }else {
                    toastSpeak("无考生成绩信息");
                }
                break;
        }
    }

    private String getItemCode() {
        return TestConfigs.sCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : TestConfigs.sCurrentItem.getItemCode();
    }

//    private void penalize(){
//        if (pair.getStudent() == null)
//            return;
//        //查询当前成绩
//        RoundResult roundResult = DBManager.getInstance().queryLastScoreByStuCode(pair.getStudent().getStudentCode());
//        //修改成绩
//        roundResult.setResultState(RoundResult.RESULT_STATE_FOUL);
//        //判断最好成绩
//        RoundResult bestResult = DBManager.getInstance().queryBestScore(pair.getStudent().getStudentCode(),testNo);
//        if (bestResult == roundResult){
//            roundResult.setIsLastResult(0);
//            DBManager.getInstance().updateRoundResult(roundResult);//保存
//            RoundResult best = DBManager.getInstance().queryOrderDecScore(pair.getStudent().getStudentCode(), testNo);
//            if (best != null && best.getIsLastResult()==0){
//                best.setIsLastResult(1);
//                DBManager.getInstance().updateRoundResult(best);//保存最好成绩
//            }
//        }else {
//            DBManager.getInstance().updateRoundResult(roundResult);//保存
//        }
//
//        //更新界面成绩
//        pair.setResultState(RoundResult.RESULT_STATE_FOUL);
//        updateResult(pair);
//        updateLastResultLed(roundResult);
//        adapter.notifyDataSetChanged();
//
//        //上传成绩
//        DBManager.getInstance().insertRoundResult(roundResult);
//        Logger.i("saveResult==>insertRoundResult->" + roundResult.toString());
//        List<RoundResult> roundResultList = new ArrayList<>();
//        roundResultList.add(roundResult);
//        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(pair.getStudent().getStudentCode());
//
//        UploadResults uploadResults = new UploadResults(studentItem.getScheduleNo(), TestConfigs.getCurrentItemCode(),
//                pair.getStudent().getStudentCode(), testNo + "", "", RoundResultBean.beanCope(roundResultList));
//        uploadResult(uploadResults);
//    }

    public void toLedSetting() {
        if (pair.getBaseDevice().getState() != BaseDeviceState.STATE_NOT_BEGAIN
                && pair.getBaseDevice().getState() != BaseDeviceState.STATE_FREE
                && pair.getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
            toastSpeak("测试中不可使用");
            return;
        }
        startActivity(new Intent(this, LEDSettingActivity.class));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.life("BasePersonTestActivity onDestroy");
        Intent serverIntent = new Intent(this, UploadService.class);
        stopService(serverIntent);
        PrinterManager.getInstance().close();
        if (TestConfigs.sCurrentItem != null) {
            mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
            String title = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()) + " " + SettingHelper.getSystemSetting().getHostId();
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), title, mLEDManager.getX(title), 0, true, false);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "菲普莱体育", 3, 3, false, true);
            mLEDManager = null;
        }
        ledHandler.removeCallbacksAndMessages(null);
        clearHandler.removeCallbacksAndMessages(null);
    }


    /**
     * 开启助跑  0不助跑 1助跑
     */
    public int runUp;
    public int baseHeight;

    private void addStudent(Student student) {
        baseHeight = 0;
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_MG && runUp == 0) {
            setBaseHeight(0);
        }
        if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN || pair.getBaseDevice().getState() == BaseDeviceState.STATE_FREE) {
//            roundNo = 1;
            pair.setResult(0);
            pair.setStudent(student);
            refreshTxtStu(student);
            txtStuResult.setText("");
            toastSpeak(String.format(getString(R.string.test_speak_hint), pair.getStudent().getSpeakStuName(), roundNo)
                    , String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), roundNo));
            if (testType == 0) {
                pair.setTestTime(DateUtil.getCurrentTime() + "");
                sendTestCommand(pair);
            }
            setShowLed(pair);

            Logger.i("addStudent:" + student.toString());
            LogUtils.operation("当前考生进行第" + testNo + "次的第" + roundNo + "轮测试");
        } else {
            toastSpeak("当前无设备可添加学生测试");
        }

    }

    /**
     * 加载学生信息
     */
    private void refreshTxtStu(@NonNull Student student) {
        if (student != null) {
            txtStuName.setText(student.getStudentName());
            txtStuSex.setText((student.getSex() == 0 ? "男" : "女"));
            txtStuCode.setText(student.getStudentCode());
//            if (student.getBitmapPortrait() != null) {
//                imgPortrait.setImageBitmap(student.getBitmapPortrait());
//            }
            Glide.with(imgPortrait.getContext()).load(MyApplication.PATH_IMAGE + student.getStudentCode() + ".jpg")
                    .error(R.mipmap.icon_head_photo).placeholder(R.mipmap.icon_head_photo).into(imgPortrait);
        } else {
            txtStuName.setText("");
            txtStuSex.setText("");
            txtStuCode.setText("");
            txtStuResult.setText("");
            imgPortrait.setImageResource(R.mipmap.icon_head_photo);
        }
    }


    private void selectTestDialog(final Student student) {
        new AlertDialog.Builder(this).setMessage(student.getStudentName() + "考生已测试过本项目，是否进行再次测试")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        testNo++;
                        roundNo = 1;
//                        //重测清除已有成绩
//                        DBManager.getInstance().deleteStuResult(student.getStudentCode());
                        addStudent(student);
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", null).show();
    }

    private void stuSkipDialog() {
        if (pair.getStudent() == null) {
            return;
        }
        new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("是否跳过" + pair.getStudent().getStudentName() + "考生测试")
                .setConfirmText("确认").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                Logger.i("stuSkip:" + pair.getStudent().toString());
                //测试结束学生清除 ，设备设置空闲状态
                roundNo = 1;
                clearHandler.sendEmptyMessageDelayed(0, 0);
                stuSkip();
                mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));

            }
        }).setCancelText("取消").setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();

            }
        }).show();

    }

    /**
     * 修改已添加设备状态，设备状态为STATE_END判定为测试结束，可进行成绩打印、播报、保存
     *
     * @param deviceState
     */
    public void updateDevice(@NonNull BaseDeviceState deviceState) {
        Logger.i("updateDevice==>" + deviceState.toString());
        if (pair.getBaseDevice() != null) {
            pair.getBaseDevice().setState(deviceState.getState());
            refreshDevice();
            //状态为测试已结束
            if (deviceState.getState() == BaseDeviceState.STATE_END) {
                if (pair.getStudent() != null) {
                    Logger.i("考生" + pair.getStudent().toString());
                }
                Logger.i("设备成绩信息STATE_END==>" + deviceState.toString());
                if (pair.getStudent() == null) {
                    return;
                }
                if (doResult()) return;
//                if (isFault && pair.getResultState() != RoundResult.RESULT_STATE_FOUL) {
//                    showPenalize();
//                } else {
//
//                }

            }
        }
        refreshDevice();

    }

    private boolean doResult() {
        //ResultDisplayUtils.getStrResultForDisplay(pair.getResult())
        result[roundNo - 1] = ((pair.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(pair.getResult()));
        resultList.clear();
        resultList.addAll(Arrays.asList(result));
        adapter.notifyDataSetChanged();
        pair.setTimeResult(result);
        //保存成绩
        saveResult(pair);
        printResult(pair);
        broadResult(pair);

        //当前的测试次数是否在项目设置的轮次中，是否满分跳过考生测试，满分由子类处理，基类只做界面展示
        if (roundNo < setTestCount()) {
            if (pair.getResultState() == RoundResult.RESULT_STATE_NORMAL && pair.isFullMark()) {
                //测试结束学生清除 ，设备设置空闲状态
                roundNo = 1;
                //4秒后清理学生信息
                clearHandler.sendEmptyMessageDelayed(0, 4000);
                return true;
            }
            roundNo++;
            txtStuResult.setText("");
            toastSpeak(String.format(getString(R.string.test_speak_hint), pair.getStudent().getSpeakStuName(), roundNo)
                    , String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), roundNo));
            Message msg = new Message();
            msg.obj = pair;
            ledHandler.sendMessageDelayed(msg, 2000);
            pair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
            if (testType != 1) {
                pair.setTestTime(DateUtil.getCurrentTime() + "");
                sendTestCommand(pair);
            }

        } else {
            setBaseHeight(0);
            //测试结束学生清除 ，设备设置空闲状态
            roundNo = 1;
            //4秒后清理学生信息
            clearHandler.sendEmptyMessageDelayed(0, 4000);

        }
        return false;
    }

    boolean clicked = false;

    /**
     * 展示判罚
     */
    private void showPenalize() {

        if (pair.getStudent() == null) {
            return;
        }
        clicked = false;
//        SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
//        alertDialog.setTitleText(getString(R.string.confirm_result));
//        alertDialog.setCancelable(false);
//        alertDialog.setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//            @Override
//            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                sweetAlertDialog.dismissWithAnimation();
//
//                if (!clicked) {
//                    doResult();
//                    clicked = true;
//                }
//
//            }
//        }).setCancelText(getString(R.string.foul)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
//            @Override
//            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                sweetAlertDialog.dismissWithAnimation();
//                if (!clicked) {
//                    pair.setResultState(RoundResult.RESULT_STATE_FOUL);
//                    updateResult(pair);
//                    doResult();
//                    clicked = true;
//                }
//
//            }
//        }).show();
    }

    /**
     * 保存测试成绩
     *
     * @param baseStuPair 当前设备
     */
    private void saveResult(@NonNull BaseStuPair baseStuPair) {
        LogUtils.operation("保存成绩:" + baseStuPair.toString());
        if (baseStuPair.getStudent() == null)
            return;
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(baseStuPair.getStudent().getStudentCode());
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(baseStuPair.getStudent().getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(baseStuPair.getResult());
        roundResult.setMachineResult(baseStuPair.getResult());
        roundResult.setResultState(baseStuPair.getResultState());
        roundResult.setTestTime(baseStuPair.getTestTime());
        //生成结束时间
        roundResult.setEndTime(System.currentTimeMillis() + "");
        roundResult.setRoundNo(roundNo);
        roundResult.setTestNo(testNo);
        roundResult.setExamType(studentItem.getExamType());
        roundResult.setScheduleNo(studentItem.getScheduleNo());
        roundResult.setUpdateState(0);
        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
        RoundResult bestResult = DBManager.getInstance().queryBestScore(baseStuPair.getStudent().getStudentCode(), testNo);
        if (bestResult != null) {
            // 原有最好成绩犯规 或者原有最好成绩没有犯规但是现在成绩更好
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && baseStuPair.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= baseStuPair.getResult()) {
                // 这个时候就要同时修改这两个成绩了
                roundResult.setIsLastResult(1);
                bestResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(bestResult);
                updateLastResultLed(roundResult);
            } else {
                if (bestResult.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                    roundResult.setIsLastResult(1);
                    bestResult.setIsLastResult(0);
                    DBManager.getInstance().updateRoundResult(bestResult);
                    updateLastResultLed(roundResult);
                } else {
                    roundResult.setIsLastResult(0);
                    updateLastResultLed(bestResult);
                }
            }
        } else {
            // 第一次测试
            roundResult.setIsLastResult(1);
            updateLastResultLed(roundResult);
        }

        DBManager.getInstance().insertRoundResult(roundResult);
        LogUtils.operation("保存成绩:" + roundResult.toString());
        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        UploadResults uploadResults = new UploadResults(studentItem.getScheduleNo(), TestConfigs.getCurrentItemCode(),
                baseStuPair.getStudent().getStudentCode(), testNo + "", null, RoundResultBean.beanCope(roundResultList));
        uploadResult(uploadResults);


    }

    /**
     * 成绩上传
     *
     * @param uploadResults 上传成绩
     */
    private void uploadResult(UploadResults uploadResults) {
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort("自动上传成绩需下载更新项目信息");
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable(UploadResults.BEAN_KEY, uploadResults);
        serverIntent.putExtras(bundle);
        startService(serverIntent);
    }

    /**
     * 更新学生成绩
     *
     * @param baseStu
     */
    public synchronized void updateResult(@NonNull BaseStuPair baseStu) {

        if (null != pair.getBaseDevice()) {
            pair.setResultState(baseStu.getResultState());
            pair.setResult(baseStu.getResult());
            pair.setFullMark(baseStu.isFullMark());
            txtStuResult.setText(((baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult())));
            refreshDevice();
            updateResultLed(((baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult())));
        }
    }


    /**
     * 播报结果
     */
    private void broadResult(@NonNull BaseStuPair baseStuPair) {
        if (baseStuPair.getStudent() == null) {
            return;
        }
        if (SettingHelper.getSystemSetting().isAutoBroadcast()) {
            if (baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) {
                TtsManager.getInstance().speak(baseStuPair.getStudent().getSpeakStuName() + "犯规");
            } else {

                TtsManager.getInstance().speak(String.format(getString(R.string.speak_result), baseStuPair.getStudent().getSpeakStuName(), ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult())));
            }


        }
    }

    /**
     * LED屏显示
     *
     * @param stuPair
     */
    private void setShowLed(BaseStuPair stuPair) {
        if (stuPair.getStudent() == null) {
            return;
        }
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), stuPair.getStudent().getLEDStuName() + "   第" + roundNo + "次", 0, 0, true, false);
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "当前：", 0, 1, false, true);
        RoundResult bestResult = DBManager.getInstance().queryBestScore(stuPair.getStudent().getStudentCode(), testNo);
        if (bestResult != null && bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
            byte[] data = new byte[16];
            String str = "最好：";
            try {
                byte[] strData = str.getBytes("GB2312");
                System.arraycopy(strData, 0, data, 0, strData.length);
                byte[] resultData = ResultDisplayUtils.getStrResultForDisplay(bestResult.getResult()).getBytes("GB2312");
                System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 2, false, true);
        } else {
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "最好：", 0, 2, false, true);

        }

    }

    private void updateResultLed(String result) {

        byte[] data = new byte[16];
        String str = "当前：";
        try {
            byte[] strData = str.getBytes("GB2312");
            System.arraycopy(strData, 0, data, 0, strData.length);
            byte[] resultData = result.getBytes("GB2312");
            System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 1, false, true);
    }

    private void updateLastResultLed(RoundResult roundResult) {
        if (roundResult != null && roundResult.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
            byte[] data = new byte[16];
            String str = "最好：";
            try {
                byte[] strData = str.getBytes("GB2312");
                System.arraycopy(strData, 0, data, 0, strData.length);
                byte[] resultData = ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult()).getBytes("GB2312");
                System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 2, false, true);
        } else {
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "最好：", 0, 2, false, true);

        }
    }


    private void printResult(@NonNull BaseStuPair baseStuPair) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        //是否已全部次数测试完成，非满分跳过与满分犯规
        if (roundNo < setTestCount() &&
                (!baseStuPair.isFullMark() || baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL)) {
            return;
        }
        Student student = baseStuPair.getStudent();
        if (student == null) {
            return;
        }
//        PrinterManager.getInstance().print("\n");
        PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "号机");
        PrinterManager.getInstance().print("考  号:" + student.getStudentCode());
        PrinterManager.getInstance().print("姓  名:" + student.getStudentName());
        for (int i = 0; i < baseStuPair.getTimeResult().length; i++) {
            if (!TextUtils.isEmpty(baseStuPair.getTimeResult()[i])) {
                PrinterManager.getInstance().print(String.format("第 %1$d 次：", i + 1) + baseStuPair.getTimeResult()[i]);
            } else {
                PrinterManager.getInstance().print(String.format("第%1$d次：", i + 1));
            }
        }
//        PrinterManager.getInstance().print("打印时间:" + TestConfigs.df.format(Calendar.getInstance().getTime()));
        PrinterManager.getInstance().print("\n");

    }

    private static class LedHandler extends Handler {

        private WeakReference<BasePersonTestActivity> mActivityWeakReference;

        public LedHandler(BasePersonTestActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BasePersonTestActivity activity = mActivityWeakReference.get();
            activity.setShowLed((BaseStuPair) msg.obj);

        }

    }

    /**
     * i清理学生信息
     */
    private static class ClearHandler extends Handler {

        private WeakReference<BasePersonTestActivity> mActivityWeakReference;

        public ClearHandler(BasePersonTestActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BasePersonTestActivity activity = mActivityWeakReference.get();
            Logger.i("ClearHandler:清理学生信息");
            if (activity != null) {
                activity.pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                activity.pair.setStudent(null);
                activity.refreshTxtStu(null);
                activity.result = null;
                activity.result = new String[activity.setTestCount()];
                activity.resultList.clear();
                activity.resultList.addAll(Arrays.asList(activity.result));
                activity.adapter.notifyDataSetChanged();
            }

        }
    }
}
