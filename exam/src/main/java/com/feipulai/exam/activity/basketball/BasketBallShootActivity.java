package com.feipulai.exam.activity.basketball;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.utils.ResultDisplayUtils;
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
        changeState(new boolean[]{true, false, false, false, false, false});
        getSetting().setTestType(3);
        int hostId = SettingHelper.getSystemSetting().getHostId();
        RunTimerManager.cmdSetting(1, hostId, 1, -1, -1, 5);
        RunTimerManager.cmdInterceptTime(1);
        checkConnect();
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
    public void compareStu(Student student) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                afrFrameLayout.setVisibility(View.GONE);
            }
        });

        if (student == null) {
            InteractUtils.toastSpeak(this, "该考生不存在");
            return;
        }
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        if (studentItem == null) {
            InteractUtils.toastSpeak(this, "无此项目");
            return;
        }
        List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
        if (results != null && results.size() >= getSetting().getTestNo()) {
            InteractUtils.toastSpeak(this, "该考生已测试");
            return;
        }
        // 可以直接检录
        onIndividualCheckIn(student, studentItem, results);
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
                RunTimerManager.waitStart();
                tvResult.setText("");
                break;
            case R.id.txt_illegal_return:
                RunTimerManager.illegalBack();
                break;
            case R.id.txt_run:
                RunTimerManager.forceStart();
                countDownTime(60);
                saved = false;
                break;
            case R.id.txt_add:

                break;
            case R.id.txt_minus:

                break;
            case R.id.txt_stop_timing:
                if (timer != null) {
                    timer.dispose();
                }
                RunTimerManager.stopRun();
                break;
            case R.id.tv_led_setting:

                break;
            case R.id.tv_print:

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
                break;
            case R.id.txt_finish_test:
                break;
        }
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
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void getResult(RunTimerResult result) {
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
    public void changeState(final boolean[] state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtWaiting.setEnabled(state[0]);
                txtWaiting.setSelected(state[0]);

                txtIllegalReturn.setEnabled(state[1]);
                txtIllegalReturn.setSelected(state[1]);

                txtRun.setEnabled(state[2]);
                txtRun.setSelected(state[2]);

                txtAdd.setEnabled(state[3]);
                txtAdd.setSelected(state[3]);
                txtMinus.setEnabled(state[4]);
                txtMinus.setSelected(state[4]);

            }
        });

    }

    private volatile int connect;
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

    @Override
    public void disposeConnect(RunTimerConnectState connectState) {
        mHandler.sendEmptyMessage(CONNECT);
        connect = 0;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timer != null) {
            timer.dispose();
            timer = null;
        }
        service.shutdown();
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
}
