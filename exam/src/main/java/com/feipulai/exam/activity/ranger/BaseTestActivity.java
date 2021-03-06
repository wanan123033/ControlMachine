package com.feipulai.exam.activity.ranger;

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
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.spputils.beans.RangerResult;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.AgainTestDialog;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.base.ResitDialog;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.person.PenalizeDialog;
import com.feipulai.exam.activity.person.adapter.BasePersonTestResultAdapter;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
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
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public abstract class BaseTestActivity extends BaseCheckActivity {
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
    @BindView(R.id.txt_commit)
    TextView txtCommit;
//    @BindView(R.id.txt_fg)
//    TextView txtFg;
//    @BindView(R.id.txt_pf)
//    TextView txtPf;
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
    //    @BindView(R.id.txt_stu_fault)
//    TextView txtStuFault;
    //??????
    protected String[] result;
    protected List<String> resultList = new ArrayList<>();
    protected BasePersonTestResultAdapter adapter;
    /**
     * ????????????
     */
    public BaseStuPair pair = new BaseStuPair();
    /**
     * ?????????????????????
     */
    protected int roundNo = 1;
    private LEDManager mLEDManager;
    //??????????????????
    private ClearHandler clearHandler = new ClearHandler(this);
    private LedHandler ledHandler = new LedHandler(this);
    private Intent serverIntent;
    private int testType = 1;//0?????? 1??????
    private boolean isFault;
    private PenalizeDialog penalizeDialog;
    private String[] lastResult;
    private Student lastStudent;
    @BindView(R.id.tv_inBack)
    TextView tvInBack;
    @BindView(R.id.tv_abandon)
    TextView tvAbandon;
    @BindView(R.id.tv_normal)
    TextView tvNormal;
    private int testNo;

    @Override
    public void setRoundNo(Student student, int roundNo) {
        SystemSetting systemSetting = SettingHelper.getSystemSetting();
        if (systemSetting.isResit())
            this.roundNo = roundNo;
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_base_test;
    }

    @Override
    public int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.life("BaseTestActivity onCreate");
        PrinterManager.getInstance().init();
        if (SettingHelper.getSystemSetting().isRtUpload()) {
            serverIntent = new Intent(this, UploadService.class);
            startService(serverIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "??????";
        } else {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "??????-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title)
//                .addRightText("????????????", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                gotoUVCFaceCamera();
//            }
//        })
                .addRightText("????????????", new View.OnClickListener() {
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
        lastResult = new String[setTestCount()];
        //???????????????
        resultList.clear();
        resultList.addAll(Arrays.asList(result));
        adapter = new BasePersonTestResultAdapter(resultList);
        //???RecyclerView???????????????
        rvTestResult.setAdapter(adapter);

        etInputText.setData(lvResults, this);

        pair.setBaseDevice(new BaseDeviceState(BaseDeviceState.STATE_FREE));
        refreshDevice();
        penalizeDialog = new PenalizeDialog(this, setTestCount());
        setBackGround(false);
    }

    public void setTestType(int testType) {
        this.testType = testType;
        if (this.testType == 1) {
            txtStartTest.setVisibility(View.VISIBLE);
        }
    }

    public void setBegin(int isBegin) {
        txtStartTest.setText(isBegin == 0 ? "????????????" : "????????????");
    }

    public void refreshDevice() {
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
            ToastUtils.showShort("???????????????????????????????????????");
        }
    }

    public void setBaseHeightVisible(int visible) {
        tvBaseHeight.setVisibility(visible == 0 ? View.VISIBLE : View.GONE);
    }

    public void setBaseHeight(int height) {
        tvBaseHeight.setText("????????????" + ResultDisplayUtils.getStrResultForDisplay(height * 10));
    }

    /**
     * ????????????????????????
     */
//    public void setFaultVisible(boolean visible){
//        txtStuFault.setVisibility(visible? View.VISIBLE:View.GONE);
//    }
    public void setFaultEnable(boolean enable) {
        isFault = enable;
    }

    /**
     * ?????????????????? ????????????????????????????????????
     */
    public abstract void sendTestCommand(BaseStuPair baseStuPair);


    /**
     * ??????????????????????????????
     */
    public abstract int setTestCount();

    /**
     * ????????????????????????
     */
    public abstract void gotoItemSetting();

    /**
     * ??????
     */
    public abstract void stuSkip();

    /**
     * ??????????????????????????????
     *
     * @param result
     * @return
     */
    public abstract boolean isResultFullReturn(int sex, int result);

    @Override
    public void onCheckIn(Student student) {
        if (student == null) {
            ToastUtils.showShort("????????????");
            return;
        }
        final StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        final List<RoundResult> roundResultList = DBManager.getInstance().queryFinallyRountScoreByExamTypeList(student.getStudentCode(), studentItem.getExamType());
        if (student != null)
            LogUtils.operation("?????????????????????:"+student.toString());
        if (studentItem != null)
            LogUtils.operation("?????????????????????StudentItem:"+studentItem.toString());
        if (roundResultList != null)
            LogUtils.operation("???????????????????????????:"+roundResultList.size()+"----"+roundResultList.toString());

        testNo = getRangerSetting().getTestNo();
        //??????????????????????????????????????????????????????
        if (roundResultList != null && roundResultList.size() >= setTestCount()) {
            if (roundResultList != null && roundResultList.size() >= TestConfigs.getMaxTestCount(this)) {
                SystemSetting setting = SettingHelper.getSystemSetting();
                if (setting.isAgainTest() && setting.isResit()){
                    final Student finalStudent = student;
                    new SweetAlertDialog(this).setContentText("????????????????????????????")
                            .setCancelText("??????")
                            .setConfirmText("??????")
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    AgainTestDialog dialog = new AgainTestDialog();
                                    dialog.setArguments(finalStudent,roundResultList,studentItem);
                                    dialog.setOnIndividualCheckInListener(BaseTestActivity.this);
                                    dialog.show(getSupportFragmentManager(),"AgainTestDialog");
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    ResitDialog dialog = new ResitDialog();
                                    dialog.setArguments(finalStudent,roundResultList,studentItem);
                                    dialog.setOnIndividualCheckInListener(BaseTestActivity.this);
                                    dialog.show(getSupportFragmentManager(),"ResitDialog");
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            }).show();
                }
                if (setting.isAgainTest()){
                    AgainTestDialog dialog = new AgainTestDialog();
                    dialog.setArguments(student,roundResultList,studentItem);
                    dialog.setOnIndividualCheckInListener(this);
                    dialog.show(getSupportFragmentManager(),"AgainTestDialog");
                    return;
                }
                if (setting.isResit()){
                    ResitDialog dialog = new ResitDialog();
                    dialog.setArguments(student,roundResultList,studentItem);
                    dialog.setOnIndividualCheckInListener(this);
                    dialog.show(getSupportFragmentManager(),"ResitDialog");
                    return;
                }else {
                    InteractUtils.toastSpeak(this, "??????????????????");
                }

            }
            return;
        }
        //??????????????????????????????????????????????????????????????????????????????????????????1????????????????????????+1
        if (roundResultList.size() == 0) {
            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
            testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1;
        } else {
            testNo = roundResultList.get(0).getTestNo();
        }
        if (pair.getBaseDevice().getState() != BaseDeviceState.STATE_NOT_BEGAIN && pair.getBaseDevice().getState() != BaseDeviceState.STATE_FREE) {
            toastSpeak("????????????????????????????????????");
            return;
        }
        testNo = setTestCount();
        roundNo = roundResultList.size() + 1;
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
        adapter.setNewData(resultList);
        addStudent(student);
        setBackGround(true);
    }

    @OnClick({R.id.txt_stu_skip, R.id.txt_start_test, R.id.txt_led_setting, R.id.img_AFR,R.id.txt_commit,
            R.id.tv_foul, R.id.tv_inBack, R.id.tv_abandon, R.id.tv_normal})//R.id.txt_pf,R.id.txt_fg,
//R.id.txt_stu_fault
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.txt_led_setting:

                toLedSetting();
                break;
            case R.id.txt_stu_skip:
                if (pair.getStudent() != null) {
                    stuSkipDialog();
                }
                break;
            case R.id.txt_start_test:
                Log.e("TAG----",pair.getBaseDevice().getState()+"");
                if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN || pair.getBaseDevice().getState() == BaseDeviceState.STATE_FREE) {
                    sendTestCommand(pair);
                    pair.setTestTime(System.currentTimeMillis()+"");
                }
                break;
