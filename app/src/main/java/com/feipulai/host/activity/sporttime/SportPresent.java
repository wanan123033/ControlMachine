package com.feipulai.host.activity.sporttime;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.manager.FileUtils;
import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.SportResult;

import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.bean.RoundResultBean;
import com.feipulai.host.bean.RunStudent;
import com.feipulai.host.bean.UploadResults;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.entity.StudentItem;
import com.feipulai.host.netUtils.UploadResultUtil;
import com.feipulai.host.netUtils.netapi.ServerIml;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.orhanobut.logger.utils.LogUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SportPresent implements SportContract.Presenter {

    //    private Disposable disposable;
    SportTimerManger sportTimerManger;
    private boolean connect;
    private SportContract.SportView sportView;
    private volatile int runState;//0空闲 1等待 2结束
    private int deviceCount;
    private int[] connectState;
    //    private int[] disConnect;
    private volatile int[] sendIndex;
    //    private volatile int[] timeState;
    private LEDManager mLEDManager;
    //    private ScheduledExecutorService checkService;
    private ExecutorService service = Executors.newCachedThreadPool();
    //    private volatile boolean[] syncTime;//与子机同步时间是否结束
    public boolean keepTime;//是否开始计时
    private boolean pause;//暂停
    private int synKeep = 0;//计时标记
    private static final String TAG = "SportPresent";
    private static final int INTERVAL = 200;
    private static final int DISCONNECT_COUNT = 10;
    private volatile int wait_stop_flag = -1;
    public SportPresent(SportContract.SportView sportView, int deviceCount) {
        mLEDManager = new LEDManager();
        mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
        sportTimerManger = new SportTimerManger();
        RadioManager.getInstance().setOnRadioArrived(sportResultListener);
        this.sportView = sportView;
        this.deviceCount = deviceCount;
        connectState = new int[deviceCount];
        sendIndex = new int[deviceCount];
        for (int i = 0; i < connectState.length; i++) {
            connectState[i] = 0;
            sendIndex[i] = 1;
        }
//        checkService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void rollConnect() {
        try {
            sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 0);
//        sportTimerManger.syncTime(SettingHelper.getSystemSetting().getHostId(), getTime());//向所有子机发同步时间
//            checkService.scheduleWithFixedDelay(checkRun, 1000, 1000, TimeUnit.MILLISECONDS);
            connect = true;

            service.submit(checkRun);
            Thread.sleep(100);
            getDeviceTime(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private CheckRun checkRun = new CheckRun();

    private class CheckRun implements Runnable {

        @Override
        public void run() {
            intervalRun();
        }
    }

    private void intervalRun() {
        while (connect) {
            if (getRunState() == 0) {//处于空闲时
                if (!pause) {
                    for (int i = 0; i < deviceCount; i++) {
                        try {
                            sportTimerManger.connect(i + 1, SettingHelper.getSystemSetting().getHostId());
                            connectState[i]++;
                            if (connectState[i] > DISCONNECT_COUNT) {
                                sportView.updateDeviceState(i + 1, 0);//连接状态失去
                            }
                            Thread.sleep(INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }

            if (getRunState() == 1) {//处于计时状态
                if (!pause) {
                    for (int i = 0; i < deviceCount; i++) {
                        try {
                            sportTimerManger.getRecentCache(i + 1, SettingHelper.getSystemSetting().getHostId(), sendIndex[i]);
                            connectState[i]++;
                            if (connectState[i] > DISCONNECT_COUNT) {
                                sportView.updateDeviceState(i + 1, 0);//连接状态失去
                            }
                            Thread.sleep(INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

            if (wait_stop_flag == 0){
                try {
                    Thread.sleep(500);
                    sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 1);
                    Thread.sleep(500);
                    sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 1);
                    wait_stop_flag = -1;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (wait_stop_flag == 1){
                try {
                    Thread.sleep(500);
                    sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 0);
                    Thread.sleep(500);
                    sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 0);
                    wait_stop_flag = -1;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void getDeviceTime(int i) {
        sportTimerManger.getTime(i, SettingHelper.getSystemSetting().getHostId());
    }

    public int getSynKeep() {
        return synKeep;
    }

    public void setSynKeep(int synKeep) {
        this.synKeep = synKeep;
    }

    @Override
    public void setContinueRoll(boolean connect) {
        this.connect = connect;
        if (connect) {
            RadioManager.getInstance().setOnRadioArrived(sportResultListener);
        }
    }

    @Override
    public void waitStart() {
        setRunLed(false);
        try {
            if (!MyApplication.RADIO_TIME_SYNC) {
                sportTimerManger.syncTime(1, SettingHelper.getSystemSetting().getHostId(), getTime());
            }
            synKeep = -1;
            setRunState(1);
            for (int i = 0; i < connectState.length; i++) {
                sendIndex[i] = 1;
            }
            wait_stop_flag = 0;
//            sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 1);
//            Thread.sleep(500);
//            sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 1);
//            Thread.sleep(500);
//            sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getDeviceState();
        setPause(false);
    }

    public void setDeviceStateStop() {
        synKeep = 0;
        try {
            setRunState(0);
            setPause(false);
            keepTime = false;
            wait_stop_flag = 1;
//            Thread.sleep(1000);
//            sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 0);
//            Thread.sleep(100);
//            sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 0);
            getDeviceState();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //释放资源
    public void presentRelease() {
//        if (disposable != null) {
//            disposable.dispose();
//        }
        connect = false;
        keepTime = false;
        wait_stop_flag = -1;
        sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 0);
        try {
//            if (checkService != null)
//                checkService.shutdownNow();
//            checkService = null;
            showReady = false;
            runLed = false;
            if (null != service) {
                service.shutdownNow();
            }
            service = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        RadioManager.getInstance().setOnRadioArrived(null);

    }

    public void setDeviceState(int deviceId, int state) {
        sportTimerManger.setDeviceState(deviceId, SettingHelper.getSystemSetting().getHostId(), state);
    }

    private SportResultListener sportResultListener = new SportResultListener(new SportResultListener.SportMsgListener() {
        @Override
        public void onConnect(SportResult result) {//连接情况
            if (result.getDeviceId() == 0 || (result.getDeviceId() - 1) >= connectState.length)
                return;
            connectState[result.getDeviceId() - 1] = 1;
            if (Math.abs(getTime() - result.getLongTime()) > 2000) {
                sportTimerManger.syncTime(SettingHelper.getSystemSetting().getHostId(), getTime());
                MyApplication.RADIO_TIME_SYNC = true;
            }
            if (result.getDeviceState() != 0 && !keepTime) {
                setDeviceState(result.getDeviceId(), 0);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getDeviceState(result.getDeviceId());
            }
            sportView.updateDeviceState(result.getDeviceId(), result.getDeviceState()+1);//正常连接


        }

        @Override
        public void onGetTime(SportResult result) {
//            syncTime[result.getDeviceId()-1] = true;
            MyApplication.RADIO_TIME_SYNC = true;
            if (Math.abs(getTime() - result.getLongTime()) > 2000) {
                sportTimerManger.syncTime(SettingHelper.getSystemSetting().getHostId(), getTime());
            }

        }

        @Override
        public void onGetResult(SportResult result) {//收到结果
            Log.i("SportResultListener", result.toString());
            FileUtils.log(result.toString());
            if (result.getDeviceId() == 0 || (result.getDeviceId() - 1) >= connectState.length)
                return;

            if (synKeep == -1) {//需要计时
                if (result.getDeviceState() != 1) {
                    setDeviceState(result.getDeviceId(), 1);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getDeviceState(result.getDeviceId());
                }
            }

            if (synKeep != -1) {//不需要计时
                if (result.getDeviceState() == 1) {
                    setDeviceState(result.getDeviceId(), 0);
                }
            }

//            if (result.getDeviceState() != 1 && keepTime) {
//                setDeviceState(result.getDeviceId(), 1);
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                getDeviceState(result.getDeviceId());
//                sportView.updateDeviceState(result.getDeviceId(), 1);
//            } else {
//                sportView.updateDeviceState(result.getDeviceId(), 2);//计时
//            }

            sportView.updateDeviceState(result.getDeviceId(), result.getDeviceState()+1);

            if (result.getDeviceState() == 0){
                return;//非计时状态成绩无效
            }
            connectState[result.getDeviceId() - 1] = 1;

            if (result.getLongTime() == 0xFFFFFFFF || result.getLongTime() == 0) {
                return;
            }

            if (result.getSumTimes() > 0) {
//                if (synKeep == -1) {
//                    synKeep = result.getLongTime();
//                }
                for (int i = 0; i < connectState.length; i++) {
                    if (i == result.getDeviceId() - 1) {
                        if (result.getSumTimes() < sendIndex[i]) {
                            sendIndex[i] = result.getSumTimes();
                        } else if (result.getSumTimes() >= sendIndex[i]) {
                            sendIndex[i]++;
                        }
                        sportView.receiveResult(result);
                    }

                }
            }
        }

        @Override
        public void onGetDeviceState(int deviceState, int deviceId) {
            if (deviceId == 0 || deviceId > connectState.length)
                return;
            if (synKeep == -1){
                if (deviceState != 1){
                    setDeviceState(deviceId, 1);
                }
            }

            if (synKeep!=-1){
                if (deviceState == 1){
                    setDeviceState(deviceId, 0);
                }
            }
//
//            if (keepTime){//正在计时
//                if (deviceState == 1){
//                    sportView.updateDeviceState(deviceId, 2);
//                }else {
//                    setDeviceState(deviceId, 1);
//                    sportView.updateDeviceState(deviceId, 1);
//                }
//            }else {
//                sportView.updateDeviceState(deviceId, 1);
//            }

            sportView.updateDeviceState(deviceId, deviceState+1);

        }
    });

    /**
     * 返回当前时间精确到毫秒 不要年月日
     *
     * @return
     */
    public int getTime() {
        Calendar Cld = Calendar.getInstance();
        int HH = Cld.get(Calendar.HOUR_OF_DAY);
        int mm = Cld.get(Calendar.MINUTE);
        int SS = Cld.get(Calendar.SECOND);
        int MI = Cld.get(Calendar.MILLISECOND);
        return HH * 60 * 60 * 1000 + mm * 60 * 1000 + SS * 1000 + MI;
    }

    /**
     * 轮询状态 0 设备连接 1轮询成绩
     *
     * @param runState
     */
    public void setRunState(int runState) {
        setPause(false);
        this.runState = runState;
    }

    public int getRunState() {
        return runState;
    }

    /**
     * 获取子机工作状态
     */
    public void getDeviceState() {
        for (int i = 0; i < connectState.length; i++) {
            sportTimerManger.getDeviceState(i + 1, SettingHelper.getSystemSetting().getHostId());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void getDeviceState(int deviceId) {
        sportTimerManger.getDeviceState(deviceId, SettingHelper.getSystemSetting().getHostId());
    }


    /**
     * @param students
     * @param context
     * @param results
     * @param trackNoMap 序号集合
     */
    public void print(List<Student> students, Context context, Map<Student, List<RoundResult>> results, Map<Student, Integer> trackNoMap) {

    }


    public void showStudent(LinearLayout llStuDetail, Student student, int testNo) {
        RoundResult result = DBManager.getInstance().queryBestScore(student.getStudentCode(), testNo);
        InteractUtils.showStuInfo(llStuDetail, student, result);
    }



    //获取缓冲区成绩
    public void getDeviceCacheResult(int deviceId, int resultIndex) {
        setPause(true);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sportTimerManger.getRecentCache(deviceId, SettingHelper.getSystemSetting().getHostId(), resultIndex);
    }

    private void setPause(boolean pause) {
        this.pause = pause;
    }


    public void printResult(Student student, List<String> results, int current, int max, int groupNo) {
        if (!SettingHelper.getSystemSetting().isAutoPrint() || current != max)
            return;
        PrinterManager.getInstance().print(" \n");
        if (groupNo != -1) {
            PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "号机" + groupNo + "组");
        } else {
            PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "号机");
        }
        PrinterManager.getInstance().print("考  号:" + student.getStudentCode());
        PrinterManager.getInstance().print("姓  名:" + student.getStudentName());
        for (int i = 0; i < results.size(); i++) {
            PrinterManager.getInstance().print(String.format("第%1$d次：", i + 1) + results.get(i));
        }
        PrinterManager.getInstance().print("打印时间:" + TestConfigs.df.format(Calendar.getInstance().getTime()));
        PrinterManager.getInstance().print(" \n");
    }

    /**
     * 成绩保存
     *
     * @param roundNo
     * @param mStudentItem
     * @param testResults
     * @param b
     */
    public void saveResult(int roundNo, StudentItem mStudentItem, SportTestResult testResults, boolean b) {
        int testNo = 1;
        RoundResult roundResult = new RoundResult();

        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(mStudentItem.getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(testResults.getResult());
        roundResult.setResultState(testResults.getResultState());
        roundResult.setTestTime(testResults.getTestTime());
        roundResult.setRoundNo(roundNo);
        roundResult.setTestNo(testNo);
        roundResult.setUpdateState(0);
        RoundResult bestResult = DBManager.getInstance().queryBestScore(mStudentItem.getStudentCode(), testNo);
        String displayResult;
        if (testResults.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
            displayResult = ResultDisplayUtils.getStrResultForDisplay(testResults.getResult());
        } else {
            displayResult = "X";
        }
        updateResultLed(displayResult);
        if (bestResult != null) {
            // 原有最好成绩犯规 或者原有最好成绩没有犯规但是现在成绩更好
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && testResults.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() > testResults.getResult()) {
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
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        ServerIml.uploadResult(UploadResultUtil.getUploadData(roundResult, roundResult));

    }


    public void showStuInfo(LinearLayout llStuDetail, Student student, List<SportTestResult> testResults) {
        TextView mTvStudentName = (TextView) llStuDetail.findViewById(R.id.tv_studentName);
        TextView mTvStudentCode = (TextView) llStuDetail.findViewById(R.id.tv_studentCode);
        TextView mTvGender = (TextView) llStuDetail.findViewById(R.id.tv_gender);
        TextView mTvGrade = (TextView) llStuDetail.findViewById(R.id.tv_grade);
        ImageView imgPortrait = llStuDetail.findViewById(R.id.iv_portrait);
        mTvStudentCode.setText(student == null ? "" : student.getStudentCode());
        mTvStudentName.setText(student == null ? "" : student.getStudentName());
        mTvGender.setText(student == null ? "" : student.getSex() == 0 ? "男" : "女");
        if (student == null) {
            imgPortrait.setImageResource(R.mipmap.icon_head_photo);
        } else {
//            imgPortrait.setImageBitmap(student.getBitmapPortrait());
            Glide.with(imgPortrait.getContext()).load(MyApplication.PATH_IMAGE + student.getStudentCode() + ".jpg")
                    .error(R.mipmap.icon_head_photo).placeholder(R.mipmap.icon_head_photo).into(imgPortrait);
        }

        if (testResults == null || testResults.size() == 0) {
            mTvGrade.setText("");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < testResults.size(); i++) {
                if (testResults.get(i).getResult() != -1) {
                    sb.append(getDisplayResult(testResults.get(i)));
                    sb.append("\n");
                }
            }
            mTvGrade.setText(sb.toString());
        }
    }

    public static String getDisplayResult(SportTestResult lastResult) {
        String displayResult;
        // 单人重测时可能有已有成绩
        if (lastResult == null) {
            displayResult = "";
        } else if (lastResult.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
            displayResult = ResultDisplayUtils.getStrResultForDisplay(lastResult.getResult());
        } else {
            displayResult = "X";
        }
        return displayResult;
    }

    public void updateLastResultLed(RoundResult roundResult) {
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

    /**
     * LED屏显示
     */
    public void setShowLed(Student student, int roundNo) {
        int testNo = 1;
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), student.getLEDStuName() + "   第" + roundNo + "次", 0, 0, true, false);
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "当前：", 0, 1, false, true);
        RoundResult bestResult = DBManager.getInstance().queryBestScore(student.getStudentCode(), testNo);
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

    /**
     * 展示方式为 名字+时间
     *
     * @param runs
     */
    public void setShowLed(List<RunStudent> runs) {
        if (!runLed) {
            List<RunStudent> ledRuns = new ArrayList<>();
            for (RunStudent ledRun : runs) {
                ledRuns.add(new RunStudent().copeRunStudent(ledRun));
            }

            MyRunnable r = new MyRunnable(ledRuns);
            service.submit(r);
        }
    }

    public void clearLed(int t) {
        mLEDManager.clearScreen(TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        mLEDManager.showString(TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId(), "菲普莱体育", 3, 0, false, true);
        mLEDManager.showString(TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId(), t == 0 ? "运动计时" : "红外计时", 4, 1, false, true);
        setShowReady(false);
    }

    public void showLedString(String time) {
        try {
            mLEDManager.showString(TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId(), time,
                    32 / time.getBytes("GBK").length, 3, false, true);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    private class MyRunnable implements Runnable {
        private List<RunStudent> runs;

        private MyRunnable(List<RunStudent> runs) {
            this.runs = runs;
        }

        @Override
        public void run() {
            runLed = true;
            runLed(runs);
        }
    }

    private boolean runLed;

    public void setRunLed(boolean runLed) {
        this.runLed = runLed;
    }

    private void runLed(List<RunStudent> runs) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (runLed) {
            for (int i = 0; i < runs.size(); i++) {
                if (!runLed) {
                    return;
                }
                //只显示有学生信息配对好的手柄
                int currentY = i % 4;
                boolean clearScreen = i % 4 == 0;
                boolean updateScreen = i % 4 == 3 || i == runs.size() - 1;
                Student student = runs.get(i).getStudent();
                String studentName = getFormatName(student.getStudentName());
                if (runs.get(i).getResultList() != null && runs.get(i).getResultList().size() > 0) {
                    int ori = runs.get(i).getResultList().get(runs.get(i).getResultList().size() - 1).getOriResult();
                    studentName = studentName + ResultDisplayUtils.getStrResultForDisplay(ori, false);
                }
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), studentName,
                        0, currentY, clearScreen, updateScreen);
                if (updateScreen) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void showReadyLed(List<RunStudent> runs) {
        if (!showReady) {
            List<RunStudent> ledRuns = new ArrayList<>();
            ledRuns.addAll(runs);
            ShowReady r = new ShowReady(ledRuns);
            service.submit(r);
        }
    }

    private class ShowReady implements Runnable {
        private List<RunStudent> runs;

        private ShowReady(List<RunStudent> runs) {
            this.runs = runs;
        }

        @Override
        public void run() {
            showReady = true;
            showReady(runs);
        }
    }

    private boolean showReady;

    public void setShowReady(boolean showReady) {
        this.showReady = showReady;
    }

    private void showReady(List<RunStudent> runs) {
        while (showReady) {
            for (int i = 0; i < runs.size(); i++) {
                if (!showReady) {
                    return;
                }
                //只显示有学生信息配对好的手柄
                int currentY = i % 4;
                boolean clearScreen = i % 4 == 0;
                boolean updateScreen = i % 4 == 3 || i == runs.size() - 1;
                Student student = runs.get(i).getStudent();
                String studentName = getFormatName(student.getStudentName());
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), String.format("%1d %-4s  %s", i + 1, studentName, "准备"),
                        0, currentY, clearScreen, updateScreen);
                if (updateScreen) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public void updateResultLed(String result) {

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


    /**
     * 新红外计时保存结果
     *
     * @param student
     * @param result
     * @param currentTestTime
     * @param testNo
     * @param startTime
     */
    public void saveResultRadio(Student student, int result, int currentTestTime, int testNo, String startTime) {

        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(student.getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(result);
        roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
        roundResult.setTestTime(startTime);
        roundResult.setRoundNo(currentTestTime);
        roundResult.setTestNo(testNo);
        roundResult.setUpdateState(0);
        RoundResult bestResult = DBManager.getInstance().queryBestScore(student.getStudentCode(), testNo);
        if (bestResult != null) {
            // 原有最好成绩犯规 或者原有最好成绩没有犯规但是现在成绩更好 //跑步时间越短越好
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() >= result) {
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

        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        ServerIml.uploadResult(UploadResultUtil.getUploadData(roundResult, roundResult));
    }

    private String getFormatName(String name) {
        String studentName = name;
        if (studentName.length() >= 4) {
            studentName = studentName.substring(0, 4);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(studentName);
            int spaces = 8 - studentName.length() * 2;
            //Log.i("james","spaces:" + spaces);
            for (int j = 0; j < spaces; j++) {
                sb.append(' ');
            }
            studentName = sb.toString();
        }

        return studentName;
    }

}
