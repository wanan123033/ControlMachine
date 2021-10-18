package com.feipulai.host.db;

import android.database.Cursor;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.host.BuildConfig;
import com.feipulai.host.MyApplication;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.DaoMaster;
import com.feipulai.host.entity.DaoSession;
import com.feipulai.host.entity.Item;
import com.feipulai.host.entity.ItemDao;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.RoundResultDao;
import com.feipulai.host.entity.Student;
import com.feipulai.host.entity.StudentDao;
import com.feipulai.host.entity.StudentItem;
import com.feipulai.host.entity.StudentItemDao;
import com.feipulai.host.utils.EncryptUtil;
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
    public static final String DB_PASSWORD = "FairPlay_2019";
    private static DBManager mInstance;
    private static ItemDao itemDao;
    private static StudentDao studentDao;
    private static StudentItemDao studentItemDao;
    private static RoundResultDao roundResultDao;
    private static Database db;
    private static DaoSession daoSession;
    public static DBOpenHelper helper;
    private DaoMaster daoMaster;

    private DBManager() {
    }

    /**
     * 这里不要使用双重检查,可能机器本身的虚拟机是比较老的版本了,双重检查可能出错
     * 将原有的在MyApplication的初始化数据库代码移动到这里,原有的代码在切换项目时可能出现NullPointerException,
     * 导致应用奔溃,概率1%左右
     */
    public synchronized static DBManager getInstance() {
        if (mInstance == null) {
            mInstance = new DBManager();
            //使用单例来保证数据库初始化,这该死的机器竟然可能在不需要的情况下清除application类的数据
            mInstance.initDB();
        }
        return mInstance;
    }

    /**
     * 数据库初始化
     */
    public void initDB() {
        // if (db != null) {
        //     db.close();
        // }
        helper = new DBOpenHelper(MyApplication.getInstance(), DB_NAME);
        db = BuildConfig.DEBUG ? helper.getWritableDb() : helper.getEncryptedReadableDb(DB_PASSWORD);
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        studentDao = daoSession.getStudentDao();
        itemDao = daoSession.getItemDao();
        studentItemDao = daoSession.getStudentItemDao();
        roundResultDao = daoSession.getRoundResultDao();

        int[] supportMachineCodes = {ItemDefault.CODE_HW, ItemDefault.CODE_TS, ItemDefault.CODE_YWQZ,
                ItemDefault.CODE_LDTY, ItemDefault.CODE_ZWTQQ,
                ItemDefault.CODE_HWSXQ, ItemDefault.CODE_FHL, ItemDefault.CODE_ZFP, ItemDefault.CODE_WLJ, ItemDefault.CODE_YTXS,
                ItemDefault.CODE_JGCJ, ItemDefault.CODE_SL, ItemDefault.CODE_SGBQS,ItemDefault.CODE_SPORT_TIMER};
        for (int machineCode : supportMachineCodes) {
            //查询是否已经存在该机器码的项,如果存在就放弃,避免重复添加
            List<Item> items = itemDao.queryBuilder().where(ItemDao.Properties.MachineCode.eq(machineCode)).list();
            if (items != null && items.size() != 0) {
                continue;
            }
            switch (machineCode) {
                case ItemDefault.CODE_HW:
                    insertItem(machineCode, TestConfigs.HEIGHT_ITEM_CODE, "身高", "厘米");
                    insertItem(machineCode, TestConfigs.WEIGHT_ITEM_CODE, "体重", "千克");
                    break;

                case ItemDefault.CODE_TS:
                    insertItem(machineCode, "跳绳", "次");
                    break;

                case ItemDefault.CODE_YWQZ:
                    insertItem(machineCode, "仰卧起坐", "次");
                    break;

                case ItemDefault.CODE_LDTY:
                    insertItem(machineCode, "立定跳远", "厘米");
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
                    insertItem(machineCode, "红外计时", "毫秒");
                    break;
                case ItemDefault.CODE_SPORT_TIMER:
                    insertItem(machineCode, "运动计时", "毫秒");
                    break;
                case ItemDefault.CODE_WLJ:
                    insertItem(machineCode, "握力", "千克");
                    break;
                case ItemDefault.CODE_YTXS:
                    insertItem(machineCode, "引体向上", "次");
                    break;
                case ItemDefault.CODE_JGCJ:
                    insertItem(machineCode, "激光测距", "米");
                    break;
                case ItemDefault.CODE_SL:
                    insertItem(machineCode, "视力", "");
                    break;
                case ItemDefault.CODE_SGBQS:
                    insertItem(machineCode, "双杠臂屈伸", "次");
                    break;

            }
        }
        Logger.i("数据库初始化完成");
    }

    //清空数据库
    public void clear() {
        db.beginTransaction();
        itemDao.deleteAll();
        studentDao.deleteAll();
        studentItemDao.deleteAll();
        roundResultDao.deleteAll();
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * 成绩表数据清空
     */
    public void roundResultClear() {
        roundResultDao.deleteAll();
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
    /********************************************
     * 学生表
     ********************************************/
    public void deleteAllStudent(){
        studentItemDao.deleteAll();
        studentDao.deleteAll();
    }
    /**
     * 根据学生考号获取学生信息
     *
     * @param stuCode 考号
     * @return
     */
    public Student queryStudentByStuCode(final String stuCode) {

        Student student = studentDao.queryBuilder()
                .where(StudentDao.Properties.StudentCode.eq(stuCode))
                .limit(1)
                .unique();
        return student;
    }

    public Student queryStudentByCode(String code) {
        Student student = studentDao.queryBuilder()
                .where(StudentDao.Properties.StudentCode.eq(code))
                .unique();
        if (student == null) {
            student = studentDao.queryBuilder()
                    .where(StudentDao.Properties.IdCardNo.eq(EncryptUtil.setEncryptString(Student.ENCRYPT_KEY, code)))
                    .unique();
        }
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

    public void updateStudent(Student student) {
        studentDao.updateInTx(student);
    }

    public List<Student> getStudentByPortrait() {
        StringBuffer sqlBuf = new StringBuffer("SELECT S.* FROM " + StudentDao.TABLENAME + " S");
        sqlBuf.append(" WHERE S." + StudentDao.Properties.StudentCode.columnName + " IN ( ");
        sqlBuf.append(" SELECT  " + StudentItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + StudentItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  )");
        sqlBuf.append(" AND  S." + StudentDao.Properties.Portrait.columnName + " != '' ");
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{TestConfigs.getCurrentItemCode()});
        List<Student> students = new ArrayList<>();
        while (c.moveToNext()) {
            Student student = studentDao.readEntity(c, 0);
            students.add(student);
        }
        return students;

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
                .where(StudentDao.Properties.IdCardNo.eq(EncryptUtil.setEncryptString(Student.ENCRYPT_KEY, idcardNo)))
                .limit(1)
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

    /**
     * 根据学号模糊查询学生信息
     */
    public List<Student> fuzzyQueryByStuCode(String studentCode, int limit, int offset) {
//		List<Student> students = studentDao.queryBuilder()
//				.where(StudentDao.Properties.StudentCode.like("%" + studentCode + "%"))
//				.limit(limit)
//				.offset(offset)
//				.list();
//		return students;


        StringBuffer sqlBuf = new StringBuffer("SELECT S.* FROM " + StudentDao.TABLENAME + " S");
        sqlBuf.append(" WHERE S." + StudentDao.Properties.StudentCode.columnName + " IN ( ");
        sqlBuf.append(" SELECT  " + StudentItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + StudentItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  )");
        sqlBuf.append(" AND  S." + StudentDao.Properties.StudentCode.columnName + " LIKE '%" + studentCode + "%' ");
        sqlBuf.append(" limit " + offset + "," + limit);
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{TestConfigs.getCurrentItemCode()});
        List<Student> students = new ArrayList<>();
        while (c.moveToNext()) {
            Student student = studentDao.readEntity(c, 0);
            students.add(student);
        }
        c.close();
        return students;
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
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  )");
        sqlBuf.append(" AND  S." + StudentDao.Properties.StudentCode.columnName + " LIKE '%" + studentCode + "%' ");
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{TestConfigs.getCurrentItemCode()});
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


    public List<Student> queryStudentFeatures() {
        return studentDao.queryBuilder()
                .where(StudentDao.Properties.FaceFeature.isNotNull())
                .where(StudentDao.Properties.FaceFeature.notEq(""))
                .list();
    }

    public List<Student> queryByItemStudentFeatures() {

        StringBuffer sqlBuf = new StringBuffer("SELECT S.* FROM " + StudentDao.TABLENAME + " S");
        sqlBuf.append(" WHERE S." + StudentDao.Properties.StudentCode.columnName + " IN ( ");
        sqlBuf.append(" SELECT  " + StudentItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + StudentItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  )");
        sqlBuf.append(" AND " + StudentDao.Properties.FaceFeature.columnName + " <>'' ");

        Logger.i("=====sql1===>" + sqlBuf.toString());
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{TestConfigs.getCurrentItemCode()});
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
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  )");
        if (limit != -1)
            sqlBuf.append(" limit " + offset + "," + limit);
        Logger.i("=====sql1===>" + sqlBuf.toString());
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{TestConfigs.getCurrentItemCode()});
        List<Student> students = new ArrayList<>();
        while (c.moveToNext()) {
            Student student = studentDao.readEntity(c, 0);
            students.add(student);
        }
        c.close();
        return students;

