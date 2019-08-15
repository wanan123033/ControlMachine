package com.feipulai.exam.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.VersionInfo;
import com.feipulai.exam.R;
import com.lgh.uvccamera.UVCCameraProxy;
import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PreviewCallback;
import com.ww.fpl.libarcface.faceserver.CompareResult;
import com.ww.fpl.libarcface.faceserver.FaceServer;
import com.ww.fpl.libarcface.model.DrawInfo;
import com.ww.fpl.libarcface.model.FacePreviewInfo;
import com.ww.fpl.libarcface.util.DrawHelper;
import com.ww.fpl.libarcface.widget.FaceRectView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UVCCameraActivity extends AppCompatActivity implements PreviewCallback {

    @BindView(R.id.textureView2)
    TextureView textureView2;
    @BindView(R.id.face_rect_view2)
    FaceRectView faceRectView2;
    @BindView(R.id.tv_name2)
    TextView tvName2;

    private static final String TAG = "UVCCameraActivity";
    private UVCCameraProxy mUVCCamera;
    private int mWidth=640;
    private int mHeight=480;
    private DrawHelper drawHelper;
    private FaceEngine faceEngine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uvccamera);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initUVCCamera();

        // Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        //本地人脸库初始化
        FaceServer.getInstance().init(this);

        drawHelper = new DrawHelper(mWidth, mHeight, mWidth, mHeight, 0
                , 1, true, false, false);
        initEngine();
    }


    /**
     * 初始化引擎
     */
    private void initEngine() {
        faceEngine = new FaceEngine();
        int afCode = faceEngine.init(this, FaceEngine.ASF_DETECT_MODE_VIDEO, FaceEngine.ASF_OP_0_HIGHER_EXT,
                16, 1, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_LIVENESS);
        VersionInfo versionInfo = new VersionInfo();
        faceEngine.getVersion(versionInfo);
        Log.i(TAG, "initEngine:  init: " + afCode + "  version:" + versionInfo);

        if (afCode != ErrorInfo.MOK) {
            Toast.makeText(this, getString(R.string.init_failed, afCode), Toast.LENGTH_SHORT).show();
        }

    }

    private void initUVCCamera() {
        mUVCCamera = new UVCCameraProxy(this);
        // 已有默认配置，不需要可以不设置
//        mUVCCamera.getConfig()
//                .isDebug(true)
//                .setPicturePath(PicturePath.APPCACHE)
//                .setDirName("uvccamera")
//                .setProductId(0)
//                .setVendorId(0);
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
                mUVCCamera.setPreviewSize(640, 480);
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
    protected void onDestroy() {
        super.onDestroy();
        FaceServer.getInstance().unInit();
    }

    private List<FaceInfo> faceInfoList = new ArrayList<>();
    private List<FacePreviewInfo> facePreviewInfoList = new ArrayList<>();
    @Override
    public void onPreviewFrame(byte[] yuv) {
        if (faceRectView2 != null) {
            faceRectView2.clearFaceInfo();
        }
        Log.d(TAG, "onPreviewResult---------: " + yuv.length);
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


            FaceFeature faceFeature = new FaceFeature();
            //特征提取
            code = faceEngine.extractFaceFeature(yuv, mWidth, mHeight, FaceEngine.CP_PAF_NV21, faceInfoList.get(0), faceFeature);
            Log.i("extractFaceFeature", "---" + code);
            if (code == ErrorInfo.MOK) {
                searchFace(faceFeature);
            }
        } else {
            Log.i("detectFaces", "检测人脸失败");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvName2.setText("");
                }
            });
        }
    }

    /**
     * 画人脸框
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
                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
                            Log.e("compareResult", "++++++++++++" + compareResult.getUserName() + "-" + compareResult.getSimilar());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvName2.setText(compareResult.getUserName() + "相似度：" + compareResult.getSimilar());
                                }
                            });

                            Intent intent=new Intent();
                            intent.putExtra("UserName",compareResult.getUserName());
                            setResult(1,intent);
                            finish();
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvName2.setText("");
                                }
                            });
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

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

}
