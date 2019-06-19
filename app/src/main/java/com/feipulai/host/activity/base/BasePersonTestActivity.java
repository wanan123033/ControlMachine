package com.feipulai.host.activity.base;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.host.R;
import com.feipulai.host.activity.LEDSettingActivity;
import com.feipulai.host.adapter.Student4JumpAndVitalAdapter;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.netapi.ItemSubscriber;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.feipulai.host.utils.SharedPrefsUtil;
import com.feipulai.host.view.StuSearchEditText;
import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 立定跳远和肺活量实心球的坐位体前屈测试基类(个人测试基类)
 */
public abstract class BasePersonTestActivity extends BaseCheckActivity {

    @BindView(R.id.et_input_text)
    StuSearchEditText etInputText;
    @BindView(R.id.btn_scan)
    ImageButton btnScan;
    @BindView(R.id.ll_search)
    LinearLayout llSearch;
    @BindView(R.id.rv_student)
    RecyclerView rvStudent;
    @BindView(R.id.lv_results)
    ListView lvResults;
    @BindView(R.id.txt_setting)
    TextView txtSetting;
    @BindView(R.id.tv_free_test)
    TextView tvFreeTest;
    @BindView(R.id.tv_led_setting)
    TextView tvLedSetting;
    @BindView(R.id.tv_about)
    TextView tvAbout;

    private List<BaseStuPair> stuPairList;
    private Student4JumpAndVitalAdapter adapter;
    //是否自动打印
    private boolean isAutoPrint;
    //设备故障点击监听
    private OnMalfunctionClickListener listener;
    //是否自动播报
    private boolean mNeedBroadcast;
    private boolean mIsResultUpload;

    public LEDManager mLEDManager;
    //清理学生信息
    private ClearHandler clearHandler = new ClearHandler(this);
    //    private ExecutorService mExecutor;
    //LED屏显示列表
//    private List<BaseStuPair> mLEDList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base4_stand_jump_and_vital);
        ButterKnife.bind(this);
        init();

