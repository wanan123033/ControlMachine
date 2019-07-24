package com.feipulai.exam.activity.MiddleDistanceRace;

import android.text.TextUtils;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.GroupItemBean;
import com.feipulai.exam.activity.MiddleDistanceRace.bean.RaceResultBean;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.utils.ResultDisplayUtils;

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

    public static void print(List<RoundResult> roundResults, List<RaceResultBean> raceResultBean2s, int digital, int carryMode) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;

        Date date = Calendar.getInstance().getTime();
        String printTime = TestConfigs.df.format(date);
        String printTimeLong = date.getTime() + "";

        String title = String.format(Locale.CHINA, "%8s%d号机%d组",
                raceResultBean2s.get(1).getItemName(), SettingHelper.getSystemSetting().getHostId(),
                Integer.parseInt(raceResultBean2s.get(1).getNo()));
        PrinterManager.getInstance().print(title);
        String header = String.format("%-4s%-10s%-4s", "", "姓名", "成绩(分秒)");
        PrinterManager.getInstance().print(header);
        for (int i = 1; i < raceResultBean2s.size(); i++) {//第一行为标题，此处从1开始
            int lastResult = Integer.parseInt(TextUtils.isEmpty(raceResultBean2s.get(i).getResults()[2]) ? "0" : raceResultBean2s.get(i).getResults()[2]);
            String lastResultText = DateUtil.caculateTime(lastResult, digital + 1, carryMode + 1);
            String line = String.format(Locale.CHINA, "%-4d%-10s%-4s", Integer.parseInt(raceResultBean2s.get(i).getResults()[0]), raceResultBean2s.get(i).getStudentName(),
                    lastResultText);
            PrinterManager.getInstance().print(line);
        }
        PrinterManager.getInstance().print(printTime + "\n");

        for (int i = 1; i < raceResultBean2s.size(); i++) {//第一行为标题，此处从1开始
            PrinterManager.getInstance().print(title);
            PrinterManager.getInstance().print("道次:" + raceResultBean2s.get(i).getResults()[0]);
            PrinterManager.getInstance().print("考  号: " + raceResultBean2s.get(i).getStudentCode());
            PrinterManager.getInstance().print("姓  名: " + raceResultBean2s.get(i).getStudentName());
            int lastResult = Integer.parseInt(TextUtils.isEmpty(raceResultBean2s.get(i).getResults()[2]) ? "0" : raceResultBean2s.get(i).getResults()[2]);
            PrinterManager.getInstance().print("成  绩: " + DateUtil.caculateTime(lastResult, digital + 1, carryMode + 1));
            PrinterManager.getInstance().print(printTime + "\n");
        }

        for (RoundResult roundresult : roundResults
                ) {
            roundresult.setPrintTime(printTimeLong);
        }
        DBManager.getInstance().updateRoundResults(roundResults);//更新数据库中打印时间
    }

    public static void print2(GroupItemBean groupItemBean, int digital, int carryMode) {
        Date date = Calendar.getInstance().getTime();
        String printTime = TestConfigs.df.format(date);

        String title = String.format(Locale.CHINA, "%8s%d号机%d组",
                groupItemBean.getItemName(), SettingHelper.getSystemSetting().getHostId(),
                groupItemBean.getGroup().getGroupNo());
        PrinterManager.getInstance().print(title);
        String header = String.format("%-4s%-10s%-4s", "", "姓名", "成绩(分秒)");
        PrinterManager.getInstance().print(header);

        List<GroupItem> groups = groupItemBean.getGroupItems();
        Collections.sort(groups, new Comparator<GroupItem>() {
            @Override
            public int compare(GroupItem o1, GroupItem o2) {
                return ((Integer) o1.getTrackNo()).compareTo(o2.getTrackNo());//按照道次升序排列
            }
        });

        for (GroupItem groupItem : groups
                ) {
            Student student = DBManager.getInstance().queryStudentByStuCode(groupItem.getStudentCode());
            RoundResult result = DBManager.getInstance().queryResultByStudentCode(groupItem.getStudentCode(), groupItem.getItemCode());
            String lastResultText = DateUtil.caculateTime(result.getResult(), digital + 1, carryMode + 1);
            String line = String.format(Locale.CHINA, "%-4d%-10s%-4s", groupItem.getTrackNo(), student.getStudentName(),
                    lastResultText);
            PrinterManager.getInstance().print(line);
        }

        PrinterManager.getInstance().print(printTime + "\n");

        for (GroupItem groupItem : groups
                ) {
            Student student = DBManager.getInstance().queryStudentByStuCode(groupItem.getStudentCode());
            RoundResult result = DBManager.getInstance().queryResultByStudentCode(groupItem.getStudentCode(), groupItem.getItemCode());
            PrinterManager.getInstance().print(title);
            PrinterManager.getInstance().print("道次:" + groupItem.getTrackNo());
            PrinterManager.getInstance().print("考  号: " + student.getStudentCode());
            PrinterManager.getInstance().print("姓  名: " + student.getStudentName());
            PrinterManager.getInstance().print("成  绩: " + DateUtil.caculateTime(result.getResult(), digital + 1, carryMode + 1));
            PrinterManager.getInstance().print(printTime + "\n");
        }
    }
}
