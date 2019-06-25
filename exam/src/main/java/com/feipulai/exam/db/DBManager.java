package com.feipulai.exam.db;

import android.database.Cursor;
import android.text.TextUtils;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.BuildConfig;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.ChipGroupDao;
import com.feipulai.exam.entity.ChipInfoDao;
import com.feipulai.exam.entity.DaoMaster;
import com.feipulai.exam.entity.DaoSession;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupDao;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.GroupItemDao;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.ItemDao;
import com.feipulai.exam.entity.ItemSchedule;
import com.feipulai.exam.entity.ItemScheduleDao;
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.entity.MachineResultDao;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.RoundResultDao;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.entity.ScheduleDao;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentDao;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.entity.StudentItemDao;
import com.orhanobut.logger.Logger;

import org.greenrobot.greendao.database.Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2017/11/24.
 */
public class DBManager {

    public static final String DB_NAME = "control_db";
    public static final String DB_PASSWORD = "fplwwj";
    private static DBManager mInstance;
    private static ItemDao itemDao;
    private static StudentDao studentDao;
    private static StudentItemDao studentItemDao;
    private static RoundResultDao roundResultDao;
    private static ScheduleDao scheduleDao;
    private static ItemScheduleDao itemScheduleDao;
    private static GroupDao groupDao;
    private static GroupItemDao groupItemDao;
    private static MachineResultDao machineResultDao;
    private static ChipGroupDao chipGroupDao;
    private static ChipInfoDao chipInfoDao;
    private static Database db;
    private static DaoSession daoSession;
    public static DBOpenHelper helper;
    private DaoMaster daoMaster;

    private DBManager() {
    }

    public synchronized static DBManager getInstance() {
        if (mInstance == null) {
            mInstance = new DBManager();
            mInstance.initDB();
        }
        return mInstance;
    }

    /**
     * 数据库初始化
     */
    public void initDB() {
        helper = new DBOpenHelper(MyApplication.getInstance(), DB_NAME);
        db = BuildConfig.DEBUG ? helper.getWritableDb() : helper.getEncryptedWritableDb(DB_PASSWORD);
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();

        studentDao = daoSession.getStudentDao();
        itemDao = daoSession.getItemDao();
        studentItemDao = daoSession.getStudentItemDao();
        roundResultDao = daoSession.getRoundResultDao();
        groupDao = daoSession.getGroupDao();
        groupItemDao = daoSession.getGroupItemDao();
        scheduleDao = daoSession.getScheduleDao();
        itemScheduleDao = daoSession.getItemScheduleDao();
        machineResultDao = daoSession.getMachineResultDao();
        chipGroupDao=daoSession.getChipGroupDao();
        chipInfoDao=daoSession.getChipInfoDao();
        int[] supportMachineCodes = {/*ItemDefault.CODE_HW, */ItemDefault.CODE_TS, ItemDefault.CODE_YWQZ, ItemDefault.CODE_YTXS,
                ItemDefault.CODE_LDTY, ItemDefault.CODE_ZWTQQ,
                ItemDefault.CODE_HWSXQ, ItemDefault.CODE_FHL, ItemDefault.CODE_ZFP,
                ItemDefault.CODE_PQ, ItemDefault.CODE_MG, ItemDefault.CODE_FWC, ItemDefault.CODE_LQYQ, ItemDefault.CODE_ZQYQ, ItemDefault.CODE_ZCP
        };
        for (int machineCode : supportMachineCodes) {
            //查询是否已经存在该机器码的项,如果存在就放弃,避免重复添加
            List<Item> items = itemDao.queryBuilder().where(ItemDao.Properties.MachineCode.eq(machineCode)).list();
            if (items != null && items.size() != 0) {
                continue;
            }
            switch (machineCode) {

                case ItemDefault.CODE_TS:
                    // insertItem(machineCode, "E11","一分钟跳绳", "次");// for test
                    insertItem(machineCode, "跳绳", "次");
                    break;

                case ItemDefault.CODE_YWQZ:
                    insertItem(machineCode, "仰卧起坐", "次");
                    break;

                case ItemDefault.CODE_YTXS:
                    insertItem(machineCode, "引体向上", "次");
                    break;

                case ItemDefault.CODE_LDTY:
                    insertItem(machineCode, "立定跳远", "米");
                    break;

                case ItemDefault.CODE_ZWTQQ:
                    insertItem(machineCode, "坐位体前屈", "厘米");
                    break;

                case ItemDefault.CODE_HWSXQ:
                    insertItem(machineCode, "红外实心球", "厘米");
                    break;

                case ItemDefault.CODE_FHL:
                    insertItem(machineCode, "肺活量", "毫升");
                    break;

                case ItemDefault.CODE_ZFP:
                    insertItem(machineCode, "红外计时", "分'秒");
                    break;

                case ItemDefault.CODE_PQ:
                    insertItem(machineCode, "排球", "次");
                    break;
                case ItemDefault.CODE_MG:
                    insertItem(machineCode, "摸高", "厘米");
                    break;
                case ItemDefault.CODE_FWC:
                    insertItem(machineCode, "俯卧撑", "次");
                    break;
                case ItemDefault.CODE_LQYQ:
                    insertItem(machineCode, "篮球运球", "分'秒");
                    break;
                case ItemDefault.CODE_ZQYQ:
                    insertItem(machineCode, "足球运球", "分'秒");
                    break;
                case ItemDefault.CODE_ZCP:
                    insertItem(machineCode, "800米", "分'秒");
                    insertItem(machineCode, "1000米", "分'秒");
                    break;

            }
        }
        Logger.i("数据库初始化完成");
    }

    //清空数据库
    public void clear() {
        itemDao.deleteAll();
        studentDao.deleteAll();
        studentItemDao.deleteAll();
        roundResultDao.deleteAll();
        groupDao.deleteAll();
        groupItemDao.deleteAll();
        scheduleDao.deleteAll();
        itemScheduleDao.deleteAll();
        machineResultDao.deleteAll();
    }

    /**
     * 成绩表数据清空
     */
    public void roundResultClear() {
        roundResultDao.deleteAll();
        machineResultDao.deleteAll();
    }

    /********************************************
     * 学生表
     ********************************************/
    /**
     * 根据学生考号获取学生信息
     *
     * @param stuCode 考号
     * @return
     */
    public Student queryStudentByStuCode(final String stuCode) {

        Student student = studentDao.queryBuilder()
                .where(StudentDao.Properties.StudentCode.eq(stuCode))
                .unique();
        return student;
    }

    /**
     * 批量添加学生信息
     *
     * @param stuList 学生列表
     */
    public void insertStudentList(List<Student> stuList) {
        if (stuList == null || stuList.isEmpty()) {
            return;
        }
        studentDao.insertOrReplaceInTx(stuList);
        studentDao.detachAll();
    }

    /**
     * 添加学生
     *
     * @param student 学生
     */
    public void insertStudent(Student student) {
        studentDao.insert(student);
    }

    /**
     * 根据身份证号获取学生信息
     *
     * @param idcardNo 身份证号
     * @return
     */
    public Student queryStudentByIDCode(String idcardNo) {
        return studentDao
                .queryBuilder()
                .where(StudentDao.Properties.IdCardNo.eq(idcardNo))
                .unique();
    }

    /**
     * 获取所有学生列表
     *
     * @return
     */
    public List<Student> dumpAllStudents() {
        return studentDao
                .queryBuilder()
                .list();
    }

//    public List<StudentItem> queryStudentByItemAndSort(String itemCode,int sort){
//
//    }

