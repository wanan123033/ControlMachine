package com.feipulai.exam.activity.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.feipulai.common.db.DataBaseExecutor;
import com.feipulai.common.db.DataBaseRespon;
import com.feipulai.common.db.DataBaseTask;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.HandlerUtil;
import com.feipulai.common.utils.ImageUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MainActivity;
import com.feipulai.exam.activity.SplashScreenActivity;
import com.feipulai.exam.activity.base.BaseAFRFragment;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.data.DataManageActivity;
import com.feipulai.exam.activity.data.DownLoadPhotoHeaders;
import com.feipulai.exam.activity.setting.SettingActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.bean.UserBean;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Account;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentFace;
import com.feipulai.exam.netUtils.CommonUtils;
import com.feipulai.exam.netUtils.HttpManager;
import com.feipulai.exam.netUtils.OnResultListener;
import com.feipulai.exam.netUtils.download.DownService;
import com.feipulai.exam.netUtils.download.DownloadHelper;
import com.feipulai.exam.netUtils.download.DownloadListener;
import com.feipulai.exam.netUtils.download.DownloadUtils;
import com.feipulai.exam.netUtils.netapi.HttpSubscriber;
import com.feipulai.exam.view.OperateProgressBar;
import com.orhanobut.logger.utils.LogUtils;
import com.ww.fpl.libarcface.common.Constants;
import com.ww.fpl.libarcface.faceserver.FaceServer;
import com.ww.fpl.libarcface.model.FaceRegisterInfo;
import com.ww.fpl.libarcface.util.ConfigUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Headers;

