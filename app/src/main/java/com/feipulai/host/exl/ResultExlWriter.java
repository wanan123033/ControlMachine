package com.feipulai.host.exl;

import android.database.Cursor;

import com.feipulai.common.exl.ExlListener;
import com.feipulai.common.exl.ExlWriter;
import com.feipulai.common.exl.ExlWriterUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.height_weight.HWConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.RoundResultDao;
import com.feipulai.host.entity.Student;
import com.feipulai.host.entity.StudentItem;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.github.mjdev.libaums.fs.UsbFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by James on 2018/11/1 0001.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ResultExlWriter extends ExlWriter {

    private List<List<String>> writeData = new ArrayList<>();

    public ResultExlWriter(ExlListener listener) {
        super(listener);
    }

    @Override
    protected void write(UsbFile file) {
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_HW) {
            String[] headers = {"学籍号", "姓名", "性别", "项目", "身高", "体重"};
            writeData.add(Arrays.asList(headers));
        } else {
            String[] headers = {"学籍号", "姓名", "性别", "项目", "成绩"};
            writeData.add(Arrays.asList(headers));
        }
        // 所有该项目的报名信息
        // 这里以报名信息开始,因为报名信息是每个报名信息 学生 机器码 项目代码 的组合是唯一的
//        List<StudentItem> studentItems = DBManager.getInstance()
//                .querystuItemsByMachineItemCode(TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.getCurrentItemCode());
        List<String> stuCodeList = DBManager.getInstance().getResultsStudentByItem(TestConfigs.getCurrentItemCode());
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_HW) {
//            generateRowsHW(studentItems);
            generateRowsHW(stuCodeList);
        } else {
//            generateRows(studentItems);
            generateRows(stuCodeList);
        }
        new ExlWriterUtil.Builder(file).setWriteData(writeData).setExlListener(listener).setSheetname("测试成绩").build().write();

    }

    //    private void generateRows(List<StudentItem> studentItems) {
    private void generateRows(List<String> studentItems) {
        List<RoundResult> roundResults;
        Student student;
        String itemName = TestConfigs.sCurrentItem.getItemName();
//        for (StudentItem stuItem : studentItems) {
        for (String stuCode : studentItems) {

            roundResults = DBManager.getInstance().queryResultsByStudentCode(stuCode);
            student = DBManager.getInstance().queryStudentByStuCode(stuCode);

            for (RoundResult result : roundResults) {
                String[] rowData = new String[5];
                rowData[0] = student.getStudentCode();
                rowData[1] = student.getStudentName();
                rowData[2] = student.getSex() == Student.MALE ? MyApplication.getInstance().getString(R.string.male) :
                        MyApplication.getInstance().getString(R.string.female);
                rowData[3] = itemName;
                if (result.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                    rowData[4] = "0";
                } else {
                    rowData[4] = ResultDisplayUtils.getStrResultForDisplay(result.getResult(), false);
                }

                writeData.add(Arrays.asList(rowData));
            }
        }
    }

    private Comparator<RoundResult> roundResultComparator = Collections.reverseOrder(new Comparator<RoundResult>() {
        @Override
        public int compare(RoundResult lhs, RoundResult rhs) {
            return lhs.getTestTime().compareTo(rhs.getTestTime());
        }
    });

    //    private void generateRowsHW(List<StudentItem> studentItems) {
    private void generateRowsHW(List<String> studentItems) {

        Student student;
        String itemName = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode());

        for (String stuCode : studentItems) {

            student = DBManager.getInstance().queryStudentByStuCode(stuCode);

            List<RoundResult> heightResults = DBManager.getInstance().queryResultsByStudentCode(stuCode, HWConfigs
                    .HEIGHT_ITEM);
            List<RoundResult> weightResults = DBManager.getInstance().queryResultsByStudentCode(stuCode, HWConfigs
                    .WEIGHT_ITEM);
            Collections.sort(heightResults, roundResultComparator);
            Collections.sort(weightResults, roundResultComparator);
            for (int i = 0; i < heightResults.size(); i++) {
                String[] rowData = new String[6];
                rowData[0] = student.getStudentCode();
                rowData[1] = student.getStudentName();
                rowData[2] = student.getSex() == Student.MALE ? MyApplication.getInstance().getString(R.string.male) :
                        MyApplication.getInstance().getString(R.string.female);
                rowData[3] = itemName;
                if (heightResults.get(i).getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                    rowData[4] = "0";
                    rowData[5] = "0";
                } else {
                    rowData[4] = ResultDisplayUtils.getStrResultForDisplay(heightResults.get(i).getResult(), HWConfigs.HEIGHT_ITEM, false);
                    rowData[5] = ResultDisplayUtils.getStrResultForDisplay(weightResults.get(i).getResult(), HWConfigs.WEIGHT_ITEM, false);
                }

                writeData.add(Arrays.asList(rowData));
            }
        }
    }
}