    /**
     * 根据学号模糊查询学生信息
     */
    public List<Student> fuzzyQueryByStuCode(String studentCode, int limit, int offset) {
//        List<Student> students = studentDao.queryBuilder()
//                .where(StudentDao.Properties.StudentCode.like("%" + studentCode + "%"))
//                .limit(limit)
//                .offset(offset)
//                .list();

        StringBuffer sqlBuf = new StringBuffer("SELECT S.* FROM " + StudentDao.TABLENAME + " S");
        sqlBuf.append(" WHERE S." + StudentDao.Properties.StudentCode.columnName + " IN ( ");
        sqlBuf.append(" SELECT  " + StudentItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + StudentItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  ");
        sqlBuf.append(" UNION SELECT  " + GroupItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + GroupItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + GroupItemDao.Properties.ItemCode.columnName + " = ?  )");
        sqlBuf.append(" AND  S." + StudentDao.Properties.StudentCode.columnName + " LIKE '%" + studentCode + "%' ");
        sqlBuf.append(" limit " + offset + "," + limit);
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{TestConfigs.getCurrentItemCode(), TestConfigs.getCurrentItemCode()});
        List<Student> students = new ArrayList<>();
        while (c.moveToNext()) {
            Student student = studentDao.readEntity(c, 0);
            students.add(student);
        }
        c.close();
        return students;

    }

    /**
     * 获取当前测试项目的所有学生信息
     *
     * @param limit  页码
     * @param offset 页数
     * @return
     */
    public List<Student> getItemStudent(int limit, int offset) {
        StringBuffer sqlBuf = new StringBuffer("SELECT S.* FROM " + StudentDao.TABLENAME + " S");
        sqlBuf.append(" WHERE S." + StudentDao.Properties.StudentCode.columnName + " IN ( ");
        sqlBuf.append(" SELECT  " + StudentItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + StudentItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  ");
        sqlBuf.append(" UNION SELECT  " + GroupItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + GroupItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + GroupItemDao.Properties.ItemCode.columnName + " = ?  )");
        if (limit != -1)
            sqlBuf.append(" limit " + offset + "," + limit);
        Logger.i("=====sql1===>" + sqlBuf.toString());
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{TestConfigs.getCurrentItemCode(), TestConfigs.getCurrentItemCode()});
        List<Student> students = new ArrayList<>();
        while (c.moveToNext()) {
            Student student = studentDao.readEntity(c, 0);
            students.add(student);
        }
        c.close();
        return students;
    }

    /**
     * 根据用户筛选获取学生列表
     *
     * @param itemCode   当前测试项目
     * @param isTested   选择已测试 （传两个参数是会出现用户不选择两个筛选项）
     * @param isUnTested 选择未测试
     * @param isUpload   选择已上传
     * @param isUnUpload 选择未上传
     * @param limit      页码
     * @param offset     页数
     * @return
     */
//    public List<Map<String, Object>> getChooseStudentList(String itemCode, boolean isTested, boolean isUnTested, boolean isUpload, boolean isUnUpload, int
//            limit, int offset) {
//        Logger.i("zzs===>" + isTested + "---" + isUnTested + "---" + isUpload + "---" + isUnUpload);
//
//        StringBuffer sqlBuf = new StringBuffer("SELECT ");
//        sqlBuf.append("S.*"
//                //				+ StudentDao.Properties.Id.columnName + ",S." + StudentDao.Properties.StudentCode.columnName
//                //				+ ",S." + StudentDao.Properties.StudentName.columnName + ",S." + StudentDao.Properties.Sex.columnName
//                //				+ ",S." + StudentDao.Properties.IdCardNo.columnName + ",S." + StudentDao.Properties.IcCardNo.columnName
//                //				+ ",S." + StudentDao.Properties.ClassName.columnName + ",S." + StudentDao.Properties.GradeName.columnName
//                //				+ ",S." + StudentDao.Properties.MajorName.columnName + ",S." + StudentDao.Properties.FacultyName.columnName
//                //				+ ",S." + StudentDao.Properties.DownloadTime.columnName + ",S." + StudentDao.Properties.Remark1.columnName
//                //				+ ",S." + StudentDao.Properties.Remark2.columnName + ",S." + StudentDao.Properties.Remark3.columnName
////                        + ",RR." + RoundResultDao.Properties.Result.columnName
////                + ",RR." + RoundResultDao.Properties.UpdateState.columnName
//        );
//        sqlBuf.append("  FROM " + StudentDao.TABLENAME + " S");
//        sqlBuf.append(" LEFT JOIN " + StudentItemDao.TABLENAME + " I ");
//        sqlBuf.append(" ON S." + StudentDao.Properties.StudentCode.columnName + " = I." + StudentItemDao.Properties.StudentCode.columnName);
////        sqlBuf.append(" LEFT JOIN " + RoundResultDao.TABLENAME + " RR  ");
////        sqlBuf.append(" ON I." + StudentDao.Properties.StudentCode.columnName + " = RR." + StudentItemDao.Properties.StudentCode.columnName);
//        sqlBuf.append(" WHERE I." + StudentItemDao.Properties.MachineCode.columnName + " = " + TestConfigs.sCurrentItem.getMachineCode());
//        sqlBuf.append(" AND I." + StudentItemDao.Properties.ItemCode.columnName + " = '" + itemCode + "'");
//        sqlBuf.append(" AND S." + StudentDao.Properties.StudentCode.columnName);
//
//        if (isTested || isUnTested) {
//            if (isTested) {
//                sqlBuf.append(" IN (SELECT " + RoundResultDao.Properties.StudentCode.columnName + " FROM " + RoundResultDao.TABLENAME + " R");
//            } else {
//                sqlBuf.append(" NOT IN (SELECT " + RoundResultDao.Properties.StudentCode.columnName + " FROM " + RoundResultDao.TABLENAME + " R");
//            }
//        }
//        if (isUpload || isUnUpload) {
//            if (isTested || isUnTested) {
//                if (isUpload) {
//                    sqlBuf.append("  WHERE R." + RoundResultDao.Properties.UpdateState.columnName + "= 1  AND ");
//                } else {
//                    sqlBuf.append("  WHERE R." + RoundResultDao.Properties.UpdateState.columnName + "= 0 AND ");
//                }
//            } else {
//                if (isUpload) {
//                    sqlBuf.append(" IN (SELECT " +
//                            RoundResultDao.Properties.StudentCode.columnName + " FROM " + RoundResultDao.TABLENAME + " R  WHERE R." + RoundResultDao
//                            .Properties.UpdateState.columnName + "= 1 AND ");
//                } else {
//                    sqlBuf.append(" IN (SELECT " +
//                            RoundResultDao.Properties.StudentCode.columnName + " FROM " + RoundResultDao.TABLENAME + " R  WHERE R." + RoundResultDao
//                            .Properties.UpdateState.columnName + "= 0 AND ");
//                }
//            }
//        } else {
//            sqlBuf.append(" WHERE ");
//        }
//        sqlBuf.append("  R." + RoundResultDao.Properties.ItemCode.columnName + " = '" + TestConfigs.getCurrentItemCode() + "'");
//        sqlBuf.append(")  ");
//        sqlBuf.append(" limit " + offset + "," + limit);
//        Logger.i("=====sql1===>" + sqlBuf.toString());
//
//        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), null);
//        List<Map<String, Object>> students = new ArrayList<>();
//        Map<String, Object> studentMap;
//        while (c.moveToNext()) {
//            studentMap = new HashMap<>();
//            Student student = studentDao.readEntity(c, 0);
////            String result = c.isNull(14) ? null : c.getString(14);
////            int updataState = c.isNull(15) ? 0 : c.getInt(15);
//            studentMap.put("student", student);
////            studentMap.put("result", result);
////            studentMap.put("updataState", updataState);
//            students.add(studentMap);
//        }
//        c.close();
//        return students;
//    }

