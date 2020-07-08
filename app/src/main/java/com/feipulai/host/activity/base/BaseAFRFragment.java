package com.feipulai.host.activity.base;

import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.TextureView;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.host.R;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Student;
import com.lgh.uvccamera.UVCCameraProxy;
import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PreviewCallback;
import com.ww.fpl.libarcface.faceserver.CompareResult;
import com.ww.fpl.libarcface.faceserver.FaceServer;
import com.ww.fpl.libarcface.faceserver.ThreadManager;
import com.ww.fpl.libarcface.model.DrawInfo;
import com.ww.fpl.libarcface.model.FacePreviewInfo;
import com.ww.fpl.libarcface.util.ConfigUtil;
import com.ww.fpl.libarcface.util.DrawHelper;
import com.ww.fpl.libarcface.util.face.FaceHelper;
import com.ww.fpl.libarcface.util.face.FaceListener;
import com.ww.fpl.libarcface.util.face.RecognizeColor;
import com.ww.fpl.libarcface.util.face.RequestFeatureStatus;
import com.ww.fpl.libarcface.widget.FaceRectView;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 人脸识别
 * Created by zzs on  2019/10/18
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BaseAFRFragment extends BaseFragment implements PreviewCallback {

    @BindView(R.id.textureView2)
    TextureView textureView2;
    @BindView(R.id.face_rect_view2)
    FaceRectView faceRectView2;
    private UVCCameraProxy mUVCCamera;
    private int mWidth = 640;
    private int mHeight = 480;
    private DrawHelper drawHelper;
    public boolean isOpenCamera = false;
    private DetectFaceOrientPriority ft_ori;
    private FaceEngine ftEngine;
    private int ftInitCode;
    private FaceEngine frEngine;
    private int frInitCode;
    private String TAG = "BaseAFRFragment";
    private FaceListener faceListener;
    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
    /**
     * 用于记录人脸特征提取出错重试次数
     */
    private ConcurrentHashMap<Integer, Integer> extractErrorRetryMap = new ConcurrentHashMap<>();
    /**
     * 出错重试最大次数
     */
    private static final int MAX_RETRY_TIME = 3;
    private int faceNumber;
    private FaceHelper faceHelper;
    private ThreadManager.ThreadPool threadPool;
    private FaceFeature mFaceFeature;
    private Integer faceId;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_base_afr;
    }

    @Override
    protected void initData() {
        initEngine();
        initCamera();
    }

    private void initCamera() {
        threadPool = ThreadManager.getThreadPool();
        faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFaceFail: " + e.getMessage());
            }

            //请求FR的回调
            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId, final Integer errorCode) {
                //FR成功
                if (faceFeature != null) {
                    //不做活体检测的情况，直接搜索
//                    searchFace(faceFeature, requestId);
                    //不做活体检测的情况，直接搜索
                    faceId = requestId;
                    mFaceFeature = faceFeature;
                    threadPool.execute(searchFace1);
                    threadPool.execute(searchFace2);
                    threadPool.execute(searchFace3);
                }
                //特征提取失败
                else {
                    if (increaseAndGetValue(extractErrorRetryMap, requestId) > MAX_RETRY_TIME) {
                        extractErrorRetryMap.put(requestId, 0);

                        String msg;
                        // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
                        if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                            msg = getString(R.string.low_confidence_level);
                        } else {
                            msg = "ExtractCode:" + errorCode;
                            if (faceNumber == 0) {
                                msg = "无注册信息";
                            }
                        }
                        faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, msg));
                        // 在尝试最大次数后，特征提取仍然失败，则认为识别未通过
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                        retryRecognizeDelayed(requestId);
                    } else {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                    }
                }
            }

            @Override
            public void onFaceLivenessInfoGet(@Nullable LivenessInfo livenessInfo, final Integer requestId, Integer errorCode) {
            }
        };

        initUVCCamera();

        drawHelper = new DrawHelper(mWidth, mHeight, textureView2.getWidth(), textureView2.getHeight(), 0
                , 0, false, false, false);

        faceHelper = new FaceHelper.Builder()
                .ftEngine(ftEngine)
                .frEngine(frEngine)
                .frQueueSize(1)
                .previewSize(mWidth, mHeight)
                .faceListener(faceListener)
                .trackedFaceCount(ConfigUtil.getTrackedFaceCount(mContext))
                .build();
    }

    /**
     * 将map中key对应的value增1回传
     *
     * @param countMap map
     * @param key      key
     * @return 增1后的value
     */
    public int increaseAndGetValue(Map<Integer, Integer> countMap, int key) {
        if (countMap == null) {
            return 0;
        }
        Integer value = countMap.get(key);
        if (value == null) {
            value = 0;
        }
        countMap.put(key, ++value);
        return value;
    }

    /**
     * 失败重试间隔时间（ms）
     */
    private static final long FAIL_RETRY_INTERVAL = 1000;
    private CompositeDisposable delayFaceTaskCompositeDisposable = new CompositeDisposable();

    /**
     * 延迟 FAIL_RETRY_INTERVAL 重新进行人脸识别
     *
     * @param requestId 人脸ID
     */
    private void retryRecognizeDelayed(final Integer requestId) {
        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸特征提取状态置为FAILED，帧回调处理时会重新进行人脸识别
//                        faceHelper.setName(requestId, Integer.toString(requestId));
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }


    public boolean gotoUVCFaceCamera(boolean isOpen) {
        if (isOpen) {
            isOpenCamera = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mUVCCamera.startPreview();
                }
            }, 100);
        } else {
            mUVCCamera.stopPreview();
            isOpenCamera = false;

        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unInitEngine();
        if (delayFaceTaskCompositeDisposable != null) {
            delayFaceTaskCompositeDisposable.clear();
        }

        if (faceHelper != null) {
            faceHelper.release();
            faceHelper = null;
        }

        faceRectView2 = null;
        drawHelper = null;
        mUVCCamera = null;
    }

    /**
     * 初始化引擎
     */
    private void initEngine() {
        ft_ori = DetectFaceOrientPriority.ASF_OP_0_ONLY;
        ftEngine = new FaceEngine();
        ftInitCode = ftEngine.init(getActivity(), DetectMode.ASF_DETECT_MODE_VIDEO, ft_ori,
                16, 1, FaceEngine.ASF_FACE_DETECT);

        frEngine = new FaceEngine();
        frInitCode = frEngine.init(getActivity(), DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                16, 1, FaceEngine.ASF_FACE_RECOGNITION);

        if (ftInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "ftEngine", ftInitCode);
            Log.i(TAG, "initEngine: " + error);
            ToastUtils.showShort(error);
        }

        if (frInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "frEngine", frInitCode);
            Log.i(TAG, "initEngine: " + error);
            ToastUtils.showShort(error);
        }

        //本地人脸库初始化
