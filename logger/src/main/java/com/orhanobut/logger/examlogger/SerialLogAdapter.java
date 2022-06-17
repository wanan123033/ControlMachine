package com.orhanobut.logger.examlogger;

import android.text.TextUtils;

import com.orhanobut.logger.AndroidDiskFormatStrategy;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogAdapter;
import com.orhanobut.logger.utils.LogUtils;

import java.text.SimpleDateFormat;

public class SerialLogAdapter implements LogAdapter, FormatStrategy {

    private AndroidDiskFormatStrategy formatStrategy;

    public SerialLogAdapter(String filePah) {
        this.formatStrategy = AndroidDiskFormatStrategy.newBuilder().path(filePah).dateFormat(new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss.SSS")).tag(LogUtils.SERIAL_TAG).build();
    }
    @Override
    public boolean isLoggable(int priority, String tag) {
        return true;
    }

    @Override
    public synchronized void log(int priority, String tag, String message) {
        if (!TextUtils.isEmpty(tag) && tag.equals(LogUtils.SERIAL_TAG))
            formatStrategy.log(priority, tag, message);
    }
}
