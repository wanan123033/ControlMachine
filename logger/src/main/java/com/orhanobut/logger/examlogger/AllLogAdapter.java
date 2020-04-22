package com.orhanobut.logger.examlogger;

import android.text.TextUtils;

import com.orhanobut.logger.AndroidDiskFormatStrategy;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogAdapter;

public class AllLogAdapter implements LogAdapter, FormatStrategy {

    private AndroidDiskFormatStrategy formatStrategy;

    public AllLogAdapter(String filePah) {
        this.formatStrategy = AndroidDiskFormatStrategy.newBuilder().path(filePah).tag(LogUtils.ALL_TAG).build();
    }
    @Override
    public boolean isLoggable(int priority, String tag) {
        return true;
    }

    @Override
    public void log(int priority, String tag, String message) {
        if (!TextUtils.isEmpty(tag) && tag.equals(LogUtils.ALL_TAG))
            formatStrategy.log(priority, tag, message);
    }
}
