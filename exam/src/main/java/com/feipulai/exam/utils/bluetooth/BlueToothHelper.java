package com.feipulai.exam.utils.bluetooth;

import android.app.Application;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.activity.setting.SystemSetting;


public final class BlueToothHelper {

    private BlueBindBean blueBindBean;

    private Application application;

    private static BlueToothHelper instances;

    private BlueToothHelper(MyApplication application) {
        this.application = application;
    }

    public static void init(MyApplication application) {
        if (instances == null) {
            instances = new BlueToothHelper(application);
        } else {
            instances.blueBindBean = SharedPrefsUtil.loadFormSource(instances.application, BlueBindBean.class);
        }
    }


    public synchronized static BlueBindBean getBlueBind() {
        if (instances == null) {
            return new BlueBindBean();
        }
        if (instances.blueBindBean == null)
            instances.blueBindBean = SharedPrefsUtil.loadFormSource(instances.application, BlueBindBean.class);
        if (instances.blueBindBean == null)
            instances.blueBindBean = new BlueBindBean();
        return instances.blueBindBean;
    }

    /**
     * 修改保存信息
     */
    public static boolean updateBlueBindCache(BlueBindBean blueBindBean) {
        if (blueBindBean == null)
            return false;
        instances.blueBindBean = blueBindBean;
        return SharedPrefsUtil.save(instances.application, blueBindBean);
    }

    /**
     * 清理信息
     */
    public static void clearBlueBindCache() {
        instances.blueBindBean = null;
        SharedPrefsUtil.remove(instances.application, BlueBindBean.class);
    }
}

