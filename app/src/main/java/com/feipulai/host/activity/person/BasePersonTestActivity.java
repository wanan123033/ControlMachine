package com.feipulai.host.activity.person;

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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseCheckActivity;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.person.adapter.BasePersonTestResultAdapter;
import com.feipulai.host.activity.setting.LEDSettingActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.UploadResultUtil;
import com.feipulai.host.netUtils.netapi.ServerIml;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.feipulai.host.view.StuSearchEditText;
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

/**
 * ??????????????????
 */
public abstract class BasePersonTestActivity extends BaseCheckActivity {
    @BindView(R.id.et_input_text)
    StuSearchEditText etInputText;
    @BindView(R.id.btn_scan)
    TextView btnScan;
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
    public TextView txtStartTest;
    @BindView(R.id.tv_base_height)
    TextView tvBaseHeight;
    @BindView(R.id.txt_stu_skip)
    public TextView txtStuSkip;
    @BindView(R.id.txt_led_setting)
    public TextView txtLedSetting;
    @BindView(R.id.view_skip)
    LinearLayout viewSkip;
    @BindView(R.id.tv_device_pair)
    public TextView tvDevicePair;
    @BindView(R.id.tv_start_test)
    public TextView tvStartTest;
    @BindView(R.id.tv_exit_test)
    TextView tvExitTest;
    @BindView(R.id.tv_stop_test)
    TextView tvStopTest;
    @BindView(R.id.tv_time_count)
    TextView tvTimeCount;
    @BindView(R.id.tv_abandon_test)
    TextView tvAbandonTest;
    //??????
    private String[] result;
    private List<String> resultList = new ArrayList<>();
    private BasePersonTestResultAdapter adapter;
    /**
     * ????????????
     */
    public BaseStuPair pair = new BaseStuPair();
    /**
     * ?????????????????????
     */
    private int testNo = 1;
    private int roundNo = 1;
    protected LEDManager mLEDManager;
    //??????????????????
    public ClearHandler clearHandler = new ClearHandler(this);
    //    private LedHandler ledHandler = new LedHandler(this);
    private int testType = 0;//0?????? 1??????

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
        init();
        PrinterManager.getInstance().init();


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

        return builder.setTitle(title).addRightText(R.string.item_setting_title, new View.OnClickListener() {
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

    private void init() {
        mLEDManager = new LEDManager();
        mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));

        PrinterManager.getInstance().init();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        rvTestResult.setLayoutManager(layoutManager);
        result = new String[1];

        //???????????????
        resultList.addAll(Arrays.asList(result));
        adapter = new BasePersonTestResultAdapter(resultList);
        //???RecyclerView???????????????
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
        txtStartTest.setText(isBegin == 0 ? R.string.stop_test : R.string.start_test);
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


    /**
     * ?????????????????? ????????????????????????????????????
     */
    public abstract void sendTestCommand(BaseStuPair baseStuPair);


    /**
     * ????????????????????????
     */
    public abstract void gotoItemSetting();

    /**
     * ??????
     */
    public abstract void stuSkip();


    @Override
    public void onCheckIn(Student student) {

        result = new String[1];
        resultList.clear();
        resultList.addAll(Arrays.asList(result));
        adapter.notifyDataSetChanged();
        addStudent(student);
    }

    @OnClick({R.id.txt_stu_skip, R.id.txt_led_setting, R.id.txt_start_test,
            R.id.tv_start_test, R.id.tv_exit_test, R.id.tv_stop_test, R.id.tv_abandon_test, R.id.img_AFR})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_AFR:
                LogUtils.operation("??????????????????");
//                gotoUVCFaceCamera();
                showAFR();
                break;
            case R.id.txt_led_setting:
                LogUtils.operation("??????LED?????????");
                toLedSetting();
                break;
            case R.id.tv_exit_test://???????????????????????????
                LogUtils.operation("????????????");
                if (pair.getStudent() != null) {
                    stuSkipDialog(1);
                }
                break;
            case R.id.txt_stu_skip:
                LogUtils.operation("????????????");
                if (pair.getStudent() != null) {
                    stuSkipDialog(0);
                }
                break;
            case R.id.txt_start_test:
                LogUtils.operation("??????????????????");
                if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN || pair.getBaseDevice().getState() == BaseDeviceState.STATE_FREE) {
                    sendTestCommand(pair);
                }

