package com.feipulai.host.activity.base;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.netUtils.netapi.ItemSubscriber;

public class UpdateService extends IntentService {

    public UpdateService() {
        super("UpdateService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String lastDownLoadTime = SharedPrefsUtil.getValue(getApplicationContext(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.LAST_DOWNLOAD_TIME, "");
        ItemSubscriber subscriber = new ItemSubscriber();
        subscriber.getStudentData(1,lastDownLoadTime);
    }
}
