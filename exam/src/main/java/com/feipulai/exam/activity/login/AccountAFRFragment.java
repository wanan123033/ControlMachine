package com.feipulai.exam.activity.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.utils.print.ViewImageUtils;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseFragment;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.bean.UserPhoto;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Account;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.netapi.HttpSubscriber;
import com.feipulai.exam.service.UpdateService;
import com.feipulai.exam.view.OperateProgressBar;
import com.lgh.uvccamera.UVCCameraProxy;
import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PreviewCallback;
import com.orhanobut.logger.utils.LogUtils;
import com.ww.fpl.libarcface.faceserver.CompareResult;
import com.ww.fpl.libarcface.faceserver.FaceServer;
import com.ww.fpl.libarcface.faceserver.ThreadManager;
import com.ww.fpl.libarcface.model.DrawInfo;
import com.ww.fpl.libarcface.model.FacePreviewInfo;
import com.ww.fpl.libarcface.model.FaceRegisterInfo;
import com.ww.fpl.libarcface.util.ConfigUtil;
import com.ww.fpl.libarcface.util.DrawHelper;
import com.ww.fpl.libarcface.util.face.FaceHelper;
import com.ww.fpl.libarcface.util.face.FaceListener;
import com.ww.fpl.libarcface.util.face.RecognizeColor;
import com.ww.fpl.libarcface.util.face.RequestFeatureStatus;
import com.ww.fpl.libarcface.widget.FaceRectView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * ????????????
 * Created by zzs on  2019/10/18
 * ??????????????????????????????????????????   ????????????:??????
 */
public class AccountAFRFragment extends BaseFragment implements PreviewCallback {

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
     * ????????????????????????????????????????????????
     */
    private ConcurrentHashMap<Integer, Integer> extractErrorRetryMap = new ConcurrentHashMap<>();
    /**
     * ????????????????????????
     */
    private static final int MAX_RETRY_TIME = 3;
    private int faceNumber;
    private FaceHelper faceHelper;
    private ThreadManager.ThreadPool threadPool;
    private FaceFeature mFaceFeature;
    private Integer faceId;
    private boolean isStartFace = false;
    private boolean isNetWork = false;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    isOpenCamera = false;
                    drawPreviewInfo(null);
                    break;
                default:
                    break;
            }
            return false;
        }
    });
    private SweetAlertDialog uploadDataDialog;
    private boolean isLodingServer = false;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_base_afr;
    }

    @Override
    protected void initData() {
        initEngine();
        initCamera();
        if (SettingHelper.getSystemSetting().getAfrContrast() == 0) {
            SIMILAR_THRESHOLD = 0.60F;
        } else if (SettingHelper.getSystemSetting().getAfrContrast() == 1) {
            SIMILAR_THRESHOLD = 0.70F;
        } else if (SettingHelper.getSystemSetting().getAfrContrast() == 2) {
            SIMILAR_THRESHOLD = 0.80F;
        } else {
            SIMILAR_THRESHOLD = 0.90F;
        }
    }

    private void initCamera() {
        threadPool = ThreadManager.getThreadPool();
        faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFaceFail: " + e.getMessage());
            }

            //??????FR?????????
            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId, final Integer errorCode) {
                Log.e("TAG", "++++++++++onFaceFeatureInfoGet");
                if (!isStartFace) {
                    return;
                }
                //FR??????
                //??????????????????
//                if (faceNumber == 0) {
//                    faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, "???????????????"));
//                    requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
//                    mUVCCamera.stopPreview();
//                    isOpenCamera = false;
//                    requestFeatureStatusMap.clear();
//                    extractErrorRetryMap.clear();
//                    isOpenCamera=false;
//                    compareListener.compareStu(null);
//                    return;
//                }
                if (faceFeature != null) {
                    Log.e("TAG", "++++++++++onFaceFeatureInfoGet if");
                    //??????????????????????????????????????????
//                    searchFace(faceFeature, requestId);
                    faceId = requestId;
                    mFaceFeature = faceFeature;
                    isStartFace = false;
                    threadPool.execute(searchFace1);
//                    threadPool.execute(searchFace2);
//                    threadPool.execute(searchFace3);


                }
                //??????????????????
                else {
                    Log.e("TAG", "++++++++++onFaceFeatureInfoGet else");
                    if (increaseAndGetValue(extractErrorRetryMap, requestId) > MAX_RETRY_TIME) {
                        extractErrorRetryMap.put(requestId, 0);
                        String msg;
//                        // ?????????FaceInfo????????????????????????????????????????????????????????????RGB????????????????????????????????????
                        if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                            msg = getString(R.string.low_confidence_level);
                        } else {
                            msg = "";
                            if (faceNumber == 0) {
                                msg = "???????????????";
                            }
                        }
                        faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, msg));
