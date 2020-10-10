package com.feipulai.exam.activity.basketball;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
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
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.manager.RunTimerManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.beans.RunTimerConnectState;
import com.feipulai.device.serial.beans.RunTimerResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseAFRFragment;
import com.feipulai.exam.activity.basketball.adapter.ShootResultAdapter;
import com.feipulai.exam.activity.basketball.result.BasketBallTestResult;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
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
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 投篮测试个人模式
 *
 * @date 2020/3/27
 */
public class BasketBallShootActivity extends BaseShootActivity implements BaseAFRFragment.onAFRCompareListener {

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
    @BindView(R.id.cb_connect)
    CheckBox cbConnect;
    //    @BindView(R.id.cb_far)
//    CheckBox cbFar;
//    @BindView(R.id.cb_led)
//    CheckBox cbLed;
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.txt_device_status)
    TextView txtDeviceStatus;
    @BindView(R.id.txt_waiting)
    TextView txtWaiting;
    @BindView(R.id.txt_illegal_return)
    TextView txtIllegalReturn;
    @BindView(R.id.txt_run)
    TextView txtRun;
    @BindView(R.id.txt_add)
    TextView txtAdd;
    @BindView(R.id.txt_minus)
    TextView txtMinus;
    //    @BindView(R.id.txt_stop_timing)