    /**
     * 获取用户筛选的所有学生（男，女）数量
     *
     * @param isTested   选择已测试 （传两个参数是会出现用户不选择两个筛选项）
     * @param isUnTested 选择未测试
     * @param isUpload   选择已上传
     * @param isUnUpload 选择未上传
     * @return
     */
    public Map<String, Object> getChooseStudentCount(boolean isTested, boolean isUnTested, boolean isUpload, boolean isUnUpload) {
        Logger.i("zzs===>" + isTested + "---" + isUnTested + "---" + isUpload + "---" + isUnUpload);

        StringBuffer sqlBuf = new StringBuffer("SELECT COUNT(*) AS STU_COUNT,");

        sqlBuf.append(" COUNT( CASE WHEN S." + StudentDao.Properties.Sex.columnName + "=0 THEN " + StudentDao.Properties.Sex.columnName + " END) AS" +
                " " +
                "MAN_COUNT, ");
        sqlBuf.append(" COUNT( CASE WHEN S." + StudentDao.Properties.Sex.columnName + "=1 THEN " + StudentDao.Properties.Sex.columnName + " END) AS" +
                " " +
                "WOMEM_COUNT ");

        sqlBuf.append("  FROM " + StudentDao.TABLENAME + " S");
        sqlBuf.append(" WHERE S." + StudentDao.Properties.StudentCode.columnName + " IN ( ");
        sqlBuf.append(" SELECT  " + StudentItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + StudentItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  ");
        sqlBuf.append(" UNION SELECT  " + GroupItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + GroupItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + GroupItemDao.Properties.ItemCode.columnName + " = ?  )");

        sqlBuf.append(" AND S." + StudentDao.Properties.StudentCode.columnName);

        if (isTested || isUnTested) {

            if (isTested) {
                sqlBuf.append(" IN (SELECT " + RoundResultDao.Properties.StudentCode.columnName + " FROM " + RoundResultDao.TABLENAME + " R");
            } else {
                sqlBuf.append(" NOT IN (SELECT " + RoundResultDao.Properties.StudentCode.columnName + " FROM " + RoundResultDao.TABLENAME + " R");
            }
        }
        if (isUpload || isUnUpload) {
            if (isTested || isUnTested) {
                if (isUpload) {
                    sqlBuf.append("  WHERE R." + RoundResultDao.Properties.UpdateState.columnName + "= 1  AND ");
                } else {
                    sqlBuf.append("  WHERE R." + RoundResultDao.Properties.UpdateState.columnName + "= 0 AND ");
                }
            } else {
                if (isUpload) {
                    sqlBuf.append(" IN (SELECT " +
                            RoundResultDao.Properties.StudentCode.columnName + " FROM " + RoundResultDao.TABLENAME + " R  WHERE R." + RoundResultDao
                            .Properties.UpdateState.columnName + "= 1 AND ");
                } else {
                    sqlBuf.append(" IN (SELECT " +
                            RoundResultDao.Properties.StudentCode.columnName + " FROM " + RoundResultDao.TABLENAME + " R  WHERE R." + RoundResultDao
                            .Properties.UpdateState.columnName + "= 0 AND ");
                }
            }

        } else {
            sqlBuf.append(" WHERE ");
        }

        sqlBuf.append("  R." + RoundResultDao.Properties.ItemCode.columnName + " = '" + TestConfigs.getCurrentItemCode() + "'");
        sqlBuf.append(")  ");

        Logger.i("=====sql1===>" + sqlBuf.toString());
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{TestConfigs.getCurrentItemCode(), TestConfigs.getCurrentItemCode()});

        Map<String, Object> countMap = new HashMap<>();

        if (c.moveToNext()) {
            int count = c.getInt(0);
            int man_count = c.getInt(1);
            int women_count = c.getInt(2);

            countMap.put("count", count);
            countMap.put("man_count", man_count);
            countMap.put("women_count", women_count);
        }
        c.close();

        return countMap;
    }

    /**
     * 获取项目中所有学生（男，女）数量
     *
     * @return
     */
    public Map<String, Object> getItemStudenCount() {
        StringBuffer sqlBuf = new StringBuffer("SELECT COUNT(*) AS STU_COUNT,");

        sqlBuf.append(" COUNT( CASE WHEN S." + StudentDao.Properties.Sex.columnName + "=0 THEN " + StudentDao.Properties.Sex.columnName + " END) AS" +
                " " +
                "MAN_COUNT, ");
        sqlBuf.append(" COUNT( CASE WHEN S." + StudentDao.Properties.Sex.columnName + "=1 THEN " + StudentDao.Properties.Sex.columnName + " END) AS" +
                " " +
                "WOMEM_COUNT ");

        sqlBuf.append("  FROM " + StudentDao.TABLENAME + " S ");
        sqlBuf.append(" WHERE S." + StudentDao.Properties.StudentCode.columnName + " IN ( ");
        sqlBuf.append(" SELECT  " + StudentItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + StudentItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  ");
        sqlBuf.append(" UNION SELECT  " + GroupItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + GroupItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + GroupItemDao.Properties.ItemCode.columnName + " = ?  ) ");

        Logger.i("=====sql1===>" + sqlBuf.toString());
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{TestConfigs.getCurrentItemCode(), TestConfigs.getCurrentItemCode()});

        Map<String, Object> countMap = new HashMap<>();

        if (c.moveToNext()) {
            int count = c.getInt(0);
            int man_count = c.getInt(1);
            int women_count = c.getInt(2);

            countMap.put("count", count);
            countMap.put("man_count", man_count);
            countMap.put("women_count", women_count);
        }
        c.close();

        return countMap;
    }

    /**
     * 根据学号模糊查询学生信息
     */
    public Map<String, Object> fuzzyQueryByStuCodeCount(String studentCode) {
        StringBuffer sqlBuf = new StringBuffer("SELECT COUNT(*) AS STU_COUNT,");

        sqlBuf.append(" COUNT( CASE WHEN S." + StudentDao.Properties.Sex.columnName + "=0 THEN " + StudentDao.Properties.Sex.columnName + " END) AS" +
                " " +
                "MAN_COUNT, ");
        sqlBuf.append(" COUNT( CASE WHEN S." + StudentDao.Properties.Sex.columnName + "=1 THEN " + StudentDao.Properties.Sex.columnName + " END) AS" +
                " " +
                "WOMEM_COUNT ");
        sqlBuf.append("  FROM " + StudentDao.TABLENAME + " S ");
        sqlBuf.append(" WHERE S." + StudentDao.Properties.StudentCode.columnName + " IN ( ");
        sqlBuf.append(" SELECT  " + StudentItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + StudentItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  ");
        sqlBuf.append(" UNION SELECT  " + GroupItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + GroupItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + GroupItemDao.Properties.ItemCode.columnName + " = ?  )");
        sqlBuf.append(" AND  S." + StudentDao.Properties.StudentCode.columnName + " LIKE '%" + studentCode + "%' ");
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{TestConfigs.getCurrentItemCode(), TestConfigs.getCurrentItemCode()});
        Map<String, Object> countMap = new HashMap<>();

        if (c.moveToNext()) {
            int count = c.getInt(0);
            int man_count = c.getInt(1);
            int women_count = c.getInt(2);

            countMap.put("count", count);
            countMap.put("man_count", man_count);
            countMap.put("women_count", women_count);
        }
        c.close();
        return countMap;

    }

