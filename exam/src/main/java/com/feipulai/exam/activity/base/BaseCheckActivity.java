package com.feipulai.exam.activity.base;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.HandlerUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.ScannerGunManager;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.dialog.DialogUtils;
import com.feipulai.device.CheckDeviceOpener;
import com.feipulai.device.ic.ICCardDealer;
import com.feipulai.device.ic.NFCDevice;
import com.feipulai.device.ic.entity.StuInfo;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.fragment.IndividualCheckFragment;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.AdvancedSettingActivity;
import com.feipulai.exam.activity.setting.SettingActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.RoundScoreBean;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.entity.StudentThermometer;
import com.feipulai.exam.netUtils.CommonUtils;
import com.feipulai.exam.netUtils.OnResultListener;
import com.feipulai.exam.netUtils.netapi.HttpSubscriber;
import com.feipulai.exam.utils.StringChineseUtil;
import com.feipulai.exam.utils.bluetooth.BlueBindBean;
import com.feipulai.exam.utils.bluetooth.BlueToothHelper;
import com.feipulai.exam.utils.bluetooth.BlueToothListActivity;
import com.feipulai.exam.utils.bluetooth.ClientManager;
import com.feipulai.exam.view.AddStudentDialog;
import com.feipulai.exam.view.OperateProgressBar;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;

