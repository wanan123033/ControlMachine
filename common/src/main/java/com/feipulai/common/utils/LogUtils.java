package com.feipulai.common.utils;

import android.os.Environment;

import com.feipulai.common.tts.TtsConstants;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.EncryptDiskLogAdapter;
import com.orhanobut.logger.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by James on 2019/2/15 0015.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class LogUtils{
	
	//初始化日志工具
	public static void initLogger(final boolean logToConsole,boolean logToRaw){
		//日志打印到控制台,在发布release版本时，会自动不打印
		Logger.addLogAdapter(new AndroidLogAdapter(){
			@Override
			public boolean isLoggable(int priority,String tag){
				return logToConsole;
			}
		});
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss",Locale.CHINA);
		String logFileName = dateFormat.format(Calendar.getInstance().getTime()) + ".txt";
		// 非加密日志存储在在sd卡中“logger”目录中
		String diskLogFilePath = Environment.getExternalStorageDirectory() + "/logger/" + logFileName;
		Logger.addLogAdapter(new DiskLogAdapter(diskLogFilePath));
		
		//加密日志存储
		String encryptLogFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/fair/play/" + logFileName;
		Logger.addLogAdapter(new EncryptDiskLogAdapter(encryptLogFilePath,TtsConstants.LOG_ENCRYPT_KEY));
		
		String rawLogFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/raw_log/" + logFileName;
		if(logToRaw){
			Logger.rawLogToFile(rawLogFilePath);
		}
	}
	
}