        PrinterManager.getInstance().init();

//        mExecutor = Executors.newFixedThreadPool(1);
//        mExecutor.submit(mLEDDisplayTask);

    }

    private void init() {
        hostId = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs
                .HOST_ID, 1);
        isAutoPrint = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.AUTO_PRINT, false);
        mNeedBroadcast = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.GRADE_BROADCAST, true);
        mIsResultUpload = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.REAL_TIME_UPLOAD, true);

        mLEDManager = new LEDManager();
        //mLEDManager.link(TestConfigs.sCurrentItem.getMachineCode(), hostId);
        mLEDManager.resetLEDScreen(hostId,TestConfigs.sCurrentItem.getItemName());

        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        rvStudent.setLayoutManager(layoutManager);
        stuPairList = new ArrayList<>();

        //创建适配器
        adapter = new Student4JumpAndVitalAdapter(stuPairList);
        //给RecyclerView设置适配器
        rvStudent.setAdapter(adapter);
        etInputText.setData(rvStudent, lvResults, this);

        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (stuPairList.get(position).getBaseDevice() != null && stuPairList.get(position).getBaseDevice().getState() == BaseDeviceState.STATE_ERROR) {
                    if (listener != null) {
                        listener.malfunctionClickListener(stuPairList.get(position));
                    }
                }
            }
        });

        if (findDevice() != null) {
            for (BaseDeviceState deviceState : findDevice()) {
                addDevice(deviceState);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * 发送测试指令 并且此时应将设备状态改变
     */
    public abstract void sendTestCommand(BaseStuPair baseStuPair);

    /**
     * 查询设备,初始化测试设备
     * 找到设备后需添加设备调用  addDevice方法
     */
    public abstract List<BaseDeviceState> findDevice();


    public void setOnMalfunctionClickListener(OnMalfunctionClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCheckIn(Student student) {
        if (rvStudent.getVisibility() == View.GONE)
            rvStudent.setVisibility(View.VISIBLE);

//        RoundResult roundResult = DBManager.getInstance().queryBestScore(student.getStudentCode());
//        if (roundResult != null) {
//            selectTestDialog(student);
//            return;
//        }


        addStudent(student);

        adapter.notifyDataSetChanged();
    }

    private void addStudent(Student student) {
        boolean flag = false;
        for (BaseStuPair pair : stuPairList) {
            //设备闲置状态添加学生
            if (pair.getBaseDevice() != null && pair.getBaseDevice().getState() == BaseDeviceState.STATE_NOT_BEGAIN) {
                flag = true;
                pair.setResult(0);
                pair.setStudent(student);
                sendTestCommand(pair);
                Logger.i("addStudent:" + student.toString());
                ToastUtils.showShort(student.getStudentName() + "请到" + pair.getBaseDevice().getDeviceId() + "号机测试");
                TtsManager.getInstance().speak(student.getStudentName() + "请到" + pair.getBaseDevice().getDeviceId() + "号机测试");
                break;
            }
        }

        if (!flag) {
            ToastUtils.showShort("当前无设备可添加学生测试");
            TtsManager.getInstance().speak("当前无设备可添加学生测试");
        }
    }


    private void selectTestDialog(final Student student) {
        new AlertDialog.Builder(this).setMessage(student.getStudentName() + "学生已测试过本项目，是否进行再次测试")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addStudent(student);
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", null).show();
    }

    /**
     * 添加设备
     *
     * @param device
     */
    public synchronized void addDevice(@NonNull BaseDeviceState device) {
        for (BaseStuPair pair : stuPairList) {
            if (pair.getBaseDevice() != null && pair.getBaseDevice().getDeviceId() == device.getDeviceId())
                return;//已经有了设备就不再添加
        }
        BaseStuPair baseStuPair = new BaseStuPair();
        baseStuPair.setBaseDevice(device);
        stuPairList.add(baseStuPair);
        adapter.notifyDataSetChanged();
    }

    /**
     * 修改已添加设备状态，设备状态为STATE_END判定为测试结束，可进行成绩打印、播报、保存
     *
     * @param deviceState
     */
    public void updateDevice(@NonNull BaseDeviceState deviceState) {
        Logger.i("updateDevice==>" + deviceState.toString());
        for (int i = 0; i < stuPairList.size(); i++) {

            if (stuPairList.get(i).getBaseDevice() != null && stuPairList.get(i).getBaseDevice().getDeviceId() == deviceState.getDeviceId()) {
                stuPairList.get(i).getBaseDevice().setState(deviceState.getState());
                //状态为测试已结束，并且学生不为空
                if (deviceState.getState() == BaseDeviceState.STATE_END && stuPairList.get(i).getStudent() != null) {
                    //保存成绩
                    saveResult(stuPairList.get(i));
//                    //添加成绩显示LED屏 重新创建对象，避免4秒清空学生信息导致LED屏获取学生信息NULL
//                    BaseStuPair stuPair = new BaseStuPair(stuPairList.get(i).getResult()
//                            , stuPairList.get(i).getResultState(), stuPairList.get(i).getStudent(),
//                            stuPairList.get(i).getBaseDevice());
//                    mLEDList.add(stuPair);
                    //LED屏显示
                    setShowLed(stuPairList.get(i));
                    //打印
                    printResult(stuPairList.get(i));
                    //播报
                    broadResult(stuPairList.get(i));
                    //4秒后清理学生信息
                    clearHandler.sendEmptyMessageDelayed(i, 4000);

                    adapter.notifyDataSetChanged();
                }
                break;
            }
        }
        adapter.notifyDataSetChanged();

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
            if (activity != null) {
                activity.stuPairList.get(msg.what).setStudent(null);
                activity.stuPairList.get(msg.what).getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
                activity.adapter.notifyDataSetChanged();
            }

        }
    }

    /**
     * 保存测试成绩
     *
     * @param baseStuPair 当前设备
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
        roundResult.setTestTime(TestConfigs.df.format(Calendar.getInstance().getTime()));
        roundResult.setRoundNo(1);
        RoundResult bestResult = DBManager.getInstance().queryBestScore(baseStuPair.getStudent().getStudentCode());
        if (bestResult != null) {
            // 原有最好成绩犯规 或者原有最好成绩没有犯规但是现在成绩更好
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= baseStuPair.getResult()) {
                // 这个时候就要同时修改这两个成绩了
                roundResult.setIsLastResult(1);
                bestResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(bestResult);
            } else {
                roundResult.setIsLastResult(0);
            }
        } else {
            // 第一次测试
            roundResult.setIsLastResult(1);
        }

        DBManager.getInstance().insertRoundResult(roundResult);


        //成绩上传判断成绩类型获取最后成绩
        if (TestConfigs.sCurrentItem.getfResultType() == 0) {
            //最好
            if (bestResult != null && bestResult.getIsLastResult() == 1)
                uploadResult(roundResult, bestResult);
            else
                uploadResult(roundResult, roundResult);
        } else {
            //最后
            uploadResult(roundResult, roundResult);
        }

    }

    /**
     * 成绩上传
     *
     * @param roundResult 当前成绩
     * @param lastResult  最后成绩
     */
    private void uploadResult(RoundResult roundResult, RoundResult lastResult) {
        if (!mIsResultUpload) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort("自动上传成绩需下载更新项目信息");
            return;
        }

//        new RequestBiz().setDataUpLoad(roundResult, lastResult);
        new ItemSubscriber().setDataUpLoad(roundResult, lastResult);

    }

    /**
     * 更新学生成绩
     *
     * @param baseStu
     */
    public synchronized void updateResult(@NonNull BaseStuPair baseStu) {
        for (BaseStuPair pair : stuPairList) {
            if (null != pair.getBaseDevice()
                    && null != pair.getStudent()
                    && pair.getBaseDevice().getDeviceId() == baseStu.getBaseDevice().getDeviceId()) {
                pair.getBaseDevice().setState(baseStu.getBaseDevice().getState());
                pair.setResult(baseStu.getResult());
                pair.setResultState(baseStu.getResultState());
                break;

            }

        }
        adapter.notifyDataSetChanged();

    }


    @OnClick({R.id.tv_free_test, R.id.tv_led_setting, R.id.txt_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_free_test://自由测试
                switchToFreeTest();
                break;
            case R.id.tv_led_setting://
                startActivity(new Intent(this, LEDSettingActivity.class));
                break;
            case R.id.txt_setting:
                startActivity(new Intent(this, BaseItemSettingActivity.class));
                break;

        }
    }

    /**
     * 跳转到自由测试
     * Class<? extends BaseActivity> activity
     */
    public void switchToFreeTest() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mLEDDisplayTask.finish();
//        unbindService(mServiceConn);
        PrinterManager.getInstance().close();
    }


    public interface OnMalfunctionClickListener {
        void malfunctionClickListener(BaseStuPair baseStuPair);
    }

    /**
     * 播报结果
     */
    private void broadResult(@NonNull BaseStuPair baseStuPair) {
        if (mNeedBroadcast) {
            if (baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) {
                TtsManager.getInstance().speak(baseStuPair.getStudent().getStudentName() + "犯规");
            } else {
                TtsManager.getInstance().speak(baseStuPair.getStudent().getStudentName() + ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult()));
            }

        }
    }


    private void printResult(@NonNull BaseStuPair baseStuPair) {
        if (!isAutoPrint)
            return;
        Student student = baseStuPair.getStudent();
        PrinterManager.getInstance().print(" \n");
        PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + hostId + "号机\n");
        PrinterManager.getInstance().print("考  号:" + student.getStudentCode() + "\n");
        PrinterManager.getInstance().print("姓  名:" + student.getStudentName() + "\n");
        PrinterManager.getInstance().print("成  绩:" + ((baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "犯规" : ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult())) + "\n");
        PrinterManager.getInstance().print("打印时间:" + TestConfigs.df.format(Calendar.getInstance().getTime()) + "\n");
        PrinterManager.getInstance().print(" \n");
        PrinterManager.getInstance().print(" \n");

    }

    /**
     * LED屏显示
     *
     * @param stuPair
     */
    private void setShowLed(BaseStuPair stuPair) {
        mLEDManager.showString(hostId, stuPair.getStudent().getStudentName(), mLEDManager.getX(stuPair.getStudent().getStudentName()), 0, true, false);
        if (stuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) {

            mLEDManager.showString(hostId, stuPair.getStudent().getStudentName() + "犯规", mLEDManager.getX(stuPair.getStudent().getStudentName() + "犯规"), 2, false, true);

        } else {
            mLEDManager.showString(hostId, ResultDisplayUtils.getStrResultForDisplay(stuPair.getResult()), mLEDManager.getX(ResultDisplayUtils.getStrResultForDisplay(stuPair.getResult())), 2, false, true);

        }

    }

