package com.feipulai.exam.exl;

import android.text.TextUtils;

import com.feipulai.common.dbutils.UsbFileAdapter;
import com.feipulai.common.exl.ExlListener;
import com.feipulai.common.exl.ExlReader;
import com.feipulai.common.utils.ExlPostfixUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.db.MachineItemCodeUtil;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.ItemSchedule;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileInputStream;
import com.orhanobut.logger.Logger;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by James on 2018/11/1 0001.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class GroupStuItemExLReader extends ExlReader {

    private List<String> mNecessaryCols;
    private Map<String, Integer> mColNums;
    //private String path;
    private boolean mHasIDCardCol;// 导入时是否有身份证信息,身份证信息时可选项
    private String mItemName;
    private String mItemCode;
    private UsbFile file;

    public GroupStuItemExLReader(ExlListener listener) {
        super(listener);
        String[] cols = {"场次", "分组性别", "组别", "分组", "道次或序号", "日程时间", "性别", "准考证号", "姓名", "项目", "项目代码", "考试类型"};
        mNecessaryCols = Arrays.asList(cols);
    }

    @Override
    protected void read(UsbFile file) {

        this.file = file;
        mColNums = new HashMap<>();
        List<ExelGroupReadBean> result = null;
        String postfix;
        if (file instanceof UsbFileAdapter) {
            Logger.i("文件路径：" + ((UsbFileAdapter) file).getFile());
            postfix = ExlPostfixUtil.getPostfix(((UsbFileAdapter) file).getFile().getName());
        } else {
            postfix = ExlPostfixUtil.getPostfix(file.getName());
        }
        if (!ExlPostfixUtil.EMPTY.equals(postfix)) {
            if (ExlPostfixUtil.OFFICE_EXCEL_2003_POSTFIX.equals(postfix)) {
                result = readXls();
            } else if (ExlPostfixUtil.OFFICE_EXCEL_2010_POSTFIX.equals(postfix)) {
                result = readXlsx();
            }
        } else {
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "请选择正确的导入文件");
        }
//        List<ExelGroupReadBean> result = readXls();
        if (result == null || result.size() == 0) {
            return;
        }

        boolean success = TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP ? insertZCPIntoDB(result) : insertIntoDB(result);

        if (success) {
            SettingHelper.getSystemSetting().setTestPattern(SystemSetting.GROUP_PATTERN);
            SettingHelper.updateSettingCache(SettingHelper.getSystemSetting());
            listener.onExlResponse(ExlListener.EXEL_READ_SUCCESS, "Excel导入成功!");
        }
    }

    private boolean insertZCPIntoDB(List<ExelGroupReadBean> result) {
//        String itemCode = TestConfigs.sCurrentItem.getItemCode();
        // 项目代码还是默认的,需要更新当前的项目的项目代码,并且将之前有的 报名信息 和 成绩 的项目代码更改
        Item nameItem = DBManager.getInstance().queryItemByName(mItemName);
        Item codeItem = DBManager.getInstance().queryItemByCode(mItemCode);

        if (codeItem != null && nameItem != null) {
            if (!TextUtils.equals(codeItem.getItemName(), nameItem.getItemName())) {
                listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel导入失败,导入项目代码已存在,拒绝导入");
                Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel导入失败,导入项目代码已存在,拒绝导入");
                return false;
            } else if (codeItem.getMachineCode() != ItemDefault.CODE_ZCP) {
                listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel导入失败,导入项目机器码错误,拒绝导入");
                Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel导入失败,导入项目机器码错误,拒绝导入");
                return false;
            }
        } else if (codeItem == null && nameItem == null) {
            DBManager.getInstance().insertItem(ItemDefault.CODE_ZCP, mItemCode
                    , mItemName, "分'秒");
        } else if (codeItem != null && nameItem == null) {
            if (codeItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                codeItem.setItemName(mItemName);
                DBManager.getInstance().updateItem(codeItem);// 更新项目表中信息
            } else {
                listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel导入失败,导入项目机器码错误,拒绝导入");
                Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel导入失败,导入项目机器码错误,拒绝导入");
                return false;
            }
        } else if (codeItem == null && nameItem != null) {
            if (nameItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                nameItem.setItemCode(mItemCode);
                DBManager.getInstance().updateItem(nameItem);// 更新项目表中信息
            } else {
                listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel导入失败,导入项目机器码错误,拒绝导入");
                Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel导入失败,导入项目机器码错误,拒绝导入");
                return false;
            }

        }

        insertDB(result);
        return true;
    }

    private boolean insertIntoDB(List<ExelGroupReadBean> result) {
        String itemCode = TestConfigs.sCurrentItem.getItemCode();
        // 项目代码还是默认的,需要更新当前的项目的项目代码,并且将之前有的 报名信息 和 成绩 的项目代码更改
        Item nameItem = DBManager.getInstance().queryItemByName(mItemName);

        if (itemCode == null) {
            try {
                Logger.i(mItemCode + " :  " + mItemName);
                if (nameItem == null) {
                    TestConfigs.sCurrentItem.setItemCode(mItemCode);
                    TestConfigs.sCurrentItem.setItemName(mItemName);
                    DBManager.getInstance().updateItem(TestConfigs.sCurrentItem);// 更新项目表中信息
                } else {
                    if (TestConfigs.sCurrentItem.getMachineCode() == nameItem.getMachineCode()) {
                        TestConfigs.sCurrentItem.setItemCode(mItemCode);
                        TestConfigs.sCurrentItem.setItemName(mItemName);
                        DBManager.getInstance().updateItem(TestConfigs.sCurrentItem);// 更新项目表中信息
                    } else {
                        listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel导入失败,导入项目名已存在,拒绝导入");
                        Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel导入失败,导入项目名已存在,拒绝导入");
                        return false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel导入失败,导入项目代码已存在,拒绝导入");
                Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel导入失败,拒绝导入" + e.getMessage());
                return false;
            }

//            TestConfigs.sCurrentItem.setItemCode(mItemCode);
//            TestConfigs.sCurrentItem.setItemName(mItemName);
//            try {
//                DBManager.getInstance().updateItem(TestConfigs.sCurrentItem);// 更新项目表中信息
//            } catch (Exception e) {
//                e.printStackTrace();
//                listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel导入失败,请选择相应项目数据导入");
//                Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel导入失败,请选择相应项目数据导入");
//                return false;
//            }

            List<RoundResult> roundResults = DBManager.getInstance().queryResultsByItemCodeDefault();
            List<StudentItem> studentItems = DBManager.getInstance().queryStuItemsByItemCodeDefault();
            MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, mItemCode);

            SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, mItemCode);
        } else if (!itemCode.equals(mItemCode)) {
            // 身高体重项目,导入体重项目时,直接当做只导入身高即可
            if (itemCode.equals(TestConfigs.HEIGHT_ITEM_CODE) && mItemCode.equals(TestConfigs.WEIGHT_ITEM_CODE)) {
                mItemName = itemCode;
            } else {
                // 已经有了项目代码,那么导入时的项目项目代码必须与已有的相同
                listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel导入失败,导入项目代码与已有项目代码不同,拒绝导入");
                Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel导入失败,导入项目代码与已有项目代码不同,拒绝导入");
                return false;
            }
        } else if (!mItemName.equals(TestConfigs.sCurrentItem.getItemName())) {

            if (nameItem == null) {
                TestConfigs.sCurrentItem.setItemName(mItemName);
                DBManager.getInstance().updateItem(TestConfigs.sCurrentItem);// 更新项目表中信息(这里实际只更新了一个项目名)
            } else {
                if (TestConfigs.sCurrentItem.getMachineCode() == nameItem.getMachineCode()) {
                    TestConfigs.sCurrentItem.setItemName(mItemName);
                    DBManager.getInstance().updateItem(TestConfigs.sCurrentItem);// 更新项目表中信息(这里实际只更新了一个项目名)
                } else {
                    listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel导入失败,导入项目名已存在,拒绝导入");
                    Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel导入失败,导入项目名已存在,拒绝导入");
                }
            }


        }
        insertDB(result);

        return true;
    }

    private void insertDB(List<ExelGroupReadBean> result) {
        // 现在就可以确定新项目代码了
        List<Student> studentList = new ArrayList<>();
//        List<StudentItem> studentItemList = new ArrayList<>();
        List<Schedule> scheduleList = new ArrayList<>();
        List<ItemSchedule> itemScheduleList = new ArrayList<>();
        List<Group> groupList = new ArrayList<>();
        List<GroupItem> groupItemList = new ArrayList<>();
        Logger.i(TestConfigs.df.format(new Date()) + "==》exel分组读取的考生：" + result.toString());
        for (ExelGroupReadBean bean : result) {
            Student student = new Student();
            student.setStudentCode(bean.getStudentCode());
            student.setStudentName(bean.getStudentName());
            student.setSex(bean.getSex());
            student.setIdCardNo(bean.getIdCardNo());
            // 插入学生信息
//            DBManager.getInstance().insertStudent(student);
            studentList.add(student);


//            StudentItem studentItem = new StudentItem();
//            studentItem.setStudentCode(bean.getStudentCode());
//            studentItem.setItemCode(mItemCode);
//            studentItem.setScheduleNo(bean.getSessionNo());
//            studentItem.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
//            studentItem.setExamType(bean.getExamType());
//            DBManager.getInstance().insertStudentItem(studentItem);
//            studentItemList.add(studentItem);

            Schedule schedule = new Schedule();
            schedule.setBeginTime(getTimeMillis(bean.getScheduleTime(), "yyyyMMddHHmmss") + "");
            schedule.setScheduleNo(bean.getSessionNo());
            scheduleList.add(schedule);
//            DBManager.getInstance().insertSchedules(schedule);

            ItemSchedule itemSchedule = new ItemSchedule();
            itemSchedule.setScheduleNo(bean.getSessionNo());
            itemSchedule.setItemCode(mItemCode);
            itemScheduleList.add(itemSchedule);

            Group group = new Group();
            group.setScheduleNo(bean.getSessionNo());
            group.setGroupType(bean.getGroupSex());
            group.setSortName(bean.getTranches());
            group.setGroupNo(bean.getGroupNo());
            int examType = 0;
            if (TextUtils.equals(bean.getExamType(), "正常")) {
                examType = 0;
            } else if (TextUtils.equals(bean.getExamType(), "补考")) {
                examType = 2;
            }
            group.setExamType(examType);
            group.setIsTestComplete(0);
            group.setItemCode(mItemCode);
//            DBManager.getInstance().insertGroup(group);
            groupList.add(group);

            GroupItem groupItem = new GroupItem();
            groupItem.setStudentCode(bean.getStudentCode());
            groupItem.setTrackNo(bean.getTrackNo());
            groupItem.setIdentityMark(0);
            groupItem.setScheduleNo(bean.getSessionNo());
            groupItem.setItemCode(mItemCode);
            groupItem.setGroupType(bean.getGroupSex());
            groupItem.setSortName(bean.getTranches());
            groupItem.setGroupNo(bean.getGroupNo());
//            DBManager.getInstance().insertGroupItem(groupItem);
            groupItemList.add(groupItem);
        }

        // 插入学生信息
        DBManager.getInstance().insertStudentList(studentList);
//        // 插入学生项目信息
//        DBManager.getInstance().insertStuItemList(studentItemList);
        //插入日程
        DBManager.getInstance().insertSchedulesList(scheduleList);
        //插入项目日程
        DBManager.getInstance().insertItemSchedulesList(itemScheduleList);
        // 插入分组信息
        DBManager.getInstance().insertGroupList(groupList);
        // 插入分组学生项目信息
        DBManager.getInstance().insertGroupItemList(groupItemList);
    }

    /**
     * 获取指定时间格式的时间戳
     *
     * @param time    时间
     * @param pattern 格式
     * @return 时间戳
     */
    public static long getTimeMillis(String time, String pattern) {

        Date date = null;
        try {
            date = new SimpleDateFormat(pattern).parse(time);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }
        return date.getTime();
    }

    private InputStream getInputStram() {
        InputStream is = null;
        try {
            if (file instanceof UsbFileAdapter) {
                Logger.i("文件路径：" + ((UsbFileAdapter) file).getFile());
                is = new FileInputStream(((UsbFileAdapter) file).getFile());
            } else {
                is = new UsbFileInputStream(file);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel读取失败,指定文件名" + file + "不存在");
            Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel读取失败,指定文件名" + file + "不存在");
            return null;
        } catch (Exception e) {
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel读取失败,文件读取异常");
            Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel读取失败,文件读取异常");
            return null;
        }
        return is;

    }

    private List<ExelGroupReadBean> readXlsx() {


        final List<ExelGroupReadBean> result = new ArrayList<>();
        final XlsxReaderUtil xlsxReader = new XlsxReaderUtil();
        xlsxReader.setDataListener(new XlsxReaderUtil.GetReaderXlsxDataListener() {
            @Override
            public void readerLineData(int rowNum, List<String> data) {
                if (rowNum == 0) {
                    if (data.size() != 19) {
                        listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "EXL文档格式错误");
                        Logger.i(TestConfigs.df.format(new Date()) + "---> " + "EXL文档格式错误");
                        xlsxReader.setStop(true);
                        return;
                    }


                    mHasIDCardCol = false;
                    for (int i = 0; i < data.size(); i++) {
                        //把需要的数据列的列名和索引记下来
                        String cellValue = data.get(i);
                        // 必须有"(*)"号
                        if (cellValue.contains("*")) {
                            cellValue = cellValue.substring(0, cellValue.indexOf("*") - 1);
                        }
                        mColNums.put(cellValue, i);
                        if (cellValue.equals("身份证号")) {
                            mHasIDCardCol = true;
                        }
                    }

                    //检查是否有所有需要的索引
                    for (int i = 0; i < mNecessaryCols.size(); i++) {
                        if (!mColNums.containsKey(mNecessaryCols.get(i))) {
                            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "缺少必要列:" + mNecessaryCols.get(i) + ",excel读取失败");
                            Logger.i(TestConfigs.df.format(new Date()) + "---> " + "缺少必要列:" + mNecessaryCols.get(i) + ",excel读取失败");
                            xlsxReader.setStop(true);
                            return;
                        }
                    }
                    return;
                }
                ExelGroupReadBean bean = readXLSXRow(data);
                if (bean == null) {
                    listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "Excel读取解析失败,第" + rowNum + 1 + "行读取失败");
                    Logger.i(TestConfigs.df.format(new Date()) + "---> " + "Excel读取解析失败,第" + rowNum + 1 + "行读取失败");
                    return;
                }
                result.add(bean);
            }
        });
        try {
            xlsxReader.read(getInputStram());
        } catch (Exception e) {
            return null;
        }