//            case R.id.txt_pf:
//                showPenalize();
//                break;
            case R.id.img_AFR:
//                gotoUVCFaceCamera();
                showAFR();
                break;
//            case R.id.txt_fg:
//                pair.setResultState(2);
//                updateResult(pair);
//                break;
            case R.id.txt_commit:
                confrim();
                break;
            case R.id.tv_foul:
                penalizeDialog.showDialog(0);
                if (pair.getStudent() == null){
                    penalizeDialog.setData(0, pair.getStudent(), result, lastStudent, lastResult);
                }else {
                    penalizeDialog.setData(1, pair.getStudent(), result, lastStudent, lastResult);
                }

                break;
            case R.id.tv_inBack:
                penalizeDialog.showDialog(1);
                if (pair.getStudent() == null){
                    penalizeDialog.setData(0, pair.getStudent(), result, lastStudent, lastResult);
                }else {
                    penalizeDialog.setData(1, pair.getStudent(), result, lastStudent, lastResult);
                }
                break;
            case R.id.tv_abandon:
                penalizeDialog.showDialog(2);
                if (pair.getStudent() == null){
                    penalizeDialog.setData(0, pair.getStudent(), result, lastStudent, lastResult);
                }else {
                    penalizeDialog.setData(1, pair.getStudent(), result, lastStudent, lastResult);
                }
                break;
            case R.id.tv_normal:
                penalizeDialog.showDialog(3);
                if (null == pair.getStudent()){
                    penalizeDialog.setData(0, pair.getStudent(), result, lastStudent, lastResult);
                }else {
                    penalizeDialog.setData(1, pair.getStudent(), result, lastStudent, lastResult);
                }
                break;
        }

    }

    protected abstract void confrim();


