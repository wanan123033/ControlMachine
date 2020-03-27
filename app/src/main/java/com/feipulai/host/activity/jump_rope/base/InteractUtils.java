package com.feipulai.host.activity.jump_rope.base;


import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.JumpRopeResult;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.activity.jump_rope.bean.TestCache;
import com.feipulai.host.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by James on 2018/12/10 0010.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class InteractUtils {

    public static void toast(Activity activity, final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showShort(msg);
            }
        });
    }

    public static void toastSpeak(Activity activity, final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showShort(msg);
            }
        });
        TtsManager.getInstance().speak(msg);
    }

    public static String getDisplayResult(RoundResult lastResult) {
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


    public static boolean isTestableState(int state) {
        return state == BaseDeviceState.STATE_FREE || state == BaseDeviceState.STATE_LOW_BATTERY;
    }

    public static RoundResult getBestResult(List<RoundResult> results) {
        RoundResult bestResult = null;
        // 有已有成绩
        if (results != null && results.size() > 0) {
            for (RoundResult result : results) {
                if (result.getIsLastResult() == 1) {
                    bestResult = result;
                    break;
                }
            }
        }
        return bestResult;
    }

    public static int getResultInt(StuDevicePair pair) {
        return pair.getDeviceResult() == null ? 0 : pair.getDeviceResult().getResult();
    }

    /**
     * 收集到一轮测试的数据后,依据当前的考生设备配对信息,保存成绩
     *
     * @param pairs 考生设备配对信息
     */
    public static void saveResults(List<StuDevicePair> pairs) {
        Map<Student, RoundResult> saveResults = new HashMap<>(pairs.size() * 2);
        Map<Student, RoundResult> bestResults = new HashMap<>(pairs.size() * 2);
        for (StuDevicePair pair : pairs) {
            int state = pair.getBaseDevice().getState();
            Student student = pair.getStudent();
            // 暂停使用的设备不记录成绩
            if (student == null
                    || state == BaseDeviceState.STATE_STOP_USE
                    || pair.getDeviceResult() == null) {
                continue;
            }
            RoundResult roundResult = new RoundResult();
            //设置数据库格式成绩其他内容
            roundResult.setStudentCode(student.getStudentCode());
            roundResult.setItemCode(TestConfigs.getCurrentItemCode());
            roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
            roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
            roundResult.setTestTime(pair.getStartTime()+"");
            roundResult.setPrintTime(pair.getEndTime()+"");
            roundResult.setRoundNo(1);

            switch (TestConfigs.sCurrentItem.getMachineCode()) {

                case ItemDefault.CODE_TS:
                    JumpRopeResult jumpRopeResult = (JumpRopeResult) pair.getDeviceResult();
                    roundResult.setResult(jumpRopeResult.getResult());
                    // Log.i("保存成绩->中断次数","" + jumpRopeResult.getStumbleTimes());
                    roundResult.setStumbleCount(jumpRopeResult.getStumbleTimes());
                    break;
                case ItemDefault.CODE_YTXS:
                case ItemDefault.CODE_YWQZ:
                    roundResult.setResult(pair.getDeviceResult().getResult());
                    break;

            }

            RoundResult bestResult = DBManager.getInstance().queryBestScore(student.getStudentCode());
            if (bestResult != null && bestResult.getResult() > roundResult.getResult()) {
                roundResult.setIsLastResult(0);
                bestResults.put(student, bestResult);
                // Log.i("james", "bestResult" +  bestResult.toString());
            } else {
                roundResult.setIsLastResult(1);
                if (bestResult != null) {
                    bestResult.setIsLastResult(0);
                    DBManager.getInstance().updateRoundResult(bestResult);
                    Logger.i("更新成绩:" + bestResult.toString());
                }
                bestResults.put(student, roundResult);
            }
            DBManager.getInstance().insertRoundResult(roundResult);
            saveResults.put(student, roundResult);
        }
        TestCache.getInstance().setBestResults(bestResults);
        TestCache.getInstance().setSaveResults(saveResults);
    }

    /**
     * 检查是否是最终成绩
     *
     * @param pairs 考生设备配对信息
     * @return 是否是最终成绩
     */
    public static boolean checkFinalResults(List<StuDevicePair> pairs) {
        for (StuDevicePair pair : pairs) {
            Student student = pair.getStudent();
            int state = pair.getBaseDevice().getState();
            if (student != null && state == BaseDeviceState.STATE_COUNTING) {
                // 存在手柄还在计数,证明当前成绩还不是最终成绩
                return false;
            }
        }
        return true;
    }

    public static void printResults(int hostId, List<StuDevicePair> pairs,
                                    Map<Student, RoundResult> saveResults,
                                    Map<Student, RoundResult> bestResults) {
        Logger.i("成绩打印开始");
        String printTime = TestConfigs.df.format(Calendar.getInstance(Locale.CHINA).getTime());
        for (StuDevicePair pair : pairs) {
            Student student = pair.getStudent();
            int state = pair.getBaseDevice().getState();
            if (student == null
                    || state == BaseDeviceState.STATE_STOP_USE
                    || pair.getDeviceResult() == null) {
                continue;
            }
            RoundResult saveResult = saveResults.get(student);
            String displayResult = ResultDisplayUtils.getStrResultForDisplay(saveResult.getResult());
            String machineName = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode());
            PrinterManager.getInstance().print(MessageFormat.format("{0}{1}号机", machineName, hostId));
            PrinterManager.getInstance().print("考  号:" + pair.getStudent().getStudentCode());
            PrinterManager.getInstance().print("姓  名:" + pair.getStudent().getStudentName());

            RoundResult bestResult = bestResults.get(student);
            String displayBestResult = ResultDisplayUtils.getStrResultForDisplay(bestResult.getResult());

            switch (TestConfigs.sCurrentItem.getMachineCode()) {
                case ItemDefault.CODE_TS:
                    PrinterManager.getInstance().print("手柄号:" + pair.getBaseDevice().getDeviceId());
                    PrinterManager.getInstance().print("成  绩:" + displayResult + "(中断:" + saveResult.getStumbleCount() + ")");
                    PrinterManager.getInstance().print("最好成绩:" + displayBestResult);
                    break;

                default:
                    PrinterManager.getInstance().print("成  绩:" + displayResult);
            }

//            saveResult.setPrintTime(printTime);
//            bestResult.setPrintTime(printTime);

//            DBManager.getInstance().updateRoundResult(saveResult);
//            DBManager.getInstance().updateRoundResult(bestResult);

            PrinterManager.getInstance().print("打印时间:" + printTime);
            PrinterManager.getInstance().print("\n");
        }
        PrinterManager.getInstance().print("\n\n");
        Logger.i("成绩打印完成");
    }

    /**
     * 将考生信息填入到ll_detail 中,ll_detail 必须为 {com.feipulai.exam.R.layout.layout_stu_info}
     *
     * @param llStuDetail 目的视图
     * @param student     考生信息
     * @param lastResult  最后成绩
     */
    public static void showStuInfo(LinearLayout llStuDetail, Student student, RoundResult lastResult) {
        TextView mTvStudentName = (TextView) llStuDetail.findViewById(R.id.tv_studentName);
        TextView mTvStudentCode = (TextView) llStuDetail.findViewById(R.id.tv_studentCode);
        TextView mTvGender = (TextView) llStuDetail.findViewById(R.id.tv_gender);
        TextView mTvGrade = (TextView) llStuDetail.findViewById(R.id.tv_grade);

        mTvStudentCode.setText(student == null ? "" : student.getStudentCode());
        mTvStudentName.setText(student == null ? "" : student.getStudentName());
        mTvGender.setText(student == null ? "" : student.getSex() == 0 ? "男" : "女");
        mTvGrade.setText(InteractUtils.getDisplayResult(lastResult));
    }

    /**
     * 保证中文字符串的长度为指定长度
     *
     * @param chinese      中文字符串
     * @param targetLength 目的长度(中文长度)
     * @return 结果字符串
     */
    public static String getStrWithLength(String chinese, int targetLength) {
        String result;
        if (chinese.length() >= targetLength) {
            result = chinese.substring(0, targetLength);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(chinese);
            int spaces = (targetLength - chinese.length()) * 2;
            for (int j = 0; j < spaces; j++) {
                sb.append(' ');
            }
            result = sb.toString();
        }
        return result;
    }

    public static String generateLEDTestString(List<StuDevicePair> pairs, int position) {
        String showContent;
        StuDevicePair pair = pairs.get(position);
        Student student = pairs.get(position).getStudent();
        // Log.i("james", pairs.size() + "   position:" + position + "  student:" + student);
        if (student == null) {
            return null;
        }
        String studentName = InteractUtils.getStrWithLength(student.getStudentName(), 4);
        int result = InteractUtils.getResultInt(pair);
        int deviceId = pair.getBaseDevice().getDeviceId();

        switch (TestConfigs.sCurrentItem.getMachineCode()) {

            case ItemDefault.CODE_TS:
                JumpRopeSetting jumpRopeSetting = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), JumpRopeSetting.class);
                if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_STOP_USE) {
                    showContent = String.format(Locale.CHINA, "%1s%-3d暂停使用    ",
                            SerialConfigs.GROUP_NAME[jumpRopeSetting.getDeviceGroup()], deviceId/*, "暂停使用"*/);
                } else {
                    showContent = String.format(Locale.CHINA, "%1s%-3d" + studentName + "%-4d",
                            SerialConfigs.GROUP_NAME[jumpRopeSetting.getDeviceGroup()], deviceId, result);
                }
                break;

            case ItemDefault.CODE_YTXS:
            case ItemDefault.CODE_YWQZ:
            case ItemDefault.CODE_FWC:
                if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_STOP_USE) {
                    showContent = String.format(Locale.CHINA, "%-3d暂停使用     ", deviceId);
                } else {
                    showContent = String.format(Locale.CHINA, "%-3d%-4s%-3d", deviceId, studentName, result);
                }
                break;

            default:
                throw new IllegalArgumentException("wrong machine code");
        }
        return showContent;
    }

    public static int stringLength(String value) {
        int valueLength = 0;
        String chinese = "[\u4e00-\u9fa5]";
        for (int i = 0; i < value.length(); i++) {
            String temp = value.substring(i, i + 1);
            if (temp.matches(chinese)) {
                valueLength += 2;
            } else {
                valueLength += 1;
            }
        }
        return valueLength;
    }
}