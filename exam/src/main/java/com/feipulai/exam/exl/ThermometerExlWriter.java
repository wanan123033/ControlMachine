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
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.entity.StudentThermometer;
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
public class ThermometerExlWriter extends ExlWriter {
    private List<List<String>> writeData = new ArrayList<>();

    public ThermometerExlWriter(ExlListener listener) {
        super(listener);
    }

    @Override
    protected void write(UsbFile file) {
        writeData.clear();
        String[] first = new String[]{"编号", "准考证号", "姓名", "性别", "学校名称", "班级", "项目", "考试状态", "体温"};

        List<String> headerList = Arrays.asList(first);
        writeData.add(headerList);
        generateRows();
        new ExlWriterUtil.Builder(file).setSheetname("测试成绩").setExlListener(listener).setWriteData(writeData).build().write();
    }


    private void generateRows() {

        List<StudentThermometer> thermometerList = DBManager.getInstance().getItemThermometerList();

        if (thermometerList == null) {
            return;
        }
        for (int i = 0; i < thermometerList.size(); i++) {
            StudentThermometer thermometer = thermometerList.get(i);
            Student student = DBManager.getInstance().queryStudentByStuCode(thermometer.getStudentCode());
            String[] rowData = new String[9];
            rowData[0] = (i + 1) + "";
            rowData[1] = student.getStudentCode();
            rowData[2] = student.getStudentName();
            rowData[3] = student.getSex() == Student.MALE ? "男" : "女";
            rowData[4] = student.getSchoolName();
            rowData[5] = student.getClassName();
            rowData[6] = TestConfigs.sCurrentItem.getItemName();
            rowData[7] = StudentItem.setResultState(thermometer.getExamType());
            rowData[8] = thermometer.getThermometer() + "";
            writeData.add(Arrays.asList(rowData));
        }

    }
}