//        FaceServer.getInstance().init(mContext);
        faceNumber = FaceServer.getInstance().getFaceNumber(mContext);
    }

    /**
     * 销毁引擎
     */
    private void unInitEngine() {
        if (ftInitCode == ErrorInfo.MOK && ftEngine != null) {
            synchronized (ftEngine) {
                int ftUnInitCode = ftEngine.unInit();
                Log.i(TAG, "unInitEngine: " + ftUnInitCode);
            }
        }
        if (frInitCode == ErrorInfo.MOK && frEngine != null) {
            synchronized (frEngine) {
                int frUnInitCode = frEngine.unInit();
                Log.i(TAG, "unInitEngine: " + frUnInitCode);
            }
        }

    }

    private void initUVCCamera() {
        mWidth = 640;
        mHeight = 480;
        mUVCCamera = new UVCCameraProxy(mContext);
        // 已有默认配置，不需要可以不设置
//        mUVCCamera.getConfig()
//                .isDebug(true)
//                .setPicturePath(PicturePath.APPCACHE)
//                .setDirName("uvccamera")
//                .setProductId(0)
//                .setVendorId(0);
        mUVCCamera.setPreviewTexture(textureView2);
//        mUVCCamera.setPreviewSurface(surfaceView);

        mUVCCamera.setConnectCallback(new ConnectCallback() {
            @Override
            public void onAttached(UsbDevice usbDevice) {
//                Log.i("mUVCCamera", "onAttached--------");
                mUVCCamera.requestPermission(usbDevice);
            }

            @Override
            public void onGranted(UsbDevice usbDevice, boolean granted) {
//                Log.i("mUVCCamera", "onGranted--------" + granted);
                if (granted) {
                    mUVCCamera.connectDevice(usbDevice);
                }
            }

            @Override
            public void onConnected(UsbDevice usbDevice) {
//                Log.i("mUVCCamera", "onConnected--------");
                mUVCCamera.openCamera();
            }

            @Override
            public void onCameraOpened() {
//                Log.i("mUVCCamera", "onCameraOpened--------");
                mUVCCamera.setPreviewSize(mWidth, mHeight);
                mUVCCamera.startPreview();
            }

            @Override
            public void onDetached(UsbDevice usbDevice) {
//                Log.i("mUVCCamera", "onDetached--------");
                mUVCCamera.closeCamera();
            }
        });
        mUVCCamera.setPreviewCallback(this);
    }

    @Override
    public void onPreviewFrame(byte[] yuv) {
        if (faceRectView2 != null) {
            faceRectView2.clearFaceInfo();
        }
        List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(yuv);
        if (facePreviewInfoList != null && faceRectView2 != null && drawHelper != null) {
            drawPreviewInfo(facePreviewInfoList);
        }
        clearLeftFace(facePreviewInfoList);

        if (facePreviewInfoList != null && facePreviewInfoList.size() > 0) {
            for (int i = 0; i < facePreviewInfoList.size(); i++) {
                Integer status = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());
                /**
                 * 对于每个人脸，若状态为空或者为失败，则请求特征提取（可根据需要添加其他判断以限制特征提取次数），
                 * 特征提取回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer, Integer)}中回传
                 */
                if (status == null
                        || status == RequestFeatureStatus.TO_RETRY) {
                    requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
                    faceHelper.requestFaceFeature(yuv, facePreviewInfoList.get(i).getFaceInfo(), mWidth, mHeight, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
//                            Log.i(TAG, "onPreview: fr start = " + System.currentTimeMillis() + " trackId = " + facePreviewInfoList.get(i).getTrackedFaceCount());
                }
            }
        }
    }

    /**
     * 删除已经离开的人脸
     *
     * @param facePreviewInfoList 人脸和trackId列表
     */
    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
            requestFeatureStatusMap.clear();
            extractErrorRetryMap.clear();
            return;
        }
        Enumeration<Integer> keys = requestFeatureStatusMap.keys();
        while (keys.hasMoreElements()) {
            int key = keys.nextElement();
            boolean contained = false;
            for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
                if (facePreviewInfo.getTrackId() == key) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                requestFeatureStatusMap.remove(key);
                extractErrorRetryMap.remove(key);
            }
        }
    }

    /**
     * 画人脸框
     *
     * @param facePreviewInfoList
     */
    private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList) {
//        Log.i(TAG, "drawPreviewInfo-------" + facePreviewInfoList.toString());
        List<DrawInfo> drawInfoList = new ArrayList<>();
        for (int i = 0; i < facePreviewInfoList.size(); i++) {
            String name = faceHelper.getName(facePreviewInfoList.get(i).getTrackId());
            Integer recognizeStatus = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());

            // 根据识别结果和活体结果设置颜色
            int color = RecognizeColor.COLOR_UNKNOWN;
            if (recognizeStatus != null) {
                if (recognizeStatus == RequestFeatureStatus.FAILED) {
                    color = RecognizeColor.COLOR_FAILED;
                }
                if (recognizeStatus == RequestFeatureStatus.SUCCEED) {
                    color = RecognizeColor.COLOR_SUCCESS;
                }
            }
            drawInfoList.add(new DrawInfo(drawHelper.adjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect()),
                    GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE, LivenessInfo.UNKNOWN, color,
                    name == null ? "" : name));
        }
        drawHelper.draw(faceRectView2, drawInfoList);
    }

    private float SIMILAR_THRESHOLD = 0.82f;

    private CompareResult lastCompareResult;

    //3个线程查找
    private CompareResult compareResult1;
    private Runnable searchFace1 = new Runnable() {
        @Override
        public void run() {
            compareResult1 = FaceServer.getInstance().getTopOfFaceLib(mFaceFeature);
            compareResult();
        }
    };
    private CompareResult compareResult2;
    private Runnable searchFace2 = new Runnable() {
        @Override
        public void run() {
            compareResult2 = FaceServer.getInstance().getTopOfFaceLib2(mFaceFeature);
            compareResult();
        }
    };

    private CompareResult compareResult3;
    private Runnable searchFace3 = new Runnable() {
        @Override
        public void run() {
            compareResult3 = FaceServer.getInstance().getTopOfFaceLib3(mFaceFeature);
            compareResult();
        }
    };

    private void compareResult() {
        if (compareResult1 == null && compareResult2 == null && compareResult3 == null || faceHelper == null) {
            requestFeatureStatusMap.put(faceId, RequestFeatureStatus.FAILED);
            return;
        }

        if (compareResult1 != null && compareResult2 != null && compareResult3 != null) {
            if (compareResult1.getSimilar() > compareResult2.getSimilar()) {
                if (compareResult1.getSimilar() > compareResult3.getSimilar()) {
                    lastCompareResult = compareResult1;
                } else {
                    lastCompareResult = compareResult3;
                }
            } else {
                if (compareResult2.getSimilar() < compareResult3.getSimilar()) {
                    lastCompareResult = compareResult3;
                } else {
                    lastCompareResult = compareResult2;
                }
            }

            compareResult1 = null;
            compareResult2 = null;
            compareResult3 = null;
            if (lastCompareResult.getSimilar() > SIMILAR_THRESHOLD) {
                requestFeatureStatusMap.put(faceId, RequestFeatureStatus.SUCCEED);
                faceHelper.setName(faceId, mContext.getString(R.string.recognize_success_notice, lastCompareResult.getUserName()));
                Student student = DBManager.getInstance().queryStudentByCode(lastCompareResult.getUserName());
                compareListener.compareStu(student);
            }else {
                compareListener.compareStu(null);
            }
        }

    }


    /**
     * 人脸库中比对
     *
     * @param faceFeature
     */
    private void searchFace(final FaceFeature faceFeature, final Integer requestId) {
        Observable
                .create(new ObservableOnSubscribe<CompareResult>() {
                    @Override
                    public void subscribe(ObservableEmitter<CompareResult> emitter) {
                        CompareResult compareResult = FaceServer.getInstance().getTopOfFaceLib(faceFeature);
                        if (compareResult == null) {
                            emitter.onError(null);
                        } else {
                            emitter.onNext(compareResult);
                        }
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CompareResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(final CompareResult compareResult) {
                        if (compareResult == null || compareResult.getUserName() == null) {
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            return;
                        }

//                        drawHelper.draw(null, null);
                        isOpenCamera = false;
                        mUVCCamera.stopPreview();
                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
                            Student student = DBManager.getInstance().queryStudentByCode(compareResult.getUserName());
//                            onCheckIn(student);
                            compareListener.compareStu(student);
                            Log.e("compareResult", "++++++++++++" + compareResult.getUserName() + "-" + compareResult.getSimilar());

                        } else {
                            compareListener.compareStu(null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private onAFRCompareListener compareListener;

    public void setCompareListener(onAFRCompareListener compareListener) {
        this.compareListener = compareListener;
    }

    public interface onAFRCompareListener {
        void compareStu(Student student);
    }
}
