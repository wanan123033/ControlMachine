package com.feipulai.host.netUtils.download;

import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.host.MyApplication;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.netUtils.CommonUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 *
 */

public class DownloadUtils {
    private List<String> stopList = new ArrayList<>();
    private List<String> downList = new ArrayList<>();

    public void stopDown(String fileName) {
        if (downList.contains(fileName)) {
            stopList.add(fileName);
        }
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
        long totalLength = Long.valueOf(response.headers().get("FileTotalSize"));
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
//                if ((int) (100 * currentLength / totalLength) == 100) {
//                }
            }

        } catch (FileNotFoundException e) {
            downloadListener.onFailure(file.getName(), "未找到文件！");
            e.printStackTrace();
            file.delete();
        } catch (IOException e) {
            downloadListener.onFailure(file.getName(), "IO错误！");
            e.printStackTrace();
            file.delete();
        } catch (ServerResponseException e) {
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


}