//        StringBuffer sqlBuf = new StringBuffer("SELECT * FROM " + StudentDao.TABLENAME + " S");
//        sqlBuf.append(" LEFT JOIN " + StudentItemDao.TABLENAME + " I ");
//        sqlBuf.append(" ON S." + StudentDao.Properties.StudentCode.columnName + " = I." + StudentItemDao.Properties.StudentCode.columnName);
//        sqlBuf.append(" WHERE I." + StudentItemDao.Properties.MachineCode.columnName + " = " + machineCode);
//        sqlBuf.append(" AND I." + StudentItemDao.Properties.ItemCode.columnName + " = '" + TestConfigs.getCurrentItemCode() + "'");
//        sqlBuf.append(" limit " + offset + "," + limit);
//        Logger.i("getItemStudent:" + sqlBuf.toString());
//        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), null);
//        List<Student> students = new ArrayList<>();
//        while (c.moveToNext()) {
//            Student student = studentDao.readEntity(c, 0);
//            students.add(student);
//        }
//        return students;
    }
    public List<String> getResultsStudentByItem(String itemCode) {
        List<String> stuCodeList = new ArrayList<>();
        StringBuffer sqlBuf1 = new StringBuffer("SELECT  DISTINCT " + RoundResultDao.Properties.StudentCode.columnName);
        sqlBuf1.append(" FROM " + RoundResultDao.TABLENAME);
        sqlBuf1.append(" WHERE " + RoundResultDao.Properties.ItemCode.columnName + " =  ?  AND ");
        sqlBuf1.append(RoundResultDao.Properties.MachineCode.columnName + " = ? ");
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf1.toString(), new String[]{itemCode, TestConfigs.sCurrentItem.getMachineCode() + ""});
        while (c.moveToNext()) {
            stuCodeList.add(c.getString(0));
        }
        c.close();
        return stuCodeList;
    }
    /**
     * 根据用户筛选获取学生列表
     *
     * @param isTested   选择已测试 （传两个参数是会出现用户不选择两个筛选项）
     * @param isUnTested 选择未测试
     * @param isUpload   选择已上传
     * @param isUnUpload 选择未上传
     * @param limit      页码
     * @param offset     页数
     * @return
     */
    public List<Student> getChooseStudentList(boolean isTested, boolean isUnTested,
                                              boolean isUpload, boolean isUnUpload, int limit, int offset) {
        //获取报名项目所有学生
        //查询学生在当前项目个人报名与分组报名的并集里的
        List<Student> studentList = new ArrayList<>();
        StringBuffer sqlBuf = new StringBuffer("SELECT  *");
        sqlBuf.append("  FROM " + StudentDao.TABLENAME);
        sqlBuf.append(" WHERE " + StudentDao.Properties.StudentCode.columnName + " IN ( ");
        sqlBuf.append(" SELECT  " + StudentItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + StudentItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  )");
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

        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{TestConfigs.getCurrentItemCode()});
        while (c.moveToNext()) {
            Student student = studentDao.readEntity(c, 0);
            studentList.add(student);
        }

        return studentList;
    }

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
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  )");

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
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{TestConfigs.getCurrentItemCode()});

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
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  )");

        Logger.i("=====sql1===>" + sqlBuf.toString());
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{TestConfigs.getCurrentItemCode()});

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
                .limit(1)
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
                .limit(1)
                .unique();
    }

    // 关键代码,丑但可用,勿改
    public List<Item> queryItemsByMachineCode(int machineCode) {
        String sql = "SELECT  * " + " FROM " + ItemDao.TABLENAME + " WHERE " + ItemDao.Properties.MachineCode.columnName + " = ?   ";
        Cursor c = db.rawQuery(sql, new String[]{machineCode + ""});
        List<Item> itemList = new ArrayList<>();
        while (c.moveToNext()) {
            itemList.add(itemDao.readEntity(c, 0));
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
        itemDao.insertInTx(items);
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
//        itemDao.deleteAll();
        itemDao.insertOrReplaceInTx(freshItems);
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
                .limit(1)
                .unique();
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

    public StudentItem querystuItemByItem(Item item) {
        return studentItemDao
                .queryBuilder()
                .where(StudentItemDao.Properties.MachineCode.eq(item.getMachineCode()))
                .where(StudentItemDao.Properties.ItemCode.eq(item.getMachineCode()))
                .unique();
    }

    public List<StudentItem> queryStuItemsByItemCodeDefault(int machineCode) {
        return studentItemDao
                .queryBuilder()
                .where(StudentItemDao.Properties.MachineCode.eq(machineCode))
                .where(StudentItemDao.Properties.ItemCode.eq(TestConfigs.DEFAULT_ITEM_CODE))
                .list();
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

    // 更新报名列表信息
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


    public List<RoundResult> queryResultsByItemCodeDefault(int machineCode) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.MachineCode.eq(machineCode))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.DEFAULT_ITEM_CODE))
                .list();
    }

    /**
     * 获取所有项目为默认的成绩列表
     *
     * @return
     */
    public List<RoundResult> queryResultsByItemCodeDefault() {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.DEFAULT_ITEM_CODE))
                .list();
    }

    // 批量修改成绩信息
    public void updateRoundResult(List<RoundResult> allScores) {
        for (RoundResult allScore : allScores) {
            String encryptData = allScore.getStudentCode() + "," + allScore.getItemCode()
                    + "," + allScore.getResult() + "," + allScore.getResultState() + "," + allScore.getTestTime();
            allScore.setRemark3(EncryptUtil.setEncryptString(RoundResult.ENCRYPT_KEY, encryptData));
        }
        roundResultDao.updateInTx(allScores);
    }

    /**
     * 更新成绩
     *
     * @param score
     */
    public void updateRoundResult(RoundResult score) {
        String encryptData = score.getStudentCode() + "," + score.getItemCode()
                + "," + score.getResult() + "," + score.getResultState() + "," + score.getTestTime();
        score.setRemark3(EncryptUtil.setEncryptString(RoundResult.ENCRYPT_KEY, encryptData));
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
     * 获取是否上传的成绩列表
     * (改：上传所有不需要状态判断)
     *
     * @param upLoaded
     * @return
     */
    public List<RoundResult> getUploadResultsAll(boolean upLoaded) {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.UpdateState.eq(upLoaded ? 1 : 0))
                .list();
    }

    /**
     * 获取是否上传的成绩列表
     * (改：上传所有不需要状态判断)
     *
     * @return
     */
    public List<RoundResult> getResultsAll() {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .list();
    }

    public List<RoundResult> getResultsListAll() {
        return roundResultDao.queryBuilder() 
                .list();
    }
    /**
     * 添加成绩
     *
     * @param roundResult
     */
    public void insertRoundResult(RoundResult roundResult) {
        String encryptData = roundResult.getStudentCode() + "," + roundResult.getItemCode()
                + "," + roundResult.getResult() + "," + roundResult.getResultState() + "," + roundResult.getTestTime();
        roundResult.setRemark3(EncryptUtil.setEncryptString(RoundResult.ENCRYPT_KEY, encryptData));
        roundResultDao.insert(roundResult);
    }

    /**
     * 查询对应考生当前项目最好成绩
     *
     * @param studentCode 考号
     * @return 对应最好成绩
     */
    public RoundResult queryBestScore(String studentCode) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\tIsLastResult:" + 1);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.IsLastResult.eq(1))
                .limit(1)
                .unique();
    }

    public RoundResult queryBestScore(String studentCode, String itemCode) {
        //Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
        //		+ "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\tIsLastResult:" + 1);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(itemCode))
                .where(RoundResultDao.Properties.IsLastResult.eq(1))
                .limit(1)
                .unique();
    }
    public RoundResult queryBestScore(String studentCode, int testNo) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\ttestNo:" + testNo);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.TestNo.eq(testNo))
                .where(RoundResultDao.Properties.IsLastResult.eq(1))
                .limit(1)
                .unique();
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

    public List<RoundResult> queryResultsByStuItem(StudentItem stuItem) {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(stuItem.getStudentCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(stuItem.getItemCode()))
                .where(RoundResultDao.Properties.MachineCode.eq(stuItem.getMachineCode()))
                .list();
    }


    /**
     * 查询对应考生当前项目最后一次成绩
     *
     * @param studentCode 考号
     * @return
     */
    public RoundResult queryFinallyRoundScore(String studentCode) {
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
    public RoundResult queryRoundByRoundNo(String studentCode, int testNo, int roundNo) {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.RoundNo.eq(roundNo))
                .where(RoundResultDao.Properties.TestNo.eq(testNo))
                .unique();
    }
    /********************************************多表操作**********************************************************************/

    /**
     * 关闭数据库
     */
    public void close() {
        db.close();
    }

}
