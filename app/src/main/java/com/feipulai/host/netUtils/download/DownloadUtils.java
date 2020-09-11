package com.feipulai.host.netUtils.download;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.feipulai.common.utils.LogUtil;
import com.feipulai.host.netUtils.CommonUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *
 */

public class DownloadUtils {
    private List<String> stopList = new ArrayList<>();
    private List<String> downList = new ArrayList<>();
    private static final String TAG = "DownloadUtil";
    private static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory() + "/DownloadFile/";
    //视频下载相关
    protected DownService mApi;
    private Call<ResponseBody> mCall;
    private File mFile;
    private Thread mThread;
    private String mPath; //下载到本地的路径


    public void stopDown(String fileName) {
        if (downList.contains(fileName)) {
            stopList.add(fileName);
        }

        if (mApi != null)
            mApi = null;
        if (mCall != null)
            mCall = null;
    }

    public void stopAllDown() {
        stopList.addAll(downList);
    }

    public void downloadFile(Observable<Response<ResponseBody>> observable, final String filePath, final String fileName, final DownloadListener downloadListener) {
//        HashMap<String, String> parameData = new HashMap<>();
//        parameData.put("batch",  "1");
//        parameData.put("uploadTime",   "");
//        parameData.put("itemcode", TestConfigs.getCurrentItemCode());
//        DownloadHelper.getInstance().buildRetrofit(CommonUtils.getIp()).createService(DownService.class)
//                .downloadFile("bearer " + MyApplication.TOKEN, CommonUtils.encryptQuery("10001", parameData))
        observable
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Response<ResponseBody>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        downloadListener.onStart(fileName);
                    }

                    @Override
                    public void onNext(Response<ResponseBody> response) {
                        LogUtil.logDebugMessage(response.headers().toString());
                        File file = new File(filePath + fileName);
                        downList.add(fileName);
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        writeFile2Disk(response, file, downloadListener);
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtil.logDebugMessage("onError: " + e.getMessage());
                        downList.remove(fileName);
                        downloadListener.onFailure(fileName, e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        LogUtil.logDebugMessage("onComplete: " + fileName);
                        downList.remove(fileName);
                        downloadListener.onFinish(fileName);
                    }
                });


    }

    private void writeFile2Disk(Response<ResponseBody> response, File file, DownloadListener downloadListener) {

        long currentLength = 0;
        OutputStream os = null;
        downloadListener.onResponse(response.headers());
        InputStream is = response.body().byteStream();
        long totalLength;
        if (response.headers() != null && response.headers().get("FileTotalSize") != null &&
                !TextUtils.equals(response.headers().get("FileTotalSize"), "0")) {
            totalLength = Long.valueOf(response.headers().get("FileTotalSize"));
        } else {
            totalLength = response.body().contentLength();
        }
        LogUtil.logDebugMessage("totalLength: " + totalLength);
        try {
            os = new FileOutputStream(file);
            int len;
            byte[] buff = new byte[1024];
            while ((len = is.read(buff)) != -1) {
                if (stopList.contains(file.getName())) {
                    file.delete();
                    stopList.remove(file.getName());
                    LogUtil.logDebugMessage("stop: " + file.getName());
                    throw new ServerResponseException(1, "");

                }
                os.write(buff, 0, len);
                currentLength += len;
                LogUtil.logDebugMessage("当前长度: " + currentLength);
                LogUtil.logDebugMessage("当前进度: " + (int) (100 * currentLength / totalLength));
                downloadListener.onProgress(file.getName(), (int) (100 * currentLength / totalLength));
                if ((int) (100 * currentLength / totalLength) == 100 && mApi != null) {
                    downloadListener.onFinish(mPath);
                }
            }

        } catch (FileNotFoundException e) {
            LogUtil.logDebugMessage("未找到文件: " + currentLength);
            downloadListener.onFailure(file.getName(), "未找到文件！");
            e.printStackTrace();
            file.delete();
        } catch (IOException e) {
            LogUtil.logDebugMessage("IO错误: " + currentLength);
            downloadListener.onFailure(file.getName(), "IO错误！");
            e.printStackTrace();
            file.delete();
        } catch (ServerResponseException e) {
            LogUtil.logDebugMessage("取消下载: " + currentLength);
            downloadListener.onFailure(file.getName(), "取消下载");
            e.printStackTrace();
            file.delete();
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

    public void downloadFile(String url, final String fileName, final DownloadListener downloadListener) {
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
                downloadListener.onFailure(fileName, "文件未找到");
                return;
            }
        }
        if (mApi == null) {
            mApi = DownloadHelper.getInstance().buildRetrofit(CommonUtils.getIp())
                    .createService(DownService.class);
        }
        if (mApi == null) {
            Log.e(TAG, "download: 下载接口为空了");
            return;
        }
        downList.add(fileName);
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
                downList.remove(fileName);
                downloadListener.onFailure("网络错误！", "IO错误");
            }
        });

    }
}

