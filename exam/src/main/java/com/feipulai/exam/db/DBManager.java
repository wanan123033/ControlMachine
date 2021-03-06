package com.feipulai.exam.db;

import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.BuildConfig;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.Account;
import com.feipulai.exam.entity.AccountDao;
import com.feipulai.exam.entity.ChipGroup;
import com.feipulai.exam.entity.ChipGroupDao;
import com.feipulai.exam.entity.ChipInfo;
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
import com.feipulai.exam.entity.StudentFace;
import com.feipulai.exam.entity.StudentFaceDao;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.entity.StudentItemDao;
import com.feipulai.exam.entity.StudentThermometer;
import com.feipulai.exam.entity.StudentThermometerDao;
import com.feipulai.exam.utils.EncryptUtil;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.feipulai.exam.activity.MiddleDistanceRace.TimingBean.GROUP_FINISH;

/**
 * ?????? ??????
 * ?????? ?????????????????????
 * ?????? ??????
 * Created on 2017/11/24.
 */
public class DBManager {

    public static final String DB_NAME = "control_db";
    public static final String DB_PASSWORD = "FairPlay2019";
    public static final int TEST_TYPE_TIME = 1;//???????????? ??????
    public static final int TEST_TYPE_COUNT = 2;//???????????? ??????
    public static final int TEST_TYPE_DISTANCE = 3;//???????????? ??????
    public static final int TEST_TYPE_POWER = 4;//???????????? ??????
    private static DBManager mInstance;
    private static ItemDao itemDao;
    private static StudentDao studentDao;
    private static StudentItemDao studentItemDao;
    public static RoundResultDao roundResultDao;
    private static ScheduleDao scheduleDao;
    private static ItemScheduleDao itemScheduleDao;
    private static GroupDao groupDao;
    private static GroupItemDao groupItemDao;
    private static MachineResultDao machineResultDao;
    private static ChipGroupDao chipGroupDao;
    private static ChipInfoDao chipInfoDao;
    private static StudentThermometerDao thermometerDao;
    private static AccountDao accountDao;
    private static StudentFaceDao studentFaceDao;
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
     * ??????????????????
     */
    public void initDB() {
        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
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
        chipGroupDao = daoSession.getChipGroupDao();
        chipInfoDao = daoSession.getChipInfoDao();
        accountDao = daoSession.getAccountDao();
        thermometerDao = daoSession.getStudentThermometerDao();
        studentFaceDao = daoSession.getStudentFaceDao();
//        Account account = accountDao.queryBuilder().where(AccountDao.Properties.Account.eq("fairplay")).unique();
//        if (account == null) {
//            insterAccount("fairplay", "fpl.2021", 0);
//        }
        int[] supportMachineCodes = {/*ItemDefault.CODE_HW, */ItemDefault.CODE_TS, ItemDefault.CODE_YWQZ, ItemDefault.CODE_YTXS,
                ItemDefault.CODE_LDTY, ItemDefault.CODE_ZWTQQ,
                ItemDefault.CODE_HWSXQ, ItemDefault.CODE_FHL, ItemDefault.CODE_ZFP,
                ItemDefault.CODE_PQ, ItemDefault.CODE_MG, ItemDefault.CODE_FWC, ItemDefault.CODE_LQYQ,
                ItemDefault.CODE_ZQYQ, ItemDefault.CODE_ZCP, ItemDefault.CODE_JGCJ, ItemDefault.CODE_WLJ, ItemDefault.CODE_SHOOT, ItemDefault.CODE_SPORT_TIMER,
                ItemDefault.CODE_ZQYQ, ItemDefault.CODE_ZCP, ItemDefault.CODE_JGCJ, ItemDefault.CODE_WLJ, ItemDefault.CODE_SHOOT,
                ItemDefault.CODE_SGBQS
        };
        for (int machineCode : supportMachineCodes) {
            //??????????????????????????????????????????,?????????????????????,??????????????????
            List<Item> items = itemDao.queryBuilder().where(ItemDao.Properties.MachineCode.eq(machineCode)).list();
            if (items != null && items.size() != 0) {
                continue;
            }
            if (TextUtils.isEmpty(TestConfigs.machineNameMap.get(machineCode))) {
                Item item = itemDao.queryBuilder().where(ItemDao.Properties.ItemName.eq(TestConfigs.machineNameMap.get(machineCode))).limit(1).unique();
                if (item != null) {
                    continue;
                }
            }

            switch (machineCode) {

                case ItemDefault.CODE_TS:
                    // insertItem(machineCode, "E11","???????????????", "???");// for test
                    insertItem(machineCode, "??????", "???", TEST_TYPE_COUNT);
                    break;

                case ItemDefault.CODE_YWQZ:
                    insertItem(machineCode, "????????????", "???", TEST_TYPE_COUNT);
                    break;

                case ItemDefault.CODE_YTXS:
                    insertItem(machineCode, "????????????", "???", TEST_TYPE_COUNT);
                    break;

                case ItemDefault.CODE_LDTY:
                    insertItem(machineCode, "????????????", "???", TEST_TYPE_DISTANCE);
                    break;

                case ItemDefault.CODE_ZWTQQ:
                    insertItem(machineCode, "???????????????", "??????", TEST_TYPE_DISTANCE);
                    break;

                case ItemDefault.CODE_HWSXQ:
                    insertItem(machineCode, "???????????????", "???", TEST_TYPE_DISTANCE);
                    break;

                case ItemDefault.CODE_FHL:
                    insertItem(machineCode, "?????????", "??????", TEST_TYPE_POWER);
                    break;

                case ItemDefault.CODE_ZFP:
                    insertItem(machineCode, "????????????", "???'???", TEST_TYPE_TIME);
                    break;

                case ItemDefault.CODE_PQ:
                    insertItem(machineCode, "????????????", "???", TEST_TYPE_COUNT);
                    break;
                case ItemDefault.CODE_MG:
                    insertItem(machineCode, "??????", "??????", TEST_TYPE_DISTANCE);
                    break;
                case ItemDefault.CODE_FWC:
                    insertItem(machineCode, "?????????", "???", TEST_TYPE_COUNT);
                    break;
                case ItemDefault.CODE_LQYQ:
                    insertItem(machineCode, "????????????", "???'???", 2, TEST_TYPE_TIME);

                    break;
                case ItemDefault.CODE_ZQYQ:
                    insertItem(machineCode, "????????????", "???'???", 2, TEST_TYPE_TIME);
                    break;
                case ItemDefault.CODE_ZCP:
                    Item item800 = DBManager.getInstance().queryItemByName("800???");
                    if (item800 == null) {
                        insertItem(machineCode, "fpl_800", "800???", "???'???", TEST_TYPE_TIME);
                        insertItem(machineCode, "fpl_1000", "1000???", "???'???", TEST_TYPE_TIME);
                    }

//                    insertMiddleRaceItem(machineCode, "800???", "???'???");
//                    insertMiddleRaceItem(machineCode, "1000???", "???'???");
                    break;
                case ItemDefault.CODE_JGCJ:
                    insertItem(machineCode, "????????????", "???", TEST_TYPE_DISTANCE);
                    break;
                case ItemDefault.CODE_WLJ:
                    insertItem(machineCode, "??????", "??????", TEST_TYPE_POWER);
                    break;
                case ItemDefault.CODE_SHOOT:
                    insertItem(machineCode, "????????????", "???", TEST_TYPE_COUNT);
                    break;
                case ItemDefault.CODE_SPORT_TIMER:
                    insertItem(machineCode, "????????????", "???'???", TEST_TYPE_TIME);
                    break;
                case ItemDefault.CODE_SGBQS:
                    insertItem(machineCode, "???????????????", "???", TEST_TYPE_COUNT);
                    break;
            }
        }
        Logger.i("????????????????????????");
    }