    public List<Student> getChooseStudentList(boolean isTested, boolean isUnTested, boolean isUpload, boolean isUnUpload, int limit, int offset) {
        //获取报名项目所有学生
        //查询学生在当前项目个人报名与分组报名的并集里的
        List<Student> studentList = new ArrayList<>();
        StringBuffer sqlBuf = new StringBuffer("SELECT  *");
        sqlBuf.append("  FROM " + StudentDao.TABLENAME);
        sqlBuf.append(" WHERE " + StudentDao.Properties.StudentCode.columnName + " IN ( ");
        sqlBuf.append(" SELECT  " + StudentItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + StudentItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  ");
        sqlBuf.append(" UNION SELECT  " + GroupItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + GroupItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + GroupItemDao.Properties.ItemCode.columnName + " = ?  ) ");
        //加筛选条件
        if (isTested || isUnTested) {
            sqlBuf.append(" AND " + StudentDao.Properties.StudentCode.columnName);
            if (isTested) {
                sqlBuf.append(" IN (SELECT " + RoundResultDao.Properties.StudentCode.columnName + " FROM " + RoundResultDao.TABLENAME + " R");
            } else {
                sqlBuf.append(" NOT IN (SELECT " + RoundResultDao.Properties.StudentCode.columnName + " FROM " + RoundResultDao.TABLENAME + " R");
            }
        }
        if (isUpload || isUnUpload) {
            if (isTested || isUnTested) {

                if (isUpload) {
                    sqlBuf.append("  WHERE R." + RoundResultDao.Properties.UpdateState.columnName + "= 1  AND ");
                } else {
                    sqlBuf.append("  WHERE R." + RoundResultDao.Properties.UpdateState.columnName + "= 0 AND ");
                }
            } else {
                sqlBuf.append(" AND " + StudentDao.Properties.StudentCode.columnName);
                if (isUpload) {
                    sqlBuf.append(" IN (SELECT " +
                            RoundResultDao.Properties.StudentCode.columnName + " FROM " + RoundResultDao.TABLENAME + " R  WHERE R." + RoundResultDao
                            .Properties.UpdateState.columnName + "= 1 AND ");
                } else {
                    sqlBuf.append(" IN (SELECT " +
                            RoundResultDao.Properties.StudentCode.columnName + " FROM " + RoundResultDao.TABLENAME + " R  WHERE R." + RoundResultDao
                            .Properties.UpdateState.columnName + "= 0 AND ");
                }
            }
        } else {
            sqlBuf.append(" WHERE ");
        }
        sqlBuf.append("  R." + RoundResultDao.Properties.ItemCode.columnName + " = '" + TestConfigs.getCurrentItemCode() + "'");
        sqlBuf.append(")  ");
        sqlBuf.append(" limit " + offset + "," + limit);

        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{TestConfigs.getCurrentItemCode(), TestConfigs.getCurrentItemCode()});
        while (c.moveToNext()) {
            Student student = studentDao.readEntity(c, 0);
            studentList.add(student);
        }

        return studentList;
    }

    /**********************************************
     * 项目表
     **********************************************************************/
    /**
     * 根据项目名称获取项目信息
     *
     * @param itemName 项目名称
     * @return
     */
    public Item queryItemByName(String itemName) {
        return itemDao
                .queryBuilder()
                .where(ItemDao.Properties.ItemName.eq(itemName))
                .unique();

    }

    public Item queryItemByCode(String itemCode) {
        return itemDao
                .queryBuilder()
                .where(ItemDao.Properties.ItemCode.eq(itemCode))
                .unique();

    }

    /**
     * 根据项目代码获取项目信息
     *
     * @param itemCode 项目代码
     * @return
     */
    public Item queryItemByItemCode(String itemCode) {
        return itemDao.queryBuilder().where(ItemDao.Properties.ItemCode.eq(itemCode)).unique();
    }

    public Item queryItemByMachineItemCode(int machineCode, String itemCode) {
        return itemDao
                .queryBuilder()
                .where(ItemDao.Properties.MachineCode.eq(machineCode))
                .where(ItemDao.Properties.ItemCode.eq(itemCode))
                .unique();
    }

