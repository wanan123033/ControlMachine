package com.feipulai.exam.activity.jump_rope.utils;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.IDeviceResult;
import com.feipulai.device.serial.beans.JumpRopeResult;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
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
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    public static String generateGroupIdentity(Group group) {
        return group.getScheduleNo() + group.getGroupType() + group.getSortName() + group.getGroupNo();
    }

    public static String generateGroupText(Group group) {
        // 项目名称+场次+组别性别+组别+组号
        return (group.getGroupType() == Group.MALE ? "男子" : (group.getGroupType() == Group.FEMALE ? "女子" : "男女混合")) +
                group.getSortName() +
                "第" + group.getGroupNo() + "组";
    }

    public static boolean isTestableState(int state) {
        return state == BaseDeviceState.STATE_FREE || state == BaseDeviceState.STATE_LOW_BATTERY;
    }

    public static boolean isTestableGroup(Group group) {
        // 未测试 || 未测完的组均可测试
        return group.getIsTestComplete() != 1;
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

    public static void uploadResults() {
        if (SettingHelper.getSystemSetting().isRtUpload()) {
            if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
                ToastUtils.showShort("自动上传成绩需下载更新项目信息");
            } else {
                List<UploadResults> uploadResults = new ArrayList<>();
                for (Student student : TestCache.getInstance().getAllStudents()) {
                    String groupNo;
                    String scheduleNo;
                    String testNo;
                    List<RoundResult> roundResultList = TestCache.getInstance().getResults().get(student);
                    if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN) {
                        Group group = TestCache.getInstance().getGroup();
                        groupNo = group.getGroupNo() + "";
                        scheduleNo = group.getScheduleNo();
                        testNo = "1";
                    } else {
                        StudentItem studentItem = TestCache.getInstance().getStudentItemMap().get(student);
                        scheduleNo = studentItem.getScheduleNo();
                        groupNo = "";
                        testNo = TestCache.getInstance().getTestNoMap().get(student) + "";
                    }
                    UploadResults uploadResult = new UploadResults(scheduleNo,
                            TestConfigs.getCurrentItemCode(), student.getStudentCode()
                            , testNo, groupNo, RoundResultBean.beanCope(roundResultList));
                    uploadResults.add(uploadResult);
                }
                Logger.i("自动上传成绩:" + uploadResults.toString());
                ServerMessage.uploadResult(/*null,*/ uploadResults);
            }
        }
    }

    /**
     * 收集到一轮测试的数据后,依据当前的考生设备配对信息,保存成绩
     *
     * @param pairs    考生设备配对信息
     * @param testDate 测试时间
     */
    public static void saveResults(List<StuDevicePair> pairs, String testDate) {
        for (StuDevicePair pair : pairs) {
            Student student = pair.getStudent();
            int state = pair.getBaseDevice().getState();
            IDeviceResult deviceResult = pair.getDeviceResult();
            // 暂停使用的设备不记录成绩
            if (student == null
                    || deviceResult == null
                    || state == BaseDeviceState.STATE_STOP_USE) {
                continue;
            }

            List<RoundResult> results = TestCache.getInstance().getResults().get(student);

            RoundResult roundResult = new RoundResult();
            roundResult.setStudentCode(student.getStudentCode());
            roundResult.setItemCode(TestConfigs.getCurrentItemCode());

            int machineCode = TestConfigs.sCurrentItem.getMachineCode();
            roundResult.setMachineCode(machineCode);


            switch (machineCode) {

                case ItemDefault.CODE_TS:
                    JumpRopeResult jumpRopeResult = (JumpRopeResult) deviceResult;
                    roundResult.setMachineResult(jumpRopeResult.getResult());
                    roundResult.setResult(jumpRopeResult.getResult());
                    roundResult.setStumbleCount(jumpRopeResult.getStumbleTimes());
                    break;

                case ItemDefault.CODE_YWQZ:
                case ItemDefault.CODE_YTXS:
                case ItemDefault.CODE_PQ:
                case ItemDefault.CODE_FWC:
                    roundResult.setMachineResult(deviceResult.getResult());
                    roundResult.setPenaltyNum(pair.getPenalty());
                    roundResult.setResult(deviceResult.getResult() + pair.getPenalty());
                    break;
                default:
                    throw new IllegalArgumentException("machine code not supported");
            }
            roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
            roundResult.setTestTime(testDate);

            if (results == null) {
                results = new ArrayList<>();
                TestCache.getInstance().getResults().put(student, results);
            }
            if (results.size() == 0) {
                roundResult.setRoundNo(1);
            } else {
                roundResult.setRoundNo(results.size() + 1);
            }

            if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN) {
                // 分组模式下,在一个分组只允许测试一次
                roundResult.setTestNo(1);
                roundResult.setGroupId(TestCache.getInstance().getGroup().getId());
                roundResult.setExamType(TestCache.getInstance().getGroup().getExamType());
                roundResult.setScheduleNo(TestCache.getInstance().getGroup().getScheduleNo());
            } else {
                StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
                roundResult.setExamType(studentItem.getExamType());
                roundResult.setTestNo(TestCache.getInstance().getTestNoMap().get(student));
                roundResult.setScheduleNo(studentItem.getScheduleNo());
            }

            // 重新判断最好成绩
            RoundResult bestResult = InteractUtils.getBestResult(results);
            // Log.i("james", "\nroundResult:" + roundResult.toString());
            if (bestResult != null && bestResult.getResult() > roundResult.getResult()) {
                roundResult.setIsLastResult(0);
                // Log.i("james", "bestResult" +  bestResult.toString());
            } else {
                roundResult.setIsLastResult(1);
                if (bestResult != null) {
                    bestResult.setIsLastResult(0);
                    DBManager.getInstance().updateRoundResult(bestResult);
                    Logger.i("更新成绩:" + bestResult.toString());
                }
            }
            results.add(roundResult);
            Logger.i("保存成绩:" + roundResult.toString());

            DBManager.getInstance().insertRoundResult(roundResult);
        }
        ToastUtils.showShort("成绩保存成功");
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

    public static void printResults(Group group, List<Student> students,
                                    Map<Student, List<RoundResult>> results,
                                    int testNo, Map<Student, Integer> trackNoMap) {
        Logger.i("成绩打印开始");
        String title;
        SystemSetting systemSetting = SettingHelper.getSystemSetting();
        boolean isGroupMode = systemSetting.getTestPattern() == SystemSetting.GROUP_PATTERN;
        String machineName = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode());
        machineName = InteractUtils.getStrWithLength(machineName, 8);
        if (isGroupMode) {
            title = String.format(Locale.CHINA, "%8s%d号机%d组",
                    machineName, systemSetting.getHostId(),
                    group.getGroupNo());
        } else {
            title = String.format(Locale.CHINA, "%8s%d号机", machineName, systemSetting.getHostId());
        }

        Date date = Calendar.getInstance().getTime();
        String printTime = TestConfigs.df.format(date);
        String printTimeLong = date.getTime() + "";

        PrinterManager.getInstance().print(title);
        String qualifiedUnit = ResultDisplayUtils.getQualifiedUnit(TestConfigs.sCurrentItem);
        String header = String.format("%-4s%-10s%-4s", "", "姓名", "成绩(" + qualifiedUnit + ")");
        PrinterManager.getInstance().print(header);
        // 获取所有的最好成绩 并打印
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            List<RoundResult> resultList = results.get(student);
            String studentName = getStrWithLength(student.getStudentName(), 10);
            Integer trackNo = trackNoMap.get(student);
            String line = null;
            if (resultList != null && resultList.size() > 0) {
                for (RoundResult roundResult : resultList) {
                    if (roundResult.getIsLastResult() == 1 && roundResult.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
                        line = isGroupMode
                                ? String.format(Locale.CHINA, "%-4d%-10s%-4s", trackNo, studentName,
                                ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult(), false))
                                : String.format(Locale.CHINA, "    %-10s%-4s", studentName,
                                ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult(), false));
                        break;
                    } else {
                        line = isGroupMode
                                ? String.format(Locale.CHINA, "%-4d%-10s%-4s", trackNo, studentName,
                                ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult(), false))
                                : String.format(Locale.CHINA, "    %-10s%-4s", studentName,
                                "X");
                    }
                }
            } else {
                line = isGroupMode
                        ? String.format(Locale.CHINA, "%-4d%-10s", trackNo, studentName)
                        : String.format(Locale.CHINA, "    %-10s%-4s", studentName, "");
            }
            PrinterManager.getInstance().print(line);
        }
        PrinterManager.getInstance().print(printTime + "\n");

        // 每个人的成绩,并打印
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            PrinterManager.getInstance().print(title);

            if (isGroupMode) {
                Integer trackNo = trackNoMap.get(student);
                // if (trackNo != null) {
                PrinterManager.getInstance().print("序号:" + trackNo);
                // }
            }

            PrinterManager.getInstance().print("考号:" + student.getStudentCode());
            PrinterManager.getInstance().print("姓名:" + student.getStudentName());

            List<RoundResult> resultList = results.get(student);
            if (resultList == null || resultList.size() == 0) {
                for (int k = 0; k < testNo; k++) {
                    PrinterManager.getInstance().print("第" + (k + 1) + "次:      /");
                }
            } else {
                for (int j = 0; j < resultList.size(); j++) {
                    RoundResult result = resultList.get(j);
//                    String printResult = "第" + (j + 1) + "次:" + (result.getResultState() == RoundResult.RESULT_STATE_NORMAL
//                            ? ResultDisplayUtils.getStrResultForDisplay(result.getResult(), false) : "X");
                    String printResult = "第" + result.getRoundNo() + "次:" + getPrintResultState(result);
                    // 跳绳需要打印绊绳次数
                    switch (TestConfigs.sCurrentItem.getMachineCode()) {
                        case ItemDefault.CODE_TS:
                            PrinterManager.getInstance().print(printResult + "(中断:" + result.getStumbleCount() + ")");
                            break;

                        case ItemDefault.CODE_YWQZ:
                        case ItemDefault.CODE_YTXS:
                        case ItemDefault.CODE_PQ:
                            PrinterManager.getInstance().print(printResult + "(判罚:" + result.getPenaltyNum() + ")");
                            break;
                        case ItemDefault.CODE_LQYQ:
                        case ItemDefault.CODE_ZQYQ:
                            PrinterManager.getInstance().print(printResult + "(违例:" + result.getPenaltyNum() + ")");
                            break;
                        default:
                            PrinterManager.getInstance().print(printResult);

                    }
                    result.setPrintTime(printTimeLong);
                }
                DBManager.getInstance().updateRoundResult(resultList);
            }
            PrinterManager.getInstance().print(printTime + "\n");
        }
        PrinterManager.getInstance().print("\n\n");
        Logger.i("成绩打印完成");
    }

    private static String getPrintResultState(RoundResult roundResult) {

        switch (roundResult.getResultState()) {
            case RoundResult.RESULT_STATE_NORMAL:
                return ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult(), false);
            case RoundResult.RESULT_STATE_FOUL:
                return "X";
            case RoundResult.RESULT_STATE_BACK:
                return roundResult.getResult() == 0 ? "中退" : ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult(), false) + "[中退]";
            case RoundResult.RESULT_STATE_WAIVE:
                return roundResult.getResult() == 0 ? "放弃" : ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult(), false) + "[放弃]";
            default:
                return "";
        }
    }

    /**
     * 将考生信息填入到ll_detail 中,ll_detail 必须为 {com.feipulai.exam.R.layout.layout_stu_info}
     *
     * @param llStuDetail 目的视图
     * @param student     考生信息
     * @param results     考生对应成绩信息
     */
    public static void showStuInfo(LinearLayout llStuDetail, Student student, List<RoundResult> results) {
        TextView mTvStudentName = (TextView) llStuDetail.findViewById(R.id.tv_studentName);
        TextView mTvStudentCode = (TextView) llStuDetail.findViewById(R.id.tv_studentCode);
        TextView mTvGender = (TextView) llStuDetail.findViewById(R.id.tv_gender);
        TextView mTvGrade = (TextView) llStuDetail.findViewById(R.id.tv_grade);

        mTvStudentCode.setText(student == null ? "" : student.getStudentCode());
        mTvStudentName.setText(student == null ? "" : student.getStudentName());
        mTvGender.setText(student == null ? "" : student.getSex() == 0 ? "男" : "女");

        if (results == null || results.size() == 0) {
            mTvGrade.setText("");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < results.size(); i++) {
                sb.append(InteractUtils.getDisplayResult(results.get(i)));
                sb.append("\n");
            }
            mTvGrade.setText(sb.toString());
        }
    }

    /**
     * 判罚后,更新当前考生的成绩
     *
     * @param results         当前考生的成绩
     * @param penalizedResult 被判罚过的成绩
     */
    // public static void updateResultsAfterPenalize(List<RoundResult> results, RoundResult penalizedResult) {
    // 	// 被判罚的成绩不是最好成绩, 或者  当前考生的成绩暂时只有一个(也就是这个被判罚的成绩)
    // 	// 不需要与其他的成绩作比较,只需要更新判罚的成绩的结果即可
    // 	if (penalizedResult.getIsLastResult() != RoundResult.LAST_RESULT
    // 			|| results.size() == 1) {
    // 		DBManager.getInstance().updateRoundResult(penalizedResult);
    // 		return;
    // 	}
    // 	RoundResult bestResult = penalizedResult;
    // 	for (RoundResult result : results) {
    // 		if (result.getResult() > bestResult.getResult()) {
    // 			bestResult = result;
    // 		}
    // 	}
    // 	if (bestResult != penalizedResult) {
    // 		bestResult.setIsLastResult(RoundResult.LAST_RESULT);
    // 		penalizedResult.setIsLastResult(RoundResult.NOT_LAST_RESULT);
    // 		DBManager.getInstance().updateRoundResult(bestResult);
    // 	}
    // 	// 不管怎样,判罚成绩都是要提交到数据库的
    // 	DBManager.getInstance().updateRoundResult(penalizedResult);
    // }

    // public static void resetLEDScreen(LEDManager ledManager) {
    //     int hostId = SettingHelper.getSystemSetting().getHostId();
    //     String title = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode())
    //             + " " + hostId;
    //     ledManager.showString(hostId, title, 0, true, false, LEDManager.MIDDLE);
    //     if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
    //         ledManager.showString(hostId, "请检录", 5, 1, false, false);
    //     }
    //     ledManager.showString(hostId, "菲普莱体育", 3, 3, false, true);
    //     // ledManager.resetLEDScreen(hostId, TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
    // }

    /**
     * 保证中文字符串的长度为指定长度
     *
     * @param chinese      中文字符串
     * @param targetLength 目的长度(中文长度)
     * @return 结果字符串
     */
    public static String getStrWithLength(String chinese, int targetLength) {
        String result;
        int currentLength = 0;
        targetLength *= 2;// 目的长度 为 中文长度
        StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0; i < chinese.length(); i++) {
            c = chinese.charAt(i);
            if (c >= 128) {// 汉字
                currentLength += 2;
            } else {// ascii
                currentLength++;
            }
            if (currentLength > targetLength) {
                break;
            }
            sb.append(c);
        }
        while (currentLength < targetLength) {
            sb.append(' ');
            currentLength++;
        }
        result = sb.toString();
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


}