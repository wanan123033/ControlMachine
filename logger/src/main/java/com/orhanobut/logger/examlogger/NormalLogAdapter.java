package com.orhanobut.logger.examlogger;

import android.text.TextUtils;

import com.orhanobut.logger.AndroidDiskFormatStrategy;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogAdapter;
import com.orhanobut.logger.utils.LogUtils;

public class NormalLogAdapter implements LogAdapter, FormatStrategy {

    private AndroidDiskFormatStrategy formatStrategy;

    public NormalLogAdapter(String filePah) {
        this.formatStrategy = AndroidDiskFormatStrategy.newBuilder().path(filePah).tag(LogUtils.NORMAL_TAG).build();
    }
    @Override
    public boolean isLoggable(int priority, String tag) {
        return true;
    }

    @Override
    public synchronized void log(int priority, String tag, String message) {
        if (!TextUtils.isEmpty(tag) && tag.equals(LogUtils.NORMAL_TAG))
        formatStrategy.log(priority, tag, message);
    }
}