/**
 * Created by James on 2018/5/24 0024.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public abstract class BaseCheckActivity
        extends BaseTitleActivity
        implements CheckDeviceOpener.OnCheckDeviceArrived, BaseAFRFragment.onAFRCompareListener, OnResultListener<RoundScoreBean>, IndividualCheckFragment.OnIndividualCheckInListener, ResitDialog.onClickQuitListener {


    public MyHandler mHandler = new MyHandler(this);
    private boolean isOpenDevice = true;
    public static final int STUDENT_CODE = 0x0;
    public static final int ID_CARD_NO = 0x1;
    public static final int CHECK_IN = 0x0;
    public static final int CHECK_THERMOMETER = 0x1;
    private Student mStudent;
    protected StudentItem mStudentItem;
    private List<RoundResult> mResults;

    protected FrameLayout afrFrameLayout;
    protected BaseAFRFragment afrFragment;
    private BlueBindBean blueBindBean;
    protected volatile boolean isStartThermometer = false;
    protected SweetAlertDialog thermometerDialog;
    private SweetAlertDialog thermometerOpenDialog;

    public void setOpenDevice(boolean openDevice) {
        isOpenDevice = openDevice;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initViews() {
        super.initViews();
        if (SettingHelper.getSystemSetting().getCheckTool() == 4 && setAFRFrameLayoutResID() != 0) {
            afrFrameLayout = findViewById(setAFRFrameLayoutResID());
            afrFragment = new BaseAFRFragment();
            afrFragment.setCompareListener(this);
            initAFR();
        }
        ScannerGunManager.getInstance().setScanListener(new ScannerGunManager.OnScanListener() {
            @Override
            public void onResult(String code) {
                LogUtils.operation("扫描结果：" + code);
                boolean needAdd = checkQulification(code, STUDENT_CODE);
                if (needAdd) {
                    Student student = new Student();
                    student.setStudentCode(code);
                    showAddHint(student);
                }
            }
        });

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (ScannerGunManager.getInstance().dispatchKeyEvent(event.getKeyCode(), event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected int setLayoutResID() {
        return 0;
    }

    //    public abstract int setAFRFrameLayoutResID();
    public int setAFRFrameLayoutResID() {
        return 0;
    }


    @Override
    protected void onResume() {
        if (isOpenDevice) {
            CheckDeviceOpener.getInstance().setQrLength(SettingHelper.getSystemSetting().getQrLength());
            CheckDeviceOpener.getInstance().setOnCheckDeviceArrived(this);
            int checkTool = SettingHelper.getSystemSetting().getCheckTool();
            CheckDeviceOpener.getInstance().open(this, checkTool == SystemSetting.CHECK_TOOL_IDCARD,
                    checkTool == SystemSetting.CHECK_TOOL_ICCARD,
                    checkTool == SystemSetting.CHECK_TOOL_QR);
        }
        if (SettingHelper.getSystemSetting().isStartThermometer()) {
            blueBindBean = BlueToothHelper.getBlueBind();
            LogUtil.logDebugMessage("蓝牙连接信息===》" + blueBindBean.toString());
            if (!TextUtils.isEmpty(blueBindBean.getBluetoothMac())) {
                ClientManager.connectDevice(blueBindBean.getBluetoothMac(), bleConnectResponse);
                ClientManager.getClient().registerConnectStatusListener(blueBindBean.getBluetoothMac(), mConnectStatusListener);
            } else {
                //提示未连接蓝牙体温计
                showThermometerOpenDialog();
            }

        }
        super.onResume();
    }

    private final BleConnectResponse bleConnectResponse = new BleConnectResponse() {
        @Override
        public void onResponse(int code, BleGattProfile bleGattProfile) {
            if (code == REQUEST_SUCCESS) {
                //设置读取
                ClientManager.getGattProfile(bleGattProfile);
                openBlueThermometerRead();
                if (thermometerOpenDialog != null && thermometerOpenDialog.isShowing()) {
                    thermometerOpenDialog.dismissWithAnimation();
                }
            } else {
                LogUtil.logDebugMessage("蓝牙连接断开");
                //提示打开体温计或去连接设备
                showThermometerOpenDialog();
            }
        }
    };

    //蓝牙连接状态
    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status != STATUS_CONNECTED && !TextUtils.isEmpty(blueBindBean.getBluetoothMac())) {
                LogUtil.logDebugMessage("蓝牙连接状态断开");
                ClientManager.connectDevice(blueBindBean.getBluetoothMac(), bleConnectResponse);
            }

        }
    };
    private BleNotifyResponse mNotifyRsp = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
            HandlerUtil.sendMessage(mHandler, CHECK_THERMOMETER, value);
        }

        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                LogUtil.logDebugMessage("蓝牙体温计读取连接成功");
                //"success");
            } else {
                LogUtil.logDebugMessage("蓝牙体温计读取连接失败");
                //"failed");
            }
        }
    };

    private void openBlueThermometerRead() {

        ClientManager.getClient().notify(blueBindBean.getBluetoothMac(), UUID.fromString(blueBindBean.getServerUUID())
                , UUID.fromString(blueBindBean.getCharacterUUID()), mNotifyRsp);
    }

    private void initAFR() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(setAFRFrameLayoutResID(), afrFragment);
        transaction.commitAllowingStateLoss();// 提交更改
    }

    public void showAFR() {
        if (SettingHelper.getSystemSetting().getCheckTool() != 4) {
            ToastUtils.showShort("未选择人脸识别检录功能");
            return;
        }
        if (afrFrameLayout == null) {
            return;
        }

        boolean isGoto = afrFragment.gotoUVCFaceCamera(!afrFragment.isOpenCamera);
        if (isGoto) {
            if (afrFragment.isOpenCamera) {
                afrFrameLayout.setVisibility(View.VISIBLE);
            } else {
                afrFrameLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isOpenDevice) {
            CheckDeviceOpener.getInstance().close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isOpenDevice) {
            CheckDeviceOpener.getInstance().destroy();
        }
        if (SettingHelper.getSystemSetting().isStartThermometer()) {
            if (!TextUtils.isEmpty(blueBindBean.getBluetoothMac())) {
                ClientManager.getClient().disconnect(blueBindBean.getBluetoothMac());
                ClientManager.getClient().unregisterConnectStatusListener(blueBindBean.getBluetoothMac(), mConnectStatusListener);
            }
        }
    }

    @Override
    public void onICCardFound(NFCDevice nfcd) {
        long startTime = System.currentTimeMillis();
        ICCardDealer icCardDealer = new ICCardDealer(nfcd);
        StuInfo stuInfo = icCardDealer.IC_ReadStuInfo();
        if (stuInfo != null) {
            Logger.i("iccard readInfo:" + stuInfo.toString());
        }

        if (stuInfo == null || TextUtils.isEmpty(stuInfo.getStuCode())) {
//            TtsManager.getInstance().speak("读卡(ka3)失败");
//            InteractUtils.toast(this, "读卡失败");
            toastSpeak("空卡");
            return;
        }

        Logger.i("处理IC卡时间:" + (System.currentTimeMillis() - startTime) + "ms");
        Logger.i("iccard readInfo:" + stuInfo.toString());
        boolean needAdd = checkQulification(stuInfo.getStuCode(), STUDENT_CODE);
        if (needAdd) {
            Student student = new Student();
            student.setStudentCode(stuInfo.getStuCode());
            student.setStudentName(stuInfo.getStuName());
            student.setSex(stuInfo.getSex());
            showAddHint(student);
        }
    }

    @Override
    public void onIdCardRead(IDCardInfo idCardInfo) {
        boolean needAdd = checkQulification(idCardInfo.getId(), ID_CARD_NO);
        if (needAdd) {
            Student student = new Student();
            student.setStudentName(idCardInfo.getName());
            student.setSex(idCardInfo.getSex().contains("男") ? Student.MALE : Student.FEMALE);
            student.setIdCardNo(idCardInfo.getId());
            showAddHint(student);
        }
    }

    @Override
    public void onQrArrived(String qrCode) {
        boolean needAdd = checkQulification(qrCode, STUDENT_CODE);
        if (needAdd) {
            Student student = new Student();
            student.setStudentCode(qrCode);
            showAddHint(student);
        }
    }

    @Override
    public void onQRWrongLength(int length, int expectLength) {
        InteractUtils.toast(this, "条码与当前设置位数不一致,请重扫条码");
    }

    // 可能在子线程运行
    // 返回是否需要新增考生
    public boolean checkQulification(String code, int flag) {
        Student student = null;
        boolean canTemporaryAdd = SettingHelper.getSystemSetting().isTemporaryAddStu();
        switch (flag) {

            case ID_CARD_NO:
                student = DBManager.getInstance().queryStudentByIDCode(code);
                break;

            case STUDENT_CODE:
                student = DBManager.getInstance().queryStudentByStuCode(code);
                break;

        }
        if (student == null) {
            // Log.i("james",canTemporaryAdd + "");
            if (!canTemporaryAdd) {
                InteractUtils.toastSpeak(this, "该考生不存在");
            }
            return canTemporaryAdd;
        }
        final StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        if (studentItem == null) {
            InteractUtils.toastSpeak(this, "无此项目");
            return false;
        }
        final List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
        if (results != null && results.size() >= TestConfigs.getMaxTestCount()) {
            showDialog(student, studentItem, results);

            return false;
        } else {
            int fullSkip[] = TestConfigs.getFullSkip();
            if (fullSkip != null) {
                for (RoundResult result : results) {
//                    if (student.getSex() == 0 && result.getResult() >= TestConfigs.getFullSkip()[0]) {//男子满分跳过
//                        showDialog(student, studentItem, results);
//                        return false;
//                    } else if (student.getSex() == 1 && result.getResult() >= TestConfigs.getFullSkip()[1]) {//女子满分跳过
//                        showDialog(student, studentItem, results);
//                        return false;
//                    }
                    if (student.getSex() == 0 && TestConfigs.getFullSkip() != null) {//男子满分跳过
                     if (machineCode == ItemDefault.CODE_ZQYQ || machineCode == ItemDefault.CODE_LQYQ) {
                            if (result.getResult() <= TestConfigs.getFullSkip()[0]) {
                                showDialog(student, studentItem, results);
                                return false;
                            }
                        } else {
                            if (result.getResult() >= TestConfigs.getFullSkip()[0]) {
                                showDialog(student, studentItem, results);
                                return false;
                            }
                        }


                    }else if (student.getSex() == 1 && TestConfigs.getFullSkip() != null  ) {//女子满分跳过

                        if (machineCode == ItemDefault.CODE_ZQYQ || machineCode == ItemDefault.CODE_LQYQ) {
                            if (result.getResult() <= TestConfigs.getFullSkip()[1]) {
                                showDialog(student, studentItem, results);
                                return false;
                            }
                        } else {
                            if (result.getResult() >= TestConfigs.getFullSkip()[1]) {
                                showDialog(student, studentItem, results);
                                return false;
                            }
                        }
                    }
                }


            }

        }
        mStudent = student;
        mStudentItem = studentItem;
        mResults = results;
        // 可以直接检录
        //TODO 考虑单机测试的开关是否开启

        checkInUIThread(student, studentItem);
        return false;
    }

    private void showDialog(final Student student, final StudentItem studentItem, final List<RoundResult> results) {
        SystemSetting setting = SettingHelper.getSystemSetting();
        if (setting.isAgainTest() && setting.isResit()) {
            new SweetAlertDialog(this).setContentText("需要重测还是补考呢?")
                    .setCancelText("重测")
                    .setConfirmText("补考")
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            AgainTestDialog dialog = new AgainTestDialog();
                            dialog.setArguments(student, results, studentItem);
                            dialog.setOnIndividualCheckInListener(BaseCheckActivity.this);
                            dialog.show(getSupportFragmentManager(), "AgainTestDialog");
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    })
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            ResitDialog dialog = new ResitDialog();
                            dialog.setArguments(student, results, studentItem);
                            dialog.setOnIndividualCheckInListener(BaseCheckActivity.this);
                            dialog.show(getSupportFragmentManager(), "ResitDialog");
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    }).show();
        }
        if (setting.isAgainTest()) {
            AgainTestDialog dialog = new AgainTestDialog();
            dialog.setArguments(student, results, studentItem);
            dialog.setOnIndividualCheckInListener(this);
            dialog.show(getSupportFragmentManager(), "AgainTestDialog");

        }
        if (setting.isResit()) {
            ResitDialog dialog = new ResitDialog();
            dialog.setArguments(student, results, studentItem);
            dialog.setOnIndividualCheckInListener(this);
            dialog.show(getSupportFragmentManager(), "ResitDialog");
        } else {
            InteractUtils.toastSpeak(this, "该考生已测试");
        }
    }

    @Override
    public void compareStu(final Student student) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (student != null)
                    afrFrameLayout.setVisibility(View.GONE);
            }
        });

//        if (student == null) {
//            InteractUtils.toastSpeak(this, "该考生不存在");
//            return;
//        }
//        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
//        if (studentItem == null) {
//            InteractUtils.toastSpeak(this, "无此项目");
//            return;
//        }
//        List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
////        if (results != null && results.size() >= TestConfigs.getMaxTestCount(this)) {
////            InteractUtils.toastSpeak(this, "该考生已测试");
////            //TODO  考虑重测
////
////            return;
////        }
//        mStudent = student;
//        mStudentItem = studentItem;
//        mResults = results;
//        // 可以直接检录
//        checkInUIThread(student, studentItem);
        //TODO  考虑重测再次调用检入检索
        checkQulification(student.getStudentCode(), STUDENT_CODE);
    }

    protected void checkInUIThread(Student student, StudentItem studentItem) {
        SystemSetting setting = SettingHelper.getSystemSetting();
        if (setting.isAutoScore()) {
            HttpSubscriber subscriber = new HttpSubscriber();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    OperateProgressBar.showLoadingUi(BaseCheckActivity.this, "正在获取云端成绩...");
                }
            });
            subscriber.getRoundResult(setting.getSitCode(), studentItem.getScheduleNo(), TestConfigs.getCurrentItemCode(), student.getStudentCode(),
                    null, null, null, String.valueOf(studentItem.getExamType()), this);
        } else {
            sendCheckHandlerMessage(student);
        }

    }


    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.TEMPORARY_ADD_STU) {
            mStudent = (Student) baseEvent.getData();
            mStudentItem = DBManager.getInstance().queryStuItemByStuCode(mStudent.getStudentCode());
            if (SettingHelper.getSystemSetting().isStartThermometer()) {
                showThermometerDialog();
            } else {
                LogUtils.operation("检入考生：" + mStudent.toString());
                onCheckIn(mStudent);
            }

        }
    }

    /**
     * 真正的有考生成功的检录进来时调用,这里不需要再验证考生信息了
     * 该方法的调用就表示了这个人可以测试了
     */
    public abstract void onCheckIn(Student student);


    public void checkInput(Student student) {
        if (student == null) {
            toastSpeak("该考生不存在");
        } else {
            StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
            if (studentItem != null) {
                mStudent = student;
                mStudentItem = studentItem;
                if (SettingHelper.getSystemSetting().isStartThermometer()) {
                    //是否已保存测量体温
                    StudentThermometer thermometer = DBManager.getInstance().getThermometer(studentItem);
                    if (thermometer == null) {
                        showThermometerDialog();
                    } else {
                        checkQulification(mStudent.getStudentCode(), STUDENT_CODE);
//                        checkInUIThread(mStudent, mStudentItem);
                    }

                } else {
                    checkQulification(mStudent.getStudentCode(), STUDENT_CODE);
//                    checkInUIThread(mStudent, mStudentItem);
                }
            } else {
                toastSpeak("无此项目");
            }
        }
    }

    @Override
    public void onSuccess(RoundScoreBean result) {
        OperateProgressBar.removeLoadingUiIfExist(this);
        if (result.getExist() == 1) {
            boolean flag = false;
            List<RoundScoreBean.ScoreBean> roundList = result.getRoundList();
            for (RoundScoreBean.ScoreBean scoreBean : roundList) {
                if (!scoreBean.mtEquipment.equals(CommonUtils.getDeviceInfo())) {
                    flag = true;
                    break;
                } else {
                    flag = false;
                }
            }
            if (flag) {
                new AlertDialog.Builder(this)
                        .setTitle("温馨提示")
                        .setMessage("该学生已在其他设备上测试,确认测试吗?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showAdvancedPwdDialog();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
            } else {
                sendCheckHandlerMessage(mStudent);
            }
        } else {
            sendCheckHandlerMessage(mStudent);
        }
    }

    @Override
    public void onResponseTime(String responseTime) {

    }

    public void sendCheckHandlerMessage(Student mStudent) {
        Message msg = Message.obtain();
        msg.what = CHECK_IN;
        msg.obj = mStudent;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onFault(int code, String errorMsg) {
        OperateProgressBar.removeLoadingUiIfExist(this);
        ToastUtils.showLong(errorMsg);
//        sendCheckHandlerMessage(mStudent);
    }

    @Override
    public void onIndividualCheckIn(Student student, StudentItem studentItem, List<RoundResult> results) {
        onCheckIn(student);
    }


    @Override
    public void onCancel() {

    }

    @Override
    public void onCommitPattern(Student student, StudentItem studentItem, List<RoundResult> results, int roundNo) {
        onIndividualCheckIn(student, studentItem, results);
        setRoundNo(student, roundNo);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case CHECK_IN:
                if (SettingHelper.getSystemSetting().isStartThermometer()) {
                    StudentThermometer thermometer = DBManager.getInstance().getThermometer(mStudentItem);
                    if (thermometer == null) {
                        showThermometerDialog();
                    } else {
                        if (mStudent != null) {
                            LogUtils.operation("检入考生：" + mStudent.toString());
                            onCheckIn(mStudent);
                        }
                    }

                } else {
                    if (mStudent != null) {
                        LogUtils.operation("检入考生：" + mStudent.toString());
                        onCheckIn(mStudent);
                    }

                }

                break;
            case CHECK_THERMOMETER:

                byte[] value = (byte[]) msg.obj;
                LogUtil.logDebugMessage("蓝牙返回数据===》" + isStartThermometer);
                if (isStartThermometer == true) {
                    LogUtil.logDebugMessage("蓝牙返回数据校验===》" + StringChineseUtil.byteToString(value));
                    if (value.length < 3) {
                        //|| value[1] + value[2] != value[3]
                        toastSpeak("体温枪异常，请再次测量体温");
                        return;
                    }

                    String getThermometer = Long.parseLong(String.format("%02X", value[1]) + String.format("%02X", value[2]), 16) + "";
                    if (getThermometer.length() < 3) {
                        toastSpeak("请规范使用体温枪重新测量");
                        return;
                    }
                    String thermometer = getThermometer.substring(0, 2) + "." + getThermometer.substring(2);
                    LogUtil.logDebugMessage("蓝牙返回数据===》" + thermometer);
                    isStartThermometer = false;
                    String contentText = mStudent.getStudentName() + ":" + thermometer + "℃";
                    //添加体温记录
                    StudentThermometer studentThermometer = new StudentThermometer();
                    studentThermometer.setStudentCode(mStudent.getStudentCode());
                    studentThermometer.setExamType(mStudentItem.getExamType());
                    studentThermometer.setThermometer(Double.valueOf(thermometer));
                    studentThermometer.setItemCode(TestConfigs.getCurrentItemCode());
                    studentThermometer.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
                    studentThermometer.setMeasureTime(DateUtil.getCurrentTime() + "");
                    DBManager.getInstance().insterThermometer(studentThermometer);

                    thermometerDialog.showCancelButton(false)
                            .setTitleText("测量完成")
                            .setContentText(contentText).changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    toastSpeak(mStudent.getSpeakStuName() + thermometer + "℃");
//
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            thermometerDialog.dismissWithAnimation();
                        }
                    }, 2000);

                    if (mStudent != null) {
                        LogUtils.operation("检入考生：" + mStudent.toString());
                        onCheckIn(mStudent);
                    }


                }


                break;
        }
    }

    private static class MyHandler extends Handler {

        private WeakReference<BaseCheckActivity> mReference;

        public MyHandler(BaseCheckActivity reference) {
            mReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            final BaseCheckActivity activity = mReference.get();
            if (activity == null) {
                return;
            }
            activity.handleMessage(msg);
        }
    }

    protected void showAddHint(final Student student) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                new SweetAlertDialog(BaseCheckActivity.this).setTitleText(getString(R.string.addStu_dialog_title))
                        .setContentText(getString(R.string.addStu_dialog_content))
                        .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        new AddStudentDialog(BaseCheckActivity.this).showDialog(student, false);
                    }
                }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                }).show();
            }
        });
    }

    protected void showThermometerDialog() {
        isStartThermometer = true;
        thermometerDialog = new SweetAlertDialog(BaseCheckActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.thermometer_dialog_title))
                .setContentText(getString(R.string.thermometer_dialog_msg))

                .setConfirmText(getString(R.string.cancel)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        isStartThermometer = false;
                        sweetAlertDialog.dismissWithAnimation();
                    }
                });
        thermometerDialog.show();
        thermometerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isStartThermometer = false;
            }
        });
    }

    private void showThermometerOpenDialog() {
        if (thermometerOpenDialog == null) {
            thermometerOpenDialog = new SweetAlertDialog(BaseCheckActivity.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.dialog_warm_prompt))
                    .setContentText(getString(R.string.thermometer_open_dialog_msg))

                    .setConfirmText("去连接").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            IntentUtil.gotoActivity(BaseCheckActivity.this, BlueToothListActivity.class);
                        }
                    }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
        }
        if (!thermometerOpenDialog.isShowing()) {
            thermometerOpenDialog.show();
        }
        thermometerOpenDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                thermometerOpenDialog = null;
            }
        });
    }

    public void showAdvancedPwdDialog() {

        View view = LayoutInflater.from(this).inflate(R.layout.view_advanced_pwd, null);
        final EditText editPwd = view.findViewById(R.id.edit_pwd);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);

        final Dialog dialog = DialogUtils.create(this, view, true);
        dialog.show();
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.equals(editPwd.getText().toString(), MyApplication.ADVANCED_PWD)) {
                    sendCheckHandlerMessage(mStudent);
                    dialog.dismiss();
                } else {
                    ToastUtils.showShort("密码错误");
                }
            }
        });
    }

    @Override
    public void onCommitGroup(Student student, GroupItem groupItem, List<RoundResult> results, int roundNo) {

    }
}
