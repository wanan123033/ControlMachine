package com.feipulai.exam.db;

import android.content.Context;

import com.feipulai.exam.entity.AccountDao;
import com.feipulai.exam.entity.ChipGroupDao;
import com.feipulai.exam.entity.ChipInfoDao;
import com.feipulai.exam.entity.DaoMaster;
import com.feipulai.exam.entity.GroupDao;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.GroupItemDao;
import com.feipulai.exam.entity.ItemDao;
import com.feipulai.exam.entity.MachineResultDao;
import com.feipulai.exam.entity.RoundResultDao;
import com.feipulai.exam.entity.StudentDao;
import com.feipulai.exam.entity.StudentFaceDao;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.entity.StudentItemDao;
import com.orhanobut.logger.Logger;

import org.greenrobot.greendao.database.Database;

/**
 * Created by zzs on  2019/6/24
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class DBOpenHelper extends DaoMaster.OpenHelper {


    public DBOpenHelper(Context context, String name) {
        super(context, name, null);
    }


    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        Logger.i(oldVersion + "---先前和更新之后的版本---" + newVersion);

        if (oldVersion < newVersion) {
            switch (newVersion) {
                case 4:
                    MigrationHelper.migrate(db, MachineResultDao.class);
                    MigrationHelper.migrate(db, ChipInfoDao.class);
                    MigrationHelper.migrate(db, ChipGroupDao.class);
                    MigrationHelper.migrate(db, ItemDao.class);
                    MigrationHelper.migrate(db, RoundResultDao.class);
                    MigrationHelper.migrate(db, GroupDao.class);
                case 5:
                    MigrationHelper.migrate(db, ItemDao.class);
                case 6:
                    MigrationHelper.migrate(db, RoundResultDao.class);
                case 7:
                    MigrationHelper.migrate(db, StudentDao.class);
                case 9:
                    MigrationHelper.migrate(db, RoundResultDao.class);
                case 10:
                    MigrationHelper.migrate(db, StudentDao.class);
                case 11://1.2.1
                case 12://1.2.1
                case 13:
                    MigrationHelper.migrate(db, StudentDao.class);
                    MigrationHelper.migrate(db, AccountDao.class);
                    MigrationHelper.migrate(db, ItemDao.class);
                    MigrationHelper.migrate(db, RoundResultDao.class);
                case 14:
                    MigrationHelper.migrate(db, RoundResultDao.class);
                case 15:
                    MigrationHelper.migrate(db, RoundResultDao.class);
                    MigrationHelper.migrate(db, StudentItemDao.class);
                    break;
                case 16:
                    MigrationHelper.migrate(db, StudentItemDao.class);
                    MigrationHelper.migrate(db, GroupItemDao.class);
                    MigrationHelper.migrate(db, StudentFaceDao.class);
                    break;

            }

        }
    }

}
