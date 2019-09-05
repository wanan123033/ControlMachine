package com.feipulai.host.exl;

import com.feipulai.common.exl.ExlListener;
import com.feipulai.common.exl.ExlWriter;
import com.feipulai.common.exl.ExlWriterUtil;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.entity.StudentItem;
import com.github.mjdev.libaums.fs.UsbFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by James on 2018/11/1 0001.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ResultExlWriter extends ExlWriter {

    int rowIndex;
    private List<List<String>> writeData = new ArrayList<>();

    public ResultExlWriter(ExlListener listener) {
        super(listener);
    }

    @Override
    protected void write(UsbFile file) {
        String[] headers = {"学籍号", "姓名", "性别", "项目", "成绩", "轮次", "测试时间", "成绩状态", "备注"};

        writeData.add(Arrays.asList(headers));

        // 所有该项目的报名信息
        // 这里以报名信息开始,因为报名信息是每个报名信息 学生 机器码 项目代码 的组合是唯一的
        List<StudentItem> studentItems = DBManager.getInstance()
                .querystuItemsByMachineItemCode(TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.getCurrentItemCode());


        generateRows(studentItems);
        new ExlWriterUtil.Builder(file).setWriteData(writeData).setExlListener(listener).setSheetname("测试成绩").build().write();
        // 身高体重,还需要生成体重成绩
        // TODO: 2019/3/12 身高体重导出成绩处理方式待定
        // if(TestConfigs.HEIGHT_ITEM_CODE.equals(TestConfigs.sCurrentItem.getItemCode())){
        // 	studentItems = DBManager.getInstance()
        // 			.querystuItemsByMachineItemCode(TestConfigs.sCurrentItem.getMachineCode(),TestConfigs.WEIGHT_ITEM_CODE);
        // 	generateRows(studentItems,sheet);
        // }
    }

    private void generateRows(List<StudentItem> studentItems) {
        List<RoundResult> roundResults;
        Student student;
        String itemName = TestConfigs.sCurrentItem.getItemName();

        for (StudentItem stuItem : studentItems) {

            roundResults = DBManager.getInstance().queryResultsByStuItem(stuItem);
            student = DBManager.getInstance().queryStudentByStuCode(stuItem.getStudentCode());

            for (RoundResult result : roundResults) {
                String[] rowData = new String[8];
                rowData[0] = student.getStudentCode();
                rowData[1] = student.getStudentName();
                rowData[2] = student.getSex() == Student.MALE ? "男" : "女";
                rowData[3] = itemName;
                rowData[4] = result.getResult() + "";
                rowData[5] = result.getRoundNo() + "";
                rowData[6] = result.getTestTime();
                rowData[7] = result.getResultState() == RoundResult.RESULT_STATE_NORMAL ? "正常" : "犯规";
                writeData.add(Arrays.asList(rowData));
            }
        }
    }

}
