package com.feipulai.host.activity.setting;

import android.app.Application;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.host.MyApplication;


public final class SettingHelper {

    private SystemSetting systemSetting;

    private Application application;

    private static SettingHelper instances;

    private SettingHelper(MyApplication application) {
        this.application = application;
    }

    public static void init(MyApplication application) {
        if (instances == null) {
            instances = new SettingHelper(application);
        } else {
            instances.systemSetting = SharedPrefsUtil.loadFormSource(instances.application, SystemSetting.class);
        }
    }


    public synchronized static SystemSetting getSystemSetting() {
        if (instances == null) {
            return new SystemSetting();
        }
        if (instances.systemSetting == null)
            instances.systemSetting = SharedPrefsUtil.loadFormSource(instances.application, SystemSetting.class);
        if (instances.systemSetting == null)
            instances.systemSetting = new SystemSetting();
        return instances.systemSetting;
    }

    /**
     * 修改保存信息
     */
    public static boolean updateSettingCache(SystemSetting systemSetting) {
        if (systemSetting == null)
            return false;
        instances.systemSetting = systemSetting;
        return SharedPrefsUtil.save(instances.application, systemSetting);
    }

    /**
     * 清理信息
     */
    public static void clearSettingCache() {
        instances.systemSetting = null;
        SharedPrefsUtil.remove(instances.application, SystemSetting.class);
    }
}

