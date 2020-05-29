package com.feipulai.exam.exl;

import android.text.TextUtils;

import com.feipulai.common.exl.ExlListener;
import com.feipulai.common.exl.ExlWriter;
import com.feipulai.common.exl.ExlWriterUtil;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by zzs on  2019/8/27
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ResultExlWriter extends ExlWriter {
    private int testCount;
    private List<List<String>> writeData = new ArrayList<>();

    public ResultExlWriter(int testCount, ExlListener listener) {
        super(listener);
        this.testCount = testCount;
    }

    @Override
    protected void write(UsbFile file) {
        writeData.clear();
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
        List<String> headerList = Arrays.asList(headers);
        writeData.add(headerList);
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
            List<Item> itemList = DBManager.getInstance().queryItemsByMachineCode(ItemDefault.CODE_ZCP);
            for (Item item : itemList) {
                List<Student> studentList = DBManager.getInstance().getItemStudent
                        (item.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : item.getItemCode(), -1, 0);

                generateRows(item, studentList);
            }
        } else {
            List<Student> studentList = DBManager.getInstance().getItemStudent(TestConfigs.getCurrentItemCode(), -1, 0);

            generateRows(TestConfigs.sCurrentItem, studentList);
        }
        new ExlWriterUtil.Builder(file).setSheetname("测试成绩").setExlListener(listener).setWriteData(writeData).build().write();
    }

    private void generateRows(Item item, List<Student> studentList) {

        List<Map<String, Object>> resultsList = DBManager.getInstance().getResultsByStu(item.getItemCode(), studentList);
        if (resultsList == null) {
            return;
        }
        int number = 1;
        for (int i = 0; i < resultsList.size(); i++) {
            Map<String, Object> dataMap = resultsList.get(i);
            List<UploadResults> uploadResults = (List<UploadResults>) dataMap.get("results");
            Student student = (Student) dataMap.get("stu");

            for (int j = 0; j < uploadResults.size(); j++) {
                String[] rowData = new String[uploadResults.get(j).getRoundResultList().size() * 5 + 12];
                rowData[0] = number + "";
                rowData[1] = student.getStudentCode();
                rowData[2] = student.getStudentName();
                rowData[3] = student.getSex() == Student.MALE ? "男" : "女";
                rowData[4] = student.getSchoolName();
                rowData[5] = student.getClassName();
                rowData[6] = item.getItemName();

                if (TextUtils.isEmpty(uploadResults.get(j).getSiteScheduleNo())) {
                    rowData[7] = "";
                    rowData[8] = "";
                } else {
                    Schedule schedule = DBManager.getInstance().getSchedulesByNo(uploadResults.get(j).getSiteScheduleNo());
                    if (schedule!=null){
                        rowData[7] = DateUtil.formatTime2(Long.valueOf(schedule.getBeginTime()), "yyyy-MM-dd HH:mm:ss");
                    }
                    rowData[8] = uploadResults.get(j).getGroupNo();
                }
                List<RoundResultBean> roundResultBeans = uploadResults.get(j).getRoundResultList();
                rowData[9] = roundResultBeans.get(0).getExamState() == 0 ? "正常" : "补考";
                for (int k = 0; k < roundResultBeans.size(); k++) {
                    if (roundResultBeans.get(k).getResultType() == 1) {

                        rowData[11] = ResultDisplayUtils.getStrResultForDisplay(roundResultBeans.get(k).getResult());
                    }
                    rowData[11 + (k * 5) + 1] = ResultDisplayUtils.getStrResultForDisplay(roundResultBeans.get(k).getResult());
                    rowData[11 + (k * 5) + 2] = roundResultBeans.get(k).getPenalty() + "";
                    rowData[11 + (k * 5) + 3] = DateUtil.formatTime2(Long.valueOf(roundResultBeans.get(k).getTestTime()), "yyyy-MM-dd HH:mm:ss");
                    rowData[11 + (k * 5) + 4] = roundResultBeans.get(k).getStumbleCount() + "";
                    rowData[11 + (k * 5) + 5] = ResultDisplayUtils.setResultState(roundResultBeans.get(k).getIsFoul());
                }
                writeData.add(Arrays.asList(rowData));

                number++;
            }
        }
    }
}