//                        // ??????????????????????????????????????????????????????????????????????????????
//                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                        retryRecognizeDelayed(requestId);
//                        isOpenCamera=false;
//                        compareListener.compareStu(null);
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

        drawHelper = new DrawHelper(mWidth, mHeight, mWidth, mHeight, 0
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
     * ???map???key?????????value???1??????
     *
     * @param countMap map
     * @param key      key
     * @return ???1??????value
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
     * ???????????????????????????ms???
     */
    private static final long FAIL_RETRY_INTERVAL = 1000;
    private CompositeDisposable delayFaceTaskCompositeDisposable = new CompositeDisposable();

    /**
     * ?????? FAIL_RETRY_INTERVAL ????????????????????????
     *
     * @param requestId ??????ID
     */
    private void retryRecognizeDelayed(final Integer requestId) {
        if (faceHelper == null) {
            return;
        }
//        isStartFace=true;
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
                        // ????????????????????????????????????FAILED????????????????????????????????????????????????
                        faceHelper.setName(requestId, Integer.toString(requestId));
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }


    public boolean gotoUVCFaceCamera(boolean isOpen) {
        isLodingServer = false;
        if (isOpen) {
            isStartFace = true;
            isOpenCamera = true;
            if (faceId == null) {
                faceId = 0;
            } else {
                faceHelper.setName(faceId, Integer.toString(faceId));
                requestFeatureStatusMap.put(faceId, RequestFeatureStatus.TO_RETRY);
            }
//            retryRecognizeDelayed(faceId);

        } else {
            isOpenCamera = false;
            isStartFace = false;
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
        mUVCCamera.stopPreview();
        faceRectView2 = null;
        drawHelper = null;
        mUVCCamera = null;
    }

    /**
     * ???????????????
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

        faceNumber = FaceServer.getInstance().getFaceNumber();
        Log.i("faceRegisterInfoList", "4----------" + faceNumber);
    }

    /**
     * ????????????
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
        mUVCCamera.setPreviewTexture(textureView2);
        mUVCCamera.setConnectCallback(new ConnectCallback() {
            @Override
            public void onAttached(UsbDevice usbDevice) {
                mUVCCamera.requestPermission(usbDevice);
            }

            @Override
            public void onGranted(UsbDevice usbDevice, boolean granted) {
                if (granted) {
                    mUVCCamera.connectDevice(usbDevice);
                }
            }

            @Override
            public void onConnected(UsbDevice usbDevice) {
                mUVCCamera.openCamera();
            }

            @Override
            public void onCameraOpened() {
                mUVCCamera.setPreviewSize(mWidth, mHeight);
                mUVCCamera.startPreview();
            }

            @Override
            public void onDetached(UsbDevice usbDevice) {
                mUVCCamera.closeCamera();
            }
        });
        mUVCCamera.setPreviewCallback(this);
    }

    @Override
    public void onPreviewFrame(final byte[] yuv) {
        if (!isStartFace) {
            return;
        }
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
                 * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                 * ??????????????????????????????????????????{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer, Integer)}?????????
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
     * ???????????????????????????
     *
     * @param facePreviewInfoList ?????????trackId??????
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
     * ????????????
     *
     * @param facePreviewInfoList
     */
    private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList) {
        List<DrawInfo> drawInfoList = new ArrayList<>();
        if (facePreviewInfoList != null) {
            for (int i = 0; i < facePreviewInfoList.size(); i++) {
                String name = faceHelper.getName(facePreviewInfoList.get(i).getTrackId());
                Integer recognizeStatus = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());

                // ?????????????????????????????????????????????
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
        }
        drawHelper.draw(faceRectView2, drawInfoList);
    }

    private float SIMILAR_THRESHOLD = 0.90f;
    private int hasTry = 0;

    //3???????????????
    private CompareResult compareResult1;
    private Runnable searchFace1 = new Runnable() {
        @Override
        public void run() {
            compareResult1 = FaceServer.getInstance().getTopOfFaceLib(mFaceFeature);
            compareResult2 = FaceServer.getInstance().getTopOfFaceLib2(mFaceFeature);
            compareResult3 = FaceServer.getInstance().getTopOfFaceLib3(mFaceFeature);
            compareResult();
        }
    };
    private CompareResult compareResult2;
    //    private Runnable searchFace2 = new Runnable() {
//        @Override
//        public void run() {
//            compareResult2 = FaceServer.getInstance().getTopOfFaceLib2(mFaceFeature);
//            compareResult();
//        }
//    };
//
    private CompareResult compareResult3;
//    private Runnable searchFace3 = new Runnable() {
//        @Override
//        public void run() {
//            compareResult3 = FaceServer.getInstance().getTopOfFaceLib3(mFaceFeature);
//            compareResult();
//        }
//    };

    private void compareResult() {
        Log.e("TAG", "BaseAFRFragment compareResult " + compareResult1);
        Log.e("TAG", "BaseAFRFragment compareResult " + compareResult2);
        Log.e("TAG", "BaseAFRFragment compareResult " + compareResult3);
        Log.e("TAG", "BaseAFRFragment compareResult " + faceHelper);
        if ((compareResult1 == null && compareResult2 == null && compareResult3 == null) || faceHelper == null) {
            Log.e("TAG", "BaseAFRFragment compareResult if ");
            requestFeatureStatusMap.put(faceId, RequestFeatureStatus.FAILED);
//            if (SettingHelper.getSystemSetting().isNetCheckTool() && !isNetface) {
//                isNetWork = true;
//                isStartFace = false;
//                Log.e("TAG", "++++++++++++++++++++++++netFace553");
//                netFace();
//            }
            return;
        }
        if (compareResult1 != null && compareResult2 != null && compareResult3 != null) {
            CompareResult lastCompareResult;
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
            Log.e("lastCompareResult", lastCompareResult.toString());
            compareResult1 = null;
            compareResult2 = null;
            compareResult3 = null;
            if (lastCompareResult.getSimilar() > SIMILAR_THRESHOLD) {
                requestFeatureStatusMap.put(faceId, RequestFeatureStatus.SUCCEED);
//                faceHelper.setName(faceId, mContext.getString(R.string.recognize_success_notice, lastCompareResult.getUserName()));
                Account account = DBManager.getInstance().queryAccountByExamPersonnelId(lastCompareResult.getUserName());

//                View view = getView();
//                view.setDrawingCacheEnabled(true);
//                view.buildDrawingCache(true);
//                Bitmap b = view.getDrawingCache();
//                saveStuImage(student,b);
//                b.recycle();
//                b = null;

                isOpenCamera = false;
                hasTry = 0;
                if (account == null) {
                    hasTry = 0;
                    faceHelper.setName(faceId, getString(R.string.recognize_failed_notice, ""));
                    isOpenCamera = false;
                    isStartFace = false;

//                    if (SettingHelper.getSystemSetting().isNetCheckTool() && !isNetface) {
//                        isNetWork = true;
//                        Log.e("TAG", "++++++++++++++++++++++++netFace602");
//                        netFace();
//                        return;
//                    } else {
//                        if (isLodingServer) {
//                            compareListener.compareStu(null);
//                        } else {
//                            showAddHint();
//                            return;
//                        }
//                    }
                    if (isLodingServer) {
                        compareListener.compareStu(null);
                    } else {
                        showAddHint();
                        return;
                    }
                } else {
                    LogUtils.operation("??????????????????" + account);
                    compareListener.compareStu(account);
                    isStartFace = false;
                    isNetWork = false;
                }

//                isOpenCamera=false;
            } else {
                hasTry++;
//                Logger.e("compareResult==>null----" + hasTry);
                if (hasTry < 3) {
                    faceHelper.setName(faceId, getString(R.string.recognize_failed_notice, ""));
                    isStartFace = true;
                    retryRecognizeDelayed(faceId);
                } else {
                    hasTry = 0;
                    faceHelper.setName(faceId, getString(R.string.recognize_failed_notice, ""));
                    isOpenCamera = false;
                    isStartFace = false;

//                    if (SettingHelper.getSystemSetting().isNetCheckTool() && !isNetface) {
//                        isNetWork = true;
//                        Log.e("TAG", "++++++++++++++++++++++++netFace602");
//                        netFace();
//                        return;
//                    } else {
//                        if (isLodingServer) {
//                            compareListener.compareStu(null);
//                        } else {
//                            showAddHint();
//                            return;
//                        }
//                    }
                    if (isLodingServer) {
                        compareListener.compareStu(null);
                    } else {
                        showAddHint();
                        return;
                    }
                }

            }
        } else {
            isStartFace = true;
            retryRecognizeDelayed(faceId);
        }

    }

    /**
     * ????????????????????????
     *
     * @param student
     * @param b
     */
    private void saveStuImage(Student student, Bitmap b) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_stu_saveinfo, null);
        ImageView iv_real = view.findViewById(R.id.iv_real);
        ImageView iv_portrait = view.findViewById(R.id.iv_portrait);
        TextView tv_name = view.findViewById(R.id.tv_name);
        TextView tv_sex = view.findViewById(R.id.tv_sex);
        TextView tv_item = view.findViewById(R.id.tv_item);
        TextView tv_school = view.findViewById(R.id.tv_school);
        TextView tv_stu_code = view.findViewById(R.id.tv_stu_code);
        TextView tv_id_card = view.findViewById(R.id.tv_id_card);
        iv_real.setImageBitmap(textureView2.getBitmap());
        iv_portrait.setImageBitmap(student.getBitmapPortrait());
        tv_name.setText("??????:" + student.getStudentName());
        tv_item.setText("??????:" + TestConfigs.sCurrentItem.getItemName());
        tv_sex.setText("??????:" + (student.getSex() == 0 ? "???" : "???"));
        tv_school.setText("??????:" + student.getSchoolName());
        tv_stu_code.setText("??????:" + student.getStudentCode());
        tv_id_card.setText("????????????:" + student.getIdCardNo());

        String path = MyApplication.PATH_FACE;

        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;     // ????????????????????????
        int height = metric.heightPixels;   // ????????????????????????
        ViewImageUtils.layoutView(view, width, height);
        ViewImageUtils.viewSaveToImage(view, path, student.getStudentCode() + "_" + TestConfigs.sCurrentItem.getItemName());

