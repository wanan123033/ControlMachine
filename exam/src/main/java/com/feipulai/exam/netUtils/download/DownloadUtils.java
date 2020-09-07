package com.feipulai.exam.netUtils.download;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.feipulai.exam.netUtils.URLConstant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by pengjf on 2018/10/10.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class DownloadUtils {
    private static final String TAG = "DownloadUtil";
    private static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory() + "/DownloadFile/";
    //视频下载相关
    protected ApiInterface mApi;
    private Call<ResponseBody> mCall;
    private File mFile;
    private Thread mThread;
    private String mPath; //下载到本地的路径

    public DownloadUtils() {
        if (mApi == null) {
            mApi = ApiHelper.getInstance().buildRetrofit(URLConstant.BASE_URL)
                    .createService(ApiInterface.class);
        }
    }

    public void downloadFile(String url, String fileName, final DownloadListener downloadListener) {
        File targetFile = new File(DOWNLOAD_PATH);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        mPath = DOWNLOAD_PATH + fileName;
        mFile = new File(mPath);
        if (mFile.exists()) {//文件不存在则新建
            try {
                mFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mApi == null) {
            Log.e(TAG, "download: 下载接口为空了");
            return;
        }
        mCall = mApi.download(url);
        mCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull final Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                //下载文件放在子线程
                mThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        //保存到本地
                        writeFile2Disk(response, mFile, downloadListener);
                    }
                };
                mThread.start();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                downloadListener.onFailure("网络错误！");
            }
        });

    }

    private void writeFile2Disk(Response<ResponseBody> response, File file, DownloadListener downloadListener) {
        downloadListener.onStart();
        long currentLength = 0;
        OutputStream os = null;

        if (response.body() == null) {
            downloadListener.onFailure("资源错误！");
            return;
        }
        InputStream is = response.body().byteStream();
        long totalLength = response.body().contentLength();
        Log.e(TAG, "totalLength: " + totalLength);
        try {
            os = new FileOutputStream(file);
            int len;
            byte[] buff = new byte[1024];
            while ((len = is.read(buff)) != -1) {
                os.write(buff, 0, len);
                currentLength += len;
                Log.e(TAG, "当前进度: " + currentLength);
                downloadListener.onProgress((int) (100 * currentLength / totalLength));
                if ((int) (100 * currentLength / totalLength) == 100) {
                    downloadListener.onFinish(mPath);
                }
            }
        } catch (FileNotFoundException e) {
            downloadListener.onFailure("未找到文件！");
            e.printStackTrace();
        } catch (IOException e) {
            downloadListener.onFailure("IO错误！");
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

