package com.feipulai.exam.activity.MiddleDistanceRace;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.widget.TextView;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.GroupItemBean;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.GroupPrintBean;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.RaceResultBean;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * created by ww on 2019/7/9.
 */
public class MiddlePrintUtil {

    /**
     * 计时结束自动打印
     *
     * @param roundResults
     * @param raceResultBean2s
     * @param digital
     * @param carryMode
     */
    public static void print(List<RoundResult> roundResults, List<RaceResultBean> raceResultBean2s, int digital, int carryMode) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        if (raceResultBean2s.size() < 1) {
            return;
        }

        Date date = Calendar.getInstance().getTime();
        String printTime = TestConfigs.df.format(date);
        String printTimeLong = date.getTime() + "";

        String itemName = InteractUtils.getStrWithLength(raceResultBean2s.get(0).getItemName(), 8);

        String title = String.format(Locale.CHINA, "%8s%d号机%d组",
                itemName, SettingHelper.getSystemSetting().getHostId(),
                Integer.parseInt(raceResultBean2s.get(0).getNo()));
        PrinterManager.getInstance().print(title);
        String header = String.format("%-4s%-10s%-4s", "", "姓名", "成绩(分秒)");
        PrinterManager.getInstance().print(header);

        List<GroupPrintBean> totalResult = new ArrayList<>();

        for (RaceResultBean resultBean : raceResultBean2s
        ) {
            int lastResult = Integer.parseInt(TextUtils.isEmpty(resultBean.getResults()[2]) ? "0" : resultBean.getResults()[2]);
            String lastResultText;
            if (carryMode == 0) {
                lastResultText = DateUtil.caculateTime(lastResult, 3, carryMode);
            } else {
                lastResultText = DateUtil.caculateTime(lastResult, digital, carryMode);
            }
            switch (resultBean.getResultState()) {
                case 1:
                    break;
                case 2:
                    lastResultText = "DQ";
                    break;
                case 3:
                    lastResultText = "DNF";
                    break;
                case 4:
                    lastResultText = "DNS";
                    break;
                case 5:
                    lastResultText = "DT";
                    break;
            }
            String line = String.format(Locale.CHINA, "%-4d%-10s%-4s", Integer.parseInt(resultBean.getResults()[0]), resultBean.getStudentName(),
                    lastResultText);
            PrinterManager.getInstance().print(line);
            totalResult.add(new GroupPrintBean(Integer.parseInt(resultBean.getResults()[0]), resultBean.getStudentCode(), resultBean.getStudentName(), lastResultText, resultBean.getResultState()));
        }
        PrinterManager.getInstance().print(printTime + "\n");

