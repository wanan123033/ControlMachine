package com.feipulai.host.activity.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.feipulai.common.utils.ScannerGunManager;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.CheckDeviceOpener;
import com.feipulai.device.ic.ICCardDealer;
import com.feipulai.device.ic.NFCDevice;
import com.feipulai.device.ic.entity.StuInfo;
import com.feipulai.host.R;
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.setting.SystemSetting;
import com.feipulai.host.config.BaseEvent;
import com.feipulai.host.config.EventConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.entity.StudentItem;
import com.feipulai.host.view.AddStudentDialog;
import com.orhanobut.logger.Logger;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;

import java.lang.ref.WeakReference;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by James on 2018/5/24 0024.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public abstract class BaseCheckActivity
        extends BaseTitleActivity
        implements CheckDeviceOpener.OnCheckDeviceArrived, BaseAFRFragment.onAFRCompareListener {

    private MyHandler mHandler = new MyHandler(this);
    private boolean isOpenDevice = true;
    private static final int STUDENT_CODE = 0x0;
    private static final int ID_CARD_NO = 0x1;
    private static final int CHECK_IN = 0x0;
    private boolean needAdd = true;
    public FrameLayout afrFrameLayout;
    private BaseAFRFragment afrFragment;
    private ScannerGunManager scannerGunManager;

    public void setOpenDevice(boolean openDevice) {
        isOpenDevice = openDevice;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        if (SettingHelper.getSystemSetting().getCheckTool() == 4 && setAFRFrameLayoutResID() != 0) {
            afrFrameLayout = findViewById(setAFRFrameLayoutResID());
            afrFragment = new BaseAFRFragment();
            afrFragment.setCompareListener(this);
            initAFR();
        }
        scannerGunManager = new ScannerGunManager(new ScannerGunManager.OnScanListener() {
            @Override
            public void onResult(String code) {
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
        if (scannerGunManager != null && scannerGunManager.dispatchKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected int setLayoutResID() {
        return 0;
    }

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
        super.onResume();
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
    public void compareStu(Student student) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                afrFrameLayout.setVisibility(View.GONE);
            }
        });
        if (student == null) {
            InteractUtils.toastSpeak(this, "该考生不存在");
            return;
        }
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        if (studentItem == null) {
            InteractUtils.toastSpeak(this, "无此项目");
            return;
        }
        // 可以直接检录
        checkInUIThread(student);
    }

    @Override
    public void onICCardFound(NFCDevice nfcd) {
        long startTime = System.currentTimeMillis();
        ICCardDealer icCardDealer = new ICCardDealer(nfcd);
        StuInfo stuInfo = icCardDealer.IC_ReadStuInfo();

        if (stuInfo == null || TextUtils.isEmpty(stuInfo.getStuCode())) {
//            TtsManager.getInstance().speak("读卡(ka3)失败");
//            InteractUtils.toast(this, "读卡失败");
            toastSpeak(getString(R.string.read_iccard_failed));
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
        InteractUtils.toast(this, getString(R.string.qr_length_error));
    }

    protected void setAddable(boolean needAdd) {
        this.needAdd = needAdd;
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
            if (!needAdd) {
                toastSpeak(getString(R.string.student_nonentity));
            }
            return needAdd;
        }

        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        if (studentItem == null) {
            if (needAdd) {
                registerStuItem(student);
                checkInUIThread(student);
            } else {
                toastSpeak(getString(R.string.no_project));
            }
            return false;
        } else {
            checkInUIThread(student);
            return false;
        }
    }

    // 为学生报名项目
    private void registerStuItem(Student student) {
        StudentItem studentItem = new StudentItem();
        studentItem.setStudentCode(student.getStudentCode());
        String itemCode = TestConfigs.sCurrentItem.getItemCode();
        studentItem.setItemCode(itemCode == null ? TestConfigs.DEFAULT_ITEM_CODE : itemCode);
        studentItem.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        DBManager.getInstance().insertStudentItem(studentItem);
    }

    private void checkInUIThread(Student student) {
        Message msg = Message.obtain();
        msg.what = CHECK_IN;
        msg.obj = student;
        mHandler.sendMessage(msg);
    }


    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.TEMPORARY_ADD_STU) {
            onCheckIn((Student) baseEvent.getData());
        }
    }

    /**
     * 真正的有考生成功的检录进来时调用,这里不需要再验证考生信息了
     * 该方法的调用就表示了这个人可以测试了
     */
    public abstract void onCheckIn(Student student);


    public void checkInput(Student student) {
        if (student == null) {
            toastSpeak(getString(R.string.student_nonentity));
        } else {
            StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
            if (studentItem != null) {
                onCheckIn(student);
            } else {
                toastSpeak(getString(R.string.no_project));
            }
        }
    }

    protected static class MyHandler extends Handler {

        private WeakReference<BaseCheckActivity> mReference;

        public MyHandler(BaseCheckActivity reference) {
            mReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseCheckActivity activity = mReference.get();
            if (activity == null) {
                return;
            }
            activity.handlerMessage(msg);
        }
    }

    protected void handlerMessage(Message msg) {
        switch (msg.what) {
            case CHECK_IN:
                onCheckIn((Student) msg.obj);
                break;
        }
    }

    private void showAddHint(final Student student) {
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

//                new AlertDialog.Builder(BaseCheckActivity.this)
//                        .setCancelable(false)
//                        .setTitle("提示")
//                        .setMessage("无考生信息，是否新增")
//                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                            }
//                        })
//                        .setNegativeButton("否", null)
//                        .show();
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (afrFragment != null && afrFragment.isOpenCamera) {
                showAFR();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        } else { // 如果不是back键正常响应
            return super.onKeyDown(keyCode, event);
        }
    }
}
