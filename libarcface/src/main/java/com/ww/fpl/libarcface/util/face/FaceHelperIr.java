package com.ww.fpl.libarcface.util.face;

import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;

import com.ww.fpl.libarcface.common.Constants;
import com.ww.fpl.libarcface.model.FacePreviewInfo;
import com.ww.fpl.libarcface.util.TrackUtil;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.LivenessInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class FaceHelperIr {
    private static final String TAG = "FaceHelperIr";
    private FaceEngine faceEngine;

    private Camera.Size previewSize;

    /**
     * fr 线程数，建议和ft初始化时的maxFaceNum相同
     */
    private int frThreadNum = 5;

    private List<FaceInfo> faceInfoList = new ArrayList<>();
    private List<LivenessInfo> livenessInfoList = new ArrayList<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean frThreadRunning = false;
    private FaceListener faceListener;
    private LinkedBlockingQueue<FaceRecognizeRunnable> faceRecognizeRunnables;
    //trackId相关
    private int currentTrackId = 0;
    private List<Integer> formerTrackIdList = new ArrayList<>();
    private List<Integer> currentTrackIdList = new ArrayList<>();
    private List<FaceInfo> formerFaceInfoList = new ArrayList<>();
    private List<FacePreviewInfo> facePreviewInfoList = new ArrayList<>();
    private ConcurrentHashMap<Integer, String> nameMap = new ConcurrentHashMap<>();

    private FaceHelperIr(Builder builder) {
        faceEngine = builder.faceEngine;
        faceListener = builder.faceListener;
        currentTrackId = builder.currentTrackId;
        previewSize = builder.previewSize;
        if (builder.frThreadNum > 0) {
            frThreadNum = builder.frThreadNum;
            faceRecognizeRunnables = new LinkedBlockingQueue<FaceRecognizeRunnable>(frThreadNum);
        } else {
            Log.e(TAG, "frThread num must > 0,now using default value:" + frThreadNum);
        }
        if (previewSize == null) {
            throw new RuntimeException("previewSize must be specified!");
        }
    }

    /**
     * 请求获取人脸特征数据
     *
     * @param nv21     图像数据
     * @param faceInfo 人脸信息
     * @param width    图像宽度
     * @param height   图像高度
     * @param format   图像格式
     * @param trackId  请求人脸特征的唯一请求码，一般使用trackId
     */
    public void requestFaceFeature(byte[] nv21, FaceInfo faceInfo, int width, int height, int format, Integer trackId) {
        if (faceListener != null) {
            if (faceEngine != null && faceRecognizeRunnables != null && faceRecognizeRunnables.size() < frThreadNum && !frThreadRunning) {
                faceRecognizeRunnables.add(new FaceRecognizeRunnable(nv21, faceInfo, width, height, format, trackId));
                executor.execute(faceRecognizeRunnables.poll());
            } else {
                faceListener.onFaceFeatureInfoGet(null, trackId);
            }
        }
    }


    public void release() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
        if (faceInfoList != null) {
            faceInfoList.clear();
        }
        if (faceRecognizeRunnables != null) {
            faceRecognizeRunnables.clear();
        }
        if (nameMap != null) {
            nameMap.clear();
        }
        faceRecognizeRunnables = null;
        nameMap = null;
        faceListener = null;
        faceInfoList = null;
    }

    public List<FacePreviewInfo> onPreviewFrame(byte[] nv21Rgb, byte[] nv21Ir) {
        if (faceListener != null) {
            if (faceEngine != null) {
                faceInfoList.clear();
                livenessInfoList.clear();
                long ftStartTime = System.currentTimeMillis();
                int code = faceEngine.detectFaces(nv21Rgb, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList);
                if (code != ErrorInfo.MOK) {
                    faceListener.onFail(new Exception("ft failed,code is " + code));
                } else {
//                    Log.i(TAG, "onPreviewFrame: ft costTime = " + (System.currentTimeMillis() - ftStartTime) + "ms");
                }
                /*
                 * 活体检测只支持一个人脸，所以只保留最大的人脸
                 * 若需要多人脸搜索，删除此行代码，并且关闭活体判断
                 */
                TrackUtil.keepMaxFace(faceInfoList);

                refreshTrackId(faceInfoList);

                if (faceInfoList.size() > 0 && faceInfoList.get(0) != null) {
                    List<FaceInfo> faceInfoListIr = adjustFaceInfoForIR(this.faceInfoList);
                    code = faceEngine.processIr(nv21Ir, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21,
                            faceInfoListIr, FaceEngine.ASF_IR_LIVENESS);
                    if (code != ErrorInfo.MOK) {
                        faceListener.onFail(new Exception("liveFaceEngine.LiveFaceDetectIr failed,code is " + code));
                    } else {
                        code = faceEngine.getIrLiveness(livenessInfoList);
                        if (code != ErrorInfo.MOK) {
                            faceListener.onFail(new Exception("getIrLiveness failed,code is " + code));
                        }
                    }
                }
            }
            facePreviewInfoList.clear();
            if (livenessInfoList.size() == faceInfoList.size()) {
                for (int i = 0; i < faceInfoList.size(); i++) {
                    facePreviewInfoList.add(new FacePreviewInfo(faceInfoList.get(i), livenessInfoList.get(i), currentTrackIdList.get(i)));
                }
            } else {
                for (int i = 0; i < faceInfoList.size(); i++) {
                    LivenessInfo livenessInfo = new LivenessInfo();
                    livenessInfoList.add(livenessInfo);
                    facePreviewInfoList.add(new FacePreviewInfo(faceInfoList.get(i), livenessInfoList.get(i), currentTrackIdList.get(i)));
                }
            }
            return facePreviewInfoList;
        } else {
            facePreviewInfoList.clear();
            return facePreviewInfoList;
        }
    }

    /**
     * 对于RGB数据的detectFaces结果，IR数据有时并不能直接使用，需要用户进行orient、rect的调整
     *
     * @param faceInfoList detectFaces获取到的人脸信息
     * @return 调整后传递给IR活体检测的人脸信息
     */
    private List<FaceInfo> adjustFaceInfoForIR(List<FaceInfo> faceInfoList) {
        List<FaceInfo> faceInfoListIr = new ArrayList<>();
        FaceInfo faceInfo = new FaceInfo(faceInfoList.get(0));
        faceInfo.getRect().offset(Constants.HORIZONTAL_OFFSET, Constants.VERTICAL_OFFSET);

        //若发现RGB相机和IR相机成像有镜像的情况，选择以下合适的一行或多行代码
//        faceInfo.setRect(mirrorRectHorizontal(faceInfo.getRect()));
//        faceInfo.setRect(mirrorRectVertical(faceInfo.getRect()));
        //若发现RGB相机和IR相机成像中人脸的朝向不同，可根据两个画面中的人脸角度差值一次或多次调用以下函数
//        faceInfo.setOrient(rotateOrient(faceInfo.getOrient()));

        faceInfoListIr.add(faceInfo);
        return faceInfoListIr;
    }

    /**
     * 人脸框水平镜像
     *
     * @param rect 人脸框
     * @return 水平镜像后的人脸框
     */
    private Rect mirrorRectHorizontal(Rect rect) {
        Rect newRect = new Rect(rect);
        newRect.right = previewSize.width - rect.left;
        newRect.left = previewSize.width - rect.right;
        return newRect;
    }

    /**
     * 人脸框垂直镜像
     *
     * @param rect 人脸框
     * @return 垂直镜像后的人脸框
     */
    private Rect mirrorRectVertical(Rect rect) {
        Rect newRect = new Rect(rect);
        newRect.top = previewSize.height - rect.bottom;
        newRect.bottom = previewSize.height - rect.top;
        return newRect;
    }


    /**
     * 获取逆时针旋转90度后的人脸角度
     *
     * @param orient 人脸角度信息，即{@link FaceInfo#orient}属性，由{@link FaceEngine#detectFaces(byte[], int, int, int, List)}接口获取
     * @return 旋转后的人脸角度
     */
    private int rotateOrient(int orient) {
        switch (orient) {
            case FaceEngine.ASF_OC_0:
                return FaceEngine.ASF_OC_90;
            case FaceEngine.ASF_OC_90:
                return FaceEngine.ASF_OC_180;
            case FaceEngine.ASF_OC_180:
                return FaceEngine.ASF_OC_270;
            case FaceEngine.ASF_OC_270:
                return FaceEngine.ASF_OC_0;
            default:
                throw new IllegalArgumentException("unsupported orient '" + orient + "'");
        }
    }

    /**
     * 人脸特征提取线程
     */
    public class FaceRecognizeRunnable implements Runnable {
        private FaceInfo faceInfo;
        private int width;
        private int height;
        private int format;
        private Integer trackId;
        private byte[] nv21Data;

        private FaceRecognizeRunnable(byte[] nv21Data, FaceInfo faceInfo, int width, int height, int format, Integer trackId) {
            if (nv21Data == null) {
                return;
            }
            this.nv21Data = nv21Data;
            this.faceInfo = new FaceInfo(faceInfo);
            this.width = width;
            this.height = height;
            this.format = format;
            this.trackId = trackId;
        }

        @Override
        public void run() {
            frThreadRunning = true;
            if (faceListener != null && nv21Data != null) {
                if (faceEngine != null) {
                    FaceFeature faceFeature = new FaceFeature();
                    long frStartTime = System.currentTimeMillis();
                    int frCode;
                    synchronized (FaceHelperIr.this) {
                        frCode = faceEngine.extractFaceFeature(nv21Data, width, height, format, faceInfo, faceFeature);
                    }
                    if (frCode == ErrorInfo.MOK) {
//                        Log.i(TAG, "run: fr costTime = " + (System.currentTimeMillis() - frStartTime) + "ms");
                        faceListener.onFaceFeatureInfoGet(faceFeature, trackId);
                    } else {
                        faceListener.onFaceFeatureInfoGet(null, trackId);
                        faceListener.onFail(new Exception("fr failed errorCode is " + frCode));
                    }
                } else {
                    faceListener.onFaceFeatureInfoGet(null, trackId);
                    faceListener.onFail(new Exception("fr failed ,frEngine is null"));
                }
                if (faceRecognizeRunnables != null && faceRecognizeRunnables.size() > 0) {
                    executor.execute(faceRecognizeRunnables.poll());
                }
            }
            nv21Data = null;
            frThreadRunning = false;
        }
    }


    /**
     * 刷新trackId
     *
     * @param ftFaceList 传入的人脸列表
     */
    private void refreshTrackId(List<FaceInfo> ftFaceList) {
        currentTrackIdList.clear();
        //每项预先填充-1
        for (int i = 0; i < ftFaceList.size(); i++) {
            currentTrackIdList.add(-1);
        }
        //前一次无人脸现在有人脸，填充新增TrackId
        if (formerTrackIdList.size() == 0) {
            for (int i = 0; i < ftFaceList.size(); i++) {
                currentTrackIdList.set(i, ++currentTrackId);
            }
        } else {
            //前后都有人脸,对于每一个人脸框
            for (int i = 0; i < ftFaceList.size(); i++) {
                //遍历上一次人脸框
                for (int j = 0; j < formerFaceInfoList.size(); j++) {
                    //若是同一张人脸
                    if (TrackUtil.isSameFace(formerFaceInfoList.get(j), ftFaceList.get(i))) {
                        //记录ID
                        currentTrackIdList.set(i, formerTrackIdList.get(j));
                        break;
                    }
                }
            }
        }
        //上一次人脸框不存在此人脸，新增
        for (int i = 0; i < currentTrackIdList.size(); i++) {
            if (currentTrackIdList.get(i) == -1) {
                currentTrackIdList.set(i, ++currentTrackId);
            }
        }
        formerTrackIdList.clear();
        formerFaceInfoList.clear();
        for (int i = 0; i < ftFaceList.size(); i++) {
            formerFaceInfoList.add(ftFaceList.get(i));
            formerTrackIdList.add(currentTrackIdList.get(i));
        }

        //刷新nameMap
        clearLeftName(currentTrackIdList);
    }

    /**
     * 获取当前的最大trackID,可用于退出时保存
     *
     * @return 当前trackId
     */
    public int getCurrentTrackId() {
        return currentTrackId;
    }

    /**
     * 新增搜索成功的人脸
     *
     * @param trackId 指定的trackId
     * @param name    trackId对应的人脸
     */
    public void addName(int trackId, String name) {
        if (nameMap != null) {
            nameMap.put(trackId, name);
        }
    }

    public String getName(int trackId) {
        return nameMap == null ? null : nameMap.get(trackId);
    }

    /**
     * 清除map中已经离开的人脸
     *
     * @param trackIdList 最新的trackIdList
     */
    private void clearLeftName(List<Integer> trackIdList) {
        Set<Integer> keySet = nameMap.keySet();
        for (Integer integer : keySet) {
            if (!trackIdList.contains(integer)) {
                nameMap.remove(integer);
            }
        }
    }

    public static final class Builder {
        private FaceEngine faceEngine;
        private Camera.Size previewSize;
        private FaceListener faceListener;
        private int frThreadNum;
        private int currentTrackId;

        public Builder() {
        }


        public Builder faceEngine(FaceEngine val) {
            faceEngine = val;
            return this;
        }


        public Builder previewSize(Camera.Size val) {
            previewSize = val;
            return this;
        }


        public Builder faceListener(FaceListener val) {
            faceListener = val;
            return this;
        }

        public Builder frThreadNum(int val) {
            frThreadNum = val;
            return this;
        }

        public Builder currentTrackId(int val) {
            currentTrackId = val;
            return this;
        }

        public FaceHelperIr build() {
            return new FaceHelperIr(this);
        }
    }
}