                break;
            case R.id.tv_start_test:
                LogUtils.operation("??????????????????");
                setTextViewsVisibility(false, false, false, true, true);
                pullStart();
                break;

            case R.id.tv_stop_test:
                LogUtils.operation("??????????????????");
                pullStop();
                break;
            case R.id.tv_abandon_test:
                LogUtils.operation("????????????");
                pullAbandon();
                break;
        }
    }

    public void pullAbandon() {

    }

    public void pullStop() {

    }

    public void pullExit() {

    }

    public void pullStart() {

    }

    public void tickInUI(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTimeCount.setText(msg);
                tvTimeCount.setVisibility(View.VISIBLE);
            }
        });
    }

    public void toLedSetting() {
        if (pair.getBaseDevice().getState() != BaseDeviceState.STATE_NOT_BEGAIN
                && pair.getBaseDevice().getState() != BaseDeviceState.STATE_FREE
                && pair.getBaseDevice().getState() != BaseDeviceState.STATE_ERROR) {
            toastSpeak(getString(R.string.testing_no_use));
            return;
        }
        startActivity(new Intent(this, LEDSettingActivity.class));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        PrinterManager.getInstance().close();
        if (TestConfigs.sCurrentItem != null) {
            mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
            String title = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()) + " " + SettingHelper.getSystemSetting().getHostId();
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), title, mLEDManager.getX(title), 0, true, false);
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), getString(R.string.fairplay), 3, 3, false, true);
            mLEDManager = null;
        }


    }

    protected void addStudent(Student student) {
        if (pair.getStudent() != null) {
            toastSpeak("????????????????????????");
            return;
        }
        if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN || pair.getBaseDevice().getState() == BaseDeviceState.STATE_FREE) {
//            roundNo = 1;
            pair.setResult(0);
            pair.setStudent(student);
            refreshTxtStu(student);
            txtStuResult.setText("");
            toastSpeak(pair.getStudent().getSpeakStuName() + "?????????");
            if (testType == 0) {
                pair.setStartTime(DateUtil.getCurrentTime());
                sendTestCommand(pair);
            }
            setShowLed(pair);
            txtStuResult.setText("");
            Logger.i("addStudent:" + student.toString());
            Logger.i("addStudent:?????????????????????" + testNo + "?????????" + roundNo + "?????????");
        } else {
            toastSpeak(getString(R.string.no_device_add_test_hint));
        }

    }

    /**
     * ??????????????????
     */
    public void refreshTxtStu(@NonNull Student student) {
        if (student != null) {
            txtStuName.setText(student.getStudentName());
            txtStuSex.setText((student.getSex() == Student.MALE ? R.string.male : R.string.female));
            txtStuCode.setText(student.getStudentCode());
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

    /**
     * @param clickType 0 ?????? 1 ??????
     */
    public void stuSkipDialog(final int clickType) {
        new SweetAlertDialog(this)
                .setTitleText(String.format(getString(R.string.dialog_skip_stu_title), pair.getStudent().getStudentName()))
                .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                if (pair.getStudent() != null)
                    LogUtils.operation("????????????:" + pair.getStudent().toString());
                //???????????????????????? ???????????????????????????
                roundNo = 1;
                clearHandler.sendEmptyMessageDelayed(0, 0);
                stuSkip();
                mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
                pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                if (clickType == 1) {
                    setTextViewsVisibility(false, false, false, false, false);
                }
                pullExit();
            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
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
                //ResultDisplayUtils.getStrResultForDisplay(pair.getResult())
                result[roundNo - 1] = ((pair.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(pair.getResult()));
                resultList.clear();
                resultList.addAll(Arrays.asList(result));
                adapter.notifyDataSetChanged();
                pair.setTimeResult(result);
                pair.setEndTime(DateUtil.getCurrentTime());
                //????????????
                saveResult(pair);
                printResult(pair);
                broadResult(pair);
                roundNo = 1;
                clearHandler.sendEmptyMessageDelayed(0, 4000);
                //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
//                if (roundNo < 1) {
//                    if (pair.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
//                        //???????????????????????? ???????????????????????????
//                        roundNo = 1;
//                        //4????????????????????????
//                        clearHandler.sendEmptyMessageDelayed(0, 4000);
//                        return;
//                    }
//                    roundNo++;
//                    txtStuResult.setText("");
//                    toastSpeak(String.format(getString(R.string.test_speak_hint), pair.getStudent().getSpeakStuName(), roundNo)
//                            , String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), roundNo));
//                    Message msg = new Message();
//                    msg.obj = pair;
//                    ledHandler.sendMessageDelayed(msg, 2000);
//                    pair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
//                    if (testType != 1) {
//                        sendTestCommand(pair);
//                    }
//
//                } else {
//                    //???????????????????????? ???????????????????????????
//                    roundNo = 1;
//                    //4????????????????????????
//                    clearHandler.sendEmptyMessageDelayed(0, 4000);
//
//                }


            }
        }
        refreshDevice();

    }


    /**
     * ??????????????????
     *
     * @param baseStuPair ????????????
     */
    private void saveResult(@NonNull BaseStuPair baseStuPair) {
        Logger.i("saveResult==>" + baseStuPair.toString());
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(baseStuPair.getStudent().getStudentCode());
        String itemCode = TestConfigs.sCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : TestConfigs.sCurrentItem.getItemCode();
        roundResult.setItemCode(itemCode);
        roundResult.setResult(baseStuPair.getResult());
        roundResult.setResultState(baseStuPair.getResultState());
        roundResult.setTestTime(baseStuPair.getStartTime() + "");
        roundResult.setPrintTime(baseStuPair.getEndTime() + "");
        roundResult.setRoundNo(1);
//        roundResult.setWeightResult(baseStuPair.getBaseHeight());
        RoundResult bestResult = DBManager.getInstance().queryBestScore(baseStuPair.getStudent().getStudentCode());
        if (bestResult != null) {
            // ???????????????????????? ????????????????????????????????????????????????????????????
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && baseStuPair.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= baseStuPair.getResult()) {
                // ????????????????????????????????????????????????
                roundResult.setIsLastResult(1);
                bestResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(bestResult);
            } else {
                if (bestResult.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                    roundResult.setIsLastResult(1);
                    bestResult.setIsLastResult(0);
                    DBManager.getInstance().updateRoundResult(bestResult);
                } else {
                    roundResult.setIsLastResult(0);
                }
            }
        } else {
            // ???????????????
            roundResult.setIsLastResult(1);
        }
        LogUtils.operation("???????????????" + roundResult.toString());
        DBManager.getInstance().insertRoundResult(roundResult);


        //????????????????????????????????????????????????
        if (TestConfigs.sCurrentItem.getfResultType() == 0) {
            //??????
            if (bestResult != null && bestResult.getIsLastResult() == 1)
                uploadResult(roundResult, bestResult);
            else
                uploadResult(roundResult, roundResult);
        } else {
            //??????
            uploadResult(roundResult, roundResult);
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

    /**
     * ??????????????????
     *
     * @param baseStu
     */
    public synchronized void updateResult(@NonNull BaseStuPair baseStu) {
        if (null != pair.getBaseDevice() && pair.getStudent() != null) {
            pair.setResultState(baseStu.getResultState());
            pair.setResult(baseStu.getResult());
            txtStuResult.setText(((baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult())));
            refreshDevice();
            updateResultLed(((baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult())));
        }
    }

    public void updateVision(BaseStuPair baseStuPair) {
        txtStuResult.setText("?????????:" + ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult()) + "\n?????????:" + ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getBaseHeight()));
        refreshDevice();
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "????????????" + ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult()),
                0, 1, false, true);
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "????????????" + ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getBaseHeight()),
                0, 2, false, true);
    }


    /**
     * ????????????
     */
    private void broadResult(@NonNull BaseStuPair baseStuPair) {
        if (SettingHelper.getSystemSetting().isAutoBroadcast()) {
            if (baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) {
                TtsManager.getInstance().speak(String.format(getString(R.string.speak_foul), baseStuPair.getStudent().getSpeakStuName()));
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
    public void setShowLed(BaseStuPair stuPair) {
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), stuPair.getStudent().getStudentName(), mLEDManager.getX(stuPair.getStudent().getLEDStuName()), 0, true, true);
//        if (stuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) {
//
//            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), String.format(getString(R.string.speak_foul), stuPair.getStudent().getSpeakStuName())
//                    , mLEDManager.getX(String.format(getString(R.string.speak_foul), stuPair.getStudent().getSpeakStuName())), 2, false, true);
//
//        } else {
//            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), ResultDisplayUtils.getStrResultForDisplay(stuPair.getResult())
//                    , mLEDManager.getX(ResultDisplayUtils.getStrResultForDisplay(stuPair.getResult())), 2, false, true);
//
//        }

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


    private void printResult(@NonNull BaseStuPair baseStuPair) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        Student student = baseStuPair.getStudent();
        PrinterManager.getInstance().print(
                String.format(getString(R.string.host_name), TestConfigs.sCurrentItem.getItemName(), SettingHelper.getSystemSetting().getHostId()));
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_stu_code), student.getStudentCode()));
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_stu_name), student.getStudentName()));
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_stu_result), (baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) ?
                        getString(R.string.foul) : ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult())));
        PrinterManager.getInstance().print(
                String.format(getString(R.string.print_result_time), TestConfigs.df.format(Calendar.getInstance().getTime())));
        PrinterManager.getInstance().print(" \n");

    }


