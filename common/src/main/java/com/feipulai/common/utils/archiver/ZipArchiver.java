package com.feipulai.common.utils.archiver;

import android.os.Handler;
import android.text.TextUtils;

import com.orhanobut.logger.utils.LogUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * zip 解压工具
 */
public class ZipArchiver {

    private Handler mHandler = new Handler();
    private ExecutorService mThreadPool = Executors.newSingleThreadExecutor();

    public void doUnArchiver(final String srcfile, final String unrarPath, final String password, final IArchiverListener listener) {
        if (TextUtils.isEmpty(srcfile) || TextUtils.isEmpty(unrarPath))
            return;
        LogUtils.normal("文件路径===》" + srcfile);
        LogUtils.normal("解压路径===》" + unrarPath);
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                File src = new File(srcfile);
                if (!src.exists())
                    return;
                try {
                    ZipFile zFile = new ZipFile(srcfile);
                    zFile.setFileNameCharset("GBK");
                    if (!zFile.isValidZipFile())
                        throw new ZipException("文件不合法!");
                    LogUtils.normal("解压文件===》1");
                    File destDir = new File(unrarPath);
                    if (destDir.isDirectory() && !destDir.exists()) {
                        destDir.mkdir();
                    }

                    if (zFile.isEncrypted()) {
                        zFile.setPassword(password.toCharArray());
                    }
                    LogUtils.normal("解压文件===》2");
                    if (listener != null)
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onStartArchiver();
                            }
                        });


//            zFile.extractAll(unrarPath);
                    FileHeader fh = null;
                    final int total = zFile.getFileHeaders().size();
                    LogUtils.normal("解压文件数量===》" + total);
                    for (int i = 0; i < zFile.getFileHeaders().size(); i++) {
                        fh = (FileHeader) zFile.getFileHeaders().get(i);
                        String entrypath = "";
                        if (fh.isFileNameUTF8Encoded()) {//解決中文乱码
                            entrypath = fh.getFileName().trim();
                        } else {
                            entrypath = fh.getFileName().trim();
                        }
                        entrypath = entrypath.replaceAll("\\\\", "/");

                        File file = new File(unrarPath + entrypath);
//                Log.d(TAG, "unrar entry file :" + file.getPath());
                        //文件夹中是否有存在文件，存在则删除文件再解压
                        if (file.exists()) {
                            file.delete();
                        }
                        zFile.extractFile(fh, unrarPath);

                        if (listener != null) {
                            final int finalI = i;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onProgressArchiver(finalI + 1, total);
                                }
                            });
                        }
                    }
                } catch (ZipException e1) {
                    e1.printStackTrace();
                }
                if (listener != null)
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onEndArchiver();
                        }
                    });
            }
        });


    }
}