//    private void penalize(){
//        if (pair.getStudent() == null)
//            return;
//        //??????????????????
//        RoundResult roundResult = DBManager.getInstance().queryLastScoreByStuCode(pair.getStudent().getStudentCode());
//        //????????????
//        roundResult.setResultState(RoundResult.RESULT_STATE_FOUL);
//        //??????????????????
//        RoundResult bestResult = DBManager.getInstance().queryBestScore(pair.getStudent().getStudentCode(),testNo);
//        if (bestResult == roundResult){
//            roundResult.setIsLastResult(0);
//            DBManager.getInstance().updateRoundResult(roundResult);//??????
//            RoundResult best = DBManager.getInstance().queryOrderDecScore(pair.getStudent().getStudentCode(), testNo);
//            if (best != null && best.getIsLastResult()==0){
//                best.setIsLastResult(1);
//                DBManager.getInstance().updateRoundResult(best);//??????????????????
//            }
//        }else {
//            DBManager.getInstance().updateRoundResult(roundResult);//??????
//        }
//
//        //??????????????????
//        pair.setResultState(RoundResult.RESULT_STATE_FOUL);
//        updateResult(pair);
//        updateLastResultLed(roundResult);
//        adapter.notifyDataSetChanged();
//
//        //????????????
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
            toastSpeak("?????????????????????");
            return;
        }
        startActivity(new Intent(this, LEDSettingActivity.class));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.life("BaseTestActivity onDestroy");
        Intent serverIntent = new Intent(this, UploadService.class);
        stopService(serverIntent);
        PrinterManager.getInstance().close();
        if (TestConfigs.sCurrentItem != null) {
            mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
            String title = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()) + " " + SettingHelper.getSystemSetting().getHostId();
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), title, mLEDManager.getX(title), 0, true, false);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "???????????????", 3, 3, false, true);
            mLEDManager = null;
        }
    }


    /**
     * ????????????  0????????? 1??????
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
                sendTestCommand(pair);
            }
//            setShowLed(pair);

            LogUtils.operation("????????????????????????:" + student.toString());
            LogUtils.operation("??????????????????:"+pair.getStudent().getStudentCode()+"?????????" + testNo + "?????????" + roundNo + "?????????");
        } else {
            toastSpeak("????????????????????????????????????");
        }

    }

    /**
     * ??????????????????
     */
    public void refreshTxtStu(@NonNull Student student) {
        if (student != null) {
            txtStuName.setText(student.getStudentName());
            txtStuSex.setText((student.getSex() == 0 ? "???" : "???"));
            txtStuCode.setText(student.getStudentCode());
//            if (student.getBitmapPortrait() != null) {
//                imgPortrait.setImageBitmap(student.getBitmapPortrait());
//            }
            Glide.with(imgPortrait.getContext()).load(MyApplication.PATH_IMAGE + student.getStudentCode() + ".jpg")
                    .error(R.mipmap.icon_head_photo).placeholder(R.mipmap.icon_head_photo).into(imgPortrait);
            resultList.clear();
            resultList.addAll(Arrays.asList(result));
            adapter.notifyDataSetChanged();
        } else {
            txtStuName.setText("");
            txtStuSex.setText("");
            txtStuCode.setText("");
            txtStuResult.setText("");

            Log.e("TAG---","????????????");
            result = new String[setTestCount()];
            adapter.setNewData(Arrays.asList(result));
            Log.e("TAG----","adapter.getCount="+adapter.getItemCount());
            imgPortrait.setImageResource(R.mipmap.icon_head_photo);
        }
        updateInitBtnState();

    }


    private void selectTestDialog(final Student student) {
        new AlertDialog.Builder(this).setMessage(student.getStudentName() + "??????????????????????????????????????????????????????")
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        testNo++;
                        roundNo = 1;
//                        //????????????????????????
//                        DBManager.getInstance().deleteStuResult(student.getStudentCode());
                        addStudent(student);
                        dialog.dismiss();
                    }
                }).setNegativeButton("??????", null).show();
    }

    private void stuSkipDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("????????????" + pair.getStudent().getStudentName() + "????????????")
                .setConfirmText("??????").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                Logger.i("stuSkip:" + pair.getStudent().toString());
                //???????????????????????? ???????????????????????????
                roundNo = 1;
                clearHandler.sendEmptyMessageDelayed(0, 0);
                stuSkip();
                mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));

            }
        }).setCancelText("??????").setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();

            }
        }).show();

    }

    /**
     * ?????????????????????????????????????????????STATE_END???????????????????????????????????????????????????????????????
     *
     * @param deviceState
     */
    public void updateDevice(@NonNull BaseDeviceState deviceState) {
        Logger.i("updateDevice==>" + deviceState.toString());
        if (pair.getBaseDevice() != null) {
            pair.getBaseDevice().setState(deviceState.getState());
            //????????????????????????
            if (deviceState.getState() == BaseDeviceState.STATE_END) {
                if (pair.getStudent() != null) {
                    Logger.i("??????" + pair.getStudent().toString());
                }
                Logger.i("??????????????????STATE_END==>" + deviceState.toString());
                if (isFault && pair.getResultState() != RoundResult.RESULT_STATE_FOUL) {
                    showPenalize();
                } else {
                    if (doResult()) return;
                }

            }
        }
        refreshDevice();

    }

    public boolean doResult() {
        //ResultDisplayUtils.getStrResultForDisplay(pair.getResult())
        result[roundNo - 1] = ((pair.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(pair.getResult()));
        resultList.clear();
        resultList.addAll(Arrays.asList(result));
        adapter.notifyDataSetChanged();
        pair.setTimeResult(result);
        lastResult = pair.getTimeResult();
        lastStudent = pair.getStudent();
        //????????????
        saveResult(pair);
        printResult(pair);
        broadResult(pair);

        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (roundNo < setTestCount()) {
            if (pair.getResultState() == RoundResult.RESULT_STATE_NORMAL && pair.isFullMark()) {
                //???????????????????????? ???????????????????????????
                roundNo = 1;
                //4????????????????????????
                clearHandler.sendEmptyMessageDelayed(0, 4000);
                return true;
            }
            roundNo++;
            txtStuResult.setText("");
            toastSpeak(String.format(getString(R.string.test_speak_hint), pair.getStudent().getSpeakStuName(), roundNo)
                    , String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), roundNo));
            LogUtils.operation("??????????????????:"+pair.getStudent().getStudentCode()+"?????????" + testNo + "?????????" + roundNo + "?????????");
            Message msg = new Message();
            msg.obj = pair;
            ledHandler.sendMessageDelayed(msg, 2000);
            pair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
            if (testType != 1) {
                sendTestCommand(pair);
            }

        } else {
            setBaseHeight(0);
            //???????????????????????? ???????????????????????????
            roundNo = 1;
            //4????????????????????????
            clearHandler.sendEmptyMessageDelayed(0, 4000);

        }
        return false;
    }

    /**
     * ????????????
     */
    public abstract void showPenalize();

    /**
     * ??????????????????
     *
     * @param baseStuPair ????????????
     */
    public void saveResult(@NonNull BaseStuPair baseStuPair) {
        LogUtils.operation("??????????????????:" + baseStuPair.toString());
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
        roundResult.setEndTime(baseStuPair.getEndTime());
        roundResult.setRoundNo(roundNo);
        roundResult.setTestNo(setTestCount());
        roundResult.setExamType(studentItem.getExamType());
        roundResult.setScheduleNo(studentItem.getScheduleNo());
        roundResult.setUpdateState(0);
        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
        RoundResult bestResult = DBManager.getInstance().queryBestScore(baseStuPair.getStudent().getStudentCode(), setTestCount());
        if (bestResult != null) {
            // ???????????????????????? ????????????????????????????????????????????????????????????
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && baseStuPair.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= baseStuPair.getResult()) {
                // ????????????????????????????????????????????????
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
            // ???????????????
            roundResult.setIsLastResult(1);
            updateLastResultLed(roundResult);
        }
        //??????????????????
        roundResult.setEndTime(System.currentTimeMillis()+"");
        DBManager.getInstance().insertRoundResult(roundResult);
        LogUtils.operation("??????????????????:" + roundResult.toString());
        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        UploadResults uploadResults = new UploadResults(studentItem.getScheduleNo(), TestConfigs.getCurrentItemCode(),
                baseStuPair.getStudent().getStudentCode(), testNo + "", null, RoundResultBean.beanCope(roundResultList));


        uploadResult(uploadResults);

        ToastUtils.showLong("????????????");
    }

    /**
     * ????????????
     *
     * @param uploadResults ????????????
     */
    private void uploadResult(UploadResults uploadResults) {
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort("?????????????????????????????????????????????");
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable(UploadResults.BEAN_KEY, uploadResults);
        serverIntent.putExtras(bundle);
        startService(serverIntent);
    }

    /**
     * ??????????????????
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
            updateDevice(baseStu.getBaseDevice());
//            updateResultLed(((baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult())));
        }
    }


    /**
     * ????????????
     */
    private void broadResult(@NonNull BaseStuPair baseStuPair) {
        if (SettingHelper.getSystemSetting().isAutoBroadcast()) {
            if (baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) {
                TtsManager.getInstance().speak(baseStuPair.getStudent().getSpeakStuName() + "??????");
            } else {

                TtsManager.getInstance().speak(String.format(getString(R.string.speak_result), baseStuPair.getStudent().getSpeakStuName(), ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult())));
            }


        }
    }

    /**
     * LED?????????
     *
     * @param stuPair
     */
    private void setShowLed(BaseStuPair stuPair) {

        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), stuPair.getStudent().getLEDStuName() + "   ???" + roundNo + "???", 0, 0, true, false);
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "?????????", 0, 1, false, true);
        RoundResult bestResult = DBManager.getInstance().queryBestScore(stuPair.getStudent().getStudentCode(), testNo);
        if (bestResult != null && bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
            byte[] data = new byte[16];
            String str = "?????????";
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
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "?????????", 0, 2, false, true);

        }

    }

    private void updateResultLed(String result) {

        byte[] data = new byte[16];
        String str = "?????????";
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
            String str = "?????????";
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
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "?????????", 0, 2, false, true);

        }
    }


    private void printResult(@NonNull BaseStuPair baseStuPair) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        //???????????????????????????????????????????????????
        if (roundNo < setTestCount() && !baseStuPair.isFullMark()) {
            return;
        }
        Student student = baseStuPair.getStudent();
