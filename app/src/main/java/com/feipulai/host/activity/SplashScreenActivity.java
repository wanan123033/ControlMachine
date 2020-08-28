package com.feipulai.host.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.feipulai.common.db.DataBaseExecutor;
import com.feipulai.common.db.DataBaseRespon;
import com.feipulai.common.db.DataBaseTask;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Student;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.host.BuildConfig;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.activity.main.MainActivity;
import com.feipulai.host.tts.TtsConfig;
import com.ww.fpl.libarcface.common.Constants;
import com.ww.fpl.libarcface.model.FaceRegisterInfo;
import com.ww.fpl.libarcface.util.ConfigUtil;
import com.ww.fpl.libarcface.faceserver.FaceServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 现在这完全是一个程序欢迎界面了
 */
public class SplashScreenActivity extends BaseActivity {

    public static final String MACHINE_CODE = "machine_code";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


    }

    @Override
    protected void onResume() {
        super.onResume();
        // 这里是否还需要延时需要再测试后再修改
//        DateUtil.setTimeZone(this,"Asia/Shangha");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                init();
                if (SettingHelper.getSystemSetting().getCheckTool() != 4) {
                    Intent intent = new Intent();
                    intent.setClass(SplashScreenActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }

            }
        }, 1000);


    }

    private void init() {
        boolean isEngine = ConfigUtil.getISEngine(this);
        if (isEngine) {
            initLocalFace();
        } else {
            activeEngine();
        }
        SoundPlayUtils.init(MyApplication.getInstance());
        LogUtils.initLogger(BuildConfig.DEBUG, BuildConfig.DEBUG, MyApplication.LOG_PATH_NAME);
        ToastUtils.init(getApplicationContext());
        //这里初始化时间很长,大约需要3s左右
        TtsManager.getInstance().init(this, TtsConfig.APP_ID, TtsConfig.APP_KEY, TtsConfig.SECRET_KEY);


    }

    private void initLocalFace() {
        //本地人脸库初始化
        boolean isFaceInit = FaceServer.getInstance().init(SplashScreenActivity.this);
        Logger.d("initLocalFace====>" + isFaceInit);
        if (SettingHelper.getSystemSetting().getCheckTool() == 4) {

            DataBaseExecutor.addTask(new DataBaseTask(this, "数据加载中...", true) {
                @Override
                public DataBaseRespon executeOper() {
                    List<Student> studentList = DBManager.getInstance().queryStudentFeatures();
                    Log.i("faceRegisterInfoList", "->" + studentList.size());
                    List<FaceRegisterInfo> registerInfoList = new ArrayList<>();
                    for (Student student : studentList) {
                        registerInfoList.add(new FaceRegisterInfo(Base64.decode(student.getFaceFeature(), Base64.DEFAULT), student.getStudentCode()));
                    }
                    FaceServer.getInstance().addFaceList(registerInfoList);
                    return new DataBaseRespon(true, "", null);
                }

                @Override
                public void onExecuteSuccess(DataBaseRespon respon) {
                    Intent intent = new Intent();
                    intent.setClass(SplashScreenActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onExecuteFail(DataBaseRespon respon) {

                }
            });


        }
    }

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    /**
     * 激活人脸识别引擎
     */
    public void activeEngine() {
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
            return;
        }
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                int activeCode = FaceEngine.activeOnline(SplashScreenActivity.this, Constants.APP_ID, Constants.SDK_KEY);
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            ToastUtils.showShort(getString(R.string.active_success));

                            ConfigUtil.setISEngine(SplashScreenActivity.this, true);
                            //本地人脸库初始化
                            initLocalFace();
                            return;
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
//                            ToastUtils.showShort(getString(R.string.already_activated));
                        } else {
                            ToastUtils.showShort(getString(R.string.active_failed));
                        }
                        Intent intent = new Intent();
                        intent.setClass(SplashScreenActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
//                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
//                        int res = FaceEngine.getActiveFileInfo(SplashScreenActivity.this, activeFileInfo);
//                        if (res == ErrorInfo.MOK) {
//                            Log.i("SplashScreenActivity", activeFileInfo.toString());
//                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                activeEngine();
            } else {
                ToastUtils.showShort(getString(R.string.permission_denied));
            }
        }
    }
}