/**
 * 登录
 * Created by zzs on  2018/12/29
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class LoginActivity extends BaseTitleActivity implements AccountAFRFragment.onAFRCompareListener {
    @BindView(R.id.frame_camera)
    protected FrameLayout afrFrameLayout;
    protected AccountAFRFragment afrFragment;
    @BindView(R.id.edit_account)
    EditText editAccount;
    @BindView(R.id.edit_pass)
    EditText editPass;
    @BindView(R.id.rgSelect)
    RadioGroup rgSelect;
    @BindView(R.id.llLogin)
    LinearLayout llLogin;
    private Account loginAccount;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_login;
    }

    @Override
    protected void initData() {
        afrFragment = new AccountAFRFragment();
        afrFragment.setCompareListener(this);
        initAFR();
        editAccount.setText(SettingHelper.getSystemSetting().getUserName());

        rgSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbAccount) {
                    afrFrameLayout.setVisibility(View.GONE);
                    llLogin.setVisibility(View.VISIBLE);
                    afrFragment.gotoUVCFaceCamera(false);
                } else {
                    afrFrameLayout.setVisibility(View.VISIBLE);
                    llLogin.setVisibility(View.GONE);
                    afrFragment.gotoUVCFaceCamera(true);
                }
            }
        });
        initFace();
    }

    private void initAFR() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_camera, afrFragment);
        transaction.commitAllowingStateLoss();// 提交更改
    }

    @Override
    public void compareStu(Account account) {
        if (account == null) {
            toastSpeak("查无此人");
        }
        loginAccount = account;
        login(1, account.getAccount(), account.getPassword());

    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.GOTO_LOGIN) {
            rgSelect.check(R.id.rbAccount);
        }
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("登录").addRightText("人脸激活", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeFace();
            }
        });
    }

    /**
     * 人脸激活
     */
    private void activeFace() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                int activeCode = FaceEngine.activeOnline(LoginActivity.this, Constants.APP_ID, Constants.SDK_KEY);
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
                            ConfigUtil.setISEngine(LoginActivity.this, true);
                            initFace();
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            ToastUtils.showShort(getString(R.string.already_activated));
                        } else {
                            ToastUtils.showShort(getString(R.string.active_failed));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.showShort("人脸识别激活失败");
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    @OnClick(R.id.btn_login)
    public void onViewClicked() {
        if (isCheckData()) {
            login(0, editAccount.getText().toString(), editPass.getText().toString());
        }
    }

    private boolean isCheckData() {
        if (TextUtils.isEmpty(editAccount.getText().toString())) {
            ToastUtils.showShort("请输入用户名");
            return false;
        }
        if (TextUtils.isEmpty(editPass.getText().toString())) {
            ToastUtils.showShort("请输入密码");
            return false;
        }
        return true;
    }

    /**
     * 用户登录
     *
     * @param loginType 0账号登录  1人脸后账号登录
     */
    private void login(final int loginType, final String accountName, final String pwd) {
        new HttpSubscriber().login(this, accountName, pwd, new OnResultListener<UserBean>() {
            @Override
            public void onResponseTime(String responseTime) {
                if (!TextUtils.isEmpty(responseTime)) {
                    DateUtil.setSysDate(LoginActivity.this, Long.valueOf(responseTime));
                }

            }

            @Override
            public void onSuccess(UserBean userBean) {
                MyApplication.TOKEN = userBean.getToken();
                SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.TOKEN, userBean.getToken());


                if (!TextUtils.isEmpty(userBean.getExamName())) {
                    SettingHelper.getSystemSetting().setTestName(userBean.getExamName());
                }
                SettingHelper.getSystemSetting().setUserName(editAccount.getText().toString());
                SettingHelper.getSystemSetting().setSitCode(userBean.getSiteId());
                SettingHelper.updateSettingCache(SettingHelper.getSystemSetting());
                if (loginType == 1) {

                    if (TextUtils.equals(loginAccount.getExamPersonnelId(), userBean.getExamPersonnelId())) {
                        getItemAll();
                        return;
                    } else {
                        DBManager.getInstance().deleteAccount(loginAccount);
                        Account account = new Account(userBean);
                        account.setAccount(accountName);
                        account.setPassword(pwd);
                        DBManager.getInstance().insterAccount(account);
                        toastSpeak("账号配对失败，请重新识别");
                        getFace(userBean);
                        return;
                    }
                } else {
                    Account dbAccount = DBManager.getInstance().queryAccountByExamPersonnelId(userBean.getExamPersonnelId());
                    if (dbAccount != null) {
                        dbAccount.setAccount(accountName);
                        dbAccount.setPassword(pwd);
                        DBManager.getInstance().updateAccount(dbAccount);
                        loginAccount = dbAccount;
                    } else {
                        Account account = new Account(userBean);
                        account.setAccount(accountName);
                        account.setPassword(pwd);
                        DBManager.getInstance().insterAccount(account);
                        loginAccount = account;
                    }
                }
                getFace(userBean);


            }

            @Override
            public void onFault(int code, String errorMsg) {
                ToastUtils.showShort(errorMsg);
            }
        });
    }

    private void gotoMain() {
        HttpManager.DEFAULT_CONNECT_TIMEOUT = 20;
        HttpManager.DEFAULT_READ_TIMEOUT = 20;
        HttpManager.DEFAULT_WRITE_TIMEOUT = 20;
        HttpManager.resetManager();

        Intent intent = new Intent();
//                        intent.setClass(SplashScreenActivity.this, AccountActivity.class);
        intent.setClass(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void getFace(UserBean userBean) {
        if (userBean.getExamPersonnelPhotoType() == 1) {
            downPhoto(userBean);
        } else {
            if (TextUtils.isEmpty(userBean.getExamPersonnelPhotoData())) {
                ToastUtils.showShort("未提交头像信息，请联系管理员");
            } else {
                ImageUtil.saveBitmapToFile(MyApplication.PATH_IMAGE, userBean.getFileName(), ImageUtil.base64ToBitmap(userBean.getExamPersonnelPhotoData()));
                doRegister(MyApplication.PATH_IMAGE + userBean.getFileName());
            }
        }
    }

    /**
     * 下载头像
     *
     * @param userBean
     */
    private void downPhoto(UserBean userBean) {
        new DownloadUtils().downloadFile(userBean.getExamPersonnelPhotoData(), userBean.getFileName(), new DownloadListener() {
            @Override
            public void onStart(String fileName) {

            }

            @Override
            public void onResponse(Headers headers) {

            }

            @Override
            public void onProgress(String fileName, int progress) {

            }

            @Override
            public void onFinish(String fileName) {
                try {
                    File srcFile = new File(MyApplication.DOWNLOAD_PATH + fileName);
                    if (!srcFile.exists()) {
                        ToastUtils.showShort("头像信息异常，请联系管理员");
                    }
                    File file = new File(MyApplication.PATH_IMAGE + fileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    //拷贝下载文件
                    FileUtil.copyFile(srcFile, file);
                    doRegister(MyApplication.PATH_IMAGE + fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String fileName, String errorInfo) {

                ToastUtils.showShort("头像信息下载失败，请联系管理员");

            }
        });


    }

    private void doRegister(String filePath) {

        File jpgFile = new File(filePath);
        Bitmap bitmap = ImageUtil.getSmallBitmap(jpgFile.getAbsolutePath());
        if (bitmap == null) {
            ToastUtils.showShort("人脸信息生成失败，请联系管理员");
            return;
        }
        bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
        if (bitmap == null) {
            ToastUtils.showShort("人脸信息生成失败，请联系管理员");
            return;
        }

        byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            ToastUtils.showShort("人脸信息生成失败，请联系管理员");
            return;
        }
        String examPersonnelId = jpgFile.getName().substring(0, jpgFile.getName().lastIndexOf("."));
        byte[] success = FaceServer.getInstance().registerBgr24Byte(LoginActivity.this, bgr24, bitmap.getWidth(), bitmap.getHeight(),
                examPersonnelId);
        if (success != null) {
//                        student.setFaceFeature(Base64.encodeToString(success, Base64.DEFAULT));
            //                        DBManager.getInstance().updateStudent(student);
            loginAccount.setFaceFeature(Base64.encodeToString(success, Base64.DEFAULT));
            DBManager.getInstance().updateAccount(loginAccount);
            List<FaceRegisterInfo> faceList = new ArrayList<FaceRegisterInfo>();
            faceList.add(new FaceRegisterInfo(success, examPersonnelId));
            FaceServer.getInstance().addFaceList(faceList);
            rgSelect.check(R.id.rbAfr);
        }
    }

    /**
     * 初始化登录头像
     */
    private void initFace() {
        DataBaseExecutor.addTask(new DataBaseTask() {
            @Override
            public DataBaseRespon executeOper() {

                List<Account> accountList = DBManager.getInstance().getAccountFeatures();
                List<FaceRegisterInfo> registerInfoList = new ArrayList<>();
                for (Account account : accountList) {
                    try {
                        byte[] faceByte = Base64.decode(account.getFaceFeature(), Base64.DEFAULT);
                        registerInfoList.add(new FaceRegisterInfo(faceByte, account.getExamPersonnelId()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                FaceServer.getInstance().addFaceList(registerInfoList);
                return new DataBaseRespon(true, "", null);
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {

            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });
    }

    private void getItemAll() {
        DBManager.getInstance().deleteAllItems();
        DBManager.getInstance().initDB();
        HttpSubscriber http = new HttpSubscriber();
        http.getItemAll(this);
        http.setOnRequestEndListener(new HttpSubscriber.OnRequestEndListener() {
            @Override
            public void onSuccess(int bizType) {
//                List<Item> items = DBManager.getInstance().queryItemNotNullItemCode();
                gotoMain();
            }

            @Override
            public void onFault(int bizType) {
                toastSpeak("登录失败");
            }

            @Override
            public void onRequestData(Object data) {

            }
        });
    }
}
