package com.feipulai.host.activity.radio_timer;

import android.content.Context;
import android.text.TextUtils;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.led.RunLEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.RunStudent;
import com.feipulai.host.entity.Student;
import com.feipulai.host.entity.StudentItem;
import com.feipulai.host.netUtils.netapi.ItemSubscriber;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
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
    }

    protected void printResult(Student student, int results) {


        PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "号机");

        PrinterManager.getInstance().print("学籍号:" + student.getStudentCode());
        PrinterManager.getInstance().print("姓  名:" + student.getStudentName());

        PrinterManager.getInstance().print("成绩："+ ResultDisplayUtils.getStrResultForDisplay(results));
        PrinterManager.getInstance().print("打印时间:" + TestConfigs.df.format(Calendar.getInstance().getTime()));

    }


    /**
     * 播报结果
     */
    protected void broadResult(Student student, String results) {
        if (SettingHelper.getSystemSetting().isAutoBroadcast()) {

            TtsManager.getInstance().speak(student.getSpeakStuName() + results);
        }
    }

    protected void saveResult(BaseStuPair baseStuPair) {
        Logger.i("saveResult==>" + baseStuPair.toString());
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(baseStuPair.getStudent().getStudentCode());
        String itemCode = TestConfigs.sCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : TestConfigs.sCurrentItem.getItemCode();
        roundResult.setItemCode(itemCode);
        roundResult.setResult(baseStuPair.getResult());
        roundResult.setResultState(baseStuPair.getResultState());
        roundResult.setTestTime(DateUtil.getCurrentTime2("yyyy-MM-dd HH:mm:ss"));
        roundResult.setRoundNo(1);
        RoundResult bestResult = DBManager.getInstance().queryBestScore(baseStuPair.getStudent().getStudentCode());
        if (bestResult != null) {
            // 原有最好成绩犯规 或者原有最好成绩没有犯规但是现在成绩更好
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() >= baseStuPair.getResult()) {
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
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort(R.string.upload_result_hint);
            return;
        }

//        new RequestBiz().setDataUpLoad(roundResult, lastResult);
        new ItemSubscriber().setDataUpLoad(roundResult, lastResult);

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
