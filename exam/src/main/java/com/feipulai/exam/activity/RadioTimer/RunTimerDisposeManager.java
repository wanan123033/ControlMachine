package com.feipulai.exam.activity.RadioTimer;

import android.content.Context;
import android.text.TextUtils;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.led.RunLEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.exam.activity.setting.SettingHelper;
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by pengjf on 2018/12/13.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class RunTimerDisposeManager {
    private Context mContext;
    private RunLEDManager mLEDManager;
    private int begin = 0;

    public RunTimerDisposeManager(Context mContext) {
        this.mContext = mContext;
        mLEDManager = new RunLEDManager();
        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId());
        PrinterManager.getInstance().init();
    }

    protected void printResult(Student student, List<String> results, int current, int max, int groupNo) {
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
     * 播报结果
     */
    protected void broadResult(Student student, String results) {
        if (SettingHelper.getSystemSetting().isAutoBroadcast()) {

            TtsManager.getInstance().speak(student.getSpeakStuName() + results);
        }
    }

    protected void saveResult(Student student, int result, int currentTestTime, int testNo,String startTime) {
        //TODO 修改测试次数
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
        roundResult.setEndTime(TestConfigs.df.format(new Date()));
        DBManager.getInstance().insertRoundResult(roundResult);

        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        UploadResults uploadResults = new UploadResults(studentItem.getScheduleNo(), TestConfigs.getCurrentItemCode(),
                student.getStudentCode(), testNo + "", "", RoundResultBean.beanCope(roundResultList));

        uploadResult(uploadResults);
    }

    /**
     * 保存分组成绩
     *
     * @param student
     * @param result
     * @param currentTestTime
     * @param group
     */
    public void saveGroupResult(Student student, int result, int currentTestTime, Group group,String startTime) {
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(student.getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(result);
        roundResult.setMachineResult(result);
        roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
        roundResult.setTestTime(startTime);
        roundResult.setEndTime(TestConfigs.df.format(new Date()));
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

        DBManager.getInstance().insertRoundResult(roundResult);


        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        UploadResults uploadResults = new UploadResults(group.getScheduleNo()
                , TestConfigs.getCurrentItemCode(), student.getStudentCode()
                , "1", group.getGroupNo() + "", RoundResultBean.beanCope(roundResultList));

        uploadResult(uploadResults);

    }

    /**
     * 成绩上传
     *
     * @param uploadResults
     */
    private void uploadResult(UploadResults uploadResults) {
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort("自动上传成绩需下载更新项目信息");
            return;
        }

        ServerMessage.uploadResult(null, uploadResults);

    }


    /**
     * 展示方式为 名字+时间
     *
     * @param runs
     */
    public void setShowLed(List<RunStudent> runs) {
        mLEDManager.clearScreen(SettingHelper.getSystemSetting().getHostId());
        int y;
        int realSize = runs.size();
        for (int i = 0; i < runs.size(); i++) {
            Student student = runs.get(i).getStudent();
            if (student == null) {
                realSize--;
            }
        }
        for (int i = 0; i < realSize; i++) {
            Student student = runs.get(i).getStudent();
            y = i;
            if (student != null) {
                String name = getFormatName(student.getStudentName());
                if (runs.get(i).getMark() != null) {
                    name = name + runs.get(i).getMark();
                }
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), name,
                        0, y, false, true);
            }

            if (i == (realSize - 1)) {
                return;
            }
            if (realSize > 3 && y == 3) {
                try {
                    Thread.sleep(2000);
                    mLEDManager.clearScreen(SettingHelper.getSystemSetting().getHostId());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    public void showReady(List<RunStudent> runs, boolean ready) {
        mLEDManager.clearScreen(SettingHelper.getSystemSetting().getHostId());
        for (int i = 0; i < runs.size(); i++) {
            Student student = runs.get(i).getStudent();
            if (student != null) {
                String studentName = getFormatName(student.getStudentName());
                if (ready) {
                    mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), String.format("%1d %-4s  %s", i + 1, studentName, "准备"),
                            0, i, false, true);
                } else {
                    mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), String.format("%1d %-4s", i + 1, studentName),
                            0, i, false, true);
                }


            }
        }
    }

    public void keepTime() {
        begin = 0;
        String ready = "计时开始";
        mLEDManager.clearScreen(SettingHelper.getSystemSetting().getHostId());
        try {
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), ready,
                    32 / ready.getBytes("GBK").length, 1, true, true);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void showTime(String time) {
        //每10*16毫秒 发送一次
        if (begin % 16 != 0) {
            begin++;
            return;
        }
        try {
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), time,
                    32 / time.getBytes("GBK").length, 2, false, true);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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
