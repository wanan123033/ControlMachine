package com.feipulai.host.activity.data;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.height_weight.HWConfigs;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.utils.ResultDisplayUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by zzs on  2019/7/11
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PrintResultUtil {
    public static void printResult(String stuCode) {
        if (stuCode == null) {
            return;
        }
        List<String> list = new ArrayList<>();
        list.add(stuCode);
        printResult(list);
    }

    public static void printResult(List<String> stuCodeList) {

        for (String s : stuCodeList) {
            //如果测试过,显示成绩
            RoundResult roundResults = DBManager.getInstance().queryBestScore(s);

            PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "号机");
            Student student = DBManager.getInstance().queryStudentByStuCode(s);
            PrinterManager.getInstance().print("考  号:" + s);
            PrinterManager.getInstance().print("姓  名:" + student.getStudentName());
//            for (int i = 0; i < roundResults.size(); i++) {
//                RoundResult result = roundResults.get(i);
            String printResult = String.format(MyApplication.getInstance().getString(R.string.print_result_stu_result), getPrintResultState(roundResults));
            // 跳绳需要打印绊绳次数
            switch (TestConfigs.sCurrentItem.getMachineCode()) {
                case ItemDefault.CODE_TS:
                    PrinterManager.getInstance().print(printResult + "(中断:" + roundResults.getStumbleCount() + ")");
                    break;

                case ItemDefault.CODE_YWQZ:
                case ItemDefault.CODE_YTXS:
                case ItemDefault.CODE_PQ:
//                        PrinterManager.getInstance().print(printResult + "(判罚:" + result.getPenalty() + ")");
                    break;
                case ItemDefault.CODE_LQYQ:
                case ItemDefault.CODE_ZQYQ:
//                        PrinterManager.getInstance().print(printResult + "(违例:" + result.getPenalty() + ")");
                    break;
                case ItemDefault.CODE_HW:
                    PrinterManager.getInstance().print(printResult);
                    PrinterManager.getInstance().print("身  高:" + ResultDisplayUtils.getStrResultForDisplay(roundResults.getResult(), HWConfigs.HEIGHT_ITEM));
                    PrinterManager.getInstance().print("体  重:" + ResultDisplayUtils.getStrResultForDisplay(roundResults.getWeightResult(), HWConfigs.WEIGHT_ITEM));
                    break;
                default:
                    PrinterManager.getInstance().print(printResult);
            }
//            }
            PrinterManager.getInstance().print("打印时间:" + TestConfigs.df.format(Calendar.getInstance().getTime()) + "\n");
            PrinterManager.getInstance().print("\n");


        }


    }


    private static String getPrintResultState(RoundResult roundResult) {

        switch (roundResult.getResultState()) {
            case RoundResult.RESULT_STATE_NORMAL:
                if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_HW) {
                    return "";
                } else {
                    return ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult(), false);
                }

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

}