//        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache(true);
//        Bitmap bs = view.getDrawingCache();
//
//        File dir = new File(path,student.getStudentCode()+".jpg");
//        if (dir.exists()){
//            try {
//                dir.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        ImageUtil.saveBitmapToFile(path,student.getStudentCode()+".jpg",bs);
//        bs.recycle();
//        bs = null;
    }

    @Override
    public void onEventMainThread(BaseEvent event) {
        super.onEventMainThread(event);
//        if (event.getTagInt()== EventConfigs.SERVICE_UPLOAD_DATA_SUCCEED){
//            OperateProgressBar.removeLoadingUiIfExist(getActivity());
//            isStartFace = true;
//            isOpenCamera = true;
//            retryRecognizeDelayed(faceId);
//            ToastUtils.showShort("???????????????????????????");
//            TtsManager.getInstance().speak("???????????????????????????");
//        }else if (event.getTagInt()==EventConfigs.SERVICE_UPLOAD_DATA_ERROR){
//            isStartFace = true;
//            isOpenCamera = true;
//            OperateProgressBar.removeLoadingUiIfExist(getActivity());
//            ToastUtils.showShort("???????????????????????????????????????");
//            TtsManager.getInstance().speak("???????????????????????????????????????");
//        }
    }
//    private boolean isNetface = false; //??????netFace()?????????????????????  ????????????????????????????????????
//
//    public void netFace() {
//        if (isNetWork) {
//            isNetface = true;
//            isNetWork = false;
//            isStartFace = false;
//            LogUtils.operation("????????????????????????");
////            if (getActivity() != null){
////                getActivity().runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        OperateProgressBar.showLoadingUi(getActivity(), "???????????????????????????????????????");
////                    }
////                });
////                Intent intent = new Intent(mContext, UpdateService.class);
////                mContext.startService(intent);
////                isNetface = false;
////                return;
////            }
//            if (getActivity() != null)
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        OperateProgressBar.showLoadingUi(getActivity(), "???????????????...");
//                    }
//                });
//
//            if (mFaceFeature != null) {
//                HttpSubscriber httpSubscriber = new HttpSubscriber();
//                httpSubscriber.setOnRequestEndListener(new HttpSubscriber.OnRequestEndListener() {
//                    @Override
//                    public void onSuccess(int bizType) {
//                        if (getActivity() != null)
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    OperateProgressBar.removeLoadingUiIfExist(getActivity());
//                                }
//                            });
//
//                    }
//
//                    @Override
//                    public void onFault(int bizType) {
//                        if (getActivity() != null)
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    OperateProgressBar.removeLoadingUiIfExist(getActivity());
//                                }
//                            });
//                        isNetface = false;
//                        if (bizType == -1) {//?????????????????????
//                            isStartFace = true;
//                            ToastUtils.showShort("??????????????????");
//                            compareListener.compareStu(null);
//
//                        } else {
//                            isStartFace = true;
//                            retryRecognizeDelayed(faceId);
//                            ToastUtils.showShort("????????????");
//                        }
//                    }
//
//                    @Override
//                    public void onRequestData(Object data) {
//                        if (getActivity() != null)
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    OperateProgressBar.removeLoadingUiIfExist(getActivity());
//                                }
//                            });
//                        isNetface = false;
//                        isStartFace = false;
//                        isLodingServer = false;
//                        UserPhoto photo = (UserPhoto) data;
//                        if (TextUtils.isEmpty(photo.getStudentcode())) {
//                            //????????????
//                            LogUtils.operation("??????????????????????????????");
//                            ToastUtils.showShort("??????????????????");
//                            compareListener.compareStu(null);
//
//                        } else {
//                            //???????????????????????????????????????????????????????????????????????????
//                            Student student = DBManager.getInstance().queryStudentByCode(photo.getStudentcode());
//                            if (student != null) {
//                                LogUtils.operation("?????????????????????????????????" + student.toString());
//                                compareListener.compareStu(student);
//                                Intent intent = new Intent(mContext, UpdateService.class);
//                                mContext.startService(intent);
//                            } else {
//                                getStudent(photo.getStudentcode());
//                            }
//                        }
//                    }
//                });
//                httpSubscriber.sendFaceOnline("", "", Base64.encodeToString(mFaceFeature.getFeatureData(), Base64.DEFAULT));
//            }
//        }
//    }

    private void showAddHint() {
        isStartFace = false;

        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (uploadDataDialog == null || !uploadDataDialog.isShowing()) {
                    uploadDataDialog = new SweetAlertDialog(mContext).setTitleText("?????????????????????")
                            .setContentText("????????????????????????")
                            .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                    EventBus.getDefault().post(new BaseEvent(EventConfigs.GOTO_LOGIN));
                                }
                            }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                    isStartFace = true;
                                    retryRecognizeDelayed(faceId);

                                }
                            });
                    uploadDataDialog.show();
                }
            }
        });


    }

