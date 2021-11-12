package com.feipulai.host.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.feipulai.common.db.DataBaseExecutor;
import com.feipulai.common.db.DataBaseRespon;
import com.feipulai.common.db.DataBaseTask;
import com.feipulai.common.utils.ActivityCollector;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.bean.ActivateBean;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.CommonUtils;
import com.feipulai.host.netUtils.HttpManager;
import com.feipulai.host.netUtils.HttpSubscriber;
import com.feipulai.host.netUtils.OnResultListener;
import com.feipulai.host.netUtils.netapi.UserSubscriber;
import com.orhanobut.logger.Logger;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.host.BuildConfig;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.activity.main.MainActivity;
import com.feipulai.host.tts.TtsConfig;
import com.orhanobut.logger.utils.LogUtils;
import com.ww.fpl.libarcface.common.Constants;
import com.ww.fpl.libarcface.model.FaceRegisterInfo;
import com.ww.fpl.libarcface.util.ConfigUtil;
import com.ww.fpl.libarcface.faceserver.FaceServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
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
    private SweetAlertDialog dialog;
    private ActivateBean activateBean;
    boolean isInit = false;
    private long runTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (!Build.MODEL.equals("FPL")){
            DateUtil.setTimeZone(this, "Asia/Shanghai");
        }



        activateBean = SharedPrefsUtil.loadFormSource(this, ActivateBean.class);
        runTime = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.APP_USE_TIME, 0L);
        if (runTime == 0) {
//            showActivateConfirm(1);
            activate();
        } else if (activateBean != null && activateBean.getValidRunTime() > 0) {

            if (runTime > activateBean.getValidRunTime()) {
                //超出使用时长
                //弹窗确定重新激活
                showActivateConfirm(2);
                return;
            }
//            activate();
//            gotoMain();
            init();

        } else {
            activate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void gotoMain() {
        HttpManager.DEFAULT_CONNECT_TIMEOUT = 20;
        HttpManager.DEFAULT_READ_TIMEOUT = 20;
        HttpManager.DEFAULT_WRITE_TIMEOUT = 20;
        HttpManager.resetManager();
        Intent intent = new Intent();
//                        intent.setClass(SplashScreenActivity.this, AccountActivity.class);
        intent.setClass(SplashScreenActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void activate() {

        new UserSubscriber().activate(runTime, new OnResultListener<ActivateBean>() {
            @Override
            public void onSuccess(ActivateBean result) {
                if (!Build.MODEL.equals("FPL")){
                    DateUtil.setSysDate(SplashScreenActivity.this, result.getCurrentTime());
                }
                activateBean = result;
                SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.APP_USE_TIME, result.getCurrentRunTime());
                SharedPrefsUtil.save(SplashScreenActivity.this, result);
                if ((int) result.getActivateTime() == 0) {
                    //需要确认激活
                    showActivateConfirm(1);
                } else if (result.getCurrentTime() > result.getValidEndTime()) {
//                     .logDebugMessage(result.getCurrentTime() + "-----" + result.getValidEndTime());
                    //超出使用时间 重新激活
                    showActivateConfirm(2);
                } else if (runTime > result.getValidRunTime()) {
                    //超出使用时长
                    //弹窗确定重新激活
                    showActivateConfirm(2);
                    return;
                } else {
                    //激活成功
                    init();

                }
            }

            @Override
            public void onFault(int code, String errorMsg) {
                if ((activateBean == null || runTime > activateBean.getValidRunTime()) && ActivityCollector.getInstance().isLastActivity(SplashScreenActivity.class)) {
                    toastSpeak(errorMsg);
                    //需要确认激活
                    showActivateConfirm(1);
                } else if (activateBean != null && ActivityCollector.getInstance().isLastActivity(SplashScreenActivity.class)) {
                    init();
                }

            }


        });
    }

    private void showActivateConfirm(int type) {
        if (dialog != null && dialog.isShowing()) {
            return;
        }
        dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText("激活设备")

                .setContentText((type == 1 ? "请联系管理员激活设备" : "已超出可使用时长\n请联系管理员重新激活设备") + "\n" + CommonUtils.getDeviceId(this))

                .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        dialog = null;
                        activate();
                    }
                }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        dialog = null;
                        finish();
                    }
                });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void init() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (activateBean != null && activateBean.getCurrentTime() < activateBean.getValidEndTime()) {
                    if (!ActivityCollector.getInstance().isExistActivity(MainActivity.class)) {

                        if (!isInit) {
                            isInit = true;

                            SoundPlayUtils.init(MyApplication.getInstance());

                            ToastUtils.init(getApplicationContext());
                            LogUtils.initLogger(true, BuildConfig.DEBUG, MyApplication.LOG_PATH_NAME);
                            boolean isEngine = ConfigUtil.getISEngine(SplashScreenActivity.this);
                            if (isEngine) {
                                initLocalFace();
                            } else {
                                if (SettingHelper.getSystemSetting().getCheckTool() == 4) {
                                    ToastUtils.showShort("请在参数设置激活人脸识别");
                                }
                                gotoMain();
                            }
                            //这里初始化时间很长,大约需要3s左右
                            TtsManager.getInstance().init(SplashScreenActivity.this, TtsConfig.APP_ID, TtsConfig.APP_KEY, TtsConfig.SECRET_KEY);

                        } else {
                            gotoMain();
                        }

                        isInit = true;

                    }
                } else {
                    showActivateConfirm(2);
                }

            }
        }, 1000);


    }

    private void initLocalFace() {
        //本地人脸库初始化
        boolean isFaceInit = FaceServer.getInstance().init(SplashScreenActivity.this);
        if (SettingHelper.getSystemSetting().getCheckTool() == 4) {

            DataBaseExecutor.addTask(new DataBaseTask(this, "数据加载中...", false) {
                @Override
                public DataBaseRespon executeOper() {
                    List<Student> studentList = DBManager.getInstance().queryStudentFeatures();
                    List<FaceRegisterInfo> registerInfoList = new ArrayList<>();
                    for (Student student : studentList) {
                        try {
                            byte[] faceByte = Base64.decode(student.getFaceFeature(), Base64.DEFAULT);
                            registerInfoList.add(new FaceRegisterInfo(faceByte, student.getStudentCode()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    FaceServer.getInstance().addFaceList(registerInfoList);
                    return new DataBaseRespon(true, "", null);
                }

                @Override
                public void onExecuteSuccess(DataBaseRespon respon) {
                    gotoMain();
                }

                @Override
                public void onExecuteFail(DataBaseRespon respon) {
                }
            });


        } else {
            gotoMain();
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
                            FaceServer.getInstance().init(SplashScreenActivity.this);
                            //本地人脸库初始化
                            initLocalFace();
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
//                            ToastUtils.showShort(getString(R.string.already_activated));
                        } else {
                            ToastUtils.showShort(getString(R.string.active_failed));
                            finish();
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