        for (GroupPrintBean groupPrintBean : totalResult
        ) {
            PrinterManager.getInstance().print(title);
            PrinterManager.getInstance().print("道次:" + groupPrintBean.getTrackNo());
            PrinterManager.getInstance().print("考  号: " + groupPrintBean.getStudentCode());
            PrinterManager.getInstance().print("姓  名: " + groupPrintBean.getStudentName());

            String lastResult = "";
            switch (groupPrintBean.getResultState()) {
                case 1:
                    lastResult = groupPrintBean.getLastResultString();
                    break;
                case 2:
                    lastResult = "DQ";
                    break;
                case 3:
                    lastResult = "DNF";
                    break;
                case 4:
                    lastResult = "DNS";
                    break;
                case 5:
                    lastResult = "DT";
                    break;
            }
            PrinterManager.getInstance().print("成  绩: " + lastResult);
            PrinterManager.getInstance().print(printTime + "\n");
        }
        //更新数据库中打印时间
        for (RoundResult roundresult : roundResults
        ) {
            roundresult.setPrintTime(printTimeLong);
        }
        DBManager.getInstance().updateRoundResults(roundResults);
    }

    /**
     * 计时界面查询打印
     *
     * @param groupItemBean
     * @param digital
     * @param carryMode
     */
    public static void print2(Context context, GroupItemBean groupItemBean, int digital, int carryMode) {
        if (SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_A4) {
            InteractUtils.printA4Result(context, groupItemBean.getGroup());
            return;
        }
        Date date = Calendar.getInstance().getTime();
        String printTime = TestConfigs.df.format(date);
        String printTimeLong = date.getTime() + "";

        String itemName = InteractUtils.getStrWithLength(groupItemBean.getItemName(), 8);
        String title = String.format(Locale.CHINA, "%8s%d号机%d组",
                itemName, SettingHelper.getSystemSetting().getHostId(),
                groupItemBean.getGroup().getGroupNo());
        PrinterManager.getInstance().print(title);
        String header = String.format("%-4s%-10s%-4s", "", "姓名", "成绩(分秒)");
        PrinterManager.getInstance().print(header);

        List<GroupItem> groups = groupItemBean.getGroupItems();
        //按道次排序
        Collections.sort(groups, new Comparator<GroupItem>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public int compare(GroupItem o1, GroupItem o2) {
                return Integer.compare(o1.getTrackNo(), o2.getTrackNo());
            }
        });

        List<GroupPrintBean> totalResult = new ArrayList<>();
        List<RoundResult> roundResults = new ArrayList<>();
        for (GroupItem groupItem : groups
        ) {
            Student student = DBManager.getInstance().queryStudentByStuCode(groupItem.getStudentCode());
            RoundResult result = DBManager.getInstance().queryResultByStudentCode(groupItem.getStudentCode(), groupItem.getItemCode(), groupItemBean.getGroup().getId());

            String lastResultText;
            if (carryMode == 0) {
                lastResultText = DateUtil.caculateTime(result.getResult(), 3, carryMode);
            } else {
                lastResultText = DateUtil.caculateTime(result.getResult(), digital, carryMode);
            }
            switch (result.getResultState()) {
                case 1:
                    break;
                case 2:
                    lastResultText = "DQ";
                    break;
                case 3:
                    lastResultText = "DNF";
                    break;
                case 4:
                    lastResultText = "DNS";
                    break;
                case 5:
                    lastResultText = "DT";
                    break;
            }
//            String lastResultText = DateUtil.caculateTime(result.getResult(), digital, carryMode);
            String line = String.format(Locale.CHINA, "%-4d%-10s%-4s", groupItem.getTrackNo(), student.getStudentName(),
                    lastResultText);
            PrinterManager.getInstance().print(line);
            roundResults.add(result);
            totalResult.add(new GroupPrintBean(groupItem.getTrackNo(), student.getStudentCode(), student.getStudentName(), lastResultText, result.getResultState()));
        }

        PrinterManager.getInstance().print(printTime + "\n");

        for (GroupPrintBean groupPrintBean : totalResult
        ) {
            PrinterManager.getInstance().print(title);
            PrinterManager.getInstance().print("道次:" + groupPrintBean.getTrackNo());
            PrinterManager.getInstance().print("考  号: " + groupPrintBean.getStudentCode());
            PrinterManager.getInstance().print("姓  名: " + groupPrintBean.getStudentName());
//            PrinterManager.getInstance().print("成  绩: " + groupPrintBean.getLastResultString());
            String lastResult = "";
            switch (groupPrintBean.getResultState()) {
                case 1:
                    lastResult = groupPrintBean.getLastResultString();
                    break;
                case 2:
                    lastResult = "DQ";
                    break;
                case 3:
                    lastResult = "DNF";
                    break;
                case 4:
                    lastResult = "DNS";
                    break;
                case 5:
                    lastResult = "DT";
                    break;
            }
            PrinterManager.getInstance().print("成  绩: " + lastResult);
            PrinterManager.getInstance().print(printTime + "\n");
        }

        //更新数据库中打印时间
        for (RoundResult roundresult : roundResults
        ) {
            roundresult.setPrintTime(printTimeLong);
        }
        DBManager.getInstance().updateRoundResults(roundResults);
    }
}
