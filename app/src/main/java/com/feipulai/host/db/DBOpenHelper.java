package com.feipulai.host.db;

import android.content.Context;

import com.feipulai.common.utils.LogUtil;
import com.feipulai.host.entity.DaoMaster;
import com.feipulai.host.entity.ItemDao;
import com.feipulai.host.entity.RoundResultDao;
import com.feipulai.host.entity.StudentDao;
import com.feipulai.host.entity.StudentItemDao;
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
        LogUtil.logDebugMessage(oldVersion + "---先前和更新之后的版本---" + newVersion);
        if (oldVersion < newVersion) {
            switch (newVersion) {
                case 3:
                    MigrationHelper.migrate(db, RoundResultDao.class);
                case 4:
                    MigrationHelper.migrate(db, StudentDao.class);
                case 5:
                    MigrationHelper.migrate(db, StudentDao.class);
                    MigrationHelper.migrate(db, ItemDao.class);

                case 8:
                    MigrationHelper.migrate(db, StudentDao.class);
                    MigrationHelper.migrate(db, ItemDao.class);
                    MigrationHelper.migrate(db, RoundResultDao.class);
                    MigrationHelper.migrate(db, StudentItemDao.class);
                    break;
            }

        }
    }

}
