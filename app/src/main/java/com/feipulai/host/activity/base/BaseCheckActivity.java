package com.feipulai.host.activity.base;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.feipulai.common.tts.TtsManager;
import com.feipulai.device.CheckDeviceOpener;
import com.feipulai.device.ic.ICCardDealer;
import com.feipulai.device.ic.NFCDevice;
import com.feipulai.device.ic.entity.StuInfo;
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.setting.SystemSetting;
import com.feipulai.host.config.BaseEvent;
import com.feipulai.host.config.EventConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Student;
import com.feipulai.host.entity.StudentItem;
import com.feipulai.host.view.AddStudentDialog;
import com.orhanobut.logger.Logger;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;

import java.lang.ref.WeakReference;

/**
 * Created by James on 2018/5/24 0024.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public abstract class BaseCheckActivity
        extends BaseTitleActivity
        implements CheckDeviceOpener.OnCheckDeviceArrived {

    private MyHandler mHandler = new MyHandler(this);
    private boolean isOpenDevice = true;
    private static final int STUDENT_CODE = 0x0;
    private static final int ID_CARD_NO = 0x1;
    private static final int CHECK_IN = 0x0;
    private boolean needAdd = true;

    public void setOpenDevice(boolean openDevice) {
        isOpenDevice = openDevice;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected int setLayoutResID() {
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

    @Override
    public void onICCardFound(NFCDevice nfcd) {
        long startTime = System.currentTimeMillis();
        ICCardDealer icCardDealer = new ICCardDealer(nfcd);
        StuInfo stuInfo = icCardDealer.IC_ReadStuInfo();

        if (stuInfo == null || TextUtils.isEmpty(stuInfo.getStuCode())) {
            TtsManager.getInstance().speak("读卡(ka3)失败");
            InteractUtils.toast(this, "读卡失败");
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
                toastSpeak("该考生不存在");
            }
            return needAdd;
        }

        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        if (studentItem == null) {
            if (needAdd) {
                registerStuItem(student);
                checkInUIThread(student);
            } else {
                toastSpeak("无此项目");
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
            toastSpeak("该考生不存在");
        } else {
            StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
            if (studentItem != null) {
                onCheckIn(student);
            } else {
                toastSpeak("无此项目");
            }
        }
    }

    private static class MyHandler extends Handler {

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
            switch (msg.what) {
                case CHECK_IN:
                    activity.onCheckIn((Student) msg.obj);
                    break;
            }
        }
    }

    private void showAddHint(final Student student) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(BaseCheckActivity.this)
                        .setCancelable(false)
                        .setTitle("提示")
                        .setMessage("无考生信息，是否新增")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new AddStudentDialog(BaseCheckActivity.this).showDialog(student, false);
                            }
                        })
                        .setNegativeButton("否", null)
                        .show();
            }
        });
    }
}
