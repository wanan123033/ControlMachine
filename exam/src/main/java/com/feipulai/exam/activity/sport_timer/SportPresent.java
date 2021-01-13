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
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.ResultDisplayUtils;
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
    private int[] checkState;
    private int[] disConnect;
    private int[] sendIndex;
    private int newTime;
    private LEDManager mLEDManager;
    private int interval;
    private ScheduledExecutorService checkService;
    private boolean syncTime;
    public SportPresent(SportContract.SportView sportView, int deviceCount) {
        mLEDManager = new LEDManager();
        mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
        sportTimerManger = new SportTimerManger();
        RadioManager.getInstance().setOnRadioArrived(sportResultListener);
        this.sportView = sportView;
        this.deviceCount = deviceCount;
        checkState = new int[deviceCount];
        disConnect = new int[deviceCount];
        sendIndex = new int[deviceCount];
        for (int i = 0; i < checkState.length; i++) {
            checkState[i] = 0;
            disConnect[i] = 0;
            sendIndex[i] = 0;
        }
        checkService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void rollConnect() {
//        disposable = Observable.interval(0, 400, TimeUnit.MILLISECONDS)
//                .observeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<Long>() {
//                    @Override
//                    public void accept(Long aLong) throws Exception {
//                        if (connect) {
//                            if (getRunState() == 0) {
//                                if (interval % 6 == 0){
//                                    for (int i = 0; i < deviceCount; i++) {
//                                        sportTimerManger.connect(i + 1, SettingHelper.getSystemSetting().getHostId());
//                                    }
//                                }
//                                interval++;
//                            }
//                            if (getRunState() == 1) {
//                                interval = 0;
//                                for (int i = 0; i < deviceCount; i++) {
//                                    sportTimerManger.getRecentCache(i + 1, SettingHelper.getSystemSetting().getHostId(),sendIndex[i]);
//                                }
//
//                            }
//
//                            for (int i = 0; i < checkState.length; i++) {
//                                if (checkState[i] == 0) {
//                                    disConnect[i]++;
//                                    if (disConnect[i] > 10) {
//                                        sportView.updateDeviceState(i + 1, 0);
//                                    }
//                                } else {
//                                    sportView.updateDeviceState(i + 1, 1);
//                                    checkState[i] = 0;
//                                    disConnect[i] = 0;
//                                }
//                            }
//                        }
//                    }
//                });

        checkService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (connect) {
                    if (getRunState() == 0) {
                        if (interval % 6 == 0){
                            for (int i = 0; i < deviceCount; i++) {
                                sportTimerManger.connect(i + 1, SettingHelper.getSystemSetting().getHostId());
                            }
                            if (!syncTime){
                                sportTimerManger.syncTime(1, SettingHelper.getSystemSetting().getHostId(), getTime());
                                sportTimerManger.getTime(1, SettingHelper.getSystemSetting().getHostId());
                            }
                        }
                        interval++;
                    }
                    if (getRunState() == 1) {
                        interval = 0;
                        for (int i = 0; i < deviceCount; i++) {
                            sportTimerManger.getRecentCache(i + 1, SettingHelper.getSystemSetting().getHostId(),sendIndex[i]);
                        }

                    }

                    for (int i = 0; i < checkState.length; i++) {
                        if (checkState[i] == 0) {
                            disConnect[i]++;
                            if (disConnect[i] > 10) {
                                sportView.updateDeviceState(i + 1, 0);
                            }
                        } else {
                            sportView.updateDeviceState(i + 1, 1);
                            checkState[i] = 0;
                            disConnect[i] = 0;
                        }
                    }
                }
            }
        },100,400,TimeUnit.MILLISECONDS);
    }



    public void presentStop() {
//        if (disposable != null) {
//            disposable.dispose();
//        }
        if (checkService != null && !checkService.isShutdown())
            checkService.shutdown();
        RadioManager.getInstance().setOnRadioArrived(null);
    }


    @Override
    public void setContinueRoll(boolean connect) {
        this.connect = connect;
    }

    @Override
    public void waitStart() {
        sportTimerManger.setDeviceState(1, SettingHelper.getSystemSetting().getHostId(), 1);
        getDeviceState();
    }

    private SportResultListener sportResultListener = new SportResultListener(new SportResultListener.SportMsgListener() {
        @Override
        public void onConnect(SportResult result) {
//            sportView.updateDeviceState(result.getDeviceId(),1);
            if (result.getDeviceId() == 0){
                return;
            }
            checkState[result.getDeviceId() - 1] = 1;
        }

        @Override
        public void onGetTime() {
            syncTime = true;
        }

        @Override
        public void onGetResult(SportResult result) {
            Log.i("SportResultListener", result.toString());
            checkState[result.getDeviceId() - 1] = 1;
            sendIndex[result.getDeviceId()-1] = result.getSumTimes() == 0? 0:(result.getSumTimes()-1);
            Log.i("SportResultListener", result.getLongTime()+"----"+newTime);
            if (result.getSumTimes()!= 0){
                if (result.getLongTime()>newTime) {
                    newTime = result.getLongTime();
                    sportView.receiveResult(result);
                }
            }
        }

        @Override
        public void onGetDeviceState(int deviceState) {
            if (deviceState == 1){
                sportView.getDeviceStart();
                newTime = -1;
                for (int i = 0; i < checkState.length; i++) {
                    sendIndex[i] = 0;
                }
            }else {
                sportView.getDeviceStop();
            }
        }
    });

    /**
     * 返回当前时间精确到毫秒 不要年月日
     *
     * @return
     */
    private int getTime() {
        Calendar Cld = Calendar.getInstance();
        int HH = Cld.get(Calendar.HOUR_OF_DAY);
        int mm = Cld.get(Calendar.MINUTE);
        int SS = Cld.get(Calendar.SECOND);
        int MI = Cld.get(Calendar.MILLISECOND);
        return HH * 60 * 60 * 1000 + mm * 60 * 1000 + SS * 1000 + MI;
    }

    public void setRunState(int runState) {
        this.runState = runState;
    }

    private int getRunState() {
        return runState;
    }

    /**
     *
     * @param students
     * @param context
     * @param results
     * @param trackNoMap 序号集合
     */
    public void print(List<Student> students , Context context, Map<Student,List<RoundResult>> results,Map<Student, Integer> trackNoMap) {
        InteractUtils.printResults(null, students, results,
                TestConfigs.getMaxTestCount(context),trackNoMap);
    }

    public void setDeviceStateStop(){
        sportTimerManger.setDeviceState(1, SettingHelper.getSystemSetting().getHostId(), 0);
    }

    public void showStudent(LinearLayout llStuDetail, Student student, int testNo){
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
        sportTimerManger.getDeviceState(1, SettingHelper.getSystemSetting().getHostId());
    }

    /**
     * 成绩保存
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
                if (testResults.get(i).getResult()!= -1){
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
     *
     */
    public void setShowLed(Student student,int roundNo) {
        int testNo  = 1;
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
        roundResult.setEndTime(System.currentTimeMillis()+"");
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
        LogUtils.operation("红外计时保存成绩:"+roundResult.toString());
        DBManager.getInstance().insertRoundResult(roundResult);


        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        UploadResults uploadResults = new UploadResults(group.getScheduleNo()
                , TestConfigs.getCurrentItemCode(), student.getStudentCode()
                , "1", group , RoundResultBean.beanCope(roundResultList,group));

        uploadResult(uploadResults);

    }
}