//    //处理LED屏在5秒内没有学生测试出现重新发送更新屏幕指令
//    boolean isResetLEDScreen = false;
//    private BasePersonLEDDisplayTask mLEDDisplayTask = new BasePersonLEDDisplayTask(new BasePersonLEDDisplayTask.OnLedDisplayListener() {
//        @Override
//        public int getDeviceCount() {
//            return mLEDList.size();
//        }
//
//        @Override
//        public Student getStuInPosition(int position) {
//            return mLEDList.get(position).getStudent();
//        }
//
//        @Override
//        public String getStringToShow(int position) {
//            BaseStuPair stuPair = mLEDList.get(position);
//            BaseDeviceState deviceState = stuPair.getBaseDevice();
//            return String.format("%-3d", deviceState.getDeviceId()) + String.format("%-4s", getStuInPosition(position).getStudentName()) +
//                    +stuPair.getResult() + setUnit();
//        }
//
//        @Override
//        public void endLED(List<String> stuCodes) {
//            if (stuCodes.size() > 0) {
//                Log.i("zzs", "remove====>" + isResetLEDScreen);
//                for (String studentCode : stuCodes) {
//                    for (int i = 0; i < mLEDList.size(); i++) {
//                        if (TextUtils.equals(studentCode, mLEDList.get(i).getStudent().getStudentCode())) {
//                            mLEDList.remove(i);
//                            continue;
//                        }
//                    }
//                }
//                isResetLEDScreen = false;
//            } else {
//                Log.i("zzs", "null====>" + isResetLEDScreen);
//                if (!isResetLEDScreen) {
//                    mLEDManager.resetLEDScreen(hostId);
//                    isResetLEDScreen = true;
//                }
//            }
//        }
//
//        @Override
//        public int getHostId() {
//            return hostId;
//        }
//    });

}