//        PrinterManager.getInstance().print("\n");
        PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "??????");
        PrinterManager.getInstance().print("???  ???:" + student.getStudentCode());
        PrinterManager.getInstance().print("???  ???:" + student.getStudentName());
        for (int i = 0; i < baseStuPair.getTimeResult().length; i++) {
            if (!TextUtils.isEmpty(baseStuPair.getTimeResult()[i])) {
                PrinterManager.getInstance().print(String.format("??? %1$d ??????", i + 1) + baseStuPair.getTimeResult()[i]);
            } else {
                PrinterManager.getInstance().print(String.format("???%1$d??????", i + 1));
            }
        }
        PrinterManager.getInstance().print("????????????:" + TestConfigs.df.format(Calendar.getInstance().getTime()));
        PrinterManager.getInstance().print("\n");

    }


    private static class LedHandler extends Handler {

        private WeakReference<BaseTestActivity> mActivityWeakReference;

        public LedHandler(BaseTestActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BaseTestActivity activity = mActivityWeakReference.get();
//            activity.setShowLed((BaseStuPair) msg.obj);

        }

    }

    /**
     * i??????????????????
     */
    private static class ClearHandler extends Handler {

        private WeakReference<BaseTestActivity> mActivityWeakReference;

        public ClearHandler(BaseTestActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BaseTestActivity activity = mActivityWeakReference.get();
            Logger.i("ClearHandler:??????????????????");
            if (activity != null) {
                activity.pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                activity.pair.setStudent(null);
                activity.refreshTxtStu(null);
                activity.result = null;
                activity.result = new String[activity.setTestCount()];
                activity.resultList.clear();
                activity.resultList.addAll(Arrays.asList(activity.result));
                activity.adapter.notifyDataSetChanged();
                activity.setBackGround(false);
            }

        }
    }
    protected void updateInitBtnState(){
        txtStartTest.setVisibility(View.VISIBLE);
        txtStuSkip.setVisibility(View.VISIBLE);
        txtCommit.setVisibility(View.GONE);
//        txtPf.setVisibility(View.GONE);
//        txtFg.setVisibility(View.GONE);
    }
    protected void updateTestBtnState(){
        txtStartTest.setVisibility(View.GONE);
        txtStuSkip.setVisibility(View.GONE);
        txtCommit.setVisibility(View.VISIBLE);
//        txtPf.setVisibility(View.VISIBLE);
//        txtFg.setVisibility(View.VISIBLE);
    }

    public void setScore(RangerResult result){
        this.result[roundNo - 1] = result+"";
        pair.setResultState(RoundResult.RESULT_STATE_NORMAL);
        calculation(result,getRangerSetting());
    }

    private void calculation(RangerResult result, RangerSetting rangerSetting) {
        int itemType = rangerSetting.getItemType();
        if (itemType == 2 || itemType == 3 || itemType == 4){ //???????????????
            double level1 = rangerSetting.getLevel1();
            double level2 = rangerSetting.getLevel2();
            Point jidian1 = RangerUtil.getPoint(level1,rangerSetting.getQd1_hor());
            Point jidian2 = RangerUtil.getPoint(level2,rangerSetting.getQd2_hor());
            double level = RangerUtil.level(result.getLevel_d(),result.getLevel_g(),result.getLevel_m());
            Point p = RangerUtil.getPoint(level,result.getResult());
            double length = RangerUtil.length(jidian1, jidian2, p);
            pair.setResult((int) length);
        }else if (itemType == 0 || itemType == 1){   //???????????????
            pair.setResult(result.getResult());
        }else if (itemType == 5 || itemType == 6 || itemType == 7 || itemType == 8){  //???????????????
            double dd = RangerUtil.level(result.getLevel_d(),result.getLevel_g(),result.getLevel_m());
            double inclination = RangerUtil.inclination(rangerSetting.getLevel(), dd);
            double length = RangerUtil.cosine(inclination, rangerSetting.getQd_hor(), result.getResult());
            pair.setResult((int) (length - rangerSetting.getRadius()));

        }
        updateResult(pair);
    }

    public abstract RangerSetting getRangerSetting();
    private void setBackGround(boolean enable){
        tvInBack.setEnabled(enable);
        tvAbandon.setEnabled(enable);
        tvInBack.setBackground(enable? getResources().getDrawable(R.drawable.btn_blue):
                getResources().getDrawable(R.drawable.btn_gray));
        tvAbandon.setBackground(enable? getResources().getDrawable(R.drawable.btn_blue):
                getResources().getDrawable(R.drawable.btn_gray));
    }
}
