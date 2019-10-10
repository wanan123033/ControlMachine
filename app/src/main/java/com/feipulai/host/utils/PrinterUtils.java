package com.feipulai.host.utils;

import android.content.Context;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.host.activity.height_weight.HWConfigs;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;

import java.util.Calendar;

/**
 * Created by James on 2019/2/14 0014.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class PrinterUtils {


    public static void printResult(Context context, Student student, RoundResult result) {
        printResult(context, student, result, null);
    }

    /**
     * 打印成绩信息
     *
     * @param context
     * @param student      考生信息
     * @param result       成绩信息
     * @param weightResult 体重成绩信息,仅身高体重项目有,其余项目传入null
     */
    public static void printResult(Context context, Student student, RoundResult result, RoundResult weightResult) {
        int machineCode = TestConfigs.sCurrentItem.getMachineCode();
        if (machineCode == ItemDefault.CODE_HW && weightResult == null) {
            throw new IllegalArgumentException("height weight item must have weight result");
        }

        String printTime = TestConfigs.df.format(Calendar.getInstance().getTime());
        int hostId = SettingHelper.getSystemSetting().getHostId();

        PrinterManager.getInstance().print(" \n");
        PrinterManager.getInstance().print(TestConfigs.machineNameMap.get(machineCode) + hostId + "号机\n");
        PrinterManager.getInstance().print("考  号:" + student.getStudentCode() + "\n");
        PrinterManager.getInstance().print("姓  名:" + student.getStudentName() + "\n");


        result.setPrintTime(printTime);

        DBManager.getInstance().updateRoundResult(result);


        switch (machineCode) {
            case ItemDefault.CODE_HW:
                String heightResult = ResultDisplayUtils.getStrResultForDisplay(result.getResult(), HWConfigs.HEIGHT_ITEM);
                PrinterManager.getInstance().print("身  高:" + heightResult);
                PrinterManager.getInstance().print("体  重:" + ResultDisplayUtils.getStrResultForDisplay(weightResult.getResult(), HWConfigs.WEIGHT_ITEM));
                weightResult.setPrintTime(printTime);
                DBManager.getInstance().updateRoundResult(weightResult);
                break;

            default:
                String displayResult = ResultDisplayUtils.getStrResultForDisplay(result.getResult());
                PrinterManager.getInstance().print("成  绩:" + displayResult);
                break;
        }

        PrinterManager.getInstance().print("打印时间:" + printTime + "\n");
        PrinterManager.getInstance().print(" \n");
    }

}
