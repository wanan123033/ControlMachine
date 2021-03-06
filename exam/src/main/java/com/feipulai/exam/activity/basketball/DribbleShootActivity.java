package com.feipulai.exam.activity.basketball;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.feipulai.common.utils.ActivityUtils;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.manager.RunTimerManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.beans.RunTimerConnectState;
import com.feipulai.device.serial.beans.RunTimerResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.BaseAFRFragment;
import com.feipulai.exam.activity.basketball.adapter.DribbleShootAdapter;
import com.feipulai.exam.activity.basketball.adapter.ShootResultAdapter;
import com.feipulai.exam.activity.basketball.result.BasketBallResult;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.fragment.IndividualCheckFragment;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * ??????????????????????????????
 */
public class DribbleShootActivity extends BaseShootActivity implements BaseAFRFragment.onAFRCompareListener {

    @BindView(R.id.iv_portrait)
    ImageView ivPortrait;
    @BindView(R.id.tv_studentCode)
    TextView tvStudentCode;
    @BindView(R.id.tv_studentName)
    TextView tvStudentName;
    @BindView(R.id.tv_gender)
    TextView tvGender;
    @BindView(R.id.tv_grade)
    TextView tvGrade;
    @BindView(R.id.ll_stu_detail)
    LinearLayout llStuDetail;
    @BindView(R.id.ll_individual_check)
    LinearLayout llIndividualCheck;
    @BindView(R.id.tv_pair)
    TextView tvPair;
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.txt_device_status)
    TextView txtDeviceStatus;
    @BindView(R.id.txt_waiting)
    TextView txtWaiting;
    @BindView(R.id.txt_illegal_return)
    TextView txtIllegalReturn;
    @BindView(R.id.txt_continue_run)
    TextView txtContinueRun;
    @BindView(R.id.txt_stop_timing)
    TextView txtStopTiming;
    @BindView(R.id.rv_state)
    RecyclerView rvState;
    @BindView(R.id.tv_led_setting)
    TextView tvLedSetting;
    @BindView(R.id.tv_print)
    TextView tvPrint;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    @BindView(R.id.txt_finish_test)
    TextView txtFinishTest;
    @BindView(R.id.lv_results)
    ListView lvResults;
    @BindView(R.id.cb_connect)
    CheckBox cbConnect;
    @BindView(R.id.rv_result)
    RecyclerView rvResult;

    private List<BasketBallResult> dateList = new ArrayList<>();
    private DribbleShootAdapter interceptAdapter;
    private String[] interceptRound = new String[]{"1??????", "2??????1", "3??????", "4??????2", "5??????", "6??????1", "7??????", "8??????2", "9??????"};
    private ShootSetting setting;
    private FrameLayout afrFrameLayout;
    private BaseAFRFragment afrFragment;
    private int hostId;
    private int startNo;
    private int back1No;
    private int back2No;
    private List<String> resultList = new ArrayList<>();
    private ShootResultAdapter resultAdapter;
    //??????
    private String[] result;
    private int testRound = 1;
    private int testNo = 1;
    private Student student;
    private boolean saved;
    private static final int WAIT_FREE = 0x0;
    private static final int WAIT_CHECK_IN = 0x1;
    private static final int WAIT_BEGIN = 0x2;
    private static final int TESTING = 0x3;
    private static final int WAIT_STOP = 0x4;
    private static final int WAIT_CONFIRM = 0x5;
    protected volatile int state = WAIT_FREE;
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_dribble_shoot_activity;
    }

    private static final String TAG = "DribbleShootActivity";

    @Override
    protected void initData() {
        super.initData();
        hostId = SettingHelper.getSystemSetting().getHostId();
        individualCheckFragment.setResultView(lvResults);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), individualCheckFragment, R.id.ll_individual_check);
        setting = SharedPrefsUtil.loadFormSource(this, ShootSetting.class);
        interceptAdapter = new DribbleShootAdapter(this, dateList);
        rvState.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvState.setAdapter(interceptAdapter);

        if (SettingHelper.getSystemSetting().getCheckTool() == 4 && setAFRFrameLayoutResID() != 0) {
            afrFrameLayout = findViewById(setAFRFrameLayoutResID());
            afrFragment = new BaseAFRFragment();
            afrFragment.setCompareListener(this);
            initAFR();
        }
        hostId = SettingHelper.getSystemSetting().getHostId();
        //getSetting().getInterceptNo()
        RunTimerManager.cmdSetting(setting.getInterceptNo(), hostId, 1, -1, -1, -1);
        checkConnect();
        GridLayoutManager layoutManager = new GridLayoutManager(this, getSetting().getTestNo());
        rvResult.setLayoutManager(layoutManager);
        result = new String[getSetting().getTestNo()];
        //???????????????
        resultList.addAll(Arrays.asList(result));
        resultAdapter = new ShootResultAdapter(resultList);
        //???RecyclerView???????????????
        rvResult.setAdapter(resultAdapter);
        state = WAIT_FREE;
        setOperationUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.life("DribbleShootActivity onResume");
        setting = SharedPrefsUtil.loadFormSource(this, ShootSetting.class);
        dateList.clear();
        for (int i = 0; i < setting.getInterceptNo(); i++) {
            BasketBallResult ballResult = new BasketBallResult();
            ballResult.setName(interceptRound[i]);
            ballResult.setState(false);
            dateList.add(ballResult);
        }
        interceptAdapter.notifyDataSetChanged();
        startNo = setting.getStartNo();
        back1No = setting.getBack1No();
        back2No = setting.getBack2No();
    }

    @Override
    public void compareStu(final Student student) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                individualCheckFragment.checkQulification(student.getStudentCode(), IndividualCheckFragment.STUDENT_CODE);
                if (student != null) {

                    afrFrameLayout.setVisibility(View.GONE);
                }
