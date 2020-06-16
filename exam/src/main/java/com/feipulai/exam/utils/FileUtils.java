package com.feipulai.exam.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by pengjf on 2020/6/11.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class FileUtils {
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH", Locale.CHINA);//yyyy_MM_dd_HH_mm_ss
    private static String logFileName = dateFormat.format(Calendar.getInstance().getTime()) + ".txt";
    private static String diskLogFilePath = Environment.getExternalStorageDirectory() + "/logger/pullSitUp/" + logFileName;

    /**
     * 文件数据写入（如果文件夹和文件不存在，则先创建，再写入）
     * @param filePath
     * @param content
     * @param flag true:如果文件存在且存在内容，则内容换行追加；false:如果文件存在且存在内容，则内容替换
     */
    public static String fileLinesWrite(String filePath,String content,boolean flag){
        String filedo = "write";
        FileWriter fw = null;
        try {
            File file=new File(filePath);
            //如果文件夹不存在，则创建文件夹
            if (!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
            if(!file.exists()){//如果文件不存在，则创建文件,写入第一行内容
                file.createNewFile();
                fw = new FileWriter(file);
                filedo = "create";
            }else{//如果文件存在,则追加或替换内容
                fw = new FileWriter(file, flag);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        pw.println(content);
        pw.flush();
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filedo;
    }


    public static void log(String log){
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CHINA);
        String logTime = dateFormat.format(Calendar.getInstance().getTime());
        fileLinesWrite(diskLogFilePath,"time:"+logTime+" log:"+log,true);
    }
}