//    private void getStudent(final String studentCode) {
//        OperateProgressBar.showLoadingUi((Activity) mContext, "???????????????????????????...");
//        HttpSubscriber itemSubscriber = new HttpSubscriber();
//        itemSubscriber.setOnRequestEndListener(new HttpSubscriber.OnRequestEndListener() {
//            @Override
//            public void onSuccess(int bizType) {
//                isLodingServer = true;
//                if (getActivity() != null)
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            OperateProgressBar.removeLoadingUiIfExist(getActivity());
//                        }
//                    });
//                isStartFace = true;
//                retryRecognizeDelayed(faceId);
//                //????????????
//                Intent intent = new Intent(mContext, UpdateService.class);
//                mContext.startService(intent);
//
//            }
//
//            @Override
//            public void onFault(int bizType) {
//                if (getActivity() != null)
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            OperateProgressBar.removeLoadingUiIfExist(getActivity());
//                        }
//                    });
//                isStartFace = true;
//                isLodingServer = true;
//                retryRecognizeDelayed(faceId);
//            }
//
//            @Override
//            public void onRequestData(Object data) {
//                isLodingServer = true;
//                if (getActivity() != null)
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            OperateProgressBar.removeLoadingUiIfExist(getActivity());
//                        }
//                    });
//
//                List<Student> studentList = (List<Student>) data;
//                List<FaceRegisterInfo> registerInfoList = new ArrayList<>();
//                for (Student student : studentList) {
//                    if (!TextUtils.isEmpty(studentCode) && TextUtils.equals(student.getStudentCode(), studentCode)) {
//                        compareListener.compareStu(student);
//                    }
//                    registerInfoList.add(new FaceRegisterInfo(Base64.decode(student.getFaceFeature(), Base64.DEFAULT), student.getStudentCode()));
//                }
//                FaceServer.getInstance().addFaceList(registerInfoList);
//
//            }
//        });
//        itemSubscriber.getItemStudent(null,TestConfigs.getCurrentItemCode(), 1, StudentItem.EXAM_NORMAL,"", studentCode);
//    }

    private onAFRCompareListener compareListener;

    public void setCompareListener(onAFRCompareListener compareListener) {
        this.compareListener = compareListener;
    }

    public interface onAFRCompareListener {
        void compareStu(Account account);
    }
}
