package com.feipulai.exam.exl;

import android.text.TextUtils;

import com.feipulai.common.exl.ExlListener;
import com.feipulai.common.exl.ExlReader;
import com.feipulai.common.exl.ExlReaderUtil;
import com.feipulai.common.exl.GetReaderDataListener;
import com.feipulai.common.utils.DateUtil;
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
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.utils.EncryptUtil;
import com.github.mjdev.libaums.fs.UsbFile;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by zzs on  2019/8/27
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StuItemExLReader extends ExlReader {
    private List<String> mNecessaryCols;
    private Map<String, Integer> mColNums;
    //private String path;
    private boolean mHasIDCardCol;// 导入时是否有身份证信息,身份证信息时可选项
    private String mItemName;
    private String mItemCode;
    private ExlReaderUtil reader;
    private int importTyle = 0; //0 个人 1 分组 默认 0

    @Override
    protected void read(UsbFile path) {
        mColNums = new HashMap<>();
        final List<Object> readBeans = new ArrayList<>();
        if (importTyle == 0 && TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "中长跑项目无法进行个人导入");
            return;
        }
        reader = new ExlReaderUtil.Builder(path).setCellLength(19).setReaderDataListener(new GetReaderDataListener() {
            @Override
            public void readerLineData(int rowNum, List<String> data) {
                if (rowNum == 0) {
                    boolean isSucceed = readFirstRow(data);
                    reader.setStop(!isSucceed);
                } else {
                    ExelGroupReadBean bean = readXLSXRow(data);
                    if (bean == null) {
                        listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "Excel读取解析失败,第" + rowNum + 1 + "行读取失败");
                        Logger.i(TestConfigs.df.format(new Date()) + "---> " + "Excel读取解析失败,第" + rowNum + 1 + "行读取失败");
                        return;
                    }
                    readBeans.add(bean);
                }
            }
        }).setExlListener(listener).build();
        reader.read();
        if (readBeans == null || readBeans.size() == 0) {
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "导入文档数据为空");
            return;
        }
        boolean success;
        if (importTyle == 0) {
            success = insertIntoDB(readBeans);
        } else {
            success = TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP ? insertZCPIntoDB(readBeans) : insertGroupIntoDB(readBeans);
        }
        if (success) {
            SettingHelper.getSystemSetting().setTestPattern(importTyle == 0 ? SystemSetting.PERSON_PATTERN : SystemSetting.GROUP_PATTERN);
            SettingHelper.updateSettingCache(SettingHelper.getSystemSetting());
            listener.onExlResponse(ExlListener.EXEL_READ_SUCCESS, "Excel导入成功!");
        }
    }


    public StuItemExLReader(int importTyle, ExlListener listener) {
        super(listener);
        this.importTyle = importTyle;
        if (importTyle == 0) {
            String[] cols = {"性别", "准考证号", "姓名", "项目", "项目代码", "场次", "考试类型"};
            mNecessaryCols = Arrays.asList(cols);
        } else {
            String[] cols = {"场次", "分组性别", "组别", "分组", "道次或序号", "日程时间", "性别", "准考证号", "姓名", "项目", "项目代码", "考试类型"};
            mNecessaryCols = Arrays.asList(cols);
        }

    }


    /**
     * 读取第一行标题，检验格式是否正确，是否有缺失必填项
     *
     * @return 第一行处理成功, 返回true;否则返回false
     */
    private boolean readFirstRow(List<String> data) {
        mHasIDCardCol = false;
        for (int i = 0; i < data.size(); i++) {
            // 必须有"(*)"号
            String cellValue = data.get(i);
            if (data.get(i).contains("*")) {
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
                return false;
            }
        }
        return true;
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
//        if (TextUtils.isEmpty(examType)) {
//            return null;
//        }
        bean.setExamType(examType);
        try {
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


                    bean.setIdCardNo(EncryptUtil.setEncryptString(Student.ENCRYPT_KEY,idCardNo));
                }
            }

            if (TextUtils.isEmpty(sessionNo)) {
                return null;
            }
            bean.setSessionNo(sessionNo);
            if (importTyle == 1) {
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
//            if (TextUtils.isEmpty(scheduleTime)) {
//                return null;
//            }
                bean.setScheduleTime(scheduleTime);
                if (TextUtils.isEmpty(trackNo)) {
                    return null;
                }
                bean.setTrackNo(Integer.valueOf(trackNo));
            }
        } catch (Exception e) {
            return null;
        }


        return bean;
    }

    private boolean insertIntoDB(List<Object> result) {
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
            List<RoundResult> roundResults = DBManager.getInstance().queryResultsByItemCodeDefault();
            List<StudentItem> studentItems = DBManager.getInstance().queryStuItemsByItemCodeDefault();
            List<MachineResult> machineResults = DBManager.getInstance().queryMachineResultByItemCodeDefault(TestConfigs.sCurrentItem.getMachineCode());
            MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, machineResults, mItemCode);

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
        // 现在就可以确定新项目代码了

        List<Student> stuList = new LinkedList<>();
        List<StudentItem> stuItemList = new LinkedList<>();
        Logger.i(TestConfigs.df.format(new Date()) + "==》exel读取的考生：" + result.toString());
        for (Object o : result) {
            ExelReadBean bean = (ExelReadBean) o;
            Student student = new Student();
            student.setStudentCode(bean.getStudentCode());
            student.setStudentName(bean.getStudentName());
            student.setSex(bean.getSex());
            student.setIdCardNo(bean.getIdCardNo());

            stuList.add(student);

            StudentItem studentItem = new StudentItem();
            studentItem.setStudentCode(bean.getStudentCode());
            studentItem.setItemCode(mItemCode);
            studentItem.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
            int examType = 0;
            if (TextUtils.equals(bean.getExamType(), "正常")) {
                examType = 0;
            } else if (TextUtils.equals(bean.getExamType(), "补考")) {
                examType = 2;
            }
            studentItem.setExamType(examType);
            studentItem.setScheduleNo(bean.getSessionNo());
            studentItem.setStudentType(0);
            stuItemList.add(studentItem);
        }

        // 插入学生信息
        DBManager.getInstance().insertStudentList(stuList);
        // 插入学生项目信息
        DBManager.getInstance().insertStuItemList(stuItemList);

        return true;
    }


    private boolean insertZCPIntoDB(List<Object> result) {
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
                    , mItemName, "分'秒", DBManager.TEST_TYPE_TIME);
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
                TestConfigs.updateItemFillAll(nameItem, mItemCode);
                nameItem.setItemCode(mItemCode);
                DBManager.getInstance().updateItem(nameItem);// 更新项目表中信息
            } else {
                listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel导入失败,导入项目机器码错误,拒绝导入");
                Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel导入失败,导入项目机器码错误,拒绝导入");
                return false;
            }

        }

        insertGroupDB(result);
        return true;
    }

    private boolean insertGroupIntoDB(List<Object> result) {
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
            List<RoundResult> roundResults = DBManager.getInstance().queryResultsByItemCodeDefault();
            List<StudentItem> studentItems = DBManager.getInstance().queryStuItemsByItemCodeDefault();
            List<MachineResult> machineResults = DBManager.getInstance().queryMachineResultByItemCodeDefault(TestConfigs.sCurrentItem.getMachineCode());
            MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, machineResults, mItemCode);

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
        insertGroupDB(result);

        return true;
    }

    private void insertGroupDB(List<Object> result) {
        // 现在就可以确定新项目代码了
        List<Student> studentList = new ArrayList<>();
//        List<StudentItem> studentItemList = new ArrayList<>();
        List<Schedule> scheduleList = new ArrayList<>();
        List<ItemSchedule> itemScheduleList = new ArrayList<>();
        List<Group> groupList = new ArrayList<>();
        List<GroupItem> groupItemList = new ArrayList<>();
        Logger.i(TestConfigs.df.format(new Date()) + "==》exel分组读取的考生：" + result.toString());
        String scheduleTime = "";
        for (Object o : result) {
            ExelGroupReadBean bean = (ExelGroupReadBean) o;
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
            if (TextUtils.isEmpty(bean.getScheduleTime())) {
                if (TextUtils.isEmpty(scheduleTime)) {
                    scheduleTime = DateUtil.getCurrentTime() + "";
                }
                schedule.setBeginTime(scheduleTime);
            } else {
                schedule.setBeginTime(DateUtil.getTimeMillis(bean.getScheduleTime(), "yyyyMMddHHmmss") + "");
            }

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
            Logger.i(groupItem.toString());
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
}
