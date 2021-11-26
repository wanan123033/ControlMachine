package com.feipulai.host.activity.base;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.usb.UsbDevice;
import android.view.TextureView;
import android.view.View;

import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.feipulai.common.utils.ImageUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Student;
import com.feipulai.host.entity.StudentItem;
import com.lgh.uvccamera.UVCCameraProxy;
import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PreviewCallback;
import com.orhanobut.logger.utils.LogUtils;
import com.ww.fpl.libarcface.faceserver.FaceServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class BaseCatupeFragment extends BaseFragment implements PreviewCallback {
    @BindView(R.id.textureView2)
    TextureView textureView2;
    private final int mWidth = 640;
    private final int mHeight = 480;
    private UVCCameraProxy mUVCCamera;
    private Student student;
    private String mPictureName;
    private boolean isTakePicture = false;
    private BaseAFRFragment.onAFRCompareListener compareListener;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_base_cature;
    }

    @Override
    protected void initData() {
        initUVCCamera();
    }

    private void initUVCCamera() {
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
                } else {
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

    @OnClick({R.id.tv_cature})
    public void onClick(View view) {
        isTakePicture = true;
        mPictureName = student.getStudentCode() + ".jpg";
    }

    @Override
    public void onPreviewFrame(final byte[] yuv) {
        if (isTakePicture) {
            isTakePicture = false;
            Observable.just("sss").map(new Function<String, File>() {
                @Override
                public File apply(String s) throws Exception {
                    YuvImage image = new YuvImage(yuv, ImageFormat.NV21, mWidth, mHeight, null);
                    File file = new File(getContext().getFilesDir(), mPictureName);
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        image.compressToJpeg(new Rect(0, 0, mWidth, mHeight), 100, fileOutputStream);
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return file;
                }
            })
                    .map(new Function<File, Student>() {
                        @Override
                        public Student apply(File file) throws Exception {
                            //插入数据库并导入到指定位置

                            student.setPortrait(MyApplication.PATH_IMAGE + File.separator + student.getStudentCode() + ".jpg");
                            DBManager.getInstance().insertStudent(student);
                            LogUtils.operation("考生添加" + student.toString());
                            StudentItem studentItem = new StudentItem();
                            studentItem.setStudentCode(student.getStudentCode());
                            studentItem.setItemCode(TestConfigs.getCurrentItemCode());
                            studentItem.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
                            DBManager.getInstance().insertStudentItem(studentItem);
                            doRegister(file);
                            mUVCCamera.clearCache();
                            mUVCCamera.closeCamera();
                            return student;
                        }
                    })
                    .observeOn(Schedulers.io()).subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Student>() {
                        @Override
                        public void onSubscribe(Disposable disposable) {

                        }

                        @Override
                        public void onNext(Student o) {
                            ToastUtils.showShort("图片已保存");
                            if (compareListener != null)
                                compareListener.compareStu(student);
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });


        }
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    private boolean doRegister(File jpgFile) {
        //路径获取图片。并对图片做缩小处理
        Bitmap bitmap = ImageUtil.getSmallBitmap(jpgFile.getAbsolutePath());
        if (bitmap == null) {
            return false;
        }
        Student student = DBManager.getInstance().queryStudentByStuCode(jpgFile.getName().substring(0, jpgFile.getName().indexOf(".")));
        if (student != null) {
            com.feipulai.common.utils.ImageUtil.saveBitmapToFile(MyApplication.PATH_IMAGE, student.getStudentCode() + ".jpg", bitmap);
        }
        bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
        if (bitmap == null) {
            return false;
        }

        byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            return false;
        }
        boolean success = FaceServer.getInstance().registerBgr24(getActivity(), bgr24, bitmap.getWidth(), bitmap.getHeight(),
                jpgFile.getName().substring(0, jpgFile.getName().lastIndexOf(".")));
        return success;
    }

    public void setCompareListener(BaseAFRFragment.onAFRCompareListener compareListener) {
        this.compareListener = compareListener;
    }
}