    //???????????????
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
//        chipInfoDao.deleteAll();
//        chipGroupDao.deleteAll();
    }

    /**
     * ?????????????????????
     */
    public void roundResultClear() {
        roundResultDao.deleteAll();
        machineResultDao.deleteAll();
    }

    /********************************************
     * ?????????
     ********************************************/
    /**
     * ????????????????????????????????????
     *
     * @param stuCode ??????
     * @return
     */
    public Student queryStudentByStuCode(final String stuCode) {
        Student student = studentDao.queryBuilder()
                .where(StudentDao.Properties.StudentCode.eq(stuCode))
                .unique();
        return student;
    }

    public void updateStudent(Student student) {
        studentDao.updateInTx(student);
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


    public List<StudentFace> getStudentFeatures() {
        return studentFaceDao.queryBuilder()
                .where(StudentFaceDao.Properties.FaceFeature.notEq(""))
                .where(StudentFaceDao.Properties.FaceFeature.isNotNull()).list();
    }

    public List<StudentFace> queryByItemStudentFeatures() {

        StringBuffer sqlBuf = new StringBuffer("SELECT S.* FROM " + StudentFaceDao.TABLENAME + " S");
        sqlBuf.append(" WHERE S." + StudentFaceDao.Properties.StudentCode.columnName + " IN ( ");
        sqlBuf.append(" SELECT  " + StudentItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + StudentItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  ");
        sqlBuf.append(" UNION SELECT  " + GroupItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + GroupItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + GroupItemDao.Properties.ItemCode.columnName + " = ?  )");
        sqlBuf.append(" AND S." + StudentFaceDao.Properties.FaceFeature.columnName + " <>'' ");

        Logger.i("=====sql1===>" + sqlBuf.toString());
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{TestConfigs.getCurrentItemCode(), TestConfigs.getCurrentItemCode()});
        List<StudentFace> students = new ArrayList<>();
        while (c.moveToNext()) {
            StudentFace student = studentFaceDao.readEntity(c, 0);
            students.add(student);
        }
        c.close();


        return students;
    }

    public List<StudentItem> queryStudentItemByItemCode(String itemCode) {
        return studentItemDao.queryBuilder().where(StudentItemDao.Properties.ItemCode.eq(itemCode)).list();
    }

    public StudentItem queryStudentItemByCode(String itemCode, String stuCode) {
        return studentItemDao.queryBuilder()
                .where(StudentItemDao.Properties.ItemCode.eq(itemCode))
                .where(StudentItemDao.Properties.StudentCode.eq(stuCode))
                .unique();
    }

    /**
     * ????????????????????????
     *
     * @param stuList ????????????
     */
    public void insertStudentList(List<Student> stuList) {
        if (stuList == null || stuList.isEmpty()) {
            return;
        }
        studentDao.insertOrReplaceInTx(stuList);
        studentDao.detachAll();
    }

    /**
     * ?????????
     * ???????????????
     *
     * @param chipGroup
     */
    public void insertChipGroup(ChipGroup chipGroup) {
        chipGroupDao.insert(chipGroup);
    }

    public void insertChipGroups(List<ChipGroup> chipGroups) {
        chipGroupDao.insertInTx(chipGroups);
    }

    public long queryChipGroup(int color) {
        return chipGroupDao.queryBuilder().where(ChipGroupDao.Properties.Color.eq(color)).count();
    }

    public long queryChipGroup(String colorName) {
        return chipGroupDao.queryBuilder().where(ChipGroupDao.Properties.ColorGroupName.eq(colorName)).count();
    }


    public ChipInfo queryChipInfoByID(String chipInfo) {
        return chipInfoDao.queryBuilder().whereOr(ChipInfoDao.Properties.ChipID1.eq(chipInfo), ChipInfoDao.Properties.ChipID2.eq(chipInfo)).unique();
    }

    public List<ChipInfo> queryChipInfoByID2(String chipInfo) {
        List<ChipInfo> chips = chipInfoDao.queryBuilder().whereOr(ChipInfoDao.Properties.ChipID1.eq(chipInfo), ChipInfoDao.Properties.ChipID2.eq(chipInfo)).list();
        Log.i("chips-----------", chips.toString());
        return chips;
    }

    public List<ChipInfo> queryChipInfoByColor(String chipColor) {
        return chipInfoDao.queryBuilder().where(ChipInfoDao.Properties.ColorGroupName.eq(chipColor)).orderAsc(ChipInfoDao.Properties.VestNo).list();
    }

    public void deleteSomeChipInfos(List<ChipInfo> chipInfos) {
        chipInfoDao.deleteInTx(chipInfos);
    }

    public ChipGroup queryChipGroupUni(String colorName) {
        return chipGroupDao.queryBuilder().where(ChipGroupDao.Properties.ColorGroupName.eq(colorName)).unique();
    }

    /**
     * ????????????????????????ID?????????
     *
     * @param colorName
     * @return
     */
    public List<ChipInfo> queryChipInfoHasChipID(String colorName) {
        return chipInfoDao.queryBuilder()
                .where(ChipInfoDao.Properties.ColorGroupName.eq(colorName))
                .whereOr(ChipInfoDao.Properties.ChipID1.isNotNull(), ChipInfoDao.Properties.ChipID1.notEq(""))
                .list();
    }

    public void deleteChipInfo(List<ChipInfo> chipInfos) {
        chipInfoDao.deleteInTx(chipInfos);
    }

    /**
     * ????????????????????????????????????
     *
     * @param colorName
     */
    public void deleteChipInfo(String colorName) {
        chipInfoDao.deleteInTx(chipInfoDao.queryBuilder().where(ChipInfoDao.Properties.ColorGroupName.eq(colorName)).list());
    }

    public void deleteChipGroup(ChipGroup chipGroup) {
        chipGroupDao.delete(chipGroup);
    }

    public List<ChipGroup> queryAllChipGroup() {
        return chipGroupDao.loadAll();
    }

    public List<ChipInfo> queryAllChipInfo() {
        return chipInfoDao.queryBuilder().orderAsc(ChipInfoDao.Properties.ColorGroupName, ChipInfoDao.Properties.VestNo).list();
    }

    public void updateChipInfo(ChipInfo chipInfo) {
        chipInfoDao.update(chipInfo);
    }

    public void updateChipGroup(ChipGroup chipGroup) {
        chipGroupDao.update(chipGroup);
    }

    public void updateChipInfo(List<ChipInfo> chipInfos) {
        chipInfoDao.updateInTx(chipInfos);
    }

    /**
     * ?????????
     * ??????????????????
     *
     * @param chipInfo
     */
    public void insertChipInfo(ChipInfo chipInfo) {
        chipInfoDao.insert(chipInfo);
    }

    /**
     * ?????????
     * ??????????????????
     *
     * @param chipInfos
     */
    public void insertChipInfos(List<ChipInfo> chipInfos) {
        chipInfoDao.insertInTx(chipInfos);
    }

    public void insertChipInfos2(List<ChipInfo> chipInfos) {
        chipInfoDao.insertOrReplaceInTx(chipInfos);
    }

    public void deleteAllChip() {
        chipGroupDao.deleteAll();
        chipInfoDao.deleteAll();
    }

    public List<ChipGroup> queryChipGroups() {
        return chipGroupDao.queryBuilder().orderDesc(ChipGroupDao.Properties.StudentNo).list();
    }

    /**
     * ????????????
     *
     * @param student ??????
     */
    public void insertStudent(Student student) {
        studentDao.insertInTx(student);
    }

    /**
     * ????????????????????????????????????
     *
     * @param idcardNo ????????????
     * @return
     */
    public Student queryStudentByIDCode(String idcardNo) {
        return studentDao
                .queryBuilder()
                .where(StudentDao.Properties.IdCardNo.eq(EncryptUtil.setEncryptString(Student.ENCRYPT_KEY, idcardNo)))
                .unique();
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    public List<Student> dumpAllStudents() {
        return studentDao
                .queryBuilder()
                .list();
    }

    public List<Student> getStudentByPortrait() {
        StringBuffer sqlBuf = new StringBuffer("SELECT S.* FROM " + StudentDao.TABLENAME + " S");
        sqlBuf.append(" WHERE S." + StudentDao.Properties.StudentCode.columnName + " IN ( ");
        sqlBuf.append(" SELECT  " + StudentItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + StudentItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  ");
        sqlBuf.append(" UNION SELECT  " + GroupItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + GroupItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + GroupItemDao.Properties.ItemCode.columnName + " = ?  )");
        sqlBuf.append(" AND  S." + StudentDao.Properties.Portrait.columnName + " != '' ");
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{TestConfigs.getCurrentItemCode(), TestConfigs.getCurrentItemCode()});
        List<Student> students = new ArrayList<>();
        while (c.moveToNext()) {
            Student student = studentDao.readEntity(c, 0);
            students.add(student);
        }
        return students;

    }
//    public List<StudentItem> queryStudentByItemAndSort(String itemCode,int sort){
//
//    }

    /**
     * ????????????????????????????????????
     */
    public List<Student> fuzzyQueryByStuCode(String studentCode, int limit, int offset) {
        return fuzzyQueryByStuCode("-2", TestConfigs.getCurrentItemCode(), studentCode, limit, offset);
    }

    /**
     * ????????????????????????????????????
     *
     * @param scheduleNo -2 ?????????
     */
    public List<Student> fuzzyQueryByStuCode(String scheduleNo, String itemCode, String studentCode, int limit, int offset) {
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
        if (!TextUtils.equals(scheduleNo, "-2")) {
            sqlBuf.append(" AND  " + StudentItemDao.Properties.ScheduleNo.columnName + " =  " + scheduleNo);
        }
        sqlBuf.append(" UNION SELECT  " + GroupItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + GroupItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + GroupItemDao.Properties.ItemCode.columnName + " = ? ");
        if (!TextUtils.equals(scheduleNo, "-2")) {
            sqlBuf.append(" AND  " + StudentItemDao.Properties.ScheduleNo.columnName + " =  " + scheduleNo);
        }
        sqlBuf.append(" )");
        sqlBuf.append(" AND  S." + StudentDao.Properties.StudentCode.columnName + " LIKE '%" + studentCode + "%' ");
        sqlBuf.append(" limit " + offset + "," + limit);
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{itemCode, itemCode});
        List<Student> students = new ArrayList<>();
        while (c.moveToNext()) {
            Student student = studentDao.readEntity(c, 0);
            students.add(student);
        }
        c.close();
        return students;

    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param scheduleNo -2 ?????????
     * @param limit      ??????
     * @param offset     ??????
     * @return
     */
    public List<Student> getItemStudent(String scheduleNo, String itemCode, int limit, int offset) {
        StringBuffer sqlBuf = new StringBuffer("SELECT S.* FROM " + StudentDao.TABLENAME + " S");
        sqlBuf.append(" WHERE S." + StudentDao.Properties.StudentCode.columnName + " IN ( ");
        sqlBuf.append(" SELECT  " + StudentItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + StudentItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  ");
        if (!TextUtils.equals(scheduleNo, "-2")) {
            sqlBuf.append(" AND  " + StudentItemDao.Properties.ScheduleNo.columnName + " =  " + scheduleNo);
        }
        sqlBuf.append(" UNION SELECT  " + GroupItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + GroupItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + GroupItemDao.Properties.ItemCode.columnName + " = ? ");
        if (!TextUtils.equals(scheduleNo, "-2")) {
            sqlBuf.append(" AND  " + StudentItemDao.Properties.ScheduleNo.columnName + " =  " + scheduleNo);
        }
        sqlBuf.append(" )");
        if (limit != -1)
            sqlBuf.append(" limit " + offset + "," + limit);
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{itemCode, itemCode});
        List<Student> students = new ArrayList<>();
        while (c.moveToNext()) {
            Student student = studentDao.readEntity(c, 0);
            students.add(student);
        }
        c.close();
        return students;
    }


    /**
     * ??????????????????????????????????????????????????????
     *
     * @param scheduleNo -2 ?????????
     * @param isTested   ??????????????? ???????????????????????????????????????????????????????????????
     * @param isUnTested ???????????????
     * @param isUpload   ???????????????
     * @param isUnUpload ???????????????
     * @return
     */
    public Map<String, Object> getChooseStudentCount(String scheduleNo, String itemCode, boolean isTested, boolean isUnTested, boolean isUpload, boolean isUnUpload) {

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

        if (!TextUtils.equals(scheduleNo, "-2")) {
            sqlBuf.append(" AND  " + StudentItemDao.Properties.ScheduleNo.columnName + " =  " + scheduleNo);
        }

        sqlBuf.append(" UNION SELECT  " + GroupItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + GroupItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + GroupItemDao.Properties.ItemCode.columnName + " = ? ");
        if (!TextUtils.equals(scheduleNo, "-2")) {
            sqlBuf.append(" AND  " + StudentItemDao.Properties.ScheduleNo.columnName + " =  " + scheduleNo);
        }
        sqlBuf.append(" )");

        if (isTested || isUnTested) {
            sqlBuf.append(" AND S." + StudentDao.Properties.StudentCode.columnName);
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
                sqlBuf.append(" AND S." + StudentDao.Properties.StudentCode.columnName);
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
            if (isTested || isUnTested) {
                sqlBuf.append(" WHERE ");
            } else {
                sqlBuf.append(" AND ");
            }
        }

        sqlBuf.append("  R." + RoundResultDao.Properties.ItemCode.columnName + " = '" + itemCode + "'");
        sqlBuf.append("    AND R." + RoundResultDao.Properties.IsDelete.columnName + "= 0  ");
        sqlBuf.append(")  ");

        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{itemCode, itemCode});

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
     * ????????????????????????????????????????????????
     *
     * @param scheduleNo -2 ?????????
     * @return
     */
    public Map<String, Object> getItemStudenCount(String scheduleNo, String itemCode) {
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
        if (!TextUtils.equals(scheduleNo, "-2")) {
            sqlBuf.append(" AND  " + StudentItemDao.Properties.ScheduleNo.columnName + " =  " + scheduleNo);
        }
        sqlBuf.append(" UNION SELECT  " + GroupItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + GroupItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + GroupItemDao.Properties.ItemCode.columnName + " = ? ");
        if (!TextUtils.equals(scheduleNo, "-2")) {
            sqlBuf.append(" AND  " + StudentItemDao.Properties.ScheduleNo.columnName + " =  " + scheduleNo);
        }
        sqlBuf.append(" )");

        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{itemCode, itemCode});

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
     * ????????????????????????????????????
     *
     * @param scheduleNo -2 ?????????
     */
    public Map<String, Object> fuzzyQueryByStuCodeCount(String scheduleNo, String itemCode, String studentCode) {
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
        if (!TextUtils.equals(scheduleNo, "-2")) {
            sqlBuf.append(" AND  " + StudentItemDao.Properties.ScheduleNo.columnName + " =  " + scheduleNo);
        }
        sqlBuf.append(" UNION SELECT  " + GroupItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + GroupItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + GroupItemDao.Properties.ItemCode.columnName + " = ? ");
        if (!TextUtils.equals(scheduleNo, "-2")) {
            sqlBuf.append(" AND  " + StudentItemDao.Properties.ScheduleNo.columnName + " =  " + scheduleNo);
        }
        sqlBuf.append(" )");

        sqlBuf.append(" AND  S." + StudentDao.Properties.StudentCode.columnName + " LIKE '%" + studentCode + "%' ");
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{itemCode, itemCode});
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
     * @param scheduleNo -2 ?????????
     * @param itemCode
     * @param isTested
     * @param isUnTested
     * @param isUpload
     * @param isUnUpload
     * @param limit
     * @param offset
     * @return
     */
    public List<Student> getChooseStudentList(String scheduleNo, String itemCode, boolean isTested, boolean isUnTested, boolean isUpload, boolean isUnUpload, int limit, int offset) {
        //??????????????????????????????
        //?????????????????????????????????????????????????????????????????????
        List<Student> studentList = new ArrayList<>();
        StringBuffer sqlBuf = new StringBuffer("SELECT  *");
        sqlBuf.append("  FROM " + StudentDao.TABLENAME);
        sqlBuf.append(" WHERE " + StudentDao.Properties.StudentCode.columnName + " IN ( ");
        sqlBuf.append(" SELECT  " + StudentItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + StudentItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + StudentItemDao.Properties.ItemCode.columnName + " = ?  ");

        if (!TextUtils.equals(scheduleNo, "-2")) {
            sqlBuf.append(" AND  " + StudentItemDao.Properties.ScheduleNo.columnName + " =  " + scheduleNo);
        }

        sqlBuf.append(" UNION SELECT  " + GroupItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + GroupItemDao.TABLENAME);
        sqlBuf.append(" WHERE  " + GroupItemDao.Properties.ItemCode.columnName + " = ? ");

        if (!TextUtils.equals(scheduleNo, "-2")) {
            sqlBuf.append(" AND  " + GroupItemDao.Properties.ScheduleNo.columnName + " =  " + scheduleNo);
        }
        sqlBuf.append(" ) ");


        //???????????????
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
        sqlBuf.append("  R." + RoundResultDao.Properties.ItemCode.columnName + " = '" + itemCode + "'");
        sqlBuf.append("    AND R." + RoundResultDao.Properties.IsDelete.columnName + "= 0  ");
        sqlBuf.append(")  ");
        sqlBuf.append(" limit " + offset + "," + limit);

        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{itemCode, itemCode});
        while (c.moveToNext()) {
            Student student = studentDao.readEntity(c, 0);
            studentList.add(student);
        }

        return studentList;
    }

    /**********************************************
     * ?????????
     **********************************************************************/
    /**
     * ????????????????????????????????????
     *
     * @param itemName ????????????
     * @return
     */
    public Item queryItemByName(String itemName) {
        return itemDao
                .queryBuilder()
                .where(ItemDao.Properties.ItemName.eq(itemName))
                .unique();

    }

    /**
     * ????????????????????????????????????
     *
     * @param itemCode ????????????
     * @return
     */
    public Item queryItemByCode(String itemCode) {
        return itemDao
                .queryBuilder()
                .where(ItemDao.Properties.ItemCode.eq(itemCode))
                .unique();

    }


    public Item queryItemByMachineItemCode(int machineCode, String itemCode) {
        return itemDao
                .queryBuilder()
                .where(ItemDao.Properties.MachineCode.eq(machineCode))
                .where(ItemDao.Properties.ItemCode.eq(itemCode))
                .unique();
    }

    /**
     * ?????????????????????????????????
     *
     * @param machineCode ?????????
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
            item = itemDao.readEntity(c, 0);
            itemList.add(item);
        }
        c.close();
        return itemList;
    }

    /**
     * ??????????????????
     *
     * @param item
     */
    public void updateItem(Item item) {
        itemDao.update(item);
    }

    public void updateItems(List<Item> items) {
        itemDao.updateInTx(items);
    }

    public void updateItemSchedules(List<ItemSchedule> itemSchedules) {
        itemScheduleDao.updateInTx(itemSchedules);
    }

    public void deleteSchedules(List<ItemSchedule> itemSchedules) {
        itemScheduleDao.deleteInTx(itemSchedules);
    }

    private void insertItem(int machineCode, String itemName, String unit, int testType) {
        Item item = new Item();
        item.setMachineCode(machineCode);
        item.setItemName(itemName);
        item.setUnit(unit);
        item.setTestType(testType);
        itemDao.insert(item);
    }

    private void insertItem(int machineCode, String itemName, String unit, int digital, int testType) {
        Item item = new Item();
        item.setMachineCode(machineCode);
        item.setItemName(itemName);
        item.setUnit(unit);
        item.setDigital(digital);
        item.setTestType(testType);
        itemDao.insert(item);
    }

    private void insertMiddleRaceItem(int machineCode, String itemName, String unit) {
        Item item = new Item();
        item.setMachineCode(machineCode);
        item.setItemName(itemName);
        item.setUnit(unit);
        item.setCarryMode(1);
        item.setDigital(2);
        itemDao.insert(item);
    }

    public void insertItem(int machineCode, String itemCode, String itemName, String unit, int testType) {
        Item item = new Item();
        item.setMachineCode(machineCode);
        item.setItemCode(itemCode);
        item.setItemName(itemName);
        item.setUnit(unit);
        item.setTestType(testType);
        itemDao.insert(item);
    }

    /**
     * ??????????????????
     *
     * @return
     */
    public List<Item> dumpAllItems() {
        return itemDao
                .queryBuilder()
                .list();
    }

    /**
     * ??????????????????
     *
     * @param items
     */
    public void insertItems(List<Item> items) {
        itemDao.insertOrReplaceInTx(items);
    }

    /**
     * ??????????????????
     */
    public void deleteAllItems() {
        itemDao.deleteAll();
    }

    // ????????????????????????
    public void freshAllItems(List<Item> freshItems) {
        db.beginTransaction();
        itemDao.deleteAll();
        itemDao.insertInTx(freshItems);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public List<Item> queryItemNotNullItemCode() {
        return itemDao.queryBuilder()
                .where(ItemDao.Properties.ItemCode.notEq(""))
                .where(ItemDao.Properties.ItemCode.isNotNull()).list();
    }

    /*******************************************
     * ?????????????????????
     ******************************************************************/
    /**
     * ?????????????????????
     */
    public int getUnUploadNum() {

        return (int) roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.UpdateState.eq(0))
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .count();


    }

    /**
     * ????????????????????????
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
     * ????????????????????????????????????????????????
     *
     * @param studentCode ??????
     * @return
     */
    public StudentItem queryStuItemByStuCode(String studentCode) {
        Log.e("TAG===", "studentCode=" + studentCode + ",ItemCode=" + TestConfigs.getCurrentItemCode() + ",MachineCode=" + TestConfigs.sCurrentItem.getMachineCode());
        return studentItemDao
                .queryBuilder()
                .where(StudentItemDao.Properties.StudentCode.eq(studentCode))
                .where(StudentItemDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(StudentItemDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .limit(1).unique();
    }

    public StudentItem queryStuItemByStuCode2(String studentCode, String code) {
        return studentItemDao
                .queryBuilder()
                .where(StudentItemDao.Properties.StudentCode.eq(studentCode))
                .where(StudentItemDao.Properties.ItemCode.eq(code))
                .where(StudentItemDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .unique();
    }

    /**
     * ??????????????????
     *
     * @param studentItem
     */
    public void insertStudentItem(StudentItem studentItem) {
        studentItemDao.insertOrReplaceInTx(studentItem);
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
     * ????????????????????????????????????????????????
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
     * ????????????????????????
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
     * ?????????
     **********************************************************************/
    /**
     * ??????????????????????????????????????????????????????
     *
     * @param itemCode
     * @return
     */
    public List<RoundResult> queryResultsByItemCode(String itemCode) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(itemCode))
                .list();
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param studentCode
     * @return
     */
    public List<RoundResult> queryResultsByStudentCode(String studentCode) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .orderAsc(RoundResultDao.Properties.RoundNo)
                .list();
    }

    public List<RoundResult> queryResultsByStudentCode(String itemCode, String studentCode) {
        Log.e("tat", "itemCode=" + itemCode + ",studentCode=" + studentCode);
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(itemCode))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .list();
    }

    public List<RoundResult> queryResultsByStudentCode(String itemCode, String studentCode, int roundNo) {
        Log.e("tat", "itemCode=" + itemCode + ",studentCode=" + studentCode);
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(itemCode))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.RoundNo.eq(roundNo))
                .list();
    }

    public List<RoundResult> queryResultsByStudentCode(String itemCode, String studentCode, Long groupId, int examType, String scheduleNo) {
        Log.e("tat", "itemCode=" + itemCode + ",studentCode=" + studentCode);
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(itemCode))
                .where(RoundResultDao.Properties.GroupId.eq(groupId))
                .where(RoundResultDao.Properties.ExamType.eq(examType))
                .where(RoundResultDao.Properties.ScheduleNo.eq(scheduleNo))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .list();
    }

    public List<RoundResult> queryResultsByStudentCode(int examType, String itemCode, String studentCode) {
        Log.e("tat", "itemCode=" + itemCode + ",studentCode=" + studentCode);
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(itemCode))
                .where(RoundResultDao.Properties.ExamType.eq(examType))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .orderAsc(RoundResultDao.Properties.RoundNo)
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
     * ????????????????????????????????????????????????
     */
    public List<RoundResult> queryResultsByStuItem(StudentItem studentItem) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.MachineCode.eq(studentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(studentItem.getItemCode()))
                .where(RoundResultDao.Properties.StudentCode.eq(studentItem.getStudentCode()))
                .where(RoundResultDao.Properties.GroupId.eq(RoundResult.DEAFULT_GROUP_ID))
                .where(RoundResultDao.Properties.ExamType.eq(studentItem.getExamType()))
                .orderAsc(RoundResultDao.Properties.RoundNo)
                .list();
    }

    /**
     * ???????????????????????????
     */
    public List<RoundResult> queryResultsByStuItemExamType(String studentCode) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .list();
    }

    public List<RoundResult> queryResultsByStudentCode(String studentCode, Item item) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getItemCode(item)))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .list();
    }

    public List<RoundResult> queryResultsByStudentCode(String studentCode, Long groupId, int examType, String scheduleNo, Item item) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getItemCode(item)))
                .where(RoundResultDao.Properties.GroupId.eq(groupId))
                .where(RoundResultDao.Properties.ExamType.eq(examType))
                .where(RoundResultDao.Properties.ScheduleNo.eq(scheduleNo))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .list();
    }

    /**
     * ?????????????????????????????????
     */
    public RoundResult queryLastScoreByStuCode(String stuCode) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
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
                .where(RoundResultDao.Properties.IsDelete.eq(false))
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
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(stuCode))
                .where(RoundResultDao.Properties.MachineCode.eq(item.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getItemCode(item)))
                .orderDesc(RoundResultDao.Properties.TestTime)
                .limit(1)
                .unique();
    }

    /**
     * ??????????????????????????????????????????
     */
    public List<RoundResult> queryResultsByItemCodeDefault() {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.DEFAULT_ITEM_CODE))
                .list();
    }

    public List<RoundResult> queryResultsByItemCodeDefault(int machineCode) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.MachineCode.eq(machineCode))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.DEFAULT_ITEM_CODE))
                .list();
    }

    /**
     * ????????????????????????
     *
     * @param allScores
     */
    public void updateRoundResult(List<RoundResult> allScores) {
        for (RoundResult allScore : allScores) {
            String encryptData = allScore.getStudentCode() + "," + allScore.getItemCode() + "," + allScore.getExamType()
                    + "," + allScore.getResult() + "," + allScore.getResultState() + "," + allScore.getTestTime();
            allScore.setRemark3(EncryptUtil.setEncryptString(RoundResult.ENCRYPT_KEY, encryptData));
        }
        roundResultDao.updateInTx(allScores);
        roundResultDao.detachAll();
    }

    /**
     * ????????????
     *
     * @param score
     */
    public void updateRoundResult(RoundResult score) {
        String encryptData = score.getStudentCode() + "," + score.getItemCode() + "," + score.getExamType()
                + "," + score.getResult() + "," + score.getResultState() + "," + score.getTestTime();
        score.setRemark3(EncryptUtil.setEncryptString(RoundResult.ENCRYPT_KEY, encryptData));
        LogUtils.operation("???????????????" + score.toString());
        roundResultDao.update(score);
    }


    /**
     * ????????????????????????????????????????????????
     *
     * @param studentCode
     * @param upLoaded
     * @return
     */
    public List<RoundResult> queryUploadStudentResults(String studentCode, boolean upLoaded) {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.UpdateState.eq(upLoaded ? 1 : 0))
                .list();
    }

    /**
     * ????????????
     *
     * @param roundResult
     */
    public void insertRoundResult(RoundResult roundResult) {
        if (TextUtils.isEmpty(roundResult.getTestTime())) {
            roundResult.setTestTime(System.currentTimeMillis() + "");
        }
        String encryptData = roundResult.getStudentCode() + "," + roundResult.getItemCode() + "," + roundResult.getExamType()
                + "," + roundResult.getResult() + "," + roundResult.getResultState() + "," + roundResult.getTestTime();
        roundResult.setRemark3(EncryptUtil.setEncryptString(RoundResult.ENCRYPT_KEY, encryptData));
        LogUtils.operation("???????????????" + roundResult.toString());
        roundResultDao.insert(roundResult);
    }

    public RoundResult insertRoundResult2(RoundResult roundResult) {
        String encryptData = roundResult.getStudentCode() + "," + roundResult.getItemCode() + "," + roundResult.getExamType()
                + "," + roundResult.getResult() + "," + roundResult.getResultState() + "," + roundResult.getTestTime();
        roundResult.setRemark3(EncryptUtil.setEncryptString(RoundResult.ENCRYPT_KEY, encryptData));
        LogUtils.operation("???????????????" + roundResult.toString());
        long id = roundResultDao.insert(roundResult);
        return roundResultDao.queryBuilder().where(RoundResultDao.Properties.Id.eq(id)).unique();
    }

    public void updateRoundResults(List<RoundResult> roundResults) {
        LogUtils.operation("????????????????????????==???====???" + roundResults.toString());
        for (RoundResult allScore : roundResults) {
            String encryptData = allScore.getStudentCode() + "," + allScore.getItemCode() + "," + allScore.getExamType()
                    + "," + allScore.getResult() + "," + allScore.getResultState() + "," + allScore.getTestTime();
            allScore.setRemark3(EncryptUtil.setEncryptString(RoundResult.ENCRYPT_KEY, encryptData));
        }
        LogUtils.operation("????????????????????????==???====???" + roundResults.toString());
        roundResultDao.updateInTx(roundResults);
    }

    public void insertRoundResults(List<RoundResult> roundResults) {
        for (RoundResult allScore : roundResults) {
            String encryptData = allScore.getStudentCode() + "," + allScore.getItemCode() + "," + allScore.getExamType()
                    + "," + allScore.getResult() + "," + allScore.getResultState() + "," + allScore.getTestTime();
            allScore.setRemark3(EncryptUtil.setEncryptString(RoundResult.ENCRYPT_KEY, encryptData));
        }
        roundResultDao.insertInTx(roundResults);
    }

    /**
     * ??????????????????????????????????????????(??????)
     *
     * @param studentCode ??????
     * @return ??????????????????
     */
    public RoundResult queryBestScore(String studentCode, int testNo) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\ttestNo:" + testNo);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.TestNo.eq(testNo))
                .where(RoundResultDao.Properties.IsLastResult.eq(1))
                .limit(1)
                .unique();
    }

    /**
     * ??????????????????????????????????????????(??????)
     *
     * @param studentCode ??????
     * @return ??????????????????
     */
    public RoundResult queryBestScore(Item item, String studentCode, int testNo) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\ttestNo:" + testNo);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(item.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(item.getItemCode()))
                .where(RoundResultDao.Properties.TestNo.eq(testNo))
                .where(RoundResultDao.Properties.IsLastResult.eq(1))
                .limit(1)
                .unique();
    }

    /**
     * ??????????????????????????????????????????(??????)
     *
     * @param studentCode ??????
     * @return ??????????????????
     */
    public RoundResult queryBestFinallyScore(Item item, String studentCode, int testNo) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\ttestNo:" + testNo);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(item.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(item.getItemCode()))
                .where(RoundResultDao.Properties.TestNo.eq(testNo))
                .orderAsc(RoundResultDao.Properties.RoundNo)
                .limit(1)
                .unique();
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param studentCode ??????
     * @return ??????
     */
    public RoundResult queryOrderAscScore(String studentCode, int testNo) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\ttestNo:" + testNo);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.TestNo.eq(testNo))
                .where(RoundResultDao.Properties.ResultState.eq(RoundResult.RESULT_STATE_NORMAL))
                .orderAsc(RoundResultDao.Properties.Result)
                .limit(1)
                .unique();
    }

    /**
     * ??????
     *
     * @param studentCode
     * @param testNo
     * @return
     */
    public RoundResult queryOrderDecScore(String studentCode, int testNo) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\ttestNo:" + testNo);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.TestNo.eq(testNo))
                .where(RoundResultDao.Properties.ResultState.eq(RoundResult.RESULT_STATE_NORMAL))
                .orderAsc(RoundResultDao.Properties.Result)
                .limit(1)
                .unique();
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param studentCode ??????
     * @return ??????
     */
    public RoundResult queryGroupOrderAscScore(String studentCode, long groupId) {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.GroupId.eq(groupId))
                .where(RoundResultDao.Properties.ResultState.eq(RoundResult.RESULT_STATE_NORMAL))
                .orderAsc(RoundResultDao.Properties.Result)
                .limit(1)
                .unique();
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param studentCode ??????
     * @return ??????
     */
    public RoundResult queryGroupOrderDescScore(String studentCode, long groupId) {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.GroupId.eq(groupId))
                .where(RoundResultDao.Properties.ResultState.eq(RoundResult.RESULT_STATE_NORMAL))
                .orderDesc(RoundResultDao.Properties.Result)
                .limit(1)
                .unique();
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param scheduleNo
     * @param itemCode
     * @param sort
     * @param sex
     * @return
     */
    public List<GroupItem> queryGroupItem(String scheduleNo, String itemCode, int sort, int sex) {
        return groupItemDao.queryBuilder().where(GroupItemDao.Properties.GroupNo.eq(sort))
                .where(GroupItemDao.Properties.ItemCode.eq(itemCode))
                .where(GroupItemDao.Properties.ScheduleNo.eq(scheduleNo))
                .where(GroupItemDao.Properties.GroupType.eq(sex)).orderDesc(GroupItemDao.Properties.TrackNo).list();
    }

    public List<GroupItem> queryGroupItemByCode(String itemCode) {
        return groupItemDao.queryBuilder().where(GroupItemDao.Properties.ItemCode.eq(itemCode)).list();
    }

    public List<GroupItem> queryGroupItemBySchedule(String schedule) {
        return groupItemDao.queryBuilder().where(GroupItemDao.Properties.ScheduleNo.eq(schedule)).list();
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param itemCode
     * @return
     */
    public List<GroupItem> queryGroupItemByItemCode(String itemCode) {
        return groupItemDao.queryBuilder()
                .where(GroupItemDao.Properties.ItemCode.eq(itemCode)).list();
    }

    /**
     * ??????????????????????????????????????????(??????)
     *
     * @param studentCode ??????
     * @return ??????????????????
     */
    public RoundResult queryGroupBestScore(String studentCode, long groupId) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\tIsLastResult:" + 1);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
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
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(item.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getItemCode(item)))
                .where(RoundResultDao.Properties.IsLastResult.eq(1))
                .unique();
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param studentCode ??????
     * @return
     */
    public RoundResult queryFinallyRountScore(String studentCode) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\tIsLastResult:" + 1);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .orderDesc(RoundResultDao.Properties.TestNo)
                .limit(1)
                .unique();
    }

    public List<RoundResult> queryRountScore(String studentCode) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\tIsLastResult:" + 1);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .orderDesc(RoundResultDao.Properties.TestNo).list();
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param studentCode ??????
     * @return
     */
    public List<RoundResult> queryFinallyRountScoreByExamTypeList(String studentCode, int exemType) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\tIsLastResult:" + 1);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.ExamType.eq(exemType))
                .orderDesc(RoundResultDao.Properties.TestNo)
                .orderAsc(RoundResultDao.Properties.RoundNo)
                .list();
    }

    public List<RoundResult> queryFinallyRountScoreByExamTypeAll(String studentCode, int exemType) {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.ExamType.eq(exemType))
                .orderDesc(RoundResultDao.Properties.TestNo)
                .list();
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param studentCode ??????
     * @param exemType
     * @return
     */
    public RoundResult queryFinallyRountScoreByExamType(String studentCode, String exemType) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\tIsLastResult:" + 1);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.ExamType.eq(exemType))
                .orderDesc(RoundResultDao.Properties.TestNo)
                .limit(1)
                .unique();
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param studentCode ??????
     * @param exemType
     * @return
     */
    public RoundResult queryLastRountScoreByExamType(String studentCode, int exemType, String itemCode) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\tIsLastResult:" + 1);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(itemCode))
                .where(RoundResultDao.Properties.ExamType.eq(exemType))
                .where(RoundResultDao.Properties.ResultState.eq(RoundResult.RESULT_STATE_NORMAL))
                .orderDesc(RoundResultDao.Properties.Result)
                .limit(1)
                .unique();
    }

    public RoundResult queryLastRountGroupBestScore(String studentCode, long groupId) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\tIsLastResult:" + 1);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.GroupId.eq(groupId))
                .where(RoundResultDao.Properties.ResultState.eq(RoundResult.RESULT_STATE_NORMAL))
                .orderDesc(RoundResultDao.Properties.Result)
                .limit(1)
                .unique();
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param studentCode ??????
     * @return
     */
    public RoundResult queryGroupFinallyRountScore(String studentCode, String groupId) {

        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.GroupId.eq(groupId))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .orderDesc(RoundResultDao.Properties.TestNo)
                .limit(1)
                .unique();
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param studentCode ??????
     * @param groupId     ??????ID
     * @param roundNo     ????????????
     * @return
     */
    public RoundResult queryGroupRoundNoResult(String studentCode, String groupId, int roundNo) {

        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.GroupId.eq(groupId))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.RoundNo.eq(roundNo))
                .limit(1)
                .unique();
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param studentCode ??????
     * @return
     */
    public List<RoundResult> queryGroupRound(String studentCode, String groupId) {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.GroupId.eq(groupId))
                .orderAsc(RoundResultDao.Properties.RoundNo)
                .list();
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param studentCode ??????
     * @return
     */
    public List<RoundResult> queryGroupRoundAll(String studentCode, String groupId) {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.GroupId.eq(groupId))
                .list();
    }

    public List<RoundResult> queryGroupRound(String studentCode, String groupId, int examType) {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.ExamType.eq(examType))
                .where(RoundResultDao.Properties.GroupId.eq(groupId))
                .list();
    }
    public List<RoundResult> queryAllRoundResult() {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .list();
    }
    /**
     * ????????????????????????????????????????????????
     *
     * @param studentCode ??????
     * @return
     */
    public List<RoundResult> queryRoundByRoundNo(String studentCode, int roundNo) {
        Logger.i("studentCode:" + studentCode + "\tMachineCode:" + TestConfigs.sCurrentItem.getMachineCode()
                + "\tItemCode:" + TestConfigs.getCurrentItemCode() + "\tIsLastResult:" + 1);
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.RoundNo.eq(roundNo))
                .list();
    }

    /**
     * ?????????????????????
     *
     * @param studentCode
     * @param itemCode
     * @param groupId
     * @return
     */
    public RoundResult queryResultByStudentCode(String studentCode, String itemCode, Long groupId) {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.ItemCode.eq(itemCode))
                .where(RoundResultDao.Properties.GroupId.eq(groupId))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .unique();
    }

    public List<RoundResult> queryResultBySchedule(String scheduleNo) {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.ScheduleNo.eq(scheduleNo))
                .list();
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param studentCode ??????
     * @return
     */
    public RoundResult queryRoundByRoundNo(String studentCode, int testNo, int roundNo) {
        return roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.RoundNo.eq(roundNo))
                .where(RoundResultDao.Properties.TestNo.eq(testNo))
                .limit(1)
                .unique();
    }


    /**
     * ???????????????????????????
     *
     * @param studentCode
     * @return
     */
    public RoundResult queryResultsByStudentCodeIsLastResult(String itemCode, String studentCode) {
        return roundResultDao
                .queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.ItemCode.eq(itemCode))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.IsLastResult.eq("1"))
                .limit(1)
                .unique();
    }

    /**
     * ??????????????????????????????
     *
     * @param studentCode
     */
    public void deleteStuResult(String studentCode) {
        roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param studentCode
     */
    public void deleteStuResult(String studentCode, int testNo, int rountNo, long groupId) {
        roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.IsDelete.eq(false))
                .where(RoundResultDao.Properties.StudentCode.eq(studentCode))
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(RoundResultDao.Properties.TestNo.eq(testNo))
                .where(RoundResultDao.Properties.RoundNo.eq(rountNo))
                .where(RoundResultDao.Properties.GroupId.eq(groupId))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }

    /**
     * ????????????????????????
     */
    public void deleteItemResult() {
        roundResultDao.queryBuilder()
                .where(RoundResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .buildDelete().executeDeleteWithoutDetachingEntities();
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
     * ???????????????????????????????????????
     *
     * @return
     */
    public List<UploadResults> getUploadResultsAll(boolean isUploadAll, String itemCode) {
        //???????????????????????????  ?????????????????????????????????
        //????????????????????? ????????????????????????????????????????????????
        //??????????????????id ????????????????????????????????????????????????

        List<String> stuCodeList = new ArrayList<>();
        StringBuffer sqlBuf1 = new StringBuffer("SELECT  DISTINCT " + RoundResultDao.Properties.StudentCode.columnName);
        sqlBuf1.append(" FROM " + RoundResultDao.TABLENAME);
//        sqlBuf1.append(" WHERE " + RoundResultDao.Properties.UpdateState.columnName + " = ? AND ");
        sqlBuf1.append(" WHERE " + RoundResultDao.Properties.ItemCode.columnName + " =  ?  AND ");
        sqlBuf1.append(RoundResultDao.Properties.IsDelete.columnName + " =  0  AND ");
        sqlBuf1.append(RoundResultDao.Properties.MachineCode.columnName + " = ? ");
        if (!isUploadAll) {
            sqlBuf1.append(" AND " + RoundResultDao.Properties.UpdateState.columnName + " = 0  ");
        }
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf1.toString(), new String[]{itemCode, TestConfigs.sCurrentItem.getMachineCode() + ""});
        while (c.moveToNext()) {
            stuCodeList.add(c.getString(0));
        }
        c.close();
        List<UploadResults> uploadResultsList = new ArrayList<>();
        for (String stuCode : stuCodeList) {
            //???????????????????????????
            List<RoundResult> stuResult = roundResultDao.queryBuilder().where(RoundResultDao.Properties.StudentCode.eq(stuCode))
//                    .where(RoundResultDao.Properties.UpdateState.eq(0))
                    .where(RoundResultDao.Properties.ItemCode.eq(itemCode))
                    .where(RoundResultDao.Properties.IsDelete.eq(false))
                    .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                    .list();
            Map<Long, List<RoundResult>> groupResult = new HashMap<>();
            Map<String, List<RoundResult>> personResult = new HashMap<>();
            //????????????
            for (RoundResult result : stuResult) {
                //???????????????
                if (result.getGroupId() > 0) {
                    //?????????ID????????????
                    List<RoundResult> saveResult = groupResult.get(result.getGroupId());
                    if (saveResult != null) {
                        saveResult.add(result);
                    } else {
                        saveResult = new ArrayList<>();
                        saveResult.add(result);
                    }
                    groupResult.put(result.getGroupId(), saveResult);
                } else {
                    //?????????No????????????
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
            //??????????????????????????????
            for (Map.Entry<String, List<RoundResult>> entity : personResult.entrySet()) {
                List<RoundResult> saveResult = entity.getValue();
                //????????????????????????
                Map<Integer, List<RoundResult>> testNumResult = new HashMap<>();
                for (RoundResult result : saveResult) {
                    //?????????ID????????????
                    List<RoundResult> resultList = testNumResult.get(result.getTestNo());
                    if (resultList != null) {
                        resultList.add(result);
                    } else {
                        resultList = new ArrayList<>();
                        resultList.add(result);
                    }
                    testNumResult.put(result.getTestNo(), resultList);
                }
                //??????????????????
                for (Map.Entry<Integer, List<RoundResult>> testEntity : testNumResult.entrySet()) {
                    if (!TextUtils.isEmpty(entity.getKey())) {

                        UploadResults uploadResults = new UploadResults(
                                TextUtils.equals(testEntity.getValue().get(0).getScheduleNo(), "-1") ? "" : testEntity.getValue().get(0).getScheduleNo(),
                                itemCode, testEntity.getValue().get(0).getStudentCode(), testEntity.getKey() + "",
                                null, RoundResultBean.beanCope(entity.getValue()));
                        uploadResultsList.add(uploadResults);
                    }
                }

            }
            for (Map.Entry<Long, List<RoundResult>> entity : groupResult.entrySet()) {
                //????????????
                Group group = groupDao.queryBuilder().where(GroupDao.Properties.Id.eq(entity.getKey())).unique();
                if (group != null) {
                    List<RoundResult> saveResult = entity.getValue();
                    UploadResults uploadResults = new UploadResults(group.getScheduleNo(),
                            itemCode, saveResult.get(0).getStudentCode(), "1",
                            group, RoundResultBean.beanCope(saveResult, group));
                    uploadResultsList.add(uploadResults);
                }
            }
        }
        return uploadResultsList;
    }


    /**
     * ???????????????????????????????????????
     *
     * @return
     */
    public List<UploadResults> getUploadResultsByStuCode(String itemCode, List<String> stuCodeList) {

        //???????????????????????????  ?????????????????????????????????
        //????????????????????? ????????????????????????????????????????????????
        //??????????????????id ????????????????????????????????????????????????
        if (stuCodeList == null || stuCodeList.size() == 0)
            return null;

        List<UploadResults> uploadResultsList = new ArrayList<>();
        for (String stuCode : stuCodeList) {
            List<RoundResult> stuResult = roundResultDao.queryBuilder().where(RoundResultDao.Properties.StudentCode.eq(stuCode))
//                    .where(RoundResultDao.Properties.UpdateState.eq(0))
                    .where(RoundResultDao.Properties.ItemCode.eq(itemCode))
                    .where(RoundResultDao.Properties.IsDelete.eq(false))
                    .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                    .list();
            Map<Long, List<RoundResult>> groupResult = new HashMap<>();
            Map<String, List<RoundResult>> personResult = new HashMap<>();
            //????????????
            for (RoundResult result : stuResult) {
                //???????????????
                if (result.getGroupId() > 0) {
                    //?????????ID????????????
                    List<RoundResult> saveResult = groupResult.get(result.getGroupId());
                    if (saveResult != null) {
                        saveResult.add(result);
                    } else {
                        saveResult = new ArrayList<>();
                        saveResult.add(result);
                    }
                    groupResult.put(result.getGroupId(), saveResult);
                } else {
                    //?????????No????????????
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
            //??????????????????????????????
            for (Map.Entry<String, List<RoundResult>> entity : personResult.entrySet()) {
                List<RoundResult> saveResult = entity.getValue();
                //????????????????????????
                Map<Integer, List<RoundResult>> testNumResult = new HashMap<>();
                for (RoundResult result : saveResult) {
                    //?????????ID????????????
                    List<RoundResult> resultList = testNumResult.get(result.getTestNo());
                    if (resultList != null) {
                        resultList.add(result);
                    } else {
                        resultList = new ArrayList<>();
                        resultList.add(result);
                    }
                    testNumResult.put(result.getTestNo(), resultList);
                }
                //??????????????????
                for (Map.Entry<Integer, List<RoundResult>> testEntity : testNumResult.entrySet()) {
                    if (!TextUtils.isEmpty(entity.getKey())) {

                        UploadResults uploadResults = new UploadResults(
                                TextUtils.equals(testEntity.getValue().get(0).getScheduleNo(), "-1") ? "" : testEntity.getValue().get(0).getScheduleNo(),
                                itemCode, testEntity.getValue().get(0).getStudentCode(), testEntity.getKey() + "",
                                null, RoundResultBean.beanCope(entity.getValue()));
                        uploadResultsList.add(uploadResults);
                    }
                }

            }
            for (Map.Entry<Long, List<RoundResult>> entity : groupResult.entrySet()) {
                //????????????
                Group group = groupDao.queryBuilder().where(GroupDao.Properties.Id.eq(entity.getKey())).unique();
                if (group != null) {
                    List<RoundResult> saveResult = entity.getValue();

                    UploadResults uploadResults = new UploadResults(group.getScheduleNo(),
                            itemCode, saveResult.get(0).getStudentCode(), "1",
                            group, RoundResultBean.beanCope(saveResult, group));
                    uploadResults.setGroupId(group.getId());
                    uploadResultsList.add(uploadResults);
                }
            }
        }

        return uploadResultsList;
    }

    /**
     * ????????????????????????????????????
     *
     * @return
     */
    public List<Map<String, Object>> getResultsByStu(String itemCode, List<Student> stuCodeList) {

        //???????????????????????????
        //????????????????????? ????????????????????????????????????
        //??????????????????id ????????????????????????????????????????????????
        if (stuCodeList == null || stuCodeList.size() == 0)
            return null;
        List<Map<String, Object>> stuResults = new ArrayList<>();
        List<UploadResults> uploadResultsList;
        Map<String, Object> dataMap;
        for (Student stu : stuCodeList) {
            uploadResultsList = new ArrayList<>();
            dataMap = new HashMap<>();
            dataMap.put("stu", stu);
            //??????????????????
            List<RoundResult> stuResult = roundResultDao.queryBuilder().where(RoundResultDao.Properties.StudentCode.eq(stu.getStudentCode()))
                    .where(RoundResultDao.Properties.ItemCode.eq(itemCode))
                    .where(RoundResultDao.Properties.IsDelete.eq(false))
                    .where(RoundResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                    .list();
            Map<Long, List<RoundResult>> groupResult = new HashMap<>();
            Map<String, List<RoundResult>> personResult = new HashMap<>();
            //????????????
            for (RoundResult result : stuResult) {
                //???????????????
                if (result.getGroupId() > 0) {
                    //?????????ID????????????
                    List<RoundResult> saveResult = groupResult.get(result.getGroupId());
                    if (saveResult != null) {
                        saveResult.add(result);
                    } else {
                        saveResult = new ArrayList<>();
                        saveResult.add(result);
                    }
                    groupResult.put(result.getGroupId(), saveResult);
                } else {
                    //?????????No????????????
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
            //??????????????????????????????
            for (Map.Entry<String, List<RoundResult>> entity : personResult.entrySet()) {
                List<RoundResult> saveResult = entity.getValue();
                //????????????????????????
                Map<Integer, List<RoundResult>> testNumResult = new HashMap<>();
                for (RoundResult result : saveResult) {
                    //???????????????????????????
                    List<RoundResult> resultList = testNumResult.get(result.getTestNo());
                    if (resultList != null) {
                        resultList.add(result);
                    } else {
                        resultList = new ArrayList<>();
                        resultList.add(result);
                    }
                    testNumResult.put(result.getTestNo(), resultList);
                    if (TextUtils.equals("0106620396", result.getStudentCode())) {
                        LogUtil.logDebugMessage(resultList.toString());
                    }
                }
                //??????????????????
                for (Map.Entry<Integer, List<RoundResult>> testEntity : testNumResult.entrySet()) {
                    if (!TextUtils.isEmpty(entity.getKey())) {

                        UploadResults uploadResults = new UploadResults(
                                TextUtils.equals(testEntity.getValue().get(0).getScheduleNo(), "-1") ? "" : testEntity.getValue().get(0).getScheduleNo(),
                                TestConfigs.getCurrentItemCode(), stu.getStudentCode(), testEntity.getKey() + "",
                                null, RoundResultBean.beanCope(testEntity.getValue()));
                        uploadResultsList.add(uploadResults);
                    }
                }

            }
            for (Map.Entry<Long, List<RoundResult>> entity : groupResult.entrySet()) {
                //????????????
                Group group = groupDao.queryBuilder().where(GroupDao.Properties.Id.eq(entity.getKey())).unique();
                if (group != null) {
                    List<RoundResult> saveResult = entity.getValue();

                    UploadResults uploadResults = new UploadResults(group.getScheduleNo(),
                            TestConfigs.getCurrentItemCode(), stu.getStudentCode(), "1",
                            group, RoundResultBean.beanCope(saveResult, group));
                    uploadResultsList.add(uploadResults);
                }
            }
            dataMap.put("results", uploadResultsList);
            stuResults.add(dataMap);
        }

        return stuResults;
    }

    public List<String> getResultTimeData(String itemCode) {
        StringBuffer sqlBuf = new StringBuffer("SELECT  DISTINCT date( " + RoundResultDao.Properties.TestTime.columnName + "/1000, 'unixepoch') as T ");
        sqlBuf.append(" FROM " + RoundResultDao.TABLENAME);
        sqlBuf.append(" WHERE " + RoundResultDao.Properties.ItemCode.columnName + "= ? AND ");
        sqlBuf.append(RoundResultDao.Properties.IsDelete.columnName + "= 0 ");
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{itemCode});

        List<String> timeList = new ArrayList<>();
        while (c.moveToNext()) {
            timeList.add(c.getString(0));
        }
        c.close();
        LogUtil.logDebugMessage(timeList.toString());
        return timeList;
    }

    public List<Student> getResultTimeDataStudent(String itemCode, String resultDate) {
        StringBuffer sqlBuf = new StringBuffer("SELECT S.* FROM " + StudentDao.TABLENAME + " S");
        sqlBuf.append(" WHERE S." + StudentDao.Properties.StudentCode.columnName + " IN ( ");
        sqlBuf.append(" SELECT  " + RoundResultDao.Properties.StudentCode.columnName);
        sqlBuf.append(" FROM " + RoundResultDao.TABLENAME);
        sqlBuf.append(" WHERE  date( " + RoundResultDao.Properties.TestTime.columnName + "/1000, 'unixepoch') = ? ");
        sqlBuf.append(" AND " + RoundResultDao.Properties.ItemCode.columnName + " = ?  ");
        sqlBuf.append(" AND " + RoundResultDao.Properties.IsDelete.columnName + " = 0  ");
        sqlBuf.append(" )");
        Cursor c = daoSession.getDatabase().rawQuery(sqlBuf.toString(), new String[]{resultDate, itemCode});

        List<Student> students = new ArrayList<>();
        while (c.moveToNext()) {
            Student student = studentDao.readEntity(c, 0);
            students.add(student);
        }
        c.close();
        return students;
    }

    /****************************??????***********************************/

    /**
     * ?????????????????????????????????????????????
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

    public List<ItemSchedule> queryItemSchedulesByItemCode(String itemCode) {
        return itemScheduleDao.queryBuilder().where(ItemScheduleDao.Properties.ItemCode.eq(itemCode)).list();
    }

    public List<ItemSchedule> queryItemSchedulesBySchedule(String scheduleNo) {
        return itemScheduleDao.queryBuilder().where(ItemScheduleDao.Properties.ScheduleNo.eq(scheduleNo)).list();
    }

//    public List<Schedule> getSchedulesByItemCode(String itemCode) {
//        List<ItemSchedule> itemSchedules = itemScheduleDao.queryBuilder().where(ItemScheduleDao.Properties.ItemCode.eq(itemCode)).list();
//        if (itemSchedules != null && itemSchedules.size() > 0) {
//            for (ItemSchedule itemSchedule : itemSchedules
//                    ) {
//                return scheduleDao.queryBuilder().where(ScheduleDao.Properties.ScheduleNo.eq(itemSchedule.getScheduleNo())).list();
//            }
//        }
//        return null;
//    }

    /**
     * ???????????????????????????????????????
     * ???????????????
     *
     * @return
     */
    public List<Schedule> queryCurrentSchedules() {
        List<Item> items = itemDao.queryBuilder()
                .where(ItemDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(ItemDao.Properties.ItemCode.isNotNull())
                .list();
        List<ItemSchedule> itemSchedules = new ArrayList<>();
        for (Item item : items
        ) {
            itemSchedules.addAll(itemScheduleDao.queryBuilder().where(ItemScheduleDao.Properties.ItemCode.eq(item.getItemCode())).list());
        }

        List<Schedule> schedules = new ArrayList<>();
        for (ItemSchedule schedule : itemSchedules
        ) {
            schedules.addAll(scheduleDao.queryBuilder().where(ScheduleDao.Properties.ScheduleNo.eq(schedule.getScheduleNo())).list());
        }

        //??????
        Set<Schedule> set = new HashSet<>();
        set.addAll(schedules);
        schedules.clear();
        schedules.addAll(set);
        //??????
        Collections.sort(schedules, new Comparator<Schedule>() {
            @Override
            public int compare(Schedule o1, Schedule o2) {
                return o1.getScheduleNo().compareTo(o2.getScheduleNo());
            }
        });
        return schedules;
    }

    /**
     * ????????????
     *
     * @return
     */
    public List<Schedule> getAllSchedules() {
        StringBuffer sqlBuf = new StringBuffer("SELECT S.*  ");
        sqlBuf.append("  FROM " + ScheduleDao.TABLENAME + " S");
        sqlBuf.append(" LEFT JOIN " + ItemScheduleDao.TABLENAME + " I ");
        sqlBuf.append(" ON S." + ScheduleDao.Properties.ScheduleNo.columnName + " = I." + ItemScheduleDao.Properties.ScheduleNo.columnName);
        sqlBuf.append(" WHERE I." + GroupItemDao.Properties.ItemCode.columnName + " = '" + TestConfigs.getCurrentItemCode() + "'");
        sqlBuf.append(" ORDER BY " + ScheduleDao.Properties.ScheduleNo.columnName + " ASC");
        List<Schedule> scheduleList = new ArrayList<>();
        Log.i("sql", sqlBuf.toString());
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

    public Schedule insertSchedule(Schedule schedule) {
        long id = scheduleDao.insertOrReplace(schedule);
        return scheduleDao.queryBuilder().where(ScheduleDao.Properties.Id.eq(id)).unique();
    }

    public void insertSchedulesList(List<Schedule> scheduleList) {
        scheduleDao.insertOrReplaceInTx(scheduleList);
    }

    public void insertItemSchedule(ItemSchedule itemSchedule) {
        itemScheduleDao.insertOrReplace(itemSchedule);
    }

    public void insertItemSchedulesList(final List<ItemSchedule> itemScheduleList) {
        //???????????????????????????????????????
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
     * ???????????????????????????
     *
     * @return
     */
    public List<Map<String, Object>> getStudenByStuItemAndGroup(Group group) {
        StringBuffer sqlBuf = new StringBuffer("SELECT S.* , I." + GroupItemDao.Properties.TrackNo.columnName);
        sqlBuf.append("  FROM " + StudentDao.TABLENAME + " S");
        sqlBuf.append(" LEFT JOIN " + GroupItemDao.TABLENAME + " I ");
        sqlBuf.append(" ON S." + StudentDao.Properties.StudentCode.columnName + " = I." + GroupItemDao.Properties.StudentCode.columnName);
        sqlBuf.append(" WHERE I." + GroupItemDao.Properties.ItemCode.columnName + " = '" + group.getItemCode() + "'");
        sqlBuf.append(" AND I." + GroupItemDao.Properties.ScheduleNo.columnName + " = " + group.getScheduleNo());
        sqlBuf.append(" AND I." + GroupItemDao.Properties.GroupType.columnName + " =  " + group.getGroupType());
        sqlBuf.append(" AND I." + GroupItemDao.Properties.SortName.columnName + " = '" + group.getSortName() + "'");
        sqlBuf.append(" AND I." + GroupItemDao.Properties.GroupNo.columnName + " =  " + group.getGroupNo());
        sqlBuf.append(" ORDER BY " + GroupItemDao.Properties.TrackNo.columnName + " ASC ");

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
                .where(GroupDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .list();
    }

    public List<Group> getGroupByScheduleNoAndItem(String scheduleNo, String itemCode, int position) {
        if (position == 0) {
            return groupDao.queryBuilder()
                    .where(GroupDao.Properties.ScheduleNo.eq(scheduleNo))
                    .where(GroupDao.Properties.ItemCode.eq(itemCode))
                    .where(GroupDao.Properties.IsTestComplete.notEq(GROUP_FINISH))
                    .orderAsc(GroupDao.Properties.GroupNo)
                    .list();
        } else {
            return groupDao.queryBuilder()
                    .where(GroupDao.Properties.ScheduleNo.eq(scheduleNo))
                    .where(GroupDao.Properties.ItemCode.eq(itemCode))
                    .where(GroupDao.Properties.IsTestComplete.eq(GROUP_FINISH))
                    .orderAsc(GroupDao.Properties.GroupNo)
                    .list();
        }
    }

    public Group getGroupByNo(String itemCode, int groupNo, int color) {
        return groupDao.queryBuilder()
                .where(GroupDao.Properties.ItemCode.eq(itemCode))
                .where(GroupDao.Properties.IsTestComplete.notEq(GROUP_FINISH))
                .where(GroupDao.Properties.GroupNo.eq(groupNo))
                .where(GroupDao.Properties.ColorId.eq(color))
                .unique();
    }

    public GroupItem getGroupItemByNo(String itemCode, int groupNo, int groupType, String scheduleNo) {
        return groupItemDao.queryBuilder()
                .where(GroupItemDao.Properties.ItemCode.eq(itemCode))
                .where(GroupItemDao.Properties.GroupNo.eq(groupNo))
                .where(GroupItemDao.Properties.GroupType.eq(groupType))
                .where(GroupItemDao.Properties.ScheduleNo.eq(scheduleNo))
                .unique();
    }


    /**
     * ????????????
     *
     * @param group
     */
    public long insertGroup(Group group) {
        return groupDao.insertOrReplace(group);
    }

    /**
     * ??????????????????
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
        //???????????????????????????????????????
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
                                group.getScheduleNo() + "", group.getExamType() + "", group.getIsTestComplete() + "", group.getItemCode(), group.getColorGroupName(), group.getColorId(), group.getRemark3()});
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
     * ??????????????????
     *
     * @param group
     */
    public void updateGroup(Group group) {
        groupDao.update(group);
    }

    public void updateGroups(List<Group> groups) {
        groupDao.updateInTx(groups);
    }

    public void updateGroupItems(List<GroupItem> groupItems) {
        groupItemDao.updateInTx(groupItems);
    }

    public List<Group> queryGroupByItemCode(String itemCode) {
        return groupDao.queryBuilder().where(GroupDao.Properties.ItemCode.eq(itemCode)).list();
    }

    public Group queryGroup(String itemCode, int groupNo) {
        return groupDao.queryBuilder().where(GroupDao.Properties.ItemCode.eq(itemCode)).where(GroupDao.Properties.GroupNo.eq(groupNo)).unique();
    }

    public Group queryGroup(String itemCode, int groupNo, String scheduleNo, int sex) {
        return groupDao.queryBuilder()
                .where(GroupDao.Properties.ItemCode.eq(itemCode))
                .where(GroupDao.Properties.GroupNo.eq(groupNo))
                .where(GroupDao.Properties.ScheduleNo.eq(scheduleNo))
                .where(GroupDao.Properties.GroupType.eq(sex))
                .unique();
    }

    public List<Group> queryGroup(String itemCode) {
        return groupDao.queryBuilder().where(GroupDao.Properties.ItemCode.eq(itemCode)).list();
    }

    public List<Group> queryGroupBySchedule(String schedule) {
        return groupDao.queryBuilder().where(GroupDao.Properties.ScheduleNo.eq(schedule)).list();
    }


    public List<Group> queryGroupByColorName(String colorName) {
        return groupDao.queryBuilder().where(GroupDao.Properties.ColorId.eq(colorName)).list();
    }

    public List<Group> loadAllGroup() {
        return groupDao.loadAll();
    }

    public List<Group> queryGroupIsFinish() {
        return groupDao.queryBuilder().where(GroupDao.Properties.IsTestComplete.notEq(GROUP_FINISH)).list();
    }

    public Group queryGroupById(long groupId) {
        return groupDao.queryBuilder().where(GroupDao.Properties.Id.eq(groupId)).unique();
    }

    /**
     * ??????????????????????????????
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
     * ????????????????????????
     *
     * @param groupItem
     */
    public void insertGroupItem(GroupItem groupItem) {
        if (groupItem == null) {
            return;
        }
        groupItemDao.insertOrReplace(groupItem);
    }


    /**
     * ???????????????????????????????????????
     * ?????????????????????TestConfigs.getCurrentItemCode()
     *
     * @param group
     * @param studentCode
     * @return
     */
    public GroupItem getItemStuGroupItem(Group group, String studentCode) {
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
            return groupItemDao.queryBuilder()
                    .where(GroupItemDao.Properties.ItemCode.eq(group.getItemCode()))
                    .where(GroupItemDao.Properties.ScheduleNo.eq(group.getScheduleNo()))
                    .where(GroupItemDao.Properties.GroupType.eq(group.getGroupType()))
                    .where(GroupItemDao.Properties.SortName.eq(group.getSortName()))
                    .where(GroupItemDao.Properties.GroupNo.eq(group.getGroupNo()))
                    .where(GroupItemDao.Properties.StudentCode.eq(studentCode))
                    .unique();
        } else {
            return groupItemDao.queryBuilder()
                    .where(GroupItemDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                    .where(GroupItemDao.Properties.ScheduleNo.eq(group.getScheduleNo()))
                    .where(GroupItemDao.Properties.GroupType.eq(group.getGroupType()))
                    .where(GroupItemDao.Properties.SortName.eq(group.getSortName()))
                    .where(GroupItemDao.Properties.GroupNo.eq(group.getGroupNo()))
                    .where(GroupItemDao.Properties.StudentCode.eq(studentCode))
                    .unique();
        }
    }

    /**
     * @param itemCode
     * @param studentCode
     * @return
     */
    public GroupItem getItemStuGroupItem(String itemCode, String studentCode) {
        List<GroupItem> groupItems = groupItemDao.queryBuilder()
                .where(GroupItemDao.Properties.ItemCode.eq(itemCode))
                .where(GroupItemDao.Properties.StudentCode.eq(studentCode))
                .list();
        for (GroupItem groupItem : groupItems) {
            if (groupItem.getExamType() == StudentItem.EXAM_MAKE) {
                return groupItem;
            }
        }
        return groupItems.get(0);
    }
//    public GroupItem getItemStuGroupItem(Group group, String studentCode) {
//        return groupItemDao.queryBuilder()
//                .where(GroupItemDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
//                .where(GroupItemDao.Properties.ScheduleNo.eq(group.getScheduleNo()))
//                .where(GroupItemDao.Properties.GroupType.eq(group.getGroupType()))
//                .where(GroupItemDao.Properties.SortName.eq(group.getSortName()))
//                .where(GroupItemDao.Properties.GroupNo.eq(group.getGroupNo()))
//                .where(GroupItemDao.Properties.StudentCode.eq(studentCode))
//                .unique();
//    }

    /**
     * ????????????????????????
     *
     * @param groupItem
     */
    public void updateStudentGroupItem(GroupItem groupItem) {
        groupItemDao.update(groupItem);

    }

    /**
     * ????????????????????????
     *
     * @param groupItems
     */
    public void updateStudentGroupItems(List<GroupItem> groupItems) {
        groupItemDao.updateInTx(groupItems);

    }

    /******************** ??????????????? **************/
    public List<MachineResult> queryMachineResultByItemCodeDefault(int machineCode) {
        return machineResultDao
                .queryBuilder()
                .where(MachineResultDao.Properties.MachineCode.eq(machineCode))
                .where(MachineResultDao.Properties.ItemCode.eq(TestConfigs.DEFAULT_ITEM_CODE))
                .list();
    }

    public List<MachineResult> getMachineResultByItemCode(String itemCode) {
        return machineResultDao.queryBuilder().where(MachineResultDao.Properties.ItemCode.eq(itemCode))
                .where(MachineResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode())).list();
    }

    public List<MachineResult> getItemRoundMachineResult(String stuCode, int testNo, int roundNo) {
        return machineResultDao.queryBuilder().where(MachineResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(MachineResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(MachineResultDao.Properties.StudentCode.eq(stuCode))
                .where(MachineResultDao.Properties.TestNo.eq(testNo))
                .where(MachineResultDao.Properties.ResultType.eq("0"))
                .where(MachineResultDao.Properties.RoundNo.eq(roundNo)).list();
    }

    public MachineResult getMachineResultReentry(String stuCode, int testNo, int roundNo) {
        return machineResultDao.queryBuilder().where(MachineResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(MachineResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(MachineResultDao.Properties.StudentCode.eq(stuCode))
                .where(MachineResultDao.Properties.TestNo.eq(testNo))
                .where(MachineResultDao.Properties.RoundNo.eq(roundNo))
                .where(MachineResultDao.Properties.ResultType.eq("1"))
                .orderDesc(MachineResultDao.Properties.Id)
                .limit(1)
                .unique();
    }

    public MachineResult getGroupMachineResultReentry(String stuCode, long groupId, int roundNo) {
        return machineResultDao.queryBuilder().where(MachineResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(MachineResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(MachineResultDao.Properties.StudentCode.eq(stuCode))
                .where(MachineResultDao.Properties.TestNo.eq(1))
                .where(MachineResultDao.Properties.GroupId.eq(groupId))
                .where(MachineResultDao.Properties.RoundNo.eq(roundNo))
                .where(MachineResultDao.Properties.ResultType.eq("1"))
                .orderDesc(MachineResultDao.Properties.Id)
                .limit(1)
                .unique();
    }


    public List<MachineResult> getItemGroupFRoundMachineResult(String stuCode, long groupId, int roundNo) {
        return machineResultDao.queryBuilder().where(MachineResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(MachineResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(MachineResultDao.Properties.StudentCode.eq(stuCode))
                .where(MachineResultDao.Properties.GroupId.eq(groupId))
                .where(MachineResultDao.Properties.ResultType.eq("0"))
                .where(MachineResultDao.Properties.RoundNo.eq(roundNo)).list();
    }

    public void insterMachineResult(MachineResult machineResult) {
        machineResultDao.insertInTx(machineResult);
    }

    public void insterMachineResults(List<MachineResult> machineResults) {
        machineResultDao.insertInTx(machineResults);
    }

    public void updateMachineResults(List<MachineResult> machineResults) {
        machineResultDao.updateInTx(machineResults);
    }

    public void deleteStuMachineResults(String studentCode, int testNo, int rountNo, long groupId) {
        machineResultDao.queryBuilder()
                .where(MachineResultDao.Properties.StudentCode.eq(studentCode))
                .where(MachineResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(MachineResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(MachineResultDao.Properties.TestNo.eq(testNo))
                .where(MachineResultDao.Properties.RoundNo.eq(rountNo))
                .where(MachineResultDao.Properties.GroupId.eq(groupId))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public void deleteStuMachineResultsReentry(String studentCode, int testNo, int rountNo, long groupId) {
        machineResultDao.queryBuilder()
                .where(MachineResultDao.Properties.StudentCode.eq(studentCode))
                .where(MachineResultDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(MachineResultDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .where(MachineResultDao.Properties.TestNo.eq(testNo))
                .where(MachineResultDao.Properties.RoundNo.eq(rountNo))
                .where(MachineResultDao.Properties.GroupId.eq(groupId))
                .where(MachineResultDao.Properties.ResultType.eq("1"))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public void deleteMachineResult(List<MachineResult> machineResultList) {
        machineResultDao.deleteInTx(machineResultList);
    }

    /**********************************??????********************************************************/
    public void insterThermometer(StudentThermometer thermometer) {
        thermometerDao.insertOrReplace(thermometer);
    }

    public List<StudentThermometer> getThermometerList(StudentItem studentItem) {
        return thermometerDao.queryBuilder().where(StudentThermometerDao.Properties.StudentCode.eq(studentItem.getStudentCode()))
                .where(StudentThermometerDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(StudentThermometerDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode())).list();
    }

    public StudentThermometer getThermometer(StudentItem studentItem) {
        return thermometerDao.queryBuilder().where(StudentThermometerDao.Properties.StudentCode.eq(studentItem.getStudentCode()))
                .where(StudentThermometerDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(StudentThermometerDao.Properties.ExamType.eq(studentItem.getExamType()))
                .where(StudentThermometerDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .limit(1).unique();
    }

    public StudentThermometer getThermometer(GroupItem groupItem) {
        return thermometerDao.queryBuilder().where(StudentThermometerDao.Properties.StudentCode.eq(groupItem.getStudentCode()))
                .where(StudentThermometerDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(StudentThermometerDao.Properties.ExamType.eq(groupItem.getExamType()))
                .where(StudentThermometerDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode()))
                .limit(1).unique();
    }


    public List<StudentThermometer> getItemThermometerList() {
        return thermometerDao.queryBuilder()
                .where(StudentThermometerDao.Properties.ItemCode.eq(TestConfigs.getCurrentItemCode()))
                .where(StudentThermometerDao.Properties.MachineCode.eq(TestConfigs.sCurrentItem.getMachineCode())).list();
    }

    /********************************************????????????**********************************************************************/
    public void insterAccount(String accountName, String pwd, int type) {
        Account account = new Account();
        account.setAccount(accountName);
        account.setPassword(pwd);
        account.setType(type + "");
        account.setCreateTime(DateUtil.getCurrentTime());
        accountDao.insertOrReplace(account);
    }

    public void insterAccount(Account account) {
        accountDao.insertOrReplace(account);
    }

    public void updateAccount(Account account) {

        account.setUpdateTime(DateUtil.getCurrentTime());
        accountDao.updateInTx(account);
    }

    public Account queryAccountByExamPersonnelId(String examPersonnelId) {
        return accountDao.queryBuilder().where(AccountDao.Properties.ExamPersonnelId.eq(examPersonnelId))
                .unique();
    }

    public Account queryAccount(String accountName, String pwd) {
        return accountDao.queryBuilder().where(AccountDao.Properties.Account.eq(accountName))
                .where(AccountDao.Properties.Password.eq(pwd)).unique();
    }

    public List<Account> getAccountAll() {
        return accountDao.loadAll();
    }

    public List<Account> getAccountFeatures() {
        return accountDao.queryBuilder()
                .where(AccountDao.Properties.FaceFeature.notEq(""))
                .where(AccountDao.Properties.FaceFeature.isNotNull()).list();
    }

    public void deleteAccountAll() {
        accountDao.deleteAll();
    }

    public void deleteAccount(Account account) {
        accountDao.delete(account);
    }
    /***********************************************??????***********************************************************/
    /**
     * ???????????????
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


    public List<Schedule> getSchedules() {
        return scheduleDao.loadAll();
    }

    public void insertItem(List<Item> itemInfos) {
        itemDao.insertOrReplaceInTx(itemInfos);
    }

    public void saveSchedules(List<Schedule> schedules) {
        scheduleDao.insertInTx(schedules);
    }

    public void updateGroupItem(GroupItem groupItem) {
        groupItemDao.updateInTx(groupItem);
    }

    public void insertStudentFaces(List<StudentFace> studentFaces) {
        studentFaceDao.insertOrReplaceInTx(studentFaces);
    }

    public void insertStudentFace(StudentFace studentFace) {
        studentFaceDao.insertOrReplace(studentFace);
    }

    public void clearFace() {
        studentFaceDao.deleteAll();
    }

    public StudentFace getStudentFeatures(String studentCode) {
        return studentFaceDao.queryBuilder().where(StudentFaceDao.Properties.StudentCode.eq(studentCode)).unique();
    }
}
