package com.feipulai.common.dbutils;

import android.content.Context;

import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.ToastUtils;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileInputStream;
import com.github.mjdev.libaums.fs.UsbFileOutputStream;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;


/**
 * Created by James on 2017/12/13.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class BackupManager {


    //自动备份文件夹
    public static final String AUTO_BACKUP_DIR = FileUtil.PATH_BASE + "/fpl_autobackup/";
    private final byte[] DB_HEAD;
    private final byte[] DB_END;

    public static final int TYPE_BODY_TEST = 0x1;
    public static final int TYPE_EXAM = 0x2;
    private File database;

    private DateFormat mDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    /**
     * @param type 数据库的类型  体测传{@link #TYPE_BODY_TEST}  考试传{@link #TYPE_EXAM}
     */
    public BackupManager(Context context, String dbName, int type) {
        switch (type) {
            // 数据库文件魔数,勿修改
            case TYPE_BODY_TEST:
                DB_HEAD = "TYPE_BODY_TEST,may the force be with you ".getBytes();
                DB_END = "TYPE_BODY_TEST,you jump ,i jump ,remember ? ,now ,you first".getBytes();
                break;

            case TYPE_EXAM:
                DB_HEAD = "TYPE_EXAM,run,forrest,run".getBytes();
                DB_END = "TYPE_EXAM,some birds can not be caged,their feathers are just too bright".getBytes();
                break;

            default:
                throw new IllegalArgumentException("wrong database type");
        }
        database = context.getDatabasePath(dbName);
    }

    /**
     * 手动备份
     *
     * @return 备份成功返回true, 否则返回false
     */
    public boolean backup(UsbFile file) {
        return backUp(database, file);
    }

    /**
     * 自动备份
     *
     * @return 备份成功返回true, 否则返回false
     */
    public boolean autoBackup() {
        String targetPath = AUTO_BACKUP_DIR + mDateFormat.format(Calendar.getInstance(Locale.CHINA).getTime()) + ".db";
        return backup(new UsbFileAdapter(new File(targetPath)));
    }

    /**
     * 恢复指定数据文件中数据到当前数据库中
     *
     * @return 恢复成功返回true, 否则返回false
     */
    public boolean restore(UsbFile file) {
        if (!isValidBackFile(file)) {
            return false;
        }
        boolean autoBackupSuccess = autoBackup();
        ToastUtils.showShort(autoBackupSuccess ? "数据库自动备份成功" : "数据库自动备份失败");
        return restore(file, database);
    }

    // 判断指定的备份文件是否是正确的备份文件格式
    private boolean isValidBackFile(UsbFile file) {
        try {
            long fileLength = file.getLength();
            if (fileLength < DB_HEAD.length + DB_END.length) {
                return false;
            }
            InputStream is;
            // 内部存储文件
            if (file instanceof UsbFileAdapter) {
                is = new FileInputStream(((UsbFileAdapter) file).getFile());
            } else {
                is = new UsbFileInputStream(file);
            }
            //先检查是否是完整的备份的数据库
            //头不对
            byte[] b = new byte[DB_HEAD.length];
            is.read(b);
            if (!Arrays.equals(b, DB_HEAD)) {
                Logger.e("备份文件头不正确");
                return false;
            }
            // 跳过这些字节
            long skip = fileLength - (DB_HEAD.length + DB_END.length);
            is.skip(skip);
            //尾不对
            b = new byte[DB_END.length];
            is.read(b);
            is.close();
            if (!Arrays.equals(b, DB_END)) {
                Logger.e("备份文件尾不正确");
                Logger.e("b" + Arrays.toString(b));
                return false;
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean restore(UsbFile file, File database) {
        OutputStream outputStream = null;
        try {
            InputStream is;
            // 内部存储文件
            if (file instanceof UsbFileAdapter) {
                is = new FileInputStream(((UsbFileAdapter) file).getFile());
            } else {
                is = new UsbFileInputStream(file);
            }
            is.skip(DB_HEAD.length);
            outputStream = new FileOutputStream(database, false);
            int totalLength = (int) (file.getLength() - DB_HEAD.length - DB_END.length);
            int hasWriteLength = 0;
            byte[] buf = new byte[4096];
            int length;
            while ((length = is.read(buf)) != -1) {
                if (hasWriteLength + length >= totalLength) {
                    //写最后的部分
                    outputStream.write(buf, 0, totalLength - hasWriteLength);
                    hasWriteLength = totalLength;
                } else {
                    outputStream.write(buf, 0, length);
                    hasWriteLength += length;
                }
            }
            outputStream.flush();
            is.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean backUp(File dbFile, UsbFile destFile) {
        InputStream inputStream;
        OutputStream outputStream;
        try {
            inputStream = new FileInputStream(dbFile);
            if (destFile instanceof UsbFileAdapter) {
                File parentFile = dbFile.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                outputStream = new FileOutputStream(((UsbFileAdapter) destFile).getFile());
            } else {
                outputStream = new UsbFileOutputStream(destFile);
            }
            //写数据库头
            outputStream.write(DB_HEAD, 0, DB_HEAD.length);
            byte[] buf = new byte[4096];
            int length;
            while ((length = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, length);
            }
            //写数据库尾
            outputStream.write(DB_END, 0, DB_END.length);
            outputStream.flush();
            inputStream.close();
            outputStream.close();
            // 非自动备份时需要进行这个处理
            if (FileSelectActivity.sSelectedFile != null && FileSelectActivity.sSelectedFile.isDirectory()) {
                UsbFile deleteFile = FileSelectActivity.sSelectedFile.createFile("." + destFile.getName() + "_delete.db");
                deleteFile.delete();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
