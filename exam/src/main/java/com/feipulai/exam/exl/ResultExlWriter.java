package com.feipulai.exam.exl;

import android.text.TextUtils;

import com.feipulai.common.dbutils.FileSelectActivity;
import com.feipulai.common.dbutils.UsbFileAdapter;
import com.feipulai.common.exl.ExlListener;
import com.feipulai.common.exl.ExlWriter;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileOutputStream;
import com.orhanobut.logger.Logger;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by James on 2018/11/1 0001.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ResultExlWriter extends ExlWriter {

    int rowIndex;
    private int testCount;

    public ResultExlWriter(int testCount, ExlListener listener) {
        super(listener);
        this.testCount = testCount;
    }

    @Override
    protected void write(UsbFile file) {
        String[] headers = new String[12 + (testCount * 5)];
        String[] first = new String[]{"编号", "准考证号", "姓名", "性别", "学校名称", "班级", "项目", "日程", "组号", "考试状态", "备注", "决定成绩"};
        System.arraycopy(first, 0, headers, 0, first.length);
//        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_TS) {
//            headers = new String[]{"学籍号", "姓名", "性别", "项目", "成绩", "轮次", "测试时间", "成绩状态", "考试类型", "拌绳次数", "备注"};
//        } else {
//            headers = new String[]{"学籍号", "姓名", "性别", "项目", "成绩", "轮次", "测试时间", "成绩状态", "考试类型", "备注"};
//        }
        for (int i = 0; i < testCount; i++) {
            headers[11 + (i * 5) + 1] = "第" + (i + 1) + "轮成绩";
            headers[11 + (i * 5) + 2] = "第" + (i + 1) + "轮判罚值";
            headers[11 + (i * 5) + 3] = "第" + (i + 1) + "轮测试时间";
            headers[11 + (i * 5) + 4] = "第" + (i + 1) + "轮绊绳次数";
            headers[11 + (i * 5) + 5] = "第" + (i + 1) + "轮成绩状态";
        }

        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet("测试成绩");
        int width = 0;
        // 产生表格标题行,确定每个格子的长
        HSSFRow firstRow = sheet.createRow(0);
        for (short i = 0; i < headers.length; i++) {
            HSSFCell cell = firstRow.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(headers[i]);
            cell.setCellValue(text);
            // 自动列宽
            width = headers[i].getBytes().length;
            sheet.setColumnWidth(i, width * 500);
        }

        // 所有该项目的报名信息
        // 这里以报名信息开始,因为报名信息是每个报名信息 学生 机器码 项目代码 的组合是唯一的
//        List<StudentItem> studentItems = DBManager.getInstance()
//                .querystuItemsByMachineItemCode(TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.getCurrentItemCode());
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
            List<Item> itemList = DBManager.getInstance().queryItemsByMachineCode(ItemDefault.CODE_ZCP);
            for (Item item : itemList) {
                List<Student> studentList = DBManager.getInstance().getItemStudent
                        (item.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : item.getItemCode(), -1, 0);
                rowIndex = 1;

                generateRows(item.getItemName(), studentList, sheet);
            }
        } else {
            List<Student> studentList = DBManager.getInstance().getItemStudent(TestConfigs.getCurrentItemCode(), -1, 0);
            rowIndex = 1;

            generateRows(TestConfigs.sCurrentItem.getItemName(), studentList, sheet);
        }


//        // 身高体重,还需要生成体重成绩
//        if (TestConfigs.HEIGHT_ITEM_CODE.equals(TestConfigs.sCurrentItem.getItemCode())) {
//            studentItems = DBManager.getInstance()
//                    .querystuItemsByMachineItemCode(TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.WEIGHT_ITEM_CODE);
//
//            generateRows(studentItems, sheet);
//        }

        OutputStream fos;
        try {
            if (file instanceof UsbFileAdapter) {
                fos = new FileOutputStream(((UsbFileAdapter) file).getFile());
            } else {
                fos = new UsbFileOutputStream(file);
            }
            workbook.write(fos);
            workbook.close();
            fos.close();
            if (listener != null) {
                Logger.i(TestConfigs.df.format(new Date()) + "---> exel文件导出成功");
                Logger.i(TestConfigs.df.format(new Date()) + "--->保存路径：" + file);
                listener.onExlResponse(ExlListener.EXEL_WRITE_SUCCESS, "Excel成绩导出成功");
            }
            UsbFile deleteFile = FileSelectActivity.sSelectedFile.createFile("." + file.getName() + "delete.exl");
            deleteFile.delete();
        } catch (IOException e) {
            if (listener != null) {
                Logger.i(TestConfigs.df.format(new Date()) + "---> Excel文件导出失败,文件写入失败");
                listener.onExlResponse(ExlListener.EXEL_WRITE_FAILED, "Excel文件导出失败,文件写入失败");
            }
            e.printStackTrace();
        }

    }

    private void generateRows(String itemName, List<Student> studentList, HSSFSheet sheet) {

        List<Map<String, Object>> resultsList = DBManager.getInstance().getResultsByStu(studentList);
        int number = 1;
        for (int i = 0; i < resultsList.size(); i++) {
            Map<String, Object> dataMap = resultsList.get(i);
            List<UploadResults> uploadResults = (List<UploadResults>) dataMap.get("results");
            Student student = (Student) dataMap.get("stu");
            for (int j = 0; j < uploadResults.size(); j++) {
                HSSFRow row = sheet.createRow(rowIndex++);
                HSSFCell cell = row.createCell(0);
                cell.setCellValue(number);
                cell = row.createCell(1);
                cell.setCellValue(student.getStudentCode());
                cell = row.createCell(2);
                cell.setCellValue(student.getStudentName());
                cell = row.createCell(3);
                cell.setCellValue(student.getSex() == Student.MALE ? "男" : "女");
                cell = row.createCell(4);
                cell.setCellValue(student.getSchoolName());
                cell = row.createCell(5);
                cell.setCellValue(student.getClassName());
                cell = row.createCell(6);
                cell.setCellValue(itemName);
                if (TextUtils.isEmpty(uploadResults.get(j).getSiteScheduleNo())) {
                    cell = row.createCell(7);
                    cell.setCellValue("");
                    cell = row.createCell(8);
                    cell.setCellValue("");
                } else {
                    Schedule schedule = DBManager.getInstance().getSchedulesByNo(uploadResults.get(j).getSiteScheduleNo());
                    cell = row.createCell(7);
                    cell.setCellValue(DateUtil.formatTime(Long.valueOf(schedule.getBeginTime()), "yyyy-MM-dd HH:mm:ss"));
                    cell = row.createCell(8);
                    cell.setCellValue(uploadResults.get(j).getGroupNo());
                }
                List<RoundResultBean> roundResultBeans = uploadResults.get(j).getRoundResultList();
                cell = row.createCell(9);
                cell.setCellValue(roundResultBeans.get(0).getExamState() == 0 ? "正常" : "补考");
                for (int k = 0; k < roundResultBeans.size(); k++) {
                    if (roundResultBeans.get(k).getResultType() == 1) {
                        cell = row.createCell(11);
                        cell.setCellValue(ResultDisplayUtils.getStrResultForDisplay(roundResultBeans.get(k).getResult()));
                    }
                    cell = row.createCell(11 + (k * 5) + 1);
                    cell.setCellValue(ResultDisplayUtils.getStrResultForDisplay(roundResultBeans.get(k).getResult()));
                    cell = row.createCell(11 + (k * 5) + 2);
                    cell.setCellValue(roundResultBeans.get(k).getPenalty());
                    cell = row.createCell(11 + (k * 5) + 3);
                    cell.setCellValue(DateUtil.formatTime(Long.valueOf(roundResultBeans.get(k).getTestTime()), "yyyy-MM-dd HH:mm:ss"));
                    cell = row.createCell(11 + (k * 5) + 4);
                    cell.setCellValue(roundResultBeans.get(k).getStumbleCount());
                    cell = row.createCell(11 + (k * 5) + 5);
                    cell.setCellValue(roundResultBeans.get(k).getIsFoul() == 0 ? "正常" : "犯规");
                }
                number++;
            }
        }

//        List<RoundResult> roundResults;
//
//        for (Student student : studentList) {
//            roundResults = DBManager.getInstance().queryResultsByStuItemExamType(student.getStudentCode());
//            for (RoundResult result : roundResults) {
//                HSSFRow row = sheet.createRow(rowIndex++);
//                HSSFCell cell = row.createCell(0);
//                cell.setCellValue(student.getStudentCode());
//                cell = row.createCell(1);
//                cell.setCellValue(student.getStudentName());
//                cell = row.createCell(2);
//                cell.setCellValue(student.getSex() == Student.MALE ? "男" : "女");
//                cell = row.createCell(3);
//                cell.setCellValue(itemName);
//                cell = row.createCell(4);
//                cell.setCellValue(result.getResult());
//                cell = row.createCell(5);
//                cell.setCellValue(result.getRoundNo());
//                cell = row.createCell(6);
//                cell.setCellValue(result.getTestTime());
//                cell = row.createCell(7);
//                cell.setCellValue(result.getResultState() == RoundResult.RESULT_STATE_NORMAL ? "正常" : "犯规");
//                cell = row.createCell(8);
//                cell.setCellValue(result.getExamType() == 0 ? "正常" : (result.getExamType() == 2 ? "补考" : "缓考"));
//                if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_TS) {
//                    cell = row.createCell(9);
//                    cell.setCellValue(result.getStumbleCount());
//                }
//
//            }
//
//        }
    }

}
