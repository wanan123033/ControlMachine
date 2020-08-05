package com.feipulai.common.utils;

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.feipulai.common.dbutils.BackupManager;
import com.feipulai.common.dbutils.UsbFileAdapter;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileOutputStream;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fujiayi on 2017/5/19.
 */
public class FileUtil {

    /**
     * 返回存储器剩余空间,百分比
     * 如30%,返回30
     */
    public static int getPercentRemainStorage() {

        File file = Environment.getExternalStorageDirectory();
        long freeSpace = file.getFreeSpace();
        long totalSpace = file.getTotalSpace();

        return (int) (freeSpace * 1.0d / totalSpace * 100);
    }

    public static long getFreeSpaceStorage() {
        File file = Environment.getExternalStorageDirectory();
        return file.getFreeSpace();
    }

    /**
     * 单位换算
     *
     * @param size      单位为B
     * @param isInteger 是否返回取整的单位
     * @return 转换后的单位
     */
    public static String formatFileSize(long size, boolean isInteger) {
        DecimalFormat fileIntegerFormat = new DecimalFormat("#0");
        DecimalFormat fileDecimalFormat = new DecimalFormat("#0.##");

        DecimalFormat df = isInteger ? fileIntegerFormat : fileDecimalFormat;
        String fileSizeString = "0M";
        if (size < 1024 && size > 0) {
            fileSizeString = df.format((double) size) + "B";
        } else if (size < 1024 * 1024) {
            fileSizeString = df.format((double) size / 1024) + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            fileSizeString = df.format((double) size / (1024 * 1024)) + "MB";
        } else {
            fileSizeString = df.format((double) size / (1024 * 1024 * 1024)) + "GB";
        }
        return fileSizeString;
    }

    public static boolean copyFromAssets(AssetManager assets, String source, UsbFile destFile) throws IOException {

        InputStream is = null;
        OutputStream fos = null;
        try {
            is = assets.open(source);
            if (destFile instanceof UsbFileAdapter) {
                fos = new FileOutputStream(((UsbFileAdapter) destFile).getFile());
            } else {
                fos = new UsbFileOutputStream(destFile);
            }


            byte[] buffer = new byte[1024];
            int size;
            while ((size = is.read(buffer, 0, 1024)) >= 0) {
                fos.write(buffer, 0, size);
            }
            return true;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            } else {
                return false;
            }
        }
    }

    public static void copyFromAssets(AssetManager assets, String source, String dest) throws IOException {
        File file = new File(dest);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = assets.open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                }
            }
        }
    }

    /**
     * 复制文件到目标文件中
     *
     * @param srcFile  源文件
     * @param destFile 目标文件
     * @return 复制成功返回true 失败返回false
     */
    public static boolean copyFile(File srcFile, File destFile) {
        if (srcFile == null || destFile == null) {
            return false;
        }
        // 如果源文件和目标文件相同则返回 false
        if (srcFile.equals(destFile)) {
            return false;
        }
        // 源文件不存在或者不是文件则返回 false
        if (!srcFile.exists() || !srcFile.isFile()) {
            return false;
        }
        //保证目标文件的文件夹存在
        if (!createOrExistsDir(destFile.getParentFile())) {
            return false;
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        //if(targetPath == null){
        //	String date = mDateFormat.format(Calendar.getInstance(Locale.CHINA).getTime());
        //	targetPath = AUTO_BACKUP_DIR + date;
        //}
        try {
            inputStream = new FileInputStream(srcFile);
            outputStream = new FileOutputStream(destFile);
            byte[] buf = new byte[4196];
            int length = 0;
            while ((length = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, length);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * 判断目录是否存在，不存在则判断是否创建成功
     *
     * @param directory 文件夹
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsDir(File directory) {
        // 如果存在，是目录则返回 true，是文件则返回 false，不存在则返回是否创建成功
        return directory != null && (directory.exists() ? directory.isDirectory() : directory.mkdirs());
    }


    /**
     * 读取文件内容，作为字符串返回
     */
    public static String readFileAsString(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        }

        if (file.length() > 1024 * 1024 * 1024) {
            throw new IOException("FILE is too large");
        }

        StringBuilder sb = new StringBuilder((int) (file.length()));
        // 创建字节输入流
        FileInputStream fis = new FileInputStream(filePath);
        // 创建一个长度为10240的Buffer
        byte[] bbuf = new byte[10240];
        // 用于保存实际读取的字节数
        int hasRead = 0;
        while ((hasRead = fis.read(bbuf)) > 0) {
            sb.append(new String(bbuf, 0, hasRead));
        }
        fis.close();
        return sb.toString();
    }

    /**
     * 根据文件路径读取byte[] 数组
     */
    public static byte[] readFileByBytes(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length());
            BufferedInputStream in = null;

            try {
                in = new BufferedInputStream(new FileInputStream(file));
                short bufSize = 1024;
                byte[] buffer = new byte[bufSize];
                int len1;
                while (-1 != (len1 = in.read(buffer, 0, bufSize))) {
                    bos.write(buffer, 0, len1);
                }

                byte[] var7 = bos.toByteArray();
                return var7;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException var14) {
                    var14.printStackTrace();
                }

                bos.close();
            }
        }
    }

    public static List<String> getFilesAllName(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
            Log.e("error", "空目录");
            return null;
        }
        List<String> s = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            s.add(files[i].getAbsolutePath());
        }
        return s;
    }

    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param fileName 要删除的文件名
     * @return 删除成功返回true，否则返回false
     */
    public static boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("删除文件失败:" + fileName + "不存在！");
            return false;
        } else {
            if (file.isFile())
                return deleteFile(fileName);
            else
                return deleteDirectory(fileName);
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param dir 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            System.out.println("删除目录失败：" + dir + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = FileUtil.deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = FileUtil.deleteDirectory(files[i]
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            System.out.println("删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            System.out.println("删除目录" + dir + "成功！");
            return true;
        } else {
            return false;
        }
    }

    /**
     * 创建根文件夹
     * <p/>
     * <br/>version
     * <br/>createTime 2016/12/27 , 下午10:58
     * <br/>updateTime 2016/12/27 , 下午10:58
     * <br/>createAuthor wzl
     * <br/>updateAuthor wzl
     * <br/>updateInfo
     */
    public static void createAllFile() {
        mkdirs(PATH_BASE);
        mkdirs(BackupManager.AUTO_BACKUP_DIR);
    }

    /**
     * 应用根目录
     */
    public static final String PATH_BASE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ControlMachine/";


    /**
     * 构建文件夹路径
     * <p>
     * <br/> Version: 1.0
     * <br/> CreateTime:  2013-11-3,下午12:08:58
     * <br/> UpdateTime:  2013-11-3,下午12:08:58
     * <br/> CreateAuthor:  CodeApe
     * <br/> UpdateAuthor:  CodeApe
     * <br/> UpdateInfo:  (此处输入修改内容,若无修改可不写.)
     *
     * @param path
     */
    public static void mkdirs(String path) {
        new File(path).mkdirs();
        try {
            new File(path + "/.nomedia").createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 构建文件夹路径
     * <p>
     * <br/> Version: 1.0
     * <br/> CreateTime:  2013-11-3,下午12:08:58
     * <br/> UpdateTime:  2013-11-3,下午12:08:58
     * <br/> CreateAuthor:  CodeApe
     * <br/> UpdateAuthor:  CodeApe
     * <br/> UpdateInfo:  (此处输入修改内容,若无修改可不写.)
     *
     * @param path
     */
    public static void mkdirs2(String path) {
        new File(path).mkdirs();

    }
}