//        XSSFWorkbook xssfWorkbook;
//        InputStream is = getInputStram();
//        try {
//            xssfWorkbook = new XSSFWorkbook(is);
//        } catch (IOException e) {
//            e.printStackTrace();
//            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel读取失败,文件读取异常");
//            Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel读取失败,文件读取异常");
//            return null;
//        }
//
//        XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);// HSSFSheet 标识某一页
//
//        List<ExelGroupReadBean> result = readRow(xssfSheet);
//
//        try {
//            is.close();
//            xssfWorkbook.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "exel读取失败,文件读取异常");
//            return null;
//        }
        return result;
    }

    private ExelGroupReadBean readXLSXRow(List<String> rowList) {
        ExelGroupReadBean bean = new ExelGroupReadBean();
        //  "场次", "分组性别", "组别", "分组", "分道", "日程时间", "性别", "准考证号", "姓名", "项目", "项目代码","考试类型"
        //  0场地	1组别	2性别	3准考证号	4姓名	5单位	6项目	7赛次	8场次	9分组	10道次或序号
        // 11考试时间	12编号	13小组号	14身份证号	15分组性别	16日程时间	17项目代码	18考试类型
        String stuCode = rowList.get(3);
        String sex = rowList.get(2);
        String stuName = rowList.get(4);
        String itemName = rowList.get(6);
        String itemCode = rowList.get(17);
        String sessionNo = rowList.get(8);
        String groupSex = rowList.get(15);
        String tranches = rowList.get(1);
        String groupNo = rowList.get(9);
        String scheduleTime = rowList.get(16);
        String trackNo = rowList.get(10);
        String examType = rowList.get(18);
        String idCardNo = rowList.get(14);
        if (TextUtils.isEmpty(examType)) {
            return null;
        }
        bean.setExamType(examType);

        if (TextUtils.isEmpty(stuCode)) {
            return null;
        }
        bean.setStudentCode(stuCode);

        if ("男".equals(sex)) {
            bean.setSex(Student.MALE);
        } else if ("女".equals(sex)) {
            bean.setSex(Student.FEMALE);
        } else {
            // 不男不女
            return null;
        }

        if (TextUtils.isEmpty(stuName)) {
            return null;
        }
        bean.setStudentName(stuName);

        if (mItemName == null) {
            mItemName = itemName;
        }
        if (TextUtils.isEmpty(itemName) || !mItemName.equals(itemName)) {
            // 表里面所有行的项目名必须相同
            return null;
        }
        bean.setItemName(itemName);

        if (mItemCode == null) {
            mItemCode = itemCode;
        }
        if (TextUtils.isEmpty(itemCode) || !mItemCode.equals(itemCode)) {
            // 表里面所有行的项目代码必须相同
            return null;
        }
        bean.setItemCode(itemCode);

        if (mHasIDCardCol) {


            if (!TextUtils.isEmpty(idCardNo)) {
                bean.setIdCardNo(idCardNo);
            }
        }

        if (TextUtils.isEmpty(sessionNo)) {
            return null;
        }
        bean.setSessionNo(sessionNo);

        if ("男子".equals(groupSex)) {
            bean.setGroupSex(Group.MALE);
        } else if ("女子".equals(groupSex)) {
            bean.setGroupSex(Group.FEMALE);
        } else if ("混合".equals(groupSex)) {
            //混合
            bean.setGroupSex(Group.MIXTURE);
        } else {
            return null;
        }

        if (TextUtils.isEmpty(tranches)) {
            return null;
        }
        bean.setTranches(tranches);
        if (TextUtils.isEmpty(groupNo)) {
            return null;
        }
        bean.setGroupNo(Integer.valueOf(groupNo));
        if (TextUtils.isEmpty(scheduleTime)) {
            return null;
        }
        bean.setScheduleTime(scheduleTime);
        if (TextUtils.isEmpty(trackNo)) {
            return null;
        }
        bean.setTrackNo(Integer.valueOf(trackNo));

        return bean;
    }


    // 读取exel文档数据
    private List<ExelGroupReadBean> readXls() {
        InputStream is = getInputStram();
        HSSFWorkbook hssfWorkbook = null;
        try {
            hssfWorkbook = new HSSFWorkbook(is);
        } catch (IOException e) {
            e.printStackTrace();
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel读取失败,文件读取异常");
            Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel读取失败,文件读取异常");
            return null;
        }
        HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);// HSSFSheet 标识某一页
        List<ExelGroupReadBean> result = readRow(hssfSheet);
        try {
            is.close();
            hssfWorkbook.close();
        } catch (IOException e) {
            e.printStackTrace();
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "exel读取失败,文件读取异常");
            return null;
        }
        return result;
    }

    private List<ExelGroupReadBean> readRow(Sheet sheet) {
        if (sheet == null) {
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel读取失败,请检查excel文件格式(Excel第一张表无内容)");
            Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel读取失败,请检查excel文件格式(Excel第一张表无内容)");
            return null;
        }
        // 处理第一行
        Row firstRow = sheet.getRow(0);
        if (firstRow == null) {
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel读取失败,请检查excel文件格式(Excel第一行无内容)");
            Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel读取失败,请检查excel文件格式(Excel第一行无内容)");
            return null;
        }

        // rowSum 总行数
        boolean isFirstRowRead = readFirstRow(firstRow);
        if (!isFirstRowRead) {
            return null;
        }

        // 到这里,文件格式就检查完成了,接下来解析真正的数据
        int rowSum = sheet.getPhysicalNumberOfRows();
        List<ExelGroupReadBean> result = new ArrayList<>();

        mItemName = null;
        mItemCode = null;

        // 循环读取每一行,从第二行开始读
        for (int rowNum = 1; rowNum < rowSum; rowNum++) {

            Row row = sheet.getRow(rowNum);

            if (row == null) {
                listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "Excel读取失败,第" + rowNum + "行读取失败");
                Logger.i(TestConfigs.df.format(new Date()) + "---> " + "Excel读取失败,第" + rowNum + "行读取失败");
                return null;
            }
            ExelGroupReadBean bean = generateBeanFromRow(row);
            if (bean == null) {
                listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "Excel读取解析失败,第" + rowNum + "行读取失败");
                Logger.i(TestConfigs.df.format(new Date()) + "---> " + "Excel读取解析失败,第" + rowNum + "行读取失败");
                return null;
            }
            result.add(bean);
        }
        return result;
    }

    // 一行数据,生成一个对象,如果读取失败直接返回null
    private ExelGroupReadBean generateBeanFromRow(Row row) {

        ExelGroupReadBean bean = new ExelGroupReadBean();
        //  "场次", "分组性别", "组别", "分组", "分道", "日程时间", "性别", "准考证号", "姓名", "项目", "项目代码","考试类型"
        Cell stuCodeCell = row.getCell(mColNums.get("准考证号"));
        Cell sexCell = row.getCell(mColNums.get("性别"));
        Cell stuNameCell = row.getCell(mColNums.get("姓名"));
        Cell itemNameCell = row.getCell(mColNums.get("项目"));
        Cell itemCodeCell = row.getCell(mColNums.get("项目代码"));
        Cell sessionNoCell = row.getCell(mColNums.get("场次"));
        Cell groupSexCell = row.getCell(mColNums.get("分组性别"));
        Cell tranchesCell = row.getCell(mColNums.get("组别"));
        Cell groupNoCell = row.getCell(mColNums.get("分组"));
        Cell scheduleTimeCell = row.getCell(mColNums.get("日程时间"));
        Cell trackNoCell = row.getCell(mColNums.get("道次或序号"));
        Cell examTypeCell = row.getCell(mColNums.get("考试类型"));
        if (stuCodeCell == null || sexCell == null || stuNameCell == null || itemNameCell == null || itemCodeCell == null
                || sessionNoCell == null || groupSexCell == null || tranchesCell == null || groupNoCell == null
                || scheduleTimeCell == null || trackNoCell == null || examTypeCell == null) {
            return null;
        }

        String stuCode = getStringVal(stuCodeCell);
        String sex = getStringVal(sexCell);
        String stuName = getStringVal(stuNameCell);
        String itemName = getStringVal(itemNameCell);
        String itemCode = getStringVal(itemCodeCell);
        String sessionNo = getStringVal(sessionNoCell);
        String groupSex = getStringVal(groupSexCell);
        String tranches = getStringVal(tranchesCell);
        String groupNo = getStringVal(groupNoCell);
        String scheduleTime = getStringVal(scheduleTimeCell);
        String trackNo = getStringVal(trackNoCell);
        String examType = getStringVal(examTypeCell);
        if (TextUtils.isEmpty(examType)) {
            return null;
        }
        bean.setExamType(examType);

        if (TextUtils.isEmpty(stuCode)) {
            return null;
        }
        bean.setStudentCode(stuCode);

        if ("男".equals(sex)) {
            bean.setSex(Student.MALE);
        } else if ("女".equals(sex)) {
            bean.setSex(Student.FEMALE);
        } else {
            // 不男不女
            return null;
        }

        if (TextUtils.isEmpty(stuName)) {
            return null;
        }
        bean.setStudentName(stuName);

        if (mItemName == null) {
            mItemName = itemName;
        }
        if (TextUtils.isEmpty(itemName) || !mItemName.equals(itemName)) {
            // 表里面所有行的项目名必须相同
            return null;
        }
        bean.setItemName(itemName);

        if (mItemCode == null) {
            mItemCode = itemCode;
        }
        if (TextUtils.isEmpty(itemCode) || !mItemCode.equals(itemCode)) {
            // 表里面所有行的项目代码必须相同
            return null;
        }
        bean.setItemCode(itemCode);

        if (mHasIDCardCol) {
            Cell idCell = row.getCell(mColNums.get("身份证号"));
            if (idCell != null) {
                String idCardNo = getStringVal(idCell);
                if (!TextUtils.isEmpty(idCardNo)) {
                    bean.setIdCardNo(idCardNo);
                }
            }
        }

        if (TextUtils.isEmpty(sessionNo)) {
            return null;
        }
        bean.setSessionNo(sessionNo);

        if ("男子".equals(groupSex)) {
            bean.setGroupSex(Group.MALE);
        } else if ("女子".equals(groupSex)) {
            bean.setGroupSex(Group.FEMALE);
        } else if ("混合".equals(groupSex)) {
            //混合
            bean.setGroupSex(Group.MIXTURE);
        } else {
            return null;
        }

        if (TextUtils.isEmpty(tranches)) {
            return null;
        }
        bean.setTranches(tranches);
        if (TextUtils.isEmpty(groupNo)) {
            return null;
        }
        bean.setGroupNo(Integer.valueOf(groupNo));
        if (TextUtils.isEmpty(scheduleTime)) {
            return null;
        }
        bean.setScheduleTime(scheduleTime);
        if (TextUtils.isEmpty(trackNo)) {
            return null;
        }
        bean.setTrackNo(Integer.valueOf(trackNo));

        return bean;
    }

    /**
     * 读取第一行标题，检验格式是否正确，是否有缺失必填项
     *
     * @param firstRow 第一行
     * @return 第一行处理成功, 返回true;否则返回false
     */
    private boolean readFirstRow(Row firstRow) {
        mHasIDCardCol = false;
        int minColIx = firstRow.getFirstCellNum();
        int maxColIx = firstRow.getLastCellNum();
        for (int colIx = minColIx; colIx < maxColIx; colIx++) {
            // HSSFCell 表示单元格
            Cell cell = firstRow.getCell(colIx);
            if (cell == null) {
                continue;
            }
            //把需要的数据列的列名和索引记下来
            String cellValue = getStringVal(cell);
            // 必须有"(*)"号
            if (cellValue.contains("*")) {
                cellValue = cellValue.substring(0, cellValue.indexOf("*") - 1);
            }
            mColNums.put(cellValue, colIx);
            if (cellValue.equals("身份证号")) {
                mHasIDCardCol = true;
            }
        }
        //检查是否有所有需要的索引
        for (int i = 0; i < mNecessaryCols.size(); i++) {
            if (!mColNums.containsKey(mNecessaryCols.get(i))) {
                listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "缺少必要列:" + mNecessaryCols.get(i) + ",excel读取失败");
                return false;
            }
        }
        return true;
    }

    private String getStringVal(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                cell.setCellType(Cell.CELL_TYPE_STRING);
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue() ? "TRUE" : "FALSE";
            case Cell.CELL_TYPE_FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

}
