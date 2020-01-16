package com.feipulai.exam.activity.base;

import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.VersionInfo;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.R;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Student;
import com.lgh.uvccamera.UVCCameraProxy;
import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PreviewCallback;
import com.orhanobut.logger.Logger;
import com.ww.fpl.libarcface.faceserver.CompareResult;
import com.ww.fpl.libarcface.faceserver.FaceServer;
import com.ww.fpl.libarcface.model.DrawInfo;
import com.ww.fpl.libarcface.model.FacePreviewInfo;
import com.ww.fpl.libarcface.util.DrawHelper;
import com.ww.fpl.libarcface.widget.FaceRectView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
    private FaceEngine faceEngine;
    private int afCode;
    public boolean isOpenCamera = false;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_base_afr;
    }

    @Override
    protected void initData() {
        initUVCCamera();
        //本地人脸库初始化
        FaceServer.getInstance().init(mContext);
        drawHelper = new DrawHelper(mWidth, mHeight, mWidth, mHeight, 0
                , 1, true, false, false);
        initEngine();
    }

    public boolean gotoUVCFaceCamera(boolean isOpen) {
        if (FaceServer.faceRegisterInfoList == null || FaceServer.faceRegisterInfoList.size() == 0) {
            ToastUtils.showShort("本地无头像信息");
            return false;
        }
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
        FaceServer.getInstance().unInit();
        faceInfoList = null;
        facePreviewInfoList = null;
        faceRectView2 = null;
        faceFeature = null;
        faceEngine = null;
        drawHelper = null;
        mUVCCamera = null;
    }

    /**
     * 初始化引擎
     */
    private void initEngine() {
        faceEngine = new FaceEngine();
        afCode = faceEngine.init(mContext, FaceEngine.ASF_DETECT_MODE_VIDEO, FaceEngine.ASF_OP_0_HIGHER_EXT,
                16, 1, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_LIVENESS);
        VersionInfo versionInfo = new VersionInfo();
        faceEngine.getVersion(versionInfo);

        if (afCode != ErrorInfo.MOK) {
            ToastUtils.showShort(getString(R.string.init_failed, afCode));
        }

    }

    /**
     * 销毁引擎
     */
    private void unInitEngine() {
        if (afCode == ErrorInfo.MOK) {
            afCode = faceEngine.unInit();
        }
    }

    private void initUVCCamera() {
        mUVCCamera = new UVCCameraProxy(mContext);
        // 已有默认配置，不需要可以不设置
//        mUVCCamera.getConfig()
//                .isDebug(true)
//                .setPicturePath(PicturePath.APPCACHE)
//                .setDirName("uvccamera")
//                .setProductId(0)
//                .setVendorId(0);
        mUVCCamera.setPreviewTexture(textureView2);
//        mUVCCamera.setPreviewSurface(surface);

        mUVCCamera.setConnectCallback(new ConnectCallback() {
            @Override
            public void onAttached(UsbDevice usbDevice) {
                mUVCCamera.requestPermission(usbDevice);
            }

            @Override
            public void onGranted(UsbDevice usbDevice, boolean granted) {
                if (granted) {
                    Logger.i("-------------onGranted");
                    mUVCCamera.connectDevice(usbDevice);
                }
            }

            @Override
            public void onConnected(UsbDevice usbDevice) {
                Logger.i("----------------onConnected");
                mUVCCamera.openCamera();
//                isCamera = true;
            }

            @Override
            public void onCameraOpened() {
                Logger.i("-----------------onCameraOpened");
                mUVCCamera.setPreviewSize(640, 480);
            }

            @Override
            public void onDetached(UsbDevice usbDevice) {
                Logger.i("-----------------onDetached");
                mUVCCamera.closeCamera();
            }
        });

        mUVCCamera.setPreviewCallback(this);
    }

    private List<FaceInfo> faceInfoList = new ArrayList<>();
    private List<FacePreviewInfo> facePreviewInfoList = new ArrayList<>();
    private FaceFeature faceFeature;

    @Override
    public void onPreviewFrame(byte[] yuv) {
        if (faceRectView2 != null) {
            faceRectView2.clearFaceInfo();
        }
//        Logger.i("onPreviewResult---------: " + yuv.length);
        faceInfoList.clear();
        int code = faceEngine.detectFaces(yuv, mWidth, mHeight, FaceEngine.CP_PAF_NV21, faceInfoList);
        Log.i("faceInfoList", faceInfoList.toString());

        if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
            facePreviewInfoList.clear();
            for (int i = 0; i < faceInfoList.size(); i++) {
                facePreviewInfoList.add(new FacePreviewInfo(faceInfoList.get(i), null, 1));
            }

            if (facePreviewInfoList != null && faceRectView2 != null && drawHelper != null) {
                drawPreviewInfo(facePreviewInfoList);
            }

            faceFeature = new FaceFeature();
            //特征提取
            code = faceEngine.extractFaceFeature(yuv, mWidth, mHeight, FaceEngine.CP_PAF_NV21, faceInfoList.get(0), faceFeature);
            Log.i("extractFaceFeature", "---" + code);
            if (code == ErrorInfo.MOK) {
                searchFace(faceFeature);
            }
        } else {
            Log.i("detectFaces", "检测人脸失败");
        }
    }

    /**
     * 画人脸框
     *
     * @param facePreviewInfoList
     */
    private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList) {
        List<DrawInfo> drawInfoList = new ArrayList<>();
        for (int i = 0; i < facePreviewInfoList.size(); i++) {
            drawInfoList.add(new DrawInfo(drawHelper.adjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect()), GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE,
                    LivenessInfo.UNKNOWN,
                    "1"));
        }
        drawHelper.draw(faceRectView2, drawInfoList);
    }

    private float SIMILAR_THRESHOLD = 0.82f;

    /**
     * 人脸库中比对
     *
     * @param faceFeature
     */
    private void searchFace(final FaceFeature faceFeature) {
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
                            return;
                        }
                        drawHelper.draw(null, null);
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