//                if (student == null) {
//                    InteractUtils.toastSpeak(DribbleShootActivity.this, "??????????????????");
//                    return;
//                }else{
//                    afrFrameLayout.setVisibility(View.GONE);
//                }
//                StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
//                if (studentItem == null) {
//                    InteractUtils.toastSpeak(DribbleShootActivity.this, "????????????");
//                    return;
//                }
//                List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
//                if (results != null && results.size() >= TestConfigs.getMaxTestCount(DribbleShootActivity.this)) {
//                    InteractUtils.toastSpeak(DribbleShootActivity.this, "??????????????????");
//                    return;
//                }
//                // ??????????????????
//                onIndividualCheckIn(student, studentItem, results);
            }
        });


    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (individualCheckFragment.dispatchKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        if (baseEvent.getTagInt() == EventConfigs.TEMPORARY_ADD_STU) {
            Student student = (Student) baseEvent.getData();
            StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
            onIndividualCheckIn(student, studentItem, new ArrayList<RoundResult>());
        }
    }

    @Override
    protected void updateStudent(Student student, List<RoundResult> results) {

        this.student = student;
        //??????????????????????????????????????????????????????????????????????????????????????????1????????????????????????+1
        if (results.size() == 0) {
            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
            testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1;
        } else {
            testNo = results.get(0).getTestNo();
        }
        testRound = results.size() + 1;
        resultList.clear();
        for (int i = 0; i < results.size(); i++) {
            result[i] = InteractUtils.getDisplayResult(results.get(i));
        }
        resultList.addAll(Arrays.asList(result));
        resultAdapter.notifyDataSetChanged();
        InteractUtils.showStuInfo(llStuDetail, student, results);
    }

    public void showAFR() {
        if (SettingHelper.getSystemSetting().getCheckTool() != 4) {
            ToastUtils.showShort("?????????????????????????????????");
            return;
        }
        if (afrFrameLayout == null) {
            return;
        }

        boolean isGoto = afrFragment.gotoUVCFaceCamera(!afrFragment.isOpenCamera);
        if (isGoto) {
            if (afrFragment.isOpenCamera) {
                afrFrameLayout.setVisibility(View.VISIBLE);
            } else {
                afrFrameLayout.setVisibility(View.GONE);
            }
        }
    }

    private void initAFR() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(setAFRFrameLayoutResID(), afrFragment);
        transaction.commitAllowingStateLoss();// ????????????
    }

    public int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }

    @OnClick({R.id.txt_waiting, R.id.txt_illegal_return, R.id.txt_stop_timing,
            R.id.tv_led_setting, R.id.tv_print, R.id.tv_confirm, R.id.txt_finish_test, R.id.img_AFR})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_waiting:
                LogUtils.operation("???????????????????????????????????????...");
                baseTimer = 0;
                RunTimerManager.waitStart();
                for (BasketBallResult basketBallResult : dateList) {
                    basketBallResult.setState(false);
                }

                break;
            case R.id.txt_illegal_return:
                LogUtils.operation("???????????????????????????????????????...");
                RunTimerManager.illegalBack();
                break;

            case R.id.txt_stop_timing:
                warmingDialog("?????????????????????????????????????");
                break;
            case R.id.tv_led_setting:
                LogUtils.operation("???????????????????????????LED??????...");
                IntentUtil.gotoActivity(this, LEDSettingActivity.class);
                break;
            case R.id.tv_print:
                LogUtils.operation("?????????????????????????????????...");
                print();
                break;
            case R.id.tv_confirm:
                LogUtils.operation("?????????????????????????????????...");
                if (student == null)
                    return;
                if (saved) {
                    toastSpeak("??????????????????????????????");
                    return;
                }
                for (int i = 0 ; i < getPairs().size() ; i++){
                    StuDevicePair pair = getPairs().get(i);
                    if (pair.getStudent().getStudentCode().equals(student.getStudentCode()) && pair.getCurrentRoundNo() != 0){
                        disposeResult(timeResult, student, pair.getCurrentRoundNo(), testNo, false);
                        pair.setCurrentRoundNo(0);
                        break;
                    }else if (pair.getStudent().getStudentCode().equals(student.getStudentCode())){
                        disposeResult(timeResult, student, testRound, testNo, false);
                        break;
                    }
                }

                StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
                List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
                InteractUtils.showStuInfo(llStuDetail, student, results);
                result[testRound - 1] = (ResultDisplayUtils.getStrResultForDisplay(timeResult));
                resultList.clear();
                resultList.addAll(Arrays.asList(result));
                resultAdapter.notifyDataSetChanged();
                testRound++;
                saved = true;
                if (studentItem.getExamType() == 2){
                    prepareForCheckIn();
                }
                break;
            case R.id.txt_finish_test:
                LogUtils.operation("???????????????????????????????????????...");
                if (!saved){
                    toastSpeak("??????????????????????????????");
                }else {
                    RunTimerManager.stopRun();
                }
                break;
            case R.id.img_AFR:
                showAFR();
                break;
        }
    }

    private void prepareForCheckIn() {
        resultList.clear();
        resultAdapter.notifyDataSetChanged();
        TestCache.getInstance().clear();
        getPairs().get(0).setStudent(null);
        InteractUtils.showStuInfo(llStuDetail, null, null);
        tvResult.setText("?????????");
        state = WAIT_CHECK_IN;
        setOperationUI();
    }

    @Override
    public void disposeConnect(RunTimerConnectState connectState) {
        mHandler.sendEmptyMessage(CONNECT);
        connect = 0;
    }

    @Override
    public void getResult(final RunTimerResult result) {
        //??????????????????????????????????????????????????????
//        int time = result.getResult();
        Log.i(TAG, "resultTime" + result.toString());
        if (result.getTrackNum() == startNo && result.getOrder() == 1) {
//            baseTimer = System.currentTimeMillis() - baseTimer;
            timeResult = 0;
            if (timer!= null && !timer.isDisposed()){
                timer.dispose();
            }
            keepTime();
        } else {
//            timeResult = (int) (result.getResult() - baseTimer);
            timeResult = rxTime.intValue();
        }
        trackNum = result.getTrackNum();
        Message msg = mHandler.obtainMessage();
        msg.what = UPDATE_RESULT;
        msg.obj = result;
        mHandler.sendMessage(msg);
    }

    @Override
    public void changeState(final int testState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (testState) {
                    case 0://??????
                    case 1:
                    case 5://????????????
                        state = WAIT_FREE;
                        break;
                    case 2://????????????
                        state = WAIT_BEGIN;
                        break;
                    case 3://??????
                        state = TESTING;
                        break;
                    case 4://???????????????
                        state = TESTING;
                        break;
                    case 6://????????????
                        state = WAIT_CONFIRM;
                        break;

                }
                setOperationUI();
            }
        });

    }

    /**
     * ??????????????????????????????UI
     */
    private void setOperationUI() {
        switch (state) {
            case WAIT_BEGIN:
                txtContinueRun.setEnabled(true);
                txtIllegalReturn.setEnabled(true);
                txtStopTiming.setEnabled(false);
                txtWaiting.setEnabled(false);
                break;
            case WAIT_FREE:
                txtContinueRun.setEnabled(false);
                txtIllegalReturn.setEnabled(false);
                txtStopTiming.setEnabled(false);
                txtWaiting.setEnabled(true);
                break;
            case TESTING:
                txtContinueRun.setEnabled(false);
                txtIllegalReturn.setEnabled(true);
                txtStopTiming.setEnabled(true);
                txtWaiting.setEnabled(false);
                break;
            case WAIT_CONFIRM:

                break;
        }
    }

    private void print() {
        if (student == null) {
            return;
        }
//        PrinterManager.getInstance().print("\n");
        PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "??????");
        PrinterManager.getInstance().print("???  ???:" + student.getStudentCode());
        PrinterManager.getInstance().print("???  ???:" + student.getStudentName());
        for (int i = 0; i < result.length; i++) {
            if (!TextUtils.isEmpty(result[i])) {
                PrinterManager.getInstance().print(String.format("??? %1$d ??????", i + 1) + result[i]);
            } else {
                PrinterManager.getInstance().print(String.format("???%1$d??????", i + 1));
            }
        }
        PrinterManager.getInstance().print("????????????:" + TestConfigs.df.format(Calendar.getInstance().getTime()));
        PrinterManager.getInstance().print("\n");
    }

    private void warmingDialog(String warming){
        SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        alertDialog.setTitleText(warming);
        alertDialog.setCancelable(false);
        alertDialog.setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                LogUtils.operation("???????????????????????????????????????...");
                RunTimerManager.stopRun();
                if (timer != null ) {
                    timer.dispose();
                }
            }
        }).setCancelText(getString(R.string.foul)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();

            }
        }).show();
    }

    private int timeResult;
    private volatile int connect;
    private volatile int trackNum;
    ScheduledExecutorService service = Executors
            .newSingleThreadScheduledExecutor();

    private void checkConnect() {
        Runnable runnable = new Runnable() {
            public void run() {
                if (connect > 3 && cbConnect.isChecked()) {
                    mHandler.sendEmptyMessage(UN_CONNECT);
                }
                connect++;
            }
        };
        service.scheduleAtFixedRate(runnable, 1, 1, TimeUnit.SECONDS);

    }

    private Disposable timer;
    private Long rxTime = new Long(0);
    private void keepTime() {
        timer = Observable.interval(100, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (timer == null) {
                            return;
                        }
                        rxTime = aLong;
                        if (!timer.isDisposed()) {
                            txtDeviceStatus.setText(String.format("??????%s", ResultDisplayUtils.getStrResultForDisplay(aLong.intValue() * 100, false)));
                        }

                    }

                });

    }

    private static final int CONNECT = 1;
    private static final int UN_CONNECT = 2;
    private static final int UPDATE_RESULT = 3;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECT:
                    cbConnect.setChecked(true);
                    break;
                case UN_CONNECT:
                    cbConnect.setChecked(false);
                    break;
                case UPDATE_RESULT:
                    tvResult.setText(ResultDisplayUtils.getStrResultForDisplay(rxTime.intValue(), false));
                    RunTimerResult result = (RunTimerResult) msg.obj;
                    if (trackNum == 1){//??????
                        if (result.getOrder() == 1){
                            dateList.get(2).setState(true);
                        }else if (dateList.size()>4 && result.getOrder() == 2){
                            dateList.get(4).setState(true);
                        }else if (dateList.size()>7 && result.getOrder() == 3){
                            dateList.get(6).setState(true);
                        }else if (dateList.size()==9 && result.getOrder() == 4){
                            dateList.get(8).setState(true);
                        }
                    }
                    else if (trackNum == startNo){//??????
                        dateList.get(0).setState(true);
                    }else if (trackNum == back1No){
                        if (result.getOrder() == 1){
                            dateList.get(1).setState(true);
                        }else if (dateList.size()>5 && result.getOrder() == 2){
                            dateList.get(5).setState(true);
                        }

                    }
                    else if (trackNum == back2No){
                        if (result.getOrder() == 1){
                            dateList.get(3).setState(true);
                        }else if (dateList.size()>8 && result.getOrder() == 2){
                            dateList.get(7).setState(true);
                        }

                    }
                    interceptAdapter.notifyDataSetChanged();
                    boolean stop = true;
                    for (int i = 0; i < dateList.size(); i++) {
                        if (!dateList.get(i).isState()){
                            stop = false;
                            break;
                        }
                    }
                    if (stop){//??????????????????
                        timer.dispose();
                        RunTimerManager.stopRun();
                    }
                    break;

            }
            return false;
        }
    });



    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (service != null) {
            service.shutdown();
        }

        if (timer != null ) {
            timer.dispose();
        }
    }
    @Override
    public void setRoundNo(Student student, int roundNo) {
        List<StuDevicePair> pairs = getPairs();
        for (StuDevicePair pair : pairs){
            Student student1 = pair.getStudent();
            if (student1 != null && student1.getStudentCode().equals(student.getStudentCode())){
                pair.setCurrentRoundNo(roundNo);
            }
        }
    }
}
