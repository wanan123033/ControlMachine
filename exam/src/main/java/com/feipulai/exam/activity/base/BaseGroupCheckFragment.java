package com.feipulai.exam.activity.base;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.feipulai.common.tts.TtsManager;
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
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.jump_rope.view.StuSearchEditText;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;

/**
 * Created by James on 2018/12/10 0010.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BaseGroupCheckFragment
        extends Fragment
        implements StuSearchEditText.OnCheckedInListener,
        CheckDeviceOpener.OnCheckDeviceArrived, OnResultListener<RoundScoreBean> {

    private static final int CHECK_IN = 0x0;
    private static final int CHECK_THERMOMETER = 0x1;
    @BindView(R.id.et_select)
    StuSearchEditText mEtSelect;

    private ListView lvResults;
    private OnIndividualCheckInListener listener;

    private MyHandler mHandler = new MyHandler(this);

    private static final int STUDENT_CODE = 0x0;
    private static final int ID_CARD_NO = 0x1;

    private Student mStudent;
    private StudentItem mStudentItem;
    private List<RoundResult> mResults;
    private SystemSetting systemSetting;

    private BlueBindBean blueBindBean;
    private volatile boolean isStartThermometer = false;
    private SweetAlertDialog thermometerDialog;
    private SweetAlertDialog thermometerOpenDialog;
    private ResitDialog.onClickQuitListener onClickQuitListener = new ResitDialog.onClickQuitListener() {
        @Override
        public void onCancel() {

        }

        @Override
        public void onCommitPattern(Student student, StudentItem studentItem, List<RoundResult> results, int roundNo) {
            if (listener != null) {
                listener.onIndividualCheckIn(student, studentItem, results);
                listener.setRoundNo(student, roundNo);
            }
        }

        @Override
        public void onCommitGroup(Student student, GroupItem groupItem, List<RoundResult> results, int roundNo) {

        }
    };

    public void setResultView(ListView lvResults) {
        this.lvResults = lvResults;
    }

    public void setOnIndividualCheckInListener(OnIndividualCheckInListener listener) {
        this.listener = listener;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_base_group_check, container, false);
        ButterKnife.bind(this, view);
        mEtSelect.setResultView(lvResults);
        mEtSelect.setOnCheckedInListener(this);
        EventBus.getDefault().register(this);
        systemSetting = SharedPrefsUtil.loadFormSource(getActivity(), SystemSetting.class);
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
        return view;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return ScannerGunManager.getInstance().dispatchKeyEvent(event.getKeyCode(), event);

    }

    @Override
    public void onResume() {
        CheckDeviceOpener.getInstance().setQrLength(systemSetting.getQrLength());
        CheckDeviceOpener.getInstance().setOnCheckDeviceArrived(this);
        int checkTool = SettingHelper.getSystemSetting().getCheckTool();
        CheckDeviceOpener.getInstance().open(getActivity(), checkTool == SystemSetting.CHECK_TOOL_IDCARD,
                checkTool == SystemSetting.CHECK_TOOL_ICCARD,
                checkTool == SystemSetting.CHECK_TOOL_QR);
        // CheckDeviceOpener.getInstance().open(getActivity(), true, true, true);

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

    @Subscribe
    public void onEventMainThread(BaseEvent baseEvent) {
        if (baseEvent.getTagInt() == EventConfigs.TEMPORARY_ADD_STU && listener != null) {

            hideSoftInput();
            mStudent = (Student) baseEvent.getData();
            mStudentItem = DBManager.getInstance().queryStuItemByStuCode(mStudent.getStudentCode());
            if (SettingHelper.getSystemSetting().isStartThermometer()) {
                showThermometerDialog();
            } else {
                listener.onIndividualCheckIn(mStudent, mStudentItem, new ArrayList<RoundResult>());
            }
        }
    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mEtSelect.getWindowToken(), 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CheckDeviceOpener.getInstance().close();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        listener = null;
        //null.unbind();
    }


    private void checkInUIThread(Student student, StudentItem studentItem) {

        SystemSetting setting = SettingHelper.getSystemSetting();
        if (setting.isAutoScore()) {
            OperateProgressBar.showLoadingUi(getActivity(), "正在获取云端成绩...");
            HttpSubscriber subscriber = new HttpSubscriber();
            subscriber.getRoundResult(setting.getSitCode(), studentItem.getScheduleNo(), TestConfigs.getCurrentItemCode(), student.getStudentCode(),
                    null, null, null, String.valueOf(studentItem.getExamType()), this);
        } else {
            sendCheckHandlerMessage(student);
        }

    }

    @Override
    public boolean onInputCheck(Student student) {
        boolean needAdd = checkQulification(student.getStudentCode(), STUDENT_CODE);
        if (needAdd) {
            showAddHint(student);
        }
        return true;
    }

    @Override
    public void onICCardFound(NFCDevice nfcd) {
        ICCardDealer icCardDealer = new ICCardDealer(nfcd);
        StuInfo stuInfo = icCardDealer.IC_ReadStuInfo();

        if (stuInfo == null || TextUtils.isEmpty(stuInfo.getStuCode())) {
            TtsManager.getInstance().speak("读卡(ka3)失败");
            InteractUtils.toast(getActivity(), "读卡失败");
            return;
        }

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
            student.setIdCardNo(idCardInfo.getId());
            student.setStudentName(idCardInfo.getName());
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
        // InteractUtils.toast(getActivity(), "条码/二维码长度不正确,长度:" + length + "\t预期长度:" + expectLength);
        InteractUtils.toast(getActivity(), "条码与当前设置位数不一致,请重扫条码");
    }

    // 可能在子线程运行
    // 返回是否需要新增考生
    private boolean checkQulification(String code, int flag) {
        Student student = null;
        switch (flag) {

            case ID_CARD_NO:
                student = DBManager.getInstance().queryStudentByIDCode(code);
                break;

            case STUDENT_CODE:
                student = DBManager.getInstance().queryStudentByStuCode(code);
                break;

        }
        if (student == null) {
            InteractUtils.toastSpeak(getActivity(), "该考生不存在");
            return false;
        }
        final StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        if (studentItem == null) {
            InteractUtils.toastSpeak(getActivity(), "无此项目");
            return false;
        }
        final List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
        if (results != null && results.size() >= TestConfigs.getMaxTestCount()) {
            SystemSetting setting = SettingHelper.getSystemSetting();
            if (setting.isAgainTest() && setting.isResit()) {
                final Student finalStudent = student;
                new SweetAlertDialog(getContext()).setContentText("需要重测还是补考呢?")
                        .setCancelText("重测")
                        .setConfirmText("补考")
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                AgainTestDialog dialog = new AgainTestDialog();
                                dialog.setArguments(finalStudent, results, studentItem);
                                dialog.setOnIndividualCheckInListener(onClickQuitListener);
                                dialog.show(getActivity().getSupportFragmentManager(), "AgainTestDialog");
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                ResitDialog dialog = new ResitDialog();
                                dialog.setArguments(finalStudent, results, studentItem);
                                dialog.setOnIndividualCheckInListener(onClickQuitListener);
                                dialog.show(getActivity().getSupportFragmentManager(), "ResitDialog");
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        }).show();
                return false;
            }
            if (setting.isAgainTest()) {
                AgainTestDialog dialog = new AgainTestDialog();
                dialog.setArguments(student, results, studentItem);
                dialog.setOnIndividualCheckInListener(onClickQuitListener);
                dialog.show(getActivity().getSupportFragmentManager(), "AgainTestDialog");
                return false;
            }
            if (setting.isResit()) {
                ResitDialog dialog = new ResitDialog();
                dialog.setArguments(student, results, studentItem);
                dialog.setOnIndividualCheckInListener(onClickQuitListener);
                dialog.show(getActivity().getSupportFragmentManager(), "ResitDialog");
            } else {
                InteractUtils.toastSpeak(getActivity(), "该考生已测试");
            }
            return false;
        }
        mStudent = student;
        mStudentItem = studentItem;
        mResults = results;
        // 可以直接检录
        //TODO 考虑单机测试的开关是否开启
        checkInUIThread(student, studentItem);
        return false;
    }

    @Override
    public void onResponseTime(String responseTime) {

    }

    @Override
    public void onSuccess(RoundScoreBean result) {
        OperateProgressBar.removeLoadingUiIfExist(getActivity());
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
                new AlertDialog.Builder(getActivity())
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

    private void sendCheckHandlerMessage(Student mStudent) {
        Message msg = Message.obtain();
        msg.what = CHECK_IN;
        msg.obj = mStudent;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onFault(int code, String errorMsg) {
        OperateProgressBar.removeLoadingUiIfExist(getActivity());
        ToastUtils.showLong(errorMsg);
        sendCheckHandlerMessage(mStudent);
    }

    private static class MyHandler extends Handler {

        private WeakReference<BaseGroupCheckFragment> mReference;

        public MyHandler(BaseGroupCheckFragment reference) {
            mReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            final BaseGroupCheckFragment fragment = mReference.get();
            if (fragment == null) {
                return;
            }
            switch (msg.what) {
                case CHECK_IN:


                    if (SettingHelper.getSystemSetting().isStartThermometer()) {
                        StudentThermometer thermometer = DBManager.getInstance().getThermometer(fragment.mStudentItem);
                        if (thermometer == null) {
                            fragment.showThermometerDialog();
                        } else {
                            fragment.check();
                        }

                    } else {
                        fragment.check();
                    }
                    break;
                case CHECK_THERMOMETER:

                    byte[] value = (byte[]) msg.obj;
                    LogUtil.logDebugMessage("蓝牙返回数据===》" + fragment.isStartThermometer);
                    if (fragment.isStartThermometer == true) {
                        LogUtil.logDebugMessage("蓝牙返回数据校验===》" + StringChineseUtil.byteToString(value));
                        if (value.length < 4) {
                            //|| value[1] + value[2] != value[3]
                            InteractUtils.toastSpeak(fragment.getActivity(), "体温枪异常，请再次测量体温");
                            return;
                        }

                        String getThermometer = Long.parseLong(String.format("%02X", value[1]) + String.format("%02X", value[2]), 16) + "";
                        if (getThermometer.length() < 3) {
                            InteractUtils.toastSpeak(fragment.getActivity(), "请规范使用体温枪重新测量");
                            return;
                        }
                        String thermometer = getThermometer.substring(0, 2) + "." + getThermometer.substring(2);
                        LogUtil.logDebugMessage("蓝牙返回数据===》" + thermometer);
                        fragment.isStartThermometer = false;
                        String contentText = fragment.mStudent.getStudentName() + ":" + thermometer + "℃";
                        //添加体温记录
                        StudentThermometer studentThermometer = new StudentThermometer();
                        studentThermometer.setStudentCode(fragment.mStudent.getStudentCode());
                        studentThermometer.setExamType(fragment.mStudentItem.getExamType());
                        studentThermometer.setThermometer(Double.valueOf(thermometer));
                        studentThermometer.setItemCode(TestConfigs.getCurrentItemCode());
                        studentThermometer.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
                        studentThermometer.setMeasureTime(DateUtil.getCurrentTime() + "");
                        DBManager.getInstance().insterThermometer(studentThermometer);

                        fragment.thermometerDialog.showCancelButton(false)
                                .setTitleText("测量完成")
                                .setContentText(contentText).changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        InteractUtils.toastSpeak(fragment.getActivity(), fragment.mStudent.getSpeakStuName() + thermometer + "℃");

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                fragment.thermometerDialog.dismissWithAnimation();
                            }
                        }, 2000);

                        fragment.check();


                    }


                    break;
            }
        }
    }

    private void check() {
        if (listener != null) {
            hideSoftInput();
            if (mStudent != null) {
                LogUtils.operation("检入考生：" + mStudent.toString());
            }
            if (mResults == null) {
                mResults = new ArrayList<>();
            }
            listener.onIndividualCheckIn(mStudent, mStudentItem, mResults);
        }
    }

    private void showAddHint(final Student student) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                new SweetAlertDialog(getActivity()).setTitleText(getString(R.string.addStu_dialog_title))
                        .setContentText(getString(R.string.addStu_dialog_content))
                        .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        new AddStudentDialog(getActivity()).showDialog(student, false);
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

    private void showThermometerDialog() {
        isStartThermometer = true;
        thermometerDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
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
            thermometerOpenDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.dialog_warm_prompt))
                    .setContentText(getString(R.string.thermometer_open_dialog_msg))

                    .setConfirmText("去连接").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            IntentUtil.gotoActivity(getActivity(), BlueToothListActivity.class);
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

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_advanced_pwd, null);
        final EditText editPwd = view.findViewById(R.id.edit_pwd);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);

        final Dialog dialog = DialogUtils.create(getActivity(), view, true);
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

    public interface OnIndividualCheckInListener {
        /**
         * 这里传入的考生信息均通过了验证
         * 检录的考生----对应的报名信息-----报名信息对应的成绩
         */
        void onIndividualCheckIn(Student student, StudentItem studentItem, List<RoundResult> results);

        void setRoundNo(Student student, int roundNo);
    }

}