//    TextView txtStopTiming;
    @BindView(R.id.rv_result)
    RecyclerView rvResult;
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
    private FrameLayout afrFrameLayout;
    private BaseAFRFragment afrFragment;
    private List<String> resultList = new ArrayList<>();
    private ShootResultAdapter adapter;
    //成绩
    private String[] result;
    private int testRound = 1;
    private int testNo = 1;
    private Student student;
    private int testResult;
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
        return R.layout.activity_basket_ball_shoot;
    }

    @Override
    protected void initData() {
        super.initData();
        individualCheckFragment.setResultView(lvResults);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), individualCheckFragment, R.id.ll_individual_check);
        if (SettingHelper.getSystemSetting().getCheckTool() == 4 && setAFRFrameLayoutResID() != 0) {
            afrFrameLayout = findViewById(setAFRFrameLayoutResID());
            afrFragment = new BaseAFRFragment();
            afrFragment.setCompareListener(this);
            initAFR();
        }
        GridLayoutManager layoutManager = new GridLayoutManager(this, getSetting().getTestNo());
        rvResult.setLayoutManager(layoutManager);
        result = new String[getSetting().getTestNo()];
        //创建适配器
        resultList.addAll(Arrays.asList(result));
        adapter = new ShootResultAdapter(resultList);
        //给RecyclerView设置适配器
        rvResult.setAdapter(adapter);

        getSetting().setTestType(3);
        int hostId = SettingHelper.getSystemSetting().getHostId();
        RunTimerManager.cmdSetting(1, hostId, 1, -1, -1, -1);
        RunTimerManager.cmdInterceptTime(1);
        checkConnect();
        state = WAIT_FREE;
        setOperationUI();
    }

    @Override
    protected void updateStudent(Student student, List<RoundResult> results) {
        this.student = student;
        //是否有成绩，没有成绩查底该项目是否有成绩，没有成绩测试次数为1，有成绩测试次数+1
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
        adapter.notifyDataSetChanged();
        InteractUtils.showStuInfo(llStuDetail, student, results);
    }



    private void initAFR() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(setAFRFrameLayoutResID(), afrFragment);
        transaction.commitAllowingStateLoss();// 提交更改
    }

    public int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }

    @Override
    public void compareStu(final Student student) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (student == null) {
                    InteractUtils.toastSpeak(BasketBallShootActivity.this, "该考生不存在");
                    return;
                }else{
                    afrFrameLayout.setVisibility(View.GONE);
                }
                StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
                if (studentItem == null) {
                    InteractUtils.toastSpeak(BasketBallShootActivity.this, "无此项目");
                    return;
                }
                List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
                if (results != null && results.size() >= getSetting().getTestNo()) {
                    InteractUtils.toastSpeak(BasketBallShootActivity.this, "该考生已测试");
                    return;
                }
                // 可以直接检录
                onIndividualCheckIn(student, studentItem, results);
            }
        });


    }

    @OnClick({R.id.img_AFR})
    public void onClick(View view) {
        showAFR();
    }

    public void showAFR() {
        if (SettingHelper.getSystemSetting().getCheckTool() != 4) {
            ToastUtils.showShort("未选择人脸识别检录功能");
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

    @OnClick({R.id.txt_waiting, R.id.txt_illegal_return, R.id.txt_run, R.id.txt_add,
            R.id.txt_minus, R.id.tv_led_setting, R.id.tv_print,
            R.id.tv_confirm, R.id.txt_finish_test})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_waiting:
                if (student == null) {
                    toastSpeak("当前无考生");
                    return;
                }
                if (testRound > getSetting().getTestNo()) {
                    toastSpeak("已测试完成");
                    return;
                }
                LogUtils.operation("篮球投篮点击了等待发令...");
                RunTimerManager.waitStart();
                tvResult.setText("");
                break;
            case R.id.txt_illegal_return:
                RunTimerManager.illegalBack();
                LogUtils.operation("篮球投篮点击了违规返回...");
                break;
            case R.id.txt_run:
                RunTimerManager.forceStart();
                saved = false;
                LogUtils.operation("篮球投篮点击了开始...");
                break;
            case R.id.txt_add:
                LogUtils.operation("篮球投篮点击了判罚加一...");
                setPunish(1);
                break;
            case R.id.txt_minus:
                setPunish(-1);
                LogUtils.operation("篮球投篮点击了判罚减一...");
                break;
            case R.id.txt_stop_timing:
                if (timer != null) {
                    timer.dispose();
                }
                RunTimerManager.stopRun();
                LogUtils.operation("篮球投篮点击了结束...");
                break;
            case R.id.tv_led_setting:

                break;
            case R.id.tv_print:
                LogUtils.operation("篮球投篮点击了打印...");
                print();
                break;
            case R.id.tv_confirm:
                if (student == null)
                    return;
                if (saved) {
                    toastSpeak("已保存，请勿重复点击");
                    return;
                }
                disposeResult(testResult, student, testRound, testNo);
                StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
                List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
                InteractUtils.showStuInfo(llStuDetail, student, results);
                result[testRound - 1] = (ResultDisplayUtils.getStrResultForDisplay(testResult));
                resultList.clear();
                resultList.addAll(Arrays.asList(result));
                adapter.notifyDataSetChanged();
                testRound++;
                saved = true;
                state  = WAIT_FREE;
                setOperationUI();
                break;
            case R.id.txt_finish_test:
                if (state!= WAIT_FREE){
                    toastSpeak("测试中不允许结束");
                    return;
                }

                prepareForCheckIn();
                break;
        }
    }

    /**
     * 判罚成绩
     *
     * @param punishType 正数 +1 负数 -1
     */
    private void setPunish(int punishType) {
        if (state == TESTING || state == WAIT_BEGIN) {
            toastSpeak("测试中,不允许更改考试成绩");
        } else {
            testResult+=punishType;
            tvResult.setText(testResult+"");
            result[testRound - 1] = (ResultDisplayUtils.getStrResultForDisplay(testResult));
            resultList.clear();
            resultList.addAll(Arrays.asList(result));
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 检入
     */
    private void prepareForCheckIn() {
        resultList.clear();
        adapter.notifyDataSetChanged();
        TestCache.getInstance().clear();
        InteractUtils.showStuInfo(llStuDetail, null, null);
        tvResult.setText("请检录");
        state = WAIT_FREE;
        setOperationUI();
        student = null;
    }

    private void print() {
        if (student == null) {
            return;
        }
//        PrinterManager.getInstance().print("\n");
        PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "号机");
        PrinterManager.getInstance().print("考  号:" + student.getStudentCode());
        PrinterManager.getInstance().print("姓  名:" + student.getStudentName());
        for (int i = 0; i < result.length; i++) {
            if (!TextUtils.isEmpty(result[i])) {
                PrinterManager.getInstance().print(String.format("第 %1$d 次：", i + 1) + result[i]);
            } else {
                PrinterManager.getInstance().print(String.format("第%1$d次：", i + 1));
            }
        }
//        PrinterManager.getInstance().print("打印时间:" + TestConfigs.df.format(Calendar.getInstance().getTime()));
        PrinterManager.getInstance().print("\n");
    }



    @Override
    public void getResult(RunTimerResult result) {
        Log.i("拦截：",result.toString());
        LogUtils.operation("篮球投篮拦截："+result.toString());
        //根据折返点拦截次数更新投篮次数
        if (result.getTrackNum() == 1 && !txtRun.isEnabled()) {//投篮的拦截的道号必须设定为1道
            testResult = result.getOrder();//第几次
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvResult.setText(testResult + "");
                }
            });

        }
    }

    private Disposable timer;

    private void countDownTime(final int time) {
        timer = Observable.interval(1, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (timer == null) {
                            return;
                        }
                        if (!timer.isDisposed() && aLong <= time) {
                            txtDeviceStatus.setText(String.format("计时 %d", time - aLong));
                        }
                        if (aLong > time) {
                            timer.dispose();
                            txtDeviceStatus.setText("空闲");
                            RunTimerManager.stopRun();
                        }
                    }

                });

    }

    @Override
    public void changeState(final int testState) {
        LogUtils.operation("篮球投篮状态..."+testState);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (testState) {
                    case 0://设置
                    case 1:
                    case 5://违规返回
                        state = WAIT_FREE;
                        break;
                    case 2://等待计时
                        state = WAIT_BEGIN;
                        break;
                    case 3://启动
                        state = TESTING;
                        if (timer!= null && !timer.isDisposed()){
                            timer.dispose();
                        }
                        countDownTime(60);
                        break;
                    case 4://获取到结果
                        state = TESTING;
                        break;
                    case 6://停止计时
                        state = WAIT_CONFIRM;
                        break;

                }
                setOperationUI();
            }
        });

    }

    /**
     * 根据测试状态显示操作UI
     */
    private void setOperationUI() {
        switch (state) {
            case WAIT_BEGIN:
                txtRun.setEnabled(true);
                txtIllegalReturn.setEnabled(true);
                txtWaiting.setEnabled(false);
                break;
            case WAIT_FREE:
                txtRun.setEnabled(false);
                txtIllegalReturn.setEnabled(false);
                txtWaiting.setEnabled(true);
                txtAdd.setEnabled(false);
                txtMinus.setEnabled(false);
                break;
            case TESTING:
                txtRun.setEnabled(false);
                txtIllegalReturn.setEnabled(true);
                txtWaiting.setEnabled(false);
                break;
            case WAIT_CONFIRM:
                txtAdd.setEnabled(true);
                txtMinus.setEnabled(true);
                break;
        }
    }

    private volatile int connect;
    private ScheduledExecutorService service = Executors
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
    public void disposeConnect(RunTimerConnectState connectState) {
        mHandler.sendEmptyMessage(CONNECT);
        connect = 0;
    }



    private static final int CONNECT = 1;
    private static final int UN_CONNECT = 2;
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
        if (timer != null) {
            timer.dispose();
            timer = null;
        }
        service.shutdown();
    }
}