    /**
     * 根据机器码获取项目列表
     *
     * @param machineCode 机器码
     * @return
     */
    public List<Item> queryItemsByMachineCode(int machineCode) {
//        QueryBuilder.LOG_SQL = true;
//        QueryBuilder.LOG_VALUES = true;
//        return itemDao.queryBuilder().where(ItemDao.Properties.MachineCode.eq(machineCode)).list();
        StringBuffer sqlBuf1 = new StringBuffer("SELECT  * ");
        sqlBuf1.append(" FROM " + ItemDao.TABLENAME);
        sqlBuf1.append(" WHERE " + ItemDao.Properties.MachineCode.columnName + " = ?   ");
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf1.toString(), new String[]{machineCode + ""});
        List<Item> itemList = new ArrayList<>();
        Item item = null;
        while (c.moveToNext()) {
            String itemcode = c.getString(c.getColumnIndex(ItemDao.Properties.ItemCode.columnName));
            Logger.i("itemcode:" + itemcode);
            item = itemDao.readEntity(c, 0);
            itemList.add(item);
        }
        c.close();
        return itemList;
    }

    /**
     * 修改项目信息
     *
     * @param item
     */
    public void updateItem(Item item) {
        itemDao.update(item);
    }

    private void insertItem(int machineCode, String itemName, String unit) {
        Item item = new Item();
        item.setMachineCode(machineCode);
        item.setItemName(itemName);
        item.setUnit(unit);
        itemDao.insert(item);
    }

    private void insertItem(int machineCode, String itemCode, String itemName, String unit) {
        Item item = new Item();
        item.setMachineCode(machineCode);
        item.setItemCode(itemCode);
        item.setItemName(itemName);
        item.setUnit(unit);
        itemDao.insert(item);
    }

    /**
     * 获取所有项目
     *
     * @return
     */
    public List<Item> dumpAllItems() {
        return itemDao
                .queryBuilder()
                .list();
    }

    /**
     * 批量添加项目
     *
     * @param items
     */
    public void insertItems(List<Item> items) {
        itemDao.insertOrReplaceInTx(items);
    }

    /**
     * 项目数据清空
     */
    public void deleteAllItems() {
        itemDao.deleteAll();
    }

    // 刷新所有项目数据
    public void freshAllItems(List<Item> freshItems) {
        db.beginTransaction();
        itemDao.deleteAll();
        itemDao.insertInTx(freshItems);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /*******************************************
     * 学生项目信息表
     ******************************************************************/
    /**
     * 未上传成绩数量
     */
    public int getUnUploadNum() {

        return (int) roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.UpdateState.eq(0))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .count();


    }

    /**
     * 批量添加学生报名
     *
     * @param stuItemList
     */
    public void insertStuItemList(List<StudentItem> stuItemList) {
        if (stuItemList == null || stuItemList.isEmpty()) {
            return;
        }
        studentItemDao.insertOrReplaceInTx(stuItemList);
        studentItemDao.detachAll();
    }

    /**
     * 找出指定学生在当前项目的报名情况
     *
     * @param studentCode 学号
     * @return
     */
    public StudentItem queryStuItemByStuCode(String studentCode) {
        return studentItemDao
                .queryBuilder()
                .where(StudentItemDao.Properties.StudentCode.eq(studentCode))
                .where(StudentItemDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(StudentItemDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .unique();
    }

    /**
     * 添加学生报名
     *
     * @param studentItem
     */
    public void insertStudentItem(StudentItem studentItem) {
        studentItemDao.insert(studentItem);
    }

    public List<StudentItem> querystuItemsByMachineItemCode(int machineCode, String itemCode) {
        return studentItemDao
                .queryBuilder()
                .where(StudentItemDao.Properties.MachineCode.eq(machineCode))
                .where(StudentItemDao.Properties.ItemCode.eq(itemCode))
                .list();
    }

    public List<StudentItem> querystuItemsByStuCode(String studentCode) {
        return studentItemDao
                .queryBuilder()
                .where(StudentItemDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(StudentItemDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(StudentItemDao.Properties.StudentCode.eq(studentCode))
                .list();
    }

    public StudentItem querystuItemByItem(Item item) {
        return studentItemDao
                .queryBuilder()
                .where(StudentItemDao.Properties.MachineCode.eq(item.getMachineCode()))
                .where(StudentItemDao.Properties.ItemCode.eq(item.getItemCode()))
                .unique();
    }

    /**
     * 获取项目代码为默认的所有报名列表
     *
     * @return
     */
    public List<StudentItem> queryStuItemsByItemCodeDefault() {
        return studentItemDao
                .queryBuilder()
                .where(StudentItemDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(StudentItemDao.Properties.ItemCode.eq(TestConfigs.DEFAULT_ITEM_CODE))
                .list();
    }

    public List<StudentItem> queryStuItemsByItemCodeDefault(int machineCode) {
        return studentItemDao
                .queryBuilder()
                .where(StudentItemDao.Properties.MachineCode.eq(machineCode))
                .where(StudentItemDao.Properties.ItemCode.eq(TestConfigs.DEFAULT_ITEM_CODE))
                .list();
    }


    /**
     * 更新报名列表信息
     *
     * @param stuItems
     */
    public void updateStudentItem(List<StudentItem> stuItems) {
        studentItemDao.updateInTx(stuItems);
    }

    public void updateStudentItem(StudentItem stuItem) {
        studentItemDao.update(stuItem);
    }

    /**********************************************
     * 成绩表
     **********************************************************************/
    /**
     * 根据学生号获取该学生的测试成绩列表
     *
     * @param studentCode
     * @return
     */
    public List<RoundResult> queryResultsByStudentCode(String studentCode) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .list();
    }

    public List<Student> getStudentsByGroup(Group group) {
        List<GroupItem> groupItems = groupItemDao
                .queryBuilder()
                .where(GroupItemDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(GroupItemDao.Properties.ScheduleNo.eq(group.getScheduleNo()))
                .where(GroupItemDao.Properties.GroupType.eq(group.getGroupType()))
                .where(GroupItemDao.Properties.SortName.eq(group.getSortName()))
                .where(GroupItemDao.Properties.GroupNo.eq(group.getGroupNo()))
                .list();
        List<Student> result = null;
        if (groupItems != null && groupItems.size() > 0) {
            result = new ArrayList<>();
            for (GroupItem groupItem : groupItems) {
                result.add(queryStudentByStuCode(groupItem.getStudentCode()));
            }
        }
        return result;
    }

    /**
     * 单人测试模式下当前项目的所有成绩
     */
    public List<RoundResult> queryResultsByStuItem(StudentItem studentItem) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.MachineCode.eq(studentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(studentItem.getItemCode()))
                .where(RoundResultDao.Properties.StudentCode.eq(studentItem.getStudentCode()))
                .where(RoundResultDao.Properties.GroupId.eq(RoundResult.DEAFULT_GROUP_ID))
                .where(RoundResultDao.Properties.ExamType.eq(studentItem.getExamType()))
                .list();
    }

    /**
     * 当前项目的所有成绩
     */
    public List<RoundResult> queryResultsByStuItemExamType(String studentCode) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .list();
    }

    public List<RoundResult> queryResultsByStudentCode(String studentCode, Item item) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getItemCode(item)))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .list();
    }

    /**
     * 查询已有的最后一次成绩
     */
    public RoundResult queryLastScoreByStuCode(String stuCode) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(stuCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .orderDesc(RoundResultDao.Properties.TestTime)
                .limit(1)
                .unique();
    }

    public RoundResult queryLastIndividualScoreByStuCode(String stuCode) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(stuCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.GroupId.eq(RoundResult.DEAFULT_GROUP_ID))
                .orderDesc(RoundResultDao.Properties.TestNo)
                .orderDesc(RoundResultDao.Properties.RoundNo)
                .limit(1)
                .unique();
    }

    public RoundResult queryLastScoreByStuCode(String stuCode, Item item) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(stuCode))
                .where(RoundResultDao.Properties.MachineCode.eq(item.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getItemCode(item)))
                .orderDesc(RoundResultDao.Properties.TestTime)
                .limit(1)
                .unique();
    }

    /**
     * 获取所有项目为默认的成绩列表
     */
    public List<RoundResult> queryResultsByItemCodeDefault() {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.DEFAULT_ITEM_CODE))
                .list();
    }

    public List<RoundResult> queryResultsByItemCodeDefault(int machineCode) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.MachineCode.eq(machineCode))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.DEFAULT_ITEM_CODE))
                .list();
    }

    /**
     * 批量修改成绩信息
     *
     * @param allScores
     */
    public void updateRoundResult(List<RoundResult> allScores) {
        roundResultDao.updateInTx(allScores);
    }

    /**
     * 更新成绩
     *
     * @param score
     */
    public void updateRoundResult(RoundResult score) {
        roundResultDao.update(score);
    }

    /**
     * 根据学生号获取是否上传的成绩列表
     *
     * @param studentCode
     * @param upLoaded
     * @return
     */
    public List<RoundResult> queryUploadStudentResults(String studentCode, boolean upLoaded) {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.UpdateState.eq(upLoaded ? 1 : 0))
                .list();
    }

    /**
     * 添加成绩
     *
     * @param roundResult
     */
    public void insertRoundResult(RoundResult roundResult) {
        roundResultDao.insert(roundResult);
    }

    /**
     * 查询对应考生当前项目最好成绩(个人)
     *
     * @param studentCode 考号
     * @return 对应最好成绩
     */
    public RoundResult queryBestScore(String studentCode, int testNo) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\ttestNo:" + testNo);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.TestNo.eq(testNo))
                .where(RoundResultDao.Properties.IsLastResult.eq(1))
                .unique();
    }

    /**
     * 查询某个项目某个性别某个组次的所有人
     *
     * @param itemCode
     * @param sort
     * @param sex
     * @return
     */
    public List<GroupItem> queryGroupItem(String itemCode, int sort, int sex) {
        return groupItemDao.queryBuilder().where(GroupItemDao.Properties.GroupNo.eq(sort))
                .where(GroupItemDao.Properties.ItemCode.eq(itemCode))
                .where(GroupItemDao.Properties.GroupType.eq(sex)).list();
    }

    /**
     * 查询对应考生当前项目最好成绩(分组)
     *
     * @param studentCode 考号
     * @return 对应最好成绩
     */
    public RoundResult queryGroupBestScore(String studentCode, long groupId) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\tIsLastResult:" + 1);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.GroupId.eq(groupId))
                .where(RoundResultDao.Properties.IsLastResult.eq(1))
                .limit(1)
                .unique();
    }


    public RoundResult queryBestScore(String studentCode, Item item) {
        //Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
        //		+ "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\tIsLastResult:" + 1);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(item.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getItemCode(item)))
                .where(RoundResultDao.Properties.IsLastResult.eq(1))
                .unique();
    }

    /**
     * 查询对应考生当前项目最后一次成绩
     *
     * @param studentCode 考号
     * @return
     */
    public RoundResult queryFinallyRountScore(String studentCode) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\tIsLastResult:" + 1);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .orderDesc(RoundResultDao.Properties.TestNo)
                .limit(1)
                .unique();
    }

    /**
     * 查询对应考生当前项目最后一次成绩
     *
     * @param studentCode 考号
     * @return
     */
    public List<RoundResult> queryFinallyRountScoreByExamTypeList(String studentCode, int exemType) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\tIsLastResult:" + 1);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.ExamType.eq(exemType))
                .orderDesc(RoundResultDao.Properties.TestNo)
                .list();
    }

    /**
     * 查询分组对应考生当前项目最后一次成绩
     *
     * @param studentCode 考号
     * @return
     */
    public RoundResult queryGroupFinallyRountScore(String studentCode, String groupId) {

        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.GroupId.eq(groupId))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .orderDesc(RoundResultDao.Properties.TestNo)
                .limit(1)
                .unique();
    }

    /**
     * 查询分组对应考生当前项目最后一次成绩
     *
     * @param studentCode 考号
     * @param groupId     分组ID
     * @param roundNo     测试轮次
     * @return
     */
    public RoundResult queryGroupRoundNoResult(String studentCode, String groupId, int roundNo) {

        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.GroupId.eq(groupId))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.RoundNo.eq(roundNo))
                .unique();
    }

    /**
     * 查询对应考生当前项目轮次所有成绩
     *
     * @param studentCode 考号
     * @return
     */
    public List<RoundResult> queryGroupRound(String studentCode, String groupId) {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.GroupId.eq(groupId))
                .list();
    }

    /**
     * 查询对应考生当前项目轮次所有成绩
     *
     * @param studentCode 考号
     * @return
     */
    public List<RoundResult> queryRoundByRoundNo(String studentCode, int roundNo) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\tIsLastResult:" + 1);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.RoundNo.eq(roundNo))
                .list();
    }

    /**
     * 获取学生最好的成绩
     *
     * @param studentCode
     * @return
     */
    public RoundResult queryResultsByStudentCodeIsLastResult(String studentCode) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.IsLastResult.eq("1"))
                .limit(1)
                .unique();
    }

    /**
     * 根据学生考号删除成绩
     *
     * @param studentCode
     */
    public void deleteStuResult(String studentCode) {
        roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }

    /**
     * 根据项目删除成绩
     */
    public void deleteItemResult() {
        roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }

    /**
     * 获取当前项目学生未上传数据
     *
     * @return
     */
    public List<UploadResults> getUploadResultsAll() {

        //查成绩表去重学生号  条件当前项目未上传成绩
        //获取根据考生号 获取考生当前项目未上传生所有成绩
        //根据成绩分组id 分配上传成绩内容（按分组、日程）

        List<String> stuCodeList = new ArrayList<>();
        StringBuffer sqlBuf1 = new StringBuffer("SELECT  DISTINCT " + RoundResultDao.Properties.StudentCode.columnName);
        sqlBuf1.append(" FROM " + RoundResultDao.TABLENAME);
        sqlBuf1.append(" WHERE " + RoundResultDao.Properties.UpdateState.columnName + " = ? AND ");
        sqlBuf1.append(RoundResultDao.Properties.ItemCode.columnName + " =  ?  AND ");
        sqlBuf1.append(RoundResultDao.Properties.MachineCode.columnName + " = ? ");
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf1.toString(), new String[]{"0", TestConfigs.getCurrentItemCode(), TestConfigs.sCurrentItem.getMachineCode() + ""});
        while (c.moveToNext()) {
            stuCodeList.add(c.getString(0));
        }
        c.close();
        List<UploadResults> uploadResultsList = new ArrayList<>();
        for (String stuCode : stuCodeList) {
            //获取学生未上传成绩
            List<RoundResult> stuResult = roundResultDao.queryBuilder().where(RoundResultDao.Properties.StudentCode.eq(stuCode))
                    .where(RoundResultDao.Properties.UpdateState.eq(0))
                    .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                    .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                    .list();
            Map<Long, List<RoundResult>> groupResult = new HashMap<>();
            Map<String, List<RoundResult>> personResult = new HashMap<>();
            //成绩筛选
            for (RoundResult result : stuResult) {
                //是否有分组
                if (result.getGroupId() > 0) {
                    //按分组ID保存成绩
                    List<RoundResult> saveResult = groupResult.get(result.getGroupId());
                    if (saveResult != null) {
                        saveResult.add(result);
                    } else {
                        saveResult = new ArrayList<>();
                        saveResult.add(result);
                    }
                    groupResult.put(result.getGroupId(), saveResult);
                } else {
                    //按日程No保存成绩
                    List<RoundResult> saveResult = personResult.get(TextUtils.isEmpty(result.getScheduleNo()) ? "-1" : result.getScheduleNo());
                    if (saveResult != null) {
                        saveResult.add(result);
                    } else {
                        saveResult = new ArrayList<>();
                        saveResult.add(result);
                    }
                    personResult.put(TextUtils.isEmpty(result.getScheduleNo()) ? "-1" : result.getScheduleNo(), saveResult);
                }
            }
            //处理个人日程上传数据
            for (Map.Entry<String, List<RoundResult>> entity : personResult.entrySet()) {
                List<RoundResult> saveResult = entity.getValue();
                //测试次数筛选数据
                Map<Integer, List<RoundResult>> testNumResult = new HashMap<>();
                for (RoundResult result : saveResult) {
                    //按分组ID保存成绩
                    List<RoundResult> resultList = testNumResult.get(result.getTestNo());
                    if (resultList != null) {
                        resultList.add(result);
                    } else {
                        resultList = new ArrayList<>();
                        resultList.add(result);
                    }
                    testNumResult.put(result.getTestNo(), resultList);
                }
                //处理上传数据
                for (Map.Entry<Integer, List<RoundResult>> testEntity : testNumResult.entrySet()) {
                    if (!TextUtils.isEmpty(entity.getKey())) {

                        UploadResults uploadResults = new UploadResults(
                                TextUtils.equals(testEntity.getValue().get(0).getScheduleNo(), "-1") ? "" : testEntity.getValue().get(0).getScheduleNo(),
                                TestConfigs.getCurrentItemCode(), testEntity.getValue().get(0).getStudentCode(), testEntity.getKey() + "",
                                null, RoundResultBean.beanCope(entity.getValue()));
                        uploadResultsList.add(uploadResults);
                    }
                }

            }
            for (Map.Entry<Long, List<RoundResult>> entity : groupResult.entrySet()) {
                //获取分组
                Group group = groupDao.queryBuilder().where(GroupDao.Properties.Id.eq(entity.getKey())).unique();
                if (group != null) {
                    List<RoundResult> saveResult = entity.getValue();
                    UploadResults uploadResults = new UploadResults(group.getScheduleNo(),
                            TestConfigs.getCurrentItemCode(), saveResult.get(0).getStudentCode(), "1",
                            group.getGroupNo() + "", RoundResultBean.beanCope(saveResult));
                    uploadResultsList.add(uploadResults);
                }
            }
        }

        return uploadResultsList;
    }

    /**
     * 获取当前项目学生未上传数据
     *
     * @return
     */
    public List<UploadResults> getUploadResultsByStuCode(List<String> stuCodeList) {

        //查成绩表去重学生号  条件当前项目未上传成绩
        //获取根据考生号 获取考生当前项目未上传生所有成绩
        //根据成绩分组id 分配上传成绩内容（按分组、日程）
        if (stuCodeList == null || stuCodeList.size() == 0)
            return null;

        List<UploadResults> uploadResultsList = new ArrayList<>();
        for (String stuCode : stuCodeList) {
            //获取学生未上传成绩
            List<RoundResult> stuResult = roundResultDao.queryBuilder().where(RoundResultDao.Properties.StudentCode.eq(stuCode))
//                    .where(RoundResultDao.Properties.UpdateState.eq(0))
                    .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                    .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                    .list();
            Map<Long, List<RoundResult>> groupResult = new HashMap<>();
            Map<String, List<RoundResult>> personResult = new HashMap<>();
            //成绩筛选
            for (RoundResult result : stuResult) {
                //是否有分组
                if (result.getGroupId() > 0) {
                    //按分组ID保存成绩
                    List<RoundResult> saveResult = groupResult.get(result.getGroupId());
                    if (saveResult != null) {
                        saveResult.add(result);
                    } else {
                        saveResult = new ArrayList<>();
                        saveResult.add(result);
                    }
                    groupResult.put(result.getGroupId(), saveResult);
                } else {
                    //按日程No保存成绩
                    List<RoundResult> saveResult = personResult.get(TextUtils.isEmpty(result.getScheduleNo()) ? "-1" : result.getScheduleNo());
                    if (saveResult != null) {
                        saveResult.add(result);
                    } else {
                        saveResult = new ArrayList<>();
                        saveResult.add(result);
                    }
                    personResult.put(TextUtils.isEmpty(result.getScheduleNo()) ? "-1" : result.getScheduleNo(), saveResult);
                }
            }
            //处理个人日程上传数据
            for (Map.Entry<String, List<RoundResult>> entity : personResult.entrySet()) {
                List<RoundResult> saveResult = entity.getValue();
                //测试次数筛选数据
                Map<Integer, List<RoundResult>> testNumResult = new HashMap<>();
                for (RoundResult result : saveResult) {
                    //按分组ID保存成绩
                    List<RoundResult> resultList = testNumResult.get(result.getTestNo());
                    if (resultList != null) {
                        resultList.add(result);
                    } else {
                        resultList = new ArrayList<>();
                        resultList.add(result);
                    }
                    testNumResult.put(result.getTestNo(), resultList);
                }
                //处理上传数据
                for (Map.Entry<Integer, List<RoundResult>> testEntity : testNumResult.entrySet()) {
                    if (!TextUtils.isEmpty(entity.getKey())) {

                        UploadResults uploadResults = new UploadResults(
                                TextUtils.equals(testEntity.getValue().get(0).getScheduleNo(), "-1") ? "" : testEntity.getValue().get(0).getScheduleNo(),
                                TestConfigs.getCurrentItemCode(), testEntity.getValue().get(0).getStudentCode(), testEntity.getKey() + "",
                                "", RoundResultBean.beanCope(entity.getValue()));
                        uploadResultsList.add(uploadResults);
                    }
                }

            }
            for (Map.Entry<Long, List<RoundResult>> entity : groupResult.entrySet()) {
                //获取分组
                Group group = groupDao.queryBuilder().where(GroupDao.Properties.Id.eq(entity.getKey())).unique();
                if (group != null) {
                    List<RoundResult> saveResult = entity.getValue();

                    UploadResults uploadResults = new UploadResults(group.getScheduleNo(),
                            TestConfigs.getCurrentItemCode(), saveResult.get(0).getStudentCode(), "1",
                            group.getGroupNo() + "", RoundResultBean.beanCope(saveResult));
                    uploadResultsList.add(uploadResults);
                }
            }
        }

        return uploadResultsList;
    }


    /**
     * 获取当前项目学生成绩数据
     *
     * @return
     */
    public List<Map<String, Object>> getResultsByStu(List<Student> stuCodeList) {

        //查成绩表去重学生号
        //获取根据考生号 获取考生当前项目所有成绩
        //根据成绩分组id 分配上传成绩内容（按分组、日程）
        if (stuCodeList == null || stuCodeList.size() == 0)
            return null;
        List<Map<String, Object>> stuResults = new ArrayList<>();
        List<UploadResults> uploadResultsList;
        Map<String, Object> dataMap;
        for (Student stu : stuCodeList) {
            uploadResultsList = new ArrayList<>();
            dataMap = new HashMap<>();
            dataMap.put("stu", stu);
            //获取学生未上传成绩
            List<RoundResult> stuResult = roundResultDao.queryBuilder().where(RoundResultDao.Properties.StudentCode.eq(stu.getStudentCode()))
                    .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                    .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                    .list();
            Map<Long, List<RoundResult>> groupResult = new HashMap<>();
            Map<String, List<RoundResult>> personResult = new HashMap<>();
            //成绩筛选
            for (RoundResult result : stuResult) {
                //是否有分组
                if (result.getGroupId() > 0) {
                    //按分组ID保存成绩
                    List<RoundResult> saveResult = groupResult.get(result.getGroupId());
                    if (saveResult != null) {
                        saveResult.add(result);
                    } else {
                        saveResult = new ArrayList<>();
                        saveResult.add(result);
                    }
                    groupResult.put(result.getGroupId(), saveResult);
                } else {
                    //按日程No保存成绩
                    List<RoundResult> saveResult = personResult.get(TextUtils.isEmpty(result.getScheduleNo()) ? "-1" : result.getScheduleNo());
                    if (saveResult != null) {
                        saveResult.add(result);
                    } else {
                        saveResult = new ArrayList<>();
                        saveResult.add(result);
                    }
                    personResult.put(TextUtils.isEmpty(result.getScheduleNo()) ? "-1" : result.getScheduleNo(), saveResult);
                }
            }
            //处理个人日程上传数据
            for (Map.Entry<String, List<RoundResult>> entity : personResult.entrySet()) {
                List<RoundResult> saveResult = entity.getValue();
                //测试次数筛选数据
                Map<Integer, List<RoundResult>> testNumResult = new HashMap<>();
                for (RoundResult result : saveResult) {
                    //按测试次数保存成绩
                    List<RoundResult> resultList = testNumResult.get(result.getTestNo());
                    if (resultList != null) {
                        resultList.add(result);
                    } else {
                        resultList = new ArrayList<>();
                        resultList.add(result);
                    }
                    testNumResult.put(result.getTestNo(), resultList);
                }
                //处理上传数据
                for (Map.Entry<Integer, List<RoundResult>> testEntity : testNumResult.entrySet()) {
                    if (!TextUtils.isEmpty(entity.getKey())) {

                        UploadResults uploadResults = new UploadResults(
                                TextUtils.equals(testEntity.getValue().get(0).getScheduleNo(), "-1") ? "" : testEntity.getValue().get(0).getScheduleNo(),
                                TestConfigs.getCurrentItemCode(), stu.getStudentCode(), testEntity.getKey() + "",
                                "", RoundResultBean.beanCope(entity.getValue()));
                        uploadResultsList.add(uploadResults);
                    }
                }

            }
            for (Map.Entry<Long, List<RoundResult>> entity : groupResult.entrySet()) {
                //获取分组
                Group group = groupDao.queryBuilder().where(GroupDao.Properties.Id.eq(entity.getKey())).unique();
                if (group != null) {
                    List<RoundResult> saveResult = entity.getValue();

                    UploadResults uploadResults = new UploadResults(group.getScheduleNo(),
                            TestConfigs.getCurrentItemCode(), stu.getStudentCode(), "1",
                            group.getGroupNo() + "", RoundResultBean.beanCope(saveResult));
                    uploadResultsList.add(uploadResults);
                }
            }
            dataMap.put("results", uploadResultsList);
            stuResults.add(dataMap);
        }

        return stuResults;
    }


    /****************************分组***********************************/

    /**
     * 获取当前测试项目对应的所有日程
     */
    public List<Schedule> getCurrentSchedules() {
        List<Schedule> result = null;
        List<ItemSchedule> list = itemScheduleDao.queryBuilder()
                .where(ItemScheduleDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .list();
        Schedule schedule;
        if (list != null && list.size() > 0) {
            result = new ArrayList<>();
            for (ItemSchedule itemSchedule : list) {
                schedule = scheduleDao.queryBuilder()
                        .where(ScheduleDao.Properties.ScheduleNo.eq(itemSchedule.getScheduleNo()))
                        .unique();
                if (schedule != null) {
                    result.add(schedule);
                }
            }
        }
        return result;
    }

    public Schedule getSchedulesByNo(String scheduleNo) {
        return scheduleDao.queryBuilder().where(ScheduleDao.Properties.ScheduleNo.eq(scheduleNo)).unique();
    }

    /**
     * 获取日程
     *
     * @return
     */
    public List<Schedule> getAllSchedules() {
        StringBuffer sqlBuf = new StringBuffer("SELECT S.*  ");
        sqlBuf.append("  FROM " + ScheduleDao.TABLENAME + " S");
        sqlBuf.append(" LEFT JOIN " + ItemScheduleDao.TABLENAME + " I ");
        sqlBuf.append(" ON S." + ScheduleDao.Properties.ScheduleNo.columnName + " = I." + ItemScheduleDao.Properties.ScheduleNo.columnName);
        sqlBuf.append(" WHERE I." + GroupItemDao.Properties.ItemCode.columnName + " = '" + TestConfigs.getCurrentItemCode() + "'");
        List<Schedule> scheduleList = new ArrayList<>();
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), null);
        while (c.moveToNext()) {
            Schedule schedule = scheduleDao.readEntity(c, 0);
            scheduleList.add(schedule);
        }
        c.close();
        return scheduleList;
    }

    public long insertSchedules(Schedule schedule) {
        return scheduleDao.insertOrReplace(schedule);
    }

    public void insertSchedulesList(List<Schedule> scheduleList) {
        scheduleDao.insertOrReplaceInTx(scheduleList);
    }

    private void insertItemSchedule(ItemSchedule itemSchedule) {
        itemScheduleDao.insertOrReplace(itemSchedule);
    }

    public void insertItemSchedulesList(final List<ItemSchedule> itemScheduleList) {
        //已经存在这条数据，则忽略。
        daoSession.runInTx(new Runnable() {
            @Override
            public void run() {
                for (ItemSchedule itemSchedule : itemScheduleList) {
                    try {
//                        itemScheduleDao.insert(itemSchedule);
                        StringBuffer sqlBuf = new StringBuffer("INSERT OR IGNORE INTO ");
                        sqlBuf.append(ItemScheduleDao.TABLENAME + " ( ");
                        sqlBuf.append(ItemScheduleDao.Properties.ItemCode.columnName + " , ");
                        sqlBuf.append(ItemScheduleDao.Properties.ScheduleNo.columnName + " ) ");
                        sqlBuf.append(" VALUES (?,?)");
                        daoSession.getDatabase().execSQL(sqlBuf.toString(), new String[]{itemSchedule.getItemCode(), itemSchedule.getScheduleNo()});

                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.e("insertItemSchedulesList", "execSQL");
                    }
                }
            }
        });

//        itemScheduleDao.insertOrReplaceInTx(itemScheduleList);
    }

    /**
     * 查看分组报名的学生
     *
     * @return
     */
    public List<Map<String, Object>> getStudenByStuItemAndGroup(Group group) {
        StringBuffer sqlBuf = new StringBuffer("SELECT S.* , I." + GroupItemDao.Properties.TrackNo.columnName);
        sqlBuf.append("  FROM " + StudentDao.TABLENAME + " S");
        sqlBuf.append(" LEFT JOIN " + GroupItemDao.TABLENAME + " I ");
        sqlBuf.append(" ON S." + StudentDao.Properties.StudentCode.columnName + " = I." + GroupItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" WHERE I." + GroupItemDao.Properties.ItemCode.columnName + " = '" + TestConfigs.getCurrentItemCode() + "'");
        sqlBuf.append(" AND I." + GroupItemDao.Properties.ScheduleNo.columnName + " = " + group.getScheduleNo());
        sqlBuf.append(" AND I." + GroupItemDao.Properties.GroupType.columnName + " =  " + group.getGroupType());
        sqlBuf.append(" AND I." + GroupItemDao.Properties.SortName.columnName + " = '" + group.getSortName() + "'");
        sqlBuf.append(" AND I." + GroupItemDao.Properties.GroupNo.columnName + " =  " + group.getGroupNo());
//        sqlBuf.append(" ORDER BY " + GroupItemDao.Properties.TrackNo.columnName + " ASC ");

        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), null);

        List<Map<String, Object>> students = new ArrayList<>();
        while (c.moveToNext()) {
            Map<String, Object> map = new HashMap<>();
            Student student = studentDao.readEntity(c, 0);
            map.put("student", student);
            map.put("trackNo", c.getInt(c.getColumnIndex(GroupItemDao.Properties.TrackNo.columnName)));
            students.add(map);
        }
        c.close();
        return students;
    }

    public List<Group> getGroupByScheduleNo(String scheduleNo) {
        return groupDao.queryBuilder()
                .where(GroupDao.Properties.ScheduleNo.eq(scheduleNo))
                .where(GroupDao.Properties.ItemCode.eq(TestConfigs.sCurrentItem.getItemCode()))
                .list();
    }

    public List<Group> getGroupByScheduleNoAndItem(String scheduleNo, String itemCode) {
        return groupDao.queryBuilder()
                .where(GroupDao.Properties.ScheduleNo.eq(scheduleNo))
                .where(GroupDao.Properties.ItemCode.eq(itemCode))
                .list();
    }


    /**
     * 添加分组
     *
     * @param group
     */
    public long insertGroup(Group group) {
        return groupDao.insertOrReplace(group);
    }

    /**
     * 批量添加分组
     *
     * @param groupList
     */
