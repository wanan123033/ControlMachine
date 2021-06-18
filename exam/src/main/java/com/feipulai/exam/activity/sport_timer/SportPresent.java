package com.feipulai.exam.activity.sport_timer;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.sport_timer.bean.SportTestResult;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.RunStudent;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
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
    private volatile int[] timeState;
    private LEDManager mLEDManager;
    private volatile int interval;
    private ScheduledExecutorService checkService;
//    private volatile boolean[] syncTime;//与子机同步时间是否结束
    private boolean keepTime;//是否开始计时
    private boolean pause;//暂停
//    private int synKeep;//累计同步时间此时
    private static final String TAG = "SportPresent";
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
        timeState = new int[deviceCount];
//        syncTime = new boolean[deviceCount];
        for (int i = 0; i < connectState.length; i++) {
            connectState[i] = 0;
            sendIndex[i] = 0;
            timeState[i] = -1;
//            syncTime[i] = false;
        }
        checkService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void rollConnect() {
        sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 0);
//        sportTimerManger.syncTime(SettingHelper.getSystemSetting().getHostId(), getTime());//向所有子机发同步时间
        checkService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                intervalRun();
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);

    }

    private void intervalRun() {
        while (connect) {
            if (getRunState() == 0) {//处于空闲时
//                if (synKeep< 10){
//                    for (int i = 0; i < syncTime.length; i++) {
//                        try {
//                            if (!syncTime[i]) {
//                                sportTimerManger.syncTime(i + 1, SettingHelper.getSystemSetting().getHostId(), getTime());
//                                Thread.sleep(1000);
//                                sportTimerManger.getTime(i+1, SettingHelper.getSystemSetting().getHostId());
//                                Thread.sleep(1000);
//                            }
//
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//                synKeep++;
                if (!pause) {
                    if (interval % 8 == 0) {
                        for (int i = 0; i < deviceCount; i++) {
                            try {
                                sportTimerManger.connect(i + 1, SettingHelper.getSystemSetting().getHostId());
                                connectState[i]++;
                                if (connectState[i] > 10) {
                                    sportView.updateDeviceState(i + 1, 0);//连接状态失去
                                }
                                Thread.sleep(100);
                                if (timeState[i] != 0) {
                                    setDeviceState(i + 1, 0);
                                    Thread.sleep(100);
                                    getDeviceState(i + 1);
                                    Thread.sleep(100);
                                }

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                    interval++;
                }

            }


            if (getRunState() == 1) {//处于计时状态
                if (!pause) {
                    interval = 0;
                    for (int i = 0; i < deviceCount; i++) {
                        try {
                            sportTimerManger.getRecentCache(i + 1, SettingHelper.getSystemSetting().getHostId(), sendIndex[i]);
                            connectState[i]++;
                            if (connectState[i] > 10) {
                                sportView.updateDeviceState(i + 1, 0);//连接状态失去
                            }
                            Thread.sleep(100);
                            if (timeState[i] != 1) {
                                setDeviceState(i + 1, 1);
                                Thread.sleep(100);
                                getDeviceState(i + 1);
                                Thread.sleep(100);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

        }
    }

    //释放资源
    public void presentRelease() {
//        if (disposable != null) {
//            disposable.dispose();
//        }
        connect = false;
        sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 0);
        try {
            if (checkService != null)
                checkService.shutdown();
            checkService = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        RadioManager.getInstance().setOnRadioArrived(null);
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
        try {
            keepTime = true;
            setPause(true);
            sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 0);
            Thread.sleep(100);
            for (int i = 0; i < connectState.length; i++) {
                sendIndex[i] = 0;
                timeState[i] = -1;
            }
            sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 1);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getDeviceState();
    }

    private void setDeviceState(int deviceId, int state) {
        sportTimerManger.setDeviceState(deviceId, SettingHelper.getSystemSetting().getHostId(), state);
    }

    private SportResultListener sportResultListener = new SportResultListener(new SportResultListener.SportMsgListener() {
        @Override
        public void onConnect(SportResult result) {//连接情况
            if (result.getDeviceId() == 0 || (result.getDeviceId() - 1) >= connectState.length)
                return;
            connectState[result.getDeviceId() - 1] = 1;
            sportView.updateDeviceState(result.getDeviceId(), 1);//正常连接
        }

        @Override
        public void onGetTime(SportResult result) {
//            syncTime[result.getDeviceId()-1] = true;
        }

        @Override
        public void onGetResult(SportResult result) {//收到结果
            Log.i("SportResultListener", result.toString());
            if (result.getDeviceId() == 0 || (result.getDeviceId() - 1) >= connectState.length)
                return;
            connectState[result.getDeviceId() - 1] = 1;
//            sportView.updateDeviceState(result.getDeviceId(), 2);//正常连接
            LogUtils.operation("SportResultListener" + result.getLongTime()
                    + "------sumTime:" + result.getSumTimes() + "-----currentTime:" + result.getCurrentTime());
            if (result.getSumTimes() != 0) {
                for (int i = 0; i < connectState.length; i++) {
                    if (i == result.getDeviceId() - 1) {
                        if (result.getSumTimes() > sendIndex[i]) {
                            sendIndex[i]++;
                            sportView.receiveResult(result);
                        }
                    }

                }
            }
        }

        @Override
        public void onGetDeviceState(int deviceState, int deviceId) {
            if (deviceId == 0 || deviceId > connectState.length)
                return;
            if (deviceState == 1) {
                if (keepTime) {
                    sportView.updateDeviceState(deviceId, 2);
                    sportView.getDeviceStart();
                    timeState[deviceId - 1] = 1;
                }
                timeState[deviceId - 1] = 1;
            } else {
                if (keepTime) {
                    sportView.updateDeviceState(deviceId, 1);
                } else {
                    sportView.getDeviceStop();
                }
                timeState[deviceId - 1] = 0;
            }
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
     * @param students
     * @param context
     * @param results
     * @param trackNoMap 序号集合
     */
    public void print(List<Student> students, Context context, Map<Student, List<RoundResult>> results, Map<Student, Integer> trackNoMap) {
        InteractUtils.printResults(null, students, results,
                TestConfigs.getMaxTestCount(context), trackNoMap);
    }

    public void setDeviceStateStop() {
        try {
            setPause(true);
            keepTime = false;
            Thread.sleep(100);
            sportTimerManger.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 0);
            Thread.sleep(100);
            getDeviceState();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void showStudent(LinearLayout llStuDetail, Student student, int testNo) {
        List<RoundResult> scoreResultList = new ArrayList<>();
        RoundResult result = DBManager.getInstance().queryBestScore(student.getStudentCode(), testNo);
        if (result != null) {
            scoreResultList.add(result);
        }
        InteractUtils.showStuInfo(llStuDetail, student, scoreResultList);
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
     */
    public void saveResult(int roundNo, StudentItem mStudentItem, SportTestResult testResults) {
        int testNo = 1;
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(mStudentItem.getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(testResults.getResult());
        roundResult.setMachineResult(testResults.getResult());
        roundResult.setResultState(testResults.getResultState());
        roundResult.setTestTime(testResults.getTestTime());
        //生成结束时间
        roundResult.setEndTime(System.currentTimeMillis() + "");
        roundResult.setRoundNo(roundNo);
        roundResult.setTestNo(testNo);
        roundResult.setExamType(mStudentItem.getExamType());
        roundResult.setScheduleNo(mStudentItem.getScheduleNo());
        roundResult.setUpdateState(0);
        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
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
        UploadResults uploadResults = new UploadResults(mStudentItem.getScheduleNo(), TestConfigs.getCurrentItemCode(),
                mStudentItem.getStudentCode(), testNo + "", null, RoundResultBean.beanCope(roundResultList));
        uploadResult(uploadResults);

    }

    private void uploadResult(UploadResults uploadResult) {
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort("自动上传成绩需下载更新项目信息");
            return;
        }
        List<UploadResults> uploadResults = new ArrayList<>();
        uploadResults.add(uploadResult);
        ServerMessage.uploadResult(uploadResults);
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
        mLEDManager.clearScreen(TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());

        int y;
        int realSize = runs.size();
        for (int i = 0; i < realSize; i++) {
            Student student = runs.get(i).getStudent();
            y = i;
            if (i <= 3) {
                if (student != null) {
                    String name = getFormatName(student.getStudentName());
                    if (runs.get(i).getMark() != null) {
                        name = name + runs.get(i).getMark();
                    }
                    mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), name,
                            0, y, false, true);
                }
            } else {
                try {
                    Thread.sleep(4000);
                    mLEDManager.clearScreen(TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
                    if (student != null) {
                        String name = getFormatName(student.getStudentName());
                        if (runs.get(i - 3).getMark() != null) {
                            name = name + runs.get(i - 3).getMark();
                        }
                        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), name,
                                0, y, false, true);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
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
     * 保存分组成绩
     *
     * @param student
     * @param result
     * @param currentTestTime
     * @param group
     */
    public void saveGroupResult(Student student, int result, int currentTestTime, Group group, String startTime) {
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(student.getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(result);
        roundResult.setMachineResult(result);
        roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
        roundResult.setTestTime(startTime);
        roundResult.setEndTime(System.currentTimeMillis() + "");
        roundResult.setRoundNo(currentTestTime);
        roundResult.setTestNo(1);
        roundResult.setGroupId(group.getId());
        roundResult.setExamType(group.getExamType());
        roundResult.setScheduleNo(group.getScheduleNo());
        roundResult.setUpdateState(0);
        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
        RoundResult bestResult = DBManager.getInstance().queryGroupBestScore(student.getStudentCode(), group.getId());
        if (bestResult != null) {
            // 原有最好成绩犯规 或者原有最好成绩没有犯规但是现在成绩更好
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
        LogUtils.operation("红外计时保存成绩:" + roundResult.toString());
        DBManager.getInstance().insertRoundResult(roundResult);


        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        UploadResults uploadResults = new UploadResults(group.getScheduleNo()
                , TestConfigs.getCurrentItemCode(), student.getStudentCode()
                , "1", group, RoundResultBean.beanCope(roundResultList, group));

        uploadResult(uploadResults);

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
        roundResult.setMachineResult(result);
        roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
        roundResult.setTestTime(startTime);
        roundResult.setRoundNo(currentTestTime);
        roundResult.setTestNo(testNo);
        roundResult.setExamType(studentItem.getExamType());
        roundResult.setScheduleNo(studentItem.getScheduleNo());
        roundResult.setUpdateState(0);
        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
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
        roundResult.setEndTime(System.currentTimeMillis() + "");
        DBManager.getInstance().insertRoundResult(roundResult);

        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        UploadResults uploadResults = new UploadResults(studentItem.getScheduleNo(), TestConfigs.getCurrentItemCode(),
                student.getStudentCode(), testNo + "", null, RoundResultBean.beanCope(roundResultList));

        uploadResult(uploadResults);
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
