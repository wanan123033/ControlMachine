package com.feipulai.host.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.bean.ActivateBean;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.CommonUtils;
import com.feipulai.host.netUtils.HttpSubscriber;
import com.feipulai.host.netUtils.OnResultListener;
import com.feipulai.host.netUtils.netapi.UserSubscriber;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

// 这里是否还需要延时需要再测试后再修改
        RadioManager.getInstance().init();
        DateUtil.setTimeZone(this, "Asia/Shanghai");


        activateBean = SharedPrefsUtil.loadFormSource(this, ActivateBean.class);
        long runTime = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.APP_USE_TIME, 0l);
        if (activateBean != null && activateBean.getValidRunTime() > 0) {

//            if (activateBean.getUseDeviceTime() - DateUtil.getDayTime() > activateBean.getValidRunTime()) {
//                //超出使用时长
//                //弹窗确定重新激活
//                showActivateConfirm(2);
//                return;
//            } else {
//                //更新使用时长 每一天有使用到都算使用一天
//                if (!TextUtils.equals(DateUtil.getCurrentTime("yyyy-MM-dd")
//                        , DateUtil.formatTime(activateBean.getUpdateTime(), "yyyy-MM-dd"))) {
//                    activateBean.setUseDeviceTime(activateBean.getUseDeviceTime() + DateUtil.getDayTime());
//                    activateBean.setUpdateTime(DateUtil.getCurrentTime());
//                    SharedPrefsUtil.save(this, activateBean);
//                }
//            }
            if (runTime > activateBean.getValidRunTime()) {
                //超出使用时长
                //弹窗确定重新激活
                showActivateConfirm(2);
                return;
            }
            activate();
            if (SettingHelper.getSystemSetting().getCheckTool() != 4) {
                gotoMain();
            }


        } else {
            activate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 这里是否还需要延时需要再测试后再修改
//        DateUtil.setTimeZone(this,"Asia/Shangha");
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                init();
//                if (SettingHelper.getSystemSetting().getCheckTool() != 4) {
//                    Intent intent = new Intent();
//                    intent.setClass(SplashScreenActivity.this, MainActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                    finish();
//                }
//
//            }
//        }, 1000);


    }

    private void gotoMain() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (activateBean != null && activateBean.getCurrentTime() < activateBean.getValidEndTime()) {
                    if (!ActivityCollector.getInstance().isExistActivity(MainActivity.class)) {
                        if (!isInit) {
                            init();
                        }

                        Intent intent = new Intent();
                        intent.setClass(SplashScreenActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    showActivateConfirm(2);
                }

            }
        }, 3000);
    }

    private void activate() {
        long currentRunTime = 0;
        if (activateBean != null) {
            //超出使用时间 重新激活
            currentRunTime = activateBean.getUseDeviceTime();
        }
        new UserSubscriber().activate(currentRunTime, new OnResultListener<ActivateBean>() {
            @Override
            public void onSuccess(ActivateBean result) {
                activateBean = result;
                SharedPrefsUtil.save(SplashScreenActivity.this, result);
                if ((int) result.getActivateTime() == 0) {
                    //需要确认激活
                    showActivateConfirm(1);
                } else if (result.getCurrentTime() > result.getValidEndTime()) {
                    //超出使用时间 重新激活
                    showActivateConfirm(2);
                } else {
                    //激活成功
                    gotoMain();

                }
            }

            @Override
            public void onFault(int code, String errorMsg) {
                if (activateBean == null && ActivityCollector.getInstance().isLastActivity(SplashScreenActivity.class)) {
                    toastSpeak(errorMsg);
                    //需要确认激活
                    showActivateConfirm(1);
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
        isInit = true;
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
                    if (dialog != null && !dialog.isShowing() && activateBean != null) {
//                        Intent intent = new Intent();
//                        intent.setClass(SplashScreenActivity.this, MainActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//                        finish();
                        gotoMain();
                    }
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
                        if (dialog != null && !dialog.isShowing() && activateBean != null) {
                            gotoMain();
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
