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

import java.util.Calendar;
import java.util.List;

/**
 * created by ww on 2019/7/9.
 */
public class MiddlePrintUtil {

    public static void print(List<RaceResultBean> raceResultBean2s) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        for (int i = 1; i < raceResultBean2s.size(); i++) {//第一行为标题，此处从1开始
            PrinterManager.getInstance().print(" \n");
            PrinterManager.getInstance().print("考  号: " + raceResultBean2s.get(i).getStudentCode());
            PrinterManager.getInstance().print("姓  名: " + raceResultBean2s.get(i).getStudentName());
            PrinterManager.getInstance().print("项  目: " + raceResultBean2s.get(i).getItemName());
            int lastResult = Integer.parseInt(TextUtils.isEmpty(raceResultBean2s.get(i).getResults()[2]) ? "0" : raceResultBean2s.get(i).getResults()[2]);
            PrinterManager.getInstance().print("成  绩: " + DateUtil.getDeltaT(lastResult));
            PrinterManager.getInstance().print("打印时间:" + TestConfigs.df.format(Calendar.getInstance().getTime()));
            PrinterManager.getInstance().print(" \n");
        }
    }

    public static void print2(GroupItemBean groupItemBean) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        for (GroupItem groupItem : groupItemBean.getGroupItems()
                ) {
            Student student = DBManager.getInstance().queryStudentByStuCode(groupItem.getStudentCode());
            RoundResult result = DBManager.getInstance().queryResultByStudentCode(groupItem.getStudentCode(), groupItem.getItemCode());
            PrinterManager.getInstance().print(" \n");
            PrinterManager.getInstance().print("考  号: " + result.getStudentCode());
            PrinterManager.getInstance().print("姓  名: " + student.getStudentName());
            PrinterManager.getInstance().print("项  目: " + groupItemBean.getItemName());
            PrinterManager.getInstance().print("成  绩: " + DateUtil.getDeltaT(result.getResult()));
            PrinterManager.getInstance().print("打印时间:" + TestConfigs.df.format(Calendar.getInstance().getTime()));
            PrinterManager.getInstance().print(" \n");
        }
    }
}
