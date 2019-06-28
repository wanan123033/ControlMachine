package com.feipulai.exam.db;

import android.content.Context;
import android.util.Log;

import com.feipulai.exam.entity.DaoMaster;
import com.feipulai.exam.entity.ItemDao;
import com.feipulai.exam.entity.MachineResultDao;

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
        Log.i("version", oldVersion + "---先前和更新之后的版本---" + newVersion);

        if (oldVersion < newVersion) {
            switch (newVersion) {
                case 2:
                    MigrationHelper.migrate(db, MachineResultDao.class);
                    break;

            }

        }
    }

}