//    private static class LedHandler extends Handler {
//
//        private WeakReference<BasePersonTestActivity> mActivityWeakReference;
//
//        public LedHandler(BasePersonTestActivity activity) {
//            mActivityWeakReference = new WeakReference<>(activity);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            BasePersonTestActivity activity = mActivityWeakReference.get();
//            activity.setShowLed((BaseStuPair) msg.obj);
//
//        }
//
//    }

    /**
     * i??????????????????
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
            Logger.i("ClearHandler:??????????????????");
            if (activity != null) {
                activity.pair.getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                activity.pair.setStudent(null);
                activity.refreshTxtStu(null);
                activity.txtStuResult.setText("?????????");
                activity.result = new String[1];
                activity.resultList.clear();
                activity.resultList.addAll(Arrays.asList(activity.result));
                activity.adapter.notifyDataSetChanged();
                activity.setTextViewsVisibility(false, false, false, false, false);
            }

        }
    }

    //    TextView tvStartTest;
//    @BindView(R.id.tv_exit_test)
//    TextView tvExitTest;
//    @BindView(R.id.tv_stop_test)
//    TextView tvStopTest;
//    @BindView(R.id.tv_time_count)
//    TextView tvTimeCount;
//    @BindView(R.id.tv_abandon_test)
//    TextView tvAbandonTest;
    public void setTextViewsVisibility(boolean start, boolean exit, boolean stop, boolean count, boolean abandon) {
        tvStartTest.setVisibility(start ? View.VISIBLE : View.GONE);
        tvExitTest.setVisibility(exit ? View.VISIBLE : View.GONE);
        tvStopTest.setVisibility(stop ? View.VISIBLE : View.GONE);
        if (!TextUtils.isEmpty(tvTimeCount.getText().toString())) {
            tvTimeCount.setVisibility(count ? View.VISIBLE : View.GONE);
        }
        tvAbandonTest.setVisibility(abandon ? View.VISIBLE : View.GONE);
        txtStuSkip.setVisibility(View.GONE);
        if (stop) {
            txtStuResult.setText("");
        }
    }
}
