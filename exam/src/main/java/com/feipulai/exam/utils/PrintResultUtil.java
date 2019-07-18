package com.feipulai.exam.utils;

import android.text.TextUtils;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;

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
        //获取分类的成绩
        List<UploadResults> stuResultsList = DBManager.getInstance().getUploadResultsByStuCode(TestConfigs.getCurrentItemCode(), stuCodeList);
        for (UploadResults uploadResults : stuResultsList) {
            //个人考试次数打印
            if (!TextUtils.isEmpty(uploadResults.getGroupNo())) {
                Group group = DBManager.getInstance().queryGroupById(uploadResults.getGroupId());
                PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "号机  " + group.getGroupNo() + "组");
                GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, uploadResults.getStudentCode());
                PrinterManager.getInstance().print("序  号:" + groupItem.getTrackNo() + "");
            } else {
                PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "号机");
            }
            Student student = DBManager.getInstance().queryStudentByStuCode(uploadResults.getStudentCode());
            PrinterManager.getInstance().print("考  号:" + uploadResults.getStudentCode());
            PrinterManager.getInstance().print("姓  名:" + student.getStudentName());
            List<RoundResultBean> printResultList = uploadResults.getRoundResultList();
            for (int i = 0; i < printResultList.size(); i++) {

                RoundResultBean result = printResultList.get(i);
                String printResult = "第" + result.getRoundNo() + "次:" + getPrintResultState(result);
                // 跳绳需要打印绊绳次数
                switch (TestConfigs.sCurrentItem.getMachineCode()) {
                    case ItemDefault.CODE_TS:
                        PrinterManager.getInstance().print(printResult + "(中断:" + result.getStumbleCount() + ")");
                        break;

                    case ItemDefault.CODE_YWQZ:
                    case ItemDefault.CODE_YTXS:
                    case ItemDefault.CODE_PQ:
                        PrinterManager.getInstance().print(printResult + "(判罚:" + result.getPenalty() + ")");
                        break;
                    case ItemDefault.CODE_LQYQ:
                    case ItemDefault.CODE_ZQYQ:
                        PrinterManager.getInstance().print(printResult + "(违例:" + result.getPenalty() + ")");
                        break;
                    default:
                        PrinterManager.getInstance().print(printResult);

                }
            }
            PrinterManager.getInstance().print("打印时间:" + TestConfigs.df.format(Calendar.getInstance().getTime()) + "\n");
            PrinterManager.getInstance().print("\n");


        }
    }

    private static String getPrintResultState(RoundResultBean roundResult) {

        switch (roundResult.getIsFoul()) {
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

}