//    public void insertGroupList(List<Group> groupList) {
//        if (groupList == null || groupList.isEmpty()) {
//            return;
//        }
//        groupDao.insertOrReplaceInTx(groupList);
//        groupDao.detachAll();
//    }
    public void insertGroupList(final List<Group> groupList) {
        //已经存在这条数据，则忽略。
        daoSession.runInTx(new Runnable() {
            @Override
            public void run() {
                for (Group group : groupList) {
                    try {
                        StringBuffer sqlBuf = new StringBuffer("INSERT OR IGNORE INTO ");
                        sqlBuf.append(GroupDao.TABLENAME + " ( ");
                        sqlBuf.append(GroupDao.Properties.GroupType.columnName + " , ");
                        sqlBuf.append(GroupDao.Properties.SortName.columnName + " , ");
                        sqlBuf.append(GroupDao.Properties.GroupNo.columnName + " , ");
                        sqlBuf.append(GroupDao.Properties.ScheduleNo.columnName + " , ");
                        sqlBuf.append(GroupDao.Properties.ExamType.columnName + " , ");
                        sqlBuf.append(GroupDao.Properties.IsTestComplete.columnName + " , ");
                        sqlBuf.append(GroupDao.Properties.ItemCode.columnName + " , ");
                        sqlBuf.append(GroupDao.Properties.Remark1.columnName + " , ");
                        sqlBuf.append(GroupDao.Properties.Remark2.columnName + " , ");
                        sqlBuf.append(GroupDao.Properties.Remark3.columnName + " ) ");
                        sqlBuf.append(" VALUES (?,?,?,?,?,?,?,?,?,?)");
                        daoSession.getDatabase().execSQL(sqlBuf.toString(), new String[]{group.getGroupType() + "", group.getSortName(), group.getGroupNo() + "",
                                group.getScheduleNo() + "", group.getExamType() + "", group.getIsTestComplete() + "", group.getItemCode(), group.getRemark1(), group.getRemark2(), group.getRemark3()});
                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.e("insertGroupList", "execSQL");
                    }
                }
            }
        });

//        itemScheduleDao.insertOrReplaceInTx(itemScheduleList);
    }


    /**
     * 更新分组信息
     *
     * @param group
     */
    public void updateGroup(Group group) {
        groupDao.update(group);
    }

    /**
     * 批量添加分组学生报名
     *
     * @param groupItemList
     */
    public void insertGroupItemList(List<GroupItem> groupItemList) {
        if (groupItemList == null || groupItemList.isEmpty()) {
            return;
        }
        groupItemDao.insertOrReplaceInTx(groupItemList);
        groupItemDao.detachAll();
    }

    /**
     * 添加分组学生报名
     *
     * @param groupItem
     */
    public void insertGroupItem(GroupItem groupItem) {
        if (groupItem == null) {
            return;
        }
        groupItemDao.insertOrReplace(groupItem);
    }


    public GroupItem getItemStuGroupItem(Group group, String studentCode) {
        return groupItemDao.queryBuilder()
                .where(GroupItemDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(GroupItemDao.Properties.ScheduleNo.eq(group.getScheduleNo()))
                .where(GroupItemDao.Properties.GroupType.eq(group.getGroupType()))
                .where(GroupItemDao.Properties.SortName.eq(group.getSortName()))
                .where(GroupItemDao.Properties.GroupNo.eq(group.getGroupNo()))
                .where(GroupItemDao.Properties.StudentCode.eq(studentCode))
                .unique();
    }

    /**
     * 更新学生分组信息
     *
     * @param groupItem
     */
    public void updateStudentGroupItem(GroupItem groupItem) {
        groupItemDao.update(groupItem);

    }

    /******************** 机器成绩表 **************/
    public List<MachineResult> getItemRoundMachineResult(String stuCode, int testNo, int roundNo) {
        return machineResultDao.queryBuilder().where(MachineResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(MachineResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(MachineResultDao.Properties.StudentCode.eq(stuCode))
                .where(MachineResultDao.Properties.TestNo.eq(testNo))
                .where(MachineResultDao.Properties.RoundNo.eq(roundNo)).list();
    }

    public void insterMachineResult(MachineResult machineResult) {
        machineResultDao.insert(machineResult);
    }
    /********************************************多表操作**********************************************************************/

    /**
     * 关闭数据库
     */
    public void close() {
        db.close();
    }

    public boolean isTestedInGroup(Student student, Group group) {
        List<RoundResult> list = roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.GroupId.eq(group.getId()))
                .where(RoundResultDao.Properties.StudentCode.eq(student.getStudentCode()))
                .list();
        return list != null && list.size() > 0;
    }

}
